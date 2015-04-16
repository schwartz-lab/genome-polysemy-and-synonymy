package edu.wisc.lmcg.map;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.zip.GZIPInputStream;
import javax.swing.ProgressMonitorInputStream;

/**
 * Loads RestrictionMaps from various input streams.
 */
public class StreamRestrictionMapsFactory implements RestrictionMapsFactory {

    private BufferedReader mp_Reader;
    private int mp_Counter;
    private String mp_curLine;
    private String mp_File;
    private boolean mp_EnableProgressMonitor;

    public StreamRestrictionMapsFactory() {
        mp_Reader = null;
        mp_Counter = 0;
        mp_File = null;
        mp_curLine = null;
        mp_EnableProgressMonitor = false;
    }

    public void enableProgressMonitor(boolean val) {
        mp_EnableProgressMonitor = val;
    }

    public void setSourceOfRestrictionMaps(String file)
            throws NullPointerException, IOException, FileNotFoundException {
        if (file == null) {
            throw new NullPointerException("file is null");
        }
        mp_File = file;

        if (mp_File.endsWith(".gz") == true) {
            FileInputStream fStream = new FileInputStream(new File(mp_File));
            GZIPInputStream gStream = new GZIPInputStream(fStream);
            setSourceOfRestrictionMaps(gStream);
        } else {
            FileInputStream reader = new FileInputStream(mp_File);
            setSourceOfRestrictionMaps(reader);
        }
    }

    public void setSourceOfRestrictionMaps(Reader in)
            throws NullPointerException {
        if (in == null) {
            throw new NullPointerException("reader is null");
        }

        mp_Reader = new BufferedReader(in);
    }

    /**
     * sets the input source of this factory to be an input stream constructed
     * from the specified file
     *
     * @param mapsFile
     * @throws FileNotFoundException
     */
    public void setSourceOfRestrictionMaps(File mapsFile) throws FileNotFoundException {

        FileInputStream reader = null;
        reader = new FileInputStream(mapsFile);
        setSourceOfRestrictionMaps(reader);
    }

    public void setSourceOfRestrictionMaps(InputStream in)
            throws NullPointerException {
        if (in == null) {
            throw new NullPointerException("input stream is null");
        }
        InputStreamReader inreader = null;
        if (this.mp_File != null && this.mp_EnableProgressMonitor == true) {
            inreader = new InputStreamReader(new ProgressMonitorInputStream(null, "Reading Maps File: " + mp_File, in));
        } else {
            inreader = new InputStreamReader(in);
        }

        setSourceOfRestrictionMaps(inreader);
    }

    public SimpleRestrictionMap getNextRestrictionMap()
            throws NullPointerException, IOException, Exception {
        if (mp_Reader == null) {
            throw new NullPointerException("mp_Reader is null");
        }

        int enzymeindex = 0;
        int enzymeinitial = 0;
        int endofenzymeindex = 0;
        int length = 0;

        SimpleRestrictionMap curmap = null;
        try {
            if (mp_Reader.ready() == true) {

                mp_curLine = mp_Reader.readLine();

                while (mp_curLine != null
                        && !mp_curLine.equals("")) {

                    if (!mp_curLine.startsWith("#")) {

                        if (mp_Counter % 3 == 0) {

                            curmap = new SimpleRestrictionMap();

                            curmap.setName(mp_curLine);
                            mp_curLine = mp_Reader.readLine();
                        } else if (mp_Counter % 3 == 1) {
                            length = mp_curLine.length();

                            enzymeindex = 0;
                            endofenzymeindex = 1;
                            //iterate until you find non space or tab character
                            while (enzymeindex < length && (mp_curLine.charAt(enzymeindex) == ' '
                                    || mp_curLine.charAt(enzymeindex) == '\t')) {
                                enzymeindex++;
                            }
                            //gone too far this is bad
                            if (enzymeindex >= length) {
                                throw new Exception("Invalid map line at line: " + mp_Counter);
                            }
                            endofenzymeindex = enzymeindex;

                            while (endofenzymeindex < length && (mp_curLine.charAt(endofenzymeindex) != ' '
                                    && mp_curLine.charAt(endofenzymeindex) != '\t')) {
                                endofenzymeindex++;
                            }

                            if (endofenzymeindex >= length) {
                                throw new Exception("No space or tab after enzyme name at line: " + mp_Counter);
                            }

                            curmap.setEnzyme(mp_curLine.substring(enzymeindex, endofenzymeindex));

                            enzymeinitial = endofenzymeindex;
                            while (enzymeinitial < length && mp_curLine.charAt(enzymeinitial) == ' '
                                    || mp_curLine.charAt(enzymeinitial) == '\t') {
                                enzymeinitial++;
                            }
                            if (enzymeinitial >= length) {
                                throw new Exception("No spaces or tabs found after enzyme name at: " + mp_Counter);
                            }

                            curmap.setMapBlock(mp_curLine.substring(enzymeinitial + 1));
                        } else if (mp_Counter % 3 == 2) {
                            mp_curLine = mp_Reader.readLine();
                            mp_Counter++;
                            return curmap;
                        }

                        mp_Counter++;
                    }else{
                        mp_curLine = mp_Reader.readLine();
                    }
                }
            }
        } catch (java.lang.NumberFormatException nfex) {
            throw new java.lang.NumberFormatException("Malformed map at line: " + mp_Counter);
        }
        return null;
    }

    public boolean resetSupported() {
        if (mp_File != null) {
            return true;
        } else {
            return false;
        }
    }

    public void reset()
            throws java.io.IOException, NullPointerException {
        if (mp_Reader != null) {
            mp_Reader.close();
            mp_Reader = null;
            mp_Counter = 0;
        }

        setSourceOfRestrictionMaps(mp_File);
    }
}
