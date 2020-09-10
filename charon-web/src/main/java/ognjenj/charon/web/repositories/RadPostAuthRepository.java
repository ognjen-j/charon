package ognjenj.charon.web.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ognjenj.charon.web.model.radius.RadPostAuth;

public interface RadPostAuthRepository extends JpaRepository<RadPostAuth, Long> {
	@Query("SELECT a FROM RadPostAuth a WHERE authdate>=:authdatefrom AND authdate<=:authdateto "
			+ "ORDER BY authdate DESC")
	List<RadPostAuth> getByDate(@Param("authdatefrom") LocalDateTime authTimeFrom,
			@Param("authdateto") LocalDateTime authTimeTo);
}
