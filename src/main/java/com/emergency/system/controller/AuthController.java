
package com.emergency.system.controller;

import com.emergency.system.dto.RegistrationRequest;
import com.emergency.system.entity.AppUser;
import com.emergency.system.repository.AppUserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {

        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Display registration page
    @GetMapping("/register")
    public String showRegistration(Model model) {

        if (!model.containsAttribute("registrationRequest")) {
            model.addAttribute(
                "registrationRequest",
                new RegistrationRequest()
            );
        }

        return "register";
    }

    // Process registration form
    @PostMapping("/register")
    public String register(
            @ModelAttribute("registrationRequest")
            RegistrationRequest request,
            RedirectAttributes redirectAttributes) {

        String name = request.getName() == null
                ? "" : request.getName().trim();

        String email = request.getEmail() == null
                ? "" : request.getEmail().trim();

        String password = request.getPassword() == null
                ? "" : request.getPassword();

        // Validate name
        if (!name.matches("[A-Za-z][A-Za-z0-9 ]{1,79}")) {
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Name must start with a letter and contain 2 to 80 characters."
            );

            return "redirect:/register";
        }

        // Validate Gmail address
        if (!email.matches(
                "(?i)[A-Za-z0-9._%+-]+@gmail\\.com")) {

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Please enter a valid email ending with @gmail.com."
            );

            return "redirect:/register";
        }

        // Validate password
        if (password.length() < 8
                || password.length() > 72
                || !password.matches("(?s).*[A-Z].*")
                || !password.matches("(?s).*[a-z].*")
                || !password.matches("(?s).*[0-9].*")
                || !password.matches("(?s).*[^A-Za-z0-9].*")) {

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Password must be 8-72 characters and contain uppercase, lowercase, a number, and a special character."
            );

            return "redirect:/register";
        }

        // Check whether the email is already registered
        if (appUserRepository.existsByEmailIgnoreCase(email)) {

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "User already exists. Please login."
            );

            return "redirect:/register";
        }

        // Create a new citizen account
        AppUser user = new AppUser();

        user.setName(name);
        user.setEmail(email);

        // Always encode the password before saving
        user.setPassword(passwordEncoder.encode(password));

        // Preserve the expected Spring Security role format
        user.setRole(com.emergency.system.entity.UserRole.CITIZEN);
        try {
            appUserRepository.saveAndFlush(user);

        } catch (DataIntegrityViolationException exception) {

            // Also handle a duplicate detected by a database constraint
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "User already exists. Please login."
            );

            return "redirect:/register";
        }

        // Registration completed successfully
        redirectAttributes.addFlashAttribute(
            "registrationSuccess",
            "Registration successful! Please log in."
        );

        return "redirect:/login";
    }
}
