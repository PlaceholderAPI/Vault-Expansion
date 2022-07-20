/*
 *
 * Vault-Expansion
 * Copyright (C) 2018-2020 Ryan McCarthy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package com.extendedclip.papi.expansion.vault;

import com.google.common.primitives.Ints;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VaultEcoHook implements VaultHook {

    private static final Pattern BALANCE_DECIMAL_POINTS_PATTERN = Pattern.compile("balance_(?<points>\\d+)dp");

    private final Map<Integer, DecimalFormat> decimalFormats = new HashMap<>();

    private final String k;
    private final String m;
    private final String b;
    private final String t;
    private final String q;
    private final DecimalFormat format = new DecimalFormat("#,###");

    private final VaultExpansion expansion;

    private Economy eco;

    VaultEcoHook(VaultExpansion expansion) {
        this.expansion = expansion;

        k = expansion.getString("formatting.thousands", "k");
        m = expansion.getString("formatting.millions", "m");
        b = expansion.getString("formatting.billions", "b");
        t = expansion.getString("formatting.trillions", "t");
        q = expansion.getString("formatting.quadrillions", "q");
    }

    @Override
    public boolean setup() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        eco = rsp.getProvider();
        return true;
    }

    protected Economy getEco() {
        return eco;
    }

    protected VaultExpansion getExpansion() {
        return this.expansion;
    }

    @Override
    public String onPlaceholderRequest(OfflinePlayer p, String identifier) {
        if (p == null) {
            return "";
        }

        if (identifier.startsWith("balance_")) {
            final Matcher matcher = BALANCE_DECIMAL_POINTS_PATTERN.matcher(identifier);

            if (matcher.find()) {
                return setDecimalPoints(getBalance(p), getInt(matcher.group("points")));
            }
        }

        switch (identifier) {
            case "balance":
                return String.valueOf(getBalance(p));
            case "balance_fixed":
                return toLong(getBalance(p));
            case "balance_formatted":
                return fixMoney(getBalance(p));
            case "balance_commas":
                return format.format(getBalance(p));
        }
        return null;
    }

    private String toLong(double amt) {
        return String.valueOf((long) amt);
    }

    private String format(double d) {
        return eco == null ? "0" : eco.format(d);
    }

    private String fixMoney(double d) {

        if (d < 1000L) {
            return format(d);
        }
        if (d < 1000000L) {
            return format(d / 1000L) + k;
        }
        if (d < 1000000000L) {
            return format(d / 1000000L) + m;
        }
        if (d < 1000000000000L) {
            return format(d / 1000000000L) + b;
        }
        if (d < 1000000000000000L) {
            return format(d / 1000000000000L) + t;
        }
        if (d < 1000000000000000000L) {
            return format(d / 1000000000000000L) + q;
        }

        return toLong(d);
    }

    private String setDecimalPoints(double d, int points) {
        DecimalFormat decimalFormat = this.decimalFormats.get(points);

        if (decimalFormat != null) {
            return decimalFormat.format(d);
        }

        decimalFormat = (DecimalFormat) DecimalFormat.getIntegerInstance();
        decimalFormat.setMaximumFractionDigits(points);
        decimalFormat.setGroupingUsed(false);

        this.decimalFormats.put(points, decimalFormat);
        return decimalFormat.format(d);
    }

    double getBalance(OfflinePlayer p) {
        return eco == null ? 0 : eco.getBalance(p);
    }

    private int getInt(String string) {
        final Integer integer = Ints.tryParse(string);
        return integer == null ? 0 : integer;
    }

}
