package ognjenj.charon.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ognjenj.charon.web.config.ConfigurationStore;
import ognjenj.charon.web.exceptions.CaException;
import ognjenj.charon.web.model.Certificate;
import ognjenj.charon.web.model.forms.CertsForm;
import ognjenj.charon.web.model.forms.NewCertForm;
import ognjenj.charon.web.model.radius.UserCert;
import ognjenj.charon.web.repositories.UserCertRepository;
import ognjenj.charon.web.services.CertificateService;

@Controller
public class CertsController {
	@Autowired
	UserCertRepository userCertRepository;
	@Autowired
	CertificateService certificateService;
	ConfigurationStore store = ConfigurationStore.getInstance();
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping({"ca/certs"})
	public String getAllActiveCerts(CertsForm certsForm, Model model) {
		Set<UserCert> certs = new HashSet<>();
		if (certsForm.isShowActive()) {
			certs.addAll(userCertRepository.getActive());
		}
		if (certsForm.isShowRevoked()) {
			certs.addAll(userCertRepository.getRevokedNonExpired());
		}
		if (certsForm.isShowExpired()) {
			certs.addAll(userCertRepository.getExpired());
		}
		model.addAttribute("certs", certs.stream().map(Certificate::new).collect(Collectors.toList()));
		return "ca/certs";
	}

	@GetMapping({"ca/revoke"})
	public String revokeCertificate(@RequestParam(name = "d") String certSerial, Model model) {
		if (certSerial == null)
			return "ca/certs";
		Locale currentLocale = LocaleContextHolder.getLocale();
		ResourceBundle messageBundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
		UserCert certificate = userCertRepository.getByCertSerial(certSerial);
		if (certificate != null) {
			certificate.setRevoked(true);
			certificate.setRevocationDate(new Date());
			userCertRepository.save(certificate);
			try {
				certificateService.generateCrlList();
			} catch (IOException | CertificateException | OperatorCreationException ex) {
			}
		} else {
			model.addAttribute("message", messageBundle.getString("message.certificate_doesnt_exist"));
		}
		return "redirect:/ca/certs";
	}

	@GetMapping({"ca/newcert"})
	public String showNewCertificateForm(@RequestParam(name = "c", required = false) String existingCommonName,
			NewCertForm form, Model model) {
		List<UserCert> certsForCommonName;
		if (existingCommonName != null)
			certsForCommonName = userCertRepository.getByCommonName(existingCommonName);
		else
			certsForCommonName = new ArrayList<>();
		Optional<Certificate> existingCert = certsForCommonName.stream().map(Certificate::new)
				.filter(e -> !e.isExpired() && !e.isRevoked()).findFirst();
		if (existingCert.isPresent()) {
			form.setCommonName(existingCert.get().getCommonName());
			form.setExtendExisting(true);
		}
		form.setExtendExisting(false);
		model.addAttribute("newCertForm", form);
		return "ca/newcert";
	}

	@PostMapping({"ca/newcert"})
	public String createNewCertificate(NewCertForm form, Model model) {
		Locale currentLocale = LocaleContextHolder.getLocale();
		ResourceBundle messageBundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
		try {
			if (form.getCommonName().isBlank()) {
				model.addAttribute("message", messageBundle.getString("message.common_name_is_mandatory"));
				return "ca/newcert";
			} else if (form.getDownloadPassword().trim().length() < 8) {
				model.addAttribute("message", messageBundle.getString("message.password_is_too_short"));
				return "ca/newcert";
			}
			certificateService.signNewCertificate(form.getCommonName(), Integer.parseInt(form.getDuration()),
					form.getDownloadPassword());
		} catch (IOException | NumberFormatException | CertificateException | CaException
				| OperatorCreationException ex) {
			logger.error(ex.getMessage(), ex);
		}
		return "redirect:/ca/certs";
	}

	@GetMapping({"crl"})
	public String downloadCrl(HttpServletResponse response) {
		try {
			File crlFile = new File(store.getCrlHome(), "crl.pem");
			if (crlFile.exists() && crlFile.isFile()) {
				response.setContentType("application/octet-stream");
				response.setHeader("Content-disposition", "attachment; filename=" + crlFile.getName());

				OutputStream out = response.getOutputStream();
				FileInputStream in = new FileInputStream(crlFile);
				byte[] buffer = new byte[1024];
				int contentRead;
				while ((contentRead = in.read(buffer)) != -1) {
					out.write(buffer, 0, contentRead);
				}
				out.close();
				in.close();
			}
		} catch (IOException ignore) {
		}
		return null;
	}
}
