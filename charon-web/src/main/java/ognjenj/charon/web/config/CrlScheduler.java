package ognjenj.charon.web.config;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.annotation.PostConstruct;

import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ognjenj.charon.web.services.CertificateService;

@Component
public class CrlScheduler {
	@Autowired
	CertificateService certificateService;

	@PostConstruct
	public void initialCrlGeneration() {
		try {
			// initial CRL generation on startup
			certificateService.generateCrlList();
		} catch (IOException | CertificateException | OperatorCreationException ex) {
			System.err.println("Error generating initial certificate revocation list (" + ex.getMessage() + ")");
		}
	}
	@Scheduled(cron = "0 10 02 * * *")
	public void renderCrlList()
			throws OperatorCreationException, CertificateException, NoSuchAlgorithmException, IOException {
		certificateService.generateCrlList();
	}
}
