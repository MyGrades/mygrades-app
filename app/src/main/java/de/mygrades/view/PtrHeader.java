package de.mygrades.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.main.events.ScrapeProgressEvent;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import org.w3c.dom.Text;

import in.srain.cube.views.ptr.indicator.PtrIndicator;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jonastheis on 18.10.15.
 */
public class PtrHeader extends FrameLayout implements PtrUIHandler {
    private ProgressWheel progressWheel;
    private TextView tvHeaderText;


    public PtrHeader(Context context) {
        super(context);

        View header = LayoutInflater.from(getContext()).inflate(R.layout.ptr_header, this);

        progressWheel = (ProgressWheel) findViewById(R.id.header_progress_wheel);

        tvHeaderText = (TextView) findViewById(R.id.tv_header_text);
        tvHeaderText.setText("Lasse los um zu laden");

        resetView();

        // register event bus
        // TODO: maybe nicer to set status of loading via activity/fragment via method call
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // unregister EventBus
        EventBus.getDefault().unregister(this);
    }


    /**
     * Receive an event about the scraping progress and set the ProgressWheel accordingly.
     *
     * @param scrapeProgressEvent ScrapeProgressEvent
     */
    public void onEventMainThread(ScrapeProgressEvent scrapeProgressEvent) {
        if (progressWheel != null) {

            int currentStep = scrapeProgressEvent.getCurrentStep();
            int stepCount = scrapeProgressEvent.getStepCount();

            float progress = ((float) currentStep) / stepCount;
            progressWheel.setProgress(progress);
        }
    }

    private void resetView() {
        progressWheel.setProgress(0.025f);
    }

    @Override
    public void onUIReset(PtrFrameLayout frame) {
        resetView();
    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {
        progressWheel.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate_indefinitely));
        tvHeaderText.setText("Zum Laden runterziehen.");
    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {
        tvHeaderText.setText("Lade deine Noten...");
    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame) {
        tvHeaderText.setText("Fertig geladen");
        progressWheel.setAnimation(null);
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE) {
            final int mOffsetToRefresh = frame.getOffsetToRefresh();
            final int currentPos = ptrIndicator.getCurrentPosY();
            final int lastPos = ptrIndicator.getLastPosY();

            if (currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh) {
                tvHeaderText.setText("Zum Laden runterziehen.");
            } else if (currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh) {
                tvHeaderText.setText("Loslassen zum aktualisieren");
            }
        }

    }
}

