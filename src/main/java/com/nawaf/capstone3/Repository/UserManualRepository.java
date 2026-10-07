package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.UserManual;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserManualRepository extends JpaRepository<UserManual, Integer> {

    UserManual findUserManualById(Integer id);

    //للاستخدام في ميزات ال AI
    // ⚠ جديد: تحديث الحالة فقط بدون حفظ الكيان كله
    @Modifying
    @Transactional
    @Query("update UserManual m set m.status = :status where m.id = :id")
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    // ⚠ جديد: حجز ذري. يرجع 1 إذا نجح الحجز، و0 إذا كان قيد التحليل أو غير موجود
    @Modifying
    @Transactional
    @Query("update UserManual m set m.status = 'ANALYZING' where m.id = :id and m.status <> 'ANALYZING'")
    int claimForAnalysis(@Param("id") Integer id);
}