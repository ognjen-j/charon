package ognjenj.charon.web.config;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import ognjenj.charon.web.services.UserService;
import ognjenj.charon.web.util.SaltGenerator;

@Configuration
@EnableWebSecurity
public class SecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
	@Autowired
	UserService userService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/ovpn/connections", "/", "/index", "/password").authenticated()
				.antMatchers("/ovpn/**").hasAnyRole("OVPN", "ROLE_OVPN").antMatchers("/ca/**")
				.hasAnyRole("CA", "ROLE_CA").and().authorizeRequests().antMatchers("/login**").permitAll().and()
				.formLogin().loginPage("/login").loginProcessingUrl("/loginAction").defaultSuccessUrl("/index")
				.permitAll().and().logout().logoutSuccessUrl("/login").permitAll().and().exceptionHandling()
				.accessDeniedPage("/error/denied").and().sessionManagement().maximumSessions(1)
				.expiredUrl("/login?expired");
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService).passwordEncoder(sha512PasswordEncoder());
	}

	@Bean
	public PasswordEncoder sha512PasswordEncoder() {
		return new PasswordEncoder() {
			@Override
			public String encode(CharSequence charSequence) {
				try {
					return SaltGenerator.generateSha512asHex(charSequence.toString());
				} catch (NoSuchAlgorithmException ignored) {
					return null;
				}
			}

			@Override
			public boolean matches(CharSequence rawPassword, String encodedPassword) {
				try {
					return SaltGenerator.generateSha512asHex(rawPassword.toString()).equals(encodedPassword);
				} catch (NoSuchAlgorithmException ignored) {
					return false;
				}
			}
		};
	}

	@Bean
	public FilterRegistrationBean<PasswordChangeFilter> registerFilter() {
		FilterRegistrationBean<PasswordChangeFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new PasswordChangeFilter());
		registrationBean.addUrlPatterns("/ca/*", "/ovpn/*");
		return registrationBean;
	}
}
