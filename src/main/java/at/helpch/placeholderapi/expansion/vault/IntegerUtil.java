package at.helpch.placeholderapi.expansion.vault;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;

public class IntegerUtil {

    public static BigInteger getBigInteger(@NotNull String number) {
        return new BigInteger(number.replaceAll("_", ""));
    }

    public static BigDecimal getBigDecimal(double number) {
        return new BigDecimal(Double.toString(number));
    }
}
