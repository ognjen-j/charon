package ognjenj.charon.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ognjenj.charon.web.config.ConfigurationStore;
import ognjenj.charon.web.model.Certificate;
import ognjenj.charon.web.model.Connection;
import ognjenj.charon.web.model.User;
import ognjenj.charon.web.model.forms.FilterForm;
import ognjenj.charon.web.model.radius.UserCert;
import ognjenj.charon.web.repositories.RadAcctRepository;
import ognjenj.charon.web.repositories.RadCheckRepository;
import ognjenj.charon.web.repositories.UserCertRepository;
import ognjenj.charon.web.services.UserService;

@Controller
public class IndexController {
	@Autowired
	RadAcctRepository radAcctRepository;
	@Autowired
	RadCheckRepository radCheckRepository;
	@Autowired
	UserCertRepository userCertRepository;
	@Autowired
	UserService userService;
	ConfigurationStore store = ConfigurationStore.getInstance();

	@GetMapping({"/", "index"})
	public String showBasicInfo(Principal principal, Model model, RedirectAttributes redirectAttributes) {
		Locale currentLocale = LocaleContextHolder.getLocale();
		ResourceBundle messageBundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
		User currentUser = userService.getUserObject(principal.getName());
		List<UserCert> allCertificates = userCertRepository.getByCommonName(principal.getName());
		Optional<Certificate> activeCertificate = allCertificates.stream().map(Certificate::new)
				.filter(e -> !e.isExpired() && !e.isRevoked()).findFirst();
		activeCertificate.ifPresent(currentUser::setActiveCertificate);
		model.addAttribute("user", currentUser);
		if (currentUser.isForcePasswordChange()) {
			redirectAttributes.addFlashAttribute("message",
					messageBundle.getString("message.you_must_change_password"));
			return "redirect:/password";
		} else {
			return "index";
		}
	}

	@GetMapping({"download"})
	public String downloadArchive(HttpServletResponse response, Principal principal) throws IOException {
		List<UserCert> allCertificates = userCertRepository.getByCommonName(principal.getName());
		Optional<UserCert> potentialActiveCertificate = allCertificates.stream()
				.filter(e -> e.getExpirationDate().after(new Date()) && !e.isRevoked()).findFirst();
		if (potentialActiveCertificate.isPresent() && !potentialActiveCertificate.get().isDownloaded()) {
			UserCert certificate = potentialActiveCertificate.get();
			File archiveFile = new File(store.getCaDownloadsHome(),
					String.format("%s.zip", potentialActiveCertificate.get().getCertSerial()));
			response.setContentType("application/zip");
			response.setHeader("Content-disposition", "attachment; filename=" + archiveFile.getName());

			OutputStream out = response.getOutputStream();
			FileInputStream in = new FileInputStream(archiveFile);
			byte[] buffer = new byte[1024];
			int contentRead;
			while ((contentRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, contentRead);
			}
			out.close();
			in.close();
			archiveFile.delete();
			certificate.setDownloaded(true);
			userCertRepository.save(certificate);
			return null;
		} else
			return "redirect:/index";
	}

	@GetMapping({"configuration"})
	public String downloadConfiguration(HttpServletResponse response, Principal principal) throws IOException {
		Locale currentLocale = LocaleContextHolder.getLocale();
		File configurationFile = userService.generatePathConfigurationFile(principal.getName(), currentLocale);
		response.setContentType("text/plain");
		response.setHeader("Content-disposition", "attachment; filename=" + configurationFile.getName());
		OutputStream out = response.getOutputStream();
		FileInputStream in = new FileInputStream(configurationFile);
		byte[] buffer = new byte[1024];
		int contentRead;
		while ((contentRead = in.read(buffer)) != -1) {
			out.write(buffer, 0, contentRead);
		}
		out.close();
		in.close();
		configurationFile.delete();
		return null;
	}

	@GetMapping({"ovpn/connections"})
	public String loadConnectionsInitial(FilterForm filterForm, Principal principal, Model model) {
		LocalDateTime startDateTime = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
		FilterForm defaultValues = new FilterForm();
		defaultValues.setEndDate(new Date());
		defaultValues.setStartDate(
				Date.from(startDateTime.toLocalDate().atStartOfDay(ZoneId.of(store.getDbTimezone())).toInstant()));
		model.addAttribute("filterForm", defaultValues);
		return loadData(principal.getName(), startDateTime, null, model);
	}

	@PostMapping({"ovpn/connections"})
	public String filterConnections(FilterForm filterForm, Principal principal, Model model) {
		Calendar calendarFrom = Calendar.getInstance();
		calendarFrom.setTime(filterForm.getStartDate());
		Calendar calendarTo = Calendar.getInstance();
		calendarTo.setTime(filterForm.getEndDate());
		LocalDateTime from = LocalDateTime.of(calendarFrom.get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH) + 1,
				calendarFrom.get(Calendar.DAY_OF_MONTH), 0, 0);
		LocalDateTime to = LocalDateTime.of(calendarTo.get(Calendar.YEAR), calendarTo.get(Calendar.MONTH) + 1,
				calendarTo.get(Calendar.DAY_OF_MONTH), 23, 59);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) auth.getPrincipal();
		String usernameParam = principal.getName();
		if (user.getRoles().contains("OVPN")) {
			usernameParam = "%%";
		}
		return loadData(usernameParam, from, to, model);
	}

	private String loadData(String username, LocalDateTime startDateTime, LocalDateTime stopDateTime, Model model) {
		List<Connection> connections = radAcctRepository
				.getByUsernameBetweenDates(startDateTime, stopDateTime, username).stream().map(Connection::new)
				.collect(Collectors.toList());
		model.addAttribute("conns", connections);
		return "ovpn/connections";
	}
}
