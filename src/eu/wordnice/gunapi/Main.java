/*
 The MIT License (MIT)

 Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package eu.wordnice.gunapi;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin implements Listener {
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		this.getLogger().info("GunAPI by wordnice was enabled!");
	}
	
	@Override
	public void onDisable() {
		this.getLogger().info("GunAPI by wordnice was disabled!");
	}
	
	
	@EventHandler
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player p = event.getPlayer();
		if(event.isSneaking() && p.isOp() && p.getGameMode() == GameMode.CREATIVE) {
			ItemStack item = p.getItemInHand();
			if(item != null && item.getType() == Material.PORK && item.getAmount() == 33) {
				Location loc = p.getLocation();
				Vector direct = loc.getDirection();
				Set<ShootedEntity> ents = new HashSet<ShootedEntity>();
				GunAPI.cacheEntities(ents, loc.getWorld().getEntities());
				Set<ShootedEntity> shoted = new HashSet<ShootedEntity>();
				GunAPI.getShootedEntities(shoted, ents, new double[] {loc.getX(), loc.getY(), loc.getZ()}, 
						new double[] {direct.getX(), direct.getY(), direct.getZ()}, 50);
				
				Iterator<ShootedEntity> it = shoted.iterator();
				boolean hadShooted = false;
				while(it.hasNext()) {
					hadShooted = true;
					ShootedEntity se = it.next();
					LivingEntity le = se.ent;
					if(le instanceof Player) {
						Player sp = (Player) le;
						p.sendMessage("You shoot player " + sp.getName() + ", headshot: " + se.wasHeadshot);
					} else {
						p.sendMessage("You shoot entity " + le.getType().toString() + ", headshot: " + se.wasHeadshot);
					}
				}
				if(!hadShooted) {
					p.sendMessage("You didnt shoot anyone! Uf.");
				}
			}
		}
	}
	
	
}
