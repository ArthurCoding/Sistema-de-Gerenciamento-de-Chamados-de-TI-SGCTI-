package br.edu.uniateneu.sgcti.service;

import br.edu.uniateneu.sgcti.dao.ChamadoDao;
import br.edu.uniateneu.sgcti.dao.TecnicoDao;
import br.edu.uniateneu.sgcti.dao.UsuarioDao;
import br.edu.uniateneu.sgcti.model.Chamado;
import br.edu.uniateneu.sgcti.model.Sessao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Ciclo de vida do chamado e permissões por perfil (RN01 a RN06). */
@Service
public class ChamadoService {
    public static final List<String> PRIORIDADES = List.of("Baixa", "Média", "Alta");

    private final ChamadoDao dao;
    private final TecnicoDao tecnicos;
    private final UsuarioDao usuarios;

    public ChamadoService(ChamadoDao dao, TecnicoDao tecnicos, UsuarioDao usuarios) {
        this.dao = dao;
        this.tecnicos = tecnicos;
        this.usuarios = usuarios;
    }

    /** Dados do formulário de abertura/edição. */
    public record Form(String titulo, String desc, String prio, Integer usr, Integer tec) {}

    public List<Chamado> visiveis(Sessao s) {
        List<Chamado> todos = dao.listar();
        if (s.role().equals("usr")) return todos.stream().filter(c -> c.usr() == s.id()).toList();
        return todos;
    }

    private Chamado obter(int id) {
        return dao.buscar(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Chamado não encontrado."));
    }

    private static ApiException erro(HttpStatus st, String msg) { return new ApiException(st, msg); }

    private void validar(Form f) {
        if (f.titulo() == null || f.titulo().trim().length() < 5)
            throw erro(HttpStatus.BAD_REQUEST, "O título precisa ter ao menos 5 caracteres.");
        if (f.desc() == null || f.desc().trim().length() < 10)
            throw erro(HttpStatus.BAD_REQUEST, "Descreva o problema com ao menos 10 caracteres.");
        if (f.prio() == null || !PRIORIDADES.contains(f.prio()))
            throw erro(HttpStatus.BAD_REQUEST, "Prioridade inválida. Use Baixa, Média ou Alta.");
    }

    private String nomeTecnico(int id) {
        return tecnicos.buscar(id).orElseThrow(() -> erro(HttpStatus.BAD_REQUEST, "Técnico inválido.")).nome();
    }

    public void abrir(Sessao s, Form f) {
        if (s.role().equals("tec")) throw erro(HttpStatus.FORBIDDEN, "Técnicos não abrem chamados.");
        validar(f);
        int usr = s.id();
        Integer tec = null;
        if (s.role().equals("adm")) {
            if (f.usr() == null || usuarios.buscar(f.usr()).isEmpty())
                throw erro(HttpStatus.BAD_REQUEST, "Selecione o usuário solicitante.");
            usr = f.usr();
            tec = f.tec();
            if (tec != null) nomeTecnico(tec);
        }
        // RN01: todo chamado tem solicitante. RN03: nasce Aberto.
        int id = dao.inserir(new Chamado(0, f.titulo().trim(), f.desc().trim(), null, null, tec, usr,
                f.prio(), "Aberto", null, new ArrayList<>()));
        dao.evento(id, "Chamado aberto por " + s.nome());
        if (tec != null) dao.evento(id, "Atribuído a " + nomeTecnico(tec));
    }

    public void editar(Sessao s, int id, Form f) {
        Chamado c = obter(id);
        if (!c.ativo()) throw erro(HttpStatus.CONFLICT, "Chamado finalizado não pode ser alterado.");
        if (s.role().equals("tec")) throw erro(HttpStatus.FORBIDDEN, "Sem permissão para editar chamados.");
        if (s.role().equals("usr") && (c.usr() != s.id() || !c.status().equals("Aberto")))
            throw erro(HttpStatus.FORBIDDEN, "Você só pode editar os seus chamados enquanto estiverem Abertos.");
        validar(f);
        int usr = c.usr();
        Integer tec = c.tec();
        if (s.role().equals("adm")) {
            if (f.usr() == null || usuarios.buscar(f.usr()).isEmpty())
                throw erro(HttpStatus.BAD_REQUEST, "Selecione o usuário solicitante.");
            usr = f.usr();
            tec = f.tec();
            if (tec != null) nomeTecnico(tec);
            if (c.status().equals("Em Atendimento") && tec == null)
                throw erro(HttpStatus.BAD_REQUEST, "Chamado em atendimento precisa de um técnico responsável.");
        }
        dao.atualizar(c.comEdicao(f.titulo().trim(), f.desc().trim(), f.prio(), usr, tec));
        dao.evento(id, "Chamado editado por " + s.nome());
    }

    public void atribuir(Sessao s, int id, Integer tecnicoId) {
        if (!s.role().equals("adm")) throw erro(HttpStatus.FORBIDDEN, "Apenas o administrador atribui técnicos.");
        Chamado c = obter(id);
        if (!c.ativo()) throw erro(HttpStatus.CONFLICT, "Chamado finalizado não pode ser alterado.");
        if (tecnicoId == null) throw erro(HttpStatus.BAD_REQUEST, "Selecione um técnico.");
        String nome = nomeTecnico(tecnicoId);  // RN02: um técnico por vez (campo único)
        dao.atualizar(c.comSituacao(c.status(), tecnicoId, c.fechamento(), c.solucao()));
        dao.evento(id, "Atribuído a " + nome + " por " + s.nome());
    }

    public void iniciar(Sessao s, int id) {
        if (!s.role().equals("tec")) throw erro(HttpStatus.FORBIDDEN, "Apenas técnicos iniciam o atendimento (RN04).");
        Chamado c = obter(id);
        if (!c.status().equals("Aberto")) throw erro(HttpStatus.CONFLICT, "Só é possível iniciar chamados Abertos.");
        if (c.tec() != null && c.tec() != s.id())
            throw erro(HttpStatus.FORBIDDEN, "Este chamado está atribuído a outro técnico.");
        dao.atualizar(c.comSituacao("Em Atendimento", s.id(), null, null));
        dao.evento(id, "Atendimento iniciado por " + s.nome());
    }

    public void resolver(Sessao s, int id, String solucao) {
        if (!s.role().equals("tec")) throw erro(HttpStatus.FORBIDDEN, "Apenas técnicos resolvem chamados (RN04).");
        Chamado c = obter(id);
        if (!c.status().equals("Em Atendimento")) throw erro(HttpStatus.CONFLICT, "O chamado precisa estar Em Atendimento.");
        if (c.tec() == null || c.tec() != s.id())
            throw erro(HttpStatus.FORBIDDEN, "Apenas o técnico responsável resolve o chamado.");
        if (solucao == null || solucao.trim().length() < 5)
            throw erro(HttpStatus.BAD_REQUEST, "Descreva a solução aplicada.");
        // RN05: a data de fechamento só é gravada na resolução.
        dao.atualizar(c.comSituacao("Resolvido", c.tec(), LocalDate.now().toString(), solucao.trim()));
        dao.evento(id, "Resolvido por " + s.nome() + ": " + solucao.trim());
    }

    public void cancelar(Sessao s, int id) {
        Chamado c = obter(id);
        if (!c.ativo()) throw erro(HttpStatus.CONFLICT, "Chamado finalizado não pode ser alterado.");
        boolean pode = s.role().equals("adm")
                || (s.role().equals("tec") && c.tec() != null && c.tec() == s.id())
                || (s.role().equals("usr") && c.usr() == s.id());
        if (!pode) throw erro(HttpStatus.FORBIDDEN, "Sem permissão para cancelar este chamado.");
        dao.atualizar(c.comSituacao("Cancelado", c.tec(), null, c.solucao()));
        dao.evento(id, "Chamado cancelado por " + s.nome());
    }

    public void excluir(Sessao s, int id) {
        if (!s.role().equals("adm")) throw erro(HttpStatus.FORBIDDEN, "Apenas o administrador exclui chamados.");
        Chamado c = obter(id);
        if (c.status().equals("Resolvido"))
            throw erro(HttpStatus.CONFLICT, "RN06: chamados resolvidos não podem ser excluídos.");
        dao.excluir(id);
    }
}
