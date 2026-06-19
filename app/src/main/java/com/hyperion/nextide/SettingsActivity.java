package com.hyperion.nextide;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.hyperion.nextide.settings.AdvancedSettings;
import com.hyperion.nextide.settings.Application;
import com.hyperion.nextide.settings.Buildrun;
import com.hyperion.nextide.settings.Codestyle;
import com.hyperion.nextide.settings.Dropbox;
import com.hyperion.nextide.settings.Editor;
import com.hyperion.nextide.settings.Gsc;
import com.hyperion.nextide.settings.Keybindings;
import com.hyperion.nextide.settings.Legal;
import com.hyperion.nextide.settings.Syntax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends Activity 
{ 
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(true); 
            getActionBar().setTitle("Settings");
        }
      
        String[] settings = new String[] {
            "Application", "Editor", "Code Style", "Syntax",
            "Build & Run", "Dropbox", "Git Source Control",
            "Keybindings", "Advanced Settings", "Donate", "Legal"
        };

        String[] hints = new String[] {
            "Configure global application settings.",
            "Configure the editor.",
            "Configure the code Style.",
            "Syntax settings in the editor.",
            "Configure build & execution settings.",
            "Configure Dropbox settings.",
            "Configure Git Source Control settings.",
            "Configure Keybindings.",
            "Next-IDE Advanced Settings.",
            "Feed a hungry coder 😜 (£5)",
            "Show Legal Information."
        };

        List<Map<String, String>> listArray = new ArrayList<>();
        for(int i = 0; i < settings.length; i++) {
            Map<String, String> listItem = new HashMap<>();
            listItem.put("titleKey", settings[i]);
            listItem.put("detailKey", hints[i]);
            listArray.add(listItem);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, listArray,
                android.R.layout.simple_list_item_2,
                new String[] {"titleKey", "detailKey" },
                new int[] {android.R.id.text1, android.R.id.text2 });

        // ပြင်ဆင်ချက် - XML ထဲက ID ပျောက်နေတဲ့ အမှားကိုကျော်လွှားရန် ListView ကို ကုဒ်ထဲကနေ တိုက်ရိုက်ဆောက်ပြီး သုံးထားပါတယ်
        ListView listView = new ListView(this);
        listView.setAdapter(simpleAdapter);
        
        // Layout ဖိုင်အစား လျှာထိုးဆောက်ထားတဲ့ listView ကို တိုက်ရိုက် View အဖြစ် သတ်မှတ်လိုက်ခြင်း
        setContentView(listView); 
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Intent Variable ကို အပြင်မှာ တစ်ခါတည်း ကြေညာပြီး switch အထဲတွင် ပြန်လည်အသုံးပြုခြင်းဖြင့် 
                // Duplicate Variable Build Error ကို ရှင်းလင်းခြင်း
                Intent targetIntent = null;

                switch(position) {
                    case 0:  
                        targetIntent = new Intent(SettingsActivity.this, Application.class);     
                        break;
                    case 1:  
                        targetIntent = new Intent(SettingsActivity.this, Editor.class);     
                        break;
                    case 2:  
                        targetIntent = new Intent(SettingsActivity.this, Codestyle.class);     
                        break;
                    case 3:  
                        targetIntent = new Intent(SettingsActivity.this, Syntax.class);     
                        break;
                    case 4:  
                        targetIntent = new Intent(SettingsActivity.this, Buildrun.class);     
                        break;
                    case 5:  
                        targetIntent = new Intent(SettingsActivity.this, Dropbox.class);     
                        break;
                    case 6:  
                        targetIntent = new Intent(SettingsActivity.this, Gsc.class);     
                        break;
                    case 7:  
                        targetIntent = new Intent(SettingsActivity.this, Keybindings.class);     
                        break;
                    case 8:  
                        targetIntent = new Intent(SettingsActivity.this, AdvancedSettings.class);     
                        break;
                    case 9:  
                        Uri uri = Uri.parse("https://www.paypal.com/paypalme/liquid8visuals/5");
                        targetIntent = new Intent(Intent.ACTION_VIEW, uri);
                        break;
                    case 10:  
                        targetIntent = new Intent(SettingsActivity.this, Legal.class);     
                        break;
                }

                // Intent သတ်မှတ်ချက် အောင်မြင်ပါက Activity အား ဖွင့်လှစ်ခြင်း
                if (targetIntent != null) {
                    startActivity(targetIntent);
                    
                    // Legal ကဏ္ဍအတွက် ကာကွယ်မှုစနစ်ဖြင့် Animation သတ်မှတ်ခြင်း
                    if (position == 10) {
                        try {
                            // R.anim ဖိုင်များ မရှိပါက App Crash မဖြစ်စေရန် try-catch အုပ်ထားခြင်း
                            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                        } catch (Exception e) {
                            // ဖိုင်မရှိပါက Android ရဲ့ မူရင်း fade-in transition ကို အစားထိုးသုံးပါမည်
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    } else {
                        // အခြား Settings များအတွက် ဘေးတိုက် slide ဝင်မည့် ပုံစံပေးခြင်း
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }
                }
            }
        });
    }
}
