package clusterer;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Random;

import util.Pair;
import clusterer.Point.Type;

public class FlameClusterer
{
	private int k, steps;
	ArrayList<Point> points;
	ArrayList<Point> CSOs = new ArrayList<>();
	
	
	public FlameClusterer(ArrayList<Point> points, int k, int steps) throws Exception
	{	

		this.points = points;
		this.k = k;
		this.steps = steps;
		for(Point point: points) {
			point.closestNeighbors.clear();
			point.clusterMemberships.clear();
			point.color = new Color(255, 255, 255);
			point.localDistance = 0.0d;
			point.relativeDensity = 0.0d;
			point.dominatingCSO = null;
			point.type = null;
			point.graphicalPoint = new Ellipse2D.Double();
			point.graphicalPoint.x = point.x;
			point.graphicalPoint.y = point.y;
			point.graphicalPoint.height = 18;
			point.graphicalPoint.width = 18;
		}
	}
	
	public void execute() {
		
		if(k>= points.size()){
			System.err.println("liczba sasiadow jest wieksza lub rowna ilosci wszystkich punktow");
			return;
		}
		
		
		System.out.println("szukanie sasiadow i wyliczenie najwiekszego dystansu");
		double maxDistance =-1; 
		for (Point point : points) {  //
			double dst = -1;
			try {
				dst = point.findClosestNeighbors(points, k);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			if(dst > maxDistance)
				maxDistance = dst;
		}
		System.out.println("maksymalna suma odleglosci miedzy punktami:" + maxDistance);
		
		
		
		System.out.println("wyliczanie lokalnej gestosci kazdego z puntkow");
		double minRelativeDensity = Double.MAX_VALUE, 
				maxRelativeDensity = -Double.MAX_VALUE;
		
		for (Point point : points) {	//gestosc lokalna 
			double down = 0d;	
			
			for (Pair<Point, Double> neighbor : point.closestNeighbors) {
				down += neighbor.second;
			}

			double relativeDensity = (maxDistance / down);
			point.relativeDensity = relativeDensity;
			if(relativeDensity < minRelativeDensity){
				minRelativeDensity = relativeDensity;
			}
			else if(relativeDensity > maxRelativeDensity){
				maxRelativeDensity = relativeDensity;
			}
		}
		if(minRelativeDensity <1d)
			try {
				throw new Exception("gestosc ponizej 1");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		System.out.println("min. gestosc: " + minRelativeDensity + " max. gestosc: "+maxRelativeDensity);
		
		
		System.out.println("wyliczanie typu kazdego z punktow");
		int outlierCount =0, standardCount =0;
		for (Point point : points) {
			point.calculateClass(maxRelativeDensity) ;	
			
			if(point.type == Type.CSO){
				CSOs.add(point);
			}
			else if(point.type == Type.STANDARD){ 
				standardCount++;}
			else if(point.type == Type.OUTLIER){ 
				outlierCount++;
			}
		}
		System.out.println("cso:"+CSOs.size()+" outlier:"+outlierCount+" sta:"+standardCount + "wszystkie punkty: "+points.size());
		
		
		
		System.out.println("wyliczanie inicjalnej wartosc przynaleznosci do kazdego z klastrow");
		for (Point point : points) {	
			if(point.type == Type.STANDARD){
				for(Point CSO : CSOs) {
					double membershipValue = 1d/(1d+CSOs.size());
					if(membershipValue == Double.NaN)
						throw new ArithmeticException("wartosc jej nan");
					point.clusterMemberships.add(new Pair<Point, Double>(CSO, membershipValue));	//@TODO inicjalna przynaleznosc jest zalezna od liczby CSO
				}
				point.clusterMemberships.add(new Pair<Point, Double>(null, 1d/(1d+CSOs.size())));	//null symbolizuje grupe outlierow
			}
			else if(point.type == Type.OUTLIER){	//jezelu punkt to outlier, to nalezy w 100% do grupy outlierow 
				for(Point CSO : CSOs) {
					point.clusterMemberships.add(new Pair<Point, Double>(CSO, 0d));
				}
				point.clusterMemberships.add(new Pair<Point, Double>(null, 1d));	
			}
			else if(point.type == Type.CSO){	//jezelu punkt to CSO, to nalezy w 100% do samego siebie
				for(Point CSO : CSOs) {
					if(point == CSO){	//iteracja w ktorej punkt iterowany jest jednoczesnie CSO - nalezy go dodac w tym momencie do tablicy przynaleznosci, aby zostala zachowana kolejnosc CSO w tablicach
						point.clusterMemberships.add(new Pair<Point, Double>(CSO, 1d));
					}
					else{
						point.clusterMemberships.add(new Pair<Point, Double>(CSO, 0d));
					}
				}
				point.clusterMemberships.add(new Pair<Point, Double>(null, 0d));	
			}
		}

		
		System.out.println("iteracje flame");
		for(int i=0;i<steps;i++) {  //iteracje FLAME
			assignMembershipValues();
		}
		
		//sprawdzenie czy dobrze dodalo wartosci membershipow
		for (Point point : points) {
			for (Pair<Point, Double> membership : point.clusterMemberships) {
				if(membership.second == Double.NaN)
					throw new ArithmeticException("wartosc jej nan");
			}
		}	
		
		System.out.println("przydzielanie do klastrow");
		for (Point point : points) {	
			point.hardClusterAssignment();
		}
		
		
		System.out.println("nadawanie kolorow\n");
		Random random = new Random();	
		for (Point point : CSOs) {
			Color clusterColor = new Color(
					random.nextInt(220),
					random.nextInt(220),
					random.nextInt(220)
					);			
			Point.CSOcolors.put(point, clusterColor);
			point.color = clusterColor;
		}
		Point.CSOcolors.put(null, new Color(255,255,255));	//bialy kolor dla outlierow
		
		for (Point point : points) {
			if(point.type == Type.STANDARD){
				Color domintatingCSOColor = Point.CSOcolors.get(point.dominatingCSO);
				if(domintatingCSOColor == null){
					try {
						throw new Exception("brak koloru dla CSO; typ punktu:"+point.dominatingCSO);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				point.color =domintatingCSOColor;
			}
			else if(point.type == Type.OUTLIER)
				point.color = new Color(255,255,255);
		}
		
		
	}
	
	private void assignMembershipValues(){
		
		for (Point point : points) {
			if(point.type != Type.STANDARD) continue;	//nie wyliczaj nowej gestosci dla CSO i outlierow

			for (int i = 0; i < point.clusterMemberships.size(); ++i) {
				double newMembership = 0.0d;
				double down = 0.0d;
				for(Pair<Point, Double> neighbor: point.closestNeighbors) {
					down += 1d/neighbor.second;
				}
				for(Pair<Point, Double> neighbor: point.closestNeighbors) {
					double top = (1d/neighbor.second);
					newMembership += (top / down * neighbor.first.clusterMemberships.get(i).second);
				}
				point.clusterMemberships.get(i).second = newMembership;
			}
		}
	}
	
}