package ognjenj.charon.web.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ognjenj.charon.web.model.radius.RadAcct;

@Repository
public interface RadAcctRepository extends JpaRepository<RadAcct, Long> {
	@Query("SELECT a FROM RadAcct a WHERE (username like :username or :username = null) "
			+ "AND acctstarttime>=:acctstarttime "
			+ "AND (acctstoptime is null OR :acctstoptime = null OR acctstoptime<=:acctstoptime) "
			+ "ORDER BY acctstarttime DESC")
	List<RadAcct> getByUsernameBetweenDates(@Param("acctstarttime") LocalDateTime acctStartTime,
			@Param("acctstoptime") LocalDateTime acctStopTime, @Param("username") String username);
}
