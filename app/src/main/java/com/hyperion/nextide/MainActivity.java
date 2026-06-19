package com.hyperion.nextide;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.dengxiao.scroll_viewgroup_library.ScrollGroup;

public class MainActivity extends AppCompatActivity
{
    private static final String[] shareStr = {
        "MyApp1","MyApp2","MyApp3","MyApp4","MyApp5","MyApp6","MyApp7","NextIDE"
    };

    public static final String[] reservedWords = { "abstract", "assert",
        "boolean", "break", "byte", "case", "catch", "char", "class",
        "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "false", "final", "finally", "float", "for", "goto",
        "if", "implements", "import", "instanceof", "int", "interface",
        "long", "native", "new", "null", "package", "private", "protected",
        "public", "return", "short", "static", "strictfp", "super",
        "switch", "synchronized", "this", "throw", "throws", "transient",
        "true", "try", "void", "volatile", "while" };

    private EditText LineNumberedEditText = null;
    
    // ပြင်ဆင်ချက် - Android 12 အတွက် Tools လမ်းကြောင်း ကိန်းရှင်
    private String toolsDir;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); 
        
        // Battery Optimization Ignore Request
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName)); 
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        setContentView(R.layout.main); 
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); 
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.image_11);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getSupportActionBar().setElevation(0); 
        }
        
        // ပြင်ဆင်ချက် - UI Freeze ဖြစ်စေသော ScrollGroup ကုဒ်များအား ခေတ္တပိတ်ထားခြင်း
        /*
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     

        ScrollGroup mScrollGroup = (ScrollGroup) findViewById(R.id.mScrollGroup);
        if (mScrollGroup != null) {
            mScrollGroup.setHorizontalOrVertical(true)
                .setStartEndScroll(true)
                .setScrollEdge(width/2)
                .setDuration(1000)
                .setInvalidate();
        }
        */
      
        Window window = this.getWindow();
        if (window != null) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }

        LineNumberedEditText = (EditText) findViewById(R.id.editor_area);
        
        if (LineNumberedEditText != null) {
            SyntaxTextWatcher watcher = new SyntaxTextWatcher(MainActivity.this);
            LineNumberedEditText.addTextChangedListener(watcher); 
        }

        View tvView = findViewById(R.id.tv);
        if (tvView != null) {
            tvView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                { 
                    showBSDialog(); 
                }
            });
        }

        // ခွင့်ပြုချက် အရင်စစ်ဆေးပြီးမှ Folder ဆောက်ရန် ဖိတ်ခေါ်ခြင်း
        checkStoragePermissions();

        // ပြင်ဆင်ချက် - Android 12 အတွက် assets/tools များကို Internal Storage ထဲသို့ Extract လုပ်ခြင်း
        toolsDir = getFilesDir().getAbsolutePath() + "/tools/";
        initAndroid12Tools();
    }

    // ပြင်ဆင်ချက် - UI Freeze မဖြစ်စေဘဲ နောက်ကွယ်မှ Tools များအား ဖြည်ချပေးမည့် လုပ်ဆောင်ချက်
    private void initAndroid12Tools() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    copyAssetsFolder("tools", toolsDir);
                    // Binary ဖိုင်များအား Run ခွင့်ပြုရန် Linux Execution Permission ပေးခြင်း
                    Runtime.getRuntime().exec("chmod -R 755 " + toolsDir);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // ပြင်ဆင်ချက် - Assets ထဲမှ ဖိုင်တွဲတစ်ခုလုံးအား ရွှေ့ပြောင်းပေးမည့် အဓိက Helper လုပ်ဆောင်ချက်
    private void copyAssetsFolder(String assetDir, String destDir) throws Exception {
        String[] files = getAssets().list(assetDir);
        if (files == null) return;

        File targetDir = new File(destDir);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        for (String file : files) {
            String assetPath = assetDir + (assetDir.isEmpty() ? "" : "/") + file;
            String destPath = destDir + (destDir.endsWith("/") ? "" : "/") + file;
            
            String[] subFiles = getAssets().list(assetPath);
            if (subFiles != null && subFiles.length > 0) {
                copyAssetsFolder(assetPath, destPath);
            } else {
                File outFile = new File(destPath);
                if (!outFile.exists()) {
                    InputStream in = getAssets().open(assetPath);
                    OutputStream out = new FileOutputStream(outFile);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    in.close();
                    out.flush();
                    out.close();
                }
            }
        }
    }

    private void checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                    startActivity(intent);
                } catch (Exception e) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivity(intent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                createProjectDirectories();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, 
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1);
            } else {
                createProjectDirectories();
            }
        }
    }

    private void createProjectDirectories() {
        try {
            File externalStorage = Environment.getExternalStorageDirectory();
            if (externalStorage != null) {
                File baseDir = new File(externalStorage, "NextIDE");
                if (!baseDir.exists()) {
                    baseDir.mkdirs();
                }
                for (String folder : shareStr) {
                    File subDir = new File(baseDir, folder);
                    if (!subDir.exists()) {
                        subDir.mkdirs();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createProjectDirectories();
            }
        }
    }

    private void showBSDialog()
    {
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.bs_rv);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this)); 
            SimpleStringRecyclerViewAdapter adapter = new SimpleStringRecyclerViewAdapter(this);
            adapter.setItemClickListener(new SimpleStringRecyclerViewAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(int pos)
                    {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "MyApp" + (pos + 1) + " " + "Folder", Toast.LENGTH_LONG).show();
                    }
                });
            recyclerView.setAdapter(adapter);
        }
        dialog.setContentView(view); 
        if (dialog.getWindow() != null) {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);  
            dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            dialog.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }

        View parentView = (View) view.getParent();
        if (parentView != null) {
            BottomSheetBehavior<View> mBehavior = BottomSheetBehavior.from(parentView);
            mBehavior.setPeekHeight(1400);
        }

        dialog.show(); 
    }

    public static class SimpleStringRecyclerViewAdapter
    extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder>
    {
        public ItemClickListener mItemClickListener;

        public void setItemClickListener(ItemClickListener listener)
        {
            mItemClickListener = listener;
        }

        public interface ItemClickListener
        {
            void onItemClick(int pos); 
        }

        private Context mContext;

        public static class ViewHolder extends RecyclerView.ViewHolder
        {
            public final ImageView mImageView;
            public final TextView mTextView;

            public ViewHolder(View view)
            {
                super(view);
                mImageView = (ImageView) view.findViewById(R.id.avatar);
                mTextView = (TextView) view.findViewById(R.id.tv);
            }
        }

        public SimpleStringRecyclerViewAdapter(Context context)
        {
            super();
            mContext = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position)
        {
            if (holder.mTextView != null) {
                holder.mTextView.setText(shareStr[position]);
                holder.mTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            if (mItemClickListener != null) {
                                mItemClickListener.onItemClick(holder.getAdapterPosition());
                            }
                        }
                    });
            }
        }

        @Override
        public int getItemCount()
        {
            return shareStr.length;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true; 
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.one || id == R.id.two || id == R.id.three)
        {
            return true;
        }
        if (id == R.id.files)
        {
            Intent manager = new Intent(MainActivity.this, FileManager.class);
            MainActivity.this.startActivity(manager);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            return true;
        }
        if (id == R.id.iconlib)
        {
            Intent myIntent = new Intent(MainActivity.this, IconPicker.class);
            MainActivity.this.startActivity(myIntent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            return true;
        }
        if (id == R.id.settings)
        {
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(myIntent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            return true;
        }
        if (id == R.id.mergeproj)
        {
            ProjMergeDialog cdd = new ProjMergeDialog(this);
            cdd.show();
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            return true;
        } 
        if (id == R.id.exit)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
