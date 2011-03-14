package com.herocraftonline.dev.Heroes;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.Heroes.persistance.SQLite;
import com.herocraftonline.dev.Heroes.persistance.player;
import com.herocraftonline.dev.Heroes.util.Properties;
import com.herocraftonline.dev.Heroes.util.ConfigManager;

/**
 * Heroes Plugin for Herocraft
 *
 * @author Herocraft's Plugin Team
 */
@SuppressWarnings("unused")
public class Heroes extends JavaPlugin {
	private final Listener Listener = new Listener(this);
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	public static final Logger Log = Logger.getLogger("Minecraft");
	private ConfigManager configManager;
	public static SQLite sql = new SQLite(null);
	
	
	public void onDisable() {
		System.out.println("Goodbye world!");
	}

	public void onEnable() {
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, Listener, Priority.Normal,this);
		PluginDescriptionFile pdfFile = this.getDescription();
		Log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
		configManager = new ConfigManager(this);
		try {
			configManager.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Properties.expCalc();
		sql.tryUpdate("CREATE TABLE IF NOT EXISTS `players` (`id` INT auto_increment, `name` VARCHAR, `class` VARCHAR, `exp` INT, `mana` INT)");
	}

	public boolean isDebugging(final Player player) {
		if (debugees.containsKey(player)) {
			return debugees.get(player);
		} else {
			return false;
		}
	}

	public void setDebugging(final Player player, final boolean value) {
		debugees.put(player, value);
	}

}