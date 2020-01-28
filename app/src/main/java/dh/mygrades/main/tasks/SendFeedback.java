package dh.mygrades.main.tasks;

import android.content.Context;
import android.os.AsyncTask;

import dh.mygrades.BuildConfig;
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import io.sentry.event.UserBuilder;

public class SendFeedback extends AsyncTask<Object, Object, Void>
{
    private TaskListener taskListener;
    private Context context;
    private String name;
    private String email;
    private String message;
    private String type;
    private String university;


    public SendFeedback(TaskListener  taskListener, Context context, String name, String email, String message, String type, String university){
        this.taskListener = taskListener;
        this.context = context;
        this.name = name;
        this.email = email;
        this.message = message;
        this.type = type;
        this.university = university;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();


    }
    @Override
    protected Void doInBackground(Object... params) {

        Sentry.init("https://9d668d239d8c435182b234391c0a1ce6@sentry.io/2014696?release=" + BuildConfig.VERSION_CODE, new AndroidSentryClientFactory(context));

        Sentry.getContext().setUser(
                new UserBuilder().setEmail(email).setUsername(name).withData("universtity", university).withData("type", type).build()
        );
        Sentry.capture(message);
        Sentry.close();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        taskListener.callback();
    }
}


