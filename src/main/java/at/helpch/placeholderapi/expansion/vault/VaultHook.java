package at.helpch.placeholderapi.expansion.vault;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class VaultHook {

    protected final VaultExpansion expansion;

    public VaultHook(VaultExpansion expansion) {
        this.expansion = expansion;
    }

    protected final <T> @Nullable T getService(final Class<T> cls) {
        return Optional.ofNullable(Bukkit.getServer().getServicesManager().getRegistration(cls))
            .map(RegisteredServiceProvider::getProvider)
            .orElse(null);
    }

    protected abstract void setup();

    public abstract boolean isReady();

    public abstract @Nullable String onRequest(@Nullable OfflinePlayer offlinePlayer, @NotNull String params);

}
