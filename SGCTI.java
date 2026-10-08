import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * SGCTI - Sistema de Gerenciamento de Chamados de TI (front-end em Java Swing).
 * Para compilar e executar:  javac SGCTI.java   e depois   java SGCTI
 */
public class SGCTI extends JFrame {

    // ===== 1. MODELO (classes de domínio, conforme seção 4 e 6 do PDF) =====
    static abstract class Pessoa {
        int id;
        abstract String[] valores();             // dados para exibir/editar
        abstract void setValores(String[] v);    // grava os dados do formulário
    }

    static class Tecnico extends Pessoa {
        String nome, cpf, especialidade, email, telefone;
        String[] valores() { return new String[]{nome, cpf, especialidade, email, telefone}; }
        void setValores(String[] v) { nome = v[0]; cpf = v[1]; especialidade = v[2]; email = v[3]; telefone = v[4]; }
        public String toString() { return nome; }
    }

    static class Usuario extends Pessoa {
        String nome, setor, email, telefone;
        String[] valores() { return new String[]{nome, setor, email, telefone}; }
        void setValores(String[] v) { nome = v[0]; setor = v[1]; email = v[2]; telefone = v[3]; }
        public String toString() { return nome; }
    }

    static class Chamado {
        int id;
        String titulo, descricao, prioridade, status;
        Usuario usuario;      // Usuário (1) -- (N) Chamado
        Tecnico tecnico;      // Técnico (1) -- (N) Chamado
        LocalDate abertura, fechamento;
    }

    // ===== 2. DADOS (em memória; no sistema real viriam do MySQL via DAO) =====
    static final List<Tecnico> tecnicos = new ArrayList<>();
    static final List<Usuario> usuarios = new ArrayList<>();
    static final List<Chamado> chamados = new ArrayList<>();
    static int seq = 4;
    static final String[] STATUS = {"Aberto", "Em Atendimento", "Resolvido", "Cancelado"};
    static final DateTimeFormatter F =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    final JComboBox<String> perfil = new JComboBox<>(new String[]{"Administrador", "Técnico"});

    // ===== 3. TELA PRINCIPAL =====
    SGCTI() {
        super("SGCTI - Sistema de Gerenciamento de Chamados de TI");
        dadosIniciais();

        PainelChamados pc = new PainelChamados();
        Runnable aoMudar = pc::recarregar;   // quando cadastros mudam, atualiza os combos dos chamados

        PainelCadastro<Tecnico> pt = new PainelCadastro<>(
                new String[]{"Nome", "CPF", "Especialidade", "E-mail", "Telefone"}, tecnicos, Tecnico::new,
                t -> chamados.stream().anyMatch(c -> c.tecnico == t), aoMudar);
        PainelCadastro<Usuario> pu = new PainelCadastro<>(
                new String[]{"Nome", "Setor", "E-mail", "Telefone"}, usuarios, Usuario::new,
                u -> chamados.stream().anyMatch(c -> c.usuario == u), aoMudar);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Chamados", pc);
        abas.addTab("Técnicos", pt);
        abas.addTab("Usuários", pu);
        abas.addTab("Relatórios", new PainelRelatorio());

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topo.add(new JLabel("Perfil:"));
        topo.add(perfil);

        add(topo, BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
    }

    void dadosIniciais() {
        Tecnico t = new Tecnico();
        t.id = 1;
        t.setValores(new String[]{"Rafael Monteiro", "52998224725", "Redes", "rafael@empresa.com", "85999112233"});
        tecnicos.add(t);
        Usuario u = new Usuario();
        u.id = 2;
        u.setValores(new String[]{"Ana Lima", "Financeiro", "ana@empresa.com", "85997011020"});
        usuarios.add(u);
        Chamado c = new Chamado();
        c.id = 3; c.titulo = "Sem acesso à rede"; c.descricao = "Notebook não conecta.";
        c.prioridade = "Alta"; c.status = "Aberto"; c.usuario = u; c.tecnico = t;
        c.abertura = LocalDate.now().minusDays(2);
        chamados.add(c);
    }

    // ===== Funções auxiliares =====
    void aviso(String msg) { JOptionPane.showMessageDialog(this, msg); }

    boolean confirma(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    static DefaultTableModel novoModelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    static JPanel linha() { return new JPanel(new FlowLayout(FlowLayout.LEFT)); }

    // ===== 4. CADASTROS: TÉCNICO E USUÁRIO (RF01 a RF08) - um painel genérico para os dois =====
    class PainelCadastro<T extends Pessoa> extends JPanel {
        final String[] campos;
        final List<T> lista;
        final Supplier<T> novo;
        final Predicate<T> temVinculo;
        final Runnable aoMudar;
        final JTextField[] txt;
        final DefaultTableModel modelo;
        final JTable tabela;
        T editando = null;

        PainelCadastro(String[] campos, List<T> lista, Supplier<T> novo, Predicate<T> temVinculo, Runnable aoMudar) {
            super(new BorderLayout(8, 8));
            this.campos = campos; this.lista = lista; this.novo = novo;
            this.temVinculo = temVinculo; this.aoMudar = aoMudar;
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel form = linha();
            txt = new JTextField[campos.length];
            for (int i = 0; i < campos.length; i++) {
                txt[i] = new JTextField(10);
                form.add(new JLabel(campos[i]));
                form.add(txt[i]);
            }
            JButton salvar = new JButton("Salvar");
            salvar.addActionListener(e -> salvar());
            JButton cancelar = new JButton("Limpar");
            cancelar.addActionListener(e -> limpar());
            form.add(salvar);
            form.add(cancelar);

            String[] colunas = new String[campos.length + 1];
            colunas[0] = "#";
            System.arraycopy(campos, 0, colunas, 1, campos.length);
            modelo = novoModelo(colunas);
            tabela = new JTable(modelo);

            JPanel sul = linha();
            JButton editar = new JButton("Editar");
            editar.addActionListener(e -> editar());
            JButton excluir = new JButton("Excluir");
            excluir.addActionListener(e -> excluir());
            sul.add(editar);
            sul.add(excluir);

            add(form, BorderLayout.NORTH);
            add(new JScrollPane(tabela), BorderLayout.CENTER);
            add(sul, BorderLayout.SOUTH);
            atualizar();
        }

        void atualizar() {
            modelo.setRowCount(0);
            for (T p : lista) {
                String[] v = p.valores();
                Object[] row = new Object[v.length + 1];
                row[0] = p.id;
                System.arraycopy(v, 0, row, 1, v.length);
                modelo.addRow(row);
            }
        }

        void limpar() {
            editando = null;
            for (JTextField t : txt) t.setText("");
        }

        void salvar() {                       // Criar (C) ou Atualizar (U)
            String[] v = new String[campos.length];
            for (int i = 0; i < campos.length; i++) {
                v[i] = txt[i].getText().trim();
                if (v[i].isEmpty()) { aviso("Preencha o campo " + campos[i] + "."); return; }
                if (campos[i].equals("CPF") && v[i].replaceAll("\\D", "").length() != 11) {
                    aviso("O CPF deve ter 11 dígitos."); return;
                }
                if (campos[i].equals("E-mail") && !v[i].contains("@")) { aviso("E-mail inválido."); return; }
            }
            T obj = editando != null ? editando : novo.get();
            obj.setValores(v);
            if (editando == null) { obj.id = seq++; lista.add(obj); }
            limpar();
            atualizar();
            aoMudar.run();
        }

        T selecionado() {
            int r = tabela.getSelectedRow();
            if (r < 0) { aviso("Selecione uma linha na tabela."); return null; }
            int id = (Integer) modelo.getValueAt(r, 0);
            for (T p : lista) if (p.id == id) return p;
            return null;
        }

        void editar() {
            T p = selecionado();
            if (p == null) return;
            editando = p;
            String[] v = p.valores();
            for (int i = 0; i < v.length; i++) txt[i].setText(v[i]);
        }

        void excluir() {                      // Excluir (D) - protege a integridade dos dados (RNF10)
            T p = selecionado();
            if (p == null) return;
            if (temVinculo.test(p)) { aviso("Existem chamados vinculados. Não é possível excluir."); return; }
            if (confirma("Excluir este cadastro?")) {
                lista.remove(p);
                limpar();
                atualizar();
                aoMudar.run();
            }
        }
    }

    // ===== 5. CHAMADOS (RF09 a RF16) =====
    class PainelChamados extends JPanel {
        final JTextField txtTitulo = new JTextField(18), txtDescricao = new JTextField(30);
        final JComboBox<String> cbPrioridade = new JComboBox<>(new String[]{"Baixa", "Média", "Alta"});
        final JComboBox<Object> cbUsuario = new JComboBox<>(), cbTecnico = new JComboBox<>();
        final JComboBox<Object> fStatus = new JComboBox<>(), fTecnico = new JComboBox<>();
        final JTextField fDe = new JTextField(8), fAte = new JTextField(8);
        final DefaultTableModel modelo = novoModelo("#", "Título", "Solicitante", "Técnico",
                "Prioridade", "Status", "Abertura", "Fechamento");
        final JTable tabela = new JTable(modelo);
        Chamado editando = null;
        LocalDate de = null, ate = null;

        PainelChamados() {
            super(new BorderLayout(8, 8));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel l1 = linha(), l2 = linha(), l3 = linha();
            l1.add(new JLabel("Título")); l1.add(txtTitulo);
            l1.add(new JLabel("Descrição")); l1.add(txtDescricao);
            l2.add(new JLabel("Prioridade")); l2.add(cbPrioridade);
            l2.add(new JLabel("Solicitante")); l2.add(cbUsuario);
            l2.add(new JLabel("Técnico")); l2.add(cbTecnico);
            JButton salvar = new JButton("Salvar chamado");
            salvar.addActionListener(e -> salvar());
            JButton limpar = new JButton("Limpar");
            limpar.addActionListener(e -> limpar());
            l2.add(salvar); l2.add(limpar);

            l3.add(new JLabel("Filtros - Status")); l3.add(fStatus);
            l3.add(new JLabel("Técnico")); l3.add(fTecnico);
            l3.add(new JLabel("De (dd/mm/aaaa)")); l3.add(fDe);
            l3.add(new JLabel("Até")); l3.add(fAte);
            JButton filtrar = new JButton("Filtrar");
            filtrar.addActionListener(e -> aplicarFiltro());
            JButton limparF = new JButton("Limpar filtros");
            limparF.addActionListener(e -> { fDe.setText(""); fAte.setText(""); de = null; ate = null; recarregar(); });
            l3.add(filtrar); l3.add(limparF);

            JPanel topo = new JPanel();
            topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
            topo.add(l1); topo.add(l2); topo.add(l3);

            JPanel sul = linha();
            sul.add(botao("Editar", e -> editar()));
            sul.add(botao("Iniciar atendimento", e -> mudarStatus("Em Atendimento")));
            sul.add(botao("Resolver", e -> mudarStatus("Resolvido")));
            sul.add(botao("Cancelar chamado", e -> mudarStatus("Cancelado")));
            sul.add(botao("Excluir", e -> excluir()));

            add(topo, BorderLayout.NORTH);
            add(new JScrollPane(tabela), BorderLayout.CENTER);
            add(sul, BorderLayout.SOUTH);
            recarregar();
        }

        JButton botao(String texto, java.awt.event.ActionListener acao) {
            JButton b = new JButton(texto);
            b.addActionListener(acao);
            return b;
        }

        void recarregar() {                   // recarrega combos e tabela
            cbUsuario.removeAllItems(); cbUsuario.addItem("Selecione");
            usuarios.forEach(cbUsuario::addItem);
            cbTecnico.removeAllItems(); cbTecnico.addItem("Sem técnico");
            tecnicos.forEach(cbTecnico::addItem);
            fTecnico.removeAllItems(); fTecnico.addItem("Todos");
            tecnicos.forEach(fTecnico::addItem);
            fStatus.removeAllItems(); fStatus.addItem("Todos");
            for (String s : STATUS) fStatus.addItem(s);
            limpar();
            atualizarTabela();
        }

        void limpar() {
            editando = null;
            txtTitulo.setText(""); txtDescricao.setText("");
            cbPrioridade.setSelectedIndex(0);
            if (cbUsuario.getItemCount() > 0) cbUsuario.setSelectedIndex(0);
            if (cbTecnico.getItemCount() > 0) cbTecnico.setSelectedIndex(0);
        }

        void aplicarFiltro() {                // RF16: status, técnico ou período
            try {
                de = fDe.getText().isBlank() ? null : LocalDate.parse(fDe.getText().trim(), F);
                ate = fAte.getText().isBlank() ? null : LocalDate.parse(fAte.getText().trim(), F);
            } catch (DateTimeParseException ex) {
                aviso("Data inválida. Use o formato dd/mm/aaaa.");
                return;
            }
            atualizarTabela();
        }

        void atualizarTabela() {
            modelo.setRowCount(0);
            Object fs = fStatus.getSelectedItem(), ft = fTecnico.getSelectedItem();
            for (Chamado c : chamados) {
                if (fs instanceof String && !fs.equals("Todos") && !c.status.equals(fs)) continue;
                if (ft instanceof Tecnico && c.tecnico != ft) continue;
                if (de != null && c.abertura.isBefore(de)) continue;
                if (ate != null && c.abertura.isAfter(ate)) continue;
                modelo.addRow(new Object[]{c.id, c.titulo, c.usuario.nome,
                        c.tecnico == null ? "—" : c.tecnico.nome, c.prioridade, c.status,
                        c.abertura.format(F), c.fechamento == null ? "—" : c.fechamento.format(F)});
            }
        }

        Chamado selecionado() {
            int r = tabela.getSelectedRow();
            if (r < 0) { aviso("Selecione um chamado na tabela."); return null; }
            int id = (Integer) modelo.getValueAt(r, 0);
            for (Chamado c : chamados) if (c.id == id) return c;
            return null;
        }

        void salvar() {                       // RF09 e RF10
            if (txtTitulo.getText().isBlank() || txtDescricao.getText().isBlank()) {
                aviso("Informe o título e a descrição."); return;
            }
            if (!(cbUsuario.getSelectedItem() instanceof Usuario)) {      // RN01
                aviso("RN01: todo chamado precisa de um usuário solicitante."); return;
            }
            Object t = cbTecnico.getSelectedItem();                       // RN02: um técnico por vez
            Tecnico tec = t instanceof Tecnico ? (Tecnico) t : null;
            Chamado c = editando;
            if (c != null && c.status.equals("Em Atendimento") && tec == null) {
                aviso("Chamado em atendimento precisa de um técnico."); return;
            }
            if (c == null) {                                              // novo chamado
                c = new Chamado();
                c.id = seq++;
                c.status = "Aberto";                                      // RN03
                c.abertura = LocalDate.now();
                chamados.add(c);
            }
            c.titulo = txtTitulo.getText().trim();
            c.descricao = txtDescricao.getText().trim();
            c.prioridade = (String) cbPrioridade.getSelectedItem();
            c.usuario = (Usuario) cbUsuario.getSelectedItem();
            c.tecnico = tec;                                              // RF13
            limpar();
            atualizarTabela();
        }

        void editar() {
            Chamado c = selecionado();
            if (c == null) return;
            if (c.status.equals("Resolvido") || c.status.equals("Cancelado")) {
                aviso("Chamados encerrados não podem ser editados."); return;
            }
            editando = c;
            txtTitulo.setText(c.titulo);
            txtDescricao.setText(c.descricao);
            cbPrioridade.setSelectedItem(c.prioridade);
            cbUsuario.setSelectedItem(c.usuario);
            if (c.tecnico == null) cbTecnico.setSelectedIndex(0); else cbTecnico.setSelectedItem(c.tecnico);
        }

        void mudarStatus(String novo) {       // RF14 + regras RN04 e RN05
            Chamado c = selecionado();
            if (c == null) return;
            if (!(c.status.equals("Aberto") || c.status.equals("Em Atendimento"))) {
                aviso("Chamado encerrado não pode mudar de status."); return;
            }
            if (novo.equals("Em Atendimento") && !c.status.equals("Aberto")) {
                aviso("Este chamado já está em atendimento."); return;
            }
            if (novo.equals("Resolvido") && !c.status.equals("Em Atendimento")) {
                aviso("Inicie o atendimento antes de resolver."); return;
            }
            boolean soTecnico = novo.equals("Em Atendimento") || novo.equals("Resolvido");
            if (soTecnico && !"Técnico".equals(perfil.getSelectedItem())) {   // RN04
                aviso("RN04: apenas técnicos podem iniciar ou resolver chamados. Troque o perfil."); return;
            }
            if (soTecnico && c.tecnico == null) {
                aviso("RF13: atribua um técnico ao chamado antes (botão Editar)."); return;
            }
            c.status = novo;
            c.fechamento = novo.equals("Resolvido") ? LocalDate.now() : null; // RN05
            atualizarTabela();
        }

        void excluir() {                      // RF11 + RN06
            Chamado c = selecionado();
            if (c == null) return;
            if (c.status.equals("Resolvido")) {
                aviso("RN06: chamados resolvidos não podem ser excluídos."); return;
            }
            if (confirma("Excluir o chamado #" + c.id + "?")) {
                chamados.remove(c);
                atualizarTabela();
            }
        }
    }

    // ===== 6. RELATÓRIOS (RF17 a RF19) =====
    class PainelRelatorio extends JPanel {
        final JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Por período", "Por técnico", "Por status"});
        final JTextField txtDe = new JTextField(8), txtAte = new JTextField(8);
        final DefaultTableModel modelo = novoModelo("Grupo", "Quantidade");
        final JTable tabela = new JTable(modelo);
        String titulo = "";

        PainelRelatorio() {
            super(new BorderLayout(8, 8));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JPanel form = linha();
            form.add(new JLabel("Tipo")); form.add(cbTipo);
            form.add(new JLabel("De (dd/mm/aaaa)")); form.add(txtDe);
            form.add(new JLabel("Até")); form.add(txtAte);
            JButton gerar = new JButton("Gerar");
            gerar.addActionListener(e -> gerar());
            JButton pdf = new JButton("Exportar PDF");
            pdf.addActionListener(e -> exportar());
            form.add(gerar); form.add(pdf);
            add(form, BorderLayout.NORTH);
            add(new JScrollPane(tabela), BorderLayout.CENTER);
        }

        void gerar() {
            LocalDate de, ate;
            try {
                de = txtDe.getText().isBlank() ? null : LocalDate.parse(txtDe.getText().trim(), F);
                ate = txtAte.getText().isBlank() ? null : LocalDate.parse(txtAte.getText().trim(), F);
            } catch (DateTimeParseException ex) {
                aviso("Data inválida. Use o formato dd/mm/aaaa.");
                return;
            }
            int tipo = cbTipo.getSelectedIndex();
            Map<String, Integer> total = new TreeMap<>();
            int soma = 0;
            for (Chamado c : chamados) {
                if (de != null && c.abertura.isBefore(de)) continue;
                if (ate != null && c.abertura.isAfter(ate)) continue;
                String chave = tipo == 0 ? c.abertura.format(DateTimeFormatter.ofPattern("MM/yyyy"))
                        : tipo == 1 ? (c.tecnico == null ? "Sem técnico" : c.tecnico.nome) : c.status;
                total.merge(chave, 1, Integer::sum);
                soma++;
            }
            modelo.setRowCount(0);
            for (Map.Entry<String, Integer> e : total.entrySet()) modelo.addRow(new Object[]{e.getKey(), e.getValue()});
            modelo.addRow(new Object[]{"Total", soma});
            titulo = "Relatorio de chamados - " + cbTipo.getSelectedItem();
        }

        void exportar() {                     // abre a impressão; escolha "Salvar como PDF"
            if (modelo.getRowCount() == 0) { aviso("Gere o relatório antes de exportar."); return; }
            try {
                tabela.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat(titulo), new MessageFormat("Pagina {0}"));
            } catch (PrinterException | HeadlessException ex) {
                aviso("Não foi possível imprimir: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SGCTI().setVisible(true));
    }
}
