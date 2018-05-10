package gydes.gyde;

/**
 * Created by rix on 5/10/18.
 */

public class SimpleCard {
    private String cardNumber;
    private String last4;
    private int expiryMonth;
    private int expiryYear;
    private String cvv;
    private String stripeToken;
    private String cardType;
    public SimpleCard() {}

    private static final int CARD_NUM_LEN_MIN = 13;
    private static final int CARD_NUM_LEN_MAX = 19;
    private static final int CVV_LEN_MIN = 3;
    private static final int CVV_LEN_MAX = 4;
    private static final int STRIPE_TOKEN_LEN_MIN = 1; // Very rough estimate
    private static final int MONTH_MIN = 1;
    private static final int MONTH_MAX = 12;
    private static final int YEAR_MIN = 1000; // Very rough estimate
    private static final int YEAR_MAX = 3000; // Very rough estimate

    public boolean setCardNumber(String cn) {
        if (cn.length() < CARD_NUM_LEN_MIN || cn.length() > CARD_NUM_LEN_MAX)
            return false;
        cardNumber = cn;
        last4 = cn.substring(cn.length() - 4, cn.length());
        return true;
    }

    public boolean setLast4(String l4) {
        if (l4.length() != 4)
            return false;
        last4 = l4;
        return true;
    }

    public boolean setExpiryMonth(int month) {
        if (month < MONTH_MIN || month > MONTH_MAX)
            return false;
        expiryMonth = month;
        return true;
    }

    public boolean setExpiryYear(int year) {
        if (year < YEAR_MIN || year > YEAR_MAX)
            return false;
        expiryYear = year;
        return true;
    }

    public boolean setCVV(String cvvCode) {
        if (cvvCode.length() < CVV_LEN_MIN || cvvCode.length() > CVV_LEN_MAX)
            return false;
        cvv = cvvCode;
        return true;
    }

    public boolean setStripeToken(String token) {
        if (token.length() < STRIPE_TOKEN_LEN_MIN)
            return false;
        stripeToken = token;
        return true;
    }

    public void setCardType(String type) {
        cardType = type;
    }

    public String getCardNumber() { return cardNumber; }
    public String getLast4() { return last4; }
    public int getExpiryMonth() { return expiryMonth; }
    public int getExpiryYear() { return expiryYear; }
    public String getCVV() { return cvv; }
    public String getStripeToken() { return stripeToken; }
    public String getCardType() { return cardType; }
}
