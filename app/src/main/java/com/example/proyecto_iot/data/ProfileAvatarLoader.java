package com.example.proyecto_iot.data;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

/** Presentation-only loader for profile photos stored in usuarios.avatarUrl. */
public final class ProfileAvatarLoader {
    private ProfileAvatarLoader() { }

    public static void load(ImageView imageView, String avatarUrl, String displayName,
                            @DrawableRes int fallbackRes) {
        if (imageView == null) return;
        imageView.setContentDescription(TextUtils.isEmpty(displayName)
                ? "Foto de perfil" : "Foto de perfil de " + displayName.trim());
        Glide.with(imageView).clear(imageView);

        if (!ProfileAvatarPolicy.isValidRemoteUrl(avatarUrl)) {
            imageView.setImageResource(fallbackRes);
            return;
        }

        Context context = imageView.getContext();
        Glide.with(context)
                .load(avatarUrl.trim())
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .placeholder(fallbackRes)
                .error(fallbackRes)
                .into(imageView);
    }

}
