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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpiralStairsCommand implements CommandExecutor {

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

        if (args.length < 1 || args.length > 2) {
            return false;
        }

        try {
            int depth = Integer.parseInt(args[0]);
            int width = 2;
            if (args.length == 2) {
                width = Integer.parseInt(args[1]);
            }

            if (depth <= 0 || width <= 0) {
                player.sendMessage("§cTiefe und Breite müssen grösser als 0 sein.");
                return true;
            }

            Location startLocation = player.getLocation().getBlock().getLocation();

            player.sendMessage("§aBeginne mit dem Aushub für eine " + width + "x" + width + " Treppe...");

            buildInTwoPhases(startLocation, depth, width);

            player.sendMessage("§aTreppe erfolgreich gebaut!");

        } catch (NumberFormatException e) {
            player.sendMessage("§cFehler: Bitte gib gültige Zahlen für Tiefe und Breite an.");
            return false;
        }

        return true;
    }

    private List<Vector> generateSpiralOffsets(int width) {
        List<Vector> offsets = new ArrayList<>();
        int w = width;

        // Nord-Seite
        for (int x = w - 1; x >= 0; x--) offsets.add(new Vector(x, 0, -1));
        offsets.add(new Vector(-1, 0, -1)); // Nord-West-Ecke

        // West-Seite
        for (int z = 0; z < w; z++) offsets.add(new Vector(-1, 0, z));
        offsets.add(new Vector(-1, 0, w)); // Süd-West-Ecke

        // Süd-Seite
        for (int x = 0; x < w; x++) offsets.add(new Vector(x, 0, w));
        offsets.add(new Vector(w, 0, w)); // Süd-Ost-Ecke

        // Ost-Seite
        for (int z = w - 1; z >= 0; z--) offsets.add(new Vector(w, 0, z));
        offsets.add(new Vector(w, 0, -1)); // Nord-Ost-Ecke

        return offsets;
    }

    /**
     * Führt den Bau in zwei getrennten Phasen aus: Aushub und Konstruktion.
     */
    private void buildInTwoPhases(Location startLocation, int depth, int width) {
        World world = startLocation.getWorld();
        final List<Vector> spiralOffsets = generateSpiralOffsets(width);

        // --- PHASE 1: AUSHUB ---
        // Wir holen uns alle einzigartigen X/Z Positionen der Treppe, um jede Säule nur einmal auszuheben.
        Set<Vector> uniqueHorizontalOffsets = new HashSet<>(spiralOffsets);

        for (Vector horizontalOffset : uniqueHorizontalOffsets) {
            for (int y = 0; y < depth; y++) {
                // Berechne die absolute Position und setze den Block auf Luft
                Location locToClear = startLocation.clone().add(horizontalOffset).add(0, -y, 0);
                locToClear.getBlock().setType(AIR_MATERIAL);
            }
        }

        // --- PHASE 2: KONSTRUKTION (VON UNTEN NACH OBEN) ---
        // Die Schleife läuft rückwärts, von der tiefsten Stufe zur höchsten.
        for (int step = depth - 1; step >= 0; step--) {
            int stepIndex = step % spiralOffsets.size();
            Vector relativePos = spiralOffsets.get(stepIndex);

            int yOffset = -step;

            Location blockLocation = startLocation.clone().add(relativePos).add(0, yOffset, 0);
            blockLocation.getBlock().setType(STAIR_BLOCK_MATERIAL);
        }
    }
}