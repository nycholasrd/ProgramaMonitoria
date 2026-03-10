package ProgramaMonitoria;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;

public class GeradorRelatorio {

    public static void gerarRelatorioEdital(Edital edital, File arquivoDestino) {
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(arquivoDestino));
            document.open();

            Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("Relatório de Resultado - Monitoria", fonteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            
            document.add(new Paragraph(" ")); 

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            document.add(new Paragraph("Edital ID: " + edital.getId()));
            document.add(new Paragraph("Período: " + edital.getDataInicio().format(dtf) + " a " + edital.getDataFim().format(dtf)));
            document.add(new Paragraph("Total de Inscritos: " + edital.getInscricoes().size()));
            
            document.add(new Paragraph(" ")); 

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100); 
            criarCelulaCabecalho(table, "Aluno");
            criarCelulaCabecalho(table, "Disciplina");
            criarCelulaCabecalho(table, "Nota Final");
            criarCelulaCabecalho(table, "Situação");

            edital.getInscricoes().sort((i1, i2) -> Double.compare(i2.getPontuacaoFinal(), i1.getPontuacaoFinal()));

            for (Inscricao i : edital.getInscricoes()) {
                table.addCell(i.getAluno().getNome());
                table.addCell(i.getDisciplina().getNome());
                table.addCell(String.format("%.2f", i.getPontuacaoFinal()));
                
                String status = i.isDesistente() ? "DESISTENTE" : "CLASSIFICADO";
                table.addCell(status);
            }

            document.add(table);
            
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Documento gerado automaticamente pelo Sistema de Monitoria.");
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.getFont().setSize(10);
            document.add(footer);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao gerar PDF: " + e.getMessage());
        } finally {
            document.close();
        }
    }

    private static void criarCelulaCabecalho(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }
}