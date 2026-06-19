package com.hyperion.nextide;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class ProjMergeDialog extends Dialog implements android.view.View.OnClickListener {
    public Activity c;
    public Button yes, no;

    public ProjMergeDialog(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        
        if (yes != null) yes.setOnClickListener(this);
        if (no != null) no.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_yes) {
            if (c != null) {
                c.finish();
            }
            dismiss();
        } else if (id == R.id.btn_no) {
            dismiss();
        }
    }
}
