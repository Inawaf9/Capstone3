package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Integer> {

    Receipt findReceiptById(Integer id);
}
