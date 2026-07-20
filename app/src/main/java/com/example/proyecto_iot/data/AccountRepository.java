package com.example.proyecto_iot.data;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AccountRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface Callback {
        void onSuccess(AccountContext account);
        void onError(String message);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface CompanyCallback {
        void onSuccess(String empresaId, String address, String email, String phone,
                       String primaryImageUrl, String secondaryImageUrl);
        default void onSuccess(String empresaId, String companyName, String address, String email, String phone,
                               String primaryImageUrl, String secondaryImageUrl) {
            onSuccess(empresaId, address, email, phone, primaryImageUrl, secondaryImageUrl);
        }
        void onError(String message);
    }

    public interface RepairCallback {
        void onSuccess(boolean profileNeedsCompletion);
        void onError(String message);
    }

    public void load(String uid, Callback callback) {
        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("No hay una cuenta autenticada.");
            return;
        }
        firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener(user -> {
                    if (!user.exists()) {
                        callback.onError("El perfil no existe en usuarios/" + uid);
                        return;
                    }
                    String empresaId = first(user, "empresaId", "inmobiliariaId");
                    if (empresaId.isEmpty()) {
                        deliverAccount(uid, user, null, callback);
                        return;
                    }
                    firestore.collection("empresas").document(empresaId).get()
                            .addOnSuccessListener(company -> deliverAccount(uid, user, company, callback))
                            .addOnFailureListener(error -> deliverAccount(uid, user, null, callback));
                })
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    public void updateProfile(
            String uid,
            String fullName,
            String email,
            String phone,
            String dni,
            String birthDate,
            SaveCallback callback
    ) {
        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("No hay una cuenta autenticada.");
            return;
        }
        String[] names = splitName(fullName);
        Map<String, Object> values = new HashMap<>();
        values.put("nombre", fullName == null ? "" : fullName.trim());
        values.put("nombres", names[0]);
        values.put("apellidos", names[1]);
        values.put("email", safe(email));
        values.put("correo", safe(email));
        values.put("telefono", safe(phone));
        values.put("dni", safe(dni));
        values.put("fechaNacimiento", safe(birthDate));
        values.put("profileNeedsCompletion", safe(fullName).isEmpty()
                || safe(email).isEmpty()
                || safe(phone).isEmpty());
        values.put("updatedAt", System.currentTimeMillis());
        firestore.collection("usuarios").document(uid).set(values, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    public void repairAdminOnLogin(String uid, String authEmail, RepairCallback callback) {
        firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener(user -> {
                    if (!user.exists() || !"admin".equalsIgnoreCase(first(user, "rol"))) {
                        callback.onError("La cuenta autenticada no tiene un perfil administrador válido.");
                        return;
                    }
                    String empresaId = first(user, "empresaId", "inmobiliariaId");
                    if (!empresaId.isEmpty()) {
                        repairAdminDocument(uid, authEmail, user, empresaId, callback);
                        return;
                    }
                    String email = safe(authEmail).isEmpty() ? first(user, "email", "correo") : safe(authEmail);
                    firestore.collection("empresas")
                            .whereEqualTo("adminEmail", email)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(companies -> {
                                String resolved = companies.isEmpty() ? "" : companies.getDocuments().get(0).getId();
                                repairAdminDocument(uid, email, user, resolved, callback);
                            })
                            .addOnFailureListener(error ->
                                    repairAdminDocument(uid, email, user, "", callback));
                })
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    private void repairAdminDocument(
            String uid,
            String authEmail,
            DocumentSnapshot user,
            String empresaId,
            RepairCallback callback
    ) {
        if (empresaId.isEmpty()) {
            persistAdminRepair(uid, authEmail, user, empresaId, null, "", callback);
            return;
        }
        firestore.collection("empresas").document(empresaId).get()
                .addOnSuccessListener(company -> resolveInvitationName(user, invitationName ->
                        persistAdminRepair(
                                uid,
                                authEmail,
                                user,
                                empresaId,
                                company,
                                CompanyNamePolicy.resolve(
                                        first(company, "nombre"),
                                        first(company, "empresaNombre", "inmobiliariaNombre"),
                                        first(user, "empresaNombre"),
                                        first(user, "inmobiliariaNombre"),
                                        invitationName),
                                callback)))
                .addOnFailureListener(error -> resolveInvitationName(user, invitationName ->
                        persistAdminRepair(
                                uid,
                                authEmail,
                                user,
                                empresaId,
                                null,
                                CompanyNamePolicy.resolve(
                                        "", "",
                                        first(user, "empresaNombre"),
                                        first(user, "inmobiliariaNombre"),
                                        invitationName),
                                callback)));
    }

    private void persistAdminRepair(
            String uid,
            String authEmail,
            DocumentSnapshot user,
            String empresaId,
            DocumentSnapshot company,
            String resolvedCompanyName,
            RepairCallback callback
    ) {
        String fullName = first(user, "nombre");
        String nombres = first(user, "nombres");
        String apellidos = first(user, "apellidos");
        if ((nombres.isEmpty() || apellidos.isEmpty()) && !fullName.isEmpty()) {
            String[] split = splitName(fullName);
            if (nombres.isEmpty()) nombres = split[0];
            if (apellidos.isEmpty()) apellidos = split[1];
        }
        String email = safe(authEmail).isEmpty() ? first(user, "email", "correo") : safe(authEmail);
        String phone = first(user, "telefono");
        boolean needsCompletion = nombres.isEmpty() || apellidos.isEmpty()
                || email.isEmpty() || phone.isEmpty() || empresaId.isEmpty();

        Map<String, Object> values = new HashMap<>();
        values.put("uid", uid);
        values.put("id", uid);
        values.put("nombre", (nombres + " " + apellidos).trim());
        values.put("nombres", nombres);
        values.put("apellidos", apellidos);
        values.put("email", email);
        values.put("correo", email);
        values.put("rol", "admin");
        values.put("estado", "activo");
        values.put("profileNeedsCompletion", needsCompletion);
        if (!empresaId.isEmpty()) {
            values.put("empresaId", empresaId);
            values.put("inmobiliariaId", empresaId);
        }
        if (!resolvedCompanyName.isEmpty()) {
            if (first(user, "empresaNombre").isEmpty()) {
                values.put("empresaNombre", resolvedCompanyName);
            }
            if (first(user, "inmobiliariaNombre").isEmpty()) {
                values.put("inmobiliariaNombre", resolvedCompanyName);
            }
        }
        values.put("updatedAt", System.currentTimeMillis());
        com.google.firebase.firestore.WriteBatch batch = firestore.batch();
        batch.set(firestore.collection("usuarios").document(uid), values, SetOptions.merge());
        if (company != null
                && CompanyNamePolicy.shouldBackfill(
                CompanyNamePolicy.resolve(
                        first(company, "nombre"),
                        first(company, "empresaNombre", "inmobiliariaNombre"),
                        "", "", ""),
                resolvedCompanyName)) {
            Map<String, Object> companyValues = new HashMap<>();
            companyValues.put("nombre", resolvedCompanyName);
            companyValues.put("updatedAt", System.currentTimeMillis());
            batch.set(company.getReference(), companyValues, SetOptions.merge());
        }
        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(needsCompletion))
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    public void loadCompany(String uid, CompanyCallback callback) {
        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("No hay una cuenta autenticada.");
            return;
        }
        firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener(user -> {
                    String empresaId = first(user, "empresaId", "inmobiliariaId");
                    if (empresaId.isEmpty()) {
                        callback.onError("El administrador no está vinculado a una empresa.");
                        return;
                    }
                    firestore.collection("empresas").document(empresaId).get()
                            .addOnSuccessListener(company -> deliverCompany(user, empresaId, company, callback))
                            .addOnFailureListener(error -> deliverCompany(user, empresaId, null, callback));
                })
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    public void updateCompany(String uid, String address, String email, String phone, SaveCallback callback) {
        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("No hay una cuenta autenticada.");
            return;
        }
        firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener(user -> {
                    String empresaId = first(user, "empresaId", "inmobiliariaId");
                    if (empresaId.isEmpty()) {
                        callback.onError("El administrador no está vinculado a una empresa.");
                        return;
                    }
                    Map<String, Object> values = new HashMap<>();
                    values.put("direccion", safe(address));
                    values.put("correo", safe(email));
                    values.put("email", safe(email));
                    values.put("telefono", safe(phone));
                    values.put("updatedAt", System.currentTimeMillis());
                    firestore.collection("empresas").document(empresaId).set(values, SetOptions.merge())
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(error -> callback.onError(message(error)));
                })
                .addOnFailureListener(error -> callback.onError(message(error)));
    }

    private void deliverAccount(String uid, DocumentSnapshot user, DocumentSnapshot company, Callback callback) {
        String resolvedName = companyNameWithoutInvitation(user, company);
        if (!resolvedName.isEmpty()) {
            callback.onSuccess(toAccount(uid, user, company, resolvedName));
            return;
        }
        resolveInvitationName(user, invitationName ->
                callback.onSuccess(toAccount(uid, user, company,
                        companyNameWithoutInvitation(user, company, invitationName))));
    }

    private void deliverCompany(
            DocumentSnapshot user,
            String empresaId,
            DocumentSnapshot company,
            CompanyCallback callback
    ) {
        String resolvedName = companyNameWithoutInvitation(user, company);
        if (!resolvedName.isEmpty()) {
            callback.onSuccess(empresaId, resolvedName,
                    first(company, "direccion"),
                    first(company, "correo", "email", "adminEmail"),
                    first(company, "telefono"),
                    first(company, "companyImageUrl", "fotoUrl"),
                    first(company, "companySecondaryImageUrl"));
            return;
        }
        resolveInvitationName(user, invitationName -> callback.onSuccess(
                empresaId,
                companyNameWithoutInvitation(user, company, invitationName),
                first(company, "direccion"),
                first(company, "correo", "email", "adminEmail"),
                first(company, "telefono"),
                first(company, "companyImageUrl", "fotoUrl"),
                first(company, "companySecondaryImageUrl")
        ));
    }

    private void resolveInvitationName(DocumentSnapshot user, NameCallback callback) {
        String invitationId = first(user, "invitationId");
        if (invitationId.isEmpty()) {
            callback.onResolved("");
            return;
        }
        firestore.collection("admin_invitations").document(invitationId).get()
                .addOnSuccessListener(invitation -> callback.onResolved(
                        first(invitation, "empresaNombre", "inmobiliariaNombre")))
                .addOnFailureListener(error -> callback.onResolved(""));
    }

    private String companyNameWithoutInvitation(DocumentSnapshot user, DocumentSnapshot company) {
        return companyNameWithoutInvitation(user, company, "");
    }

    private String companyNameWithoutInvitation(
            DocumentSnapshot user,
            DocumentSnapshot company,
            String invitationName
    ) {
        return CompanyNamePolicy.resolve(
                first(company, "nombre"),
                first(company, "empresaNombre", "inmobiliariaNombre"),
                first(user, "empresaNombre"),
                first(user, "inmobiliariaNombre"),
                invitationName);
    }

    private AccountContext toAccount(
            String uid,
            DocumentSnapshot user,
            DocumentSnapshot company,
            String resolvedCompanyName
    ) {
        String fullName = first(user, "nombre");
        String nombres = first(user, "nombres");
        String apellidos = first(user, "apellidos");
        if (nombres.isEmpty() && !fullName.isEmpty()) {
            String[] parts = splitName(fullName);
            nombres = parts[0];
            apellidos = parts[1];
        }
        return new AccountContext(
                uid,
                nombres,
                apellidos,
                first(user, "email", "correo"),
                first(user, "telefono"),
                first(user, "rol"),
                first(user, "estado"),
                first(user, "empresaId", "inmobiliariaId"),
                resolvedCompanyName,
                first(user, "avatarUrl")
        );
    }

    private interface NameCallback {
        void onResolved(String name);
    }

    private String first(DocumentSnapshot document, String... keys) {
        if (document == null) return "";
        for (String key : keys) {
            Object value = document.get(key);
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                return String.valueOf(value).trim();
            }
        }
        return "";
    }

    private String[] splitName(String fullName) {
        String value = safe(fullName);
        if (value.isEmpty()) return new String[]{"", ""};
        String[] parts = value.split("\\s+", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String message(Exception error) {
        return error.getMessage() == null ? "Error al cargar la cuenta." : error.getMessage();
    }
}
