/*						    __	   		 __  ___ 	  	 ________	  ________
 / /    		/  |/   |		/ ____  /	 / ______/
 / /    	   / /|  /| | 	   / /   /_/ 	/ /   __
 / /____	  / / |_/ | |	  / /_____	   / /___/ /
 /______/  o  /_/      |_|  o  \______/  o  \______/  o

 Main class:			programs.MapViewer
 LMCG dependencies:	gui.MapGraphics
 map.SimpleRestrictionMap
 map.RestrictionFragment
 David Meyerson 2013
 *//////////////////////////////////////////100 cols wide////////////////////////////////////////////
package edu.wisc.lmcg.gui;

import edu.wisc.lmcg.alignment.contigrun.ContigRun;
import edu.wisc.lmcg.alignment.contigrun.StandardContigRun;
import edu.wisc.lmcg.alignment.contigrun.XmlContigRunFactory;
import edu.wisc.lmcg.alignment.mapalignment.MapAlignment;
import edu.wisc.lmcg.alignment.mapalignment.StandardMapAlignment;
import edu.wisc.lmcg.gui.themes.AlignmentColorTheme;
import edu.wisc.lmcg.gui.themes.DefaultAlignmentColorTheme;
import edu.wisc.lmcg.map.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;


public class MapGUI extends JFrame implements ActionListener, ItemListener, GPSController, ChangeListener{

    private final String Title = "Genome Polysemy and Synonymy";
    private JMenu view;
    private JMenu tools;
    private JMenu help;
    private final JMenuItem open, saveAlignment, hideAllAlignLines, showMapNames, reorganize, addMaps, openMaps, saveMaps, about, basic_alignment;
    private final JButton open_btn, saveAlignment_btn, reorganize_btn, pan_btn, pointer_btn, zoomin_btn, zoomout_btn, screenshot_btn, locate_drawing_btn;
    private JCheckBox hideAllAlignLines_cb, showMapNames_cb, sortMaps_cb;
    private JSpinner linesFrequency, fragmentBoxWidthChooser, emptyFragmentBoxWidthChooser, AlignmentLineWidthChooser, minimumGapSizeChooser;
    private ContigRun crun;
    private JSplitPane contentPanel;
    public static JContigInformationPanel contigInfoPanel;
    private MapsPanel mapsPanel;
    private AboutFrame aboutFrame; //keeps an instances of the JFrame used to show information about the software
    private BasicAlignmentFrame basic_alignmentFrame; //keeps an instances of the JFrame used to align contigs with a basic algorithm
    private static AlignmentColorTheme theme = new DefaultAlignmentColorTheme();
    public static  final java.util.List<AlignmentColorTheme> THEME_LIST = new LinkedList<>();

    private final Color SELECTED_BUTTON_COLOR = UIManager.getColor("Button.select");
    private final Color UNSELECTED_BUTTON_COLOR = UIManager.getColor("Button.background");
    
    private final String OPEN_ALIGNMENT_ICON = "/edu/wisc/lmcg/resources/open.png";
    private final String SAVE_ALIGNMENT_ICON = "/edu/wisc/lmcg/resources/save_alignment.png";
    private final String REORGANIZE_ALIGNMENT_ICON = "/edu/wisc/lmcg/resources/reset_alignments.png";
    private final String PAN_ICON = "/edu/wisc/lmcg/resources/pan.png";
    private final String POINTER_ICON = "/edu/wisc/lmcg/resources/pointer.png";
    private final String ZOOMIN_ICON =  "/edu/wisc/lmcg/resources/zoomin.png";
    private final String ZOOMOUT_ICON = "/edu/wisc/lmcg/resources/zoomout.png";    
    private final String SCREENSHOT_ICON = "/edu/wisc/lmcg/resources/screenshot.png";
    private final String LOCATE_DRAWING_ICON = "/edu/wisc/lmcg/resources/locate_contig.png";
    
    private final int DEFAULT_BUTTON_SIZE = 16;

    public MapGUI(File xmlFile) {

        //set up the frame where we'll put the GUI
        super();
        setTitle(Title);
        
        //loads all the color themes to drawn the alignments
        MapGUI.loadAlignmentColorThemes();

        //Make the background completely black
        this.getContentPane().setBackground(Color.BLACK);
        
        contentPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);  
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setDividerSize(2);
        add(contentPanel);
        
        setBounds(100, 100, 800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        try {
            //Read the contig run (if there's one specified by the xml file)
            //and put the panel in the window frame            
            createMapsPanel(xmlFile);            
            setTitle(Title + " - " + xmlFile.getName() );            
        } catch (Exception e) {
            setTitle(Title);
            crun = null;
            stopLoadingState();
        }
        
        

        //set up the frame's menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        //Set up all the menus for teh menu toolbar
        JMenu file = new JMenu("File");
        menuBar.add(file);

        open = new JMenuItem("Open Alignment...");
        open.setIcon(new ImageIcon(getScalatedIcon(OPEN_ALIGNMENT_ICON, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE)));
        open.addActionListener(this);
        file.add(open);

        saveAlignment = new JMenuItem("Save Alignment...");
        saveAlignment.setIcon(new ImageIcon(getScalatedIcon(SAVE_ALIGNMENT_ICON, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE)));
        saveAlignment.addActionListener(this);
        file.add(saveAlignment);

        saveMaps = new JMenuItem("Save Maps...");
        saveMaps.addActionListener(this);
        file.add(saveMaps);

        addMaps = new JMenuItem("Add Maps From File...");
        addMaps.addActionListener(this);
        file.add(addMaps);

        openMaps = new JMenuItem("Open Maps File...");
        openMaps.addActionListener(this);
        file.add(openMaps);

        view = new JMenu("View");
        menuBar.add(view);

        reorganize = new JMenuItem("Re-arrange maps");
        reorganize.setIcon(new ImageIcon(getScalatedIcon(REORGANIZE_ALIGNMENT_ICON, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE)));
        reorganize.addActionListener(this);
        view.add(reorganize);

        //Adds a separator between action buttons and check boxes
        view.add(new JSeparator());

        hideAllAlignLines = new JCheckBoxMenuItem("Hide Alignment Lines");
        hideAllAlignLines.addItemListener(this);
        view.add(hideAllAlignLines);

        showMapNames = new JCheckBoxMenuItem("Show map names");
        showMapNames.setSelected(true);
        showMapNames.setPreferredSize(new Dimension(100, DEFAULT_BUTTON_SIZE));
        showMapNames.addItemListener(this);
        view.add(showMapNames);

        tools = new JMenu("Tools");
        menuBar.add(tools);

        basic_alignment = new JMenuItem("Basic alignment");
        basic_alignment.addActionListener(this);
        tools.add(basic_alignment);
        
        help = new JMenu("Help");
        menuBar.add(help);

        about = new JMenuItem("About MapViewer");
        about.addActionListener(this);
        help.add(about);

        //Creates a toolbar                 
        JToolBar toolbar = new JToolBar();
        toolbar.setRollover(true);
        toolbar.setFloatable(false);        
                
        add(toolbar, BorderLayout.PAGE_START);

        //-------------------------------------------------------------
        pan_btn = new JButton();
        pan_btn.setIcon(new ImageIcon(getScalatedIcon(PAN_ICON, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE)));
        pan_btn.setToolTipText("Move all elements");
        pan_btn.addActionListener(this);

        pointer_btn = new JButton();
        pointer_btn.setIcon(new ImageIcon(getScalatedIcon(POINTER_ICON, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE)));
        pointer_btn.setToolTipText("Move individual maps");
        pointer_btn.addActionListener(this);
        
        zoomin_btn = new JButton();
        zoomin_btn.setIcon(new ImageIcon(getScalatedIcon(ZOOMIN_ICON, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE)));
        zoomin_btn.setToolTipText("Zoom in");
        zoomin_btn.addActionListener(this);

        zoomout_btn = new JButton();
        zoomout_btn.setIcon(new ImageIcon(getScalatedIcon(ZOOMOUT_ICON, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE)));
        zoomout_btn.setToolTipText("Zoom out");
        zoomout_btn.addActionListener(this);

        open_btn = new JButton();
        open_btn.setIcon(new ImageIcon(getScalatedIcon(OPEN_ALIGNMENT_ICON, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE)));
        open_btn.setToolTipText("Open a new alignment");
        open_btn.addActionListener(this);

        saveAlignment_btn = new JButton();
        saveAlignment_btn.setIcon(new ImageIcon(getScalatedIcon(SAVE_ALIGNMENT_ICON, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE)));
        saveAlignment_btn.setToolTipText("Save changes in the curretn alignment");
        saveAlignment_btn.addActionListener(this);        

        reorganize_btn = new JButton();
        reorganize_btn.setIcon(new ImageIcon(getScalatedIcon(REORGANIZE_ALIGNMENT_ICON, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE)));
        reorganize_btn.setToolTipText("Re-arrange maps");
        reorganize_btn.addActionListener(this);        
        
        screenshot_btn = new JButton();
        screenshot_btn.setIcon(new ImageIcon(getScalatedIcon(SCREENSHOT_ICON, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE)));
        screenshot_btn.setToolTipText("Take a screenshot");
        screenshot_btn.addActionListener(this);
        
        locate_drawing_btn = new JButton();
        locate_drawing_btn.setIcon(new ImageIcon(getScalatedIcon(LOCATE_DRAWING_ICON, DEFAULT_BUTTON_SIZE, DEFAULT_BUTTON_SIZE)));
        locate_drawing_btn.setToolTipText("Go to the contig");
        locate_drawing_btn.addActionListener(this);

        hideAllAlignLines_cb = new JCheckBox("Hide Alignment Lines");
        hideAllAlignLines_cb.setPreferredSize(new Dimension(100, DEFAULT_BUTTON_SIZE));
        hideAllAlignLines_cb.addItemListener(this);
        
        
        JLabel lblLinesFrequency = new JLabel("Lines frquency: ");
        SpinnerNumberModel snm = new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1);
        linesFrequency = new JSpinner(snm);
        linesFrequency.setToolTipText("Sets a parameter to calculate how many fragments an alignment line will be displayed.");
        linesFrequency.setPreferredSize(new Dimension(60, 30));
        linesFrequency.setMaximumSize(new Dimension(60, 30));
        linesFrequency.addChangeListener(this);
        
        JLabel lblFragmentBoxWidthText = new JLabel("Fragment box width: ");
        snm = new SpinnerNumberModel(0.02d, 0d, 1, 0.002d);
        fragmentBoxWidthChooser = new JSpinner(snm);
        fragmentBoxWidthChooser.setToolTipText("Sets a parameter to show a bow around each fragment in a map.");
        fragmentBoxWidthChooser.setPreferredSize(new Dimension(100, 30));
        fragmentBoxWidthChooser.setMaximumSize(new Dimension(100, 30));
        fragmentBoxWidthChooser.addChangeListener(this);
        
        JLabel lblEmptyFragmentBoxWidthText = new JLabel("Empty fragment box width: ");
        snm = new SpinnerNumberModel(0.02d, 0d, 1, 0.002d);
        emptyFragmentBoxWidthChooser = new JSpinner(snm);
        emptyFragmentBoxWidthChooser.setToolTipText("Sets a parameter to show a bow around each fragment in a map.");
        emptyFragmentBoxWidthChooser.setPreferredSize(new Dimension(100, 30));
        emptyFragmentBoxWidthChooser.setMaximumSize(new Dimension(100, 30));
        emptyFragmentBoxWidthChooser.addChangeListener(this);
        
        JLabel lblAlignmentLineWidth = new JLabel("Alignment line width: ");
        snm = new SpinnerNumberModel(0.02d, 0d, 1, 0.002d);
        AlignmentLineWidthChooser = new JSpinner(snm);
        AlignmentLineWidthChooser.setToolTipText("Sets a parameter to define the width of each alignment line.");
        AlignmentLineWidthChooser.setPreferredSize(new Dimension(100, 30));
        AlignmentLineWidthChooser.setMaximumSize(new Dimension(100, 30));
        AlignmentLineWidthChooser.addChangeListener(this);
        
        JLabel lblMinimumGapSizeText = new JLabel("Minimum Gap size (kb): ");
        snm = new SpinnerNumberModel(30.0d, 0.001, Double.MAX_VALUE, 10.0d);
        minimumGapSizeChooser = new JSpinner(snm);
        minimumGapSizeChooser.setToolTipText("Sets a parameter for showing lines when there is a gap in the alignment.");
        minimumGapSizeChooser.setPreferredSize(new Dimension(100, 30));
        minimumGapSizeChooser.setMaximumSize(new Dimension(100, 30));
        minimumGapSizeChooser.addChangeListener(this);
        

        showMapNames_cb = new JCheckBox("Show map names");
        showMapNames_cb.setSelected(true);
        showMapNames_cb.setPreferredSize(new Dimension(100, DEFAULT_BUTTON_SIZE));
        showMapNames_cb.addItemListener(this);
        
        sortMaps_cb = new JCheckBox("Sort by reference map");
        sortMaps_cb.setSelected(false);
        sortMaps_cb.setPreferredSize(new Dimension(100, DEFAULT_BUTTON_SIZE));
        sortMaps_cb.addItemListener(this);

        toolbar.add(pointer_btn);
        toolbar.add(pan_btn);
        toolbar.add(zoomin_btn);
        toolbar.add(zoomout_btn);
        toolbar.addSeparator();
        toolbar.add(open_btn);
        toolbar.add(saveAlignment_btn);
        toolbar.add(reorganize_btn);
        toolbar.add(screenshot_btn);
        toolbar.add(locate_drawing_btn);
        //add a separator to the toolbar
        toolbar.addSeparator();
        toolbar.add(hideAllAlignLines_cb);
        toolbar.add(showMapNames_cb);        
        toolbar.add(sortMaps_cb);
        toolbar.addSeparator();
        toolbar.add(lblLinesFrequency);
        toolbar.add(linesFrequency);
        toolbar.add(lblFragmentBoxWidthText);
        toolbar.add(fragmentBoxWidthChooser);
        toolbar.add(lblEmptyFragmentBoxWidthText);
        toolbar.add(emptyFragmentBoxWidthChooser);
        toolbar.add(lblAlignmentLineWidth);
        toolbar.add(AlignmentLineWidthChooser);
        toolbar.add(lblMinimumGapSizeText);
        toolbar.add(minimumGapSizeChooser);

        
        //FIAT LUX
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        
    }

    public static Image getScalatedIcon(String resource, int width, int height) {        
        ImageIcon i = new ImageIcon(MapGUI.class.getResource(resource));
        return i.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    /**
     * get the contig data specified by the xml file
     *
     * @param xmlFile a string representing the xmlFile we're going to use
     * @return a hashtable of contigs
     */
    public ContigRun parseXmlFile(File xmlFile) throws Exception {
        

        XmlContigRunFactory crf = new XmlContigRunFactory();
        crf.setXmlFile(xmlFile);
        return crf.getNextContigRun();
    }

    /**
     *
     * @param mapsFile the given file
     * @return an arraylist of the restrictionMaps described in the given file
     */
    public ArrayList<SimpleRestrictionMap> parseMapsFile(File mapsFile) {

        StreamRestrictionMapsFactory mapFac = new StreamRestrictionMapsFactory();
        try {
            mapFac.setSourceOfRestrictionMaps(mapsFile);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        ArrayList<SimpleRestrictionMap> rMaps = new ArrayList<SimpleRestrictionMap>();
        while (true) {
            try {
                SimpleRestrictionMap rMap = mapFac.getNextRestrictionMap();
                if (rMap != null) {
                    rMaps.add(rMap);
                } else {
                    return rMaps;
                }
            } catch (NullPointerException e) {
                return rMaps;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Problem parsing input file, see console for details",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return rMaps;
            }
        }
    }

    /**
     * saves each map on the screen to the specified printwriter
     *
     * @param mapsFile
     */
    private void writeMapsFile(PrintWriter mapsWriter) {

        Map<String, RestrictionMap> maps = null;
        try {
            maps = crun.getMapset().getRestrictionMaps();
            if (maps.isEmpty()) {
                throw new NullPointerException(); //not exactly appropriate to throw 
            }											//nullpointer exception here, but it's compact and simple
        } catch (NullPointerException e) {
            JOptionPane.showMessageDialog(this,
                    "No maps to save!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        for (RestrictionMap aMap : maps.values()) {            
            mapsWriter.print(aMap.getMapInTabFormat());
        }
    }
    
    /**
     * respond to a fired action event in the GUI
     * @param event
     */
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {

        //find out what part of the GUI fired the ActionEvent
        Object source = event.getSource();

        if (source == open || source == open_btn) {

            JFileChooser fc = new JFileChooser();
            FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("Alignment file (*.xml)", "xml");            
            fc.addChoosableFileFilter(xmlFilter);
            fc.setFileFilter(xmlFilter);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File xmlFile = fc.getSelectedFile();
                boolean exceptionThrown = false;
                try {
                    //Read the contig run (if there's one specified by the xml file)
                    //and put the panel in the window frame                    
                    createMapsPanel(xmlFile);
                    setTitle(Title + " - " + xmlFile.getName() );                    
                } catch (Exception e) {
                    setTitle(Title);
                    stopLoadingState();
                    JOptionPane.showMessageDialog(this,
                        "Input file invalid, please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                }
            }
        }

        if (source == addMaps || source == openMaps) {
            //get the file chosen by the user
            JFileChooser fc = new JFileChooser();
            fc.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter mapsFilter = new FileNameExtensionFilter("Map file (*.maps)", "maps");
            fc.addChoosableFileFilter(mapsFilter);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File mapsFile = fc.getSelectedFile();
                startLoadingState();
                
                
                ArrayList<SimpleRestrictionMap> moreMaps = parseMapsFile(mapsFile);
                
                if ( moreMaps == null ){
                    stopLoadingState();
                    JOptionPane.showMessageDialog(this,
                            "Input file invalid, please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }else if (mapsPanel == null) {  
                    try{
                        this.createMapsPanel(() -> {
                            mapsPanel.addMaps(moreMaps);
                            setTitle(Title + " - " + fc.getSelectedFile().getName() );

                        });
                    }catch(Exception ex){
                        stopLoadingState();
                    JOptionPane.showMessageDialog(this,
                            "Input file invalid, please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                    }
                }else{
                    mapsPanel.addMaps(moreMaps);
                }
                
                stopLoadingState();
            }
            return;
        }

        if (source == saveAlignment || source == saveAlignment_btn) {

            JFileChooser fc = new JFileChooser();
            FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("Alignment file (*.xml)", "xml");            
            fc.addChoosableFileFilter(xmlFilter);
            fc.setFileFilter(xmlFilter);
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                PrintWriter xmlWriter = null;
                
                if ( fc.getSelectedFile().exists() && !fc.getSelectedFile().delete() ){
                    JOptionPane.showMessageDialog(this,
                            "The file is being used and can not be overwritten.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {                    
                    xmlWriter = new PrintWriter(fc.getSelectedFile());
                } catch (FileNotFoundException e) {
                    JOptionPane.showMessageDialog(this,
                            "there's no way this message will ever get printed.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    xmlWriter.print(crun.getContigAsXml());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this,
                            "Unexpected problem, file not saved",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    xmlWriter.close();
                }
            }
            return;
        }

        if (source == saveMaps) {

            JFileChooser fc = new JFileChooser();
            fc.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter mapsFilter = new FileNameExtensionFilter("Map file (*.maps)", "maps");
            fc.addChoosableFileFilter(mapsFilter);
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                PrintWriter mapsWriter = null;
                try {
                    mapsWriter = new PrintWriter(fc.getSelectedFile());
                } catch (FileNotFoundException e) {
                    JOptionPane.showMessageDialog(this,
                            "unexpected error writing to file. Check console for details.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    writeMapsFile(mapsWriter);
                } finally {
                    mapsWriter.close();
                }
            }
        }

        if (source == reorganize || source == reorganize_btn) {

            /*try {
                Hashtable<String, SimpleRestrictionMap> maps = crun.getMapset().getRestrictionMaps();
                if (maps.isEmpty()) {
                    throw new NullPointerException();
                }
            } catch (NullPointerException e) {
                JOptionPane.showMessageDialog(this,
                        "No maps to reset!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            MapDrawing.setResetState(true);            
            MapsPanel.resetZoomFactor();
            mapsPanel = new MapsPanel(mapsPanel.getContigRun());
            MapDrawing.setResetState(false);*/
            
            if ( mapsPanel != null ){
                mapsPanel.sortMaps();
            }
        }
        
        if ( source == screenshot_btn && this.mapsPanel != null ){
            JFileChooser fc = new JFileChooser();
            fc.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter svgfilter = new FileNameExtensionFilter("Scalable Vector Graphics (*.svg)", "svg");                        
            FileNameExtensionFilter pngfilter = new FileNameExtensionFilter("Portable Network Graphics (*.png)", "png");            
            fc.addChoosableFileFilter(svgfilter);            
            fc.addChoosableFileFilter(pngfilter);
            
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {                
                
                if ( fc.getSelectedFile().exists() && !fc.getSelectedFile().delete() ){
                    JOptionPane.showMessageDialog(this,
                            "The file is being used and can not be overwritten.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                startLoadingState();
                boolean screenshotSaved;
                if ( fc.getFileFilter().equals(pngfilter) ){
                    String out_filename = fc.getSelectedFile().getAbsolutePath();
                    if ( !out_filename.endsWith(".png") )
                        out_filename += ".png";
                        
                    screenshotSaved = this.mapsPanel.getScreenShot(out_filename, "png");
                }else if ( fc.getFileFilter().equals(svgfilter) ){
                    
                    String out_filename = fc.getSelectedFile().getAbsolutePath();
                    if ( !out_filename.endsWith(".svg") )
                        out_filename += ".svg";
                    
                    screenshotSaved = this.mapsPanel.getScreenShot(out_filename, "svg");
                }else{
                    stopLoadingState();
                    JOptionPane.showMessageDialog(this,
                            "The screenshot could no be saved. Select a valid file type.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                stopLoadingState();
                
                if ( !screenshotSaved ){                
                    JOptionPane.showMessageDialog(this,
                            "The screenshot could no be saved. Check the filename and try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }else{
                    JOptionPane.showMessageDialog(this,
                            "The screenshot was successfully saved.",
                            "Screenshot",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
        }
        
        if (source == about) {
            if (aboutFrame != null) {
                aboutFrame.dispose();
            }
            aboutFrame = new AboutFrame();
            aboutFrame.setVisible(true);
        }
        
        if (source == locate_drawing_btn) {
            if ( mapsPanel != null){
                
                try {
                    mapsPanel.getUpdateManager().getUpdateRunnableQueue().invokeAndWait(() -> {
                        //mapsPanel.resetRenderingTransform();
                        mapsPanel.goToOrigin();
                    });                    
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(MapGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        if (source == basic_alignment) {
            if (basic_alignmentFrame != null) {
                basic_alignmentFrame.dispose();
            }
            basic_alignmentFrame = new BasicAlignmentFrame(this);
            basic_alignmentFrame.setVisible(true);
        }

        //Respond to the event only if mapsPanel is not null(there is a drawing)
        if (mapsPanel != null) {
            pan_btn.setBackground(UNSELECTED_BUTTON_COLOR);
            pointer_btn.setBackground(UNSELECTED_BUTTON_COLOR);
            zoomin_btn.setBackground(UNSELECTED_BUTTON_COLOR);
            zoomout_btn.setBackground(UNSELECTED_BUTTON_COLOR);
            if (source == pan_btn) {
                pan_btn.setBackground(SELECTED_BUTTON_COLOR);
                mapsPanel.setMouseState(MapViewerMouseState.PAN);
            } else if (source == pointer_btn) {
                pointer_btn.setBackground(SELECTED_BUTTON_COLOR);                
                mapsPanel.setMouseState(MapViewerMouseState.POINTER);
            }
             else if (source == zoomin_btn) {
                zoomin_btn.setBackground(SELECTED_BUTTON_COLOR);
                mapsPanel.setMouseState(MapViewerMouseState.ZOOM_IN);
            }
             else if (source == zoomout_btn) {
                zoomout_btn.setBackground(SELECTED_BUTTON_COLOR);
                mapsPanel.setMouseState(MapViewerMouseState.ZOOM_OUT);
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent event) {

        Object source = event.getItemSelectable();

        if (source == hideAllAlignLines || source == hideAllAlignLines_cb) {
            
            if ( mapsPanel != null ){
            
                //if it's deselected, true. If not, false.                    
                boolean hide = event.getStateChange() == ItemEvent.DESELECTED;            
                mapsPanel.setAllAlignLinesVisible(hide);
                hideAllAlignLines.setSelected(!hide);
                hideAllAlignLines_cb.setSelected(!hide);
            }
        }        

        if (source == showMapNames_cb || source == showMapNames) {
            
            if ( mapsPanel != null ){
            
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    mapsPanel.showMapNames();
                    showMapNames_cb.setSelected(true);
                    showMapNames.setSelected(true);
                } else {
                    mapsPanel.hideMapNames();
                    showMapNames_cb.setSelected(false);
                    showMapNames.setSelected(false);
                }
            }

        }
        
        if (source == sortMaps_cb ) {
            MapsPanel.SORT_MAPS = sortMaps_cb.isSelected();
        }
    }

    /**
     * removes the old maps panel to replace it with a new one
     */
    private void createMapsPanel(Runnable onCanvasReradyCallback)  throws Exception{
        
        createMapsPanel(new StandardContigRun(), onCanvasReradyCallback);
    }
    
    /**
     * removes the old maps panel to replace it with a new one
     */
    private void createMapsPanel()  throws Exception {
        
        createMapsPanel(() -> {});
    }
    
    /**
     * removes the old maps panel to replace it with a new one
     */
    private void createMapsPanel(File xmlFile) throws Exception{
        createMapsPanel(xmlFile, null);        
    }
    
    private void createMapsPanel(File xmlFile, Runnable onCanvasReradyCallback) throws Exception {
        createMapsPanel(parseXmlFile(xmlFile), onCanvasReradyCallback);
    }
    
    /**
     * removes the old maps panel to replace it with a new one
     *
     * @param mp the new panel
     */
    private void createMapsPanel(ContigRun cr, Runnable onCanvasReradyCallback) throws Exception {        
        
        crun = cr;
        
        if (mapsPanel != null) {
            remove(mapsPanel);
        }
        
        startLoadingState();
        
        contigInfoPanel = new JContigInformationPanel();
        contentPanel.setLeftComponent(contigInfoPanel);
        
        startLoadingState();
        mapsPanel = new MapsPanel(crun, () -> { 
            stopLoadingState();
            if ( onCanvasReradyCallback != null )
                onCanvasReradyCallback.run();
        });
        contentPanel.setRightComponent(mapsPanel);        

        hideAllAlignLines.setSelected(false);
        mapsPanel.setShiftResponder(new KeyResponder());
        setVisible(true);
        
    }
    
    private void startLoadingState() {                
        this.setEnabled(false);
        if ( this.contigInfoPanel != null )
            this.contigInfoPanel.showProgressBar();
        this.setCursor(new Cursor((Cursor.WAIT_CURSOR)));
    }

    private void stopLoadingState() {        
        this.setEnabled(true);
        if ( this.contigInfoPanel != null )
            this.contigInfoPanel.hideProgressBar();
        this.setCursor(new Cursor((Cursor.DEFAULT_CURSOR)));
    }

    @Override
    public java.util.List<MapAlignment> constructMapAligment(SimpleRestrictionMap referenceMap, java.util.List<SimpleRestrictionMap> unalignedMaps, String baseDir) {
        
        java.util.List<MapAlignment> alignments = new LinkedList<>();
        MapAlignment m_alignment;
        
        //Reads the contigs.result file in the working directory and construct one alignment map for each one of the maps in it
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(baseDir + File.separator + "contigs.result")));
            String line;
            Scanner sc;
            int startIndex = - 1;
            double p_value = -1;
            
            //Check for aligned maps in every line of the file
            while ( br.ready() ){
                line = br.readLine();
                
                //Every line is a potential aligned map and it needs to check what could it be
                for ( SimpleRestrictionMap m : unalignedMaps ){
                    if ( line.startsWith(m.getName()) ){
                        
                        line = line.replaceFirst(m.getName(), "");
                       
                        //Obtains the basic information of the alignment
                        sc = new Scanner(line);
                        while ( sc.hasNext() ){
                            
                            switch (sc.next()) {
                                case "Start:":
                                    startIndex = sc.nextInt();
                                    break;
                                case "P-value:":
                                    p_value = sc.nextDouble();
                                    break;
                                default:
                                    break;
                            }
                            
                        }
                        
                        //There is not enough information to create the alignment map
                        if ( startIndex == -1 || p_value == -1 )
                            break;
                        
                        //Creates the MapAlignment object with the above information
                        m_alignment = new StandardMapAlignment();
                        m_alignment.setAlignedMapName( m.getName() + " aligned to " + referenceMap.getName());
                        m_alignment.setOrientation("N"); //TODO: Chech this type of orientation
                        m_alignment.setPvalue(p_value);
                        m_alignment.setReferenceMapName(referenceMap.getName());
                        m_alignment.setScore(0.0);

                        java.util.List<RestrictionFragment> res_fragments = m.getFragments();
                        for ( int i = 0; i < res_fragments.size(); i++ ){
                            m_alignment.addAlignment(i, startIndex, startIndex);
                            startIndex++;
                        }

                        alignments.add(m_alignment);
                        break;
                    }
                }                                
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MapGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MapGUI.class.getName()).log(Level.SEVERE, null, ex);
        }catch( Exception e ){
            
        }
        
        return alignments;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if ( e.getSource() == linesFrequency ){
            AlignLines.LINE_FREQUENCY = (int) linesFrequency.getValue();
        }else if ( e.getSource() == fragmentBoxWidthChooser ){
            ContigDrawer.BOX_WIDTH = (double) fragmentBoxWidthChooser.getValue() / 2.0;
        }else if ( e.getSource() == minimumGapSizeChooser ){
            AlignLines.MINIMUM_GAP_SIZE = (double) minimumGapSizeChooser.getValue();
        }else if( e.getSource() == emptyFragmentBoxWidthChooser ){
            ContigDrawer.EMPTY_BOX_WIDTH = (double) emptyFragmentBoxWidthChooser.getValue() / 2.0;
        }else if( e.getSource() == AlignmentLineWidthChooser ){
            ContigDrawer.ALIGNMENT_LINE_WIDTH = (double) AlignmentLineWidthChooser.getValue() / 2.0;
        }
    }

    class KeyResponder extends AbstractAction{
        
        @Override
        public void actionPerformed(ActionEvent e) {
            pan_btn.setBackground(UNSELECTED_BUTTON_COLOR);
            pointer_btn.setBackground(UNSELECTED_BUTTON_COLOR);
            zoomin_btn.setBackground(UNSELECTED_BUTTON_COLOR);
            zoomout_btn.setBackground(UNSELECTED_BUTTON_COLOR);

            mapsPanel.setMouseState(MapViewerMouseState.NONE);
        }
        
    }
    
    @Override
    public void restrictionMap2OptFormat(SimpleRestrictionMap map, String optFilename) throws IOException{
        double default_dev = 0.01;
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(optFilename));            
        
        for( RestrictionFragment fg : map.getFragments()){
            double length_kb = ( (double) fg.getMassInBp()) / 1000;
            bw.write(length_kb + " " + default_dev);
            bw.newLine();
        }
        bw.close();
    }
    
    @Override
    public void restrictionMaps2SilicoFormat(java.util.List<SimpleRestrictionMap> maps, String silicoFilename) throws IOException{
        double default_dev = 0.01;
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(silicoFilename));
        
        for ( RestrictionMap m : maps ){
            
            //List of fragments in the map
            java.util.List<RestrictionFragment> frags = m.getFragments();
            int totalMass = 0;
            String cutPoints = "";
            for( int i = 0; i < frags.size(); i++){
                
                RestrictionFragment fg = frags.get(i);
                totalMass += fg.getMassInBp();
                
                //Collects all the cut points in a single line
                if ( i < frags.size() - 1 )
                    cutPoints += totalMass + " ";
            }
            
            //Writes the name, total size and total number of cuts in the map            
            bw.write(m.getName() + "\t" + totalMass + " " + (frags.size() - 1));
            bw.newLine();
            bw.write(cutPoints);
            bw.newLine();
        }
        bw.close();
    }
    
    /**
     * Perform the alignment
     * @param basicFolder
     * @param silicoFile
     * @param optFile 
     */
    @Override
    public void alignMaps(String basicFolder, String silicoFile, String optFile){
        
        //Creates the "commands" for excecuting the alignment procedures
        String commandPath = "soma" +  File.separator + "bin" +  File.separator ;
        LinkedList <String> cmds = new LinkedList<>();
        String st = commandPath + "match" + " -s @silico -m @opt -p 0.01 -f 0.01 -o @basic_folder/contigs";
        String completeCmd = st.replaceAll("@basic_folder", basicFolder)
                .replace("@silico", silicoFile)
                .replace("@opt", optFile);
        cmds.add(completeCmd);
        cmds.add( commandPath + "place_rest.pl @basic_folder/contigs".replace("@basic_folder", basicFolder));
        cmds.add( commandPath + "schedule @basic_folder/contigs".replace("@basic_folder", basicFolder));
        cmds.add( commandPath + "collect_results.pl @basic_folder/contigs".replace("@basic_folder", basicFolder));       
        
        //Execute the alignment via calls to an external library
        try {
            String s;
            for ( String cmd : cmds ){
                Process p = Runtime.getRuntime().exec(cmd);                  
                int ec = p.waitFor();
                
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(MapGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static AlignmentColorTheme getAlignmentColorTheme(){
        return theme;
    }
    
    public static void setAlignmentColorTheme(int idx){
        theme = THEME_LIST.get(idx);
    }    
    
    private static java.util.List<AlignmentColorTheme> loadAlignmentColorThemes() {
        // Code from JWhich
        // ======
        // Translate the package name into an absolute path
        java.util.List<AlignmentColorTheme> themeList = new LinkedList<>();
        String pckgname = "edu.wisc.lmcg.gui.themes";
        String name = new String(pckgname);
        if (!name.startsWith("/")) {
            name = "/" + name;
        }        
        name = name.replace('.','/');
        
        // Get a File object for the package
        URL url = MapGUI.class.getResource(name);
        File directory = new File(url.getFile());
        // New code
        // ======
        if (directory.exists()) {
            // Get the list of the files contained in the package
            String [] files = directory.list();
            for (int i=0;i < files.length;i++) {
                 
                // we are only interested in .class files
                if (files[i].endsWith(".class")) {
                    // removes the .class extension
                    String classname = files[i].substring(0,files[i].length()-6);
                    try {
                        // Try to create an instance of the object
                        Object o = Class.forName(pckgname+"."+classname).newInstance();
                        if (o instanceof AlignmentColorTheme) {
                            themeList.add((AlignmentColorTheme) o);
                        }
                    } catch (ClassNotFoundException cnfex) {
                        System.err.println(cnfex);
                    } catch (InstantiationException iex) {
                        // We try to instantiate an interface
                        // or an object that does not have a 
                        // default constructor
                    } catch (IllegalAccessException iaex) {
                        // The class is not public
                    }
                }
            }
        }
        return themeList;
    }
}
