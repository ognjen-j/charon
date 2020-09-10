package ognjenj.charon.web.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ognjenj.charon.web.model.radius.RadCheck;

@Repository
public interface RadCheckRepository extends JpaRepository<RadCheck, Integer> {
	List<RadCheck> findByUsername(String username);
	List<RadCheck> findByUsernameAndAttribute(String username, String attribute);
}
