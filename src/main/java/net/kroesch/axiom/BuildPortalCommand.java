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

public class BuildPortalCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("axiom.command.buildportal")) {
            player.sendMessage("§cDu hast keine Berechtigung, diesen Befehl zu verwenden.");
            return true;
        }

        Location startLocation = player.getLocation().getBlock().getLocation();
        World world = startLocation.getWorld();

        // Finde die Blickrichtung des Spielers heraus, um das Portal korrekt auszurichten.
        float yaw = player.getLocation().getYaw();
        boolean facingNorthSouth = (yaw >= -45 && yaw < 45) || (yaw >= 135 || yaw < -135);

        // Baue den 4x5 Obsidian-Rahmen
        if (facingNorthSouth) {
            // Portal wird in Ost-West-Richtung gebaut (breit entlang der X-Achse)
            for (int x = 0; x < 4; x++) {
                // Boden
                world.getBlockAt(startLocation.clone().add(x - 1, 0, 0)).setType(Material.OBSIDIAN);
                // Decke
                world.getBlockAt(startLocation.clone().add(x - 1, 4, 0)).setType(Material.OBSIDIAN);
            }
            for (int y = 1; y < 4; y++) {
                // Seiten
                world.getBlockAt(startLocation.clone().add(-1, y, 0)).setType(Material.OBSIDIAN);
                world.getBlockAt(startLocation.clone().add(2, y, 0)).setType(Material.OBSIDIAN);
            }
            // Zünde das Portal an
            world.getBlockAt(startLocation.clone().add(0, 1, 0)).setType(Material.FIRE);

        } else {
            // Portal wird in Nord-Süd-Richtung gebaut (breit entlang der Z-Achse)
            for (int z = 0; z < 4; z++) {
                // Boden
                world.getBlockAt(startLocation.clone().add(0, 0, z - 1)).setType(Material.OBSIDIAN);
                // Decke
                world.getBlockAt(startLocation.clone().add(0, 4, z - 1)).setType(Material.OBSIDIAN);
            }
            for (int y = 1; y < 4; y++) {
                // Seiten
                world.getBlockAt(startLocation.clone().add(0, y, -1)).setType(Material.OBSIDIAN);
                world.getBlockAt(startLocation.clone().add(0, y, 2)).setType(Material.OBSIDIAN);
            }
            // Zünde das Portal an
            world.getBlockAt(startLocation.clone().add(0, 1, 0)).setType(Material.FIRE);
        }

        player.sendMessage("§aNether-Portal wurde erfolgreich gebaut!");
        return true;
    }
}