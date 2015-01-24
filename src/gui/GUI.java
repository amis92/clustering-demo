package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import clusterer.FlameClusterer;
import clusterer.Point;

@SuppressWarnings("serial")
public class GUI extends JFrame
{	
	private FlameClusterer flameClusterer;
	private ArrayList<Point> points = new ArrayList<>();
	private final MouseListener mouseListener = new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent arg0) {
			points.add(new Point(arg0.getXOnScreen()-0, arg0.getYOnScreen() -28));
			drawingPanel.repaint();
		}
		@Override
		public void mouseEntered(MouseEvent arg0) {}
		@Override
		public void mouseExited(MouseEvent arg0) {}
		@Override
		public void mousePressed(MouseEvent arg0) {}
		@Override
		public void mouseReleased(MouseEvent arg0) {}
	};
	
	private final SomePanel drawingPanel = new SomePanel();
	class SomePanel extends JPanel implements ActionListener {
		

		@Override
		public void actionPerformed(ActionEvent arg0) {

		}
		
		@Override
		protected void paintComponent(Graphics g){
			super.paintComponent(g);		
			Graphics2D g2d = (Graphics2D)g;
			for(Point point: points) {
					g2d.setPaint(point.color);
					g2d.fill(point.graphicalPoint);
			}
			repaint();
		}
		
	};

	
	
	public GUI()
	{
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_SPACE) {
					points.clear();
				}
				else if(arg0.getKeyCode() == KeyEvent.VK_F){
					try {
						flameClusterer = new FlameClusterer(points, 15, 100);
					} catch (Exception e) {
						e.printStackTrace();
					}
					flameClusterer.execute();
				}
				else if(arg0.getKeyCode() == KeyEvent.VK_C){	//miejsce na fuzzy c means
					
				}
				drawingPanel.repaint();
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		this.setPreferredSize(new Dimension(1280, 768));
		this.addMouseListener(mouseListener);
		
		add(drawingPanel);
		//add(iterationsSlider, BorderLayout.EAST);
		
		
		pack();
		setVisible(true);
	}
	
	public void setPointSet(ArrayList<Point> points){
		this.points = points;
	}
	
}
