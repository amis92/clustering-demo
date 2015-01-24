package clusterer;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NavigableSet;
import java.util.TreeMap;

import util.Pair;

public class Point
{
	public static HashMap<Point, Color> CSOcolors = new HashMap<>();
	public Ellipse2D.Double graphicalPoint = new Ellipse2D.Double();
	public Color color;
	
	public enum Type { CSO, OUTLIER, STANDARD };
	public Type type = null;
	
	public final double x;
	public final double y;
	public ArrayList<Pair<Point,Double>> closestNeighbors = new ArrayList<>();
	public ArrayList<Pair<Point, Double>> clusterMemberships = new ArrayList<>();
	
	/* clustering stats */
	public double localDistance = 0.0;
	public double relativeDensity;
	
	public Point dominatingCSO;
	
	public Point(double x, double y)
	{
		this.x = x;
		this.y = y;
		this.graphicalPoint.x = x;
		this.graphicalPoint.y = y;
		this.graphicalPoint.height = 18;
		this.graphicalPoint.width = 18;
		this.color = new Color(255,255,255);
	}
	
	/**
	 * @param points
	 * @param k
	 * @return suma dystansow miedzy wszystkimi punktami
	 * @throws Exception
	 */
	public double findClosestNeighbors(ArrayList<Point> points, int k) throws Exception 
	{
		double maxDistance = -1d;
		TreeMap<Double, ArrayList<Point>> closestPoints = new TreeMap<>();
		for(Point p: points) {
			if(p == this) {
				continue;
			}
			double proximity = this.calculateDistance(p);
			ArrayList<Point> al = closestPoints.get(proximity);
			if(al == null) {
				al = new ArrayList<>();
				closestPoints.put(proximity, al);
			}
			al.add(p);
		}
		
		NavigableSet<Double> ascending = closestPoints.navigableKeySet();
		maxDistance = ascending.first();
		int visited = 0;
		for(Double d: ascending) {
			for(Point point: closestPoints.get(d)) {		
				this.closestNeighbors.add(new Pair<Point, Double>(point, d));
				this.localDistance += d;
				if(++visited >= k) {
					return localDistance;
				}
			}
		}
		throw new RuntimeException("kod ma tu nie dojsc");
	}
	
	private double calculateDistance(Point p){
		return Math.sqrt(Math.pow(this.x - p.x, 2) + Math.pow(this.y - p.y, 2));
	}
	
	public Type calculateClass(double maxDensity){
		double minNeighborDensity =Double.MAX_VALUE, 
				maxNeighborDensity = -Double.MAX_VALUE;
		
		for (Pair<Point, Double> neighbor : closestNeighbors) {
			if(neighbor.first.relativeDensity < minNeighborDensity)
				minNeighborDensity = neighbor.first.relativeDensity;
			else if(neighbor.first.relativeDensity > maxNeighborDensity)
				maxNeighborDensity = neighbor.first.relativeDensity;
		}
		
		if(relativeDensity > maxNeighborDensity && this.relativeDensity > 0.5d*maxDensity)
			type = Type.CSO;
		else if (relativeDensity < minNeighborDensity){
			type = Type.OUTLIER;
		}
		else type = Type.STANDARD;	
		return type;
	}
	
	void hardClusterAssignment()
	{
		if(type == Type.CSO || type == Type.OUTLIER) {
			this.dominatingCSO = this;
			return;
		}
		
		//wyszukuje dominujace CSO dla standardowych punktow
		double maxMembership = -1d;
		boolean isOutlier = false;
		for(int i=0; i<clusterMemberships.size(); i++) {
			Pair<Point, Double> pointInfo= this.clusterMemberships.get(i);
			if(pointInfo.second > maxMembership) {
				maxMembership = pointInfo.second;
				this.dominatingCSO = pointInfo.first;
				if(i == clusterMemberships.size()-1)	isOutlier = true;
			}
		}
		
		if(dominatingCSO == null && isOutlier == false)
			try {
				throw new Exception("nie udalo sie przydzielic zadnego CSO dla standrdowego punktu");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	

}
