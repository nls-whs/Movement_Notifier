package com.vuzix.nextlevelsports.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.vuzix.nextlevelsports.BladeSampleApplication;
import com.vuzix.nextlevelsports.R;

import java.text.DecimalFormat;

import static android.content.Context.MODE_PRIVATE;
import static com.vuzix.nextlevelsports.activities.SettingsActivity.SHARED_PREFS;

/**
 * Implementierung der Widget Funktionalität.
 */
public class Widget extends AppWidgetProvider {

    private int progress;

    private static boolean serviceRunning = false;

    private static PendingIntent service;

    private static int countedSteps;

    /**
     * Updatet das Widget.
     * @param context Context
     * @param appWidgetManager AppWidgetManager
     * @param appWidgetId AppWidgetId
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        int stepGoal = sharedPreferences.getInt("stepGoal", 10000);
        int stepsToday = sharedPreferences.getInt("stepsToday", 0);

        boolean isLightMode = isLightMode(context);

        if (appWidgetManager == null) {
            appWidgetManager = (AppWidgetManager) context
                    .getSystemService(Context.APPWIDGET_SERVICE);
            appWidgetId = appWidgetManager.getAppWidgetIds
                    (new ComponentName(context, Widget.class))[0];
        }

        RemoteViews view = new RemoteViews(context.getPackageName(),
                isLightMode ? R.layout.widget_light: R.layout.widget_dark);

        DecimalFormat nf = new DecimalFormat();

         view.setTextViewText(R.id.appwidget_text_steps_light, nf.format(stepsToday)
                 .replace(",",".") + "");
         view.setTextViewText(R.id.stepGoalTV, "/ " + nf.format(stepGoal)
                 .replace(",",".") + "\nSchritten");
         view.setProgressBar(R.id.progress_bar_dark_outer_light, 100,
                stepsToday * 100 / stepGoal, false);

         view.setImageViewResource(R.id.calories, R.drawable.ic_fire);
         view.setImageViewResource(R.id.distance, R.drawable.ic_map_marker_distance);
         view.setImageViewResource(R.id.time, R.drawable.ic_timer);
         view.setImageViewResource(R.id.exercises, R.drawable.ic_weight_lifter);

         view.setTextViewText(R.id.text_calories, 375 + " Kcal");
         view.setTextViewText(R.id.text_time, 60 + " min");
         view.setTextViewText(R.id.text_exercises, 10 + " Übg.");

        appWidgetManager.updateAppWidget(appWidgetId, view);
    }

    /**
     * Aktionen bei Erhalten eines Broadcasts.
     * @param context Context
     * @param intent Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        updateAppWidget(context,null,0);
    }

    /**
     * Aktionen bei Update eines Widgets.
     * @param context Context
     * @param appWidgetManager AppWidgetManager
     * @param appWidgetIds AppWidgetId
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAppWidget(context, appWidgetManager, appWidgetIds[0]);
    }

    public static void getStepCount(Intent intent) {
        countedSteps = intent.getIntExtra("Counted_Step_Int", 0);
    }

    /**
     * Prüft, ob Lightmode aktiviert ist.
     * @param context Context
     * @return Context
     */
    private static boolean isLightMode(Context context) {
        return ((BladeSampleApplication)context.getApplicationContext()).isLightMode();
    }

}