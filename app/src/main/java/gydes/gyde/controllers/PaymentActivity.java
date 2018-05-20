package gydes.gyde.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import gydes.gyde.R;
import gydes.gyde.models.SimpleCard;
import gydes.gyde.models.PaymentModel;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

/**
 * Created by rix on 5/10/18.
 */

public class PaymentActivity extends AppCompatActivity implements PaymentModel.PaymentModelCallback {

    private class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView brandImg;
            public TextView last4Text;
            public TextView expiryText;
            public ViewHolder(View itemView) {
                super(itemView);
                brandImg = (ImageView) itemView.findViewById(R.id.cardBrand);
                last4Text = (TextView) itemView.findViewById(R.id.cardLast4);
                expiryText = (TextView) itemView.findViewById(R.id.cardExpiry);
            }
        }

        private ArrayList<SimpleCard> cards;

        public CardListAdapter(ArrayList<SimpleCard> cards) {
            this.cards = cards;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View contactView = inflater.inflate(R.layout.card_item, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull CardListAdapter.ViewHolder holder, int position) {

            SimpleCard card = cards.get(position);
            if (card != null) {
                ImageView brandImg = (ImageView) holder.brandImg;
                TextView last4Text = (TextView) holder.last4Text;
                TextView expiryText = (TextView) holder.expiryText;

                int brand = R.drawable.cio_ic_visa; // Default as Visa
                switch (card.getCardType()) {
                    case "Visa":
                        brand = R.drawable.cio_ic_visa;
                        break;
                    case "MasterCard":
                        brand = R.drawable.cio_ic_mastercard;
                        break;
                    case "Mastercard":
                        brand = R.drawable.cio_ic_mastercard;
                        break;
                    case "American Express":
                        brand = R.drawable.cio_ic_amex;
                        break;
                    case "Discover":
                        brand = R.drawable.cio_ic_discover;
                        break;
                    case "JCB":
                        brand = R.drawable.cio_ic_jcb;
                        break;
                }

                brandImg.setImageResource(brand);
                last4Text.setText(card.getFormattedCardNumber());
                expiryText.setText(card.getFormattedExpiry());
            }
        }

        @Override
        public int getItemCount() {
            return cards.size();
        }
    }

    private int SCAN_REQUEST_CODE = 1337; // Or any other unique integer
    private PaymentModel paymentModel;
    private RecyclerView cardListView;
    private ArrayList<SimpleCard> cardList;
    private CardListAdapter cardListAdapter;
    private Paint p = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        cardListView = (RecyclerView) findViewById(R.id.cardList);
        paymentModel = new PaymentModel(this);
        cardList = new ArrayList<SimpleCard>();

        // TODO: Research on thread problem
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        cardListView.setLayoutManager(llm);
        this.cardListAdapter = new CardListAdapter(cardList);
        cardListView.setAdapter(this.cardListAdapter);
        initSwipe();
    }

    public void onAddCardPress(View v) {
        Intent scanIntent = new Intent(this, CardIOActivity.class);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false
        startActivityForResult(scanIntent, SCAN_REQUEST_CODE);
    }

    private void initSwipe(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT){
                    paymentModel.removeCard(cardList.get(position));
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if(dX > 0){
                        // Left-To-Right swipe unused
                        /*
                        p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
                        */
                        /*
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_edit_white);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                        c.drawBitmap(null,null,icon_dest,p);
                        */
                    } else {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
                        /* // TODO: Find suitable icon
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                        c.drawBitmap(null,null,icon_dest,p);
                        */
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(cardListView);
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
                makeToast("Stripe error");
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
                break;
            case PaymentModel.STRIPE_CUSTOMER_SUCCESS:
                break;
            case PaymentModel.STRIPE_REMOVE_CARD_SUCCESS:
                makeToast("Removed card successfully");
                break;
        }
    }

    @Override
    public void onCardData(ArrayList<SimpleCard> cards) {
        this.cardList.clear();
        this.cardList.addAll(cards);
        this.cardListAdapter.notifyDataSetChanged();
    }

    private void makeToast(String msg) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }
}
