package com.sheetconn.connector.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserSubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
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
