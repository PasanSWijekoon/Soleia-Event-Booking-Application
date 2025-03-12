package com.strawhats.soleia.Models;
import android.content.Context;
import android.content.SharedPreferences;
public class PrefManager {
    private static final String PREF_NAME = "onboarding_pref";
    private static final String IS_FIRST_TIME = "IsFirstTime";

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    public PrefManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME, isFirstTime);
        editor.apply();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME, true);
    }
}
