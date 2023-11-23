package com.group.cs520.service;
import com.group.cs520.model.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.cs520.model.Preference;
import com.group.cs520.model.User;
import com.group.cs520.repository.PreferenceRepository;
import com.group.cs520.repository.ProfileRepository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

@Service
public class ProfileService {
    public ProfileService() {

    }

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PreferenceService preferenceService;

    @Autowired
    private UserService userService;


    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Profile> allProfiles() {
        return profileRepository.findAll();
    }

    public Profile singleProfile(ObjectId id) {
        return profileRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Profile not found"));
    }

    public Profile update(ObjectId id, Map<String, String> profileMap) {
        Profile profile = this.singleProfile(id);

        Class<?> profileClass = Profile.class;
        Field[] fields = profileClass.getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            if (profileMap.containsKey(fieldName)) {
                setField(profile, field, profileMap.get(fieldName));
            }
        }
        profileRepository.save(profile);
        return profile;
    }

     private void setField(Object object, Field field, String value) {
        try {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                field.set(object, Integer.parseInt(value));
            }
            else if (fieldType.equals(List.class)) {
                field.set(object, TypeUtil.jsonStringArray(value));
            } else {
                field.set(object, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error setting field: " + field.getName(), e);
        }
    }

    public Profile updatePreferences(String profile_id, Map<String, String> profileMap) {
        ObjectId profile_obj_id = TypeUtil.objectIdConverter(profile_id);
        Profile profile = this.singleProfile(profile_obj_id);

        List<String> preference_ids = TypeUtil.jsonStringArray(profileMap.get("preference_ids"));
        List<Preference> preferences = new ArrayList<>();

        for (Integer ind = 0; ind < preference_ids.size(); ind ++) {
            preferences.add(preferenceService.singlePreference(preference_ids.get(ind)));
        }

        profile.setPreferences(preferences);
        profileRepository.save(profile);
        return profile;
    }

    public Profile getProfileByUser(String user_id) {
        ObjectId userObjId = TypeUtil.objectIdConverter(user_id);
        User user = userService.singleUser(userObjId);
        return user.getProfile();
    }

    public void setProfilePhoto(String userId, String photoURL){
        Profile profile = getProfileByUser(userId);

        if (profile != null) {
            if (profile.getImageUrls() == null) {
                profile.setImageUrls(new ArrayList<>());
            }
            profile.getImageUrls().add(photoURL);
            profileRepository.save(profile);
        } else {
            throw new IllegalStateException("Profile not found for user id: " + userId);
        }
    }

}
