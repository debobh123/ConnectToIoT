package com.sandvik.databearerdev.services.fdm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.Gravity;
import android.widget.Toast;

import com.sandvik.databearerdev.App;
import com.sandvik.databearerdev.R;
import com.sandvik.databearerdev.services.ftp.FsService;
import com.sandvik.databearerdev.util.Logger;

public class RequestStartStopReceiver extends BroadcastReceiver {

    static final String TAG = RequestStartStopReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.v(TAG, "Received: " + intent.getAction());

        // TODO: analog code as in ServerPreferenceActivity.start/stopServer(), refactor
        try {
            if (intent.getAction().equals(FsService.ACTION_START_FTPSERVER)) {
                Intent serverService = new Intent(context, FsService.class);
                if (!FsService.isRunning()) {
                    warnIfNoExternalStorage();
                    context.startService(serverService);
                }
            } else if (intent.getAction().equals(FsService.ACTION_STOP_FTPSERVER)) {
                Intent serverService = new Intent(context, FsService.class);
                context.stopService(serverService);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to start/stop on intent " + e.getMessage());
        }
    }

    /**
     * Will check if the device contains external storage (sdcard) and display a warning
     * for the user if there is no external storage. Nothing more.
     */
    private void warnIfNoExternalStorage() {
        String storageState = Environment.getExternalStorageState();
        if (!storageState.equals(Environment.MEDIA_MOUNTED)) {
            Logger.v(TAG, "Warning due to storage state " + storageState);
            Toast toast = Toast.makeText(App.getAppContext(),
                    R.string.storage_warning, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
	
}
