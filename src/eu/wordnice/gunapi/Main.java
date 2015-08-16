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
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
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
	
	public MapView mw = null;
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player p = event.getPlayer();
		if(event.isSneaking() && p.isOp() && p.getGameMode() == GameMode.CREATIVE) {
			ItemStack item = p.getItemInHand();
			if(item != null && item.getType() == Material.PORK && item.getAmount() == 33) {
				Location loc = p.getEyeLocation(); //!!! It is NOT getLocation()
				Vector direct = loc.getDirection();
				
				Set<ShootedEntity> ents = new HashSet<ShootedEntity>(); //There will be cached all living entities from world
				GunAPI.cacheLivingEntities(ents, loc.getWorld().getEntities()); //Load living entities
				
				double[] location = new double[] {loc.getX(), loc.getY(), loc.getZ()}; //double[3] location
				double[] vector = new double[] {direct.getX(), direct.getY(), direct.getZ()}; //double[3] direction
				GunAPI.maximize(vector, 0.1); //For better search (smaller = better)
				
				int radius = 50;
				int cycles = GunAPI.cyclesFromRadius(vector, radius);
				
				Set<ShootedEntity> shoted = new HashSet<ShootedEntity>(); //There will be saved shoted entities
				GunAPI.getShootedEntities(shoted, ents, location, vector, cycles, true);
				
				boolean shootSomeone = false;
				
				Iterator<ShootedEntity> it = shoted.iterator();
				LivingEntity first = null;
				while(it.hasNext()) {
					ShootedEntity se = it.next();
					LivingEntity le = (LivingEntity) se.ent;
					if(le.equals(p)) {
						continue;
					}
					shootSomeone = true;
					if(first == null) {
						first = le;
					}
					if(le instanceof Player) {
						Player sp = (Player) le;
						p.sendMessage(ChatColor.YELLOW + "You shoot player " + sp.getName() + ", headshot: " + se.wasHeadshot);
					} else {
						p.sendMessage(ChatColor.YELLOW + "You shoot entity " + le.getType().toString() + ", headshot: " + se.wasHeadshot);
					}
					if(se.wasHeadshot) {
						le.damage(10.01);
					} else {
						le.damage(3.01);
					}
				}
				if(!shootSomeone) {
					p.sendMessage(ChatColor.GREEN + "Uf, you didn't hurt anyone!");
				} else {
					if(this.mw == null) {
						this.mw = Bukkit.createMap(loc.getWorld());
						this.mw.addRenderer(new AreaRenderer(first));
						p.getInventory().addItem(new ItemStack(Material.MAP, 1, this.mw.getId()));
					} else {
						Iterator<MapRenderer> mit = this.mw.getRenderers().iterator();
						while(mit.hasNext()) {
							MapRenderer mr = mit.next();
							if(mr instanceof AreaRenderer) {
								AreaRenderer ar = (AreaRenderer) mr;
								if(ar.ent.isDead()) {
									ar.ent = first;
								}
							}
						}
					}
				}
			}
		}
	}
	
	
}
