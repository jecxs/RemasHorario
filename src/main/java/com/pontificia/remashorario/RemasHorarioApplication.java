package com.pontificia.remashorario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RemasHorarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemasHorarioApplication.class, args);
    }

}
