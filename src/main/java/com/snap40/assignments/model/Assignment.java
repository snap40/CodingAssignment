package com.snap40.assignments.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Assignment {
    @Id
    private String id;

    private String patientId;

    private Timestamp openedTime;

    private Timestamp closedTime;

    @OneToMany(mappedBy = "assignment")
    private List<Device> devices;
}
