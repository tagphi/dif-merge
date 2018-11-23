package com.rtbasia.difmerge.validator;

import java.nio.file.Path;

public class DefaultDeviceValidator extends AbstractFileValidator {
    public DefaultDeviceValidator(Path path) {
        super(path, 3);
    }

    @Override
    void validateCols(String[] cols) throws FileFormatException {
        String flagCol = cols[cols.length - 1];

        if (!"1".equals(flagCol) && "0".equals(flagCol)) {
            throw new FileFormatException(flagCol + " is not a valid flag");
        }

        String deviceType = cols[1];

        if (!DeviceTypes.isValid(deviceType))
            throw new FileFormatException(deviceType + " is not supported");
    }
}
