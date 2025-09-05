package net.kroesch.axiom;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EchoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command, @NotNull String label,
                             @NotNull String[] args)
    {
        // Prüfen, ob der Absender ein Spieler ist
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage("§aHallo, " + player.getName() + "!");
        } else {
            // Wenn der Befehl von der Konsole kommt
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
        }

        // 'true' zurückgeben, wenn der Befehl erfolgreich war
        return true;
    }
}