package dh.mygrades.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import dh.mygrades.R;
import dh.mygrades.util.Constants;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * Pull to refresh header with ProgressWheel.
 */
public class PtrHeader extends FrameLayout implements PtrUIHandler {
    private boolean isScraping;
    private ProgressWheelWrapper progressWheel;
    private TextView tvHeaderText;
    private String refreshLoadingText;
    private boolean isError;

    public PtrHeader(Context context, String refreshLoadingText) {
        super(context);
        this.isError = false;
        this.refreshLoadingText = refreshLoadingText;

        View header = LayoutInflater.from(getContext()).inflate(R.layout.ptr_header, this);

        progressWheel = new ProgressWheelWrapper((ProgressWheel) findViewById(R.id.header_progress_wheel), findViewById(R.id.header_progress_wheel_bg));
        tvHeaderText = (TextView) findViewById(R.id.tv_header_text);

        progressWheel.reset();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void onUIReset(PtrFrameLayout frame) {
        progressWheel.reset();
        isError = false;
    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {
        tvHeaderText.setText(R.string.ptr_header_pull_to_refresh);
        progressWheel.startAnimation(getContext());
    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {
        tvHeaderText.setText(refreshLoadingText);
        isScraping = true;
    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame) {
        if (isError) {
            tvHeaderText.setText(R.string.ptr_header_refresh_error);
        } else {
            tvHeaderText.setText(R.string.ptr_header_refresh_complete);
        }

        progressWheel.loadingFinished(getContext(), isError);
        isScraping = false;
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE) {
            final int mOffsetToRefresh = frame.getOffsetToRefresh();
            final int currentPos = ptrIndicator.getCurrentPosY();
            final int lastPos = ptrIndicator.getLastPosY();

            if (currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh) {
                tvHeaderText.setText(R.string.ptr_header_pull_to_refresh);
            } else if (currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh) {
                tvHeaderText.setText(R.string.ptr_header_release_to_refresh);
            }
        }
    }

    /**
     * Increases progress on progressWheel.
     *
     * @param currentStep current step
     * @param stepCount count of steps
     */
    public void increaseProgress(int currentStep, int stepCount) {
        progressWheel.increaseProgress(currentStep, stepCount);
    }


    /**
     * Set progress on progressWheel.
     *
     * @param progress Progress of Loading
     */
    public void setProgress(float progress) {
        progressWheel.setProgress(progress);
    }

    /**
     * Save attributes regarding ptr to instance state.
     * @param outState instance state bundle
     */
    public void saveInstanceState(Bundle outState) {
        outState.putBoolean(Constants.INSTANCE_IS_SCRAPING_STATE, isScraping);
        outState.putFloat(Constants.INSTANCE_PROGRESS_STATE, progressWheel.getProgress());
    }

    /**
     * Load attributes from instance state and show Loading animation if it was loading before.
     * @param savedInstanceState instance state bundle
     */
    public void restoreInstanceState(Bundle savedInstanceState, PtrFrameLayout ptrFrame) {
        if (savedInstanceState != null) {
            isScraping = savedInstanceState.getBoolean(Constants.INSTANCE_IS_SCRAPING_STATE);
            Log.d("test", "restoreInstanceState() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
            if (isScraping) {
                float progress = savedInstanceState.getFloat(Constants.INSTANCE_PROGRESS_STATE, 0);
                progressWheel.startAnimation(getContext());
                tvHeaderText.setText(refreshLoadingText);
                setProgress(progress);
                ptrFrame.autoRefresh();
            }
        }
    }

    public boolean isScraping() {
        return isScraping;
    }

    public boolean isError() {
        return isError;
    }

    public void setIsError(boolean isError) {
        this.isError = isError;
    }
}

