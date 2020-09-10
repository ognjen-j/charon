package ognjenj.charon.web.model.radius;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.*;

@Entity
@Table(name = "user_role")
public class UserRole implements Serializable {
	@Column(name = "username", nullable = false)
	String username;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_role_id", nullable = false, unique = true)
	private Long id;
	@Column(name = "role_name", nullable = false)
	private String roleName;

	public UserRole() {

	}

	public UserRole(String username, String roleName) {
		this.username = username;
		this.roleName = roleName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserRole userRole = (UserRole) o;
		return username.equals(userRole.username) && roleName.equals(userRole.roleName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, roleName);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}
