/*
 * AlignmentParseError.java
 *
 * Created on March 5, 2006, 3:49 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package edu.wisc.lmcg.alignment.xml.parsers;

import edu.wisc.lmcg.map.RestrictionMap;

/**
 * Instances of this class represent an error found when AlignmentParser was
 * run. This object allows parser to run through an alignment file and build up
 * a list of problems allowing for extraction of data from partially correct
 * alignment files.
 *
 * @author churas
 */
public class AlignmentParseError {

    private RestrictionMap mp_ResMap;
    private String mp_Message;

    /**
     * Creates a new instance of AlignmentParseError
     */
    public AlignmentParseError() {
        mp_ResMap = null;
        mp_Message = null;
    }

    /**
     * Sets the message describing this parse error.
     */
    public void setMessage(String message) {
        mp_Message = message;
    }

    /**
     * gets the message describing this parse error.
     *
     * @return message describing error or null if message not set.
     */
    public String getMessage() {
        return mp_Message;
    }

    /**
     * Sets the RestrictionMap involved with the error.
     */
    public void setMap(RestrictionMap resmap) {
        mp_ResMap = resmap;
    }

    /**
     * Gets the restriction map involved with the error if one exists.
     *
     * @return RestrictionMap involved with error or null if none exists.
     */
    public RestrictionMap getMap() {
        return mp_ResMap;
    }
}
