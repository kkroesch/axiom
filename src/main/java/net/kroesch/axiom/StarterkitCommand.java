package net.kroesch.axiom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StarterkitCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        if (!player.hasPermission("axiom.command.kit.starter")) {
            player.sendMessage("§cDu hast keine Berechtigung, diesen Befehl zu verwenden.");
            return true;
        }

        // Finde einen Platz vor dem Spieler, um die Kiste zu platzieren.
        // Wir nehmen den Block, auf den der Spieler schaut, und gehen eine Position nach oben
        Block targetBlock = player.getTargetBlock(null, 5).getRelative(0, 1, 0);
        Location chestLocation = targetBlock.getLocation();

        // Erstelle die grosse Kiste (zwei Blöcke nebeneinander)
        Block chestBlock1 = chestLocation.getBlock();
        Block chestBlock2 = chestLocation.clone().add(1, 0, 0).getBlock(); // Der zweite Teil der Kiste
        chestBlock1.setType(Material.CHEST);
        chestBlock2.setType(Material.CHEST);

        // Hole dir das Inventar der (jetzt grossen) Kiste
        Chest chestState = (Chest) chestBlock1.getState();
        Inventory chestInventory = chestState.getInventory();
        chestInventory.clear(); // Leere die Kiste zur Sicherheit

        // ----- Befülle die Kiste -----
        // Materialien
        chestInventory.addItem(new ItemStack(Material.RAW_IRON, 64));
        chestInventory.addItem(new ItemStack(Material.RAW_IRON, 64));
        chestInventory.addItem(new ItemStack(Material.COAL, 64));
        chestInventory.addItem(new ItemStack(Material.COAL, 64));
        chestInventory.addItem(new ItemStack(Material.WHITE_WOOL, 16));
        chestInventory.addItem(new ItemStack(Material.OAK_PLANKS, 64));
        chestInventory.addItem(new ItemStack(Material.STICK, 64));

        // Werkzeuge
        chestInventory.addItem(new ItemStack(Material.FLINT_AND_STEEL, 1));
        chestInventory.addItem(new ItemStack(Material.BUCKET, 1));
        chestInventory.addItem(new ItemStack(Material.NETHERITE_AXE, 1));
        chestInventory.addItem(new ItemStack(Material.NETHERITE_SWORD, 1));
        chestInventory.addItem(new ItemStack(Material.NETHERITE_SHOVEL, 1));

        // NEU: Rüstung
        chestInventory.addItem(new ItemStack(Material.IRON_HELMET, 1));
        chestInventory.addItem(new ItemStack(Material.IRON_CHESTPLATE, 1));
        chestInventory.addItem(new ItemStack(Material.IRON_LEGGINGS, 1));
        chestInventory.addItem(new ItemStack(Material.IRON_BOOTS, 1));

        // Nahrung & Farmen
        chestInventory.addItem(new ItemStack(Material.COOKED_BEEF, 32));
        chestInventory.addItem(new ItemStack(Material.WHEAT_SEEDS, 16));
        // -----------------------------

        player.sendMessage("§aEine Starter-Kiste wurde vor dir platziert!");
        return true;
    }
}