package com.example.demo.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

@Entity
@ToString
public class Court  implements Serializable {
    @Id
    private long id;

    public Court() {}


    public Court(Long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Court court = (Court) o;
        return id == court.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
