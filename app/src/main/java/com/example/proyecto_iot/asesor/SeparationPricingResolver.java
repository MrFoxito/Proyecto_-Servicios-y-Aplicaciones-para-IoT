package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.entity.Separacion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/** Read-only compatibility resolver for pricing snapshots missing in historic separations. */
public final class SeparationPricingResolver {
    public interface Callback { void onResolved(); }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void resolve(Separacion separation, Callback callback) {
        if (separation == null) {
            callback.onResolved();
            return;
        }
        String projectId = SeparationPricingPolicy.value(separation.getProjectId());
        if (projectId.isEmpty()) {
            callback.onResolved();
            return;
        }
        firestore.collection("proyectos").document(projectId).get()
                .addOnSuccessListener(project -> {
                    if (!project.exists()) {
                        callback.onResolved();
                        return;
                    }
                    applyProjectFallback(separation, project);
                    resolveTypology(separation, project.getId(), callback);
                })
                .addOnFailureListener(error -> callback.onResolved());
    }

    private void resolveTypology(Separacion separation, String projectId, Callback callback) {
        String typologyId = SeparationPricingPolicy.value(separation.getTipologiaId());
        if (!typologyId.isEmpty()) {
            firestore.collection("proyectos_tipologias").document(typologyId).get()
                    .addOnSuccessListener(typology -> {
                        if (typology.exists() && projectId.equals(typology.getString("projectId"))) {
                            applyTypology(separation, typology);
                            callback.onResolved();
                            return;
                        }
                        resolveOnlyTypology(separation, projectId, callback);
                    })
                    .addOnFailureListener(error -> resolveOnlyTypology(separation, projectId, callback));
            return;
        }
        resolveOnlyTypology(separation, projectId, callback);
    }

    private void resolveOnlyTypology(Separacion separation, String projectId, Callback callback) {
        firestore.collection("proyectos_tipologias").whereEqualTo("projectId", projectId).limit(2).get()
                .addOnSuccessListener(types -> {
                    List<DocumentSnapshot> documents = types.getDocuments();
                    if (documents.size() == 1) {
                        separation.setTipologiaId(documents.get(0).getId());
                        applyTypology(separation, documents.get(0));
                    }
                    callback.onResolved();
                })
                .addOnFailureListener(error -> callback.onResolved());
    }

    private void applyProjectFallback(Separacion separation, DocumentSnapshot project) {
        if (SeparationPricingPolicy.hasAmount(separation.getPrecioTotal())) return;
        String value = SeparationPricingPolicy.value(project.getString("precioDesde"));
        double amount = SeparationPricingPolicy.number(value);
        if (SeparationPricingPolicy.hasAmount(amount)) {
            separation.setPrecioTotal(amount);
            separation.setPrecioTotalTexto(value);
        }
    }

    private void applyTypology(Separacion separation, DocumentSnapshot typology) {
        double total = SeparationPricingPolicy.number(first(typology, "totalAmount", "montoTotal"));
        double separationAmount = SeparationPricingPolicy.number(first(typology, "separationAmount", "montoSeparacion"));
        if (!SeparationPricingPolicy.hasAmount(separation.getPrecioTotal())
                && SeparationPricingPolicy.hasAmount(total)) {
            separation.setPrecioTotal(total);
            separation.setPrecioTotalTexto(firstString(typology, "totalAmountLabel", "montoTotalLabel"));
        }
        if (!SeparationPricingPolicy.hasAmount(separation.getMontoSeparacion())
                && SeparationPricingPolicy.hasAmount(separationAmount)) {
            separation.setMontoSeparacion(separationAmount);
            separation.setMontoSeparacionTexto(firstString(typology, "separationAmountLabel", "montoSeparacionLabel"));
        }
    }

    private Object first(DocumentSnapshot document, String first, String second) {
        Object value = document.get(first);
        return value != null ? value : document.get(second);
    }

    private String firstString(DocumentSnapshot document, String first, String second) {
        String value = document.getString(first);
        return SeparationPricingPolicy.value(value).isEmpty() ? document.getString(second) : value;
    }
}
