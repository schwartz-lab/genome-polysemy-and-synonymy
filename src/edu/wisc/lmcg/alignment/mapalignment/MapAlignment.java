package edu.wisc.lmcg.alignment.mapalignment;

import java.util.*;

import edu.wisc.lmcg.map.RestrictionMap;
import edu.wisc.lmcg.alignment.FragAlignment;

/**
 * This represents an alignment of an aligned map to a reference map. Within
 * this object the score of the alignment along with orientation and fragment
 * alignment information is stored.
 */
public interface MapAlignment {

    /**
     * Removes the fragalignment at specified index.
     *
     * @param val
     * @return true if the fragliangment was removed.
     */
    public boolean removeFragAlignmentAtIndex(int val);

    public void setScore(final double val);

    public void setScore(final String score);

    public void setPvalue(final String pval);

    public void setAlignmentId(final String id);

    public double getScore();

    public String getAlignmentId();

    public void setPvalue(final double val);

    public double getPvalue();

    public Map getFragAlignments();

    /**
     * Sets the name of the Reference map is aligned to.
     *
     * @param name - String containing name of Reference Map.
     */
    public void setReferenceMapName(final String name);

    /**
     * Gets name of Reference Map.
     *
     * @return String containing name of reference map.
     */
    public String getReferenceMapName();

    /**
     * Sets name of Aligned Map.
     *
     * @param name - String containing name of aligned map.
     */
    public void setAlignedMapName(final String name);

    /**
     * Gets aligned map name.
     *
     * @return String containing aligned map name.
     */
    public String getAlignedMapName();

    /**
     * Sets orientation of aligned map to either N = normal or R = reversed.
     *
     * @param orientation - String containing N or R.
     */
    public void setOrientation(final String orientation);

    /**
     * Gets whether aligned map is forward oriented or if N was set in
     * setOrientation method.
     *
     * @return boolean where true means aligned map is forward orientated.
     */
    public boolean isForwardOriented();

    /**
     * Sets whether reference map is forward oriented or not
     *
     * @param val - boolean set to true if reference map is forward oriented
     */
    public void isRefMapForwardOriented(final boolean val);

    /**
     * Gets whether the reference map is forward oriented.
     *
     * @return boolean true means the reference map is forward oriented false
     * means no.
     */
    public boolean isRefMapForwardOriented();

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
    public void addAlignment(final String index, final String left, final String right);

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
    public void addAlignment(final int index, final int left, final int right);

    /**
     * Gets FragAlignment for given aligned map fragment index.
     *
     * @param i - int representing fragment index of aligned map.
     * @return FragAlignment - containing alignment at specified index.
     */
    public FragAlignment getFragAlignmentAtMapIndex(final int i);

    /**
     * Gets the aligned map index of a reference map index
     *
     * @param iref - int representing fragment index of reference map.
     * @return int - containing alignment at specified index.
     */
    public int getMapAlignedIndexAtRefIndex(final int iref);

    /**
     * Gets the index of last frag alignment in relation to fragment index of
     * the aligned map. Please note this method finds the highest fragment index
     * and does not find lowest if the aligned map is reversed.
     *
     * @return int representing index of last FragAlignment.
     */
    public int getIndexOfLastFragAlignment();

    /**
     * Gets last FragAlignment for MapAlignment. Same as
     * getIndexOfLastFragAlignment but this method actually returns the
     * FragAlignment instead of just an index.
     *
     * @return FragAlignment of last aligned Fragment.
     */
    public FragAlignment getLastFragAlignment();

    public int getIndexOfFirstFragAlignment();

    /**
     * Gets first FragAlignment for MapAlignment.
     *
     * @return FragAlignment of first aligned Fragment.
     */
    public FragAlignment getFirstFragAlignment();

    /**
     * This method simply counts the number of fragment alignments. It could
     * also be described as the number of aligned fragments.
     *
     * @return number of aligned fragments.
     */
    public int getNumberOfAlignedFragments();

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
     * @throws java.lang.Exception
     */
    public MapAlignment realignMapAlignmentToNewAlignment(MapAlignment newalign) throws Exception;

    /**
     * This method kicks out a string of xml representing the fragment
     * alignments.
     *
     * @return String containing xml fragment alignments.
     * @throws java.lang.Exception
     */
    public String getXmlFragAlignments() throws Exception;

    /**
     * This method calculates the number of false cuts in an aligned map by
     * counting the number of instances where a fragment alignment points to two
     * reference fragments.
     *
     * @return count of the number of false cuts.
     * @throws java.lang.Exception
     */
    public int getNumberOfFalseCuts() throws Exception;

    /**
     * Checks if cut to left of fragment index is false. The algorithm is simply
     * to take the fragalignment at index passed in and look if the left
     * alignment of this fragment matches the right alignment of the previous
     * fragment. if so then the cut to the left is a false cut.
     *
     * @param fragindex - int representing fragment index of aligned map.
     * @return boolean - true if cut is a false cut.
     * @throws java.lang.Exception
     */
    public boolean isCutLeftOfFragIndexFalse(final int fragindex) throws Exception;

    /**
     * Get total number of missing cuts by summing the number reference
     *
     * @return
     * @throws java.lang.Exception
     */
    public int getNumberOfMissingCuts() throws Exception, IndexOutOfBoundsException;

    /**
     * Gets count of missing counts for aligned fragment. This method calculates
     * count of missing fragments by subtracting the right alignment from the
     * left alignment for the fragment <B>fragindex</B> being examined.
     *
     * @param fragindex - index of aligned frag to check.
     * @return int - number of missing cuts for aligned fragment.
     * @throws java.lang.Exception
     */
    public int getNumberOfMissingCutsAtFragIndex(final int fragindex) throws Exception;

    /**
     * This method gets mass of Aligned chunk at Aligned Frag index.
     *
     * @param fragindex
     * @param resmap
     * @return mass in base pairs of aligned chunk.
     * @throws java.lang.Exception
     */
    public int getMassOfAlignedChunkAtFragIndex(final int fragindex, final RestrictionMap resmap) throws Exception;

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
    public int getLeftMostFragmentAlignedToThisRefFrag(final int fragindex, final int curalignfragindex) throws IndexOutOfBoundsException;

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
    public int getRightMostFragmentAlignedToThisRefFrag(final int fragindex, final int curalignfragindex) throws IndexOutOfBoundsException;

    public int getMassOfRefChunkAtFragIndex(final int fragindex, final RestrictionMap resmap) throws Exception;

    public boolean isMapAlignedToRefMapWithinThisWindow(final int startindex, final int endindex);

    public String getMapAlignmentAsXml();

    /**
     * This method shifts all frag alignments by value specified. any -1 values
     * will be set to the other (left or right) frag alignment
     *
     * @param shiftval
     * @return
     */
    public boolean shiftFragAlignments(final int shiftval);

    /**
     * This method shifts all frag alignments by value specified. any -1 values
     * will be set to the other (left or right) frag alignment
     *
     * @param shiftval
     * @return
     */
    public boolean shiftFragAlignmentIndexes(final int shiftval);

    /**
     * This method inverts the alignments of this map alignment by first setting
     * the reference map as the aligned map and the aligned map as the reference
     * map. The method then flips every FragAlignment. Calling this method twice
     * should result in no change in the alignment.
     *
     * @param numFragsInRefMap
     * @param numFragsInAmap
     * @return Inverted MapAlignment or null if operation could not be performed
     * @throws java.lang.Exception
     */
    public MapAlignment invert(final int numFragsInRefMap, final int numFragsInAmap) throws Exception;

    /**
     * This method creates a new MapAlignment with the FragAlignments shifted as
     * defined in the hashtable passed in. This is useful if a map is changed
     * and a map alignment needs to be modified to accomodate the change.
     *
     * @param shiftHash - Hashtable key is an Integer representing old reference
     * index and value is an Integer representing the new reference index.
     * @return MapAlignment upon success or null upon error.
     */
    public MapAlignment shiftFragAlignments(Hashtable shiftHash);

    /**
     * Change the orientation of the alignment
     *
     * @param numFragsInRefMap Number of fragments contained by the Aligned map
     * @param numFragsInAlignedMap Number of fragments contained by the
     * reference map
     * @return new Map alignment in reverse order
     * @throws Exception
     */
    public MapAlignment reverseAlignment(final int numFragsInRefMap, final int numFragsInAlignedMap) throws Exception;

    /**
     * This method lets caller know if two map alignments are equal
     *
     * @param ma Map alignment for doing the comparison
     * @return true if they are equal false otherwise.
     */
    public boolean isEqual(MapAlignment ma);

    /**
     * Counts the number of times a reference fragment is aligned
     *
     * @param refFragIndex
     * @return Number of times a reference fragment is aligned
     * @throws java.lang.Exception
     */
    public int getNumberOfFragmentsAlignedToThisReferenceFragment(int refFragIndex) throws Exception;
}
