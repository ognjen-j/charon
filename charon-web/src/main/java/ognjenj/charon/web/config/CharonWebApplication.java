package ognjenj.charon.web.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"ognjenj.charon.web"})
@EntityScan("ognjenj.charon.web.model")
@EnableJpaRepositories("ognjenj.charon.web.repositories")
@EnableScheduling
public class CharonWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(CharonWebApplication.class, args);
	}
}
