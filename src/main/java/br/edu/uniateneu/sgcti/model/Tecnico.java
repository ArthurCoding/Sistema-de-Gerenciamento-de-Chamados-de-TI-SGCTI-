package br.edu.uniateneu.sgcti.model;

/** Técnico de suporte. O CPF trafega formatado (000.000.000-00); no banco fica só com 11 dígitos. */
public record Tecnico(int id, String nome, String cpf, String esp, String email, String tel) {}
