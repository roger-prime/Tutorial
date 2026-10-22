package com.example.campusreport.report;

import com.example.campusreport.catalog.CatalogRepository;
import com.example.campusreport.model.Reporte;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reports;
    private final CatalogRepository catalog;

    public ReportService(ReportRepository reports, CatalogRepository catalog) {
        this.reports = reports;
        this.catalog = catalog;
    }

    public record Reception(Reporte reporte, boolean creado) {}

    @Transactional
    public Reception recibir(ReportRequest input) {

        Reporte r = validar(input);

        var existing = reports.buscar(r.uuid());

        if (existing.isPresent()) {
            return repetido(existing.get(), r);
        }

        if (!catalog.existeLugar(r.lugarId())) {
            throw invalid("El lugar no existe en el servidor");
        }

        if (!catalog.existeCategoria(r.categoriaId())) {
            throw invalid("La categoría no existe en el servidor");
        }

        try {
            reports.guardar(r);
        } catch (DuplicateKeyException e) {
            return repetido(
                reports.buscar(r.uuid()).orElseThrow(),
                r
            );
        }

        return new Reception(r, true);
    }

    private Reception repetido(Reporte existing, Reporte input) {

        if (!existing.equals(input)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El UUID ya pertenece a otro contenido"
            );
        }

        return new Reception(existing, false);
    }

    @Transactional(readOnly = true)
    public List<Reporte> listar() {
        return reports.listar();
    }

    @Transactional(readOnly = true)
    public List<Reporte> novedades(String desde) {

        String fecha;

        try {
            fecha = Instant.parse(desde).toString();
        } catch (Exception e) {
            throw invalid("Fecha inválida; usá formato ISO 8601");
        }

        return reports.listarDesde(fecha);
    }

    @Transactional(readOnly = true)
    public Reporte buscar(String uuid) {
        return reports.buscar(uuid)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró este reporte"
                )
            );
    }

    private Reporte validar(ReportRequest r) {

        if (r == null) {
            throw invalid("El cuerpo JSON es obligatorio");
        }

        String uuid;

        try {
            uuid = UUID.fromString(r.uuid()).toString();

            if (!uuid.equalsIgnoreCase(r.uuid())) {
                throw new IllegalArgumentException();
            }

        } catch (Exception e) {
            throw invalid("UUID inválido");
        }

        if (r.lugarId() == null || r.lugarId() <= 0) {
            throw invalid("Seleccioná un lugar del servidor");
        }

        if (r.categoriaId() == null || r.categoriaId() <= 0) {
            throw invalid("Seleccioná una categoría del servidor");
        }

        String desc =
            r.descripcion() == null ? "" : r.descripcion().trim();

        if (desc.length() < 10 || desc.length() > 500) {
            throw invalid(
                "La descripción debe tener entre 10 y 500 caracteres"
            );
        }

        if (r.anonimo() == null) {
            throw invalid("Indicá si el reporte es anónimo");
        }

        String author = r.anonimo()
            ? null
            : (r.autor() == null ? "" : r.autor().trim());

        if (!r.anonimo() &&
            (author.length() < 2 || author.length() > 80)) {

            throw invalid(
                "Tu nombre debe tener entre 2 y 80 caracteres"
            );
        }

        String date;

        try {
            date = Instant.parse(r.fechaCreacion()).toString();
        } catch (Exception e) {
            throw invalid(
                "Fecha de creación inválida; usá el formato ISO 8601"
            );
        }

        return new Reporte(
            uuid,
            r.lugarId(),
            r.categoriaId(),
            desc,
            date,
            r.anonimo(),
            author
        );
    }

    private ResponseStatusException invalid(String message) {
        return new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            message
        );
    }
}