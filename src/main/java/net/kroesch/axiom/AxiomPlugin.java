package net.kroesch.axiom;
import org.bukkit.plugin.java.JavaPlugin;

public final class AxiomPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Mein erstes Plugin wurde erfolgreich geladen!");

        // Registriert den /hallo Befehl und weist ihn der HalloCommand Klasse zu
        this.getCommand("hallo").setExecutor(new EchoCommand());
        this.getCommand("digtunnel").setExecutor(new DigTunnelCommand());
        this.getCommand("cleararea").setExecutor(new ClearAreaCommand());
        this.getCommand("buildwall").setExecutor(new BuildWallCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Mein Plugin wurde heruntergefahren.");
    }
}