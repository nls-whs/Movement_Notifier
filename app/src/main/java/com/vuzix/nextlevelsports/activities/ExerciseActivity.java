package com.vuzix.nextlevelsports.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vuzix.hud.actionmenu.ActionMenuActivity;
import com.vuzix.hud.actionmenu.DefaultActionMenuItemView;
import com.vuzix.nextlevelsports.BladeSampleApplication;
import com.vuzix.nextlevelsports.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ExerciseActivity extends ActionMenuActivity {

    // SharedPreferences
    private static final String SHARED_PREFS = "sharedPrefs";

    // TextViews
    private TextView tvExerciseDescription;
    private TextView tvExerciseName;
    private TextView textView;

    private String value;

    private ProgressBar progressBar;

    private Handler handler = new Handler();

    private Runnable runnable;

    private SwitchMenuItemView switchMenuItemView;

    private boolean timerRunning = false;

    private CountDownTimer countDownTimer;

    /**
     * Erstellt die Oberfläche der ExerciseActivity
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prüft, ob der Lightmode aktiviert ist, und wählt das dementsprechende Layout aus
        boolean isLightMode = isLightMode(getContext());

        if (isLightMode) {
            setContentView(R.layout.activity_exercise_light);
        } else {
            setContentView(R.layout.activity_exercise);
        }

        progressBar = findViewById(R.id.barTimer);
        textView = findViewById(R.id.tvTimer);

        /*
         * Lädt die einzelnen Übungen und deren Erläuterungen. Wählt dann eine zufällige, zur
         * Umgebung passende Übung aus.
         */
        Resources res = getResources();
        String[] exerciseNames = res.getStringArray(R.array.exercise_names);
        String[] exerciseDescriptions = res.getStringArray(R.array.exercise_descriptions);

        Map<String, String> exercisesMap = new HashMap<String, String>();
        int i = 0;
        while (i < exerciseNames.length && i < exerciseDescriptions.length) {
            exercisesMap.put(exerciseNames[i], exerciseDescriptions[i]);
            i++;
        }
        String[] exercises;

        tvExerciseDescription = (TextView) findViewById(R.id.exerciseDescriptionTV);
        tvExerciseName = (TextView) findViewById(R.id.exerciseTV);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            value = extras.getString("situation").toLowerCase();
        }

        int resourceId = getResources().getIdentifier(value,
                "array", getContext().getPackageName());

        exercises = res.getStringArray(resourceId);

        Random r = new Random();

        String exercise = exercises[r.nextInt(exercises.length)];

        tvExerciseDescription.setText(exercisesMap.get(exercise));
        tvExerciseName.setText(exercise);

    }

    /**
     * Erstellt das ActionMenu.
     * @param menu Menu
     * @return Menu
     */
    @Override
    protected boolean onCreateActionMenu(Menu menu) {
        super.onCreateActionMenu(menu);

        getMenuInflater().inflate(R.menu.menu_exercise, menu);

        MenuItem previewMenuItem = menu.findItem(R.id.timer);
        previewMenuItem.setActionView(switchMenuItemView = new SwitchMenuItemView(this));

        updateMenuItems();

        return true;
    }

    /**
     * Setzt die Position des ActionMenus
     * @return BOTTOM
     */
    @Override
    protected int getActionMenuGravity() { return Gravity.BOTTOM; }

    /**
     * Fokussiert das Menü-Element mit dem Index 1 bei Erstellen der App.
     * @return
     */
    @Override
    protected int getDefaultAction() {
        return 1;
    }

    /**
     * Prüft, ob Lightmode aktiviert ist.
     * @param context Context
     * @return true / false
     */
    private static boolean isLightMode(Context context) {
        return ((BladeSampleApplication)context.getApplicationContext()).isLightMode();
    }

    /**
     * Bricht den CountDownTimer bei Betätigung der Zurück-Taste ab.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(countDownTimer != null) {
            countDownTimer.cancel();
        }

    }


    /**
     * Befasst sich mit den Aktionen bei Betätigung eines Menü-Elements
     * @param item Betätigtes Menü-Element
     * @return Item
     */
    @Override
    protected boolean onActionItemSelected(MenuItem item) {

        SharedPreferences sharedPreferences
                = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int exercisesDoneToday = sharedPreferences.getInt("exercisesDone", 0);

        int id = item.getItemId();

        if (id == R.id.exerciseDone) {
            editor.putInt("exercisesDone", exercisesDoneToday + 1);
            editor.apply();
            this.finish();
            Intent intent = new Intent(ExerciseActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            countDownTimer.cancel();
            startActivity(intent);
        } else if (id == R.id.timer) {

            // Überprüft, ob der Timer aktiv ist
            if (!timerRunning) {
                timerRunning = true;
                closeActionMenu(true);

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                tvExerciseDescription.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                double max = 60000;

                // Startet CountDownTimer
                countDownTimer = new CountDownTimer(60000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        textView.setText(millisUntilFinished / 1000 + "");
                        progressBar.setProgress((int)(((double) millisUntilFinished / max) * 100));
                    }

                    public void onFinish() {
                        tvExerciseDescription.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        textView.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Weiter geht's!", Toast.LENGTH_SHORT).show();
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(VibrationEffect.createOneShot(200,
                                    VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            v.vibrate(200);
                        }
                    }

                }.start();

                switchMenuItemView.setSwitchState(false);
            } else if (timerRunning == true) {
                timerRunning = false;
                countDownTimer.cancel();
                tvExerciseDescription.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                switchMenuItemView.setSwitchState(true);
            }

        }

        return super.onActionItemSelected(item);
    }

    /**
     * Updatet die Menü-Items.
     */
    private void updateMenuItems() {
        switchMenuItemView.setSwitchState(true);
    }

    /**
     * Erlaubt es, das Menüelement für den Timer live anzupassen.
     */
    private static class SwitchMenuItemView extends DefaultActionMenuItemView {

        public SwitchMenuItemView(Context context) {
            super(context);
        }

        private void setSwitchState(boolean on) {
            if (on) {
                setIcon(getResources().getDrawable(R.drawable.ic_timer,
                        getContext().getTheme()));
                setTitle("Starte Timer 1 Minute");
            } else {
                setIcon(getResources().getDrawable(R.drawable.ic_timer,
                        getContext().getTheme()));
                setTitle("Timer beenden");
            }
        }
    }

}
