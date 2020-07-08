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
package org.linphone.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import org.linphone.LinphoneManager;
import org.linphone.R;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.tools.Log;

public class QrCodeConfigurationAssistantActivity extends AssistantActivity {
    private TextureView mQrcodeView;

    private CoreListenerStub mListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.assistant_qr_code_remote_configuration);

        mQrcodeView = findViewById(R.id.qr_code_capture_texture);

        mListener =
                new CoreListenerStub() {
                    @Override
                    public void onQrcodeFound(Core core, String result) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("URL", result);
                        setResult(Activity.RESULT_OK, resultIntent);
                        //                        Log.e("ttt", "+" + result);
                        //                        Toast.makeText(
                        //
                        // QrCodeConfigurationAssistantActivity.this,
                        //                                        "F" + result,
                        //                                        Toast.LENGTH_LONG)
                        //                                .show();
                        finish();
                    }
                };

        ImageView changeCamera = findViewById(R.id.qr_code_capture_change_camera);
        changeCamera.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinphoneManager.getCallManager().switchCamera();
                    }
                });
        Core core = LinphoneManager.getCore();
        if (core != null && core.getVideoDevicesList().length > 1) {
            changeCamera.setVisibility(View.VISIBLE);
        }

        if (checkPermission(Manifest.permission.CAMERA)) {
            setBackCamera();
        } else {
            checkAndRequestPermission(Manifest.permission.CAMERA, 12);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 12) {
            if (checkPermission(Manifest.permission.CAMERA)) {
                setBackCamera();
                enableQrcodeReader(true);
            }
        }
    }

    private boolean checkPermission(String permission) {
        int granted = getPackageManager().checkPermission(permission, getPackageName());
        Log.i(
                "[Permission] "
                        + permission
                        + " permission is "
                        + (granted == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
        return granted == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkAndRequestPermission(String permission, int result) {
        if (!checkPermission(permission)) {
            Log.i("[Permission] Asking for " + permission);
            ActivityCompat.requestPermissions(this, new String[] {permission}, result);
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        enableQrcodeReader(true);
    }

    @Override
    public void onPause() {
        enableQrcodeReader(false);

        super.onPause();
    }

    private void enableQrcodeReader(boolean enable) {
        Core core = LinphoneManager.getCore();
        if (core == null) return;

        core.setNativePreviewWindowId(enable ? mQrcodeView : null);
        core.enableQrcodeVideoPreview(enable);
        core.enableVideoPreview(enable);

        if (enable) {
            core.addListener(mListener);
        } else {
            core.removeListener(mListener);
        }
    }

    private void setBackCamera() {
        Core core = LinphoneManager.getCore();
        if (core == null) return;

        String firstDevice = null;
        for (String camera : core.getVideoDevicesList()) {
            if (firstDevice == null) {
                firstDevice = camera;
            }

            if (camera.contains("Back")) {
                Log.i("[QR Code] Found back facing camera: " + camera);
                core.setVideoDevice(camera);
                return;
            }
        }

        Log.i("[QR Code] Using first camera available: " + firstDevice);
        core.setVideoDevice(firstDevice);
    }
}
