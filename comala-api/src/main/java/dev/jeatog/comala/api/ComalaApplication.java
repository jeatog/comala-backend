package dev.jeatog.comala.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "dev.jeatog.comala")
@EntityScan(basePackages = "dev.jeatog.comala")
@EnableJpaRepositories(basePackages = "dev.jeatog.comala")
public class ComalaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComalaApplication.class, args);
    }
}
