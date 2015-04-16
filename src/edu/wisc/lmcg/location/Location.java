////////////////////////////////////////////100 cols wide////////////////////////////////////////////
package edu.wisc.lmcg.location;

import java.util.Iterator;
import java.lang.String;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * This class defines a location on a genome. It contains a label, chromosome,
 * start position, and end position.
 * 
* revised June '13 by dmeyerson
 */
public class Location implements Cloneable {

    private String mp_Label;

    private String mp_Chromosome;
    private long mp_StartPos;
    private long mp_EndPos;
    private LinkedHashMap mp_Attributes;

    public Location() {
        mp_Chromosome = null;
        mp_StartPos = -1;
        mp_EndPos = -1;
        mp_Label = null;
        mp_Attributes = null;
    }

    /**
     * Determines if the chromosome, start,end, and label match then this
     * location is considered to be the same. NOTE THIS DOES NOT CHECK THE
     * ATTRIBUTES!!!
     *
     * @return true if the are the same false otherwise.
     */
    public boolean equals(final Location loc) {
        if (loc == null) {
            return false;
        }

        if (loc.mp_Chromosome == mp_Chromosome
                && loc.mp_StartPos == mp_StartPos
                && loc.mp_EndPos == mp_EndPos
                && loc.mp_Label == mp_Label) {
            return true;
        }
        return false;
    }

    public String getLocationAsString() {
        if (mp_Chromosome == null || mp_Label == null) {
            return null;
        }
        String locStr = mp_Label + "," + mp_Chromosome + "," + mp_StartPos + "," + mp_EndPos;

        return locStr;
    }

    public void setLabel(String label) {
        mp_Label = label;
    }

    public String getLabel() {
        return mp_Label;
    }

    public void setAttribute(String name, String val) {
        if (name == null || val == null) {
            return;
        }
        if (mp_Attributes == null) {
            mp_Attributes = new LinkedHashMap<String, String>();
        }

        mp_Attributes.put(name, val);
    }

    /**
     *
     * @return a long representing the coordinate, relative to the left side of
     * the aligned map, of the left side of the aligned region of that map
     * @throws Exception
     */
    public long get1stAlignedCut() throws Exception {
        String cut = (String) mp_Attributes.get("1staligncut");
        if (cut != null) {
            return Long.parseLong(cut);
        } else {
            throw new Exception("aint no 1st aligned cut");
        }
    }

    /**
     * @return a long representing the coordinate, right side of the aligned
     * map, of the right side of the aligned region of that aligned map
     * @throws Exception
     */
    public long getLastAlignedCut() throws Exception {
        String cut = (String) mp_Attributes.get("lastaligncut");
        if (cut != null) {
            return Long.parseLong(cut);
        } else {
            throw new Exception("aint no last aligned cut");
        }
    }

    public String getAttribute(String name) {
        if (mp_Attributes == null) {
            return null;
        }
        return (String) mp_Attributes.get(name);
    }

    /**
     * Gets the attribute keys
     *
     * @return Collection of attribute keys or null if none exist.
     */
    public Collection getAttributeKeys() {
        if (mp_Attributes == null) {
            return null;
        }
        return (Collection) mp_Attributes.keySet();
    }

    public long getStartPos() {

        return mp_StartPos;
    }

    public long getStartPosition() {
        return mp_StartPos;
    }

    public long getEndPos() {
        return mp_EndPos;
    }

    public long getEndPosition() {
        return mp_EndPos;
    }

    public String getChromosome() {

        return mp_Chromosome;
    }

    public void setStartPosition(long val) {
        mp_StartPos = val;
    }

    public void setStartPosition(String val) {

        mp_StartPos = Long.parseLong(val);
    }

    public void setEndPosition(long val) {
        mp_EndPos = val;
    }

    public void setEndPosition(String val) {
        mp_EndPos = Long.parseLong(val);
    }

    public void setChromosome(String chrom) {
        mp_Chromosome = chrom;
    }

    /**
     * returns amount of mass residing within locaiton specified.
     *
     * @return int representing base pairs within location passed in.
     */
    public long getTotalMassWithinLocation(Location loc)
            throws NullPointerException {
        if (loc == null) {
            throw new NullPointerException("Location passed in is null");
        }

        //if the chromosome is not set we will simply return 0
        if (mp_Chromosome == null
                || mp_StartPos == -1
                || mp_EndPos == -1) {
            return 0;
        }

        long locstart = 0;
        long locend = 0;

        if (loc.getChromosome().equals(mp_Chromosome) == true) {

            locstart = loc.getStartPos();
            locend = loc.getEndPos();

            if (locstart == -1 || locend == -1) {
                return 0;
            }

            if (mp_StartPos >= locstart && mp_StartPos <= locend) {
                if (mp_EndPos > locend) {
                    return locend - mp_StartPos;
                } else {
                    return mp_EndPos - mp_StartPos;
                }
            } else if (mp_EndPos >= locstart && mp_EndPos <= locend) {
                return mp_EndPos - locstart;
            } else if (mp_StartPos <= locstart && mp_EndPos >= locend) {
                return locend - locstart;
            }
        }

        return 0;
    }

    /**
     * Determines if two locations intersect.
     *
     * @return int representing base pairs within location passed in.
     */
    public boolean doLocationsIntersect(Location loc)
            throws NullPointerException {
        if (loc == null) {
            throw new NullPointerException("Location passed in is null");
        }

        long locstart = 0;
        long locend = 0;

        if (mp_Chromosome == null || loc.getChromosome() == null) {
            return false;
        }

        if (loc.getChromosome().equals(mp_Chromosome) == true) {
            locstart = loc.getStartPos();
            locend = loc.getEndPos();

            if (locstart == -1 || locend == -1) {
                return false;
            }

            if ((mp_StartPos >= locstart && mp_StartPos <= locend)
                    || (mp_EndPos >= locstart && mp_EndPos <= locend)
                    || (mp_StartPos <= locstart && mp_EndPos >= locend)) {
                return true;
            }
        }
        return false;
    }

    /**
     * makes a copy of the object
     */
    public Object clone() throws CloneNotSupportedException {
        Location newLoc = new Location();
        newLoc.mp_Chromosome = this.mp_Chromosome;
        newLoc.mp_EndPos = this.mp_EndPos;
        newLoc.mp_StartPos = this.mp_StartPos;
        newLoc.mp_Label = this.mp_Label;
        if (mp_Attributes != null) {
            newLoc.mp_Attributes = (LinkedHashMap) mp_Attributes.clone();
        }
        return newLoc;
    }

}
