package edu.wisc.lmcg.alignment.contig;

import edu.wisc.lmcg.alignment.mapalignment.MapAlignment;
import edu.wisc.lmcg.alignment.mapalignment.StandardMapAlignment;
import java.util.List;
import java.util.Map;

/**
 * Defines an interface for a Contig or set of Maps aligned to a reference maps.
 */
public interface Contig {

    /**
     * Sets the T value for the contig
     * @param tval 
     */
    public void setTvalue(double tval);

    /**
     * Gets the T value
     * @return T value
     */
    public double getTvalue();

    /**
     * Sets the reference map's name and returns true if the names was changed successfully
     * @param name new map name
     * @return True if the name could be changed
     */
    public boolean setReferenceMapName(String name);

    /**
     * Gets the reference map's name
     * @return the current reference map name
     */
    public String getReferenceMapName();

    /**
     * Adds a new alignment to the contig
     * @param alignment new Alignment
     * @return True if there was any problem adding the alignment 
     */
    public boolean addMapAlignment(MapAlignment alignment);

    /**
     * Returns a dictionary with all the alignments associated with the contig. The name of the aligned map is the key for every entry.
     * @return A dictionary with all the alignments
     */
    public Map<String, List<StandardMapAlignment>> getMapAlignments();

    /**
     * FIXME: Not sure to understand this function, also nobody use it
     * @return 
     */
    public String getStrippedRefMapName();

    /**
     * FIXME: Not sure to understand this function, also nobody use it
     * @param mapname
     * @return
     * @throws Exception 
     */
    public Contig realignContigToMap(String mapname) throws Exception;

    /**
     * Gets the max depth of a contig in window specified.
     *
     * @param startindex - starting index of reference fragment to search for
     * max depth.
     * @param endindex - ending index of reference fragment to stop looking for
     * max depth.
     * @return depth of contig as int.
     */
    public int getDepth(int startindex, int endindex);

    /**
     * Counts the number of aligned maps
     * @return The number of current aligned maps
     */
    public int getNumberAlignedMaps();

    /**
     * Exports the contig to xml format
     * @return A string containing this contig in xml format
     */
    public String getContigAsXml();
}
