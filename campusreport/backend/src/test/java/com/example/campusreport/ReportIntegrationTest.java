package com.example.campusreport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:reports_test;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
@Transactional
class ReportIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    private Map<String,Object> report() {
        Map<String,Object> r=new HashMap<>();r.put("uuid",UUID.randomUUID().toString());
        r.put("lugarId",1);r.put("categoriaId",1);r.put("descripcion","La luz del aula no enciende");
        r.put("fechaCreacion","2026-10-22T12:00:00Z");r.put("anonimo",true);return r;
    }
    private org.springframework.test.web.servlet.ResultActions send(Map<String,Object> r) throws Exception {
        return mvc.perform(post("/api/reportes").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(r)));
    }
    @Test void anonymousJsonIsStoredWithoutNameAndCanBeRead() throws Exception {
        var r=report();r.put("autor","No almacenar");
        send(r).andExpect(status().isCreated()).andExpect(header().string("Location","/api/reportes/"+r.get("uuid")))
            .andExpect(jsonPath("$.anonimo").value(true)).andExpect(jsonPath("$.autor").isEmpty());
        assertNull(jdbc.queryForObject("SELECT autor FROM reporte WHERE uuid=?",String.class,r.get("uuid")));
        mvc.perform(get("/api/reportes/"+r.get("uuid"))).andExpect(status().isOk()).andExpect(jsonPath("$.descripcion").value(r.get("descripcion")));
        mvc.perform(get("/api/reportes")).andExpect(status().isOk()).andExpect(jsonPath("$[0].uuid").value(r.get("uuid")));
    }
    @Test void sameUuidDoesNotDuplicateAndDifferentContentConflicts() throws Exception {
        var r=report();send(r).andExpect(status().isCreated());send(r).andExpect(status().isOk());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM reporte",Integer.class));
        r.put("descripcion","El enchufe de la puerta no funciona");send(r).andExpect(status().isConflict());
        assertEquals("La luz del aula no enciende",jdbc.queryForObject("SELECT descripcion FROM reporte",String.class));
    }
    @Test void namedReportTrimsAndRequiresName() throws Exception {
        var r=report();r.put("anonimo",false);send(r).andExpect(status().isBadRequest());
        r.put("autor"," Roger Báez ");send(r).andExpect(status().isCreated()).andExpect(jsonPath("$.autor").value("Roger Báez"));
    }
    @Test void invalidFieldsAndUnknownReferencesAreRejected() throws Exception {
        var r=report();r.put("descripcion","corta");send(r).andExpect(status().isBadRequest());
        r=report();r.put("lugarId",999);send(r).andExpect(status().isBadRequest());
        r=report();r.put("categoriaId",999);send(r).andExpect(status().isBadRequest());
        r=report();r.put("lugarId",-1);send(r).andExpect(status().isBadRequest());
        r=report();r.put("uuid","invalido");send(r).andExpect(status().isBadRequest());
        r=report();r.put("fechaCreacion","ayer");send(r).andExpect(status().isBadRequest());
        r=report();r.remove("anonimo");send(r).andExpect(status().isBadRequest());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM reporte",Integer.class));
    }
    @Test void malformedJsonAndMissingReportHaveClearResponses() throws Exception {
        mvc.perform(post("/api/reportes").contentType(MediaType.APPLICATION_JSON).content("{incorrecto"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensaje").exists());
        mvc.perform(get("/api/reportes/"+UUID.randomUUID())).andExpect(status().isNotFound());
    }
}
