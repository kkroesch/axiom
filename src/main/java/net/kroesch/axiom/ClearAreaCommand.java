package net.kroesch.axiom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * /cleararea &lt;länge&gt; &lt;breite&gt; [höhe] [corner] [sync] [x y z]
 *
 * <p>Räumt einen Quader leer. Ohne {@code corner} ist die Fläche um den Spieler zentriert,
 * mit {@code corner} beginnt sie an der Spielerposition (oder an x y z) und wächst nach
 * Osten (+X) und Süden (+Z) - genau wie ein Blueprint, der an dieser Ecke verankert ist.</p>
 *
 * <p>Das Räumen läuft in Portionen über mehrere Ticks, damit der Server bei grossen
 * Flächen nicht stehen bleibt. Mit {@code sync} wird stattdessen alles sofort in einem Tick
 * erledigt - nötig, wenn direkt danach per Skript/RCON gebaut wird.</p>
 */
public class ClearAreaCommand implements CommandExecutor, TabCompleter {

    static final int DEFAULT_HEIGHT = 10;
    static final int MAX_SIDE = 512;
    static final int MAX_HEIGHT = 512;
    static final long MAX_BLOCKS = 5_000_000L;
    /** So viele Blöcke werden pro Tick geprüft. */
    static final int BLOCKS_PER_TICK = 8192;

    private static final String USAGE =
            "/cleararea <länge> <breite> [höhe] [corner] [sync] [x y z]";

    private final JavaPlugin plugin;
    /** Absender mit laufendem Räumauftrag (nur Hauptthread). */
    private final Set<String> running = new HashSet<>();

    public ClearAreaCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cFehler: Falsche Verwendung. Bitte nutze: " + USAGE);
            return true;
        }

        int length, width, height = DEFAULT_HEIGHT;
        boolean corner = false;
        boolean sync = false;
        Integer ox = null, oy = null, oz = null;

        try {
            length = Integer.parseInt(args[0]);
            width = Integer.parseInt(args[1]);
            int i = 2;
            if (i < args.length && isInt(args[i])) {
                height = Integer.parseInt(args[i++]);
            }
            while (i < args.length) {
                if (isCornerKeyword(args[i])) {
                    corner = true;
                } else if (args[i].equalsIgnoreCase("sync")) {
                    sync = true;
                } else {
                    break;
                }
                i++;
            }
            if (i < args.length) {
                if (!corner || args.length - i != 3) {
                    sender.sendMessage("§cFehler: Falsche Verwendung. Bitte nutze: " + USAGE);
                    return true;
                }
                ox = Integer.parseInt(args[i]);
                oy = Integer.parseInt(args[i + 1]);
                oz = Integer.parseInt(args[i + 2]);
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cFehler: Bitte gib gültige ganze Zahlen an. Verwendung: " + USAGE);
            return true;
        }

        if (length < 1 || width < 1 || height < 1) {
            sender.sendMessage("§cFehler: Länge, Breite und Höhe müssen mindestens 1 sein.");
            return true;
        }
        if (length > MAX_SIDE || width > MAX_SIDE || height > MAX_HEIGHT) {
            sender.sendMessage("§cFehler: Maximal " + MAX_SIDE + " Blöcke pro Seite und "
                    + MAX_HEIGHT + " in der Höhe.");
            return true;
        }
        if ((long) length * width * height > MAX_BLOCKS) {
            sender.sendMessage("§cFehler: Fläche zu gross (max. " + MAX_BLOCKS + " Blöcke).");
            return true;
        }

        // Welt und Ursprung bestimmen
        World world;
        int originX, originY, originZ;
        if (sender instanceof Player player) {
            world = player.getWorld();
            originX = ox != null ? ox : player.getLocation().getBlockX();
            originY = oy != null ? oy : player.getLocation().getBlockY();
            originZ = oz != null ? oz : player.getLocation().getBlockZ();
        } else {
            if (ox == null) {
                sender.sendMessage("§cFehler: Von der Konsole bitte mit Koordinaten aufrufen: "
                        + "/cleararea <länge> <breite> [höhe] corner <x> <y> <z>");
                return true;
            }
            world = Bukkit.getWorlds().get(0);
            originX = ox;
            originY = oy;
            originZ = oz;
        }

        final int x0 = corner ? originX : originX - length / 2;
        final int x1 = x0 + length - 1;
        final int z0 = corner ? originZ : originZ - width / 2;
        final int z1 = z0 + width - 1;
        final int y0 = Math.max(originY, world.getMinHeight());
        final int y1 = Math.min(originY + height - 1, world.getMaxHeight() - 1);
        if (y0 > y1) {
            sender.sendMessage("§cFehler: Die Höhe liegt ausserhalb der Welt ("
                    + world.getMinHeight() + ".." + (world.getMaxHeight() - 1) + ").");
            return true;
        }

        final String key = sender.getName();
        if (!running.add(key)) {
            sender.sendMessage("§cEs läuft bereits ein Räumauftrag von dir. Bitte warten.");
            return true;
        }

        final long total = (long) (x1 - x0 + 1) * (z1 - z0 + 1) * (y1 - y0 + 1);
        sender.sendMessage("§aRäume " + (x1 - x0 + 1) + "x" + (z1 - z0 + 1) + "x" + (y1 - y0 + 1)
                + " Blöcke in " + world.getName() + " ab (" + x0 + ", " + y0 + ", " + z0 + ")...");

        ClearJob job = new ClearJob(sender, key, world, x0, x1, y0, y1, z0, z1, total);
        if (sync) {
            job.step(Integer.MAX_VALUE);
        } else {
            job.schedule();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length >= 3 && args.length <= 5) {
            String prefix = args[args.length - 1].toLowerCase();
            return Stream.of("corner", "sync").filter(k -> k.startsWith(prefix)).toList();
        }
        return List.of();
    }

    private static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isCornerKeyword(String s) {
        return s.equalsIgnoreCase("corner") || s.equalsIgnoreCase("ecke");
    }

    /** Räumt den Quader von oben nach unten, höchstens BLOCKS_PER_TICK Blöcke pro Tick. */
    private final class ClearJob extends BukkitRunnable {
        private final CommandSender sender;
        private final String key;
        private final World world;
        private final int x0, x1, y0, z0, z1;
        private final long total;
        private int x, y, z;
        private long removed;

        ClearJob(CommandSender sender, String key, World world,
                 int x0, int x1, int y0, int y1, int z0, int z1, long total) {
            this.sender = sender;
            this.key = key;
            this.world = world;
            this.x0 = x0;
            this.x1 = x1;
            this.y0 = y0;
            this.z0 = z0;
            this.z1 = z1;
            this.total = total;
            this.x = x0;
            this.y = y1;
            this.z = z0;
        }

        @Override
        public void run() {
            step(BLOCKS_PER_TICK);
        }

        private boolean scheduled;

        void schedule() {
            scheduled = true;
            runTaskTimer(plugin, 0L, 1L);
        }

        /** cancel() wirft, wenn der Auftrag nie eingeplant war (sync-Modus). */
        private void stop() {
            if (scheduled) {
                cancel();
            }
        }

        /** Prüft höchstens {@code maxBlocks} Blöcke weiter; beendet den Auftrag, wenn alles durch ist. */
        void step(int maxBlocks) {
            try {
                for (int budget = maxBlocks; budget > 0; budget--) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!block.getType().isAir()) {
                        // false: keine Physik-Updates, spart Zeit und verhindert Drops/Nachfliessen
                        block.setType(Material.AIR, false);
                        removed++;
                    }
                    if (++z > z1) {
                        z = z0;
                        if (++x > x1) {
                            x = x0;
                            if (--y < y0) {
                                finish();
                                return;
                            }
                        }
                    }
                }
            } catch (RuntimeException e) {
                plugin.getLogger().severe("cleararea abgebrochen: " + e);
                sender.sendMessage("§cFehler beim Räumen: " + e.getMessage());
                running.remove(key);
                stop();
            }
        }

        private void finish() {
            running.remove(key);
            stop();
            String msg = "Bereich geräumt: " + removed + " von " + total + " Blöcken entfernt.";
            sender.sendMessage("§a" + msg);
            plugin.getLogger().info("cleararea (" + key + "): " + msg);
        }
    }
}
