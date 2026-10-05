package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User ,Integer> {

    User findUsersById(Integer id);
}
