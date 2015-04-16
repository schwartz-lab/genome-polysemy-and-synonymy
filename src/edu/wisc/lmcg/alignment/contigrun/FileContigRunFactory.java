/*
 * FileContigRunFactory.java
 *
 * Created on July 5, 2007, 8:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package edu.wisc.lmcg.alignment.contigrun;

import java.io.File;
import java.util.Collection;

/**
 * This interface extends the ContigRunFactory with a method to set a list of
 * file paths.
 *
 * @author churas
 */
public interface FileContigRunFactory extends ContigRunFactory {

    /**
     * Sets the path to the xml file. This method is used if user only has one
     * xml file to parse
     *
     * 8/13 @dmeyerson changed the argument type from String to File
     * @param file Filename containing the contigRun
     */
    public void setXmlFile(File file);

    /**
     * Sets path to file containing a list of xml files one per line
     * @param file Filename containing a list of xml files
     * @throws java.lang.Exception
     */
    public void setFileContainingListOfXmlFiles(String file) throws Exception;

    /**
     * Passed in a collection of string objects which represent full paths to
     * xml files
     * @param files Collection of files. Every one represents a ContigRun in xml format
     * @throws java.lang.Exception
     */
    public void setCollectionOfXmlFiles(Collection files) throws Exception;
}
