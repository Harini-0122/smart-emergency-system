
package com.emergency.system.config;

import com.emergency.system.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String ADMIN_EMAIL = "harini@smart.com";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider)
            throws Exception {

        http
            .authenticationProvider(authenticationProvider)

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/admin-login",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // Only the designated administrator can access /admin/**
                .requestMatchers("/admin/**").access((authentication, context) -> {
                    var currentUser = authentication.get();

                    boolean correctEmail =
                            ADMIN_EMAIL.equalsIgnoreCase(currentUser.getName());

                    boolean hasAdminRole = currentUser.getAuthorities()
                            .stream()
                            .anyMatch(authority ->
                                    authority.getAuthority().equals("ROLE_ADMIN"));

                    return new AuthorizationDecision(
                            currentUser.isAuthenticated()
                                    && correctEmail
                                    && hasAdminRole
                    );
                })

                .requestMatchers("/user/**").hasRole("CITIZEN")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")

                .successHandler((request, response, authentication) -> {

                    String email = authentication.getName();

                    boolean isAdmin = authentication.getAuthorities()
                            .stream()
                            .anyMatch(authority ->
                                    authority.getAuthority().equals("ROLE_ADMIN"));

                    boolean isCitizen = authentication.getAuthorities()
                            .stream()
                            .anyMatch(authority ->
                                    authority.getAuthority().equals("ROLE_CITIZEN"));

                    if (isAdmin && ADMIN_EMAIL.equalsIgnoreCase(email)) {
                        response.sendRedirect("/admin/dashboard");
                    } else if (isCitizen) {
                        response.sendRedirect("/user/dashboard");
                    } else {
                        response.sendRedirect("/login?error=true");
                    }
                })

                .failureUrl("/login?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
