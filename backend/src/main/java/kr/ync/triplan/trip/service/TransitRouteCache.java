package kr.ync.triplan.trip.service;

import kr.ync.triplan.trip.dto.response.TransitRouteResponse.Route;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

// 같은 출발 · 도착 좌표의 경로 조회 결과를 서버 메모리에 보관한다 (오디세이 무료 호출이 하루 30건이라 절약용)
// 서버를 다시 시작하면 지워진다. 실패한 조회는 저장하지 않는다
@Component
public class TransitRouteCache {

	// 메모리가 끝없이 늘지 않도록, 이 개수를 넘으면 한 번 비운다
	private static final int MAX_SIZE = 500;

	private final Map<String, List<Route>> cache = new ConcurrentHashMap<>();

	public List<Route> get(double startX, double startY, double endX, double endY, Supplier<List<Route>> loader) {
		String key = startX + "," + startY + "->" + endX + "," + endY;
		List<Route> cached = cache.get(key);
		if (cached != null) {
			return cached;
		}
		List<Route> routes = loader.get();
		if (cache.size() >= MAX_SIZE) {
			cache.clear();
		}
		cache.put(key, routes);
		return routes;
	}

	public void clear() {
		cache.clear();
	}
}
