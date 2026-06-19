package com.hyperion.nextide;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileManager extends AppCompatActivity {

    private String path;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private Context mContext;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filemanager);

        mContext = this;

        // XML layout ထဲက ListView ကို ချိတ်ဆက်ခြင်း
        listView = (ListView) findViewById(android.R.id.list);

        checkAndRequestPermissions();

        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
        } else {
            // NextIDE folder ကို အဓိက လမ်းကြောင်းအဖြစ် သတ်မှတ်ခြင်း
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NextIDE/";
        }
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(path);
        }

        refreshFileList();

        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (values != null && position < values.size()) {
                        String filename = values.get(position);
                        
                        if (path.endsWith(File.separator)) {
                            filename = path + filename;
                        } else {
                            filename = path + File.separator + filename;
                        }
                        
                        File clickedFile = new File(filename);

                        if (clickedFile.isDirectory()) {
                            Intent intent = new Intent(FileManager.this, FileManager.class);
                            intent.putExtra("path", filename);
                            startActivity(intent);
                        } else {
                            try {
                                FileOpen.openFile(mContext, clickedFile);
                            } catch (IOException e) {
                                Toast.makeText(mContext, "Error opening file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });
        }
    }

    private void refreshFileList() {
        values = new ArrayList<>();
        File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(file);
                }
            }
        }
        Collections.sort(values);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);
        
        if (listView != null) {
            listView.setAdapter(adapter);
        }
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Please grant All Files Access for Next-IDE", Toast.LENGTH_LONG).show();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, STORAGE_PERMISSION_CODE);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFileList();
    }
}
