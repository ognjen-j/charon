package ognjenj.charon.web.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ognjenj.charon.web.exceptions.IncompleteConfigurationException;

@Configuration
public class DatasourceConfiguration {

	@Bean
	public DataSource datasource() throws IncompleteConfigurationException {
		ConfigurationStore store = ConfigurationStore.getInstance();
		return DataSourceBuilder.create().driverClassName("com.mysql.cj.jdbc.Driver")
				.url(String.format("jdbc:mysql://%s:3306/radius?serverTimezone=%s&useSSL=false",
						store.getRadiusDbHost(), store.getDbTimezone()))
				.username(store.getRadiusDbUsername()).password(store.getRadiusDbPassword()).build();
	}

}
