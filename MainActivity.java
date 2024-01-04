package com.sandvik.databearerdev.gui.main;


import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sandvik.databearerdev.App;
import com.sandvik.databearerdev.AppSettings;
import com.sandvik.databearerdev.R;
import com.sandvik.databearerdev.gui.log.TransferLogController;
import com.sandvik.databearerdev.gui.log.TransferLogItem;
import com.sandvik.databearerdev.gui.log.TransferLogListView;
import com.sandvik.databearerdev.gui.settings.XSettingsActivity;
import com.sandvik.databearerdev.services.fdm.FDMService;
import com.sandvik.databearerdev.services.ftp.FsService;
import com.sandvik.databearerdev.storage.FileStorage;
import com.sandvik.databearerdev.storage.IFileStorageListener;
import com.sandvik.databearerdev.storage.OutboxFile;
import com.sandvik.databearerdev.util.Logger;
import com.sandvik.databearerdev.util.NetUtils;

import java.io.File;
import java.io.FileFilter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IFileStorageListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    private static Handler handler;    // Handlers for runnable
    private static boolean isActive = false;

    private static MachineController machineList = null;

    private static TransferLogController transferLogCtrl = null;

    private TextView uiPendingReportsValue;
    private static boolean isCloudTransferActive = false;

    private static TextView connectionTypeLabel;

    private static ImageView cloudTransferStateIcon;
    public static FrameLayout logViewFrame;

    public static TextView fdm_status;
    public static TextView ftp_status;
    public static TextView server_status;
    public static TextView battery_status;
    public static FloatingActionButton wifiDirectButton;

    static private final Thread.UncaughtExceptionHandler defaultExpHandler = Thread.getDefaultUncaughtExceptionHandler();


    private static final String PREFS_NAME = "MyAppPreferences";
    private static final String KEY_FIRST_INSTALLATION = "firstInstallation";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_SETTINGS)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS}, 121);
        }
        //changed this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FileStorage.createInstance(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
        } else {
            FileStorage.createInstance(Environment.getExternalStorageDirectory());
        }

        // Android 6.0 permission for write settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }


        if (isFirstInstallation(App.getAppContext())) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        } else {
            // Proceed with the regular flow
            setContentView(R.layout.rebrand_mainpage);
            // Other initialization code...
        }



        logViewFrame = findViewById(R.id.logViewFrame);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                Logger.e(TAG, "uncaughtException", e);
                Logger.scanLogs();
                defaultExpHandler.uncaughtException(thread, e);
            }
        });
        Logger.i(TAG, "onCreate()");

        // prompt user to disable battery optimizations for databearer.
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                //  Prompt the user to disable battery optimization
                Logger.i(TAG, "Battery optimizations are not disabled, prompting user.");
                Toast.makeText(this, R.string.battery_optimization_info_1, Toast.LENGTH_LONG).show();
                try {
                    startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS));
                } catch (ActivityNotFoundException e) {
                    Logger.e(TAG, "This device does not support automated battery optimization disable, take user to settings page.", e);
                    try {
                        startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                        Toast.makeText(this, R.string.battery_optimization_info_2, Toast.LENGTH_LONG).show();
                    } catch (ActivityNotFoundException e2) {
                        Logger.e(TAG, "This device does not support any kind of battery optimization disabling, nothing we can do.", e2);
                        Toast.makeText(this, R.string.battery_optimization_info_3, Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Logger.i(TAG, "Battery optimizations already disabled.");
            }
        }

        if (machineList == null) {
            machineList = new MachineController();
        }
        if (transferLogCtrl == null) {
            transferLogCtrl = new TransferLogController();
            addExistingFilesToTransferLog();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DataTransferListViewAdapter dataTransferViewAdapter = new DataTransferListViewAdapter(App.getAppContext());
        machineList.setListViewAdapter(this, dataTransferViewAdapter);
        ListView dataTransferList = findViewById(R.id.dataTransferListView);
        dataTransferList.setAdapter(dataTransferViewAdapter);

        TransferLogListView transferLogView = new TransferLogListView(App.getAppContext());
        transferLogCtrl.setParentView(this, transferLogView);
        ListView transferLogList = findViewById(R.id.logListView);
        transferLogList.setAdapter(transferLogView);






        uiPendingReportsValue = findViewById(R.id.pendingReportsValue);
        connectionTypeLabel = findViewById(R.id.connectionTypeHeader);

        FileStorage.instance().addListener(this);



        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        cloudTransferStateIcon = findViewById(R.id.cloudConnStateIcon);

        handler = new Handler();

        Logger.d(TAG, "Started Sandvik DataMule ver. " + App.getVersion());
        startfdmService();

        Button logButton = findViewById(R.id.logButton);
        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logViewFrame.setVisibility(View.VISIBLE);

            }
        });


        setCloudTransferActive(isCloudTransferActive);
        refreshConnectionLabel();
        onMachineMetaDataChanged(null);

        isActive = true;

        //app status feature
        fdm_status = findViewById(R.id.fdm_status);
        ftp_status = findViewById(R.id.ftp_status);
        server_status = findViewById(R.id.server_status);
        battery_status = findViewById(R.id.battery_status);

        if(GetRunningServices())
        {
            int textColor = Color.parseColor("#000000");
            fdm_status.setTextColor(textColor);
        } else if (!GetRunningServices()) {

            int textColor = Color.parseColor("#cccccc");
            fdm_status.setTextColor(textColor);
        }

        battery_status = findViewById(R.id.battery_status);

        // Register a BroadcastReceiver to listen for battery level changes
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);




        /*WifiDirectFeature*//*

        wifiDirectButton = findViewById(R.id.wifiDirectFloatingActionButton);
        wifiDirectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent wifiDirectIntent = new Intent(MainActivity.this, WifiDirectActivity.class);
                startActivity(wifiDirectIntent);

            }
        });*/


    }

    private static boolean isFirstInstallation(Context context) {
            SharedPreferences installationPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean firstInstallation = installationPreferences.getBoolean(KEY_FIRST_INSTALLATION, true);

            if (firstInstallation) {
                // Update the flag to indicate that the app has been installed
                installationPreferences.edit().putBoolean(KEY_FIRST_INSTALLATION, false).apply();
            }

            return firstInstallation;
    }

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPercentage = (int) ((level / (float) scale) * 100);

            battery_status.setText("Cell: " + batteryPercentage + "%");


            if (batteryPercentage <= 20) {
                battery_status.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                //startBlinkingAnimation();  *** Problem with animation not canceling once battery is above again
            } else if (batteryPercentage <= 50) {
                battery_status.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                battery_status.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        }
    };


    static public TransferLogController getTransferLogController() {
        return transferLogCtrl;
    }

    private void addExistingFilesToTransferLog() {

        // get all files from outbox and m2moutbox
        ArrayList<File> files = new ArrayList<File>();
        // get files from outbox
        new File(FileStorage.instance().getOutboxPath()).listFiles(new FileFilter() {
                                                                       @Override
                                                                       public boolean accept(File pathname) {
                                                                           if (pathname.isFile()) {
                                                                               files.add(pathname);
                                                                           }
                                                                           return false;
                                                                       }
                                                                   }
        );
        // get files from m2moutbox
        new File(FileStorage.instance().getM2MOutboxPath()).listFiles(new FileFilter() {
                                                                          @Override
                                                                          public boolean accept(File pathname) {
                                                                              if (pathname.isFile()) {
                                                                                  files.add(pathname);
                                                                              }
                                                                              return false;
                                                                          }
                                                                      }
        );

        // iterate received files in outbox and m2moutbox, add to transferlog with Received state.
        for (File file : files) {
            TransferLogItem i = transferLogCtrl.getOrAddFile(file.getName());
            if (i != null) {
                i.setState(TransferLogItem.State.Received);
            }
        }

        // iterate all files from Sent path and add to transferlog with Uploaded state.
        new File(FileStorage.instance().getSentFilesPath()).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile()) {
                    TransferLogItem i = transferLogCtrl.getOrAddFile(pathname.getName());
                    if (i != null) {
                        i.setState(TransferLogItem.State.Uploaded);
                    }
                }
                return false;
            }
        });

        transferLogCtrl.sortFiles();
        transferLogCtrl.cleanOldFiles();

    }

    @Override
    public void onBackPressed() {
        if (logViewFrame.getVisibility() == View.VISIBLE) {
            logViewFrame.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Logger.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onMachineMetaDataChanged(String machineSerial) {
        try {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    uiPendingReportsValue.setText(String.valueOf(FileStorage.instance().getPendingReportCount()));
                }
            });
        } catch (Exception ignored) {
        } // silently ignore, this may happen occasionally if Activity is closed at the same time we execute this.

        setCloudTransferActive(isCloudTransferActive);
    }

    public static void refreshConnectionLabel() {
        try {
            if (!isActive) return;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (AppSettings.getServerType() == AppSettings.ServerType.Optimine) {
                        connectionTypeLabel.setText(R.string.optimine_connection);
                    } else {
                        connectionTypeLabel.setText(R.string.iothub_connection);
                    }
                }
            });
        } catch (Exception ignored) {
        } // silently ignore, this may happen occasionally if Activity is closed at the same time we execute this.
    }

    public static void unableToChangeWifiState() {
        Logger.d(TAG, "Unable to change Wifi state");
        MainActivity.showWarning("You must allow DataBearer to change system settings.");
    }

    public static void noValidClock() {
        Logger.d(TAG, "No valid clock");
        MainActivity.showWarning("Clock time/date is invalid. Connect internet to synchronize or set manually.");
    }

    private static void showWarning(String msg) {
        try {
            if (!isActive) return;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(App.getAppContext(),
                            msg, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            });
        } catch (Exception ignored) {
        } // silently ignore, this may happen occasionally if Activity is closed at the same time we execute this.

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, XSettingsActivity.class);
                this.startActivity(intent);
                break;
            case R.id.action_app_guide:
                Intent intent_1 = new Intent(this, OnboardingActivity.class);
                this.startActivity(intent_1);
                break;
            case R.id.action_view_mule_log:
                 openDocumentsDirectory();


            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void openDocumentsDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("*/*");
        startActivityForResult(intent, 0);
    }

    public static void setCloudTransferActive(boolean active) {
        isCloudTransferActive = active;
        try {
            if (!isActive) return;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (isCloudTransferActive) {
                        cloudTransferStateIcon.setImageResource(R.drawable.baseline_send_receive_24);
                        cloudTransferStateIcon.setVisibility(View.VISIBLE);
                        server_status.setTextColor(Color.BLACK);
                    } else {
                        if (FileStorage.instance().getPendingReportCount() > 0) {
                            cloudTransferStateIcon.setImageResource(R.drawable.pending_black_100x100);
                            cloudTransferStateIcon.setVisibility(View.VISIBLE);
                            server_status.setTextColor(Color.BLACK);
                        } else {
                            cloudTransferStateIcon.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        } catch (Exception ignored) {
        } // silently ignore, this may happen occasionally if Activity is closed at the same time we execute this.
    }

    private static void refreshMachineListUI() {
        try {
            if (!isActive) return;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    machineList.refreshUI();
                }
            });
        } catch (Exception ignored) {
        } // silently ignore, this may happen occasionally if Activity is closed at the same time we execute this.
    }

    public static void refreshTransferLogUI() {
        try {
            if (!isActive) return;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    transferLogCtrl.refreshUI();
                }
            });
        } catch (Exception ignored) {
        } // silently ignore, this may happen occasionally if Activity is closed at the same time we execute this.
    }

    public static void ftpDeviceConnected(String ip) {
        MachineState m = machineList.addMachineIP(ip);
        m.setConnectionState(MachineState.ConnectionState.Idle);
        refreshMachineListUI();
    }

    public static void ftpDeviceDisconnected(String ip) {
        String serial = machineList.getMachineSerial(ip);
        if (serial != null) {
            MachineState m = machineList.getMachine(serial);
            if (m != null) {
                m.setConnectionState(MachineState.ConnectionState.Disconnected);
                refreshMachineListUI();
            }
        }
    }

    public static void ftpFileReceiveStarted(String ip, String filename) {
        OutboxFile.OutboxFileMetaData fileInfo = OutboxFile.parseFileName(filename);
        if (fileInfo.IsValid) {
            MachineState m = machineList.associateMachineSerial(ip, fileInfo.DeviceName);
            m.setConnectionState(MachineState.ConnectionState.Receiving);
            refreshMachineListUI();

            TransferLogItem i = transferLogCtrl.getOrAddFile(filename);
            if (i != null) {
                i.setState(TransferLogItem.State.FtpDownload);
                refreshTransferLogUI();
            }
        }
    }

    public static void ftpFileReceiveCompleted(String ip, String filename, boolean succeeded) {
        OutboxFile.OutboxFileMetaData fileInfo = OutboxFile.parseFileName(filename);
        if (fileInfo.IsValid) {
            MachineState m = machineList.associateMachineSerial(ip, fileInfo.DeviceName);
            m.setConnectionState(MachineState.ConnectionState.Idle);
            refreshMachineListUI();

            TransferLogItem i = transferLogCtrl.getOrAddFile(filename);
            if (i != null) {
                i.setState(TransferLogItem.State.Received);
                refreshTransferLogUI();
            }
        }
    }

    private void updateRunningState() {
        Resources res = getResources();
        ftp_status = findViewById(R.id.ftp_status);
        if (FsService.isRunning()) {
            // Fill in the FTP server address
            InetAddress address = NetUtils.getLocalInetAddress();
            if (address == null) {
                Logger.v(TAG, "Unable to retrieve wifi ip address");
                return;
            }
            String ipText = "ftp://" + address.getHostAddress() + ":"
                    + AppSettings.getPortNumber() + "/";
            String summary = res.getString(R.string.running_summary_started, ipText);

            int textColor = Color.parseColor("#000000");
            ftp_status.setTextColor(textColor);
        }

        else
        {
            int textColor = Color.parseColor("#cccccc");
            ftp_status.setTextColor(textColor);
        }

    }

    /**
     * This receiver will check FTPServer.ACTION* messages and will update the button,
     * running_state, if the server is running and will also display at what url the
     * server is running.
     */
    private final BroadcastReceiver mFsActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.v(TAG, "action received: " + intent.getAction());
            // remove all pending callbacks
            handler.removeCallbacksAndMessages(null);
            // action will be ACTION_STARTED or ACTION_STOPPED
            updateRunningState();
            // or it might be ACTION_FAILEDTOSTART
            if (intent.getAction().equals(FsService.ACTION_FAILEDTOSTART)) {
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        updateRunningState();

        Logger.d(TAG, "onResume: Registering the FTP server actions");
        IntentFilter filter = new IntentFilter();
        filter.addAction(FsService.ACTION_STARTED);
        filter.addAction(FsService.ACTION_STOPPED);
        filter.addAction(FsService.ACTION_FAILEDTOSTART);
        registerReceiver(mFsActionsReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        startfdmService();
        Logger.v(TAG, "onPause: Unregistering the FTPServer actions");
        unregisterReceiver(mFsActionsReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.i(TAG, "onDestroy()");
        isActive = false;
        FileStorage.instance().removeListener(this);
        startfdmService();
        Logger.scanLogs();
        unregisterReceiver(batteryReceiver);


    }

    @Override
    protected void onStop() {
        super.onStop();
        startfdmService();
    }

    private void startfdmService() {

        if (GetRunningServices()) {
            Logger.d(TAG, "Service State: Running");
            return;
        }
        // Start the service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(getBaseContext(), FDMService.class));
        } else {
            startService(new Intent(getBaseContext(), FDMService.class));
        }
        Logger.d(TAG, "Service State: Running");
    }

    private void stopfdmService() {
        // We are already running the babyapp service
        if (GetRunningServices()) {
            Logger.d(TAG, "Service State: Running");
            stopService(new Intent(getBaseContext(), FDMService.class));
        }
    }

    private boolean GetRunningServices() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals("com.sandvik.databearerdev.services.fdm.FDMService")) {
                Log.d("GetRunStatChk", "FDMservice is already running");
                return true;
            }
        }
        return false;
    }

    private void startBlinkingAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofInt(battery_status, "textColor", getResources().getColor(R.color.Alert), getResources().getColor(android.R.color.transparent));
        anim.setDuration(500);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(ObjectAnimator.REVERSE);
        anim.setRepeatCount(ObjectAnimator.INFINITE);
        anim.start();
    }

   /* // Check if the hotspot is enabled
    private boolean isHotspotEnabled() {
        try {
            WifiConfiguration config = (WifiConfiguration) wifiManager.getClass().getMethod("getWifiApConfiguration").invoke(wifiManager);
            return config != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get information about connected devices
    private String getConnectedDevicesInfo() {
        StringBuilder connectedDevices = new StringBuilder();

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return TODO;
            }
            for (WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
                connectedDevices.append(config.deviceAddress).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connectedDevices.toString();
    }*/
}
