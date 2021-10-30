package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import button.ButtonManager;
import button.SliderButton;
import button.ToggleButton;
import main.MainPanel;
import util.MathTools;
import util.PerlinNoise;
import util.Point;
import util.Point3D;
import util.Vector;
import util.Vector3D;

public class Board {
	
	ButtonManager bm;
	
	public PerlinNoise pnOcean = new PerlinNoise(112, 1, 0.1, 1, 2);
	public PerlinNoise pnMountains = new PerlinNoise(234, 0.1, 0.7, 15, 5);
	public PerlinNoise pnMountainRoughness = new PerlinNoise(1230, 0.1, 2.5, 2, 5);
	
	public Vector3D cameraPos = new Vector3D(0, 5, 0);
	public Vector3D cameraDir = new Vector3D(0, 0, 1);
	public Vector3D lightDir = new Vector3D(0, -1, 0);
	
	public double xRot = 0;
	public double yRot = 0;
	
	public boolean mousePressed = false;
	
	public java.awt.Point prevMouse = new java.awt.Point(0, 0);
	
	public static double graphIncrement = 0.2;
	
	public double graphMinX = -15;
	public double graphMaxX = 15;
	public double graphMinZ = -15;
	public double graphMaxZ = 15;
	
	public boolean forward = false;
	public boolean backward = false;
	public boolean left = false;
	public boolean right = false;
	public boolean up = false;
	public boolean down = false;
	
	public double moveSpeed = 0.27;
	
	public boolean drawPoints = false;
	public boolean drawFaces = true;

	public Board() {
		this.bm = new ButtonManager();
		
		bm.addToggleButton(new ToggleButton(690, 10, 100, 25, "Draw Faces"));						bm.toggleButtons.get(0).setToggled(true);
		
		bm.addSliderButton(new SliderButton(10, 30, 210, 10, 1, 200, "Ocean Seed"));		bm.sliderButtons.get(0).setVal(1);
		bm.addSliderButton(new SliderButton(10, 60, 210, 10, 1, 200, "Ocean Frequency"));	bm.sliderButtons.get(1).setVal(1);
		bm.addSliderButton(new SliderButton(10, 90, 210, 10, 1, 200, "Ocean Amplitude"));	bm.sliderButtons.get(2).setVal(13);
		
		bm.addSliderButton(new SliderButton(230, 30, 210, 10, 1, 200, "Mountains Seed"));		bm.sliderButtons.get(3).setVal(30);
		bm.addSliderButton(new SliderButton(230, 60, 210, 10, 1, 200, "Mountains Frequency"));	bm.sliderButtons.get(4).setVal(7);
		bm.addSliderButton(new SliderButton(230, 90, 210, 10, 1, 200, "Mountains Amplitude"));	bm.sliderButtons.get(5).setVal(81);
		
		bm.addSliderButton(new SliderButton(460, 30, 210, 10, 1, 200, "Roughness Seed"));		bm.sliderButtons.get(6).setVal(90);
		bm.addSliderButton(new SliderButton(460, 60, 210, 10, 1, 200, "Roughness Frequency"));	bm.sliderButtons.get(7).setVal(48);
		bm.addSliderButton(new SliderButton(460, 90, 210, 10, 1, 200, "Roughness Amplitude"));	bm.sliderButtons.get(8).setVal(87);
		
		
		bm.addSliderButton(new SliderButton(10, 120, 210, 10, 1, 200, "Terrain Detail"));		bm.sliderButtons.get(9).setVal(5);
		bm.addSliderButton(new SliderButton(230, 120, 210, 10, 1, 200, "Terrain Size"));		bm.sliderButtons.get(10).setVal(15);
		
		
	}
	
	public void draw(Graphics g, java.awt.Point mouse) {
		
		this.bm.tick(mouse);
		
		//setting draw options
		this.drawFaces = bm.toggleButtons.get(0).getToggled();
		this.drawPoints = !bm.toggleButtons.get(0).getToggled();
		
		//setting seed based on sliders
		this.pnOcean.set(bm.sliderButtons.get(0).getVal(), 1, (double) bm.sliderButtons.get(1).getVal() / 100d, (double) bm.sliderButtons.get(2).getVal() / 10d, 2);
		this.pnMountains.set(bm.sliderButtons.get(3).getVal(), 1, (double) bm.sliderButtons.get(4).getVal() / 100d, (double) bm.sliderButtons.get(5).getVal() / 10d, 2);
		this.pnMountainRoughness.set(bm.sliderButtons.get(6).getVal(), 1, (double) bm.sliderButtons.get(7).getVal() / 100d, (double) bm.sliderButtons.get(8).getVal() / 10d, 2);
		
		//setting terrain
		Board.graphIncrement = 1d / (double) bm.sliderButtons.get(9).getVal();
		this.graphMaxX = bm.sliderButtons.get(10).getVal();
		this.graphMinX = -bm.sliderButtons.get(10).getVal();
		this.graphMaxZ = bm.sliderButtons.get(10).getVal();
		this.graphMinZ = -bm.sliderButtons.get(10).getVal();
		
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
		
		ArrayList<ArrayList<Point3D>> graphPoints = new ArrayList<ArrayList<Point3D>>();
		
		//generate graph points
		
		double xRem = this.cameraPos.x % this.graphIncrement;
		double yRem = this.cameraPos.z % this.graphIncrement;
		
		double xMultiple = this.cameraPos.x - xRem;
		double yMultiple = this.cameraPos.z - yRem;
		
		
		for(double i = graphMinX; i <= graphMaxX; i += graphIncrement) {
			graphPoints.add(new ArrayList<Point3D>());
			for(double j = graphMinZ; j <= graphMaxZ; j += graphIncrement) {
				
				
				
				double x = i + xMultiple;
				double z = j + yMultiple;
				
				Point3D nextPoint = new Point3D(i - xRem, Math.max(0, this.pnOcean.getHeight(x, z) * (this.pnMountains.getHeight(x, z) + this.pnMountainRoughness.getHeight(x, z))), j - yRem);
				
				graphPoints.get(graphPoints.size() - 1).add(nextPoint);
			}
		}
		
		if(this.drawFaces) {
			//generate the triangles
			
			ArrayList<Triangle> graphTris = new ArrayList<Triangle>();
			ArrayList<Triangle> clippedTris = new ArrayList<Triangle>();
			ArrayList<Triangle> projectedTris = new ArrayList<Triangle>();
			
			
			//TODO figure a way to preprocess triangles so we don't have to do this every frame
			
			//generate the triangles from the graph
			for(int i = 0; i < graphPoints.size(); i ++) {
				for(int j = (i % 2 == 0? 0 : 1); j < graphPoints.get(i).size(); j += 2) {
					//first point then second one to maintain cw rotation
					if(i + 1 < graphPoints.size() && j + 1 < graphPoints.get(i).size()) {
						graphTris.add(new Triangle(graphPoints.get(i).get(j), graphPoints.get(i + 1).get(j), graphPoints.get(i).get(j + 1)));
					}
					if(j + 1 < graphPoints.get(i).size() && i - 1 >= 0) {
						graphTris.add(new Triangle(graphPoints.get(i).get(j), graphPoints.get(i).get(j + 1), graphPoints.get(i - 1).get(j)));
					}
					if(i - 1 >= 0 && j - 1 >= 0) {
						graphTris.add(new Triangle(graphPoints.get(i).get(j), graphPoints.get(i - 1).get(j), graphPoints.get(i).get(j - 1)));
					}
					if(j - 1 >= 0 && i + 1 < graphPoints.size()) {
						graphTris.add(new Triangle(graphPoints.get(i).get(j), graphPoints.get(i).get(j - 1), graphPoints.get(i + 1).get(j)));
					}
				}
			}
			
			//rotate the light dir with world
			Vector3D lightDir = new Vector3D(this.lightDir);
			lightDir.rotateY(-yRot);
			lightDir.rotateX(-xRot);
			
			//view the points
			//backface culling and clip tris against camera
			for(int i = 0; i < graphTris.size(); i++) {
				Triangle nextTri = graphTris.get(i);
				
				//apply camera transformations
				for(int j = 0; j < 3; j++) {
					nextTri.p[j] = this.cameraTransformation(nextTri.p[j]);
				}
				
				//do backface culling
				//calculating normal
				Vector3D v1 = new Vector3D(nextTri.p[0], nextTri.p[2]);
				Vector3D v2 = new Vector3D(nextTri.p[0], nextTri.p[1]);
				
				Vector3D normal = MathTools.crossProduct(v1, v2);
				
				Point3D pa = new Point3D(nextTri.p[0]);
				Point3D pb = new Point3D(nextTri.p[1]);
				Point3D pc = new Point3D(nextTri.p[2]);
				
				Vector3D avgPoint = new Vector3D((pa.x + pb.x + pc.x) / 3d, (pa.y + pb.y + pc.y) / 3d, (pa.z + pb.z + pc.z) / 3d);
				Vector3D vecToCamera = new Vector3D(avgPoint, new Vector3D(0, 0, 0));
				
				//if the normal is pointed at the camera
				if(MathTools.dotProduct3D(normal, vecToCamera) > 0) {
					//calculate triangle lighting
					normal.normalize();
					double dot = MathTools.dotProduct3D(normal, lightDir);
					dot *= -1;	//do this because normal is facing the light dir if it's lighted. 
					//System.out.println(dot);
					
					//decide the color of the triangle
					
					Color nextColor = Color.blue;
					
					if(nextTri.avgMapY <= 0) {
						nextColor = new Color(1, 120, 189);	//ocean, blue
					}
					else if(nextTri.maxSlope > 1.5 && nextTri.avgMapY <= 3.5) {
						nextColor = new Color(111, 125, 126);	//cliff, grey
					}
					else if(nextTri.avgMapY <= 0.1 && nextTri.maxSlope <= 0.2) {
						nextColor = new Color(250, 234, 183);	//sand, yellow
					}
					else if(nextTri.avgMapY <= 3){
						nextColor = new Color(25, 162, 58);	//grass, green
					}
					else {
						nextColor = new Color(255, 255, 255);	//snow, white
					}
					
					
					int red = (int) Math.max(0, nextColor.getRed() * dot);
					int green = (int) Math.max(0, nextColor.getGreen() * dot);
					int blue = (int) Math.max(0, nextColor.getBlue() * dot);
					
					nextTri.color = new Color(red, green, blue);
					
					//clip triangle against camera
					ArrayList<Point3D[]> outTri = MathTools.triangleClipAgainstPlane(new Point3D(0, 0, 0.1), this.cameraDir, nextTri.p, nextTri.tex, null, nextTri.w, null);
				
					for(Point3D[] p : outTri) {
						Triangle newTri = new Triangle(p[0], p[1], p[2]);
						newTri.color = nextTri.color;
						clippedTris.add(newTri);
					}
				}
				
			}
			
			//project tris and save depth info
			for(int i = 0; i < clippedTris.size(); i++) {
				Triangle nextTri = clippedTris.get(i);
				double[] outW = new double[1];
				nextTri.depth = (nextTri.p[0].z + nextTri.p[1].z + nextTri.p[2].z) / 3d; 
				nextTri.p[0] = MathTools.projectPoint(nextTri.p[0], outW);
				nextTri.p[1] = MathTools.projectPoint(nextTri.p[1], outW);
				nextTri.p[2] = MathTools.projectPoint(nextTri.p[2], outW);
				
				
				projectedTris.add(nextTri);
			}
			
			projectedTris.sort((a, b) -> {return -Double.compare(a.depth, b.depth);});
			
			//scale and draw tris
			for(int i = 0; i < projectedTris.size(); i++) {
				
				Triangle nextTri = projectedTris.get(i);
				
				int[] x = new int[3];
				int[] y = new int[3];
				
				for(int j = 0; j < 3; j++) {
					Point3D scaled = MathTools.scalePoint(nextTri.p[j]);
					x[j] = (int) scaled.x;
					y[j] = (int) scaled.y;
				}
				
				g.setColor(nextTri.color);
				g.fillPolygon(x, y, 3);
				
			}
		}
		
		if(this.drawPoints) {
			//generate and draw points from function
			
			g.setColor(Color.WHITE);
			for(int i = 0; i < graphPoints.size(); i++) {
				for(int j = 0; j < graphPoints.size(); j++) {
					Point3D nextPoint = graphPoints.get(i).get(j);
					
					//apply camera transformations to point
					nextPoint = this.cameraTransformation(nextPoint);
					
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
					g.fillRect((int) nextPoint.x - 1, (int) nextPoint.y - 1, 3, 3);
				}
			}
		}
		
		this.bm.draw(g);
		
	}
	
	public Point3D cameraTransformation(Point3D p) {
		Point3D out = new Point3D(p);
		out.y -= this.cameraPos.y;
		out.rotateY(-yRot);
		out.rotateX(-xRot);
		return out;
	}
	
	public void mousePressed(MouseEvent arg0) {
		this.bm.pressed(arg0);
		if(this.bm.getPressed(arg0)) {
			System.out.println("YES");
			return;
		}
		this.mousePressed = true;
	}
	
	public void mouseReleased(MouseEvent arg0) {
		this.bm.mouseReleased();
		this.mousePressed = false;
	}
	
	public void mouseClicked(MouseEvent arg0) {
		this.bm.buttonClicked(arg0);
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

class Triangle {
	
	public Point3D[] p;
	public Point[] tex;
	public double[] w;
	
	public double depth;
	public Color color;
	
	public double avgMapY;	//y position in real space
	public double maxSlope;	
	
	public Triangle(Point3D p1, Point3D p2, Point3D p3) {
		p = new Point3D[] {new Point3D(p1), new Point3D(p2), new Point3D(p3)};
		tex = new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0)};
		w = new double[3];
		color = Color.WHITE;
		avgMapY = (p[0].y + p[1].y + p[2].y) / 3d; 
		
		//get slope of plane defined by the triangle
		
		Vector3D v1 = new Vector3D(p[0], p[1]);
		Vector3D v2 = new Vector3D(p[0], p[2]);
		
		Vector3D normal = MathTools.crossProduct(v1, v2);
		normal.normalize();
		
		Vector3D maximumSlope = MathTools.crossProduct(normal, new Vector3D(1, 0, 0));
		maximumSlope.normalize();
		
		double yDiff = Math.abs(maximumSlope.y);
		double ang = Math.acos((yDiff));
		double xDiff = Math.tan(ang) * yDiff;
		
		maxSlope = yDiff / xDiff;
		
	}
	
}
