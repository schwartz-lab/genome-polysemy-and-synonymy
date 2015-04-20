/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wisc.lmcg.gui.themes;

import edu.wisc.lmcg.gui.FragmentColorType;
import java.awt.Color;

/**
 *
 * @author dipaco
 */
public class DefaultAlignmentColorTheme extends AlignmentColorTheme{

    @Override
    public Color getNormalColor(FragmentColorType mapType) {
        return new Color(12, 182, 186);
    }

    @Override
    public Color getMissingCutColor() {
        return new Color(32, 54, 215);
    }

    @Override
    public Color getExtraCutColor() {
        return new Color(223, 4, 235);
    }

    @Override
    public Color getOverlappingColor() {
        return new Color(230, 0, 128);
    }
    
}
