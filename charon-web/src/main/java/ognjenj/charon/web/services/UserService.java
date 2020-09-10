package ognjenj.charon.web.services;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.digest.Crypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import ognjenj.charon.web.config.ConfigurationStore;
import ognjenj.charon.web.model.User;
import ognjenj.charon.web.model.radius.RadCheck;
import ognjenj.charon.web.model.radius.UserRole;
import ognjenj.charon.web.repositories.RadCheckRepository;
import ognjenj.charon.web.repositories.UserRoleRepository;

@Service
public class UserService implements UserDetailsService {
	public static final String PASSWORD_ATTRIBUTE_NAME = "Cleartext-Password";
	public static final String EXPIRATION_ATTRIBUTE_NAME = "Expiration";
	public static final String CONTROL_ATTRIBUTE_OPERATOR = ":=";
	public static final String ROUTE_ATTRIBUTE_OPERATOR = "+=";
	public static final String ROUTE_ATTRIBUTE_NAME = "Framed-Route";
	public static final String STATIC_ADDRESS_ATTRIBUTE_NAME = "Framed-IP-Address";
	public static final String STATIC_ADDRESS_ATTRIBUTE_OPERATOR = "=";
	public static final String DEFAULT_ROUTE = "0.0.0.0/0";
	@Autowired
	RadCheckRepository radCheckRepository;
	@Autowired
	UserRoleRepository userRoleRepository;
	ConfigurationStore store = ConfigurationStore.getInstance();

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User userObject = getUserObject(username);
		if (userObject.getPassword() == null) {
			throw new UsernameNotFoundException("Requested user is not found.");
		}
		return userObject;
	}

	public User getUserObject(String username) {
		List<RadCheck> allUserEntries = radCheckRepository.findByUsername(username);
		User userObject = new User();
		userObject.setUsername(username);
		Set<GrantedAuthority> authorities = new HashSet<>();
		for (RadCheck radcheck : allUserEntries) {
			switch (radcheck.getAttribute()) {
				case PASSWORD_ATTRIBUTE_NAME :
					if (Crypt.crypt(radcheck.getUsername(), radcheck.getValue()).equals(radcheck.getValue())) {
						userObject.setForcePasswordChange(true);
					}
					userObject.setPassword(radcheck.getValue());
					break;
				case EXPIRATION_ATTRIBUTE_NAME :
					LocalDateTime expirationDate = LocalDateTime.ofEpochSecond(Long.parseLong(radcheck.getValue()), 0,
							ZoneId.of(store.getDbTimezone()).getRules().getOffset(Instant.now()));
					userObject.setExpirationDate(expirationDate);
					userObject.setActive(userObject.getExpirationDate().isAfter(LocalDateTime.now()));
			}
		}
		userObject.getRoles().addAll(userRoleRepository.getByUsername(username).stream().map(UserRole::getRoleName)
				.collect(Collectors.toList()));
		return userObject;
	}

	public List<User> getAllUserObjects() {
		List<RadCheck> allUserEntries = radCheckRepository.findAll();
		Map<String, User> userMap = new HashMap<>();
		for (RadCheck radcheck : allUserEntries) {
			User userObject = userMap.computeIfAbsent(radcheck.getUsername(), e -> new User());
			userObject.setUsername(radcheck.getUsername());
			switch (radcheck.getAttribute()) {
				case PASSWORD_ATTRIBUTE_NAME :
					userObject.getRoles().addAll(userRoleRepository.getByUsername(radcheck.getUsername()).stream()
							.map(UserRole::getRoleName).collect(Collectors.toList()));
					break;
				case EXPIRATION_ATTRIBUTE_NAME :
					LocalDateTime expirationDate = LocalDateTime.ofEpochSecond(Long.parseLong(radcheck.getValue()), 0,
							ZoneId.of(store.getDbTimezone()).getRules().getOffset(Instant.now()));
					userObject.setExpirationDate(expirationDate);
					userObject.setActive(userObject.getExpirationDate().isAfter(LocalDateTime.now()));
			}
		}
		return new ArrayList<>(userMap.values());
	}

	public File generateIntegratedConfigurationFile(String username, File caCert, File clientCert, File clientKey,
			Locale locale) {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.addTemplateResolver(textTemplateResolver());
		Context thymeleafContext = new Context(locale);
		String caCertificatePemContents = "";
		String clientCertificatePemContents = "";
		String clientKeyPemContents = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(caCert))) {
			caCertificatePemContents = reader.lines().collect(Collectors.joining("\n"));
		} catch (IOException ignored) {
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(clientCert))) {
			clientCertificatePemContents = reader.lines().collect(Collectors.joining("\n"));
		} catch (IOException ignored) {
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(clientKey))) {
			clientKeyPemContents = reader.lines().collect(Collectors.joining("\n"));
		} catch (IOException ignored) {
		}
		thymeleafContext.setVariable("host", store.getOvpnPublicAddress());
		thymeleafContext.setVariable("port", store.getOvpnPublicPort());
		thymeleafContext.setVariable("cacert", caCertificatePemContents);
		thymeleafContext.setVariable("clientcert", clientCertificatePemContents);
		thymeleafContext.setVariable("clientkey", clientKeyPemContents);
		thymeleafContext.setVariable("ovpncipher", store.getOvpnCipher());
		thymeleafContext.setVariable("ovpndigest", store.getOvpnDigest());
		String rendered = templateEngine.process("client-integrated.ovpn", thymeleafContext);
		File configurationFile = new File(store.getCaDownloadsHome(), String.format("%s.ovpn", username));
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(configurationFile, false)), true)) {
			writer.println(rendered);
		} catch (IOException ex) {
		}
		return configurationFile;
	}

	public File generatePathConfigurationFile(String username, Locale locale) {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.addTemplateResolver(textTemplateResolver());
		Context thymeleafContext = new Context(locale);
		thymeleafContext.setVariable("host", store.getOvpnPublicAddress());
		thymeleafContext.setVariable("port", store.getOvpnPublicPort());
		thymeleafContext.setVariable("cacertpath", String.format("%s-ca.pem", username));
		thymeleafContext.setVariable("clientcertpath", String.format("%s.pem", username));
		thymeleafContext.setVariable("clientkeypath", String.format("%s.key", username));
		thymeleafContext.setVariable("ovpncipher", store.getOvpnCipher());
		thymeleafContext.setVariable("ovpndigest", store.getOvpnDigest());
		String rendered = templateEngine.process("client-path.ovpn", thymeleafContext);
		File configurationFile = new File(store.getCaDownloadsHome(), String.format("%s.ovpn", username));
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(configurationFile, false)), true)) {
			writer.println(rendered);
		} catch (IOException ex) {
		}
		return configurationFile;
	}

	private ITemplateResolver textTemplateResolver() {
		final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setOrder(1);
		templateResolver.setResolvablePatterns(Collections.singleton("*"));
		templateResolver.setPrefix("/ovpnconfig/");
		templateResolver.setSuffix(".template");
		templateResolver.setTemplateMode(TemplateMode.TEXT);
		templateResolver.setCharacterEncoding(CharEncoding.UTF_8);
		templateResolver.setCacheable(false);
		return templateResolver;
	}

}
