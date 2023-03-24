package de.falcon.browser.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

import java.util.Objects;

import de.baumann.browser.R;
import de.falcon.browser.preferences.BasePreferenceFragment;

public class Fragment_settings_UI extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.preference_ui, rootKey);
        Context context = getContext();
        assert context != null;
        initSummary(getPreferenceScreen());
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            if (p.getSummaryProvider() == null) p.setSummary(listPref.getEntry());
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (Objects.requireNonNull(p.getTitle()).toString().toLowerCase().contains("password")) {
                p.setSummary("******");
            } else {
                p.setSummary(editTextPref.getText());
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sp, String key) {
        if (key.equals("sp_exit") || key.equals("sp_toggle") || key.equals("sp_add") || key.equals("sp_theme")
                || key.equals("nav_position") || key.equals("sp_hideOmni") || key.equals("start_tab") || key.equals("sp_hideSB")
                || key.equals("overView_place") || key.equals("overView_hide") || key.equals("hideToolbar") || key.equals("useDynamicColor")) {
            sp.edit().putInt("restart_changed", 1).apply();
        }
        updatePrefSummary(findPreference(key));
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getPreferenceScreen().getSharedPreferences()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Objects.requireNonNull(getPreferenceScreen().getSharedPreferences()).unregisterOnSharedPreferenceChangeListener(this);
    }
}
