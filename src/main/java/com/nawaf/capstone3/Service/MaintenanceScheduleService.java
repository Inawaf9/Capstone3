package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Model.MaintenanceRecord;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Model.Vehicle;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** One deterministic due-state policy shared by reminders and AI context. */
@Service
public class MaintenanceScheduleService {
    public record DueState(String type, MaintenanceRecord latest, String cycle) {}

    public DueState evaluate(Vehicle vehicle, MaintenanceRule rule, List<MaintenanceRecord> history, LocalDate today) {
        MaintenanceRecord latest = history.stream()
                .filter(r -> r.getMaintenanceRule() != null && Objects.equals(r.getMaintenanceRule().getId(), rule.getId()))
                .max(Comparator.comparing(MaintenanceRecord::getServiceDate)
                        .thenComparing(r -> r.getId() == null ? 0 : r.getId())).orElse(null);
        if (rule.getKilometers() != null) {
            if (latest != null) return new DueState("COMPLETED", latest, "scheduled");
            if (vehicle.getCurrentKilometers() == null) return new DueState("UNKNOWN", null, "scheduled");
            long remaining = (long) rule.getKilometers() - vehicle.getCurrentKilometers();
            String state = remaining < 0 ? "MAINTENANCE_OVERDUE" : remaining == 0 ? "MAINTENANCE_DUE"
                    : remaining <= 500 ? "MAINTENANCE_SOON" : "NOT_DUE";
            return new DueState(state, null, "scheduled");
        }
        if (rule.getMonthInterval() == null) return new DueState("UNKNOWN", latest, "condition");
        // Registration is not a known last-service date. Do not invent a time baseline.
        if (latest == null) return new DueState("UNKNOWN", null, "initial");
        LocalDate due = latest.getServiceDate().plusMonths(rule.getMonthInterval());
        String state = today.isAfter(due) ? "MAINTENANCE_OVERDUE" : today.equals(due) ? "MAINTENANCE_DUE"
                : !today.plusDays(7).isBefore(due) ? "MAINTENANCE_SOON" : "NOT_DUE";
        return new DueState(state, latest, "record-" + latest.getId());
    }

    public boolean requiresReminder(DueState state) { return state.type().startsWith("MAINTENANCE_"); }
}
