/*
Copyright 2011-2013 Pieter Pareit

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.sandvik.databearerdev.gui;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.sandvik.databearerdev.AppSettings;
import com.sandvik.databearerdev.R;
import com.sandvik.databearerdev.gui.settings.FsPreferenceActivity;
import com.sandvik.databearerdev.services.ftp.FsService;
import com.sandvik.databearerdev.util.NetUtils;

import net.vrallev.android.cat.Cat;

import java.net.InetAddress;

public class FsNotification extends BroadcastReceiver {


    public final static String CHANNEL_ID = "com.sandvik.databearerdev";
    private final static int NOTIFICATIONID = 7890;

    @Override
    public void onReceive(Context context, Intent intent) {
        Cat.d("onReceive broadcast: " + intent.getAction());
        switch (intent.getAction()) {
            case FsService.ACTION_STARTED:
                setupNotification(context);
                break;
            case FsService.ACTION_STOPPED:
                clearNotification(context);
                break;
        }
    }

    @SuppressLint("NewApi")
    private void setupNotification(Context context) {
        Cat.d("Setting up the notification");
        // Get NotificationManager reference
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) context.getSystemService(ns);

        // get ip address
        InetAddress address = NetUtils.getLocalInetAddress();
        if (address == null) {
            Cat.w("Unable to retrieve the local ip address");
            return;
        }
        String iptext = "ftp://" + address.getHostAddress() + ":"
                + AppSettings.getPortNumber() + "/";

        // Instantiate a Notification
        int icon = R.mipmap.notification;
        CharSequence tickerText = String.format(context.getString(R.string.notif_server_starting), iptext);
        long when = System.currentTimeMillis();

        // Define Notification's message and Intent
        CharSequence contentTitle = context.getString(R.string.notif_title);
        CharSequence contentText = String.format(context.getString(R.string.notif_text), iptext);

        Intent notificationIntent = new Intent(context, FsPreferenceActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        int stopIcon = android.R.drawable.ic_menu_close_clear_cancel;
        CharSequence stopText = context.getString(R.string.notif_stop_text);
        Intent stopIntent = new Intent(FsService.ACTION_STOP_FTPSERVER);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0,
                stopIntent, PendingIntent.FLAG_IMMUTABLE);

        int preferenceIcon = android.R.drawable.ic_menu_preferences;
        CharSequence preferenceText = context.getString(R.string.notif_settings_text);
        Intent preferenceIntent = new Intent(context, FsPreferenceActivity.class);
        preferenceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent preferencePendingIntent = PendingIntent.getActivity(context, 0, preferenceIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(context, FsNotification.CHANNEL_ID)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setWhen(when)
                .setOngoing(true);

        Notification notification = null;

        // go from hight to low android version adding extra options
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            nb.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            nb.setCategory(Notification.CATEGORY_SERVICE);
            nb.setPriority(Notification.PRIORITY_MAX);
        }
        nb.addAction(stopIcon, stopText, stopPendingIntent);
        nb.addAction(preferenceIcon, preferenceText, preferencePendingIntent);
        nb.setShowWhen(false);
        notification = nb.build();

        // Pass Notification to NotificationManager
        nm.notify(NOTIFICATIONID, notification);

        Cat.d("Notification setup done");
    }


    private void clearNotification(Context context) {
        Cat.d("Clearing the notifications");
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) context.getSystemService(ns);
        nm.cancelAll();
        Cat.d("Cleared notification");
    }
}
