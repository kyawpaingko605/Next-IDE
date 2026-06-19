package com.hyperion.nextide;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;

public class FileOpen {

    public static void openFile(Context context, File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist or is null");
        }

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        } else {
            uri = Uri.fromFile(file);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            intent.setDataAndType(uri, "application/msword");
        } else if (fileName.endsWith(".pdf")) {
            intent.setDataAndType(uri, "application/pdf");
        } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (fileName.endsWith(".zip")) {
            intent.setDataAndType(uri, "application/zip"); 
        } else if (fileName.endsWith(".rar")) {
            intent.setDataAndType(uri, "application/x-rar-compressed"); 
        } else if (fileName.endsWith(".rtf")) {
            intent.setDataAndType(uri, "application/rtf");
        } else if (fileName.endsWith(".wav")) {
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (fileName.endsWith(".mp3")) {
            intent.setDataAndType(uri, "audio/mpeg"); 
        } else if (fileName.endsWith(".gif")) {
            intent.setDataAndType(uri, "image/gif");
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
            intent.setDataAndType(uri, "image/*");
        } else if (fileName.endsWith(".txt") || fileName.endsWith(".java") || fileName.endsWith(".xml") || fileName.endsWith(".json")) {
            intent.setDataAndType(uri, "text/plain");
        } else if (fileName.endsWith(".3gp") || fileName.endsWith(".mpg") || fileName.endsWith(".mpeg") || 
                   fileName.endsWith(".mpe") || fileName.endsWith(".mp4") || fileName.endsWith(".avi")) {
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }

        // ပြင်ဆင်ချက် - ဖတ်ခွင့်အပြင် အခြား App များမှပါ ရေးခွင့် (Write Permission) ကို တစ်ပါတည်းပေးခြင်း
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        
        // ပြင်ဆင်ချက် - ဖိုင်ဖွင့်မည့် App မရှိပါက Crash ဖြစ်ခြင်းမှ ကာကွယ်ရန် try-catch အသုံးပြုခြင်း
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No app found to open this file type", Toast.LENGTH_SHORT).show();
        }
    }
}
