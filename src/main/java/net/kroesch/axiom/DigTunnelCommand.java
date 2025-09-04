package net.kroesch.axiom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rail;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class DigTunnelCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        if (args.length != 1) {
            return false;
        }

        try {
            int length = Integer.parseInt(args[0]);

            BlockFace direction = getCardinalDirection(player);
            Vector directionVector = direction.getDirection();
            Vector leftVector = new Vector(directionVector.getZ(), 0, -directionVector.getX());

            Location startLocation = player.getLocation().getBlock().getLocation();

            player.sendMessage("§aGrabe einen " + length + " Blöcke langen Tunnel in Richtung " + direction.name() + "...");

            for (int i = 0; i < length; i++) {
                Location currentBase = startLocation.clone().add(directionVector.clone().multiply(i));

                // Tunnel graben (2x2 Bereich)
                for (int h = 0; h < 2; h++) {
                    for (int w = 0; w < 2; w++) {
                        currentBase.clone().add(leftVector.clone().multiply(w)).add(0, h, 0).getBlock().setType(Material.AIR, false);
                    }
                }

                // Schiene auf der linken Seite des Tunnels legen
                Block railBlock = currentBase.clone().add(leftVector).getBlock();
                setRail(railBlock, direction);

                // Alle 10 Blöcke eine Nische mit Fackel
                if (i > 0 && i % 10 == 0) {
                    Location nicheLocation = currentBase.clone().add(leftVector.clone().multiply(2)).add(0, 1, 0);
                    nicheLocation.getBlock().setType(Material.AIR, false);

                    // --- KORRIGIERTER FACKEL-TEIL ---
                    Block torchBlock = nicheLocation.getBlock();
                    torchBlock.setType(Material.WALL_TORCH); // Das richtige Material verwenden

                    // BlockData holen und über das Interface ansprechen
                    BlockData blockData = torchBlock.getBlockData();
                    if (blockData instanceof Directional directional) {
                        // Fackel an der Wand ausrichten, sodass sie in den Tunnel zeigt
                        directional.setFacing(direction.getOppositeFace());
                        torchBlock.setBlockData(directional);
                    }
                }
            }

            player.sendMessage("§aTunnel erfolgreich gegraben!");

        } catch (NumberFormatException e) {
            player.sendMessage("§cFehler: Bitte gib eine gültige Zahl für die Länge an.");
            return false;
        }

        return true;
    }

    private BlockFace getCardinalDirection(Player player) {
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) {
            yaw += 360;
        }
        if (yaw >= 315 || yaw < 45) {
            return BlockFace.SOUTH;
        } else if (yaw < 135) {
            return BlockFace.WEST;
        } else if (yaw < 225) {
            return BlockFace.NORTH;
        } else {
            return BlockFace.EAST;
        }
    }

    private void setRail(Block railBlock, BlockFace tunnelDirection) {
        railBlock.setType(Material.RAIL);
        Rail railData = (Rail) railBlock.getBlockData();

        if (tunnelDirection == BlockFace.NORTH || tunnelDirection == BlockFace.SOUTH) {
            railData.setShape(Rail.Shape.NORTH_SOUTH);
        } else {
            railData.setShape(Rail.Shape.EAST_WEST);
        }
        railBlock.setBlockData(railData);
    }
}