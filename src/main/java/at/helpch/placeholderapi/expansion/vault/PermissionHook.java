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
import java.util.Optional;
import java.util.stream.Collectors;

public class PermissionHook extends VaultHook {

    private Permission permission;
    private Chat chat;

    public PermissionHook(VaultExpansion expansion) {
        super(expansion);
        setup();
    }

    private @NotNull String[] getPlayerGroups(@NotNull final OfflinePlayer player) {
        final String[] groups = permission.getPlayerGroups(null, player);
        return (groups == null) ? new String[0] : groups;
    }

    private @NotNull Optional<String> getPrimaryGroup(@NotNull final OfflinePlayer player) {
        return Optional.ofNullable(permission.getPrimaryGroup(null, player));
    }

    private @NotNull Optional<String> getGroupMeta(@NotNull final String group, final boolean isPrefix) {
        // No need to look up the meta if the group doesn't exist
        if (group.isEmpty()) {
            return Optional.empty();
        }

        final String meta = isPrefix ? chat.getGroupPrefix((World) null, group) : chat.getGroupSuffix((World) null, group);
        return Optional.ofNullable(meta);
    }

    private @NotNull Optional<String> getPlayerMeta(@NotNull final OfflinePlayer player, final boolean isPrefix) {
        final String meta = isPrefix ? chat.getPlayerPrefix(null, player) : chat.getPlayerSuffix(null, player);
        return Optional.ofNullable(meta);
    }

    private @NotNull String getGroupMeta(@NotNull final OfflinePlayer player, final int startIndex, final boolean isPrefix) {
        final String[] groups = getPlayerGroups(player);

        if (startIndex > groups.length) {
            return "";
        }

        for (int i = startIndex - 1; i < groups.length; i++) {
            final Optional<String> meta = getGroupMeta(groups[i], isPrefix);

            if (meta.isPresent()) {
                return meta.get();
            }
        }

        return "";
    }

    private @NotNull String capitalize(@NotNull final String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase();
    }

    private @NotNull String bool(final boolean bool) {
        return bool ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }

    @Override
    public void setup() {
        permission = getService(Permission.class);
        chat = getService(Chat.class);
    }

    @Override
    public boolean isReady() {
        return permission != null && chat != null;
    }

    @SuppressWarnings({"SpellCheckingInspection", "UnstableApiUsage"})
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

        if (params.startsWith("hasgroup_")) {
            final String group = params.substring("hasgroup_".length());
            return bool(permission.playerInGroup(null, player, group));
        }

        if (params.startsWith("inprimarygroup_")) {
            final String group = params.substring("inprimarygroup_".length());
            return getPrimaryGroup(player)
                .map(group::equals)
                .map(this::bool)
                .orElseGet(() -> bool(false));
        }

        switch (params) {
            case "group":
            case "rank":
                return getPrimaryGroup(player).orElse("");
            case "group_capital":
            case "rank_capital":
                return getPrimaryGroup(player)
                    .map(this::capitalize)
                    .orElse("");

            case "groups":
            case "ranks":
                return String.join(", ", getPlayerGroups(player));
            case "groups_capital":
            case "ranks_capital":
                return Arrays.stream(getPlayerGroups(player))
                    .map(this::capitalize)
                    .collect(Collectors.joining(", "));

            case "prefix":
                return getPlayerMeta(player, true).orElse("");
            case "suffix":
                return getPlayerMeta(player, false).orElse("");

            case "groupprefix":
            case "rankprefix":
                return getPrimaryGroup(player)
                    .map(primaryGroup -> getGroupMeta(primaryGroup, true).orElse(""))
                    .orElse("");
            case "groupsuffix":
            case "ranksuffix":
                return getPrimaryGroup(player)
                    .map(primaryGroup -> getGroupMeta(primaryGroup, false).orElse(""))
                    .orElse("");

            default:
                return null;
        }
    }

}
