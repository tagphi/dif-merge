package com.rtbasia.difmerge.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractFileValidator {
    private Path path;
    private int columeCount;

    abstract void validateCols(String[] cols) throws FileFormatException;

    public AbstractFileValidator(Path path, int columnCount) {
        this.path = path;
        this.columeCount = columnCount;
    }

    public void validate() throws FileFormatException, IOException {
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line = null;

            while((line = br.readLine()) != null) {
                String[] cols = line.split("\t");

                if (cols.length != columeCount) {
                    throw new FileFormatException(line);
                }

                validateCols(cols);
            }
        }
    }
}
