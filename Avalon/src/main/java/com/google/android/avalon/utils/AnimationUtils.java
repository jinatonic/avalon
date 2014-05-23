package com.google.android.avalon.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;

import com.google.android.R;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jinyan on 5/17/14.
 *
 * Some useful animations to have. Note that these functions MUST BE CALLED ON THE MAIN THREAD.
 */
public class AnimationUtils {
    private static final int SHORT_ANIM_DURATION = 500;     // ms
    private static final int LONG_SWITCH_DURATION = 10000;  // ms

    // This synchronized map is a way to ensure that we don't accidentally allow any pending changes
    // accidentally override the current change.
    private static final Map<Integer, Long> PENDING_VIEWS = new ConcurrentHashMap<Integer, Long>();

    /**
     * Helper function to crossfade the views
     */
    public static void fadeIn(final View v) {
        if (v != null) {
            v.setVisibility(View.VISIBLE);
            v.animate()
                    .alpha(1f)
                    .setDuration(SHORT_ANIM_DURATION);
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
                            v.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }

    /**
     * Helper function for showing an image for a long duration then switch to the other image
     */
    public static void switchImageLong(final ImageView v, final int img1, final int img2) {
        final Long curr = SystemClock.uptimeMillis();
        PENDING_VIEWS.put(v.hashCode(), curr);
        setImage(v, img1, false);
        if (v != null) {
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    // Only run it if the image hasn't already been over-ridden
                    if (PENDING_VIEWS.remove(v.hashCode()) == curr) {
                        setImage(v, img2, false);
                    }
                }
            }, LONG_SWITCH_DURATION);
        }
    }

    /**
     * Use fade to fade out the current image, set image, then fade in.
     */
    public static void setImage(final ImageView v, final int imgRes, final boolean storePrev) {
        PENDING_VIEWS.remove(v.hashCode());
        Object tag = v.getTag(R.id.current_image_res_id);
        if (tag == null || (Integer) tag != imgRes) {
            if (tag != null && storePrev) {
                v.setTag(R.id.prev_image_res_id, tag);
            } else {
                v.setTag(R.id.prev_image_res_id, null);
            }

            v.setVisibility(View.INVISIBLE);
            v.setImageResource(imgRes);
            v.setTag(R.id.current_image_res_id, imgRes);
            fadeIn(v);
        }
    }

    /**
     * Restore the previous image
     */
    public static void restorePrev(final ImageView v) {
        PENDING_VIEWS.remove(v.hashCode());
        Object tag = v.getTag(R.id.prev_image_res_id);
        if (tag != null) {
            setImage(v, (Integer) tag, false);
        }
    }
}
