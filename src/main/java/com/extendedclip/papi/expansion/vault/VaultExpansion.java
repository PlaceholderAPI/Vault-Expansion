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

import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class VaultExpansion extends PlaceholderExpansion implements Cacheable, Configurable {

    private VaultPermsHook perms;
    private VaultEcoHook eco;

    public VaultExpansion() {
        perms = new VaultPermsHook();
        eco = new VaultEcoHook(this, perms);
    }

    @Override
    public void clear() {
        if (eco != null) {
            eco.clear();
        }
        eco = null;
        perms = null;
    }

    @Override
    public Map<String, Object> getDefaults() {
        final Map<String, Object> defaults = new HashMap<>();
        defaults.put("baltop.enabled", false);
        defaults.put("baltop.cache_size", 100);
        defaults.put("baltop.check_delay", 30);
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
    public @NotNull String getAuthor() {
        return "clip";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "vault";
    }

    @Override
    public @Nullable String getRequiredPlugin() {
        return "Vault";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.7.1";
    }

    @Override
    public String onRequest(OfflinePlayer p, @NotNull String i) {
        if (eco != null && i.startsWith("eco_")) {
            return eco.onPlaceholderRequest(p, i.replace("eco_", ""));
        }

        if (perms != null) {
            return perms.onPlaceholderRequest(p, i);
        }

        return null;
    }
}
