package com.example.proyecto_iot.asesor;

import androidx.annotation.Nullable;

import com.example.proyecto_iot.entity.Separacion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reads existing project media only to render separation cards. It performs no writes. */
public final class SeparationProjectImageResolver {
    public interface Callback { void onResolved(); }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final Map<String, String> cache = new HashMap<>();

    public void resolve(List<Separacion> separaciones, Callback callback) {
        if (separaciones == null || separaciones.isEmpty()) {
            callback.onResolved();
            return;
        }
        Map<String, List<Separacion>> byProject = new HashMap<>();
        for (Separacion separation : separaciones) {
            String projectId = value(separation == null ? null : separation.getProjectId());
            if (!projectId.isEmpty()) byProject.computeIfAbsent(projectId, ignored -> new ArrayList<>()).add(separation);
        }
        if (byProject.isEmpty()) {
            callback.onResolved();
            return;
        }
        int[] pending = {byProject.size()};
        for (Map.Entry<String, List<Separacion>> entry : byProject.entrySet()) {
            String projectId = entry.getKey();
            String cached = cache.get(projectId);
            if (cached != null) {
                apply(entry.getValue(), cached);
                if (--pending[0] == 0) callback.onResolved();
                continue;
            }
            resolveProject(projectId, url -> {
                cache.put(projectId, url);
                apply(entry.getValue(), url);
                if (--pending[0] == 0) callback.onResolved();
            });
        }
    }

    public void resolve(Separacion separation, Callback callback) {
        resolve(Collections.singletonList(separation), callback);
    }

    private void resolveProject(String reference, ImageCallback callback) {
        firestore.collection("proyectos").document(reference).get()
                .addOnSuccessListener(project -> {
                    if (project.exists()) {
                        resolveProjectDocument(project, callback);
                    } else {
                        resolveLegacyReference(reference, callback);
                    }
                })
                .addOnFailureListener(error -> callback.onImage(""));
    }

    private void resolveLegacyReference(String reference, ImageCallback callback) {
        firestore.collection("proyectos").whereEqualTo("propertyId", reference).limit(2).get()
                .addOnSuccessListener(byProperty -> {
                    if (byProperty.size() == 1) {
                        resolveProjectDocument(byProperty.getDocuments().get(0), callback);
                    } else if (byProperty.size() > 1) {
                        callback.onImage("");
                    } else {
                        firestore.collection("proyectos").whereEqualTo("projectId", reference).limit(2).get()
                                .addOnSuccessListener(byProject -> {
                                    if (byProject.size() == 1) resolveProjectDocument(byProject.getDocuments().get(0), callback);
                                    else callback.onImage("");
                                })
                                .addOnFailureListener(error -> callback.onImage(""));
                    }
                })
                .addOnFailureListener(error -> callback.onImage(""));
    }

    private void resolveProjectDocument(DocumentSnapshot project, ImageCallback callback) {
        String primary = SeparationProjectImagePolicy.select(project.getString("primaryImageUrl"),
                project.getString("imageUrl"), project.getString("imagenUrl"),
                project.getString("propertyImageUrl"), "");
        if (!primary.isEmpty()) {
            callback.onImage(primary);
            return;
        }
        firestore.collection("proyectos_imagenes").whereEqualTo("projectId", project.getId()).get()
                .addOnSuccessListener(images -> {
                    List<DocumentSnapshot> sorted = new ArrayList<>(images.getDocuments());
                    Collections.sort(sorted, (left, right) -> Integer.compare(position(left), position(right)));
                    for (DocumentSnapshot image : sorted) {
                        String url = SeparationProjectImagePolicy.select("", "", "", "", image.getString("imageUrl"));
                        if (!url.isEmpty()) {
                            callback.onImage(url);
                            return;
                        }
                    }
                    callback.onImage("");
                })
                .addOnFailureListener(error -> callback.onImage(""));
    }

    private int position(DocumentSnapshot document) {
        Object value = document.get("position");
        return value instanceof Number ? ((Number) value).intValue() : Integer.MAX_VALUE;
    }

    private void apply(List<Separacion> separaciones, String url) {
        for (Separacion separation : separaciones) if (separation != null) separation.setProjectImageUrl(url);
    }

    private interface ImageCallback { void onImage(String url); }

    private String value(@Nullable String value) { return value == null ? "" : value.trim(); }
}
