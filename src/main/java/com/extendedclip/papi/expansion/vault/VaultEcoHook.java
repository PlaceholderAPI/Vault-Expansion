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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultEcoHook implements VaultHook {

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
    baltopEnabled = (Boolean) expansion.get("baltop.enabled", true);
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
    if (p == null) {
      return "";
    }

    if (identifier.startsWith("top_balance_fixed_")) {
      String[] args = identifier.split("top_balance_fixed_");

      if (args.length > 1) {
        return toLong(getTopBalance(getInt(args[1])));
      }

      return "0";
    }

    if (identifier.startsWith("top_balance_formatted_")) {
      String[] args = identifier.split("top_balance_formatted_");

      if (args.length > 1) {
        return fixMoney(getTopBalance(getInt(args[1])));
      }

      return "0";
    }

    if (identifier.startsWith("top_balance_commas_")) {
      String[] args = identifier.split("top_balance_commas_");

      if (args.length > 1) {
        return format.format(getTopBalance(getInt(args[1])));
      }

      return "0";
    }

    if (identifier.startsWith("top_balance_")) {
      String[] args = identifier.split("top_balance_");

      if (args.length > 1) {
        return String.valueOf(getTopBalance(getInt(args[1])));
      }

      return "0";
    }

    if (identifier.startsWith("top_player_")) {
      String[] args = identifier.split("top_player_");

      if (args.length > 1) {
        return getTopPlayer(getInt(args[1]));
      }

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
    return String.valueOf((long) amt);
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

  double getBalance(OfflinePlayer p) {
    if (eco != null) {
      return eco.getBalance(p);
    }
    return 0;
  }

  private String getTopPlayer(int rank) {
    if (!baltopEnabled || !balTop.containsKey(rank)) {
      return "";
    }
    return balTop.get(rank).getName();
  }

  private double getTopBalance(int rank) {
    if (!baltopEnabled || !balTop.containsKey(rank)) {
      return 0;
    }
    return balTop.get(rank).getBal();
  }

  private String getRank(String player) {
    if (!baltopEnabled) {
      return null;
    }
    Entry<Integer, TopPlayer> entry = balTop.entrySet().stream().filter(e ->
        e.getValue().getName().equals(player)).findFirst().orElse(null);
    return entry == null ? "" : entry.getKey().toString();
  }

  private int getInt(String string) {
    try {
      return Integer.parseInt(string);
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}