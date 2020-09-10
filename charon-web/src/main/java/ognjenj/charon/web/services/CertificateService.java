package ognjenj.charon.web.services;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.bc.BcPEMDecryptorProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import ognjenj.charon.web.config.ConfigurationStore;
import ognjenj.charon.web.exceptions.CaException;
import ognjenj.charon.web.model.Certificate;
import ognjenj.charon.web.model.radius.UserCert;
import ognjenj.charon.web.repositories.UserCertRepository;

@Service
public class CertificateService {
	private static final String SIGNING_ALGORITHM = "SHA256withRSA";
	@Autowired
	UserCertRepository userCertRepository;
	@Autowired
	UserService userService;
	ConfigurationStore configurationStore = ConfigurationStore.getInstance();

	public void signNewCertificate(String commonName, int durationInDays, String archivePassword)
			throws CaException, OperatorCreationException, CertificateException, IOException {
		List<UserCert> certsForCommonName = userCertRepository.getByCommonName(commonName);
		Optional<Certificate> existingCert = certsForCommonName.stream().map(Certificate::new)
				.filter(e -> !e.isExpired() && !e.isRevoked()).findFirst();
		if (existingCert.isPresent()) {
			// revoke existing certificate
			UserCert certToRevoke = certsForCommonName.stream()
					.filter(e -> e.getCertId().equals(existingCert.get().getCertificateId())).findFirst().get();
			certToRevoke.setRevocationDate(new Date());
			certToRevoke.setRevoked(true);
			userCertRepository.save(certToRevoke);
			generateCrlList();
		}
		try {
			KeyPair keyPair = generateKeyPair(configurationStore.getKeyLength());
			KeyPair caKeyPair = loadCaKey();
			X509Certificate caCertificate = loadCaCertificate();
			X500Name x500name = new JcaX509CertificateHolder(caCertificate).getSubject();
			X500Principal principal = new X500Principal(String.format("C=%s, ST=%s, L=%s, O=%s, OU=%s, CN=%s",
					x500name.getRDNs(BCStyle.C)[0].getFirst().getValue().toString(),
					x500name.getRDNs(BCStyle.ST)[0].getFirst().getValue().toString(),
					x500name.getRDNs(BCStyle.L)[0].getFirst().getValue().toString(),
					x500name.getRDNs(BCStyle.O)[0].getFirst().getValue().toString(),
					x500name.getRDNs(BCStyle.OU)[0].getFirst().getValue().toString(), commonName));
			ContentSigner clientSigner = new JcaContentSignerBuilder(SIGNING_ALGORITHM)
					.setProvider(new BouncyCastleProvider()).build(keyPair.getPrivate());

			PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(principal,
					keyPair.getPublic());
			PKCS10CertificationRequest csr = builder.build(clientSigner);
			X509Certificate clientCertificate = generateClientCertificate(caCertificate, caKeyPair, csr,
					durationInDays);
			generateFilesAndArchive(commonName, clientCertificate, caCertificate, keyPair, archivePassword);
			UserCert newCert = new UserCert();
			newCert.setRevoked(false);
			newCert.setRevocationDate(null);
			newCert.setCertSerial(clientCertificate.getSerialNumber().toString(16));
			newCert.setCommonName(commonName);
			newCert.setDn(clientCertificate.getSubjectDN().toString());
			newCert.setDownloaded(false);
			newCert.setExpirationDate(clientCertificate.getNotAfter());
			newCert.setIssueDate(clientCertificate.getNotBefore());
			userCertRepository.saveAndFlush(newCert);
		} catch (NoSuchAlgorithmException | IOException | CertificateException | OperatorCreationException ex) {
			throw new CaException(ex.getMessage(), ex);
		}
	}

	private KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(keySize);
		return generator.generateKeyPair();
	}

	private KeyPair loadCaKey() throws IOException {
		PEMParser pemParser = new PEMParser(new FileReader(new File(configurationStore.getCaKey())));
		PEMEncryptedKeyPair pemEncryptedKeyPair = (PEMEncryptedKeyPair) pemParser.readObject();
		PEMKeyPair pemKeyPair = pemEncryptedKeyPair
				.decryptKeyPair(new BcPEMDecryptorProvider(configurationStore.getCaKeyPass().toCharArray()));
		JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter();
		return keyConverter.getKeyPair(pemKeyPair);
	}

	private X509Certificate loadCaCertificate() throws IOException, CertificateException {
		PEMParser pemParser = new PEMParser(new FileReader(new File(configurationStore.getCaCertificate())));
		JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter()
				.setProvider(new BouncyCastleProvider());
		return certConverter.getCertificate((X509CertificateHolder) pemParser.readObject());
	}

	private void generateFilesAndArchive(String commonName, X509Certificate clientCertificate,
			X509Certificate caCertificate, KeyPair clientKey, String archivePassword) throws IOException {
		File clientCertificateFile = new File(configurationStore.getCaDownloadsHome(),
				String.format("%s.pem", commonName));
		File caCertificateFile = new File(configurationStore.getCaDownloadsHome(),
				String.format("%s-ca.pem", commonName));
		File keyFile = new File(configurationStore.getCaDownloadsHome(), String.format("%s.key", commonName));

		try (OutputStreamWriter clientCertWriter = new OutputStreamWriter(
				new FileOutputStream(clientCertificateFile))) {
			try (JcaPEMWriter pemWriter = new JcaPEMWriter(clientCertWriter)) {
				pemWriter.writeObject(clientCertificate);
			}
		}
		try (OutputStreamWriter caCertWriter = new OutputStreamWriter(new FileOutputStream(caCertificateFile))) {
			try (JcaPEMWriter pemWriter = new JcaPEMWriter(caCertWriter)) {
				pemWriter.writeObject(caCertificate);
			}
		}
		try (OutputStreamWriter keyWriter = new OutputStreamWriter(new FileOutputStream(keyFile))) {
			try (JcaPEMWriter pemWriter = new JcaPEMWriter(keyWriter)) {
				pemWriter.writeObject(clientKey);
			}
		}
		Locale currentLocale = LocaleContextHolder.getLocale();
		File configurationFile = userService.generateIntegratedConfigurationFile(commonName, caCertificateFile,
				clientCertificateFile, keyFile, currentLocale);
		ZipFile archive = new ZipFile(new File(configurationStore.getCaDownloadsHome(),
				String.format("%s.zip", clientCertificate.getSerialNumber().toString(16))));
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionLevel(CompressionLevel.NORMAL);
		parameters.setEncryptFiles(true);
		parameters.setEncryptionMethod(EncryptionMethod.AES);
		File[] filesToAdd = new File[]{caCertificateFile, clientCertificateFile, keyFile, configurationFile};
		archive.setPassword(archivePassword.toCharArray());
		archive.addFiles(Arrays.asList(filesToAdd), parameters);
		Files.copy(clientCertificateFile.toPath(),
				Path.of(configurationStore.getCaHome(), clientCertificateFile.getName()),
				StandardCopyOption.REPLACE_EXISTING);
		Files.deleteIfExists(clientCertificateFile.toPath());
		Files.deleteIfExists(keyFile.toPath());
		Files.deleteIfExists(caCertificateFile.toPath());
		Files.deleteIfExists(configurationFile.toPath());
	}

	private X509Certificate generateClientCertificate(X509Certificate caCertificate, KeyPair caKeyPair,
			PKCS10CertificationRequest csr, int durationInDays)
			throws IOException, CertificateException, OperatorCreationException {
		Date issuedDate = new Date();
		Date expiryDate = new Date(System.currentTimeMillis() + durationInDays * 86400000L);

		X500Name issuer = new JcaX509CertificateHolder(caCertificate).getSubject();
		List<RDN> subjectRdns = Arrays.asList(csr.getSubject().getRDNs());
		Collections.reverse(subjectRdns);
		X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(issuer,
				configurationStore.getNextCertSerial(), issuedDate, expiryDate,
				new X500Name(subjectRdns.toArray(new RDN[0])), csr.getSubjectPublicKeyInfo());
		certificateBuilder.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
		certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false,
				new BcX509ExtensionUtils().createSubjectKeyIdentifier(csr.getSubjectPublicKeyInfo()));
		certificateBuilder.addExtension(Extension.keyUsage, false,
				new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment | KeyUsage.dataEncipherment));
		certificateBuilder.addExtension(Extension.extendedKeyUsage, false,
				new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));

		JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(SIGNING_ALGORITHM);
		ContentSigner signer = csBuilder.build(caKeyPair.getPrivate());
		X509CertificateHolder holder = certificateBuilder.build(signer);
		return new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(holder);
	}

	public void generateCrlList() throws IOException, CertificateException, OperatorCreationException {
		X509Certificate caCertificate = loadCaCertificate();
		X500Name x500CaName = new JcaX509CertificateHolder(caCertificate).getSubject();
		X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(x500CaName, new Date());
		List<Certificate> revokedCertificates = userCertRepository.getRevokedNonExpired().stream().map(Certificate::new)
				.collect(Collectors.toList());
		for (Certificate revokedCertificate : revokedCertificates) {
			crlBuilder.addCRLEntry(new BigInteger(revokedCertificate.getCertificateSerial(), 16),
					Date.from(revokedCertificate.getRevocationDate()
							.atZone(ZoneId.of(configurationStore.getDbTimezone())).toInstant()),
					CRLReason.privilegeWithdrawn);
		}
		KeyPair caKeyPair = loadCaKey();
		ContentSigner contentSigner = new JcaContentSignerBuilder(SIGNING_ALGORITHM)
				.setProvider(new BouncyCastleProvider()).build(caKeyPair.getPrivate());
		X509CRLHolder crlHolder = crlBuilder
				.setNextUpdate(Date.from(LocalDateTime.now().plus(1, ChronoUnit.DAYS)
						.atZone(ZoneId.of(configurationStore.getDbTimezone())).toInstant()))
				.addExtension(Extension.cRLNumber, false, new CRLNumber(configurationStore.getNextCrlSerial()))
				.build(contentSigner);
		try (OutputStreamWriter crlWriter = new OutputStreamWriter(
				new FileOutputStream(new File(configurationStore.getCrlHome(), "crl.pem")))) {
			try (JcaPEMWriter pemWriter = new JcaPEMWriter(crlWriter)) {
				pemWriter.writeObject(crlHolder);
			}
		}
	}
}
