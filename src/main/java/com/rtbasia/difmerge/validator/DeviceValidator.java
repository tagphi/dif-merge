package com.rtbasia.difmerge.validator;

import java.nio.file.Path;

public class DeviceValidator extends AbstractFileValidator{

    public DeviceValidator(Path path) {
        super(path, 4);
    }

    @Override
    void validateCols(String[] cols) throws FileFormatException {
        String flagCol = cols[cols.length - 1];

        if (!"1".equals(flagCol) && !"0".equals(flagCol)) {
            throw new FileFormatException(flagCol + " is not a valid flag");
        }

        String deviceType = cols[1];
        String encryptType = cols[2];

        if (!DeviceTypes.isValid(deviceType)) {
            throw new FileFormatException(deviceType + " is not supported");
        }

        if (!EnryptTypes.isValid(encryptType)) {
            throw new FileFormatException(encryptType + " is not supported");
        }
    }
}
