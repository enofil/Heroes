package com.herocraftonline.dev.heroes.command.skill.skills;

import org.bukkit.entity.LivingEntity;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.command.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.persistence.Hero;

public class SkillCharge extends TargettedSkill{

    public SkillCharge(Heroes plugin) {
        super(plugin);
        name = "Charge";
        description = "Charges towards your target";
        usage = "/skill charge";
        minArgs = 0;
        maxArgs = 0;
        identifiers.add("skill charge");
    }

    @Override
    public boolean use(Hero hero, LivingEntity target, String[] args) {
        hero.getPlayer().teleport(target);
        return true;
    }

}
