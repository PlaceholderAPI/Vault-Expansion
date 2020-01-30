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

public class VaultPermsHook implements VaultHook {

  private Permission perms = null;
  private Chat chat = null;

  @Override
  public boolean setup() {
    RegisteredServiceProvider<Permission> permsProvider = Bukkit.getServer().getServicesManager()
        .getRegistration(Permission.class);
    if (permsProvider != null && permsProvider.getPlugin() != null) {
      perms = permsProvider.getProvider();
    }
    RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager()
        .getRegistration(Chat.class);
    if (chatProvider != null && chatProvider.getPlugin() != null) {
      chat = chatProvider.getProvider();
    }
    return perms != null && chat != null;
  }

  @Override
  public String onPlaceholderRequest(OfflinePlayer p, String identifier) {
    if (identifier.startsWith("rankprefix_")) {
      int i = 1;
      try {
        i = Integer.parseInt(identifier.split("rankprefix_")[1]);
      } catch (Exception e) {
        //uh oh
      }
      return getGroupPrefix(p, i);
    } else if (identifier.startsWith("ranksuffix_")) {
      int i = 1;
      try {
        i = Integer.parseInt(identifier.split("ranksuffix_")[1]);
      } catch (NumberFormatException e) {
        // uh fucking oh...
      }
      return getGroupSuffix(p, i);
    } else if (identifier.startsWith("groupprefix_")) {
      int i = 1;
      try {
        i = Integer.parseInt(identifier.split("groupprefix_")[1]);
      } catch (NumberFormatException e) {
        //  uh fucking FUCKING oh.
      }
      return getGroupPrefix(p, i);
    } else if (identifier.startsWith("groupsuffix_")) {
      int i = 1;
      try {
        i = Integer.parseInt(identifier.split("groupsuffix_")[1]);
      } catch (NumberFormatException e) {
        //easter egg
        return "provide a number dipshit";
      }
      return getGroupSuffix(p, i);
    } else if (identifier.startsWith("hasgroup_")) {

      return perms.playerInGroup(getWorldName(), p, identifier.split("hasgroup_")[1])
          ? PlaceholderAPIPlugin
          .booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    } else if (identifier.startsWith("inprimarygroup_")) {
      return getMainGroup(p).equals(identifier.split("inprimarygroup_")[1])
          ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }
    switch (identifier) {
      case "group":
      case "rank":
        return getMainGroup(p) != null ? getMainGroup(p) : "";
      case "group_capital":
      case "rank_capital":
        return getMainGroup(p) != null ? WordUtils.capitalize(getMainGroup(p).toLowerCase()) : "";
      case "groups":
      case "ranks":
        return String.join(", ", getGroups(p));
      case "groups_capital":
      case "ranks_capital":
        return WordUtils.capitalize(String.join(", ", getGroups(p)));
      case "prefix":
        return getPlayerPrefix(p) != null ? getPlayerPrefix(p) : "";
      case "groupprefix":
      case "rankprefix":
        return getGroupPrefix(p) != null ? getGroupPrefix(p) : "";
      case "suffix":
        return getPlayerSuffix(p) != null ? getPlayerSuffix(p) : "";
      case "groupsuffix":
      case "ranksuffix":
        return getGroupSuffix(p) != null ? getGroupSuffix(p) : "";
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

  public String getWorldName() {
    return getMainWorld().getName();
  }

  public World getMainWorld() {
    return Bukkit.getWorlds().get(0);
  }

  public String[] getGroups(OfflinePlayer p) {
    if (perms.getPlayerGroups(getWorldName(), p) != null) {
      return perms.getPlayerGroups(Bukkit.getWorlds().get(0).getName(), p);
    }
    return new String[]{""};
  }

  public String getMainGroup(OfflinePlayer p) {
    if (perms.getPrimaryGroup(getWorldName(), p) != null) {
      return perms.getPrimaryGroup(getWorldName(), p);
    }
    return "";
  }

  public boolean hasPermission(OfflinePlayer p, String perm) {
    return perms.playerHas(getWorldName(), p, perm);
  }

  public String getPlayerPrefix(OfflinePlayer p) {
    if (chat.getPlayerPrefix(getWorldName(), p) != null) {
      return chat.getPlayerPrefix(getWorldName(), p);
    }
    return "";
  }

  public String getPlayerSuffix(OfflinePlayer p) {
    if (chat.getPlayerSuffix(getWorldName(), p) != null) {
      return chat.getPlayerSuffix(getWorldName(), p);
    }
    return "";
  }

  public String getGroupSuffix(OfflinePlayer p) {
    if (perms.getPrimaryGroup(getWorldName(), p) == null) {
      return "";
    }
    String suffix = chat.getGroupSuffix(getMainWorld(), getMainGroup(p));
    if (suffix != null) {
      return suffix;
    }
    return "";
  }

  public String getGroupPrefix(OfflinePlayer p) {
    if (perms.getPrimaryGroup(getWorldName(), p) == null) {
      return "";
    }
    String prefix = chat.getGroupPrefix(getMainWorld(), getMainGroup(p));
    if (prefix != null) {
      return prefix;
    }
    return "";
  }

  public String getGroupSuffix(OfflinePlayer p, int i) {
    String[] groups = getGroups(p);
    if (i > groups.length) {
      return "";
    }
    int count = 1;
    for (String group : groups) {
      if (count < i) {
        count++;
        continue;
      }
      if (chat.getGroupSuffix(getMainWorld(), group) != null) {
        return chat.getGroupSuffix(getWorldName(), group);
      }
    }
    return "";
  }

  public String getGroupPrefix(OfflinePlayer p, int i) {
    String[] groups = getGroups(p);
    if (i > groups.length) {
      return "";
    }
    int count = 1;
    for (String group : groups) {
      if (count < i) {
        count++;
        continue;
      }
      if (chat.getGroupPrefix(getMainWorld(), group) != null) {
        return String.valueOf(chat.getGroupPrefix(getMainWorld(), group));
      }
    }
    return "";
  }

  public String getLastColor(String s) {
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

  public boolean hasPerm(OfflinePlayer p, String perm) {
    if (perms != null) {
      return perms.playerHas(getWorldName(), p, perm);
    }
    return p.isOnline() ? ((Player) p).hasPermission(perm) : false;
  }

  public String[] getServerGroups() {
    if (perms.getGroups() != null) {
      return perms.getGroups();
    }
    return new String[]{""};
  }
}
