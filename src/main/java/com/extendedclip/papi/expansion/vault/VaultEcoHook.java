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
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VaultEcoHook implements VaultHook {

    private static final Pattern TOP_BALANCE_FIXED_PATTERN = Pattern.compile("top_balance_fixed_");
    private static final Pattern TOP_BALANCE_FORMATTED_PATTERN = Pattern.compile("top_balance_formatted_");
    private static final Pattern TOP_BALANCE_COMMAS_PATTERN = Pattern.compile("top_balance_commas_");
    private static final Pattern TOP_BALANCE_PATTERN = Pattern.compile("top_balance_");
    private static final Pattern TOP_PLAYER_PATTERN = Pattern.compile("top_player_");

    private static final Pattern BALANCE_DECIMAL_POINTS_PATTERN = Pattern.compile("balance_(?<points>\\d+)dp");

    private final Map<Integer, DecimalFormat> decimalFormats = new HashMap<>();

    private final VaultExpansion expansion;
    private final String k;
    private final String m;
    private final String b;
    private final String t;
    private final String q;
    private final DecimalFormat format = new DecimalFormat("#,###");
    private final boolean baltopEnabled;
    private final int taskDelay;
    private final int topSize;
    private final Map<Integer, TopPlayer> balTop = new TreeMap<>();
    private Economy eco;
    private VaultPermsHook perms;
    private BalTopTask balTopTask;

    VaultEcoHook(VaultExpansion expansion, VaultPermsHook perms) {
        this.expansion = expansion;
        this.perms = perms;
        baltopEnabled = (Boolean) expansion.get("baltop.enabled", false);
        topSize = expansion.getInt("baltop.cache_size", 100);
        taskDelay = expansion.getInt("baltop.check_delay", 30);
        k = expansion.getString("formatting.thousands", "k");
        m = expansion.getString("formatting.millions", "m");
        b = expansion.getString("formatting.billions", "b");
        t = expansion.getString("formatting.trillions", "t");
        q = expansion.getString("formatting.quadrillions", "q");
    }

    @Override
    public boolean setup() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager()
                .getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        eco = rsp.getProvider();

        if (eco != null && baltopEnabled) {
            this.balTopTask = new BalTopTask(this, perms);
            balTopTask.runTaskTimerAsynchronously(expansion.getPlaceholderAPI(), 20, 20 * taskDelay);
        }
        return eco != null;
    }

    public void clear() {
        balTop.clear();
        if (this.balTopTask != null) {
            this.balTopTask.cancel();
            this.balTopTask = null;
        }
    }

    protected Economy getEco() {
        return eco;
    }

    protected VaultExpansion getExpansion() {
        return this.expansion;
    }

    protected Map<Integer, TopPlayer> getBalTop() {
        return balTop;
    }

    void setBalTop(Map<String, Double> map) {
        this.balTop.clear();
        int count = 1;
        for (Entry<String, Double> entry : map.entrySet()) {
            if (count >= topSize) {
                break;
            }
            balTop.put(count, new TopPlayer(entry.getKey(), entry.getValue()));
            count++;
        }
    }

    @Override
    public String onPlaceholderRequest(OfflinePlayer p, String identifier) {
        if (!baltopEnabled) {
            return (identifier.startsWith("top_balance")) ? "0" : "";
        }

        if (identifier.startsWith("top_balance_fixed_")) {
            String[] args = TOP_BALANCE_FIXED_PATTERN.split(identifier);

            if (args.length > 1) {
                return toLong(getTopBalance(getInt(args[1])));
            }

            return "0";
        }

        if (identifier.startsWith("top_balance_formatted_")) {
            String[] args = TOP_BALANCE_FORMATTED_PATTERN.split(identifier);

            if (args.length > 1) {
                return fixMoney(getTopBalance(getInt(args[1])));
            }

            return "0";
        }

        if (identifier.startsWith("top_balance_commas_")) {
            String[] args = TOP_BALANCE_COMMAS_PATTERN.split(identifier);

            if (args.length > 1) {
                return format.format(getTopBalance(getInt(args[1])));
            }

            return "0";
        }

        if (identifier.startsWith("top_balance_")) {
            String[] args = TOP_BALANCE_PATTERN.split(identifier);

            if (args.length > 1) {
                return String.valueOf(getTopBalance(getInt(args[1])));
            }

            return "0";
        }

        if (identifier.startsWith("top_player_")) {
            String[] args = TOP_PLAYER_PATTERN.split(identifier);

            if (args.length > 1) {
                return getTopPlayer(getInt(args[1]));
            }

            return "";
        }

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
            case "top_rank":
                return getRank(p.getName());
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

    private String getTopPlayer(int rank) {
        if (!baltopEnabled) {
            return "";
        }

        final TopPlayer topPlayer = balTop.get(rank);
        return topPlayer.getName();
    }

    private double getTopBalance(int rank) {
        if (!baltopEnabled) {
            return 0;
        }

        final TopPlayer topPlayer = balTop.get(rank);
        return topPlayer == null ? 0 : topPlayer.getBal();
    }

    private String getRank(String player) {
        if (!baltopEnabled) {
            return null;
        }

        return balTop.entrySet().stream()
                .filter(e -> e.getValue().getName().equals(player))
                .findFirst()
                .map(e -> e.getKey().toString())
                .orElse("");
    }

    private int getInt(String string) {
        final Integer integer = Ints.tryParse(string);
        return integer == null ? 0 : integer;
    }

}