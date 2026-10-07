package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.DTO.CompleteMaintenanceDTO;
import com.nawaf.capstone3.DTO.DueMaintenanceDTO;
import com.nawaf.capstone3.DTO.VehicleMaintenanceSummaryDTO;
import com.nawaf.capstone3.Model.MaintenanceRecord;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.MaintenanceRecordRepository;
import com.nawaf.capstone3.Repository.MaintenanceRuleRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceRecordService {
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceRuleRepository maintenanceRuleRepository;


    public List<MaintenanceRecord> getAll() {
        return maintenanceRecordRepository.findAll();
    }

    //تم اضافة التعديل
    public void addMaintenanceRecord(MaintenanceRecord maintenanceRecord) {
        Vehicle vehicle = vehicleRepository.findVehicleById(maintenanceRecord.getVehicle().getId());
        MaintenanceRule maintenanceRule = maintenanceRuleRepository.findMaintenanceRuleById(maintenanceRecord.getMaintenanceRule().getId());

        if (vehicle == null) {
            throw new ApiException("vehicle id not  found");
        }
        if (maintenanceRule == null) {
            throw new ApiException("maintenance rule id not found");
        }


        maintenanceRecordRepository.save(maintenanceRecord);
    }

    public void updateMaintenanceRecord(Integer maintenanceRecordId, MaintenanceRecord maintenanceRecord) {
        MaintenanceRecord oldMaintenanceRecord = maintenanceRecordRepository.findMaintenanceRecordById(maintenanceRecordId);
        if (oldMaintenanceRecord == null) {
            throw new ApiException("maintenance record id not found ");
        }
        oldMaintenanceRecord.setCost(maintenanceRecord.getCost());
        oldMaintenanceRecord.setKilometers(maintenanceRecord.getKilometers());
        oldMaintenanceRecord.setNote(maintenanceRecord.getNote());
        oldMaintenanceRecord.setServiceDate(maintenanceRecord.getServiceDate());
        oldMaintenanceRecord.setWorkshop(maintenanceRecord.getWorkshop());


        //ما نعدل عليها
//        oldMaintenanceRecord.setReceipts(maintenanceRecord.getReceipts());
//        oldMaintenanceRecord.setMaintenanceRule(maintenanceRecord.getMaintenanceRule());
//        oldMaintenanceRecord.setVehicle(maintenanceRecord.getVehicle());


        maintenanceRecordRepository.save(oldMaintenanceRecord);
    }

    public void deleteMaintenanceRecord(Integer maintenanceId) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findMaintenanceRecordById(maintenanceId);
        if (maintenanceRecord == null) {
            throw new ApiException("maintenance record id not found");

        }
        maintenanceRecordRepository.deleteById(maintenanceId);
    }

    //get by       id
    public MaintenanceRecord getMaintenanceRecordById(Integer maintenanceRecordId) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findMaintenanceRecordById(maintenanceRecordId);
        if (maintenanceRecord == null) {
            throw new ApiException("maintenance record ID not found");
        }
        return maintenanceRecord;
    }


    //5
    public VehicleMaintenanceSummaryDTO getVehicleMaintenanceHistory(Integer vehicleId) {
        // 1. التأكد من وجود المركبة
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);
        if (vehicle == null) {
            throw new ApiException("Vehicle not found");
        }

        // 2. جلب جميع سجلات الصيانة الخاصة بالمركبة
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByVehicleIdOrderByServiceDateDesc(vehicleId);

        // 3. تغليف البيانات وإرجاعها
        return new VehicleMaintenanceSummaryDTO(
                vehicle.getId(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getCurrentKilometers(),
                records.size(),
                records
        );
    }

//الصيانة المستحقة
    public List<DueMaintenanceDTO> getDueMaintenances(Integer vehicleId) {

        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);

        if (vehicle == null) {
            throw new ApiException("Vehicle not found");
        }

        int currentKm = 0;

        if (vehicle.getCurrentKilometers() != null) {
            currentKm = vehicle.getCurrentKilometers();
        }

        List<MaintenanceRule> rules =
                maintenanceRuleRepository.findByUserManualVehicleId(vehicleId);

        List<DueMaintenanceDTO> dueList = new ArrayList<>();

        for (int i = 0; i < rules.size(); i++) {
            MaintenanceRule rule = rules.get(i);

            Integer intervalKm = rule.getKilometerInterval();

            if (intervalKm == null || intervalKm <= 0) {
                continue;
            }

            MaintenanceRecord record = maintenanceRecordRepository
                    .findFirstByVehicleIdAndMaintenanceRule_ServiceNameOrderByKilometersDesc(
                            vehicleId, rule.getServiceName());

            int lastServiceKm = 0;

            if (record != null) {
                if (record.getKilometers() != null) {
                    lastServiceKm = record.getKilometers();
                }
            }

            int dueKm = lastServiceKm + intervalKm;
            int remainingKm = dueKm - currentKm;

            // مستحقة الآن فقط
            if (remainingKm == 0) {
                DueMaintenanceDTO dto = new DueMaintenanceDTO(
                        rule.getId(),
                        rule.getServiceName(),
                        dueKm,
                        currentKm,
                        remainingKm,
                        "DUE_NOW"
                );

                dueList.add(dto);
            }
        }

        return dueList;
    }

//الصيانة المتأخرة
    public List<DueMaintenanceDTO> getOverdueMaintenances(Integer vehicleId) {

        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);

        if (vehicle == null) {
            throw new ApiException("Vehicle not found");
        }

        int currentKm = 0;

        if (vehicle.getCurrentKilometers() != null) {
            currentKm = vehicle.getCurrentKilometers();
        }

        List<MaintenanceRule> rules =
                maintenanceRuleRepository.findByUserManualVehicleId(vehicleId);

        List<DueMaintenanceDTO> overdueList = new ArrayList<>();

        for (int i = 0; i < rules.size(); i++) {
            MaintenanceRule rule = rules.get(i);

            Integer intervalKm = rule.getKilometerInterval();

            if (intervalKm == null || intervalKm <= 0) {
                continue;
            }

            MaintenanceRecord record = maintenanceRecordRepository
                    .findFirstByVehicleIdAndMaintenanceRule_ServiceNameOrderByKilometersDesc(
                            vehicleId, rule.getServiceName());

            int lastServiceKm = 0;

            if (record != null) {
                if (record.getKilometers() != null) {
                    lastServiceKm = record.getKilometers();
                }
            }

            int dueKm = lastServiceKm + intervalKm;
            int remainingKm = dueKm - currentKm;

            // متأخرة فقط
            if (remainingKm < 0) {
                DueMaintenanceDTO dto = new DueMaintenanceDTO(
                        rule.getId(),
                        rule.getServiceName(),
                        dueKm,
                        currentKm,
                        remainingKm,
                        "OVERDUE"
                );

                overdueList.add(dto);
            }
        }

        return overdueList;
    }


//  بالايام    اقرب صيانة متوقعة

    public DueMaintenanceDTO getNextUpcomingMaintenance(Integer vehicleId) {
        // 1. التحقق من وجود المركبة
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);
        if (vehicle == null) {
            throw new ApiException("Vehicle not found");
        }

        // 2. قراءة العداد الحالي للمركبة
        int currentKm = 0;
        if (vehicle.getCurrentKilometers() != null) {
            currentKm = vehicle.getCurrentKilometers();
        }

        // 3. جلب جميع قواعد الصيانة الخاصة بالمركبة
        List<MaintenanceRule> rules = maintenanceRuleRepository.findByUserManualVehicleId(vehicleId);

        DueMaintenanceDTO nextMaintenance = null;
        int minRemainingKm = Integer.MAX_VALUE; // نبدأ بأعلى قيمة ممكنة للمقارنة

        // 4. المرور على قواعد الصيانة بـ For Loop عادية
        for (int i = 0; i < rules.size(); i++) {
            MaintenanceRule rule = rules.get(i);

            int intervalKm = rule.getKilometerInterval();


            MaintenanceRecord record = maintenanceRecordRepository
                    .findFirstByVehicleIdAndMaintenanceRule_ServiceNameOrderByServiceDateDesc(vehicleId, rule.getServiceName());

            int lastServiceKm = 0;
            if (record != null) {
                if (record.getKilometers() != null) {
                    lastServiceKm = record.getKilometers();
                }
            }

            int dueKm = lastServiceKm + intervalKm;
            int remainingKm = dueKm - currentKm;

            // البحث عن القيمة الموجبة الأقل (أقرب صيانة مستقبلية لم تُستحق بعد)
            if (remainingKm > 0) {
                if (remainingKm < minRemainingKm) {
                    minRemainingKm = remainingKm;

                    nextMaintenance = new DueMaintenanceDTO(
                            rule.getId(),
                            rule.getServiceName(),
                            dueKm,
                            currentKm,
                            remainingKm,
                            "UPCOMING"
                    );
                }
            }
        }

        return nextMaintenance;
    }


//11
    public void completMaintenance(Integer vehicleId , Integer maintenanceRuleId, CompleteMaintenanceDTO completeMaintenanceDTO){
        Vehicle vehicle=vehicleRepository.findVehicleById(vehicleId);
        MaintenanceRule maintenanceRule=maintenanceRuleRepository.findMaintenanceRuleById(maintenanceRuleId);
        if(vehicle==null){
            throw new ApiException("vehicle id not found");
        }
        if(maintenanceRule==null){
            throw new ApiException("maintenance rule id not found");
        }
        //توثيق الصيانة في الداتا بيس

        MaintenanceRecord maintenanceRecord=new MaintenanceRecord();
        maintenanceRecord.setVehicle(vehicle);
        maintenanceRecord.setMaintenanceRule(maintenanceRule);
        maintenanceRecord.setKilometers(completeMaintenanceDTO.getKilometers());
        maintenanceRecord.setServiceDate(LocalDate.now());
        maintenanceRecord.setCost(completeMaintenanceDTO.getCost());
        maintenanceRecord.setWorkshop(completeMaintenanceDTO.getWorkshop());
        maintenanceRecord.setNote(completeMaintenanceDTO.getNote());

        maintenanceRecordRepository.save(maintenanceRecord);
        //تحديث العدادا لو اكبر من القراءة القديمة
        //اضافة قيمة النل بحيل لو السيارة جديدة ما يعطيني خطأ
        if (vehicle.getCurrentKilometers() == null || completeMaintenanceDTO.getKilometers() > vehicle.getCurrentKilometers()) {
            vehicle.setCurrentKilometers(completeMaintenanceDTO.getKilometers());
            vehicleRepository.save(vehicle);
        }


    }


}