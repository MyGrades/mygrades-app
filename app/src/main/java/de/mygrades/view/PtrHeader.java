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
 * Pull to refresh header with ProgressWheel.
 */
public class PtrHeader extends FrameLayout implements PtrUIHandler {
    private ProgressWheelWrapper progressWheel;
    private TextView tvHeaderText;

    public PtrHeader(Context context) {
        super(context);

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
    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {
        tvHeaderText.setText(R.string.ptr_header_pull_to_refresh);
        progressWheel.startAnimation(getContext());
    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {
        tvHeaderText.setText(R.string.ptr_header_refreshing);
    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame) {
        tvHeaderText.setText(R.string.ptr_header_refresh_complete);

        progressWheel.loadingFinished(getContext());
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

}

