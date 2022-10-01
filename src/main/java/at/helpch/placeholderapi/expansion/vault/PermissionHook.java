package at.helpch.placeholderapi.expansion.vault;

import com.google.common.primitives.Ints;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PermissionHook extends VaultHook {

    private Permission permission;
    private Chat chat;

    public PermissionHook(VaultExpansion expansion) {
        super(expansion);
    }

    private @NotNull String[] getPlayerGroups(@NotNull final OfflinePlayer player) {
        final String[] groups = permission.getPlayerGroups(null, player);
        return (groups == null) ? new String[0] : groups;
    }

    private @NotNull String getPrimaryGroup(@NotNull final OfflinePlayer player) {
        return valueOrEmpty(permission.getPrimaryGroup(null, player));
    }

    private @NotNull String getGroupMeta(@NotNull final String group, final boolean prefix) {
        final String meta = prefix ? chat.getGroupPrefix((World) null, group) : chat.getGroupSuffix((World) null, group);
        return valueOrEmpty(meta);
    }

    private @NotNull String getPlayerMeta(@NotNull final OfflinePlayer player, final boolean prefix) {
        final String meta = prefix ? chat.getPlayerPrefix(null, player) : chat.getPlayerSuffix(null, player);
        return valueOrEmpty(meta);
    }

    private @NotNull String getGroupMeta(@NotNull final OfflinePlayer player, final int n, final boolean prefix) {
        final String[] groups = getPlayerGroups(player);

        if (n > groups.length) {
            return "";
        }

        for (int i = n - 1; i < groups.length; i++) {
            final String meta = prefix ? chat.getGroupPrefix((World) null, groups[i]) : chat.getGroupSuffix((World) null, groups[i]);

            if (meta != null) {
                return meta;
            }
        }

        return "";
    }

    private @NotNull String valueOrEmpty(@Nullable final String string) {
        return (string == null) ? "" : string;
    }

    private @NotNull String capitalize(@NotNull final String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase();
    }

    private @NotNull String bool(final boolean bool) {
        return bool ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }

    @Override
    public boolean setup() {
        permission = getService(Permission.class);
        chat = getService(Chat.class);
        return permission != null && chat != null;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if ((params.startsWith("rankprefix_") || params.startsWith("groupprefix_")) || (params.startsWith("ranksuffix_") || params.startsWith("groupsuffix_"))) {
            final String[] parts = params.split("_", 2);
            final Integer index = Ints.tryParse(parts[1]);

            if (index == null || index < 0) {
                return "Invalid number " + parts[1];
            }

            return getGroupMeta(player, index, parts[0].contains("prefix"));
        }

        if (params.equals("hasgroup_")) {
            final String group = params.replace("hasgroup_", "");
            return bool(permission.playerInGroup(null, player, group));
        }

        if (params.startsWith("inprimarygroup_")) {
            final String group = params.replace("inprimarygroup_", "");
            return bool(getPrimaryGroup(player).equals(group));
        }

        switch (params) {
            case "group":
            case "rank":
                return getPrimaryGroup(player);
            case "group_capital":
            case "rank_capital":
                return capitalize(getPrimaryGroup(player));

            case "groups":
            case "ranks":
                return String.join(", ", getPlayerGroups(player));
            case "groups_capital":
            case "ranks_capital":
                return Arrays.stream(getPlayerGroups(player))
                        .map(this::capitalize)
                        .collect(Collectors.joining(", "));

            case "prefix":
                return getPlayerMeta(player, true);
            case "suffix":
                return getPlayerMeta(player, false);

            case "groupprefix":
            case "rankprefix":
                return getGroupMeta(getPrimaryGroup(player), true);
            case "groupsuffix":
            case "ranksuffix":
                return getGroupMeta(getPrimaryGroup(player), false);

            default:
                return null;
        }
    }

}
