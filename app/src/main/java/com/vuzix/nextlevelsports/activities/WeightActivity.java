package com.vuzix.nextlevelsports.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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

public class WeightActivity extends ActionMenuActivity {

    public static final String SHARED_PREFS = "sharedPrefs";

    private int weightKg;
    private int weightG;

    private NumberPicker npKg;
    private NumberPicker npG;

    private MenuItem backButton;

    private boolean isLightMode;

    private static Context context;


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isLightMode = isLightMode(getContext());

        if(isLightMode) {
            setContentView(R.layout.activity_weight_light);
        } else {
            setContentView(R.layout.activity_weight);
        }

        loadWeight();

        WeightActivity.context = getApplicationContext();

        npKg = findViewById(R.id.numberPickerKG);
        npG = findViewById(R.id.numberPickerG);

        npKg.setMinValue(20);
        npKg.setMaxValue(200);
        npKg.setWrapSelectorWheel(false);

        npG.setMinValue(0);
        npG.setMaxValue(9);
        npG.setWrapSelectorWheel(false);

        if (weightKg > 0) {
            npKg.setValue(weightKg);
            npG.setValue(weightG);
        }

        setDividerColor(npKg);
        setDividerColor(npG);
    }

    @Override
    protected boolean onCreateActionMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_disabled_back, menu);

        backButton = menu.getItem(0).setVisible(false);

        return true;
    }

    /**
     * Prüft, ob Lightmode aktiviert ist.
     * @param context
     * @return
     */
    private static boolean isLightMode(Context context) {
        return ((BladeSampleApplication)context.getApplicationContext()).isLightMode();
    }

    private static Context getAppContext() {
        return WeightActivity.context;
    }

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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_ENTER) {
            saveWeight();
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            onLeft();
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            onRight();
        }

        return super.onKeyUp(keyCode, event);
    }

    private void onLeft() {
        Drawable drawable = getResources().getDrawable(R.drawable.border, null);
        Drawable drawableLight = getResources().getDrawable(R.drawable.border_light, null);
        if(isLightMode) {
            if(npKg.getBackground() != drawableLight) {
                npKg.setBackground(drawableLight);
                npG.setBackground(null);
            }
        } else {
            if(npKg.getBackground() != drawable) {
                npKg.setBackground(drawable);
                npG.setBackground(null);
            }
        }

    }

    private void onRight() {
        Drawable drawable = getResources().getDrawable(R.drawable.border, null);
        Drawable drawableLight = getResources().getDrawable(R.drawable.border_light, null);
        if(isLightMode) {
            if(npG.getBackground() != drawableLight) {
                npG.setBackground(drawableLight);
                npKg.getBackground().setAlpha(0);
            }
        } else {
            if(npG.getBackground() != drawable) {
                npG.setBackground(drawable);
                npKg.getBackground().setAlpha(0);
            }
        }

    }

    public void saveWeight() {
        SharedPreferences sharedPreferences
                = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        weightKg = npKg.getValue();
        weightG = npG.getValue();

        editor.putInt("weightKg", weightKg);
        editor.putInt("weightG", weightG);

        editor.apply();

        this.finish();
    }

    public void loadWeight() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        weightKg = sharedPreferences.getInt("weightKg", 80);
        weightG = sharedPreferences.getInt("weightG", 0);
    }

    @Override
    protected int getActionMenuGravity() { return Gravity.BOTTOM; }

    @Override
    protected boolean alwaysShowActionMenu() { return true; }
}
