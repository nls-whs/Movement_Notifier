package com.vuzix.nextlevelsports.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vuzix.nextlevelsports.services.StepCounterService;

/**
 * Wird benachrichtigt, nachdem das Gerät gestartet wurde und startet den Schrittzähler
 */
public class OnBootCompleteReceiver extends BroadcastReceiver {

    /**
     * Startet den Schrittzähler-Service, nachdem das Gerät gestartet wurde.
     * @param context Context
     * @param intent Gerät gestartet
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, StepCounterService.class);
            context.startService(serviceIntent);
        }
    }

}
