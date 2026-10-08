package br.edu.uniateneu.sgcti.service;

import org.springframework.http.HttpStatus;

/** Erro de regra de negócio/permissão, convertido em JSON pelo TratadorErros. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
