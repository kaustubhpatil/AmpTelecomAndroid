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
package com.amptelecom.android.app.assistant;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import com.amptelecom.android.app.LinphoneManager;
import com.amptelecom.android.app.R;
import com.amptelecom.android.app.network.ApiService;
import com.amptelecom.android.app.network.RetrofitClientInstance;
import com.amptelecom.android.app.network.model.LoginData;
import com.amptelecom.android.app.network.model.ServerResponse;
import com.amptelecom.android.app.settings.LinphonePreferences;
import com.amptelecom.android.app.utils.FileUtil;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.List;
import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.TransportType;
import org.linphone.core.tools.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrCodeConfigurationAssistantActivity extends AssistantActivity {
    //    private TextureView mQrcodeView;

    //    private CoreListenerStub mListener;

    private CodeScanner mCodeScanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.assistant_qr_code_remote_configuration);

        if (checkPermission(Manifest.permission.CAMERA)
                && checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            loadQrCode();
        } else {
            checkAndRequestPermission(
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, 12);
        }
    }

    @Override
    public void onBackPressed() {}

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 12) {
            if (checkPermission(Manifest.permission.CAMERA)
                    && checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                loadQrCode();
            }
        }
    }

    private void loadCamera() {
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(
                new DecodeCallback() {
                    @Override
                    public void onDecoded(@NonNull final Result result) {
                        QrCodeConfigurationAssistantActivity.this.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        fetchLoginDetails(result.getText());
                                    }
                                });
                    }
                });
        scannerView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCodeScanner.startPreview();
                    }
                });
        mCodeScanner.startPreview();
    }

    private void loadQrCode2() {

        new AlertDialog.Builder(this)
                .setTitle("Something went wrong, Try again?")
                .setPositiveButton(
                        "Camera",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (mCodeScanner != null) {
                                    mCodeScanner.startPreview();
                                } else {
                                    loadCamera();
                                }
                            }
                        })
                .setNegativeButton(
                        "Gallery",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_PICK);
                                startActivityForResult(
                                        Intent.createChooser(intent, "Choose Image"),
                                        PICK_IMAGE_REQUEST);
                            }
                        })
                .setCancelable(false)
                .show();
    }

    private void loadQrCode() {

        new AlertDialog.Builder(this)
                .setTitle("Scan QR code using?")
                .setPositiveButton(
                        "Camera",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (mCodeScanner != null) {
                                    mCodeScanner.startPreview();
                                } else {
                                    loadCamera();
                                }
                            }
                        })
                .setNegativeButton(
                        "Gallery",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_PICK);
                                startActivityForResult(
                                        Intent.createChooser(intent, "Choose Image"),
                                        PICK_IMAGE_REQUEST);
                            }
                        })
                .setCancelable(false)
                .show();
    }

    private static final int PICK_IMAGE_REQUEST = 9;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(
                                QrCodeConfigurationAssistantActivity.this,
                                "Failed to open picture!",
                                Toast.LENGTH_SHORT)
                        .show();
                loadQrCode2();
                return;
            }
            try {
                File actualImage = FileUtil.from(this, data.getData());

                Bitmap bMap = BitmapFactory.decodeFile(actualImage.getPath());
                String str = "null";
                if (bMap != null) {
                    str = "notnull";
                }
                android.util.Log.i("FirebaseMessaging", str + actualImage.getAbsolutePath());

                int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
                // copy pixel data from the Bitmap into the 'intArray' array
                bMap.getPixels(
                        intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

                LuminanceSource source =
                        new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Hashtable<DecodeHintType, Object> decodeHints =
                        new Hashtable<DecodeHintType, Object>();
                decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

                try {
                    Reader reader = new MultiFormatReader();
                    Result result = reader.decode(bitmap, decodeHints);
                    //                    android.util.Log.i("FirebaseMessaging", result.getText());
                    fetchLoginDetails(result.getText());
                    //                    Toast.makeText(
                    //                                    QrCodeConfigurationAssistantActivity.this,
                    //                                    "Data" + result.getText(),
                    //                                    Toast.LENGTH_SHORT)
                    //                            .show();

                } catch (Exception e) {
                    loadQrCode2();
                }

            } catch (IOException e) {
                loadQrCode2();
                e.printStackTrace();
            }
        }
    }

    ProgressDialog progressDialog;

    private void fetchLoginDetails(final String url) {
        progressDialog =
                new ProgressDialog(
                        QrCodeConfigurationAssistantActivity.this, ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        ApiService service = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        service.getLoginDetails(url)
                .enqueue(
                        new Callback<ServerResponse<List<LoginData>>>() {
                            @Override
                            public void onResponse(
                                    Call<ServerResponse<List<LoginData>>> call,
                                    Response<ServerResponse<List<LoginData>>> response) {
                                if (response != null
                                        && response.body() != null
                                        && response.body().statusCode == 200
                                        && response.body().data.size() > 0) {
                                    LoginData loginData = response.body().data.get(0);
                                    LinphonePreferences.instance().setUsername(loginData.username);
                                    LinphonePreferences.instance().setDomain(loginData.domain);
                                    LinphonePreferences.instance().setPassword(loginData.password);
                                    LinphonePreferences.instance().setProtocol(loginData.protocol);
                                    login(
                                            loginData.username,
                                            loginData.password,
                                            loginData.domain,
                                            loginData.protocol);
                                } else {
                                    loadQrCode2();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<ServerResponse<List<LoginData>>> call, Throwable t) {
                                progressDialog.dismiss();
                                loadQrCode2();
                            }
                        });
    }

    private void login(
            final String username,
            final String password,
            final String domain,
            final String protocol) {

        Core core = LinphoneManager.getCore();
        if (core != null) {
            Log.i("[Generic Connection Assistant] Reloading configuration with default");
            reloadDefaultAccountCreatorConfig();
        }

        AccountCreator accountCreator = getAccountCreator();
        accountCreator.setUsername(username);
        accountCreator.setDomain(domain);
        accountCreator.setPassword(password);
        accountCreator.setDisplayName("");

        switch (protocol.toUpperCase()) {
            case "UDP":
                accountCreator.setTransport(TransportType.Udp);
                break;
            case "TCP":
                accountCreator.setTransport(TransportType.Tcp);
                break;
            case "TLS":
                accountCreator.setTransport(TransportType.Tls);
                break;
        }
        progressDialog.dismiss();
        createProxyConfigAndLeaveAssistant(true);

        // upload push token
        String pushNotificationRegistrationID =
                LinphonePreferences.instance().getPushNotificationRegistrationID();
        if (!TextUtils.isEmpty(pushNotificationRegistrationID)) {
            LinphonePreferences.instance()
                    .setPushNotificationRegistrationID(
                            pushNotificationRegistrationID,
                            QrCodeConfigurationAssistantActivity.this);
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

    private void checkAndRequestPermission(String permission, String permission2, int result) {
        //        if (!checkPermission(permission)) {
        Log.i("[Permission] Asking for " + permission);
        ActivityCompat.requestPermissions(this, new String[] {permission, permission2}, result);
        //            return false;
        //        }
        //        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCodeScanner != null) {
            mCodeScanner.startPreview();
        }
        //        enableQrcodeReader(true);
    }

    @Override
    public void onPause() {
        if (mCodeScanner != null) {
            mCodeScanner.releaseResources();
        }

        //        enableQrcodeReader(false);

        super.onPause();
    }

    //    private void enableQrcodeReader(boolean enable) {
    //        Core core = LinphoneManager.getCore();
    //        if (core == null) return;
    //
    //        core.setNativePreviewWindowId(enable ? mQrcodeView : null);
    //        core.enableQrcodeVideoPreview(enable);
    //        core.enableVideoPreview(enable);
    //
    //        if (enable) {
    //            core.addListener(mListener);
    //        } else {
    //            core.removeListener(mListener);
    //        }
    //    }

    //    private void setBackCamera() {
    //        Core core = LinphoneManager.getCore();
    //        if (core == null) return;
    //
    //        String firstDevice = null;
    //        for (String camera : core.getVideoDevicesList()) {
    //            if (firstDevice == null) {
    //                firstDevice = camera;
    //            }
    //
    //            if (camera.contains("Back")) {
    //                Log.i("[QR Code] Found back facing camera: " + camera);
    //                core.setVideoDevice(camera);
    //                return;
    //            }
    //        }
    //
    //        Log.i("[QR Code] Using first camera available: " + firstDevice);
    //        core.setVideoDevice(firstDevice);
    //    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
