package dev.jeatog.comala.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "dev.jeatog.comala")
public class ComalaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComalaApplication.class, args);
    }
}
