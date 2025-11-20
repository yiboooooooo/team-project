package stakemate.use_case.fetch_games;

import com.google.gson.annotations.SerializedName;

/**
 * Data Transfer Object representing a sport from the Odds API.
 */
public class OddsApiSport {
    @SerializedName("key")
    private String key;

    @SerializedName("group")
    private String group;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("active")
    private boolean active;

    @SerializedName("has_outrights")
    private boolean hasOutrights;

    public OddsApiSport() {
        // Default constructor for JSON deserialization
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public boolean isHasOutrights() {
        return hasOutrights;
    }

    public void setHasOutrights(final boolean hasOutrights) {
        this.hasOutrights = hasOutrights;
    }
}

