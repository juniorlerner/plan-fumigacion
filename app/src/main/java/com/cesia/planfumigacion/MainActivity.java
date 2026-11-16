package com.cesia.planfumigacion;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {

    private static final int VERDE = Color.rgb(46, 125, 50);
    private static final int VERDE_OSCURO = Color.rgb(20, 83, 45);
    private static final int VERDE_CLARO = Color.rgb(232, 245, 233);
    private static final int GRIS = Color.rgb(117, 117, 117);
    private static final int AZUL = Color.rgb(21, 101, 192);
    private static final int CAFE = Color.rgb(121, 85, 72);
    private static final int ROJO = Color.rgb(198, 40, 40);
    private static final int DORADO = Color.rgb(191, 143, 0);
    private static final int CREMA = Color.rgb(250, 248, 240);

    private int selAnio, selMes, selDia;
    private TextView txtFecha;
    private boolean enDetalle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        enDetalle = getSharedPreferences("ui", MODE_PRIVATE).getBoolean("detalle", false);
        Calendar hoy = Calendar.getInstance();
        selAnio = hoy.get(Calendar.YEAR);
        selMes = hoy.get(Calendar.MONTH);
        selDia = hoy.get(Calendar.DAY_OF_MONTH);
        mostrar();
        Calendar b = Plan.base(this);
        if (b != null) Plan.programarAvisos(this, b);
    }

    @Override
    public void onBackPressed() {
        if (enDetalle) {
            enDetalle = false;
            getSharedPreferences("ui", MODE_PRIVATE).edit().putBoolean("detalle", false).apply();
            mostrar();
        } else {
            super.onBackPressed();
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable fondo(int color, float radioDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp((int) radioDp));
        return g;
    }

    // Efecto onda al tocar (como las apps profesionales)
    private Drawable conOnda(Drawable fondo, boolean oscuro) {
        int onda = oscuro ? Color.argb(32, 0, 0, 0) : Color.argb(70, 255, 255, 255);
        return new RippleDrawable(ColorStateList.valueOf(onda), fondo, null);
    }

    private TextView texto(String s, float sp, int color, boolean negrita) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (negrita) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private int colorTipo(String tipo) {
        switch (tipo) {
            case "control": return VERDE;
            case "foliar": return Color.rgb(104, 159, 56);
            case "suelo": return CAFE;
            case "engrose": return Color.rgb(0, 121, 107);
            case "carencia": return ROJO;
            case "cosecha": return DORADO;
            default: return AZUL;
        }
    }

    private String iconoTipo(String tipo) {
        switch (tipo) {
            case "base": return "🌱";
            case "control": return "🚜";
            case "foliar": return "💧";
            case "suelo": return "🟤";
            case "engrose": return "📈";
            case "carencia": return "⛔";
            case "cosecha": return "🌾";
            default: return "📌";
        }
    }

    private String etiquetaDias(Calendar fecha) {
        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0); hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0); hoy.set(Calendar.MILLISECOND, 0);
        Calendar f = (Calendar) fecha.clone();
        f.set(Calendar.HOUR_OF_DAY, 0); f.set(Calendar.MINUTE, 0);
        f.set(Calendar.SECOND, 0); f.set(Calendar.MILLISECOND, 0);
        long d = (f.getTimeInMillis() - hoy.getTimeInMillis()) / 86400000L;
        if (d < 0) return "Ya pasó";
        if (d == 0) return "¡HOY!";
        if (d == 1) return "Mañana";
        return "Faltan " + d + " días";
    }

    private int avanceCiclo(Calendar base) {
        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0); hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0); hoy.set(Calendar.MILLISECOND, 0);
        long d = (hoy.getTimeInMillis() - base.getTimeInMillis()) / 86400000L;
        if (d < 0) d = 0;
        if (d > 90) d = 90;
        return (int) d;
    }

    private void mostrar() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(CREMA);
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(raiz);

        // ===== Encabezado con degradado verde y logo =====
        LinearLayout cab = new LinearLayout(this);
        cab.setOrientation(LinearLayout.HORIZONTAL);
        cab.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable grad = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{VERDE_OSCURO, VERDE});
        grad.setCornerRadii(new float[]{0, 0, 0, 0, dp(28), dp(28), dp(28), dp(28)});
        cab.setBackground(grad);
        cab.setPadding(dp(20), dp(22), dp(20), dp(20));
        raiz.addView(cab);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher);
        logo.setLayoutParams(new LinearLayout.LayoutParams(dp(58), dp(58)));
        cab.addView(logo);

        LinearLayout colCab = new LinearLayout(this);
        colCab.setOrientation(LinearLayout.VERTICAL);
        colCab.setPadding(dp(14), 0, 0, 0);
        cab.addView(colCab);

        colCab.addView(texto("Plan de Fumigación", 26, Color.WHITE, true));
        colCab.addView(texto("Ciclo de 90 días desde tu cosecha · Shinahota", 14,
                Color.rgb(200, 230, 201), false));

        LinearLayout cuerpo = new LinearLayout(this);
        cuerpo.setOrientation(LinearLayout.VERTICAL);
        cuerpo.setPadding(dp(16), dp(16), dp(16), dp(24));
        raiz.addView(cuerpo);

        Calendar base = Plan.base(this);
        if (base == null) {
            cuerpo.addView(vistaBienvenida());
        } else if (enDetalle) {
            cuerpo.addView(vistaDetalle(base));
        } else {
            cuerpo.addView(vistaVentana(base));
        }
        setContentView(scroll);
    }

    // ===== Vista: bienvenida (sin plan) =====

    private LinearLayout vistaBienvenida() {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher);
        LinearLayout.LayoutParams lpLogo = new LinearLayout.LayoutParams(dp(110), dp(110));
        lpLogo.gravity = Gravity.CENTER_HORIZONTAL;
        lpLogo.topMargin = dp(8);
        logo.setLayoutParams(lpLogo);
        col.addView(logo);

        TextView nombre = texto("Plan de Fumigación", 24, VERDE_OSCURO, true);
        nombre.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lpNom = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpNom.topMargin = dp(12);
        nombre.setLayoutParams(lpNom);
        col.addView(nombre);

        TextView desc = texto("Tu plan completo de 90 días, creado solo con la fecha de tu cosecha.",
                14, GRIS, false);
        desc.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lpDesc = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpDesc.topMargin = dp(4);
        desc.setLayoutParams(lpDesc);
        col.addView(desc);

        LinearLayout ben = new LinearLayout(this);
        ben.setOrientation(LinearLayout.VERTICAL);
        ben.setBackground(fondo(Color.WHITE, 16));
        ben.setElevation(dp(3));
        ben.setPadding(dp(18), dp(14), dp(18), dp(14));
        LinearLayout.LayoutParams lpBen = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpBen.topMargin = dp(18);
        ben.setLayoutParams(lpBen);
        String[] items = {
                "📅  Plan automático de 90 días",
                "⏰  Avisos el día antes y el mismo día",
                "📝  Notas en cada tarea",
                "📲  Compartí tu plan por WhatsApp"
        };
        for (int i = 0; i < items.length; i++) {
            TextView t = texto(items[i], 15, Color.rgb(55, 55, 55), false);
            if (i > 0) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.topMargin = dp(9);
                t.setLayoutParams(lp);
            }
            ben.addView(t);
        }
        col.addView(ben);

        LinearLayout crear = vistaCrear(null);
        LinearLayout.LayoutParams lpCrear = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpCrear.topMargin = dp(20);
        crear.setLayoutParams(lpCrear);
        col.addView(crear);
        return col;
    }

    // ===== Vista: elegir fecha y crear plan =====

    private LinearLayout vistaCrear(Calendar baseExistente) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);

        col.addView(texto("¿Cuándo fue tu cosecha?", 19, Color.rgb(33, 33, 33), true));
        TextView ay = texto("Tocá la fecha y elegí el día de tu cosecha. La app crea sola todo tu plan de 90 días.",
                14, GRIS, false);
        LinearLayout.LayoutParams lpAy = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpAy.topMargin = dp(4);
        ay.setLayoutParams(lpAy);
        col.addView(ay);

        txtFecha = new TextView(this);
        txtFecha.setTextSize(18);
        txtFecha.setTypeface(Typeface.DEFAULT_BOLD);
        txtFecha.setTextColor(VERDE_OSCURO);
        txtFecha.setGravity(Gravity.CENTER);
        txtFecha.setPadding(dp(12), dp(16), dp(12), dp(16));
        txtFecha.setBackground(conOnda(fondo(Color.WHITE, 14), true));
        txtFecha.setElevation(dp(3));
        txtFecha.setClickable(true);
        LinearLayout.LayoutParams lpF = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpF.topMargin = dp(14);
        txtFecha.setLayoutParams(lpF);
        actualizarTxtFecha();
        txtFecha.setOnClickListener(v -> {
            DatePickerDialog d = new DatePickerDialog(this,
                    (DatePicker view, int a, int m, int dd) -> {
                        selAnio = a; selMes = m; selDia = dd;
                        actualizarTxtFecha();
                    }, selAnio, selMes, selDia);
            d.show();
        });
        col.addView(txtFecha);

        Button btn = new Button(this);
        btn.setText("🌿  Crear mi plan de 90 días");
        btn.setTextSize(17);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setTextColor(Color.WHITE);
        btn.setBackground(conOnda(fondo(VERDE, 24), false));
        btn.setAllCaps(false);
        LinearLayout.LayoutParams lpB = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56));
        lpB.topMargin = dp(14);
        btn.setLayoutParams(lpB);
        btn.setOnClickListener(v -> {
            if (baseExistente != null) Plan.cancelarAvisos(this, baseExistente);
            Plan.guardarBase(this, selAnio, selMes, selDia);
            Calendar b = Plan.base(this);
            Plan.programarAvisos(this, b);
            enDetalle = true;
            getSharedPreferences("ui", MODE_PRIVATE).edit().putBoolean("detalle", true).apply();
            Toast.makeText(this, "¡Plan creado! 🎉", Toast.LENGTH_LONG).show();
            mostrar();
        });
        col.addView(btn);
        return col;
    }

    private void actualizarTxtFecha() {
        Calendar c = Calendar.getInstance();
        c.set(selAnio, selMes, selDia);
        txtFecha.setText("📅  " + Plan.fechaLarga(c));
    }

    // ===== Vista: ventana del plan (pantalla principal) =====

    private LinearLayout vistaVentana(Calendar base) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);

        LinearLayout ventana = new LinearLayout(this);
        ventana.setOrientation(LinearLayout.VERTICAL);
        ventana.setBackground(conOnda(fondo(Color.WHITE, 18), true));
        ventana.setElevation(dp(4));
        ventana.setPadding(dp(18), dp(16), dp(18), dp(16));
        ventana.setClickable(true);
        ventana.setOnClickListener(v -> abrirDetalle());
        col.addView(ventana);

        ventana.addView(texto("🌿 Mi plan de 90 días", 19, VERDE_OSCURO, true));
        TextView fechas = texto("Cosecha base: " + Plan.fechaLarga(base)
                + "\nCosecha final: " + Plan.fechaLarga(sumarDias(base, 90)), 14, GRIS, false);
        LinearLayout.LayoutParams lpFe = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpFe.topMargin = dp(6);
        fechas.setLayoutParams(lpFe);
        ventana.addView(fechas);

        int dias = avanceCiclo(base);
        int pct = (int) Math.round(dias * 100.0 / 90.0);
        ProgressBar barra = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        barra.setMax(90);
        barra.setProgress(dias);
        barra.setProgressTintList(ColorStateList.valueOf(VERDE));
        LinearLayout.LayoutParams lpBar = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(10));
        lpBar.topMargin = dp(12);
        barra.setLayoutParams(lpBar);
        ventana.addView(barra);
        TextView av = texto("Día " + dias + " de 90 · " + pct + "% del ciclo", 13, GRIS, false);
        LinearLayout.LayoutParams lpAv = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpAv.topMargin = dp(4);
        av.setLayoutParams(lpAv);
        ventana.addView(av);

        // Próxima tarea
        Plan.Evento prox = proximaTarea(base);
        LinearLayout caja = new LinearLayout(this);
        caja.setOrientation(LinearLayout.VERTICAL);
        caja.setBackground(fondo(VERDE_CLARO, 12));
        caja.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams lpCaja = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpCaja.topMargin = dp(14);
        caja.setLayoutParams(lpCaja);
        if (prox != null) {
            caja.addView(texto("⏭ PRÓXIMA TAREA", 12, VERDE, true));
            caja.addView(texto(prox.titulo, 16, Color.rgb(33, 33, 33), true));
            caja.addView(texto(Plan.fechaLarga(prox.fecha) + " · " + etiquetaDias(prox.fecha),
                    14, GRIS, false));
        } else {
            caja.addView(texto("🌾 ¡Ya llegó la cosecha!", 16, DORADO, true));
            caja.addView(texto("El ciclo de 90 días terminó.", 14, GRIS, false));
        }
        ventana.addView(caja);

        Button ver = new Button(this);
        ver.setText("Ver todo el plan  →");
        ver.setTextSize(16);
        ver.setTypeface(Typeface.DEFAULT_BOLD);
        ver.setTextColor(Color.WHITE);
        ver.setBackground(conOnda(fondo(VERDE, 22), false));
        ver.setAllCaps(false);
        LinearLayout.LayoutParams lpVer = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        lpVer.topMargin = dp(14);
        ver.setLayoutParams(lpVer);
        ver.setOnClickListener(v -> abrirDetalle());
        ventana.addView(ver);

        TextView pie = texto("🌿 ¡Buena cosecha!", 15, VERDE_OSCURO, true);
        pie.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lpPie = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpPie.topMargin = dp(22);
        pie.setLayoutParams(lpPie);
        col.addView(pie);
        return col;
    }

    private void abrirDetalle() {
        enDetalle = true;
        getSharedPreferences("ui", MODE_PRIVATE).edit().putBoolean("detalle", true).apply();
        mostrar();
    }

    private Plan.Evento proximaTarea(Calendar base) {
        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0); hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0); hoy.set(Calendar.MILLISECOND, 0);
        for (Plan.Evento e : Plan.eventos(base)) {
            Calendar f = (Calendar) e.fecha.clone();
            f.set(Calendar.HOUR_OF_DAY, 0); f.set(Calendar.MINUTE, 0);
            f.set(Calendar.SECOND, 0); f.set(Calendar.MILLISECOND, 0);
            if (!f.before(hoy)) return e;
        }
        return null;
    }

    private Calendar sumarDias(Calendar base, int dias) {
        Calendar c = (Calendar) base.clone();
        c.add(Calendar.DAY_OF_YEAR, dias);
        return c;
    }

    // ===== Vista: detalle completo del plan =====

    private LinearLayout vistaDetalle(Calendar base) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);

        // Fila superior: Volver + Eliminar
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        col.addView(fila);

        Button volver = new Button(this);
        volver.setText("← Volver");
        volver.setTextSize(14);
        volver.setTextColor(VERDE_OSCURO);
        volver.setBackground(conOnda(fondo(VERDE_CLARO, 18), true));
        volver.setAllCaps(false);
        volver.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        volver.setOnClickListener(v -> {
            enDetalle = false;
            getSharedPreferences("ui", MODE_PRIVATE).edit().putBoolean("detalle", false).apply();
            mostrar();
        });
        fila.addView(volver);

        Button eliminar = new Button(this);
        eliminar.setText("🗑 Eliminar");
        eliminar.setTextSize(14);
        eliminar.setTextColor(Color.WHITE);
        eliminar.setBackground(conOnda(fondo(ROJO, 18), false));
        eliminar.setAllCaps(false);
        LinearLayout.LayoutParams lpEl = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        lpEl.leftMargin = dp(10);
        eliminar.setLayoutParams(lpEl);
        eliminar.setOnClickListener(v -> confirmarEliminar());
        fila.addView(eliminar);

        // Botón compartir por WhatsApp
        Button compartir = new Button(this);
        compartir.setText("📲  Compartir plan por WhatsApp");
        compartir.setTextSize(15);
        compartir.setTypeface(Typeface.DEFAULT_BOLD);
        compartir.setTextColor(Color.WHITE);
        compartir.setBackground(conOnda(fondo(Color.rgb(37, 190, 95), 20), false));
        compartir.setAllCaps(false);
        LinearLayout.LayoutParams lpComp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(50));
        lpComp.topMargin = dp(12);
        compartir.setLayoutParams(lpComp);
        compartir.setOnClickListener(v -> {
            Intent it = new Intent(Intent.ACTION_SEND);
            it.setType("text/plain");
            it.putExtra(Intent.EXTRA_TEXT, Plan.textoParaCompartir(base));
            startActivity(Intent.createChooser(it, "Compartir plan"));
        });
        col.addView(compartir);

        // Tarjeta de avance
        LinearLayout avance = new LinearLayout(this);
        avance.setOrientation(LinearLayout.VERTICAL);
        avance.setBackground(fondo(Color.WHITE, 16));
        avance.setElevation(dp(3));
        avance.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams lpAva = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpAva.topMargin = dp(14);
        avance.setLayoutParams(lpAva);
        int dias = avanceCiclo(base);
        int pct = (int) Math.round(dias * 100.0 / 90.0);
        avance.addView(texto("📊 Tu avance", 16, VERDE_OSCURO, true));
        ProgressBar barra = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        barra.setMax(90);
        barra.setProgress(dias);
        barra.setProgressTintList(ColorStateList.valueOf(VERDE));
        LinearLayout.LayoutParams lpBar = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(10));
        lpBar.topMargin = dp(10);
        barra.setLayoutParams(lpBar);
        avance.addView(barra);
        avance.addView(texto("Día " + dias + " de 90 · " + pct + "% del ciclo", 13, GRIS, false));
        col.addView(avance);

        // Tareas por fases
        List<Plan.Evento> evs = Plan.eventos(base);
        String[] fases = {"FASE 1 · Días 1 a 30", "FASE 2 · Días 31 a 60", "FASE 3 · Días 61 a 90"};
        int[] colores = {VERDE, AZUL, CAFE};
        int[] desde = {0, 30, 60};
        int[] hasta = {29, 59, 90};
        for (int f = 0; f < 3; f++) {
            col.addView(bandaFase(fases[f], colores[f]));
            for (int i = 0; i < evs.size(); i++) {
                Plan.Evento e = evs.get(i);
                long off = (e.fecha.getTimeInMillis() - base.getTimeInMillis()) / 86400000L;
                if (off >= desde[f] && off <= hasta[f]) {
                    col.addView(tarjeta(e, i, base));
                }
            }
        }

        // Nuevo plan
        TextView otro = texto("¿Querés empezar un plan nuevo? Crealo acá y reemplaza al actual:",
                13, GRIS, false);
        LinearLayout.LayoutParams lpOtro = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpOtro.topMargin = dp(20);
        otro.setLayoutParams(lpOtro);
        col.addView(otro);
        col.addView(vistaCrear(base));

        TextView pie = texto("🌿 ¡Buena cosecha!", 15, VERDE_OSCURO, true);
        pie.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lpPie = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpPie.topMargin = dp(22);
        pie.setLayoutParams(lpPie);
        col.addView(pie);
        return col;
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar plan")
                .setMessage("¿Seguro que querés eliminar este plan? Se borran el plan, sus notas y todos sus avisos.")
                .setPositiveButton("Sí, eliminar", (d, w) -> {
                    Plan.borrarPlan(this);
                    enDetalle = false;
                    getSharedPreferences("ui", MODE_PRIVATE).edit().putBoolean("detalle", false).apply();
                    Toast.makeText(this, "Plan eliminado", Toast.LENGTH_LONG).show();
                    mostrar();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private TextView bandaFase(String titulo, int color) {
        TextView t = new TextView(this);
        t.setText(titulo);
        t.setTextSize(14);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(Color.WHITE);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(10), dp(9), dp(10), dp(9));
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{color, Color.rgb(
                        Math.min(255, Color.red(color) + 40),
                        Math.min(255, Color.green(color) + 40),
                        Math.min(255, Color.blue(color) + 40))});
        g.setCornerRadius(dp(10));
        t.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(18);
        t.setLayoutParams(lp);
        return t;
    }

    private LinearLayout tarjeta(Plan.Evento e, int idx, Calendar base) {
        int color = colorTipo(e.tipo);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackground(fondo(Color.WHITE, 18));
        card.setElevation(dp(3));
        LinearLayout.LayoutParams lpCard = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpCard.topMargin = dp(10);
        card.setLayoutParams(lpCard);

        // Franja de color al costado
        View raya = new View(this);
        GradientDrawable gr = new GradientDrawable();
        gr.setColor(color);
        gr.setCornerRadii(new float[]{dp(18), dp(18), 0, 0, 0, 0, dp(18), dp(18)});
        raya.setBackground(gr);
        raya.setLayoutParams(new LinearLayout.LayoutParams(dp(7),
                LinearLayout.LayoutParams.MATCH_PARENT));
        card.addView(raya);

        // Círculo con ícono
        TextView icono = new TextView(this);
        icono.setText(iconoTipo(e.tipo));
        icono.setTextSize(22);
        icono.setGravity(Gravity.CENTER);
        GradientDrawable circ = new GradientDrawable();
        circ.setShape(GradientDrawable.OVAL);
        circ.setColor(Color.rgb(
                Color.red(color) + (255 - Color.red(color)) * 85 / 100,
                Color.green(color) + (255 - Color.green(color)) * 85 / 100,
                Color.blue(color) + (255 - Color.blue(color)) * 85 / 100));
        icono.setBackground(circ);
        LinearLayout.LayoutParams lpIco = new LinearLayout.LayoutParams(dp(48), dp(48));
        lpIco.leftMargin = dp(12);
        lpIco.topMargin = dp(14);
        icono.setLayoutParams(lpIco);
        card.addView(icono);

        LinearLayout colT = new LinearLayout(this);
        colT.setOrientation(LinearLayout.VERTICAL);
        colT.setPadding(dp(12), dp(12), dp(12), dp(12));
        colT.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        card.addView(colT);

        colT.addView(texto(e.titulo, 16, Color.rgb(33, 33, 33), true));

        colT.addView(texto(Plan.fechaLarga(e.fecha), 13, color, true));

        if (!e.detalle.isEmpty()) {
            TextView det = texto(e.detalle, 13, GRIS, false);
            LinearLayout.LayoutParams lpDet = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lpDet.topMargin = dp(2);
            det.setLayoutParams(lpDet);
            colT.addView(det);
        }

        // Nota guardada (si hay)
        String notaTxt = Plan.nota(this, base, idx);
        if (!notaTxt.isEmpty()) {
            TextView n = texto("📝 " + notaTxt, 13, Color.rgb(66, 66, 66), false);
            n.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
            LinearLayout.LayoutParams lpN = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lpN.topMargin = dp(4);
            n.setLayoutParams(lpN);
            colT.addView(n);
        }

        // Fila inferior: etiqueta de días + botón Nota
        LinearLayout filaB = new LinearLayout(this);
        filaB.setOrientation(LinearLayout.HORIZONTAL);
        filaB.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lpFila = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpFila.topMargin = dp(8);
        filaB.setLayoutParams(lpFila);
        colT.addView(filaB);

        TextView chip = new TextView(this);
        chip.setText(etiquetaDias(e.fecha));
        chip.setTextSize(12);
        chip.setTypeface(Typeface.DEFAULT_BOLD);
        chip.setTextColor(Color.WHITE);
        chip.setPadding(dp(10), dp(4), dp(10), dp(4));
        chip.setBackground(fondo(color, 12));
        filaB.addView(chip);

        TextView espacio = new TextView(this);
        espacio.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        filaB.addView(espacio);

        Button btnNota = new Button(this);
        btnNota.setText("📝 Nota");
        btnNota.setTextSize(13);
        btnNota.setTextColor(VERDE_OSCURO);
        btnNota.setBackground(conOnda(fondo(VERDE_CLARO, 16), true));
        btnNota.setAllCaps(false);
        btnNota.setOnClickListener(v -> editarNota(e, idx, base));
        filaB.addView(btnNota);

        return card;
    }

    private void editarNota(Plan.Evento e, int idx, Calendar base) {
        EditText input = new EditText(this);
        input.setText(Plan.nota(this, base, idx));
        input.setHint("Ej: qué producto usé, cómo estaba el cultivo...");
        input.setMinLines(2);
        new AlertDialog.Builder(this)
                .setTitle("Nota · " + e.titulo)
                .setView(input)
                .setPositiveButton("Guardar", (d, w) -> {
                    Plan.guardarNota(this, base, idx, input.getText().toString().trim());
                    mostrar();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
