package com.speedata.yilang;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by lenovo-pc on 2017/6/21.
 */

public class DialogShow {
    private Dialog dialog = null;
    private Context context;

    public DialogShow(Context context) {
        this.context = context;
    }

    public void DialogShow(@DrawableRes int resId) {
        dialog = new Dialog(context, R.style.edit_AlertDialog_style);
        dialog.setContentView(R.layout.dialog_layout);
        ImageView imageView = (ImageView) dialog.findViewById(R.id.start_img);
        imageView.setImageResource(resId);
        //选择true的话点击其他地方可以使dialog消失，为false的话不会消失
        dialog.setCanceledOnTouchOutside(true); // Sets whether this dialog is
        Window w = dialog.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        lp.y = 40;
        dialog.onWindowAttributesChanged(lp);
        imageView.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

        dialog.show();

        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

            }
        },500);
    }

}
