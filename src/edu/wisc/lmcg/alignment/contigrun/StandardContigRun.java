/*
 * StandardContigRun.java
 *
 * Created on November 15, 2004, 2:33 PM
 */
package edu.wisc.lmcg.alignment.contigrun;

import edu.wisc.lmcg.alignment.contig.Contig;
import edu.wisc.lmcg.alignment.contig.StandardContig;
import edu.wisc.lmcg.alignment.mapalignment.MapAlignment;
import edu.wisc.lmcg.map.Mapset;
import edu.wisc.lmcg.map.RestrictionMap;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class ContigRun represents a Single Run of soma or Gentig The ContigRun
 * consists of a set of alignments.
 *
 * @author churas
 */
public class StandardContigRun implements ContigRun {

    private final Map<String, Contig> mp_Contigs;
    private String mp_UUID;
    private Mapset mp_Mapset;
    private String mp_Creator;
    private final HashMap mp_Experiment;

    /**
     * Creates a new instance of StandardContigRun
     */
    public StandardContigRun() {
        mp_Contigs = new TreeMap<>();
        mp_UUID = "";
        mp_Mapset = new Mapset();
        mp_Creator = null;
        mp_Experiment = new HashMap();
    }

    /**
     * Sets the experiment property for the key passed in. if either value is
     * null nothing is done.
     *
     */
    @Override
    public void setExperimentProperty(final String key, final String val) {
        if (key == null || val == null) {
            return;
        }
        mp_Experiment.put(key, val);
    }

    /**
     * Gets the value for the given experiment key
     */
    @Override
    public String getExperimentProperty(final String key) {
        return (String) mp_Experiment.get(key);
    }

    /**
     * Get a set of the experiment keys
     */
    @Override
    public Set getExperimentKeys() {
        return mp_Experiment.keySet();
    }

    /**
     * Sets the name of the program that created the contigrun.
     */
    @Override
    public void setCreator(String creator) {
        mp_Creator = creator;
    }

    /**
     * Returns the name of the program that created contigrun.
     *
     * @return String.
     */
    @Override
    public String getCreator() {
        return mp_Creator;
    }

    /**
     * Adds a new contig to the contigrun
     * @param con new contig
     * @return true if the contig was successfully added
     */
    @Override
    public boolean addContig(Contig con) {
        if (con == null) {
            return false;
        }

        if (con.getReferenceMapName() == null) {
            return false;
        }

        if (mp_Contigs.containsKey(con.getReferenceMapName()) == true) {
            return false;
        }

        mp_Contigs.put(con.getReferenceMapName(), con);
        return true;
    }

     /**
     * Adds a new Alignment to the contig
     * @param alignment new alignment
     * @return true if the alignment was successfully added
     * @throws Exception 
     */
    @Override
    public boolean addMapAlignment(MapAlignment alignment)
            throws Exception {
        if (alignment == null) {
            throw new NullPointerException("alignment passed to method is null");

        }

        if (alignment.getReferenceMapName() == null) {
            throw new NullPointerException("alignment passed to method has null reference map");
        }

        if (mp_Contigs.containsKey(alignment.getReferenceMapName()) == true) {
            Contig theContig = (Contig) mp_Contigs.get(alignment.getReferenceMapName());

            if (theContig == null) {
                throw new NullPointerException("Contig found for alignment was null");
            }

            return theContig.addMapAlignment(alignment);
        } else {
            StandardContig newContig = new StandardContig();

            newContig.setReferenceMapName(alignment.getReferenceMapName());

            mp_Contigs.put(alignment.getReferenceMapName(), newContig);

            return newContig.addMapAlignment(alignment);
        }
    }

    /**
     * Sets the id for the contig run
     * @param uuid 
     */
    @Override
    public void addUUID(String uuid) {
        if (uuid == null) {
            return;
        }
        mp_UUID += uuid;
    }

    /**
     * Gets the id of this contig run
     * @return 
     */
    @Override
    public String getUUID() {
        return mp_UUID;
    }

    /**
     * Gets a collection with all the contigs in the contigrun
     * @return List of contigs
     */
    @Override
    public Map<String, Contig> getContigs() {
        return mp_Contigs;
    }

    @Override
    /**
     * Stores the contigrun in a database specified by the parameter
     * @param dbcon Database connection
     * @return true if the operation was completed
     * @throws Exception 
     */
    public boolean saveToDatabase(Connection dbcon)
            throws Exception {
        checkConnection(dbcon);

        if (isContigRunIDInContigRunTable(dbcon) == true) {
            //okay we have an id in the database lets
            //iterate thru the contigs and insert them into the database
            //if they are not already inserted

            //lets iterate through mp_Contigs :)
        }

        return false;
    }

    /**
     * Returns true if the Contig Run ID is In the ContigRunTable inside the database
     * @param dbcon Connection to the database
     * @return true if the contig run id is in fact in the ContigRunTable
     * @throws Exception 
     */
    protected boolean isContigRunIDInContigRunTable(Connection dbcon)
            throws Exception {
        checkConnection(dbcon);
        boolean uuid = true;
        Statement stmt = null;
        ResultSet rs = null;

        try {

            if (mp_UUID == null || mp_UUID.equals("")) {
                throw new Exception("uuid is null or not set");
            }

            stmt = dbcon.createStatement();
            String theQuery = "SELECT COUNT(*) FROM contigrun WHERE contigrun_id='" + mp_UUID + "'";

            rs = stmt.executeQuery(theQuery);

            if (rs != null) {
                if (rs.next() != false) {
                    int rsval = rs.getInt(1);

                    if (rsval != 1) {
                        uuid = false;
                    }
                    if (rsval > 1) {
                        throw new Exception("More then one contigrun found for id: " + uuid);
                    }
                }
            }
        } catch (Exception ex) {
            //gotta rethrow the exception cause we dont know how to handle it
            throw new Exception(ex);
        } finally {
            rs.close();
            stmt.close();
        }
        return uuid;
    }

    /**
     * Checs if a connection is valid
     * @param dbcon Database connection
     * @throws Exception 
     */
    protected void checkConnection(Connection dbcon)
            throws Exception {
        if (dbcon == null) {
            throw new Exception("Database connection is null");
        }
    }

    /**
     * Includes another map to the list of available  maps
     * @param resmap New restriction map
     */
    @Override
    public void addMapToMapset(RestrictionMap resmap) {
        if (mp_Mapset == null) {
            mp_Mapset = new Mapset();
        }

        mp_Mapset.addRestrictionMap(resmap);
    }

    /**
     * Sets a structure for storing the maps in the contig run
     * @param ms new map set
     */
    @Override
    public void setMapset(Mapset ms) {
        mp_Mapset = ms;
    }

    /**
     * Returns the map set for this contig run
     * @return map set
     */
    @Override
    public Mapset getMapset() {
        return mp_Mapset;
    }

    /**
     * Sets the t-value for this contig run
     * @param refmapname Reference map name
     * @param tval t-value
     * @throws Exception 
     */
    @Override
    public void setTvalueForContig(String refmapname,
            double tval)
            throws Exception {
        if (refmapname == null) {
            throw new Exception("refmapname is null");
        }

        if (mp_Contigs.containsKey(refmapname) == true) {
            Contig theContig = (Contig) mp_Contigs.get(refmapname);

            if (theContig == null) {
                throw new NullPointerException("Contig found for alignment was null");
            }

            theContig.setTvalue(tval);

        } else {
            StandardContig newContig = new StandardContig();

            newContig.setReferenceMapName(refmapname);

            newContig.setTvalue(tval);

            mp_Contigs.put(refmapname, newContig);
        }
    }

    /**
     * Returns all the information in the contigrun as an xml String
     * @return
     * @throws java.io.IOException 
     */
    @Override
    public String getContigAsXml() throws java.io.IOException {

        StringWriter fs = new StringWriter();

        fs.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n");
        fs.write("<aligned_maps_document version=\"0.5\">\n");

        if (mp_Mapset != null) {
            Map<String, RestrictionMap> resmaphash = mp_Mapset.getRestrictionMaps();            

            if (resmaphash != null) {
                for ( RestrictionMap resmap : resmaphash.values()) {
                    
                    if (resmap == null) {
                        continue;
                    }
                    fs.write(resmap.getRestrictionMapAsXml());
                }
            }
        }

        for (Contig con : this.mp_Contigs.values()) {            

            if (con == null) {
                continue;
            }

            fs.write(con.getContigAsXml());
        }

        fs.write("</aligned_maps_document>\n");
        fs.close();

        return fs.getBuffer().toString();
    }

    /**
     * Saves the contigrun to a file specified by the parameter
     * @param file filename for storing the contigrun
     * @return true if the file could be successfully saved
     * @throws NullPointerException
     * @throws java.io.IOException 
     */
    @Override
    public boolean saveContigToFile(final String file) throws NullPointerException, java.io.IOException {

        if (file == null) {
            throw new NullPointerException("file is null");
        }

        try (BufferedWriter fs = new BufferedWriter(new FileWriter(file))) {
            fs.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n");
            fs.write("<aligned_maps_document version=\"0.5\">\n");
            
            if (mp_Mapset != null) {
                Map<String, RestrictionMap> resmaphash = mp_Mapset.getRestrictionMaps();
                
                if (resmaphash != null) {
                    for (RestrictionMap resmap : resmaphash.values()) {
                        if (resmap == null) {
                            continue;
                        }
                        fs.write(resmap.getRestrictionMapAsXml());
                    }
                }
            }
            
            for (Contig con : this.mp_Contigs.values()) {
                
                if (con == null) {
                    continue;
                }
                
                fs.write(con.getContigAsXml());
            }
            
            fs.write("</aligned_maps_document>\n");
        }
        return true;
    }

}
