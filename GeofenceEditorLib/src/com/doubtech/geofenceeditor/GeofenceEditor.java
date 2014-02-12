package com.doubtech.geofenceeditor;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

public class GeofenceEditor {
    private static final String DEFAULT_EDITOR_PACKAGENAME = "com.doubtech.geofenceditor";
    public static String EXTRA_STARTING_LATITUDE = "starting_latitude";
    public static String EXTRA_STARTING_LONGITUDE = "starting_longitude";
    public static String EXTRA_STARTING_ZOOM = "starting_zoom";

    public static Intent getIntent() {
        return getIntent(null);
    }

    public static Intent getIntent(SimpleGeofence geofence) {
        Intent intent = new Intent(Actions.CREATE_GEOFENCE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (null != geofence) {
            geofence.fillIntent(intent);
        }
        return intent;
    }

    public static boolean isGeofenceEditorInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(getIntent(), 0);

        return resolveInfos.size() > 0;
    }

    public static void installGeofenceEditor(Context context) {
        final String appPackageName = DEFAULT_EDITOR_PACKAGENAME;
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
}
