package com.example.campusreport.catalog;
import com.example.campusreport.model.Lugar;
import com.example.campusreport.model.Categoria;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public class CatalogRepository {
    private final JdbcTemplate jdbc;
    public CatalogRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public List<Lugar> lugares() {
        return jdbc.query("SELECT id,nombre FROM lugar ORDER BY id",(rs,n)->new Lugar(rs.getLong("id"),rs.getString("nombre")));
    }
    public List<Categoria> categorias() {
        return jdbc.query("SELECT id,nombre FROM categoria ORDER BY id",(rs,n)->new Categoria(rs.getLong("id"),rs.getString("nombre")));
    }
    public boolean existeLugar(long id) { return jdbc.queryForObject("SELECT COUNT(*) FROM lugar WHERE id=?",Integer.class,id)>0; }
    public boolean existeCategoria(long id) { return jdbc.queryForObject("SELECT COUNT(*) FROM categoria WHERE id=?",Integer.class,id)>0; }
}
