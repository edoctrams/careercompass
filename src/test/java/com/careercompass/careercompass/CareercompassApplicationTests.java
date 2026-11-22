package com.careercompass.careercompass;

import com.careercompass.careercompass.repository.CareerPathRepository;
import com.careercompass.careercompass.repository.CareerSkillRepository;
import com.careercompass.careercompass.repository.SkillRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:careercompass;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration"
})
class CareercompassApplicationTests {

    @Autowired private CareerPathRepository careerPaths;
    @Autowired private SkillRepository skills;
    @Autowired private CareerSkillRepository careerSkills;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        // Just verify context loads without security auto-configuration conflict
    }
}