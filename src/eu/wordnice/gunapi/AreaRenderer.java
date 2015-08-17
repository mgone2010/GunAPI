package eu.wordnice.gunapi;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class AreaRenderer extends MapRenderer {

	public Entity ent = null;
	public long next = 0;
	
	public AreaRenderer(Entity ent) {
		this.ent = ent;
	}

	@Override
	public void render(MapView mw, MapCanvas canv, Player p) {
		if(this.next > System.currentTimeMillis()) {
			return;
		}
		this.next = System.currentTimeMillis() + 100L;
		
		ShootedEntity se = new ShootedEntity(new double[2], new double[2], this.ent);
				
		BufferedImage bf = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bf.getGraphics();
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 128, 128);

		g.setColor(new Color(255, 0, 0, 128));
		Polygon pl = new Polygon();
		double fromx = Math.min(Math.min(se.body_x[0], se.body_x[1]), 
				Math.min(se.body_x[2], se.body_x[3])) - 0.5;
		double fromz = Math.min(Math.min(se.body_z[0], se.body_z[1]), 
				Math.min(se.body_z[2], se.body_z[3])) - 0.5;
		pl.addPoint((int) Math.round((se.body_x[0] - fromx) * 40), (int) Math.round((se.body_z[0] - fromz) * 40));
		pl.addPoint((int) Math.round((se.body_x[1] - fromx) * 40), (int) Math.round((se.body_z[1] - fromz) * 40));
		pl.addPoint((int) Math.round((se.body_x[2] - fromx) * 40), (int) Math.round((se.body_z[2] - fromz) * 40));
		pl.addPoint((int) Math.round((se.body_x[3] - fromx) * 40), (int) Math.round((se.body_z[3] - fromz) * 40));
		g.fillPolygon(pl);
		
		g.setColor(new Color(0, 255, 0, 128));
		pl = new Polygon();
		pl.addPoint((int) Math.round((se.head_x[0] - fromx) * 40), (int) Math.round((se.head_z[0] - fromz) * 40));
		pl.addPoint((int) Math.round((se.head_x[1] - fromx) * 40), (int) Math.round((se.head_z[1] - fromz) * 40));
		pl.addPoint((int) Math.round((se.head_x[2] - fromx) * 40), (int) Math.round((se.head_z[2] - fromz) * 40));
		pl.addPoint((int) Math.round((se.head_x[3] - fromx) * 40), (int) Math.round((se.head_z[3] - fromz) * 40));
		g.fillPolygon(pl);
		
		canv.drawImage(0, 0, bf);
		p.sendMap(mw);
	}
	
	
	
}
