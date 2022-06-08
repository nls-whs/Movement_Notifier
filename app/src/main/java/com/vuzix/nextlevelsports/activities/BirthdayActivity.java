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
import android.widget.DatePicker;
import android.widget.NumberPicker;

import com.vuzix.hud.actionmenu.ActionMenuActivity;
import com.vuzix.nextlevelsports.BladeSampleApplication;
import com.vuzix.nextlevelsports.R;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Lässt den Benutzer sein Geburtsdatum speichern.
 */
public class BirthdayActivity extends ActionMenuActivity {

    // SharedPreferences
    public static final String SHARED_PREFS = "sharedPrefs";

    // Drawables
    private static Drawable drawable;
    private static Drawable drawableLight;

    // Context
    private static Context context;

    // DatePicker und Datum
    private DatePicker datePicker;
    private String date;

    // Lightmode ja / nein
    boolean isLightMode;

    /**
     * Erstellt die Oberfläche der BirthdayActivity.
     * @param savedInstanceState savedInstaneState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prüft, ob der Lightmode aktiviert ist und wählt dementsprechend das passende Layout aus
        isLightMode = isLightMode(getContext());

        if(isLightMode) {
            setContentView(R.layout.activity_birthday_light);
        } else {
            setContentView(R.layout.activity_birthday);
        }

        BirthdayActivity.context = getApplicationContext();

        // Lädt das Geburtsdatum
        loadDate();

        drawable = getResources().getDrawable(R.drawable.border, null);
        drawableLight = getResources().getDrawable(R.drawable.border_light, null);

        datePicker = findViewById(R.id.datePicker);
        datePicker.setMaxDate(new Date().getTime());

        colorizeDatePicker(datePicker);

        if (date != null) {

            int day = Integer.parseInt(date.substring(0, 2));
            int month = Integer.parseInt(date.substring(3, 5)) - 1;
            int year = Integer.parseInt(date.substring(6, 10));

            datePicker.updateDate(year, month, day);

        }

    }

    /**
     * Erstellt das ActionMenu.
     * @param menu Menu
     * @return true
     */
    @Override
    protected boolean onCreateActionMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_disabled_back, menu);

        MenuItem backButton = menu.getItem(0).setVisible(false);

        return true;
    }

    /**
     * Prüft, ob Lightmode aktiviert ist.
     * @param context Context der Birthday Activity
     * @return Lightmode On / Lightmode Off
     */
    private static boolean isLightMode(Context context) {
        return ((BladeSampleApplication)context.getApplicationContext()).isLightMode();
    }

    /**
     * Gibt den Context der BirthdayActivity zurück.
     * @return Context der BirthdayActivity.
     */
    private static Context getAppContext() {
        return BirthdayActivity.context;
    }

    /**
     * Bei Swipe-Bewegung nach links wird dementsprechend das von der aktuellen Position aus linke
     * Element angewählt.
     */
    private void onLeft() {
        Resources system = Resources.getSystem();
        int dayId = system.getIdentifier("day", "id", "android");
        int monthId = system.getIdentifier("month", "id", "android");
        int yearId = system.getIdentifier("year", "id", "android");

        NumberPicker dayPicker = (NumberPicker) datePicker.findViewById(dayId);
        NumberPicker monthPicker = (NumberPicker) datePicker.findViewById(monthId);
        NumberPicker yearPicker = (NumberPicker) datePicker.findViewById(yearId);

        Drawable drawable = getResources().getDrawable(R.drawable.border, null);
        Drawable drawableLight = getResources().getDrawable(R.drawable.border_light, null);

        if(isLightMode) {
            if(dayPicker.getBackground() == null && monthPicker.getBackground() == null) {
                dayPicker.setBackground(drawableLight);
                yearPicker.setBackground(null);
            } else {
                monthPicker.setBackground(drawableLight);
                dayPicker.setBackground(null);
            }
        } else {
            if(dayPicker.getBackground() == null && monthPicker.getBackground() == null) {
                dayPicker.setBackground(drawable);
                yearPicker.setBackground(null);
            } else {
                monthPicker.setBackground(drawable);
                dayPicker.setBackground(null);
            }
        }

    }

    /**
     * Bei Swipe-Bewegung nach rechts wird dementsprechend das von der aktuellen Position aus rechte
     * Element angewählt.
     */
    private void onRight() {
        Resources system = Resources.getSystem();
        int dayId = system.getIdentifier("day", "id", "android");
        int monthId = system.getIdentifier("month", "id", "android");
        int yearId = system.getIdentifier("year", "id", "android");

        NumberPicker dayPicker = (NumberPicker) datePicker.findViewById(dayId);
        NumberPicker monthPicker = (NumberPicker) datePicker.findViewById(monthId);
        NumberPicker yearPicker = (NumberPicker) datePicker.findViewById(yearId);

        Drawable drawable = getResources().getDrawable(R.drawable.border, null);
        Drawable drawableLight = getResources().getDrawable(R.drawable.border_light, null);

        /*
         * Überprüft, ob der Lightmode aktiviert ist, und wählt dementsprechend den passenden
         * Hintergrund aus.
         */
        if(isLightMode) {
            if(dayPicker.getBackground() == null && yearPicker.getBackground() == null) {
                dayPicker.setBackground(drawableLight);
                monthPicker.setBackground(null);
            } else {
                yearPicker.setBackground(drawableLight);
                dayPicker.setBackground(null);
            }
        } else {
            if(dayPicker.getBackground() == null && yearPicker.getBackground() == null) {
                dayPicker.setBackground(drawable);
                monthPicker.setBackground(null);
            } else {
                yearPicker.setBackground(drawable);
                dayPicker.setBackground(null);
            }
        }

    }

    /**
     * Passt die Farben des Datepickers an.
     * @param datePicker Datepicker.
     */
    public static void colorizeDatePicker(DatePicker datePicker) {
        Resources system = Resources.getSystem();
        int dayId = system.getIdentifier("day", "id", "android");
        int monthId = system.getIdentifier("month", "id", "android");
        int yearId = system.getIdentifier("year", "id", "android");

        NumberPicker dayPicker = (NumberPicker) datePicker.findViewById(dayId);
        NumberPicker monthPicker = (NumberPicker) datePicker.findViewById(monthId);
        NumberPicker yearPicker = (NumberPicker) datePicker.findViewById(yearId);

        setDividerColor(dayPicker);
        setDividerColor(monthPicker);
        setDividerColor(yearPicker);

        /*
         * Überprüft, ob der Lightmode aktiviert ist, und wählt dementsprechend den passenden
         * Hintergrund aus.
         */
        if (isLightMode(getAppContext())) {
            monthPicker.setBackground(drawableLight);
        } else {
            monthPicker.setBackground(drawable);
        }

        dayPicker.setBackground(null);
        yearPicker.setBackground(null);

    }

    /**
     * Passt die Farben der Divider im Datepicker an.
     * @param picker Einzelne Auswahlmöglichkeit des Datepickers.
     */
    private static void setDividerColor(NumberPicker picker) {
        if (picker == null)
            return;

        final int count = picker.getChildCount();
        if (isLightMode(getAppContext())) {
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
     * Handelt die Eingaben des Touchfelds.
     * @param keyCode KeyCode
     * @param event Event
     * @return Aktionen, die bei den unterschiedlichen Eingaben passieren.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_ENTER) {
            saveDate();
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            onLeft();
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            onRight();
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Speichert das ausgewählte Geburtsdatum in den SharedPreferences.
     */
    public void saveDate() {
        SharedPreferences sharedPreferences
                = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        String formattedDate = dateFormat.format(calendar.getTime());

        editor.putString("birthday", formattedDate);

        editor.apply();

        this.finish();
    }

    /**
     * Lädt das gespeicherte Geburtsdatum aus den SharedPreferences. Ist dort keins abgespeichert,
     * wird der 01.01.2000 als Standardwert dargestellt.
     */
    public void loadDate() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        date = sharedPreferences.getString("birthday", "01.01.2000");
    }

    /**
     * Gibt die Position des ActionMenus an
     * @return Position
     */
    @Override
    protected int getActionMenuGravity() { return Gravity.BOTTOM; }

    /**
     * Auswahl, ob das ActionMenu immer dargestellt werden soll.
     * @return Ja / Nein
     */
    @Override
    protected boolean alwaysShowActionMenu() { return true; }
}
