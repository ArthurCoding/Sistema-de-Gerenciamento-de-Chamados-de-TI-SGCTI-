package br.edu.uniateneu.sgcti.controller;

import br.edu.uniateneu.sgcti.service.ApiException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Transforma erros em mensagens JSON {"erro": "..."} para a tela (RNF09). */
@RestControllerAdvice
public class TratadorErros {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> regra(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> corpoInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Dados inválidos."));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> banco(DataAccessException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("erro", "Erro ao acessar o banco de dados."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> geral(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("erro", "Erro inesperado no servidor."));
    }
}
