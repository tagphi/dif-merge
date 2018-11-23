package com.rtbasia.difmerge.validator;

import java.io.IOException;
import java.nio.file.Path;

public class DeltaFileValidator {
    public static void validate(String type, Path path) throws IOException, FileFormatException {
        if (type.equalsIgnoreCase("IP")) {
            new IpValidator(path).validate();
        } else if (type.equalsIgnoreCase("Device")) {
            new DeviceValidator(path).validate();
        } else if (type.equalsIgnoreCase("DefaultDevice")) {
            new DefaultDeviceValidator(path).validate();
        }  else {
            throw new IllegalArgumentException("unkown type " + type);
        }
    }
}
