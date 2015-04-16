/*
 * StandardMapAlignment.java
 *
 * Created on November 12, 2004, 3:18 PM
 */
package edu.wisc.lmcg.alignment.mapalignment;

import java.util.*;

import edu.wisc.lmcg.map.RestrictionMap;
import edu.wisc.lmcg.alignment.FragAlignment;

/**
 * This represents an alignment of an aligned map to a reference map. Within
 * this object the score of the alignment along with orientation and fragment
 * alignment information is stored.
 *
 * @author churas
 */
public class StandardMapAlignment implements MapAlignment {

    private String mp_RefMapName;
    private String mp_AlignedMapName;
    private boolean mp_IsForwardOriented;
    private boolean mp_IsRefMapForwardOriented;
    //private InvertibleHashMap<Integer, FragAlignment> mp_AlignmentTable;
    private final TreeMap<Integer, FragAlignment> mp_AlignmentTable;
    private final TreeMap<Integer, Integer> mp_ReferenceAlignmentTable;
    private double mp_Score;
    private double mp_Pvalue;
    private String alignmentId;
    private static int nextId = 0;

    /**
     * Creates a new instance of StandardMapAlignment
     */
    public StandardMapAlignment() {
        mp_RefMapName = null;
        mp_AlignedMapName = null;
        mp_IsForwardOriented = true;
        //mp_AlignmentTable = new InvertibleHashMap<>(mp_IsForwardOriented);
        mp_AlignmentTable = new TreeMap<>();
        mp_ReferenceAlignmentTable = new TreeMap<>();
        mp_Score = -1000000;
        mp_Pvalue = -1000000;
        mp_IsRefMapForwardOriented = true;
        alignmentId = "alignment" + (++nextId);
    }

    /**
     * Removes a frag alignment at specified index.
     *
     * @return true if the object was removed or false if not
     */
    @Override
    public boolean removeFragAlignmentAtIndex(int val) {        
        if (mp_AlignmentTable == null) {
            return false;
        }
        Integer key = val;
        if (mp_AlignmentTable.containsKey(key)) {
            if (mp_AlignmentTable.remove(key) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setScore(double val) {
        mp_Score = val;
    }

    @Override
    public double getScore() {
        return mp_Score;

    }

    @Override
    public void setPvalue(double val) {
        mp_Pvalue = val;
    }

    @Override
    public double getPvalue() {
        return mp_Pvalue;
    }

    /**
     * Sets the name of the Reference map is aligned to.
     *
     * @param name - String containing name of Reference Map.
     */
    @Override
    public void setReferenceMapName(String name) {
        mp_RefMapName = name;
    }

    /**
     * Appends value of name to ReferenceMapName.
     *
     * @param name - String containing name to append to ReferenceMapName.
     */
    public void addReferenceMapName(String name) {
        if (name != null) {
            if (mp_RefMapName == null) {
                mp_RefMapName = name;
            } else {
                mp_RefMapName += name;
            }
        }
    }

    /**
     * sets score of alignment.
     *
     * @param score - String containing score to append to score.
     */
    @Override
    public void setScore(final String score) {
        if (score == null) {
            return;
        }

        Double temp = new Double(score);

        mp_Score = temp;
    }

    @Override
    public void setPvalue(final String pval) {
        if (pval == null) {
            return;
        }
        Double temp = new Double(pval);
        mp_Pvalue = temp;

    }

    /**
     * Gets name of Reference Map.
     *
     * @return String containing name of reference map.
     */
    @Override
    public String getReferenceMapName() {
        return mp_RefMapName;
    }

    /**
     * Sets name of Aligned Map.
     *
     * @param name - String containing name of aligned map.
     */
    @Override
    public void setAlignedMapName(final String name) {
        mp_AlignedMapName = name;
    }

    /**
     * Appends name param passed in to aligned map name.
     *
     * @param name - String to append to aligned map name.
     */
    public void addAlignedMapName(final String name) {
        if (name != null) {
            if (mp_AlignedMapName == null) {
                mp_AlignedMapName = name;
            } else {
                mp_AlignedMapName += name;
            }
        }
    }

    /**
     * Gets aligned map name.
     *
     * @return String containing aligned map name.
     */
    @Override
    public String getAlignedMapName() {
        return mp_AlignedMapName;
    }

    /**
     * Sets orientation of aligned map to either N = normal or R = reversed.
     *
     * @param orientation - String containing N or R.
     */
    @Override
    public void setOrientation(final String orientation) {
        if (orientation != null) {
            mp_IsForwardOriented = orientation.equals("N");
        }
    }

    /**
     * Gets whether aligned map is forward oriented or if N was set in
     * setOrientation method.
     *
     * @return boolean where true means aligned map is forward orientated.
     */
    @Override
    public boolean isForwardOriented() {
        return mp_IsForwardOriented;
    }

    /**
     * Sets whether reference map is forward oriented or not
     *
     * @param val - boolean set to true if reference map is forward oriented
     */
    @Override
    public void isRefMapForwardOriented(final boolean val) {
        mp_IsRefMapForwardOriented = val;
    }

    /**
     * Gets whether the reference map is forward oriented.
     *
     * @return boolean true means the reference map is forward oriented false
     * means no.
     */
    @Override
    public boolean isRefMapForwardOriented() {
        return mp_IsRefMapForwardOriented;
    }

    /**
     * Adds new alignment to MapAlignment.
     *
     * @param index - String containing int representing index of aligned map
     * this alignment corresponds to.
     * @param left - String containing int representing left index of fragment
     * in Reference map this Fragment aligns to.
     * @param right - String containing int representing right index of fragment
     * in Reference map this Fragment aligns to.
     */
    @Override
    public void addAlignment(final String index, final String left, final String right) {
        if (index == null || left == null || right == null) {
            return;
        }

        mp_AlignmentTable.put(new Integer(index), new FragAlignment(left, right));
        mp_ReferenceAlignmentTable.put(new Integer(left), new Integer(index));
    }

    /**
     * Adds new alignment to MapAlignment.
     *
     * @param index - containing int representing index of aligned map this
     * alignment corresponds to.
     * @param left - containing int representing left index of fragment in
     * Reference map this Fragment aligns to.
     * @param right - containing int representing right index of fragment in
     * Reference map this Fragment aligns to.
     */
    @Override
    public void addAlignment(final int index, final int left, final int right) {
        mp_AlignmentTable.put(index, new FragAlignment(left, right));
        mp_ReferenceAlignmentTable.put(left, index);
    }

    /**
     * Gets FragAlignment for given aligned map fragment index.
     *
     * @param i - int representing fragment index of aligned map.
     * @return FragAlignment - containing alignment at specified index.
     */
    @Override
    public FragAlignment getFragAlignmentAtMapIndex(final int i) {
        return (FragAlignment) mp_AlignmentTable.get(i);
    }

    /**
     * Gets the index of last frag alignment in relation to fragment index of
     * the aligned map. Please note this method finds the highest fragment index
     * and does not find lowest if the aligned map is reversed.
     *
     * @return int representing index of last FragAlignment.
     */
    @Override
    public int getIndexOfLastFragAlignment() {
        
        int maxint = -1;

        for ( Integer curint : mp_AlignmentTable.keySet() ){
            
            if (curint != null) {
                if (curint > maxint) {
                    maxint = curint;
                }
            }
        }

        return maxint;
    }

    /**
     * Gets last FragAlignment for MapAlignment. Same as
     * getIndexOfLastFragAlignment but this method actually returns the
     * FragAlignment instead of just an index.
     *
     * @return FragAlignment of last aligned Fragment.
     */
    @Override
    public FragAlignment getLastFragAlignment() {
        Integer curint = null;        

        int maxint = getIndexOfLastFragAlignment();

        if (maxint > -1) {
            return (FragAlignment) mp_AlignmentTable.get(maxint);
        }

        return null;
    }

    @Override
    public int getIndexOfFirstFragAlignment() {
        
        int minint = -1;

        for ( Integer curint : mp_AlignmentTable.keySet() ){
        
            if (curint != null) {
                if (minint == -1) {
                    minint = curint;
                } else if (curint < minint) {
                    minint = curint;
                }
            }
        }

        return minint;
    }

    /**
     * Gets first FragAlignment for MapAlignment.
     *
     * @return FragAlignment of first aligned Fragment.
     */
    @Override
    public FragAlignment getFirstFragAlignment() {
        
        int minint = -1;

        for ( Integer curint : mp_AlignmentTable.keySet() ){        
            if (curint != null) {
                if (minint == -1) {
                    minint = curint;
                } else if (curint < minint) {
                    minint = curint;
                }
            }
        }

        if (minint > -1) {
            return (FragAlignment) mp_AlignmentTable.get(new Integer(minint));
        }

        return null;
    }

    /**
     * This method simply counts the number of fragment alignments. It could
     * also be described as the number of aligned fragments.
     *
     * @return number of aligned fragments.
     */
    @Override
    public int getNumberOfAlignedFragments() {
        return mp_AlignmentTable.size();
    }

    /**
     * This method realigns the aligned map to the aligned map in the
     * MapAlignment passed in. In a more detailed description this method takes
     * the MapAlignment passed in and creates a new map alignment with the
     * reference map of the new map alignment set to the aligned map of the
     * MapAlignment passed in. The method then walks across the aligned map in
     * this map alignment and translates the alignments to reference the aligned
     * map in the MapAlignment passed in. This information is stored in the new
     * MapAlignment that is returned to the caller. The method does its best
     * translate the alignments, but if there are a lot of missed and false cuts
     * its possible the decisions may not be optimal. It is assumed that the
     * reference maps in both map alignments are identical.
     *
     * @param newalign containing a map aligned to the same reference map as
     * this map alignment.
     * @return MapAlignment representing
     */
    @Override
    public MapAlignment realignMapAlignmentToNewAlignment(MapAlignment newalign)
            throws Exception {
        if (newalign == null) {
            throw new NullPointerException("newalign MapAlignment is null");
        }

        FragAlignment oldfrag;
        FragAlignment newfrag;

        StandardMapAlignment realignedMa = new StandardMapAlignment();

        int leftalign;
        int rightalign;

        int refleft;
        int refright;

        int newleft;
        int newright;
        StandardMapAlignment newstdalign = (StandardMapAlignment) newalign;
        realignedMa.mp_RefMapName = newstdalign.getAlignedMapName();
        realignedMa.mp_AlignedMapName = mp_AlignedMapName;
        realignedMa.mp_IsForwardOriented = mp_IsForwardOriented;
        realignedMa.mp_Score = mp_Score;
        realignedMa.mp_Pvalue = mp_Pvalue;

        realignedMa.mp_IsRefMapForwardOriented = newstdalign.isForwardOriented();

        for ( Integer curint : mp_AlignmentTable.keySet() ){        

            oldfrag = (FragAlignment) mp_AlignmentTable.get(curint);

            leftalign = oldfrag.getLeftAlignment();
            rightalign = oldfrag.getRightAlignment();

            newleft = -1000;

            newright = -1000;

            for ( Integer newalign_index : mp_AlignmentTable.keySet() ){            

                newfrag = (FragAlignment) newstdalign.mp_AlignmentTable.get(newalign_index);

                refleft = newfrag.getLeftAlignment();
                refright = newfrag.getRightAlignment();

                //System.out.println("newalign_index: "+newalign_index+" curint: "+curint+" refleft: "+refleft+" leftalign: "+
                //	   leftalign+" refright: "+
                //	   refright+" rightalign: "+rightalign);
                if (((leftalign == refleft
                        || leftalign == refright) && leftalign != -1)
                        || ((rightalign == refleft
                        || rightalign == refright) && rightalign != -1)) {

                    if (newleft == -1000) {
                        newleft = newalign_index;
                    }

                    if (newright == -1000) {
                        newright = newalign_index;
                    }
                    if (newalign_index < newleft) {
                        newleft = newalign_index;
                    }

                    if (newalign_index > newright) {
                        newright = newalign_index;
                    }
                }
                //		else if (leftalign > refleft && rightalign < refright){
                //   System.out.println("In this area");
                // newleft = newalign_index.intValue();
                //newright = newalign_index.intValue();

                //}
            }
            if (newleft != -1000 && newright != -1000) {
                //System.out.println("Adding index: "+curint+" left: "+newleft+" right: "+newright);

                realignedMa.addAlignment(curint.toString(), Integer.toString(newleft), Integer.toString(newright));
            }
        }

        return realignedMa;
    }

    /**
     * This method kicks out a string of xml representing the fragment
     * alignments.
     *
     * @return String containing xml fragment alignments.
     */
    @Override
    public String getXmlFragAlignments()
            throws Exception {

        FragAlignment frag;
        String summary = "";
        List<Integer> myvec = new ArrayList<>();

        for ( Integer indexint : mp_AlignmentTable.keySet() ){        
            myvec.add(indexint);
        }
        Collections.sort(myvec);

        for (Integer indexint : myvec) {

            frag = (FragAlignment) mp_AlignmentTable.get(indexint);

            summary += "\t\t<f><i>" + indexint.toString() + "</i><l>"
                    + Integer.toString(frag.getLeftAlignment()) + "</l><r>"
                    + Integer.toString(frag.getRightAlignment()) + "</r></f>\n";
        }

        return summary;
    }

    /**
     * This method calculates the number of false cuts in an aligned map by
     * counting the number of instances where a fragment alignment points to two
     * reference fragments.
     *
     * @return count of the number of false cuts.
     */
    @Override
    public int getNumberOfFalseCuts()
            throws Exception {
        
        int numfalsecuts = 0;

        for ( Integer curKey : mp_AlignmentTable.keySet() ){        
            
            if (isCutLeftOfFragIndexFalse(curKey) == true) {
                numfalsecuts++;
            }
        }
        return numfalsecuts;
    }

    /**
     * Checks if cut to left of fragment index is false. The algorithm is simply
     * to take the fragalignment at index passed in and look if the left
     * alignment of this fragment matches the right alignment of the previous
     * fragment. if so then the cut to the left is a false cut.
     *
     * @param fragindex - int representing fragment index of aligned map.
     * @return boolean - true if cut is a false cut.
     */
    @Override
    public boolean isCutLeftOfFragIndexFalse(int fragindex)
            throws Exception {
        if (fragindex < 0) {

            throw new IndexOutOfBoundsException("Negative fragment index passed in to method");
        }

        //first fragment of map cannot have a false cut to the left
        if (fragindex == 0) {
            return false;
        }

        FragAlignment curalignment;

        Integer curindex = fragindex;

        curalignment = (FragAlignment) mp_AlignmentTable.get(curindex);

        if (curalignment == null) {
            return false;
            //    throw new NullPointerException("alignment at index: "+fragindex+" was null");
        }

        int leftalign = curalignment.getLeftAlignment();

        //if the fragment doesnt align to anything ie -1 just return
        //false
        if (leftalign == -1) {
            return false;
        }

        FragAlignment aligntoleft = null;

        int checkindex = fragindex - 1;

        while (aligntoleft == null && checkindex >= 0) {
            Integer indexint = checkindex;
            aligntoleft = (FragAlignment) mp_AlignmentTable.get(indexint);
            checkindex--;
        }

        //there were no more alignments to left of this one so there
        //cannot be any false cuts
        if (checkindex < 0) {
            return false;
        }

        return aligntoleft.getRightAlignment() == leftalign
                || aligntoleft.getLeftAlignment() == leftalign;
    }

    /**
     * Get total number of missing cuts by summing the number reference
     */
    @Override
    public int getNumberOfMissingCuts()
            throws Exception, IndexOutOfBoundsException {
        
        int leftalign;
        int rightalign;
        int numMissCuts = 0;

        for ( FragAlignment curfrag : mp_AlignmentTable.values() ){
        

            if (curfrag == null) {
                continue;
            }

            leftalign = curfrag.getLeftAlignment();
            rightalign = curfrag.getRightAlignment();

            if (leftalign < -1) {
                throw new IndexOutOfBoundsException("left alignment is less then -1");
            }

            if (rightalign < -1) {
                throw new IndexOutOfBoundsException("right alignment is less then -1");
            }

            //if the left alignment or right alignment is -1 we cant say anything
            //cause its the first or last fragment
            if (leftalign != -1 && rightalign != -1) {
                //if the left and right alignment point to the same fragment then
                //return 0
                numMissCuts += (rightalign - leftalign);
            }
        }
        return numMissCuts;
    }

    /**
     * Gets count of missing counts for aligned fragment. This method calculates
     * count of missing fragments by subtracting the right alignment from the
     * left alignment for the fragment <B>fragindex</B> being examined.
     *
     * @param fragindex - index of aligned frag to check.
     * @return int - number of missing cuts for aligned fragment.
     */
    @Override
    public int getNumberOfMissingCutsAtFragIndex(int fragindex)
            throws Exception {
        if (fragindex < 0) {

            throw new IndexOutOfBoundsException("Negative fragment index passed in to method");
        }

        Integer indexint = fragindex;

        FragAlignment curfrag = (FragAlignment) mp_AlignmentTable.get(indexint);

        if (curfrag == null) {
            return -1;
            //	    throw new NullPointerException("fragalignment at index: "+fragindex+" is null");
        }

        int leftalign = curfrag.getLeftAlignment();
        int rightalign = curfrag.getRightAlignment();

        if (leftalign < -1) {
            throw new IndexOutOfBoundsException("left alignment is less then -1");
        }

        if (rightalign < -1) {
            throw new IndexOutOfBoundsException("right alignment is less then -1");
        }

        //if the left alignment or right alignment is -1 we cant say anything
        //cause its the first or last fragment
        if (leftalign == -1 || rightalign == -1) {
            return 0;
        }

        //if the left and right alignment point to the same fragment then
        //return 0
        return (rightalign - leftalign);
    }

    /**
     * This method gets mass of Aligned chunk at Aligned Frag index.
     *
     * @return mass in base pairs of aligned chunk.
     */
    @Override
    public int getMassOfAlignedChunkAtFragIndex(int fragindex, RestrictionMap resmap)
            throws Exception {
        if (fragindex < 0) {

            throw new IndexOutOfBoundsException("Negative fragment index passed in to method");
        }

        if (resmap == null) {
            throw new NullPointerException("resmap RestrictionMap is null");
        }

        if (fragindex >= resmap.getNumberFragments()) {
            throw new IndexOutOfBoundsException("Fragment index passed in to method exceeds the number of aligned fragments");
        }

        int indexcount = fragindex;

        int leftalign;
        int rightalign;

        int leftmostalign = -1;
        int rightmostalign = -1;
        FragAlignment fa = null;

        while (indexcount >= 0) {
            Integer curindex = indexcount;
            fa = (FragAlignment) mp_AlignmentTable.get(curindex);

            if (fa == null) {
                return 0;
            }

            leftalign = fa.getLeftAlignment();

            if (leftalign == -1) {
                leftmostalign = indexcount;
                break;
            } else {
                leftmostalign = getLeftMostFragmentAlignedToThisRefFrag(leftalign, indexcount);
            }

            if (leftmostalign == indexcount
                    && indexcount == curindex) {
                break;
            }

            indexcount = leftmostalign;
        }

        indexcount = fragindex;

        while (indexcount < mp_AlignmentTable.size()) {
            Integer curindex = indexcount;
            fa = (FragAlignment) mp_AlignmentTable.get(curindex);

            if (fa == null) {
                return 0;
            }

            rightalign = fa.getRightAlignment();
            if (rightalign == -1) {
                rightmostalign = indexcount;
                break;
            }

            rightmostalign = getRightMostFragmentAlignedToThisRefFrag(rightalign, indexcount);

            if (rightmostalign == indexcount
                    && indexcount == curindex) {
                break;
            }

            indexcount = rightmostalign;
        }

        //okay we now have the left most aligned fragment and the right most
        //aligned fragment so we need to the mass between them
        if (rightmostalign == -1 && leftmostalign == -1) {
            return -1;
        }

        if (leftmostalign == -1 && rightmostalign != -1) {
            rightmostalign = leftmostalign;
        }

        if (rightmostalign == -1 && leftmostalign != -1) {
            rightmostalign = leftmostalign;
        }

        if (mp_IsForwardOriented == false) {
            return resmap.getMassInBpBetweenFragmentIndexes(resmap.getNumberFragments() - rightmostalign - 1,
                    resmap.getNumberFragments() - leftmostalign - 1);
        } else {
            return resmap.getMassInBpBetweenFragmentIndexes(leftmostalign, rightmostalign);
        }
    }

    /**
     * Gets the index of the left most fragment aligned to the specified.
     * reference fragment.
     *
     * @param fragindex - reference fragment index to find left most fragment
     * aligned to.
     * @param curalignfragindex the maximum aligned fragment index to look to.
     * if you want to look at all fragments set to length size of map.
     * @return int representing index of left most aligned fragment aligned to
     * the reference fragment or -1 if none exist.
     * @throws IndexOutOfBoundsException when indexes passed in are invalid.
     */
    @Override
    public int getLeftMostFragmentAlignedToThisRefFrag(int fragindex, int curalignfragindex)
            throws IndexOutOfBoundsException {
        if (fragindex < 0) {
            throw new IndexOutOfBoundsException("Negative reference map index");
        }

        if (curalignfragindex < 0) {

            throw new IndexOutOfBoundsException("Negative fragment index passed in to method");
        }

        FragAlignment fa;
        int minIndex = -1;

        for ( Integer kVal : mp_AlignmentTable.keySet() ){        

            fa = (FragAlignment) mp_AlignmentTable.get(kVal);

            if (fa == null) {
                continue;
            }

            if (fa.getLeftAlignment() == fragindex
                    || fa.getRightAlignment() == fragindex) {
                if (minIndex == -1) {
                    minIndex = kVal;
                } else if (kVal < minIndex) {
                    minIndex = kVal;
                }
            }
        }

        return minIndex;
    }

    /**
     * Get right most fragment index aligned to the reference fragment specified
     *
     * @param fragindex - reference fragment index to find right most fragment
     * aligned to.
     * @param curalignfragindex the maximum aligned fragment index to look to.
     * if you want to look at all fragments set to 0.
     * @return index of right most fragment aligned to this reference fragment.
     * or -1 if none exist
     * @throws IndexOutOfBoundsException when indexes are invalid.
     */
    @Override
    public int getRightMostFragmentAlignedToThisRefFrag(int fragindex, int curalignfragindex)
            throws IndexOutOfBoundsException {
        if (fragindex < 0) {
            throw new IndexOutOfBoundsException("Negative reference map index");
        }

        if (curalignfragindex < 0) {

            throw new IndexOutOfBoundsException("Negative fragment index passed in to method");
        }

        if (curalignfragindex >= mp_AlignmentTable.size()) {
            throw new IndexOutOfBoundsException("Fragment index passed in to method exceeds the number of aligned fragments");
        }

        FragAlignment fa;

        int maxIndex = -1;
        for ( Integer kVal : mp_AlignmentTable.keySet() ){        

            fa = (FragAlignment) mp_AlignmentTable.get(kVal);

            if (fa == null) {
                continue;
            }

            if (fa.getLeftAlignment() == fragindex
                    || fa.getRightAlignment() == fragindex) {
                if (kVal > maxIndex) {
                    maxIndex = kVal;
                }
            }

        }

        return maxIndex;

    }

    @Override
    public int getMassOfRefChunkAtFragIndex(int fragindex, RestrictionMap resmap)
            throws Exception {

        if (fragindex < 0) {
            throw new IndexOutOfBoundsException("Negative fragment index passed in to method");
        }

        if (resmap == null) {
            throw new NullPointerException("resmap RestrictionMap is null");
        }

        int indexcount = fragindex;

        int leftalign;
        int rightalign;

        int leftmostalign = -1;
        int rightmostalign = -1;
        FragAlignment fa;
        FragAlignment leftfa = null;
        FragAlignment rightfa = null;

        while (indexcount >= 0) {

            Integer curindex = indexcount;
            fa = (FragAlignment) mp_AlignmentTable.get(curindex);

            if (fa == null) {
                return 0;
            }

            leftfa = fa;

            leftalign = fa.getLeftAlignment();

            if (leftalign == -1) {
                leftmostalign = indexcount;
                break;
            } else {
                leftmostalign = getLeftMostFragmentAlignedToThisRefFrag(leftalign, indexcount);
            }

            if (leftmostalign == indexcount && indexcount == curindex) {
                break;
            }

            indexcount = leftmostalign;
        }

        indexcount = fragindex;

        while (indexcount < mp_AlignmentTable.size()) {
            Integer curindex = indexcount;
            fa = (FragAlignment) mp_AlignmentTable.get(curindex);

            if (fa == null) {
                return 0;
            }

            rightfa = fa;

            rightalign = fa.getRightAlignment();
            if (rightalign == -1) {
                rightmostalign = indexcount;
                break;
            }

            rightmostalign = getRightMostFragmentAlignedToThisRefFrag(rightalign, indexcount);

            if (rightmostalign == indexcount
                    && indexcount == curindex) {
                break;
            }
            indexcount = rightmostalign;
        }

        //okay we now have the left most aligned fragment and the right most
        //aligned fragment so we need to the mass between them
        if (rightmostalign == -1 && leftmostalign == -1) {
            return -1;
        }

        if (leftmostalign == -1 && rightmostalign != -1) {
            leftfa = rightfa;
        }

        if (rightmostalign == -1 && leftmostalign != -1) {
            rightfa = leftfa;
        }

        if ((leftfa.getLeftAlignment() > -1 && leftfa.getLeftAlignment()
                < leftfa.getRightAlignment())) {
            leftmostalign = leftfa.getLeftAlignment();
        } else {
            leftmostalign = leftfa.getRightAlignment();
        }

        if ((rightfa.getRightAlignment() > -1 && leftfa.getRightAlignment()
                > rightfa.getLeftAlignment())) {

            rightmostalign = rightfa.getRightAlignment();
        } else {
            rightmostalign = rightfa.getLeftAlignment();
        }

        return resmap.getMassInBpBetweenFragmentIndexes(leftmostalign, rightmostalign);
    }

    @Override
    public boolean isMapAlignedToRefMapWithinThisWindow(int startindex, int endindex) {

        for ( FragAlignment fa : mp_AlignmentTable.values() ){        

            if (fa.getLeftAlignment() >= startindex
                    && fa.getLeftAlignment() <= endindex) {
                return true;
            } else if (fa.getRightAlignment() >= startindex
                    && fa.getRightAlignment() <= endindex) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getMapAlignmentAsXml() {
        /**
         * <map_alignment>
         * <uuid>c7b6eee4-7680-4b96-8660-628863b29a38</uuid>
         * <reference_map>
         * <name>omdb:36809891:gi|37539904|ref|NT_021937.16|Hs1_22093</name>
         * </reference_map>
         * <aligned_map>
         * <name>chr1_0-10MB:contig343_81</name>
         * <orientation>N</orientation>
         * </aligned_map>
         * <soma_score>99.4870758056641</soma_score>
         * <soma_pvalue>0.01</soma_pvalue>
         * <count>150</count>
         * <f><i>1</i><l>5</l><r>5</r></f>
         * </map_alignment>
         */

        String mapStr = "\t<map_alignment>\n\t\t<uuid></uuid>\n\t\t<reference_map>\n\t\t\t<name>" + mp_RefMapName + "</name>\n\t\t</reference_map>\n\t\t<aligned_map>\n\t\t\t<name>" + mp_AlignedMapName + "</name>\n\t\t\t";

        if (mp_IsForwardOriented == true) {
            mapStr += "<orientation>N</orientation>\n";
        } else {
            mapStr += "<orientation>R</orientation>\n";
        }

        mapStr += "\t\t</aligned_map>\n";

        //if soma score is not -100000 then print it out
        if (mp_Score != -1000000) {
            mapStr += "\t\t<soma_score>" + mp_Score + "</soma_score>\n";
        }

        //if soma p value is not -1000000 then print it out
        if (mp_Pvalue != -1000000) {
            mapStr += "\t\t<soma_pvalue>" + mp_Pvalue + "</soma_pvalue>\n";
        }

        mapStr += "\t\t<count>" + mp_AlignmentTable.size() + "</count>\n";

        try {
            mapStr += this.getXmlFragAlignments();
        } catch (Exception ex) {

        }
        mapStr += "\t</map_alignment>\n";

        return mapStr;
    }

    /**
     * This method shifts all frag alignments by value specified. any -1 values
     * will be set to the other (left or right) frag alignment
     *
     */
    @Override
    public boolean shiftFragAlignments(int shiftval) {
        
        for ( FragAlignment fa : mp_AlignmentTable.values() ){        

            if (fa.shiftAlignment(shiftval) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method inverts the alignments of this map alignment by first setting
     * the reference map as the aligned map and the aligned map as the reference
     * map. The method then flips every FragAlignment. Calling this method twice
     * should result in no change in the alignment.
     *
     * @return Inverted MapAlignment or null if operation could not be performed
     */
    @Override
    public MapAlignment invert(int numFragsInRefMap, int numFragsInAmap) throws Exception {
        if (mp_AlignedMapName == null) {
            throw new NullPointerException("Aligned Map Name must not be null when calling invert");
        }

        if (mp_RefMapName == null) {
            throw new NullPointerException("Reference Map Name must not be null when calling invert");
        }

        StandardMapAlignment ma = new StandardMapAlignment();

        ma.mp_RefMapName = this.mp_AlignedMapName;
        ma.mp_AlignedMapName = this.mp_RefMapName;
        ma.mp_Pvalue = this.mp_Pvalue;
        ma.mp_Score = this.mp_Score;

        ma.mp_IsRefMapForwardOriented = this.mp_IsForwardOriented;
        ma.mp_IsForwardOriented = this.mp_IsRefMapForwardOriented;

        int conStartIndex = this.getMinimumAlignedConsensusFragmentIndex();
        int conEndIndex = this.getMaximumAlignedConsensusFragmentIndex();

        int lastFragIndex = getIndexOfLastFragAlignment();

        int left = -1;
        int right = -1;
        int leftx = -1;
        int rightx = -1;
        int x = 0;
        int newI = -1;

        for (int i = conStartIndex; i <= conEndIndex; i++) {
            right = getRightMostFragmentAlignedToThisRefFrag(i, 0);
            left = getLeftMostFragmentAlignedToThisRefFrag(i, lastFragIndex);

            newI = i;
            //System.out.println("i is: "+i+" newI is: "+newI);
            if (left != -1 && right == -1) { //case 1

                //System.out.println("in case 1 left not null: "+left+" and right "+right);
                ma.addAlignment(newI, left, left);
            } else if (left == -1 && right != -1) {//still case 1

                //System.out.println("in case 1 left: "+left+" and right not null "+right);
                ma.addAlignment(newI, right, right);
            } else if (left != -1 && right != -1) {//case 2

                //System.out.println("in case 2  i: "+i+" newI: "+newI+" left: "+left+" and right "+right);
                if (left < right) {
                    ma.addAlignment(newI, left, right);
                } else {
                    ma.addAlignment(newI, right, left);
                }
            } else if (left == -1 && right == -1) { //case 3
                x = i;
                leftx = -1;
                rightx = -1;

                while (x >= 0) {
                    leftx = getLeftMostFragmentAlignedToThisRefFrag(x, lastFragIndex);
                    if (leftx != -1) {
                        break;
                    }
                    x--;
                }

                x = i;

                while (x <= conEndIndex) {
                    rightx = getRightMostFragmentAlignedToThisRefFrag(x, 0);
                    if (rightx != -1) {
                        break;
                    }
                    x++;
                }

                //System.out.println("case 3: left: "+leftx+" right: "+rightx);
                if (leftx == rightx) {
                    ma.addAlignment(newI, leftx, leftx);
                }
            }
        }

        if (this.mp_IsForwardOriented == false) {
            return ma.reverseAlignment(numFragsInAmap, numFragsInRefMap);
        }

        return ma;
    }

    /**
     * Finds the Minimum Reference index that any FragAlignment aligns to.
     */
    private int getMinimumAlignedConsensusFragmentIndex() {
        if (mp_AlignmentTable == null || mp_AlignmentTable.isEmpty() == true) {
            return -1;
        }
        
        int minVal = -1;

        for ( FragAlignment fa : mp_AlignmentTable.values() ){        

            if (fa.getLeftAlignment() > -1) {
                if (minVal == -1) {
                    minVal = fa.getLeftAlignment();
                } else if (minVal > fa.getLeftAlignment()) {
                    minVal = fa.getLeftAlignment();
                }
            }
        }
        return minVal;
    }

    /**
     * Finds the maximum reference index that any fragalignment maps to.
     */
    private int getMaximumAlignedConsensusFragmentIndex() {
        if (mp_AlignmentTable == null || mp_AlignmentTable.isEmpty() == true) {
            return -1;
        }
        
        int maxVal = -1;

        for ( FragAlignment fa : mp_AlignmentTable.values() ){        

            if (fa.getRightAlignment() > -1) {
                if (maxVal == -1) {
                    maxVal = fa.getRightAlignment();
                } else if (maxVal < fa.getRightAlignment()) {
                    maxVal = fa.getRightAlignment();
                }
            }
        }
        return maxVal;
    }

    /**
     * This method creates a new MapAlignment with the FragAlignments shifted as
     * defined in the hashtable passed in. This is useful if a map is changed
     * and a map alignment needs to be modified to accomodate the change.
     *
     * @param shiftHash - Hashtable key is an Integer representing old reference
     * index and value is an Integer representing the new reference index.
     * @return MapAlignment upon success or null upon error.
     */
    @Override
    public MapAlignment shiftFragAlignments(Hashtable shiftHash) {
        if (shiftHash == null) {
            throw new NullPointerException("shiftHash in shiftFragAlignment is null");
        }

        if (shiftHash.isEmpty() == true) {
            throw new NullPointerException("shift hash is empty in shiftFragAlignment");
        }

        if (mp_RefMapName == null) {
            throw new NullPointerException("reference map name is null in shiftFragAlignment");
        }

        if (mp_AlignedMapName == null) {
            throw new NullPointerException("Aligned Map name is null in shiftFragAlignment");
        }

        StandardMapAlignment newMa = new StandardMapAlignment();

        newMa.mp_RefMapName = this.mp_RefMapName;
        newMa.mp_AlignedMapName = this.mp_AlignedMapName;
        newMa.mp_IsForwardOriented = this.mp_IsForwardOriented;
        newMa.mp_IsRefMapForwardOriented = this.mp_IsRefMapForwardOriented;
        newMa.mp_Score = this.mp_Score;
        newMa.mp_Pvalue = this.mp_Pvalue;

        FragAlignment fa;

        Integer newLeftIndex;
        Integer newRightIndex;

        for ( Integer kVal : mp_AlignmentTable.keySet() ){
        
            if (kVal == null) {
                continue;
            }

            fa = (FragAlignment) mp_AlignmentTable.get(kVal);

            if (fa.getLeftAlignment() == -1) {
                newLeftIndex = -1;
            } else {
                newLeftIndex = (Integer) shiftHash.get(fa.getLeftAlignment());
            }

            if (fa.getRightAlignment() == -1) {
                newRightIndex = -1;
            } else {
                newRightIndex = (Integer) shiftHash.get(fa.getRightAlignment());
            }

            if (newLeftIndex == null || newRightIndex == null) {
                continue;
            }

            newMa.addAlignment(kVal, newLeftIndex, newRightIndex);
        }

        return newMa;
    }

    @Override
    public MapAlignment reverseAlignment(int numFragsInRefMap, int numFragsInAlignedMap) throws Exception {

        StandardMapAlignment newMa = new StandardMapAlignment();

        newMa.mp_IsForwardOriented = !this.mp_IsForwardOriented;
        newMa.mp_IsRefMapForwardOriented = !this.mp_IsRefMapForwardOriented;
        newMa.mp_Pvalue = this.mp_Pvalue;
        newMa.mp_Score = this.mp_Score;
        newMa.mp_AlignedMapName = mp_AlignedMapName;
        newMa.mp_RefMapName = mp_RefMapName;

        FragAlignment fragAlign = null;

        for ( Integer index : mp_AlignmentTable.keySet() ){        
        
            if (index == null) {
                continue;
            }

            fragAlign = (FragAlignment) mp_AlignmentTable.get(index);

            newMa.addAlignment(numFragsInAlignedMap - index - 1,
                    numFragsInRefMap - fragAlign.getRightAlignment() - 1,
                    numFragsInRefMap - fragAlign.getLeftAlignment() - 1);
        }
        return newMa;
    }

    @Override
    public Map getFragAlignments() {
        return this.mp_AlignmentTable;
    }

    @Override
    public boolean shiftFragAlignmentIndexes(int shiftval) {
        FragAlignment fa;        
        Map<Integer, FragAlignment> tmpTable = new TreeMap<>();
        tmpTable.putAll(mp_AlignmentTable);

        mp_AlignmentTable.clear();
        int newKey;
        for (Integer curKey : tmpTable.keySet()) {            

            fa = (FragAlignment) tmpTable.get(curKey);
            newKey = curKey + shiftval;
            mp_AlignmentTable.put(newKey, fa);
        }
        return true;
    }

    @Override
    public boolean isEqual(MapAlignment ma) {
        if (ma == null) {
            return false;
        }
        //check aligned map name
        if ((this.mp_AlignedMapName == null && ma.getAlignedMapName() != null)
                || (this.mp_AlignedMapName != null && ma.getAlignedMapName() == null)) {
            return false;
        }

        if (this.mp_AlignedMapName != null && ma.getAlignedMapName() != null) {
            if (!this.mp_AlignedMapName.equals(ma.getAlignedMapName())) {
                return false;
            }
        }

        //check ref map name
        if ((this.mp_RefMapName == null && ma.getReferenceMapName() != null)
                || (this.mp_RefMapName != null && ma.getReferenceMapName() == null)) {
            return false;
        }

        if (this.mp_RefMapName != null && ma.getReferenceMapName() != null) {
            if (!this.mp_RefMapName.equals(ma.getReferenceMapName())) {
                return false;
            }
        }

        //check map orientations
        if (this.mp_IsForwardOriented != ma.isForwardOriented()) {
            return false;
        }

        if (this.mp_IsRefMapForwardOriented != ma.isRefMapForwardOriented()) {
            return false;
        }

        if (this.mp_Pvalue != ma.getPvalue() || this.mp_Score != ma.getScore()) {
            return false;
        }

        //check map alignments
        Map fragAligns = ma.getFragAlignments();
        if ((this.mp_AlignmentTable == null && fragAligns != null)
                || (this.mp_AlignmentTable != null && fragAligns == null)) {
            return false;
        }
        if (this.mp_AlignmentTable != null && fragAligns != null) {
            //check size
            if (this.mp_AlignmentTable.size() != fragAligns.size()) {
                return false;
            }
            
            FragAlignment thisfa;
            FragAlignment compfa;
            for ( Integer thiskey : mp_AlignmentTable.keySet() ){
            
                compfa = (FragAlignment) fragAligns.get(thiskey);
                thisfa = (FragAlignment) this.mp_AlignmentTable.get(thiskey);

                if ((thisfa == null && compfa != null)
                        || (thisfa != null && compfa == null)) {
                    return false;
                }

                if (thisfa.isEqual(compfa) == false) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int getNumberOfFragmentsAlignedToThisReferenceFragment(int refFragIndex) throws Exception {
        if (mp_AlignmentTable == null || mp_AlignmentTable.isEmpty() == true) {
            return 0;
        }
        
        int count = 0;

        for ( FragAlignment fa : mp_AlignmentTable.values() ){        
            if (fa.getLeftAlignment() == refFragIndex || fa.getRightAlignment() == refFragIndex) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void setAlignmentId(String id) {
        alignmentId = id;
    }

    @Override
    public String getAlignmentId() {
        return alignmentId;
    }

    @Override
    public int getMapAlignedIndexAtRefIndex(int iref) {
        if ( mp_ReferenceAlignmentTable.containsKey(iref) )
            return mp_ReferenceAlignmentTable.get(iref);
        else
            return -1;        
    }
}
