package ognjenj.charon.web.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ognjenj.charon.web.model.radius.UserRole;

import javax.transaction.Transactional;
import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
	@Transactional
	long deleteByUsername(String username);
	List<UserRole> getByUsername(String username);
}
