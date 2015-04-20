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

import edu.wisc.lmcg.alignment.FragAlignment;
import edu.wisc.lmcg.alignment.mapalignment.StandardMapAlignment;
import java.util.ArrayList;

/**
 * An AlignLines object stores the information needed to draw the pair of lines
 * that designates an alignment in a GUI. Keeps special variables for the lines
 * on the left an rightmost sides of the alignment and keeps the rest of the
 * alignment lines in an ArrayList
 *
 * @author dmeyerson
 *
 */
public class AlignLines {

    //coordinates of the ends of the lines
    private double alignLeftX, alignRightX, alignY,
            refLeftX, refRightX, refY; //confusing so pay attention: refLeftX is
            //the x coordinate of the location on the
            //reference map corresponding to alignLeftX.
            //It is not necessarily the left side of the
            //aligned region of the ref map. Same with
            //refRightX
    private int leftAlMapIndex, rightAlMapIndex,
            leftRefMapIndex, rightRefMapIndex;

    private MapDrawing refMap, alMap;
    private StandardMapAlignment alignment;
    private boolean isReversed = false, visible = true;
    private FragRectangle leftAlignRect, leftRefRect,
            rightAlignRect, rightRefRect;
    private ArrayList<Line> lines; //will hold all of the lines between the ends of this alignment
    private int fragHeight = 30;    //Height of the fragment

    private ContigDrawer c_drawer;
    public static int LINE_FREQUENCY = 10;
    public static double MINIMUM_GAP_SIZE = 30;

    /**
     * constructs a set of alignlines, if either the reference or aligned map is
     * null, constructs nothing
     *
     * @param refMap drawing representing the reference map
     * @param alMap
     * @param alignment details where the alignment starts and ends on each
     * molecule
     */
    public AlignLines(MapDrawing refMap, MapDrawing alMap, StandardMapAlignment alignment) {

        if (alMap == null || refMap == null) {
            return;
        }
        this.c_drawer = ContigDrawer.getDrawer();
        this.refMap = refMap;
        refMap.setAligned(true);
        this.alMap = alMap;
        alMap.setAligned(true);
        this.alignment = alignment;
        isReversed = !alignment.isForwardOriented();
        this.refMap.addAlignment(this);
        lines = new ArrayList<>();
		//isReversed = !alignment.isForwardOriented(); //don't think we ever use this

        //Get the rectangles at the ends of the left side of the alignment.
        leftAlMapIndex = alignment.getIndexOfFirstFragAlignment(); //index of the aligned Map's first aligned fragment
        leftRefMapIndex = pairRectangles(leftAlMapIndex, false);

        //Get the rectangles at the ends of the right side of the alignment.
        rightAlMapIndex = alignment.getIndexOfLastFragAlignment(); //index of the aligned Map's last aligned fragment
        rightRefMapIndex = pairRectangles(rightAlMapIndex, true);

        setLineEnds();

        //set the color of both maps' fragments appropriately
        
        int lastLeftIdx = -1, lastRightIdx = -1;
        int currentLeftPosition = -1;
        int currentRightPosition = -1;
        for (int i = leftAlMapIndex; i <= rightAlMapIndex; i++) {
            
            FragAlignment fragAlignment = alignment.getFragAlignmentAtMapIndex(i);
            if (fragAlignment != null) {
                
                if ( isReversed ){                
                    alMap.colorAlignedRegion( alMap.getFragRectangles().size() - i - 1, FragmentColorType.OpticalMap);
                }else{
                    //Color the aligned map
                    alMap.colorAlignedRegion(i, FragmentColorType.OpticalMap);
                }
                
                lastLeftIdx = currentLeftPosition;
                lastRightIdx = currentRightPosition;
                currentLeftPosition = fragAlignment.getLeftAlignment();
                currentRightPosition = fragAlignment.getRightAlignment();
                
                FragmentColorType ct = FragmentColorType.ReferenceMap;
                if ( currentLeftPosition == currentRightPosition ){
                    ct = FragmentColorType.ReferenceMap;                                        
                }else if ( currentLeftPosition < currentRightPosition ){
                    ct = FragmentColorType.MissingCut;
                }else if ( currentLeftPosition == lastLeftIdx || currentLeftPosition == lastRightIdx ||
                           currentRightPosition == lastLeftIdx || currentRightPosition == lastRightIdx)
                    ct = FragmentColorType.ExtraCut;
                
                //color the reference map
                for (int j = currentLeftPosition; j <= currentRightPosition ; j++){
                    refMap.colorAlignedRegion(j, ct);
                }
            }
        }
        
        /*for (int refIndex = leftRefMapIndex; refIndex <= rightRefMapIndex; refIndex++) {
        
            
            //int idx = alignment.getLeftMostFragmentAlignedToThisRefFrag(i, rightAlMapIndex);
            int alMapIndex = alignment.getMapAlignedIndexAtRefIndex(refIndex);            
            if ( alMapIndex >= 0 ){                
                refMap.colorAlignedRegion(refIndex, MapDrawingType.ReferenceMap);
            }
        }*/
    }

    public void paintComponent() {

        //set the coordinates of the lines' ends and draw them
        setLineEnds();

        //Add a correction to the points where the line begins and ends, based on 
        //if the reference map is upper than the aligned map
        double refHeightCorrection;
        double alignHeightCorrection;
        if (alignY < refY) {
            refHeightCorrection = refMap.getHeight();
            alignHeightCorrection = 0;
        } else {
            refHeightCorrection = 0;
            alignHeightCorrection = alMap.getHeight();
        }
        
        double height = MapDrawing.DEFAULT_FRAGMENT_HEIGHT * c_drawer.getCanvasHeight() / MapDrawing.getMaxY();
        double lineWidth = 0.01 * ContigDrawer.ALIGNMENT_LINE_WIDTH * height;
        
        if (visible) {
            c_drawer.drawAlignment(
                    this.alignment.getAlignmentId(),
                    alMap.getName(),
                    refMap.getName(),
                    false,
                    alignLeftX * c_drawer.getCanvasWidth() / MapDrawing.getMaxX(),
                    (alignY + refHeightCorrection) * c_drawer.getCanvasHeight() / MapDrawing.getMaxY(),
                    refLeftX * c_drawer.getCanvasWidth() / MapDrawing.getMaxX(),
                    (refY + alignHeightCorrection) * c_drawer.getCanvasHeight() / MapDrawing.getMaxY(),
                    lineWidth);
            c_drawer.drawAlignment(
                    this.alignment.getAlignmentId(),
                    alMap.getName(),
                    refMap.getName(),
                    false,
                    alignRightX * c_drawer.getCanvasWidth() / MapDrawing.getMaxX(),
                    (alignY + refHeightCorrection) * c_drawer.getCanvasHeight() / MapDrawing.getMaxY(),
                    refRightX * c_drawer.getCanvasWidth() / MapDrawing.getMaxX(),
                    (refY + alignHeightCorrection) * c_drawer.getCanvasHeight() / MapDrawing.getMaxY(),
                    lineWidth);
            for (Line line : lines) {
                c_drawer.drawAlignment(
                        this.alignment.getAlignmentId(),
                        alMap.getName(),
                        refMap.getName(),
                        true,
                        line.getX1() * c_drawer.getCanvasWidth() / MapDrawing.getMaxX(),
                        (line.getY1() + refHeightCorrection) * c_drawer.getCanvasHeight() / MapDrawing.getMaxY(),
                        line.getX2() * c_drawer.getCanvasWidth() / MapDrawing.getMaxX(),
                        (line.getY2() + alignHeightCorrection)*c_drawer.getCanvasHeight() / MapDrawing.getMaxY(),
                        lineWidth);
            }
        }
    }

    public void setFragHeight(int value) {
        this.fragHeight = value;
    }

    public int getFragHeight() {
        return fragHeight;
    }

    public void setLineEnds() {
        
        //make the left and right side lines
        //get the y coordinates for both maps
        alignY = (int) leftAlignRect.getY();
        refY = (int) leftRefRect.getY();

        //set the x coordinates of the left line's ends
        alignLeftX = (isReversed) ? leftAlignRect.getMaxX() : leftAlignRect.getX();
        refLeftX = leftRefRect.getX();

        //set the x coordinates of the right line's ends
        alignRightX = (isReversed) ? rightAlignRect.getX() : rightAlignRect.getMaxX();
        refRightX = rightRefRect.getMaxX();

        //make all the in-between lines
        lines = new ArrayList<>();
        int lastRefIndex = -1;
        int initRefGapIndex = -1, initAlMapIndex = -1;
        double gapSize = 0;
        for (int i = leftAlMapIndex + 1; i <= rightAlMapIndex - 1; i += 1) {
            
            /*makes a new alignment line connecting the specified aligned map fragment
              (or, if that fragment isn't aligned, its closest aligned neighbor) to its
              ref map counterpart*/
            int refIndex = -1;
            int alMapIndex = i;
            while (refIndex < 0) {

                FragAlignment fragAlignment = alignment.getFragAlignmentAtMapIndex(alMapIndex);
                if (fragAlignment == null) {
                    //There is a gap                    
                    gapSize += alMap.getRect(alMapIndex).getWidth();                    
                    
                    if ( alignment.getFragAlignmentAtMapIndex(alMapIndex - 1) != null && lastRefIndex + 1 < refMap.getFragRectangles().size()){
                        initAlMapIndex = alMapIndex;
                        initRefGapIndex = lastRefIndex + 1;
                    }
                    break;
                }else{
                    
                    //feel from LEFT to RIGHT for the first proper alignment
                    lastRefIndex = refIndex = (!isReversed) ? fragAlignment.getLeftAlignment() : fragAlignment.getRightAlignment();
                    if (refIndex < 0) {
                        alMapIndex++;
                    }
                    
                    
                    if ( alignment.getFragAlignmentAtMapIndex(alMapIndex - 1) == null){                        
                        //the Gap closes
                        if ( gapSize > MINIMUM_GAP_SIZE )
                        {   
                            addLine(initAlMapIndex, initRefGapIndex);
                            addLine(alMapIndex, refIndex);
                        } 
                        gapSize = 0;
                    }else if ( (i - leftAlMapIndex) % LINE_FREQUENCY == 0 ){
                        addLine(alMapIndex, refIndex);                        
                    }
                    
                }
            }
        }
        
        int lastAlignedMapIndex = -1;
        gapSize = 0;
        for (int refIndex = leftRefMapIndex; refIndex <= rightRefMapIndex; refIndex++) {
            
            //int idx = alignment.getLeftMostFragmentAlignedToThisRefFrag(i, rightAlMapIndex);
            int alMapIndex = alignment.getMapAlignedIndexAtRefIndex(refIndex);
            
            if ( alMapIndex > 0 ){
                lastAlignedMapIndex = alMapIndex;
                if ( alignment.getMapAlignedIndexAtRefIndex(refIndex - 1) < 0){                    
                    //The gap ends
                    if ( gapSize > MINIMUM_GAP_SIZE ){                    
                        addLine(alMapIndex, refIndex);
                        addLine(initAlMapIndex, initRefGapIndex);
                    }
                    gapSize = 0;
                }
                
            }else{
                //There is a gap
                gapSize += refMap.getRect(refIndex).getWidth();
                
                if ( alignment.getMapAlignedIndexAtRefIndex(refIndex - 1) >= 0 && lastAlignedMapIndex + 1 < alMap.getFragRectangles().size() ){
                    initAlMapIndex = lastAlignedMapIndex + 1;
                    initRefGapIndex = refIndex;                                            
                }
            }
        }
    }
    
    /**
     * 
     * @param alMapIndex
     * @param refIndex 
     */
    private void addLine(int alMapIndex, int refIndex){
        //Correct alMapIndex if the alignment is inverted
        if ( !alignment.isForwardOriented() )
            alMapIndex = alMap.getFragRectangles().size() - alMapIndex - 1;

        FragRectangle alFrag = alMap.getRect(alMapIndex);
        FragRectangle refFrag = refMap.getRect(refIndex);

        Line line = new Line(alFrag, refFrag, isReversed);
        lines.add(line);
    }

    /**
     * @return the x coordinate of the left edge of the aligned region of the
     * reference map. This corresponds to the right side of the aligned map if
     * the alignment is reversed or otherwise to the left side of the aligned
     * map
     */
    public double getLeftSideX() {
        double left = refRightX;
        if (refLeftX < left) {
            return refLeftX;
        }
        return left; //just take whichever is smaller
    }

    public void setVisible(boolean b) {
        visible = b;
    }

    public boolean isVisible() {
        return visible;
    }

    /**
     * designate the pair of rectangles that determine one end of an alignment.
     * MapAlignment objects sometimes specify extra unaligned fragments on the
     * ends, so we move towards the middle of the aligned map until we find the
     * first actual aligned pair of fragments.
     *
     * @param alMapIndex the index on the aligned map where we THINK the
     * alignment starts (if this isn't the one, we look to the next one)
     *
     * @param rightSide true if we're supposed to be finding the rightmost
     * aligned fragment (and its reference map counterpart). false if we're
     * supposed to be finding the leftmost aligned fragment and its ref
     * counterpart
     *
     * @return the index of the reference map fragment that corresponds to the
     * specified aligned fragment
     */
    private int pairRectangles(int alMapIndex, boolean rightSide) {
        int refIndex = -1;
        while (refIndex < 0) {
            FragAlignment fragAlignment = alignment.getFragAlignmentAtMapIndex(alMapIndex);

            if (rightSide) { //feel from RIGHT to LEFT for the first proper alignment
                refIndex = (isReversed) ? fragAlignment.getLeftAlignment() : fragAlignment.getRightAlignment();
                if (refIndex < 0) {
                    alMapIndex--;
                }
            } else { //or feel from LEFT to RIGHT for the first proper alignment
                refIndex = (!isReversed) ? fragAlignment.getLeftAlignment() : fragAlignment.getRightAlignment();
                if (refIndex < 0) {
                    alMapIndex++;
                }
            }
        }
        if (rightSide) {
            rightAlignRect = isReversed ? alMap.getRect( alMap.getFragRectangles().size() - alMapIndex - 1) : alMap.getRect(alMapIndex);
            rightRefRect = refMap.getRect(refIndex);
        } else {
            leftAlignRect =  isReversed ? alMap.getRect( alMap.getFragRectangles().size() - alMapIndex - 1) : alMap.getRect(alMapIndex);
            leftRefRect = refMap.getRect(refIndex);
        }
        return refIndex;
    }
}
