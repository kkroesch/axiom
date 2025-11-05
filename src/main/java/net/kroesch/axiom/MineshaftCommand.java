package net.kroesch.axiom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag; // Wird nicht mehr benötigt, da wir die manuelle Prüfung nutzen
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MineshaftCommand implements CommandExecutor {

    // Konstanten für die Geometrie
    private static final int BREITE = 5; 
    private static final int TUNNEL_HOEHE = 5; 
    private static final int RAMP_CYCLE = 3; // 1:3 Steigung
    private static final int LAMPEN_INTERVALL = 5; 
    private static final Material BODEN_MATERIAL = Material.COBBLESTONE; 
    private static final Material TREPPEN_MATERIAL = Material.COBBLESTONE_STAIRS;
    private static final int SCHIENEN_SPUR_W = -2; // w = -2 (Linker Rand)

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;
        int tiefe = 100;
        if (args.length > 0) {
            try {
                tiefe = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage("Ungültige Zahl für die Tiefe. Verwende Standard: 100.");
            }
        }

        BlockFace blickRichtung = getCardinalDirection(player);
        BlockFace querRichtung = getQuerRichtung(blickRichtung);
        BlockFace treppenRichtung = blickRichtung.getOppositeFace();

        Location startPos = player.getLocation().add(blickRichtung.getModX(), -1, blickRichtung.getModZ());
        int halbeBreite = BREITE / 2; // 2

        for (int d = 0; d < tiefe; d++) {

            int yOffset = d / RAMP_CYCLE; 
            int posInCycle = d % RAMP_CYCLE; 
            
            Location currentPos = startPos.clone().add(
                blickRichtung.getModX() * d, -yOffset, blickRichtung.getModZ() * d
            );

            Rail.Shape railRichtung = (blickRichtung == BlockFace.NORTH || blickRichtung == BlockFace.SOUTH) ?
                                      Rail.Shape.NORTH_SOUTH : Rail.Shape.EAST_WEST;

            // --- 4b. NEU: Scan-Schleife (Vorausschauen) ---
            // Wir prüfen alle 25 Blöcke (5x5) des nächsten Schnitts
            boolean flüssigkeitEntdeckt = false;
            for (int w = -halbeBreite; w <= halbeBreite; w++) {
                for (int h = 0; h < TUNNEL_HOEHE; h++) {
                    Location checkPos = currentPos.clone().add(
                        querRichtung.getModX() * w, h, querRichtung.getModZ() * w
                    );
                    Material mat = checkPos.getBlock().getType();
                    
                    if (mat == Material.WATER || mat == Material.LAVA) {
                        flüssigkeitEntdeckt = true;
                        break;
                    }
                }
                if (flüssigkeitEntdeckt) break;
            }

            // --- 4c. NEU: Aktion (Versiegeln oder Graben) ---
            if (flüssigkeitEntdeckt) {
                // STOP! Wir bauen eine Wand und beenden den Befehl.
                player.sendMessage("§c[Axiom] Warnung: Flüssigkeit bei Y=" + currentPos.getBlockY() + " entdeckt! Minenschacht wird versiegelt.");
                
                for (int w = -halbeBreite; w <= halbeBreite; w++) {
                    for (int h = 0; h < TUNNEL_HOEHE; h++) { 
                        Location sealPos = currentPos.clone().add(
                            querRichtung.getModX() * w, h, querRichtung.getModZ() * w
                        );
                        // Wir ersetzen ALLES (auch Luft) mit Cobblestone, um eine dichte Wand zu bauen
                        sealPos.getBlock().setType(BODEN_MATERIAL); 
                    }
                }
                return true; // Befehl erfolgreich beendet (gestoppt).
            }

            // --- 4d. Tunnel graben (Wenn KEINE Flüssigkeit gefunden wurde) ---
            for (int w = -halbeBreite; w <= halbeBreite; w++) { 
                for (int h = 0; h < TUNNEL_HOEHE; h++) { 
                    
                    Location grabPos = currentPos.clone().add(
                        querRichtung.getModX() * w, h, querRichtung.getModZ() * w
                    );
                    Block block = grabPos.getBlock();

                    // VEIN MINER: Erze nicht zerstören 
                    // (h>0, da der Boden h=0 immer gesetzt werden MUSS)
                    if (h > 0 && isOre(block)) {
                        continue; 
                    }
                    
                    // (Wasser/Lava-Check ist nicht mehr nötig, da wir oben scannen)

                    if (h == 0) {
                        if (w == SCHIENEN_SPUR_W) {
                            block.setType(BODEN_MATERIAL);
                        } else {
                            if (posInCycle == (RAMP_CYCLE - 1)) {
                                setStair(block, treppenRichtung);
                            } else {
                                block.setType(BODEN_MATERIAL);
                            }
                        }
                    } 
                    else if (h == 1 && w == SCHIENEN_SPUR_W) {
                        setRail(block, posInCycle, railRichtung);
                    } 
                    else {
                        block.setType(Material.AIR);
                    }
                }
            }

            // --- 4e. Beleuchtung ---
            if (d > 0 && d % LAMPEN_INTERVALL == 0) {
                Location deckenPos = currentPos.clone().add(0, (TUNNEL_HOEHE - 1), 0); 
                Location links = deckenPos.clone().add(
                    querRichtung.getModX() * SCHIENEN_SPUR_W, 0, querRichtung.getModZ() * SCHIENEN_SPUR_W
                );
                setLantern(links.getBlock());
                Location rechts = deckenPos.clone().add(
                    querRichtung.getModX() * halbeBreite, 0, querRichtung.getModZ() * halbeBreite
                );
                setLantern(rechts.getBlock());
            }
        }

        player.sendMessage("Mineschaft (1:3 Rampe) gegraben!");
        return true;
    }

    /**
     * Setzt die Schienenlogik für den 1:3-Aufzug (zum Hochfahren).
     * Zyklus (beim Graben nach unten): 
     * [Normal (d=0)] -> [Powered (d=1)] -> [Detector (d=2, Rampe)]
     */
    private void setRail(Block b, int posInCycle, Rail.Shape shape) {
        Rail railData;
        switch (posInCycle) {
            case 0:
                b.setType(Material.RAIL);
                railData = (Rail) b.getBlockData();
                break;
            case 1:
                b.setType(Material.POWERED_RAIL);
                railData = (Rail) b.getBlockData();
                ((org.bukkit.block.data.type.RedstoneRail) railData).setPowered(false);
                break;
            case 2:
            default:
                b.setType(Material.DETECTOR_RAIL);
                railData = (Rail) b.getBlockData();
                break;
        }
        railData.setShape(shape);
        b.setBlockData(railData);
    }

    /**
     * Prüft, ob ein Block ein Erz ist (damit wir ihn nicht überschreiben).
     */
    private boolean isOre(Block b) {
        Material m = b.getType();
        switch (m) {
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
            case NETHER_GOLD_ORE:
            case NETHER_QUARTZ_ORE:
            case ANCIENT_DEBRIS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Setzt eine Treppe, die in die angegebene Richtung zeigt.
     */
    private void setStair(Block b, BlockFace facing) {
        b.setType(TREPPEN_MATERIAL);
        Stairs stairData = (Stairs) b.getBlockData();
        stairData.setFacing(facing); 
        stairData.setHalf(Bisected.Half.BOTTOM); 
        b.setBlockData(stairData);
    }

    /**
     * Setzt eine hängende Laterne.
     */
    private void setLantern(Block b) {
        Block decke = b.getRelative(BlockFace.UP); 
        if (!decke.getType().isSolid()) {
            decke.setType(BODEN_MATERIAL);
        }
        b.setType(Material.LANTERN);
        Lantern lanternData = (Lantern) b.getBlockData();
        lanternData.setHanging(true);
        b.setBlockData(lanternData);
    }

    /**
     * Findet die nächste Himmelsrichtung (N, S, W, E).
     */
    private BlockFace getCardinalDirection(Player player) {
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) {
            yaw += 360;
        }
        yaw = (yaw + 45) % 360;
        if (yaw >= 0 && yaw < 90) return BlockFace.SOUTH;
        if (yaw >= 90 && yaw < 180) return BlockFace.WEST;
        if (yaw >= 180 && yaw < 270) return BlockFace.NORTH;
        if (yaw >= 270 && yaw < 360) return BlockFace.EAST;
        return BlockFace.NORTH;
    }

    /**
     * Helper-Funktion für die Querrichtung
     */
    private BlockFace getQuerRichtung(BlockFace blickRichtung) {
        switch (blickRichtung) {
            case NORTH: return BlockFace.WEST;
            case WEST: return BlockFace.SOUTH;
            case SOUTH: return BlockFace.EAST;
            case EAST: 
            default: return BlockFace.NORTH;
        }
    }
}