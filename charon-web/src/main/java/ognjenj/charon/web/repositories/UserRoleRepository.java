package ognjenj.charon.web.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import ognjenj.charon.web.model.radius.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
	@Transactional
	long deleteByUsername(String username);
	List<UserRole> getByUsername(String username);
}
