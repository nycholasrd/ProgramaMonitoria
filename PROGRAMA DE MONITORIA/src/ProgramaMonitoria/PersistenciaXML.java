package ProgramaMonitoria;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import java.io.*;
import java.util.List;

public class PersistenciaXML {

    private XStream xstream;
    private final String ARQUIVO = "central.xml";

    public PersistenciaXML() {
        xstream = new XStream(new DomDriver());
        
        xstream.addPermission(AnyTypePermission.ANY); 
        
        xstream.alias("sistema", SistemaDadosWrapper.class); 
        xstream.alias("aluno", Aluno.class);
        xstream.alias("coordenador", Coordenador.class);
        xstream.alias("edital", Edital.class);
        xstream.alias("disciplina", Disciplina.class);
        xstream.alias("inscricao", Inscricao.class);
    }

    public void salvar(List<Usuario> usuarios, List<Edital> editais) {
        SistemaDadosWrapper dados = new SistemaDadosWrapper(usuarios, editais);
        
        try (PrintWriter writer = new PrintWriter(new File(ARQUIVO))) {
            String xml = xstream.toXML(dados);
            writer.write(xml);
            System.out.println("Dados salvos com sucesso em " + ARQUIVO);
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao salvar XML: " + e.getMessage());
        }
    }

    public SistemaDadosWrapper carregar() {
        File arquivo = new File(ARQUIVO);
        if (!arquivo.exists()) {
            return null; 
        }

        try (FileInputStream fis = new FileInputStream(arquivo)) {
            return (SistemaDadosWrapper) xstream.fromXML(fis);
        } catch (IOException e) {
            System.err.println("Erro ao ler XML: " + e.getMessage());
            return null;
        }
    }
}