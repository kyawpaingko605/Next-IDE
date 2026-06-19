package com.hyperion.nextide.settings;

// ပြင်ဆင်ချက် - ရိုးရိုး Activity အစား AppCompatActivity သို့ ပြောင်းလဲခြင်း
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
// ပြင်ဆင်ချက် - AppCompat androidx ActionBar သို့ ပြောင်းလဲခြင်း
import androidx.appcompat.app.ActionBar; 
import com.hyperion.nextide.R;

public class AdvancedSettings extends AppCompatActivity 
{ 
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advancedsettings); 
        
        // ပြင်ဆင်ချက် - AppCompatActivity အတွက် getSupportActionBar() ကို အသုံးပြုခြင်း
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true); 
            actionBar.setTitle("Advanced Settings"); 
        }
    }
}
