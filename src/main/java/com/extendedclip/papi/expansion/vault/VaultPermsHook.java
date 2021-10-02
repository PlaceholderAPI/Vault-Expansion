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
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.regex.Pattern;

public class VaultPermsHook implements VaultHook {

    private static final Pattern RANK_PREFIX_PATTERN = Pattern.compile("rankprefix_");
    private static final Pattern RANK_SUFFIX_PATTERN = Pattern.compile("ranksuffix_");
    private static final Pattern GROUP_PREFIX_PATTERN = Pattern.compile("groupprefix_");
    private static final Pattern GROUP_SUFFIX_PATTERN = Pattern.compile("groupsuffix_");

    private static final Pattern HAS_GROUP_PATTERN = Pattern.compile("hasgroup_");
    private static final Pattern IN_PRIMARY_GROUP_PATTERN = Pattern.compile("inprimarygroup_");

    private Permission perms;
    private Chat chat;

    @Override
    public boolean setup() {
        final RegisteredServiceProvider<Permission> permsProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);

        if (permsProvider != null) {
            this.perms = permsProvider.getProvider();
        }

        final RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);

        if (chatProvider != null) {
            this.chat = chatProvider.getProvider();
        }

        return perms != null && chat != null;
    }

    @Override
    public String onPlaceholderRequest(OfflinePlayer p, String identifier) {
        if (p == null) {
            return "";
        }

        if (identifier.startsWith("rankprefix_")) {
            return getGroupPrefix(p, parseInt(RANK_PREFIX_PATTERN.split(identifier)[1], 1));
        }

        if (identifier.startsWith("ranksuffix_")) {
            return getGroupSuffix(p, parseInt(RANK_SUFFIX_PATTERN.split(identifier)[1], 1));
        }

        if (identifier.startsWith("groupprefix_")) {
            return getGroupPrefix(p, parseInt(GROUP_PREFIX_PATTERN.split(identifier)[1], 1));
        }

        if (identifier.startsWith("groupsuffix_")) {
            return getGroupSuffix(p, parseInt(GROUP_SUFFIX_PATTERN.split(identifier)[1], 1));
        }

        if (identifier.startsWith("hasgroup_")) {
            return bool(perms.playerInGroup(getWorldName(), p, HAS_GROUP_PATTERN.split(identifier)[1]));
        }

        if (identifier.startsWith("inprimarygroup_")) {
            return bool(getMainGroup(p).equals(IN_PRIMARY_GROUP_PATTERN.split(identifier)[1]));
        }

        switch (identifier) {
            case "group":
            case "rank":
                return getMainGroup(p);
            case "group_capital":
            case "rank_capital":
                return WordUtils.capitalize(getMainGroup(p).toLowerCase());
            case "groups":
            case "ranks":
                return String.join(", ", getGroups(p));
            case "groups_capital":
            case "ranks_capital":
                return WordUtils.capitalize(String.join(", ", getGroups(p)));
            case "prefix":
                return getPlayerPrefix(p);
            case "groupprefix":
            case "rankprefix":
                return getGroupPrefix(p);
            case "suffix":
                return getPlayerSuffix(p);
            case "groupsuffix":
            case "ranksuffix":
                return getGroupSuffix(p);
            case "prefix_color":
                return getLastColor(getGroupPrefix(p));
            case "suffix_color":
                return getLastColor(getGroupSuffix(p));
            case "user_prefix_color":
                return getLastColor(getPlayerPrefix(p));
            case "user_suffix_color":
                return getLastColor(getPlayerSuffix(p));
        }
        return null;
    }

    private String getWorldName() {
        return getMainWorld().getName();
    }

    private World getMainWorld() {
        return Bukkit.getWorlds().get(0);
    }

    private String[] getGroups(OfflinePlayer p) {
        final String[] groups = perms.getPlayerGroups(getWorldName(), p);
        return groups == null ? new String[]{} : groups;
    }

    private String getMainGroup(OfflinePlayer p) {
        final String group = perms.getPrimaryGroup(getWorldName(), p);
        return group == null ? "" : group;
    }

    public boolean hasPermission(OfflinePlayer p, String perm) {
        return perms.playerHas(getWorldName(), p, perm);
    }

    private String getPlayerPrefix(OfflinePlayer p) {
        final String prefix = chat.getPlayerPrefix(getWorldName(), p);
        return prefix == null ? "" : prefix;
    }

    private String getPlayerSuffix(OfflinePlayer p) {
        final String suffix = chat.getPlayerSuffix(getWorldName(), p);
        return suffix == null ? "" : suffix;
    }

    private String getGroupSuffix(OfflinePlayer p) {
        final String suffix = chat.getGroupSuffix(getMainWorld(), getMainGroup(p));
        return suffix == null? "" : suffix;
    }

    private String getGroupPrefix(OfflinePlayer p) {
        final String prefix = chat.getGroupPrefix(getMainWorld(), getMainGroup(p));
        return prefix == null ? "" : prefix;
    }

    private String getGroupSuffix(OfflinePlayer p, int n) {
        final String[] groups = getGroups(p);

        if (n > groups.length) {
            return "";
        }

        for (int i = n - 1; i < groups.length; i++) {
            final String suffix = chat.getGroupSuffix(getMainWorld(), groups[i]);

            if (suffix != null) {
                return suffix;
            }
        }

        return "";
    }

    private String getGroupPrefix(OfflinePlayer p, int n) {
        final String[] groups = getGroups(p);

        if (n > groups.length) {
            return "";
        }

        for (int i = n - 1; i < groups.length; i++) {
            final String prefix = chat.getGroupPrefix(getMainWorld(), groups[i]);

            if (prefix != null) {
                return prefix;
            }
        }

        return "";
    }

    private String getLastColor(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        int c = s.lastIndexOf(ChatColor.COLOR_CHAR);
        if (c == -1) {
            c = s.lastIndexOf("&");
            if (c == -1) {
                return "";
            }
        }
        String clr = s.substring(c);
        if (c - 2 >= 0) {
            if (s.charAt(c - 2) == ChatColor.COLOR_CHAR || s.charAt(c - 2) == '&') {
                clr = s.substring(c - 2);
            }
        }
        return clr;
    }

    private int parseInt(String string, int def) {
        final Integer integer = Ints.tryParse(string);
        return integer == null ? def : integer;
    }

    private String bool(boolean bool) {
        return bool ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }

    public boolean hasPerm(OfflinePlayer p, String perm) {
        if (perms != null) {
            return perms.playerHas(getWorldName(), p, perm);
        }
        return p.isOnline() && ((Player) p).hasPermission(perm);
    }

    public String[] getServerGroups() {
        final String[] groups = perms.getGroups();
        return groups == null ? new String[]{} : groups;
    }



}
