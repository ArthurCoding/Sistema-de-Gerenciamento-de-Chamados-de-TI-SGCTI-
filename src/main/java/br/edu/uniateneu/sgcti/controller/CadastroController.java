package br.edu.uniateneu.sgcti.controller;

import br.edu.uniateneu.sgcti.service.CadastroService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/** CRUD de técnicos e usuários solicitantes (somente administrador). */
@RestController
@RequestMapping("/api")
public class CadastroController {
    private final Auth auth;
    private final CadastroService service;

    public CadastroController(Auth auth, CadastroService service) {
        this.auth = auth;
        this.service = service;
    }

    @PostMapping("/tecnicos")
    public void criarTecnico(HttpServletRequest rq, @RequestBody CadastroService.Form f) { auth.admin(rq); service.criarTecnico(f); }

    @PutMapping("/tecnicos/{id}")
    public void editarTecnico(HttpServletRequest rq, @PathVariable int id, @RequestBody CadastroService.Form f) { auth.admin(rq); service.editarTecnico(id, f); }

    @DeleteMapping("/tecnicos/{id}")
    public void excluirTecnico(HttpServletRequest rq, @PathVariable int id) { auth.admin(rq); service.excluirTecnico(id); }

    @PostMapping("/usuarios")
    public void criarUsuario(HttpServletRequest rq, @RequestBody CadastroService.Form f) { auth.admin(rq); service.criarUsuario(f); }

    @PutMapping("/usuarios/{id}")
    public void editarUsuario(HttpServletRequest rq, @PathVariable int id, @RequestBody CadastroService.Form f) { auth.admin(rq); service.editarUsuario(id, f); }

    @DeleteMapping("/usuarios/{id}")
    public void excluirUsuario(HttpServletRequest rq, @PathVariable int id) { auth.admin(rq); service.excluirUsuario(id); }
}
