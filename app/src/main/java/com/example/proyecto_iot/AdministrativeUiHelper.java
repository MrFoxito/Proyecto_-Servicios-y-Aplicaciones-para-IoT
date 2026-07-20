package com.example.proyecto_iot;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Applies only presentation tokens to the administrator surfaces. It has no
 * dependency on repositories, navigation destinations or application state.
 */
public final class AdministrativeUiHelper {
    private AdministrativeUiHelper() { }

    public static void apply(Activity activity, View root, int navigationId) {
        if (root == null) return;
        root.setBackgroundColor(color(activity, R.color.admin_surface));
        normalizeTree(activity, root);
        View navigation = root.findViewById(navigationId);
        if (navigation instanceof ViewGroup) applyNavigation(activity, (ViewGroup) navigation);
    }

    private static void normalizeTree(Activity activity, View view) {
        if (view instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) view;
            card.setCardBackgroundColor(color(activity, R.color.admin_surface_raised));
            card.setStrokeColor(color(activity, R.color.admin_outline));
        } else if (view instanceof TextInputLayout) {
            TextInputLayout input = (TextInputLayout) view;
            int primary = color(activity, R.color.admin_text_primary);
            int secondary = color(activity, R.color.admin_text_secondary);
            input.setBoxStrokeColorStateList(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}},
                    new int[]{primary, color(activity, R.color.admin_outline)}));
            input.setHintTextColor(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}},
                    new int[]{primary, secondary}));
        } else if (view instanceof TextView) {
            normalizeText(activity, (TextView) view);
        } else if (view.getBackground() instanceof ColorDrawable) {
            // Legacy role layouts contain many hard-coded neutral/blue blocks.
            // Treat flat presentation surfaces consistently; buttons and shaped
            // status resources are deliberately left alone.
            ColorDrawable drawable = (ColorDrawable) view.getBackground();
            if (drawable.getColor() != Color.TRANSPARENT) {
                view.setBackgroundColor(color(activity, R.color.admin_surface_raised));
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) normalizeTree(activity, group.getChildAt(i));
        }
    }

    private static void normalizeText(Activity activity, TextView text) {
        int source = text.getCurrentTextColor();
        int size = Math.round(text.getTextSize() / activity.getResources().getDisplayMetrics().scaledDensity);
        if (size < 11) text.setTextSize(12);
        if (isDanger(source)) {
            text.setTextColor(color(activity, R.color.app_danger_text));
            return;
        }
        if (isPositive(source)) {
            text.setTextColor(color(activity, R.color.admin_status_positive));
            return;
        }
        if (isWarm(source)) {
            text.setTextColor(color(activity, R.color.admin_status_warning));
            return;
        }
        if (isNearWhite(source)) {
            text.setTextColor(color(activity, R.color.admin_on_action));
        } else if (size >= 18) {
            text.setTextColor(color(activity, R.color.admin_text_primary));
        } else if (size <= 12) {
            text.setTextColor(color(activity, R.color.admin_text_tertiary));
        } else {
            text.setTextColor(color(activity, R.color.admin_text_secondary));
        }
    }

    private static boolean isNearWhite(int value) {
        return Color.red(value) > 230 && Color.green(value) > 230 && Color.blue(value) > 230;
    }

    private static boolean isDanger(int value) {
        return Color.red(value) > 145 && Color.green(value) < 100 && Color.blue(value) < 105;
    }

    private static boolean isPositive(int value) {
        return Color.green(value) > Color.red(value) + 25 && Color.green(value) > Color.blue(value);
    }

    private static boolean isWarm(int value) {
        return Color.red(value) > 105 && Color.green(value) > 75 && Color.blue(value) < 100;
    }

    private static void applyNavigation(Activity activity, ViewGroup navigation) {
        navigation.setBackgroundColor(color(activity, R.color.admin_surface_raised));
        for (int i = 0; i < navigation.getChildCount(); i++) {
            View item = navigation.getChildAt(i);
            boolean active = item.getBackground() != null && !(item.getBackground() instanceof ColorDrawable
                    && ((ColorDrawable) item.getBackground()).getColor() == Color.TRANSPARENT);
            if (active) item.setBackgroundResource(R.drawable.ad_pill_active);
            else item.setBackgroundColor(Color.TRANSPARENT);
            if (!(item instanceof ViewGroup)) continue;
            ViewGroup tab = (ViewGroup) item;
            for (int child = 0; child < tab.getChildCount(); child++) {
                View tabChild = tab.getChildAt(child);
                if (tabChild instanceof ImageView) {
                    ((ImageView) tabChild).setColorFilter(color(activity,
                            active ? R.color.admin_on_action : R.color.admin_nav_inactive));
                } else if (tabChild instanceof TextView) {
                    ((TextView) tabChild).setTextColor(color(activity,
                            active ? R.color.admin_on_action : R.color.admin_nav_inactive));
                }
            }
        }
    }

    private static int color(Activity activity, int id) {
        return ContextCompat.getColor(activity, id);
    }
}
