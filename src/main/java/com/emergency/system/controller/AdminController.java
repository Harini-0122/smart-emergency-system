
package com.emergency.system.controller;

import com.emergency.system.entity.EmergencyReport;
import com.emergency.system.repository.EmergencyReportRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EmergencyReportRepository repository;

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("PENDING", "IN_PROGRESS", "RESOLVED");

    public AdminController(EmergencyReportRepository repository) {
        this.repository = repository;
    }

    // Display all emergency reports
    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {

        List<EmergencyReport> reports = repository.findAll();

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

        return "admin-dashboard";
    }

    // Update the status of an emergency report
    @PostMapping("/reports/{id}/status")
    public String updateReportStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        String newStatus = status.trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(newStatus)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Invalid emergency status."
            );
            return "redirect:/admin/dashboard";
        }

        EmergencyReport report = repository.findById(id).orElse(null);

        if (report == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Emergency report not found."
            );
            return "redirect:/admin/dashboard";
        }

        if (!"IN_PROGRESS".equals(newStatus)
        && !"RESOLVED".equals(newStatus)) {
    report.setResponseStartedAt(null);
    report.setResolvedAt(null);
}

if ("IN_PROGRESS".equals(newStatus)
        && report.getResponseStartedAt() == null) {
    report.setResponseStartedAt(LocalDateTime.now());
}

if ("RESOLVED".equals(newStatus)) {
    if (report.getResponseStartedAt() == null) {
        report.setResponseStartedAt(LocalDateTime.now());
    }

    if (report.getResolvedAt() == null) {
        report.setResolvedAt(LocalDateTime.now());
    }
}

report.setStatus(newStatus);
repository.save(report);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Emergency status updated successfully!"
        );

        return "redirect:/admin/dashboard";
    }
    @GetMapping("/map")
public String showEmergencyMap(Model model) {
    List<EmergencyReport> reports = repository.findAll();

    long mappedReports = reports.stream()
            .filter(report -> report.getLatitude() != null
                    && report.getLongitude() != null
                    && Double.isFinite(report.getLatitude())
                    && Double.isFinite(report.getLongitude())
                    && report.getLatitude() >= -90
                    && report.getLatitude() <= 90
                    && report.getLongitude() >= -180
                    && report.getLongitude() <= 180)
            .count();

    model.addAttribute("reports", reports);
    model.addAttribute("totalReports", reports.size());
    model.addAttribute("mappedReports", mappedReports);

    return "admin-map";
}
}
