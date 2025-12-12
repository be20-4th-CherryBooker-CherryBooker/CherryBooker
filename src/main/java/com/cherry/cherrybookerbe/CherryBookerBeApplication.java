package com.cherry.cherrybookerbe;

import com.cherry.cherrybookerbe.common.config.AdminProperties;
import com.cherry.cherrybookerbe.user.command.domain.entity.User;
import com.cherry.cherrybookerbe.user.command.domain.entity.UserRole;
import com.cherry.cherrybookerbe.user.command.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableJpaAuditing
@SpringBootApplication
public class CherryBookerBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CherryBookerBeApplication.class, args);
    }

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder,
                                       AdminProperties adminProps) {
        return args -> {
            boolean exists = userRepository.existsByUserEmailAndUserRole(adminProps.getEmail(), UserRole.ADMIN);

            if (exists) {
                return;
            }

            User admin = User.builder()
                    .userEmail(adminProps.getEmail())
                    .userPassword(passwordEncoder.encode(adminProps.getPassword()))
                    .userName(adminProps.getName())
                    .userNickname("관리자")
                    .userRole(UserRole.ADMIN)
                    .build();

            userRepository.save(admin);
        };
    }


}
