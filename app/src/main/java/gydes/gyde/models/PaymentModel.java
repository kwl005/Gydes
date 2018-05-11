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

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rix on 5/10/18.
 */

public class PaymentModel {
    public interface PaymentModelCallback {
        void onError(int errorCode);
        void onSuccess(int successCode);
        void onCardData(List<SimpleCard> cards);
    }
    private final static String STRIPE_PRIVATE_API_KEY = "sk_test_ZbL5ejRj63bOCU1mUbVZBdiL";
    private final static String STRIPE_PUBLIC_API_KEY = "pk_test_6mUSZUJf0upFQzlocgvyQknF";
    private final static String TRAVELLER_PATH = "Traveller";
    private final static String CARD_PATH = "cards";
    private final static String CUSTOMER_ID_PATH = "customerId";

    public final static int FIREBASE_CARD_ERROR = 1;
    public final static int STRIPE_CUSTOMER_ERROR = 2;
    public final static int STRIPE_ADD_SOURCE_ERROR = 3;
    public final static int STRIPE_CARD_ERROR = 4;

    public final static int STRIPE_CUSTOMER_SUCCESS = 5;
    public final static int STRIPE_ADD_SOURCE_SUCCESS = 6;
    public final static int STRIPE_ADD_CARD_SUCCESS = 7;

    private Stripe stripe;
    private FirebaseUser currentUser;
    private String dataPath;
    private DatabaseReference databaseRef;
    private List<SimpleCard> cards;
    private Context context;
    private Activity activity;
    private Customer customer;

    public PaymentModel(final Activity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.customer = null;
        this.cards = new ArrayList<SimpleCard>();

        stripe = new Stripe(context, STRIPE_PUBLIC_API_KEY);
        com.stripe.Stripe.apiKey = STRIPE_PRIVATE_API_KEY;

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        dataPath = TRAVELLER_PATH + "/" + currentUser.getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference(dataPath);

        initCustomer();

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cards.clear(); // clear local
                DataSnapshot cardsSnapshot = dataSnapshot.child(CARD_PATH);
                for(DataSnapshot cardObj: cardsSnapshot.getChildren()) {
                    cards.add(objToCard(cardObj)); // Append all cards
                }
                ((PaymentModelCallback)activity).onCardData(cards); // Send card data back
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ((PaymentModelCallback)activity).onError(FIREBASE_CARD_ERROR);
            }
        });
    }

    private SimpleCard objToCard(DataSnapshot cardObj) {
        SimpleCard card = new SimpleCard();
        card.setLast4((String) cardObj.child("last4").getValue());
        card.setCVV((String) cardObj.child("cvv").getValue());
        card.setCardType((String) cardObj.child("cardType").getValue());

        // Type casting & converting required
        Long expiryMonth = (Long) cardObj.child("expiryMonth").getValue();
        Long expiryYear = (Long) cardObj.child("expiryYear").getValue();
        card.setExpiryMonth(expiryMonth.intValue());
        card.setExpiryYear(expiryYear.intValue());
        return card;
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
                        dataSnapshot.getRef().child(CUSTOMER_ID_PATH).setValue(newCustomer.getId());
                        ((PaymentModelCallback)activity).onSuccess(STRIPE_CUSTOMER_SUCCESS);
                    } catch(Exception e) {
                        ((PaymentModelCallback)activity).onError(STRIPE_CUSTOMER_ERROR);
                    }
                } else {
                    String customerId = (String) dataSnapshot.child(CUSTOMER_ID_PATH).getValue();
                    try {
                        customer = Customer.retrieve(customerId);
                        ((PaymentModelCallback)activity).onSuccess(STRIPE_CUSTOMER_SUCCESS);
                    } catch(Exception e) {
                        ((PaymentModelCallback)activity).onError(STRIPE_CUSTOMER_ERROR);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ((PaymentModelCallback)activity).onError(FIREBASE_CARD_ERROR);
            }
        });
    }

    private boolean addCardOnCustomer(String token) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("source", token);
        try {
            customer.getSources().create(params);
            ((PaymentModelCallback)activity).onSuccess(STRIPE_ADD_SOURCE_SUCCESS);
            return true;
        } catch(Exception e) {
            ((PaymentModelCallback)activity).onError(STRIPE_ADD_SOURCE_ERROR);
            return false;
        }
    }

    public void addCard(final SimpleCard card) {
        if (customer == null)
            return;

        Card newCard = new Card(
                card.getCardNumber(),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getCVV()
        ); // Create Card object provided by stripe

        if (!newCard.validateCard()) {
            return;
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
                        if ( addCardOnCustomer(token.getId()) ) {
                            ((PaymentModelCallback)activity).onSuccess(STRIPE_ADD_CARD_SUCCESS);
                        } else {
                            ((PaymentModelCallback)activity).onError(STRIPE_CARD_ERROR);
                        }
                        // charge(token); // For testing
                    }
                    public void onError(Exception error) {
                        ((PaymentModelCallback)activity).onError(STRIPE_CARD_ERROR);
                    }
                }
        );

        return;
    }

    public void removeCard(SimpleCard card) {
        // TODO: Complete this function
    }

    // For testing
    private void charge(Token token) {
        // Create customer
        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("description", "Customer for joseph.davis@example.com");
        customerParams.put("source", token.getId());
        try {
            Customer newCustomer = Customer.create(customerParams);
            // Create charge
            Map<String, Object> chargeMap = new HashMap<String, Object>();
            chargeMap.put("amount", 100); // $1, it's in cent
            chargeMap.put("currency", "usd");
            chargeMap.put("customer", newCustomer.getId());
            Charge charge = Charge.create(chargeMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
