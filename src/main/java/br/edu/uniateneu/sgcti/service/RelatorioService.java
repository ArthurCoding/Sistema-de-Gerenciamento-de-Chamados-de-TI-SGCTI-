package br.edu.uniateneu.sgcti.service;

import br.edu.uniateneu.sgcti.dao.ChamadoDao;
import br.edu.uniateneu.sgcti.dao.TecnicoDao;
import br.edu.uniateneu.sgcti.dao.UsuarioDao;
import br.edu.uniateneu.sgcti.model.Chamado;
import br.edu.uniateneu.sgcti.model.Sessao;
import br.edu.uniateneu.sgcti.model.Tecnico;
import br.edu.uniateneu.sgcti.model.Usuario;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Relatórios gerenciais em PDF (OpenPDF): por período, por técnico e por status. */
@Service
public class RelatorioService {
    private static final List<String> STATUS = List.of("Aberto", "Em Atendimento", "Resolvido", "Cancelado");

    private final ChamadoDao chamados;
    private final TecnicoDao tecnicos;
    private final UsuarioDao usuarios;

    public RelatorioService(ChamadoDao chamados, TecnicoDao tecnicos, UsuarioDao usuarios) {
        this.chamados = chamados;
        this.tecnicos = tecnicos;
        this.usuarios = usuarios;
    }

    public byte[] gerarPdf(Sessao s, String tipo, String de, String ate) {
        if (!s.role().equals("adm")) throw new ApiException(HttpStatus.FORBIDDEN, "Apenas o administrador gera relatórios.");
        if (!List.of("per", "tec", "status").contains(tipo)) throw new ApiException(HttpStatus.BAD_REQUEST, "Tipo de relatório inválido.");
        de = de == null || de.isBlank() ? null : de;
        ate = ate == null || ate.isBlank() ? null : ate;
        try {
            if (de != null) LocalDate.parse(de);
            if (ate != null) LocalDate.parse(ate);
        } catch (RuntimeException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Data inválida.");
        }
        if (de != null && ate != null && de.compareTo(ate) > 0)
            throw new ApiException(HttpStatus.BAD_REQUEST, "A data inicial não pode ser maior que a final.");

        final String ini = de, fim = ate;
        Map<Integer, String> nomeTec = tecnicos.listar().stream().collect(Collectors.toMap(Tecnico::id, Tecnico::nome));
        Map<Integer, String> nomeUsr = usuarios.listar().stream().collect(Collectors.toMap(Usuario::id, Usuario::nome));
        List<Chamado> lista = chamados.listar().stream()
                .filter(c -> (ini == null || c.abertura().compareTo(ini) >= 0) && (fim == null || c.abertura().compareTo(fim) <= 0))
                .sorted(Comparator.comparing(Chamado::abertura).thenComparingInt(Chamado::id))
                .toList();

        String titulo = switch (tipo) {
            case "tec" -> "Chamados por técnico";
            case "status" -> "Chamados por status";
            default -> "Chamados por período";
        };
        String periodo = (ini != null || fim != null) ? Formato.data(ini) + " a " + Formato.data(fim) : "Todo o período";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(doc, out);
        doc.open();
        Font h0 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.GRAY);
        Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font txt = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font neg = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font cab = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font cel = FontFactory.getFont(FontFactory.HELVETICA, 9);

        doc.add(new Paragraph("UniATENEU – SGCTI", h0));
        doc.add(new Paragraph(titulo, h1));
        Paragraph info = new Paragraph("Período: " + periodo + ". Emitido em " + Formato.data(LocalDate.now().toString())
                + " por " + s.nome() + ".", txt);
        info.setSpacingAfter(12);
        doc.add(info);

        if (lista.isEmpty()) {
            doc.add(new Paragraph("Nenhum chamado no período informado.", txt));
            doc.close();
            return out.toByteArray();
        }

        // Resumo agrupado (chave ordenável; o rótulo é derivado dela)
        TreeMap<String, List<Chamado>> grupos = new TreeMap<>();
        for (Chamado c : lista) {
            String k = switch (tipo) {
                case "tec" -> c.tec() == null ? "Sem técnico" : nomeTec.getOrDefault(c.tec(), "Sem técnico");
                case "status" -> String.valueOf(STATUS.indexOf(c.status()));
                default -> c.abertura().substring(0, 7);
            };
            grupos.computeIfAbsent(k, x -> new ArrayList<>()).add(c);
        }
        PdfPTable resumo = new PdfPTable(2 + STATUS.size());
        resumo.setWidthPercentage(100);
        for (String h : concat(List.of(tipo.equals("tec") ? "Técnico" : tipo.equals("status") ? "Status" : "Mês", "Total"), STATUS))
            resumo.addCell(cabecalho(h, cab));
        for (Map.Entry<String, List<Chamado>> e : grupos.entrySet()) {
            String rotulo = switch (tipo) {
                case "status" -> STATUS.get(Integer.parseInt(e.getKey()));
                case "per" -> e.getKey().substring(5) + "/" + e.getKey().substring(0, 4);
                default -> e.getKey();
            };
            resumo.addCell(celula(rotulo, cel));
            resumo.addCell(celula(String.valueOf(e.getValue().size()), neg));
            for (String st : STATUS) resumo.addCell(celula(String.valueOf(e.getValue().stream().filter(c -> c.status().equals(st)).count()), cel));
        }
        resumo.addCell(celula("Total geral", neg));
        resumo.addCell(celula(String.valueOf(lista.size()), neg));
        for (String st : STATUS) resumo.addCell(celula(String.valueOf(lista.stream().filter(c -> c.status().equals(st)).count()), neg));
        doc.add(resumo);

        Paragraph det = new Paragraph("Detalhamento", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
        det.setSpacingBefore(16);
        det.setSpacingAfter(6);
        doc.add(det);
        PdfPTable t = new PdfPTable(new float[]{4, 22, 14, 14, 9, 11, 10, 10});
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        for (String h : List.of("#", "Título", "Solicitante", "Técnico", "Prioridade", "Status", "Abertura", "Fechamento"))
            t.addCell(cabecalho(h, cab));
        for (Chamado c : lista) {
            t.addCell(celula(String.valueOf(c.id()), cel));
            t.addCell(celula(c.titulo(), cel));
            t.addCell(celula(nomeUsr.getOrDefault(c.usr(), "—"), cel));
            t.addCell(celula(c.tec() == null ? "Sem técnico" : nomeTec.getOrDefault(c.tec(), "Sem técnico"), cel));
            t.addCell(celula(c.prio(), cel));
            t.addCell(celula(c.status(), cel));
            t.addCell(celula(Formato.data(c.abertura()), cel));
            t.addCell(celula(Formato.data(c.fechamento()), cel));
        }
        doc.add(t);
        doc.close();
        return out.toByteArray();
    }

    private static List<String> concat(List<String> a, List<String> b) {
        List<String> r = new ArrayList<>(a);
        r.addAll(b);
        return r;
    }

    private static PdfPCell cabecalho(String t, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(t, f));
        c.setBackgroundColor(new Color(0x14, 0x30, 0x3A));
        c.setPadding(5);
        return c;
    }

    private static PdfPCell celula(String t, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(t, f));
        c.setPadding(4);
        return c;
    }
}
