
package com.emergency.system.controller;

import com.emergency.system.entity.EmergencyReport;
import com.emergency.system.repository.EmergencyReportRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class EmergencyReportController {

    private final EmergencyReportRepository repository;

    public EmergencyReportController(
            EmergencyReportRepository repository) {
        this.repository = repository;
    }

    // Display emergency report form
    @GetMapping("/user/report-emergency")
    public String showReportForm() {
        return "report-emergency";
    }

    // Submit emergency report with map coordinates
@PostMapping("/user/report-emergency")
public String submitReport(
        @RequestParam String title,
        @RequestParam String emergencyType,
        @RequestParam String location,
        @RequestParam String description,
        @RequestParam(required = false) Double latitude,
        @RequestParam(required = false) Double longitude,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    // Check authentication
    if (authentication == null
            || !authentication.isAuthenticated()
            || "anonymousUser".equals(authentication.getName())) {

        return "redirect:/login";
    }

    // Validate required fields
    if (title.isBlank()
            || emergencyType.isBlank()
            || location.isBlank()
            || description.isBlank()) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Please fill in all required fields."
        );

        return "redirect:/user/report-emergency";
    }

    // Validate map coordinates
    boolean bothCoordinatesPresent =
            latitude != null && longitude != null;

    boolean neitherCoordinatePresent =
            latitude == null && longitude == null;

    boolean validCoordinates =
            neitherCoordinatePresent
            || (bothCoordinatesPresent
                && Double.isFinite(latitude)
                && Double.isFinite(longitude)
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180);

    if (!validCoordinates || neitherCoordinatePresent) {
        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Please select a valid location on the map."
        );

        return "redirect:/user/report-emergency";
    }

    // Create emergency report
    EmergencyReport report = new EmergencyReport();

    report.setTitle(title.trim());
    report.setEmergencyType(emergencyType.trim());
    report.setLocation(location.trim());
    report.setDescription(description.trim());
    report.setReporterEmail(authentication.getName());
    report.setStatus("PENDING");

    // Save selected map coordinates
    report.setLatitude(latitude);
    report.setLongitude(longitude);

    // Save to MySQL
    repository.save(report);

    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Emergency report submitted successfully!"
    );

    return "redirect:/user/dashboard";
}

    // Display citizen dashboard and the citizen's own reports
    @GetMapping("/user/dashboard")
    public String showUserDashboard(
            Authentication authentication,
            Model model) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        List<EmergencyReport> reports =
                repository.findByReporterEmailOrderByCreatedAtDesc(
                        authentication.getName()
                );

        model.addAttribute("reports", reports);
        model.addAttribute("totalReports", reports.size());

        long pendingCount = reports.stream()
                .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                .count();

        long inProgressCount = reports.stream()
                .filter(r -> "IN_PROGRESS".equalsIgnoreCase(r.getStatus()))
                .count();

        long resolvedCount = reports.stream()
                .filter(r -> "RESOLVED".equalsIgnoreCase(r.getStatus()))
                .count();

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount", resolvedCount);

        return "user-dashboard";
    }
}
