package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import main.MainPanel;
import util.MathTools;
import util.Point3D;
import util.Vector;
import util.Vector3D;

public class Board {
	
	public String function = "x + 0.5z^2+5";
	
	public Vector3D cameraPos = new Vector3D(0, 1, -20);
	public Vector3D cameraDir = new Vector3D(0, 0, 1);
	
	public double xRot = 0;
	public double yRot = 0;
	
	public boolean mousePressed = false;
	
	public java.awt.Point prevMouse = new java.awt.Point(0, 0);
	
	public double graphIncrement = 0.5;
	
	public double graphMinX = -20;
	public double graphMaxX = 20;
	public double graphMinZ = -20;
	public double graphMaxZ = 20;
	
	public boolean forward = false;
	public boolean backward = false;
	public boolean left = false;
	public boolean right = false;
	public boolean up = false;
	public boolean down = false;
	
	public double moveSpeed = 0.05;

	public Board() {
		
	}
	
	public void draw(Graphics g, java.awt.Point mouse) {
		
		//****camera controls
		
		//camera dir
		double xDiff = mouse.x - prevMouse.x;
		double yDiff = mouse.y - prevMouse.y;
		
		if(mousePressed) {
			this.xRot += yDiff * 0.01;
			this.yRot += xDiff * 0.01;
		}
		
		this.prevMouse = new java.awt.Point(mouse.x, mouse.y);
		
		//movement
		Vector3D cameraDir = new Vector3D(this.cameraDir);
		cameraDir.rotateX(xRot);
		cameraDir.rotateY(yRot);
		
		Vector3D forwardVector = new Vector3D(cameraDir);
		forwardVector.setMagnitude(this.moveSpeed);
		
		if(forward) {
			this.cameraPos.addVector(forwardVector);
		}
		if(backward) {
			this.cameraPos.subtractVector(forwardVector);
		}
		
		Vector3D leftVector = new Vector3D(cameraDir.x, 0, cameraDir.z);
		leftVector.rotateY(Math.toRadians(-90));
		leftVector.setMagnitude(this.moveSpeed);
		
		if(left) {
			this.cameraPos.addVector(leftVector);
		}
		if(right) {
			this.cameraPos.subtractVector(leftVector);
		}
		
		if(up) {
			this.cameraPos.y += moveSpeed;
		}
		if(down) {
			this.cameraPos.y -= moveSpeed;
		}
		
		
		//****draw 
		
		//draw background
		g.fillRect(0, 0, MainPanel.WIDTH * 2, MainPanel.HEIGHT * 2);
		
		ArrayList<Vector3D> graphedPoints = new ArrayList<Vector3D>();
		
		//generate and draw points from function
		
		for(double i = graphMinX; i <= graphMaxX; i += graphIncrement) {
			for(double j = graphMinZ; j <= graphMaxZ; j += graphIncrement) {
				Point3D nextPoint = new Point3D(i, Math.sin(i / 2) * Math.cos(j / 2), j);
				
				//apply camera transformations to point
				nextPoint.addVector(new Vector3D(-cameraPos.x, -cameraPos.y, -cameraPos.z));
				nextPoint.rotateY(-yRot);
				nextPoint.rotateX(-xRot);
				
				//if point is behind camera, then don't draw it
				if(nextPoint.z < 0) {
					continue;
				}
				
				//project point
				double[] wOut = new double[1];	//don't think i'll need this later, but just in case
				nextPoint = MathTools.projectPoint(nextPoint, wOut);
				
				
				//if point is off the screen, then don't draw it
				//point screen coordinates are normalized, so check against normalized coordinates
				if(nextPoint.x < -1 || nextPoint.x > 1 || nextPoint.y < -1 || nextPoint.y > 1) {
					continue;
				}
				
				//scale normalized point to screen
				nextPoint = MathTools.scalePoint(nextPoint);
				
				//draw point
				g.setColor(Color.WHITE);
				g.fillRect((int) nextPoint.x - 1, (int) nextPoint.y - 1, 3, 3);
			}
		}
		
	}
	
	public void mousePressed(MouseEvent arg0) {
		this.mousePressed = true;
	}
	
	public void mouseReleased(MouseEvent arg0) {
		this.mousePressed = false;
	}
	
	public void keyPressed(int k) {
		switch(k) {
		case KeyEvent.VK_W:
			this.forward = true;
			break;
			
		case KeyEvent.VK_S:
			this.backward = true;
			break;
			
		case KeyEvent.VK_A:
			this.left = true;
			break;
			
		case KeyEvent.VK_D:
			this.right = true;
			break;
			
		case KeyEvent.VK_SPACE:
			this.up = true;
			break;
			
		case KeyEvent.VK_CONTROL:
			this.down = true;
		}
	}
	
	public void keyReleased(int k) {
		switch(k) {
		case KeyEvent.VK_W:
			this.forward = false;
			break;
			
		case KeyEvent.VK_S:
			this.backward = false;
			break;
			
		case KeyEvent.VK_A:
			this.left = false;
			break;
			
		case KeyEvent.VK_D:
			this.right = false;
			break;
			
		case KeyEvent.VK_SPACE:
			this.up = false;
			break;
			
		case KeyEvent.VK_CONTROL:
			this.down = false;
		}
	}
	
}
