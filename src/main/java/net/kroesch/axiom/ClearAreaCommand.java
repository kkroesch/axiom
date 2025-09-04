package net.kroesch.axiom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClearAreaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // 1. Prüfen, ob der Befehl von einem Spieler ausgeführt wird
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        // 2. Prüfen, ob die richtige Anzahl an Argumenten (Länge, Breite) angegeben wurde
        if (args.length != 2) {
            player.sendMessage("§cFehler: Falsche Verwendung. Bitte nutze: /cleararea <länge> <breite>");
            return false; // 'false' zurückgeben, um die 'usage' Nachricht aus der plugin.yml anzuzeigen
        }

        try {
            // 3. Argumente in Zahlen umwandeln
            int length = Integer.parseInt(args[0]); // x-Achse
            int width = Integer.parseInt(args[1]);  // z-Achse
            int height = 10; // Feste Höhe

            // 4. Spieler-Position als Zentrum holen
            Location center = player.getLocation();
            World world = center.getWorld();

            // 5. Eckpunkte des zu löschenden Quaders berechnen
            int startX = center.getBlockX() - (length / 2);
            int endX = center.getBlockX() + (length / 2);

            int startZ = center.getBlockZ() - (width / 2);
            int endZ = center.getBlockZ() + (width / 2);

            // Wir löschen von der Höhe des Spielers 10 Blöcke nach oben
            int startY = center.getBlockY();
            int endY = center.getBlockY() + height;

            player.sendMessage("§aRäume Bereich von " + length + "x" + width + "x" + height + " Blöcken...");

            // 6. Durch jeden Block im definierten Bereich iterieren
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y < endY; y++) {
                    for (int z = startZ; z <= endZ; z++) {
                        Block currentBlock = world.getBlockAt(x, y, z);

                        // Den Block nur entfernen, wenn er nicht bereits Luft ist
                        if (currentBlock.getType() != Material.AIR) {
                            currentBlock.setType(Material.AIR);
                        }
                    }
                }
            }

            player.sendMessage("§aBereich erfolgreich geräumt!");

        } catch (NumberFormatException e) {
            player.sendMessage("§cFehler: Bitte gib gültige Zahlen für Länge und Breite an.");
            return false;
        }

        return true;
    }
}