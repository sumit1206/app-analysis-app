package rma.shivam.audiorecorder.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import rma.shivam.audiorecorder.R;
import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.model.AutoStartHandler;

public class FloatingViewService extends Service {
    Context context;
    private WindowManager mWindowManager;
    private View mFloatingView;
    WindowManager.LayoutParams params;
    View.OnClickListener viewClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //todo button click action here
            sendBroadcastToStop();
        }
    };

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        Utils.logPrint(getClass(), "FloatingViewService", "onCreate");
//        init();
//        mWindowManager.addView(mFloatingView, params);
//        makeMovable(params);

        //….
        //….
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.logPrint(getClass(), "FloatingViewService", "onCreate");
        init();
        mWindowManager.addView(mFloatingView, params);
        makeMovable(params);
        return super.onStartCommand(intent, flags, startId);
    }

    private void init() {
        context = this;
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_layout, null);
        setupUi(mFloatingView);
        mFloatingView.findViewById(R.id.collapsed_iv).setOnClickListener(viewClicked);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

    }

    private void setupUi(View view) {
        if(!AutoStartHandler.isActivated){
            return;
        }
        view.findViewById(R.id.floating_info_layout).setVisibility(View.VISIBLE);
        TextView tvTotal = view.findViewById(R.id.collapsed_iv);//view.findViewById(R.id.floating_total_iteration);
        TextView tvLeft = view.findViewById(R.id.floating_left_iteration);
        int total = AutoStartHandler.iteration * AutoStartHandler.appCount;
        tvTotal.setText(total+"");
        int running = (AutoStartHandler.currentIteration * AutoStartHandler.appCount) + (AutoStartHandler.runningAppNo + 1);
        tvLeft.setText(running+"");
    }

    private void makeMovable(final WindowManager.LayoutParams params){
        mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:


                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;


                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);


                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    void sendBroadcastToStop(){
        Intent intent = new Intent(Constant.ACTION_FLOATING_BUTTON_CLICK);    //action: "msg"
        intent.setPackage(getPackageName());
        intent.putExtra(Constant.ACTION_FLOATING_BUTTON_CLICK, Constant.ACTION_FLOATING_BUTTON_CLICK);
        getApplicationContext().sendBroadcast(intent);
        Utils.logPrint(getClass(),Constant.ACTION_FLOATING_BUTTON_CLICK, Constant.ACTION_FLOATING_BUTTON_CLICK);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.logPrint(getClass(), "FloatingViewService", "onDestroy");
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }
}