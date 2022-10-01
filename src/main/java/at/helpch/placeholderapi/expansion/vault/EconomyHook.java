package at.helpch.placeholderapi.expansion.vault;

import com.google.common.primitives.Ints;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private final NavigableMap<Long, String> suffixes = new TreeMap<>();
    private Economy economy;

    public EconomyHook(VaultExpansion expansion) {
        super(expansion);
        suffixes.put(1_000L, expansion.getString("formatting.thousands", "K"));
        suffixes.put(1_000_000L, expansion.getString("formatting.millions", "M"));
        suffixes.put(1_000_000_000L, expansion.getString("formatting.billions", "B"));
        suffixes.put(1_000_000_000_000L, expansion.getString("formatting.trillions", "T"));
        suffixes.put(1_000_000_000_000_000L, expansion.getString("formatting.quadrillions", "Q"));
    }

    private double getBalance(@NotNull final OfflinePlayer player) {
        return economy.getBalance(player);
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
    private @NotNull String formatBalance(long balance) {
        //Long.MIN_VALUE == -Long.MIN_VALUE, so we need an adjustment here
        if (balance == Long.MIN_VALUE) {
            return formatBalance(Long.MIN_VALUE + 1);
        }
        if (balance < 0) {
            return "-" + formatBalance(-balance);
        }

        if (balance < 1000) {
            return Long.toString(balance); //deal with easy case
        }

        final Map.Entry<Long, String> e = suffixes.floorEntry(balance);
        final Long divideBy = e.getKey();
        final String suffix = e.getValue();

        long truncated = balance / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    @Override
    public boolean setup() {
        return (economy = getService(Economy.class)) != null;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) {
            return "";
        }

        final double balance = getBalance(offlinePlayer);

        if (params.startsWith("balance_")) {
            final Matcher matcher = BALANCE_DECIMAL_POINTS_PATTERN.matcher(params);

            if (matcher.find()) {
                final Integer points = Ints.tryParse(matcher.group("points"));

                if (points == null) {
                    return matcher.group("points") + " is not a valid number";
                }

                return setDecimalPoints(balance, points);
            }
        }

        switch (params) {
            case "balance":
                return setDecimalPoints(balance, Math.max(2, economy.fractionalDigits()));
            case "balance_fixed":
                return FIXED_FORMAT.format(balance);
            case "balance_formatted":
                return formatBalance((long) balance);
            case "balance_commas":
                return COMMAS_FORMAT.format(balance);
            default:
                return null;
        }
    }

}
