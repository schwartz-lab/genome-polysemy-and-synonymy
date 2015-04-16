/*
 * PlacedAlignment.java
 *
 * Created on September 15, 2004, 4:33 PM
 */
package edu.wisc.lmcg.alignment.placed;

import java.sql.Connection;
import edu.wisc.lmcg.location.Location;
import edu.wisc.lmcg.alignment.contigrun.ContigRun;

/**
 * This interface defines an alignment at the contigrun level that has been
 * placed to a specific location.
 *
 * @author churas
 */
public interface PlacedAlignment {

    /**
     * Returns the location of the contigrun
     * @return location of the contigrun
     */
    public Location getLocation();

    /**
     * Gets this placedalignment in an xml form.
     * @return placed alignment
     */
    public String getAsXml();

    /**
     * gets the contigrun.
     * @return Contig run
     */
    public ContigRun getContigRun();

    /**
     * gets the xmlfile this contigrun is stored in.
     * @return Gets the file in which this contigrun is stored in
     */
    public String getXmlFile();

    /**
     * Gets the placed alignment type
     * @return String representing the placed alignment type
     */
    public PlacedAlignmentType getType();

    /**
     * 
     * @param con
     * @param omrefalignment_id
     * @throws java.sql.SQLException 
     */
    public void addToVariationDB(Connection con, String omrefalignment_id) throws java.sql.SQLException;

}
