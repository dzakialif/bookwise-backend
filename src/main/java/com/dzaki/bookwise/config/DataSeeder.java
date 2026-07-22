package com.dzaki.bookwise.config;

import java.time.OffsetDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dzaki.bookwise.entity.User;
import com.dzaki.bookwise.repo.UserRepo;
import com.dzaki.bookwise.security.BCrypt;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepo userRepo) {
        return args -> {
            if (!userRepo.existsByEmail("admin@bookwise.com")) {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@bookwise.com");
                admin.setPasswordHash(BCrypt.hashpw("Admin123", BCrypt.gensalt()));
                admin.setRole("ADMIN");
                admin.setIsActive(true);
                admin.setCreatedAt(OffsetDateTime.now());
                admin.setUpdatedAt(OffsetDateTime.now());
                userRepo.save(admin);

                System.out.println("✅ Admin user created: admin@bookwise.com / Admin123");
            }
        };
    }
}
