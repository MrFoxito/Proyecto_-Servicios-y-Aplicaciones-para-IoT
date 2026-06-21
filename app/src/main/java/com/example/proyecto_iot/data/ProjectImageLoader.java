package com.example.proyecto_iot.data;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public final class ProjectImageLoader {
    private static final String DATA_PREFIX = "data:image/";

    private ProjectImageLoader() {
    }

    public static void load(ImageView imageView, String value, int fallbackRes) {
        if (value != null && value.startsWith(DATA_PREFIX)) {
            int separator = value.indexOf(',');
            if (separator > 0 && separator < value.length() - 1) {
                try {
                    byte[] bytes = Base64.decode(value.substring(separator + 1), Base64.DEFAULT);
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    return;
                } catch (Exception ignored) {
                    // Use the normal fallback below.
                }
            }
        }
        if (value != null && !value.trim().isEmpty()) {
            Glide.with(imageView).load(value).centerCrop().into(imageView);
        } else if (fallbackRes != 0) {
            imageView.setImageResource(fallbackRes);
        } else {
            imageView.setImageDrawable(null);
        }
    }
}
