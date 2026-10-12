package com.emergency.system.service;

import com.emergency.system.entity.AppUser;
import com.emergency.system.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        AppUser appUser = appUserRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "No account found for this email."
                        ));

        String role = appUser.getRole().name();

        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        return User.withUsername(appUser.getEmail())
                .password(appUser.getPassword())
                .authorities(role)
                .build();
    }
}
