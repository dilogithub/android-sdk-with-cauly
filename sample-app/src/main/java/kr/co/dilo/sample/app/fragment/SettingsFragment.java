package kr.co.dilo.sample.app.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.*;
import kr.co.dilo.sample.app.R;
import kr.co.dilo.sdk.DiloConst;

public class SettingsFragment extends PreferenceFragmentCompat {

    SharedPreferences prefs;
    EditTextPreference companionWidth;
    EditTextPreference companionHeight;
    ListPreference productType;
    ListPreference fillType;
    EditTextPreference duration;
    EditTextPreference packageName;
    EditTextPreference epiCode;
    SwitchPreference target;
    SwitchPreference companionSize;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);

        companionWidth = findPreference("companion_width");
        companionHeight = findPreference("companion_height");
        productType = findPreference("product_type");
        fillType = findPreference("fill_type");
        duration = findPreference("duration");
        packageName = findPreference("package_name");
        epiCode = findPreference("epi_code");
        target = findPreference("target");
        companionSize = findPreference("companion_size");

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if(!prefs.getString("companion_width", "").equals("")) {
            companionWidth.setSummary(prefs.getString("companion_width", "300"));
        }

        if(!prefs.getString("companion_height", "").equals("")) {
            companionHeight.setSummary(prefs.getString("companion_height", "300"));
        }

        if(!prefs.getString("product_type", "").equals("")){
            productType.setSummary(prefs.getString("product_type", "DILO_PLUS_ONLY"));
        }

        if(!prefs.getString("fill_type", "").equals("")){
            fillType.setSummary(prefs.getString("fill_type", "MULTI"));
        }

        if(!prefs.getString("duration", "").equals("")){
            duration.setSummary(prefs.getString("duration", "15"));
        }

        if(!prefs.getString("package_name", "").equals("")){
            packageName.setSummary(prefs.getString("package_name", "300"));
        }

        if(!prefs.getString("epi_code", "").equals("")){
            epiCode.setSummary(prefs.getString("epi_code", "300"));
        }

        target.setSummary(prefs.getBoolean("target", false)? DiloConst.AD_TEST_URL : DiloConst.AD_URL);
        target.setTitle(prefs.getBoolean("target", false)? "테스트 서버":"운영 서버");

        boolean autoCompanionSize = prefs.getBoolean("companion_size", false);
        companionWidth.setEnabled(!autoCompanionSize);
        companionHeight.setEnabled(!autoCompanionSize);
        companionSize.setSummary(autoCompanionSize? "자동":"수동 설정");

        prefs.registerOnSharedPreferenceChangeListener(prefListener);

    }

    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("companion_width")){
                companionWidth.setSummary(prefs.getString("companion_width", "300"));
            }

            if(key.equals("companion_height")){
                companionHeight.setSummary(prefs.getString("companion_height", "300"));
            }

            if(key.equals("product_type")){
                productType.setSummary(prefs.getString("product_type", "DILO_PLUS_ONLY"));
            }

            if(key.equals("fill_type")){
                fillType.setSummary(prefs.getString("fill_type", "MULTI"));
            }

            if(key.equals("duration")){
                duration.setSummary(prefs.getString("duration", "15"));
            }

            if(key.equals("package_name")){
                packageName.setSummary(prefs.getString("package_name", "com.queen.sampleapp"));
            }

            if(key.equals("epi_code")){
                epiCode.setSummary(prefs.getString("epi_code", "test_live"));
            }

            if(key.equals("target")) {
                target.setSummary(prefs.getBoolean("target", false)? DiloConst.AD_TEST_URL : DiloConst.AD_URL);
                target.setTitle(prefs.getBoolean("target", false)? "테스트 서버":"운영 서버");
            }

            if(key.equals("companion_size")) {
                boolean autoCompanionSize = prefs.getBoolean("companion_size", false);
                companionWidth.setEnabled(!autoCompanionSize);
                companionHeight.setEnabled(!autoCompanionSize);
                companionSize.setSummary(autoCompanionSize? "자동":"수동 설정");
            }

        }
    };

}
