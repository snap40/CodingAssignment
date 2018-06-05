package com.snap40.assignments.services;

import com.snap40.assignments.model.Assignment;

import java.util.List;

public interface AssignmentService {
    void openAssignment(String patientId);
    void closeAssignment(String assignmentId);
    void addDeviceToAssignment(String assignmentId, String deviceId);
    void removeDeviceFromAssignment(String assignmentId, String deviceId);
    Assignment findAssignment(String assignmentId);
    List<Assignment> findAssignmentsForDevice(String deviceId);
    List<Assignment> findAssignmentsForPatient(String patientId);
}
