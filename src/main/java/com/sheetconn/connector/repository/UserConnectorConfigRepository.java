package com.sheetconn.connector.repository;

import java.util.List;

import com.sheetconn.connector.model.ConnectorType;
import com.sheetconn.connector.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sheetconn.connector.model.UserConnectorConfig;

public interface UserConnectorConfigRepository extends JpaRepository<UserConnectorConfig, String> {

    List<UserConnectorConfig> findAllByUser(User user);

    List<UserConnectorConfig> findAllByUserAndType(User user, ConnectorType type);
}
