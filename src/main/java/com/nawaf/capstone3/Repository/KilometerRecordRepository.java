package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.KilometerRecord;
import com.nawaf.capstone3.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KilometerRecordRepository extends JpaRepository<KilometerRecord ,Integer> {

    KilometerRecord findKilometerRecordById(Integer id);

}
