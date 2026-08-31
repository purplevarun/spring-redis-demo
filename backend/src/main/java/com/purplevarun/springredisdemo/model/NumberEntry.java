package com.purplevarun.springredisdemo.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "numbers")
public class NumberEntry implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private int value;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public NumberEntry() {}

    public NumberEntry(UUID id, int value, Instant createdAt) {
        this.id = id;
        this.value = value;
        this.createdAt = createdAt;
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
