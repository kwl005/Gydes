package gydes.gyde.controllers;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

import gydes.gyde.R;
import gydes.gyde.models.SimpleCard;
import gydes.gyde.models.StripeModel;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

/**
 * Created by rix on 5/10/18.
 */

public class PaymentActivity extends AppCompatActivity {

    private int SCAN_REQUEST_CODE = 1337; // Or any other unique integer
    private StripeModel stripeModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        stripeModel = new StripeModel(this);
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
                boolean isAdded = stripeModel.addCard(newCard); // TODO: use callback
                if (!isAdded) {
                    // TODO: Card not added
                }
            } else {
                // TODO: scan canceled
                //resultDisplayStr = "Scan was canceled.";
            }
        }
    }
}
