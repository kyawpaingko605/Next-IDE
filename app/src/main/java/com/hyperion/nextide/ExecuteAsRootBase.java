package com.hyperion.nextide;

import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;

public abstract class ExecuteAsRootBase
{
    private static final String TAG = "ROOT_EXEC";

    public static void canRunRootCommands(String path)
    {
        // အသုံးမလိုလျှင် ဒီအတိုင်း ထားနိုင်ပါသည်
    }

    public static boolean canRunRootCommands()
    {
        boolean retval = false;
        Process suProcess = null;
        DataOutputStream os = null;
        BufferedReader reader = null;

        try
        {
            // su process စတင်ခြင်း
            suProcess = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(suProcess.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));

            // လက်ရှိ user ရဲ့ uid ကို စစ်ဆေးရန် command ပို့ခြင်း
            os.writeBytes("id\n");
            os.flush();

            // ပြင်ဆင်ချက် - Android 12 တွင် ဖုန်းစနစ်ကြောင့် အချိန်အကြာကြီး တုံ့ဆိုင်းမနေစေရန် ready() စစ်ဆေးခြင်း
            String currUid = null;
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 1500) {
                if (reader.ready()) {
                    currUid = reader.readLine();
                    break;
                }
                Thread.sleep(50);
            }

            boolean exitSu = false;

            if (currUid == null)
            {
                Log.d(TAG, "Can't get root access or denied by user");
            }
            else if (currUid.contains("uid=0"))
            {
                retval = true;
                exitSu = true;
                Log.d(TAG, "Root access granted");
            }
            else
            {
                exitSu = true;
                Log.d(TAG, "Root access rejected: " + currUid);
            }

            if (exitSu)
            {
                os.writeBytes("exit\n");
                os.flush();
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
        }
        finally 
        {
            try {
                if (os != null) os.close();
                if (reader != null) reader.close();
                if (suProcess != null) suProcess.destroy();
            } catch (IOException ignored) {}
        }

        return retval;
    }

    public final boolean execute()
    {
        boolean retval = false;
        Process suProcess = null;
        DataOutputStream os = null;

        try
        {
            ArrayList<String> commands = getCommandsToExecute();
            if (commands != null && !commands.isEmpty())
            {
                suProcess = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(suProcess.getOutputStream());

                for (String currCommand : commands)
                {
                    os.writeBytes(currCommand + "\n");
                    os.flush();
                }

                os.writeBytes("exit\n");
                os.flush();

                try
                {
                    int suProcessRetval = suProcess.waitFor();
                    retval = (255 != suProcessRetval);
                }
                catch (Exception ex)
                {
                    Log.e(TAG, "Error executing root action", ex);
                }
            }
        }
        catch (Exception ex)
        {
            Log.w(TAG, "Error executing internal operation", ex);
        }
        finally 
        {
            try {
                if (os != null) os.close();
                if (suProcess != null) suProcess.destroy();
            } catch (IOException ignored) {}
        }

        return retval;
    }

    protected abstract ArrayList<String> getCommandsToExecute();
}
