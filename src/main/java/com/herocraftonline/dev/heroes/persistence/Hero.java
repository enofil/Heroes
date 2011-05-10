package com.herocraftonline.dev.heroes.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.ExperienceGainEvent;
import com.herocraftonline.dev.heroes.api.LevelEvent;
import com.herocraftonline.dev.heroes.classes.HeroClass;
import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
import com.herocraftonline.dev.heroes.party.HeroParty;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Properties;

public class Hero {

    protected final Heroes plugin;
    protected Player player;
    protected HeroClass heroClass;
    protected int exp;
    protected int mana;
    protected boolean verbose;
    protected List<String> masteries;
    protected Map<String, Long> cooldowns;
    protected Map<Entity, CreatureType> summons;
    protected HeroEffects effects;
    protected Map<Material, String[]> binds;
    protected HeroParty party;
    protected Map<Player, HeroParty> invites;
    protected List<ItemStack> itemRecovery;

    public Hero(Heroes plugin, Player player, HeroClass heroClass, int exp, int mana, boolean verbose, List<String> masteries, List<ItemStack> itemRecovery, Map<Material, String[]> binds) {
        this.plugin = plugin;
        this.player = player;
        this.heroClass = heroClass;
        this.exp = exp;
        this.mana = mana;
        this.masteries = masteries;
        this.itemRecovery = itemRecovery;
        this.cooldowns = new HashMap<String, Long>();
        this.summons = new HashMap<Entity, CreatureType>();
        this.binds = binds;
        this.party = null;
        this.invites = new HashMap<Player, HeroParty>();
        this.effects = new HeroEffects(plugin.getCommandManager(), this);
        this.verbose = verbose;
    }

    public void addItem(ItemStack item) {
        this.itemRecovery.add(item);
    }

    public void setItems(List<ItemStack> items) {
        this.itemRecovery = items;
    }

    public List<ItemStack> getItems() {
        return this.itemRecovery;
    }

    public Player getPlayer() {
        Player servPlayer = plugin.getServer().getPlayer(player.getName());
        if (servPlayer != null && player != servPlayer) {
            player = servPlayer;
        }
        return player;
    }

    public HeroClass getHeroClass() {
        return heroClass;
    }

    public int getExp() {
        return exp;
    }

    public int getMana() {
        return mana;
    }

    public List<String> getMasteries() {
        return masteries;
    }

    public void setHeroClass(HeroClass heroClass) {
        this.heroClass = heroClass;

        // max experience if this class is mastered
        Properties prop = plugin.getConfigManager().getProperties();
        if (masteries.contains(heroClass.getName())) {
            exp = prop.maxExp;
        } else {
            exp = 0;
        }

        // Check the Players inventory now that they have changed class.
        this.plugin.inventoryCheck(getPlayer());
    }

    public void gainExp(int expGain, ExperienceType source) {
        Properties prop = plugin.getConfigManager().getProperties();
        int currentLevel = prop.getLevel(exp);
        int newLevel = prop.getLevel(exp + expGain);

        // If they're at max level, we don't add experience
        if (currentLevel == prop.maxLevel) {
            return;
        }

        // add the experience
        exp += expGain;

        // call event
        ExperienceGainEvent expEvent;
        if (newLevel == currentLevel) {
            expEvent = new ExperienceGainEvent(this, expGain, source);
        } else {
            expEvent = new LevelEvent(this, expGain, currentLevel, newLevel, source);
        }
        plugin.getServer().getPluginManager().callEvent(expEvent);
        if (expEvent.isCancelled()) {
            // undo the experience gain
            exp -= expGain;
            return;
        }

        // undo the previous gain to make sure we use the updated value
        exp -= expGain;
        expGain = expEvent.getExpGain();

        // add the updated experience
        exp += expGain;

        // notify the user
        if (expGain != 0) {
            if (verbose) {
                Messaging.send(player, "$1: Gained $2 Exp", heroClass.getName(), String.valueOf(expGain));
            }
            if (newLevel != currentLevel) {
                Messaging.send(player, "You leveled up! (Lvl $1 $2)", String.valueOf(newLevel), heroClass.getName());
                if (newLevel >= prop.maxLevel) {
                    exp = prop.getExperience(prop.maxLevel);
                    masteries.add(heroClass.getName());
                    Messaging.broadcast(plugin, "$1 has become a master $2!", player.getName(), heroClass.getName());
                    plugin.getHeroManager().saveHeroFile(player);
                }
            }
        }
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public void setMasteries(ArrayList<String> masterys) {
        this.masteries = masterys;
    }

    public Map<String, Long> getCooldowns() {
        return cooldowns;
    }

    public Map<Entity, CreatureType> getSummons() {
        return summons;
    }

    public HeroEffects getEffects() {
        return effects;
    }

    public Map<Material, String[]> getBinds() {
        return binds;
    }

    public void bind(Material material, String[] skillName) {
        binds.put(material, skillName);
    }

    public void unbind(Material material) {
        binds.remove(material);
    }

    public HeroParty getParty() {
        return party;
    }

    public void setParty(HeroParty party) {
        this.party = party;
    }

    public Map<Player, HeroParty> getInvites() {
        return invites;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public int hashCode() {
        return player == null ? 0 : player.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Hero other = (Hero) obj;
        if (player == null) {
            if (other.player != null) {
                return false;
            }
        } else if (!player.getName().equals(other.player.getName())) {
            return false;
        }
        return true;
    }
}