/*
 * MetaChromosome.java
 *
 * Created on September 15, 2004, 4:36 PM
 */
package edu.wisc.lmcg.alignment.placed;

import java.sql.Connection;
import java.sql.Statement;

import edu.wisc.lmcg.location.Location;
import edu.wisc.lmcg.alignment.contigrun.ContigRun;

/**
 * Represents a MetaChromosome alignment which means a fake chromosome was
 * generated and maps were aligned to that fake chromosome from other
 * alignments.
 *
 * @author churas
 */
public class MetaChromosome implements PlacedAlignment {

    /**
     * the location of the placed alignment
     */
    private Location mp_Location;

    /**
     * the contigrun associated with this placed alignment
     */
    private ContigRun mp_ContigRun;

    /**
     * the xml file this contigrun is saved to
     */
    private String mp_XmlFile;

    /**
     * Creates a new instance of MetaChromosome
     */
    public MetaChromosome() {
        mp_Location = null;
    }

    /**
     * Get metachromosome's xml representation 
     * @return 
     */
    @Override
    public String getAsXml() {
        String xmlStr = "\t<MetaChromosome>\n";

        if (mp_Location != null) {
            xmlStr += "\t\t<Location>\n";
            xmlStr += "\t\t\t<Chrom>" + mp_Location.getChromosome() + "</Chrom>\n";
            xmlStr += "\t\t\t<StartPos>" + mp_Location.getStartPos() + "</StartPos>\n";
            xmlStr += "\t\t\t<EndPos>" + mp_Location.getEndPos() + "</EndPos>\n";
            xmlStr += "\t\t</Location>\n";
        }
        if (mp_XmlFile != null) {
            xmlStr += "\t\t<XmlFile>" + mp_XmlFile + "</XmlFile>\n";
        }
        xmlStr += "\t</MetaChromosome>\n";

        return xmlStr;
    }

    public void setContigRun(ContigRun crun) {
        mp_ContigRun = crun;
    }

    @Override
    public ContigRun getContigRun() {
        return mp_ContigRun;
    }

    public void setLocation(Location loc) {
        mp_Location = loc;
    }

    @Override
    public Location getLocation() {
        return mp_Location;
    }

    public void setXmlFile(String xmlfile) {
        mp_XmlFile = xmlfile;
    }

    @Override
    public String getXmlFile() {
        return mp_XmlFile;
    }

    @Override
    public PlacedAlignmentType getType() {
        return PlacedAlignmentType.METACHROMOSOME;
    }

    @Override
    public void addToVariationDB(Connection con, String omrefalignment_id) throws java.sql.SQLException {
        if (con == null) {
            throw new NullPointerException("connection is null");
        }

        if (omrefalignment_id == null) {
            throw new NullPointerException("omrefalignment_id is null");
        }

        if (mp_XmlFile == null) {
            throw new NullPointerException("xmlfile not set");
        }

        //String xmlalignment = null;
        //try {
        //  xmlalignment = mp_ContigRun.getContigAsXml();
        // }
        // catch(Exception ex){
        //     return;
        // }
        String insertStr = "INSERT INTO ommetachrom (omrefalignment_id,xmlfile) VALUES('"
                + omrefalignment_id + "','" + mp_XmlFile + "')";

        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(insertStr);
        } finally {
            stmt.close();
        }
    }

}
