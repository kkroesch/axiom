package net.kroesch.axiom;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            return false; // Zeigt die 'usage' aus der plugin.yml
        }

        try {
            int length = Integer.parseInt(args[0]);
            int height = 2;
            int width = 2;

            Location startLocation = player.getLocation();
            World world = startLocation.getWorld();

            // 1. Blickrichtung des Spielers als Vektor holen
            Vector direction = player.getEyeLocation().getDirection().setY(0).normalize();

            // 2. Einen Vektor, der 90 Grad links von der Blickrichtung ist (für die Breite)
            Vector left = new Vector(direction.getZ(), 0, -direction.getX()).normalize();

            player.sendMessage("§aGrabe einen " + length + " Blöcke langen Tunnel...");

            // 3. Den Tunnel Block für Block bauen
            for (int i = 0; i < length; i++) {
                // Berechne den Mittelpunkt des aktuellen Tunnel-Segments
                Location currentCenter = startLocation.clone().add(direction.clone().multiply(i));

                // Tunnel graben (2x2 Bereich)
                for (int w = 0; w < width; w++) {
                    for (int h = 0; h < height; h++) {
                        Location blockToClear = currentCenter.clone().add(left.clone().multiply(w)).add(0, h, 0);
                        blockToClear.getBlock().setType(Material.AIR, false); // 'false' verhindert Physik-Updates während des Grabens
                    }
                }

                // Boden-Blöcke (für die Schienen)
                Location railLocation = currentCenter.clone();
                Location railLocationLeft = currentCenter.clone().add(left);

                // Schienen legen
                setRail(railLocation.getBlock());
                setRail(railLocationLeft.getBlock());


                // Alle 10 Blöcke eine Nische mit Fackel bauen
                if (i > 0 && i % 10 == 0) {
                    // Nische graben (1 Block tief in die linke Wand)
                    Location nicheLocation = currentCenter.clone().add(left.clone().multiply(width)).add(0, 1, 0); // Augenhöhe
                    nicheLocation.getBlock().setType(Material.AIR, false);

                    // Fackel an die Rückwand der Nische platzieren
                    Block torchLocationBlock = nicheLocation.clone().add(left).getBlock();
                    torchLocationBlock.setType(Material.TORCH);
                }
            }

            player.sendMessage("§aTunnel erfolgreich gegraben!");

        } catch (NumberFormatException e) {
            player.sendMessage("§cFehler: Bitte gib eine gültige Zahl für die Länge an.");
            return false;
        }

        return true;
    }

    /**
     * Eine Hilfsmethode, um eine Schiene zu setzen.
     * @param railBlock Der Block, an dem die Schiene platziert werden soll.
     */
    private void setRail(Block railBlock) {
        if (railBlock.getType() != Material.RAIL) {
            railBlock.setType(Material.RAIL);
        }
        // Optional: Schienen ausrichten. Für gerade Tunnel ist das meist nicht nötig.
        // Rail railData = (Rail) railBlock.getBlockData();
        // railData.setShape(Rail.Shape.NORTH_SOUTH); // oder EAST_WEST, je nach Richtung
        // railBlock.setBlockData(railData);
    }
}