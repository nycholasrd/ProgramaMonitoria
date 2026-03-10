package ProgramaMonitoria;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Disciplina implements Serializable {
    private String nome;
    private int vagasRemuneradas;
    private int vagasVoluntarias;

    public Disciplina(String nome, int vagasRemuneradas, int vagasVoluntarias) {
        this.nome = nome;
        this.vagasRemuneradas = vagasRemuneradas;
        this.vagasVoluntarias = vagasVoluntarias;
    }
    
    public String getNome() { return nome; }
    public int getVagasRemuneradas() { return vagasRemuneradas; }
    public int getVagasVoluntarias() { return vagasVoluntarias; }
}