package com.vuzix.nextlevelsports.receiver;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WidgetUpdateReceiver extends BroadcastReceiver {

    /**
     * Updatet bei Erhalt eines Broadcasts das Interface des Widgets.
     * @param context Context
     * @param intent Update Widget
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        Log.d("Widget_RECEIVER", "Sending Intent");
        context.sendBroadcast(updateIntent);
        Log.d("Widget_RECEIVER", "Intent Send");

    }

}
