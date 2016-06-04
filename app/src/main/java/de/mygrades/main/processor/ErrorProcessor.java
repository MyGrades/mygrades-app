package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import de.greenrobot.event.EventBus;
import de.mygrades.BuildConfig;
import de.mygrades.main.events.ErrorEvent;
import de.mygrades.main.events.ErrorReportDoneEvent;
import de.mygrades.util.Constants;
import retrofit.RetrofitError;

/**
 * ErrorProcessor is responsible to send error reports from users to the server.
 */
public class ErrorProcessor extends BaseProcessor {
    private static final String TAG = ErrorProcessor.class.getSimpleName();

    public ErrorProcessor(Context context) {
        super(context);
    }

    /**
     * Posts an error to the server.
     *
     * @param name - name
     * @param email - email address
     * @param errorMessage - error message
     */
    public void postErrorReport(String name, String email, String errorMessage) {
        // No Connection -> event no Connection, abort
        if (!isOnline()) {
            postErrorEvent(ErrorEvent.ErrorType.NO_NETWORK, "No Internet Connection!");
            return;
        }

        try {
            // get university id
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            long universityId = prefs.getLong(Constants.PREF_KEY_UNIVERSITY_ID, -1);
            long ruleId = prefs.getLong(Constants.PREF_KEY_RULE_ID, -1);

            // create error report
            Error error = new Error();
            error.setName(name);
            error.setEmail(email);
            error.setMessage(errorMessage);
            error.setAppVersion(BuildConfig.VERSION_NAME);
            error.setUniversityId(universityId);
            error.setRuleId(ruleId);
            error.setAndroidVersion(Build.VERSION.RELEASE + ", " + Build.VERSION.SDK_INT);
            error.setDevice(Build.MANUFACTURER + ", " + Build.MODEL);

            // post to server
            restClient.getRestApi().postError(error);

            // post event
            EventBus.getDefault().post(new ErrorReportDoneEvent());
        } catch (RetrofitError e) {
            postRetrofitError(TAG, e);
        }
    }

    /**
     * POJO for an error to be send via post to server.
     */
    public class Error {
        private String name;
        private String email;
        private String message;
        private long universityId;
        private String appVersion;
        private long ruleId;
        private String androidVersion;
        private String device;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getUniversityId() {
            return universityId;
        }

        public void setUniversityId(long universityId) {
            this.universityId = universityId;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public long getRuleId() {
            return ruleId;
        }

        public void setRuleId(long ruleId) {
            this.ruleId = ruleId;
        }

        public String getAndroidVersion() {
            return androidVersion;
        }

        public void setAndroidVersion(String androidVersion) {
            this.androidVersion = androidVersion;
        }

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }
    }
}
