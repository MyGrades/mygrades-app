package dh.mygrades.view.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import de.greenrobot.event.EventBus;
import dh.mygrades.R;
import dh.mygrades.main.MainServiceHelper;
import dh.mygrades.main.events.ErrorEvent;

import dh.mygrades.BuildConfig;
import dh.mygrades.main.tasks.SendFeedback;
import dh.mygrades.main.tasks.TaskListener;
import dh.mygrades.util.Constants;
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import io.sentry.event.UserBuilder;

/**
 * Fragment with input fields to post a university wish.
 */
public class FragmentPostWish extends Fragment {

    private EditText etUniversityName;
    private EditText etName;
    private EditText etEmail;
    private EditText etMessage;
    private Button btnPostWish;
    private TextView tvStatus;
    private ProgressWheel progressWheel;

    private static final String ERROR_TYPE_STATE = "error_type_state";
    private ErrorEvent.ErrorType actErrorType;

    private static final String POST_WISH_DONE_STATE = "error_report_done_state";
    private boolean postWishDone;

    private MainServiceHelper mainServiceHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_wish, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainServiceHelper = new MainServiceHelper(getContext());

        etUniversityName = (EditText) view.findViewById(R.id.et_university_name);
        etName = (EditText) view.findViewById(R.id.et_name);
        etEmail = (EditText) view.findViewById(R.id.et_email);
        etMessage = (EditText) view.findViewById(R.id.et_message);
        btnPostWish = (Button) view.findViewById(R.id.btn_post_wish);
        tvStatus = (TextView) view.findViewById(R.id.tv_status);
        progressWheel = (ProgressWheel) view.findViewById(R.id.progress_wheel);

        TextView tvPrivacyInfo = (TextView) view.findViewById(R.id.tv_privacy_info);
        tvPrivacyInfo.setText(Html.fromHtml(getString(R.string.tv_post_wish_privacy_info)));
        tvPrivacyInfo.setMovementMethod(LinkMovementMethod.getInstance());

        btnPostWish.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    progressWheel.setVisibility(View.VISIBLE);
                    btnPostWish.setVisibility(View.GONE);
                    tvStatus.setText("");

                    // hide keyboard
                    InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    postWish();
                }
            }
        });

        // restore instance state
        if (savedInstanceState != null) {
            String error = savedInstanceState.getString(ERROR_TYPE_STATE);
            if (error != null) {
                showError(ErrorEvent.ErrorType.valueOf(error));
            }

            if (savedInstanceState.getBoolean(POST_WISH_DONE_STATE, false)) {
                showPostWishDone();
            }
        }

        // register event bus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save error state
        if (actErrorType != null) {
            outState.putString(ERROR_TYPE_STATE, actErrorType.name());
        }

        outState.putBoolean(POST_WISH_DONE_STATE, postWishDone);
    }

    /**
     * Posts the university wish.
     */
    private void postWish() {
        
        String universityName = etUniversityName.getText().toString();
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String message = etMessage.getText().toString();

        TaskListener feedbackListener = new TaskListener() {
            @Override
            public void callback(){
                showPostWishDone();
            }
        };


        new SendFeedback(feedbackListener, getActivity(), name, email, message, "wish", universityName).execute();
    }

    /**
     * Checks if the university name is provided.
     *
     * @return true if input is correct
     */
    private boolean validateInput() {
        boolean inputCorrect = true;

        if (TextUtils.isEmpty(etUniversityName.getText().toString())) {
            etUniversityName.setError(getResources().getString(R.string.university_name_not_empty));
            inputCorrect = false;
        }

        return inputCorrect;
    }

    /**
     * Show error message by given error type.
     *
     * @param errorType - ErrorEvent.ErrorType
     */
    private void showError(ErrorEvent.ErrorType errorType) {
        actErrorType = errorType;
        progressWheel.setVisibility(View.GONE);
        btnPostWish.setVisibility(View.VISIBLE);

        String errorMessage;
        switch (errorType) {
            case NO_NETWORK:
                errorMessage = getString(R.string.error_no_network);
                break;
            case TIMEOUT:
                errorMessage = getString(R.string.error_server_timeout);
                break;
            case GENERAL:
            default:
                errorMessage = getString(R.string.error_post_error_report);
        }
        tvStatus.setText(errorMessage);
    }

    /**
     * Receive error events and show error message.
     *
     * @param errorEvent - ErrorEvent
     */
    public void onEventMainThread(ErrorEvent errorEvent) {
        showError(errorEvent.getType());
    }

    /**
     * Show text view that indicates the the wish has been posted successfully.
     */
    private void showPostWishDone() {
        actErrorType = null;
        postWishDone = true;

        progressWheel.setVisibility(View.GONE);
        btnPostWish.setVisibility(View.GONE);
        tvStatus.setText(getString(R.string.error_report_done));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
