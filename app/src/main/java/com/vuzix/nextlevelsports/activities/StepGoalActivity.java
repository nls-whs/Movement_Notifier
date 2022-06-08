package com.vuzix.nextlevelsports.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;

import com.vuzix.hud.actionmenu.ActionMenuActivity;
import com.vuzix.nextlevelsports.BladeSampleApplication;
import com.vuzix.nextlevelsports.R;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Lässt den Nutzer sein individuelles Schrittziel einstellen.
 */
public class StepGoalActivity extends ActionMenuActivity {

    public static final String SHARED_PREFS = "sharedPrefs";

    private int stepGoal = 0;

    private String[] numberValues;

    private NumberPicker npStepGoal;

    private boolean isLightMode;

    private static Context context;

    /**
     * Erstellt die Oberfläche der Activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isLightMode = isLightMode(getContext());

        if(isLightMode) {
            setContentView(R.layout.activity_step_goal_light);
        } else {
            setContentView(R.layout.activity_step_goal);
        }

        StepGoalActivity.context = getApplicationContext();

        int numberOfValues = 100;
        numberValues = new String[numberOfValues];

        loadStepGoal();

        for (int i=0; i < numberOfValues; i++) {
            numberValues[i] = String.valueOf(500  * (i+1));
        }

        npStepGoal = findViewById(R.id.numberPickerStepGoal);
        npStepGoal.setMinValue(0);
        npStepGoal.setMaxValue(99);
        npStepGoal.setDisplayedValues(numberValues);
        npStepGoal.setWrapSelectorWheel(false);

        if (stepGoal > 0) {
            npStepGoal.setValue(Arrays.asList(numberValues).indexOf(Integer.toString(stepGoal)));
        } else {
            // Setzt einen Standardwert von 10.000 Schritten
            npStepGoal.setValue(19);
        }

        setDividerColor(npStepGoal);
    }

    /**
     * Erstellt das Menü der Activity.
     * @param menu Menü
     * @return true
     */
    @Override
    protected boolean onCreateActionMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_disabled_back, menu);

        MenuItem backButton = menu.getItem(0).setVisible(false);

        return true;
    }

    /**
     * Bottom Darstellung des Menüs durch Überschreiben der Methode.
     * @return CENTER
     */
    @Override
    protected int getActionMenuGravity() { return Gravity.BOTTOM; }

    /**
     * Methode zum Auswählen, ob das ActionMenu immer dargestellt werden soll.
     * @return true / false.
     */
    @Override
    protected boolean alwaysShowActionMenu() { return true; }

    /**
     * Prüft, ob Lightmode aktiviert ist.
     * @param context Context
     * @return true / false
     */
    private static boolean isLightMode(Context context) {
        return ((BladeSampleApplication)context.getApplicationContext()).isLightMode();
    }

    /**
     * Gibt den Context der Activity zurück.
     * @return Context.
     */
    private static Context getAppContext() {
        return StepGoalActivity.context;
    }

    /**
     * Legt die Farbe des Dividers des NumberPickers fest.
     * @param picker Numberpicker.
     */
    private static void setDividerColor(NumberPicker picker) {
        if (picker == null)
            return;

        final int count = picker.getChildCount();

        if(isLightMode(getAppContext())) {
            for (int i = 0; i < count; i++) {
                try {
                    Field dividerField = picker.getClass().getDeclaredField("mSelectionDivider");
                    dividerField.setAccessible(true);
                    ColorDrawable colorDrawable
                            = new ColorDrawable(picker.getResources().getColor(R.color.black));
                    colorDrawable.setAlpha(90);
                    dividerField.set(picker, colorDrawable);
                    picker.invalidate();
                } catch (Exception e) {
                    Log.w("setDividerColor", e);
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                try {
                    Field dividerField = picker.getClass().getDeclaredField("mSelectionDivider");
                    dividerField.setAccessible(true);
                    ColorDrawable colorDrawable
                            = new ColorDrawable(picker.getResources().getColor(R.color.vuzix_green));
                    colorDrawable.setAlpha(90);
                    dividerField.set(picker, colorDrawable);
                    picker.invalidate();
                } catch (Exception e) {
                    Log.w("setDividerColor", e);
                }
            }
        }

    }

    /**
     * Befasst sich mit den Eingaben des Touch-Bedienfelds.
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_ENTER) {
            saveStepGoal();
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Speichert das ausgewählte Schrittziel
     */
    private void saveStepGoal() {
        SharedPreferences sharedPreferences
                = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        stepGoal = Integer.parseInt(numberValues[npStepGoal.getValue()]);

        editor.putInt("stepGoal", stepGoal);

        editor.apply();

        this.finish();
    }

    /**
     * Lädt das gespeicherte Schrittziel
     */
    private void loadStepGoal() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        stepGoal = sharedPreferences.getInt("stepGoal", 0);
    }

}
