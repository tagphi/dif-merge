package com.rtbasia.difmerge.validator;

import java.nio.file.Path;

public class DefaultDeviceAppealValidator extends AbstractFileValidator {
    public DefaultDeviceAppealValidator(Path path) {
        super(path, 2);
    }

    @Override
    void validateCols(String[] cols) throws FileFormatException {
        String deviceType = cols[1];

        if (!DeviceTypes.isValid(deviceType))
            throw new FileFormatException(deviceType + " is not supported");
    }
}
