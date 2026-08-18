package com.cs.atto3probe;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import dalvik.system.PathClassLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView status, log;
    private final StringBuilder report = new StringBuilder();
    private static final String[] JARS = {
            "/system/framework/bmmcamera.jar",
            "/system/framework/framework.jar"
    };
    private static final String[] CLASSES = {
            "android.hardware.AVMCamera",
            "android.hardware.JNIBMMCamera",
            "android.hardware.BmmCameraInfo"
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);
        log = findViewById(R.id.log);
        ((Button)findViewById(R.id.scan)).setOnClickListener(v -> scan());
        ((Button)findViewById(R.id.save)).setOnClickListener(v -> saveLog());
        append("CS Camera Probe started");
        append("Read-only diagnostic build. No vehicle control commands are issued.");
    }

    private void scan() {
        report.setLength(0);
        status.setText("Taranıyor…");
        append("=== BYD CAMERA FRAMEWORK SCAN ===");
        append("Android: " + android.os.Build.VERSION.RELEASE + " / SDK " + android.os.Build.VERSION.SDK_INT);
        append("Device: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);

        for (String jar : JARS) {
            File f = new File(jar);
            append((f.exists() ? "[OK] " : "[--] ") + jar + (f.exists() ? " size=" + f.length() : ""));
        }

        ClassLoader parent = getClassLoader();
        ClassLoader bydLoader = parent;
        File bmm = new File("/system/framework/bmmcamera.jar");
        if (bmm.exists()) {
            bydLoader = new PathClassLoader(bmm.getAbsolutePath(), parent);
            append("[OK] PathClassLoader created for bmmcamera.jar");
        }

        int found = 0;
        for (String cn : CLASSES) {
            try {
                Class<?> c = Class.forName(cn, false, bydLoader);
                found++;
                append("\n[CLASS] " + cn);
                for (Constructor<?> x : c.getDeclaredConstructors()) append("  ctor " + x.toString());
                for (Method m : c.getDeclaredMethods()) append("  method " + m.getName() + Arrays.toString(m.getParameterTypes()) + " -> " + m.getReturnType().getSimpleName());
                for (Field f : c.getDeclaredFields()) {
                    String line = "  field " + f.getName() + " : " + f.getType().getSimpleName();
                    try {
                        if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                            f.setAccessible(true);
                            Object val = f.get(null);
                            if (val != null && (val instanceof Number || val instanceof String || val instanceof Boolean)) line += " = " + val;
                        }
                    } catch (Throwable ignored) {}
                    append(line);
                }
            } catch (Throwable t) {
                append("[MISS] " + cn + " -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }
        status.setText(found > 0 ? "BYD kamera sınıfları bulundu: " + found : "BYD kamera sınıfı bulunamadı");
        append("\n=== SCAN END ===");
    }

    private void saveLog() {
        try {
            File dir = getExternalFilesDir(null);
            if (dir == null) dir = getFilesDir();
            File out = new File(dir, "camera_probe_log.txt");
            try (FileOutputStream fos = new FileOutputStream(out, false)) {
                fos.write(report.toString().getBytes(StandardCharsets.UTF_8));
            }
            status.setText("Log kaydedildi: " + out.getAbsolutePath());
            append("[SAVED] " + out.getAbsolutePath());
        } catch (Throwable t) {
            status.setText("Log kaydedilemedi");
            append("[ERROR] saveLog: " + t);
        }
    }

    private void append(String s) {
        String ts = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        report.append('[').append(ts).append("] ").append(s).append('\n');
        if (log != null) log.setText(report.toString());
    }
}
