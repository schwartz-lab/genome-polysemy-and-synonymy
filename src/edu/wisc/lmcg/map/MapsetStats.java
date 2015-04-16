package edu.wisc.lmcg.map;

import java.util.*;
import java.lang.*;

import edu.wisc.lmcg.alignment.*;

/**
 * MapsetStats is a class that contains stats for a set of RestrictionMaps.
 */
public class MapsetStats {

    private long mp_NumberMolecules;
    private long mp_NumberFragments;
    private double mp_AverageMoleculeSize;
    private double mp_AverageFragSize;
    private double mp_MaxMolSize;
    private double mp_MinMolSize;
    private double mp_TotalSize;
    private long mp_MaxNumberFragments;
    private long mp_MinNumberFragments;

    public MapsetStats() {
        mp_NumberMolecules = 0;
        mp_NumberFragments = 0;
        mp_AverageMoleculeSize = 0;
        mp_AverageFragSize = 0;
        mp_MaxMolSize = -1;
        mp_MinMolSize = -1;
        mp_TotalSize = 0;
        mp_MaxNumberFragments = -1;
        mp_MinNumberFragments = -1;
    }

    public void addStatsFromMap(RestrictionMap resmap)
            throws NullPointerException {
        if (resmap == null) {
            throw new NullPointerException("Map is null");
        }

        int numfrags = resmap.getNumberFragments();
        int totalmass = resmap.getTotalMassInBP();

        //if this map has no fragments or no mass just return
        if (totalmass <= 0 || numfrags <= 0) {
            return;
        }

        mp_NumberFragments += numfrags;
        mp_NumberMolecules++;

        double totalmasskb = (double) totalmass / (double) 1000.0;

        mp_TotalSize += totalmasskb;

        if (mp_MaxNumberFragments == -1) {
            mp_MaxNumberFragments = numfrags;
        } else {
            if (numfrags > mp_MaxNumberFragments) {
                mp_MaxNumberFragments = numfrags;
            }
        }

        if (mp_MinNumberFragments == -1) {
            mp_MinNumberFragments = numfrags;
        } else {
            if (numfrags < mp_MinNumberFragments) {
                mp_MinNumberFragments = numfrags;
            }
        }

        if (mp_MaxMolSize == -1) {
            mp_MaxMolSize = totalmasskb;
        } else {
            if (totalmasskb > mp_MaxMolSize) {
                mp_MaxMolSize = totalmasskb;
            }
        }

        if (mp_MinMolSize == -1) {
            mp_MinMolSize = totalmasskb;
        } else {
            if (totalmasskb < mp_MinMolSize) {
                mp_MinMolSize = totalmasskb;
            }
        }

        if (mp_TotalSize > 0) {
            mp_AverageMoleculeSize = mp_TotalSize / (double) mp_NumberMolecules;
            mp_AverageFragSize = mp_TotalSize / (double) mp_NumberFragments;
        }

    }

    public boolean addStats(MapsetStats ms)
            throws Exception {
        if (ms == null) {
            throw new NullPointerException("mapsetstats is null");
        }

        mp_NumberMolecules += ms.mp_NumberMolecules;
        mp_NumberFragments += ms.mp_NumberFragments;
        if (ms.mp_MaxMolSize > mp_MaxMolSize) {
            mp_MaxMolSize = ms.mp_MaxMolSize;
        }

        if (ms.mp_MinMolSize > -1 && ms.mp_MinMolSize < mp_MinMolSize) {
            mp_MinMolSize = ms.mp_MinMolSize;
        }

        if (ms.mp_MaxNumberFragments > mp_MaxNumberFragments) {
            mp_MaxNumberFragments = ms.mp_MaxNumberFragments;
        }

        if (ms.mp_MinNumberFragments > -1
                && ms.mp_MinNumberFragments < mp_MinNumberFragments) {

            mp_MinNumberFragments = ms.mp_MinNumberFragments;
        }

        mp_TotalSize += ms.mp_TotalSize;

        if (mp_TotalSize > 0) {
            mp_AverageMoleculeSize = mp_TotalSize / (double) mp_NumberMolecules;
            mp_AverageFragSize = mp_TotalSize / (double) mp_NumberFragments;
        }
        return true;
    }

    public double getTotalSizeKb() {
        return mp_TotalSize;
    }

    public void setTotalSizeKb(double val) {
        mp_TotalSize = val;
    }

    public double getMinMoleculeSizeKb() {
        return mp_MinMolSize;
    }

    public void setMinMoleculeSizeKb(double val) {
        mp_MinMolSize = val;
    }

    public double getMaxMoleculeSizeKb() {
        return mp_MaxMolSize;
    }

    public void setMaxMoleculeSizeKb(double val) {
        mp_MaxMolSize = val;
    }

    public double getAverageFragSizeKb() {
        return mp_AverageFragSize;
    }

    public void setAverageFragSizeKb(double val) {
        mp_AverageFragSize = val;
    }

    public double getAverageMoleculeSizeKb() {
        return mp_AverageMoleculeSize;
    }

    public void setAverageMoleculeSizeKb(double val) {
        mp_AverageMoleculeSize = val;
    }

    public void setNumberFragments(long numFrags) {
        mp_NumberFragments = numFrags;
    }

    public long getNumberFragments() {
        return mp_NumberFragments;
    }

    public void setNumberMolecules(long numMollies) {
        mp_NumberMolecules = numMollies;
    }

    public long getNumberMolecules() {
        return mp_NumberMolecules;
    }

    public void setMaxNumberFragments(long val) {
        mp_MaxNumberFragments = val;
    }

    public long getMaxNumberFragments() {
        return mp_MaxNumberFragments;
    }

    public void setMinNumberFragments(long val) {
        mp_MinNumberFragments = val;
    }

    public long getMinNumberFragments() {
        return mp_MinNumberFragments;
    }

}
