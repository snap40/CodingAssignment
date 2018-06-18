package com.snap40.assignments.services;

import com.snap40.assignments.model.Assignment;
import com.snap40.assignments.model.Device;
import com.snap40.assignments.repositories.DeviceRepository;
import com.snap40.assignments.repositories.AssignmentRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DefaultAssignmentService implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final DeviceRepository deviceRepository;

    public DefaultAssignmentService(AssignmentRepository assignmentRepository, DeviceRepository deviceRepository) {
        this.assignmentRepository = assignmentRepository;
        this.deviceRepository = deviceRepository;
    }

    @Override
    public void openAssignment(String patientId) {
        /*  Since we don't have a patient lookup, let's assume that a patientID is valid as long
            as it is not empty or null. In real environment we would want to verify that it is indeed a real
            patient. */
        if (patientId == null || patientId.isEmpty()) {
            throw new RuntimeException("PatientId cannot be null or empty.");
        }

        Assignment newAssignment = new Assignment();

        UUID uuid = UUID.randomUUID();
        newAssignment.setId(uuid.toString()); // Adding as UUID since we don't have a assignmentID passed

        newAssignment.setPatientId(patientId);
        newAssignment.setOpenedTime(new Timestamp(System.currentTimeMillis()));

        try {
            assignmentRepository.save(newAssignment);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void closeAssignment(String assignmentId) {
        Assignment assignmentToClose = findAssignment(assignmentId);
        assignmentToClose.setClosedTime(new Timestamp(System.currentTimeMillis()));

        try {
            assignmentRepository.save(assignmentToClose);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void addDeviceToAssignment(String assignmentId, String deviceId) {
        //Ensure that our input data is valid
        if(deviceId == null || deviceId.isEmpty()){
            throw new RuntimeException("DeviceId cannot be null or empty.");
        }

        //We will first need to create an assignment and then we can add a device
        Assignment toAssign = findAssignment(assignmentId);

        // Device can only be added if it doesn't exist in the db yet or if it's not assigned to another patient
        Device device = null;
        try{
            device = deviceRepository.findDeviceByID(deviceId);
        }catch(Exception e){
            e.printStackTrace();
        }

        //No device found, we can add it
        if(device == null){
            device = new Device();
            device.setId(deviceId);
            device.setAssignedTime(new Timestamp(System.currentTimeMillis()));
            device.setAssignment(toAssign);

            try {
                deviceRepository.save(device);
            }catch(Exception e){
                e.printStackTrace();
            }
        }// This is an existing device
        else{
            if(device.getUnassignedTime() == null && device.getAssignedTime() != null) { //check if currently assigned
                throw new RuntimeException("Cannot add device to assignment as it is already assigned.");
            }else{
                device.setAssignedTime(new Timestamp(System.currentTimeMillis()));
                device.setUnassignedTime(null);
                device.setAssignment(toAssign);
                try {
                    deviceRepository.save(device);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void removeDeviceFromAssignment(String assignmentId, String deviceId) {
        //Ensure that our input data is valid
        if(deviceId == null || deviceId.isEmpty()){
            throw new RuntimeException("DeviceId cannot be null or empty.");
        }
        // Ensure that our input data is valid
        if (assignmentId == null || assignmentId.isEmpty()) {
            throw new RuntimeException("AssignmentId cannot be null or empty.");
        }

        // Device can only be removed from assignment if it is currently assigned to it
        Assignment assignment = findAssignment(assignmentId);
        Device device = null;

        try {
            device = deviceRepository.findDeviceByIDAndAssignment(deviceId, assignment);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(device == null){
            throw new RuntimeException("Device not assigned to this assignment.");
        }else{
            if(device.getUnassignedTime() != null){
                throw new RuntimeException("Device has already been unassigned from this assignment");
            }else{
                device.setUnassignedTime(new Timestamp(System.currentTimeMillis()));
                try{
                    deviceRepository.save(device);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Assignment findAssignment(String assignmentId) {
        // Ensure that our input data is valid
        if (assignmentId == null || assignmentId.isEmpty()) {
            throw new RuntimeException("AssignmentId cannot be null or empty.");
        }
        Assignment retrieved = null;

        try {
           retrieved = assignmentRepository.findAssignmentById(assignmentId);
        } catch (Exception e){
            e.printStackTrace();
        }

        if(retrieved == null){
            throw new RuntimeException("Cannot find an Assignment with AssignmentId "+assignmentId);
        }

        return retrieved;
    }

    @Override
    public List<Assignment> findAssignmentsForDevice(String deviceId) {
        //Ensure that our input data is valid
        if(deviceId == null || deviceId.isEmpty()){
            throw new RuntimeException("DeviceId cannot be null or empty.");
        }

        /*
        Because the ID of the device is unique, we cannot have the same device assigned to more than
        1 assignment as we would violate this constraint. This could only work if we ever overwrite the assignmentid
        or make a multi-field unique ID where we take into account a double of ID and AssignmentID
         */

        List<Assignment> assignments = new ArrayList<>();

        try {
            Device d = deviceRepository.findDeviceByID(deviceId);
            if(d.getAssignment() != null && d.getUnassignedTime() == null){
                Assignment a = assignmentRepository.findAssignmentById(d.getAssignment().getId());
                assignments.add(a);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return assignments;
    }

    @Override
    public List<Assignment> findAssignmentsForPatient(String patientId) {
        //Ensure that our input data is valid
        if(patientId == null || patientId.isEmpty()){
            throw new RuntimeException("PatientId cannot be null or empty.");
        }

        List<Assignment> assignments = new ArrayList<>();

        try{
            assignments = assignmentRepository.findAssignmentsByPatientId(patientId);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(assignments == null || assignments.size() == 0){
            throw new RuntimeException("Cannot find an Assignment with PatientID "+patientId);
        }

        return assignments;
    }
}
