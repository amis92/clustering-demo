package clusterer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Fuzzy C Means Clusterer for 2D points used in demo GUI application.
 * Source: <a href=
 * "https://code.google.com/p/classifiers/source/browse/clustering/src/org/pr/clustering/soft/FuzzyCMeans.java?r=18"
 * >Google Code page</a>
 * 
 * @author Amadeusz Sadowski
 *
 */
public class FuzzyCMeansClusterer {

	/** Data to cluster. */
	private final Vector[] patterns;
	/** Number of clusters to create. */
	private final int c;
	/**
	 * Fuzzying parameter. Real number greater than 1, the greater the more
	 * fuzzy clusters are created.
	 */
	private final double m;

	/**
	 * Matrix of cluster memberships.
	 */
	private MembershipMatrix mm;

	/**
	 * Assigns provided values, initializing clusterer.
	 * 
	 * @param patterns
	 *            - the data to cluster.
	 * @param c
	 *            - number of clusters expected.
	 * @param m
	 *            - fuzzifier. Any real number greater than 1. The greater
	 *            number, the more fuzzy clusters are produced.
	 */
	public FuzzyCMeansClusterer(Vector[] patterns, int c, double m) {
		this.patterns = patterns;
		this.c = c;
		this.m = m;
	}
	
	public MembershipMatrix getMembershipMatrix()
	{
		return mm;
	}

	/**
	 * Clusters data using FCM algorithm.
	 * 
	 * @return cluster center list.
	 */
	public List<Vector> partition() {
		// 1. choose an arbitrary membership matrix
		mm = new MembershipMatrix(patterns.length, c);

		// 2. calculate cluster centers
		List<Vector> newC = calculateC();

		List<Vector> oldC;

		double exponent = 2.0 / (m - 1);
		while (true) {
			oldC = newC;

			// 3. update membership matrix
			for (int i = 0; i < patterns.length; i++) { // loop over patterns
				for (int j = 0; j < c; j++) { // loop over clusters
					double denominator = 0;
					double xi_cj_distance = Vector.euclideanDistance(
							patterns[i], newC.get(j));
					for (int r = 0; r < c; r++) {
						double xi_cr_distance = Vector.euclideanDistance(
								patterns[i], newC.get(r));
						double ratio = Math.pow(
								xi_cj_distance / xi_cr_distance, exponent);
						denominator += ratio;
					}

					mm.matrix[i][j] = 1.0 / denominator;
				}
			}

			newC = calculateC();

			if (newC.equals(oldC))
				break;
		}

		return newC;
	}

	/**
	 * calculates cluster centers with current clustering configurations.
	 */
	protected List<Vector> calculateC() {
		List<Vector> C = new ArrayList<Vector>();
		for (int j = 0; j < c; j++) {
			double[] values = new double[this.patterns[0].getDimensionCount()];
			// we need to iteration to get each dimension
			for (int dimIndex = 0; dimIndex < values.length; dimIndex++) {
				double nominator = 0;
				for (int i = 0; i < patterns.length; i++) {
					nominator += Math.pow(mm.matrix[i][j], m)
							* patterns[i].getDimension(dimIndex);
				}

				double denominator = 0;
				for (int i = 0; i < patterns.length; i++) {
					denominator += Math.pow(mm.matrix[i][j], m);
				}

				values[dimIndex] = nominator / denominator;
			}
			C.add(new Vector(values));
		}

		return C;
	}

	public String printResults() {
		StringBuilder sb = new StringBuilder("");
		sb.append("pattern \t\t cluster memberships" + "\n");

		DecimalFormat format = new DecimalFormat("#.###");

		for (int i = 0; i < patterns.length; i++) { // loop over patterns
			sb.append("\n"); // + patterns[i] + ": ");
			for (int j = 0; j < c; j++) { // loop over clusters
				sb.append(format.format(mm.matrix[i][j]) + "\t");
			}
		}

		return sb.toString();
	}
}
