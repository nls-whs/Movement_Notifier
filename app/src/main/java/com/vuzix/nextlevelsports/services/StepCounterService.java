package com.vuzix.nextlevelsports.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.vuzix.nextlevelsports.R;
import com.vuzix.nextlevelsports.activities.CameraActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static com.vuzix.nextlevelsports.activities.SettingsActivity.SHARED_PREFS;

/**
 * Service zum Schritte zählen wird bei nach dem Starten der Brille gestartet.
 */
public class StepCounterService extends Service implements SensorEventListener {

    SensorManager sensorManager;
    Sensor stepCounterSensor;
    Sensor stepDetectorSensor;

    private int notificationId = 1;

    int currentStepCount;
    int currentStepsDetected;

    int stepCounter;
    int newStepCounter;

    boolean serviceStopped;

    NotificationManager notificationManager;

    Intent intent;
    public static final String BROADCAST_ACTION = "com.vuzix.nextlevelsports.broadcast";
    private final Handler handler = new Handler();

    private int stepGoal;
    private int stepsLastHour;
    int counter = 0;
    int stepsToday;
    SharedPreferences sharedPreferences;

    boolean isNewDay = false;

    /**
     * Erstellt den Service.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences
                = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        if(checkIfNewDay()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("stepsToday", 0);
            editor.apply();
            stepsToday = sharedPreferences.getInt("stepsToday", 0);
        } else {
            stepsToday = sharedPreferences.getInt("stepsToday", 0);
        }

        stepGoal = sharedPreferences.getInt("stepGoal", 10000);
        stepsLastHour = sharedPreferences.getInt("stepsLastHour", 0);

        intent = new Intent(BROADCAST_ACTION);

        notificationManager =
                (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

    }

    /**
     * Aktionen, die bei Start des Services passieren.
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(this, stepCounterSensor, 0);
        sensorManager.registerListener(this, stepDetectorSensor, 0);

        currentStepCount = 0;
        currentStepsDetected = 0;
        stepCounter = 0;
        newStepCounter = 0;

        serviceStopped = false;

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String today = sdf.format(new Date());
        String dateToday = sharedPreferences.getString("dateToday", null);

        if (checkIfNewDay()) {
            isNewDay = false;
        } else {
            isNewDay = true;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("dateToday", today);
            editor.putInt("exercisesDone", 0);
            editor.putInt("stepsToday", 0);
            editor.putBoolean("halfGoalReached", false);
            editor.putBoolean("goalReached", false);
            editor.apply();
        }

        // handler.removeCallbacks(updateBroadcastData);
        // handler.post(updateBroadcastData);

        /*
         * Überprüft jede Stunde, ob sich der Benutzer der app ausreichend bewegt hat. Hat er das
         * nicht, erhält er eine Benachrichtigung.
         * Zusätzlich erhält er bei Erreichen der Hälfte sowie des kompletten Schrittziels
         * jeweils eine Benachrichtigung.
         * Hat er das Tagesziel erreicht, erhält er für den Tag keine Nachrichten mehr.
         */
        final Runnable updateBroadcastData = new Runnable() {

            @Override
            public void run() {
                if (!serviceStopped) {
                    broadcastSensorValue();

                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,
                            MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    if (stepsToday >= stepGoal
                            && !sharedPreferences.getBoolean("goalReached", false)) {
                        showNotificationGoalReached();
                    } else if (stepsToday >= stepGoal * 0.5
                            && !sharedPreferences.getBoolean("halfGoalReached", false)) {
                        showNotificationHalfGoalReached();
                    } else if (stepsToday - stepsLastHour <= stepGoal * 0.05 &&
                            !sharedPreferences.getBoolean("goalReached", false)) {
                        showNotificationExerxcise();
                    }

                    editor.putInt("stepsLastHour", newStepCounter);
                    editor.apply();

                    /* geändert auf 60, vorher 30 */
                    handler.postDelayed(this, 60000 * 60);
                }
            }
        };

        handler.postDelayed(updateBroadcastData, 60000 * 60);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Beim Zerstören des Service wird die Variable auf true gesetzt.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceStopped = true;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /**
     * Reagiert auf die Änderungen der einzelnen Sensoren.
     * @param sensorEvent SensorEvent.
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int countSteps = (int) sensorEvent.values[0];

            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Fall 1: Neustart der Brille, Zähler dadurch zurückgesetzt.
            // Fall 2: Neuer Tag, zwischengespeicherte Werte werden zurückgesetzt.
            if (newStepCounter == 0) {
                if (!isNewDay) {
                    if (sharedPreferences.getInt("stepsBeforeRestart", 0) == 0
                            || sharedPreferences.getInt("stepsBeforeRestart", 0)
                            != sharedPreferences.getInt("stepsToday", 0)) {
                        editor.putInt("stepsBeforeRestart", sharedPreferences
                                .getInt("stepsToday", 0));
                    }
                    newStepCounter = countSteps + sharedPreferences.getInt("stepsToday", 0);
                    editor.putInt("stepsToday", newStepCounter);
                    editor.apply();
                } else {
                    newStepCounter = countSteps;
                    editor.putInt("stepsToday", newStepCounter);
                    editor.putInt("stepsBeforeRestart", 0);
                    editor.apply();
                    isNewDay = false;
                }
            }
            // Fall 3: Neustart der Brille, Werte vor Neustart werden übergeben.
            else if (newStepCounter >= countSteps) {
                newStepCounter = sharedPreferences.getInt("stepsBeforeRestart", 0)
                        + countSteps;
                editor.putInt("stepsToday", newStepCounter);
                editor.apply();
            }
            else {
                newStepCounter = countSteps;
                editor = sharedPreferences.edit();
                editor.putInt("stepsToday", newStepCounter);
                editor.apply();
            }

            // Schritte werden für den Fall eines Neustarts zwischengespeichert
            editor.apply();

        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            int detectSteps = (int) sensorEvent.values[0];
            currentStepsDetected += detectSteps;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private boolean checkIfNewDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String today = sdf.format(new Date());
        String dateToday = sharedPreferences.getString("dateToday", null);

        isNewDay = !today.equals(dateToday);

        return isNewDay;
    }

    /**
     * Benachrichtigung, falls die Hälfte des gesetzten Schrittziels erreicht wurde.
     */
    private void showNotificationHalfGoalReached() {
        String NOTIFICATION_CHANNEL_ID_4 = "CH_ID_4";

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("halfGoalReached", true);
        editor.apply();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID_4)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Weiter so!")
                .setContentText("Du hast bereits die Hälfte deines täglichen Schrittziels " +
                        "erreicht.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify((int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE),
                mBuilder.build());
    }

    /**
     * Benachrichtigung, falls das gesetzte Schrittziel erreicht wurde bzw. mehrere Tage in Folge
     * erreicht wurde.
     */
    private void showNotificationGoalReached() {
        String NOTIFICATION_CHANNEL_ID_3 = "CH_ID_3";

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("goalReached", true);
        editor.putInt("goalReachedStreak",
                sharedPreferences.getInt("goalReachedStreak", 0) + 1);
        editor.apply();

        NotificationCompat.Builder mBuilder;

        if (sharedPreferences.getInt("goalReachedStreak", 0)  > 1) {
            mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_3)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Glückwunsch!")
                    .setContentText("Du hast dein tägliches Schrittziel "
                            + sharedPreferences.getInt("goalReachedStreak", 0)
                            + " Tage in Folge erreicht.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        } else {
            mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_3)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Glückwunsch!")
                    .setContentText("Du hast dein tägliches Schrittziel erreicht.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        notificationManager.notify((int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE),
                mBuilder.build());
    }

    /**
     * Benachrichtigung, falls sich in der letzten Stunde zu wenig bewegt wurde.
     */
    private void showNotificationExerxcise() {
        String NOTIFICATION_CHANNEL_ID_2 = "CH_ID_2";
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID_2);

        Resources res = getResources();
        String[] motivationTextsTitles = res.getStringArray(R.array.motivation_texts_titles);
        String[] motivationTextsContent = res.getStringArray(R.array.motivation_texts_content);

        Random r = new Random();

        // Erstellen eines Intents, für die Aktivität, die gestartet werden soll
        Intent resultIntent = new Intent(this, CameraActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(motivationTextsTitles[r.nextInt(motivationTextsTitles.length)])
                .setContentText(motivationTextsContent[r.nextInt(motivationTextsContent.length)])
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_baseline_check_circle_24, "Übung vorschlagen",
                        resultPendingIntent);

        notificationManager.notify((int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE),
                mBuilder.build());
    }

    /**
     * Übermittelt die Schrittzahl
     */
    private void broadcastSensorValue() {
        intent.putExtra("Counted_Step_Int", newStepCounter);
        intent.putExtra("Counted_Step", String.valueOf(newStepCounter));

        sendBroadcast(intent);
    }

}
