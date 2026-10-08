package br.edu.uniateneu.sgcti.service;

import br.edu.uniateneu.sgcti.dao.TecnicoDao;
import br.edu.uniateneu.sgcti.dao.UsuarioDao;
import br.edu.uniateneu.sgcti.model.Tecnico;
import br.edu.uniateneu.sgcti.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Validações e CRUD de técnicos e usuários solicitantes. */
@Service
public class CadastroService {
    private final TecnicoDao tecnicos;
    private final UsuarioDao usuarios;

    public CadastroService(TecnicoDao tecnicos, UsuarioDao usuarios) {
        this.tecnicos = tecnicos;
        this.usuarios = usuarios;
    }

    /** Campos do formulário: técnico usa nome, cpf, esp, email, tel; usuário usa nome, setor, email, tel. */
    public record Form(String nome, String cpf, String esp, String setor, String email, String tel) {}

    private static ApiException invalido(String msg) { return new ApiException(HttpStatus.BAD_REQUEST, msg); }

    private static String vazio(String s) { return s == null ? "" : s.trim(); }

    private void validarComum(Form f) {
        if (vazio(f.nome()).length() < 3) throw invalido("Informe o nome completo.");
    }

    private void validarContato(Form f) {
        if (!Formato.emailValido(f.email())) throw invalido("E-mail inválido.");
        if (!Formato.telefoneValido(f.tel())) throw invalido("Telefone inválido. Use DDD e número.");
    }

    private Tecnico paraTecnico(int id, Form f) {
        validarComum(f);
        if (!Formato.cpfValido(f.cpf())) throw invalido("CPF inválido. Confira os 11 dígitos.");
        if (tecnicos.cpfExiste(Formato.digitos(f.cpf()), id))
            throw new ApiException(HttpStatus.CONFLICT, "Já existe um técnico com este CPF.");
        if (vazio(f.esp()).isEmpty()) throw invalido("Informe a especialidade.");
        validarContato(f);
        return new Tecnico(id, f.nome().trim(), Formato.digitos(f.cpf()), f.esp().trim(), f.email().trim(), f.tel().trim());
    }

    private Usuario paraUsuario(int id, Form f) {
        validarComum(f);
        if (vazio(f.setor()).isEmpty()) throw invalido("Informe o setor.");
        validarContato(f);
        return new Usuario(id, f.nome().trim(), f.setor().trim(), f.email().trim(), f.tel().trim());
    }

    public void criarTecnico(Form f) { tecnicos.inserir(paraTecnico(0, f)); }

    public void editarTecnico(int id, Form f) {
        tecnicos.buscar(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Técnico não encontrado."));
        tecnicos.atualizar(paraTecnico(id, f));
    }

    public void excluirTecnico(int id) {
        Tecnico t = tecnicos.buscar(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Técnico não encontrado."));
        int n = tecnicos.totalChamados(id);
        if (n > 0) throw new ApiException(HttpStatus.CONFLICT, "Não é possível excluir: há " + n + " chamado(s) vinculado(s) a " + t.nome() + ".");
        tecnicos.excluir(id);
    }

    public void criarUsuario(Form f) { usuarios.inserir(paraUsuario(0, f)); }

    public void editarUsuario(int id, Form f) {
        usuarios.buscar(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        usuarios.atualizar(paraUsuario(id, f));
    }

    public void excluirUsuario(int id) {
        Usuario u = usuarios.buscar(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        int n = usuarios.totalChamados(id);
        if (n > 0) throw new ApiException(HttpStatus.CONFLICT, "Não é possível excluir: há " + n + " chamado(s) vinculado(s) a " + u.nome() + ".");
        usuarios.excluir(id);
    }
}
