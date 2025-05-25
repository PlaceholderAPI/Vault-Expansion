package at.helpch.placeholderapi.expansion.vault;

import com.google.common.primitives.Ints;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EconomyHook extends VaultHook {

    private static final Pattern BALANCE_DECIMAL_POINTS_PATTERN = Pattern.compile("balance_(?<points>\\d+)dp");
    private static final DecimalFormat COMMAS_FORMAT = new DecimalFormat("#,###");
    private static final DecimalFormat FIXED_FORMAT = new DecimalFormat("#");
    private static final Map<Integer, DecimalFormat> DECIMAL_FORMATS_CACHE = new HashMap<>();

    private final NavigableMap<BigInteger, String> suffixes = new TreeMap<>();
    private Economy economy;

    public EconomyHook(VaultExpansion expansion) {
        super(expansion);
        suffixes.put(IntegerUtil.getBigInteger("1_000"), expansion.getString("formatting.thousands", "K"));
        suffixes.put(IntegerUtil.getBigInteger("1_000_000"), expansion.getString("formatting.millions", "M"));
        suffixes.put(IntegerUtil.getBigInteger("1_000_000_000"), expansion.getString("formatting.billions", "B"));
        suffixes.put(IntegerUtil.getBigInteger("1_000_000_000_000"), expansion.getString("formatting.trillions", "T"));
        suffixes.put(IntegerUtil.getBigInteger("1_000_000_000_000_000"), expansion.getString("formatting.quadrillions", "Q"));
        suffixes.put(IntegerUtil.getBigInteger("1_000_000_000_000_000_000"), expansion.getString("formatting.quintillion", "Qi"));
        suffixes.put(IntegerUtil.getBigInteger("1_000_000_000_000_000_000_000"), expansion.getString("formatting.sextillion", "Sx"));
        suffixes.put(IntegerUtil.getBigInteger("1_000_000_000_000_000_000_000_000"), expansion.getString("formatting.septillion", "Sp"));
        setup();
    }

    private BigDecimal getBalance(@NotNull final OfflinePlayer player) {
        return IntegerUtil.getBigDecimal(economy.getBalance(player));
    }

    private @NotNull String setDecimalPoints(double balance, int points) {
        final DecimalFormat cachedFormat = DECIMAL_FORMATS_CACHE.get(points);

        if (cachedFormat != null) {
            return cachedFormat.format(balance);
        }

        final DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getIntegerInstance();
        decimalFormat.setMaximumFractionDigits(points);
        decimalFormat.setGroupingUsed(false);
        DECIMAL_FORMATS_CACHE.put(points, decimalFormat);
        return decimalFormat.format(balance);
    }

    /**
     * Format player's balance, 1200 -> 1.2K
     *
     * @param balance balance to format
     * @return balance formatted
     * @author <a href="https://stackoverflow.com/users/829571/assylias">assylias</a> (<a href="https://stackoverflow.com/a/30661479/11496439">source</a>)
     */
    private @NotNull String formatBalance(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }

        BigInteger intPart = new BigInteger(balance.toPlainString().split("\\.")[0]);
        Map.Entry<BigInteger, String> e = suffixes.floorEntry(intPart);

        if (e == null) {
            return balance.stripTrailingZeros().toPlainString();
        }

        BigDecimal divideBy = new BigDecimal(e.getKey());
        String suffix = e.getValue();

        BigDecimal result = balance.divide(divideBy, 3, RoundingMode.DOWN).stripTrailingZeros();

        return result.toPlainString() + suffix;
    }

    @Override
    public void setup() {
        economy = getService(Economy.class);
    }

    @Override
    public boolean isReady() {
        return economy != null;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) {
            return "";
        }

        final BigDecimal balance = getBalance(offlinePlayer);

        if (params.startsWith("balance_")) {
            final Matcher matcher = BALANCE_DECIMAL_POINTS_PATTERN.matcher(params);

            if (matcher.find()) {
                final Integer points = Ints.tryParse(matcher.group("points"));

                if (points == null) {
                    return matcher.group("points") + " is not a valid number";
                }

                return setDecimalPoints(balance.doubleValue(), points);
            }
        }

        switch (params) {
            case "balance":
                return setDecimalPoints(balance.doubleValue(), Math.max(2, economy.fractionalDigits()));
            case "balance_fixed":
                return FIXED_FORMAT.format(balance);
            case "balance_formatted": {
                return formatBalance(balance);
            }
            case "balance_commas":
                return COMMAS_FORMAT.format(balance);
            default:
                return null;
        }
    }

}
