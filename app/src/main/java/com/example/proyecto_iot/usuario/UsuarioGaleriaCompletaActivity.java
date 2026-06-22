package com.example.proyecto_iot.usuario;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.proyecto_iot.R;

import java.util.ArrayList;

public class UsuarioGaleriaCompletaActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URLS = "extra_image_urls";
    public static final String EXTRA_INITIAL_POSITION = "extra_initial_position";

    private ViewPager2 vpFullScreenGallery;
    private TextView tvFullScreenCounter;
    private UsuarioProjectGalleryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_galeria_completa);
        applyInsets();

        ArrayList<String> urls = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
        int initialPosition = getIntent().getIntExtra(EXTRA_INITIAL_POSITION, 0);

        vpFullScreenGallery = findViewById(R.id.vpFullScreenGallery);
        tvFullScreenCounter = findViewById(R.id.tvFullScreenCounter);
        ImageView btnClose = findViewById(R.id.btnCloseGallery);

        if (urls == null || urls.isEmpty()) {
            finish();
            return;
        }

        adapter = new UsuarioProjectGalleryAdapter(null); // No click listener needed in full screen
        adapter.setItems(urls);
        vpFullScreenGallery.setAdapter(adapter);
        
        vpFullScreenGallery.setCurrentItem(initialPosition, false);
        updateCounter(initialPosition, urls.size());

        vpFullScreenGallery.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateCounter(position, adapter.getItemCount());
            }
        });

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }
    }

    private void updateCounter(int position, int total) {
        if (tvFullScreenCounter != null) {
            tvFullScreenCounter.setText((position + 1) + " / " + total);
        }
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}
