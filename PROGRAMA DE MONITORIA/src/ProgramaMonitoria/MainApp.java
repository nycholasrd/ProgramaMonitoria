package ProgramaMonitoria;

import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {
    private SistemaMonitoria sistema;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    private static final String TELA_LOGIN = "LOGIN";
    private static final String TELA_COORD = "COORD";
    private static final String TELA_ALUNO = "ALUNO";

    public MainApp() {
        sistema = new SistemaMonitoria();
        
        setTitle("Sistema de Gestão de Monitoria");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(criarPainelLogin(), TELA_LOGIN);
        
        add(mainPanel);
        
        verificarPrimeiroAcesso();
    }

    private void verificarPrimeiroAcesso() {
        if (!sistema.existeCoordenador()) {
            JOptionPane.showMessageDialog(this, "Bem-vindo! Nenhum coordenador detectado.\nCrie a conta de Administrador.");
            cadastrarUsuario(true); 
        }
    }

    private JPanel criarPainelLogin() {
        JPanel panel = new JPanel(new GridBagLayout()); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtEmail = new JTextField(20);
        JPasswordField txtSenha = new JPasswordField(20);
        JButton btnLogin = new JButton("Entrar");
        JButton btnCadastro = new JButton("Cadastrar Aluno");

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("E-mail:"), gbc);
        gbc.gridx = 1; panel.add(txtEmail, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1; panel.add(txtSenha, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; 
        panel.add(btnLogin, gbc);
        
        gbc.gridy = 3; 
        panel.add(btnCadastro, gbc);

        btnLogin.addActionListener(e -> {
            try {
                Usuario u = sistema.fazerLogin(txtEmail.getText(), new String(txtSenha.getPassword()));
                navegarParaAreaUsuario(u);
            } catch (MonitoriaException ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        btnCadastro.addActionListener(e -> cadastrarUsuario(false)); 

        return panel;
    }

    private void cadastrarUsuario(boolean isCoordenador) {
        JDialog d = new JDialog(this, "Cadastro de " + (isCoordenador ? "Coordenador" : "Aluno"), true);
        d.setLayout(new GridLayout(0, 2));
        d.setSize(300, 250);
        d.setLocationRelativeTo(this);

        JTextField tNome = new JTextField();
        JTextField tEmail = new JTextField();
        JPasswordField tSenha = new JPasswordField();
        JTextField tMatricula = new JTextField();

        d.add(new JLabel("Nome:")); d.add(tNome);
        d.add(new JLabel("Email:")); d.add(tEmail);
        d.add(new JLabel("Senha:")); d.add(tSenha);
        
        if (!isCoordenador) {
            d.add(new JLabel("Matrícula:")); d.add(tMatricula);
        }

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(e -> {
            Usuario novo;
            if (isCoordenador) {
                novo = new Coordenador(tNome.getText(), tEmail.getText(), new String(tSenha.getPassword()));
            } else {
                novo = new Aluno(tNome.getText(), tEmail.getText(), new String(tSenha.getPassword()), tMatricula.getText());
            }
            sistema.cadastrarUsuario(novo);
            JOptionPane.showMessageDialog(d, "Cadastro realizado com sucesso!");
            d.dispose();
        });
        d.add(btnSalvar);
        d.setVisible(true);
    }

    private void navegarParaAreaUsuario(Usuario u) {
        if (u instanceof Coordenador) {
            mainPanel.add(new TelaCoordenador(sistema, (Coordenador) u, this), TELA_COORD);
            cardLayout.show(mainPanel, TELA_COORD);
        } else {
            mainPanel.add(new TelaAluno(sistema, (Aluno) u, this), TELA_ALUNO);
            cardLayout.show(mainPanel, TELA_ALUNO);
        }
    }

    public void fazerLogout() {
        cardLayout.show(mainPanel, TELA_LOGIN);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}