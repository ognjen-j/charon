package ognjenj.charon.web.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ognjenj.charon.web.model.radius.UserCert;

@Repository
public interface UserCertRepository extends JpaRepository<UserCert, Long> {
	@Query("SELECT c FROM UserCert c WHERE is_revoked=0 AND expiration_date>NOW()")
	List<UserCert> getActive();
	@Query("SELECT c FROM UserCert c WHERE is_revoked=1 AND expiration_date>NOW()")
	List<UserCert> getRevokedNonExpired();
	@Query("SELECT c FROM UserCert c WHERE expiration_date<NOW() AND is_revoked=0")
	List<UserCert> getExpired();
	UserCert getByCertSerial(String certSerial);
	List<UserCert> getByCommonName(String commonName);
}
