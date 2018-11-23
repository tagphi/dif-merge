package com.rtbasia.difmerge.validator;

import java.nio.file.Path;

public class DeviceAppealValidator extends AbstractFileValidator{

    public DeviceAppealValidator(Path path) {
        super(path, 3);
    }

    @Override
    void validateCols(String[] cols) throws FileFormatException {
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
