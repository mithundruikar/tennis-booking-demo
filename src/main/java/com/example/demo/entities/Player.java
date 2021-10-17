package com.example.demo.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@EqualsAndHashCode
@ToString
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "contact_number" }) })
public class Player implements Serializable {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Exclude
    private long id;
    private String name;
    @Column(name = "contact_number")
    private String contactNumber;

    public Player() { }

    public Player(String name, String contactNumber) {
        this.name = name;
        this.contactNumber = contactNumber;
    }


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

}
