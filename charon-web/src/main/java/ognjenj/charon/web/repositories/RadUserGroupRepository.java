package ognjenj.charon.web.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ognjenj.charon.web.model.radius.RadUserGroup;

@Repository
public interface RadUserGroupRepository extends JpaRepository<RadUserGroup, RadUserGroup.RadUserGroupId> {

}
