/*						    __	   	  __  ___ 	   ________	 ________
 / /    	 /  |/   |	  / ____  / / ______/
 / /    	/ /|  /| |   / /   /_/ / /   __
 / /____   / / |_/ | |	/ /_____  / /___/ /
 /______/o /_/      |_|o \______/o \______/o

 Main class:			programs.MapViewer
 LMCG dependencies:	gui.MapGraphics
 map.SimpleRestrictionMap
 map.RestrictionFragment
 David Meyerson 2013
 *//////////////////////////////////////////100 cols wide////////////////////////////////////////////
package edu.wisc.lmcg.gui;

import edu.wisc.lmcg.map.RestrictionFragment;
import edu.wisc.lmcg.map.RestrictionMap;
import edu.wisc.lmcg.map.SimpleRestrictionMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * represents a drawing of an individual restriction map
 *
 * @author dmeyerson
 *
 */
public class MapDrawing {

    SimpleRestrictionMap resMap; //the restriction map object that corresponds to this drawing

    //holds rectangles representing the fragments of this molecule, these are invisible, but we'll 
    //use them to tell us where to draw the lines that depict the molecule
    private ArrayList<FragRectangle> fragRectangles;

    //holds the actual software objects representing the fragments
    private ArrayList<RestrictionFragment> frags;

    //holds information needed to draw the lines designating this map's alignments
    private ArrayList<AlignLines> alignments;

    private boolean clickedOn = false, //true iff user is holding down the mouse inside the molecule boundary
            aligned = false; //true iff this map is aligned to another
    private String name; //the molecule's name
    private Rectangle2D.Double boundary; //represents the boundary of this drawing
    private double cornerX = -1;
    private double cornerY = -1; //coordinates of the drawing's upper left corner. Will be -1 unless otherwise specified by the xml input file
    private double alignX; //coordinate of the left side of this alignment, to use when resetting the display    
    private double width; //the width, in pixels, of this drawing
    public static final double DEFAULT_FRAGMENT_HEIGHT = 20; // default height of the drawing
    public static final double DEFAULT_PADDING = 60; // default height of the drawing
    private static double height = 20, //height of the drawing, in pixels    
            maxX, //the length in pixels of the longest molecule seen in all instances
            maxY;
    private static boolean reset = false; //true iff we want to ignore and reset all map positions
    private boolean showMapName = true;

    private final String KILOBASES_SYMBOL = "kb";
    private final int MAP_PADDING = 3;
    private final Font defaultFont = Font.decode(Font.SANS_SERIF);
    private Font drawingFont = Font.getFont(Font.SANS_SERIF);
    private boolean isMovingMap = false;
    private double totalOffsetX = 0.0;
    private double totalOffsetY = 0.0;

    private final ContigDrawer c_drawer;

    /**
     * Constructor
     * @param resMap
     */
    public MapDrawing(SimpleRestrictionMap resMap) {
        
        fragRectangles = new ArrayList<>();
        this.c_drawer = ContigDrawer.getDrawer();
        this.resMap = resMap;
        this.name = resMap.getName();
        alignments = new ArrayList<>();
        boundary = new Rectangle2D.Double();
        if (!reset) {
            setX(resMap.getMapViewerX());
            setY(resMap.getMapViewerY());
        }

        ArrayList<RestrictionFragment> frags = new ArrayList<RestrictionFragment>(resMap.getFragments());
        this.frags = frags;

        makeRectangles();
    }

    public void paintComponent() {
        
        //Creates the mapdrawing element in the svg file
        Point2D p = c_drawer.real2CanvasCoords(cornerX, cornerY);        
        
        c_drawer.createMapDrawing(name, p.getX(), p.getY());

        double xCounter =  cornerX;
        Iterator<FragRectangle> iter = fragRectangles.iterator();

        int a;
        while (iter.hasNext()) {
            FragRectangle frag = iter.next();
            
            frag.x = xCounter;
            frag.y = cornerY;                    
            
            Color fill = frag.getColor();
            if (fill != null) {
                c_drawer.createFragment(frag, name);
            } else {
                //Skip unaligned fragments with less than 5kb of size
                if ( frag.getBasePairs() > 5000 )
                    c_drawer.createEmptyFragment(frag, name);
            }

            xCounter += frag.getWidth();
        }
        
        repositionBoundary();
    }

    /**
     * creates rectangles representing the fragments of this molecule but
     * doesn't draw those rectangles
     */
    private void makeRectangles() {
        double xCounter = Math.max(cornerX, 0.0);   //The max function is for control the "-1" value when cornerX isn't set
        int index = 0;
        Iterator<RestrictionFragment> iter = frags.iterator();
        double position = 0;
        while (iter.hasNext()) {
            RestrictionFragment aFrag = iter.next();
            int basePairs = aFrag.getMassInBp();
            double fragWidth = (double) basePairs / 1000.0;
            FragRectangle rect = new FragRectangle(xCounter, cornerY, fragWidth, height, basePairs, index, position);
            fragRectangles.add(rect);
            xCounter += fragWidth;
            position += fragWidth;
            index++;
        }

        //make an invisible box around the drawing
        width = xCounter - Math.max(cornerX, 0.0);
        boundary.setRect(cornerX, cornerY, width, height);        

        //check if this is the longest molecule we've seen
        if (xCounter > maxX) {
            maxX =  xCounter;
        }
    }

    /**
     * @param x the x coordinate in pixels
     * @param y the y coordinate in pixels
     * @return true if the given coordinates specify a point inside this
     * drawing
     */
    public boolean contains(double x, double y) {     
        return boundary.contains(x, y);
    }

    /**
     * add a pair of AlignLines to this drawing
     *
     * @param alignment the pair of AlignLines
     */
    public void addAlignment(AlignLines alignment) {
        alignments.add(alignment);
        alignX = alignment.getLeftSideX() + 5;
        if (cornerX == -1) { //If cornerX hasn't been already set,
            cornerX = alignX; //put it in it's aligned position.
        }
    }
    
    public void moveToItsAlignedPosition(){        
        
        alignX = MapDrawing.getMaxX() - width;
        for ( AlignLines al : alignments ){
            alignX =  Math.min(al.getLeftSideX(), alignX);
        }

        setCornerCoords(alignX, getY());
    }

    /**
     * draw each of the sets of lines representing this map's alignments     
     */
    public void paintAlignLines() {

        for (AlignLines alignment : alignments) {
            alignment.paintComponent();
        }
    }

    /**
     * sets all this drawings alignment lines to be either visible or invisible
     */
    public void setAlignLinesVisible(boolean b) {
        for (AlignLines alignment : alignments) {
            alignment.setVisible(b);
        }
    }

    /**
     * toggle the visibility of this map's alignment lines
     */
    public void toggleAlignLines() {
        for (AlignLines alignment : alignments) {
            setAlignLinesVisible(!alignment.isVisible());
        }
    }

    /**
     * colors every fragment in an aligned region
     *
     * @param leftIndex left bound of the aligned region
     * @param rightIndex right bound of the aligned region
     * @param mapType
     */
    public void colorAlignedRegion(int leftIndex, int rightIndex, FragmentColorType mapType) {
        for (int i = leftIndex; i <= rightIndex; i++) {
            
            fragRectangles.get(i).color(mapType);
        }
    }
    
    /**
     * colors every fragment in an aligned region
     *
     * @param index
     * @param mapType
     */
    public void colorAlignedRegion(int index, FragmentColorType mapType) {
            
        fragRectangles.get(index).color(mapType);        
        
    }

    /**
     * repositions the invisible box that outlines this map
     */
    public void repositionBoundary() {        
        
        boundary.x = cornerX;
        boundary.y = cornerY;
    }

    /**
     * sets the x and y coordinates of the top left corner of this drawing. A
     * coordinate is set to zero if the specified coordinate is negative
     *
     * @param x
     * @param y
     */
    public void setCornerCoords(double x, double y) {
                    
        double finalx = x;
        double finaly = y;

        if (x < -1) {
            finalx = 0;
        }
        if (y < -1) {
            finaly = 0;
        }

        if ( getMaxX() > 0.0 && x > getMaxX() - width)
            finalx = getMaxX() - width;
        if ( getMaxY() > 0.0 && y > getMaxY() - height )
            finaly = getMaxY() - height;

        double oldX = getX();
        double oldY = getY();
        cornerX = finalx;
        cornerY = finaly;
        /*if ( cornerY > getMaxY()) {
            setMaxY(cornerY);
        }*/


        if ( isMovingMap ){  
            Point2D finalPoint = c_drawer.real2CanvasCoords(finalx, finaly);
            Point2D currentPoint = c_drawer.real2CanvasCoords(oldX, oldY);
            double xOffset = finalPoint.getX() - currentPoint.getX();
            double yOffset = finalPoint.getY() - currentPoint.getY();
            //Since the in-between lines don't move inmediatly with the map
            //we need to keep track of the total offset
            totalOffsetX += xOffset;
            totalOffsetY += yOffset;
            //Change the molecule's position on the canvas
            c_drawer.moveMap(this.getName(), resMap.getType(), xOffset, yOffset);
        }
        repositionBoundary();
    }
    
    /**
     * Updates the maxY value, as well as the canvas height.
     * @param newMaxY new Max Y coordinate for this drawing.
     */
    public static void setMaxY(double newMaxY){
        //FIXME: Chech if resize the canvas can be really necesary
        //double increase_factor = (newMaxY + DEFAULT_FRAGMENT_HEIGHT + 2*DEFAULT_PADDING) / ( maxY + DEFAULT_FRAGMENT_HEIGHT + 2*DEFAULT_PADDING);        
        maxY = newMaxY;
        //c_drawer.setCanvasHeight( c_drawer.getCanvasHeight() * increase_factor );
    }

    public void setX(double x) {
        /*double xOffset = x - cornerX;
        cornerX = x;        
        
        //Change the molecule's position on the canvas
        c_drawer.moveMap(this.getName(), resMap.getType(), xOffset, 0.0);        
        
        repositionBoundary();*/
        setCornerCoords(x, getY());
    }

    public void setY(double y) {
        /*double yOffset = y - cornerY;
        cornerY = y;
        
        //if the new y coordinate is greater than the old value, then 
        //it updates the maxY value, as well as the canvas height.
        if ( cornerY > maxY) {
            setMaxY(cornerY);
        }
        
        //Change the molecule's position on the canvas
        c_drawer.moveMap(this.getName(), resMap.getType(), 0.0, yOffset);
        repositionBoundary();*/
        setCornerCoords(getX(), y);
    }   
    

    /**
     * sets this map's x position so that it's aligned to the reference map
     */
    public void resetX() {
        cornerX = alignX;
        repositionBoundary();
    }

    /**
     * Goes back into the contig that made this drawing and updates the
     * "mapViewerX" and "mapViewerY" variables according to the map drawing's
     * current position. We do this so that other classes can save to an xml
     * file the current positions of maps in a map GUI
     */
    public void updateContigCoords() {
        resMap.setMapViewerX( cornerX );
        resMap.setMapViewerY( cornerY );
    }

    /**
     * simple accessor methods
     */
    public ArrayList<RestrictionFragment> getFrags() {
        return frags;
    }

    public boolean isClickedOn() {
        return clickedOn;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return cornerX;
    }

    public double getY() {
        return cornerY;
    }

    public ArrayList<FragRectangle> getFragRectangles() {
        return fragRectangles;
    }

    public FragRectangle getRect(int index) {
        return fragRectangles.get(index);
    }

    public static double getMaxX() {
        // the Maximum x coordiante is: MaxX coordinate among all the drawings plus two times a default padding ( left and right )
        return 2*maxX;//Math.max(2*maxX, MINIMUM_CANVAS_SIZE);
    }

    public static double getMaxY() {
        // the Maximum y coordiante is: MaxY coordinate among all the drawings plus one time the height of a rectangle (to correctly show the last one)
        //plus two times a default padding  ( top and bottom )
        return 4*maxY;// Math.max(4*maxY, MINIMUM_CANVAS_SIZE);
    }

    public boolean isAligned() {
        return aligned;
    }

    /**
     * simple mutator methods
     */
    public void setClickedOn(boolean b) {
        clickedOn = b;
    }

    //public void setBasesPerPixel(int newBasesPerPixel) {basesPerPixel = newBasesPerPixel;}
    public void setHeight(int newHeight) {
        height = newHeight;
    }

    public double getHeight() {
        return height;
    }
    
    public double getWidth() {
        return width;
    }

    public static void resetMaxX() {
        maxX = 0;
    }

    public static void resetMaxY() {
        maxY = 0;
    }

    public static void setResetState(boolean b) {
        reset = b;
    }

    public void setAligned(boolean b) {
        aligned = b;
    }

    public void removeAlignLines() {
        for (AlignLines alignment : alignments) {
            //TODO kludgey solution - still need to unalign and recolor this alignment's maps
			/**/
            alignment.setVisible(false);/**/

        }
    }

    public void setShowMapName(boolean value) {
        this.showMapName = value;
    }

    public boolean getShowMapName() {
        return this.showMapName;
    }
    
    public String getType(){
        return resMap.getType();
    }
    
    public void startMovingMaps(){
        this.isMovingMap = true;
        this.totalOffsetX = this.totalOffsetY = 0.0;
    }
    
    public void stopMovingMaps(){
        this.isMovingMap = false;
        
        c_drawer.moveAlignmentLinesForReferenceMap(name, totalOffsetX, totalOffsetY, c_drawer.INTERMEDIATE_ALIGNMENT_LINE_PREFIX);
        c_drawer.moveAlignmentLines(name, totalOffsetX, totalOffsetY, c_drawer.INTERMEDIATE_ALIGNMENT_LINE_PREFIX);
        
        this.totalOffsetX = this.totalOffsetY = 0.0;
    }
    
    public boolean isMovingMaps(){
        return isMovingMap;
    }
}
