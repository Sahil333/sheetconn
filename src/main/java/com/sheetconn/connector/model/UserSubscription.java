package com.sheetconn.connector.model;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@Getter
@AllArgsConstructor
public class UserSubscription {
    
    @Id
    private String id;

    @OneToOne
    @JoinColumn(name = "uid")
    private User user;

    @Column(name = "subscription_type")
    private Subscription subscription;

    @Column(name = "active")
    private boolean active;

    @Column(name = "end_date")
    private ZonedDateTime endDate;
}
