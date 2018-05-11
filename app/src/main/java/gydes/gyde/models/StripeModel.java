package gydes.gyde.models;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Token;
import com.stripe.android.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Customer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rix on 5/10/18.
 */

public class StripeModel {
    private final static String STRIPE_PRIVATE_API_KEY = "sk_test_ZbL5ejRj63bOCU1mUbVZBdiL";
    private final static String STRIPE_PUBLIC_API_KEY = "pk_test_6mUSZUJf0upFQzlocgvyQknF";
    private final static String TRAVELLER_PATH = "Traveller";
    private final static String CARD_PATH = "cards";
    private final static String CUSTOMER_ID_PATH = "customerId";

    private Stripe stripe;
    private FirebaseUser currentUser;
    private String dataPath;
    private DatabaseReference databaseRef;
    private List<Object> cards;
    private Context context;
    private Customer customer;

    public StripeModel(Context context) {
        this.context = context;
        this.customer = null;
        this.cards = new ArrayList<Object>();

        stripe = new Stripe(context, STRIPE_PUBLIC_API_KEY);
        com.stripe.Stripe.apiKey = STRIPE_PRIVATE_API_KEY;

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        dataPath = TRAVELLER_PATH + "/" + currentUser.getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference(dataPath);

        initCustomer();

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("Cards:");
                System.out.println(cards.toString());
                cards.clear(); // clear local
                DataSnapshot cardsSnapshot = dataSnapshot.child(CARD_PATH);
                for(DataSnapshot child: cardsSnapshot.getChildren()) {
                    Object obj = child.getValue();
                    cards.add(obj); // Append all cards
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: handle error
            }
        });
    }

    private void initCustomer() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (customer != null)
                    return;

                if (dataSnapshot.child(CUSTOMER_ID_PATH).getValue() == null) {
                    Map<String, Object> customerParams = new HashMap<String, Object>();
                    customerParams.put("email", currentUser.getEmail());
                    customerParams.put("description", currentUser.getDisplayName());
                    try {
                        Customer newCustomer = Customer.create(customerParams);
                        System.out.println("Customer created");
                        dataSnapshot.getRef().child(CUSTOMER_ID_PATH).setValue(newCustomer.getId());
                    } catch(Exception e) {
                        e.printStackTrace();
                        // TODO: handle exception
                    }
                } else {
                    String customerId = (String) dataSnapshot.child(CUSTOMER_ID_PATH).getValue();
                    try {
                        customer = Customer.retrieve(customerId);
                    } catch(Exception e) {
                        e.printStackTrace();
                        // TODO: handle exception
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                        // TODO: handle error
            }
        });
    }

    private void addCardOnCustomer(String token) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("source", token);
        try {
            customer.getSources().create(params);
        } catch(Exception e) {
            e.printStackTrace();
            // TODO: handle error
        }
    }

    public boolean addCard(final SimpleCard card) {
        if (customer == null)
            return false;

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
                        SimpleCard cardWithToken = new SimpleCard();
                        cardWithToken.setLast4(card.getLast4());
                        cardWithToken.setCVV(card.getCVV());
                        cardWithToken.setExpiryMonth(card.getExpiryMonth());
                        cardWithToken.setExpiryYear(card.getExpiryYear());
                        cardWithToken.setStripeToken(token.getId());
                        cardWithToken.setCardType(token.getCard().getBrand());
                        cards.add(cardWithToken);
                        databaseRef.child(CARD_PATH).setValue(cards); // Push to firebase
                        addCardOnCustomer(token.getId());
                        // charge(token); // For testing
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
