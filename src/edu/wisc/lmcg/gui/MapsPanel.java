/*						    __	   		 __  ___ 	  	 ________	  ________
 / /    		/  |/   |		/ ____  /	 / ______/
 / /    	   / /|  /| | 	   / /   /_/ 	/ /   __
 / /____	  / / |_/ | |	  / /_____	   / /___/ /
 /______/  o  /_/      |_|  o  \______/  o  \______/  o

 Main class:			programs.MapViewer
 LMCG dependencies:	gui.MapGraphics
 map.SimpleRestrictionMap
 map.RestrictionFragment
 David Meyerson 2013
 *//////////////////////////////////////////100 cols wide////////////////////////////////////////////
package edu.wisc.lmcg.gui;

import edu.wisc.lmcg.alignment.contig.Contig;
import edu.wisc.lmcg.alignment.contigrun.ContigRun;
import edu.wisc.lmcg.alignment.mapalignment.StandardMapAlignment;
import edu.wisc.lmcg.map.Mapset;
import edu.wisc.lmcg.map.RestrictionMap;
import edu.wisc.lmcg.map.SimpleRestrictionMap;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

/**
 * represents a JPanel that displays map drawings
 *
 * @author dmeyerson
 */
public class MapsPanel extends JSVGCanvas implements MouseListener, MouseMotionListener, ActionListener, MouseWheelListener, SVGDocumentLoaderListener, GVTTreeRendererListener {
    private static int yBuffer = 50; //the default distance between the top corners of adjacent maps
    public static double ZOOM_FACTOR = 1.0; //Defines the zoom factor in the current Drawing
    public static final double ZOOM_STEP = 0.6; //Defines the depth of the zoom action

    public static void resetZoomFactor() {
        ZOOM_FACTOR = 1.0;
    }

    private ContigRun crun; //the contig run that specifies the maps and their alignments
    private Map<String,MapDrawing> mapDrawings; //a drawing representing each map
    private MapDrawing activeMap, rightClickMap; //the map being dragged at any given point
    private FragRectangle hoverFrag;
    private double xOffset, yOffset, //coordinates of the mouse relative to the top left of the active drawing
            yCounter; //used for initializing the y coordinates of map drawings
    private double prefWidth, prefHeight; //lengths, in pixels, of the boundaries we want to set on this panel's size
    private JMenuItem toggleAlignLines, deleteMap;
    private JScrollPane scrollPane;         //Scrollpane containing the panel
    private MapDrawing hoverMap;
    private int maxY;
    private boolean showLegend = false;  //Defines if the legend to interpret the maps will be showed
    private final String ZOOM_RECTANGLE = "zoomRect";
    private String hoverMapId = null;

    //Constanst to draw the legend
    private final int LEGEND_WIDTH = 200;
    private final int LEGEND_HEIGHT = 100;
    private final int LEGEND_PADDING = 10;
    private int LEGEND_ARC = 8;
    public static boolean SORT_MAPS = false;

    //Stores the mouse action selecte by the user
    private MapViewerMouseState mouseState = MapViewerMouseState.POINTER;
    private boolean isPanning = false;
    private Point initialPoint = new Point();

    //Cursor's icons
    private final String ZOOMIN_ICON = "/edu/wisc/lmcg/resources/zoomin_icon_map.png";
    private final String ZOOMOUT_ICON = "/edu/wisc/lmcg/resources/zoomout_icon_map.png";

    //Resource for an empty canvas
    private final String EMPTY_CANVAS = "/edu/wisc/lmcg/resources/empty_drawing.svg";

    private SVGDocument doc;
    private ContigDrawer c_drawer;
    private boolean isDocumentReady = false;
    private boolean firstRendering = true;

    private final int FRAG_INFO_DELAY = 1000;
    private Runnable onCanvasIsReadyCallback = null;
    
    private LinkedList<String> ordeList = new LinkedList<>();  
    private OpenAlignmentDialog sortMapsDialog;

    public MapsPanel(ContigRun crun, Runnable onCanvasIsReady) {
        super();
        
        this.onCanvasIsReadyCallback = onCanvasIsReady;
        
        //reset the contig drawing

        //Make the background completely black
        setBackground(Color.BLACK);

        addSVGDocumentLoaderListener(this);
        addGVTTreeRendererListener(this);

        //add mouse listeners to this panel
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        
        if ( SORT_MAPS )
        {
        
            sortMapsDialog = new OpenAlignmentDialog(null, true);
            
            //Create a list with all the reference map names
            LinkedList<String> refmap_names = new LinkedList<>();
            for ( Contig c : crun.getContigs().values() ){
                refmap_names.add(c.getReferenceMapName());
            }
            sortMapsDialog.setReferenceMapNames(refmap_names);
            
            sortMapsDialog.setVisible(true);
        }

        try {
            
            this.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
            startLoadingState();
            this.setURI(this.getClass().getResource(EMPTY_CANVAS).toURI().toString());
        } catch (URISyntaxException ex) {            
            stopLoadingState();
        }

        //get a set of all contigs
        this.crun = crun;
        this.mouseState = MapViewerMouseState.NONE;
    }

    public void setAllAlignLinesVisible(boolean visible) {

        if (visible) {
            c_drawer.showAllAlignmentLines();
        } else {
            c_drawer.hideAllAlignmentLines();
        }
    }

    /**
     * sets the x coordinate of every map to the x coordinate of the left side
     * of its alignment to the reference map;
     */
    public void resetAllMapXs() {
        for (MapDrawing aMap : mapDrawings.values()) {            
            aMap.resetX();
        }
    }

    public ContigRun getContigRun() {
        return crun;
    }

    /**
     * sets the preferred size of this panel based on the width and number of
     * displayed maps
     */
    /**
     * Don't use for adding aligned maps. Adds unaligned maps to the panel.
     *
     * @param moreMaps
     */
    public void addMaps(Collection<SimpleRestrictionMap> moreMaps) {

        //yCounter = MapDrawing.getMaxY() + yBuffer;

        for (SimpleRestrictionMap aMap : moreMaps) {
            addMap(aMap, yCounter);
            yCounter += yBuffer;
        }

    }

    /**
     * don't use for adding aligned maps. Adds an unaligned map to the panel.
     *
     * @param rMap
     * @param y
     */
    public void addMap(SimpleRestrictionMap rMap, double y) {

        //give the map a drawing and add it to the panel
        MapDrawing drawing = new MapDrawing(rMap);
        String name = drawing.getName();
        if (!mapDrawings.containsKey(name)) {
            mapDrawings.put(drawing.getName(), drawing);            
            drawing.setCornerCoords(MapDrawing.DEFAULT_PADDING, y);
            drawing.paintComponent();
            drawing.paintAlignLines();
        } else {
            JOptionPane.showMessageDialog(this,
                    name + " not added because a map with that name already exists",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        //create a record for this map in the contig run
        rMap.setMapViewerX(MapDrawing.DEFAULT_PADDING);
        rMap.setMapViewerY(y);
        Mapset mapset = crun.getMapset();
        mapset.addRestrictionMap(rMap);
    }

    private Comparator<Contig> refMapLengthComparator(){
        return (Contig o1, Contig o2) -> {
            MapDrawing refMap1 = mapDrawings.get(o1.getReferenceMapName());
            MapDrawing refMap2 = mapDrawings.get(o2.getReferenceMapName());
            //Logest reference map means a larger contig

            List<FragRectangle> fragRectangles1 = refMap1.getFragRectangles();
            List<FragRectangle> fragRectangles2 = refMap2.getFragRectangles();
            double sizeRefMap1 = fragRectangles1.get(fragRectangles1.size()-1).getPosition() + fragRectangles1.get(fragRectangles1.size()-1).getWidth();
            double sizeRefMap2 = fragRectangles2.get(fragRectangles2.size()-1).getPosition() + fragRectangles2.get(fragRectangles2.size()-1).getWidth();
            if ( sizeRefMap1 == sizeRefMap2 ){
                return 0;
            }else if ( sizeRefMap1 < sizeRefMap2 ){
                return 1;
            }else{
                return -1;
            }
        };
    }
    
    private Comparator<Contig> customOrderComparator(){
        
        return (Contig o1, Contig o2) -> {
            if ( ordeList.indexOf(o1.getReferenceMapName()) == ordeList.indexOf(o2.getReferenceMapName()) )
                    return 0;
                else if ( ordeList.indexOf(o1.getReferenceMapName()) > ordeList.indexOf(o2.getReferenceMapName()) )
                    return 1;
                else
                    return -1;
        };
    }
    
    /**
     * assign each map a y position on the panel according to contig order (i.e.     
     * @param sortMaps Ignores the map order specified in the xml file
     */
    private void arrangeMapDrawings(boolean sortMaps) {
        arrangeMapDrawings(sortMaps, this.customOrderComparator());
    }
    
    /**
     * assign each map a y position on the panel according to contig order (i.e.     
     * @param sortMaps Ignores the map order specified in the xml file
     * @param contigComparator 
     */
    private void arrangeMapDrawings(boolean sortMaps, Comparator<Contig> contigComparator) {

        //The contigs are placed in an order in which the longes reference map is on top
        Map<String, Contig> contigs = crun.getContigs();
        yCounter = yBuffer;
        List<Contig> arrangedContigs = new ArrayList<>(contigs.values());
        
        //Sorts the contig usign the contigComparator criterion
        Collections.sort(arrangedContigs, contigComparator);

        //set the placement of each map so that each reference map is above its aligned maps
        for ( Contig c : arrangedContigs ) {
            arrangeMapsInsideContig(c, sortMaps);
            yCounter += yBuffer;
        }

        //then print all the unaligned maps at the bottom
        for (MapDrawing aMap : mapDrawings.values()) {
            if (!aMap.isAligned()) {
                aMap.setX(MapDrawing.DEFAULT_PADDING);
                aMap.setY(yCounter);
                yCounter += yBuffer;
            }
        }
    }

    /**
     * populates the mapDrawings hashtable with a drawing for every restriction
     * map in the given mapset
     *
     * @param mapsTable
     */
    private void makeMapDrawings(Map<String, RestrictionMap> mapsTable) {
        //create (but don't yet display) a drawing for every map in contig order...
        mapDrawings = new Hashtable<>();
        MapDrawing.resetMaxX();
        MapDrawing.resetMaxY();
        int mapCounter = 0;
        for (RestrictionMap resMap : mapsTable.values()) {
            mapCounter++;
            //for each map, prepare, but don't yet display, a drawing
            //FIXME: The next cast must not be done.
            MapDrawing aMap = new MapDrawing((SimpleRestrictionMap) resMap);
            mapDrawings.put(aMap.getName(), aMap);
        }
        
        MapDrawing.setMaxY(mapCounter*yBuffer);
        
    }

    private void arrangeMapsInsideContig(Contig contig, boolean sortMaps) {

        //place the contig's reference map in the next spot (after an extra vertical space) in the panel
        MapDrawing refMap = mapDrawings.get(contig.getReferenceMapName());
        if (refMap == null) {
            return; //if there's no reference map, we don't want to try any of this
        }        

        MapDrawing[] contigAlMaps = getUnsortedDrawingArray(contig);
        
        //Removes from the contigAlMaps array, those maps whose alignment to the current reference maps
        //isn't the strogest (more alignment lines)
        contigAlMaps = checkMaps(contigAlMaps, contig);
        
        addAlignLines(contig, refMap); //by adding alignlines here, we can know where the maps are positioned
        //and use that information to sort them on the next line

        //Sorts the maps placing the logest in the top of the array
        Arrays.sort(contigAlMaps, (MapDrawing o1, MapDrawing o2) -> {
            List<StandardMapAlignment> als = contig.getMapAlignments().get(o1.getName());
            int maxAlignmentSize1 = 0;
            for ( StandardMapAlignment sma : als ){
                int alignmentSize = sma.getFragAlignments().size();
                if ( alignmentSize > maxAlignmentSize1 ){
                    maxAlignmentSize1 = alignmentSize;
                }
            }
            
            als = contig.getMapAlignments().get(o2.getName());
            int maxAlignmentSize2 = 0;
            for ( StandardMapAlignment sma : als ){
                int alignmentSize = sma.getFragAlignments().size();
                if ( alignmentSize > maxAlignmentSize2 ){
                    maxAlignmentSize2 = alignmentSize;
                }
            }
            
            if ( maxAlignmentSize1 == maxAlignmentSize2 )
                return 0;
            else if ( maxAlignmentSize1 < maxAlignmentSize2 )
                return 1;
            else
                return -1;
        });
        
        double xcenter = 0;
        
        int n = (int) Math.ceil(((double) contigAlMaps.length ) / 2.0 );        

        //Place all the aligned maps around the reference map
        int indexCounter = 0;
        for ( int i = 1; i <= n; i++){
            
            MapDrawing topMap = contigAlMaps[indexCounter++];
            topMap.startMovingMaps();
            if (topMap.getY() <= -1 || sortMaps ) {
                topMap.setY(yCounter + (n - i) * yBuffer);                
            }
            if (topMap.getX() <= -1 || sortMaps ) {
                topMap.moveToItsAlignedPosition();
            }
            topMap.stopMovingMaps();
            xcenter += (2*topMap.getX() + topMap.getWidth()) / ((double) contigAlMaps.length );
            
            if ( indexCounter < contigAlMaps.length ){                
                MapDrawing bottomMap = contigAlMaps[indexCounter++];
                bottomMap.startMovingMaps();
                if ( bottomMap.getY() <= -1 || sortMaps ) {
                    bottomMap.setY(yCounter + (n + i) * yBuffer);                
                }
                if ( bottomMap.getX() <= -1 || sortMaps ) {
                    bottomMap.moveToItsAlignedPosition();
                }
                bottomMap.stopMovingMaps();
                xcenter = 2*bottomMap.getX() + bottomMap.getWidth() / ((double) contigAlMaps.length );
            }
            
        }
        
        //place the reference map in the middle of all its aligned maps
        refMap.startMovingMaps();
        if ( refMap.getY() <= -1 || sortMaps ) { //but only if it hasn't already been assigned a position
            refMap.setY(yCounter + yBuffer * n);            
        }

        //coord set to negative value means that map doesn't already have an assigned position
        if (refMap.getX() <= -1 || sortMaps) {
            refMap.setX( xcenter - refMap.getWidth() / 2.0 );
        }
        refMap.stopMovingMaps();
        
        yCounter += (contigAlMaps.length + 1) * yBuffer;
    }
    
    private MapDrawing[] checkMaps(MapDrawing[] maps, Contig currentContig){
        
        List<MapDrawing> newList = new ArrayList<>();
        
        
        Contig mContig = null;         
        for (MapDrawing d : maps){
            int maxNumLines = 0;
            for (Contig contig : crun.getContigs().values()) {
                
                if ( d == null ){
                    continue;
                }
                
                Map<String, List<StandardMapAlignment>> aaa = contig.getMapAlignments();
                
                List<StandardMapAlignment> als = aaa.get(d.getName());
                //List<StandardMapAlignment> als = contig.getMapAlignments().get(d.getName());                
                
                if ( als == null )
                    continue;
                
                for ( StandardMapAlignment sma : als ){
                    int alignmentSize = sma.getFragAlignments().size();
                    if ( alignmentSize > maxNumLines ){
                        maxNumLines = alignmentSize;
                        mContig = contig;
                    }
                }
            }
            
            if ( mContig != null && mContig.equals(currentContig) ){
                newList.add(d);
            }
        }
        return newList.toArray(new MapDrawing[newList.size()]);
    }

    private MapDrawing[] getUnsortedDrawingArray(Contig contig) {

        //get that reference map's alignments
        Map<String, List<StandardMapAlignment>> alignments = contig.getMapAlignments();

        //will hold a drawing of each aligned map
        MapDrawing[] contigAlMaps = new MapDrawing[alignments.size()];
        int i = 0;

        //for each non-null alignment, put the aligned map in the array
        for (List<StandardMapAlignment> mapAlignVector : alignments.values()) {
            
            if (mapAlignVector == null || mapAlignVector.isEmpty() == true) {
                continue;
            }

            //Still not sure why this is a Vector and not an individual map alignment, but I'm leaving it how it is.
            StandardMapAlignment alignment = mapAlignVector.get(0);

            //find the MapDrawing corresponding to the aligned map and assign it to the next spot in the panel
            MapDrawing alignedMap = mapDrawings.get(alignment.getAlignedMapName());

            contigAlMaps[i] = alignedMap;
            i++;
        }
        return contigAlMaps;
    }

    /**
     * Gives a set of alignLines to every alignment in the given contig
     *
     * @param contig a set of one reference map's alignments
     * @param refMap the contig's refMap
     */
    private void addAlignLines(Contig contig, MapDrawing refMap) {

        //get the reference map's alignments
        Map<String, List<StandardMapAlignment>> alignments = contig.getMapAlignments();

        for (String alignedMapName : alignments.keySet()) {

            List<StandardMapAlignment> mapAlignVector = alignments.get(alignedMapName);
            if (mapAlignVector == null || mapAlignVector.isEmpty() == true) {
                continue;
            }

            //find the MapDrawing corresponding to the aligned map and assign it to the next spot in the panel
            MapDrawing alignedMap = mapDrawings.get(alignedMapName);

            for (StandardMapAlignment alignment : mapAlignVector) {

                //unless the aligned map is null, give the aligned map a set of AlignLines
                if (alignedMap != null) {
                    alignedMap.addAlignment(new AlignLines(refMap, alignedMap, alignment));
                }
            }
        }
    }

    private void quicksort(MapDrawing[] maps) {
        if (maps.length <= 1) {
            return;
        }
        quicksort(maps, 0, maps.length - 1);
    }

    /**
     * Fastest sorting algorithm in the West.
     *
     * @param maps array of map drawings to be sorted by position relative to
     * the reference map
     * @param lo left side of the subarray
     * @param hi right side of the subarray
     */
    private void quicksort(MapDrawing[] maps, int lo, int hi) {
        
        if (hi - lo <= 0) {
            return; //1 or fewer maps
        }
        int left = lo + 1;
        int right = hi;
        MapDrawing pivot = maps[lo];

        while (true) {
            while (left <= right) {
                if (maps[left].getX() > pivot.getX()) {
                    break;
                } else {
                    left++;
                }
            }

            while (right >= left) {
                if (maps[right].getX() < pivot.getX()) {
                    break;
                } else {
                    right--;
                }
            }

            if (left >= right) {
                break;
            }

            //swap the right and left
            MapDrawing tmp = maps[left];
            maps[left] = maps[right];
            maps[right] = tmp;

            left++;
            right--;
        }

        //swap the pivot with the map at the split point, left-1
        maps[lo] = maps[left - 1];
        maps[left - 1] = pivot;

        //recursively sort the left and right subarrays
        quicksort(maps, lo, left - 2);
        quicksort(maps, left, hi);
    }

    private boolean setActiveMap(MouseEvent e) {

        try {
            Point2D realMousePoint = c_drawer.screen2RealCoords(e.getX(), e.getY());
            //iterate through each map drawing to see if the mouse is over one of them...
            for (MapDrawing aMap : mapDrawings.values()) {
                
                //... if it is, set the active map and coordinate offsets
                if (aMap.contains(realMousePoint.getX(), realMousePoint.getY())) {
                    activeMap = aMap;
                    xOffset = activeMap.getX() - realMousePoint.getX();
                    yOffset = activeMap.getY() - realMousePoint.getY();
                    return true;
                }
            }
            return false;
        } catch (NoninvertibleTransformException ex) {
            return false;
        }
    }

    private boolean setHoverMap(MouseEvent e) {
        if (!isDocumentReady) {
            return false;
        }

        try {
            Point2D realMousePoint = c_drawer.screen2RealCoords(e.getX(), e.getY());
            //If there's already a hovermap and we're still hovering over it, we're done.
            if (hoverMap != null && hoverMap.contains(realMousePoint.getX(), realMousePoint.getY())) {

                xOffset = hoverMap.getX() - realMousePoint.getX();
                yOffset = hoverMap.getY() - realMousePoint.getY();
                return true;
            }

            //iterate through each map drawing to see if the mouse is over one of them...
            for (MapDrawing aMap : mapDrawings.values()) {                

                //... if it is, set the hover map and coordinate offsets
                if (aMap.contains(realMousePoint.getX(), realMousePoint.getY())) {
                    hoverMap = aMap;
                    xOffset = hoverMap.getX() - realMousePoint.getX();
                    yOffset = hoverMap.getY() - realMousePoint.getY();
                    return true;
                }
            }
            return false;
        } catch (NoninvertibleTransformException ex) {
            return false;
        }
    }

    public void writeSVG(String filename) {
        // Write the generated SVG document to a file
        try {
            OutputFormat format = new OutputFormat(doc);
            format.setIndenting(true);
            XMLSerializer serializer = new XMLSerializer(
                    new FileOutputStream(new File(filename)), format);
            serializer.serialize(doc);

        } catch (Exception ioe) {

        }
    }

    //@Override
    public void paintComponentSVG() {

        for (MapDrawing aMap : mapDrawings.values()) {
            
            aMap.paintComponent();
        }

        //Show the alignmentn lines between maps
        for (MapDrawing aMap : mapDrawings.values()) {            
            aMap.paintAlignLines();
        }

        /*//Show the legend if it was requested
         if (showLegend) {
         paintLegend(g2d);
         }*/
        this.setEnableRotateInteractor(false);
    }

    @Override
    public void mousePressed(MouseEvent e) {

        initialPoint = e.getPoint();
        switch (this.mouseState) {
            case PAN:
            case ZOOM_IN:
            case ZOOM_OUT:
                break;
            case POINTER:

                if (hoverMap != null) {
                    activeMap = hoverMap;
                    //Start the functionality for moving a map in the canvas
                    activeMap.startMovingMaps();
                }

                this.setEnableRotateInteractor(false);
                //if (activeMap == null) setActiveMap(e);
                break;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        switch (this.mouseState) {
            case PAN:
                AffineTransform at = new AffineTransform();
                double dx = e.getPoint().x - initialPoint.x;
                double dy = e.getPoint().y - initialPoint.y;

                at.translate(dx, dy);

                at.concatenate(this.getRenderingTransform());
                this.setRenderingTransform(at);

                initialPoint = e.getPoint();
                break;
            case POINTER:
                if (activeMap != null) {

                    try {
                        //Hides the intermediate alignment lines while the map it's moving
                        c_drawer.hideIntermediateLines(activeMap.getName());
                        Point2D realCoords = c_drawer.screen2RealCoords(e.getX(), e.getY());
                        activeMap.setCornerCoords(realCoords.getX() + xOffset, realCoords.getY() + yOffset);

                    } catch (NoninvertibleTransformException ex) {
                        Logger.getLogger(MapsPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            case ZOOM_IN:
            case ZOOM_OUT:

                double selectionRectWidth = Math.abs(e.getPoint().x - initialPoint.x);
                double selectionRectHeight = Math.abs(e.getPoint().y - initialPoint.y);

                double scale = Math.min(
                        ((double) this.getParent().getWidth()) / ((double) selectionRectWidth),
                        ((double) this.getParent().getHeight()) / ((double) selectionRectHeight));

                Point2D rectPosition = new Point();
                rectPosition.setLocation(
                        Math.min(e.getPoint().x, initialPoint.x),
                        Math.min(e.getPoint().y, initialPoint.y));

                showZoomRectangle(rectPosition, selectionRectWidth, selectionRectHeight);

                break;
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {

        //if the map exceeds the panel dimensions, then it stops moving the map                    
        if (activeMap != null) {
            activeMap.updateContigCoords();
            //Stops the moving funcionality and shows the intermediate lines again after drop the map in its final position            
            activeMap.stopMovingMaps();
            c_drawer.showIntermediateLines(activeMap.getName());
        }
        activeMap = null;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
        switch (this.mouseState) {
            case ZOOM_IN:
            case ZOOM_OUT:

                Point2D canvasInitialPoint = new Point2D.Double();
                Point2D canvasFinalPoint = new Point2D.Double();
                try {
                    canvasInitialPoint = c_drawer.screen2CanvasCoords(initialPoint);
                    canvasFinalPoint = c_drawer.screen2CanvasCoords(e.getPoint());
                } catch (NoninvertibleTransformException ex) {
                    Logger.getLogger(MapsPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            
                zoomTo( canvasInitialPoint.getX(), canvasInitialPoint.getY(), canvasFinalPoint.getX(), canvasFinalPoint.getY() );
            
                break;
        }

        if (activeMap != null) {
            activeMap.updateContigCoords();
            //Stops the moving funcionality and shows the intermediate lines again after drop the map in its final position            
            activeMap.stopMovingMaps();
            c_drawer.showIntermediateLines(activeMap.getName());
        }
        activeMap = null;

        hideZoomRectangle();
    }

    private void zoomTo(double x1, double y1, double x2, double y2) {

        double xCurrent = x2;
        double yCurrent = y2;
        double xStart = x1;
        double yStart = y1;

        if ((xCurrent - xStart) != 0
                && (yCurrent - yStart) != 0) {

            int dx = (int) (xCurrent - xStart);
            int dy = (int) (yCurrent - yStart);

            if (dx < 0) {
                dx = -dx;
                xStart = xCurrent;
            }
            if (dy < 0) {
                dy = -dy;
                yStart = yCurrent;
            }

            Dimension size = this.getParent().getSize();

            // Zoom factor
            float scaleX = size.width / (float) dx;
            float scaleY = size.height / (float) dy;
            float scale = (scaleX < scaleY) ? scaleX : scaleY;

            // Zoom translate
            AffineTransform at = new AffineTransform();
            at.scale(scale, scale);
            at.translate(-xStart, -yStart);

            at.concatenate(this.getRenderingTransform());
            this.setRenderingTransform(at);
        }
    }

    private void zoomTo(Point p, boolean zoomIn) {

        AffineTransform at = new AffineTransform();

        double f;
        if (zoomIn) {
            f = Math.pow(1 + ZOOM_STEP, 1);
        } else {
            f = Math.pow(1 + ZOOM_STEP, -1);
        }

        at.scale(f, f);
        at.translate(-p.x, -p.y);
        at.concatenate(this.getRenderingTransform());
        this.setRenderingTransform(at);

        at = new AffineTransform();
        at.translate(p.x, p.y);
        at.concatenate(this.getRenderingTransform());
        this.setRenderingTransform(at);
    }

    private void zoomTo(Point2D p, double zoomFactorX, double zoomFactorY) {

        AffineTransform at = new AffineTransform();

        at.scale(zoomFactorX, zoomFactorY);
        at.translate(-p.getX(), -p.getY());
        at.concatenate(this.getRenderingTransform());
        this.setRenderingTransform(at);

        at = new AffineTransform();
        at.translate(p.getX(), p.getY());
        at.concatenate(this.getRenderingTransform());
        this.setRenderingTransform(at);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
        if (this.mouseState == MapViewerMouseState.ZOOM_IN) {
            zoomTo(e.getPoint(), true);
        } else if (this.mouseState == MapViewerMouseState.ZOOM_OUT) {
            zoomTo(e.getPoint(), false);
        }else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
            zoomTo(e.getPoint(), true);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        //setHoverMap returns true iff the mouse is over a frag
        if (!setHoverMap(e)) {

            //if the mouse isn't over a fragment, set hoverfrag to null
            hoverMap = null;
            if (hoverFrag != null) {
                //c_drawer.hideFragmentInfo();
            }
            hoverFrag = null;
        } else {
            if (hoverFrag != null) {

                //if the mouse is still in the old frag, do nothing
                if (hoverFrag.isScreenPointOverFragment(e.getX(), e.getY())) {
                    return;
                }

                //if the mouse has moved to a new frag, stop displaying the old frag's info
                //c_drawer.hideFragmentInfo();
            }

            //Setup the variable hoverFrag which indicates what is the fragment beneath the mouse pointer
            for (FragRectangle frag : hoverMap.getFragRectangles()) {
                if (frag.isScreenPointOverFragment(e.getX(), e.getY())) {
                    hoverFrag = frag;

                    //Makes a delay of 1/2 seconds to show the frag info the first time
                    //the mouse is inside the current frag
                    Thread delay = new Thread(() -> {

                        try {
                            Thread.sleep(FRAG_INFO_DELAY);
                            //c_drawer.showFragmentInfo(frag);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MapsPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    });
                    delay.start();

                    break;
                }
            }
        }
    }

    /**
     * Removes a given map's alignments by going through each contig and
     * deleting map alignments for which the given map is the aligned map
     *
     * @param name the name of the given map
     */
    private void removeAMapsAlignments(String name) {

        for (Contig contig : crun.getContigs().values()) {
            Map<String, List<StandardMapAlignment>> alignments = contig.getMapAlignments();
            alignments.remove(name);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Object source = e.getSource();

        if (source == toggleAlignLines) {
            rightClickMap.toggleAlignLines();
        } else if (source == deleteMap) {

            //remove the drawing and its alignlines
            rightClickMap.removeAlignLines();
            mapDrawings.remove(rightClickMap.getName());

            //delete the restriction map and alignments that the drawing is based on
            Map<String, RestrictionMap> rMaps = crun.getMapset().getRestrictionMaps();
            rMaps.remove(rightClickMap.getName());
            removeAMapsAlignments(rightClickMap.getName());
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();

        //if notches > 0 => zoom out else => zoom in        
        Point currentPoint = e.getPoint();

        AffineTransform at = new AffineTransform();
        double scaleX = Math.pow(1 + ZOOM_STEP, -notches);
        double scaleY = Math.pow(1 + 0.05 * ZOOM_STEP, -notches);

        //The trasnformation scales the "x" axis but keeps the "y" axis' scale
        at.scale(scaleX, scaleY);
        at.concatenate(this.getRenderingTransform());
        this.setRenderingTransform(at);

        //Adjusts the title of each map to prevent deformations
        c_drawer.adjustMapTitle(1 / scaleX, 1 / scaleY);
    }

    public void setShowLegend(boolean value) {
        this.showLegend = value;
    }

    private void paintLegend(Graphics2D g2d) {
        //the legend will be show only if the painting spaces is greater than a square of 50x100 pixels
        //plus a padding of 10 px each side
        if (getWidth() > LEGEND_WIDTH + 2 * LEGEND_PADDING && getHeight() > LEGEND_HEIGHT + 2 * LEGEND_PADDING) {
            g2d.setColor(Color.RED);
            g2d.fillRoundRect(
                    getWidth() - LEGEND_WIDTH - LEGEND_PADDING,
                    getHeight() - LEGEND_HEIGHT - LEGEND_PADDING,
                    LEGEND_WIDTH,
                    LEGEND_HEIGHT,
                    LEGEND_ARC,
                    LEGEND_ARC
            );
            g2d.setColor(Color.BLACK);
        }
    }

    private void setMapNamesVisibility(boolean value) {
        for (MapDrawing map : mapDrawings.values()) {
            map.setShowMapName(value);
        }
    }

    public void showMapNames() {
        setMapNamesVisibility(true);
        
        c_drawer.setMapNamesVisibility(true);
    }

    public void hideMapNames() {
        setMapNamesVisibility(false);
        c_drawer.setMapNamesVisibility(false);
    }

    public void setMouseState(MapViewerMouseState type) {

        Toolkit toolkit;
        Image cursorImage;
        Point cursorHotSpot;

        JFrame frame = (JFrame) SwingUtilities.getRoot(this);
        switch (type) {
            case NONE:
                frame.setCursor(Cursor.getDefaultCursor());
                this.mouseState = MapViewerMouseState.NONE;
                break;
            case PAN:
                frame.setCursor(new Cursor(Cursor.MOVE_CURSOR));
                this.mouseState = MapViewerMouseState.PAN;
                break;
            case POINTER:
                frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                this.mouseState = MapViewerMouseState.POINTER;
                break;
            case ZOOM_IN:
                toolkit = Toolkit.getDefaultToolkit();
                toolkit.getMaximumCursorColors();
                cursorImage = MapGUI.getScalatedIcon(ZOOMIN_ICON, 32, 32);
                cursorHotSpot = new Point(0, 0);
                frame.setCursor(toolkit.createCustomCursor(cursorImage, cursorHotSpot, "ZoomInCursor"));
                this.mouseState = MapViewerMouseState.ZOOM_IN;
                break;
            case ZOOM_OUT:
                toolkit = Toolkit.getDefaultToolkit();
                cursorImage = MapGUI.getScalatedIcon(ZOOMOUT_ICON, 32, 32);
                cursorHotSpot = new Point(0, 0);
                frame.setCursor(toolkit.createCustomCursor(cursorImage, cursorHotSpot, "ZoomOutCursor"));
                this.mouseState = MapViewerMouseState.ZOOM_OUT;
                break;
        }
    }

    private void startLoadingState() {                
        JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
        if ( frame != null ){
            this.setEnabled(false);
        }
        this.setCursor(new Cursor((Cursor.WAIT_CURSOR)));
    }

    private void stopLoadingState() {
        JFrame frame = (JFrame) SwingUtilities.getRoot(this);
        if ( frame != null ){
            this.setEnabled(true);
        }
        this.setCursor(new Cursor((Cursor.DEFAULT_CURSOR)));
    }

    @Override
    public void documentLoadingStarted(SVGDocumentLoaderEvent svgdle) {

    }

    @Override
    public void documentLoadingCompleted(SVGDocumentLoaderEvent svgdle) {
        doc = getSVGDocument();       
        
        isDocumentReady = true;
        c_drawer = ContigDrawer.getDrawer(doc, this);
        c_drawer.resetDrawer();

        Map<String, RestrictionMap> mapsTable = this.crun.getMapset().getRestrictionMaps();

        //make and arrange the map drawings
        makeMapDrawings(mapsTable);   
        
        //if this option is enabled, then all the contigs will be re-arranged
        //this.getUpdateManager().suspend();
        if ( SORT_MAPS ){            
            if ( sortMapsDialog.getOrderMethod() == OrderMethod.CustomList ){
                ordeList = sortMapsDialog.getCustomList();
                arrangeMapDrawings(true, this.customOrderComparator());
            }else if ( sortMapsDialog.getOrderMethod() == OrderMethod.ReferenceMapName ){
                arrangeMapDrawings(true, this.refMapLengthComparator());
            }
            else{
                arrangeMapDrawings(true, this.refMapLengthComparator());
            }
        }else{
            arrangeMapDrawings(false, this.refMapLengthComparator());
        }

        //now that we've drawn every map, set the width and depth of the canvas
        c_drawer.setCanvasDimension();            

        //Draws all the elements over the canvas
        paintComponentSVG();
        
        //fire the callback
        if ( onCanvasIsReadyCallback != null)
            onCanvasIsReadyCallback.run();
    }

    @Override
    public void documentLoadingCancelled(SVGDocumentLoaderEvent svgdle) {
        stopLoadingState();
    }

    @Override
    public void documentLoadingFailed(SVGDocumentLoaderEvent svgdle) {
        stopLoadingState();
    }

    @Override
    public void gvtRenderingPrepare(GVTTreeRendererEvent gvttre) {

    }

    @Override
    public void gvtRenderingStarted(GVTTreeRendererEvent gvttre) {

    }

    @Override
    public void gvtRenderingCompleted(GVTTreeRendererEvent gvttre) {
        
        //Updates the view to an initial zoom view
        if (firstRendering) {
            zoomToDrawing();
            firstRendering = false;
        }
        
        stopLoadingState();
    }

    @Override
    public void gvtRenderingCancelled(GVTTreeRendererEvent gvttre) {

    }

    @Override
    public void gvtRenderingFailed(GVTTreeRendererEvent gvttre) {
        stopLoadingState();
    }

    public void setShiftResponder(AbstractAction a) {
        //It needs to respond to the event "KeyPressed" for the key Shift and anulate the mouse option selection
        String ACTION = "ShiftAction";
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK, false), ACTION);
        getActionMap().put(ACTION, a);
    }

    private void showZoomRectangle(Point2D leftUpperPosition, double width, double height) {
        try {
            Element zoomRect = doc.getElementById(ZOOM_RECTANGLE);

            Point2D rightBottomPosition = new Point2D.Double();
            rightBottomPosition.setLocation(leftUpperPosition.getX() + width, leftUpperPosition.getY() + height);

            leftUpperPosition = c_drawer.screen2CanvasCoords(leftUpperPosition);
            rightBottomPosition = c_drawer.screen2CanvasCoords(rightBottomPosition);

            //If some of the points are outside de boundary it doesn't show the zoomRectangle
            if (leftUpperPosition.getX() < 0
                    || leftUpperPosition.getX() > c_drawer.getCanvasWidth()
                    || leftUpperPosition.getY() < 0
                    || leftUpperPosition.getY() > c_drawer.getCanvasHeight()
                    || rightBottomPosition.getX() < 0
                    || rightBottomPosition.getX() > c_drawer.getCanvasWidth()
                    || rightBottomPosition.getY() < 0
                    || rightBottomPosition.getY() > c_drawer.getCanvasHeight()) {
                return;
            }

            double stroke_width = 0.1 * MapDrawing.DEFAULT_FRAGMENT_HEIGHT * c_drawer.getCanvasHeight() / MapDrawing.getMaxY();;

            zoomRect.setAttribute("visibility", "visible");
            zoomRect.setAttribute("width", "" + (rightBottomPosition.getX() - leftUpperPosition.getX()));
            zoomRect.setAttribute("height", "" + (rightBottomPosition.getY() - leftUpperPosition.getY()));
            zoomRect.setAttribute("transform", "translate(" + leftUpperPosition.getX() + ", " + leftUpperPosition.getY() + ")");
            zoomRect.setAttribute("style", "stroke-dasharray:" + stroke_width + ";stroke:rgb(255,255,255);stroke-width:" + stroke_width);

        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(MapsPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void hideZoomRectangle() {
        Element zoomRect = doc.getElementById(ZOOM_RECTANGLE);
        zoomRect.setAttribute("visibility", "hidden");
    }

    public boolean getScreenShot(String filename, String type) {
        Rectangle screen_rect = this.getVisibleRect();
        Rectangle.Double aoi;
        try {
            Point2D initial_p = c_drawer.screen2CanvasCoords(screen_rect.x, screen_rect.y);
            Point2D final_p = c_drawer.screen2CanvasCoords(screen_rect.x + screen_rect.width, screen_rect.y + screen_rect.height);
            aoi = new Rectangle.Double(
                    initial_p.getX(),
                    initial_p.getY(),
                    final_p.getX() - initial_p.getX(),
                    final_p.getY() - initial_p.getY());

            if (type.equals("png")) {
                return c_drawer.canvasImageToPng(filename, aoi) != null;
            } else if (type.equals("svg")) {
                return c_drawer.canvasImageToSVGFile(filename, aoi);
            } else {
                return false;
            }

        } catch (Exception ex) {
            return false;
        }
    }
    
    public void sortMaps(){
        startLoadingState();
        arrangeMapDrawings(true, this.refMapLengthComparator());
        stopLoadingState();
    }

    public void goToOrigin(){
        
        try {
            
            Point2D in = c_drawer.canvas2ScreenCoords(0.0, 0.0);
            AffineTransform at = new AffineTransform();
            at.translate(-in.getX(), -in.getY());
            at.concatenate(this.getRenderingTransform());
            this.setRenderingTransform(at);            
            
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(MapsPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void zoomToDrawing() {        
        Dimension d = this.getParent().getSize();
        double scaleX = 1.1 * (((double) d.width) / c_drawer.getCanvasWidth());
        double scaleY = 2.7 * (((double) d.height) / c_drawer.getCanvasHeight() );
        
        zoomTo(
                new Point(0, 0),
                scaleX, scaleY
        );
        
        //Adjusts the title of each map to prevent deformations
        c_drawer.adjustMapTitle(scaleY / scaleX, 1);
    }
}
