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

import java.util.HashMap;
import java.util.Map;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class VaultExpansion extends PlaceholderExpansion implements Cacheable, Configurable {

  private VaultPermsHook perms = null;
  private VaultEcoHook eco = null;

  private final String VERSION = getClass().getPackage().getImplementationVersion();

  public VaultExpansion() {
    perms = new VaultPermsHook();
    eco = new VaultEcoHook(this);
  }

  @Override
  public void clear() {
    eco = null;
    perms = null;
  }

  @Override
  public boolean canRegister() {
    return Bukkit.getPluginManager().getPlugin(getPlugin()) != null;
  }

  @Override
  public Map<String, Object> getDefaults() {
    Map<String, Object> defaults = new HashMap<String, Object>();
    defaults.put("formatting.thousands", "k");
    defaults.put("formatting.millions", "M");
    defaults.put("formatting.billions", "B");
    defaults.put("formatting.trillions", "T");
    defaults.put("formatting.quadrillions", "Q");
    return defaults;
  }

  @Override
  public boolean register() {
    if (!eco.setup()) {
      eco = null;
    }
    if (!perms.setup()) {
      perms = null;
    }
    if (perms != null || eco != null) {
      return super.register();
    }
    return false;
  }

  @Override
  public String getAuthor() {
    return "clip";
  }

  @Override
  public String getIdentifier() {
    return "vault";
  }

  @Override
  public String getPlugin() {
    return "Vault";
  }

  @Override
  public String getVersion() {
    return VERSION;
  }

  @Override
  public String onRequest(OfflinePlayer p, String i) {
    if (i.startsWith("eco_") && eco != null) {
      return eco.onPlaceholderRequest(p, i.replace("eco_", ""));
    }
    if (perms != null) {
      return perms.onPlaceholderRequest(p, i);
    }
    return null;
  }
}
