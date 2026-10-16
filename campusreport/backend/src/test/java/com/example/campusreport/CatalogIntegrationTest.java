package com.example.campusreport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:campus_test;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
@Transactional
class CatalogIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Test void returnsCatalogFromDatabase() throws Exception {
        mvc.perform(get("/api/catalogo")).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.lugares",hasSize(4)))
            .andExpect(jsonPath("$.categorias",hasSize(4)))
            .andExpect(jsonPath("$.lugares[0].nombre").value("Aula 1"));
    }
    @Test void endpointReadsNewDatabaseRows() throws Exception {
        jdbc.update("INSERT INTO lugar(id,nombre) VALUES(99,'Auditorio')");
        mvc.perform(get("/api/lugares")).andExpect(status().isOk())
            .andExpect(jsonPath("$",hasSize(5))).andExpect(jsonPath("$[4].nombre").value("Auditorio"));
    }
    @Test void categoriesEndpointWorks() throws Exception {
        mvc.perform(get("/api/categorias")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Electricidad"));
    }
    @Test void reportTableAndForeignKeysExist() {
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM reporte",Integer.class));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->
            jdbc.update("INSERT INTO reporte VALUES('test',999,1,'Una descripción válida','2026-10-16T12:00:00Z')"));
    }
}
