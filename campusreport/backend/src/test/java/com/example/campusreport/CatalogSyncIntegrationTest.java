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
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:sync_test;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc @Transactional
class CatalogSyncIntegrationTest {
 @Autowired MockMvc mvc; @Autowired JdbcTemplate jdbc; @Autowired ObjectMapper json;
 private org.springframework.test.web.servlet.ResultActions send(String path,Map<String,Object> r) throws Exception {
  return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(r)));
 }
 @Test void referencesRetryWithoutDuplicatingAndRejectConflicts() throws Exception {
  for(String path:List.of("/api/lugares","/api/categorias")) {
   var r=new HashMap<String,Object>();r.put("uuid",UUID.randomUUID().toString());r.put("nombre","Espacio nuevo");
   send(path,r).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(5));
   send(path,r).andExpect(status().isOk()).andExpect(jsonPath("$.uuid").value(r.get("uuid")));
   r.put("nombre","Contenido diferente");send(path,r).andExpect(status().isConflict());
  }
  assertEquals(5,jdbc.queryForObject("SELECT COUNT(*) FROM lugar",Integer.class));
  assertEquals(5,jdbc.queryForObject("SELECT COUNT(*) FROM categoria",Integer.class));
 }
 @Test void existingNameGetsCanonicalIdAndIndependentStableReceipt() throws Exception {
  var r=Map.<String,Object>of("uuid",UUID.randomUUID().toString(),"nombre"," aula 1 ");
  send("/api/lugares",r).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
  send("/api/lugares",r).andExpect(status().isOk());
  assertEquals(4,jdbc.queryForObject("SELECT COUNT(*) FROM lugar",Integer.class));
 }
 @Test void referencesSupportReportsAndDownload() throws Exception {
  send("/api/lugares",Map.of("uuid",UUID.randomUUID().toString(),"nombre","Sala nueva")).andExpect(status().isCreated());
  send("/api/categorias",Map.of("uuid",UUID.randomUUID().toString(),"nombre","Equipo nuevo")).andExpect(status().isCreated());
  send("/api/reportes",Map.of("uuid",UUID.randomUUID().toString(),"lugarId",5,"categoriaId",5,"descripcion","El equipo no funciona","fechaCreacion","2026-10-30T12:00:00Z","anonimo",true)).andExpect(status().isCreated());
  mvc.perform(get("/api/catalogo")).andExpect(status().isOk()).andExpect(jsonPath("$.lugares[4].nombre").value("Sala nueva"));
 }
 @Test void invalidReferencesNeverWrite() throws Exception {
  send("/api/lugares",Map.of("uuid","mal","nombre","Sala nueva")).andExpect(status().isBadRequest());
  send("/api/categorias",Map.of("uuid",UUID.randomUUID().toString(),"nombre","x")).andExpect(status().isBadRequest());
  assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM catalog_receipt",Integer.class));
 }
}
