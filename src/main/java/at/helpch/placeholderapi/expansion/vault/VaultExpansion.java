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
        return "1.8.3";
    }

    @Override
    public @Nullable String getRequiredPlugin() {
        return "Vault";
    }

    @Override
    public void clear() {
        economyHook = null;
        permissionHook = null;
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
    public boolean canRegister() {
        economyHook = new EconomyHook(this);
        permissionHook = new PermissionHook(this);
        return economyHook.isReady() || permissionHook.isReady();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (economyHook.isReady() && params.startsWith("eco_")) {
            return economyHook.onRequest(player, params.replace("eco_", ""));
        }

        return (permissionHook.isReady()) ? permissionHook.onRequest(player, params) : null;
    }

}
