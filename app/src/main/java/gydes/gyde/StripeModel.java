package gydes.gyde;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Token;
import com.stripe.android.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.Source;

import android.content.Context;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import static java.security.AccessController.getContext;

/**
 * Created by rix on 5/10/18.
 */

public class StripeModel {
    private final static String STRIPE_PRIVATE_API_KEY = "sk_test_ZbL5ejRj63bOCU1mUbVZBdiL";
    private final static String STRIPE_PUBLIC_API_KEY = "pk_test_6mUSZUJf0upFQzlocgvyQknF";
    private Stripe stripe;

    public StripeModel(Context context) {
        stripe = new Stripe(context, STRIPE_PUBLIC_API_KEY);
    }

    public boolean addCard(SimpleCard card) {
        Card newCard = new Card(
                card.getCardNumber(),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getCVV()
        ); // Create Card object provided by stripe

        if (!newCard.validateCard()) {
            return false;
        }

        stripe.createToken(
                newCard,
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        // Received token
                        System.out.println("Received token");
                        // TODO: Save token to firebase
                        charge(token); // For testing
                    }
                    public void onError(Exception error) {
                        System.out.println("Create token failed");
                        // TODO: Show error
                        // Error
                       error.printStackTrace();
                    }
                }
        );

        return true;
    }

    // For testing
    private void charge(Token token) {
        com.stripe.Stripe.apiKey = STRIPE_PRIVATE_API_KEY;
        // Create customer
        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("description", "Customer for joseph.davis@example.com");
        customerParams.put("source", token.getId());
        try {
            Customer newCustomer = Customer.create(customerParams);
            System.out.println("Customer created");
            // Create charge
            Map<String, Object> chargeMap = new HashMap<String, Object>();
            chargeMap.put("amount", 100); // $1, it's in cent
            chargeMap.put("currency", "usd");
            chargeMap.put("customer", newCustomer.getId());
            Charge charge = Charge.create(chargeMap);
            System.out.println(charge);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
