package clusterer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Demo for FCM clusterer on displayable dots.
 * 
 * @author Amadeusz Sadowski
 *
 */
public class FCMPointsClusterer {
	private static final Logger logger = Logger
			.getLogger(FCMPointsClusterer.class.getName());

	private final List<Point> points;
	private final Vector[] patterns;
	private final int c;
	private final double m;

	/**
	 * Assigns provided values, initializing clusterer.
	 * 
	 * @param points
	 *            - the data to cluster. They are copied (by reference) and
	 *            won't change.
	 * @param c
	 *            - number of clusters expected.
	 * @param m
	 *            - fuzzifier. Any real number greater than 1. The greater
	 *            number, the more fuzzy clusters are produced.
	 */
	public FCMPointsClusterer(ArrayList<Point> points, int c, double m) {
		if (points == null || points.isEmpty() || c < 1 || m <= 1.0) {
			throw new IllegalArgumentException();
		}
		this.points = new ArrayList<>(points);
		this.c = c;
		this.m = m;
		this.patterns = new Vector[points.size()];
		int i = 0;
		for (Point p : points) {
			patterns[i++] = new Vector(p.x, p.y);
		}
	}

	public void execute() {
		FuzzyCMeansClusterer clusterer = new FuzzyCMeansClusterer(patterns, c,
				m);
		clusterer.partition();
		Color[] clusterColors = generateColours();
		double[][] mm = clusterer.getMembershipMatrix().matrix;
		for (int i = 0; i < mm.length; i++) {
			int clusterIndex = getHighestMembership(mm[i]);
			Point p = points.get(i);
			p.color = clusterColors[clusterIndex];
		}
	}

	/**
	 * Finds the index of the highest membership value. If more than one is
	 * highest, the smaller index is returned.
	 * 
	 * @param memberships
	 *            - values of memberships
	 * @return found index of highest value.
	 */
	private int getHighestMembership(double[] memberships) {
		double highest = 0.0;
		int highestIndex = 0;
		for (int i = 0; i < memberships.length; ++i) {
			double value = memberships[i];
			if (value > highest) {
				highest = value;
				highestIndex = i;
			}
		}
		return highestIndex;
	}

	/**
	 * Generates {@link #c} colors (for each cluster).
	 * 
	 * @return
	 */
	private Color[] generateColours() {
		logger.info("nadawanie kolorow");
		Color[] clusterColors = new Color[c];
		Random random = new Random();
		float delta = 1.0f / ((float)c);
		for (int i = 0; i < c; ++i) {
			float h = ((float)i) * delta;
			float s = 0.9f + random.nextFloat() * 0.1f;
			float b = 0.6f + random.nextFloat() * 0.1f;
			clusterColors[i] = Color.getHSBColor(h, s, b);
		}
		logger.info(clusterColors.toString());
		return clusterColors;
	}
}
