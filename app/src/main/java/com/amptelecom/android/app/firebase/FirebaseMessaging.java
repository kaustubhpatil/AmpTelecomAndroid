/*
 * Copyright (c) 2010-2019 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.amptelecom.android.app.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import com.amptelecom.android.app.LinphoneContext;
import com.amptelecom.android.app.LinphoneManager;
import com.amptelecom.android.app.R;
import com.amptelecom.android.app.activities.MainActivity;
import com.amptelecom.android.app.settings.LinphonePreferences;
import com.amptelecom.android.app.utils.LinphoneUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.ArrayList;
import java.util.Random;
import org.linphone.core.Core;
import org.linphone.core.tools.Log;

public class FirebaseMessaging extends FirebaseMessagingService {
    private Runnable mPushReceivedRunnable =
            new Runnable() {
                @Override
                public void run() {
                    if (!LinphoneContext.isReady()) {
                        android.util.Log.i(
                                "FirebaseMessaging", "[Push Notification] Starting context");
                        new LinphoneContext(getApplicationContext());
                        LinphoneContext.instance().start(true);
                    } else {
                        Log.i("[Push Notification] Notifying Core");
                        if (LinphoneManager.getInstance() != null) {
                            Core core = LinphoneManager.getCore();
                            if (core != null) {
                                core.ensureRegistered();
                            }
                        }
                    }
                }
            };

    public FirebaseMessaging() {}

    @Override
    public void onNewToken(final String token) {
        android.util.Log.i("FirebaseIdService", "[Push Notification] Refreshed token: " + token);

        LinphoneUtils.dispatchOnUIThread(
                new Runnable() {
                    @Override
                    public void run() {
                        LinphonePreferences.instance()
                                .setPushNotificationRegistrationID(token, getApplicationContext());
                    }
                });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        android.util.Log.i("ttt", "[Push Notification] Received" + remoteMessage.toString());

        //        try {
        //            Toast.makeText(getApplicationContext(), "Push notification received",
        // Toast.LENGTH_LONG)
        //                    .show();
        //        } catch (Exception e) {
        //        }
        //        LinphoneUtils.dispatchOnUIThread(mPushReceivedRunnable);
        try {
            //            if (remoteMessage.getData().size() > 0) {
            //                android.util.Log.i("ttt", "success" +
            // remoteMessage.getData().toString());
            //                showNoti(
            //                        remoteMessage.getData().get("title"),
            //                        remoteMessage.getData().get("message"));
            showNoti("Sender Name", "This is message");
            //            } else {
            //                android.util.Log.i("ttt", "success" +
            // remoteMessage.getNotification().toString());
            //            }
        } catch (Exception e) {
        }
    }

    public void showNoti(String title, String message) {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", "Notification");
            intent.putExtra("chatid", "88994b47-cba8-4d42-91b7-83255396aa9d");
            intent.putExtra("to", "+15129991212");
            intent.putStringArrayListExtra("cc", new ArrayList<>());

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            final RemoteViews remoteViews =
                    new RemoteViews(getPackageName(), R.layout.custom_notification);
            remoteViews.setTextViewText(R.id.title, title);
            remoteViews.setTextViewText(R.id.message, message);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                String name = "AmpTelecom";
                String id2 = "AmpTelecom";
                String description = "Info";

                androidx.core.app.NotificationCompat.Builder builder;

                NotificationManager notifManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = notifManager.getNotificationChannel(id2);
                if (mChannel == null) {
                    mChannel = new NotificationChannel(id2, name, importance);
                    mChannel.setDescription(description);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(
                            new long[] {100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notifManager.createNotificationChannel(mChannel);
                }
                builder =
                        new androidx.core.app.NotificationCompat.Builder(
                                getApplicationContext(), id2);

                builder.setContentTitle(title)
                        .setSmallIcon(R.drawable.linphone_notification_icon)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .setGroup("" + new Random().nextInt())
                        .setContentIntent(resultPendingIntent)
                        .setVibrate(new long[] {100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setContent(remoteViews);

                Notification notification = builder.build();

                notifManager.notify(new Random().nextInt() /* ID of notification */, notification);

            } else {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                Notification notification;
                notification =
                        mBuilder.setSmallIcon(R.drawable.linphone_notification_icon)
                                .setWhen(0)
                                .setOngoing(false)
                                .setAutoCancel(true)
                                .setSound(soundUri)
                                .setGroup("" + new Random().nextInt())
                                .setContentIntent(resultPendingIntent)
                                .setContentTitle(title)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContent(remoteViews)
                                .build();
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(new Random().nextInt(), notification);
            }
        } catch (Exception e) {
        }
    }
}
