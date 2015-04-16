/*						    __		  __  ___	   ________  ________
 / /	     /  |/   |    / ____  / / ______/
 / /	    / /|  /| |   / /   /_/ / /   __
 / /____   / / |_/ | |  / /_____  / /___/ /
 /______/o /_/      |_|o \______/o \______/o

 Main class:			programs.MapViewer
 LMCG dependencies:	gui.MapGUI - the window frame that holds...
 gui.MapsPanel - the content panel that displays MapDrawings and AlignLines
 gui.MapDrawing - array of rectangles representing a restriction map
 gui.AlignLines - set of lines designating an alignment of two maps
									
 map.Mapset
 map.SimpleRestrictionMap
 map.RestrictionFragment
 alignment.contigrun.XmlContigRunFactory
 David Meyerson 2013
 *//////////////////////////////////////////100 cols wide///////////////////////////////////////////
package edu.wisc.lmcg.programs;

import java.io.*;

import edu.wisc.lmcg.gui.MapGUI;

/**
 * Displays a GUI showing the maps from a given map set.
 *
 * @author dmeyerson
 */
public class MapViewer {

    public static void main(String[] args) {

        File xmlFile = null;
        if (args.length > 0) {
            xmlFile = new File(args[0]);
        }
        MapGUI mapGUI = new MapGUI(xmlFile);
    }
}
