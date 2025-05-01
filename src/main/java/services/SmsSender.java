package services;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsSender {
    // Replace these with your actual Twilio credentials
    public static final String ACCOUNT_SID = "ACd914e6e7d81fd9cfbd3a72f9528a6816";
    public static final String AUTH_TOKEN = "07ea8c71f626e2bacb3131cf7608710a";
    public static final String TWILIO_NUMBER = "+17756407607"; // Your Twilio phone number

    public static void sendSms(String toNumber, String message) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message.creator(
                new PhoneNumber(toNumber),  // To number
                new PhoneNumber(TWILIO_NUMBER),  // From Twilio number
                message
        ).create();
    }
}