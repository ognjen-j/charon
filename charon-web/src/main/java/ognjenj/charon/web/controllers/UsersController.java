package ognjenj.charon.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.Crypt;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import inet.ipaddr.IPAddressString;
import ognjenj.charon.web.config.ConfigurationStore;
import ognjenj.charon.web.exceptions.CaException;
import ognjenj.charon.web.model.Certificate;
import ognjenj.charon.web.model.User;
import ognjenj.charon.web.model.forms.NewUserForm;
import ognjenj.charon.web.model.forms.PasswordForm;
import ognjenj.charon.web.model.radius.*;
import ognjenj.charon.web.repositories.*;
import ognjenj.charon.web.services.CertificateService;
import ognjenj.charon.web.services.UserService;
import ognjenj.charon.web.util.SaltGenerator;

@Controller
public class UsersController {

	@Autowired
	UserService userService;
	@Autowired
	UserCertRepository userCertRepository;
	@Autowired
	RadCheckRepository radCheckRepository;
	@Autowired
	RadReplyRepository radReplyRepository;
	@Autowired
	UserRoleRepository userRoleRepository;
	@Autowired
	RadUserGroupRepository radUserGroupRepository;
	@Autowired
	CertificateService certificateService;
	ConfigurationStore store = ConfigurationStore.getInstance();

	@RequestMapping({"ovpn/users"})
	public String getAllActiveUsers(Model model) {
		List<User> allUsers = userService.getAllUserObjects();
		Set<UserCert> certs = new HashSet<>();
		certs.addAll(userCertRepository.getActive());
		certs.addAll(userCertRepository.getRevokedNonExpired());
		for (User user : allUsers) {
			Optional<UserCert> matchingCert = certs.stream().filter(e -> e.getCommonName().equals(user.getUsername()))
					.findFirst();
			matchingCert.ifPresent(e -> user.setActiveCertificate(new Certificate(e)));
		}
		model.addAttribute("users", allUsers);
		return "ovpn/users";
	}

	@GetMapping({"ovpn/block"})
	public String blockUser(@RequestParam(name = "d", required = false) String username) {
		if (username != null) {
			LocalDateTime newExpirationDate = LocalDateTime.now().minus(5, ChronoUnit.MINUTES);
			String newExpirationDateAsString = String.valueOf(newExpirationDate
					.toEpochSecond(ZoneId.of(store.getDbTimezone()).getRules().getOffset(Instant.now())));
			List<RadCheck> userEntries = radCheckRepository.findByUsernameAndAttribute(username,
					UserService.EXPIRATION_ATTRIBUTE_NAME);
			Optional<RadCheck> existing = userEntries.stream().findFirst();
			if (existing.isPresent()) {
				existing.get().setValue(newExpirationDateAsString);
				radCheckRepository.save(existing.get());
			} else {
				RadCheck expirationRadcheck = new RadCheck();
				expirationRadcheck.setOperator(UserService.CONTROL_ATTRIBUTE_OPERATOR);
				expirationRadcheck.setAttribute(UserService.EXPIRATION_ATTRIBUTE_NAME);
				expirationRadcheck.setUsername(username);
				expirationRadcheck.setValue(newExpirationDateAsString);
				radCheckRepository.save(expirationRadcheck);
			}
		}
		return "redirect:/ovpn/users";
	}

	@GetMapping({"ovpn/reset"})
	public String resetUserPassword(@RequestParam(name = "d", required = false) String username,
			RedirectAttributes redirectAttributes) {
		if (username != null) {
			Locale currentLocale = LocaleContextHolder.getLocale();
			ResourceBundle messageBundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
			List<RadCheck> userEntries = radCheckRepository.findByUsernameAndAttribute(username,
					UserService.PASSWORD_ATTRIBUTE_NAME);
			Optional<RadCheck> existing = userEntries.stream().findFirst();
			if (existing.isPresent()) {
				existing.get().setValue(SaltGenerator.generatePasswordWithRandomSalt(username));
				radCheckRepository.save(existing.get());
				redirectAttributes.addFlashAttribute("message", messageBundle.getString("message.password_reset"));
			}
		}
		return "redirect:/ovpn/users";
	}

	@PostMapping({"ovpn/newuser"})
	public String editUser(NewUserForm newUserForm, Authentication authentication, HttpServletRequest request,
			Model model) {
		Locale currentLocale = LocaleContextHolder.getLocale();
		ResourceBundle messageBundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
		if (newUserForm.getUsername() == null) {
			model.addAttribute("message", messageBundle.getString("message.username_is_mandatory"));
			model.addAttribute("user", newUserForm);
			return null;
		}
		String[] routes = newUserForm.getRouteString().split("[\\r]?(\\n)");
		List<String> formattedRoutes = new ArrayList<>();
		if (newUserForm.isRouteAllTraffic()) {
			formattedRoutes.add(UserService.DEFAULT_ROUTE);
		} else {
			for (String route : routes) {
				if (route.isBlank())
					continue;
				IPAddressString addressObject = new IPAddressString(route);
				if (!addressObject.isIPv4()) {
					model.addAttribute("message",
							MessageFormat.format(messageBundle.getString("message.route_format_invalid"), route));
					model.addAttribute("user", newUserForm);
					return null;
				}
				formattedRoutes.add(String.format("%s/%d", addressObject.getHostAddress().toAddressString(),
						addressObject.getNetworkPrefixLength() == null ? 32 : addressObject.getNetworkPrefixLength()));
			}
		}
		boolean allowedToChangeCertificates = ((UserDetails) authentication.getPrincipal()).getAuthorities().stream()
				.map(GrantedAuthority::getAuthority).anyMatch(e -> e.equals("ROLE_CA"));
		if (newUserForm.isRegenerateCertificate() && allowedToChangeCertificates
				&& (newUserForm.getArchivePassword() == null || newUserForm.getArchivePassword().length() < 8)) {
			model.addAttribute("message", messageBundle.getString("message.you_must_provide_archive_password"));
			model.addAttribute("user", newUserForm);
			return null;
		}
		Optional<RadCheck> existingPassword = radCheckRepository
				.findByUsernameAndAttribute(newUserForm.getUsername(), UserService.PASSWORD_ATTRIBUTE_NAME).stream()
				.findAny();
		Optional<RadCheck> existingExpiration = radCheckRepository
				.findByUsernameAndAttribute(newUserForm.getUsername(), UserService.EXPIRATION_ATTRIBUTE_NAME).stream()
				.findAny();
		if (existingPassword.isEmpty()) {
			RadCheck password = new RadCheck();
			password.setUsername(newUserForm.getUsername());
			password.setOperator(UserService.CONTROL_ATTRIBUTE_OPERATOR);
			password.setAttribute(UserService.PASSWORD_ATTRIBUTE_NAME);
			password.setValue(SaltGenerator.generatePasswordWithRandomSalt(newUserForm.getUsername()));
			radCheckRepository.save(password);
			RadUserGroup userGroup = new RadUserGroup(newUserForm.getUsername());
			radUserGroupRepository.save(userGroup);
		}

		Calendar calendarExpiration = Calendar.getInstance();
		calendarExpiration.setTime(newUserForm.getExpiration());
		LocalDateTime expirationLocal = LocalDateTime.of(calendarExpiration.get(Calendar.YEAR),
				calendarExpiration.get(Calendar.MONTH) + 1, calendarExpiration.get(Calendar.DAY_OF_MONTH), 0, 0);
		RadCheck expiration = new RadCheck();
		expiration.setUsername(newUserForm.getUsername());
		expiration.setOperator(UserService.CONTROL_ATTRIBUTE_OPERATOR);
		expiration.setAttribute(UserService.EXPIRATION_ATTRIBUTE_NAME);
		expiration.setValue(String.valueOf(
				expirationLocal.toEpochSecond(ZoneId.of(store.getDbTimezone()).getRules().getOffset(Instant.now()))));
		existingExpiration.ifPresent(radCheck -> expiration.setId(radCheck.getId()));
		radCheckRepository.save(expiration);

		userRoleRepository.deleteByUsername(newUserForm.getUsername());
		List<UserRole> newRoles = newUserForm.getAssignedRoles().stream()
				.map(e -> new UserRole(newUserForm.getUsername(), e)).collect(Collectors.toList());
		userRoleRepository.saveAll(newRoles);

		radReplyRepository.deleteByUsernameAndAttribute(newUserForm.getUsername(), UserService.ROUTE_ATTRIBUTE_NAME);
		radReplyRepository.deleteByUsernameAndAttribute(newUserForm.getUsername(),
				UserService.STATIC_ADDRESS_ATTRIBUTE_NAME);
		List<RadReply> routeReplies = formattedRoutes.stream().map(e -> new RadReply(newUserForm.getUsername(),
				UserService.ROUTE_ATTRIBUTE_NAME, UserService.ROUTE_ATTRIBUTE_OPERATOR, e))
				.collect(Collectors.toList());
		if (newUserForm.isAssignStaticIpAddress() && !newUserForm.getStaticIpAddress().isBlank()) {
			IPAddressString addressObject = new IPAddressString(newUserForm.getStaticIpAddress());
			if (addressObject.isIPv4()) {
				List<RadReply> existingStaticAddresses = radReplyRepository.findByAttributeAndValue(
						UserService.STATIC_ADDRESS_ATTRIBUTE_NAME,
						addressObject.getHostAddress().toAddressString().toString());
				if (existingStaticAddresses.isEmpty()) {
					routeReplies.add(new RadReply(newUserForm.getUsername(), UserService.STATIC_ADDRESS_ATTRIBUTE_NAME,
							UserService.STATIC_ADDRESS_ATTRIBUTE_OPERATOR,
							addressObject.getHostAddress().toAddressString().toString()));
				} else {
					model.addAttribute("message", messageBundle.getString("message.address_already_assigned"));
					return null;
				}
			} else {
				model.addAttribute("message", messageBundle.getString("message.address_format_invalid"));
				return null;
			}
		}
		radReplyRepository.saveAll(routeReplies);

		if (allowedToChangeCertificates && newUserForm.isRegenerateCertificate()) {
			try {
				certificateService.signNewCertificate(newUserForm.getUsername(),
						Integer.parseInt(newUserForm.getDuration()), newUserForm.getArchivePassword());
			} catch (IOException | OperatorCreationException | CaException | CertificateException
					| NumberFormatException ex) {
				model.addAttribute("message", MessageFormat
						.format(messageBundle.getString("message.error_generating_certificate"), ex.getMessage()));
				model.addAttribute("user", newUserForm);
				return null;
			}
		}
		return "redirect:/ovpn/users";
	}

	@GetMapping({"ovpn/newuser"})
	public String fillEditUserForm(@RequestParam(name = "d", required = false) String username, NewUserForm newUserForm,
			Model model) {
		newUserForm.setAssignedRoles(Collections.singletonList("USER"));
		LocalDateTime inFiveYears = LocalDateTime.now().plusYears(5);
		newUserForm.setExpiration(
				Date.from(inFiveYears.toLocalDate().atStartOfDay(ZoneId.of(store.getDbTimezone())).toInstant()));
		if (username != null) {
			List<RadCheck> userEntries = radCheckRepository.findByUsername(username);
			Optional<RadCheck> existingPassword = userEntries.stream()
					.filter(e -> e.getAttribute().equals(UserService.PASSWORD_ATTRIBUTE_NAME)).findFirst();
			Optional<RadCheck> existingExpiration = userEntries.stream()
					.filter(e -> e.getAttribute().equals(UserService.EXPIRATION_ATTRIBUTE_NAME)).findFirst();
			if (existingPassword.isPresent()) {
				newUserForm.setExistingUser(true);
				newUserForm.setUsername(username);
				newUserForm.setExpiration(new Date(Long.parseLong(existingExpiration.get().getValue()) * 1000));
				newUserForm.setAssignedRoles(userRoleRepository.getByUsername(existingPassword.get().getUsername())
						.stream().map(UserRole::getRoleName).collect(Collectors.toList()));
				List<RadReply> routes = radReplyRepository.findByUsername(username);
				newUserForm.setAssignStaticIpAddress(routes.stream()
						.anyMatch(e -> e.getAttribute().equals(UserService.STATIC_ADDRESS_ATTRIBUTE_NAME)));
				if (newUserForm.isAssignStaticIpAddress()) {
					newUserForm.setStaticIpAddress(routes.stream()
							.filter(e -> e.getAttribute().equals(UserService.STATIC_ADDRESS_ATTRIBUTE_NAME)).findFirst()
							.get().getValue());
				}
				boolean pushDefaultRoute = routes.stream()
						.anyMatch(e -> e.getAttribute().equals(UserService.ROUTE_ATTRIBUTE_NAME)
								&& e.getValue().equals(UserService.DEFAULT_ROUTE));
				if (pushDefaultRoute) {
					newUserForm.setRouteAllTraffic(true);
					newUserForm.setRouteString("");
				} else {
					newUserForm.setRouteAllTraffic(false);
					newUserForm.setRouteString(
							routes.stream().filter(e -> e.getAttribute().equals(UserService.ROUTE_ATTRIBUTE_NAME))
									.map(RadReply::getValue).collect(Collectors.joining("\n")));
				}
			}
		}
		model.addAttribute("user", newUserForm);
		return "ovpn/newuser";
	}

	@GetMapping({"password"})
	public String showPasswordChangeForm(PasswordForm passwordForm, Model model) {
		model.addAttribute("passwordForm", passwordForm);
		return "password";
	}

	@PostMapping({"password"})
	public String changePassword(PasswordForm passwordForm, Principal principal, Model model) {
		Locale currentLocale = LocaleContextHolder.getLocale();
		ResourceBundle messageBundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
		List<RadCheck> currentPasswordEntries = radCheckRepository.findByUsernameAndAttribute(principal.getName(),
				UserService.PASSWORD_ATTRIBUTE_NAME);
		Optional<RadCheck> currentPassword = currentPasswordEntries.stream().findFirst();
		if (currentPassword.isPresent()) {
			if (passwordForm.getNewPassword().toLowerCase().contains(principal.getName())) {
				model.addAttribute("message", messageBundle.getString("message.new_password_contains_username"));
			} else if (!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordRepeat())) {
				model.addAttribute("message", messageBundle.getString("message.new_passwords_dont_match"));
			} else if (passwordForm.getNewPassword().length() < 8) {
				model.addAttribute("message", messageBundle.getString("message.new_password_too_short"));
			} else {
				String encodedPassword = Crypt.crypt(passwordForm.getCurrentPassword(),
						currentPassword.get().getValue());
				if (encodedPassword.equals(currentPassword.get().getValue())) {
					currentPassword.get()
							.setValue(SaltGenerator.generatePasswordWithRandomSalt(passwordForm.getNewPassword()));
					radCheckRepository.save(currentPassword.get());
					Authentication auth = SecurityContextHolder.getContext().getAuthentication();
					User user = (User) auth.getPrincipal();
					user.setForcePasswordChange(false);
					model.addAttribute("message", messageBundle.getString("message.password_set"));
				} else {
					model.addAttribute("message", messageBundle.getString("message.current_password_wrong"));
				}
			}
		} else {
			model.addAttribute("message", messageBundle.getString("message.user_doesnt_exist"));
		}
		return "password";
	}
}
