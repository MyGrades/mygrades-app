package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.main.events.ScrapeProgressEvent;
import de.mygrades.view.ProgressImageViewOverlay;

/**
 * Fragment to show initial loading screen.
 */
public class FragmentInitialLoading extends Fragment {

    private ProgressImageViewOverlay progressImageViewOverlay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_initial_loading, container, false);

        progressImageViewOverlay = (ProgressImageViewOverlay) view.findViewById(R.id.iv_progress_overlay);

        EventBus.getDefault().register(this);

        return view;
    }

    /**
     * Receive event about scraping progress.
     *
     * @param scrapeProgressEvent ScrapeProgressEvent
     */
    public void onEventMainThread(ScrapeProgressEvent scrapeProgressEvent) {
        int currentStep = scrapeProgressEvent.getCurrentStep();
        int stepCount = scrapeProgressEvent.getStepCount();

        float progress = ((float)currentStep) / stepCount;

        progressImageViewOverlay.setProgress(progress);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
