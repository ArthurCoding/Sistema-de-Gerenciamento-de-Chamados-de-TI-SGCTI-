package br.edu.uniateneu.sgcti.service;

import java.time.LocalDate;

/** Utilitários de formatação e validação (CPF, e-mail, telefone, datas). */
public final class Formato {
    private Formato() {}

    public static String digitos(String s) { return s == null ? "" : s.replaceAll("\\D", ""); }

    public static String cpf(String d) {
        if (d == null || d.length() != 11) return d;
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    public static boolean cpfValido(String s) {
        String d = digitos(s);
        if (d.length() != 11 || d.matches("(\\d)\\1+")) return false;
        for (int t = 9; t < 11; t++) {
            int soma = 0;
            for (int i = 0; i < t; i++) soma += (d.charAt(i) - '0') * (t + 1 - i);
            if (d.charAt(t) - '0' != (soma * 10) % 11 % 10) return false;
        }
        return true;
    }

    public static boolean emailValido(String s) { return s != null && s.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"); }

    public static boolean telefoneValido(String s) { return digitos(s).length() >= 10; }

    /** yyyy-MM-dd (ou ISO com hora) para dd/MM/yyyy. */
    public static String data(String iso) {
        if (iso == null || iso.length() < 10) return "—";
        LocalDate d = LocalDate.parse(iso.substring(0, 10));
        return String.format("%02d/%02d/%04d", d.getDayOfMonth(), d.getMonthValue(), d.getYear());
    }
}
