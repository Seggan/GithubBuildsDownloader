package io.github.seggan.githubbuildsdownloader.githubbuildsdownloader;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class DownloadExecutor implements CommandExecutor {

    private static Optional<URL> checkURL(String s) {
        try {
            URL url = new URL(s);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("HEAD");
            huc.setInstanceFollowRedirects(true);
            huc.connect();
            if (Integer.toString(huc.getResponseCode()).startsWith("2")) {
                return Optional.of(url);
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2 || args.length > 4) return false;

        String author = args[0];
        String repo = args[1];
        String branch = args.length < 3 ? "master" : args[2];
        int version;
        try {
            version = args.length < 4 ? 0 : Integer.parseInt(args[3]);
            if (version < 0) {
                sender.sendMessage(ChatColor.RED + "Cannot obtain a negative version");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Could not obtain version");
            return true;
        }

        sender.sendMessage("Resolving plugin...");
        if (!checkURL(String.format("https://thebusybiscuit.github.io/builds/%s/%s/%s", author, repo, branch)).isPresent()) {
            sender.sendMessage(ChatColor.RED + "Could not find the specified plugin");
            return true;
        }

        Optional<URL> url;
        if (version == 0) {
            do {
                version++;
                url = checkURL(String.format(
                    "https://thebusybiscuit.github.io/builds/%s/%s/%s/%s-%d.jar",
                    author,
                    repo,
                    branch,
                    repo,
                    version
                ));
            } while (url.isPresent());

            version--;
        }
        url = checkURL(String.format(
            "https://thebusybiscuit.github.io/builds/%s/%s/%s/%s-%d.jar",
            author,
            repo,
            branch,
            repo,
            version
        ));

        if (!url.isPresent()) {
            sender.sendMessage(ChatColor.RED + "Could not find the specified plugin");
            return true;
        }

        sender.sendMessage(String.format("Downloading %s version %d", repo, version));
        try (BufferedInputStream inputStream = new BufferedInputStream(url.get().openStream())) {
            FileOutputStream outputStream = new FileOutputStream("plugins/" + repo + " - v" + version + ".jar");
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(dataBuffer, 0, 1024)) != -1) {
                outputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        sender.sendMessage("Finished");
        return true;
    }
}
