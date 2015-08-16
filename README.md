 GunAPI
========

*GunAPI is under MIT license, you can find sources there: https://github.com/wordnice/GunAPI*

GunAPI provides simple, low-level oriented API for getting blocks and entities on cursor. Simple example is implemented in library, you can try it:

* Get OP
* Get 33 porks - /i pork 33
* Get creative gamemode
* Take a look at some entities
* Press shift
* You will get list of entities you hit in radius 50 blocks (including headshots)

```java
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
				}
			}
		}
	}
```


[Screenshots](/screenshots/)
