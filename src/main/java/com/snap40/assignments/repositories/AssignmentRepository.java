package com.snap40.assignments.repositories;

import com.snap40.assignments.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    @Async
    @Query("SELECT a FROM Assignment a WHERE a.id = :id")
    Assignment findAssignmentById(@Param("id") String idAssignment);

    @Async
    @Query("SELECT a FROM Assignment a WHERE a.patientId = :patientId")
    List<Assignment> findAssignmentsByPatientId(@Param("patientId") String patientId);

}
