package com.sheetconn.connector.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sheetconn.connector.model.UserConnectorConfig;

public interface UserConnectorConfigRepository extends JpaRepository<UserConnectorConfig, String> {

    List<UserConnectorConfig> findAllByUid(String uid);
}
