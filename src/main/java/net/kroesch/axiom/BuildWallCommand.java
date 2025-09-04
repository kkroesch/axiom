package net.kroesch.axiom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BuildWallCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        // 1. Prüfen, ob mindestens 3 Argumente (Länge, Breite, Höhe) vorhanden sind
        if (args.length < 3 || args.length > 4) {
            return false; // Zeigt die korrekte 'usage' aus der plugin.yml
        }

        try {
            // 2. Argumente in Zahlen umwandeln
            int length = Integer.parseInt(args[0]);
            int width = Integer.parseInt(args[1]);
            int height = Integer.parseInt(args[2]);
            int wallThickness = 2; // Feste Wandstärke

            // 3. Material bestimmen (optional, default: COBBLESTONE)
            Material wallMaterial = Material.COBBLESTONE;
            if (args.length == 4) {
                // Versuche, das Material aus dem Argument zu finden
                Material specifiedMaterial = Material.matchMaterial(args[3].toUpperCase());
                if (specifiedMaterial != null && specifiedMaterial.isBlock()) {
                    wallMaterial = specifiedMaterial;
                } else {
                    player.sendMessage("§cFehler: Ungültiges Material '" + args[3] + "'. Verwende Bruchstein.");
                }
            }

            // 4. Eckpunkte des äußeren Rechtecks berechnen
            Location center = player.getLocation();
            World world = center.getWorld();
            int startX = center.getBlockX() - (length / 2);
            int endX = center.getBlockX() + (length / 2);
            int startZ = center.getBlockZ() - (width / 2);
            int endZ = center.getBlockZ() + (width / 2);
            int startY = center.getBlockY();
            int endY = startY + height;

            player.sendMessage("§aBaue Mauern mit Material " + wallMaterial.name() + "...");

            // 5. Durch den gesamten Quader iterieren und nur die Ränder setzen
            for (int y = startY; y < endY; y++) {
                for (int x = startX; x <= endX; x++) {
                    for (int z = startZ; z <= endZ; z++) {
                        // Prüfen, ob sich der Block am Rand befindet
                        if (x < startX + wallThickness || x > endX - wallThickness || z < startZ + wallThickness || z > endZ - wallThickness) {
                            world.getBlockAt(x, y, z).setType(wallMaterial);
                        }
                    }
                }
            }

            player.sendMessage("§aMauern erfolgreich errichtet!");

        } catch (NumberFormatException e) {
            player.sendMessage("§cFehler: Bitte gib gültige Zahlen für Länge, Breite und Höhe an.");
            return false;
        }

        return true;
    }
}