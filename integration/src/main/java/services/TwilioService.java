package services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import model.user;

public class TwilioService {
    // Vos identifiants Twilio (à remplacer par vos vraies valeurs)

    private static final String TWILIO_PHONE_NUMBER = "+1 903 476 6411";

    // Initialisation de l'API Twilio
    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    /**
     * Envoie un SMS à un utilisateur
     * @param recipient L'utilisateur destinataire
     * @param messageBody Le contenu du message
     * @return true si l'envoi a réussi, false sinon
     */
    public boolean sendSms(user recipient, String messageBody) {
        // Vérifier si l'utilisateur et son numéro de téléphone sont valides
        if (recipient == null || recipient.getNum_tel() == null || recipient.getNum_tel().isEmpty()) {
            System.out.println("Impossible d'envoyer le SMS: numéro de téléphone invalide");
            return false;
        }

        try {
            // Récupérer le numéro de téléphone du destinataire
            String destinationPhoneNumber = recipient.getNum_tel();
            System.out.println("Préparation à envoyer un SMS au numéro: " + destinationPhoneNumber);

            // Formatage du numéro de téléphone (ajout du préfixe international si nécessaire)
            String formattedPhoneNumber = formatPhoneNumber(destinationPhoneNumber);

            // Envoi du message
            Message message = Message.creator(
                            new PhoneNumber(formattedPhoneNumber),  // Numéro du destinataire (formaté)
                            new PhoneNumber(TWILIO_PHONE_NUMBER),   // Votre numéro Twilio
                            messageBody)                            // Contenu du message
                    .create();

            System.out.println("SMS envoyé avec succès à " + recipient.getNom() + " " + recipient.getPrenom() +
                    " (numéro: " + formattedPhoneNumber + ", SID: " + message.getSid() + ")");
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du SMS: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Formate un numéro de téléphone pour s'assurer qu'il contient l'indicatif international
     * @param phoneNumber Le numéro à formater
     * @return Le numéro formaté
     */
    // Modification suggérée pour la méthode formatPhoneNumber()
    private String formatPhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.replaceAll("[^\\d+]", "");

        if (!phoneNumber.startsWith("+")) {
            if (phoneNumber.startsWith("216")) { // Cas Tunisie
                return "+" + phoneNumber;
            }
            if (phoneNumber.startsWith("0")) {
                return "+216" + phoneNumber.substring(1); // Remplace 0 par +216
            }
            return "+216" + phoneNumber; // Fallback pour numéros sans indicatif
        }
        return phoneNumber;
    }

    /**
     * Envoie une notification pour informer qu'un utilisateur a répondu à un commentaire
     * @param commentOwner Propriétaire du commentaire original
     * @param responder Utilisateur qui a répondu
     * @param postTitle Titre du post
     * @return true si l'envoi a réussi, false sinon
     */
    public boolean sendCommentReplyNotification(user commentOwner, user responder, String postTitle) {
        // ...

        String rawMessage = responder.getNom() + " a repondu a votre commentaire  \""
                + "\". Voir l'appli.";

        // Supprimer les accents
        String sanitizedMessage = rawMessage
                .replace("é", "e")
                .replace("è", "e")
                .replace("à", "a");

        // Vérifier la longueur
        if (sanitizedMessage.length() > 160) {
            System.err.println("Message trop long: " + sanitizedMessage.length());
            return false;
        }

        return sendSms(commentOwner, sanitizedMessage);
    }
}