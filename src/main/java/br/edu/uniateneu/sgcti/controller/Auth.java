package br.edu.uniateneu.sgcti.controller;

import br.edu.uniateneu.sgcti.dao.TecnicoDao;
import br.edu.uniateneu.sgcti.dao.UsuarioDao;
import br.edu.uniateneu.sgcti.model.Sessao;
import br.edu.uniateneu.sgcti.service.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Identifica quem está logado a partir dos cabeçalhos X-Perfil e X-Id (login simplificado por perfil, sem senha,
 * como no protótipo). Todas as permissões são conferidas aqui e nos services, e não apenas na tela (RNF05).
 */
@Component
public class Auth {
    private final TecnicoDao tecnicos;
    private final UsuarioDao usuarios;

    public Auth(TecnicoDao tecnicos, UsuarioDao usuarios) {
        this.tecnicos = tecnicos;
        this.usuarios = usuarios;
    }

    public Sessao entrar(String perfil, int id) {
        if ("adm".equals(perfil)) return new Sessao("adm", 0, "Administrador");
        if ("tec".equals(perfil))
            return tecnicos.buscar(id).map(t -> new Sessao("tec", t.id(), t.nome()))
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Selecione um técnico cadastrado."));
        if ("usr".equals(perfil))
            return usuarios.buscar(id).map(u -> new Sessao("usr", u.id(), u.nome()))
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Selecione um usuário cadastrado."));
        throw new ApiException(HttpStatus.UNAUTHORIZED, "Perfil inválido.");
    }

    public Sessao de(HttpServletRequest rq) {
        String perfil = rq.getHeader("X-Perfil"), id = rq.getHeader("X-Id");
        if (perfil == null || id == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "Faça login para continuar.");
        try {
            return entrar(perfil, Integer.parseInt(id));
        } catch (NumberFormatException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sessão inválida.");
        }
    }

    public Sessao admin(HttpServletRequest rq) {
        Sessao s = de(rq);
        if (!s.role().equals("adm")) throw new ApiException(HttpStatus.FORBIDDEN, "Acesso restrito ao administrador.");
        return s;
    }
}
