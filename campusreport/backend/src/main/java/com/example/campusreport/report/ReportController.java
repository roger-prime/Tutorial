package com.example.campusreport.report;
import com.example.campusreport.model.Reporte;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
@RestController
@RequestMapping("/api/reportes")
public class ReportController {
    private final ReportService service;
    public ReportController(ReportService service) {this.service=service;}
    @GetMapping public List<Reporte> listar() {return service.listar();}
    @GetMapping("/{uuid}") public Reporte buscar(@PathVariable String uuid) {return service.buscar(uuid);}
    @PostMapping public ResponseEntity<Reporte> recibir(@RequestBody ReportRequest input) {
        var result=service.recibir(input);
        return result.creado()?ResponseEntity.created(URI.create("/api/reportes/"+result.reporte().uuid())).body(result.reporte())
            :ResponseEntity.ok(result.reporte());
    }
}
