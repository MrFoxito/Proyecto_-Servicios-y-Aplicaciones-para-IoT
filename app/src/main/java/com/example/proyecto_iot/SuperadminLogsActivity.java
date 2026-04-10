package com.example.proyecto_iot;

import android.os.Bundle;

public class SuperadminLogsActivity extends BaseSuperadminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_logs);
        setupCommonNavigation();
    }
}

