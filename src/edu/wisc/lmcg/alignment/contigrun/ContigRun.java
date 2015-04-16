package edu.wisc.lmcg.alignment.contigrun;

import edu.wisc.lmcg.alignment.contig.Contig;
import edu.wisc.lmcg.alignment.mapalignment.MapAlignment;
import edu.wisc.lmcg.map.Mapset;
import edu.wisc.lmcg.map.RestrictionMap;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

/**
 * Represents a run of an alignment program. This could be gentig, soma or some
 * other alignment tool that generates Contigs.
 *
 * @author Churas
 */
public interface ContigRun {

    /**
     * Sets an experiment's property
     * @param key Property name
     * @param val Property value
     */
    public void setExperimentProperty(final String key, final String val);

    /**
     * Returns an experiment's property
     * @param key Property name
     * @return The value for the property
     */
    public String getExperimentProperty(final String key);

    /**
     * Returns the names of all the experiment's properties
     * @return 
     */
    public Set getExperimentKeys();

    /**
     * Sets the name of the program that created the contigrun.
     * @param creator Specifies the name of the software that created the contigRun
     */
    public void setCreator(final String creator);

    /**
     * Returns the name of the program that created contigrun.
     *
     * @return String.
     */
    public String getCreator();

    /**
     * Adds a new contig
     * @param con new contig
     * @return true if the contig was successfully added
     */
    public boolean addContig(Contig con);

    /**
     * Adds a new Alignment to the contig
     * @param alignment new alignment
     * @return true if the alignment was successfully added
     * @throws Exception 
     */
    public boolean addMapAlignment(MapAlignment alignment) throws Exception;

    /**
     * Sets the id for the contig run
     * @param uuid 
     */
    public void addUUID(final String uuid);

    /**
     * Gets the id of this contig run
     * @return 
     */
    public String getUUID();

    /**
     * Gets a collection with all the contigs in the contigrun
     * @return List of contigs
     */
    public Map<String, Contig> getContigs();

    /**
     * Stores the contigrun in a database specified by the parameter
     * @param dbcon Database connection
     * @return true if the operation was completed
     * @throws Exception 
     */
    public boolean saveToDatabase(Connection dbcon) throws Exception;

    /**
     * Includes another map to the list of available  maps
     * @param resmap New restriction map
     */
    public void addMapToMapset(RestrictionMap resmap);

    /**
     * Sets a structure for storing the maps in the contig run
     * @param ms new map set
     */
    public void setMapset(Mapset ms);

    /**
     * Returns the map set for this contig run
     * @return map set
     */
    
    public Mapset getMapset();

    /**
     * Sets the t-value for this contig run
     * @param refmapname Reference map name
     * @param tval t-value
     * @throws Exception 
     */
    public void setTvalueForContig(String refmapname,
            double tval) throws Exception;

    /**
     * Returns all the information in the contigrun as an xml String
     * @return
     * @throws java.io.IOException 
     */
    public String getContigAsXml() throws java.io.IOException;

    /**
     * Saves the contigrun to a file specified by the parameter
     * @param file filename for storing the contigrun
     * @return true if the file could be successfully saved
     * @throws NullPointerException
     * @throws java.io.IOException 
     */
    public boolean saveContigToFile(final String file) throws NullPointerException, java.io.IOException;

}
