package com.rtbasia.difmerge.validator;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class IpAppealValidator extends AbstractFileValidator {
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private static Pattern pattern;

    static {
        pattern = Pattern.compile(IPADDRESS_PATTERN);
    }

    public IpAppealValidator(Path path) {
        super(path, 1);
    }

    @Override
    void validateCols(String[] cols) throws FileFormatException {
        String ip = cols[0];

        if (!pattern.matcher(ip).matches()) {
            throw new FileFormatException(ip + " is not a valid ip address");
        }
    }
}
