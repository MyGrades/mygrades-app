package de.mygrades.view.activity;

import android.support.v4.app.Fragment;

/**
 * Interface which declares a method to replace a fragment.
 */
public interface ReplacableFragment {

    /**
     * Replaces a fragment.
     * If animate==true, a bottom-to-top slide animation will be shown.
     *
     * @param resLayoutId frame layout id, whose content should be replaced
     * @param newFragment new fragment
     * @param animate true, if the replacement should be animated
     */
    void replaceFragment(int resLayoutId, Fragment newFragment, boolean animate);
}
