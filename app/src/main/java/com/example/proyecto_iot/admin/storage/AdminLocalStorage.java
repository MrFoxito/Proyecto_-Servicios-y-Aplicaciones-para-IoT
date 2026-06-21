package com.example.proyecto_iot.admin.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.proyecto_iot.admin.model.AdminAssignmentRecord;
import com.example.proyecto_iot.admin.model.AdminAssignableProjectItem;
import com.example.proyecto_iot.admin.model.AdminEditedProjectRecord;
import com.example.proyecto_iot.admin.model.AdminProjectDraft;
import com.example.proyecto_iot.admin.model.AdminProjectFormAmenityItem;
import com.example.proyecto_iot.admin.model.AdminProjectFormTypologyItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdminLocalStorage {
    private static final String PREFS_NAME = "admin_local_storage";
    private static final String KEY_ASSIGNMENT_HISTORY = "assignment_history";
    private static final String KEY_FILTER_PREFIX = "last_filter_";
    private static final String KEY_CREATE_PROJECT_DRAFT = "create_project_draft";
    private static final String KEY_EDIT_PROJECT_DRAFT = "edit_project_draft";
    private static final String KEY_EDIT_PROJECT_DRAFT_ID = "edit_project_draft_id";
    private static final String KEY_EDIT_PROJECT_DRAFT_IMAGES = "edit_project_draft_images";
    private static final String KEY_EDITED_PROJECT_HISTORY = "edited_project_history";
    private static final String KEY_DISMISSED_NOTIFICATIONS = "dismissed_notifications";
    private static final String KEY_COMPANY_PROFILE = "company_profile";
    private static final String KEY_COMPANY_IMAGE_PRIMARY = "company_image_primary";
    private static final String KEY_COMPANY_IMAGE_SECONDARY = "company_image_secondary";
    private static final String KEY_ADMIN_PROFILE_AVATAR = "admin_profile_avatar";
    private static final int MAX_ASSIGNMENTS = 20;
    private static final int MAX_EDITED_PROJECTS = 10;

    private final SharedPreferences sharedPreferences;

    public AdminLocalStorage(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public AdminAssignmentRecord saveProjectAssignment(AdminAssignableProjectItem project, String advisorName) {
        AdminAssignmentRecord record = new AdminAssignmentRecord(
                String.valueOf(System.currentTimeMillis()),
                project.getTitle(),
                project.getLocation(),
                project.getNeighborhood(),
                project.getStatus(),
                advisorName,
                new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date())
        );

        List<AdminAssignmentRecord> records = getAssignmentHistory();
        records.add(0, record);
        if (records.size() > MAX_ASSIGNMENTS) {
            records = new ArrayList<>(records.subList(0, MAX_ASSIGNMENTS));
        }

        sharedPreferences.edit()
                .putString(KEY_ASSIGNMENT_HISTORY, toJson(records).toString())
                .apply();

        return record;
    }

    public void saveLastFilter(String screenKey, String filter) {
        sharedPreferences.edit()
                .putString(KEY_FILTER_PREFIX + screenKey, filter)
                .apply();
    }

    public String getLastFilter(String screenKey, String defaultFilter) {
        return sharedPreferences.getString(KEY_FILTER_PREFIX + screenKey, defaultFilter);
    }

    public void saveCreateProjectDraft(AdminProjectDraft draft) {
        saveProjectDraft(KEY_CREATE_PROJECT_DRAFT, draft);
    }

    public AdminProjectDraft getCreateProjectDraft() {
        return getProjectDraft(KEY_CREATE_PROJECT_DRAFT);
    }

    public void clearCreateProjectDraft() {
        sharedPreferences.edit().remove(KEY_CREATE_PROJECT_DRAFT).apply();
    }

    public void saveEditProjectDraft(AdminProjectDraft draft) {
        saveProjectDraft(KEY_EDIT_PROJECT_DRAFT, draft);
    }

    public void saveEditProjectDraft(String projectId, AdminProjectDraft draft, List<String> imageUris) {
        JSONArray images = new JSONArray();
        if (imageUris != null) {
            for (String value : imageUris) {
                images.put(value);
            }
        }
        sharedPreferences.edit()
                .putString(KEY_EDIT_PROJECT_DRAFT, draftToJson(draft).toString())
                .putString(KEY_EDIT_PROJECT_DRAFT_ID, projectId == null ? "" : projectId)
                .putString(KEY_EDIT_PROJECT_DRAFT_IMAGES, images.toString())
                .apply();
    }

    public AdminProjectDraft getEditProjectDraft() {
        return getProjectDraft(KEY_EDIT_PROJECT_DRAFT);
    }

    public boolean hasEditProjectDraftFor(String projectId) {
        return sharedPreferences.contains(KEY_EDIT_PROJECT_DRAFT)
                && sharedPreferences.getString(KEY_EDIT_PROJECT_DRAFT_ID, "")
                .equals(projectId == null ? "" : projectId);
    }

    public List<String> getEditProjectDraftImages() {
        List<String> values = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(
                    sharedPreferences.getString(KEY_EDIT_PROJECT_DRAFT_IMAGES, "[]")
            );
            for (int i = 0; i < array.length(); i++) {
                String value = array.optString(i);
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
        } catch (JSONException ignored) {
            // An invalid local draft is treated as an empty gallery.
        }
        return values;
    }

    public void clearEditProjectDraft() {
        sharedPreferences.edit()
                .remove(KEY_EDIT_PROJECT_DRAFT)
                .remove(KEY_EDIT_PROJECT_DRAFT_ID)
                .remove(KEY_EDIT_PROJECT_DRAFT_IMAGES)
                .apply();
    }

    public void saveEditedProject(AdminProjectDraft draft) {
        AdminEditedProjectRecord record = new AdminEditedProjectRecord(
                draft.getProjectName().isEmpty() ? "Proyecto sin nombre" : draft.getProjectName(),
                draft.getStatus(),
                getFormattedNow()
        );

        List<AdminEditedProjectRecord> records = getEditedProjectHistory();
        records.add(0, record);
        if (records.size() > MAX_EDITED_PROJECTS) {
            records = new ArrayList<>(records.subList(0, MAX_EDITED_PROJECTS));
        }

        JSONArray array = new JSONArray();
        for (AdminEditedProjectRecord item : records) {
            JSONObject object = new JSONObject();
            try {
                object.put("projectName", item.getProjectName());
                object.put("status", item.getStatus());
                object.put("editedAt", item.getEditedAt());
                array.put(object);
            } catch (JSONException ignored) {
                // Values are controlled by the app.
            }
        }

        sharedPreferences.edit()
                .putString(KEY_EDITED_PROJECT_HISTORY, array.toString())
                .apply();
    }

    public List<AdminEditedProjectRecord> getEditedProjectHistory() {
        String rawHistory = sharedPreferences.getString(KEY_EDITED_PROJECT_HISTORY, "[]");
        List<AdminEditedProjectRecord> records = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(rawHistory);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                records.add(new AdminEditedProjectRecord(
                        object.optString("projectName"),
                        object.optString("status"),
                        object.optString("editedAt")
                ));
            }
        } catch (JSONException ignored) {
            sharedPreferences.edit().putString(KEY_EDITED_PROJECT_HISTORY, "[]").apply();
        }
        return records;
    }

    public void saveDismissedNotificationIds(Set<String> dismissedIds) {
        sharedPreferences.edit()
                .putString(KEY_DISMISSED_NOTIFICATIONS, new JSONArray(dismissedIds).toString())
                .apply();
    }

    public Set<String> getDismissedNotificationIds() {
        String rawDismissedIds = sharedPreferences.getString(KEY_DISMISSED_NOTIFICATIONS, "[]");
        Set<String> dismissedIds = new HashSet<>();
        try {
            JSONArray array = new JSONArray(rawDismissedIds);
            for (int i = 0; i < array.length(); i++) {
                dismissedIds.add(array.optString(i));
            }
        } catch (JSONException ignored) {
            sharedPreferences.edit().putString(KEY_DISMISSED_NOTIFICATIONS, "[]").apply();
        }
        return dismissedIds;
    }

    public void saveCompanyProfile(String address, String email, String phone) {
        JSONObject object = new JSONObject();
        try {
            object.put("address", address);
            object.put("email", email);
            object.put("phone", phone);
        } catch (JSONException ignored) {
            // Values are plain form strings.
        }
        sharedPreferences.edit().putString(KEY_COMPANY_PROFILE, object.toString()).apply();
    }

    public String[] getCompanyProfile() {
        String rawProfile = sharedPreferences.getString(KEY_COMPANY_PROFILE, null);
        if (rawProfile == null) {
            return new String[]{"123 Luxury Boulevard, Suite 400", "admin@editorialestate.com", "+1 (555) 000-0000"};
        }

        try {
            JSONObject object = new JSONObject(rawProfile);
            return new String[]{
                    object.optString("address", "123 Luxury Boulevard, Suite 400"),
                    object.optString("email", "admin@editorialestate.com"),
                    object.optString("phone", "+1 (555) 000-0000")
            };
        } catch (JSONException ignored) {
            sharedPreferences.edit().remove(KEY_COMPANY_PROFILE).apply();
            return new String[]{"123 Luxury Boulevard, Suite 400", "admin@editorialestate.com", "+1 (555) 000-0000"};
        }
    }

    public void saveCompanyImageUri(int slot, String uri) {
        sharedPreferences.edit()
                .putString(slot == 0 ? KEY_COMPANY_IMAGE_PRIMARY : KEY_COMPANY_IMAGE_SECONDARY, uri)
                .apply();
    }

    public String getCompanyImageUri(int slot) {
        return sharedPreferences.getString(slot == 0 ? KEY_COMPANY_IMAGE_PRIMARY : KEY_COMPANY_IMAGE_SECONDARY, "");
    }

    public void saveAdminProfileAvatarUri(String uri) {
        sharedPreferences.edit()
                .putString(KEY_ADMIN_PROFILE_AVATAR, uri)
                .apply();
    }

    public String getAdminProfileAvatarUri() {
        return sharedPreferences.getString(KEY_ADMIN_PROFILE_AVATAR, "");
    }

    public List<AdminAssignmentRecord> getAssignmentHistory() {
        String rawHistory = sharedPreferences.getString(KEY_ASSIGNMENT_HISTORY, "[]");
        List<AdminAssignmentRecord> records = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(rawHistory);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                records.add(new AdminAssignmentRecord(
                        object.optString("id"),
                        object.optString("projectTitle"),
                        object.optString("projectLocation"),
                        object.optString("projectNeighborhood"),
                        object.optString("projectStatus"),
                        object.optString("advisorName"),
                        object.optString("assignedAt")
                ));
            }
        } catch (JSONException ignored) {
            sharedPreferences.edit().putString(KEY_ASSIGNMENT_HISTORY, "[]").apply();
        }

        return records;
    }

    private JSONArray toJson(List<AdminAssignmentRecord> records) {
        JSONArray array = new JSONArray();
        for (AdminAssignmentRecord record : records) {
            JSONObject object = new JSONObject();
            try {
                object.put("id", record.getId());
                object.put("projectTitle", record.getProjectTitle());
                object.put("projectLocation", record.getProjectLocation());
                object.put("projectNeighborhood", record.getProjectNeighborhood());
                object.put("projectStatus", record.getProjectStatus());
                object.put("advisorName", record.getAdvisorName());
                object.put("assignedAt", record.getAssignedAt());
                array.put(object);
            } catch (JSONException ignored) {
                // Values come from in-app plain strings.
            }
        }
        return array;
    }

    private void saveProjectDraft(String key, AdminProjectDraft draft) {
        sharedPreferences.edit()
                .putString(key, draftToJson(draft).toString())
                .apply();
    }

    private AdminProjectDraft getProjectDraft(String key) {
        String rawDraft = sharedPreferences.getString(key, null);
        if (rawDraft == null) {
            return null;
        }

        try {
            JSONObject object = new JSONObject(rawDraft);
            return new AdminProjectDraft(
                    object.optString("projectName"),
                    object.optString("description"),
                    object.optString("address"),
                    object.optString("city"),
                    object.optString("mapLabel"),
                    object.optString("status"),
                    object.optString("deliveryDate"),
                    object.optDouble("latitude", -12.0464),
                    object.optDouble("longitude", -77.0428),
                    typologiesFromJson(object.optJSONArray("typologies")),
                    amenitiesFromJson(object.optJSONArray("amenities"))
            );
        } catch (JSONException ignored) {
            sharedPreferences.edit().remove(key).apply();
            return null;
        }
    }

    private JSONObject draftToJson(AdminProjectDraft draft) {
        JSONObject object = new JSONObject();
        try {
            object.put("projectName", draft.getProjectName());
            object.put("description", draft.getDescription());
            object.put("address", draft.getAddress());
            object.put("city", draft.getCity());
            object.put("mapLabel", draft.getMapLabel());
            object.put("status", draft.getStatus());
            object.put("deliveryDate", draft.getDeliveryDate());
            object.put("latitude", draft.getLatitude());
            object.put("longitude", draft.getLongitude());
            object.put("typologies", typologiesToJson(draft.getTypologies()));
            object.put("amenities", amenitiesToJson(draft.getAmenities()));
        } catch (JSONException ignored) {
            // Values are controlled by the app.
        }
        return object;
    }

    private JSONArray typologiesToJson(List<AdminProjectFormTypologyItem> typologies) {
        JSONArray array = new JSONArray();
        for (AdminProjectFormTypologyItem item : typologies) {
            JSONObject object = new JSONObject();
            try {
                object.put("title", item.getTitle());
                object.put("available", item.isAvailable());
                object.put("area", item.getArea());
                object.put("bedrooms", item.getBedrooms());
                object.put("bathrooms", item.getBathrooms());
                object.put("totalAmount", item.getTotalAmount());
                object.put("separationAmount", item.getSeparationAmount());
                array.put(object);
            } catch (JSONException ignored) {
                // Values are controlled by the app.
            }
        }
        return array;
    }

    private List<AdminProjectFormTypologyItem> typologiesFromJson(JSONArray array) {
        List<AdminProjectFormTypologyItem> typologies = new ArrayList<>();
        if (array == null) {
            return typologies;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null) {
                continue;
            }
            typologies.add(new AdminProjectFormTypologyItem(
                    object.optString("title"),
                    object.optBoolean("available", true),
                    object.optString("area"),
                    object.optString("bedrooms"),
                    object.optString("bathrooms", "2 banos"),
                    object.optString("totalAmount"),
                    object.optString("separationAmount")
            ));
        }
        return typologies;
    }

    private JSONArray amenitiesToJson(List<AdminProjectFormAmenityItem> amenities) {
        JSONArray array = new JSONArray();
        for (AdminProjectFormAmenityItem item : amenities) {
            JSONObject object = new JSONObject();
            try {
                object.put("title", item.getTitle());
                object.put("iconRes", item.getIconRes());
                object.put("selected", item.isSelected());
                array.put(object);
            } catch (JSONException ignored) {
                // Values are controlled by the app.
            }
        }
        return array;
    }

    private List<AdminProjectFormAmenityItem> amenitiesFromJson(JSONArray array) {
        List<AdminProjectFormAmenityItem> amenities = new ArrayList<>();
        if (array == null) {
            return amenities;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null) {
                continue;
            }
            amenities.add(new AdminProjectFormAmenityItem(
                    object.optString("title"),
                    object.optInt("iconRes"),
                    object.optBoolean("selected")
            ));
        }
        return amenities;
    }

    private String getFormattedNow() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }
}
