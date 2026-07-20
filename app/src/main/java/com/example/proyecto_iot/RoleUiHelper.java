package com.example.proyecto_iot;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.FontRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.admin.BaseAdminActivity;

import java.util.Locale;

public final class RoleUiHelper {

    private RoleUiHelper() {
    }

    public static void applyRoleChrome(AppCompatActivity activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) {
            return;
        }

        View root = content.getChildAt(0);
        applySafeAreaInsets(activity, root);
        // Admin has its own neutral/navy visual language. Keep the existing
        // client/advisor normalization untouched so this role-specific polish
        // cannot recolor the rest of the product.
        normalizeTypography(activity, root, activity instanceof BaseAdminActivity);
    }

    private static void applySafeAreaInsets(Activity activity, View root) {
        View bottomNav = root.findViewById(R.id.bottomNav);

        final int rootLeft = root.getPaddingLeft();
        final int rootTop = root.getPaddingTop();
        final int rootRight = root.getPaddingRight();
        final int rootBottom = root.getPaddingBottom();

        final int navLeft = bottomNav != null ? bottomNav.getPaddingLeft() : 0;
        final int navTop = bottomNav != null ? bottomNav.getPaddingTop() : 0;
        final int navRight = bottomNav != null ? bottomNav.getPaddingRight() : 0;
        final int navBottom = bottomNav != null ? bottomNav.getPaddingBottom() : 0;
        final int navHeight = getInitialHeight(bottomNav);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    rootLeft + bars.left,
                    rootTop + bars.top,
                    rootRight + bars.right,
                    bottomNav == null ? rootBottom + bars.bottom : rootBottom
            );

            if (bottomNav != null) {
                bottomNav.setPadding(navLeft, navTop, navRight, navBottom + bars.bottom);
                if (navHeight > 0) {
                    ViewGroup.LayoutParams params = bottomNav.getLayoutParams();
                    params.height = navHeight + bars.bottom;
                    bottomNav.setLayoutParams(params);
                }
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private static int getInitialHeight(View view) {
        if (view == null || view.getLayoutParams() == null) {
            return 0;
        }
        int height = view.getLayoutParams().height;
        return height > 0 ? height : 0;
    }

    private static void normalizeTypography(Context context, View view, boolean adminUi) {
        if (view instanceof TextView) {
            applyTextRole(context, (TextView) view, adminUi);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                normalizeTypography(context, group.getChildAt(i), adminUi);
            }
        }
    }

    private static void applyTextRole(Context context, TextView textView, boolean adminUi) {
        float sp = textView.getTextSize() / context.getResources().getDisplayMetrics().scaledDensity;
        String text = textView.getText() == null ? "" : textView.getText().toString().trim();

        TextRole role = resolveRole(textView, text, sp);
        Typeface typeface = getTypeface(context, role.fontRes);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, role.textSizeSp);
        textView.setLineHeight(spToPx(context, role.lineHeightSp));
        textView.setLetterSpacing(role.letterSpacing);

        if (!isInsideBottomNav(textView)) {
            textView.setTextColor(resolveTextColor(context, textView, role, adminUi));
        }
    }

    private static TextRole resolveRole(TextView textView, String text, float currentSp) {
        boolean nav = isInsideBottomNav(textView);
        boolean buttonLike = textView instanceof Button || textView.isClickable() || isButtonId(textView);
        boolean allCaps = hasLetter(text) && text.equals(text.toUpperCase(Locale.ROOT));

        if (nav) {
            return TextRole.NAV;
        }
        if (buttonLike) {
            return TextRole.BUTTON;
        }
        if (currentSp >= 26f) {
            return TextRole.H1;
        }
        if (currentSp >= 18f) {
            return TextRole.H2;
        }
        if (allCaps || currentSp <= 11f) {
            return TextRole.H3;
        }
        if (currentSp <= 13f) {
            return TextRole.CAPTION;
        }
        return TextRole.PARAGRAPH;
    }

    private static boolean isButtonId(View view) {
        if (view.getId() == View.NO_ID) {
            return false;
        }
        try {
            String name = view.getResources().getResourceEntryName(view.getId()).toLowerCase(Locale.ROOT);
            return name.startsWith("btn") || name.contains("button");
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean isInsideBottomNav(View view) {
        View current = view;
        while (current != null) {
            if (current.getId() == R.id.bottomNav) {
                return true;
            }
            if (!(current.getParent() instanceof View)) {
                return false;
            }
            current = (View) current.getParent();
        }
        return false;
    }

    private static boolean hasLetter(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isLetter(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static int resolveTextColor(Context context, TextView textView, TextRole role, boolean adminUi) {
        int currentColor = textView.getCurrentTextColor();
        if (adminUi) {
            if (isLogoutId(textView) || isDanger(currentColor)) {
                return ContextCompat.getColor(context, R.color.app_danger_text);
            }
            if (isNearWhite(currentColor)) {
                return ContextCompat.getColor(context, R.color.admin_on_action);
            }
            if (role == TextRole.H1 || role == TextRole.H2) {
                return ContextCompat.getColor(context, R.color.admin_text_primary);
            }
            if (role == TextRole.H3 || role == TextRole.CAPTION) {
                return ContextCompat.getColor(context, R.color.admin_text_tertiary);
            }
            return ContextCompat.getColor(context, R.color.admin_text_secondary);
        }
        if (isLogoutId(textView)) {
            return ContextCompat.getColor(context, R.color.app_danger_text);
        }
        if (isNearWhite(currentColor)) {
            return Color.WHITE;
        }
        if (isDanger(currentColor)) {
            return ContextCompat.getColor(context, R.color.app_danger_text);
        }
        if (isGold(currentColor) || role == TextRole.H3) {
            return ContextCompat.getColor(context, R.color.app_accent_gold);
        }
        if (role == TextRole.CAPTION) {
            return ContextCompat.getColor(context, R.color.app_text_muted);
        }
        if (role == TextRole.PARAGRAPH) {
            return ContextCompat.getColor(context, R.color.app_text_secondary);
        }
        return ContextCompat.getColor(context, R.color.app_text_primary);
    }

    private static boolean isLogoutId(View view) {
        if (view.getId() == View.NO_ID) {
            return false;
        }
        try {
            String name = view.getResources().getResourceEntryName(view.getId()).toLowerCase(Locale.ROOT);
            return name.contains("cerrarsesion") || name.contains("logout");
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean isNearWhite(int color) {
        return Color.alpha(color) > 0
                && Color.red(color) > 230
                && Color.green(color) > 230
                && Color.blue(color) > 230;
    }

    private static boolean isDanger(int color) {
        return Color.red(color) > 145
                && Color.green(color) < 80
                && Color.blue(color) < 80;
    }

    private static boolean isGold(int color) {
        return Color.red(color) > 100
                && Color.green(color) > 70
                && Color.blue(color) < 60;
    }

    private static Typeface getTypeface(Context context, @FontRes int fontRes) {
        try {
            return ResourcesCompat.getFont(context, fontRes);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static int spToPx(Context context, int sp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                context.getResources().getDisplayMetrics()
        );
    }

    private enum TextRole {
        H1(R.font.manrope_extrabold, 30, 36, -0.025f),
        H2(R.font.manrope_bold, 20, 28, -0.01f),
        H3(R.font.manrope_bold, 11, 14, 0.08f),
        PARAGRAPH(R.font.inter_regular, 15, 24, 0f),
        CAPTION(R.font.manrope_medium, 12, 16, 0.01f),
        NAV(R.font.manrope_medium, 11, 14, 0.06f),
        BUTTON(R.font.manrope_bold, 13, 18, 0.01f);

        final int fontRes;
        final int textSizeSp;
        final int lineHeightSp;
        final float letterSpacing;

        TextRole(int fontRes, int textSizeSp, int lineHeightSp, float letterSpacing) {
            this.fontRes = fontRes;
            this.textSizeSp = textSizeSp;
            this.lineHeightSp = lineHeightSp;
            this.letterSpacing = letterSpacing;
        }
    }
}
