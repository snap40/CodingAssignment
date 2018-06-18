package com.snap40.assignments.controller;

import com.snap40.assignments.model.Assignment;
import com.snap40.assignments.model.Device;
import com.snap40.assignments.repositories.AssignmentRepository;
import com.snap40.assignments.repositories.DeviceRepository;
import com.snap40.assignments.services.AssignmentService;
import com.snap40.assignments.services.DefaultAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.ws.Response;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class AssignmentController {

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    DeviceRepository deviceRepository;

    @GetMapping("/openAssignment")
    @ResponseBody
    public String openAssignment(@RequestParam(name="patientId", required=true) String patientId) {
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
            return e.getMessage();
        }

        return "OK";
    }

    @GetMapping("/closeAssignment")
    @ResponseBody
    public String closeAssignment(@RequestParam(name="assignmentId", required=true) String assignmentId) {
        DefaultAssignmentService service = new DefaultAssignmentService(assignmentRepository, deviceRepository);

        Assignment assignmentToClose = service.findAssignment(assignmentId);
        assignmentToClose.setClosedTime(new Timestamp(System.currentTimeMillis()));

        try {
            assignmentRepository.save(assignmentToClose);
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }

        return "OK";
    }

    @GetMapping("/addDeviceToAssignment")
    @ResponseBody
    public String addDeviceToAssignment(@RequestParam(name="assignmentId", required=true)String assignmentId, @RequestParam(name="deviceId", required=true)String deviceId) {
        DefaultAssignmentService service = new DefaultAssignmentService(assignmentRepository, deviceRepository);

        //Ensure that our input data is valid
        if(deviceId == null || deviceId.isEmpty()){
            return "DeviceId cannot be null or empty.";
        }

        //We will first need to create an assignment and then we can add a device
        Assignment toAssign = service.findAssignment(assignmentId);

        // Device can only be added if it doesn't exist in the db yet or if it's not assigned to another patient
        Device device = null;
        try{
            device = deviceRepository.findDeviceByID(deviceId);
        }catch(Exception e){
            e.printStackTrace();
            return e.getMessage();
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
                return e.getMessage();
            }
        }// This is an existing device
        else{
            if(device.getUnassignedTime() == null && device.getAssignedTime() != null) { //check if currently assigned
                return "Cannot add device to assignment as it is already assigned.";
            }else{
                device.setAssignedTime(new Timestamp(System.currentTimeMillis()));
                device.setUnassignedTime(null);
                device.setAssignment(toAssign);
                try {
                    deviceRepository.save(device);
                }catch(Exception e){
                    e.printStackTrace();
                    return e.getMessage();
                }
            }
        }

        return "OK";
    }

    @GetMapping("/removeDeviceFromAssignment")
    @ResponseBody
    public String removeDeviceFromAssignment(@RequestParam(name="assignmentId", required=true)String assignmentId, @RequestParam(name="deviceId", required=true)String deviceId) {
        DefaultAssignmentService service = new DefaultAssignmentService(assignmentRepository, deviceRepository);

        //Ensure that our input data is valid
        if(deviceId == null || deviceId.isEmpty()){
            return "DeviceId cannot be null or empty.";
        }
        // Ensure that our input data is valid
        if (assignmentId == null || assignmentId.isEmpty()) {
            return "AssignmentId cannot be null or empty.";
        }

        // Device can only be removed from assignment if it is currently assigned to it
        Assignment assignment = service.findAssignment(assignmentId);
        Device device = null;

        try {
            device = deviceRepository.findDeviceByIDAndAssignment(deviceId, assignment);
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }

        if(device == null){
            return "Device not assigned to this assignment.";
        }else{
            if(device.getUnassignedTime() != null){
                return "Device has already been unassigned from this assignment";
            }else{
                device.setUnassignedTime(new Timestamp(System.currentTimeMillis()));
                try{
                    deviceRepository.save(device);
                }catch(Exception e){
                    e.printStackTrace();
                    return e.getMessage();
                }
            }
        }

        return "OK";
    }

    @GetMapping("/findAssignment")
    @ResponseBody
    public String findAssignment(@RequestParam(name="assignmentId", required=true)String assignmentId) {
        // Ensure that our input data is valid
        if (assignmentId == null || assignmentId.isEmpty()) {
            return "AssignmentId cannot be null or empty.";
        }
        Assignment retrieved = null;

        try {
            retrieved = assignmentRepository.findAssignmentById(assignmentId);
        } catch (Exception e){
            e.printStackTrace();
        }

        if(retrieved == null){
           return "Cannot find an Assignment with AssignmentId "+assignmentId;
        }

        return retrieved.toString();
    }

    @GetMapping("/findAssignmentsForDevice")
    @ResponseBody
    public String findAssignmentsForDevice(@RequestParam(name="deviceId", required=true)String deviceId) {
        //Ensure that our input data is valid
        if(deviceId == null || deviceId.isEmpty()){
            return "DeviceId cannot be null or empty.";
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
            return e.getMessage();
        }

        return assignments.toString();
    }

    @GetMapping("/findAssignmentsForPatient")
    @ResponseBody
    public String findAssignmentsForPatient(@RequestParam(name="patientId", required=true)String patientId) {
        //Ensure that our input data is valid
        if(patientId == null || patientId.isEmpty()){
           return "PatientId cannot be null or empty.";
        }

        List<Assignment> assignments = new ArrayList<>();

        try{
            assignments = assignmentRepository.findAssignmentsByPatientId(patientId);
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }

        if(assignments == null || assignments.size() == 0){
            return "Cannot find an Assignment with PatientID "+patientId;
        }

        return assignments.toString();
    }
}
