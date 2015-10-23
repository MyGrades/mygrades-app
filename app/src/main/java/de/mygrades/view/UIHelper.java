package de.mygrades.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import de.mygrades.main.events.ErrorEvent;

/**
 * Helper methods for UI.
 */
public class UIHelper {

    /**
     * Shows a Snackbar with a given text and action on the given view.
     *
     * @param view view where the snackbar should be shown
     * @param text text to show
     * @param action OnClickListener
     * @param actionText text for the OnClickListener
     */
    public static void showSnackbar(View view, String text, View.OnClickListener action, String actionText) {
        if (view != null) {
            Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                    .setAction(actionText, action)
                    .show();
        }
    }

    /**
     * Evaluates the Error from errorEvent and displays specific message.
     *
     * @param view view where the error should be shown
     * @param context Context
     * @param errorEvent Error Event which holds the information about the concrete Error
     * @param tryAgainListener Button
     */
    public static void displayErrorMessage(View view, Context context, ErrorEvent errorEvent, View.OnClickListener tryAgainListener) {
        switch (errorEvent.getType()) {
            case TIMEOUT:
                showSnackbar(view, "Zeitüberschreitung", tryAgainListener, "Nochmal");
                break;
            case NO_NETWORK:
                showSnackbar(view, "Keine Internetverbindung", tryAgainListener, "Nochmal");
                break;
            case GENERAL:
            default:
                String title = "Fehler beim Abrufen der Noten";
                String text = "Deine Noten konnten nicht abgerufen werden. \n" +
                        "Das kann verschiedene Gründe haben: \n" +
                        "1. Deine Zugangsdaten sind falsch. \n" +
                        "2. Probleme mit der Internetverbindung oder dem Server der Hochschule. \n" +
                        "3. Die Linkstruktur innerhalb deines Notensystems könnte sich geändert haben" +
                        " und so ist es zur Zeit nicht möglich deine Noten abzurufen.";
                // TODO: remove context and AlertDialog
                if (context != null) {
                    new AlertDialog.Builder(context)
                            .setTitle(title)
                            .setMessage(text)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
        }
    }
}
