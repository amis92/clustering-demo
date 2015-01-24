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
	private final SomePanel drawingPanel = new SomePanel();
	private int expectedClusters = 6;
	private FlameClusterer flameClusterer = null;
	private FCMPointsClusterer fmcClusterer = null;
	private double fuzzifier = 2.0;

	private final MouseAdapter mouseListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent arg0) {
			points.add(new Point(arg0.getXOnScreen() - 0,
					arg0.getYOnScreen() - 28));
			drawingPanel.repaint();
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
	}

	private void getNewFmcParams() {
		final JSpinner cField = new JSpinner(new SpinnerNumberModel(
				expectedClusters, 2, Integer.MAX_VALUE, 1));
		final JSpinner mField = new JSpinner(new SpinnerNumberModel(fuzzifier,
				1.001, 100.0, 0.001));
		final Object[] params = {
				new JLabel("Number of clusters in Fuzzy C Means:"), cField,
				new JLabel("Fuzzying parameter (any real > 1):"), mField };
		// show panel
		final int d = JOptionPane.showConfirmDialog(drawingPanel, params,
				"Fuzzy C Means parameters:", JOptionPane.OK_CANCEL_OPTION);
		// show confirmation and set params
		if (d == 0) {
			expectedClusters = (Integer) cField.getValue();
			fuzzifier = (Double) mField.getValue();
			String message = String.format("New parameters:\n"
					+ "Number of clusters in Fuzzy C Means: = %d\n"
					+ "Fuzzying parameter (any real > 1): = %f\n",
					expectedClusters, fuzzifier);
			JOptionPane.showMessageDialog(null, message);
		}
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
			newFmcClusterer();
			break;
		case KeyEvent.VK_R:
			getNewFmcParams();
			break;
		}
		drawingPanel.repaint();
	}

	private void newFmcClusterer() {
		fmcClusterer = new FCMPointsClusterer(points, expectedClusters,
				fuzzifier);
	}

	private void runFlame() {
		try {
			flameClusterer = new FlameClusterer(points, 15, 100);
			flameClusterer.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void runFuzzyC() {
		try {
			newFmcClusterer();
			fmcClusterer.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setPointSet(ArrayList<Point> points) {
		this.points = points;
	}
}
