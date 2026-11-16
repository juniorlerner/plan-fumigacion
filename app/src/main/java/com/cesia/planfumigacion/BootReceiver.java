package com.cesia.planfumigacion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Al reiniciar el celular se reprograman los avisos del plan guardado.
        String base = Plan.getBase(context);
        if (base != null) {
            Plan.programarAvisos(context, base);
        }
    }
}
