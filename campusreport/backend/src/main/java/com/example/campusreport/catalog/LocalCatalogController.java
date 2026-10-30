
package com.example.campusreport.catalog;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class LocalCatalogController {

    private final JdbcTemplate jdbc;

    public LocalCatalogController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record NuevaReferencia(
            String uuid,
            String nombre
    ) {}

    public record ReferenciaCreada(
            long id,
            String nombre,
            String uuid
    ) {}

    // Registrar lugares nuevos
    @PostMapping("/lugares")
    @Transactional
    public ResponseEntity<ReferenciaCreada> crearLugar(
            @RequestBody NuevaReferencia datos) {

        return guardar("lugar", "lugar_seq", datos);
    }

    // Registrar categorías nuevas
    @PostMapping("/categorias")
    @Transactional
    public ResponseEntity<ReferenciaCreada> crearCategoria(
            @RequestBody NuevaReferencia datos) {

        return guardar("categoria", "categoria_seq", datos);
    }

    // Guardar lugares y categorías sin duplicarlos
    private ResponseEntity<ReferenciaCreada> guardar(
            String tabla,
            String secuencia,
            NuevaReferencia datos) {

        // Validar que existan los datos
        if (datos == null
                || datos.uuid() == null
                || datos.nombre() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Faltan datos"
            );
        }

        // Validar UUID
        String uuid;

        try {
            uuid = UUID.fromString(
                    datos.uuid()
            ).toString();

            if (!uuid.equalsIgnoreCase(datos.uuid())) {
                throw new IllegalArgumentException();
            }

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "UUID invalido"
            );
        }

        // Validar nombre
        String nombre = datos.nombre().trim();

        if (nombre.length() < 3 || nombre.length() > 80) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre invalido"
            );
        }

        // Buscar si el UUID ya existe en H2
        List<ReferenciaCreada> existentes = jdbc.query(
                "SELECT id, nombre, uuid FROM "
                        + tabla + " WHERE uuid=?",

                (rs, n) -> new ReferenciaCreada(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("uuid")
                ),

                uuid
        );

        // Si existe, evitar duplicados
        if (!existentes.isEmpty()) {

            ReferenciaCreada anterior = existentes.get(0);

            if (!anterior.nombre().equals(nombre)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "UUID asociado a otro nombre"
                );
            }

            // Devolver el ID existente
            return ResponseEntity.ok(anterior);
        }

        // Generar un ID positivo nuevo
        Long id = jdbc.queryForObject(
                "SELECT NEXT VALUE FOR " + secuencia,
                Long.class
        );

        // Guardar en H2
        jdbc.update(
                "INSERT INTO " + tabla
                        + " (id, nombre, uuid) "
                        + "VALUES (?, ?, ?)",

                id,
                nombre,
                uuid
        );

        // Confirmar el registro creado
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ReferenciaCreada(
                                id,
                                nombre,
                                uuid
                        )
                );
    }
}

