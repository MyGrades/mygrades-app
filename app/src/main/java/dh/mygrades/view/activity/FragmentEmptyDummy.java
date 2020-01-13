package dh.mygrades.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dh.mygrades.R;

/**
 * Fragment with empty layout.
 *
 * This is used for an animation while replacing the InitialScrapingFragment.
 * The InitialScrapingFragment is translated to the top,
 * and this fills the space beneath (by moving up).
 */
public class FragmentEmptyDummy extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_faq, container, false);
    }
}
