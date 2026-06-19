package com.hyperion.nextide;

import android.content.Context;
import java.io.File;

public class Bbuild {
    
    private Context context;
    private String toolsDir;
    private String ecjPath;
    private String d8Path;
    private String apkSignerPath;
    private String aapt2Path;
    private String zipalignPath;

    public Bbuild(Context context) {
        this.context = context;
        
        // ၁။ Assets ထဲက .jar ဖိုင်များအတွက် Internal Storage လမ်းကြောင်း
        this.toolsDir = context.getFilesDir().getAbsolutePath() + "/tools/";
        this.ecjPath = toolsDir + "ecj.jar";
        this.d8Path = toolsDir + "d8.jar";
        this.apkSignerPath = toolsDir + "apksigner.jar";
        
        // ၂။ jniLibs/arm64-v8a ထဲက .so ဖိုင်များအတွက် Native Library လမ်းကြောင်း (Android 12+ အကြိုက်)
        String nativeDir = context.getApplicationInfo().nativeLibraryDir;
        this.aapt2Path = nativeDir + "/libaapt2.so";
        this.zipalignPath = nativeDir + "/libzipalign.so";
    }

    /**
     * Java ဖိုင်များကို Compile လုပ်မည့် နေရာ
     */
    public void compileJava(String sourceFolder, String outputBinFolder) {
        try {
            String command = "dalvikvm -cp " + ecjPath + " org.eclipse.jdt.internal.compiler.batch.Main " 
                           + sourceFolder + " -d " + outputBinFolder;
                           
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * .class ဖိုင်များကို .dex ပြောင်းမည့် နေရာ
     */
    public void convertToDex(String binFolder, String outputDexFolder) {
        try {
            // d8.jar ကို com.android.tools.r8.D8 Main Class နဲ့ တွဲပြီး run ပေးရပါမယ်
            String command = "dalvikvm -cp " + d8Path + " com.android.tools.r8.D8 --output " 
                           + outputDexFolder + " " + binFolder + "/*.class";
                           
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
