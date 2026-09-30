package kr.ync.triplan.member.repository;

import kr.ync.triplan.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// 마이페이지 전용: 공통 Member 엔티티를 고치지 않고 닉네임/비밀번호를 쿼리로 수정
public interface MyPageMemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByEmail(String email);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update Member m set m.nickname = :nickname where m.id = :id")
	int updateNickname(@Param("id") Long id, @Param("nickname") String nickname);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update Member m set m.password = :password where m.id = :id")
	int updatePassword(@Param("id") Long id, @Param("password") String encodedPassword);
}
