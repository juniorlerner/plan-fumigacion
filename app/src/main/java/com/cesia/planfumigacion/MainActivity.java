package com.cesia.planfumigacion;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int VERDE = Color.rgb(46, 125, 50);
    private static final int VERDE_OSCURO = Color.rgb(27, 94, 32);
    private static final int VERDE_CLARO = Color.rgb(232, 245, 233);
    private static final int FONDO = Color.rgb(243, 246, 243);
    private static final int GRIS = Color.rgb(158, 158, 158);
    private static final int GRIS_TEXTO = Color.rgb(97, 97, 97);
    private static final int NARANJA = Color.rgb(239, 108, 0);
    private static final int ROJO = Color.rgb(198, 40, 40);
    private static final int AZUL = Color.rgb(30, 100, 180);
    private static final int MARRON = Color.rgb(205, 120, 40);

    private Calendar selCal;
    private Button fechaBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        String base = Plan.getBase(this);
        selCal = Calendar.getInstance();
        if (base != null) {
            try {
                selCal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(base));
            } catch (Exception ignored) {
            }
            Plan.programarAvisos(this, base);
        }

        boolean detalle = base != null && viendoDetalle();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(FONDO);
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(raiz);

        // ===== Encabezado con degradado verde =====
        LinearLayout cab = new LinearLayout(this);
        cab.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable grad = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{VERDE_OSCURO, VERDE});
        grad.setCornerRadii(new float[]{0, 0, 0, 0, dp(28), dp(28), dp(28), dp(28)});
        cab.setBackground(grad);
        cab.setPadding(dp(20), dp(22), dp(20), dp(20));
        raiz.addView(cab);

        TextView titulo = new TextView(this);
        titulo.setText("🌿 Plan de Fumigación");
        titulo.setTextSize(27);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setTextColor(Color.WHITE);
        cab.addView(titulo);

        TextView sub = new TextView(this);
        sub.setText("Ciclo de 90 días desde tu cosecha · Shinahota");
        sub.setTextSize(14);
        sub.setTextColor(Color.rgb(200, 230, 201));
        cab.addView(sub);

        LinearLayout cuerpo = new LinearLayout(this);
        cuerpo.setOrientation(LinearLayout.VERTICAL);
        cuerpo.setPadding(dp(16), dp(16), dp(16), dp(16));
        raiz.addView(cuerpo);

        if (detalle) {
            vistaDetalle(cuerpo, base);
        } else {
            vistaInicio(cuerpo, base);
        }

        setContentView(scroll);
    }

    // ===== Vista principal: ventana del plan + crear =====

    private void vistaInicio(LinearLayout cuerpo, String base) {
        if (base != null) {
            TextView etiquetaPlan = new TextView(this);
            etiquetaPlan.setText("Tu plan");
            etiquetaPlan.setTextSize(16);
            etiquetaPlan.setTypeface(Typeface.DEFAULT_BOLD);
            etiquetaPlan.setTextColor(VERDE_OSCURO);
            cuerpo.addView(etiquetaPlan);
            cuerpo.addView(ventanaPlan(base));

            TextView etiquetaOtro = new TextView(this);
            etiquetaOtro.setText("Crear un plan nuevo");
            etiquetaOtro.setTextSize(16);
            etiquetaOtro.setTypeface(Typeface.DEFAULT_BOLD);
            etiquetaOtro.setTextColor(VERDE_OSCURO);
            etiquetaOtro.setPadding(0, dp(16), 0, 0);
            cuerpo.addView(etiquetaOtro);
            TextView nota = new TextView(this);
            nota.setText("Si creás uno nuevo, reemplaza al plan actual.");
            nota.setTextSize(13);
            nota.setTextColor(GRIS_TEXTO);
            cuerpo.addView(nota);
        }

        TextView etiqueta = new TextView(this);
        etiqueta.setText("1️⃣  Elegí el día de tu cosecha base");
        etiqueta.setTextSize(16);
        etiqueta.setTypeface(Typeface.DEFAULT_BOLD);
        etiqueta.setTextColor(VERDE_OSCURO);
        etiqueta.setPadding(0, dp(8), 0, 0);
        cuerpo.addView(etiqueta);

        fechaBtn = new Button(this);
        fechaBtn.setAllCaps(false);
        fechaBtn.setTextSize(17);
        fechaBtn.setTypeface(Typeface.DEFAULT_BOLD);
        fechaBtn.setTextColor(VERDE_OSCURO);
        GradientDrawable fondoFecha = new GradientDrawable();
        fondoFecha.setColor(Color.WHITE);
        fondoFecha.setCornerRadius(dp(12));
        fondoFecha.setStroke(dp(2), VERDE);
        fechaBtn.setBackground(fondoFecha);
        fechaBtn.setPadding(dp(10), dp(12), dp(10), dp(12));
        LinearLayout.LayoutParams lpF = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpF.setMargins(0, dp(8), 0, dp(10));
        fechaBtn.setLayoutParams(lpF);
        actualizarTextoFecha();
        fechaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selCal.set(year, month, dayOfMonth);
                        actualizarTextoFecha();
                    }
                }, selCal.get(Calendar.YEAR), selCal.get(Calendar.MONTH), selCal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        cuerpo.addView(fechaBtn);

        Button crearBtn = new Button(this);
        crearBtn.setAllCaps(false);
        crearBtn.setText("2️⃣  Crear mi plan de 90 días");
        crearBtn.setTextSize(18);
        crearBtn.setTypeface(Typeface.DEFAULT_BOLD);
        crearBtn.setTextColor(Color.WHITE);
        GradientDrawable fondoCrear = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{VERDE, VERDE_OSCURO});
        fondoCrear.setCornerRadius(dp(24));
        crearBtn.setBackground(fondoCrear);
        crearBtn.setPadding(dp(10), dp(13), dp(10), dp(13));
        LinearLayout.LayoutParams lpC = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpC.setMargins(0, 0, 0, dp(14));
        crearBtn.setLayoutParams(lpC);
        crearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String baseIso = iso(selCal);
                Plan.setBase(MainActivity.this, baseIso);
                Plan.cancelarTodos(MainActivity.this);
                Plan.programarAvisos(MainActivity.this, baseIso);
                setViendoDetalle(true);
                recreate();
            }
        });
        cuerpo.addView(crearBtn);

        if (base == null) {
            cuerpo.addView(tarjetaTexto("Todavía no creaste un plan 🌱\n\nElegí la fecha de tu cosecha base y tocá el botón verde: la app arma sola todo tu plan de fumigación de 90 días y activa los avisos."));
        }
    }

    /** La ventana del plan en la pantalla principal. Al tocarla se abre el detalle. */
    private LinearLayout ventanaPlan(final String baseIso) {
        List<Plan.Evento> eventos = Plan.generar(baseIso);
        String finalIso = Plan.sumarDias(baseIso, 90);
        int hechos = 0;
        for (int i = 0; i < eventos.size(); i++) {
            if (Plan.esHecho(this, baseIso, i)) hechos++;
        }
        long faltaBase = Plan.diasFaltan(baseIso);
        long faltaFinal = Plan.diasFaltan(finalIso);
        int transcurridos = (int) Math.max(0, Math.min(90, -faltaBase));

        LinearLayout ventana = new LinearLayout(this);
        ventana.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable f = new GradientDrawable();
        f.setColor(Color.WHITE);
        f.setCornerRadius(dp(14));
        ventana.setBackground(f);
        ventana.setElevation(dp(4));
        LinearLayout.LayoutParams lpV = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpV.setMargins(0, dp(8), 0, dp(4));
        ventana.setLayoutParams(lpV);
        ventana.setClickable(true);
        ventana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViendoDetalle(true);
                recreate();
            }
        });

        View franja = new View(this);
        franja.setBackgroundColor(VERDE);
        franja.setLayoutParams(new LinearLayout.LayoutParams(dp(7), LinearLayout.LayoutParams.MATCH_PARENT));
        ventana.addView(franja);

        LinearLayout contenido = new LinearLayout(this);
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(dp(14), dp(12), dp(14), dp(14));
        contenido.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        ventana.addView(contenido);

        TextView nombre = new TextView(this);
        nombre.setText("🌿 Mi plan de fumigación");
        nombre.setTextSize(20);
        nombre.setTypeface(Typeface.DEFAULT_BOLD);
        nombre.setTextColor(Color.rgb(33, 33, 33));
        contenido.addView(nombre);

        TextView fechas = new TextView(this);
        fechas.setText("Cosecha base: " + Plan.fechaLinda(baseIso)
                + "\n🌾 Cosecha final: " + Plan.fechaLinda(finalIso)
                + "\n✔ Tareas hechas: " + hechos + " de " + eventos.size());
        fechas.setTextSize(14);
        fechas.setTextColor(GRIS_TEXTO);
        fechas.setPadding(0, dp(4), 0, dp(8));
        contenido.addView(fechas);

        contenido.addView(barraProgreso(transcurridos));

        TextView estado = new TextView(this);
        if (faltaBase > 0) {
            estado.setText("Tu ciclo empieza en " + faltaBase + (faltaBase == 1 ? " día" : " días") + " 🌱");
        } else if (faltaFinal < 0) {
            estado.setText("¡Ciclo terminado! 🎉");
        } else {
            estado.setText("Vas por el día " + (transcurridos + 1) + " de 90 · " + (transcurridos * 100 / 90) + "% del ciclo · faltan " + faltaFinal + " días para la cosecha");
        }
        estado.setTextSize(14);
        estado.setTypeface(Typeface.DEFAULT_BOLD);
        estado.setTextColor(VERDE);
        estado.setPadding(0, dp(6), 0, dp(10));
        contenido.addView(estado);

        // Próxima tarea dentro de la ventana
        String proximaTxt = null;
        for (int i = 0; i < eventos.size(); i++) {
            Plan.Evento ev = eventos.get(i);
            if (Plan.esHecho(this, baseIso, i)) continue;
            long df = Plan.diasFaltan(ev.fechaIso);
            if (df < 0) continue;
            proximaTxt = "⏭ Próxima tarea: " + ev.titulo + "\n" + Plan.fechaLinda(ev.fechaIso)
                    + (df == 0 ? " · ¡ES HOY!" : (df == 1 ? " · es mañana" : " · faltan " + df + " días"));
            break;
        }
        if (proximaTxt == null) proximaTxt = "⏭ No quedan tareas pendientes 🎉";
        TextView proxima = new TextView(this);
        proxima.setText(proximaTxt);
        proxima.setTextSize(14);
        proxima.setTypeface(Typeface.DEFAULT_BOLD);
        proxima.setTextColor(VERDE_OSCURO);
        GradientDrawable fondoProx = new GradientDrawable();
        fondoProx.setColor(VERDE_CLARO);
        fondoProx.setCornerRadius(dp(10));
        proxima.setBackground(fondoProx);
        proxima.setPadding(dp(10), dp(8), dp(10), dp(8));
        LinearLayout.LayoutParams lpProx = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpProx.setMargins(0, 0, 0, dp(10));
        proxima.setLayoutParams(lpProx);
        contenido.addView(proxima);

        TextView ver = new TextView(this);
        ver.setText("Ver todo el plan  →");
        ver.setTextSize(15);
        ver.setTypeface(Typeface.DEFAULT_BOLD);
        ver.setTextColor(Color.WHITE);
        ver.setGravity(Gravity.CENTER);
        GradientDrawable fondoVer = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{VERDE, VERDE_OSCURO});
        fondoVer.setCornerRadius(dp(20));
        ver.setBackground(fondoVer);
        ver.setPadding(dp(12), dp(10), dp(12), dp(10));
        contenido.addView(ver);

        return ventana;
    }

    // ===== Vista detalle: todo el plan + eliminar =====

    private void vistaDetalle(LinearLayout cuerpo, final String baseIso) {
        Button volverBtn = new Button(this);
        volverBtn.setAllCaps(false);
        volverBtn.setText("← Volver");
        volverBtn.setTextSize(15);
        volverBtn.setTypeface(Typeface.DEFAULT_BOLD);
        volverBtn.setTextColor(VERDE_OSCURO);
        GradientDrawable fondoVolver = new GradientDrawable();
        fondoVolver.setColor(Color.WHITE);
        fondoVolver.setCornerRadius(dp(20));
        fondoVolver.setStroke(dp(1), VERDE);
        volverBtn.setBackground(fondoVolver);
        LinearLayout.LayoutParams lpVol = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpVol.setMargins(0, 0, 0, dp(12));
        volverBtn.setLayoutParams(lpVol);
        volverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViendoDetalle(false);
                recreate();
            }
        });

        // Fila superior del detalle: Volver + Eliminar (visible al entrar)
        LinearLayout filaTop = new LinearLayout(this);
        filaTop.setOrientation(LinearLayout.HORIZONTAL);
        filaTop.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lpFila = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpFila.setMargins(0, 0, 0, dp(12));
        filaTop.setLayoutParams(lpFila);
        filaTop.addView(volverBtn);
        View espacio = new View(this);
        espacio.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1));
        filaTop.addView(espacio);
        Button eliminarTopBtn = new Button(this);
        eliminarTopBtn.setAllCaps(false);
        eliminarTopBtn.setText("🗑 Eliminar");
        eliminarTopBtn.setTextSize(15);
        eliminarTopBtn.setTypeface(Typeface.DEFAULT_BOLD);
        eliminarTopBtn.setTextColor(Color.WHITE);
        GradientDrawable fondoElimTop = new GradientDrawable();
        fondoElimTop.setColor(ROJO);
        fondoElimTop.setCornerRadius(dp(20));
        eliminarTopBtn.setBackground(fondoElimTop);
        eliminarTopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarEliminar(baseIso);
            }
        });
        filaTop.addView(eliminarTopBtn);
        cuerpo.addView(filaTop);

        List<Plan.Evento> eventos = Plan.generar(baseIso);
        String finalIso = Plan.sumarDias(baseIso, 90);
        int hechos = 0;
        for (int i = 0; i < eventos.size(); i++) {
            if (Plan.esHecho(this, baseIso, i)) hechos++;
        }
        long faltaBase = Plan.diasFaltan(baseIso);
        int transcurridos = (int) Math.max(0, Math.min(90, -faltaBase));

        // Tarjeta resumen del ciclo
        LinearLayout prog = tarjetaBlanca();
        TextView tp = new TextView(this);
        tp.setText("📊 Tu ciclo");
        tp.setTextSize(16);
        tp.setTypeface(Typeface.DEFAULT_BOLD);
        tp.setTextColor(VERDE_OSCURO);
        prog.addView(tp);
        TextView fechas = new TextView(this);
        fechas.setText("Cosecha base: " + Plan.fechaLinda(baseIso)
                + "\n🌾 Cosecha final: " + Plan.fechaLinda(finalIso)
                + "\n✔ Tareas hechas: " + hechos + " de " + eventos.size());
        fechas.setTextSize(14);
        fechas.setTextColor(GRIS_TEXTO);
        fechas.setPadding(0, dp(4), 0, dp(8));
        prog.addView(fechas);
        prog.addView(barraProgreso(transcurridos));
        TextView estadoProg = new TextView(this);
        long faltaFinalD = Plan.diasFaltan(finalIso);
        if (faltaBase > 0) {
            estadoProg.setText("Tu ciclo empieza en " + faltaBase + (faltaBase == 1 ? " día" : " días") + " 🌱");
        } else if (faltaFinalD < 0) {
            estadoProg.setText("¡Ciclo terminado! 🎉");
        } else {
            estadoProg.setText("Día " + (transcurridos + 1) + " de 90 · " + (transcurridos * 100 / 90) + "% del ciclo");
        }
        estadoProg.setTextSize(14);
        estadoProg.setTypeface(Typeface.DEFAULT_BOLD);
        estadoProg.setTextColor(VERDE);
        estadoProg.setPadding(0, dp(6), 0, 0);
        prog.addView(estadoProg);
        cuerpo.addView(prog);

        boolean yaPuseProximo = false;
        int faseActual = 0;
        for (int i = 0; i < eventos.size(); i++) {
            Plan.Evento e = eventos.get(i);
            final int idx = i;
            boolean hecho = Plan.esHecho(this, baseIso, idx);

            int fase = Plan.fase(e.diasDesdeBase);
            if (fase != faseActual) {
                faseActual = fase;
                TextView h = new TextView(this);
                h.setText(fase == 1 ? "🌱  FASE 1 · Días 1 a 30" : (fase == 2 ? "🌿  FASE 2 · Días 31 a 60" : "🌾  FASE 3 · Días 61 a 90"));
                h.setTextSize(15);
                h.setTypeface(Typeface.DEFAULT_BOLD);
                h.setTextColor(Color.WHITE);
                GradientDrawable fondoFase = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                        fase == 1 ? new int[]{VERDE_OSCURO, VERDE}
                                : (fase == 2 ? new int[]{Color.rgb(21, 71, 128), AZUL}
                                : new int[]{Color.rgb(160, 85, 20), MARRON}));
                fondoFase.setCornerRadius(dp(12));
                h.setBackground(fondoFase);
                h.setPadding(dp(14), dp(10), dp(14), dp(10));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, dp(14), 0, dp(10));
                h.setLayoutParams(lp);
                cuerpo.addView(h);
            }

            long dias = Plan.diasFaltan(e.fechaIso);
            boolean pasado = dias < 0;
            boolean esProximo = !pasado && !hecho && !yaPuseProximo;
            if (esProximo) yaPuseProximo = true;

            LinearLayout tarjeta = new LinearLayout(this);
            tarjeta.setOrientation(LinearLayout.VERTICAL);
            tarjeta.setPadding(dp(14), dp(12), dp(14), dp(14));
            GradientDrawable fondoT = new GradientDrawable();
            fondoT.setColor(esProximo ? VERDE_CLARO : Color.WHITE);
            fondoT.setCornerRadius(dp(18));
            tarjeta.setBackground(fondoT);
            tarjeta.setElevation(dp(4));
            LinearLayout.LayoutParams lpT = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lpT.setMargins(0, 0, 0, dp(12));
            tarjeta.setLayoutParams(lpT);

            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.CENTER_VERTICAL);
            fila.addView(circuloIcono(iconoPara(e), colorSuavePara(e)));

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setPadding(dp(12), 0, dp(8), 0);
            col.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView nombre = new TextView(this);
            nombre.setText(e.titulo);
            nombre.setTextSize(esProximo ? 20 : 18);
            nombre.setTypeface(Typeface.DEFAULT_BOLD);
            nombre.setTextColor(hecho ? GRIS_TEXTO : (pasado ? GRIS : Color.rgb(33, 33, 33)));
            col.addView(nombre);

            TextView fecha = new TextView(this);
            fecha.setText("Día " + e.diasDesdeBase + " · " + Plan.fechaLinda(e.fechaIso));
            fecha.setTextSize(14);
            fecha.setTextColor(pasado || hecho ? GRIS : GRIS_TEXTO);
            col.addView(fecha);

            fila.addView(col);
            fila.addView(chip(dias, hecho));
            tarjeta.addView(fila);

            if (e.detalle != null && !e.detalle.isEmpty()) {
                TextView det = new TextView(this);
                det.setText(e.detalle);
                det.setTextSize(15);
                det.setTextColor(pasado || hecho ? GRIS : Color.rgb(66, 66, 66));
                det.setPadding(0, dp(8), 0, 0);
                tarjeta.addView(det);
            }

            Button btn = new Button(this);
            btn.setAllCaps(false);
            btn.setText(hecho ? "↩ Desmarcar" : "✓ ¡Ya lo hice!");
            btn.setTextSize(14);
            btn.setTypeface(Typeface.DEFAULT_BOLD);
            btn.setTextColor(hecho ? GRIS_TEXTO : VERDE_OSCURO);
            GradientDrawable fondoBtn = new GradientDrawable();
            fondoBtn.setColor(hecho ? Color.rgb(238, 238, 238) : VERDE_CLARO);
            fondoBtn.setCornerRadius(dp(18));
            fondoBtn.setStroke(dp(1), hecho ? GRIS : VERDE);
            btn.setBackground(fondoBtn);
            LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp3.setMargins(0, dp(8), 0, 0);
            btn.setLayoutParams(lp3);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean nuevo = !Plan.esHecho(MainActivity.this, baseIso, idx);
                    Plan.setHecho(MainActivity.this, baseIso, idx, nuevo);
                    if (nuevo) {
                        Plan.cancelarAvisos(MainActivity.this, idx);
                    } else {
                        Plan.programarAvisos(MainActivity.this, baseIso);
                    }
                    recreate();
                }
            });
            tarjeta.addView(btn);

            cuerpo.addView(tarjeta);
        }

        // Banner final
        TextView pie = new TextView(this);
        pie.setText("🌿 ¡Buena cosecha!");
        pie.setTextSize(16);
        pie.setTypeface(Typeface.DEFAULT_BOLD);
        pie.setTextColor(Color.WHITE);
        pie.setGravity(Gravity.CENTER);
        GradientDrawable fondoPie = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{VERDE_OSCURO, VERDE});
        fondoPie.setCornerRadius(dp(14));
        pie.setBackground(fondoPie);
        pie.setPadding(dp(12), dp(14), dp(12), dp(14));
        LinearLayout.LayoutParams lpPie = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpPie.setMargins(0, dp(6), 0, dp(8));
        pie.setLayoutParams(lpPie);
        cuerpo.addView(pie);
    }

    /** Pregunta antes de eliminar el plan (botones de arriba y de abajo). */
    private void confirmarEliminar(final String baseIso) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar plan")
                .setMessage("¿Seguro que querés eliminar este plan? Se borran el plan y todos sus avisos.")
                .setPositiveButton("Sí, eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Plan.borrarPlan(MainActivity.this, baseIso);
                        setViendoDetalle(false);
                        recreate();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (viendoDetalle()) {
            setViendoDetalle(false);
            recreate();
        } else {
            super.onBackPressed();
        }
    }

    // ===== Piezas comunes =====

    private boolean viendoDetalle() {
        return getSharedPreferences("ui", MODE_PRIVATE).getBoolean("detalle", false);
    }

    private void setViendoDetalle(boolean v) {
        getSharedPreferences("ui", MODE_PRIVATE).edit().putBoolean("detalle", v).apply();
    }

    private LinearLayout barraProgreso(int transcurridos) {
        LinearLayout barra = new LinearLayout(this);
        barra.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable fondoBarra = new GradientDrawable();
        fondoBarra.setColor(Color.rgb(224, 224, 224));
        fondoBarra.setCornerRadius(dp(7));
        barra.setBackground(fondoBarra);
        barra.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(16)));
        View relleno = new View(this);
        GradientDrawable fondoRelleno = new GradientDrawable();
        fondoRelleno.setColor(VERDE);
        fondoRelleno.setCornerRadius(dp(7));
        relleno.setBackground(fondoRelleno);
        relleno.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, Math.max(transcurridos, 0.001f)));
        barra.addView(relleno);
        View resto = new View(this);
        resto.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, Math.max(90 - transcurridos, 0.001f)));
        barra.addView(resto);
        return barra;
    }

    private LinearLayout tarjetaBlanca() {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable f = new GradientDrawable();
        f.setColor(Color.WHITE);
        f.setCornerRadius(dp(14));
        t.setBackground(f);
        t.setElevation(dp(3));
        t.setPadding(dp(14), dp(12), dp(14), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(6));
        t.setLayoutParams(lp);
        return t;
    }

    private TextView tarjetaTexto(String texto) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(16);
        tv.setTextColor(GRIS_TEXTO);
        GradientDrawable f = new GradientDrawable();
        f.setColor(Color.WHITE);
        f.setCornerRadius(dp(14));
        tv.setBackground(f);
        tv.setElevation(dp(3));
        tv.setPadding(dp(14), dp(14), dp(14), dp(14));
        return tv;
    }

    private TextView chip(long dias, boolean hecho) {
        TextView c = new TextView(this);
        int bg;
        if (hecho) {
            c.setText("✅ Hecho");
            bg = VERDE;
        } else if (dias < 0) {
            c.setText("Ya pasó");
            bg = GRIS;
        } else if (dias == 0) {
            c.setText("🔴 ¡HOY!");
            bg = ROJO;
        } else if (dias == 1) {
            c.setText("Mañana");
            bg = NARANJA;
        } else if (dias <= 3) {
            c.setText("Faltan " + dias + " días");
            bg = NARANJA;
        } else {
            c.setText("Faltan " + dias + " días");
            bg = VERDE;
        }
        c.setTextSize(12);
        c.setTypeface(Typeface.DEFAULT_BOLD);
        c.setTextColor(Color.WHITE);
        GradientDrawable f = new GradientDrawable();
        f.setColor(bg);
        f.setCornerRadius(dp(14));
        c.setBackground(f);
        c.setPadding(dp(10), dp(5), dp(10), dp(5));
        return c;
    }

    /** Círculo de color suave con el ícono de la tarea. */
    private TextView circuloIcono(String emoji, int colorFondo) {
        TextView c = new TextView(this);
        c.setText(emoji);
        c.setTextSize(24);
        c.setGravity(Gravity.CENTER);
        GradientDrawable f = new GradientDrawable();
        f.setShape(GradientDrawable.OVAL);
        f.setColor(colorFondo);
        c.setBackground(f);
        c.setLayoutParams(new LinearLayout.LayoutParams(dp(50), dp(50)));
        return c;
    }

    private int colorSuavePara(Plan.Evento e) {
        if (e.titulo.contains("COSECHA FINAL")) return Color.rgb(255, 236, 179);
        if (e.titulo.contains("Cosecha base")) return Color.rgb(200, 230, 201);
        if (e.titulo.contains("Control")) return Color.rgb(178, 223, 180);
        if (e.titulo.contains("Foliar")) return Color.rgb(178, 223, 219);
        if (e.titulo.contains("suelo")) return Color.rgb(215, 204, 200);
        if (e.titulo.contains("engrose")) return Color.rgb(255, 224, 178);
        if (e.titulo.contains("CARENCIA")) return Color.rgb(255, 205, 210);
        return VERDE_CLARO;
    }

    private String iconoPara(Plan.Evento e) {
        if (e.titulo.contains("COSECHA FINAL")) return "🌾";
        if (e.titulo.contains("Cosecha base")) return "🌱";
        if (e.titulo.contains("Control")) return "🚜";
        if (e.titulo.contains("Foliar")) return "💧";
        if (e.titulo.contains("suelo")) return "🟤";
        if (e.titulo.contains("engrose")) return "📈";
        if (e.titulo.contains("CARENCIA")) return "⛔";
        return "🌿";
    }

    private int colorPara(Plan.Evento e) {
        if (e.titulo.contains("COSECHA FINAL")) return Color.rgb(230, 150, 0);
        if (e.titulo.contains("Cosecha base")) return VERDE;
        if (e.titulo.contains("Control")) return VERDE_OSCURO;
        if (e.titulo.contains("Foliar")) return Color.rgb(0, 131, 143);
        if (e.titulo.contains("suelo")) return Color.rgb(121, 85, 72);
        if (e.titulo.contains("engrose")) return NARANJA;
        if (e.titulo.contains("CARENCIA")) return ROJO;
        return VERDE;
    }

    private void actualizarTextoFecha() {
        fechaBtn.setText("📅  " + Plan.fechaLinda(iso(selCal)));
    }

    private String iso(Calendar c) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
