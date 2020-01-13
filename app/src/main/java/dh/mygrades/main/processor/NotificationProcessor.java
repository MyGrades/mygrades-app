package dh.mygrades.main.processor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import dh.mygrades.R;
import dh.mygrades.database.dao.GradeEntry;
import dh.mygrades.view.activity.MainActivity;

/**
 * Shows notifications if new grades are found or if old grades changed.
 */
public class NotificationProcessor extends BaseProcessor {
    private static final int NOTIFY_NEW_GRADES_ID = 0;
    private static final int NOTIFY_UPDATED_GRADES_ID = 1;

    public NotificationProcessor(Context context) {
        super(context);
    }

    /**
     * Shows notifications if new grades are found or if old grades changed.
     *
     * @param toInsert - list of new grade entries
     * @param toUpdate - list of updated grade entries
     */
    public void showNotificationForGrades(List<GradeEntry> toInsert, List<GradeEntry> toUpdate) {
        notifyNewGrades(toInsert);
        notifyUpdatedGrades(toUpdate);
    }

    /**
     * Shows a notification for new grades.
     * Multiple new grades will be summarized.
     *
     * @param toInsert - list of new grade entries
     */
    private void notifyNewGrades(List<GradeEntry> toInsert) {
        if (toInsert.size() == 1) {
            String title = context.getString(R.string.notification_new_grade_title);;
            String message = toInsert.get(0).getName();

            NotificationCompat.Builder notificationBuilder = getDefaultBuilder(title, message);
            notify(notificationBuilder, NOTIFY_NEW_GRADES_ID);
        } else if (toInsert.size() > 1) {
            String title = toInsert.size() + " " + context.getString(R.string.notification_new_grades_title);
            String message = joinNames(toInsert);

            NotificationCompat.Builder notificationBuilder = getDefaultBuilder(title, message);
            notificationBuilder.setNumber(toInsert.size());
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
            notify(notificationBuilder, NOTIFY_NEW_GRADES_ID);
        }
    }

    /**
     * Shows a notification for updated grades.
     * Multiple new grades will be summarized.
     *
     * @param toUpdate - list of updated grade entries
     */
    private void notifyUpdatedGrades(List<GradeEntry> toUpdate) {
        List<GradeEntry> updatedGrades = new ArrayList<>();

        // make sure that grade entries are loaded from database and not from cache
        daoSession.clear();

        // get grade entries where grade changed
        if (toUpdate.size() > 0) {
            for(GradeEntry gradeEntry : toUpdate) {
                Double oldGrade = daoSession.getGradeEntryDao().load(gradeEntry.getHash()).getGrade();
                Double newGrade = gradeEntry.getGrade();

                if (!oldGrade.equals(newGrade)) {
                    updatedGrades.add(gradeEntry);
                }
            }
        }

        if (updatedGrades.size() == 1) {
            String title = context.getString(R.string.notification_updated_grade_title);
            String message = updatedGrades.get(0).getName();

            NotificationCompat.Builder notificationBuilder = getDefaultBuilder(title, message);
            notify(notificationBuilder, NOTIFY_UPDATED_GRADES_ID);
        } else if (updatedGrades.size() > 1) {
            String title = updatedGrades.size() + " " + context.getString(R.string.notification_updated_grades_title);
            String message = joinNames(updatedGrades);

            NotificationCompat.Builder notificationBuilder = getDefaultBuilder(title, message);
            notificationBuilder.setNumber(updatedGrades.size());
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
            notify(notificationBuilder, NOTIFY_UPDATED_GRADES_ID);
        }
    }

    /**
     * Creates a NotificationCompat.Builder with all default settings.
     *
     * @param title - title
     * @param message - message
     * @return NotificationCompat.Builder
     */
    private NotificationCompat.Builder getDefaultBuilder(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        // get default notification sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.mygrades_logo_notification)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(message)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setVibrate(new long[]{0, 300})
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentIntent(pendingIntent);

        // show notification on lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        return notificationBuilder;
    }

    /**
     * Shows a notification.
     *
     * @param notificationBuilder - NotificationCompat.Builder
     * @param id - notification id
     */
    private void notify(NotificationCompat.Builder notificationBuilder, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notificationBuilder.build());
    }

    /**
     * Joins the names of grade entries with a comma.
     *
     * @param gradeEntries - list of grade entries
     * @return string
     */
    private String joinNames(List<GradeEntry> gradeEntries) {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < gradeEntries.size(); i++) {
            sb.append(gradeEntries.get(i).getName());
            if (i + 1 < gradeEntries.size()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
