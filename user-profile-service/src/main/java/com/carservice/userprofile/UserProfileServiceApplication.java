package com.carservice.userprofile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.carservice.userprofile.entity.AdminProfile;
import com.carservice.userprofile.repository.AdminProfileRepository;
import com.carservice.userprofile.util.HashUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class UserProfileServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserProfileServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(AdminProfileRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                AdminProfile admin = new AdminProfile();
                admin.setUsername("admin");
                admin.setPassword(HashUtil.hashPassword("adminpassword"));
                admin.setRole("admin");
                repository.save(admin);
                System.out.println(">>> Database Seeded: Default Admin Created (username: admin, password: adminpassword)");
            }
        };
    }
}
