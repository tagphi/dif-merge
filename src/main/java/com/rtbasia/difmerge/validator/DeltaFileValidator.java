package com.rtbasia.difmerge.validator;

import java.io.IOException;
import java.nio.file.Path;

public class DeltaFileValidator {
    public static void validate(String type, Path path) throws IOException, FileFormatException {
        if (type.equalsIgnoreCase("ip")) {
            new IpValidator(path).validate();
        } else if (type.equalsIgnoreCase("device")) {
            new DeviceValidator(path).validate();
        } else if (type.equalsIgnoreCase("default_device")) {
            new DefaultDeviceValidator(path).validate();
        }
    }
}
