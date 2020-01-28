package dh.mygrades.view.activity;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
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
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import io.sentry.event.UserBuilder;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import dh.mygrades.util.Constants;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

/**
 * Fragment with input fields to report an error.
 */
public class FragmentReportError extends Fragment {

    private EditText etName;
    private EditText etEmail;
    private EditText etErrorMessage;
    private Button btnReportError;
    private TextView tvStatus;
    private ProgressWheel progressWheel;

    private static final String ERROR_TYPE_STATE = "error_type_state";
    private ErrorEvent.ErrorType actErrorType;

    private static final String ERROR_REPORT_DONE_STATE = "error_report_done_state";
    private boolean errorReportDone;

    private MainServiceHelper mainServiceHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report_error, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainServiceHelper = new MainServiceHelper(getContext());

        etName = (EditText) view.findViewById(R.id.et_name);
        etEmail = (EditText) view.findViewById(R.id.et_email);
        etErrorMessage = (EditText) view.findViewById(R.id.et_error);
        btnReportError = (Button) view.findViewById(R.id.btn_report_error);
        tvStatus = (TextView) view.findViewById(R.id.tv_status);
        progressWheel = (ProgressWheel) view.findViewById(R.id.progress_wheel);

        TextView tvPrivacyInfo = (TextView) view.findViewById(R.id.tv_privacy_info);
        tvPrivacyInfo.setText(Html.fromHtml(getString(R.string.tv_post_wish_privacy_info)));
        tvPrivacyInfo.setMovementMethod(LinkMovementMethod.getInstance());

        btnReportError.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    progressWheel.setVisibility(View.VISIBLE);
                    btnReportError.setVisibility(View.GONE);
                    tvStatus.setText("");

                    // hide keyboard
                    InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    postErrorReport();
                }
            }
        });

        // restore instance state
        if (savedInstanceState != null) {
            String error = savedInstanceState.getString(ERROR_TYPE_STATE);
            if (error != null) {
                showError(ErrorEvent.ErrorType.valueOf(error));
            }

            if (savedInstanceState.getBoolean(ERROR_REPORT_DONE_STATE, false)) {
                showErrorReportDone();
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

        outState.putBoolean(ERROR_REPORT_DONE_STATE, errorReportDone);
    }

    /**
     * Post the error report.
     */
    private void postErrorReport() {
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String errorMessage = etErrorMessage.getText().toString();

        TaskListener feedbackListener = new TaskListener() {
            @Override
            public void callback(){
                showErrorReportDone();
            }
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long universityId = prefs.getLong(Constants.PREF_KEY_UNIVERSITY_ID, -1);
        long ruleId = prefs.getLong(Constants.PREF_KEY_RULE_ID, -1);

        new SendFeedback(feedbackListener, getActivity(), name, email, errorMessage, "error", universityId + "/" + ruleId).execute();
    }

    /**
     * Checks if the error message is not empty and validates the email address, if present.
     *
     * @return true if input is correct
     */
    private boolean validateInput() {
        boolean inputCorrect = true;

        if (TextUtils.isEmpty(etErrorMessage.getText().toString())) {
            etErrorMessage.setError(getResources().getString(R.string.error_message_not_empty));
            inputCorrect = false;
        }

        String email = etEmail.getText().toString();
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getResources().getString(R.string.invalid_email_address));
            inputCorrect = false;
        }

        return inputCorrect;
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
     * Show error message by given error type.
     *
     * @param errorType - ErrorEvent.ErrorType
     */
    private void showError(ErrorEvent.ErrorType errorType) {
        actErrorType = errorType;
        progressWheel.setVisibility(View.GONE);
        btnReportError.setVisibility(View.VISIBLE);

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
     * Show text view that indicates that the report was successfully sent.
     */
    private void showErrorReportDone() {
        actErrorType = null;
        errorReportDone = true;

        progressWheel.setVisibility(View.GONE);
        btnReportError.setVisibility(View.GONE);
        tvStatus.setText(getString(R.string.error_report_done));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}