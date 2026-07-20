package com.example.proyecto_iot;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.proyecto_iot.data.AdvisorRegistrationPolicy;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AuthSessionManager {

    // Roles
    public static final String ROLE_CLIENTE = "cliente";
    public static final String ROLE_ASESOR = "asesor";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_SUPERADMIN = "superadmin";

    // Preferencias
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

    public interface AccessListener {
        void onAllowed(FirebaseUser user, String role);
        void onBlocked(String status);
        void onError(String errorMessage);
    }

    public static final class RegistrationRequest {
        public final String role;
        public final String requestedCompanyId;
        public final String requestedCompanyName;

        private RegistrationRequest(String role, String requestedCompanyId, String requestedCompanyName) {
            this.role = normalizeRequestedRole(role);
            this.requestedCompanyId = safeValue(requestedCompanyId);
            this.requestedCompanyName = safeValue(requestedCompanyName);
        }

        public static RegistrationRequest client() {
            return new RegistrationRequest(ROLE_CLIENTE, "", "");
        }

        public static RegistrationRequest advisor(String companyId, String companyName) {
            return new RegistrationRequest(ROLE_ASESOR, companyId, companyName);
        }

        private static String normalizeRequestedRole(String role) {
            return ROLE_ASESOR.equalsIgnoreCase(role) ? ROLE_ASESOR : ROLE_CLIENTE;
        }

        private static String safeValue(String value) {
            return value == null ? "" : value.trim();
        }
    }

    // ---------- REGISTRO CON CORREO ----------
    public void registerWithEmail(String email, String password,
                                  String nombres, String apellidos,
                                  String telefono, AuthListener listener) {
        registerWithEmail(email, password, nombres, apellidos, telefono,
                RegistrationRequest.client(), listener);
    }

    public void registerWithEmail(String email, String password,
                                  String nombres, String apellidos,
                                  String telefono, RegistrationRequest request,
                                  AuthListener listener) {
        RegistrationRequest safeRequest = request == null ? RegistrationRequest.client() : request;
        if (ROLE_ASESOR.equals(safeRequest.role) && safeRequest.requestedCompanyId.isEmpty()) {
            listener.onError("Selecciona la inmobiliaria a la que deseas postular.");
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserProfileToFirestore(user.getUid(), email, nombres, apellidos, telefono,
                                    safeRequest, listener);
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
        String webClientId = getWebClientId(activity);
        if (webClientId.isEmpty()) {
            listener.onError("Falta configurar el cliente web de Google para esta aplicacion.");
            return;
        }
        CredentialManager credentialManager = CredentialManager.create(activity);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
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
        try {
        Credential credential = result.getCredential();
        GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
        String idToken = googleIdTokenCredential.getIdToken();
        if (idToken == null || idToken.trim().isEmpty()) {
            listener.onError("Google no devolvio un token de inicio de sesion valido.");
            return;
        }
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
        } catch (Exception error) {
            Log.e("AuthManager", "Credencial de Google invalida", error);
            listener.onError("No se pudo procesar la credencial de Google. Intenta nuevamente.");
        }
    }

    private String getWebClientId(Activity activity) {
        try {
            int configuredId = activity.getResources().getIdentifier(
                    "google_web_client_id", "string", activity.getPackageName());
            if (configuredId != 0) {
                return activity.getString(configuredId).trim();
            }
            int generatedId = activity.getResources().getIdentifier(
                    "default_web_client_id", "string", activity.getPackageName());
            if (generatedId != 0) {
                return activity.getString(generatedId).trim();
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
                                            String telefono, RegistrationRequest request,
                                            AuthListener listener) {
        String role = request.role;
        boolean advisorApplication = ROLE_ASESOR.equals(role);

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", email);
        userData.put("nombres", nombres);
        userData.put("apellidos", apellidos);
        userData.put("telefono", telefono);
        userData.put("rol", role);
        userData.put("estado", AdvisorRegistrationPolicy.initialStatus(role));
        userData.put("createdAt", System.currentTimeMillis());
        if (advisorApplication) {
            userData.put("empresaSolicitadaId", request.requestedCompanyId);
            userData.put("empresaSolicitadaNombre", request.requestedCompanyName);
        }

        db.collection("usuarios").document(uid).set(userData)
                .addOnSuccessListener(aVoid -> {
                    String fullName = nombres + " " + apellidos;
                    saveUserSession(uid, fullName.trim(), email, telefono, role);
                    com.example.proyecto_iot.data.SystemLogger.logEvent(
                            "registro", "info", "Nuevo Usuario Registrado",
                            "Email: " + email, "Registro exitoso", advisorApplication
                                    ? "El usuario " + fullName.trim() + " enviÃ³ una solicitud para ser asesor."
                                    : "El usuario " + fullName.trim() + " ha creado una cuenta nueva."
                    );
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
                            String nombres = firstString(doc, "nombres");
                            String apellidos = firstString(doc, "apellidos");
                            String fullStoredName = firstString(doc, "nombre");
                            String email = firstString(doc, "email", "correo");
                            String telefono = firstString(doc, "telefono");
                            String rol = firstString(doc, "rol");

                            String fullName = (nombres != null ? nombres : "") +
                                    (apellidos != null ? " " + apellidos : "");
                            if (fullName.trim().isEmpty()) {
                                fullName = fullStoredName;
                            }

                            saveUserSession(uid, fullName.trim(), email, telefono, rol);
                            com.example.proyecto_iot.data.SystemLogger.logEvent(
                                    "sesion", "info", "Inicio de Sesión",
                                    "Usuario: " + fullName.trim(), "Login exitoso", "El usuario " + email + " inició sesión correctamente."
                            );
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
        String role = ROLE_CLIENTE;

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

    // ---------- UTILIDADES ----------

    /**
     * Revalida el perfil remoto antes de permitir una ruta protegida. Esto evita que una
     * sesiÃ³n local desactualizada conceda acceso a un asesor pendiente o rechazado.
     */
    public void resolveCurrentAccess(AccessListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            clearLocalSession();
            listener.onError("No hay una sesiÃ³n autenticada.");
            return;
        }
        db.collection("usuarios").document(user.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        clearLocalSession();
                        mAuth.signOut();
                        listener.onError("No se encontrÃ³ el perfil de la cuenta.");
                        return;
                    }
                    String role = normalizeRole(firstString(document, "rol"));
                    String status = firstString(document, "estado");
                    if (status.isEmpty()) status = "activo";
                    if (AdvisorRegistrationPolicy.blocksAdvisorAccess(role, status)) {
                        clearLocalSession();
                        mAuth.signOut();
                        listener.onBlocked(status);
                        return;
                    }
                    String names = firstString(document, "nombres");
                    String surnames = firstString(document, "apellidos");
                    String fullName = (names + " " + surnames).trim();
                    if (fullName.isEmpty()) fullName = firstString(document, "nombre");
                    saveUserSession(user.getUid(), fullName,
                            firstString(document, "email", "correo"),
                            firstString(document, "telefono"), role);
                    listener.onAllowed(user, role);
                })
                .addOnFailureListener(error -> listener.onError(
                        "No se pudo validar el estado de la cuenta: " + safeMessage(error)));
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
        clearLocalSession();
    }

    public void clearLocalSession() {
        sharedPreferences.edit()
                .putBoolean(KEY_LOGGED_IN, false)
                .remove(KEY_ROLE)
                .remove(KEY_UID)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_PHONE)
                .apply();
    }

    private String firstString(DocumentSnapshot document, String... keys) {
        for (String key : keys) {
            Object value = document.get(key);
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                return String.valueOf(value).trim();
            }
        }
        return "";
    }

    private String safeMessage(Exception error) {
        return error == null || error.getMessage() == null || error.getMessage().trim().isEmpty()
                ? "Error desconocido."
                : error.getMessage().trim();
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
        String normalized = role.trim().toLowerCase(java.util.Locale.ROOT);
        if ("user".equals(normalized)) return ROLE_CLIENTE;
        if (normalized.equals(ROLE_ASESOR) || normalized.equals(ROLE_ADMIN) || normalized.equals(ROLE_SUPERADMIN)) {
            return normalized;
        }
        return ROLE_CLIENTE;
    }
}
