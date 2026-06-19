package com.hyperion.nextide;

import android.content.Context;
import java.io.File;

public class Bbuild {
    
    private Context context;
    private String toolsDir;
    private String ecjPath;
    private String d8Path;
    private String apkSignerPath;
    private String javacPath;
    private String zipalignPath;

    public Bbuild(Context context) {
        this.context = context;
        // Android 12 အကြိုက် Internal Storage လမ်းကြောင်း သတ်မှတ်ခြင်း
        this.toolsDir = context.getFilesDir().getAbsolutePath() + "/tools/";
        this.ecjPath = toolsDir + "ecj.jar";
        this.d8Path = toolsDir + "d8.jar";
        this.apkSignerPath = toolsDir + "apksigner.jar";
        this.javacPath = toolsDir + "javac";
        this.zipalignPath = toolsDir + "zipalign";
    }

    /**
     * Java ဖိုင်များကို Compile လုပ်မည့် နေရာ
     */
    public void compileJava(String sourceFolder, String outputBinFolder) {
        try {
            // ဥပမာ - ecj.jar သို့မဟုတ် javac ကို သုံးပြီး run မည့် command
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
            String command = "dalvikvm -jar " + d8Path + " --output " + outputDexFolder + " " + binFolder + "/*.class";
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
