package edu.wisc.lmcg.map;

import java.util.*;
import edu.wisc.lmcg.alignment.*;

/**
 * Defines a RestrictionMap
 */
public interface RestrictionMap {
    
    public static final String CONSENSUS_TYPE = "consensus";
    public static final String OPMAP_TYPE = "opmap";

    public boolean isMapForwardOriented();

    public boolean isForwardOriented();

    public String getName();

    /**
     * This method gets the type of map.
     *
     * @return String containing type of map.
     */
    public String getType();

    /**
     * This method gets the Enzyme specified for the map.
     *
     * @return String containing Enzyme like XhoI.
     */
    public String getEnzyme();

    /**
     * This method generates a String containing the RestrictionMap in the
     * standard map format. The format is as follows:
     * {@literal <mapname><newline>
     * <tab><tab><first letter of enzyme><tab><enzyme><tab><fragment><newline>
     * <newline>
     * <mapname>... Example: 123455_5_5 \tX\tXhoI\t12.5\t15.3\t20.2\t\n \n
     * 5555_5..}
     *
     * @return String containing map in tab format upon success otherwise null.
     */
    public String getMapInTabFormat();

    /**
     * Outputs map with fragments below minconfidence merged together.
     *
     * @return String containing map in OM map format.
     */
    public String getMapInTabFormatWithMinConfidence(double minconfidence);

    /**
     * This method checks for omdb: identifier at the start of the map name if
     * found it then pulls out the omm database identifier. The format is as
     * follows: omdb:#:... where # is the database identifier.
     *
     * @return String containing identifier or null if no map name or identifier
     * is not found.
     */
    public String getOmmdbId();

    /**
     * This method removes the omdb:#: prefix if it exists in map name
     *
     * @return String containing map name without omdb:#: prefix.
     */
    public String getOmmdbIdStrippedName();

    /**
     * Parses GroupID from map name. This method strips away omdb:.*: prefix and
     * takes all characters before _ as groupid.
     *
     * @return String containing GroupID if found.
     */
    public String getGroupIdFromMapName();

    /**
     * This method gets total mass of map in base pairs.
     *
     * @return int containing total mass of map in base pairs.
     */
    public int getTotalMassInBP();

    /**
     * see getTotalMassInBPLeftOfIndex with reOrientMapForward flag set to
     * false.
     */
    public int getTotalMassInBPLeftOfIndex(FragAlignment fragAlign) throws Exception;

    /**
     * This method sums up the mass in bases of fragments at the leftmost
     * location of the FragAlignment until the start of the molecule This method
     * does not flip the molecule if it is oriented in reverse. It is up to the
     * caller to do that calculation.
     *
     * @throws Exception if fragAlign is null or if the index in fragAlign
     * object exceeds the number of fragments in the molecule or if the index is
     * less then 0
     * @return number of bases from fragment index to start of molecule.
     */
    public int getTotalMassInBPLeftOfIndex(FragAlignment fragAlign,
            boolean reOrientMapForward) throws Exception;

    /**
     * This method sums up the mass in bases of fragments at the rightmost
     * location of the index until the start of the molecule This method does
     * not flip the molecule if it is oriented in reverse. It is up to the
     * caller to do that calculation.
     *
     * @throws Exception if fragAlign is null or if the index in fragAlign
     * object exceeds the number of fragments in the molecule or if the index is
     * less then 0
     * @return number of bases from fragment index to start of molecule.
     */
    public int getTotalMassInBPLeftOfIndex(int index) throws Exception;

    /**
     * see getTotalMassInBPRightOfIndex with reOrientMapForward flag set to
     * false.
     */
    public int getTotalMassInBPRightOfIndex(FragAlignment fragAlign) throws Exception;

    /**
     * This method sums up the mass in bases of fragments at the rightmost
     * location of the FragAlignment until the end of the molecule This method
     * does not flip the molecule if it is oriented in reverse. It is up to the
     * caller to do that calculation.
     *
     * @throws Exception if fragAlign is null or if the index in fragAlign
     * object exceeds the number of fragments in the molecule.
     * @return number of bases from fragment index to right end of molecule.
     */
    public int getTotalMassInBPRightOfIndex(FragAlignment fragAlign,
            boolean reOrientMapForward) throws Exception;

    /**
     * This method returns the number of fragments in the Restriction Map.
     *
     * @return int containing number of fragments.
     */
    public int getNumberFragments();

    /**
     * This method gets all the fragments in the RestrictionMap.
     *
     * @return Vector containing RestrictionFragment objects.
     */
    public List<RestrictionFragment> getFragments();

    public boolean isMapWellFormed();

    /**
     * This method was added as a double check to ensure no 0 mass fragments or
     * negative fragments exist in map.
     *
     * @return true if there are fragments with negative or zero mass.
     */
    public boolean containsNegativeOrZeroMassFragments();

    public int getMassInBpOfLeftMostAlignedFrag(FragAlignment fragAlign) throws Exception;

    /**
     * This method gets the mass of leftmost aligned fragment from
     * FragAlignment.
     *
     * @param fragAlign - FragAlignment containing index of fragment to obtain
     * mass from.
     * @return int mass of left most alignment in FragAlignment in base pairs.
     */
    public int getMassInBpOfLeftMostAlignedFrag(FragAlignment fragAlign,
            boolean reOrientMapForward) throws Exception;

    public int getMassInBpOfRightMostAlignedFrag(FragAlignment fragAlign) throws Exception;

    /**
     * This method gets the mass of leftmost aligned fragment from
     * FragAlignment.
     *
     * @param fragAlign - FragAlignment containing index of fragment to obtain
     * mass from.
     * @return int mass of left most alignment in FragAlignment in base pairs.
     */
    public int getMassInBpOfRightMostAlignedFrag(FragAlignment fragAlign,
            boolean reOrientMapForward) throws Exception;

    /**
     * Concatenates the Fragments of cmap to this map.
     * <B> Note both maps must have same enzyme</B>
     *
     * @return RestrictionMap containing concatenated fragments.
     */
    public RestrictionMap concat(RestrictionMap cmap) throws Exception;

    /**
     * This method reverses the fragment of this map and switches the
     * orientation to the opposite of what it is.
     *
     * @return true if the operation was successful.
     */
    public boolean reverseMap() throws Exception;

    /**
     * This method attempts to remove massinbases base pairs from end of
     * RestrictionMap. This method only removes fragments so it may remove less
     * but never more then the value specified.
     *
     * @return true if operation was successful or false if no fragments were
     * removed or if the value exceeds the mass of the map.
     */
    public boolean deleteMassFromEndOfMap(int massinbases) throws Exception;

    /**
     * Creates a subset map from starting base pair offset to ending base pair
     * offset.
     *
     * @return RestrictionMap.
     */
    public RestrictionMap getSubsetMap(int startbase,
            int endbase) throws Exception;

    /**
     * Gets the mass including and between fragment indexes.
     *
     * @return mass in base pairs.
     */
    public int getMassInBpBetweenFragmentIndexes(int startindex, int endindex) throws Exception;

    /**
     * This method returns a xml string representation of this restriction map
     *
     * @return String containing xml representation of RestrictionMap
     */
    public String getRestrictionMapAsXml();

    public int getTotalMassInBPRightOfIndex(int index) throws Exception;
}
