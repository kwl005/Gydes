package gydes.gyde.controllers;

import android.content.Context;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Toast;

import java.util.List;

import gydes.gyde.R;
import gydes.gyde.models.SimpleCard;
import gydes.gyde.models.PaymentModel;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

/**
 * Created by rix on 5/10/18.
 */

public class PaymentActivity extends AppCompatActivity implements PaymentModel.PaymentModelCallback {

    private int SCAN_REQUEST_CODE = 1337; // Or any other unique integer
    private PaymentModel paymentModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        paymentModel = new PaymentModel(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void onAddCardPress(View v) {
        Intent scanIntent = new Intent(this, CardIOActivity.class);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false
        startActivityForResult(scanIntent, SCAN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                SimpleCard newCard = new SimpleCard();
                newCard.setCardNumber(scanResult.cardNumber);
                newCard.setExpiryMonth(scanResult.expiryMonth);
                newCard.setExpiryYear(scanResult.expiryYear);
                newCard.setCVV(scanResult.cvv);
                paymentModel.addCard(newCard);
            } else {
                // Silent when cancelled
            }
        }
    }

    @Override
    public void onError(int errorCode) {
        switch (errorCode) {
            case PaymentModel.FIREBASE_CARD_ERROR:
                makeToast("Database error");
                break;
            case PaymentModel.STRIPE_CARD_ERROR:
                makeToast("Unable to add card");
                break;
            case PaymentModel.STRIPE_CUSTOMER_ERROR:
                makeToast("Unable to create customer");
                break;
        }
    }

    @Override
    public void onSuccess(int successCode) {
        switch (successCode) {
            case PaymentModel.STRIPE_ADD_CARD_SUCCESS:
                makeToast("Added card successfully");
        }
    }

    @Override
    public void onCardData(List<SimpleCard> cards) {
        // TODO: Show cards on screen
        System.out.println(cards.toString());
    }

    private void makeToast(String msg) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }
}
