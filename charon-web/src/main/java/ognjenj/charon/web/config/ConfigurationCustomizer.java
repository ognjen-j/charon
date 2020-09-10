package ognjenj.charon.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {
		ConfigurationStore store = ConfigurationStore.getInstance();
		Logger logger = LoggerFactory.getLogger(this.getClass());
		logger.info(String.format("Starting server on port %d", store.getWebServerPort()));
		factory.setPort(store.getWebServerPort());
		Ssl sslInfo = new Ssl();
		sslInfo.setClientAuth(Ssl.ClientAuth.NONE);
		sslInfo.setEnabled(true);
		sslInfo.setKeyAlias("charon");
		sslInfo.setKeyStoreType("pkcs12");
		sslInfo.setKeyStore(store.getWebServerKeystore());
		sslInfo.setKeyStorePassword(store.getWebServerKeystorePass());
		factory.setSsl(sslInfo);
	}
}
