package com.hyperion.nextide;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Advanced Crash Exception Handler for Next-IDE.
 * Saves crash reports directly into NextIDE/crash_logs/ folder for easy debugging.
 */
public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {
    
    private final Thread.UncaughtExceptionHandler defaultUEH;
    private final Context appContext; 

    public TopExceptionHandler(Context context) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        StringBuilder reportBuilder = new StringBuilder();
        
        // Crash ဖြစ်ပွားသည့် အချိန်အား ထည့်သွင်းခြင်း
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        reportBuilder.append("Crash Time: ").append(timeStamp).append("\n\n");
        
        reportBuilder.append(e.toString()).append("\n\n");
        reportBuilder.append("--------- Stack trace ---------\n\n");
        
        StackTraceElement[] arr = e.getStackTrace();
        for (StackTraceElement element : arr) {
            reportBuilder.append("    ").append(element.toString()).append("\n");
        }
        reportBuilder.append("-------------------------------\n\n");

        reportBuilder.append("--------- Cause ---------\n\n");
        Throwable cause = e.getCause();
        if (cause != null) {
            reportBuilder.append(cause.toString()).append("\n\n");
            StackTraceElement[] causeArr = cause.getStackTrace();
            for (StackTraceElement element : causeArr) {
                reportBuilder.append("    ").append(element.toString()).append("\n");
            }
        }
        reportBuilder.append("-------------------------------\n\n");

        String report = reportBuilder.toString();
        Log.e("NextIDE_Crash", report);

        // ပြင်ဆင်ချက် - သုံးစွဲသူ ဖတ်ရှုရလွယ်ကူစေရန် NextIDE/crash_logs/ ဖိုဒါထဲသို့ လှမ်းရေးခြင်း
        try {
            File externalStorage = Environment.getExternalStorageDirectory();
            if (externalStorage != null) {
                File crashDir = new File(externalStorage, "NextIDE/crash_logs");
                if (!crashDir.exists()) {
                    crashDir.mkdirs();
                }
                
                // ဖိုင်အမည်ကို နေ့စွဲ၊ အချိန်ဖြင့် ခွဲခြားသိမ်းဆည်းခြင်း
                String fileDate = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File crashFile = new File(crashDir, "crash_" + fileDate + ".txt");
                
                FileOutputStream trace = new FileOutputStream(crashFile);
                trace.write(report.getBytes());
                trace.flush();
                trace.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            
            // External Storage ရေးမရပါက Internal တွင် Backup အနေဖြင့် ရေးသားခြင်း
            if (appContext != null) {
                try {
                    FileOutputStream trace = appContext.openFileOutput("stack.trace", Context.MODE_PRIVATE);
                    trace.write(report.getBytes());
                    trace.flush();
                    trace.close();
                } catch (IOException ignored) {}
            }
        }

        if (defaultUEH != null) {
            defaultUEH.uncaughtException(t, e);
        }
    }
}
