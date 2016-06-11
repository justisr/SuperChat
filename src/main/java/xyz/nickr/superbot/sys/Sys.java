package xyz.nickr.superbot.sys;

import java.util.regex.Pattern;

public interface Sys {

    final Pattern START_OF_LINE = Pattern.compile("^", Pattern.MULTILINE);

    String getName();

    String prefix();

    boolean isUIDCaseSensitive();

    default boolean hasKeyboards() {
        return false;
    }

    MessageBuilder message();

    String getUserFriendlyName(String uniqueId);

    GroupConfiguration getGroupConfiguration(String uniqueId);

    void addGroupConfiguration(GroupConfiguration cfg);

    default void onLoaded() {}

}
