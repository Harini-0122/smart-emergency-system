
package com.emergency.system.repository;

import com.emergency.system.entity.EmergencyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmergencyReportRepository
        extends JpaRepository<EmergencyReport, Long> {

    List<EmergencyReport> findByReporterEmailOrderByCreatedAtDesc(
            String reporterEmail);
}
