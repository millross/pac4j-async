package org.pac4j.async.oauth.profile.definition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.converter.Converters;
import org.pac4j.core.profile.converter.DateConverter;
import org.pac4j.oauth.profile.JsonHelper;
import org.pac4j.oauth.profile.converter.JsonConverter;
import org.pac4j.oauth.profile.facebook.*;
import org.pac4j.oauth.profile.facebook.converter.FacebookRelationshipStatusConverter;

import java.util.Arrays;
import java.util.List;

/**
 * This class is the Facebook profile definition.
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class FacebookProfileDefinition extends OAuth20ProfileDefinition<FacebookProfile> {

    public static final String NAME = "name";
    public static final String MIDDLE_NAME = "middle_name";
    public static final String LAST_NAME = "last_name";
    public static final String LANGUAGES = "languages";
    public static final String LINK = "link";
    public static final String THIRD_PARTY_ID = "third_party_id";
    public static final String TIMEZONE = "timezone";
    public static final String UPDATED_TIME = "updated_time";
    public static final String VERIFIED = "verified";
    public static final String ABOUT = "about";
    public static final String BIRTHDAY = "birthday";
    public static final String EDUCATION = "education";
    public static final String HOMETOWN = "hometown";
    public static final String INTERESTED_IN = "interested_in";
    public static final String POLITICAL = "political";
    public static final String FAVORITE_ATHLETES = "favorite_athletes";
    public static final String FAVORITE_TEAMS = "favorite_teams";
    public static final String QUOTES = "quotes";
    public static final String RELATIONSHIP_STATUS = "relationship_status";
    public static final String RELIGION = "religion";
    public static final String SIGNIFICANT_OTHER = "significant_other";
    public static final String WEBSITE = "website";
    public static final String WORK = "work";
    public static final String FRIENDS = "friends";
    public static final String MOVIES = "movies";
    public static final String MUSIC = "music";
    public static final String BOOKS = "books";
    public static final String LIKES = "likes";
    public static final String ALBUMS = "albums";
    public static final String EVENTS = "events";
    public static final String GROUPS = "groups";
    public static final String MUSIC_LISTENS = "music.listens";
    public static final String PICTURE = "picture";

    public FacebookProfileDefinition() {
        super(x -> new FacebookProfile());
        Arrays.stream(new String[] {
            NAME, MIDDLE_NAME, LAST_NAME, THIRD_PARTY_ID, ABOUT, POLITICAL, QUOTES, RELIGION, WEBSITE
        }).forEach(a -> primary(a, Converters.STRING));
        primary(TIMEZONE, Converters.INTEGER);
        primary(VERIFIED, Converters.BOOLEAN);
        primary(LINK, Converters.URL);
        final JsonConverter<FacebookObject> objectConverter = new JsonConverter<>(FacebookObject.class);
        final JsonConverter multiObjectConverter = new JsonConverter(List.class, new TypeReference<List<FacebookObject>>() {});
        final JsonConverter multiInfoConverter = new JsonConverter(List.class, new TypeReference<List<FacebookInfo>>() {});
        primary(UPDATED_TIME, Converters.DATE_TZ_GENERAL);
        primary(BIRTHDAY, new DateConverter("MM/dd/yyyy"));
        primary(RELATIONSHIP_STATUS, new FacebookRelationshipStatusConverter());
        primary(LANGUAGES, multiObjectConverter);
        primary(EDUCATION, new JsonConverter(List.class, new TypeReference<List<FacebookEducation>>() {}));
        primary(HOMETOWN, objectConverter);
        primary(INTERESTED_IN, new JsonConverter(List.class, new TypeReference<List<String>>() {}));
        primary(LOCATION, objectConverter);
        primary(FAVORITE_ATHLETES, multiObjectConverter);
        primary(FAVORITE_TEAMS, multiObjectConverter);
        primary(SIGNIFICANT_OTHER, objectConverter);
        primary(WORK, new JsonConverter(List.class, new TypeReference<List<FacebookWork>>() {}));
        secondary(FRIENDS, multiObjectConverter);
        secondary(MOVIES, multiInfoConverter);
        secondary(MUSIC, multiInfoConverter);
        secondary(BOOKS, multiInfoConverter);
        secondary(LIKES, multiInfoConverter);
        secondary(ALBUMS, new JsonConverter(List.class, new TypeReference<List<FacebookPhoto>>() {}));
        secondary(EVENTS, new JsonConverter(List.class, new TypeReference<List<FacebookEvent>>() {}));
        secondary(GROUPS, new JsonConverter(List.class, new TypeReference<List<FacebookGroup>>() {}));
        secondary(MUSIC_LISTENS, new JsonConverter(List.class, new TypeReference<List<FacebookMusicListen>>() {}));
        secondary(PICTURE, new JsonConverter<>(FacebookPicture.class));
    }

    @Override
    public FacebookProfile extractUserProfile(final String body) throws HttpAction {
        final FacebookProfile profile = newProfile();
        final JsonNode json = JsonHelper.getFirstNode(body);
        if (json != null) {
            profile.setId(JsonHelper.getElement(json, "id"));
            for (final String attribute : getPrimaryAttributes()) {
                convertAndAdd(profile, attribute, JsonHelper.getElement(json, attribute));
            }
            extractData(profile, json, org.pac4j.oauth.profile.facebook.FacebookProfileDefinition.FRIENDS);
            extractData(profile, json, org.pac4j.oauth.profile.facebook.FacebookProfileDefinition.MOVIES);
            extractData(profile, json, org.pac4j.oauth.profile.facebook.FacebookProfileDefinition.MUSIC);
            extractData(profile, json, org.pac4j.oauth.profile.facebook.FacebookProfileDefinition.BOOKS);
            extractData(profile, json, org.pac4j.oauth.profile.facebook.FacebookProfileDefinition.LIKES);
            extractData(profile, json, org.pac4j.oauth.profile.facebook.FacebookProfileDefinition.ALBUMS);
            extractData(profile, json, org.pac4j.oauth.profile.facebook.FacebookProfileDefinition.EVENTS);
            extractData(profile, json, org.pac4j.oauth.profile.facebook.FacebookProfileDefinition.GROUPS);
            extractData(profile, json, org.pac4j.oauth.profile.facebook.FacebookProfileDefinition.MUSIC_LISTENS);
            extractData(profile, json, org.pac4j.oauth.profile.facebook.FacebookProfileDefinition.PICTURE);
        }
        return profile;
    }

    protected void extractData(final FacebookProfile profile, final JsonNode json, final String name) {
        final JsonNode data = (JsonNode) JsonHelper.getElement(json, name);
        if (data != null) {
            convertAndAdd(profile, name, JsonHelper.getElement(data, "data"));
        }
    }
}
