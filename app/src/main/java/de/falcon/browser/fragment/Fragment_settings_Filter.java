package de.falcon.browser.fragment;

import android.os.Bundle;

import de.baumann.browser.R;
import de.falcon.browser.preferences.BasePreferenceFragment;

public class Fragment_settings_Filter extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_filter, rootKey);
    }
}