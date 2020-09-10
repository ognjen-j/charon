package ognjenj.charon.web.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ognjenj.charon.web.model.radius.RadReply;

import javax.transaction.Transactional;

@Repository
public interface RadReplyRepository extends JpaRepository<RadReply, Long> {
	List<RadReply> findByUsername(String username);
	@Query("SELECT a FROM RadReply a WHERE attribute=:attributename AND value=:value")
	List<RadReply> findByAttributeAndValue(@Param("attributename") String attributeName, @Param("value") String value);
	@Transactional
	long deleteByUsernameAndAttribute(String username, String attribute);
}
