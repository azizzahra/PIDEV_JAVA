package services.marketPlace;

import model.user;
import model.order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import services.UserService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StripePaymentService {

    private static final Logger LOGGER = Logger.getLogger(StripePaymentService.class.getName());

    private final UserService userService;

    public StripePaymentService() {
        this.userService = new UserService();
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException("Stripe API key is not set.");
        }
        Stripe.apiKey = API_KEY;
    }


    public String createCheckoutSession(order order, double amount, String successUrl, String cancelUrl) throws StripeException {
        // Conversion en devise supportée (USD) - 1 USD = 100 cents
        double exchangeRate = 0.33; // Remplacer par le taux de change réel
        long amountCents = (long)(amount * exchangeRate * 100);

        // Récupération email
        String buyerEmail = getBuyerEmail(order.getBuyerId());

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd") // Devise supportée
                                                .setUnitAmount(amountCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Commande AgroSphere #" + order.getId())
                                                                .setDescription(String.format(
                                                                        "Total TND: %.2f | Converti en USD: %.2f",
                                                                        amount,
                                                                        amount * exchangeRate))
                                                                .addImage("https://example.com/logo.png") // Optionnel
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("order_id", String.valueOf(order.getId()))
                .putMetadata("original_amount_tnd", String.valueOf(amount));

        if (buyerEmail != null && !buyerEmail.isEmpty()) {
            paramsBuilder.setCustomerEmail(buyerEmail);
        }

        Session session = Session.create(paramsBuilder.build());
        LOGGER.info("Session Stripe créée : " + session.getId());
        return session.getUrl();
    }

    private String getBuyerEmail(int buyerId) {
        try {
            user buyer = userService.getOne(buyerId);
            return buyer != null ? buyer.getMail() : null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur récupération email client ID: " + buyerId, e);
            return null;
        }
    }

    public boolean verifyPayment(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        boolean isPaid = "paid".equalsIgnoreCase(session.getPaymentStatus());
        LOGGER.info((isPaid ? "PAID" : "NOT_PAID") + " result for session " + sessionId + ": " +
                "(Status: " + session.getStatus() + ", Payment Status: " + session.getPaymentStatus() + ")");
        return isPaid;
    }
}