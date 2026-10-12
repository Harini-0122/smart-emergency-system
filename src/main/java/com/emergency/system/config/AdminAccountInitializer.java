
package com.emergency.system.config;

import com.emergency.system.entity.AppUser;
import com.emergency.system.entity.UserRole;
import com.emergency.system.repository.AppUserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminAccountInitializer {

    @Bean
    ApplicationRunner initializeAdmin(
            AppUserRepository repository,
            PasswordEncoder encoder,
            @Value("${app.admin.password:}") String adminPassword) {

        return args -> {

            String email = "harini@smart.com";

            if (adminPassword == null
                    || adminPassword.isBlank()
                    || adminPassword.length() < 12
                    || adminPassword.length() > 72) {

                throw new IllegalStateException(
                        "Configure ADMIN_PASSWORD with 12-72 characters."
                );
            }

            var existing = repository.findByEmailIgnoreCase(email);

            if (existing.isPresent()) {

                AppUser admin = existing.get();

                if (admin.getRole() != UserRole.ADMIN) {
                    throw new IllegalStateException(
                            "Reserved administrator email has a non-admin role."
                    );
                }

                // Update the existing admin password
                admin.setPassword(encoder.encode(adminPassword));
                repository.save(admin);

                System.out.println(
                        "Administrator password updated successfully."
                );

            } else {

                // Create the administrator if it does not exist
                AppUser admin = new AppUser();
                admin.setName("System Administrator");
                admin.setEmail(email);
                admin.setPassword(encoder.encode(adminPassword));
                admin.setRole(UserRole.ADMIN);

                repository.save(admin);

                System.out.println(
                        "Administrator account initialized."
                );
            }
        };
    }
}
