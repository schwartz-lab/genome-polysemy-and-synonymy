/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wisc.lmcg.gui.themes;

import edu.wisc.lmcg.gui.MapDrawingType;
import java.awt.Color;

/**
 *
 * @author dipaco
 */
public class SecondaryAlignmentColorTheme extends AlignmentColorTheme{

    @Override
    public Color getNormalColor(MapDrawingType mapType) {
        if ( mapType == MapDrawingType.ReferenceMap )
            return new Color(6, 181, 185);
        else
            return new Color(224, 167, 5);
    }

    @Override
    public Color getMissingCutColor() {
        return new Color(81, 129, 255);
    }

    @Override
    public Color getExtraCutColor() {
        return new Color(255, 75, 113);
    }

    @Override
    public Color getOverlappingColor() {
        return new Color(239, 12, 105);
    }
}
