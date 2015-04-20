/*						    __	   	   __  ___ 	     ________   ________
 / /    	  /  |/   |     / ____  /  / ______/
 / /    	 / /|  /| |    / /   /_/  / /   __
 / /____    / / |_/ | |	  / /_____   / /___/ /
 /______/ o /_/      |_| o \______/ o \______/ o

 Main class:			programs.MapViewer
 LMCG dependencies:	gui.MapGraphics
 map.SimpleRestrictionMap
 map.RestrictionFragment
 David Meyerson 2013
 *//////////////////////////////////////////100 cols wide////////////////////////////////////////////
package edu.wisc.lmcg.gui;

import edu.wisc.lmcg.gui.themes.AlignmentColorTheme;
import java.awt.Color;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * represents an individual fragment of an individual map; The basic component
 * of a MapDrawing
 *
 * @author dmeyerson
 *
 */
public class FragRectangle extends Rectangle2D.Double {

    private int basePairs,
            index;	//index of this frag in the molecule it belongs to
    private Color fill;
    private double doubWidth, //we need to remember width as a double value because resizing sometimes doesn't work otherwise
            position; //position, in kilobases, from the left side of the molecule
    private final String kilobaseString;
    private final String basePairString;
    private boolean displayLength; //true iff we should display this frag's length below the molecule
    private final int KILOBASES_DECIMALS_POINTS = 2;

    /*
     public FragRectangle(int width, int height) {
     super(width, height);
     doubWidth = width;
     }
	
     public FragRectangle(int x, int y, int width, int height) {
     super(x, y, width, height);
     doubWidth = width;
     }
     */
    public FragRectangle(double x, double y, double width, double height, int basePairs, int index, double position) {
        super(x, y, width, height);
        this.basePairs = basePairs;
        doubWidth = width;
        this.index = index;
        this.position = position;

        double kilobases = (double) basePairs / (double) 1000;
        
        kilobaseString = String.format("%.3f", kilobases );
        basePairString = String.format("%,d", basePairs); 
    }
    
    public void color(FragmentColorType mapType) {
        
        AlignmentColorTheme theme = MapGUI.getAlignmentColorTheme();
        
        
        if (fill == null) {
            
            if ( mapType.equals(FragmentColorType.OpticalMap) )            
                fill = theme.getNormalColor(mapType);
            else if (mapType.equals(FragmentColorType.ReferenceMap))
                fill = theme.getNormalColor(mapType);
            else if (mapType.equals(FragmentColorType.MissingCut))                
                fill = theme.getMissingCutColor();
            else if (mapType.equals(FragmentColorType.ExtraCut))
                fill = theme.getExtraCutColor();
        }else{
            fill = theme.getOverlappingColor();
        }
    }
    
    public double getPosition(){
        return position;
    }
    
    public int getIndex(){
        return this.index;
    }

    public Color getColor() {
        return fill;
    }

    @Override
    public double getWidth() {
        return doubWidth;
    }

    public int getBasePairs() {
        return basePairs;
    }

    public String getKilobaseString() {
        return kilobaseString;
    }
    
    public String getBasePairString() {
        return basePairString;
    }

    public boolean displayLength() {
        return displayLength;
    }

    public void displayLength(boolean b) {
        displayLength = b;
    }
    
    /**
     * Indicates if the point given by the parameters x and y is inside de fragment. The point is in screen coordinates.
     * @param x
     * @param y
     * @return 
     */
    public boolean isScreenPointOverFragment(double x, double y){
        
        Point2D realPoint;
        ContigDrawer c_drawer = ContigDrawer.getDrawer();
        try {
            realPoint = ContigDrawer.getDrawer().screen2CanvasCoords(x, y);
            realPoint.setLocation(
                realPoint.getX() * MapDrawing.getMaxX() / c_drawer.getCanvasWidth(),
                realPoint.getY() * MapDrawing.getMaxY() / c_drawer.getCanvasHeight());
            return contains(realPoint);
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(FragRectangle.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
