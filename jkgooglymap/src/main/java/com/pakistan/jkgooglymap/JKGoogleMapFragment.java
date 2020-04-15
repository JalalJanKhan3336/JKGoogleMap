package com.pakistan.jkgooglymap;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public abstract class JKGoogleMapFragment extends Fragment {
    private static final int REQUEST_PERMISSION_CODE = 1624;

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    public JKGoogleMapFragment() {}

    public abstract void onGranted();

    public abstract void onDenied();

    protected void requestRuntimePermissions(Context context) {
        if (!isPermissionEnabled(context, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSION_CODE);
        }
    }

    protected boolean isPermissionEnabled(Context context, String... permissions) {
        boolean flag = false;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != 0) {
                flag = false;
                break;
            }

            flag = true;
        }

        return flag;
    }

    protected void showDenialDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle("Permission Required!");
        builder.setMessage("Permissions are required to proceed. ");
        builder.setPositiveButton("Goto Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                gotoSettings(context);
            }
        });
        builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    protected void gotoSettings(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        Uri uri = Uri.fromParts("package", context.getPackageName(), (String)null);
        intent.setData(uri);
        this.startActivity(intent);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.length > 0) {
            boolean flag = false;

            for (int result : grantResults) {
                if (result != 0) {
                    flag = false;
                    break;
                }

                flag = true;
            }

            if (flag) {
                onGranted();
            } else {
                onDenied();
            }
        }
    }

}
