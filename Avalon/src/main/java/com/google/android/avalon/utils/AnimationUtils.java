package com.google.android.avalon.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

/**
 * Created by jinyan on 5/17/14.
 *
 * Some useful animations to have. Note that these functions MUST BE CALLED ON THE MAIN THREAD.
 */
public class AnimationUtils {
    private static final int SHORT_ANIM_DURATION = 500;     // ms

    /**
     * Helper function to crossfade the views
     */
    public static void fadeIn(final View v) {
        if (v != null) {
            v.animate()
                    .alpha(1f)
                    .setDuration(SHORT_ANIM_DURATION)
                    .setListener(null);
        }
    }

    /**
     * Helper function to crossfade the views
     */
    public static void fadeOut(final View v) {
        if (v != null) {
            v.animate()
                    .alpha(0f)
                    .setDuration(SHORT_ANIM_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            v.setVisibility(View.GONE);
                        }
                    });
        }
    }
}
