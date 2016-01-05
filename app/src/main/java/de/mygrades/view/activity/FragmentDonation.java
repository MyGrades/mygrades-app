package de.mygrades.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.pnikosis.materialishprogress.ProgressWheel;

import de.mygrades.R;
import de.mygrades.util.Config;
import de.mygrades.util.billing.IabHelper;
import de.mygrades.util.billing.IabResult;
import de.mygrades.util.billing.Purchase;
import de.mygrades.view.UIHelper;

/**
 * Fragment to connect to Google Play (in app billing / IAB) to make a donation.
 * Please support us and our work :).
 */
public class FragmentDonation extends Fragment {
    private static final String TAG = FragmentDonation.class.getSimpleName();

    private static final String SKU_DONATE_2 = "donation2";
    private static final String SKU_DONATE_5 = "donation5";
    private static final String SKU_DONATE_10 = "donation10";
    // (arbitrary) request code for the purchase flow
    private static final int RC_REQUEST = 10001;

    // Views
    private ProgressWheel progressWheel;
    private ScrollView svDonationInfo;
    private Button btDonate2;
    private Button btDonate5;
    private Button btDonate10;
    private LinearLayout llDonationThanks;

    // IAB
    private IabHelper mHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initButtons();

        initIAB();
    }

    /**
     * Initializes the views for the fragment.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     */
    private void initViews(View view) {
        progressWheel = (ProgressWheel) view.findViewById(R.id.progress_wheel);
        svDonationInfo = (ScrollView) view.findViewById(R.id.donation_info);
        btDonate2 = (Button) view.findViewById(R.id.bt_donation2);
        btDonate5 = (Button) view.findViewById(R.id.bt_donation5);
        btDonate10 = (Button) view.findViewById(R.id.bt_donation10);
        llDonationThanks = (LinearLayout) view.findViewById(R.id.donation_thanks);
    }

    /**
     * Initializes the behaviour of the buttons.
     */
    private void initButtons() {
        btDonate2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPurchase(SKU_DONATE_2);
            }
        });

        btDonate5.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPurchase(SKU_DONATE_5);
            }
        });

        btDonate10.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPurchase(SKU_DONATE_10);
            }
        });
    }

    /**
     * Starts the purchase process for given sku.
     * @param sku String identifying the unique number of the IAB product
     */
    private void startPurchase(String sku) {
        // launch the purchase UI flow.
        showLoadingScreen();
        Log.d(TAG, "Launching purchase flow for " + sku);

        String payload = "Thanks for your donation :)";

        // We will be notified of completion via mPurchaseFinishedListener
        mHelper.launchPurchaseFlow(getActivity(), sku, RC_REQUEST,
                mPurchaseFinishedListener, payload);
    }

    /**
     * Initializes the connection to Google Play for in app billing.
     */
    private void initIAB() {
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(getContext(), Config.getPlayStorePublicKey());
        // disable debug logging
        mHelper.enableDebugLogging(false);

        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    showError("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                Log.d(TAG, "Setup successful.");
            }
        });
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // if it fails show info screen with error msg
            if (result.isFailure()) {
                showError("Error purchasing: " + result);
                return;
            }

            // consume donation -> another donation is possible
            mHelper.consumeAsync(purchase, mConsumeFinishedListener);
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // if it fails show info screen with error msg
            if (result.isFailure()) {
                showError("Error while consuming: " + result);
                showDonationInfoScreen();
                return;
            }

            showThanksScreen();
        }
    };

    /**
     * Shows the thanks screen within the fragment and hides loading and info.
     */
    private void showThanksScreen() {
        svDonationInfo.setVisibility(View.GONE);
        progressWheel.setVisibility(View.GONE);
        llDonationThanks.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the loading screen within the fragment and hides loading and thanks.
     */
    private void showLoadingScreen() {
        svDonationInfo.setVisibility(View.GONE);
        llDonationThanks.setVisibility(View.GONE);
        progressWheel.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the info screen within the fragment and hides loading and thanks.
     */
    private void showDonationInfoScreen() {
        llDonationThanks.setVisibility(View.GONE);
        progressWheel.setVisibility(View.GONE);
        svDonationInfo.setVisibility(View.VISIBLE);
    }

    /**
     * Shows error and info screen and logs error msg.
     */
    private void showError(String msg) {
        UIHelper.showSnackbar(getView(), getString(R.string.snackbar_error_iab));
        Log.d(TAG, msg);
        showDonationInfoScreen();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }
}
