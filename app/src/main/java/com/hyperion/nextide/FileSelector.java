package com.hyperion.nextide;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Javad on 2019-11-28 at 4:43 PM.
 * [Updated] Fixed Android 11+ Storage and NullPointerException crashes.
 */
public class FileSelector {

    private final Activity context;
    private final String[] extensions;
    private final ArrayList<SelectedFile> itemsData = new ArrayList<>();
    
    public static final String MP4 = ".mp4", MP3 = ".mp3", JPG = ".jpg", JPEG = ".jpeg", 
                               PNG = ".png", DOC = ".doc", DOCX = ".docx", XLS = ".xls", 
                               XLSX = ".xlsx", PDF = ".pdf";

    public FileSelector(Activity context, String[] extensions) {
        this.context = context;
        this.extensions = extensions;
    }

    public interface OnSelectListener {
        void onSelect(String path);
    }

    public void selectFile(OnSelectListener listener) {
        itemsData.clear(); 
        
        // ပြင်ဆင်ချက် - ဖုန်းတစ်လုံးလုံး ရှာပါက Android 12 တွင် ANR/Crash ဖြစ်တတ်သဖြင့် NextIDE Folder အောက်ကိုသာ ဦးတည်ရှာခိုင်းခြင်း
        File rootDir = Environment.getExternalStorageDirectory();
        if (rootDir != null) {
            File nextIdeDir = new File(rootDir, "NextIDE");
            if (nextIdeDir.exists()) {
                listOfFile(nextIdeDir);
            } else {
                listOfFile(rootDir); // NextIDE folder မရှိမှသာ root ကို ရှာခြင်း
            }
        }
        
        dialogFileList(listener);
    }

    private void listOfFile(File dir) {
        if (dir == null || !dir.exists()) return;

        File[] list = dir.listFiles();
        if (list == null) {
            return;
        }

        for (File file : list) {
            if (file.isDirectory()) {
                if (!new File(file, ".nomedia").exists() && !file.getName().startsWith(".")) {
                    listOfFile(file); 
                }
            } else {
                String path = file.getAbsolutePath();
                String lowerPath = path.toLowerCase(); 

                for (String ext : extensions) {
                    if (lowerPath.endsWith(ext.toLowerCase())) {
                        SelectedFile selectedFile = new SelectedFile();
                        selectedFile.path = path;
                        String[] split = path.split("/");
                        selectedFile.name = split[split.length - 1];
                        itemsData.add(selectedFile);
                        break; 
                    }
                }
            }
        }
    }

    private void dialogFileList(OnSelectListener listener) {
        LinearLayout lytMain = new LinearLayout(context);
        lytMain.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        lytMain.setOrientation(LinearLayout.VERTICAL);
        int p = convertToPixels(12);
        lytMain.setPadding(p, p, p, p);
        lytMain.setGravity(Gravity.CENTER);

        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        textView.setText("~JDM File Selector~");
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTypeface(null, Typeface.BOLD);

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convertToPixels(300)));

        lytMain.addView(textView);
        lytMain.addView(recyclerView);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(lytMain);
        dialog.setCancelable(true);
        dialog.show();

        AdapterFile adapter = new AdapterFile(dialog, listener, itemsData);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
    }

    private int convertToPixels(int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int screenWidth() {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    private static class SelectedFile {
        public String path = "";
        public String name = "";
    }

    private class AdapterFile extends RecyclerView.Adapter<AdapterFile.ViewHolder> {

        private final ArrayList<SelectedFile> itemsData;
        private final OnSelectListener listener;
        private final Dialog dialog;

        public AdapterFile(Dialog dialog, OnSelectListener listener, ArrayList<SelectedFile> itemsData) {
            this.itemsData = itemsData;
            this.listener = listener;
            this.dialog = dialog;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.setPadding(0, convertToPixels(8), 0, convertToPixels(8));

            TextView txtName = new TextView(context);
            txtName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            txtName.setTextColor(0xFF000000); 

            TextView txtPath = new TextView(context);
            if (txtPath.getTypeface() != null) {
                txtPath.setTypeface(txtPath.getTypeface(), Typeface.ITALIC);
            } else {
                txtPath.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
            }
            txtPath.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            txtPath.setTextColor(0xFF757575); 

            linearLayout.addView(txtName);
            linearLayout.addView(txtPath);

            return new ViewHolder(linearLayout);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout linearLayout;
            public TextView txtName;
            public TextView txtPath;

            public ViewHolder(View itemLayoutView) {
                super(itemLayoutView);
                linearLayout = (LinearLayout) itemLayoutView;
                txtName = (TextView) linearLayout.getChildAt(0);
                txtPath = (LinearLayout) linearLayout.getChildAt(1) != null ? (TextView) linearLayout.getChildAt(1) : null;
            }
        }

        @Override
        public int getItemCount() {
            return itemsData.size();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
            final SelectedFile selectedFile = itemsData.get(position);

            if (viewHolder.txtName != null) {
                viewHolder.txtName.setText(selectedFile.name);
            }
            if (viewHolder.txtPath != null) {
                viewHolder.txtPath.setText(selectedFile.path);
            }

            viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    listener.onSelect(selectedFile.path);
                }
            });
        }
    }
}
