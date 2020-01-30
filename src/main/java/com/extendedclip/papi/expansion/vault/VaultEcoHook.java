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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import static java.util.stream.Collectors.toMap;

public class VaultEcoHook implements VaultHook {

  private Economy econ = null;

  private String k, m, b, t, q;

  private DecimalFormat format = new DecimalFormat("#,###");

  VaultEcoHook(VaultExpansion ex) {
    k = ex.getString("formatting.thousands", "k");
    m = ex.getString("formatting.millions", "m");
    b = ex.getString("formatting.billions", "b");
    t = ex.getString("formatting.trillions", "t");
    q = ex.getString("formatting.quadrillions", "q");
  }

  @Override
  public boolean setup() {
    RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager()
            .getRegistration(Economy.class);

    if (rsp == null) {
      return false;
    }

    econ = rsp.getProvider();
    return econ != null;
  }

  @Override
  public String onPlaceholderRequest(OfflinePlayer p, String identifier) {
    if (p == null) {
      return "";
    }

    if (identifier.startsWith("top_balance_fixed_")) {
      String[] args = identifier.split("top_balance_fixed_");

      if (args.length > 1)
        return toLong(getTopBalance(getInt(args[1])));

      return "0";
    }

    if (identifier.startsWith("top_balance_formatted_")) {
      String[] args = identifier.split("top_balance_formatted_");

      if (args.length > 1)
        return fixMoney(getTopBalance(getInt(args[1])));

      return "0";
    }

    if (identifier.startsWith("top_balance_commas")) {
      String[] args = identifier.split("top_balance_commas_");

      if (args.length > 1)
        return format.format(getTopBalance(getInt(args[1])));

      return "0";
    }

    if (identifier.startsWith("top_balance_")) {
      String[] args = identifier.split("top_balance_");

      if (args.length > 1)
        return String.valueOf(getTopBalance(getInt(args[1])));

      return "0";
    }

    if (identifier.startsWith("top_player_")) {
      String[] args = identifier.split("top_player_");

      if (args.length > 1)
        return getTopPlayer(getInt(args[1]));

      return "";
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
    long send = (long) amt;
    return String.valueOf(send);
  }

  private String format(double d) {
    NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
    format.setMaximumFractionDigits(2);
    format.setMinimumFractionDigits(0);
    return format.format(d);
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

  private double getBalance(OfflinePlayer p) {
    if (econ != null) {
      return econ.getBalance(p);
    }
    return 0;
  }

  private Map<String, Double> getTopPlayers() {
    Map<String, Double> top = new LinkedHashMap<>();

    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
        if (player == null || player.getName() == null)
            continue;

        top.put(player.getName(), econ.getBalance(player));
    }

    return top.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (player, balance) -> balance, LinkedHashMap::new));
  }

  private String getTopPlayer(int rank) {
      String[] topPlayers = getTopPlayers().keySet().toArray(new String[0]);

      if (rank > topPlayers.length || rank < 1)
          return "";

      return topPlayers[rank - 1];
  }

  private double getTopBalance(int rank) {
      Double[] topBalances = getTopPlayers().values().toArray(new Double[0]);

      if (rank > topBalances.length || rank < 1)
          return 0;

      return topBalances[rank - 1];
  }

  private String getRank(String player) {
      String[] topPlayers = getTopPlayers().keySet().toArray(new String[0]);

      return String.valueOf(ArrayUtils.indexOf(topPlayers, player) + 1);
  }

  private int getInt(String string) {
      try {
          return Integer.parseInt(string);
      } catch (NumberFormatException e) {
          return 0;
      }
  }
}