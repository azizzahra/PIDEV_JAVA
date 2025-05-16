package services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EmailVerificationService {
    private static EmailVerificationService instance;
    private final ConcurrentMap<String, String> codes = new ConcurrentHashMap<>();

    public static EmailVerificationService getInstance() {
        if (instance == null) {
            instance = new EmailVerificationService();
        }
        return instance;
    }

    // Generate and send verification code
    public void generateAndSendCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        codes.put(email, code);
        sendEmail(email, "Verification Code", "Your code is: " + code);
    }

    // Send any email
    public void sendEmail(String toEmail, String subject, String content) {
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
                props.put("mail.smtp.port", "2525");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");

                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("7f999efb1201b6", "96c45c4965482e");
                    }
                });

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress("noreply@carenova.com"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setText(content);
                Transport.send(message);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Verify code
    public boolean verifyCode(String email, String inputCode) {
        return codes.getOrDefault(email, "").equals(inputCode);
    }
}