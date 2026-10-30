package com.example.campusreport.catalog;
import com.example.campusreport.model.Lugar;
import com.example.campusreport.model.Categoria;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogRepository repository;
    private final CatalogService service;
    public CatalogController(CatalogRepository repository,CatalogService service) {this.repository=repository;this.service=service;}
    @PostMapping("/lugares") public ResponseEntity<CatalogService.Item> guardarLugar(@RequestBody CatalogRequest r) {return guardar(true,r);}
    @PostMapping("/categorias") public ResponseEntity<CatalogService.Item> guardarCategoria(@RequestBody CatalogRequest r) {return guardar(false,r);}
    private ResponseEntity<CatalogService.Item> guardar(boolean lugar,CatalogRequest r) {
        var result=service.recibir(lugar,r);return ResponseEntity.status(result.creado()?201:200).body(result.item());
    }
    @GetMapping("/lugares") public List<Lugar> lugares() { return repository.lugares(); }
    @GetMapping("/categorias") public List<Categoria> categorias() { return repository.categorias(); }
    @GetMapping("/catalogo") @Transactional(readOnly=true)
    public Map<String,Object> catalogo() {
        return Map.of("lugares",repository.lugares(),"categorias",repository.categorias());
    }
}
