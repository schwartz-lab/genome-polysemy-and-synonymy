/*
 * XmlContigRunFactory.java
 *
 * Created on September 7, 2004, 3:27 PM
 */
package edu.wisc.lmcg.alignment.contigrun;

import edu.wisc.lmcg.alignment.xml.parsers.AlignmentParser;
import edu.wisc.lmcg.lang.LmcgRuntime;
import edu.wisc.lmcg.lang.LmcgRuntimeImpl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

/**
 * Obtains ContigRun objects from xml files.
 *
 * @author churas
 */
public class XmlContigRunFactory implements FileContigRunFactory {

    private LmcgRuntime mp_Runtime;
    private List<String> mp_XmlFileVector;
    private File xmlFile; //the single xml file we'll be using
    private boolean mp_LogAlignmentParseErrors;

    public XmlContigRunFactory() {
        mp_LogAlignmentParseErrors = false;
        mp_Runtime = new LmcgRuntimeImpl();
    }

    /**
     * Sets the xml file to extract the contigRuns
     * @param xmlFile Filename containing the contigRuns
     */
    @Override
    public void setXmlFile(File xmlFile) {
        this.xmlFile = xmlFile;
    }

    /**
     * Sets the runtime
     * @param lrt
     */
    public void setLmcgRuntime(LmcgRuntime lrt) {
        mp_Runtime = lrt;
    }

    /**
     * If set to true the parser will attempt to log parser errors instead of
     * throwing exceptions
     * @param val activation value for the log system
     */
    public void logAlignmentParseErrors(boolean val) {
        mp_LogAlignmentParseErrors = val;
    }

    /**
     * Indicates a file containing a list with the filenames of several xml files. Each xml file must represent a contigRun
     * @param file Filanem with the list of xml files
     * @throws Exception 
     */
    @Override
    public void setFileContainingListOfXmlFiles(String file) throws Exception {
        String curxmlfile;
        mp_XmlFileVector = new LinkedList<>();
        //InputStreamReader isr = new InputStreamReader(mp_IOFactory.createInputStream(mp_IOFactory.createFile(file)));
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
        try (BufferedReader bufin = new BufferedReader(isr)) {
            if (bufin.ready() == true) {
                curxmlfile = bufin.readLine();
                
                while (curxmlfile != null) {
                    mp_XmlFileVector.add(curxmlfile);
                    curxmlfile = bufin.readLine();
                }
            }
        }
    }

    /**
     * Takes a collection of string objects which contain full paths to xml
     * files to generate contigrun objects from
     */
    @Override
    public void setCollectionOfXmlFiles(Collection files) throws Exception {
        if (files == null) {
            throw new IllegalArgumentException("files argument is null");
        }        
        if (files.isEmpty() == true) {
            return;
        }
        mp_XmlFileVector = new LinkedList<>();

        for (Iterator itr = files.iterator(); itr.hasNext();) {            
            mp_XmlFileVector.add((String) itr.next());
        }
    }

    /**
     * Gets the next ContigRun available
     * @return A contig run
     * @throws NullPointerException
     * @throws Exception 
     */
    @Override
    public ContigRun getNextContigRun() throws NullPointerException, Exception {

        String file = null;

        /*//this is all code that deals with multiple xml files at once. Don't
         //think we need it for anything we're doing + it just complicates the task
         //@dmeyerson 2013

         if (mp_XmlFileVector != null) {
         if (mp_XmlFileVector.size() > 0){
         file = (String)mp_XmlFileVector.lastElement();
         mp_XmlFileVector.remove(mp_XmlFileVector.size()-1);
         }
         else {
         mp_XmlFileVector = null;
         return null;
         }
         }

         if (file == null){
         throw new NullPointerException("File obtained was null");
         }

         File thefile = this.mp_IOFactory.createFile(file);

         if (thefile.exists() == false){
         file+=".gz";

         thefile = this.mp_IOFactory.createFile(file);

         if (thefile.exists() == false){
         throw new NullPointerException("File does not exist: "+file);
         }
         }
         */
        //create a sax parser factory
        SAXParserFactory factory = SAXParserFactory.newInstance();

        //create a new alignment parser that is derived from
        //default handler 
        AlignmentParser aparser = new AlignmentParser();
        aparser.logAlignmentParseErrors(mp_LogAlignmentParseErrors);
        SAXParser saxParser = factory.newSAXParser();

        //if the file ends with .gz we need to gunzip it before
        //parsing the file.
        if (xmlFile.getName().endsWith(".gz") == true) {
            //InputStream fStream = mp_IOFactory.createInputStream(mp_IOFactory.createFile(file));\
            InputStream fStream = new FileInputStream(file);

            try {
                GZIPInputStream gStream = new GZIPInputStream(fStream);

                saxParser.parse(gStream, aparser);
                gStream.close();
            } catch (IOException | SAXException ex) {
                String command = "/bin/zcat " + file;

                Process p = mp_Runtime.exec(command);

                saxParser.parse(p.getInputStream(), aparser);
            }
        } else {
            try ( //no .gz extension so we assume the file is not compressed
                InputStream inStream = new FileInputStream(xmlFile) /*mp_IOFactory.createFile(file));*/ ) {
                saxParser.parse(inStream, aparser);
            }
        }

        return aparser.getContigRun();

    }

    /**
     * Restarts the factory
     */
    @Override
    public boolean resetSupported() {
        return false;
    }

    /**
     * Indicates if the reset functionality is available
     * @throws java.lang.Exception 
     */
    @Override
    public void reset() throws Exception {
    }
}
