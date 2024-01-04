package com.sandvik.databearerdev.gui.settings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import com.google.android.material.snackbar.Snackbar;
import com.sandvik.databearerdev.AppSettings;
import com.sandvik.databearerdev.IoTHUBClient;
import com.sandvik.databearerdev.R;
import com.sandvik.databearerdev.gui.main.MainActivity;
import com.sandvik.databearerdev.services.ftp.FsService;
import com.sandvik.databearerdev.util.Logger;
import com.sandvik.databearerdev.util.NetUtils;
import java.net.InetAddress;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{

    private Handler mHandler = new Handler();
    SwitchPreferenceCompat runningPref;
    private View rootView;
    private Snackbar snackbar;
    private String[] messages;
    private int currentIndex;
    private Handler handler;
    private EditTextPreference mPassWordPref;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.xpreferences, rootKey);
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireContext());
        Resources resources = getResources();



        runningPref = findPreference("running_switch");





        runningPref.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                startServer();
            } else {
                stopServer();
            }
            updateRunningEState();
            return true;
        });
        EditTextPreference serverUrl_pref = findPreference("server_url");
        serverUrl_pref.setOnPreferenceChangeListener(((preference, newValue) -> {
            if (!IoTHUBClient.isValidUrlBase((String)newValue)) {
                if (AppSettings.getServerType() == AppSettings.ServerType.Optimine) {
                    Toast.makeText(requireContext(), "Optimine Server URL must be valid url without port or path.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(requireContext(), "Server URL must be valid url address.", Toast.LENGTH_LONG).show();
                }
                return false;
            }
            return true;
        }));
        EditTextPreference ssidPref = findPreference("ssid");
        ssidPref.setOnPreferenceChangeListener((preference, newValue) -> {


            return true;
        });









        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PreferenceCategory hotspotSetting = findPreference("hotspot_set");
            hotspotSetting.setEnabled(false);
            hotspotSetting.setSummary(R.string.hotspotsettings_android_oreo_info);
        }


    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == runningPref) {
            boolean isChecked = runningPref.getSharedPreferences().getBoolean(runningPref.getKey(), false);

            if (isChecked) {

                String[] OnSuccessCalls = {"com.sandvik.databeaerer.ACTION_START_FTPSERVER","Server is not running (null serverThread)","Creating server thread","Server thread running","Taking wifi lock","Taking full wake lock","Ftp Server up and running, broadcasting ACTION_STARTED","onReceive broadcast: com.sandvik.databearerdev.FTPSERVER_STARTED","Setting up the notification","Notification setup done","FTP server started"};
                if (snackbar != null && snackbar.isShown()) {
                    snackbar.dismiss();
                }
               showSnackbarWithUpdates(rootView,OnSuccessCalls);


            } else {

                String[] OffSuccessCalls = {"Received: com.sandvik.databearerdev.ACTION_STOP_FTPSERVER","Stopping server","Thread interrupted","Exiting cleanly, returning from run()","serverThread join()ed ok","Releasing wifi lock","Releasing wake lock"," Clearing the notifications","Cleared notification"};
                if (snackbar != null && snackbar.isShown()) {
                    snackbar.dismiss();
                }
                showSnackbarWithUpdates(rootView,OffSuccessCalls);

            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void startServer() {
        requireContext().sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
    }

    private void stopServer() {
        requireContext().sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
    }

    private void updateRunningState() {
        Resources res = getResources();
        SwitchPreferenceCompat runningPref = findPreference("running_switch");
        if (FsService.isRunning()) {
            runningPref.setChecked(true);
            // Fill in the FTP server address
            InetAddress address = NetUtils.getLocalInetAddress();
            if (address == null) {
                Logger.v("From Settings:", "Unable to retrieve wifi ip address");
                runningPref.setSummary(R.string.cant_get_url);
                requireContext().sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
                return;
            }
            String iptext = "ftp://" + address.getHostAddress() + ":"
                    + AppSettings.getPortNumber() + "/";
            String summary = res.getString(R.string.running_summary_started, iptext);
            runningPref.setSummary(summary);
        } else {
            //
            runningPref.setChecked(false);
            runningPref.setSummary(R.string.running_summary_stopped);

        }
    }
    private void updateRunningEState() {
        Resources res = getResources();
        SwitchPreferenceCompat runningPref = findPreference("running_switch");
        if (FsService.isRunning()) {
            runningPref.setChecked(true);
            // Fill in the FTP server address
            InetAddress address = NetUtils.getLocalInetAddress();
            if (address == null) {
                Logger.v("From Settings:", "Unable to retrieve wifi ip address");
                runningPref.setSummary(R.string.cant_get_url);
                requireContext().sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
                return;
            }
            runningPref.setSummary(R.string.running_summary_stopped);
        } else {

            runningPref.setChecked(false);
            InetAddress address = NetUtils.getLocalInetAddress();
            String iptext = "ftp://" + address.getHostAddress() + ":"
                    + AppSettings.getPortNumber() + "/";
            String summary = res.getString(R.string.running_summary_started, iptext);
            runningPref.setSummary(summary);

        }
    }
    BroadcastReceiver mFsActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.v("From Settings:", "action received: " + intent.getAction());
            // remove all pending callbacks
            mHandler.removeCallbacksAndMessages(null);
            // action will be ACTION_STARTED or ACTION_STOPPED
            updateRunningState();
            // or it might be ACTION_FAILEDTOSTART
            final SwitchPreferenceCompat runningPref = findPreference("running_switch");
            if (intent.getAction().equals(FsService.ACTION_FAILEDTOSTART)) {
                runningPref.setChecked(false);
                mHandler.postDelayed(
                        () -> runningPref.setSummary(R.string.running_summary_failed),
                        100);
                mHandler.postDelayed(
                        () -> runningPref.setSummary(R.string.running_summary_stopped),
                        5000);
            }
        }
    };
    private void updateServerInfo() {

        ListPreference serverTypePref = findPreference("server_type");
        String serverType = AppSettings.getServerType().toString();
        serverTypePref.setSummary(serverType);

        EditTextPreference ssidPref = findPreference("server_url");
        ssidPref.setSummary(AppSettings.getServerUrl());

        MainActivity.refreshConnectionLabel();
    }





    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
    }

   /* private void performSomeTask(boolean isToggledOn) {
        // Simulate some task or method call
        // Update the text of the Snackbar dynamically based on the task result
        String newText;
        if (isToggledOn) {
            // Perform task when toggled on
            newText = "Switched on!";
        } else {
            // Perform task when toggled off
            newText = "Switched off!";
        }
        updateSnackbarText(newText);
    }*/



    @Override
    public void onResume() {
        super.onResume();
        updateRunningState();

        Logger.d("From Settings:", "onResume: Register the preference change listner");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sp.registerOnSharedPreferenceChangeListener(this);

        Logger.d("From Settings:", "onResume: Registering the FTP server actions");
        IntentFilter filter = new IntentFilter();
        filter.addAction(FsService.ACTION_STARTED);
        filter.addAction(FsService.ACTION_STOPPED);
        filter.addAction(FsService.ACTION_FAILEDTOSTART);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mFsActionsReceiver, filter);
    }

    @Override
    public void onPause() {

        super.onPause();
        Logger.v("From Settings:", "onPause: Unregistering the FTPServer actions");
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mFsActionsReceiver);

        Logger.d("From Settings:", "onPause: Unregistering the preference change listener");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        updateRunningState();
    }

    private void updateSnackbarText(String newText) {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.setText(newText);
            snackbar.show();
        }
    }

    private void showSnackbarWithUpdates(View view, String[] messages) {
        if (messages == null || messages.length == 0) {
            return;
        }

        this.messages = messages;
        currentIndex = 0;


        snackbar = Snackbar.make(view, messages[0], 7000).setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        snackbar.show();


        handler = new Handler();
        updateSnackbarText();
    }

    private void updateSnackbarText() {
        currentIndex++;
        if (currentIndex < messages.length) {
            snackbar.setText(messages[currentIndex]);
            handler.postDelayed(this::updateSnackbarText, 300);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //updateRunningState();
        updateServerInfo();

    }
}
