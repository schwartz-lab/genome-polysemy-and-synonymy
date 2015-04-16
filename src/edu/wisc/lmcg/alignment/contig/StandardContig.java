/*
 * StandardContig.java
 *
 * Created on November 15, 2004, 1:54 PM
 */
package edu.wisc.lmcg.alignment.contig;

import edu.wisc.lmcg.alignment.mapalignment.MapAlignment;
import edu.wisc.lmcg.alignment.mapalignment.StandardMapAlignment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author churas
 */
public class StandardContig implements Contig {

    private String mp_RefMapName;
    private Map<String, List<StandardMapAlignment>> mp_MapAlignments;
    private double mp_Tval;

    /**
     * Creates a new instance of StandardContig
     */
    public StandardContig() {
        mp_Tval = -1.0;
        mp_RefMapName = null;
        mp_MapAlignments = new HashMap<>();
    }

    @Override
    public void setTvalue(double tval) {
        mp_Tval = tval;
    }

    @Override
    public double getTvalue() {
        return mp_Tval;
    }

    @Override
    public boolean setReferenceMapName(String name) {
        if (name == null) {
            return false;
        }
        mp_RefMapName = name;
        return true;
    }

    @Override
    public String getReferenceMapName() {
        return mp_RefMapName;
    }

    @Override
    public boolean addMapAlignment(MapAlignment alignment) {
        if (alignment == null) {
            return false;
        }

        if (alignment.getAlignedMapName() == null) {
            return false;
        }

        if (mp_MapAlignments.containsKey(alignment.getAlignedMapName()) == true) {
            Vector alignList = (Vector) mp_MapAlignments.get(alignment.getAlignedMapName());
            alignList.add(alignment);
            return true;
        }

        Vector newVector = new Vector();

        newVector.add(alignment);

        mp_MapAlignments.put(alignment.getAlignedMapName(), newVector);

        return true;
    }

    @Override
    public Map<String, List<StandardMapAlignment>> getMapAlignments() {
        return mp_MapAlignments;
    }

    @Override
    public String getStrippedRefMapName() {
        if (mp_RefMapName == null) {
            return null;
        }

        int colpos = mp_RefMapName.lastIndexOf(':');

        return mp_RefMapName.substring(colpos + 1);
    }

    public Contig realignContigToMap(String mapname)
            throws Exception {
        if (mapname == null) {
            throw new NullPointerException("mapname is null");
        }

        Vector mapalignvec = (Vector) mp_MapAlignments.get(mapname);

        //lets find the map in question and the associated map alignment.
        MapAlignment newrefmapalign = (MapAlignment) mapalignvec.elementAt(0);

        if (newrefmapalign == null) {
            throw new NullPointerException("unable to find aligned map named: " + mapname);
        }

        //System.out.println("the new refalign: "+newrefmapalign.getMapAlignmentAsXml());
        //okay got the map alignment of the reference map.
        //lets make a new contig
        StandardContig realigncon = new StandardContig();

        realigncon.mp_RefMapName = mapname;
        realigncon.mp_Tval = mp_Tval;

        Vector oldmavec = null;

        MapAlignment oldma = null;
        MapAlignment newmapalignment = null;

        //all right now for the fun part of figuring out new map alignments        
        for (String curalignmapname : mp_MapAlignments.keySet()) {            

            //ignore the map that is to become the next reference map
            if (curalignmapname.equals(mapname)) {
                continue;
            }

            oldmavec = (Vector) mp_MapAlignments.get(curalignmapname);
            oldma = (MapAlignment) oldmavec.elementAt(0);

            if (oldma == null) {
                throw new NullPointerException("oldma is null");
            }

            // System.out.println("old map alignment: "+oldma.getMapAlignmentAsXml());
            newmapalignment = oldma.realignMapAlignmentToNewAlignment(newrefmapalign);
            if (newmapalignment == null) {
                System.err.println("couldnt realign map to new alignment");
                continue;
            }
            //  System.out.println("New map alignment: "+newmapalignment.getMapAlignmentAsXml());
            realigncon.addMapAlignment(newmapalignment);
        }

        return realigncon;
    }

    /**
     * Gets the max depth of a contig in window specified.
     *
     * @param startindex - starting index of reference fragment to search for
     * max depth.
     * @param endindex - ending index of reference fragment to stop looking for
     * max depth.
     * @return depth of contig as int.
     */
    public int getDepth(int startindex, int endindex) {        
        
        int maxDepth = 0;
        int curFragDepth = 0;

        //go through each fragment and obtain depth of contig at that fragment
        for (int x = startindex; x < endindex; x++) {

            //iterate through all map alignments to find depth at the current
            //fragment
            for (  List<StandardMapAlignment> mapAvec : mp_MapAlignments.values()) {
                
                for ( StandardMapAlignment mapAlign : mapAvec) {

                    //if a map aligns at this fragment add 1 to depth 
                    if (mapAlign.isMapAlignedToRefMapWithinThisWindow(x, x + 1)
                            == true) {
                        curFragDepth++;
                    }
                }
            }

            if (curFragDepth > maxDepth) {
                maxDepth = curFragDepth;
            }
            //reset curfrag depth
            curFragDepth = 0;
        }

        return maxDepth;
    }

    public int getNumberAlignedMaps() {
        List<StandardMapAlignment> mapAVec = null;
        int numMaps = 0;
        
        for ( List<StandardMapAlignment> mapAvec : mp_MapAlignments.values()) {            
            numMaps += mapAVec.size();
        }

        return numMaps;
    }

    public String getContigAsXml() {

        StringBuffer xmlStr = new StringBuffer();
        for (List<StandardMapAlignment> mapalignVec : this.mp_MapAlignments.values()) {            
            if (mapalignVec == null) {
                continue;
            }
            for (StandardMapAlignment ma : mapalignVec) {
                if (ma == null) {
                    continue;
                }
                xmlStr.append(ma.getMapAlignmentAsXml());
            }
        }

        return xmlStr.toString();
    }

}
