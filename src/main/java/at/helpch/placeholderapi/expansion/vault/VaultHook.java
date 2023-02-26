package at.helpch.placeholderapi.expansion.vault;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class VaultHook {

    protected final VaultExpansion expansion;

    public VaultHook(VaultExpansion expansion) {
        this.expansion = expansion;
    }

    protected final <T> T getService(final Class<T> cls) {
        final RegisteredServiceProvider<T> rsp = Bukkit.getServer().getServicesManager().getRegistration(cls);

        if (rsp == null) {
            return null;
        }

        return rsp.getProvider();
    }

    protected abstract void setup();

    public abstract boolean isReady();

    public abstract @Nullable String onRequest(@Nullable OfflinePlayer offlinePlayer, @NotNull String params);

}
