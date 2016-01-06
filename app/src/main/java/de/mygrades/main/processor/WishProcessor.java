package de.mygrades.main.processor;

import android.content.Context;

import de.greenrobot.event.EventBus;
import de.mygrades.BuildConfig;
import de.mygrades.main.events.ErrorEvent;
import de.mygrades.main.events.PostWishDoneEvent;
import retrofit.RetrofitError;

/**
 * WishProcessor is used to send university wishes from users to the server.
 */
public class WishProcessor extends BaseProcessor {
    private static final String TAG = WishProcessor.class.getSimpleName();

    public WishProcessor(Context context) {
        super(context);
    }

    /**
     * Posts an university wish to the server.
     *
     * @param universityName - university name
     * @param name - name
     * @param email - email address
     * @param message - message
     */
    public void postWish(String universityName, String name, String email, String message) {
        // No Connection -> event no Connection, abort
        if (!isOnline()) {
            postErrorEvent(ErrorEvent.ErrorType.NO_NETWORK, "No Internet Connection!");
            return;
        }

        try {
            // create wish
            Wish wish = new Wish();
            wish.setUniversityName(universityName);
            wish.setName(name);
            wish.setEmail(email);
            wish.setMessage(message);
            wish.setAppVersion(BuildConfig.VERSION_NAME);

            // post to server
            restClient.getRestApi().postWish(wish);

            // post event
            EventBus.getDefault().post(new PostWishDoneEvent());
        } catch (RetrofitError e) {
            postRetrofitError(TAG, e);
        }
    }

    /**
     * POJO for a wish to be send via post to server.
     */
    public class Wish {
        private String universityName;
        private String name;
        private String message;
        private String email;
        private String appVersion;

        public String getUniversityName() {
            return universityName;
        }

        public void setUniversityName(String universityName) {
            this.universityName = universityName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }
    }
}
