package edu.wisc.lmcg.alignment.xml.parsers;

import java.util.*;
import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.*;

import edu.wisc.lmcg.map.*;
import edu.wisc.lmcg.alignment.contigrun.ContigRun;
import edu.wisc.lmcg.alignment.contigrun.StandardContigRun;
import edu.wisc.lmcg.alignment.mapalignment.MapAlignment;
import edu.wisc.lmcg.alignment.mapalignment.StandardMapAlignment;

public class AlignmentParser extends DefaultHandler {

    static private Writer out;

    protected int mp_State;

    final static protected int UNKNOWN = 0;
    final static protected int ALIGNED_MAPS_DOCUMENT = 1;
    static protected int EXPERIMENT = 100;
    static protected int EXPERIMENT_CREATOR = 101;
    static protected int EXPERIMENT_UUID = 102;

    static protected int RESTRICTION_MAP = 200;
    static protected int RESTRICTION_MAP_TYPE = 201;
    static protected int RESTRICTION_MAP_NAME = 202;
    static protected int RESTRICTION_MAP_ENZYMES = 203;
    static protected int RESTRICTION_MAP_ORIENTATION = 204;
    static protected int RESTRICTION_MAP_NUM_FRAGS = 205;
    static protected int RESTRICTION_MAP_MAP_BLOCK = 206;
    static protected int RESTRICTION_MAP_TVAL = 207;

    static protected int MAP_ALIGNMENT = 300;
    static protected int MAP_ALIGNMENT_UUID = 301;
    static protected int MAP_ALIGNMENT_SOMA_SCORE = 302;
    static protected int MAP_ALIGNMENT_SOMA_PVALUE = 303;
    static protected int MAP_ALIGNMENT_COUNT = 304;
    static protected int MAP_ALIGNMENT_REFERENCE_MAP = 305;
    static protected int MAP_ALIGNMENT_REFERENCE_MAP_NAME = 306;
    static protected int MAP_ALIGNMENT_ALIGNED_MAP = 307;
    static protected int MAP_ALIGNMENT_ALIGNED_MAP_NAME = 308;
    static protected int MAP_ALIGNMENT_ALIGNED_MAP_ORIENTATION = 309;
    static protected int MAP_ALIGNMENT_F = 310;
    static protected int MAP_ALIGNMENT_F_I = 311;
    static protected int MAP_ALIGNMENT_F_L = 312;
    static protected int MAP_ALIGNMENT_F_R = 313;

    //public static Vector mp_RestrictionMaps;
    //    public static Vector mp_MapAlignments;
    private ContigRun mp_ContigRun;
    private MapAlignment mp_CurrentAlignment;
    private String mp_CurrentLeftAlignment;
    private String mp_CurrentRightAlignment;
    private String mp_CurrentAlignmentIndex;
    private Mapset mp_Mapset;
    private List<AlignmentParseError> mp_AlignmentParserErrors;
    private double mp_Tval;

    private SimpleRestrictionMap mp_CurrentMap;

    private String mp_CharactersStr;

    public void AlignmentParser() {
        mp_State = UNKNOWN;
        mp_AlignmentParserErrors = null;
        mp_Mapset = null;

        mp_CharactersStr = "";

        //	mp_MapAlignments = null;
        mp_ContigRun = null;
        mp_Tval = -1.0;

        mp_CurrentAlignment = null;
        mp_CurrentMap = null;
        mp_CurrentLeftAlignment = null;
        mp_CurrentRightAlignment = null;
        mp_CurrentAlignmentIndex = null;

    }

    /**
     * If set to true then the parser will not throw exceptions but instead log
     * errors outside xml parse errors that can be later retrieved.
     *
     * @param val
     */
    public void logAlignmentParseErrors(boolean val) {
        if (val == true && mp_AlignmentParserErrors == null) {
            mp_AlignmentParserErrors = new ArrayList<>();
        } else {
            mp_AlignmentParserErrors = null;
        }
    }

    /**
     * Gets the alignment parse errors if logAlignmentParseErrors method was set
     * to true before parsing starts otherwise this method will return null.
     *
     * @return collection of AlignmentParseError objects or null if none exist
     * or if logging was not enabled.
     */
    public Collection getAlignmentParseErrors() {
        return mp_AlignmentParserErrors;
    }

    public static void main(String[] args) {
        DefaultHandler handler = new AlignmentParser();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {

            out = new OutputStreamWriter(System.out, "UTF8");
            SAXParser saxParser = factory.newSAXParser();

            Date startDate = new Date();

            //use FileInputStream to create GZIPInputStream to send to
            //sax parser
            saxParser.parse(new File(args[0]), handler);
            Date endDate = new Date();

            System.out.println("Parse Time: " + (((float) endDate.getTime() - (float) startDate.getTime()) / (float) 1000.0) + " Seconds");

        } catch (IOException t) {
            t.printStackTrace();
        } catch (ParserConfigurationException t) {
            t.printStackTrace();
        } catch (SAXException t) {
            t.printStackTrace();
        }

        System.exit(0);
    }

    public Mapset getAlignmentMapset() {
        return mp_Mapset;
    }

    public ContigRun getContigRun() {
        return mp_ContigRun;
    }

    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================
    
    /**
     * 
     * @throws SAXException 
     */
    @Override
    public void startDocument()
            throws SAXException {

        mp_State = UNKNOWN;
        //	mp_RestrictionMaps = new Vector();
        mp_Mapset = new Mapset();
        mp_ContigRun = new StandardContigRun();

        mp_Tval = -1.0;
        //	mp_MapAlignments = new Vector();
        mp_CurrentLeftAlignment = "";
        mp_CurrentRightAlignment = "";
        mp_CurrentAlignmentIndex = "";

        mp_CurrentMap = null;
    }

    /**
     * 
     * @throws SAXException 
     */
    @Override
    public void endDocument()
            throws SAXException {
        //we dont do anything write now.  should we?
        if (mp_ContigRun == null) {
            System.err.println("a contig run was never created!!!");
            return;
        }

        mp_ContigRun.setMapset(mp_Mapset);
    }

    /**
     * This method implements the startElement method of DefaultHandler class.
     * This method is called when a new tag is encountered in an xml document.
     * @param namespaceURI
     * @param lName
     * @param attrs
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void startElement(String namespaceURI,
            String lName, // local name
            String qName, // qualified name
            Attributes attrs)
            throws SAXException {

        mp_CharactersStr = "";

        if ("aligned_maps_document".equals(qName)) {
            mp_State = ALIGNED_MAPS_DOCUMENT;
        } else if ("experiment".equals(qName) && mp_State == ALIGNED_MAPS_DOCUMENT) {
            mp_State = EXPERIMENT;
        } else if ("creator".equals(qName) && mp_State == EXPERIMENT) {
            mp_State = EXPERIMENT_CREATOR;
        } else if ("uuid".equals(qName)) {
            if (mp_State == EXPERIMENT) {
                mp_State = EXPERIMENT_UUID;
            } else if (mp_State == MAP_ALIGNMENT) {
                mp_State = MAP_ALIGNMENT_UUID;
            }
        } else if ("restriction_map".equals(qName) && mp_State == ALIGNED_MAPS_DOCUMENT) {
            mp_State = RESTRICTION_MAP;
            mp_CurrentMap = new SimpleRestrictionMap();

        } else if ("map_alignment".equals(qName) && mp_State == ALIGNED_MAPS_DOCUMENT) {
            mp_State = MAP_ALIGNMENT;
            mp_CurrentAlignment = new StandardMapAlignment();
        } else if ("type".equals(qName) && mp_State == RESTRICTION_MAP) {
            mp_State = RESTRICTION_MAP_TYPE;
        } else if ("name".equals(qName)) {

            if (mp_State == RESTRICTION_MAP) {
                mp_State = RESTRICTION_MAP_NAME;
            } else if (mp_State == MAP_ALIGNMENT_REFERENCE_MAP) {
                mp_State = MAP_ALIGNMENT_REFERENCE_MAP_NAME;
            } else if (mp_State == MAP_ALIGNMENT_ALIGNED_MAP) {
                mp_State = MAP_ALIGNMENT_ALIGNED_MAP_NAME;
            }

        } else if ("map_block".equals(qName) && mp_State == RESTRICTION_MAP) {
            mp_State = RESTRICTION_MAP_MAP_BLOCK;
        } else if ("enzymes".equals(qName) && mp_State == RESTRICTION_MAP) {
            mp_State = RESTRICTION_MAP_ENZYMES;
        } else if ("orientation".equals(qName)) {
            if (mp_State == MAP_ALIGNMENT_ALIGNED_MAP) {
                mp_State = MAP_ALIGNMENT_ALIGNED_MAP_ORIENTATION;
            } else if (mp_State == RESTRICTION_MAP) {
                mp_State = RESTRICTION_MAP_ORIENTATION;
            }
        } else if ("f".equals(qName) && mp_State == MAP_ALIGNMENT) {
            mp_State = MAP_ALIGNMENT_F;
        } else if ("l".equals(qName) && mp_State == MAP_ALIGNMENT_F) {
            mp_State = MAP_ALIGNMENT_F_L;
        } else if ("r".equals(qName) && mp_State == MAP_ALIGNMENT_F) {
            mp_State = MAP_ALIGNMENT_F_R;
        } else if ("i".equals(qName) && mp_State == MAP_ALIGNMENT_F) {
            mp_State = MAP_ALIGNMENT_F_I;
        } else if ("reference_map".equals(qName) && mp_State == MAP_ALIGNMENT) {
            mp_State = MAP_ALIGNMENT_REFERENCE_MAP;
        } else if ("aligned_map".equals(qName) && mp_State == MAP_ALIGNMENT) {

            mp_State = MAP_ALIGNMENT_ALIGNED_MAP;
        } else if ("num_frags".equals(qName) && mp_State == RESTRICTION_MAP) {
            mp_State = RESTRICTION_MAP_NUM_FRAGS;
        } else if ("soma_score".equals(qName) && mp_State == MAP_ALIGNMENT) {
            mp_State = MAP_ALIGNMENT_SOMA_SCORE;
        } else if ("soma_pvalue".equals(qName) && mp_State == MAP_ALIGNMENT) {
            mp_State = MAP_ALIGNMENT_SOMA_PVALUE;
        } else if ("T-val".equals(qName) && mp_State == RESTRICTION_MAP) {
            mp_State = RESTRICTION_MAP_TVAL;
        }
        //@dmeyerson's addition
        //else if (qName == "MapViewerX") {}
    }

    @Override
    public void endElement(String namespaceURI,
            String sName, // simple name
            String qName // qualified name
    )
            throws SAXException {

        try {

            if ("aligned_maps_document".equals(qName)) {
                mp_State = UNKNOWN;
            } else if ("experiment".equals(qName)) {
                mp_State = ALIGNED_MAPS_DOCUMENT;
            } else if ("creator".equals(qName) && mp_State == EXPERIMENT_CREATOR) {
                mp_ContigRun.setCreator(mp_CharactersStr);
                mp_State = EXPERIMENT;
            } else if ("uuid".equals(qName)) {
                if (mp_State == EXPERIMENT_UUID) {
                    mp_ContigRun.addUUID(mp_CharactersStr);

                    mp_State = EXPERIMENT;
                } else if (mp_State == MAP_ALIGNMENT_UUID) {
                    mp_State = MAP_ALIGNMENT;
                }
            } else if (mp_State == EXPERIMENT) {
                //if we are in the experiment state and creator and uuid is not this
                //tag put it in the experiment hash :)
                mp_ContigRun.setExperimentProperty(qName, mp_CharactersStr);
            } else if ("restriction_map".equals(qName) && mp_State == RESTRICTION_MAP) {
                mp_State = ALIGNED_MAPS_DOCUMENT;
                mp_CurrentMap.parseMapBlock();
                if (mp_Mapset.addRestrictionMap(mp_CurrentMap) == false) {
                    if (this.mp_AlignmentParserErrors != null) {
                        AlignmentParseError ape = new AlignmentParseError();
                        ape.setMap(mp_CurrentMap);
                        ape.setMessage("Error adding map to the mapset the"
                                + " main causes for this are duplicate named"
                                + " maps or a null map or a map with no name");
                        mp_AlignmentParserErrors.add(ape);
                    } else {
                        throw new Exception("Error adding map named: "
                                + mp_CurrentMap.getName() + " main causes for"
                                + " this are duplicate named maps or a null map"
                                + " or a map with no name");
                    }
                }

                if (mp_Tval != -1.0) {
                    //we have a valid t value here lets add it to the
                    //contig via a method in contigrun.
                    mp_ContigRun.setTvalueForContig(mp_CurrentMap.getName(),
                            mp_Tval);

                    mp_Tval = -1.0;
                }
            } else if ("map_alignment".equals(qName)) {
                mp_State = ALIGNED_MAPS_DOCUMENT;

                mp_ContigRun.addMapAlignment(mp_CurrentAlignment);
                mp_CurrentAlignment = null;

                //mp_MapAlignments.add(mp_CurrentAlignment);
            } else if ("type".equals(qName) && mp_State == RESTRICTION_MAP_TYPE) {

                mp_CurrentMap.setType(mp_CharactersStr);

                mp_State = RESTRICTION_MAP;
            } else if ("name".equals(qName)) {
                if (mp_State == RESTRICTION_MAP_NAME) {
                    mp_CurrentMap.setName(mp_CharactersStr);

                    mp_State = RESTRICTION_MAP;
                } else if (mp_State == MAP_ALIGNMENT_ALIGNED_MAP_NAME) {
                    mp_CurrentAlignment.setAlignedMapName(mp_CharactersStr);
                    mp_State = MAP_ALIGNMENT_ALIGNED_MAP;
                } else if (mp_State == MAP_ALIGNMENT_REFERENCE_MAP_NAME) {
                    mp_CurrentAlignment.setReferenceMapName(mp_CharactersStr);
                    mp_State = MAP_ALIGNMENT_REFERENCE_MAP;
                }

            } else if ("map_block".equals(qName) && mp_State == RESTRICTION_MAP_MAP_BLOCK) {
                mp_CurrentMap.addMapBlock(mp_CharactersStr);
                mp_State = RESTRICTION_MAP;
            } else if ("enzymes".equals(qName) && mp_State == RESTRICTION_MAP_ENZYMES) {
                mp_CurrentMap.setEnzyme(mp_CharactersStr);

                mp_State = RESTRICTION_MAP;
            } else if ("orientation".equals(qName)) {
                if (mp_State == MAP_ALIGNMENT_ALIGNED_MAP_ORIENTATION) {
                    mp_CurrentAlignment.setOrientation(mp_CharactersStr);
                    mp_State = MAP_ALIGNMENT_ALIGNED_MAP;
                } else if (mp_State == RESTRICTION_MAP_ORIENTATION) {
                    mp_CurrentMap.setOrientation(mp_CharactersStr);
                    mp_State = RESTRICTION_MAP;
                }
            } else if ("f".equals(qName) && mp_State == MAP_ALIGNMENT_F) {
                mp_State = MAP_ALIGNMENT;
                //this is where we need to add a new alignment to the
                //map alignment object
                if (!(mp_CurrentLeftAlignment.equals("-1") && mp_CurrentRightAlignment.equals("-1"))) {
                    mp_CurrentAlignment.addAlignment(mp_CurrentAlignmentIndex,
                            mp_CurrentLeftAlignment,
                            mp_CurrentRightAlignment);
                }

                mp_CurrentAlignmentIndex = "";
                mp_CurrentLeftAlignment = "";
                mp_CurrentRightAlignment = "";
            } else if ("l".equals(qName) && mp_State == MAP_ALIGNMENT_F_L) {
                mp_State = MAP_ALIGNMENT_F;
            } else if ("r".equals(qName) && mp_State == MAP_ALIGNMENT_F_R) {
                mp_State = MAP_ALIGNMENT_F;
            } else if ("i".equals(qName) && mp_State == MAP_ALIGNMENT_F_I) {
                mp_State = MAP_ALIGNMENT_F;
            } else if ("num_frags".equals(qName) && mp_State == RESTRICTION_MAP_NUM_FRAGS) {
                mp_State = RESTRICTION_MAP;
            } else if ("reference_map".equals(qName) && mp_State == MAP_ALIGNMENT_REFERENCE_MAP) {
                mp_State = MAP_ALIGNMENT;
            } else if ("aligned_map".equals(qName) && mp_State == MAP_ALIGNMENT_ALIGNED_MAP) {
                mp_State = MAP_ALIGNMENT;
            } else if ("soma_score".equals(qName) && mp_State == MAP_ALIGNMENT_SOMA_SCORE) {
                mp_CurrentAlignment.setScore(mp_CharactersStr);
                mp_State = MAP_ALIGNMENT;
            } else if ("soma_pvalue".equals(qName) && mp_State == MAP_ALIGNMENT_SOMA_PVALUE) {
                mp_CurrentAlignment.setPvalue(mp_CharactersStr);

                mp_State = MAP_ALIGNMENT;
            } else if ("T-val".equals(qName) && mp_State == RESTRICTION_MAP_TVAL) {
                //need to set t value for current restriction map :)
                Double tval = new Double(mp_CharactersStr);
                mp_Tval = tval;
                mp_State = RESTRICTION_MAP;
            } //TODO: does this work??
            else if ("mapviewerx".equals(qName)) {                
                mp_CurrentMap.setMapViewerX(Double.parseDouble(mp_CharactersStr));
            } else if ("mapviewery".equals(qName)) {
                mp_CurrentMap.setMapViewerY(Double.parseDouble(mp_CharactersStr));
            }
        } catch (Exception ex) {
            System.exit(-1);
            throw new SAXException(ex.getMessage());
        }
        mp_CharactersStr = "";
    }

    @Override
    public void characters(char buf[], int offset, int len)
            throws SAXException {
        String s = new String(buf, offset, len);

        //since the sax parser will often call the characters method
        //multiple times for large strings within an elements tag we
        //are going to use a temporary variable to hold the contents
        //until an end element tag is reached.  once the end element
        //tag is reached we will put the data in mp_CharactersStr into
        //the proper object and set mp_CharactersStr to an empty string
        try {

            if (mp_State == MAP_ALIGNMENT_F_L) {

                mp_CurrentLeftAlignment += s;
            } else if (mp_State == MAP_ALIGNMENT_F_R) {

                mp_CurrentRightAlignment += s;
            } else if (mp_State == MAP_ALIGNMENT_F_I) {

                mp_CurrentAlignmentIndex += s;
            } else {
                mp_CharactersStr += s;
            }

        } catch (Exception ex) {
            throw new SAXException(ex.getMessage());
        }

    }
}
