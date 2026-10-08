package br.edu.uniateneu.sgcti.dao;

import br.edu.uniateneu.sgcti.model.Tecnico;
import br.edu.uniateneu.sgcti.service.Formato;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TecnicoDao {
    private static final RowMapper<Tecnico> MAP = (rs, i) -> new Tecnico(rs.getInt("id"), rs.getString("nome"),
            Formato.cpf(rs.getString("cpf")), rs.getString("especialidade"), rs.getString("email"), rs.getString("telefone"));
    private final JdbcTemplate jdbc;

    public TecnicoDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Tecnico> listar() { return jdbc.query("SELECT * FROM tecnico ORDER BY id", MAP); }

    public Optional<Tecnico> buscar(int id) {
        return jdbc.query("SELECT * FROM tecnico WHERE id = ?", MAP, id).stream().findFirst();
    }

    public boolean cpfExiste(String cpfDigitos, int ignorarId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM tecnico WHERE cpf = ? AND id <> ?", Integer.class, cpfDigitos, ignorarId);
        return n != null && n > 0;
    }

    public int totalChamados(int id) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM chamado WHERE tecnico_responsavel_id = ?", Integer.class, id);
        return n == null ? 0 : n;
    }

    public void inserir(Tecnico t) {
        jdbc.update("INSERT INTO tecnico (nome, cpf, especialidade, email, telefone) VALUES (?,?,?,?,?)",
                t.nome(), Formato.digitos(t.cpf()), t.esp(), t.email(), t.tel());
    }

    public void atualizar(Tecnico t) {
        jdbc.update("UPDATE tecnico SET nome=?, cpf=?, especialidade=?, email=?, telefone=? WHERE id=?",
                t.nome(), Formato.digitos(t.cpf()), t.esp(), t.email(), t.tel(), t.id());
    }

    public void excluir(int id) { jdbc.update("DELETE FROM tecnico WHERE id = ?", id); }
}
