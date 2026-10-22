package com.example.campusreport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:status_test;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
class StatusIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void estadoDelServidorRespondeOk() throws Exception {
        mvc.perform(get("/api/estado"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ok"));
    }
}