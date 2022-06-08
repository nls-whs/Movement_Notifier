package com.vuzix.nextlevelsports.activities;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vuzix.hud.actionmenu.ActionMenuActivity;
import com.vuzix.nextlevelsports.BladeSampleApplication;
import com.vuzix.nextlevelsports.R;

import com.vuzix.nextlevelsports.services.StepCounterService;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends ActionMenuActivity {

    /* TextViews */
    private TextView textView;
    private TextView stepsTaken;
    private TextView tvDistance;
    private TextView tvStepGoal;
    private TextView tvExercisesDone;
    private TextView tvDate;
    private TextView tvTimeMoving;
    private TextView tvCalories;

    boolean halfGoalReached = false;
    boolean goalReached = false;

    private Handler mHandler;
    private Runnable _timer1;
    private int stepCounter = 0;
    private int lastStep = 0;
    private boolean showedGoalReach = false;
    private int exercisesDoneToday;

    private boolean isRunning = false;

    private SimpleDateFormat sdf;

    private Intent intent;
    boolean isServiceStopped;

    String countedStep;
    int countedSteps;

    boolean isNewDay = false;

    ProgressBar progressBarSteps;

    public static final String SHARED_PREFS = "sharedPrefs";

    private double distance;
    private int calories;

    /**
     *
     * @param savedInstanceState
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Überprüft, ob der Lightmode aktiviert ist und wählt je nach Helligkeit das passende
         * Layout aus.
         */
        boolean isLightMode = isLightMode(getContext());
        if(isLightMode) {
            setContentView(R.layout.activity_main_light);
        } else {
            setContentView(R.layout.activity_main);
        }

        // Initialisierung der einzelnen Layout-Elemente
        textView = findViewById(R.id.tvStepsTaken);
        tvDistance = findViewById(R.id.tvDistanceMain);
        tvStepGoal = findViewById(R.id.stepGoalGoal);
        tvExercisesDone = findViewById(R.id.tvExercisesDone);
        tvDate = findViewById(R.id.tvDate);
        tvTimeMoving = findViewById(R.id.tvTimeMoving);
        tvCalories = findViewById(R.id.tvCalories);
        progressBarSteps = findViewById(R.id.progress_bar_steps_main);

        // Lädt die erzielten Tagesfortschritte aus den SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        int stepsToday = sharedPreferences.getInt("stepsToday", 0);
        int stepGoal = sharedPreferences.getInt("stepGoal", 10000);

        // Tagesaktuell durchgeführte Anzahl an Übungen
        exercisesDoneToday = sharedPreferences.getInt("exercisesDone", 0);
        tvExercisesDone.setText(exercisesDoneToday + "");

        // Tagesaktuelle Schrittzahl sowie Fortschritt des Tagesziels
        DecimalFormat nf = new DecimalFormat();
        textView.setText(nf.format(stepsToday).replace(",", ".") + "");
        tvStepGoal.setText("/ " + nf.format(stepGoal)
                .replace(",", ".") + " Schritten");
        progressBarSteps.setProgress(stepsToday * 100 / stepGoal);

        // Stellt das aktuelle Datum auf der MainActivity dar.
        sdf = new SimpleDateFormat("dd.MM.yy", Locale.GERMANY);
        String currentDate = sdf.format(new Date());
        String dayOfTheWeek = new SimpleDateFormat("EE", Locale.GERMANY).format(new Date());
        tvDate.setText(dayOfTheWeek + ", " + currentDate);

        // Ungefähr zurückgelegte Distanz durch Errechnung
        tvDistance.setText(calculateDistance() + "");

        tvTimeMoving.setText("00:00");
        tvCalories.setText(calculateCalories() + "");

        // Falls der Service zum Schritte zählen nicht aktiv ist, wird dieser aktiviert.
        intent = new Intent(this, StepCounterService.class);
        if (!isMyServiceRunning(StepCounterService.class)) {
            startService(new Intent(getBaseContext(), StepCounterService.class));
        }

        /*
        try {
            registerReceiver(broadcastReceiver,
                    new IntentFilter(StepCounterService.BROADCAST_ACTION));
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
         */

    }

    /**
     * Erstellung des Action Menüs.
     * @param menu Menü
     * @return true
     */
    @Override
    protected boolean onCreateActionMenu(Menu menu) {
        super.onCreateActionMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Ruft bei Erhalt eines dementsprechenden Broadcasts Methode zum Updaten der View auf.
     */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateViews(intent);
        }
    };

    /**
     * Prüft, ob Lightmode aktiviert ist.
     * @param context
     * @return
     */
    private static boolean isLightMode(Context context) {
        return ((BladeSampleApplication)context.getApplicationContext()).isLightMode();
    }

    /**
     * Überprüft, ob der übergeben Service aktiv ist.
     * @param serviceClass Übergebene Service Klasse.
     * @return true / false
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update der Views bei eingegangenem Intent.
     * @param intent Intent
     */
    private void updateViews(Intent intent) {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int stepGoal = sharedPreferences.getInt("stepGoal", 10000);
        int stepsToday = sharedPreferences.getInt("stepsToday", 0);

        /*
        int height = sharedPreferences.getInt("height", 180);
        int stepsLastHour = sharedPreferences.getInt("stepsLastHour", 0);

        sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        String today = sdf.format(new Date());
        String dateToday = sharedPreferences.getString("dateToday", null);
         */

        countedSteps = intent.getIntExtra("Counted_Step_Int", 0);

        // Setzt den Maximalwert der Progressbar auf 100%.
        progressBarSteps.setMax(100);

        if(countedSteps == stepsToday) {
            textView.setText(countedSteps + "");
            progressBarSteps.setProgress(countedSteps * 100 / stepGoal);
        }

        tvDistance.setText(calculateDistance() + "");
    }

    /**
     * Errechnet die ungefähr zurückgelegte Distanz anhand der durch die Körpergröße errechneten
     * Schrittlänge.
     * @return Distanz
     */
    private double calculateDistance() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        int stepsToday = sharedPreferences.getInt("stepsToday", 0);
        int height = sharedPreferences.getInt("height", 180);

        if (sharedPreferences.getString("gender", "Männlich").equals("Männlich")) {
            distance = (height * 0.415 * stepsToday) / 100000;
        } else {
            distance = (height * 0.413 * stepsToday) / 100000;
        }
        distance = Math.round(distance * 100.0) / 100.0;

        return distance;
    }

    /**
     * Errechnet den ungefähren Kalorienverbrauch anhand der Schrittzahl.
     * @return Kalorien
     */
    private int calculateCalories() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        int stepsToday = sharedPreferences.getInt("stepsToday", 0);

        calories = (int) (0.04 * stepsToday);

        return calories;
    }

    /**
     * Action Menü immer sichtbar durch Überschreiben der Methode.
     * @return true
     */
    @Override
    protected boolean alwaysShowActionMenu() {
        return false;
    }

    /**
     * Bottom Darstellung des Menüs durch Überschreiben der Methode.
     * @return CENTER
     */
    @Override
    protected int getActionMenuGravity() {
        return Gravity.BOTTOM;
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    protected boolean onActionItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.camera) {
            startActivity(new Intent(this, CameraActivity.class));
        }

        return super.onActionItemSelected(item);
    }

    /**
     * Bei Fortsetzen der Aktivität wird die Oberfläche refresht.
     */
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int lastTimeStarted = settings.getInt("last_time_started", -1);
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_YEAR);

        if (today != lastTimeStarted) {

            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("last_time_started", today);
            editor.apply();

            this.recreate();
        }
    }

    /**
     * Bei Neustart der Aktivität werden UI-Elemente aktualisiert.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onRestart() {
        super.onRestart();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        exercisesDoneToday = sharedPreferences.getInt("exercisesDone", 0);
        tvExercisesDone.setText(exercisesDoneToday + "");
    }

    /**
     * Setzt das Menü mit dem Index 1 als Startoption fest.
     * @return Index.
     */
    @Override
    protected int getDefaultAction() {
        return 1;
    }

}