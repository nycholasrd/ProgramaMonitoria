package ProgramaMonitoria;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SistemaMonitoria {
    private List<Usuario> usuarios;
    private List<Edital> editais;
    
    private Usuario usuarioLogado;
    private final String ARQUIVO_DADOS = "dados_monitoria.bin";
    private PersistenciaXML persistencia;

    public SistemaMonitoria() {
        this.usuarios = new ArrayList<>();
        this.editais = new ArrayList<>();
        this.persistencia = new PersistenciaXML(); 
        
        carregarDados(); 
    }

    public void cadastrarUsuario(Usuario u) {
        usuarios.add(u);
        salvarDados();
    }
    
    public Inscricao cadastrarInscricao(Aluno aluno, Disciplina disciplina, double cre, double nota, Edital edital) throws MonitoriaException {

        if (!edital.podeInscrever()) {
            throw new MonitoriaException("Limite de vagas atingido (" + edital.getLimiteInscricoes() + ") para este edital.");
        }
        Inscricao novaInscricao = new Inscricao(aluno, disciplina, cre, nota, edital);
        edital.incrementarInscricoesAtivas();
        salvarAlteracoes(); 
        
        return novaInscricao;
    }

    public Usuario fazerLogin(String email, String senha) throws MonitoriaException {
        for (Usuario u : usuarios) {
            if (u.autenticar(email, senha)) {
                this.usuarioLogado = u;
                return u;
            }
        }
        throw new MonitoriaException("Credenciais inválidas.");
    }

    public void cadastrarEdital(Edital e) {
        if (usuarioLogado instanceof Coordenador) { 
            editais.add(e);
            salvarDados();
        }
    }
    
    public List<Edital> getEditais() { 
        if (this.editais == null) this.editais = new ArrayList<>();
        return editais; 
    }

    private void salvarDados() {
        persistencia.salvar(this.usuarios, this.editais);
    }

    private void carregarDados() {
        SistemaDadosWrapper dados = persistencia.carregar();
        
        if (dados != null) {
            this.usuarios = dados.getUsuarios();
            this.editais = dados.getEditais();
            System.out.println("Dados carregados via XML.");
        } else {
            System.out.println("Nenhum arquivo XML encontrado. Iniciando sistema vazio.");
            this.usuarios = new ArrayList<>();
            this.editais = new ArrayList<>();
        }
    }
    
    public void salvarAlteracoes() {
        persistencia.salvar(this.usuarios, this.editais);
    }
    
    public boolean existeCoordenador() {
        if (usuarios == null) return false;
        for(Usuario u : usuarios) {
            if(u instanceof Coordenador) return true;
        }
        return false;
    }
    
    public List<Aluno> getAlunos() {
        List<Aluno> listaFiltrada = new ArrayList<>();
        
        if (this.usuarios != null) {
            for (Usuario u : this.usuarios) {
                if (u instanceof Aluno) {
                    listaFiltrada.add((Aluno) u);
                }
            }
        }
        return listaFiltrada;
    }
}