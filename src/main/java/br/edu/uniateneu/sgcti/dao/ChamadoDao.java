package br.edu.uniateneu.sgcti.dao;

import br.edu.uniateneu.sgcti.model.Chamado;
import br.edu.uniateneu.sgcti.model.Evento;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@Repository
public class ChamadoDao {
    private final JdbcTemplate jdbc;

    public ChamadoDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static String dia(Timestamp t) { return t == null ? null : t.toLocalDateTime().toLocalDate().toString(); }

    private static Chamado mapear(ResultSet rs) throws SQLException {
        int tec = rs.getInt("tecnico_responsavel_id");
        Integer tecnico = rs.wasNull() ? null : tec;
        return new Chamado(rs.getInt("id"), rs.getString("titulo"), rs.getString("descricao"),
                dia(rs.getTimestamp("data_abertura")), dia(rs.getTimestamp("data_fechamento")), tecnico,
                rs.getInt("usuario_solicitante_id"), rs.getString("prioridade"), rs.getString("status"),
                rs.getString("solucao"), new ArrayList<>());
    }

    /** Lista todos os chamados já com o histórico (duas consultas, sem N+1). */
    public List<Chamado> listar() {
        List<Chamado> lista = jdbc.query("SELECT * FROM chamado ORDER BY id", (rs, i) -> mapear(rs));
        Map<Integer, Chamado> porId = new HashMap<>();
        lista.forEach(c -> porId.put(c.id(), c));
        jdbc.query("SELECT chamado_id, data_evento, descricao FROM historico_chamado ORDER BY data_evento, id", rs -> {
            Chamado c = porId.get(rs.getInt("chamado_id"));
            if (c != null) c.hist().add(new Evento(rs.getTimestamp("data_evento").toLocalDateTime().toString(), rs.getString("descricao")));
        });
        return lista;
    }

    public Optional<Chamado> buscar(int id) {
        Optional<Chamado> c = jdbc.query("SELECT * FROM chamado WHERE id = ?", (rs, i) -> mapear(rs), id).stream().findFirst();
        c.ifPresent(x -> jdbc.query("SELECT data_evento, descricao FROM historico_chamado WHERE chamado_id = ? ORDER BY data_evento, id",
                rs -> { x.hist().add(new Evento(rs.getTimestamp("data_evento").toLocalDateTime().toString(), rs.getString("descricao"))); }, id));
        return c;
    }

    public int inserir(Chamado c) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO chamado (titulo, descricao, tecnico_responsavel_id, usuario_solicitante_id, prioridade, status) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.titulo());
            ps.setString(2, c.desc());
            ps.setObject(3, c.tec(), Types.INTEGER);
            ps.setInt(4, c.usr());
            ps.setString(5, c.prio());
            ps.setString(6, c.status());
            return ps;
        }, kh);
        return Objects.requireNonNull(kh.getKey()).intValue();
    }

    public void atualizar(Chamado c) {
        Timestamp fechamento = c.fechamento() == null ? null : Timestamp.valueOf(LocalDate.parse(c.fechamento()).atStartOfDay());
        jdbc.update("UPDATE chamado SET titulo=?, descricao=?, prioridade=?, status=?, tecnico_responsavel_id=?, "
                        + "usuario_solicitante_id=?, data_fechamento=?, solucao=? WHERE id=?",
                c.titulo(), c.desc(), c.prio(), c.status(), c.tec(), c.usr(), fechamento, c.solucao(), c.id());
    }

    public void evento(int chamadoId, String texto) {
        jdbc.update("INSERT INTO historico_chamado (chamado_id, descricao) VALUES (?,?)", chamadoId, texto);
    }

    public void excluir(int id) { jdbc.update("DELETE FROM chamado WHERE id = ?", id); }
}
