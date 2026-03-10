package ProgramaMonitoria;

import java.io.Serializable;

public class Inscricao implements Serializable, Comparable<Inscricao> {
    
    private static final long serialVersionUID = 1L; 
    
    private Aluno aluno;
    private Disciplina disciplina;
    private double creAluno;
    private double notaDisciplina;
    private double pontuacaoFinal;
    private Edital edital; 
    private boolean desistente;
    private int insRestantes;

    public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

	public void setDisciplina(Disciplina disciplina) {
		this.disciplina = disciplina;
	}

	public void setCreAluno(double creAluno) {
		this.creAluno = creAluno;
	}

	public void setNotaDisciplina(double notaDisciplina) {
		this.notaDisciplina = notaDisciplina;
	}

	public void setPontuacaoFinal(double pontuacaoFinal) {
		this.pontuacaoFinal = pontuacaoFinal;
	}

	public Inscricao(Aluno aluno, Disciplina disciplina, double creAluno, double notaDisciplina, Edital edital) throws MonitoriaException {
        
        if (creAluno < 0 || creAluno > 10) {
            throw new MonitoriaException("Valor inválido para CRE! O CRE deve estar entre 0 e 10.");
        }
        
        if (notaDisciplina < 0 || notaDisciplina > 100) {
            throw new MonitoriaException("Valor inválido para Nota! A nota deve estar entre 0 e 100.");
        }
        
        this.aluno = aluno;
        this.disciplina = disciplina;
        this.creAluno = creAluno;
        this.notaDisciplina = notaDisciplina;
        this.edital = edital;
        this.desistente = false;
        this.pontuacaoFinal = 0.0;
    }
    
	int vagasAtuaisRestantes = Inscricao.getEdital().getVagasRestantes();

    public void calcularPontuacao(double pesoCre, double pesoNota) {
        this.pontuacaoFinal = (this.creAluno * pesoCre) + ((this.notaDisciplina*0.1) * pesoNota);
    }
    
    public boolean isDesistente() {
        return desistente;
    }

    public void setDesistente(boolean desistente) {
        this.desistente = desistente;
    }
    
    public void setEdital(Edital edital) {
        this.edital = edital;
    }
    
    public static Edital getEdital() {
		return null;
    }

    public int compareTo(Inscricao outra) {
    	if (this.isDesistente() && !outra.isDesistente()) return 1;
        if (!this.isDesistente() && outra.isDesistente()) return -1;
        return Double.compare(outra.pontuacaoFinal, this.pontuacaoFinal);
    }
    
    public double getPontuacaoFinal() { return pontuacaoFinal; }
    public Aluno getAluno() { return aluno; }
    public Disciplina getDisciplina() { return disciplina; }
    public double getCreAluno() { return creAluno; }
    public double getNotaDisciplina() { return notaDisciplina; }
}