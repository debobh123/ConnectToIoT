package com.sandvik.databearerdev.gui.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.sandvik.databearerdev.AppSettings;
import com.sandvik.databearerdev.R;
import com.sandvik.databearerdev.gui.FolderPickerDialogBuilder;
import com.sandvik.databearerdev.services.ftp.FsService;

import java.io.File;

public class AdvancedSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.xpreference_advanced_settings, rootKey);
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireContext());
        Resources resources = getResources();

        EditTextPreference portnum_pref = findPreference("portNum");
        portnum_pref.setSummary(sp.getString("portNum",
                resources.getString(R.string.portnumber_default)));
        portnum_pref.setOnPreferenceChangeListener((preference, newValue) -> {
            String newPortnumString = (String) newValue;
            if (preference.getSummary().equals(newPortnumString))
                return false;
            int portnum = 0;
            try {
                portnum = Integer.parseInt(newPortnumString);
            } catch (Exception e) {
            }
            if (portnum <= 0 || 65535 < portnum) {
                Toast.makeText(requireContext(),
                        R.string.port_validation_error, Toast.LENGTH_LONG).show();
                return false;
            }
            preference.setSummary(newPortnumString);
            stopServer();
            return true;
        });
        Preference chroot_pref = findPreference("chrootDir");
        chroot_pref.setSummary(AppSettings.getChrootDirAsString());
        chroot_pref.setOnPreferenceClickListener(preference -> {
            AlertDialog folderPicker = new FolderPickerDialogBuilder(requireContext(), AppSettings.getChrootDir())
                    .setSelectedButton(R.string.select, path -> {
                        if (preference.getSummary().equals(path))
                            return;
                        if (!AppSettings.setChrootDir(path))
                            return;
                        final File root = new File(path);
                        if (!root.canRead()) {
                            Toast.makeText(requireContext(),
                                    "Notice that we can't read/write in this folder.",
                                    Toast.LENGTH_LONG).show();
                        } else if (!root.canWrite()) {
                            Toast.makeText(requireContext(),
                                    "Notice that we can't write in this folder, reading will work. Writing in subfolders might work.",
                                    Toast.LENGTH_LONG).show();
                        }

                        preference.setSummary(path);
                        stopServer();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create();
            folderPicker.show();
            return true;
        });
        final CheckBoxPreference wakelock_pref = findPreference("stayAwake");
        wakelock_pref.setOnPreferenceChangeListener((preference, newValue) -> {
            stopServer();
            return true;
        });
    }
    private void startServer() {
        requireContext().sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
    }

    private void stopServer() {
        requireContext().sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
    }

}
