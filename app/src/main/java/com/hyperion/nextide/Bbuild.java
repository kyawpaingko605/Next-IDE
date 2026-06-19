package com.hyperion.nextide;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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
     * Assets ထဲက .jar ဖိုင်များကို ဖုန်း၏ Internal Storage ထဲသို့ Copy ကူးထည့်ပေးမည့်နေရာ
     */
    public void initTools() {
        String[] jarFiles = {"ecj.jar", "d8.jar", "apksigner.jar"};
        File dir = new File(toolsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (String fileName : jarFiles) {
            File file = new File(dir, fileName);
            // ဖိုင်မရှိသေးမှသာ Assets ထဲကနေ ဆွဲယူပြီး ကူးထည့်မည်
            if (!file.exists()) { 
                try {
                    InputStream in = context.getAssets().open("tools/" + fileName);
                    FileOutputStream out = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    in.close();
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
