package at.helpch.placeholderapi.expansion.vault;

import com.google.common.collect.ImmutableMap;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class VaultExpansion extends PlaceholderExpansion implements Cacheable, Configurable {

    private EconomyHook economyHook;
    private PermissionHook permissionHook;

    @Override
    public @NotNull String getIdentifier() {
        return "vault";
    }

    @Override
    public @NotNull String getAuthor() {
        return "HelpChat";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.8.1";
    }

    @Override
    public @Nullable String getRequiredPlugin() {
        return "Vault";
    }

    @Override
    public void clear() {
        economyHook = null;
    }

    @Override
    public Map<String, Object> getDefaults() {
        return ImmutableMap.<String, Object>builder()
                .put("formatting.thousands", "k")
                .put("formatting.millions", "M")
                .put("formatting.billions", "B")
                .put("formatting.trillions", "T")
                .put("formatting.quadrillions", "Q")
                .build();
    }

    @Override
    public boolean register() {
        if (economyHook.setup()) {
            economyHook = new EconomyHook(this);
        }

        if (permissionHook.setup()) {
            permissionHook = new PermissionHook(this);
        }

        return economyHook != null || permissionHook != null;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (economyHook != null && params.startsWith("eco_")) {
            return economyHook.onRequest(player, params.replace("eco_", ""));
        }

        return (permissionHook != null) ? permissionHook.onRequest(player, params) : null;
    }

}
