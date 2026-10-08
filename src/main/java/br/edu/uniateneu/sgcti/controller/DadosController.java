package br.edu.uniateneu.sgcti.controller;

import br.edu.uniateneu.sgcti.dao.TecnicoDao;
import br.edu.uniateneu.sgcti.dao.UsuarioDao;
import br.edu.uniateneu.sgcti.model.Chamado;
import br.edu.uniateneu.sgcti.model.Sessao;
import br.edu.uniateneu.sgcti.model.Tecnico;
import br.edu.uniateneu.sgcti.model.Usuario;
import br.edu.uniateneu.sgcti.service.ChamadoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Entrega à tela os dados que o perfil logado pode ver. */
@RestController
@RequestMapping("/api/dados")
public class DadosController {
    private final Auth auth;
    private final TecnicoDao tecnicos;
    private final UsuarioDao usuarios;
    private final ChamadoService chamados;

    public DadosController(Auth auth, TecnicoDao tecnicos, UsuarioDao usuarios, ChamadoService chamados) {
        this.auth = auth;
        this.tecnicos = tecnicos;
        this.usuarios = usuarios;
        this.chamados = chamados;
    }

    @GetMapping
    public Map<String, Object> dados(HttpServletRequest rq) {
        Sessao s = auth.de(rq);
        boolean adm = s.role().equals("adm");
        // Quem não é administrador recebe só id e nome (para exibir nomes nos chamados).
        List<Tecnico> tec = tecnicos.listar().stream()
                .map(t -> adm ? t : new Tecnico(t.id(), t.nome(), null, null, null, null)).toList();
        List<Usuario> usr = usuarios.listar().stream()
                .map(u -> adm ? u : new Usuario(u.id(), u.nome(), null, null, null)).toList();
        List<Chamado> cha = chamados.visiveis(s);
        return Map.of("tec", tec, "usr", usr, "cha", cha);
    }
}
