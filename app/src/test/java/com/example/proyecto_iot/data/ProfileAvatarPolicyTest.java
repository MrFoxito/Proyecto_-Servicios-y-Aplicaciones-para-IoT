package com.example.proyecto_iot.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProfileAvatarPolicyTest {
    @Test public void acceptsPublicHttpAndHttpsAvatarUrls() {
        assertTrue(ProfileAvatarPolicy.isValidRemoteUrl("https://cdn.example.com/avatars/a.png"));
        assertTrue(ProfileAvatarPolicy.isValidRemoteUrl("http://images.example.com/a.jpg"));
    }

    @Test public void rejectsMissingEmptyAndMalformedAvatarUrls() {
        assertFalse(ProfileAvatarPolicy.isValidRemoteUrl(null));
        assertFalse(ProfileAvatarPolicy.isValidRemoteUrl(""));
        assertFalse(ProfileAvatarPolicy.isValidRemoteUrl("  "));
        assertFalse(ProfileAvatarPolicy.isValidRemoteUrl("not-a-url"));
        assertFalse(ProfileAvatarPolicy.isValidRemoteUrl("https://"));
    }

    @Test public void rejectsNonRemoteOrWhitespaceUrls() {
        assertFalse(ProfileAvatarPolicy.isValidRemoteUrl("file:///avatar.png"));
        assertFalse(ProfileAvatarPolicy.isValidRemoteUrl("https://example.com/avatar image.png"));
    }
}
