package ProgramaMonitoria;

import java.time.LocalDate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Representa um Edital de Monitoria dentro do sistema.
 * <p>
 * Esta classe é responsável por gerenciar as datas de vigência do processo seletivo,
 * as disciplinas ofertadas, a lista de inscrições e, principalmente, a lógica
 * de cálculo do resultado final (Ranking) e verificação de prazos.
 * </p>
 * Implementa {@link Serializable} para permitir a persistência de dados.
 *
 * @author Eriky Farias, Endrew Daniel, Nycholas Richards
 * @version 1.0
 */
public class Edital implements Serializable {
    private static final long serialVersionUID = 1L; // Recomendado para Serializable

    private long id;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private double pesoCre;
    private double pesoNota;
    private int limiteInscricoes;
    private int inscricoesAtivas = 0;
    private List<Disciplina> disciplinas;
    private List<Inscricao> inscricoes;
    private boolean encerrado;

    /**
     * Construtor principal para criação de um novo Edital.
     * <p>
     * Valida se a soma dos pesos (CRE e Nota) é igual a 1.0, conforme regra de negócio.
     * Gera automaticamente um ID único baseado no tempo do sistema.
     * Inicializa as listas de disciplinas e inscrições vazias.
     * </p>
     *
     * @param dataInicio Data de abertura das inscrições.
     * @param dataFim    Data de encerramento das inscrições.
     * @param pesoCre    Peso atribuído ao CRE do aluno (entre 0.0 e 1.0).
     * @param pesoNota   Peso atribuído à nota do aluno na disciplina (entre 0.0 e 1.0).
     * @param limiteInscricoesS 
     * @throws MonitoriaException Caso a soma dos pesos não seja exatamente 1.0.
     */
    public Edital(LocalDate dataInicio, LocalDate dataFim, double pesoCre, double pesoNota, int limiteInscricoesS) throws MonitoriaException {
        if (Math.abs((pesoCre + pesoNota) - 1.0) > 0.0001) { // Comparação segura para double
            throw new MonitoriaException("A soma dos pesos deve ser igual a 1.0");
        }
        this.id = System.currentTimeMillis();
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.pesoCre = pesoCre;
        this.pesoNota = pesoNota;
        this.disciplinas = new ArrayList<>();
        this.inscricoes = new ArrayList<>();
        this.encerrado = false;
    }

    /**
     * Adiciona uma disciplina à lista de ofertas deste edital.
     *
     * @param d O objeto {@link Disciplina} contendo nome e quantidade de vagas.
     */
    public void adicionarDisciplina(Disciplina d) {
        this.disciplinas.add(d);
    }

    /**
     * Realiza a inscrição de um aluno em uma disciplina do edital.
     * <p>
     * Este método verifica se a data atual ({@code LocalDate.now()}) está dentro do prazo de inscrição
     * (entre {@code dataInicio} e {@code dataFim}, inclusive).
     * </p>
     *
     * @param i O objeto {@link Inscricao} contendo os dados do aluno e notas.
     * @throws MonitoriaException Caso a inscrição seja tentada fora do prazo estabelecido.
     */
    public void adicionarInscricao(Inscricao i) throws MonitoriaException {
        LocalDate hoje = LocalDate.now();
        if (hoje.isBefore(dataInicio) || hoje.isAfter(dataFim)) {
            throw new MonitoriaException("Fora do prazo de inscrição.");
        }
        this.inscricoes.add(i);
    }

    /**
     * Processa o resultado final do edital para todos os inscritos.
     * <p>
     * O método executa as seguintes ações:
     * <ol>
     * <li>Verifica se há inscrições; se não houver, retorna imediatamente.</li>
     * <li>Percorre a lista de inscritos.</li>
     * <li>Se o aluno for desistente, sua nota é zerada.</li>
     * <li>Se o aluno estiver ativo, calcula a pontuação final baseada nos pesos.</li>
     * <li>Ordena a lista de inscrições de forma decrescente (maior nota primeiro),
     * movendo desistentes para o final.</li>
     * </ol>
     * </p>
     */
    public void processarResultados() {
        if (inscricoes == null || inscricoes.isEmpty()) {
            return;
        }
        
        for (Inscricao i : inscricoes) {
            if (!i.isDesistente()) {
                i.calcularPontuacao(this.pesoCre, this.pesoNota);
            } else {
                // Define nota como zero para garantir que fique no fim do ranking
                i.setPontuacaoFinal(0.0);
            }
        }
        // A ordenação depende da implementação de compareTo na classe Inscricao
        Collections.sort(inscricoes); 
    }
    
    /**
     * Verifica se o período de desistência ainda está aberto.
     * <p>
     * A regra de negócio define que um aluno pode desistir durante o período de inscrição
     * e até 5 dias após a data de encerramento do edital.
     * </p>
     *
     * @return {@code true} se a data atual for anterior ou igual a (dataFim + 5 dias),
     * {@code false} caso contrário.
     */
    public boolean isPeriodoDesistenciaAberto() {
        LocalDate hoje = LocalDate.now();
        LocalDate dataLimite = this.getDataFim().plusDays(5);
        
        return !hoje.isAfter(dataLimite);
    }

    /**
     * Retorna a lista de disciplinas ofertadas neste edital.
     * * @return Lista de objetos {@link Disciplina}.
     */
    public List<Disciplina> getDisciplinas() { return disciplinas; }
    
    /**
     * Retorna a lista completa de inscrições realizadas.
     * * @return Lista de objetos {@link Inscricao}.
     */
    public List<Inscricao> getInscricoes() { return inscricoes; }
    
    /**
     * Obtém o identificador único do edital.
     * * @return ID gerado via {@code System.currentTimeMillis()}.
     */
    public long getId() { return id; }
    
    /**
     * Obtém a data de início das inscrições.
     * * @return {@link LocalDate} do início.
     */
    public LocalDate getDataInicio() {
        return dataInicio;
    }

    /**
     * Obtém a data limite para inscrições.
     * * @return {@link LocalDate} do fim.
     */
    public LocalDate getDataFim() {
        return dataFim;
    }

    /**
     * Verifica se o edital já foi encerrado administrativamente ou manualmente pelo coordenador.
     * * @return {@code true} se estiver encerrado, {@code false} caso contrário.
     */
    public boolean isEncerrado() {
        return encerrado;
    }

    /**
     * Obtém o peso atribuído ao Coeficiente de Rendimento Escolar (CRE).
     * * @return Valor do peso CRE (0.0 a 1.0).
     */
    public double getPesoCre() {
        return pesoCre;
    }

    /**
     * Obtém o peso atribuído à nota da disciplina.
     * * @return Valor do peso Nota (0.0 a 1.0).
     */
    public double getPesoNota() {
        return pesoNota;
    }
    
    public int getLimiteInscricoes() {
    	return limiteInscricoes;
    }
    
    public int getInscricoesAtivas() {
        return inscricoesAtivas;
    }

    public void incrementarInscricoesAtivas() {
        this.inscricoesAtivas++;
    }

    public void decrementarInscricoesAtivas() {
        if (this.inscricoesAtivas > 0) {
            this.inscricoesAtivas--;
        }
    }

    public boolean podeInscrever() {
        return limiteInscricoes == 0 || this.inscricoesAtivas < this.limiteInscricoes;
    }

    /**
     * Define manualmente o ID do edital (usado principalmente em clonagem ou persistência).
     * * @param id O novo identificador.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Atualiza a data de início das inscrições.
     * * @param dataInicio Nova data de início.
     */
    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    /**
     * Atualiza a data de fim das inscrições.
     * * @param dataFim Nova data de fim.
     */
    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    /**
     * Define o peso do CRE para o cálculo da nota final.
     * * @param pesoCre Novo peso do CRE.
     */
    public void setPesoCre(double pesoCre) {
        this.pesoCre = pesoCre;
    }

    /**
     * Define o peso da nota da disciplina para o cálculo da nota final.
     * * @param pesoNota Novo peso da nota.
     */
    public void setPesoNota(double pesoNota) {
        this.pesoNota = pesoNota;
    }
    /**
     * Define o limite de inscritos para o edital. 
     * @param limiteInscricoes
     */


    /**
     * Define a lista de disciplinas do edital.
     * * @param disciplinas Lista de novas disciplinas.
     */
    public void setDisciplinas(List<Disciplina> disciplinas) {
        this.disciplinas = disciplinas;
    }

    /**
     * Define a lista de inscrições do edital.
     * * @param inscricoes Lista de inscrições.
     */
    public void setInscricoes(List<Inscricao> inscricoes) {
        this.inscricoes = inscricoes;
    }

    /**
     * Define o status de encerramento do edital.
     * * @param encerrado {@code true} para encerrar o edital, {@code false} para reabrir.
     */
    public void setEncerrado(boolean encerrado) {
        this.encerrado = encerrado;
    }
    /**
     * Define o limite de inscritos para o edital. 
     * @param limiteInscricoes
     */
	public void setLimiteInscricoes(int limiteInscricoes) {
		this.limiteInscricoes = limiteInscricoes;
		
	}



}