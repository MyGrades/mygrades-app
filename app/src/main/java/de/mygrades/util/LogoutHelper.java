package de.mygrades.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.view.activity.SelectUniversityActivity;

/**
 * LogoutHelper shows an AlertDialog to let the user choose, if he wants to logout or not.
 */
public class LogoutHelper {

    private Context context;

    public LogoutHelper(Context context) {
        this.context = context;
    }

    /**
     * Shows the logout dialog.
     */
    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.dialog_logout_message);
        builder.setTitle(context.getString(R.string.dialog_logout_title));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Logout user and goes to SelectUniversityActivity.
     * The user cannot return to the MainActivity by pressing the back-button.
     */
    private void logout() {
        MainServiceHelper mainServiceHelper = new MainServiceHelper(context);
        mainServiceHelper.logout();

        Intent intent = new Intent(context, SelectUniversityActivity.class);
        // set flags, so the user won't be able to go back to the main activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
