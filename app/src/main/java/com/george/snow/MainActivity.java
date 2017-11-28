package com.george.snow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    private FrameLayout frameLayout;
    private static final String TIME_ADDED = "time_added";
    private static final String START_TIME = "start_time";
    private static final String STOP_TIME = "stop_time";
    private Cronometer cronometerVariable = new Cronometer(this);

    static boolean isActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //First time the app starts we check if there is added time
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean contains = sharedPreferences.contains(TIME_ADDED);
        if(!contains){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(TIME_ADDED,0);
            editor.apply();
        }

        //Checking internet availability and if time is stored then push to firebase
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected){
            cronometerVariable.sendOnLine();
        }

        frameLayout = (FrameLayout) findViewById(R.id.frameID);

        //We add some snow for effect in Layout
        SnowFallView snowFallView = new SnowFallView(this);
        snowFallView.setBackgroundDrawable(getResources().getDrawable(R.drawable.rudolf));
        frameLayout.addView(snowFallView);

        snowFallView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SecondActivity.class);
                startActivity(intent);
            }
        });

    }



    @Override
    protected void onStop() {
        super.onStop();
        Log.e("MainStop", "MainStop");
    }

    //We use this method to set start time in onResume
    @Override
    protected void onResume() {
        super.onResume();

        //set true if app is running
        isActive=true;

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected){
            long startTime = System.currentTimeMillis();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(START_TIME,startTime);
            editor.apply();

            cronometerVariable.setStartTime();
        }
    }




    @Override
    protected void onStart() {
        super.onStart();
            }

    //We use this method to set stop time in onPause method because onStop doesnt get called when we go to second activity and back
    @Override
    protected void onPause() {
        super.onPause();

        //set true if app is running
        isActive=false;

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected){
            long stopTime = System.currentTimeMillis();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(STOP_TIME,stopTime);
            editor.apply();

            cronometerVariable.getStopTime();
        }

    }

    //Just some effect,not necessary for time keeping
    private class SnowFallView extends View {
        private int snow_flake_count = 10;
        private final List<Drawable> drawables = new ArrayList<Drawable>();
        private int[][] coords;
        private final Drawable snow_flake;
        private final Drawable snow_flake2;

        public SnowFallView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);

            snow_flake = context.getResources().getDrawable(R.drawable.flake);
            snow_flake.setBounds(0, 0, snow_flake.getIntrinsicWidth(), snow_flake.getIntrinsicHeight());

            snow_flake2 = context.getResources().getDrawable(R.drawable.flake);
            snow_flake2.setBounds(0, 0, (int) (snow_flake.getIntrinsicWidth() / 2), (int) (snow_flake.getIntrinsicHeight() / 2));


        }

        @Override
        protected void onSizeChanged(int width, int height, int oldw, int oldh) {
            super.onSizeChanged(width, height, oldw, oldh);
            Random random = new Random();
            Interpolator interpolator = new LinearInterpolator();

            snow_flake_count = Math.max(width, height) / 10;
            coords = new int[snow_flake_count][];
            drawables.clear();
            for (int i = 0; i < snow_flake_count; i++) {
//                Log.e("sizeChange", " ang width = " + width + " the height = " + height);
                Animation animation = new TranslateAnimation(0, height / 10
                        - random.nextInt(height / 5), 0, height + 30);
                animation.setDuration(10 * height + random.nextInt(5 * height));
                animation.setRepeatCount(-1);
                animation.initialize(10, 10, 10, 10);
                animation.setInterpolator(interpolator);

                coords[i] = new int[]{random.nextInt(width - 30), -30};

                int y = 0;
                y = random.nextInt(2);
                if (y == 0)
                    drawables.add(new AnimateDrawable(snow_flake, animation));
                else
                    drawables.add(new AnimateDrawable(snow_flake2, animation));

                /*drawables.add(new AnimateDrawable(snow_flake, animation));*/
                animation.setStartOffset(random.nextInt(20 * height));
                animation.startNow();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            for (int i = 0; i < snow_flake_count; i++) {
                Drawable drawable = drawables.get(i);
                canvas.save();
                canvas.translate(coords[i][0], coords[i][1]);
                drawable.draw(canvas);
                canvas.restore();
                /*Log.e("sizeChange", " ang width = " + coords[i][0] + " the height = " + coords[i][0]);*/
            }
            invalidate();
        }

    }

    public class AnimateDrawable extends ProxyDrawable {

        private Animation mAnimation;
        private Transformation mTransformation = new Transformation();

        public AnimateDrawable(Drawable target) {
            super(target);
        }

        public AnimateDrawable(Drawable target, Animation animation) {
            super(target);
            mAnimation = animation;
        }

        public Animation getAnimation() {
            return mAnimation;
        }

        public void setAnimation(Animation anim) {
            mAnimation = anim;
        }

        public boolean hasStarted() {
            return mAnimation != null && mAnimation.hasStarted();
        }

        public boolean hasEnded() {
            return mAnimation == null || mAnimation.hasEnded();
        }

        @Override
        public void draw(Canvas canvas) {
            Drawable dr = getProxy();
            if (dr != null) {
                int sc = canvas.save();
                Animation anim = mAnimation;
                if (anim != null) {
                    anim.getTransformation(
                            AnimationUtils.currentAnimationTimeMillis(),
                            mTransformation);
                    canvas.concat(mTransformation.getMatrix());
                }
                dr.draw(canvas);
                canvas.restoreToCount(sc);
            }
        }
    }

    public class ProxyDrawable extends Drawable {

        private Drawable mProxy;

        public ProxyDrawable(Drawable target) {
            mProxy = target;
        }

        public Drawable getProxy() {
            return mProxy;
        }

        public void setProxy(Drawable proxy) {
            if (proxy != this) {
                mProxy = proxy;
            }
        }

        @Override
        public void draw(Canvas canvas) {
            if (mProxy != null) {
                mProxy.draw(canvas);
            }
        }

        @Override
        public int getIntrinsicWidth() {
            return mProxy != null ? mProxy.getIntrinsicWidth() : -1;
        }

        @Override
        public int getIntrinsicHeight() {
            return mProxy != null ? mProxy.getIntrinsicHeight() : -1;
        }

        @Override
        public int getOpacity() {
            return mProxy != null ? mProxy.getOpacity() : PixelFormat.TRANSPARENT;
        }

        @Override
        public void setFilterBitmap(boolean filter) {
            if (mProxy != null) {
                mProxy.setFilterBitmap(filter);
            }
        }

        @Override
        public void setDither(boolean dither) {
            if (mProxy != null) {
                mProxy.setDither(dither);
            }
        }


        @Override
        public void setAlpha(int alpha) {
            if (mProxy != null) {
                mProxy.setAlpha(alpha);
            }
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            if (mProxy != null) {
                mProxy.setColorFilter(colorFilter);
            }
        }
    }
}

