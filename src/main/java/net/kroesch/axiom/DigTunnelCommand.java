package net.kroesch.axiom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DigTunnelCommand implements CommandExecutor {

    // --- Configuration Constants ---
    // (Die Rail-Offsets werden jetzt dynamisch berechnet)
    private static final int LANTERN_INTERVAL = 10;
    private static final Material RAIL_SUPPORT_MATERIAL = Material.COBBLESTONE;

    /**
     * Ein Record (Datenklasse) zur Speicherung der geparsten Argumente.
     */
    private record TunnelParams(int length, int diameter) {}

    // --- 1. Command Execution (Der "Controller") ---

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Optional<TunnelParams> paramsOpt = parseArguments(args);
        if (paramsOpt.isEmpty()) {
            player.sendMessage("§cUsage: /digtunnel <länge> <durchmesser (min 3)>");
            return false;
        }

        try {
            digTunnel(player, paramsOpt.get());
            player.sendMessage("§aTunnel erfolgreich gegraben!");
        } catch (Exception e) {
            player.sendMessage("§cEin unerwarteter Fehler ist aufgetreten: " + e.getMessage());
        }
        
        return true;
    }

    // --- 2. Core Logic (Die "Use Cases") ---

    /**
     * Führt die Haupt-Grabschleife aus.
     */
    private void digTunnel(Player player, TunnelParams params) {
        BlockFace direction = getCardinalDirection(player);
        Vector directionVector = direction.getDirection();
        Vector rightVector = getRightVector(direction);
        
        // KORREKTUR: Berechne die relative W-Position des Spielers
        int radiusInt = (int) Math.floor(params.diameter / 2.0);
        int railWOffset = radiusInt; // Die Schiene ist, wo der Spieler steht

        Location startLocation = player.getLocation().getBlock().getLocation();
        
        player.sendMessage("§aGrabe einen runden Tunnel (D=" + params.diameter + ") für " + params.length + " Blöcke in Richtung " + direction.name() + "...");

        for (int i = 0; i < params.length; i++) {
            Location sliceBottomCenter = startLocation.clone().add(directionVector.clone().multiply(i));
            Location sliceBottomLeft = sliceBottomCenter.clone().add(rightVector.clone().multiply(-radiusInt));

            digTunnelSlice(sliceBottomLeft, rightVector, direction, params.diameter, railWOffset);
            placeSupportStructures(sliceBottomLeft, rightVector, params.diameter, i);
        }
    }

    /**
     * Gräbt einen einzelnen "runden" Querschnitt (Slice) des Tunnels.
     */
    private void digTunnelSlice(Location sliceBottomLeft, Vector rightVector, BlockFace direction, int diameter, int railWOffset) {
        for (int h = 0; h < diameter; h++) {
            for (int w = 0; w < diameter; w++) {
                if (isCorner(w, h, diameter)) {
                    continue;
                }
                
                Location loc = sliceBottomLeft.clone().add(rightVector.clone().multiply(w)).add(0, h, 0);
                handleBlock(loc.getBlock(), w, h, direction, railWOffset);
            }
        }
    }

    /**
     * Entscheidet, was mit einem einzelnen Block im Querschnitt passiert (Graben, Schiene, Erz).
     */
    private void handleBlock(Block block, int w, int h, BlockFace direction, int railWOffset) {
        // 1. Schienen-Logik (KORREKTUR: h=0, w=railWOffset)
        if (h == 0 && w == railWOffset) {
            // Dies ist der Schienenblock (der Block, auf dem der Spieler stand)
            setRail(block, direction);
            
            // Setze den Block UNTER der Schiene
            Block supportBlock = block.getRelative(BlockFace.DOWN);
            if (supportBlock.getType() == Material.AIR || isOre(supportBlock)) {
                supportBlock.setType(RAIL_SUPPORT_MATERIAL);
            }
            return;
        }

        // 2. Standard-Graben-Logik (Erze erhalten)
        if (!isOre(block)) {
            block.setType(Material.AIR, false);
        }
    }

    /**
     * Platziert Laternen, falls das Intervall erreicht ist.
     */
    private void placeSupportStructures(Location sliceBottomLeft, Vector rightVector, int diameter, int i) {
        if (i == 0 || i % LANTERN_INTERVAL != 0) {
            return; // Nur alle X Blöcke (und nicht am Start)
        }
        
        // Linke Laterne (nahe der linken Wand)
        Location leftLoc = sliceBottomLeft.clone()
                .add(rightVector.clone().multiply(1)) // w=1
                .add(0, diameter - 1, 0); // Decke
        setLantern(leftLoc.getBlock());

        // Rechte Laterne (nahe der rechten Wand)
        Location rightLoc = sliceBottomLeft.clone()
                .add(rightVector.clone().multiply(diameter - 2)) // w=D-2
                .add(0, diameter - 1, 0); // Decke
        setLantern(rightLoc.getBlock());
    }


    // --- 3. Utility Functions (Die "Tools") ---

    /**
     * Parst die Befehlsargumente und validiert sie.
     */
    private Optional<TunnelParams> parseArguments(String[] args) {
        if (args.length != 2) {
            return Optional.empty();
        }
        try {
            int length = Integer.parseInt(args[0]);
            int diameter = Integer.parseInt(args[1]);

            if (diameter < 3) { // Min 3 für "runde" Ecken
                return Optional.empty();
            }
            return Optional.of(new TunnelParams(length, diameter));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Prüft, ob eine Koordinate eine Ecke der DxD-Box ist.
     */
    private boolean isCorner(int w, int h, int diameter) {
        boolean isTopOrBottom = (h == 0 || h == diameter - 1);
        boolean isLeftOrRight = (w == 0 || w == diameter - 1);
        return isTopOrBottom && isLeftOrRight;
    }

    /**
     * Setzt eine hängende Laterne.
     */
    private void setLantern(Block lanternBlock) {
        if (isOre(lanternBlock)) {
            return; // Ersetze keine Erze an der Decke
        }
        
        Block supportBlock = lanternBlock.getRelative(BlockFace.UP);
        if (!supportBlock.getType().isSolid()) {
            return; // Kann nicht in der Luft hängen
        }

        lanternBlock.setType(Material.LANTERN);
        BlockData blockData = lanternBlock.getBlockData();
        if (blockData instanceof Lantern lantern) {
            lantern.setHanging(true);
            lanternBlock.setBlockData(lantern);
        }
    }

    /**
     * Prüft, ob ein Block ein Erz ist.
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
     * Setzt eine Schiene mit korrekter Ausrichtung.
     */
    private void setRail(Block railBlock, BlockFace tunnelDirection) {
        railBlock.setType(Material.RAIL);
        BlockData blockData = railBlock.getBlockData(); 

        if (blockData instanceof Rail railData) {
            if (tunnelDirection == BlockFace.NORTH || tunnelDirection == BlockFace.SOUTH) {
                railData.setShape(Rail.Shape.NORTH_SOUTH);
            } else {
                railData.setShape(Rail.Shape.EAST_WEST);
            }
            railBlock.setBlockData(railData);
        }
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
     * Holt den Vektor, der 90 Grad rechts von der Blickrichtung liegt.
     */
    private Vector getRightVector(BlockFace direction) {
        // (x, z) -> (z, -x) ist 90 Grad rechts.
        return new Vector(direction.getModZ(), 0, -direction.getModX());
    }
}