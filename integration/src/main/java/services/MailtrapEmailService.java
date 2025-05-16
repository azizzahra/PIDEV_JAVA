package services;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;

import java.util.List;

public class MailtrapEmailService {
    private final MailtrapClient client;

    public MailtrapEmailService() {
        MailtrapConfig config = new MailtrapConfig.Builder()
                .sandbox(true)
                .inboxId(3483795L) // your Mailtrap inbox ID
                .token("ee0e3680324788cde0d2759241bf7032") // your Mailtrap token
                .build();

        this.client = MailtrapClientFactory.createMailtrapClient(config);
    }

    public void sendEmail(String toEmail, String subject, String content) {
        MailtrapMail mail = MailtrapMail.builder()
                .from(new Address("hello@example.com", "Carenova Formation"))
                .to(List.of(new Address(toEmail)))
                .subject(subject)
                .text(content)
                .category("Formation Join")
                .build();

        try {
            client.send(mail);
            System.out.println("Email sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
