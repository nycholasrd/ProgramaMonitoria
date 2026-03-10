package ProgramaMonitoria;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class ServicoEmail {
    private static final String HOST = "smtp.gmail.com";
    private static final String PORTA = "587";
    private static final String USUARIO = "deverikyfarias@gmail.com"; 
    private static final String SENHA = "iwpthokucciikcpm"; 

    public static void enviarEmail(String destinatario, String assunto, String mensagemTexto) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORTA);
        props.put("mail.smtp.ssl.trust", HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USUARIO, SENHA);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USUARIO));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        message.setSubject(assunto);
        message.setText(mensagemTexto);

        Transport.send(message);
        System.out.println("E-mail enviado com sucesso para: " + destinatario);
    }
}