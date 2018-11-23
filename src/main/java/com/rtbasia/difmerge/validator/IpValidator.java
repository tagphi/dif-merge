package com.rtbasia.difmerge.validator;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class IpValidator extends AbstractFileValidator {
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private static Pattern pattern;

    static {
        pattern = Pattern.compile(IPADDRESS_PATTERN);
    }

    public IpValidator(Path path) {
        super(path, 2);
    }

    @Override
    void validateCols(String[] cols) throws FileFormatException {
        String flagCol = cols[cols.length - 1];

        if (!"1".equals(flagCol) && !"0".equals(flagCol)) {
            throw new FileFormatException(flagCol + " is not a valid flag");
        }

        String ip = cols[0];

        if (!pattern.matcher(ip).matches()) {
            throw new FileFormatException(ip + " is not a valid ip address");
        }
    }
}
