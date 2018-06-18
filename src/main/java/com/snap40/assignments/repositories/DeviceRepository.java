package com.snap40.assignments.repositories;

import com.snap40.assignments.model.Assignment;
import com.snap40.assignments.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {

    @Async
    @Query("SELECT d FROM Device d WHERE d.assignment = :assignmentID")
    List<Device> findDevicesByAssignmentID(@Param("assignmentID") String assignmentID);

    @Async
    @Query("SELECT d FROM Device d WHERE d.id = :id")
    Device findDeviceByID(@Param("id") String deviceID);

    @Async
    @Query("SELECT d FROM Device d WHERE d.id = :id AND d.assignment = :assignment")
    Device findDeviceByIDAndAssignment(@Param("id") String id, @Param("assignment") Assignment assignment);
}
