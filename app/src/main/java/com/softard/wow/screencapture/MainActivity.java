package com.softard.wow.screencapture;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.softard.wow.screencapture.QRCode.ScanQRActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 100;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };
    private static MediaProjection sMediaProjection;
    @BindView(R.id.btn_capture) Button mBtnCapture;
    @BindView(R.id.btn_record) Button mBtnRecordScreen;
    @BindView(R.id.btn_record_camera) Button mBtnRecordCamera;
    @BindView(R.id.btn_scan_qr) Button mBtnScanQR;
    @BindView(R.id.btn_rtsp_player) Button mBtnRtsp;
    private MediaProjectionManager mProjectionManager;
    private ScreenCapture mSC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBtnCapture.setOnClickListener(this);
        mBtnRecordScreen.setOnClickListener(this);
        mBtnRecordCamera.setOnClickListener(this);
        mBtnScanQR.setOnClickListener(this);
        mBtnRtsp.setOnClickListener(this);

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_capture:
//                Intent i = new Intent(MainActivity.this, CaptureActivity.class);
//                i.putExtra("path", "some path");
//                startActivity(i);
                startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                        REQUEST_CODE);
                break;
            case R.id.btn_record:
                startActivity(new Intent(MainActivity.this, ScreenRecordActivity.class));
                break;

            case R.id.btn_record_camera:
                startActivity(new Intent(MainActivity.this, CameraRecordActivity.class));
                break;

            case R.id.btn_scan_qr:
                startActivity(new Intent(MainActivity.this, ScanQRActivity.class));
                break;
            case R.id.btn_rtsp_player:
                startActivity(new Intent(MainActivity.this, RTSPlayerActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("WOW", "on result : requestCode = " + requestCode + " resultCode = " + resultCode);
        if (RESULT_OK == resultCode && REQUEST_CODE == requestCode) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            if (sMediaProjection != null) {
//                Intent i = new Intent(MainActivity.this, ScreenCaptureService.class);
//                i.putExtra(ScreenCaptureService.EXTRA_PATH, "some path");
//                i.putExtra(ScreenCaptureService.EXTRA_MEDIA_PROJECTION, () sMediaProjection);
                Log.d("WOW", "Start capturing...");
                new ScreenCapture(this, sMediaProjection, "").startProjection();
            }
        }
    }

    //permission
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        Boolean isAllPermissionGranted = false;
        for (String permission : PERMISSIONS) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                isAllPermissionGranted = false;
                break;
            }
            isAllPermissionGranted = true;
        }

        if (!isAllPermissionGranted) {
            requestPermissions(PERMISSIONS, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE == requestCode) {
            boolean isAllGranted = false;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
                isAllGranted = true;
            }
            if (!isAllGranted) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Need Permission");
                builder.setPositiveButton("Grant now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("Fuck yourself", null);
                builder.show();
            } else {

            }
        }
    }
}
