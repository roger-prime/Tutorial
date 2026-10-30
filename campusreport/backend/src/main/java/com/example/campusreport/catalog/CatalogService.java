package com.example.campusreport.catalog;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** La misma solicitud devuelve la misma confirmación, incluso si la respuesta se perdió. */
@Service
public class CatalogService {
    private final JdbcTemplate jdbc;
    public CatalogService(JdbcTemplate jdbc) {this.jdbc=jdbc;}
    public record Item(long id,String uuid,String nombre) {}
    public record Reception(Item item,boolean creado) {}
    @Transactional
    public Reception recibir(boolean lugar,CatalogRequest input) {
        String uuid;
        try {
            uuid=UUID.fromString(input.uuid()).toString();
            if(!uuid.equalsIgnoreCase(input.uuid())) throw new IllegalArgumentException();
        } catch(Exception e) {throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"UUID inválido");}
        String nombre=input.nombre()==null?"":input.nombre().trim();
        if(nombre.length()<3||nombre.length()>80) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El nombre debe tener entre 3 y 80 caracteres");
        String table=lugar?"lugar":"categoria";
        // Serializa asignaciones de ID y comprobaciones de reenvío dentro de la transacción.
        jdbc.queryForObject("SELECT id FROM sync_lock WHERE id=1 FOR UPDATE",Long.class);
        var previous=jdbc.query("SELECT item_id,uuid,nombre FROM catalog_receipt WHERE tipo=? AND uuid=?",
            (rs,n)->new Item(rs.getLong(1),rs.getString(2),rs.getString(3)),table,uuid);
        if(!previous.isEmpty()) {
            if(!previous.get(0).nombre().equals(nombre)) throw new ResponseStatusException(HttpStatus.CONFLICT,"El UUID ya pertenece a otro contenido");
            return new Reception(previous.get(0),false);
        }
        var existing=jdbc.query("SELECT id FROM "+table+" WHERE LOWER(TRIM(nombre))=LOWER(?) ORDER BY id",(rs,n)->rs.getLong(1),nombre);
        long id;
        if(existing.isEmpty()) {
            id=jdbc.queryForObject("SELECT COALESCE(MAX(id),0)+1 FROM "+table,Long.class);
            jdbc.update("INSERT INTO "+table+"(id,nombre) VALUES(?,?)",id,nombre);
        } else id=existing.get(0);
        jdbc.update("INSERT INTO catalog_receipt(tipo,uuid,nombre,item_id) VALUES(?,?,?,?)",table,uuid,nombre,id);
        return new Reception(new Item(id,uuid,nombre),true);
    }
}
