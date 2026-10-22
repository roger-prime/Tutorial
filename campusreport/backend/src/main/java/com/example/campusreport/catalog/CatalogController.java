package com.example.campusreport.catalog;
import com.example.campusreport.model.Lugar;
import com.example.campusreport.model.Categoria;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogRepository repository;
    public CatalogController(CatalogRepository repository) { this.repository=repository; }
    @GetMapping("/lugares") public List<Lugar> lugares() { return repository.lugares(); }
    @GetMapping("/categorias") public List<Categoria> categorias() { return repository.categorias(); }
    @GetMapping("/catalogo") @Transactional(readOnly=true)
    public Map<String,Object> catalogo() {
        return Map.of("lugares",repository.lugares(),"categorias",repository.categorias());
    }
}
