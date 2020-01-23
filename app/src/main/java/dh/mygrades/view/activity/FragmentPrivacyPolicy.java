package dh.mygrades.view.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import dh.mygrades.R;
import dh.mygrades.util.Config;

/**
 * Fragment to show the privacy policy within a webview.
 */
public class FragmentPrivacyPolicy extends Fragment {

    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webView = (WebView) view.findViewById(R.id.webview);
        webView.loadUrl(Config.getServerUrl() + "/datenschutz");
    }
}
