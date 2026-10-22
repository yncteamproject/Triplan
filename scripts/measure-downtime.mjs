// 배포하는 동안 API가 얼마나 끊기는지 재는 스크립트 (#92)
//
// 1초에 정해진 횟수만큼 가벼운 읽기 요청(GET 게시글 목록 1개)을 보내고,
// 실패한 요청 수와 "가장 길게 끊긴 시간"을 기록한다. 배포 방식(재시작 · 블루-그린)을 같은 조건으로 비교하는 데 쓴다.
//
// 쓰는 법 (저장소 맨 위 폴더, Node 22):
//   node scripts/measure-downtime.mjs --label restart-1
//   → 측정이 시작되면 다른 창에서 배포를 실행하고, 배포가 끝나 정상 응답이 이어지면 Ctrl + C
//   → 끝나면 요약을 보여주고 measurements/ 폴더에 결과(JSON)를 남긴다
//
// 옵션:
//   --url       측정할 주소 (기본: 배포 서버의 게시글 목록 1개)
//   --rps       1초에 보낼 요청 수 (기본 2). 서버가 1초에 처리할 수 있는 양보다 적어야 배포 때문에 끊긴 것만 잡힌다
//   --duration  자동으로 끝낼 시간(초). 0이면 Ctrl + C로 끝낼 때까지 (기본 0)
//   --timeout   요청 하나를 기다리는 시간(밀리초). 넘으면 실패로 센다 (기본 5000)
//   --label     결과 파일 이름에 붙일 이름 (예: restart-1, blue-green-2)
//
// 주의: 조회수가 오르지 않는 목록 API만 부른다. 운영 서버에 부담을 주지 않게 rps를 크게 올리지 않는다.
//   2026-10-22 측정: 목록 API는 요청 하나에 서버 안에서 약 1.6초(서버 시드니 ↔ DB 싱가포르 왕복 × 쿼리 수),
//   DB 연결 10개 기준 1초에 약 6건이 한계였다. 10건씩 보내면 측정이 과부하를 만들어 배포와 상관없는 타임아웃이 대부분이 된다.
//   502는 "앞단(Caddy · Nginx)은 살아 있는데 백엔드가 꺼짐"이라 배포로 인한 중단만 보여준다.

import fs from "fs";
import path from "path";

const args = Object.fromEntries(
	process.argv.slice(2).reduce((pairs, arg, index, list) => {
		if (arg.startsWith("--")) {
			pairs.push([arg.slice(2), list[index + 1]]);
		}
		return pairs;
	}, []),
);

const URL_TO_CHECK = args.url ?? "https://triplan-ync.duckdns.org/api/share-pages?size=1";
const RPS = Number(args.rps ?? 2);
const DURATION_SECONDS = Number(args.duration ?? 0);
const TIMEOUT_MS = Number(args.timeout ?? 5000);
const LABEL = args.label ?? "measure";

if (!(RPS > 0 && RPS <= 20)) {
	console.error("--rps는 1~20 사이로 넣어주세요. (운영 서버에 부담을 주지 않게)");
	process.exit(1);
}

const results = []; // { at: 보낸 시각(ms), ok, status, ms: 걸린 시간, error }
const startedAt = Date.now();
let sent = 0;
let finished = false;

async function hit() {
	const at = Date.now();
	try {
		const response = await fetch(URL_TO_CHECK, { signal: AbortSignal.timeout(TIMEOUT_MS), cache: "no-store" });
		// 본문까지 받아야 정말 응답이 온 것으로 본다
		await response.arrayBuffer();
		results.push({ at, ok: response.ok, status: response.status, ms: Date.now() - at });
	} catch (error) {
		const reason = error.name === "TimeoutError" ? "timeout" : (error.cause?.code ?? error.name);
		results.push({ at, ok: false, status: 0, ms: Date.now() - at, error: reason });
	}
}

// 요청을 일정한 간격으로 보낸다. 앞 요청을 기다리지 않아서, 서버가 멈춰도 간격은 그대로 유지된다
const interval = setInterval(() => {
	sent += 1;
	hit();
}, 1000 / RPS);

// 1초마다 지금까지의 성공 · 실패를 한 줄로 보여준다
const progress = setInterval(() => {
	const done = results.length;
	const failed = results.filter((result) => !result.ok).length;
	const last = results.at(-1);
	const state = last ? (last.ok ? "정상" : `실패(${last.status || last.error})`) : "-";
	const elapsed = Math.round((Date.now() - startedAt) / 1000);
	process.stdout.write(`\r${elapsed}초 · 보냄 ${sent} · 받음 ${done} · 실패 ${failed} · 마지막 ${state}      `);
}, 1000);

function percentile(values, p) {
	if (values.length === 0) {
		return 0;
	}
	const sorted = [...values].sort((a, b) => a - b);
	return sorted[Math.min(sorted.length - 1, Math.ceil((p / 100) * sorted.length) - 1)];
}

// 연속으로 실패한 구간 중 가장 긴 것: 마지막 성공 뒤부터 다음 성공 전까지의 시간
function longestOutage(sortedResults) {
	let longest = { ms: 0, from: null, to: null, count: 0 };
	let runStart = null;
	let runCount = 0;
	let lastOkAt = null;
	for (const result of sortedResults) {
		if (result.ok) {
			if (runStart !== null) {
				const from = lastOkAt ?? runStart;
				const ms = result.at - from;
				if (ms > longest.ms) {
					longest = { ms, from, to: result.at, count: runCount };
				}
				runStart = null;
				runCount = 0;
			}
			lastOkAt = result.at;
		} else {
			runStart ??= result.at;
			runCount += 1;
		}
	}
	// 끝날 때까지 실패가 이어졌으면 측정이 끝난 시각까지를 센다
	if (runStart !== null) {
		const from = lastOkAt ?? runStart;
		const ms = Date.now() - from;
		if (ms > longest.ms) {
			longest = { ms, from, to: null, count: runCount };
		}
	}
	return longest;
}

async function finish() {
	if (finished) {
		return;
	}
	finished = true;
	clearInterval(interval);
	clearInterval(progress);
	// 보낸 요청이 모두 끝날 때까지 잠깐 기다린다
	const waitUntil = Date.now() + TIMEOUT_MS + 500;
	while (results.length < sent && Date.now() < waitUntil) {
		await new Promise((resolve) => setTimeout(resolve, 100));
	}

	const sorted = [...results].sort((a, b) => a.at - b.at);
	const failures = sorted.filter((result) => !result.ok);
	const okTimes = sorted.filter((result) => result.ok).map((result) => result.ms);
	const byReason = {};
	for (const failure of failures) {
		const reason = failure.status ? `HTTP ${failure.status}` : failure.error;
		byReason[reason] = (byReason[reason] ?? 0) + 1;
	}
	const outage = longestOutage(sorted);
	const time = (ms) => (ms === null ? "측정 끝까지" : new Date(ms).toLocaleTimeString("ko-KR", { hour12: false }));

	const summary = {
		label: LABEL,
		url: URL_TO_CHECK,
		rps: RPS,
		startedAt: new Date(startedAt).toISOString(),
		seconds: Math.round((Date.now() - startedAt) / 1000),
		total: sorted.length,
		failed: failures.length,
		failureRate: sorted.length ? Number(((failures.length / sorted.length) * 100).toFixed(2)) : 0,
		failuresByReason: byReason,
		longestOutageSeconds: Number((outage.ms / 1000).toFixed(1)),
		longestOutageFrom: outage.from ? new Date(outage.from).toISOString() : null,
		longestOutageTo: outage.to ? new Date(outage.to).toISOString() : null,
		latencyMs: { p50: percentile(okTimes, 50), p95: percentile(okTimes, 95), max: Math.max(0, ...okTimes) },
	};

	console.log("\n");
	console.log(`=== 측정 결과: ${LABEL} ===`);
	console.log(`측정 시간      ${summary.seconds}초 (1초에 ${RPS}번)`);
	console.log(`요청           ${summary.total}건`);
	console.log(`실패           ${summary.failed}건 (${summary.failureRate}%)`);
	for (const [reason, count] of Object.entries(byReason)) {
		console.log(`  - ${reason}: ${count}건`);
	}
	console.log(`가장 긴 끊김   ${summary.longestOutageSeconds}초${outage.ms ? ` (${time(outage.from)} ~ ${time(outage.to)}, 실패 ${outage.count}건)` : ""}`);
	console.log(`응답 시간      p50 ${summary.latencyMs.p50}ms · p95 ${summary.latencyMs.p95}ms · 최대 ${summary.latencyMs.max}ms (성공한 요청)`);

	const dir = path.resolve("measurements");
	fs.mkdirSync(dir, { recursive: true });
	const stamp = new Date(startedAt).toISOString().replace(/[:.]/g, "-").slice(0, 19);
	const file = path.join(dir, `${stamp}-${LABEL}.json`);
	fs.writeFileSync(file, JSON.stringify({ summary, requests: sorted }, null, 2));
	console.log(`\n결과 파일: ${path.relative(process.cwd(), file)}`);
	process.exit(0);
}

process.on("SIGINT", finish);
if (DURATION_SECONDS > 0) {
	setTimeout(finish, DURATION_SECONDS * 1000);
}
console.log(`측정 시작: ${URL_TO_CHECK} (1초에 ${RPS}번)${DURATION_SECONDS ? `, ${DURATION_SECONDS}초 뒤 자동 종료` : ", 끝내려면 Ctrl + C"}`);
