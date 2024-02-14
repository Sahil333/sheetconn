package com.sheetconn.connector.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

@Entity
@Getter
@AllArgsConstructor
public class UserSubscription {
    
    @Id
    private String id;

    @Column(name = "uid")
    private String uid;

    @Column(name = "subscription_type")
    private Subscription subscription;

    @Column(name = "active")
    private boolean active;

    @Column(name = "end_date")
    private ZonedDateTime endDate;
}
