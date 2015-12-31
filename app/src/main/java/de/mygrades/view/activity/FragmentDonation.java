package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pnikosis.materialishprogress.ProgressWheel;

import de.mygrades.R;
import de.mygrades.util.billing.IabHelper;

/**
 * Fragment to connect to Google Play (in app billing / IAB) to make a donation.
 * Please support us and our work :).
 */
public class FragmentDonation extends Fragment {
    private static final String TAG = FragmentDonation.class.getSimpleName();

    // Views
    private ProgressWheel progressWheel;
    private LinearLayout llDonationInfo;
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

        /*
        mHelper = new IabHelper(getContext(), Config.getPlayStorePublicKey());
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                } else {
                    Log.d(TAG, "successful: " + result);
                }
            }
        });*/

        initViews(view);
        initButtons();

        Log.d(TAG, "onViewCreated");
    }

    /**
     * Init the views for the fragment.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     */
    private void initViews(View view) {
        progressWheel = (ProgressWheel) view.findViewById(R.id.progress_wheel);
        llDonationInfo = (LinearLayout) view.findViewById(R.id.donation_info);
        btDonate2 = (Button) view.findViewById(R.id.bt_donation2);
        btDonate5 = (Button) view.findViewById(R.id.bt_donation5);
        btDonate10 = (Button) view.findViewById(R.id.bt_donation10);
        llDonationThanks = (LinearLayout) view.findViewById(R.id.donation_thanks);
    }

    private void initButtons() {
        btDonate2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Donate 2");
            }
        });

        btDonate5.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Donate 5");
            }
        });

        btDonate10.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Donate 10");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mHelper != null) mHelper.dispose();
        mHelper = null;
        Log.d(TAG, "onDestroyView");
    }
}
