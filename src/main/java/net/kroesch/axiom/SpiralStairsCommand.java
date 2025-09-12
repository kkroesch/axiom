package net.kroesch.axiom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SpiralStairsCommand implements CommandExecutor {

    // Das Material für die Treppenblöcke. Kann beliebig geändert werden.
    private static final Material STAIR_BLOCK_MATERIAL = Material.COBBLESTONE;
    private static final Material AIR_MATERIAL = Material.AIR;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("axiom.command.spiralstair")) {
            player.sendMessage("§cDu hast keine Berechtigung, diesen Befehl zu verwenden.");
            return true;
        }

        if (args.length != 1) {
            return false;
        }

        try {
            int depth = Integer.parseInt(args[0]);
            if (depth <= 0) {
                player.sendMessage("§cDie Tiefe muss grösser als 0 sein.");
                return true;
            }

            // Startpunkt ist der Block, auf dem der Spieler steht.
            Location startLocation = player.getLocation().getBlock().getLocation();

            player.sendMessage("§aGrabe einen " + depth + " Blöcke tiefen Schacht mit Block-Treppe...");

            // Rufe die neue Methode zum Bauen auf.
            buildShaftWithBlockStairs(startLocation, depth);

            player.sendMessage("§aSchacht und Treppe erfolgreich gebaut!");

        } catch (NumberFormatException e) {
            player.sendMessage("§cFehler: Bitte gib eine gültige Zahl für die Tiefe an.");
            return false;
        }

        return true;
    }

    /**
     * Baut einen 2x2 Schacht und eine darumliegende, linksläufige Treppe aus einzelnen Blöcken.
     * @param startLocation Der Startpunkt (oberste Ebene).
     * @param depth Die zu grabende Tiefe in Blöcken.
     */
    private void buildShaftWithBlockStairs(Location startLocation, int depth) {
        World world = startLocation.getWorld();

        // Definiert die 8 relativen Positionen der Blöcke für eine volle 360°-Drehung
        // gegen den Uhrzeigersinn ("linksrum").
        final List<Vector> counterClockwiseSpiral = Arrays.asList(
                new Vector(1, 0, -1),  // Nord-Ost
                new Vector(0, 0, -1),  // Nord-West
                new Vector(-1, 0, 0),  // West-Nord
                new Vector(-1, 0, 1),  // West-Süd
                new Vector(0, 0, 2),   // Süd-West
                new Vector(1, 0, 2),   // Süd-Ost
                new Vector(2, 0, 1),   // Ost-Süd
                new Vector(2, 0, 0)    // Ost-Nord
        );

        // 1. Zuerst den gesamten 4x4-Bereich leeren, um den Schacht und den Luftraum zu schaffen.
        // Der Kern ist bei (0,0) bis (1,1), die Treppe läuft aussenrum bei (-1,-1) bis (2,2).
        for (int yOffset = 0; yOffset < depth; yOffset++) {
            for (int x = -1; x <= 2; x++) {
                for (int z = -1; z <= 2; z++) {
                    startLocation.clone().add(x, -yOffset, z).getBlock().setType(AIR_MATERIAL);
                }
            }
        }

        // 2. Jetzt die Treppenblöcke Schicht für Schicht platzieren.
        for (int yOffset = 0; yOffset < depth; yOffset++) {
            // Finde die richtige Position in der 8-Schritte-Spirale
            int stepIndex = yOffset % 8;
            Vector relativePos = counterClockwiseSpiral.get(stepIndex);

            // Berechne die absolute Position des Blocks für die aktuelle Tiefe
            Location blockLocation = startLocation.clone().add(relativePos.getX(), -yOffset, relativePos.getZ());

            // Setze den Block
            blockLocation.getBlock().setType(STAIR_BLOCK_MATERIAL);
        }
    }
}