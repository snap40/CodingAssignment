import com.snap40.assignments.Application;
import com.snap40.assignments.model.Assignment;
import com.snap40.assignments.model.Device;
import com.snap40.assignments.repositories.AssignmentRepository;
import com.snap40.assignments.repositories.DeviceRepository;
import com.snap40.assignments.services.AssignmentService;
import com.snap40.assignments.services.DefaultAssignmentService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.List;

import static junit.framework.TestCase.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(
        classes = Application.class)
@AutoConfigureMockMvc
public class DeviceTest {
    private Assignment testAssignment;
    private Assignment testAssignment2;
    private final String deviceID = "test1";
    private final int startTime = 300000;
    private final int endTime = 350000;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Before
    public void setExampleAssignment(){
        testAssignment = new Assignment();
        testAssignment.setId("TEST_A");
        testAssignment.setPatientId("PATIENT_A");
        testAssignment.setOpenedTime(new Timestamp(300000));
        testAssignment.setClosedTime(new Timestamp(900000));

        testAssignment2 = new Assignment();
        testAssignment2.setId("TEST_B");
        testAssignment2.setPatientId("PATIENT_B");
        testAssignment2.setOpenedTime(new Timestamp(300000));
        testAssignment2.setClosedTime(new Timestamp(900000));
    }

    @Test
    public void testDeviceModel(){
        Device d = new Device();
        d.setId(deviceID);
        d.setAssignment(testAssignment);
        d.setAssignedTime(new Timestamp(startTime));
        d.setUnassignedTime(new Timestamp(endTime));

        Assert.assertEquals(deviceID, d.getId());
        Assert.assertEquals(testAssignment, d.getAssignment());
        Assert.assertEquals(new Timestamp(startTime), d.getAssignedTime());
        Assert.assertEquals(new Timestamp(endTime), d.getUnassignedTime());

        Device d2 = new Device();
        d2.setId(deviceID);
        d2.setAssignment(testAssignment);
        d2.setAssignedTime(new Timestamp(startTime));
        d2.setUnassignedTime(new Timestamp(endTime));

        Assert.assertTrue(d.equals(d2));
    }

    @Test
    public void testRepoConnections(){
        Assert.assertNotNull(deviceRepository);
        Assert.assertNotNull(assignmentRepository);

        DefaultAssignmentService service = new DefaultAssignmentService(assignmentRepository, deviceRepository);

        Assert.assertNotNull(service);
    }

    @Test
    public void testCreateAndDeleteDeviceRecord(){
        //Because of FK we need an assignment first
        assignmentRepository.save(testAssignment);

        Device d = new Device();
        d.setId(deviceID);
        d.setAssignment(testAssignment);
        d.setAssignedTime(new Timestamp(startTime));
        d.setUnassignedTime(new Timestamp(endTime));

        //DefaultAssignmentService service = new DefaultAssignmentService(assignmentRepository, deviceRepository);
        deviceRepository.save(d);

        Assert.assertTrue(testAssignment.getId().equals(assignmentRepository.findAssignmentById(testAssignment.getId()).getId()));
        Assert.assertTrue(d.getId().equals(deviceRepository.findDeviceByID(deviceID).getId()));

        deviceRepository.delete(d);
        assignmentRepository.delete(testAssignment);

        Assert.assertNull(assignmentRepository.findAssignmentById(testAssignment.getId()));
        Assert.assertNull(deviceRepository.findDeviceByID(d.getId()));
    }

    @Test
    public void testAddingRemovingUnassignedDevice(){
        //Because of FK we need an assignment first
        //Would want to do this in isolation and have a test assignment set up since not testing assignment
        assignmentRepository.save(testAssignment);

        DefaultAssignmentService service = new DefaultAssignmentService(assignmentRepository, deviceRepository);

        Device d = new Device();
        d.setId(deviceID);
        deviceRepository.save(d);

        //Check that no assignments
        List<Assignment> assignmentList = service.findAssignmentsForDevice(d.getId());
        Assert.assertEquals(0, assignmentList.size());

        service.addDeviceToAssignment(testAssignment.getId(), d.getId());
        assignmentList = service.findAssignmentsForDevice(d.getId());
        Assert.assertEquals(1, assignmentList.size());

        //Check that no end date yet
        Assert.assertNotNull(deviceRepository.findDeviceByID(d.getId()).getAssignedTime());
        Assert.assertNull(deviceRepository.findDeviceByID(d.getId()).getUnassignedTime());

        //Remove assignment
        service.removeDeviceFromAssignment(testAssignment.getId(), d.getId());
        assignmentList = service.findAssignmentsForDevice(d.getId());
        Assert.assertEquals(0, assignmentList.size());

        deviceRepository.delete(d);
        assignmentRepository.delete(testAssignment);
    }

    @Test
    public void testAddingAlreadyAssignedDevice(){
        //Because of FK we need an assignment first
        //Would want to do this in isolation and have a test assignment set up since not testing assignment
        assignmentRepository.save(testAssignment);
        assignmentRepository.save(testAssignment2);

        DefaultAssignmentService service = new DefaultAssignmentService(assignmentRepository, deviceRepository);

        Device d = new Device();
        d.setId(deviceID);
        deviceRepository.save(d);

        //Check that no assignments
        List<Assignment> assignmentList = service.findAssignmentsForDevice(d.getId());
        Assert.assertEquals(0, assignmentList.size());

        service.addDeviceToAssignment(testAssignment.getId(), d.getId());
        assignmentList = service.findAssignmentsForDevice(d.getId());
        Assert.assertEquals(1, assignmentList.size());

        //Check that no end date yet
        Assert.assertNotNull(deviceRepository.findDeviceByID(d.getId()).getAssignedTime());
        Assert.assertNull(deviceRepository.findDeviceByID(d.getId()).getUnassignedTime());

        //Try to assign to a different patient - can't do while unassigned_time null

        try{
            service.addDeviceToAssignment(testAssignment2.getId(), d.getId());
            fail();
        }catch(Exception e){
            Assert.assertTrue(e.getMessage().contains("Cannot add device to assignment as it is already assigned."));
        }

        // Check that no change was made
        assignmentList = service.findAssignmentsForDevice(d.getId());
        Assert.assertEquals(1, assignmentList.size());

        //Remove assignment
        service.removeDeviceFromAssignment(testAssignment.getId(), d.getId());
        assignmentList = service.findAssignmentsForDevice(d.getId());
        Assert.assertEquals(0, assignmentList.size());

        //Now we can assign to a new assignment
        service.addDeviceToAssignment(testAssignment2.getId(), d.getId());
        assignmentList = service.findAssignmentsForDevice(d.getId());
        Assert.assertEquals(1, assignmentList.size());

        deviceRepository.delete(d);
        assignmentRepository.delete(testAssignment);
        assignmentRepository.delete(testAssignment2);
    }

}
