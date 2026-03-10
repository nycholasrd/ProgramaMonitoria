package ProgramaMonitoria;

import java.util.List;

public class SistemaDadosWrapper {
    private List<Usuario> usuarios;
    private List<Edital> editais;

    public SistemaDadosWrapper(List<Usuario> usuarios, List<Edital> editais) {
        this.usuarios = usuarios;
        this.editais = editais;
    }

    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Edital> getEditais() { return editais; }
}