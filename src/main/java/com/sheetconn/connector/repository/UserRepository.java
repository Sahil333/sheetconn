package com.sheetconn.connector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sheetconn.connector.model.User;

public interface UserRepository extends JpaRepository<User, String> {
    
}
