/*
 *
 * Vault-Expansion
 * Copyright (C) 2018 Ryan McCarthy
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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import static java.util.stream.Collectors.toMap;

public class VaultEcoHook implements VaultHook {

  private Economy econ = null;

  private String k, m, b, t, q;

  private DecimalFormat format = new DecimalFormat("#,###");

  public VaultEcoHook(VaultExpansion ex) {
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
    if (identifier.equals("balance")) {
      return String.valueOf(getBalance(p));
    } else if (identifier.equals("balance_fixed")) {
      return toLong(getBalance(p));
    } else if (identifier.equals("balance_formatted")) {
      return fixMoney(getBalance(p));
    } else if (identifier.equals("balance_commas")) {
      return format.format(getBalance(p));
    } else if (identifier.equals("top_rank")) {
        return getTop(p.getPlayer().getName(), 1);
    } else if (identifier.startsWith("top_balance_fixed_")) {
      int rank = Integer.parseInt(identifier.split("top_balance_fixed_")[1]);
      return toLong(Double.parseDouble(getTop("bal", rank)));
    } else if (identifier.startsWith("top_balance_formatted_")) {
      int rank = Integer.parseInt(identifier.split("top_balance_formatted_")[1]);
      return fixMoney(Double.parseDouble(getTop("bal", rank)));
    } else if (identifier.startsWith("top_balance_commas")) {
      int rank = Integer.parseInt(identifier.split("top_balance_commas_")[1]);
      return format.format(Double.parseDouble(getTop("bal", rank)));
    } else if (identifier.startsWith("top_balance_")) {
      int rank = Integer.parseInt(identifier.split("top_balance_")[1]);
      return getTop("bal", rank);
    } else if (identifier.startsWith("top_player_")) {
      int rank = Integer.parseInt(identifier.split("top_player_")[1]);
      return getTop("player", rank);
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

    return String.valueOf(toLong(d));
  }

  private double getBalance(OfflinePlayer p) {
    if (econ != null) {
      return econ.getBalance(p);
    }
    return 0;
  }

  private String getTop(String balOrPlayer, int rank) {
    Map<String, Double> top = new LinkedHashMap<>();
    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
      top.put(player.getName(), econ.getBalance(player));
    }
    Map<String, Double> sorted = top.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    List players = new ArrayList(sorted.keySet());
    List balances = new ArrayList(sorted.values());
    if (rank <= balances.size()) {
      if (balOrPlayer.equalsIgnoreCase("bal")) {
        return String.valueOf(balances.get(rank - 1));
      } else if (balOrPlayer.equals("player")) {
        return String.valueOf(players.get(rank - 1));
      }
    }
    if (!balOrPlayer.equals("bal") && !balOrPlayer.equals("player"))  {
        return String.valueOf(players.indexOf(balOrPlayer) + 1);
    }
    return "0";
  }
}