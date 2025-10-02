package br.com.gabryel.waes.rdc.banking.helper;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Objects;

@UtilityClass
public class NullUtils {
    @SafeVarargs
    public static <T> T firstNonNull(T... values) {
        return Arrays.stream(values)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No non-null values found"));
    }
}
