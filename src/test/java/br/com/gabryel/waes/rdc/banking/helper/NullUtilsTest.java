package br.com.gabryel.waes.rdc.banking.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static br.com.gabryel.waes.rdc.banking.helper.NullUtils.firstNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NullUtilsTest {

    @Test
    @DisplayName("when getting firstNonNull, should return first non null value")
    void whenGettingFirstNonNull_shouldReturnFirstNonNull() {
        assertThat(firstNonNull(null, null, "first", null, "middle", "last"), equalTo("first"));
    }

    @Test
    @DisplayName("given there is no non null values, when getting firstNonNull, should return first non null value")
    void givenThereIsNoNonNullValues_whenGettingFirstNonNull_shouldReturnFirstNonNull() {
        assertThrows(IllegalArgumentException.class, () -> firstNonNull(null, null));
    }
}
