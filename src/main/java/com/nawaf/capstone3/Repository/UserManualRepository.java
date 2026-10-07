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

    @Modifying
    @Transactional
    @Query("""
            update UserManual u
            set u.status = 'ANALYZING'
            where u.id = :manualId
            and u.status <> 'ANALYZING'
            """)
    int claimForAnalysis(@Param("manualId") Integer manualId);

    @Modifying
    @Transactional
    @Query("""
            update UserManual u
            set u.status = :status
            where u.id = :manualId
            """)
    int updateStatus(@Param("manualId") Integer manualId, @Param("status") String status);
}