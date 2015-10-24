package de.mygrades.view;

import android.support.design.widget.Snackbar;
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
     * @param errorEvent Error Event which holds the information about the concrete Error
     * @param tryAgainListener Button
     * @param goToFaqListener Button
     */
    public static void displayErrorMessage(View view, ErrorEvent errorEvent, View.OnClickListener tryAgainListener, View.OnClickListener goToFaqListener) {
        switch (errorEvent.getType()) {
            case TIMEOUT:
                showSnackbar(view, "Zeitüberschreitung", tryAgainListener, "Nochmal");
                break;
            case NO_NETWORK:
                showSnackbar(view, "Keine Internetverbindung", tryAgainListener, "Nochmal");
                break;
            case GENERAL:
            default:
                showSnackbar(view, "Allgemeiner Fehler", goToFaqListener, "Was ist das?");
        }
    }
}
