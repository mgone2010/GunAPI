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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;

public class GunAPI {
	
	/*
	 * Cached fields - make reflection faster
	 */
	private static Method handleMethod = null;
	private static Field lengthField = null;
	private static Field heightField = null;
	private static Field widthField = null;
	
	private static double widthDefault = 0.9;
	private static double heightDefault = 0.4;
	private static double lengthDefault = 1.8;
	
	private static double headWidthDefault = 0.5;
	private static double headHeightDefault = 0.5;
	private static double headLengthDefault = 0.55;
	
	/*
	 * walk(...) and walk3D(...) method parameter appenders
	 * 
	 * Use as: walk(yaw + WALK_xxx)
	 */
	public static double WALK_STRAIGHT = 0;
	public static double WALK_RIGHT = 90;
	public static double WALK_BACK = 180;
	public static double WALK_LEFT = 270;
	
	/*
	 * isPointIn method was derived from 
	 * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
	 * 
	 * Thanks to
	 * * Loren Albertazzi, for finding an error in the treatment of multiple component polygons.
	 * * Mark Sullivan for suggesting the documentation change from "vertical" to "horizontal".
	 * * Christopher Sargent for suggesting an optimization based on Joe O'Rourke's Computational Geometry in C.
	 * 
	 * @return true if point (testx, texty) is inside of polygon made of points stored in (vertx, verty)
	 */
	public static boolean isPointIn(double[] vertx, double[] verty, int vert_count,
			double testx, double testy) {
		int i, j;
		boolean c = false;
		for(i = 0, j = vert_count - 1; i < vert_count; j = i++) {
			if(((verty[i]>testy) != (verty[j]>testy)) &&
					(testx < (vertx[j]-vertx[i]) * (testy-verty[i]) / (verty[j]-verty[i]) + vertx[i])) {
				c = !c;
			}
		}
		return c;
	}
	
	/*
	 * isPointIn4 method was derived from 
	 * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
	 * 
	 * Thanks to
	 * * Loren Albertazzi, for finding an error in the treatment of multiple component polygons.
	 * * Mark Sullivan for suggesting the documentation change from "vertical" to "horizontal".
	 * * Christopher Sargent for suggesting an optimization based on Joe O'Rourke's Computational Geometry in C.
	 * 
	 * @return true if point (testx, texty) is inside of polygon made of points stored in (vertx, verty)
	 */
	public static boolean isPointIn4(double[] vertx, double[] verty,
			double testx, double testy) {
		boolean c = false;
		if(((verty[0]>testy) != (verty[3]>testy)) &&
				(testx < (vertx[3]-vertx[0]) * (testy-verty[0]) / (verty[3]-verty[0]) + vertx[0])) {
			c = !c;
		}
		if(((verty[1]>testy) != (verty[0]>testy)) &&
				(testx < (vertx[0]-vertx[1]) * (testy-verty[1]) / (verty[0]-verty[1]) + vertx[1])) {
			c = !c;
		}
		if(((verty[2]>testy) != (verty[1]>testy)) &&
				(testx < (vertx[1]-vertx[2]) * (testy-verty[2]) / (verty[1]-verty[2]) + vertx[2])) {
			c = !c;
		}
		if(((verty[3]>testy) != (verty[2]>testy)) &&
				(testx < (vertx[2]-vertx[3]) * (testy-verty[3]) / (verty[2]-verty[3]) + vertx[3])) {
			c = !c;
		}
		return c;
	}
	
	/*
	 * Version-indepent of `return ((CraftEntity) entity).getHandle()`
	 * 
	 * @return ((CraftEntity) entity).getHandle()
	 */
	public static Object getEntityHandle(Entity e) {
		Class<?> clz = e.getClass();
		if(GunAPI.handleMethod != null) {
			try {
				return GunAPI.handleMethod.invoke(e);
			} catch(Throwable t) {}
		}
		Method first = null;
		while(clz != null) {
			try {
				Method m = clz.getDeclaredMethod("getHandle");
				m.setAccessible(true);
				if(first == null) {
					first = m;
				}
				if(clz.getName().endsWith(".CraftEntity") || clz.getName().endsWith(".Entity")) {
					Object handle = m.invoke(e);
					GunAPI.handleMethod = m;
					return handle;
				}
			} catch(Throwable t) {}
			clz = clz.getSuperclass();
		}
		try {
			Object handle = first.invoke(e);
			GunAPI.handleMethod = first;
			return handle;
		} catch(Throwable t) {}
		return null;
	}
	
	/*
	 * Get entity width
	 * 
	 * @return Entity width
	 */
	public static double getWidth(Entity e) {
		try {
			Object handle = GunAPI.getEntityHandle(e);
			if(GunAPI.widthField != null) {
				try {
					return (((Number) GunAPI.widthField.get(handle)).doubleValue() + 0.3);
				} catch(Throwable t2) {}
			}
			Class<?> hc = handle.getClass();
			while(hc != null) {
				try {
					Field f = hc.getDeclaredField("width");
					f.setAccessible(true);
					double n = ((Number) f.get(handle)).doubleValue();
					GunAPI.widthField = f;
					return (n + 0.3);
				} catch(Throwable t3) {}
				hc = hc.getSuperclass();
			}
		} catch(Throwable t) {}
		return GunAPI.widthDefault;
	}
	
	/*
	 * Get entity height
	 * 
	 * @return Entity height
	 */
	public static double getHeight(Entity e) {
		try {
			try {
				Object handle = GunAPI.getEntityHandle(e);
				if(GunAPI.heightField != null) {
					try {
						double ret = ((Number) GunAPI.heightField.get(handle)).doubleValue();
						if(ret != 0) {
							return ret;
						}
						return GunAPI.heightDefault;
					} catch(Throwable t2) {}
				}
				Class<?> hc = handle.getClass();
				while(hc != null) {
					try {
						Field f = hc.getDeclaredField("height");
						f.setAccessible(true);
						double n = ((Number) f.get(handle)).doubleValue();
						GunAPI.heightField = f;
						if(n != 0) {
							return n;
						}
						return GunAPI.heightDefault;
					} catch(Throwable t3) {}
					hc = hc.getSuperclass();
				}
			} catch(Throwable t) {}
		} catch(Throwable t) {}
		return GunAPI.heightDefault;
	}
	
	/*
	 * Get entity length
	 * 
	 * @return Entity length
	 */
	public static double getLength(Entity e) {
		try {
			Object handle = GunAPI.getEntityHandle(e);
			if(GunAPI.lengthField != null) {
				try {
					return ((Number) GunAPI.lengthField.get(handle)).doubleValue();
				} catch(Throwable t2) {}
			}
			Class<?> hc = handle.getClass();
			while(hc != null) {
				try {
					Field f = hc.getDeclaredField("length");
					f.setAccessible(true);
					double n = ((Number) f.get(handle)).doubleValue();
					GunAPI.lengthField = f;
					return n;
				} catch(Throwable t3) {}
				hc = hc.getSuperclass();
			}
		} catch(Throwable t) {}
		return GunAPI.lengthDefault;
	}
	
	/*
	 * Get entity head width
	 * 
	 * @return Entity head width
	 */
	public static double getHeadWidth(Entity e) {
		if(!(e instanceof LivingEntity) || e instanceof Slime || e instanceof Silverfish) {
			return 0;
		}
		return GunAPI.headWidthDefault;
	}
	
	/*
	 * Get entity head height
	 * 
	 * @return Entity head height
	 */
	public static double getHeadHeight(Entity e) {
		if(!(e instanceof LivingEntity) || e instanceof Slime || e instanceof Silverfish) {
			return 0;
		}
		return GunAPI.headHeightDefault;
	}
	
	/*
	 * Get entity head length
	 * 
	 * @return Entity head length
	 */
	public static double getHeadLength(Entity e) {
		if(!(e instanceof LivingEntity) || e instanceof Slime || e instanceof Silverfish) {
			return 0;
		}
		return GunAPI.headLengthDefault;
	}
	
	/*
	 * Get entity head cord; Location head = Location#getDirection() + GunAPI.getHeadCord();
	 * 
	 * @return Entity head cord (loc = rotation + cord)
	 */
	public static double getHeadCord(Entity e) {
		if(!(e instanceof LivingEntity)) {
			return 0;
		}
		boolean isAdult = true;
		if(e instanceof Ageable) {
			isAdult = ((Ageable) e).isAdult();
		}
		if(e instanceof Wolf) {
			return (isAdult) ? 0.4 : 0.1;
		}
		return 0;
	}
	
	/*
	 * Modify minecraft angle to work propertly with other code
	 * 
	 * @return Modified angle (yaw)
	 */
	public static double correctAngle(double in) {
		return (360 - ((in + 360) % 360)) + 90;
	}
	
	/*
	 * Rotate point (x, y) stored in first arr parameter by given angle
	 * 
	 * @return nothing. Values are stored in arr
	 */
	public static void rotatePoint2D(double[] arr, double angle) {
		angle = Math.toRadians(angle);
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double x = arr[0];
		double y = arr[1];
		arr[0] = ((x * cos) + (y * sin));
		arr[1] = (-(x * sin) + (y * cos));
	}
	
	/*
	 * Rotate 4 points (x, y) stored in first and second double[] parameters by given angle
	 * 
	 * @return nothing. Values are stored in x and y arrays
	 */
	public static void rotateRectangle2D(double[] x, double[] y, double angle) {
		double ox1 = x[0];
		double oy1 = y[0];
		double ox2 = x[1];
		double oy2 = y[1];
		double ox3 = x[2];
		double oy3 = y[2];
		double ox4 = x[3];
		double oy4 = y[3];
		double x1 = ox3 - ox1;
		double z1 = oy3 - oy1;
		double x2 = ox4 - ox2;
		double z2 = oy4 - oy2;
		double hx1 = x1 / 2d;
		double hz1 = z1 / 2d;
		double hx2 = x2 / 2d;
		double hz2 = z2 / 2d;
		
		double[] v1 = new double[] {hx1, hz1};
		double[] v2 = new double[] {hx2, hz2};
		
		GunAPI.rotatePoint2D(v1, angle);
		GunAPI.rotatePoint2D(v2, angle);
		
		x[0] = (ox1 + hx1 - v1[0]);
		y[0] = (oy1 + hz1 - v1[1]);
		x[1] = (ox2 + hx2 - v2[0]);
		y[1] = (oy2 + hz2 - v2[1]);
		x[2] = (ox3 - hx1 + v1[0]);
		y[2] = (oy3 - hz1 + v1[1]);
		x[3] = (ox4 - hx2 + v2[0]);
		y[3] = (oy4 - hz2 + v2[1]);
	}
	
	/*
	 * Compute number of cycles from point (x, y, z) arguments and radius
	 * by Math.round(radius / Math.max(x, y, z))
	 * 
	 * @return Number of cycles needed to explore blocks and entities in given radius
	 */
	public static int cyclesFromRadius(double[] xyz, double radius) {
		int ret = 0;
		
		double x = Math.abs(xyz[0]);
		double y = Math.abs(xyz[1]);
		double z = Math.abs(xyz[2]);
		
		if(x >= y && x >= z) {
			ret = (int) ((radius / x) + 0.5);
		} else if(y >= x && y >= z) {
			ret = (int) ((radius / y) + 0.5);
		} else if(z >= x && z >= y) {
			ret = (int) ((radius / z) + 0.5);
		}
		
		return ret;
	}
	
	/*
	 * Walk given distance by given yaw
	 * 
	 * @return nothing. Data are stored in xy array
	 */
	public static void walk2D(double[] xy, double distance, double yaw) {
		xy[0] -= (distance * Math.sin(Math.toRadians(yaw)));
		xy[1] += (distance * Math.cos(Math.toRadians(yaw)));
	}
	
	/*
	 * Walk given distance by given yaw and pitch
	 * 
	 * @return nothing. Data are stored in xyz array
	 */
	public static void walk3D(double[] xyz, double distance, double yaw, double pitch) {
		double[] vec = new double[3];
		GunAPI.getDirection(vec, pitch, yaw);
		GunAPI.normalize(vec, distance);
		xyz[0] += vec[0];
		xyz[1] += vec[1];
		xyz[2] += vec[2];
	}
	
	/*
	 * Maximize point (x, y, z) with given value, that mean
	 * biggest value with be equal to f parameter
	 * 
	 * @return nothing. Output is stored in xyz parameter
	 */
	public static void maximize(double[] xyz, double f) {
		double x = xyz[0];
		double y = xyz[1];
		double z = xyz[2];
		boolean bx = false;
		boolean by = false;
		boolean bz = false;
		
		if(x < 0d) {
			x = -x;
			bx = true;
		}
		if(y < 0d) {
			y = -y;
			by = true;
		}
		if(z < 0d) {
			z = -z;
			bz = true;
		}
		
		double nas = 0d;
		
		if(x >= y && x >= z) {
			nas = f / x;
			x = f;
			y = y * nas;
			z = z * nas;
		} else if(y >= x && y >= z) {
			nas = f / y;
			y = f;
			x = x * nas;
			z = z * nas;
		} else if(z >= x && z >= y) {
			nas = f / z;
			z = f;
			x = x * nas;
			y = y * nas;
		}
		
		if(bx) {
			x = -x;
		}
		if(by) {
			y = -y;
		}
		if(bz) {
			z = -z;
		}
		
		xyz[0] = x;
		xyz[1] = y;
		xyz[2] = z;
	}
	
	/*
	 * Normalize point (x, y, z) with given value
	 * 
	 * @return nothing. Output is stored in xyz parameter
	 */
	public static void normalize(double[] xyz, double f) {
		double sqr = Math.sqrt((xyz[0] * xyz[0]) + (xyz[1] * xyz[1]) + (xyz[2] * xyz[2]));
		sqr = (1 / sqr) * f;
		xyz[0] *= sqr;
		xyz[1] *= sqr;
		xyz[2] *= sqr;
	}
	
	/*
	 * getDirection by entered yaw and pitch
	 * 
	 * @return nothing. Computed direction vector (x,y,z) is stored in the first parameter
	 */
	public static void getDirection(double[] out, double yaw, double pitch) {
		double p = Math.toDegrees(pitch);
		double y = Math.toDegrees(yaw);
		double xz = Math.cos(p);

		out[0] = (-xz * Math.sin(y));
		out[1] = -(Math.sin(p));
		out[2] = (xz * Math.cos(y));
	}
	
	/*
	 * Cache entities for fast & easy collide checking
	 * 
	 * @return nothing. Results are stored in ShootedEntity
	 */
	public static void cacheEntities(Collection<ShootedEntity> out, Collection<? extends Object> in) {
		Iterator<? extends Object> it = in.iterator();
		while(it.hasNext()) {
			Object e = it.next();
			if(e instanceof LivingEntity) {
				out.add(new ShootedEntity((LivingEntity) e));
			}
		}
	}
	
	/*
	 * Get shooted entities in given location. Add them to first out parameter 
	 * and remove them from second parameter.
	 * 
	 * @return nothing. Shooted entities are added into first out parameter
	 */
	public static void getShootedEntities(Collection<ShootedEntity> out, 
			Collection<ShootedEntity> in, double[] loc, boolean remove) {
		if(remove) {
			Iterator<ShootedEntity> it = in.iterator();
			while(it.hasNext()) {
				ShootedEntity sec = it.next();
				int res = sec.collide(loc);
				if(res == 2) {
					sec.wasHeadshot = true;
					out.add(sec);
					it.remove();
				} else if(res == 1) {
					sec.wasHeadshot = false;
					out.add(sec);
					it.remove();
				}
			}
		} else if(out instanceof Set) {
			Iterator<ShootedEntity> it = in.iterator();
			while(it.hasNext()) {
				ShootedEntity sec = it.next();
				int res = sec.collide(loc);
				if(res == 2) {
					sec.wasHeadshot = true;
					out.add(sec);
				} else if(res == 1) {
					sec.wasHeadshot = false;
					out.add(sec);
				}
			}
		} else {
			Iterator<ShootedEntity> it = in.iterator();
			while(it.hasNext()) {
				ShootedEntity sec = it.next();
				int res = sec.collide(loc);
				if(res == 2) {
					sec.wasHeadshot = true;
					if(!out.contains(sec)) {
						out.add(sec);
					}
				} else if(res == 1) {
					sec.wasHeadshot = false;
					if(!out.contains(sec)) {
						out.add(sec);
					}
				}
			}
		}
	}
	
	/*
	 * Get shooted entities in given vector -> radius. Add them to first out parameter 
	 * and remove them from second parameter.
	 * 
	 * @return nothing. Shooted entities are added into first out parameter
	 */
	public static void getShootedEntities(Collection<ShootedEntity> out, 
			Collection<ShootedEntity> in, double[] loc, double[] vec, int cycles, boolean remove) {
		if(remove) {
			while(cycles-- != 0) {
				Iterator<ShootedEntity> it = in.iterator();
				while(it.hasNext()) {
					ShootedEntity sec = it.next();
					int res = sec.collide(loc);
					if(res == 2) {
						sec.wasHeadshot = true;
						out.add(sec);
						it.remove();
					} else if(res == 1) {
						sec.wasHeadshot = false;
						out.add(sec);
						it.remove();
					}
				}
				loc[0] += vec[0];
				loc[1] += vec[1];
				loc[2] += vec[2];
			}
		} else if(out instanceof Set) {
			while(cycles-- != 0) {
				Iterator<ShootedEntity> it = in.iterator();
				while(it.hasNext()) {
					ShootedEntity sec = it.next();
					int res = sec.collide(loc);
					if(res == 2) {
						sec.wasHeadshot = true;
						out.add(sec);
					} else if(res == 1) {
						sec.wasHeadshot = false;
						out.add(sec);
					}
				}
				loc[0] += vec[0];
				loc[1] += vec[1];
				loc[2] += vec[2];
			}
		} else {
			while(cycles-- != 0) {
				Iterator<ShootedEntity> it = in.iterator();
				while(it.hasNext()) {
					ShootedEntity sec = it.next();
					int res = sec.collide(loc);
					if(res == 2) {
						sec.wasHeadshot = true;
						if(!out.contains(sec)) {
							out.add(sec);
						}
					} else if(res == 1) {
						sec.wasHeadshot = false;
						if(!out.contains(sec)) {
							out.add(sec);
						}
					}
				}
				loc[0] += vec[0];
				loc[1] += vec[1];
				loc[2] += vec[2];
			}
		}
	}
	
	/*
	 * Get all shooted blocks in given vector -> radius. Add them to first out parameter 
	 * and remove them from second parameter.
	 * 
	 * @return nothing. Shooted blocks are added into first out parameter
	 */
	public static void getShootedBlocks(Collection<Block> out, 
			World w, double[] loc, double[] vec, int cycles) {
		if(vec[0] >= 1 || vec[1] >= 1 || vec[2] >= 1) {
			while(cycles-- != 0) {
				out.add(w.getBlockAt(Location.locToBlock(loc[0]), 
						Location.locToBlock(loc[1]), Location.locToBlock(loc[2])));
				loc[0] += vec[0];
				loc[1] += vec[1];
				loc[2] += vec[2];
			}
		} else {
			int prevx = Integer.MAX_VALUE, prevy = Integer.MAX_VALUE, prevz = Integer.MAX_VALUE;
			int curx = Integer.MAX_VALUE, cury = Integer.MAX_VALUE, curz = Integer.MAX_VALUE;
			while(cycles-- != 0) {
				curx = Location.locToBlock(loc[0]);
				cury = Location.locToBlock(loc[1]);
				curz = Location.locToBlock(loc[2]);
				if(prevx != curx || prevy != cury || prevz != curz) {
					out.add(w.getBlockAt(curx, cury, curz));
					prevx = curx;
					prevy = cury;
					prevz = curz;
				}
				loc[0] += vec[0];
				loc[1] += vec[1];
				loc[2] += vec[2];
			}
		}
	}
	
}
