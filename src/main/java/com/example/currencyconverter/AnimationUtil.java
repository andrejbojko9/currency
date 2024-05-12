package com.example.currencyconverter;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class AnimationUtil {

    public static void fadeInAnimation(TextView textView) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1000);
        textView.startAnimation(fadeIn);
    }

    public static void fadeOutAnimation(TextView textView) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(1000);
        textView.startAnimation(fadeOut);
    }
}