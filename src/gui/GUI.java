package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import clusterer.FCMPointsClusterer;
import clusterer.FlameClusterer;
import clusterer.Point;

@SuppressWarnings("serial")
public class GUI extends JFrame {
	class SomePanel extends JPanel implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			for (Point point : points) {
				g2d.setPaint(point.color);
				g2d.fill(point.graphicalPoint);
			}
			repaint();
		}
	}

	public static final int DEFAULT_FLAME_ITERATIONS = 100;
	public static final int DEFAULT_FLAME_NEIGHBOURS = 10;
	public static final int DEFAULT_FUZZY_CLUSTERS = 6;
	public static final double DEFAULT_FUZZY_M = 2.0;
	private static final Logger logger = Logger.getLogger(GUI.class.getName());
	private final SomePanel drawingPanel = new SomePanel();
	private int fuzzyClusters = DEFAULT_FUZZY_CLUSTERS;
	private FlameClusterer flameClusterer = null;
	private int flameIterations = DEFAULT_FLAME_ITERATIONS;
	private int flameNeighbours = DEFAULT_FLAME_NEIGHBOURS;
	private FCMPointsClusterer fmcClusterer = null;
	private double fuzzyM = DEFAULT_FUZZY_M;

	private final MouseAdapter mouseListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent arg0) {
			//points.add(new Point(arg0.getXOnScreen() - 0,
			//		arg0.getYOnScreen() - 28));
			//drawingPanel.repaint();
		}
	};

	private ArrayList<Point> points = new ArrayList<>();;

	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				GUI.this.keyPressed(e);
			}
		});
		this.setPreferredSize(new Dimension(1280, 768));
		this.addMouseListener(mouseListener);
		add(drawingPanel);
		// add(iterationsSlider, BorderLayout.EAST);
		pack();
		setVisible(true);
		logger.info("GUI initialized.");
	}

	private void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE:
			points.clear();
			break;
		case KeyEvent.VK_F:
			runFlame();
			break;
		case KeyEvent.VK_C:
			runFuzzyC();
			break;
		case KeyEvent.VK_S:
			setupParams();
			break;
		case KeyEvent.VK_R:
			resetParams();
			break;
		}
		drawingPanel.repaint();
	}

	private void resetFuzzyC() {
		fmcClusterer = new FCMPointsClusterer(points, fuzzyClusters, fuzzyM);
	}

	private void resetParams() {
		flameIterations = DEFAULT_FLAME_ITERATIONS;
		flameNeighbours = DEFAULT_FLAME_NEIGHBOURS;
		fuzzyM = DEFAULT_FUZZY_M;
		fuzzyClusters = DEFAULT_FUZZY_CLUSTERS;
	}

	private void runFlame() {
		logger.info("Starting FLAME clustering.");
		logger.info(() -> String.format(
				"Parameters:\nNeighbours: %d\nIterations: %d", flameNeighbours,
				flameIterations));
		try {
			flameClusterer = new FlameClusterer(points, flameNeighbours,
					flameIterations);
			flameClusterer.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		logger.info("Finished FLAME clustering.");
	}

	private void runFuzzyC() {
		try {
			resetFuzzyC();
			fmcClusterer.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setPointSet(ArrayList<Point> points) {
		this.points = points;
	}

	private void setupParams() {
		final JSpinner neighboursField = new JSpinner(new SpinnerNumberModel(
				flameIterations, 1, Integer.MAX_VALUE, 1));
		final JSpinner iterationsField = new JSpinner(new SpinnerNumberModel(
				flameNeighbours, 1, Integer.MAX_VALUE, 1));
		final JSpinner cField = new JSpinner(new SpinnerNumberModel(
				fuzzyClusters, 2, Integer.MAX_VALUE, 1));
		final JSpinner mField = new JSpinner(new SpinnerNumberModel(fuzzyM,
				1.001, 100.0, 0.001));
		final Object[] params = { new JLabel("FLAME neighbours:"),
				neighboursField, new JLabel("FLAME iterations"),
				iterationsField,
				new JLabel("Fuzzy C Means number of clusters:"), cField,
				new JLabel("Fuzzy C Means Fuzzying parameter (any real > 1):"),
				mField };
		// show panel
		final int d = JOptionPane.showConfirmDialog(drawingPanel, params,
				"Parameters:", JOptionPane.OK_CANCEL_OPTION);
		// show confirmation and set params
		if (d == 0) {
			flameNeighbours = (Integer) neighboursField.getValue();
			flameIterations = (Integer) iterationsField.getValue();
			fuzzyClusters = (Integer) cField.getValue();
			fuzzyM = (Double) mField.getValue();
			String message = String.format("New parameters:\n"
					+ "FLAME neighbours = %d" + "FLAME iterations = %d"
					+ "Number of clusters in Fuzzy C Means: = %d\n"
					+ "Fuzzying parameter (any real > 1): = %f\n",
					flameNeighbours, flameIterations, fuzzyClusters, fuzzyM);
			JOptionPane.showMessageDialog(null, message);
		}
	}
}
