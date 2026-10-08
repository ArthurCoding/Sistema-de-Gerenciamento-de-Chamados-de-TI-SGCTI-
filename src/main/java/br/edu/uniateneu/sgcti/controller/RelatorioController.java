package br.edu.uniateneu.sgcti.controller;

import br.edu.uniateneu.sgcti.service.RelatorioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {
    private final Auth auth;
    private final RelatorioService service;

    public RelatorioController(Auth auth, RelatorioService service) {
        this.auth = auth;
        this.service = service;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(HttpServletRequest rq, @RequestParam String tipo,
                                      @RequestParam(required = false) String de, @RequestParam(required = false) String ate) {
        byte[] pdf = service.gerarPdf(auth.de(rq), tipo, de, ate);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("relatorio-" + tipo + ".pdf").build().toString())
                .body(pdf);
    }
}
