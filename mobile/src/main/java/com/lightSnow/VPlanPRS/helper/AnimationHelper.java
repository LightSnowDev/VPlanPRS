package org.inwi.finanzentablet.helper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Jonathan on 26.06.2016.
 */
public class AnimationHelper {

    public static void swipeAnimation(final View fadeInTarget, final View fadeOutTarget, final View containerView, long duration) {
        fadeOutTarget.animate()
                .translationXBy(-containerView.getWidth())
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fadeOutTarget.setVisibility(View.GONE);
                    }
                });

        fadeInTarget.setTranslationX(containerView.getWidth());
        fadeInTarget.animate()
                .translationXBy(-containerView.getWidth())
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        fadeInTarget.setVisibility(View.VISIBLE);
                    }
                });

    }

}
