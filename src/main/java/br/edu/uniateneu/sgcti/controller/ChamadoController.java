package br.edu.uniateneu.sgcti.controller;

import br.edu.uniateneu.sgcti.service.ChamadoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {
    private final Auth auth;
    private final ChamadoService service;

    public ChamadoController(Auth auth, ChamadoService service) {
        this.auth = auth;
        this.service = service;
    }

    public record Atribuicao(Integer tec) {}
    public record Solucao(String solucao) {}

    @PostMapping
    public void abrir(HttpServletRequest rq, @RequestBody ChamadoService.Form f) { service.abrir(auth.de(rq), f); }

    @PutMapping("/{id}")
    public void editar(HttpServletRequest rq, @PathVariable int id, @RequestBody ChamadoService.Form f) { service.editar(auth.de(rq), id, f); }

    @PutMapping("/{id}/tecnico")
    public void atribuir(HttpServletRequest rq, @PathVariable int id, @RequestBody Atribuicao a) { service.atribuir(auth.de(rq), id, a.tec()); }

    @PostMapping("/{id}/iniciar")
    public void iniciar(HttpServletRequest rq, @PathVariable int id) { service.iniciar(auth.de(rq), id); }

    @PostMapping("/{id}/resolver")
    public void resolver(HttpServletRequest rq, @PathVariable int id, @RequestBody Solucao s) { service.resolver(auth.de(rq), id, s.solucao()); }

    @PostMapping("/{id}/cancelar")
    public void cancelar(HttpServletRequest rq, @PathVariable int id) { service.cancelar(auth.de(rq), id); }

    @DeleteMapping("/{id}")
    public void excluir(HttpServletRequest rq, @PathVariable int id) { service.excluir(auth.de(rq), id); }
}
