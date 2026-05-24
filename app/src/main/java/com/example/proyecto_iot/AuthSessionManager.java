package com.example.proyecto_iot;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthSessionManager {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ASESOR = "asesor";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_SUPERADMIN = "superadmin";

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_REGISTERED = "registered";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_ROLE = "role";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";

    private final SharedPreferences sharedPreferences;

    public AuthSessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isRegistered() {
        return sharedPreferences.getBoolean(KEY_REGISTERED, false);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public void markRegisteredAndLoggedIn() {
        sharedPreferences.edit()
                .putBoolean(KEY_REGISTERED, true)
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_ROLE, ROLE_USER)
                .apply();
    }

    public void markRegisteredAndLoggedIn(String userId, String name, String email, String phone) {
        sharedPreferences.edit()
                .putBoolean(KEY_REGISTERED, true)
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_ROLE, ROLE_USER)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_PHONE, phone)
                .apply();
    }

    public void markLoggedIn() {
        sharedPreferences.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .apply();
    }

    public void markLoggedIn(String role) {
        sharedPreferences.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, ROLE_SUPERADMIN);
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, "");
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public String getUserPhone() {
        return sharedPreferences.getString(KEY_USER_PHONE, "");
    }

    public void updateUserProfile(String name, String email, String phone) {
        sharedPreferences.edit()
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_PHONE, phone)
                .apply();
    }

    public void logout() {
        sharedPreferences.edit()
                .putBoolean(KEY_LOGGED_IN, false)
                .remove(KEY_ROLE)
                .remove(KEY_USER_ID)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_PHONE)
                .apply();
    }
}
