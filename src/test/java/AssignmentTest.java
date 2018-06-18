import com.snap40.assignments.Application;
import com.snap40.assignments.model.Assignment;
import com.snap40.assignments.model.Device;
import com.snap40.assignments.repositories.AssignmentRepository;
import com.snap40.assignments.repositories.DeviceRepository;
import com.snap40.assignments.services.DefaultAssignmentService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(
        classes = Application.class)
@AutoConfigureMockMvc
public class AssignmentTest {
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
    public void setExampleAssignment() {
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
    public void testAssignmentModel(){
        Assignment testAssignment2 = new Assignment();
        testAssignment2.setId("TEST_A");
        testAssignment2.setPatientId("PATIENT_A");
        testAssignment2.setOpenedTime(new Timestamp(300000));
        testAssignment2.setClosedTime(new Timestamp(900000));

        assertEquals(testAssignment, testAssignment2);
    }

    @Test
    public void testAddingAndRemovingAssignmentRecords(){
        DefaultAssignmentService service = new DefaultAssignmentService(assignmentRepository, deviceRepository);

        Assignment a;

        try {
            a = service.findAssignment(testAssignment.getId());
            fail();
        }catch(Exception e){
            Assert.assertTrue(e.getMessage().contains("Cannot find an Assignment with AssignmentId"));
        }

        assignmentRepository.save(testAssignment);

        a = assignmentRepository.findAssignmentById(testAssignment.getId());

        assertNotNull(a);

        assignmentRepository.delete(testAssignment);

        try {
            a = service.findAssignment(testAssignment.getId());
            fail();
        }catch(Exception e){
            Assert.assertTrue(e.getMessage().contains("Cannot find an Assignment with AssignmentId"));
        }
    }

    @Test
    public void testFindAssignmentByID(){
        assignmentRepository.save(testAssignment);

        DefaultAssignmentService service = new DefaultAssignmentService(assignmentRepository, deviceRepository);
        Assignment a = service.findAssignment(testAssignment.getId());

        assertEquals(a.getId(), testAssignment.getId());
        assignmentRepository.delete(testAssignment);
    }

    @Test
    public void testFindAssignmentsForPatient(){
        assignmentRepository.save(testAssignment);

        DefaultAssignmentService service = new DefaultAssignmentService(assignmentRepository, deviceRepository);
        List<Assignment> assignmentList = service.findAssignmentsForPatient("PATIENT_A");

        assertEquals(1, assignmentList.size());
        assignmentRepository.delete(testAssignment);
    }

    @Test
    public void testFindAssignmentForPatientWithNoAssignments(){
        DefaultAssignmentService service = new DefaultAssignmentService(assignmentRepository, deviceRepository);

        try {
            List<Assignment> assignmentList = service.findAssignmentsForPatient("PATIENT_C");
            fail();
        }catch(Exception e){
            Assert.assertTrue(e.getMessage().contains("Cannot find an Assignment with PatientID"));
        }
    }
}