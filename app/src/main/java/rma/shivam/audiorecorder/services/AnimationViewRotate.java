package rma.shivam.audiorecorder.services;

import android.content.Context;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import rma.shivam.audiorecorder.R;

public class AnimationViewRotate {

    private View view;
    private Context context;
    private boolean isRunning;
    private Animation aniRotateClock;
//    private static Runnable runnable;
//    private static Handler handler;

    public AnimationViewRotate(final View view) {
        this.view = view;
        context = view.getContext();
//        handler = new android.os.Handler();
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                view.setRotation(10);
//                handler.postDelayed(runnable, 10);
//            }
//        };
        aniRotateClock = AnimationUtils.loadAnimation(context, R.anim.rotation_clockwise);
    }

    public void startRotation(){
        isRunning = true;
//        runnable.run();
        view.startAnimation(aniRotateClock);
    }

    public void stopRotation(){
        isRunning = false;
        view.clearAnimation();
//        handler.removeCallbacks(runnable);
    }

    public boolean isRotating(){
        return isRunning;
    }
}
