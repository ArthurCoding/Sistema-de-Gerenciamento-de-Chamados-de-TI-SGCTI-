package br.edu.uniateneu.sgcti.dao;

import br.edu.uniateneu.sgcti.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioDao {
    private static final RowMapper<Usuario> MAP = (rs, i) -> new Usuario(rs.getInt("id"), rs.getString("nome"),
            rs.getString("setor"), rs.getString("email"), rs.getString("telefone"));
    private final JdbcTemplate jdbc;

    public UsuarioDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Usuario> listar() { return jdbc.query("SELECT * FROM usuario_solicitante ORDER BY id", MAP); }

    public Optional<Usuario> buscar(int id) {
        return jdbc.query("SELECT * FROM usuario_solicitante WHERE id = ?", MAP, id).stream().findFirst();
    }

    public int totalChamados(int id) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM chamado WHERE usuario_solicitante_id = ?", Integer.class, id);
        return n == null ? 0 : n;
    }

    public void inserir(Usuario u) {
        jdbc.update("INSERT INTO usuario_solicitante (nome, setor, email, telefone) VALUES (?,?,?,?)",
                u.nome(), u.setor(), u.email(), u.tel());
    }

    public void atualizar(Usuario u) {
        jdbc.update("UPDATE usuario_solicitante SET nome=?, setor=?, email=?, telefone=? WHERE id=?",
                u.nome(), u.setor(), u.email(), u.tel(), u.id());
    }

    public void excluir(int id) { jdbc.update("DELETE FROM usuario_solicitante WHERE id = ?", id); }
}
