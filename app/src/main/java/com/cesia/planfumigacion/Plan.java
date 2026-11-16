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
        public final String fechaIso;
        public final String titulo;
        public final String detalle;
        public final int diasDesdeBase;

        public Evento(String fechaIso, String titulo, String detalle, int diasDesdeBase) {
            this.fechaIso = fechaIso;
            this.titulo = titulo;
            this.detalle = detalle;
            this.diasDesdeBase = diasDesdeBase;
        }
    }

    private static final String CTRL = "Insecticida: en la MAÑANA\nFungicida: en la TARDE";

    /** Genera el plan completo de 90 días desde la cosecha base (patrón de Cesia). */
    public static List<Evento> generar(String baseIso) {
        List<Evento> l = new ArrayList<>();
        l.add(ev(baseIso, 0, "Cosecha base", "Inicio del ciclo de 90 días"));
        l.add(ev(baseIso, 5, "Control 1", CTRL));
        l.add(ev(baseIso, 9, "Nutrición Foliar 1", ""));
        l.add(ev(baseIso, 19, "Control 2", CTRL));
        l.add(ev(baseIso, 23, "Nutrición Foliar 2 (abono)", ""));
        l.add(ev(baseIso, 32, "Nutrición al suelo", ""));
        l.add(ev(baseIso, 33, "Control 3", CTRL));
        l.add(ev(baseIso, 39, "Nutrición 2da fase (engrose)", ""));
        l.add(ev(baseIso, 47, "Control 4", CTRL));
        l.add(ev(baseIso, 61, "Control 5 - ÚLTIMO control", CTRL));
        l.add(ev(baseIso, 62, "Empieza la CARENCIA", "No fumigar más hasta la cosecha"));
        l.add(ev(baseIso, 90, "🌾 COSECHA FINAL", "¡Llegó el gran día de cosechar!"));
        return l;
    }

    private static Evento ev(String baseIso, int dias, String titulo, String detalle) {
        return new Evento(sumarDias(baseIso, dias), titulo, detalle, dias);
    }

    /** Fase según el día del ciclo: 1 (días 1-30), 2 (31-60), 3 (61-90). */
    public static int fase(int diasDesdeBase) {
        if (diasDesdeBase <= 29) return 1;
        if (diasDesdeBase <= 59) return 2;
        return 3;
    }

    public static String sumarDias(String fechaIso, int dias) {
        try {
            Calendar c = Calendar.getInstance();
            java.util.Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fechaIso);
            c.setTime(d);
            c.add(Calendar.DAY_OF_YEAR, dias);
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
        } catch (Exception e) {
            return fechaIso;
        }
    }

    public static long millisEn(String fechaIso, int hora) {
        try {
            Calendar c = Calendar.getInstance();
            java.util.Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fechaIso);
            c.setTime(d);
            c.set(Calendar.HOUR_OF_DAY, hora);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    /** "2027-01-30" -> "Sábado 30 de enero de 2027" */
    public static String fechaLinda(String fechaIso) {
        try {
            java.util.Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fechaIso);
            String s = new SimpleDateFormat("EEEE d 'de' MMMM 'de' yyyy", new Locale("es")).format(d);
            return s.substring(0, 1).toUpperCase(new Locale("es")) + s.substring(1);
        } catch (Exception e) {
            return fechaIso;
        }
    }

    public static long diasFaltan(String fechaIso) {
        long hoy = inicioDelDia(System.currentTimeMillis());
        long ese = inicioDelDia(millisEn(fechaIso, 12));
        return Math.round((ese - hoy) / (24.0 * 60 * 60 * 1000));
    }

    private static long inicioDelDia(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    // ===== Fecha base guardada =====

    public static String getBase(Context ctx) {
        return ctx.getSharedPreferences("plan", Context.MODE_PRIVATE).getString("baseIso", null);
    }

    public static void setBase(Context ctx, String baseIso) {
        ctx.getSharedPreferences("plan", Context.MODE_PRIVATE).edit().putString("baseIso", baseIso).apply();
    }

    /** Elimina el plan guardado: avisos, fecha base y marcas de hecho. */
    public static void borrarPlan(Context ctx, String baseIso) {
        cancelarTodos(ctx);
        ctx.getSharedPreferences("plan", Context.MODE_PRIVATE).edit().remove("baseIso").apply();
        SharedPreferences p = prefsEstado(ctx);
        SharedPreferences.Editor ed = p.edit();
        for (String clave : p.getAll().keySet()) {
            if (clave.startsWith("hecho_" + baseIso + "_")) ed.remove(clave);
        }
        ed.apply();
    }

    // ===== Estado "hecho" por plan (según la fecha base) =====

    private static SharedPreferences prefsEstado(Context ctx) {
        return ctx.getSharedPreferences("estado", Context.MODE_PRIVATE);
    }

    public static boolean esHecho(Context ctx, String baseIso, int i) {
        return prefsEstado(ctx).getBoolean("hecho_" + baseIso + "_" + i, false);
    }

    public static void setHecho(Context ctx, String baseIso, int i, boolean hecho) {
        prefsEstado(ctx).edit().putBoolean("hecho_" + baseIso + "_" + i, hecho).apply();
    }

    // ===== Avisos =====

    /** Programa dos avisos por evento futuro: el día anterior 18:00 y el mismo día 07:00. */
    public static void programarAvisos(Context ctx, String baseIso) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null || baseIso == null) return;
        List<Evento> eventos = generar(baseIso);
        long ahora = System.currentTimeMillis();
        for (int i = 0; i < eventos.size(); i++) {
            if (esHecho(ctx, baseIso, i)) continue;
            Evento e = eventos.get(i);
            long antes = millisEn(e.fechaIso, 18) - 24L * 60 * 60 * 1000;
            if (antes > ahora) {
                programar(am, ctx, i * 10 + 1, antes,
                        "🌱 Mañana toca: " + e.titulo, textoAviso(e));
            }
            long mismoDia = millisEn(e.fechaIso, 7);
            if (mismoDia > ahora) {
                programar(am, ctx, i * 10 + 2, mismoDia,
                        "🚜 Hoy toca: " + e.titulo, textoAviso(e));
            }
        }
    }

    /** Cancela todos los avisos posibles (al crear un plan nuevo). */
    public static void cancelarTodos(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        for (int i = 0; i < 20; i++) {
            for (int suf : new int[]{1, 2}) {
                Intent intent = new Intent(ctx, AvisoReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(ctx, i * 10 + suf, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                am.cancel(pi);
            }
        }
    }

    /** Cancela los dos avisos de una tarea (al marcarla hecha). */
    public static void cancelarAvisos(Context ctx, int i) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        for (int suf : new int[]{1, 2}) {
            Intent intent = new Intent(ctx, AvisoReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(ctx, i * 10 + suf, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            am.cancel(pi);
        }
    }

    private static String textoAviso(Evento e) {
        String fecha = fechaLinda(e.fechaIso);
        if (e.detalle == null || e.detalle.isEmpty()) return fecha;
        return fecha + "\n" + e.detalle;
    }

    private static void programar(AlarmManager am, Context ctx, int codigo, long cuando,
                                  String titulo, String texto) {
        Intent intent = new Intent(ctx, AvisoReceiver.class);
        intent.putExtra("titulo", titulo);
        intent.putExtra("texto", texto);
        intent.putExtra("id", codigo);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, codigo, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cuando, pi);
    }
}
