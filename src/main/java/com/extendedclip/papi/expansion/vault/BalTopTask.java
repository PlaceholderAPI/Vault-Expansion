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

import static java.util.stream.Collectors.toMap;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

class BalTopTask extends BukkitRunnable {

  private final VaultEcoHook eco;

  public BalTopTask(VaultEcoHook eco) {
    this.eco = eco;
  }

  @Override
  public void run() {
    Map<String, Double> top = new LinkedHashMap<>();

    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
      if (player == null || player.getName() == null) {
        continue;
      }
      top.put(player.getName(), eco.getBalance(player));
    }

    eco.setBalTop(
        top.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (player, balance) -> balance,
                LinkedHashMap::new)));
  }
}
