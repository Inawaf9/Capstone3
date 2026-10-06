package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Integer> {

    Receipt findReceiptById(Integer id);
    List<Receipt>findReceiptsByMaintenanceRecord_Vehicle_Id(Integer vehicleId);


    @Query("""
SELECT COALESCE(SUM(r.totalAmount),0)
FROM Receipt r
WHERE r.maintenanceRecord.vehicle.id=:vehicleId""")
    Double getTotalAmountByVehicleId(@Param("vehicleId") Integer vehicleId);
}
