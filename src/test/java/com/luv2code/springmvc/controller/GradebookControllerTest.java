package com.luv2code.springmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Iterator;
import java.util.Optional;
import java.util.Random;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class GradebookControllerTest {
    private static final Random rnd = new Random();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private MockHttpServletRequest createStudentReq;

    private MockHttpServletRequest createGradeReq;

    @Autowired
    private CollegeStudent collegeStudent;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradesDao mathGradeDao;

    @Value("${sql.script.create.student}")
    private String sqlAddStudent;

    @Value("${sql.script.create.math.grade}")
    private String sqlAddMathGrade;

    @Value("${sql.script.create.science.grade}")
    private String sqlAddScienceGrade;

    @Value("${sql.script.create.history.grade}")
    private String sqlAddHistoryGrade;

    @Value("${sql.script.delete.student}")
    private String sqlDeleteStudent;

    @Value("${sql.script.delete.math.grade}")
    private String sqlDeleteMathGrade;

    @Value("${sql.script.delete.science.grade}")
    private String sqlDeleteScienceGrade;

    @Value("${sql.script.delete.history.grade}")
    private String sqlDeleteHistoryGrade;

    @BeforeEach
    void setUp() {
        //set up requests
        createStudentReq = new MockHttpServletRequest();
        createStudentReq.setParameter("firstname", "Ali");
        createStudentReq.setParameter("lastname", "Hamzayev");
        createStudentReq.setParameter("emailAddress", "alihmzyv@gmail.com");
        createGradeReq = new MockHttpServletRequest();
        createGradeReq.setParameter("grade", "90.0");
        createGradeReq.setParameter("gradeType", "math");
        createGradeReq.setParameter("studentId", "1");

        //execute sql's
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);
    }

    @AfterEach
    void tearDown() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }

    @Test
    void getStudentsHttpRequest() throws Exception {
        collegeStudent.setFirstname("Chad");
        collegeStudent.setLastname("Darby");
        collegeStudent.setEmailAddress("chad@luv2code.com");
        studentDao.save(collegeStudent);
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                        .andExpectAll(
                                status().isOk(), //isCreated() is the right choice actually
                                content().contentType(APPLICATION_JSON),
                                jsonPath("$", hasSize(2)));
        assertTrue(studentDao.findByEmailAddress("chad@luv2code.com").isPresent(),
                "Should have been saved.");
    }

    @Test
    void createStudentHttpRequest() throws Exception {
        collegeStudent.setFirstname("Chad");
        collegeStudent.setLastname("Darby");
        collegeStudent.setEmailAddress("chad@luv2code.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(collegeStudent)))
                .andExpectAll(
                        status().isOk(), //isCreated() is the right choice actually
                        content().contentType(APPLICATION_JSON),
                        jsonPath("$", hasSize(2)));
        studentDao.findAll().forEach(System.out::println);
        assertTrue(studentDao.findByEmailAddress("chad@luv2code.com").isPresent(), "Student should have been saved already.");
    }

    @Test
    void deleteNonExistingStudentRequestTest() throws Exception {
        assertFalse(studentDao.existsById(2));
        mockMvc.perform(MockMvcRequestBuilders.delete("/student/{id}", 2))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.message", equalTo("Student or Grade was not found")),
                        jsonPath("$.status", equalTo(404)));
    }

    @Test
    void deleteExistingStudentRequestTest() throws Exception {
        Optional<CollegeStudent> studentOpt = studentDao.findById(1);
        assertTrue(studentOpt.isPresent());
        CollegeStudent student = studentOpt.get();
        mockMvc.perform(MockMvcRequestBuilders.delete("/student/{id}", student.getId()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(APPLICATION_JSON),
                        jsonPath("$", hasSize(0)));
        assertFalse(studentDao.existsById(student.getId()));
    }

    @Test
    void studentInformationNonExistingStudent() throws Exception {
        assertFalse(studentDao.existsById(2));
        mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}", 2))
                .andExpect(status().isNotFound())
                .andExpectAll(
                        jsonPath("$.status", equalTo(404)),
                        jsonPath("$.message", equalTo("Student or Grade was not found")));
    }

    @Test
    void studentInformationExistingStudent() throws Exception {
        collegeStudent.setFirstname("Ali");
        collegeStudent.setLastname("Hamzayev");
        collegeStudent.setEmailAddress("alihmzyv@gmail.com");
        studentDao.save(collegeStudent);
        mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}", collegeStudent.getId()))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.firstname", equalTo("Ali")),
                        jsonPath("$.lastname", equalTo("Hamzayev")),
                        jsonPath("$.emailAddress", equalTo("alihmzyv@gmail.com")));
    }

    @Test
    void createGradeNonExistingStudent() throws Exception {
        assertFalse(studentDao.existsById(2));
        createGradeReq.setParameter("studentId", "2");
        mockMvc.perform(MockMvcRequestBuilders.post("/grades")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("grade", createGradeReq.getParameter("grade"))
                .param("gradeType", createGradeReq.getParameter("gradeType"))
                .param("studentId", createGradeReq.getParameter("studentId")))
                .andExpectAll(
                        jsonPath("$.status", equalTo(404)),
                        jsonPath("$.message", equalTo("Student or Grade was not found")));
    }

    @Test
    void createGradeNotInRange() throws Exception {
        createGradeReq.setParameter("grade", String.valueOf(rnd.nextDouble(100.1, 1000)));
        mockMvc.perform(MockMvcRequestBuilders.post("/grades")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("grade", createGradeReq.getParameter("grade"))
                        .param("gradeType", createGradeReq.getParameter("gradeType"))
                        .param("studentId", createGradeReq.getParameter("studentId")))
                .andExpectAll(
                        jsonPath("$.status", equalTo(404)),
                        jsonPath("$.message", equalTo("Student or Grade was not found")));
    }

    @Test
    void createGradeNonExistingGradeType() throws Exception {
        createGradeReq.setParameter("gradeType", "gibberish");
        mockMvc.perform(MockMvcRequestBuilders.post("/grades")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("grade", createGradeReq.getParameter("grade"))
                        .param("gradeType", createGradeReq.getParameter("gradeType"))
                        .param("studentId", createGradeReq.getParameter("studentId")))
                .andExpectAll(
                        jsonPath("$.status", equalTo(404)),
                        jsonPath("$.message", equalTo("Student or Grade was not found")));
    }

    @Test
    void createGradeValid() throws Exception {
        Optional<CollegeStudent> studentOpt = studentDao.findById(1);
        assertTrue(studentOpt.isPresent());
        CollegeStudent student = studentOpt.get();
        Iterator<MathGrade> iterator = mathGradeDao.findGradeByStudentId(student.getId()).iterator();
        iterator.next();
        assertFalse(iterator.hasNext());
        mockMvc.perform(MockMvcRequestBuilders.post("/grades")
                        .param("grade", createGradeReq.getParameter("grade"))
                        .param("gradeType", createGradeReq.getParameter("gradeType"))
                        .param("studentId", createGradeReq.getParameter("studentId")))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.id", equalTo(student.getId())),
                        jsonPath("$.firstname", equalTo(student.getFirstname())),
                        jsonPath("$.lastname", equalTo(student.getLastname())),
                        jsonPath("$.emailAddress", equalTo(student.getEmailAddress())),
                        jsonPath("$.studentGrades.mathGradeResults", hasSize(2)));
        assertTrue(mathGradeDao.findGradeByStudentId(student.getId()).iterator().hasNext());
    }
}