package de.mygrades.view.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.ScrapeProgressEvent;
import de.mygrades.view.ProgressImageViewOverlay;

/**
 * Fragment to show initial loading screen.
 */
public class FragmentInitialLoading extends Fragment {

    private ProgressImageViewOverlay progressImageViewOverlay;

    private Handler handler = new Handler();
    private Runnable progressAnimation = new Runnable() {
        @Override
        public void run() {
            progressImageViewOverlay.increaseProgress(0.001f);
            handler.postDelayed(progressAnimation, 20);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_initial_loading, container, false);

        progressImageViewOverlay = (ProgressImageViewOverlay) view.findViewById(R.id.iv_progress_overlay);

        // register to events
        EventBus.getDefault().register(this);

        // start scraping
        MainServiceHelper mainServiceHelper = new MainServiceHelper(getContext());
        mainServiceHelper.scrapeForGrades(true);

        return view;
    }

    /**
     * Receive event about scraping progress and set the current progress.
     * If the current step is the first one, an ongoing animation runnable will be started.
     *
     * @param scrapeProgressEvent ScrapeProgressEvent
     */
    public void onEventMainThread(ScrapeProgressEvent scrapeProgressEvent) {
        int currentStep = scrapeProgressEvent.getCurrentStep();
        int stepCount = scrapeProgressEvent.getStepCount();

        float progress = ((float) currentStep) / stepCount;
        float nextProgress =((float) currentStep + 1) / stepCount;

        // set current progress
        progressImageViewOverlay.setProgress(progress, nextProgress);

        if (currentStep == 0) {
            // start animation at first step, it runs continuously
            progressAnimation.run();
        } else if (currentStep == stepCount) {
            // stop animation, if last step is reached
            handler.removeCallbacks(progressAnimation);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
