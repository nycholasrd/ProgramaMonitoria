package ProgramaMonitoria;

import java.util.ArrayList;
import java.util.List;

public class Aluno extends Usuario {
    private String matricula;
    private List<Inscricao> inscricoes;
    
    public Aluno(String nome, String email, String senha, String matricula) {
        super(nome, email, senha);
        this.matricula = matricula;
        this.inscricoes = new ArrayList<>();
    }
    
    public List<Inscricao> getInscricoes() {
    	if (this.inscricoes == null) {
            this.inscricoes = new ArrayList<>();
        }
        return this.inscricoes;
    }

    public void adicionarInscricao(Inscricao inscricao) {
        this.inscricoes.add(inscricao);
    }
    
    public String getMatricula() { return matricula; }

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}

	public void setInscricoes(List<Inscricao> inscricoes) {
		this.inscricoes = inscricoes;
	}
    
    
}