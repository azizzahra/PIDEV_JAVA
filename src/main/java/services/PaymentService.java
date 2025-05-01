package services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import entities.Loan;

public class PaymentService {
    private static final String STRIPE_SECRET_KEY = "sk_test_51RJcqBPYiKRa3ZKirCzGSk8VFR3AFksM5s9F42vo6rIaNYqrN1eOqgAXpVHLyTPiKI0J9Zto5N2uZo0DlFGvrrEA00frBwf8mI";

    public PaymentService() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

    public String createCheckoutSession(Loan loan) throws StripeException {
        long amount = (long) (loan.getTicketPrice() * 100); // amount in cents

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("https://example.com/success") // Set to your own success URL
                        .setCancelUrl("https://example.com/cancel")   // Set to your own cancel URL
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("usd")
                                                        .setUnitAmount(amount)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Loan: " + loan.getFormation())
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

        Session session = Session.create(params);
        return session.getUrl(); // Use this URL to redirect user
    }
}
