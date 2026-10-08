package br.edu.uniateneu.sgcti.model;

import java.util.List;

public record Chamado(int id, String titulo, String desc, String abertura, String fechamento,
                      Integer tec, int usr, String prio, String status, String solucao,
                      List<Evento> hist) {

    public boolean ativo() {
        return "Aberto".equals(status) || "Em Atendimento".equals(status);
    }

    public Chamado comEdicao(String titulo, String desc, String prio, int usr, Integer tec) {
        return new Chamado(id, titulo, desc, abertura, fechamento, tec, usr, prio, status, solucao, hist);
    }

    public Chamado comSituacao(String status, Integer tec, String fechamento, String solucao) {
        return new Chamado(id, titulo, desc, abertura, fechamento, tec, usr, prio, status, solucao, hist);
    }
}
