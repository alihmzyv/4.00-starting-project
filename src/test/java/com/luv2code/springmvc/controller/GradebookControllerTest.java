package com.luv2code.springmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureMockMvc
@SpringBootTest
class GradebookControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private static MockHttpServletRequest request;

    @Autowired
    private ObjectMapper mapper;

    private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;

    @Autowired
    private CollegeStudent collegeStudent;

    @PersistenceContext
    private EntityManager entityManager;

    @Mock
    private StudentAndGradeService studentCreateServiceMock;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradesDao mathGradeDao;

    @Autowired
    private ScienceGradesDao scienceGradeDao;

    @Autowired
    private HistoryGradesDao historyGradeDao;

    @Autowired
    private StudentAndGradeService studentService;

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

    @BeforeAll
    static void beforeAll() {
        request = new MockHttpServletRequest();
        request.setParameter("firstname", "Ali");
        request.setParameter("lastname", "Hamzayev");
        request.setParameter("emailAddress", "alihmzyv@gmail.com");
    }

    @BeforeEach
    void setUp() {
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
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)));
    }

    @Test
    void createStudentHttpRequest() throws Exception {
        collegeStudent.setFirstname("Chad");
        collegeStudent.setLastname("Darby");
        collegeStudent.setEmailAddress("chad@luv2code.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(mapper.writeValueAsString(collegeStudent)))
                .andExpect(MockMvcResultMatchers.status().isOk()) //isCreated() is the right choice actually
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)));
        assertTrue(studentDao.existsById(2), "Student should have been saved already.");
    }
}