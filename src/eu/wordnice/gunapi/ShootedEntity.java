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

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class ShootedEntity {
	
	public double[] head_x; //4
	public double head_hy;
	public double head_ly;
	public double[] head_z; //4
	public double[] body_x; //4
	public double body_hy;
	public double body_ly;
	public double[] body_z; //4
	public Entity ent;
	public boolean wasHeadshot;
	
	/*
	 * Compute coordinates of Entities for fast checking
	 * 
	 * @return new Object for fast & easy collide checking
	 */
	public ShootedEntity(double[] buff1, double[] buff2, Entity ent) {
		this.ent = ent;
		
		double size_headx = GunAPI.getHeadWidth(ent) / 2;
		double size_heady = GunAPI.getHeadLength(ent) / 2;
		double size_headz = GunAPI.getHeadHeight(ent) / 2;
		
		double size_x = GunAPI.getWidth(ent) / 2;
		double size_y = (GunAPI.getLength(ent) - (2 * size_heady));
		double size_z = GunAPI.getHeight(ent) / 2;
		
		//Head
		Location lo = null;
		if(ent instanceof LivingEntity) {
			lo = ((LivingEntity) ent).getEyeLocation();
		} else {
			lo = ent.getLocation().add(0, size_y, 0);
		}
		
		this.head_hy = (lo.getY() + size_heady);
		this.head_ly = (lo.getY() - size_heady);
		
		double val1 = (lo.getX() - size_headx);
		double val2 = (lo.getX() + size_headx);
		this.head_x = new double[] {val1, val2, val2, val1};
		
		val1 = (lo.getZ() - size_headz);
		val2 = (lo.getZ() + size_headz);
		this.head_z = new double[] {val1, val1, val2, val2};
		
		GunAPI.rotateRectangle2D(buff1, buff2, this.head_x, this.head_z, GunAPI.correctAngle(lo.getYaw()));
	
		
		//Body
		lo = ent.getLocation();
		
		this.body_hy = (lo.getY() + size_y);
		this.body_ly = lo.getY();
		
		val1 = (lo.getX() - size_x);
		val2 = (lo.getX() + size_x);
		this.body_x = new double[] {val1, val2, val2, val1};
		
		val1 = (lo.getZ() - size_z);
		val2 = (lo.getZ() + size_z);
		this.body_z = new double[] {val1, val1, val2, val2};
		
		GunAPI.rotateRectangle2D(buff1, buff2, this.body_x, this.body_z, GunAPI.correctAngle(lo.getYaw()));
	}
	
	/*
	 * Check if loc (x, y, z) is colliding with this entity
	 * 
	 * @return 0 if not colliding, 1 if colliding with body or 2 if colliding with head
	 */
	public int collide(double[] loc) {
		double y = loc[1];
		if(y >= this.head_ly && y <= this.head_hy
				&& GunAPI.isPointIn4(this.head_x, this.head_z, loc[0], loc[2])) {
			return 2;
		}
		if(y >= this.body_ly && y <= this.body_hy
				&& GunAPI.isPointIn4(this.body_x, this.body_z, loc[0], loc[2])) {
			return 1;
		}
		return 0;
	}
	
	/*
	 * Override
	 * Check if input object has same data
	 * 
	 * @return `true` if input object has same data as this
	 */
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ShootedEntity)) {
			return false;
		}
		ShootedEntity se = (ShootedEntity) o;
		return (this.body_hy == se.body_hy && this.body_ly == se.body_ly
				&& this.head_hy == se.head_hy && this.head_ly == se.head_ly
				&& this.ent.equals(se.ent) &&
				(this.body_x[0] == se.body_x[0] && this.body_x[1] == se.body_x[1]
						&& this.body_x[2] == se.body_x[2] && this.body_x[3] == se.body_x[3]));
	}
	
}
