package com.cesia.planfumigacion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Al reiniciar el celular se reprograman los avisos del plan guardado.
        Calendar b = Plan.base(context);
        if (b != null) {
            Plan.programarAvisos(context, b);
        }
    }
}
