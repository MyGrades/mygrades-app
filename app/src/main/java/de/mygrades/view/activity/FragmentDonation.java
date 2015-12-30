package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.mygrades.R;
import de.mygrades.util.Config;
import de.mygrades.util.billing.IabHelper;
import de.mygrades.util.billing.IabResult;

/**
 * Created by jonastheis on 21.12.15.
 */
public class FragmentDonation extends Fragment {
    private static final String TAG = FragmentDonation.class.getSimpleName();

    private IabHelper mHelper;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        });

        Log.d(TAG, "onViewCreated");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mHelper != null) mHelper.dispose();
        mHelper = null;
        Log.d(TAG, "onDestroyView");
    }
}
