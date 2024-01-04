package com.sandvik.databearerdev.gui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.sandvik.databearerdev.App;
import com.sandvik.databearerdev.AppSettings;
import com.sandvik.databearerdev.R;
import com.sandvik.databearerdev.services.ftp.FsService;
import com.sandvik.databearerdev.util.Logger;

public class LoginFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.xpreference_login, rootKey);

        EditTextPreference usernamePref = findPreference("username");
        usernamePref.setOnPreferenceChangeListener((preference, newValue) -> {
            String newUsername = (String) newValue;
            if (preference.getSummary().equals(newUsername))
                return false;
            if (!newUsername.matches("[a-zA-Z0-9]+")) {
                Toast.makeText(requireContext(),
                        R.string.username_validation_error, Toast.LENGTH_LONG).show();
                return false;
            }
            stopServer();
            return true;
        });
        EditTextPreference mPassWordPref = findPreference("password");
        mPassWordPref.setOnPreferenceChangeListener((preference, newValue) -> {
            stopServer();
            return true;
        });

    }

    private void updateSummary() {

        String username = AppSettings.getUserName();
        String password = AppSettings.getPassWord();

        Logger.v("From Settings:", "Updating login summary");
        Preference loginPreference = findPreference("login");
        loginPreference.setSummary(username + " : " + transformPassword(password));



        EditTextPreference usernamePref = findPreference("username");
        usernamePref.setSummary(username);

        EditTextPreference passWordPref = findPreference("password");
        passWordPref.setSummary(transformPassword(password));
    }
    static private String transformPassword(String password) {
        Context context = App.getAppContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Resources res = context.getResources();
        String showPasswordString = res.getString(R.string.show_password_default);
        boolean showPassword = showPasswordString.equals("true");
        showPassword = sp.getBoolean("show_password", showPassword);
        if (showPassword)
            return password;
        else {
            StringBuilder sb = new StringBuilder(password.length());
            for (int i = 0; i < password.length(); ++i)
                sb.append('*');
            return sb.toString();
        }
    }
    private void startServer() {
        requireContext().sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
    }

    private void stopServer() {
        requireContext().sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
        updateSummary();
        return true;
    }
}
