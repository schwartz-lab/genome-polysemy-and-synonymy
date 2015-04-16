/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wisc.lmcg.gui;

import static edu.wisc.lmcg.gui.MapGUI.getScalatedIcon;
import edu.wisc.lmcg.map.RestrictionMap;
import edu.wisc.lmcg.svg.DynamicElement;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.swing.svg.AbstractJSVGComponent;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGRect;
import org.xml.sax.InputSource;

/**
 *
 * @author dipaco
 */
public final class ContigDrawer {

    //Singleton instance
    private static ContigDrawer singletonInstance = null;

    private SVGDocument doc;
    private AbstractJSVGComponent canvas;
    private AffineTransform transform;
    private final Color ALIGNMENT_LINE_COLOR = new Color(224, 224, 224);
    public final String INIT_START_ALIGNMENT_LINE_PREFIX = "GPSStartEndAlignmentLines";
    public final String INTERMEDIATE_ALIGNMENT_LINE_PREFIX = "GPSIntermediateAlignmentLines";
    public final String FRAGMENT_INFO_ELEMENT = "fragInfoRect";
    public static double BOX_WIDTH = 0.02;
    public static double EMPTY_BOX_WIDTH = 0.02;
    public static double ALIGNMENT_LINE_WIDTH = 0.02;
    private Element root = null;
    private final StringBuilder sb_helper = new StringBuilder(0);
    private final Map<String, MapSVGElements> mapsElements;
    private final Map<String, AlignmentSVGElements> alignments;
    private Element fragmentInfoRect;
    private Element fragmentInfoRectText;
    private final double DEFAULT_MAX_CANVAS_SIDE = 2000;
    private double canvas_width = 0;
    private double canvas_height = 0;
    private double textScaleX = 1.0;
    private double textScaleY = 1.0;
    private boolean isShowingInfoRect = false;
    private final DecimalFormat df = new DecimalFormat("#");
    private Element clickedAlignment;
    private Element clickedMap;
    private JPopupMenu rightClickMenu;
    private final JMenuItem toggleAlignLines, deleteMap, flipMap;
    private final String DELETE_ICON = "/edu/wisc/lmcg/resources/delete_icon.png";
    private final String HIDE_ICON = "/edu/wisc/lmcg/resources/hide_icon.png";
    private final String FLIP_MAP_ICON = "/edu/wisc/lmcg/resources/flip_map.png";

    private ContigDrawer() {
        mapsElements = new HashMap<>();
        alignments = new HashMap<>();
        df.setMaximumFractionDigits(10);

        rightClickMenu = new JPopupMenu();

        toggleAlignLines = new JMenuItem("Hide alignment");
        toggleAlignLines.setIcon(new ImageIcon(getScalatedIcon(HIDE_ICON, 16, 16)));
        toggleAlignLines.addActionListener((ActionEvent e) -> {
            canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {

                AlignmentSVGElements d = alignments.get(clickedAlignment.getAttribute("id"));
                d.intermediateLinesElement.setAttribute("visibility", "hidden");
                d.startEndLinesElement.setAttribute("visibility", "hidden");
                d.alignmentElement.setAttribute("visibility", "hidden");
            });
        });
        rightClickMenu.add(toggleAlignLines);

        deleteMap = new JMenuItem("Delete alignment");
        deleteMap.setIcon(new ImageIcon(getScalatedIcon(DELETE_ICON, 16, 16)));
        deleteMap.addActionListener((ActionEvent e) -> {
            canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {

                String alignmentId = clickedAlignment.getAttribute("id");
                AlignmentSVGElements d = alignments.get(alignmentId);
                this.doc.getDocumentElement().removeChild(d.alignmentElement);
                alignments.remove(alignmentId);
            });
        });
        rightClickMenu.add(deleteMap);

        flipMap = new JMenuItem("Flip map");
        flipMap.setIcon(new ImageIcon(getScalatedIcon(FLIP_MAP_ICON, 16, 16)));
        flipMap.addActionListener((ActionEvent e) -> {
            canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {
                startLoadingState();
                flipMap(clickedMap.getAttribute("id"));
                stopLoadingState();
            });
        });
        rightClickMenu.add(flipMap);
    }

    private void createFragmentRect() throws DOMException {

        Point2D rectSize = real2CanvasCoords(5 * MapDrawing.DEFAULT_FRAGMENT_HEIGHT, 5 * MapDrawing.DEFAULT_FRAGMENT_HEIGHT);
        double rectWidth = rectSize.getX();
        double rectHeight = rectSize.getY();
        double rectStrokeLine = 0.02 * rectWidth;

        DynamicElement de = new DynamicElement();
        Attr at = doc.createAttribute("id");
        sb_helper.setLength(0);
        sb_helper.append("fragInfoRect");
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        //stroke-dasharray="1,1" fill-opacity="1.0" fill="rgb(255, 255, 255)" style="stroke:rgb(255,255,0)
        at = doc.createAttribute("stroke-width");
        sb_helper.setLength(0);
        sb_helper.append(df.format(rectStrokeLine));
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        at = doc.createAttribute("fill");
        sb_helper.setLength(0);
        sb_helper.append("rgb(255, 255, 255)");
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        at = doc.createAttribute("width");
        sb_helper.setLength(0);
        sb_helper.append(df.format(rectWidth));
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        at = doc.createAttribute("height");
        sb_helper.setLength(0);
        sb_helper.append(df.format(rectHeight));
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        at = doc.createAttribute("stroke");
        sb_helper.setLength(0);
        sb_helper.append("rgb(255, 255, 0)");
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        //Create a text node for the map name
        DynamicElement detext = new DynamicElement();

        at = doc.createAttribute("x");
        at.setValue("10.0");
        de.setAttribute(at);

        at = doc.createAttribute("y");
        at.setValue("10.0");
        de.setAttribute(at);

        at = doc.createAttribute("fill");
        at.setValue(color2rgbString(ALIGNMENT_LINE_COLOR));
        de.setAttribute(at);

        at = doc.createAttribute("font-size");        
        at.setValue(df.format(MapDrawing.DEFAULT_FRAGMENT_HEIGHT * canvas_height / MapDrawing.getMaxY()));
        de.setAttribute(at);

        fragmentInfoRectText = createElement("text", detext);
        fragmentInfoRectText.appendChild(doc.createTextNode("edd"));

        fragmentInfoRect = createElement("rect", de);
        //doc.getRootElement().appendChild(fragmentInfoRect);
        addToElement(fragmentInfoRect, fragmentInfoRectText);
        addToElement(root, fragmentInfoRect);
    }

    public static ContigDrawer getDrawer() {

        if (singletonInstance == null) {
            singletonInstance = new ContigDrawer();
        }

        return singletonInstance;

    }

    public static ContigDrawer getDrawer(SVGDocument doc, AbstractJSVGComponent canvas) {
        ContigDrawer instance = getDrawer();
        instance.setSvgDocument(doc);
        instance.setSvgCanvas(canvas);
        return instance;
    }

    public void setSvgDocument(SVGDocument svgDoc) {
        doc = svgDoc;
        this.root = doc.getDocumentElement();
    }

    public void setSvgCanvas(AbstractJSVGComponent canvas) {
        this.canvas = canvas;
        transform = this.canvas.getViewBoxTransform();
    }

    public Element createUsedElement(String idDefinedElement, DynamicElement newDynamicElement) {
        Element newElement = doc.createElementNS(
                SVGDOMImplementation.SVG_NAMESPACE_URI, "svg:use");
        newElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href",
                "#" + idDefinedElement);
        newElement.setAttributeNS(null, "id", newDynamicElement.getIdElement());
        newElement.setAttributeNS(null, "x", df.format(newDynamicElement.getPosition().getX()) + "");
        newElement.setAttributeNS(null, "y", df.format(newDynamicElement.getPosition().getY()) + "");
        newElement.setAttributeNS(null, "transform", "scale("
                + newDynamicElement.getScaleX() + ","
                + newDynamicElement.getScaleY() + ") rotate("
                + newDynamicElement.getAngle() + ")");

        // Add the new element        
        return newElement;
    }

    public final Element createElement(String name, DynamicElement de) {
        sb_helper.setLength(0);
        sb_helper.append("svg:").append(name);
        Element newElement = doc.createElementNS(
                SVGDOMImplementation.SVG_NAMESPACE_URI, sb_helper.toString());

        for (Attr a : de.getAttributes()) {
            newElement.setAttributeNode(a);
        }

        return newElement;
    }

    public double getCanvasWidth() {
        return canvas_width;
    }

    public double getCanvasHeight() {
        return canvas_height;
    }

    public void setCanvasDimension() {

        double maxX = MapDrawing.getMaxX();
        double maxY = MapDrawing.getMaxY();

        if (maxX > maxY) {
            canvas_width = DEFAULT_MAX_CANVAS_SIDE;
            canvas_height = DEFAULT_MAX_CANVAS_SIDE * maxY / maxX;
        } else {
            canvas_height = DEFAULT_MAX_CANVAS_SIDE;
            canvas_width = DEFAULT_MAX_CANVAS_SIDE * maxX / maxY;
        }

        doc.getRootElement().setAttribute("width", "" + canvas_width);
        doc.getRootElement().setAttribute("height", "" + canvas_height);
    }

    public void setCanvasWidth(double width) {
        setCanvasDimension(width, canvas_height);
    }

    public void setCanvasHeight(double height) {
        setCanvasDimension(canvas_width, height);
    }

    public void setCanvasDimension(double width, double height) {

        canvas_width = width;
        canvas_height = height;

        if (canvas.getUpdateManager() != null) {
            canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {
                doc.getRootElement().setAttribute("width", "" + canvas_width);
                doc.getRootElement().setAttribute("height", "" + canvas_height);
            });
        } else {
            doc.getRootElement().setAttribute("width", "" + canvas_width);
            doc.getRootElement().setAttribute("height", "" + canvas_height);
        }
    }

    public void createFragment(FragRectangle frag, String mapDrawingName) {

        Point2D coords = real2CanvasCoords(frag.getX(), frag.getY());
        Point2D size = real2CanvasCoords(frag.getWidth(), frag.getHeight());
        double height = size.getY();
        double width = size.getX();

        Color fill = frag.getColor();

        double radius = 0.02 * height;
        double border_width = BOX_WIDTH * height;
        Element e = createRoundRect(
                fill, //Background Color
                coords.getX(),
                coords.getY(),
                (width - border_width > 0) ? width - border_width : width,
                height,
                radius,
                radius,
                Color.BLACK,
                border_width);
        e.setAttribute("id", "frag:" + mapDrawingName + ":" + frag.getIndex());

        //Adds the width of the fragment to the total size of the map
        if (mapsElements.containsKey(mapDrawingName)) {
            mapsElements.get(mapDrawingName).frags.put(frag.getIndex(), frag);
            mapsElements.get(mapDrawingName).minX = Math.min(coords.getX(), mapsElements.get(mapDrawingName).minX);
            mapsElements.get(mapDrawingName).maxX = Math.max(coords.getX() + width, mapsElements.get(mapDrawingName).maxX);
        }

        addToElement(mapDrawingName, e);

        ((EventTarget) e).addEventListener("mouseover", (Event evt) -> {
            canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {
                MapGUI.contigInfoPanel.setFragmentIndex(frag.getIndex() + "");
                MapGUI.contigInfoPanel.setFragmentSize(frag.getKilobaseString() + " kb");
                int position = (int) (frag.getPosition() * 1000);
                MapGUI.contigInfoPanel.setFragmentPosition(String.format("%,d", position) + " bp");
            });
        }, false);

        ((EventTarget) e).addEventListener("mouseout", (Event evt) -> {
            canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {
                MapGUI.contigInfoPanel.setFragmentIndex("--");
                MapGUI.contigInfoPanel.setFragmentSize("--");
                MapGUI.contigInfoPanel.setFragmentPosition("--");
            });
        }, false);
    }

    public void createEmptyFragment(FragRectangle frag, String mapDrawingName) {

        Point2D coords = real2CanvasCoords(frag.getX(), frag.getY());
        Point2D size = real2CanvasCoords(frag.getWidth(), frag.getHeight());
        double height = size.getY();
        double width = size.getX();

        Color fill = frag.getColor();

        double radius = 0.02 * height;
        double padding = 0.01 * height;
        double border_width = EMPTY_BOX_WIDTH * height;
        Element e = createRoundRect(
                Color.WHITE, //Background Color
                coords.getX() + padding, //Lets give it a 10% padding to the width
                coords.getY() + 0.25 * height,
                (width - 2 * padding > 0) ? width - 2 * padding : width,
                0.5 * height,
                radius,
                radius,
                Color.BLACK,
                border_width);
        e.setAttribute("id", "frag:" + mapDrawingName + ":" + frag.getIndex());

        //Adds the width of the fragment to the total size of the map
        if (mapsElements.containsKey(mapDrawingName)) {
            mapsElements.get(mapDrawingName).frags.put(frag.getIndex(), frag);
            mapsElements.get(mapDrawingName).minX = Math.min(coords.getX(), mapsElements.get(mapDrawingName).minX);
            mapsElements.get(mapDrawingName).maxX = Math.max(coords.getX() + width, mapsElements.get(mapDrawingName).maxX);
        }

        addToElement(mapDrawingName, e);

        ((EventTarget) e).addEventListener("mouseover", (Event evt) -> {
            canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {
                MapGUI.contigInfoPanel.setFragmentIndex(frag.getIndex() + "");
                MapGUI.contigInfoPanel.setFragmentSize(frag.getKilobaseString() + " kbp");
                int position = (int) (frag.getPosition() * 1000);
                MapGUI.contigInfoPanel.setFragmentPosition(String.format("%,d", position) + " bp");                
            });
        }, false);

        ((EventTarget) e).addEventListener("mouseout", (Event evt) -> {
            canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {
                MapGUI.contigInfoPanel.setFragmentIndex("--");
                MapGUI.contigInfoPanel.setFragmentSize("--");
                MapGUI.contigInfoPanel.setFragmentPosition("--");
            });
        }, false);
    }

    public Element createRoundRect(Color fill, double x, double y, double width, double height, double rx, double ry, Color stroke_color, double stroke_width) {

        DynamicElement nel = new DynamicElement();

        Attr at = doc.createAttribute("x");
        sb_helper.setLength(0);
        sb_helper.append(df.format(x));
        at.setValue(sb_helper.toString());
        nel.setAttribute(at);

        at = doc.createAttribute("y");
        sb_helper.setLength(0);
        sb_helper.append(df.format(y));
        at.setValue(sb_helper.toString());
        nel.setAttribute(at);

        at = doc.createAttribute("rx");
        sb_helper.setLength(0);
        sb_helper.append(df.format(rx));
        at.setValue(sb_helper.toString());
        nel.setAttribute(at);

        at = doc.createAttribute("ry");
        sb_helper.setLength(0);
        sb_helper.append(df.format(ry));
        at.setValue(sb_helper.toString());
        nel.setAttribute(at);

        at = doc.createAttribute("width");
        sb_helper.setLength(0);
        sb_helper.append(df.format(width));
        at.setValue(sb_helper.toString());
        nel.setAttribute(at);

        at = doc.createAttribute("height");
        sb_helper.setLength(0);
        sb_helper.append(df.format(height));
        at.setValue(sb_helper.toString());
        nel.setAttribute(at);

        double stroke_opacity = ((double) stroke_color.getAlpha()) / 255.0;
        double opacity = ((double) fill.getAlpha()) / 255.0;

        at = doc.createAttribute("stroke");
        sb_helper.setLength(0);
        sb_helper.append(color2rgbString(stroke_color));
        at.setValue(sb_helper.toString());
        nel.setAttribute(at);

        at = doc.createAttribute("fill");
        sb_helper.setLength(0);
        sb_helper.append(color2rgbString(fill));
        at.setValue(sb_helper.toString());
        nel.setAttribute(at);

        at = doc.createAttribute("fill-opacity");
        sb_helper.setLength(0);
        sb_helper.append(opacity);
        at.setValue(sb_helper.toString());
        nel.setAttribute(at);

        at = doc.createAttribute("stroke-width");
        sb_helper.setLength(0);
        sb_helper.append(df.format(stroke_width));
        at.setValue(sb_helper.toString());
        nel.setAttribute(at);

        at = doc.createAttribute("stroke-opacity");
        sb_helper.setLength(0);
        sb_helper.append(stroke_opacity);
        at.setValue(sb_helper.toString());
        nel.setAttribute(at);

        nel.setAttribute(at);

        return createElement("rect", nel);
    }

    public Element createRoundRect(Color fill, double x, double y, double width, double height, double rx, double ry) {

        return createRoundRect(fill, x, y, width, height, rx, ry, Color.WHITE, 0.0);
    }

    public void drawAlignment(String aligntmentId, String mapId, String refMapId, boolean intermediate_line, double x1, double y1, double x2, double y2, double stroke_width) {

        //If this alignmentId is not still in the hashmap
        AlignmentSVGElements svgels;
        if (alignments.containsKey(aligntmentId)) {
            svgels = alignments.get(aligntmentId);
        } else {
            svgels = new AlignmentSVGElements();
            svgels.intermediate_lines_refOnTop_on = y2 < y1;
            svgels.start_end_lines_refOnTop_on = y2 < y1;
            svgels.alignedMapId = mapId;
            svgels.refMapId = refMapId;

            DynamicElement de = new DynamicElement();
            Attr attr = doc.createAttribute("id");
            attr.setValue(aligntmentId);
            de.setAttribute(attr);
            attr = doc.createAttribute("visibility");
            attr.setValue("visible");
            de.setAttribute(attr);
            svgels.alignmentElement = createElement("g", de);
            addToElement(root, svgels.alignmentElement);

            de = new DynamicElement();
            svgels.startEndLinesElement = createElement("g", de);
            addToElement(svgels.alignmentElement, svgels.startEndLinesElement);

            de = new DynamicElement();
            svgels.intermediateLinesElement = createElement("g", de);
            addToElement(svgels.alignmentElement, svgels.intermediateLinesElement);

            alignments.put(aligntmentId, svgels);

            ((EventTarget) svgels.alignmentElement).addEventListener("click", (Event evt) -> {
                MouseEvent me = (MouseEvent) evt;
                if (me.getButton() == 2) {

                    toggleAlignLines.setEnabled(true);
                    deleteMap.setEnabled(true);
                    flipMap.setEnabled(false);

                    clickedAlignment = (Element) evt.getCurrentTarget();
                    rightClickMenu.show(canvas, me.getClientX(), me.getClientY());
                }
            }, true);
        }

        //Add the line to the especific group of elements (startendlines or intermediatelines)
        Element line = drawLine(ALIGNMENT_LINE_COLOR, x1, y1, x2, y2, stroke_width);
        if (intermediate_line) {
            addToElement(svgels.intermediateLinesElement, line);
        } else {
            addToElement(svgels.startEndLinesElement, line);
        }
    }

    public Element drawLine(Color c, double x1, double y1, double x2, double y2, double strokeWidth) {

        DynamicElement de = new DynamicElement();

        Attr at = doc.createAttribute("x1");
        sb_helper.setLength(0);
        sb_helper.append(df.format(x1));
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        at = doc.createAttribute("x2");
        sb_helper.setLength(0);
        sb_helper.append(df.format(x2));
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        at = doc.createAttribute("y1");
        sb_helper.setLength(0);
        sb_helper.append(df.format(y1));
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        at = doc.createAttribute("y2");
        sb_helper.setLength(0);
        sb_helper.append(df.format(y2));
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        at = doc.createAttribute("stroke");
        sb_helper.setLength(0);
        String fillColorString = color2rgbString(c);
        sb_helper.append(fillColorString);
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        at = doc.createAttribute("stroke-width");
        sb_helper.setLength(0);
        sb_helper.append(df.format(strokeWidth));
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        return createElement("line", de);
    }

    public Element drawCircle(Color c, double x_center, double y_center, double r) {

        DynamicElement de = new DynamicElement();

        Attr at = doc.createAttribute("cx");
        sb_helper.setLength(0);
        sb_helper.append(df.format(x_center));
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        at = doc.createAttribute("cy");
        sb_helper.setLength(0);
        sb_helper.append(df.format(y_center));
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        at = doc.createAttribute("r");
        sb_helper.setLength(0);
        sb_helper.append(df.format(r));
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        at = doc.createAttribute("style");
        String fillColorString = color2rgbString(c);
        sb_helper.setLength(0);
        sb_helper.append("fill:").append(fillColorString);
        at.setValue(sb_helper.toString());
        de.setAttribute(at);

        return createElement("circle", de);
    }

    private String color2rgbString(Color c) {

        if (c.getRed() == 255 && c.getGreen() == 255 && c.getBlue() == 255) {
            return "white";
        }

        StringBuilder builder = new StringBuilder(16);
        builder.append("rgb(").append(c.getRed()).append(",").append(c.getGreen()).append(",").append(c.getBlue()).append(")");
        return builder.toString();
    }

    public void createMapDrawing(String name, double x, double y) {
        
        double mapRealHeihgt = MapDrawing.DEFAULT_FRAGMENT_HEIGHT * canvas_height / MapDrawing.getMaxY();

        //Create a text node for the map name
        DynamicElement de = new DynamicElement();

        Attr at = doc.createAttribute("x");
        at.setValue(df.format(x));
        de.setAttribute(at);

        at = doc.createAttribute("y");
        at.setValue(df.format(y - mapRealHeihgt / 2.0));
        de.setAttribute(at);

        at = doc.createAttribute("fill");
        at.setValue(color2rgbString(ALIGNMENT_LINE_COLOR));
        de.setAttribute(at);

        at = doc.createAttribute("font-size");        
        at.setValue(df.format(mapRealHeihgt));
        de.setAttribute(at);

        at = doc.createAttribute("transform");
        at.setValue("scale(1.0 1.0)");
        de.setAttribute(at);

        at = doc.createAttribute("id");
        at.setValue("title:" + name);
        de.setAttribute(at);

        Element nmd_text = createElement("text", de);
        nmd_text.appendChild(doc.createTextNode(name));

        //Create a new empty map
        de = new DynamicElement();

        at = doc.createAttribute("id");
        at.setValue(name);
        de.setAttribute(at);

        Element nmd = createElement("g", de);

        ((EventTarget) nmd).addEventListener("click", (Event evt) -> {
            MouseEvent me = (MouseEvent) evt;
            if (me.getButton() == 2) {

                clickedMap = (Element) evt.getCurrentTarget();
                toggleAlignLines.setEnabled(false);
                deleteMap.setEnabled(false);
                flipMap.setEnabled(true);

                rightClickMenu.show(canvas, me.getClientX(), me.getClientY());
            }
        }, true);

        // Add the new element
        addToElement(nmd, nmd_text);
        addToElement(root, nmd);

        //Register the new map and his name in the dictionary
        MapSVGElements me = new MapSVGElements();
        me.boxElement = nmd;
        me.titleElement = nmd_text;
        mapsElements.put(name, me);
    }

    public void addToElement(String name, Element e) {

        Element parent = doc.getElementById(name);
        parent.appendChild(e);
    }

    public void addToElement(Element parent, Element child) {
        parent.appendChild(child);

    }

    public void hideAllAlignmentLines() {
        if (canvas.getUpdateManager() == null) {
            return;
        }

        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {

            for (AlignmentSVGElements d : alignments.values()) {
                d.startEndLinesElement.setAttribute("visibility", "hidden");
                d.intermediateLinesElement.setAttribute("visibility", "hidden");
                d.alignmentElement.setAttribute("visibility", "hidden");
            }
        });
    }

    public void showAllAlignmentLines() {

        if (canvas.getUpdateManager() == null) {
            return;
        }

        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {

            for (AlignmentSVGElements d : alignments.values()) {
                d.startEndLinesElement.setAttribute("visibility", "visible");
                d.intermediateLinesElement.setAttribute("visibility", "visible");
                d.alignmentElement.setAttribute("visibility", "visible");
            }
        });
    }

    private void setAlignmentLinesVisibility(String mapDrawingName, boolean show, String prefix) {

        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {

            for (AlignmentSVGElements d : alignments.values()) {
                boolean isParentVisibility = d.alignmentElement.getAttribute("visibility").equals("visible");
                if ((d.refMapId.equals(mapDrawingName) || d.alignedMapId.equals(mapDrawingName))
                        && (isParentVisibility || !show)) {

                    //Set the lines' visibility                
                    String visibilityValue = show ? "visible" : "hidden";
                    switch (prefix) {
                        case INIT_START_ALIGNMENT_LINE_PREFIX:
                            d.startEndLinesElement.setAttribute("visibility", visibilityValue);
                            break;
                        case INTERMEDIATE_ALIGNMENT_LINE_PREFIX:
                            d.intermediateLinesElement.setAttribute("visibility", visibilityValue);
                            break;
                        default:
                            d.startEndLinesElement.setAttribute("visibility", visibilityValue);
                            break;
                    }
                }
            }
        });
    }

    private void flipMap(String name) {

        if (!mapsElements.containsKey(name)) {
            return;
        }

        //Flips every fragment
        MapSVGElements mapElements = mapsElements.get(name);
        for (int fragIdx : mapElements.frags.keySet()) {

            FragRectangle frag = mapElements.frags.get(fragIdx);
            Element fragElement = doc.getElementById("frag:" + name + ":" + fragIdx);

            double x = Double.parseDouble(fragElement.getAttribute("x"));
            Point2D size = real2CanvasCoords(frag.width, 0);
            fragElement.setAttribute("x", df.format(mapElements.minX + mapElements.maxX - x - size.getX()));
        }

        //Flip the alignment lines
        //Moves all the alignment lins in the drawing to fit the map drawing        
        flipAlignmentLines(name);
        flipAlignmentLinesForReferenceMap(name);

        /*//Changes the order of all the fragments
         Element g = mapsElements.get(name).boxElement;
        
         NodeList ns = ((Node) g).getChildNodes();
         for ( int i = 0; i < ns.getLength(); i++){
            
         if ( ns.item(i).getNodeType() == Node.ELEMENT_NODE){
                
         Element e = (Element) ns.item(i);
         if ( e.getNodeName().equals("svg:rect") ){
                    
                    
                    
         }
                
         }
            
         }*/
    }

    public void showIntermediateLines(String mapDrawingName) {
        setAlignmentLinesVisibility(mapDrawingName, true, INTERMEDIATE_ALIGNMENT_LINE_PREFIX);
    }

    public void hideIntermediateLines(String mapDrawingName) {
        setAlignmentLinesVisibility(mapDrawingName, false, INTERMEDIATE_ALIGNMENT_LINE_PREFIX);
    }

    public void showInitStartLines(String mapDrawingName) {
        setAlignmentLinesVisibility(mapDrawingName, true, INIT_START_ALIGNMENT_LINE_PREFIX);
    }

    public void hideInitStartLines(String mapDrawingName) {
        setAlignmentLinesVisibility(mapDrawingName, false, INIT_START_ALIGNMENT_LINE_PREFIX);
    }

    public void moveAlignmentLinesForReferenceMap(String referenceMapName, double xOffset, double yOffset, String prefix) {

        if (canvas.getUpdateManager() == null) {
            return;
        }

        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {

            List<AlignmentSVGElements> alignmentLayers = new LinkedList<>();
            for (AlignmentSVGElements el : alignments.values()) {

                if (el.refMapId.equals(referenceMapName)) {
                    alignmentLayers.add(el);
                }
            }

            for (AlignmentSVGElements d : alignmentLayers) {

                boolean refOnTop;
                Element linesElement;
                switch (prefix) {
                    case INIT_START_ALIGNMENT_LINE_PREFIX:
                        linesElement = d.startEndLinesElement;
                        refOnTop = d.start_end_lines_refOnTop_on;
                        break;
                    case INTERMEDIATE_ALIGNMENT_LINE_PREFIX:
                        linesElement = d.intermediateLinesElement;
                        refOnTop = d.intermediate_lines_refOnTop_on;
                        break;
                    default:
                        linesElement = d.startEndLinesElement;
                        refOnTop = d.start_end_lines_refOnTop_on;
                        break;
                }

                //Flag to indicate if the reference map is above the aligned map
                boolean new_refMapOnTop = refOnTop;

                NodeList nodes = linesElement.getChildNodes();

                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element el = (Element) nodes.item(i);

                        if (el.getNodeName().equals("svg:line")) {

                            double c_x2 = Double.parseDouble(el.getAttribute("x2"));
                            double c_y2 = Double.parseDouble(el.getAttribute("y2"));
                            double c_y1 = Double.parseDouble(el.getAttribute("y1"));
                            double new_x2 = c_x2 + xOffset;
                            double new_y2 = c_y2 + yOffset;
                            Point2D corr = real2CanvasCoords(0.0, MapDrawing.DEFAULT_FRAGMENT_HEIGHT);

                            //applies a correction to the begining and end of the line
                            //if the references map cross the aligned map
                            if (refOnTop) {
                                if (new_y2 > c_y1 + 2 * corr.getY()) {
                                    el.setAttribute("y1", df.format(c_y1 + corr.getY()));
                                    new_y2 = new_y2 - corr.getY();
                                    new_refMapOnTop = false;
                                }
                            } else {
                                if (new_y2 < c_y1 - 2 * corr.getY()) {
                                    el.setAttribute("y1", df.format(c_y1 - corr.getY()));
                                    new_y2 = new_y2 + corr.getY();
                                    new_refMapOnTop = true;
                                }
                            }

                            el.setAttribute("x2", df.format(new_x2));
                            el.setAttribute("y2", df.format(new_y2));
                        }
                    }
                }

                //Fixme: Must be a better way to do this without repeating this code
                switch (prefix) {
                    case INIT_START_ALIGNMENT_LINE_PREFIX:
                        d.start_end_lines_refOnTop_on = new_refMapOnTop;
                        break;
                    case INTERMEDIATE_ALIGNMENT_LINE_PREFIX:
                        d.intermediate_lines_refOnTop_on = new_refMapOnTop;
                        break;
                    default:
                        d.start_end_lines_refOnTop_on = new_refMapOnTop;
                        break;
                }
            }
        });
    }

    @SuppressWarnings("empty-statement")
    public void moveAlignmentLines(String mapDrawingName, double xOffset, double yOffset, String pref) {

        if (canvas.getUpdateManager() == null) {
            return;
        }

        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {

            List<AlignmentSVGElements> alignmentLayers = new LinkedList<>();
            for (AlignmentSVGElements el : alignments.values()) {

                if (el.alignedMapId.equals(mapDrawingName)) {
                    alignmentLayers.add(el);
                }
            }

            for (AlignmentSVGElements d : alignmentLayers) {

                Element mapLinesElement;
                boolean refOnTop;
                switch (pref) {
                    case INIT_START_ALIGNMENT_LINE_PREFIX:
                        mapLinesElement = d.startEndLinesElement;
                        refOnTop = d.start_end_lines_refOnTop_on;
                        break;
                    case INTERMEDIATE_ALIGNMENT_LINE_PREFIX:
                        mapLinesElement = d.intermediateLinesElement;
                        refOnTop = d.intermediate_lines_refOnTop_on;
                        break;
                    default:
                        mapLinesElement = d.startEndLinesElement;
                        refOnTop = d.start_end_lines_refOnTop_on;
                        break;
                }

                //Flag to indicate if the reference map is above the aligned map                
                boolean new_refMapOnTop = refOnTop;

                NodeList nodes = mapLinesElement.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element el = (Element) nodes.item(i);

                        double c_x1 = Double.parseDouble(el.getAttribute("x1"));
                        double c_y1 = Double.parseDouble(el.getAttribute("y1"));
                        double c_y2 = Double.parseDouble(el.getAttribute("y2"));

                        double new_x1 = c_x1 + xOffset;
                        double new_y1 = c_y1 + yOffset;
                        Point2D corr = real2CanvasCoords(0.0, MapDrawing.DEFAULT_FRAGMENT_HEIGHT);

                        //applies a correction to the begining and end of the line
                        //if the references map cross the aligned map
                        if (!refOnTop) {
                            if (new_y1 > c_y2 + 2 * corr.getY()) {
                                el.setAttribute("y2", df.format(c_y2 + corr.getY()));
                                new_y1 = new_y1 - corr.getY();
                                new_refMapOnTop = true;
                            }
                        } else {
                            if (new_y1 < c_y2 - 2 * corr.getY()) {
                                el.setAttribute("y2", df.format(c_y2 - corr.getY()));
                                new_y1 = new_y1 + corr.getY();
                                new_refMapOnTop = false;
                            }
                        }

                        el.setAttribute("x1", df.format(new_x1));
                        el.setAttribute("y1", df.format(new_y1));
                    }
                }
                //Fixme: Must be a better way to do this without repeating this code
                switch (pref) {
                    case INIT_START_ALIGNMENT_LINE_PREFIX:
                        d.start_end_lines_refOnTop_on = new_refMapOnTop;
                        break;
                    case INTERMEDIATE_ALIGNMENT_LINE_PREFIX:
                        d.intermediate_lines_refOnTop_on = new_refMapOnTop;
                        break;
                    default:
                        d.start_end_lines_refOnTop_on = new_refMapOnTop;
                        break;
                }
            }
        });

    }

    private void flipAlignmentLinesForReferenceMap(String referenceMapName) {

        List<AlignmentSVGElements> alignmentLayers = new LinkedList<>();
        for (AlignmentSVGElements el : alignments.values()) {

            if (el.refMapId.equals(referenceMapName)) {
                alignmentLayers.add(el);
            }
        }

        for (AlignmentSVGElements d : alignmentLayers) {

            for (Element linesElement : Arrays.asList(d.startEndLinesElement, d.intermediateLinesElement)) {

                NodeList nodes = linesElement.getChildNodes();

                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element el = (Element) nodes.item(i);

                        if (el.getNodeName().equals("svg:line")) {

                            double c_x2 = Double.parseDouble(el.getAttribute("x2"));

                            MapSVGElements me = mapsElements.get(referenceMapName);
                            double new_x2 = 2 * me.translationX + me.minX + me.maxX - c_x2;
                            el.setAttribute("x2", df.format(new_x2));
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("empty-statement")
    private void flipAlignmentLines(String mapDrawingName) {

        List<AlignmentSVGElements> alignmentLayers = new LinkedList<>();
        for (AlignmentSVGElements el : alignments.values()) {

            if (el.alignedMapId.equals(mapDrawingName)) {
                alignmentLayers.add(el);
            }
        }

        for (AlignmentSVGElements d : alignmentLayers) {

            for (Element mapLinesElement : Arrays.asList(d.startEndLinesElement, d.intermediateLinesElement)) {

                NodeList nodes = mapLinesElement.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element el = (Element) nodes.item(i);

                        double c_x1 = Double.parseDouble(el.getAttribute("x1"));
                        MapSVGElements me = mapsElements.get(mapDrawingName);
                        double new_x1 = 2 * me.translationX + me.minX + me.maxX - c_x1;
                        el.setAttribute("x1", df.format(new_x1));
                    }
                }
            }
        }

    }

    /**
     *
     * @param mapDrawingName
     * @param mapType
     * @param xOffset
     * @param yOffset
     */
    public void moveMap(String mapDrawingName, String mapType, double xOffset, double yOffset) {

        Element mapElement;

        if (!mapsElements.containsKey(mapDrawingName) || canvas.getUpdateManager() == null) {
            return;
        }

        mapElement = mapsElements.get(mapDrawingName).boxElement;

        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {
            String transformText = mapElement.getAttribute("transform");
            Pattern pattern = Pattern.compile("translate\\(-*\\d+\\.\\d+ -*\\d+\\.\\d+\\)");
            Matcher matcher = pattern.matcher(transformText);
            if (matcher.find()) {
                String translateString = matcher.group(0);
                String[] s_coords = translateString.replace("translate(", "").replace(")", "").split(" ");
                double c_x = Double.parseDouble(s_coords[0]);
                double c_y = Double.parseDouble(s_coords[1]);

                mapElement.setAttribute(
                        "transform",
                        transformText.replace(translateString, "translate(" + df.format(c_x + xOffset) + " " + df.format(c_y + yOffset) + ")"));
            } else {
                mapElement.setAttribute(
                        "transform",
                        transformText + "translate(" + df.format(xOffset) + " " + df.format(yOffset) + ")");
            }
            mapsElements.get(mapDrawingName).translationX += xOffset;
            mapsElements.get(mapDrawingName).translationY += yOffset;
        });

        //Moves all the alignment lins in the drawing to fit the map drawing        
        moveAlignmentLines(mapDrawingName, xOffset, yOffset, INIT_START_ALIGNMENT_LINE_PREFIX);
        moveAlignmentLinesForReferenceMap(mapDrawingName, xOffset, yOffset, INIT_START_ALIGNMENT_LINE_PREFIX);
    }

    /**
     * Keep track of the zoom changes made to the canvas, in order to adjust the
     * text and prevents deformations.
     *
     * @param scaleX applied x scale factor
     * @param scaleY applied y scale factor
     */
    public void adjustMapTitle(double scaleX, double scaleY) {

        textScaleX *= scaleX;
        textScaleY *= scaleY;

        if (canvas.getUpdateManager() == null) {
            return;
        }

        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {

            for (MapSVGElements map : mapsElements.values()) {

                Element mapElement = map.titleElement;
                double xTranslation = (1 / scaleX) * Double.parseDouble(mapElement.getAttribute("x"));
                double yTranslation = (1 / scaleY) * Double.parseDouble(mapElement.getAttribute("y"));
                mapElement.setAttribute("transform", "scale(" + textScaleX + " " + textScaleY + ")");
                mapElement.setAttribute("x", df.format(xTranslation));
                mapElement.setAttribute("y", df.format(yTranslation));
            }
        });
    }

    public Point2D real2CanvasCoords(double xReal, double yReal) {

        Point2D realPoint = new Point2D.Double();
        realPoint.setLocation(xReal, yReal);
        return real2CanvasCoords(realPoint);
    }

    public Point2D real2CanvasCoords(Point2D realPoint) {
        Point2D newPoint = new Point2D.Double(realPoint.getX(), realPoint.getY());
        newPoint.setLocation(
                newPoint.getX() * canvas_width / MapDrawing.getMaxX(),
                newPoint.getY() * canvas_height / MapDrawing.getMaxY());
        return newPoint;
    }

    public Point2D screen2RealCoords(double xScreen, double yScreen) throws NoninvertibleTransformException {

        Point2D screenPoint = new Point2D.Double();
        screenPoint.setLocation(xScreen, yScreen);
        return screen2RealCoords(screenPoint);
    }

    public Point2D screen2RealCoords(Point2D screenPoint) throws NoninvertibleTransformException {
        Point2D newPoint = screen2CanvasCoords(screenPoint);
        return canvas2RealCoords(newPoint);
    }

    public Point2D screen2CanvasCoords(Point2D screenPoint) throws NoninvertibleTransformException {
        transform = canvas.getViewBoxTransform();
        //transform = canvas.getViewingTransform();
        //transform = canvas.getPaintingTransform();
        //transform = canvas.getInitialTransform();
        //transform = canvas.getRenderingTransform();
        Point2D pt = new Point2D.Double();
        transform.inverseTransform(screenPoint, pt);
        return pt;
    }

    public Point2D screen2CanvasCoords(double screenX, double screenY) throws NoninvertibleTransformException {

        Point2D point = new Point2D.Double();
        point.setLocation(screenX, screenY);
        return screen2CanvasCoords(point);
    }

    public Point2D canvas2ScreenCoords(Point2D screenPoint) throws NoninvertibleTransformException {
        transform = canvas.getViewBoxTransform();
        Point2D pt = new Point2D.Double();
        transform.transform(screenPoint, pt);
        return pt;
    }

    public Point2D canvas2ScreenCoords(double canvasX, double canvasY) throws NoninvertibleTransformException {

        Point2D point = new Point2D.Double();
        point.setLocation(canvasX, canvasY);
        return canvas2ScreenCoords(point);
    }

    public Point2D canvas2RealCoords(Point2D canvasPoint) throws NoninvertibleTransformException {

        Point2D newPoint = new Point2D.Double();
        newPoint.setLocation(
                canvasPoint.getX() * MapDrawing.getMaxX() / canvas_width,
                canvasPoint.getY() * MapDrawing.getMaxY() / canvas_height);
        return newPoint;

    }

    public Point2D canvas2RealCoords(double canvasX, double canvasY) throws NoninvertibleTransformException {

        Point2D point = new Point2D.Double();
        point.setLocation(canvasX, canvasY);
        return canvas2RealCoords(point);
    }

    public void resetDrawer() {
        this.mapsElements.clear();
        this.alignments.clear();
        this.canvas_width = 0.0;
        this.canvas_height = 0.0;
        this.textScaleX = 1;
        this.textScaleY = 1;
        this.fragmentInfoRect = null;
    }

    public void showFragmentInfo(FragRectangle frag) {

        //set the element which represents the rectangle to show the fragment information:
        if (fragmentInfoRect == null) {
            createFragmentRect();
        }

        hideFragmentInfo();

        Point2D realcoords = real2CanvasCoords(frag.x, frag.y);
        fragmentInfoRect.setAttribute("x", df.format(realcoords.getX()));
        fragmentInfoRect.setAttribute("y", df.format(realcoords.getY()));
        fragmentInfoRect.setAttribute("visibility", "visible");

        fragmentInfoRectText.setTextContent(
                "Size: " + frag.getKilobaseString()
                + "\nIndex: " + frag.getIndex()
                + "\nPosition: " + frag.getPosition());
        fragmentInfoRectText.appendChild(doc.createTextNode(""));
    }

    public void hideFragmentInfo() {

        //set the element which represents the rectangle to show the fragment information:
        if (fragmentInfoRect == null) {
            createFragmentRect();
        }

        fragmentInfoRect.setAttribute("visibility", "hidden");

        isShowingInfoRect = false;
    }

    public double getTextScaleX() {
        return textScaleX;
    }

    public double getTextScaleY() {
        return textScaleY;
    }

    public Document crop(Rectangle.Double aoi, boolean mergeFragments) throws NullPointerException, IOException {
        // Create a rectangle at the position and size we wish to crop to
        SVGRect rectangle = this.doc.getRootElement().createSVGRect();
        rectangle.setX(new Float(aoi.x));
        rectangle.setY(new Float(aoi.y));
        rectangle.setWidth(new Float(aoi.width));
        rectangle.setHeight(new Float(aoi.height));

        NodeList croppedNodes = this.doc.getRootElement().getIntersectionList(rectangle, null);

        DOMImplementation dom = GenericDOMImplementation.getDOMImplementation();
        Document newSVGDocument = dom.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);

        double scaleX = 1.0 / getTextScaleX();
        double scaleY = 1.0 / getTextScaleY();

        double size_factor;
        if (aoi.width > aoi.height) {
            size_factor = 2000 / (scaleX * aoi.width);
        } else {
            size_factor = 2000 / (scaleY * aoi.height);
        }

        newSVGDocument.getDocumentElement().setAttribute("width", "" + (size_factor * scaleX * aoi.width));
        newSVGDocument.getDocumentElement().setAttribute("height", "" + (size_factor * scaleY * aoi.height));

        Element g = newSVGDocument.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg:g");
        g.setAttribute(
                "transform",
                "translate(" + (-size_factor * scaleX * aoi.x) + " " + (-size_factor * scaleY * aoi.y) + ") "
                + "scale(" + (size_factor * scaleX) + " " + (size_factor * scaleY) + ")");
        newSVGDocument.getDocumentElement().appendChild(g);

        Map<String, Element> newMapNodes = new HashMap<>();
        for (int i = 0; i < croppedNodes.getLength(); i++) {

            // Create a duplicate node and transfer ownership of the
            // new node into the destination document
            Node newNode = newSVGDocument.importNode(croppedNodes.item(i), true);

            if (newNode.getNodeType() == Node.ELEMENT_NODE && (newNode.getNodeName().equals("svg:rect") || newNode.getNodeName().equals("svg:text"))) {
                Element frag = (Element) newNode;
                String[] parts = frag.getAttribute("id").split(":");

                if ((parts.length == 3 && parts[0].equals("frag"))
                        || (parts.length == 2 && parts[0].equals("title"))) {
                    if (newMapNodes.containsKey(parts[1])) {
                        newMapNodes.get(parts[1]).appendChild(newNode);
                    } else {

                        Element parentMap = (Element) newSVGDocument.importNode((Node) this.mapsElements.get(parts[1]).boxElement, false);
                        parentMap.appendChild(newNode);
                        g.appendChild(parentMap);
                        newMapNodes.put(parts[1], parentMap);
                    }
                } else {
                    // Make the new node an actual item in the target document
                    g.appendChild(newNode);
                }
            } else {
                // Make the new node an actual item in the target document
                g.appendChild(newNode);
            }
        }

        //Delete the zoomRect element used to display the selected area when zooming
        Element zr = newSVGDocument.getElementById("zoomRect");
        if (zr != null) {
            g.removeChild(zr);
        }

        //Delete the fragInfoRect element used to display the information of an individual fragment
        Element fi = newSVGDocument.getElementById("fragInfoRect");
        if (fi != null) {
            g.removeChild(fi);
        }

        newSVGDocument.normalizeDocument();
        return newSVGDocument;
    }    
    
    public Image canvasImageToPng(String filename, Rectangle.Double aoi) {

        //Only if there is a valid document
        if (this.doc == null) {
            return null;
        }

        // Create a JPEG transcoder
        PNGTranscoder t = new PNGTranscoder();

        // Set the transcoding hints.        
        try {

            Document newSVGDocument = crop(aoi, false);

            //Replaces the bright elements for dark ones to enhance the contrast
            DOMSource domSource = new DOMSource(newSVGDocument);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            InputSource is = new InputSource(new StringReader(writer.toString().replace("rgb(224,224,224)", "rgb(32,32,32)")));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document d = factory.newDocumentBuilder().parse(is);

            //The transcoder's input is the SVG document on the canvas.
            TranscoderInput input = new TranscoderInput(d);

            // Create the transcoder output.
            OutputStream ostream = new FileOutputStream(filename);
            TranscoderOutput output = new TranscoderOutput(ostream);

            // Save the image.
            t.transcode(input, output);

            // Flush and close the stream.
            ostream.flush();
            ostream.close();

            ImageIcon im = new ImageIcon(filename);
            return im.getImage();

        } catch (Exception ex) {
            return null;
        }
    }

    public boolean canvasImageToSVGFile(String filename, Rectangle.Double aoi) throws NullPointerException, IOException{
        return canvasImageToSVGFile(filename, aoi, false);
    }
    
    public boolean canvasImageToSVGFile(String filename, Rectangle.Double aoi, boolean mergeRectangles) throws NullPointerException, IOException {

        //Only if there is a valid document
        if (this.doc == null) {
            return false;
        }

        Document newSVGDocument = crop(aoi, mergeRectangles);
        try {

            OutputFormat format = new OutputFormat(newSVGDocument);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            format.setEncoding("UTF-8");
            format.setLineSeparator("\n");
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(newSVGDocument);

            File tempFile = File.createTempFile("temp", Long.toString(System.nanoTime()));
            FileWriter fstream = new FileWriter(tempFile);
            fstream.write(out.toString());
            fstream.flush();
            fstream.close();

            BufferedReader br = new BufferedReader(new FileReader(tempFile));
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

            while (br.ready()) {
                String line = br.readLine();
                line = line.replace("rgb(224,224,224)", "rgb(32,32,32)");

                bw.write(line);
            }

            br.close();
            bw.close();

            tempFile.delete();
            return true;

        } catch (Exception ioe) {
            System.out.println("Unable to write to file in crop: "
                    + ioe.getLocalizedMessage());
            return false;
        }
    }

    private void startLoadingState() {
        JFrame frame = (JFrame) SwingUtilities.windowForComponent(this.canvas);
        if (frame != null) {
            this.canvas.setEnabled(false);
        }
        this.canvas.setCursor(new Cursor((Cursor.WAIT_CURSOR)));
    }

    private void stopLoadingState() {
        JFrame frame = (JFrame) SwingUtilities.getRoot(this.canvas);
        if (frame != null) {
            this.canvas.setEnabled(true);
        }
        this.canvas.setCursor(new Cursor((Cursor.DEFAULT_CURSOR)));
    }
    
    public void setMapNamesVisibility(boolean visible){
        
        if ( canvas.getUpdateManager() == null )
            return;
        
        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(() -> {
            
            String visibilityValue = "";
            if ( visible ){
                visibilityValue = "visible";
            }else{
                visibilityValue = "hidden";
            }
            
            for ( MapSVGElements me : mapsElements.values() ){
                me.titleElement.setAttribute("visibility", visibilityValue);
            }
        });
    }

    class AlignmentSVGElements {

        public Element alignmentElement;
        public Element startEndLinesElement;
        public Element intermediateLinesElement;
        public boolean start_end_lines_refOnTop_on = true;
        public boolean intermediate_lines_refOnTop_on = true;
        public String alignedMapId;
        public String refMapId;
    }

    class MapSVGElements {

        public Element boxElement;
        public Element titleElement;
        public double translationX = 0.0;
        public double translationY = 0.0;
        public double minX = Double.MAX_VALUE;
        public double maxX = Double.MIN_VALUE;
        public final Map<Integer, FragRectangle> frags = new TreeMap<>();
    }
}
