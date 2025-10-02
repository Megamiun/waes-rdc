package br.com.gabryel.waes.rdc.banking.matchers;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class TestConstants {

    public static BigDecimal MONEY_EPSILON = BigDecimal.valueOf(0.001);
}
