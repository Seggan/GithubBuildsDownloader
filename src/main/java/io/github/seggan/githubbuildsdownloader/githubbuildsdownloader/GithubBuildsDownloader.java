package io.github.seggan.githubbuildsdownloader.githubbuildsdownloader;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class GithubBuildsDownloader extends JavaPlugin {

    @Override
    public void onEnable() {
        PluginCommand cmd = getCommand("download");
        if (cmd == null) {
            throw new IllegalStateException("Cannot get command");
        }
        cmd.setExecutor(new DownloadExecutor());
    }

    @Override
    public void onDisable() {
    }
}
