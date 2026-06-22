package com.example.proyecto_iot;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AuthSessionManager {

    private Set<String> allowedDomainsCache = new HashSet<>();
    private boolean domainsLoaded = false;

    // Roles
    public static final String ROLE_CLIENTE = "cliente";
    public static final String ROLE_ASESOR = "asesor";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_SUPERADMIN = "superadmin";

    // Preferencias
    private static final String KEY_DOMAINS = "allowed_domains";
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_REGISTERED = "registered";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_ROLE = "role";
    private static final String KEY_UID = "uid";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";

    // Firebase
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final Executor executor = Executors.newSingleThreadExecutor();

    // Preferencias locales
    private final SharedPreferences sharedPreferences;

    // Singleton
    private static AuthSessionManager instance;

    private AuthSessionManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mAuth.setLanguageCode("es");
    }

    public static synchronized AuthSessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthSessionManager(context);
        }
        return instance;
    }

    // --- Interfaz de callback ---
    public interface AuthListener {
        void onSuccess(FirebaseUser user);
        void onError(String errorMessage);
    }

    // ---------- REGISTRO CON CORREO ----------
    public void registerWithEmail(String email, String password,
                                  String nombres, String apellidos,
                                  String telefono, AuthListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserProfileToFirestore(user.getUid(), email, nombres, apellidos, telefono, listener);
                        } else {
                            listener.onError("Error: usuario nulo después de registro.");
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error de registro";
                        Log.e("AuthManager", "registerWithEmail: " + errorMsg, task.getException());
                        listener.onError(errorMsg);
                    }
                });
    }

    // ---------- INICIO DE SESIÓN CON CORREO ----------
    public void loginWithEmail(String email, String password, AuthListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchAndSaveUserProfile(user.getUid(), listener);
                        } else {
                            listener.onError("Error: usuario nulo después de login.");
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error de inicio de sesión";
                        Log.e("AuthManager", "loginWithEmail: " + errorMsg, task.getException());
                        listener.onError(errorMsg);
                    }
                });
    }

    // ---------- GOOGLE SIGN-IN (moderno con CredentialManager) ----------
    public void startGoogleSignIn(Activity activity, AuthListener listener) {
        CredentialManager credentialManager = CredentialManager.create(activity);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getWebClientId(activity))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                null,
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleSignInResult(result, listener);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e("AuthManager", "Error en startGoogleSignIn: " + e.getMessage(), e);
                        listener.onError("Error al iniciar sesión con Google: " + e.getMessage());
                    }
                });
    }

    private void handleGoogleSignInResult(GetCredentialResponse result, AuthListener listener) {
        Credential credential = result.getCredential();
        GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
        String idToken = googleIdTokenCredential.getIdToken();
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkOrCreateUserProfile(user, listener);
                        } else {
                            listener.onError("Usuario nulo después de auth con Google.");
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error al autenticar con Google";
                        Log.e("AuthManager", "handleGoogleSignInResult: " + errorMsg, task.getException());
                        listener.onError(errorMsg);
                    }
                });
    }

    private String getWebClientId(Activity activity) {
        try {
            int id = activity.getResources().getIdentifier("default_web_client_id", "string", activity.getPackageName());
            if (id != 0) {
                return activity.getString(id);
            } else {
                Log.e("AuthManager", "No se encontró el recurso default_web_client_id");
                return "";
            }
        } catch (Exception e) {
            Log.e("AuthManager", "Error al obtener el Web Client ID: " + e.getMessage(), e);
            return "";
        }
    }

    // ---------- MÉTODOS DE PERFIL EN FIRESTORE ----------

    private void saveUserProfileToFirestore(String uid, String email,
                                            String nombres, String apellidos,
                                            String telefono, AuthListener listener) {
        String role = getRoleFromEmail(email);

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", email);
        userData.put("nombres", nombres);
        userData.put("apellidos", apellidos);
        userData.put("telefono", telefono);
        userData.put("rol", role);
        userData.put("estado", "activo");

        db.collection("usuarios").document(uid).set(userData)
                .addOnSuccessListener(aVoid -> {
                    String fullName = nombres + " " + apellidos;
                    saveUserSession(uid, fullName.trim(), email, telefono, role);
                    listener.onSuccess(mAuth.getCurrentUser());
                })
                .addOnFailureListener(e -> {
                    listener.onError("Error al guardar perfil: " + e.getMessage());
                });
    }

    private void fetchAndSaveUserProfile(String uid, AuthListener listener) {
        db.collection("usuarios").document(uid).get()
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            String nombres = doc.getString("nombres");
                            String apellidos = doc.getString("apellidos");
                            String email = doc.getString("email");
                            String telefono = doc.getString("telefono");
                            String rol = doc.getString("rol");

                            String fullName = (nombres != null ? nombres : "") +
                                    (apellidos != null ? " " + apellidos : "");

                            saveUserSession(uid, fullName.trim(), email, telefono, rol);
                            listener.onSuccess(mAuth.getCurrentUser());
                        } else {
                            // Si no existe el documento, crearlo con datos de FirebaseUser
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                createMissingUserProfile(user, listener);
                            } else {
                                listener.onError("No se encontró perfil de usuario.");
                            }
                        }
                    } else {
                        listener.onError("Error al obtener perfil: " +
                                (task.getException() != null ? task.getException().getMessage() : ""));
                    }
                });
    }

    private void createMissingUserProfile(FirebaseUser user, AuthListener listener) {
        String uid = user.getUid();
        String email = user.getEmail();
        String displayName = user.getDisplayName();
        String[] nameParts = splitFullName(displayName);
        String nombres = nameParts[0];
        String apellidos = nameParts.length > 1 ? nameParts[1] : "";
        String telefono = user.getPhoneNumber() != null ? user.getPhoneNumber() : "";
        String role = getRoleFromEmail(email);

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", email);
        userData.put("nombres", nombres);
        userData.put("apellidos", apellidos);
        userData.put("telefono", telefono);
        userData.put("rol", role);
        userData.put("estado", "activo");

        db.collection("usuarios").document(uid).set(userData)
                .addOnSuccessListener(aVoid -> {
                    saveUserSession(uid, displayName, email, telefono, role);
                    listener.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    listener.onError("Error al crear perfil: " + e.getMessage());
                });
    }

    private void checkOrCreateUserProfile(FirebaseUser user, AuthListener listener) {
        String uid = user.getUid();
        db.collection("usuarios").document(uid).get()
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        fetchAndSaveUserProfile(uid, listener);
                    } else {
                        createMissingUserProfile(user, listener);
                    }
                });
    }

    public Task<Void> sendPasswordResetEmail(String email) {
        return mAuth.sendPasswordResetEmail(email);
    }

    private void loadAllowedDomainsFromFirestore() {
        db.collection("Inmobiliarias")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> domains = new HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String dominio = doc.getString("dominio_correo");
                        if (dominio != null && !dominio.isEmpty()) {
                            domains.add(dominio.toLowerCase());
                        }
                    }
                    allowedDomainsCache = domains;
                    domainsLoaded = true;
                    saveDomainsToCache(domains);
                })
                .addOnFailureListener(e -> {
                    loadDomainsFromSharedPreferences();
                    Log.e("AuthManager", "Error cargando dominios de Firestore", e);
                });
    }

    private void saveDomainsToCache(Set<String> domains) {
        String joined = TextUtils.join(",", domains);
        sharedPreferences.edit().putString(KEY_DOMAINS, joined).apply();
    }

    private void loadDomainsFromSharedPreferences() {
        String joined = sharedPreferences.getString(KEY_DOMAINS, "");
        if (!joined.isEmpty()) {
            String[] parts = joined.split(",");
            allowedDomainsCache = new HashSet<>(Arrays.asList(parts));
            domainsLoaded = true;
        }
    }

    // ---------- UTILIDADES ----------

    private String getRoleFromEmail(String email) {
        if (email == null || !email.contains("@")) return ROLE_CLIENTE;
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();

        if (!domainsLoaded) {
            loadDomainsFromSharedPreferences();
        }

        return allowedDomainsCache.contains(domain) ? ROLE_ASESOR : ROLE_CLIENTE;
    }

    private String[] splitFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        String[] parts = fullName.trim().split(" ", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        } else {
            return parts;
        }
    }

    // ---------- GESTIÓN DE SESIÓN LOCAL (SharedPreferences) ----------

    public void saveUserSession(String uid, String name, String email, String phone, String role) {
        String normalizedRole = normalizeRole(role);
        sharedPreferences.edit()
                .putBoolean(KEY_REGISTERED, true)
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_UID, uid)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_PHONE, phone)
                .putString(KEY_ROLE, normalizedRole)
                .apply();
    }

    public void markLoggedIn() {
        sharedPreferences.edit().putBoolean(KEY_LOGGED_IN, true).apply();
    }

    public void updateRole(String role) {
        String normalized = normalizeRole(role);
        sharedPreferences.edit().putString(KEY_ROLE, normalized).apply();
    }

    public void updateUserData(String name, String email, String phone) {
        sharedPreferences.edit()
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_PHONE, phone)
                .apply();
    }

    public void logout() {
        mAuth.signOut();
        sharedPreferences.edit()
                .putBoolean(KEY_LOGGED_IN, false)
                .remove(KEY_ROLE)
                .remove(KEY_UID)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_PHONE)
                .apply();
    }

    // Getters de sesión local
    public boolean isRegistered() { return sharedPreferences.getBoolean(KEY_REGISTERED, false); }
    public boolean isLoggedIn() { return sharedPreferences.getBoolean(KEY_LOGGED_IN, false); }
    public String getRole() { return sharedPreferences.getString(KEY_ROLE, ROLE_CLIENTE); }
    public String getUid() { return sharedPreferences.getString(KEY_UID, ""); }
    public String getUserName() { return sharedPreferences.getString(KEY_USER_NAME, ""); }
    public String getUserEmail() { return sharedPreferences.getString(KEY_USER_EMAIL, ""); }
    public String getUserPhone() { return sharedPreferences.getString(KEY_USER_PHONE, ""); }

    public FirebaseUser getCurrentFirebaseUser() { return mAuth.getCurrentUser(); }

    private String normalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) return ROLE_CLIENTE;
        if ("user".equalsIgnoreCase(role)) return ROLE_CLIENTE;
        if (role.equals(ROLE_ASESOR) || role.equals(ROLE_ADMIN) || role.equals(ROLE_SUPERADMIN)) {
            return role;
        }
        return ROLE_CLIENTE;
    }
}