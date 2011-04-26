package com.herocraftonline.dev.heroes.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.NewPlayerEvent;
import com.herocraftonline.dev.heroes.classes.HeroClass;

/**
 * Player management
 * 
 * @author Herocraft's Plugin Team
 */
public class HeroManager {

    private Heroes plugin;
    private Set<Hero> heroes;
    private File playerFolder;

    public HeroManager(Heroes plugin) {
        this.plugin = plugin;
        this.heroes = new HashSet<Hero>();
        playerFolder = new File(plugin.getDataFolder(), "players"); // Setup our Player Data Folder
        playerFolder.mkdirs(); // Create the folder if it do esn't exist.
    }

    /**
     * Load the given Players Data file.
     * 
     * @param player
     */
    public void loadHeroFile(Player player) {
        File playerFile = new File(playerFolder, player.getName() + ".yml"); // Setup our Players Data File.
        // Check if it already exists, if so we load the data.
        if (playerFile.exists()) {
            Configuration playerConfig = new Configuration(playerFile); // Setup the Configuration
            playerConfig.load(); // Load the Config File

            HeroClass playerClass;
            // Grab the Players Class.
            if (playerConfig.getString("class") != null) {
                playerClass = plugin.getClassManager().getClass(playerConfig.getString("class")); // Grab the Players Class from the File.
            } else {
                playerClass = plugin.getClassManager().getDefaultClass(); // If no Class saved then revert to the Default Class.
            }

            // Grab the Data we need.
            List<String> masteries = playerConfig.getStringList("masteries", new ArrayList<String>());
            List<String> itemRecovery = playerConfig.getStringList("itemrecovery", new ArrayList<String>());
            int mana = playerConfig.getInt("mana", 0);
            int exp = playerConfig.getInt("experience", 0);

            // Create a New Hero
            Hero playerHero = new Hero(plugin, player, playerClass, exp, mana, masteries, itemRecovery);
            // Add the Hero to the Set.
            addHero(playerHero);
            plugin.log(Level.INFO, "Loaded hero: " + player.getName());
        } else {
            // Create a New Hero with the Default Setup.
            createNewHero(player);
            plugin.log(Level.INFO, "Created hero: " + player.getName());
        }
    }

    /**
     * Save the given Players Data to a file.
     * 
     * @param player
     */
    public void saveHeroFile(Player player) {
        File playerFile = new File(playerFolder, player.getName() + ".yml");
        Configuration playerConfig = new Configuration(playerFile);
        // Save the players stuff
        playerConfig.setProperty("class", getHero(player).getPlayerClass().toString());
        playerConfig.setProperty("experience", getHero(player).getExperience());
        playerConfig.setProperty("mana", getHero(player).getMana());
        playerConfig.setProperty("masteries", getHero(player).getMasteries());
        playerConfig.setProperty("itemrecovery", getHero(player).getItems());
        playerConfig.save();
        plugin.log(Level.INFO, "Saved hero: " + player.getName());
    }

    public boolean createNewHero(Player player) {
        plugin.getServer().getPluginManager().callEvent(new NewPlayerEvent(player));
        // Add a new Hero with the default setup.
        return addHero(new Hero(plugin, player, plugin.getClassManager().getDefaultClass(), 0, 0, new ArrayList<String>(), new ArrayList<String>()));
    }

    public boolean addHero(Hero hero) {
        return heroes.add(hero);
    }

    public boolean removeHero(Hero hero) {
        return heroes.remove(hero);
    }

    public boolean containsPlayer(Player player) {
        return getHero(player) != null;
    }

    public Hero getHero(Player player) {
        for (Hero hero : heroes) {
            if (hero.getPlayer() == null) {
                continue;
            }
            if (player.getName().equalsIgnoreCase(hero.getPlayer().getName())) {
                return hero;
            }
        }
        return null;
    }

    public Hero[] getHeroes() {
        return heroes.toArray(new Hero[0]);
    }
}
