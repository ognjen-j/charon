package ognjenj.charon.web.repositories;

import ognjenj.charon.web.model.radius.RadUserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RadUserGroupRepository extends JpaRepository<RadUserGroup, RadUserGroup.RadUserGroupId> {

}
