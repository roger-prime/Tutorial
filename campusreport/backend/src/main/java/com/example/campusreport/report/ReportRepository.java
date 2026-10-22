package com.example.campusreport.report;
import com.example.campusreport.model.Reporte;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public class ReportRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Reporte> MAPPER=(rs,n)->new Reporte(rs.getString("uuid"),rs.getLong("lugar_id"),
        rs.getLong("categoria_id"),rs.getString("descripcion"),rs.getString("fecha_creacion"),rs.getBoolean("anonimo"),rs.getString("autor"));
    public ReportRepository(JdbcTemplate jdbc) {this.jdbc=jdbc;}
    public Optional<Reporte> buscar(String uuid) {
        return jdbc.query("SELECT * FROM reporte WHERE uuid=?",MAPPER,uuid).stream().findFirst();
    }
    public List<Reporte> listar() {return jdbc.query("SELECT * FROM reporte ORDER BY fecha_creacion DESC,uuid",MAPPER);}
    public List<Reporte> listarDesde(String desde) {
        return jdbc.query(
            "SELECT * FROM reporte WHERE fecha_creacion > ? ORDER BY fecha_creacion DESC, uuid",
            MAPPER,
            desde
        );
    }
    public void guardar(Reporte r) {
        jdbc.update("INSERT INTO reporte(uuid,lugar_id,categoria_id,descripcion,fecha_creacion,anonimo,autor) VALUES(?,?,?,?,?,?,?)",
            r.uuid(),r.lugarId(),r.categoriaId(),r.descripcion(),r.fechaCreacion(),r.anonimo(),r.autor());
    }
}
