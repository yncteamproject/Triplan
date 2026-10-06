package kr.ync.triplan.global.config.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class TokenProvider {

	private final JwtProperties jwtProperties;
	private SecretKey key;

	// 비밀키는 Base64 문자열로 받아서 디코딩한다 (application-secret.yaml의 jwt.secret_key)
	@PostConstruct
	void init() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	public String createToken(String email, String role) {
		Date now = new Date();
		Date expiry = new Date(now.getTime()
				+ Duration.ofMinutes(jwtProperties.getAccessExpirationMinutes()).toMillis());
		return Jwts.builder()
				.header().type("JWT")
				.and()
				.issuer(jwtProperties.getIssuer())
				.issuedAt(now)
				.expiration(expiry)
				.subject(email)
				.claim("role", role)
				.signWith(key)
				.compact();
	}

	public String getEmail(String token) {
		return Jwts.parser().verifyWith(key).build()
				.parseSignedClaims(token).getPayload().getSubject();
	}

	public boolean validate(String token) {
		try {
			Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}