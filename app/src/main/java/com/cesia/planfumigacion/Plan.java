package com.cesia.planfumigacion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Plan {

    public static class Evento {
        public String titulo;
        public String detalle;
        public String tipo; // base, control, foliar, suelo, engrose, carencia, cosecha
        public Calendar fecha;

        Evento(String titulo, String detalle, String tipo, Calendar fecha) {
            this.titulo = titulo;
            this.detalle = detalle;
            this.tipo = tipo;
            this.fecha = fecha;
        }
    }

    private static final String PREFS = "plan";
    private static final Locale ES = new Locale("es", "ES");

    public static void guardarBase(Context ctx, int anio, int mes, int dia) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt("anio", anio).putInt("mes", mes).putInt("dia", dia).apply();
    }

    public static Calendar base(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!p.contains("anio")) return null;
        Calendar c = Calendar.getInstance();
        c.set(p.getInt("anio", 2027), p.getInt("mes", 0), p.getInt("dia", 1), 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    private static Calendar sumar(Calendar base, int dias) {
        Calendar c = (Calendar) base.clone();
        c.add(Calendar.DAY_OF_YEAR, dias);
        return c;
    }

    public static List<Evento> eventos(Calendar base) {
        List<Evento> l = new ArrayList<>();
        String ctrl = "Insecticida: en la MAÑANA\nFungicida: en la TARDE";
        l.add(new Evento("Cosecha base", "Inicio del nuevo ciclo", "base", sumar(base, 0)));
        l.add(new Evento("Control 1 · Insecticida + Fungicida", ctrl, "control", sumar(base, 5)));
        l.add(new Evento("Nutrición Foliar 1", "Abono foliar para el brote", "foliar", sumar(base, 9)));
        l.add(new Evento("Control 2 · Insecticida + Fungicida", ctrl, "control", sumar(base, 19)));
        l.add(new Evento("Nutrición Foliar 2 (abono)", "Abono foliar", "foliar", sumar(base, 23)));
        l.add(new Evento("Nutrición al suelo", "Abono al suelo", "suelo", sumar(base, 32)));
        l.add(new Evento("Control 3 · Insecticida + Fungicida", ctrl, "control", sumar(base, 33)));
        l.add(new Evento("Nutrición 2da fase (engrose)", "Para que la hoja engrose", "engrose", sumar(base, 39)));
        l.add(new Evento("Control 4 · Insecticida + Fungicida", ctrl, "control", sumar(base, 47)));
        l.add(new Evento("Control 5 · ÚLTIMO control", ctrl, "control", sumar(base, 61)));
        l.add(new Evento("Empieza la carencia", "Sin fumigar hasta la cosecha", "carencia", sumar(base, 62)));
        l.add(new Evento("¡COSECHA FINAL!", "¡Llegó el gran día de cosechar!", "cosecha", sumar(base, 90)));
        return l;
    }

    public static String fechaCorta(Calendar c) {
        return new SimpleDateFormat("EEE d MMM", ES).format(c.getTime());
    }

    public static String fechaLarga(Calendar c) {
        return new SimpleDateFormat("EEEE d 'de' MMMM", ES).format(c.getTime());
    }

    private static String iso(Calendar c) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    // ===== Notas por tarea =====

    public static String nota(Context ctx, Calendar base, int i) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return p.getString("nota_" + iso(base) + "_" + i, "");
    }

    public static void guardarNota(Context ctx, Calendar base, int i, String texto) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("nota_" + iso(base) + "_" + i, texto).apply();
    }

    // ===== Texto para compartir por WhatsApp =====

    public static String textoParaCompartir(Calendar base) {
        StringBuilder sb = new StringBuilder();
        sb.append("🌿 *MI PLAN DE FUMIGACIÓN*\n");
        sb.append("Cosecha base: ").append(fechaLarga(base)).append("\n");
        sb.append("Cosecha final: ").append(fechaLarga(sumar(base, 90))).append("\n\n");
        for (Evento e : eventos(base)) {
            sb.append("• ").append(fechaCorta(e.fecha)).append(" — ").append(e.titulo).append("\n");
            if (e.tipo.equals("control")) {
                sb.append("   Insecticida en la mañana · Fungicida en la tarde\n");
            }
        }
        sb.append("\n🌿 ¡Buena cosecha!");
        return sb.toString();
    }

    // ===== Avisos (notificaciones) =====

    private static PendingIntent pendiente(Context ctx, int code, Evento e, String cuando) {
        Intent it = new Intent(ctx, AvisoReceiver.class);
        it.putExtra("titulo", e.titulo);
        it.putExtra("cuando", cuando);
        it.putExtra("code", code);
        return PendingIntent.getBroadcast(ctx, code, it,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void programarUno(Context ctx, AlarmManager am, Calendar fecha, int hora, PendingIntent pi) {
        Calendar t = (Calendar) fecha.clone();
        t.set(Calendar.HOUR_OF_DAY, hora);
        t.set(Calendar.MINUTE, 0);
        t.set(Calendar.SECOND, 0);
        if (t.getTimeInMillis() <= System.currentTimeMillis()) return;
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t.getTimeInMillis(), pi);
        } catch (SecurityException ex) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t.getTimeInMillis(), pi);
        }
    }

    public static void programarAvisos(Context ctx, Calendar base) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        List<Evento> evs = eventos(base);
        for (int i = 0; i < evs.size(); i++) {
            Evento e = evs.get(i);
            // Aviso el día anterior a las 18:00
            Calendar antes = (Calendar) e.fecha.clone();
            antes.add(Calendar.DAY_OF_YEAR, -1);
            programarUno(ctx, am, antes, 18, pendiente(ctx, i * 2, e, "Mañana toca: "));
            // Aviso el mismo día a las 07:00
            programarUno(ctx, am, e.fecha, 7, pendiente(ctx, i * 2 + 1, e, "Hoy toca: "));
        }
    }

    public static void cancelarAvisos(Context ctx, Calendar base) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        List<Evento> evs = eventos(base);
        for (int i = 0; i < evs.size(); i++) {
            Evento e = evs.get(i);
            am.cancel(pendiente(ctx, i * 2, e, ""));
            am.cancel(pendiente(ctx, i * 2 + 1, e, ""));
        }
    }

    public static void borrarPlan(Context ctx) {
        Calendar b = base(ctx);
        if (b != null) {
            cancelarAvisos(ctx, b);
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = p.edit();
            String prefijo = "nota_" + iso(b) + "_";
            for (String k : p.getAll().keySet()) {
                if (k.startsWith(prefijo)) ed.remove(k);
            }
            ed.remove("anio").remove("mes").remove("dia").apply();
        }
    }
}
