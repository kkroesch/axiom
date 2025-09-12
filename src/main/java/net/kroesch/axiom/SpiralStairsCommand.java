package net.kroesch.axiom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Stairs;
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

    private static final Material STAIRS_MATERIAL = Material.COBBLESTONE_STAIRS;
    private static final Material CORNER_MATERIAL = Material.COBBLESTONE_SLAB; // NEU: Material für die Ecken
    private static final Material AIR_MATERIAL = Material.AIR;

    // ... (onCommand und generateSpiralOffsets bleiben unverändert) ...
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

            player.sendMessage("§aBeginne mit dem Aushub für eine " + width + "x" + width + " Treppe mit massiven Ecken...");

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

        for (int x = w - 1; x >= 0; x--) offsets.add(new Vector(x, 0, -1));
        offsets.add(new Vector(-1, 0, -1));

        for (int z = 0; z < w; z++) offsets.add(new Vector(-1, 0, z));
        offsets.add(new Vector(-1, 0, w));

        for (int x = 0; x < w; x++) offsets.add(new Vector(x, 0, w));
        offsets.add(new Vector(w, 0, w));

        for (int z = w - 1; z >= 0; z--) offsets.add(new Vector(w, 0, z));
        offsets.add(new Vector(w, 0, -1));

        return offsets;
    }

    private void buildInTwoPhases(Location startLocation, int depth, int width) {
        final List<Vector> spiralOffsets = generateSpiralOffsets(width);

        // --- PHASE 1: AUSHUB (unverändert) ---
        Set<Vector> uniqueHorizontalOffsets = new HashSet<>(spiralOffsets);
        for (Vector horizontalOffset : uniqueHorizontalOffsets) {
            for (int y = 0; y < depth; y++) {
                Location locToClear = startLocation.clone().add(horizontalOffset).add(0, -y, 0);
                locToClear.getBlock().setType(AIR_MATERIAL);
            }
        }

        // --- PHASE 2: KONSTRUKTION (MIT MASSIVEN ECKEN) ---
        for (int step = 0; step < depth; step++) {
            int listSize = spiralOffsets.size();
            Vector currentOffset = spiralOffsets.get(step % listSize);
            Vector nextOffset = spiralOffsets.get((step + 1) % listSize);
            Vector prevOffset = spiralOffsets.get((step - 1 + listSize) % listSize);

            Vector deltaOut = nextOffset.clone().subtract(currentOffset);
            Vector deltaIn = currentOffset.clone().subtract(prevOffset);

            Location blockLocation = startLocation.clone().add(currentOffset).add(0, -step, 0);
            Block block = blockLocation.getBlock();

            // KORREKTUR: Prüfen, ob es eine Ecke ist.
            if (!deltaIn.equals(deltaOut)) {
                // In der Ecke einen massiven Block setzen
                block.setType(CORNER_MATERIAL);
            } else {
                // Ansonsten eine normale, gerade Treppenstufe setzen
                BlockFace facing = getFaceFromVector(deltaOut).getOppositeFace();

                block.setType(STAIRS_MATERIAL);
                Stairs stairData = (Stairs) block.getBlockData();

                stairData.setFacing(facing);
                stairData.setShape(Stairs.Shape.STRAIGHT); // Form ist jetzt immer gerade
                stairData.setHalf(Stairs.Half.BOTTOM);

                block.setBlockData(stairData);
            }
        }
    }

    private BlockFace getFaceFromVector(Vector v) {
        if (v.getX() > 0.1) return BlockFace.EAST;
        if (v.getX() < -0.1) return BlockFace.WEST;
        if (v.getZ() > 0.1) return BlockFace.SOUTH;
        if (v.getZ() < -0.1) return BlockFace.NORTH;
        return BlockFace.NORTH;
    }
}