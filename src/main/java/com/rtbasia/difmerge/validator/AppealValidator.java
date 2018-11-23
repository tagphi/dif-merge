package com.rtbasia.difmerge.validator;

import java.io.IOException;
import java.nio.file.Path;

public class AppealValidator {
    public static void validate(String type, Path path) throws IOException, FileFormatException {
        if (type.equalsIgnoreCase("ip")) {
            new IpAppealValidator(path).validate();
        } else if (type.equalsIgnoreCase("device")) {
            new DeviceAppealValidator(path).validate();
        } else if (type.equalsIgnoreCase("default_device")) {
            new DefaultDeviceAppealValidator(path).validate();
        } else {
            throw new IllegalArgumentException("unkown type " + type);
        }
    }
}
