package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Repository.MaintenanceRecordRepository;
import com.nawaf.capstone3.Repository.UserManualRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service

public class ManualAnalysisService {

    private final UserManualRepository manualRepo;
    private final ManualAnalysisWorker worker;
    private final MaintenanceRecordRepository recordRepo;   // ⚠ أضفه للحقول وللـ constructor


    public ManualAnalysisService(UserManualRepository manualRepo, ManualAnalysisWorker worker, MaintenanceRecordRepository recordRepo) {
        this.manualRepo = manualRepo;
        this.worker = worker;
        this.recordRepo = recordRepo;
    }


    public void startAnalysis(Integer manualId) {

        if (recordRepo.existsByMaintenanceRule_UserManual_Id(manualId)) {      // ⚠ جديد
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Manual rules are already used by maintenance records; re-analysis is not allowed");
        }// ⚠ Integer
        // ⚠ حجز ذري: يحل مشكلة السباق ويضع الحالة ANALYZING (String)
        int claimed = manualRepo.claimForAnalysis(manualId);

        if (claimed == 0) {
            if (!manualRepo.existsById(manualId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Manual not found");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already analyzing");
        }

        worker.analyze(manualId);   // ⚠ استدعاء من bean آخر، فيعمل @Async فعلاً
    }

    public String getStatus(Integer manualId) {                   // ⚠ يرجع String
        return manualRepo.findById(manualId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manual not found"))
                .getStatus();
    }
}