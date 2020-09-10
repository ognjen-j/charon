package ognjenj.charon.web.config;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ognjenj.charon.web.model.User;

@Component
public class PasswordChangeFilter implements Filter {
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) auth.getPrincipal();
		if (user.isForcePasswordChange()) {
			((HttpServletResponse) servletResponse).sendRedirect("/password");
		} else {
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}
}
