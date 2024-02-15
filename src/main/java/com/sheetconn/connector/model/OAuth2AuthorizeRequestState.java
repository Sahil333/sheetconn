package com.sheetconn.connector.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OAuth2AuthorizeRequestState {

    @Id
    @Column(name = "state")
    private String state;

    @Column(name = "uid")
    private String uid;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;
}
