package com.sheetconn.connector.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_info")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    // We only use google provider as login provider and hence the same uid provided by google
    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "name")
    private String name;
    
    @Column(name = "email_id")
    private String emailId;
    
    @Column(name = "picture_url")
    private String pictureUrl;
}
