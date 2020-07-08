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
package org.linphone.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import org.linphone.R;
import org.linphone.assistant.GenericConnectionAssistantActivity;
import org.linphone.chat.ChatActivity;
import org.linphone.contacts.ContactsActivity;
import org.linphone.dialer.DialerActivity;
import org.linphone.history.HistoryActivity;
import org.linphone.service.LinphoneService;
import org.linphone.service.ServiceWaitThread;
import org.linphone.service.ServiceWaitThreadListener;
import org.linphone.settings.LinphonePreferences;

/** Creates LinphoneService and wait until Core is ready to start main Activity */
public class LinphoneLauncherActivity extends Activity implements ServiceWaitThreadListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.orientation_portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (!getResources().getBoolean(R.bool.use_full_screen_image_splashscreen)) {
            setContentView(R.layout.launch_screen);
        } // Otherwise use drawable/launch_screen layer list up until first activity starts
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (LinphoneService.isReady()) {
            onServiceReady();
        } else {
            try {
                startService(
                        new Intent()
                                .setClass(LinphoneLauncherActivity.this, LinphoneService.class));
                new ServiceWaitThread(this).start();
            } catch (IllegalStateException ise) {
                Log.e("Linphone", "Exception raised while starting service: " + ise);
            }
        }
    }

    @Override
    public void onServiceReady() {
        final Class<? extends Activity> classToStart;

        boolean useFirstLoginActivity =
                getResources().getBoolean(R.bool.display_account_assistant_at_first_start);
        if (useFirstLoginActivity && LinphonePreferences.instance().isFirstLaunch()) {
            classToStart = GenericConnectionAssistantActivity.class;
        } else {
            if (getIntent().getExtras() != null) {
                String activity = getIntent().getExtras().getString("Activity", null);
                if (ChatActivity.NAME.equals(activity)) {
                    classToStart = ChatActivity.class;
                } else if (HistoryActivity.NAME.equals(activity)) {
                    classToStart = HistoryActivity.class;
                } else if (ContactsActivity.NAME.equals(activity)) {
                    classToStart = ContactsActivity.class;
                } else {
                    classToStart = DialerActivity.class;
                }
            } else {
                classToStart = DialerActivity.class;
            }
        }

        Intent intent = new Intent();
        intent.setClass(LinphoneLauncherActivity.this, classToStart);
        if (getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        intent.setAction(getIntent().getAction());
        intent.setType(getIntent().getType());
        intent.setData(getIntent().getData());
        startActivity(intent);

        //                        LinphoneManager.getInstance().changeStatusToOnline();
        //                        checkAndRequestPermission(Manifest.permission.CAMERA, 12);
        //                Intent i = new Intent(LinphoneLauncherActivity.this,
        // QrCodeActivity.class);
        //                startActivityForResult(i, REQUEST_CODE_QR_SCAN);
    }

    //    private boolean checkPermission(String permission) {
    //        int granted = getPackageManager().checkPermission(permission, getPackageName());
    //        org.linphone.core.tools.Log.i(
    //                "[Permission] "
    //                        + permission
    //                        + " permission is "
    //                        + (granted == PackageManager.PERMISSION_GRANTED ? "granted" :
    // "denied"));
    //        return granted == PackageManager.PERMISSION_GRANTED;
    //    }
    //
    //    private boolean checkAndRequestPermission(String permission, int result) {
    //        if (!checkPermission(permission)) {
    //            org.linphone.core.tools.Log.i("[Permission] Asking for " + permission);
    //            ActivityCompat.requestPermissions(this, new String[] {permission}, result);
    //            return false;
    //        }
    //        return true;
    //    }
    //
    //    private static final int REQUEST_CODE_QR_SCAN = 101;
    //
    //    @Override
    //    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //        if (requestCode == REQUEST_CODE_QR_SCAN) {
    //            if (data == null) return;
    //            // Getting the passed result
    //            String result =
    // data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
    //            //            Log.d(LOGTAG,"Have scan result in your app activity :"+ result);
    //            AlertDialog alertDialog =
    //                    new AlertDialog.Builder(LinphoneLauncherActivity.this).create();
    //            alertDialog.setTitle("Scan result");
    //            alertDialog.setMessage(result);
    //            alertDialog.setButton(
    //                    AlertDialog.BUTTON_NEUTRAL,
    //                    "OK",
    //                    new DialogInterface.OnClickListener() {
    //                        public void onClick(DialogInterface dialog, int which) {
    //                            dialog.dismiss();
    //                        }
    //                    });
    //            alertDialog.show();
    //        }
    //    }
}
