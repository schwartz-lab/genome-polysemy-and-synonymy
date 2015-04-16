package edu.wisc.lmcg.map;

import java.util.*;

/**
 * Mapset is a class that contains a set of RestrictionMaps. There are methods
 * to get a RestrictionMap and add a RestrictionMap.
 */
public class Mapset implements RestrictionMapsFactory {

    private Map<String, RestrictionMap> mp_RestrictionMaps;

    public Mapset() {
        mp_RestrictionMaps = new TreeMap<>();
    }

    public int getNumberRestrictionMaps() {
        return mp_RestrictionMaps.size();
    }

    /**
     * This method adds a new RestrictionMap to the Mapset.
     *
     * @param resmap - RestrictionMap to add to Mapset.
     * @return boolean set to false if map is null or if name is unset or if map
     * by that name already inserted
     */
    public boolean addRestrictionMap(RestrictionMap resmap) {
        if (resmap == null) {
            return false;
        }

        if (resmap.getName().equals("")) {
            return false;
        }

        if (mp_RestrictionMaps.containsKey(resmap.getName()) == true) {
            return true; //if we encounter a duplicate, just return
            //without adding the duplicate to the table
        }

        mp_RestrictionMaps.put(resmap.getName(), resmap);
        return true;
    }

    /**
     * This method gets the RestrictionMap with specified name if found.
     *
     * @param name - name of RestrictionMap to retreive.
     * @return RestrictionMap that matches name requested in name parameter or
     * null if name passed in was null or if map was not found in Mapset.
     */
    public RestrictionMap getRestrictionMapForName(String name) {
        if (name == null) {
            return null;
        }

        return (RestrictionMap) mp_RestrictionMaps.get(name);
    }

    /**
     * This method gets all RestrictionMaps in Mapset that have String passed in
     * as their name.
     *
     * @param name - String to match in RestrictionMap names.
     * @return Vector of RestrictionMaps where name param consist of part of map
     * name.
     */
    public Vector getRestrictionMapForPartialName(String name)
            throws NullPointerException {
        if (name == null) {
            throw new NullPointerException("name was null");
        }

        Vector resmaps = new Vector();

        for (String mapName : mp_RestrictionMaps.keySet()) {            

            if (mapName.indexOf(name) != -1) {
                resmaps.add(mp_RestrictionMaps.get(mapName));

            }
        }

        return resmaps;
    }

    /**
     * This method returns a Mapset of RestrictionMaps that are not in the
     * Mapset passed into this method.
     *
     * @param msb - used in this NOT msb or the maps in <B>this</B> that are NOT
     * in <B>msb</B>
     * @return Mapset of RestrictionMaps in this but not in msb Mapset.
     */
    public Mapset MapsetNot(Mapset msb)
            throws NullPointerException {
        if (msb == null) {
            throw new NullPointerException("msb is null");
        }

        Mapset newms = new Mapset();
        
        for (String mapName : mp_RestrictionMaps.keySet()) {            

            if (msb.containsRestrictionMap(mapName) == false) {
                newms.addRestrictionMap((RestrictionMap) mp_RestrictionMaps.get(mapName));
            }

        }
        return newms;
    }

    /**
     * This method returns a Mapset of RestrictionMaps that are in either
     * mapset.
     *
     * @param msb - the maps in either <B>this</B> or <B>msb</B>
     * @throws NullPointerException msb is null
     * @return Mapset of RestrictionMaps in this or in msb Mapset.
     */
    public Mapset MapsetOr(Mapset msb)
            throws NullPointerException {
        if (msb == null) {
            throw new NullPointerException("msb is null");
        }

        Mapset newms = new Mapset();
        
        for (String mapName : mp_RestrictionMaps.keySet()) {            
            newms.addRestrictionMap((RestrictionMap) mp_RestrictionMaps.get(mapName));
        }

        for (String mapName : msb.mp_RestrictionMaps.keySet()) {            
            newms.addRestrictionMap((RestrictionMap) msb.mp_RestrictionMaps.get(mapName));
        }

        return newms;
    }

    /**
     * This method returns a Mapset of RestrictionMaps that are not in the
     * Mapset passed into this method.
     *
     * @param msb - used in this AND msb or the maps in <B>this</B> that ARE in
     * <B>msb</B>
     * @throws Exception if ms is null or other error.
     * @return Mapset of RestrictionMaps in msb and this Mapset.
     */
    public Mapset MapsetAnd(Mapset msb)
            throws Exception {
        if (msb == null) {
            throw new Exception("msb is null");
        }

        Mapset newms = new Mapset();        

        for (String mapName : mp_RestrictionMaps.keySet()) {            

            if (msb.containsRestrictionMap(mapName) == true) {
                newms.addRestrictionMap((RestrictionMap) mp_RestrictionMaps.get(mapName));
            }
        }
        return newms;
    }

    /**
     * This method returns true if map exists in Mapset.
     *
     * @return boolean of true if map exists in Mapset.
     */
    public boolean containsRestrictionMap(String mapName)
            throws NullPointerException {
        if (mapName == null) {
            throw new NullPointerException("mapName is null");
        }

        return mp_RestrictionMaps.containsKey(mapName);
    }

    /**
     * Gets all maps in Mapset and returns them in a Hashtable hashed by name of
     * map.
     *
     * @return Hashtable of RestrictionMaps hashed by map name
     */
    public Map<String, RestrictionMap> getRestrictionMaps() {
        return mp_RestrictionMaps;
    }

    /**
     * Gets stats on all maps in mapset.
     *
     * @return MapsetStats.
     */
    public MapsetStats getMapsetStats() {

        double totalsizekb = 0;
        long totalfrags = 0;
        long totalmolecules = 0;
        double maxmolkb = 0;
        double minmolkb = -1;
        double curmolsizekb = 0;

        for (RestrictionMap resmap : mp_RestrictionMaps.values()) {            

            curmolsizekb = ((double) resmap.getTotalMassInBP() / (double) 1000.0);

            totalsizekb += curmolsizekb;

            totalfrags += resmap.getNumberFragments();

            totalmolecules++;

            if (curmolsizekb > maxmolkb) {
                maxmolkb = curmolsizekb;
            }

            if (minmolkb == -1) {
                minmolkb = curmolsizekb;
            } else {
                if (curmolsizekb < minmolkb) {
                    minmolkb = curmolsizekb;
                }
            }
        }

        //okay lets fill the MapsetStats object and return it
        MapsetStats mstat = new MapsetStats();

        mstat.setAverageFragSizeKb((totalsizekb / (double) totalfrags));
        mstat.setAverageMoleculeSizeKb((totalsizekb / (double) totalmolecules));

        mstat.setMaxMoleculeSizeKb(maxmolkb);

        mstat.setMinMoleculeSizeKb(minmolkb);

        mstat.setNumberFragments(totalfrags);

        mstat.setNumberMolecules(totalmolecules);

        mstat.setTotalSizeKb(totalsizekb);

        return mstat;
    }

    /**
     * Generates a histogram of map sizes in mapset.
     *
     * @return Hashtable containing histogram of map sizes where the key is the
     * lower limit of bin and key+ binsize is upper limit.
     */
    public Hashtable getMapHistogram(int binSizeBp)
            throws Exception {
        Hashtable bins = new Hashtable();
        Integer CurBin = null;
        int counter = 0;        

        if (binSizeBp <= 0) {
            throw new Exception("Bin size must be larger then 0");
        }

        for ( RestrictionMap resmap : mp_RestrictionMaps.values()) {            

            Integer BinNum = new Integer((int) Math.ceil((double) resmap.getTotalMassInBP() / (double) binSizeBp));

            if (bins.containsKey(BinNum) == false) {
                counter = 0;
                CurBin = new Integer(counter);
                bins.put(BinNum, CurBin);
            }

            CurBin = (Integer) bins.get(BinNum);

            Integer NewVal = new Integer(CurBin.intValue() + 1);

            bins.put(BinNum, NewVal);
        }
        return bins;
    }

    /**
     * Generates a histogram of fragment sizes in mapset.
     *
     * @return Hashtable containing histogram of map sizes where the key is the
     * lower limit of bin and key+ binsize is upper limit.
     */
    public Hashtable getFragmentHistogram(int binSizeBp) throws Exception {        
        Vector fragments = null;
        RestrictionFragment frag = null;
        int counter = 0;
        Hashtable bins = new Hashtable();
        Integer CurBin = null;

        if (binSizeBp <= 0) {
            throw new Exception("Bin size must be larger then 0");
        }

        for (RestrictionMap resmap : mp_RestrictionMaps.values()) {            

            fragments = (Vector) resmap.getFragments();

            for (Enumeration f = fragments.elements(); f.hasMoreElements();) {
                frag = (RestrictionFragment) f.nextElement();

                Integer BinNum = new Integer((int) Math.ceil((double) frag.getMassInBp() / (double) binSizeBp));

                if (bins.containsKey(BinNum) == false) {
                    counter = 0;
                    CurBin = new Integer(counter);
                    bins.put(BinNum, CurBin);
                }

                CurBin = (Integer) bins.get(BinNum);

                Integer NewVal = new Integer(CurBin.intValue() + 1);

                bins.put(BinNum, NewVal);

            }
        }

        return bins;
    }

    public RestrictionMap getNextRestrictionMap() throws Exception {

        
        if (this.mp_RestrictionMaps == null || mp_RestrictionMaps.isEmpty() == true) {
            return null;
        }

        String curKey = mp_RestrictionMaps.keySet().iterator().next();
        if (curKey == null) {
            return null;
        }

        return (RestrictionMap) mp_RestrictionMaps.remove(curKey);
    }

    public void reset() throws java.io.IOException, NullPointerException {
    }

    public boolean resetSupported() {
        return false;
    }

}
