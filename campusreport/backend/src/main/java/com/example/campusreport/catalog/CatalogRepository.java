package com.example.campusreport.catalog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public class CatalogRepository {
    private final JdbcTemplate jdbc;
    public CatalogRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public List<NamedItem> lugares() {
        return jdbc.query("SELECT id,nombre FROM lugar ORDER BY id",(rs,n)->new NamedItem(rs.getLong("id"),rs.getString("nombre")));
    }
    public List<NamedItem> categorias() {
        return jdbc.query("SELECT id,nombre FROM categoria ORDER BY id",(rs,n)->new NamedItem(rs.getLong("id"),rs.getString("nombre")));
    }
}
