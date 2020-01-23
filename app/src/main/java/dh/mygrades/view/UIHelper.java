package dh.mygrades.view;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import dh.mygrades.R;
import dh.mygrades.main.events.ErrorEvent;

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
            Snackbar snackbar = Snackbar
                    .make(view, text, Snackbar.LENGTH_LONG)
                    .setAction(actionText, action)
                    .setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimary));

            // change text color
            View snackbarView = snackbar.getView();
            TextView textView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);

            snackbar.show();
        }
    }

    /**
     * Shows a Snackbar with a given text on the given view.
     *
     * @param view view where the snackbar should be shown
     * @param text text to show
     */
    public static void showSnackbar(View view, String text) {
        showSnackbar(view, text, null, null);
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


    public enum STATE {AN, BE, NB, EN}

    /**
     * Evaluates the state string
     * @param state string to evaluate
     * @return a {@link STATE} or null
     */
    public static @Nullable STATE evaluateStateString(@Nullable String state){
        if (state == null) return null;
        switch (state.toLowerCase()){
            case "an":
            case "angemeldet":
            case "registered":
                return STATE.AN;
            case "be":
            case "bestanden":
            case "passed":
            case "pass":
            case "ak":
            case "anerkannt":
            case "acknowledged":
                return STATE.BE;
            case "nb":
            case "nicht bestanden":
            case "fail":
            case "failed":
            case "ta":
            case "täuschung":
            case "cheating":
                return STATE.NB;
            case "en":
            case "endgültig nicht bestanden":
            case "failed in the final attempt":
                return STATE.EN;
            default:
                return null;
        }
    }

    /**
     * Returns short state string
     * @param state any state string
     * @return shortened state string
     */
    public static @Nullable String getShortState(@NonNull Context context, @Nullable String state){
        STATE s = evaluateStateString(state);
        if (s == null) return state;
        switch (s){
            case AN:
                return context.getString(R.string.state_an);
            case BE:
                return context.getString(R.string.state_be);
            case NB:
                return context.getString(R.string.state_nb);
            case EN:
                return context.getString(R.string.state_en);
        }
        return null;
    }

    /**
     * Returns the color (passed/failed/default) for the given grade and state
     *
     * @param context a context to get the color resource
     * @param grade grade to evaluate or null
     * @param state state to evaluate or null
     * @return color int
     */
    public static @ColorInt int getGradeColor(@NonNull Context context, @Nullable Double grade,
                                              @Nullable String state){
        Integer gradeColor = null;
        STATE s = evaluateStateString(state);
        if (s != null) {
            // evaluate state
            switch (s){
                case BE:
                    gradeColor = R.color.text_passed;
                    break;
                case NB:
                    gradeColor = R.color.text_failed;
                    break;
                case EN:
                    gradeColor = R.color.text_failed_permanently;
                    break;
            }

        }
        if (gradeColor == null && grade != null){
            // evaluate grade instead
            gradeColor = grade <= 4 ? R.color.text_passed : R.color.text_failed;
        }
        if (gradeColor == null){
            // default fallback
            gradeColor = R.color.text_default;
        }

        return context.getResources().getColor(gradeColor);
    }
}
