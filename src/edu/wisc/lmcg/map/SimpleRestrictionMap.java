/*
 * SimpleRestrictionMap.java
 *
 * Created on September 7, 2004, 10:35 AM
 */
package edu.wisc.lmcg.map;

import edu.wisc.lmcg.alignment.FragAlignment;
import edu.wisc.lmcg.location.Location;
import edu.wisc.lmcg.map.restrictionmap.RestrictionMapUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author churas
 * @author dmeyerson (minor additions August 2013)
 */
public class SimpleRestrictionMap implements RestrictionMap {

    /**
     * Creates a new instance of SimpleRestrictionMap
     */
    private String mp_Name;
    private String mp_Type;
    private String mp_Enzyme;
    private List<RestrictionFragment> mp_Fragments;
    private String mp_MapBlock;
    private boolean mp_ForwardOriented;
    private double mapViewerX = -1, mapViewerY = -1; //coordinates used when displaying this map in gui.MapsPanel

    public SimpleRestrictionMap() {
        mp_Name = "";
        mp_Type = "opmap";
        mp_Enzyme = "";
        mp_Fragments = null;
        mp_MapBlock = null;
        mp_ForwardOriented = true;
    }

    public void setMapViewerX(double x) {
        mapViewerX = x;
    }

    public void setMapViewerY(double y) {
        mapViewerY = y;
    }

    public double getMapViewerX() {
        return mapViewerX;
    }

    public double getMapViewerY() {
        return mapViewerY;
    }

    public void addRestrictionFragment(RestrictionFragment resfrag)
            throws NullPointerException {
        if (mp_Fragments == null) {
            mp_Fragments = new Vector();
        }
        if (resfrag == null) {
            throw new NullPointerException("resfrag is null in addRestrictionFragment method");
        }

        mp_Fragments.add(resfrag);
    }

    /**
     * This method tells object the orientation of RestrictionMap.
     *
     * @param orientation - String that should contain either N or R. where N
     * means normal orientation and R means reversed.
     *
     */
    public void setOrientation(String orientation) {
        if (orientation == null) {
            return;
        }

        if (orientation.equals("N")
                || orientation.equals("n")) {
            mp_ForwardOriented = true;
            return;
        }

        if (orientation.equals("R")
                || orientation.equals("r")) {
            mp_ForwardOriented = false;
            return;
        }
    }

    public boolean isMapForwardOriented() {
        return mp_ForwardOriented;
    }

    public boolean isForwardOriented() {
        return mp_ForwardOriented;
    }

    public void setName(String name) {
        mp_Name = name;
    }

    /**
     * sets the name of the map.
     */
    public void setType(String type) {
        mp_Type = type;
    }

    public String getName() {
        return mp_Name;
    }

    /**
     * This method gets the type of map.
     *
     * @return String containing type of map.
     */
    public String getType() {
        return mp_Type;
    }

    /**
     * Lets caller set the Enzyme.
     *
     * @param Enzyme - value to set Enzyme to.
     */
    public void setEnzyme(String Enzyme) {
        mp_Enzyme = Enzyme;
    }

    /**
     * This method gets the Enzyme specified for the map.
     *
     * @return String containing Enzyme like XhoI.
     */
    public String getEnzyme() {
        return mp_Enzyme;
    }

    /**
     * This method should also be redone.
     */
    public void addMapBlock(String curBlock) //    throws Exception
    {
        if (curBlock == null) {
            return;
        }

        if (mp_MapBlock == null) {
            mp_MapBlock = new String(curBlock);
        } else {
            mp_MapBlock += curBlock;
        }
    }

    /**
     * This method should be redone it is confusing.
     */
    public void setMapBlock(String mapBlock)
            throws Exception {
        try {
            StringTokenizer st = null;

            if (mapBlock == null && mp_MapBlock != null) {
                st = new StringTokenizer(mp_MapBlock);
            } else if (mapBlock != null) {
                st = new StringTokenizer(mapBlock);
            } else {
                return;
            }

            mp_Fragments = new Vector();
            RestrictionFragment frag = null;
            String curToken = null;
            String justmass = null;

            int locOflc = -1;
            int locOflm = -1;
            int locOfls = -1;

            int locOfW = -1;
            int locOfL = -1;
            int locOfC = -1;
            int locOfX = -1;
            int locOfF = -1;

            while (st.hasMoreTokens()) {
                curToken = st.nextToken();

                if (curToken != null) {
                    if (curToken.length() > 0) {
                        locOflc = curToken.indexOf('c');
                        locOflm = curToken.indexOf('m');
                        locOfls = curToken.indexOf('s');

                        locOfW = curToken.indexOf('W');
                        locOfL = curToken.indexOf('L');
                        locOfC = curToken.indexOf('C');
                        locOfX = curToken.indexOf('X');
                        locOfF = curToken.indexOf('F');

                        justmass = null;

                        //depending on format of map there are lots
                        //of possible tags in the map format
                        //so lets check them first to get mass
                        if (locOflc != -1) {
                            justmass = curToken.substring(0, locOflc);
                        } else if (locOflm != -1) {
                            justmass = curToken.substring(0, locOflm);
                        } else if (locOfW != -1) {
                            justmass = curToken.substring(0, locOfW);
                        } else {
                            //turns out genspect outputs the mass and then a s
                            //if a user uses that program to save an alignment
                            if (locOfls != -1) {
                                justmass = curToken.substring(0, locOfls);
                            } else {
                                justmass = curToken;
                            }
                        }

                        if (justmass != null && justmass.length() > 0) {
                            frag = new RestrictionFragment();

                            frag.setMassInKb(justmass);

                            if (locOflc != -1) {
                                if (locOflm != -1) {
                                    frag.setContigCutConfidence(curToken.substring(locOflc + 1, locOflm));
                                } else if (locOfls != -1) {
                                    frag.setContigCutConfidence(curToken.substring(locOflc + 1, locOfls));
                                } else {
                                    frag.setContigCutConfidence(curToken.substring(locOflc + 1));
                                }

                            }

                            if (locOfW != -1 && locOfL != -1) {
                                frag.setStandardWeight(curToken.substring(locOfW + 1, locOfL));
                            }

                            if (locOfL != -1 && locOfC != -1) {
                                frag.setLengthInPixels(curToken.substring(locOfL + 1, locOfC));
                            }

                            if (locOfC != -1) {
                                if (locOfX != -1) {
                                    frag.setLengthBasedMassInKb(curToken.substring(locOfC + 1, locOfX));
                                } else {
                                    frag.setLengthBasedMassInKb(curToken.substring(locOfC + 1));
                                }

                            }

                            if (locOfX != -1) {
                                if (locOfF != -1) {
                                    frag.setMarkupCutConfidence(curToken.substring(locOfX + 1, locOfF));
                                } else {
                                    frag.setMarkupCutConfidence(curToken.substring(locOfX + 1));
                                }
                            }

                            if (locOfF != -1) {
                                frag.setLengthInPixelsFlagged(curToken.substring(locOfF + 1));
                            }

                            mp_Fragments.add(frag);
                            frag = null;
                        }
                    }
                }
            }

            if (mp_MapBlock != null) {
                mp_MapBlock = null;
            }
        } catch (Exception ex) {
            mp_Fragments.clear();
            mp_Fragments = null;
            mp_MapBlock = null;
            throw new Exception(ex);
        }
    }

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
    public String getMapInTabFormat() {
        if (mp_Fragments == null || mp_Fragments.size() == 0) {
            return null;
        }

        String tabStr = new String();

        tabStr = mp_Name + "\n\t" + mp_Enzyme + "\t" + mp_Enzyme.charAt(0);
        RestrictionFragment curFrag = null;

        for (int i = 0; i < mp_Fragments.size(); i++) {
            curFrag = (RestrictionFragment) mp_Fragments.get(i);

            //tabStr += "\t"+Float.toString((float)curFrag.getMassInBp()/(float)1000.0);
            tabStr += "\t" + curFrag.getFragmentInTextBlock();

        }
        tabStr += "\n"; //TODO does this fix the problem??

        return tabStr;
    }

    /**
     * Outputs map with fragments below minconfidence merged together.
     *
     * @return String containing map in OM map format.
     */
    public String getMapInTabFormatWithMinConfidence(double minconfidence) {
        if (mp_Fragments == null || mp_Fragments.size() == 0) {
            return null;
        }

        String tabStr = new String();

        tabStr = mp_Name + "\n\t" + mp_Enzyme + "\t" + mp_Enzyme.charAt(0);
        RestrictionFragment curFrag = null;

        int curfragmass = 0;

        for (int i = 0; i < mp_Fragments.size(); i++) {
            curFrag = (RestrictionFragment) mp_Fragments.get(i);

            if (curfragmass == 0) {
                curfragmass = curFrag.getMassInBp();
                continue;
            }

            if (curFrag.getContigCutConfidence() < minconfidence) {
                curfragmass += curFrag.getMassInBp();
                continue;
            }

            tabStr += "\t" + Float.toString(((float) curfragmass / (float) 1000.0));
            curfragmass = curFrag.getMassInBp();
        }

        if (curfragmass > 0) {
            tabStr += "\t" + Float.toString(((float) curfragmass / (float) 1000.0));
        }

        tabStr += "\n\n";

        return tabStr;
    }

    /**
     * This method informs the object to parse the map from the map block This
     * method should be called after all of the contents of the map_block have
     * been passed into this object.
     */
    public void parseMapBlock()
            throws Exception {
        //finally we can parse the map block to get fragments :)
        setMapBlock(null);
        mp_MapBlock = null;

    }

    /**
     * This method checks for omdb: identifier at the start of the map name if
     * found it then pulls out the omm database identifier. The format is as
     * follows: omdb:#:... where # is the database identifier.
     *
     * @return String containing identifier or null if no map name or identifier
     * is not found.
     */
    public String getOmmdbId() {
        return RestrictionMapUtil.getOmdbIdFromMapName(mp_Name);
    }

    /**
     * This method removes the omdb:#: prefix if it exists in map name
     *
     * @return String containing map name without omdb:#: prefix.
     */
    public String getOmmdbIdStrippedName() {
        return RestrictionMapUtil.getOmdbStrippedMapName(mp_Name);
    }

    /**
     * Parses GroupID from map name. This method strips away omdb:.*: prefix and
     * takes all characters before _ as groupid.
     *
     * @return String containing GroupID if found.
     */
    public String getGroupIdFromMapName() {
        return RestrictionMapUtil.getGroupIdFromMapName(mp_Name);
    }

    /**
     * This method gets the location of an insilicomap in the genome it belongs
     * to.
     *
     * @deprecated Check back for new objects to do this.
     * @param dbcon - database connection connected to omm database.
     * @return Location.
     */
    public Location getInsilicoMapLocation(Connection dbcon)
            throws Exception {
        //this method gets the location of the restriction map if its
        //known by seeing if map is actually an insilicomap
        checkConnection(dbcon);

        Statement stmt = null;
        ResultSet rs = null;

        String omdbid = getOmmdbId();

        if (omdbid == null) {
            return null;
        }

        Location theLoc = null;

        String query = "SELECT seq_source.chromosome,seq_contig.startpos,seq_contig.endpos,insilicorun.start_offset FROM restriction_maps,insilicorun,seq_contig,seq_source WHERE restriction_maps.restriction_maps_id='" + omdbid + "' and restriction_maps.type='insil' AND restriction_maps.insilicorun_id=insilicorun.insilicorun_id AND insilicorun.seq_contig_id=seq_contig.seq_contig_id AND seq_contig.seq_source_id=seq_source.seq_source_id";

        try {
            stmt = dbcon.createStatement();
            rs = stmt.executeQuery(query);

            if (rs != null) {
                if (rs.next() != false) {
                    theLoc = new Location();
                    theLoc.setChromosome(rs.getString(1));
                    theLoc.setStartPosition(rs.getInt(2) + rs.getInt(4));
                    theLoc.setEndPosition(rs.getInt(3));
                }

            }

        } catch (Exception ex) {
            throw new Exception(ex);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }

        return theLoc;
    }

    /**
     * This method just verifies the database connection and should really be
     * internal.
     *
     * @throws Exception if there is a database problem.
     */
    public void checkConnection(Connection dbcon)
            throws Exception {
        if (dbcon == null) {
            throw new Exception("database connection is null");
        }
    }

    /**
     * This method gets total mass of map in base pairs.
     *
     * @return int containing total mass of map in base pairs.
     */
    public int getTotalMassInBP() {
        int totalmass = 0;
        RestrictionFragment frag = null;

        if (mp_Fragments != null && mp_Fragments.isEmpty() == false) {
            for (int i = 0; i < mp_Fragments.size(); i++) {
                frag = mp_Fragments.get(i);
                totalmass += frag.getMassInBp();
            }
        }
        return totalmass;
    }

    /**
     * see getTotalMassInBPLeftOfIndex with reOrientMapForward flag set to
     * false.
     */
    public int getTotalMassInBPLeftOfIndex(FragAlignment fragAlign)
            throws Exception {
        return getTotalMassInBPLeftOfIndex(fragAlign, false);
    }

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
            boolean reOrientMapForward)
            throws Exception {
        int totalmass = 0;
        RestrictionFragment frag = null;
        int totalfrags = 0;

        if (fragAlign == null) {
            throw new Exception("FragAlignment passed in was null");
        }
        int numfragstosum = 0;

        //	if (reOrientMapForward == true && mp_ForwardOriented == false){
        numfragstosum = fragAlign.getLeftAlignment();

        if (numfragstosum <= -1) {
            numfragstosum = fragAlign.getRightAlignment();
        }

        if (numfragstosum <= -1) {
            throw new Exception("FragAlignment index specified is less then 0");
        }

        if (mp_Fragments != null && mp_Fragments.isEmpty() == false) {

            if (numfragstosum > mp_Fragments.size()) {
                throw new Exception("FragAlignment index is greater then length of molecule");
            }

            totalfrags = mp_Fragments.size();

            if (reOrientMapForward == true && mp_ForwardOriented == false) {
                for (int i = totalfrags - numfragstosum;
                        i < totalfrags; i++) {
                    frag = mp_Fragments.get(i);
                    totalmass += frag.getMassInBp();
                }
            } else {
                for (int i = 0; i < numfragstosum; i++) {
                    frag = (RestrictionFragment) mp_Fragments.get(i);
                    totalmass += frag.getMassInBp();
                }
            }

        }
        return totalmass;
    }

    /**
     * This method sums up mass of the fragments to the left of the index passed
     * in. The orientation of the map does not effect the result of this method
     * only calling reverse does.
     *
     * @param index - valid values are 0 (just returns 0) to number of fragments
     * fragments to the LEFT of this fragment index will be summed up.
     *
     * @throws Exception If index less then 0
     * @throws Exception If map contains no fragments
     * @throws Exception If index is greater then number of fragments in map
     * @return int containing mass in basepairse to left of fragment
     * corresponding to index.
     */
    public int getTotalMassInBPLeftOfIndex(final int index) throws Exception {
        int totalmass = 0;
        RestrictionFragment frag = null;

        if (index < 0) {
            throw new Exception("index less then zero for map: " + this.mp_Name);
        }

        if (mp_Fragments == null || mp_Fragments.isEmpty() == true) {
            throw new Exception("map: " + this.mp_Name + " contains no fragments");
        }

        if (index > mp_Fragments.size()) {
            throw new Exception("index of: " + index + " is greater then length of molecule: " + mp_Fragments.size() + " for map: " + mp_Name);
        }

        for (int i = 0; i < index; i++) {
            frag = mp_Fragments.get(i);
            totalmass += frag.getMassInBp();
        }
        return totalmass;
    }

    /**
     * This method sums up mass of the fragments to the right of the index
     * passed in. The orientation of the map does not effect the result of this
     * method only calling reverse does.
     *
     * @param index - valid values are -1 (to size whole map) to number of
     * fragments -1 fragments to the RIGHT of this fragment index will be summed
     * up.
     *
     * @throws Exception If index less then -1
     * @throws Exception If map contains no fragments
     * @throws Exception If index is greater then or equal to number of
     * fragments in map
     * @return int containing mass in basepairse to right of fragment
     * corresponding to index.
     */
    public int getTotalMassInBPRightOfIndex(int index)
            throws Exception {
        int totalmass = 0;
        RestrictionFragment frag = null;

        if (index < -1) {
            throw new Exception("index less then -1 for map: " + mp_Name);
        }

        if (mp_Fragments == null || mp_Fragments.isEmpty() == true) {
            throw new Exception("map: " + mp_Name + " contains no fragments");
        }

        if (index >= mp_Fragments.size()) {
            throw new Exception("index of: " + index + " is equal or greater then length of molecule: " + mp_Fragments.size() + " for map: " + mp_Name);
        }

        for (int i = mp_Fragments.size() - 1; i > index; i--) {
            frag = mp_Fragments.get(i);
            totalmass += frag.getMassInBp();
        }

        return totalmass;
    }

    /**
     * see getTotalMassInBPRightOfIndex with reOrientMapForward flag set to
     * false.
     */
    public int getTotalMassInBPRightOfIndex(FragAlignment fragAlign)
            throws Exception {
        return getTotalMassInBPRightOfIndex(fragAlign, false);
    }

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
            boolean reOrientMapForward)
            throws Exception {

        int totalmass = 0;
        RestrictionFragment frag = null;

        if (fragAlign == null) {
            throw new Exception("FragAlignment passed in was null");
        }

        int startindex = fragAlign.getRightAlignment();

        if (startindex == -1) {
            startindex = fragAlign.getLeftAlignment();
        }

        //move to the fragment just to the right
        startindex++;

        if (startindex == -1) {
            startindex = 0;
        }

        if (startindex > mp_Fragments.size()) {
            throw new Exception("Index to start summing up mass is greater then the number of fragments");
        }

        if (mp_Fragments != null && mp_Fragments.isEmpty() == false) {

            int totalfrags = mp_Fragments.size();

            if (reOrientMapForward == true && mp_ForwardOriented == false) {

                for (int i = (totalfrags - startindex - 1);
                        i >= 0; i--) {
                    frag = (RestrictionFragment) mp_Fragments.get(i);
                    totalmass += frag.getMassInBp();
                }
            } else {
                for (int i = startindex; i < mp_Fragments.size(); i++) {
                    frag = (RestrictionFragment) mp_Fragments.get(i);
                    totalmass += frag.getMassInBp();
                }
            }

        }
        return totalmass;
    }

    /**
     * This method returns the number of fragments in the Restriction Map.
     *
     * @return int containing number of fragments.
     */
    public int getNumberFragments() {
        if (mp_Fragments == null || mp_Fragments.isEmpty() == true) {
            return 0;
        }

        return mp_Fragments.size();
    }

    /**
     * This method gets all the fragments in the RestrictionMap.
     *
     * @return Vector containing RestrictionFragment objects.
     */
    public List<RestrictionFragment> getFragments() {

        return mp_Fragments;
    }

    /**
     * This method will output the map with fragments that have less then
     * minconfidence contig cut confidence merged together.
     *
     * @return Vector of RestrictionFragments.
     */
    public Vector getFragmentsWithMinConfidence(double minconfidence) {
        if (mp_Fragments == null || mp_Fragments.size() == 0) {
            return null;
        }

        RestrictionFragment curFrag = null;
        RestrictionFragment newFrag = null;
        int curfragmass = 0;
        Vector newFrags = new Vector();

        for (int i = 0; i < mp_Fragments.size(); i++) {
            curFrag = (RestrictionFragment) mp_Fragments.get(i);

            if (curfragmass == 0) {
                curfragmass = curFrag.getMassInBp();
                continue;
            }

            if (curFrag.getContigCutConfidence() < minconfidence) {
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

        return newFrags;
    }

    public boolean isMapWellFormed() {
        return !containsNegativeOrZeroMassFragments();
    }

    /**
     * This method was added as a double check to ensure no 0 mass fragments or
     * negative fragments exist in map.
     *
     * @return true if there are fragments with negative or zero mass.
     */
    public boolean containsNegativeOrZeroMassFragments() {
        RestrictionFragment frag = null;
        boolean hasnegativeorzerofrag = false;

        if (mp_Fragments != null && mp_Fragments.isEmpty() == false) {
            for (int i = 0; i < mp_Fragments.size(); i++) {
                frag = mp_Fragments.get(i);
                if (frag.getMassInBp() <= 0.000) {
                    hasnegativeorzerofrag = true;
                    break;
                }

            }
        }
        return hasnegativeorzerofrag;
    }

    public int getMassInBpOfLeftMostAlignedFrag(FragAlignment fragAlign)
            throws Exception {
        return getMassInBpOfLeftMostAlignedFrag(fragAlign, false);
    }

    /**
     * This method gets the mass of leftmost aligned fragment from
     * FragAlignment.
     *
     * @param fragAlign - FragAlignment containing index of fragment to obtain
     * mass from.
     * @return int mass of left most alignment in FragAlignment in base pairs.
     */
    public int getMassInBpOfLeftMostAlignedFrag(FragAlignment fragAlign,
            boolean reOrientMapForward)
            throws Exception {
        if (fragAlign == null) {
            throw new Exception("FragAlignment passed in was null");
        }

        int fragindex = fragAlign.getLeftAlignment();

        if (fragindex <= -1) {
            fragindex = fragAlign.getRightAlignment();
        }

        if (fragindex <= -1) {
            return -1;
        }

        //okay lets get the fragment with the fragindex and return its
        //mass in base pairs
        if (mp_Fragments == null || mp_Fragments.isEmpty()
                || mp_Fragments.size() <= fragindex) {
            return -1;
        }

        RestrictionFragment frag = null;

        if (reOrientMapForward == true && mp_ForwardOriented == false) {
            frag = mp_Fragments.get(mp_Fragments.size() - 1 - fragindex);

        } else {
            frag = mp_Fragments.get(fragindex);
        }

        if (frag == null) {
            return -1;
        }

        return frag.getMassInBp();
    }

    public int getMassInBpOfRightMostAlignedFrag(FragAlignment fragAlign)
            throws Exception {
        return getMassInBpOfRightMostAlignedFrag(fragAlign, false);
    }

    /**
     * This method gets the mass of leftmost aligned fragment from
     * FragAlignment.
     *
     * @param fragAlign - FragAlignment containing index of fragment to obtain
     * mass from.
     * @return int mass of left most alignment in FragAlignment in base pairs.
     */
    public int getMassInBpOfRightMostAlignedFrag(FragAlignment fragAlign,
            boolean reOrientMapForward)
            throws Exception {
        if (fragAlign == null) {
            throw new Exception("FragAlignment passed in was null");
        }

        int fragindex = fragAlign.getRightAlignment();

        if (fragindex <= -1) {
            fragindex = fragAlign.getLeftAlignment();
        }

        if (fragindex <= -1) {
            return -1;
        }

        //okay lets get the fragment with the fragindex and return its
        //mass in base pairs
        if (mp_Fragments == null || mp_Fragments.isEmpty()
                || mp_Fragments.size() <= fragindex) {
            return -1;
        }

        RestrictionFragment frag = null;

        if (reOrientMapForward == true && mp_ForwardOriented == false) {
            frag = mp_Fragments.get(mp_Fragments.size() - 1 - fragindex);

        } else {
            frag = mp_Fragments.get(fragindex);
        }

        if (frag == null) {
            return -1;
        }

        return frag.getMassInBp();
    }

    /**
     * Concatenates the Fragments of cmap to this map.
     * <B> Note both maps must have same enzyme</B>
     *
     * @return RestrictionMap containing concatenated fragments.
     */
    public RestrictionMap concat(RestrictionMap cmap)
            throws Exception {
        if (cmap == null) {
            throw new NullPointerException("cmap was null");
        }

        if (mp_Enzyme.equals("")
                || cmap.getEnzyme().equals("")) {
            throw new Exception("Enzyme was not set in one or both restriction maps");
        }

        if (!mp_Enzyme.equals(cmap.getEnzyme())) {
            throw new Exception("Enzyme differs between Restriction Maps");
        }

        SimpleRestrictionMap concatmaps = new SimpleRestrictionMap();

        concatmaps.setName(mp_Name + "_" + cmap.getName());

        concatmaps.setEnzyme(mp_Enzyme);

        concatmaps.mp_Fragments = new Vector();

        RestrictionFragment resfrag = null;

        for ( RestrictionFragment resfragment : mp_Fragments) {
            concatmaps.mp_Fragments.add(resfragment);
        }
        List<RestrictionFragment> cmapfrags = cmap.getFragments();
        
        for ( RestrictionFragment rf : cmapfrags) {
            concatmaps.mp_Fragments.add(rf);
        }
        return concatmaps;
    }

    /**
     * This method reverses the fragment of this map and switches the
     * orientation to the opposite of what it is.
     *
     * @return true if the operation was successful.
     */
    public boolean reverseMap()
            throws Exception {
        if (mp_Fragments == null) {
            throw new NullPointerException("There are no fragments to reverse in this map");
        }

        //need to create a temp Vector and fill it reverse order and
        //then set mp_Fragments to this vector.
        RestrictionFragment frag = null;
        Vector reverseFrags = new Vector();

        for (int i = mp_Fragments.size() - 1; i >= 0; i--) {
            frag = mp_Fragments.get(i);
            reverseFrags.add(frag);
        }
        mp_Fragments.clear();

        mp_Fragments = reverseFrags;

        if (mp_ForwardOriented == true) {
            mp_ForwardOriented = false;
        } else {
            mp_ForwardOriented = true;
        }

        return true;
    }

    /**
     * This method attempts to remove massinbases base pairs from end of
     * RestrictionMap. This method only removes fragments so it may remove less
     * but never more then the value specified.
     *
     * @return true if operation was successful or false if no fragments were
     * removed or if the value exceeds the mass of the map.
     */
    public boolean deleteMassFromEndOfMap(int massinbases)
            throws Exception {
        if (mp_Fragments == null) {
            throw new NullPointerException("There are no fragments in this map");
        }

        if (massinbases <= 0) {
            throw new Exception("Mass to delete must be greater then zero.");
        }

        RestrictionFragment frag = null;
        int totalmass = 0;
        boolean removeok = false;
        int i = 0;
        Vector tempfrags = new Vector();

        for (i = mp_Fragments.size() - 1; i >= 0; i--) {
            frag = mp_Fragments.get(i);
            totalmass += frag.getMassInBp();

            if (totalmass >= massinbases) {
                if (totalmass == massinbases) {
                    tempfrags.add(frag);
                }
                removeok = true;
                break;
            }
            tempfrags.add(frag);
        }

        if (removeok == true) {
            //need to remove the ending fragments from vector
            //and return true
            mp_Fragments.removeAll(tempfrags);
            return true;
        }

        return false;
    }

    /**
     * Creates a subset map from starting base pair offset to ending base pair
     * offset.
     *
     * @return RestrictionMap.
     */
    public RestrictionMap getSubsetMap(int startbase,
            int endbase)
            throws Exception {
        if (mp_Fragments == null) {
            throw new NullPointerException("there are no fragments in map");
        }

        if (endbase < 0 || startbase < 0) {
            throw new Exception("the start or end base position is less then 0");
        }

        if (endbase - startbase <= 0) {
            throw new Exception("the subset length specified is zero or less.");
        }

        RestrictionFragment frag = null;
        int totalmass = 0;
        boolean removeok = false;

        SimpleRestrictionMap newmap = new SimpleRestrictionMap();

        newmap.setName(mp_Name);
        newmap.setEnzyme(mp_Enzyme);
        newmap.mp_ForwardOriented = mp_ForwardOriented;

        for ( RestrictionFragment fragg : mp_Fragments) {
            
            totalmass += fragg.getMassInBp();

            if (totalmass >= startbase
                    && totalmass <= endbase) {
                if (newmap.mp_Fragments == null) {
                    newmap.mp_Fragments = new Vector();
                }
                newmap.mp_Fragments.add(fragg);
            }
        }
        return newmap;
    }

    /**
     * Gets the mass including and between fragment indexes.
     *
     * @param startindex
     * @param endindex
     * @return mass in base pairs.
     */
    @Override
    public int getMassInBpBetweenFragmentIndexes(int startindex, int endindex)
            throws Exception {
        if (mp_Fragments == null) {
            throw new NullPointerException("There are no fragments in the map");
        }

        if (startindex < 0) {
            throw new IndexOutOfBoundsException("negative start index value");
        }

        if (startindex > endindex) {
            throw new IndexOutOfBoundsException("start index greater then end index start: " + startindex + " and end index: " + endindex + " for map: " + mp_Name);
        }

        if (endindex >= mp_Fragments.size()) {
            throw new IndexOutOfBoundsException("end index greater then map size");
        }

        int totalmass = 0;
        RestrictionFragment frag = null;

        for (int i = startindex; i <= endindex; i++) {
            frag = mp_Fragments.get(i);
            totalmass += frag.getMassInBp();
        }

        return totalmass;
    }

    /**
     * This method returns a xml string representation of this restriction map
     *
     * @return String containing xml representation of RestrictionMap
     */
    public String getRestrictionMapAsXml()
            throws NullPointerException {
        /**
         * <restriction_map>
         * <type>consensus</type>
         * <name>omdb:36809891:gi|37539904|ref|NT_021937.16|Hs1_22093</name>
         * <circular>false</circular>
         * <orientation>N</orientation>
         * <num_frags>270</num_frags>
         * <enzymes>SwaI</enzymes>
         * <map_block></map_block>
         * </restriction_map>
         */
        if (mp_Fragments == null) {
            throw new NullPointerException("No fragments in this map");
        }

        String resMapStr = "<restriction_map>\n\t<type>" + mp_Type + "</type>\n\t<name>" + mp_Name + "</name>\n\t<circular>false</circular>\n";

        if (mp_ForwardOriented == true) {
            resMapStr += "\t<orientation>N</orientation>\n";
        } else {
            resMapStr += "\t<orientation>R</orientation>\n";
        }

        resMapStr += "\t<num_frags>" + mp_Fragments.size() + "</num_frags>\n"
                + "\t<enzymes>" + mp_Enzyme + "</enzymes>\n"
                + "\t<mapviewerx>" + mapViewerX + "</mapviewerx>\n"
                + "\t<mapviewery>" + mapViewerY + "</mapviewery>\n"
                + "\t<map_block>";

        RestrictionFragment curFrag = null;

        if (mp_Fragments != null) {
            for (int i = 0; i < mp_Fragments.size(); i++) {
                curFrag = (RestrictionFragment) mp_Fragments.get(i);

                if (i == 0) {
                    resMapStr += curFrag.getFragmentInTextBlock();
                } else {
                    resMapStr += " " + curFrag.getFragmentInTextBlock();
                }
            }
        }

        resMapStr += "</map_block>\n</restriction_map>\n";
        return resMapStr;
    }

    public Hashtable getIndexShiftFromChangingMinCutConfidence(double minConfidence) {

        if (mp_Fragments == null || mp_Fragments.size() == 0) {
            return null;
        }
        Hashtable shiftHash = new Hashtable();
        RestrictionFragment curFrag = null;
        RestrictionFragment prevFrag = null;

        int adjustedIndex = 0;
        for (int i = 0; i < mp_Fragments.size(); i++) {
            curFrag = (RestrictionFragment) mp_Fragments.get(i);
            //gotta have a cut confidence above threshold and the fragment must have
            //positive mass
            if (curFrag.getContigCutConfidence() >= minConfidence && i > 0) {
                adjustedIndex++;
            }

            if (prevFrag != null && prevFrag.getMassInBp() <= 0) {
                adjustedIndex--;
                if (adjustedIndex < 0) {
                    adjustedIndex = 0;
                }
            }
            shiftHash.put(new Integer(i), new Integer(adjustedIndex));
            prevFrag = curFrag;
        }
        return shiftHash;
    }

    /**
     * Sets the fragments for this map. Please note it is up to the caller to
     * adjust orientation.
     *
     * @param frags - Vector of RestricionFragment objects the or in the vector
     * determines the order of the map
     */
    public void setFragments(Vector frags) {
        mp_Fragments = frags;
    }
}
