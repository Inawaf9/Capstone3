package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.Notification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    Notification findNotificationById(Integer id);
    Notification findTopByUserIdAndMessageStartingWithOrderByIdDesc(Integer userId, String prefix);
    boolean existsByUserIdAndMessageStartingWith(Integer userId, String prefix);
    List<Notification> findByUserId(Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from Notification n where n.id = :id")
    Notification findNotificationForUpdate(@Param("id") Integer id);
}
