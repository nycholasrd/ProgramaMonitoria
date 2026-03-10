package ProgramaMonitoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class TelaAluno extends JPanel {
    private SistemaMonitoria sistema;
    private Aluno alunoLogado;
    private MainApp mainApp;
    private JTable tabelaEditais;
    private DefaultTableModel modeloEditais;
    private JTable tabelaInscricoes;
    private DefaultTableModel modeloInscricoes;

    public TelaAluno(SistemaMonitoria sistema, Aluno aluno, MainApp mainApp) {
        this.sistema = sistema;
        this.alunoLogado = aluno;
        this.mainApp = mainApp;

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(new JLabel("Olá, " + aluno.getNome()));
        JButton btnLogout = new JButton("Sair");
        btnLogout.addActionListener(e -> mainApp.fazerLogout());
        topPanel.add(btnLogout);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        String[] colunasEditais = {"ID", "Início", "Fim", "Disciplinas"};
        modeloEditais = new DefaultTableModel(colunasEditais, 0) {
            public boolean isCellEditable(int r, int c) { 
            	return false; 
            }
        };
        tabelaEditais = new JTable(modeloEditais);
        JScrollPane scrollEditais = new JScrollPane(tabelaEditais);
        tabbedPane.addTab("Editais Disponíveis", scrollEditais);

        String[] colunasInscricoes = {"Disciplina", "Edital ID", "Minha Nota", "Situação"};
        modeloInscricoes = new DefaultTableModel(colunasInscricoes, 0) {
            public boolean isCellEditable(int r, int c) { 
            	return false; 
            }
        };
        tabelaInscricoes = new JTable(modeloInscricoes);
        JScrollPane scrollInscricoes = new JScrollPane(tabelaInscricoes);
        tabbedPane.addTab("Minhas Inscrições", scrollInscricoes);

        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton btnInscrever = new JButton("Inscrever-se");
        JButton btnDesistir = new JButton("Desistir da Inscrição"); 
        JButton btnAcompanhar = new JButton("Ver Resultados");
        
        btnDesistir.setForeground(Color.RED);
        
        btnInscrever.addActionListener(e -> abrirInscricao()); 
        btnAcompanhar.addActionListener(e -> acompanharEdital());
        
        btnDesistir.addActionListener(e -> {
            Inscricao inscricao = getInscricaoSelecionada();
            
            if (inscricao == null) {
                JOptionPane.showMessageDialog(this, "Vá na aba 'Minhas Inscrições' e selecione uma linha.");
                return;
            }
            
            Edital edital = inscricao.getEdital();

            if (inscricao.isDesistente()) {
                JOptionPane.showMessageDialog(this, "Você já desistiu desta inscrição.");
                return;
            }

            if (edital != null && edital.isPeriodoDesistenciaAberto()) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Tem certeza que deseja desistir?\nEsta ação é irreversível e você irá para o final da fila.", 
                    "Confirmar Desistência", JOptionPane.YES_NO_OPTION);
                    
                if (confirm == JOptionPane.YES_OPTION) {
                    inscricao.setDesistente(true);
                    
                    sistema.salvarAlteracoes();
                    
                    atualizarMinhasInscricoes(); 
                    
                    JOptionPane.showMessageDialog(this, "Desistência registrada.");
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "O prazo para desistência (5 dias após o fim do edital) já expirou.");
            }
        });

        bottomPanel.add(btnInscrever);
        bottomPanel.add(btnDesistir);
        bottomPanel.add(btnAcompanhar);
        add(bottomPanel, BorderLayout.SOUTH);

        atualizarTabela();      
        atualizarMinhasInscricoes();   
    }

    private void atualizarTabela() {
        modeloEditais.setRowCount(0);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Edital e : sistema.getEditais()) {
            modeloEditais.addRow(new Object[]{
                e.getId(),
                e.getDataInicio().format(dtf),
                e.getDataFim().format(dtf),
                e.getDisciplinas().size() + " disciplinas ofertadas"
            });
        }
    }
    
    private void acompanharEdital() {
        int row = tabelaEditais.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um edital na tabela para acompanhar.");
            return;
        }

        Edital edital = sistema.getEditais().get(row);

        boolean temResultado = edital.getInscricoes().stream().anyMatch(i -> i.getPontuacaoFinal() > 0);
        
        if (!temResultado) {
            JOptionPane.showMessageDialog(this, "O resultado deste edital ainda não foi processado pelo Coordenador.");
            return;
        }

        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Resultado do Edital", true);
        d.setSize(700, 500);
        d.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        for (Disciplina disc : edital.getDisciplinas()) {
            
            List<Inscricao> inscritosDisciplina = new ArrayList<>();
            for (Inscricao i : edital.getInscricoes()) {
                if (i.getDisciplina().getNome().equals(disc.getNome())) {
                    inscritosDisciplina.add(i);
                }
            }

            Collections.sort(inscritosDisciplina);

            String[] colunas = {"Classificação", "Aluno", "Nota Final", "Situação", "Tipo Vaga"};
            DefaultTableModel modelRank = new DefaultTableModel(colunas, 0);

            int vagasRemuneradas = disc.getVagasRemuneradas();
            int vagasVoluntarias = disc.getVagasVoluntarias();
            int posicao = 1;

            for (Inscricao insc : inscritosDisciplina) {
                String situacao;
                String tipoVaga;

                if (insc.isDesistente()) {
                    situacao = "DESISTENTE";
                    tipoVaga = "-";
                } else if (posicao <= vagasRemuneradas) {
                    situacao = "SELECIONADO";
                    tipoVaga = "BOLSISTA (Remunerada)";
                } else if (posicao <= (vagasRemuneradas + vagasVoluntarias)) {
                    situacao = "SELECIONADO";
                    tipoVaga = "VOLUNTÁRIO";
                } else {
                    situacao = "LISTA DE ESPERA";
                    tipoVaga = "-";
                }

                modelRank.addRow(new Object[]{
                    posicao + "º",
                    insc.getAluno().getNome(),
                    String.format("%.2f", insc.getPontuacaoFinal()),
                    situacao,
                    tipoVaga
                });
                
                posicao++;
            }

            tabbedPane.addTab(disc.getNome(), new JScrollPane(new JTable(modelRank)));
        }

        d.add(tabbedPane, BorderLayout.CENTER);
        
        JLabel lblInfo = new JLabel("  * A classificação é baseada na nota final (CRE + Nota Disciplina).");
        d.add(lblInfo, BorderLayout.SOUTH);

        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void abrirInscricao() {
        int row = tabelaEditais.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um edital primeiro.");
            return;
        }
        Edital edital = sistema.getEditais().get(row);

        Disciplina[] opcoes = edital.getDisciplinas().toArray(new Disciplina[0]);
        
        JComboBox<Disciplina> comboDisc = new JComboBox<>(opcoes);
        comboDisc.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if(value instanceof Disciplina){
                    setText(((Disciplina)value).getNome());
                }
                return this;
            }
        });

        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField txtCre = new JTextField();
        JTextField txtNota = new JTextField();

        panel.add(new JLabel("Disciplina:")); panel.add(comboDisc);
        panel.add(new JLabel("Seu CRE Atual:")); panel.add(txtCre);
        panel.add(new JLabel("Sua Média na Disciplina:")); panel.add(txtNota);

        int result = JOptionPane.showConfirmDialog(this, panel, "Inscrição", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                Disciplina disciplinaSelecionada = (Disciplina) comboDisc.getSelectedItem();
                double cre = Double.parseDouble(txtCre.getText());
                double nota = Double.parseDouble(txtNota.getText());

                Inscricao novaInscricao = new Inscricao(alunoLogado, disciplinaSelecionada, cre, nota, edital);
                
                edital.adicionarInscricao(novaInscricao);
                alunoLogado.adicionarInscricao(novaInscricao); 
                
                sistema.salvarAlteracoes();
                
                atualizarMinhasInscricoes();
                
                JOptionPane.showMessageDialog(this, "Inscrição realizada com sucesso!");
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Digite apenas números válidos (use ponto para decimal).");
            } catch (MonitoriaException ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        }
    }
    
    private void atualizarMinhasInscricoes() {
        modeloInscricoes.setRowCount(0);
        
        List<Inscricao> lista = alunoLogado.getInscricoes();
        
        if (lista != null) {
            for (Inscricao i : lista) {
                
                String status = i.isDesistente() ? "DESISTENTE" : "Ativo";
                String infoEdital = (i.getEdital() != null) ? "Edital " + i.getEdital().getId() : "Edital ?";

                Object[] linha = {
                    i.getDisciplina().getNome(),
                    infoEdital,
                    i.getNotaDisciplina(),
                    status        
                };
                
                modeloInscricoes.addRow(linha);
            }
        }
    }
    
    private Inscricao getInscricaoSelecionada() {
        int row = tabelaInscricoes.getSelectedRow();
        
        if (row == -1) {
            return null;
        }
        
        return alunoLogado.getInscricoes().get(row);
    }
}