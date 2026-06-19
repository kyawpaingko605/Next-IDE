package com.hyperion.nextide.settings;

// ပြင်ဆင်ချက် - ਰိုးရိုး Activity အစား AppCompatActivity သို့ ပြောင်းလဲခြင်း
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
// ပြင်ဆင်ချက် - AndroidX androidx ActionBar သို့ ပြောင်းလဲခြင်း
import androidx.appcompat.app.ActionBar; 
import com.hyperion.nextide.R;

public class Application extends AppCompatActivity 
{ 
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application); 
        
        // ပြင်ဆင်ချက် - AppCompatActivity အတွက် getSupportActionBar() ကို အသုံးပြုခြင်း
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true); 
            actionBar.setTitle("Application Settings"); 
        }
    }
}
