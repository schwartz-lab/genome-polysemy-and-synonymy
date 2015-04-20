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
public abstract class AlignmentColorTheme {
    
    private final String NORMAL_COLOR_DESCRIPTION = "Normal fragment.";
    private final String MISSING_CUT_COLOR_DESCRIPTION = "Missing cut.";
    private final String EXTRA_CUTL_COLOR_DESCRIPTION = "Extra cut.";
    private final String OVERLAPPING_COLOR_DESCRIPTION = "This fragment overlaps another one.";
    
    /**
     * Returns the color for normal aligned fragments, depending of the type of map
     * @param mapType enumeration indicating whether the map is a reference map or not
     * @return The color for the aligned fragment
     */
    public abstract Color getNormalColor(FragmentColorType mapType);
    
    /**
     * Returns the color for a missing cut
     * @return 
     */
    public abstract Color getMissingCutColor();
    
    /**
     *Returns the color for an extra cut
     * @return 
     */
    public abstract Color getExtraCutColor();
    
    /**
     * Returns the color for an overlapping aligned fragment
     * @return 
     */
    public abstract Color getOverlappingColor();
    
    public final String getNormalColorDescription(){
        return NORMAL_COLOR_DESCRIPTION;
    }
    
    public final String getMissingCutColorDescription(){
        return MISSING_CUT_COLOR_DESCRIPTION;
    }
    
    public final String getExtraCutColorDescription(){
        return EXTRA_CUTL_COLOR_DESCRIPTION;
    }
    
    public final String getOverlappingColorDescription(){
        return OVERLAPPING_COLOR_DESCRIPTION;
    }
    
}
