package ognjenj.charon.web.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class User implements Serializable, UserDetails {
	private String username;
	private LocalDateTime expirationDate;
	private boolean active;
	private Set<String> roles = new HashSet<>();
	private List<String> routes = new ArrayList<>();
	private Certificate activeCertificate = null;
	private boolean forcePasswordChange = false;
	private String password;

	public boolean isForcePasswordChange() {
		return forcePasswordChange;
	}

	public void setForcePasswordChange(boolean forcePasswordChange) {
		this.forcePasswordChange = forcePasswordChange;
	}

	public List<String> getRoutes() {
		return routes;
	}

	public void setRoutes(List<String> routes) {
		this.routes = routes;
	}

	public Certificate getActiveCertificate() {
		return activeCertificate;
	}

	public void setActiveCertificate(Certificate activeCertificate) {
		this.activeCertificate = activeCertificate;
	}

	public LocalDateTime getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(LocalDateTime expirationDate) {
		this.expirationDate = expirationDate;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.roles.stream().map(e -> new SimpleGrantedAuthority("ROLE_" + e)).collect(Collectors.toSet());
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.expirationDate == null || this.expirationDate.isAfter(LocalDateTime.now());
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.active;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.expirationDate == null || this.expirationDate.isAfter(LocalDateTime.now());
	}

	@Override
	public boolean isEnabled() {
		return this.active;
	}
}
