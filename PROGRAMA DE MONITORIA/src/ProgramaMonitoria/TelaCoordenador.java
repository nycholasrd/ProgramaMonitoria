package ProgramaMonitoria;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TelaCoordenador extends JPanel {
    private SistemaMonitoria sistema;
    private Coordenador coordenadorLogado;
    private MainApp mainApp;
    private JTable tabelaEditais;
    private DefaultTableModel modeloTabela;

    public TelaCoordenador(SistemaMonitoria sistema, Coordenador coord, MainApp mainApp) {
        this.sistema = sistema;
        this.coordenadorLogado = coord;
        this.mainApp = mainApp;

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(new JLabel("Olá, Coordenador " + coord.getNome()));
        JButton btnLogout = new JButton("Sair");
        btnLogout.addActionListener(e -> mainApp.fazerLogout());
        topPanel.add(btnLogout);
        add(topPanel, BorderLayout.NORTH);

        String[] colunas = {"ID", "Início", "Fim", "Disciplinas", "Status"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaEditais = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabelaEditais);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout()); 
        
        JButton btnNovo = new JButton("Novo");
        JButton btnEditar = new JButton("Editar");
        JButton btnClonar = new JButton("Clonar");
        JButton btnEncerrar = new JButton("Encerrar");
        JButton btnDetalhes = new JButton("Detalhes/Resultados");
        JButton btnAlunos = new JButton("Gerenciar Alunos"); 
        btnAlunos.addActionListener(e -> abrirGerenciamentoAlunos());
        
        btnEncerrar.setForeground(Color.RED);
        btnClonar.setForeground(Color.BLUE);

        btnNovo.addActionListener(e -> abrirFormularioEdital(null, false));
        
        btnEditar.addActionListener(e -> {
            Edital selecionado = getEditalSelecionado();
            if (selecionado != null) abrirFormularioEdital(selecionado, false);
        });

        btnClonar.addActionListener(e -> {
            Edital selecionado = getEditalSelecionado();
            if (selecionado != null) abrirFormularioEdital(selecionado, true);
        });

        btnEncerrar.addActionListener(e -> encerrarEdital());
        btnDetalhes.addActionListener(e -> verDetalhesEdital());
        
        bottomPanel.add(btnNovo);
        bottomPanel.add(btnEditar);
        bottomPanel.add(btnClonar);
        bottomPanel.add(btnEncerrar);
        bottomPanel.add(btnDetalhes);
        bottomPanel.add(btnAlunos);
        
        add(bottomPanel, BorderLayout.SOUTH);

        atualizarTabela();
    }

    private Edital getEditalSelecionado() {
        int row = tabelaEditais.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um edital na tabela.");
            return null;
        }
        return sistema.getEditais().get(row);
    }

    private void atualizarTabela() {
        modeloTabela.setRowCount(0); 
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate hoje = LocalDate.now();

        for (Edital e : sistema.getEditais()) {
            String status = "Aberto";
            if (e.getDataFim().isBefore(hoje)) {
                status = "Encerrado";
            } else if (e.getDataInicio().isAfter(hoje)) {
                status = "Futuro";
            }

            Object[] linha = {
                e.getId(),
                e.getDataInicio().format(dtf),
                e.getDataFim().format(dtf),
                e.getDisciplinas().size(), 
                status 
            };
            modeloTabela.addRow(linha);
        }
    }

    private void encerrarEdital() {
        Edital edital = getEditalSelecionado();
        if (edital == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Tem certeza que deseja encerrar este edital imediatamente?\n" +
            "A data de fim será alterada para ontem.", 
            "Encerrar Edital", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            edital.setDataFim(LocalDate.now().minusDays(1));
            sistema.salvarAlteracoes(); 
            atualizarTabela();
            JOptionPane.showMessageDialog(this, "Edital encerrado.");
        }
    }

    private void abrirFormularioEdital(Edital editalBase, boolean isClonagem) {
        String tituloDialogo = (editalBase == null) ? "Novo Edital" : (isClonagem ? "Clonar Edital" : "Editar Edital");
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), tituloDialogo, true);
        d.setLayout(new BorderLayout());
        d.setSize(500, 500);

        JPanel formPanel = new JPanel(new GridLayout(5, 2));
        JTextField txtInicio = new JTextField();
        JTextField txtFim = new JTextField();
        JTextField txtPesoCre = new JTextField();
        JTextField txtPesoNota = new JTextField();
        JTextField txtLimiteInscricoes = new JTextField();
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<Disciplina> disciplinasTemp = new ArrayList<>();
        DefaultListModel<String> listModel = new DefaultListModel<>();

        if (editalBase != null) {
            txtInicio.setText(editalBase.getDataInicio().format(dtf));
            txtFim.setText(editalBase.getDataFim().format(dtf));
            txtPesoCre.setText(String.valueOf(editalBase.getPesoCre()));
            txtPesoNota.setText(String.valueOf(editalBase.getPesoNota()));
            txtLimiteInscricoes.setText(String.valueOf(editalBase.getLimiteInscricoes()));

            for (Disciplina disc : editalBase.getDisciplinas()) {
                if (isClonagem) {
                    Disciplina cloneDisc = new Disciplina(disc.getNome(), disc.getVagasRemuneradas(), disc.getVagasVoluntarias());
                    disciplinasTemp.add(cloneDisc);
                } else {
                    disciplinasTemp.add(disc); 
                }
                listModel.addElement(disc.getNome() + " (R:" + disc.getVagasRemuneradas() + ", V:" + disc.getVagasVoluntarias() + ")");
            }
        } else {
            txtInicio.setText("01/02/2025");
            txtFim.setText("20/02/2025");
            txtPesoCre.setText("0.7");
            txtPesoNota.setText("0.3");
            txtLimiteInscricoes.setText("0");
        }

        formPanel.add(new JLabel("Início (dd/MM/yyyy):")); formPanel.add(txtInicio);
        formPanel.add(new JLabel("Fim (dd/MM/yyyy):")); formPanel.add(txtFim);
        formPanel.add(new JLabel("Peso CRE (0.0 - 1.0):")); formPanel.add(txtPesoCre);
        formPanel.add(new JLabel("Peso Nota (0.0 - 1.0):")); formPanel.add(txtPesoNota);
        formPanel.add(new JLabel("Limite de inscritos (0 - 1000)")); formPanel.add(txtLimiteInscricoes);

        JList<String> listDisciplinas = new JList<>(listModel);
        JPanel discPanel = new JPanel(new BorderLayout());
        discPanel.setBorder(BorderFactory.createTitledBorder("Disciplinas"));
        discPanel.add(new JScrollPane(listDisciplinas), BorderLayout.CENTER);
        
        JPanel panelBotoesDisc = new JPanel(new GridLayout(1, 2));
        JButton btnAddDisc = new JButton("Adicionar");
        JButton btnRemDisc = new JButton("Remover");

        btnAddDisc.addActionListener(ev -> {
            JPanel p = new JPanel(new GridLayout(3, 2));
            JTextField tNome = new JTextField();
            JTextField tVagasR = new JTextField("1");
            JTextField tVagasV = new JTextField("0");
            p.add(new JLabel("Nome:")); p.add(tNome);
            p.add(new JLabel("Vagas Remuneradas:")); p.add(tVagasR);
            p.add(new JLabel("Vagas Voluntárias:")); p.add(tVagasV);
            
            int result = JOptionPane.showConfirmDialog(d, p, "Nova Disciplina", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    Disciplina disc = new Disciplina(tNome.getText(), 
                            Integer.parseInt(tVagasR.getText()), 
                            Integer.parseInt(tVagasV.getText()));
                    disciplinasTemp.add(disc);
                    listModel.addElement(disc.getNome() + " (R:" + disc.getVagasRemuneradas() + ", V:" + disc.getVagasVoluntarias() + ")");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(d, "Erro nos números.");
                }
            }
        });

        btnRemDisc.addActionListener(ev -> {
            int selected = listDisciplinas.getSelectedIndex();
            if (selected != -1) {
                disciplinasTemp.remove(selected);
                listModel.remove(selected);
            }
        });

        panelBotoesDisc.add(btnAddDisc);
        panelBotoesDisc.add(btnRemDisc);
        discPanel.add(panelBotoesDisc, BorderLayout.SOUTH);

        JButton btnSalvar = new JButton(isClonagem ? "Confirmar Clonagem" : (editalBase == null ? "Publicar" : "Salvar Alterações"));
        btnSalvar.addActionListener(ev -> {
            try {
                LocalDate inicio = LocalDate.parse(txtInicio.getText(), dtf);
                LocalDate fim = LocalDate.parse(txtFim.getText(), dtf);
                double pCre = Double.parseDouble(txtPesoCre.getText());
                double pNota = Double.parseDouble(txtPesoNota.getText());
                int limiteInscricoes = Integer.parseInt(txtLimiteInscricoes.getText());
                
                if (editalBase != null && !isClonagem) {
                    editalBase.setDataInicio(inicio);
                    editalBase.setDataFim(fim);
                    editalBase.setPesoCre(pCre);
                    editalBase.setPesoNota(pNota);
                    editalBase.setLimiteInscricoes(limiteInscricoes);                    
                    editalBase.getDisciplinas().clear();
                    for (Disciplina dTemp : disciplinasTemp) {
                        editalBase.adicionarDisciplina(dTemp);
                    }
                    JOptionPane.showMessageDialog(d, "Edital atualizado!");

                } else {
                    Edital novoEdital = new Edital(inicio, fim, pCre, pNota,limiteInscricoes);
                    for (Disciplina disc : disciplinasTemp) {
                        novoEdital.adicionarDisciplina(disc);
                    }
                    sistema.cadastrarEdital(novoEdital);
                    JOptionPane.showMessageDialog(d, isClonagem ? "Edital clonado com sucesso!" : "Novo edital cadastrado!");
                }

                sistema.salvarAlteracoes(); 
                atualizarTabela();
                d.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Erro ao salvar: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        d.add(formPanel, BorderLayout.NORTH);
        d.add(discPanel, BorderLayout.CENTER);
        d.add(btnSalvar, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void verDetalhesEdital() {
        Edital edital = getEditalSelecionado();
        if (edital == null) return;
        
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Gerenciar Inscritos", true);
        d.setSize(700, 500); 
        d.setLayout(new BorderLayout());

        String[] colunas = {"Aluno", "Email", "Disciplina", "Pontuação", "Status"};
        DefaultTableModel modelInscritos = new DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        List<Inscricao> listaOrdenada = new ArrayList<>(edital.getInscricoes());
        
        listaOrdenada.sort(Comparator.comparing(Inscricao::isDesistente)
                .thenComparing(i -> i.getAluno().getNome()));

        for (Inscricao i : listaOrdenada) {
            String statusStr = i.isDesistente() ? "DESISTENTE" : "Ativo";
            
            Object[] linha = {
                i.getAluno().getNome(),
                i.getAluno().getEmail(),
                i.getDisciplina().getNome(),
                String.format("%.2f", i.getPontuacaoFinal()),
                statusStr
            };
            modelInscritos.addRow(linha);
        }
        
        JTable tableInscritos = new JTable(modelInscritos);
        
        tableInscritos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String status = (String) table.getModel().getValueAt(row, 4); // Coluna 4 é Status
                
                if ("DESISTENTE".equals(status)) {
                    c.setForeground(Color.RED);
                    c.setFont(c.getFont().deriveFont(Font.ITALIC));
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        d.add(new JScrollPane(tableInscritos), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnCalcular = new JButton("Calcular Resultados");
        btnCalcular.addActionListener(e -> {
            try {
                edital.processarResultados();
                
                sistema.salvarAlteracoes();
                
                JOptionPane.showMessageDialog(d, "Resultados calculados com sucesso!");
                d.dispose(); 
                verDetalhesEdital(); 
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(d, "Erro ao calcular: " + ex.getMessage());
            }
        });
        JButton btnEmail = new JButton("Contatar Aluno");
	    btnEmail.addActionListener(e -> {
	          int selectedRow = tableInscritos.getSelectedRow();
	          if (selectedRow == -1) {
	              JOptionPane.showMessageDialog(d, "Selecione um aluno na lista para enviar e-mail.");
	              return;
	          }
	
	          String emailAluno = (String) modelInscritos.getValueAt(selectedRow, 1);
	          String nomeAluno = (String) modelInscritos.getValueAt(selectedRow, 0);
	
	          String mensagem = JOptionPane.showInputDialog(d, 
	                  "Escreva a mensagem para " + nomeAluno + ":", 
	                  "Enviar E-mail", JOptionPane.PLAIN_MESSAGE);
	
	          if (mensagem != null && !mensagem.trim().isEmpty()) {
	              try {
	                  ServicoEmail.enviarEmail(emailAluno, "Comunicado Monitoria", mensagem);
	                  JOptionPane.showMessageDialog(d, "E-mail enviado com sucesso!");
	              } catch (Exception ex) {
	                  ex.printStackTrace(); 
	                  JOptionPane.showMessageDialog(d, "Erro ao enviar: " + ex.getMessage() + 
	                          "\nVerifique se configurou a Senha de App corretamente.");
	              }
	          }
	    });
	    JButton btnPdf = new JButton("Gerar PDF");
	    btnPdf.setBackground(new Color(220, 53, 69)); 
	    btnPdf.setForeground(Color.BLACK);
	    
	    btnPdf.addActionListener(e -> {
	        JFileChooser fileChooser = new JFileChooser();
	        fileChooser.setDialogTitle("Salvar Relatório PDF");
	        fileChooser.setSelectedFile(new java.io.File("Relatorio_Edital_" + edital.getId() + ".pdf"));

	        int userSelection = fileChooser.showSaveDialog(d);

	        if (userSelection == JFileChooser.APPROVE_OPTION) {
	            java.io.File arquivoParaSalvar = fileChooser.getSelectedFile();
	            
	            if (!arquivoParaSalvar.getAbsolutePath().endsWith(".pdf")) {
	                arquivoParaSalvar = new java.io.File(arquivoParaSalvar.getAbsolutePath() + ".pdf");
	            }

	            GeradorRelatorio.gerarRelatorioEdital(edital, arquivoParaSalvar);
	            
	            JOptionPane.showMessageDialog(d, "PDF gerado com sucesso em:\n" + arquivoParaSalvar.getAbsolutePath());
	            
	            try {
	                Desktop.getDesktop().open(arquivoParaSalvar);
	            } catch (Exception ex) {
	               
	            }
	        }
	    });


        btnPanel.add(btnCalcular);
        btnPanel.add(btnPdf);
        btnPanel.add(btnEmail);
        
        d.add(btnPanel, BorderLayout.SOUTH);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
    
    private void abrirGerenciamentoAlunos() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Gerenciar Alunos", true);
        d.setSize(600, 400);
        d.setLayout(new BorderLayout());

        String[] colunas = {"Matrícula", "Nome", "Email"};
        DefaultTableModel modelAlunos = new DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int r, int c) { 
            	return false; 
            }
        };
        
        modelAlunos.setRowCount(0);

        List<Aluno> listaCentral = sistema.getAlunos();

        if (listaCentral == null) {
            System.out.println("ERRO CRÍTICO: A lista de alunos retornou NULL.");
        } else {
            System.out.println("DEBUG: Buscando dados... Encontrados " + listaCentral.size() + " alunos no sistema.");
        }

        if (listaCentral != null) {
            for (Aluno a : listaCentral) {
                modelAlunos.addRow(new Object[]{ 
                    a.getMatricula(), 
                    a.getNome(), 
                    a.getEmail() 
                });
            }
        }
        
        JTable tableAlunos = new JTable(modelAlunos);
        d.add(new JScrollPane(tableAlunos), BorderLayout.CENTER);

        JPanel panelBotoes = new JPanel();
        JButton btnDetalhes = new JButton("Ver Detalhes");
        JButton btnEditar = new JButton("Editar Cadastro");
        
        btnDetalhes.addActionListener(e -> {
            int row = tableAlunos.getSelectedRow();
            if (row != -1) {
                Aluno alunoSelecionado = listaCentral.get(row);
                verDetalhesAluno(alunoSelecionado);
            } else {
                JOptionPane.showMessageDialog(d, "Selecione um aluno na lista.");
            }
        });

        btnEditar.addActionListener(e -> {
            int row = tableAlunos.getSelectedRow();
            if (row != -1) {
                Aluno alunoSelecionado = listaCentral.get(row);
                editarAluno(alunoSelecionado, modelAlunos, row);
            } else {
                JOptionPane.showMessageDialog(d, "Selecione um aluno para editar.");
            }
        });

        panelBotoes.add(btnDetalhes);
        panelBotoes.add(btnEditar);
        d.add(panelBotoes, BorderLayout.SOUTH);

        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void verDetalhesAluno(Aluno aluno) {
        StringBuilder info = new StringBuilder();
        info.append("Nome: ").append(aluno.getNome()).append("\n");
        info.append("Matrícula: ").append(aluno.getMatricula()).append("\n");
        info.append("Email: ").append(aluno.getEmail()).append("\n\n");
        
        info.append("--- Histórico de Inscrições ---\n");
        if (aluno.getInscricoes() == null || aluno.getInscricoes().isEmpty()) {
            info.append("Nenhuma inscrição realizada.");
        } else {
            for (Inscricao i : aluno.getInscricoes()) {
                String status = i.isDesistente() ? "[DESISTENTE]" : "[ATIVO]";
                info.append("- ").append(i.getDisciplina().getNome())
                    .append(" (Nota: ").append(i.getNotaDisciplina()).append(") ")
                    .append(status).append("\n");
            }
        }

        JOptionPane.showMessageDialog(this, new JTextArea(info.toString()), 
                "Detalhes do Aluno", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editarAluno(Aluno aluno, DefaultTableModel model, int row) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Editar Aluno", true);
        d.setSize(400, 300);
        d.setLayout(new GridLayout(5, 2));

        JTextField txtNome = new JTextField(aluno.getNome());
        JTextField txtEmail = new JTextField(aluno.getEmail());
        JTextField txtMatricula = new JTextField(aluno.getMatricula());
        
        d.add(new JLabel("Nome:")); d.add(txtNome);
        d.add(new JLabel("Matrícula:")); d.add(txtMatricula);
        d.add(new JLabel("Email:")); d.add(txtEmail);
        
        JButton btnSalvar = new JButton("Salvar Alterações");
        btnSalvar.addActionListener(ev -> {
            if (txtNome.getText().isEmpty() || txtMatricula.getText().isEmpty()) {
                JOptionPane.showMessageDialog(d, "Nome e Matrícula são obrigatórios.");
                return;
            }

            aluno.setNome(txtNome.getText());
            aluno.setMatricula(txtMatricula.getText());
            aluno.setEmail(txtEmail.getText());
           
            sistema.salvarAlteracoes(); 

            model.setValueAt(aluno.getMatricula(), row, 0);
            model.setValueAt(aluno.getNome(), row, 1);
            model.setValueAt(aluno.getEmail(), row, 2);

            JOptionPane.showMessageDialog(d, "Aluno atualizado com sucesso!");
            d.dispose();
        });

        d.add(new JLabel("")); 
        d.add(btnSalvar);

        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
}