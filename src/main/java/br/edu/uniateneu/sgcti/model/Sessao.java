package br.edu.uniateneu.sgcti.model;

/** Quem está logado: role = adm | tec | usr. */
public record Sessao(String role, int id, String nome) {}
