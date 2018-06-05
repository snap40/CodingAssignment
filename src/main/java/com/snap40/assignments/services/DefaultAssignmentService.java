package com.snap40.assignments.services;

import com.snap40.assignments.model.Assignment;
import com.snap40.assignments.repositories.DeviceRepository;
import com.snap40.assignments.repositories.AssignmentRepository;

import java.util.List;

public class DefaultAssignmentService implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final DeviceRepository deviceRepository;

    public DefaultAssignmentService(AssignmentRepository assignmentRepository, DeviceRepository deviceRepository) {
        this.assignmentRepository = assignmentRepository;
        this.deviceRepository = deviceRepository;
    }

    @Override
    public void openAssignment(String patientId) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void closeAssignment(String assignmentId) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void addDeviceToAssignment(String assignmentId, String deviceId) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void removeDeviceFromAssignment(String assignmentId, String deviceId) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Assignment findAssignment(String assignmentId) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public List<Assignment> findAssignmentForDevice(String deviceId) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public List<Assignment> findAssignmentForPatient(String patientId) {
        throw new RuntimeException("Not yet implemented");
    }
}
