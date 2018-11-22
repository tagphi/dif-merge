package com.rtbasia.difmerge.validator;

import java.util.HashSet;
import java.util.Set;

public class EnryptTypes {
    public static Set<String> TYPES = new HashSet<>();

    static {
        TYPES.add("MD5");
        TYPES.add("RAW");
    }

    public static boolean isValid(String type) {
        return TYPES.contains(type);
    }
}
