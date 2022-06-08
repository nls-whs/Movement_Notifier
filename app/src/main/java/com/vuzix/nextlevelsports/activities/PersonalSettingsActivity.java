package com.vuzix.nextlevelsports.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.vuzix.hud.actionmenu.ActionMenuActivity;
import com.vuzix.nextlevelsports.BladeSampleApplication;
import com.vuzix.nextlevelsports.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Einstellung zur Anpassung der einzelnen Körperwerte.
 */
public class PersonalSettingsActivity extends ActionMenuActivity {

    public static final String SHARED_PREFS = "sharedPrefs";

    private MenuItem genderMenuItem;
    private MenuItem birthdayMenuItem;
    private MenuItem heightMenuItem;
    private MenuItem weightMenuItem;
    private MenuItem backButton;

    private TextView genderTv;
    private TextView birthdayTv;
    private TextView heightTv;
    private TextView weightTv;
    private TextView bmiTv;

    private TextView mainTitle;

    /**
     *
     * @param savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isLightMode = isLightMode(getContext());

        if(isLightMode) {
            setContentView(R.layout.activity_personal_settings_light);
        } else {
            setContentView(R.layout.activity_personal_settings);
        }

        mainTitle = findViewById(R.id.personal_settings_title);

        genderTv = findViewById(R.id.genderTv2);
        birthdayTv = findViewById(R.id.birthdayTv2);
        heightTv = findViewById(R.id.heightTv2);
        weightTv = findViewById(R.id.weightTv2);
        bmiTv = findViewById(R.id.BMITv2);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        String gender = sharedPreferences.getString("gender", "Männlich");
        if(gender.equals("Männlich")) {
            genderTv.setText("Männlich");
        } else {
            genderTv.setText("Weiblich");
        }

        String dateOfBirth = sharedPreferences.getString("birthday", "01.01.2000");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Date d = null;
        try {
            d = sdf.parse(dateOfBirth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - c.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < c.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();
        birthdayTv.setText(ageS + " Jahre");

        int height = sharedPreferences.getInt("height", 180);
        heightTv.setText(height + " cm");

        int weightKg = sharedPreferences.getInt("weightKg", 80);
        int weightG = sharedPreferences.getInt("weightG", 0);
        weightTv.setText(weightKg + "." + weightG + " kg");

        double bmi = ((double) weightKg + (double) weightG / 10)
                / ((double) height / 100 * (double) height / 100);
        bmiTv.setText((int) bmi + "");

    }

    /**
     * Erstellt das Action Menu
     * @param menu
     * @return
     */
    @Override
    protected boolean onCreateActionMenu(Menu menu) {
        super.onCreateActionMenu(menu);
        getMenuInflater().inflate(R.menu.menu_personal_settings, menu);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String gender = sharedPreferences.getString("gender", "Männlich");

        genderMenuItem = menu.findItem(R.id.gender);
        birthdayMenuItem = menu.findItem(R.id.birthday);
        heightMenuItem = menu.findItem(R.id.height);
        weightMenuItem = menu.findItem(R.id.weight);
        backButton = menu.getItem(0);

        if (gender != null) {
            if (gender.equals("Männlich")) {
                genderMenuItem.setIcon(R.drawable.ic_human_male);
            } else {
                genderMenuItem.setIcon(R.drawable.ic_human_female);
            }
        }

        return true;
    }

    /**
     * Bei Neustart der Activity wird die Oberfläche aktualisiert.
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    /**
     * Prüft, ob Lightmode aktiviert ist.
     * @param context Context
     * @return Context
     */
    private static boolean isLightMode(Context context) {
        return ((BladeSampleApplication)context.getApplicationContext()).isLightMode();
    }

    /**
     * Menü immer sichtbar durch Überschreiben der Methode.
     * @return true
     */
    @Override
    protected boolean alwaysShowActionMenu() {
        return true;
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
     * Fokussiert das Element mit dem übergebenen Index bei Start der Activity.
     * @return Index
     */
    @Override
    protected int getDefaultAction() {
        return 1;
    }

    /**
     * Befasst sich mit den Menü-Elementen, die fokussiert werden.
     * @param item Menüelement.
     */
    @Override
    protected void onActionItemFocused(MenuItem item) {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        int id = item.getItemId();

        if (id == R.id.gender) {
            String gender = sharedPreferences.getString("gender", "Männlich");
            if (gender != null) {
                mainTitle.setText(gender);
                if(gender.equals("Männlich")) {
                    genderMenuItem.setIcon(R.drawable.ic_human_male);
                } else {
                    genderMenuItem.setIcon(R.drawable.ic_human_female);
                }
            } else {
                mainTitle.setText("");
            }
        } else if (id == R.id.birthday) {
            String date = sharedPreferences.getString("birthday", "01.01.2000");
            if (date != null) {
                mainTitle.setText(date);
            } else {
                mainTitle.setText("");
            }
        } else if (id == R.id.height) {
            int height = sharedPreferences.getInt("height", 180);
            if(height > 0) {
                mainTitle.setText(height + " cm");
            } else {
                mainTitle.setText("");
            }
        } else if (id == R.id.weight) {
            int weightKg = sharedPreferences.getInt("weightKg", 80);
            int weightG = sharedPreferences.getInt("weightG", 0);
            if(weightKg > 0) {
                mainTitle.setText(weightKg + "." + weightG + " kg");
            } else {
                mainTitle.setText("");
            }
        } else if (id == backButton.getItemId()) {
            mainTitle.setText("");
        }
    }

    /**
     * Befasst sich mit den Aktionen bei der Betätigung eines Menüelements.
     * @param item
     * @return
     */
    @Override
    protected boolean onActionItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.gender) {
            startActivity(new Intent(this, GenderActivity.class));
        } else if (id == R.id.birthday) {
            startActivity(new Intent(this, BirthdayActivity.class));
        } else if (id == R.id.height) {
            startActivity(new Intent(this, HeightActivity.class));
        } else if (id == R.id.weight) {
            startActivity(new Intent(this, WeightActivity.class));
        }

        return super.onActionItemSelected(item);
    }

}
