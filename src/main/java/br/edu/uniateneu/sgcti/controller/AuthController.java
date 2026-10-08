package br.edu.uniateneu.sgcti.controller;

import br.edu.uniateneu.sgcti.dao.TecnicoDao;
import br.edu.uniateneu.sgcti.dao.UsuarioDao;
import br.edu.uniateneu.sgcti.model.Sessao;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/login")
public class AuthController {
    private final Auth auth;
    private final TecnicoDao tecnicos;
    private final UsuarioDao usuarios;

    public AuthController(Auth auth, TecnicoDao tecnicos, UsuarioDao usuarios) {
        this.auth = auth;
        this.tecnicos = tecnicos;
        this.usuarios = usuarios;
    }

    public record Login(String perfil, Integer id) {}

    /** Nomes para a tela de login (público; só id e nome). */
    @GetMapping("/opcoes")
    public Map<String, List<Map<String, Object>>> opcoes() {
        return Map.of(
                "tec", tecnicos.listar().stream().map(t -> Map.<String, Object>of("id", t.id(), "nome", t.nome())).toList(),
                "usr", usuarios.listar().stream().map(u -> Map.<String, Object>of("id", u.id(), "nome", u.nome())).toList());
    }

    @PostMapping
    public Sessao entrar(@RequestBody Login l) {
        return auth.entrar(l.perfil(), l.id() == null ? 0 : l.id());
    }
}
