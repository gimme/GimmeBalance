package me.gimme.gimmebalance.language;

import me.gimme.gimmecore.language.PlaceholderString;

public enum Placeholder implements PlaceholderString {

    TIME;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    @Override
    public String getPlaceholder() {
        return name().toLowerCase();
    }

}
