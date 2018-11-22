package com.rtbasia.difmerge.validator;

import java.util.HashSet;
import java.util.Set;

public class DeviceTypes {
    public static Set<String> TYPES = new HashSet<>();

    static {
        TYPES.add("IMEI");
        TYPES.add("IDFA");
        TYPES.add("MAC");
        TYPES.add("ANDROIDID");
    }

    public static boolean isValid(String type) {
        return TYPES.contains(type);
    }
}
