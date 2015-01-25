package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import javax.swing.UIManager;

import clusterer.Point;

public class Main 
{
	private static final Logger logger = Logger.getLogger(Main.class.getName());
	public static void main(String[] args)
	{
		
		ArrayList<Point> points = new ArrayList<>();
		try {
			File file = new File("atesty/europe");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] splited = line.split("\\s+");	
				points.add(new Point(Integer.parseInt(splited[1])/10000d, Integer.parseInt(splited[2])/10000d));
				System.out.println(splited[1]+" "+splited[2]);
			}
			fileReader.close();
			System.out.println("zakonczono wczytywanie pliku\n\n");
		} catch (IOException e) {
			System.err.println("niepowodzenie w otwarciu pliku");
		}	
		

        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            // we can live without system L&F
            logger.info("Failed to set System L&F");
        }
		GUI gui = new GUI();
		points.clear();
		
		
		Random random = new Random();	
		for(int i=0; i<400; ++i) {
			Point p = new Point(
				Math.abs((int)(random.nextGaussian() * 1280d)) %500,
				Math.abs((int)(random.nextGaussian() * 768d)) %300
			);
			points.add(p);
		}
		
		for(int i=0; i<400; ++i) {
			Point p = new Point(
				Math.abs((int)(random.nextGaussian() * 1280d)) %500+500,
				Math.abs((int)(random.nextGaussian() * 768d))%300+500
			);
			points.add(p);
		}
		
		gui.setPointSet(points);
	}
}
