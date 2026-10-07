import styles from "./ComingSoon.module.css";

// 아직 만들지 않은 화면의 자리 표시. 화면을 만들 때 이 부품을 지우고 내용을 채운다
export default function ComingSoon({ title, figma, apis = [] }) {
	return (
		<section className={styles.wrap}>
			<h2 className={styles.title}>{title}</h2>
			<p className={styles.badge}>준비 중인 화면이에요</p>
			<dl className={styles.info}>
				<dt>피그마</dt>
				<dd>{figma}</dd>
				{apis.length > 0 && (
					<>
						<dt>쓰는 API</dt>
						<dd>
							{apis.map((api) => (
								<code key={api} className={styles.api}>
									{api}
								</code>
							))}
						</dd>
					</>
				)}
			</dl>
		</section>
	);
}
