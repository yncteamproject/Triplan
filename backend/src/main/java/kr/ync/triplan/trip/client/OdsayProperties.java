package kr.ync.triplan.trip.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// 오디세이(대중교통 길찾기) 설정. api-key는 application-secret.yaml의 odsay.api-key
// 키가 없어도 서버는 뜨고, 경로 조회를 할 때만 502로 실패한다 (키가 없는 팀원도 다른 기능은 쓸 수 있도록)
@Setter
@Getter
@Configuration
@ConfigurationProperties("odsay")
public class OdsayProperties {
	private String apiKey;
	private String baseUrl = "https://api.odsay.com/v1/api";
}
