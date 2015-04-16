/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wisc.lmcg.gui;

import edu.wisc.lmcg.alignment.contigrun.ContigRun;
import edu.wisc.lmcg.alignment.mapalignment.MapAlignment;
import edu.wisc.lmcg.map.RestrictionMap;
import edu.wisc.lmcg.map.SimpleRestrictionMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.rmi.CORBA.Util;

/**
 *
 * @author Diego Patino
 */
public interface GPSController {
    
    /**
     *
     * @param mapsFile the given file
     * @return an arraylist of the restrictionMaps described in the given file
     */
    ArrayList<SimpleRestrictionMap> parseMapsFile(File mapsFile);
    
    /**
     * get the contig data specified by the xml file
     *
     * @param xmlFile a string representing the xmlFile we're going to use
     * @return a hashtable of contigs
     */
    ContigRun parseXmlFile(File xmlFile) throws Exception;   
    
    /**
     * Performs the alignment of several maps with a reference map, with 
     * @param basicFolder Basic folder for executing the alignment procedures
     * @param silicoFile .silico file with the maps to be aligned, using the soma algorithm: http://www.cbcb.umd.edu/soma/
     * @param optFile .opt file containing the reference map to align the maps
     */
    void alignMaps(String basicFolder, String silicoFile, String optFile);
    
    void restrictionMap2OptFormat(SimpleRestrictionMap map, String optFilename) throws IOException;
    
    void restrictionMaps2SilicoFormat(List<SimpleRestrictionMap> maps, String silicoFilename) throws IOException;
    
    List<MapAlignment> constructMapAligment(SimpleRestrictionMap referenceMap, List<SimpleRestrictionMap> unalignedMaps, String baseDir);
}
