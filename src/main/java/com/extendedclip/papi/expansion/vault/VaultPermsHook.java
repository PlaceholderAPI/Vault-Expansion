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

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultPermsHook implements VaultHook {

	private Permission perms = null;
	private Chat chat = null;

	@Override
	public boolean setup() {
		RegisteredServiceProvider<Permission> permsProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
		if (permsProvider != null && permsProvider.getPlugin() != null) {
			perms = permsProvider.getProvider();
		}
		RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
		if (chatProvider != null && chatProvider.getPlugin() != null) {
			chat = chatProvider.getProvider();
		}
		return perms != null && chat != null;
	}

	@Override
	public String onPlaceholderRequest(Player p, String identifier) {
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
	}
	else if(identifier.startsWith("hasgroup_")){
		return perms.playerInGroup(p, identifier.split("hasgroup_")[1]) ? "true" : "false";
	}
	else if(identifier.startsWith("inprimarygroup_")){
		return perms.getPrimaryGroup(p).equals(identifier.split("inprimarygroup_")[1]) ? "true" : "false";
	}
	switch (identifier) {
	case "group":
	case "rank":
		return getMainGroup(p) != null ? getMainGroup(p) : "";
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

	public String[] getGroups(Player p) {
		if (perms.getPlayerGroups(p) != null) {
			return perms.getPlayerGroups(p);
		}
		return new String[] { "" };
	}

	public String getMainGroup(Player p) {
		if (perms.getPrimaryGroup(p) != null) {
			return String.valueOf(perms.getPrimaryGroup(p));
		}
		return "";
	}

	public boolean opHasPermission(Player p, String perm) {
		if (perms.getPrimaryGroup(p) != null) {

			return perms.groupHas(p.getWorld(), perms.getPrimaryGroup(p), perm);
		}
		return false;
	}

	public boolean addPerm(Player p, String perm) {
		return perms.playerAdd(p, perm);
	}

	public String getPlayerPrefix(Player p) {
		if (chat.getPlayerPrefix(p) != null) {
			return String.valueOf(chat.getPlayerPrefix(p));
		}
		return "";
	}

	public String getPlayerSuffix(Player p) {
		if (chat.getPlayerSuffix(p) != null) {
			return String.valueOf(chat.getPlayerSuffix(p));
		}
		return "";
	}
	
	public String getGroupSuffix(Player p) {
		if (perms.getPrimaryGroup(p) == null) {
			return "";
		}
		
		if (chat.getGroupSuffix(p.getWorld(), perms.getPrimaryGroup(p)) != null) {
			return String.valueOf(chat.getGroupSuffix(p.getWorld(), perms.getPrimaryGroup(p)));
		}
		return "";
	}
	
	public String getGroupPrefix(Player p) {
		if (perms.getPrimaryGroup(p) == null) {
			return "";
		}
		if (chat.getGroupPrefix(p.getWorld(), perms.getPrimaryGroup(p)) != null) {
			return String.valueOf(chat.getGroupPrefix(p.getWorld(), perms.getPrimaryGroup(p)));
		}
		return "";
	}
	
	public String getGroupSuffix(Player p, int i) {
		if (perms.getPlayerGroups(p) == null) {
			return "";
		}
		String[] groups = perms.getPlayerGroups(p);
		if (i > groups.length) {
			return "";
		}
		int count = 1;
		for (String group : groups) {
			if (count < i) {
				count++;
				continue;
			}
			if (chat.getGroupSuffix(p.getWorld(), group) != null) {
				return String.valueOf(chat.getGroupSuffix(p.getWorld(), group));
			}
		}
		return "";
	}
	
	public String getGroupPrefix(Player p, int i) {
		if (perms.getPlayerGroups(p) == null) {
			return "";
		}
		String[] groups = perms.getPlayerGroups(p);
		if (i > groups.length) {
			return "";
		}
		int count = 1;
		for (String group : groups) {
			if (count < i) {
				count++;
				continue;
			}
			if (chat.getGroupPrefix(p.getWorld(), group) != null) {
				return String.valueOf(chat.getGroupPrefix(p.getWorld(), group));
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
		if (c-2 >= 0) {
			if (s.charAt(c-2) == ChatColor.COLOR_CHAR || s.charAt(c-2) == '&') {
				clr = s.substring(c-2);
			}
		}
		return clr;
	}

	public boolean hasPerm(Player p, String perm) {
		if (perms != null) {
			return perms.has(p, perm);
		}
		return p.hasPermission(perm);
	}

	public String[] getServerGroups() {
		if (perms.getGroups() != null) {
			return perms.getGroups();
		}
		return new String[] { "" };
	}
}
