/*
 * RestrictionMapUtilImpl.java
 *
 * Created on March 30, 2005, 1:34 PM
 */
package edu.wisc.lmcg.map.restrictionmap;

import edu.wisc.lmcg.map.RestrictionFragment;
import edu.wisc.lmcg.map.RestrictionMap;
import edu.wisc.lmcg.map.SimpleRestrictionMap;
import java.util.List;
import java.util.Vector;

/**
 * Provides standardized ways of parsing out group id and omdb identifiers from
 * map names.
 *
 * @author churas
 */
public class RestrictionMapUtil {

    /**
     * Creates a new instance of RestrictionMapUtilImpl
     */
    private RestrictionMapUtil() {
    }

    /**
     * This method outputs the remainder of the map name after the first _
     */
    public static String getContentsOfMapNameAfterGroupId(final String mapname) {
        if (mapname == null || mapname.equals("")) {
            return null;
        }
        int startpos = 0;
        startpos = mapname.indexOf("_");
        if (startpos == -1) {
            return null;
        }
        return mapname.substring(startpos);
    }

    /**
     * it is assumed that a group with a valid run number in it is of the
     * following format: (omdb id optional)(groupid)_(runnumber)_(moleculeindex)
     */
    public static String getRunNumberFromMapName(final String mapname) {
        if (mapname == null || mapname.equals("")) {
            return null;
        }
        int startpos = 0;
        int endpos = mapname.length();

        startpos = mapname.indexOf("_");
        if (startpos == -1) {
            return null;
        }

        endpos = mapname.indexOf("_", startpos + 1);
        if (endpos == -1) {
            return null;
        }

        return mapname.substring(startpos + 1, endpos);
    }

    public static String getGroupIdFromMapName(String mapname) {
        if (mapname == null || mapname.equals("")) {
            return null;
        }
        int startpos = 0;
        int endpos = mapname.length();

        if (mapname.startsWith("omdb:") == true) {
            startpos = mapname.indexOf(':', 5) + 1;
        }

        endpos = mapname.indexOf("_");

        if (endpos == -1 || endpos > mapname.length()) {
            endpos = mapname.length();
        }

        return mapname.substring(startpos, endpos);

    }

    public static String getOmdbIdFromMapName(String mapname) {
        if (mapname == null || mapname.equals("")) {
            return null;
        }

        if (mapname.startsWith("omdb:") == true) {
            return mapname.substring(5, mapname.indexOf(':', 5));
        }
        return null;
    }

    public static String getOmdbStrippedMapName(String mapname) {
        if (mapname == null || mapname.equals("")) {
            return null;
        }

        if (mapname.startsWith("omdb:") == true) {
            return mapname.substring(mapname.indexOf(':', 5) + 1);
        }
        return mapname;
    }

    /**
     * This method returns a restriction map when given map data from omm
     * database.
     *
     * @param id - database id of map
     * @param map_identifier - map name example: 1040340_1_2
     * @param map - the actual map example: \tSwaI\tS\t10.0\t11.0\t12.0
     * @return RestrictionMap upon success or null upon failure.
     */
    public static RestrictionMap getRestrictionMap(final String id, final String map_identifier, final String map) throws Exception {
        //just return null if any inputs are null
        if (id == null || map_identifier == null || map == null) {
            return null;
        }

        int enzymeindex = 0;
        int enzymeinitial = 0;

        enzymeindex = map.indexOf('\t', 1);

        if (enzymeindex == -1) {
            return null;
        }

        enzymeinitial = map.indexOf('\t', (enzymeindex + 1));
        if (enzymeinitial == -1) {
            return null;
        }

        SimpleRestrictionMap resmap = new SimpleRestrictionMap();

        resmap.setName("omdb:" + id + ":" + map_identifier);

        resmap.setEnzyme(map.substring(1, enzymeindex));
        resmap.setMapBlock(map.substring(enzymeinitial + 1));

        return resmap;
    }

    public static RestrictionMap getRestrictionMapWithMinConfidence(final RestrictionMap resmap, double minConfidence) throws Exception {
        if (resmap == null) {
            throw new NullPointerException("Restriction map is null");
        }

        RestrictionFragment curFrag = null;
        RestrictionFragment newFrag = null;
        int curfragmass = 0;
        Vector newFrags = new Vector();
        List<RestrictionFragment> frags = resmap.getFragments();
        for (int i = 0; i < frags.size(); i++) {
            curFrag = (RestrictionFragment) frags.get(i);

            if (curfragmass == 0) {
                curfragmass = curFrag.getMassInBp();
                continue;
            }

            if (curFrag.getContigCutConfidence() < minConfidence) {
                curfragmass += curFrag.getMassInBp();
                continue;
            }
            newFrag = new RestrictionFragment();
            newFrag.setMassInBp(curfragmass);
            newFrags.add(newFrag);
            curfragmass = curFrag.getMassInBp();
        }

        if (curfragmass > 0) {
            newFrag = new RestrictionFragment();
            newFrag.setMassInBp(curfragmass);
            newFrags.add(newFrag);
        }

        SimpleRestrictionMap sres = new SimpleRestrictionMap();
        sres.setName(resmap.getName());
        sres.setEnzyme(resmap.getEnzyme());
        sres.setType(resmap.getType());
        sres.setFragments(newFrags);
        if (resmap.isForwardOriented() == false) {
            sres.setOrientation("R");
        }
        return (RestrictionMap) sres;
    }

}
