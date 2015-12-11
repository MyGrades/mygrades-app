package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.ErrorEvent;
import de.mygrades.main.events.ErrorReportDoneEvent;

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

        btnReportError.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    progressWheel.setVisibility(View.VISIBLE);
                    btnReportError.setVisibility(View.GONE);
                    tvStatus.setText("");
                    postErrorReport();
                }
            }
        });

        // register event bus
        EventBus.getDefault().register(this);
    }

    /**
     * Post the error report.
     */
    private void postErrorReport() {
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String errorMessage = etErrorMessage.getText().toString();

        mainServiceHelper.postErrorReport(name, email, errorMessage);
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
     * Receive the ErrorReportDoneEvent, after the post was successful.
     *
     * @param errorReportDoneEvent - ErrorReportDoneEvent
     */
    public void onEventMainThread(ErrorReportDoneEvent errorReportDoneEvent) {
        progressWheel.setVisibility(View.GONE);
        tvStatus.setText(getString(R.string.error_report_done));
    }

    /**
     * Receive error events and show error message.
     *
     * @param errorEvent - ErrorEvent
     */
    public void onEventMainThread(ErrorEvent errorEvent) {
        progressWheel.setVisibility(View.GONE);
        btnReportError.setVisibility(View.VISIBLE);

        String errorMessage;
        switch (errorEvent.getType()) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
