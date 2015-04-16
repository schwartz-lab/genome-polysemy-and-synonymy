package edu.wisc.lmcg.map;

import java.lang.*;

/**
 * A RestrictionFragment is a fragment from a RestrictionMap. At the moment it
 * only contains the mass of the fragment it represents.
 */
public class RestrictionFragment {

    private int mp_Mass;
    private float mp_StandardWeight;
    private float mp_LengthInPixels;
    private int mp_LengthBasedMass;
    private float mp_MarkupCutConfidence;
    private float mp_LengthInPixelsFlagged;

    private float mp_ContigCutConfidence;

    public RestrictionFragment() {
        initialize();
    }

    public RestrictionFragment(int massBp) {
        initialize();
        mp_Mass = massBp;
    }

    private void initialize() {
        mp_LengthBasedMass = -1;
        mp_Mass = 0;
        mp_StandardWeight = -1;
        mp_LengthInPixels = -1;
        mp_MarkupCutConfidence = -1;
        mp_LengthInPixelsFlagged = -1;
        mp_ContigCutConfidence = -1;
    }

    public void setContigCutConfidence(String cutcon) {
        if (cutcon == null) {
            return;
        }

        Float tempFloat = new Float(cutcon);

        mp_ContigCutConfidence = tempFloat.floatValue();
    }

    public float getContigCutConfidence() {
        if (mp_ContigCutConfidence == -1) {
            return 1;
        }
        return mp_ContigCutConfidence;
    }

    /**
     * This method sets the length in pixels the fragment was flagged by
     * pathfinder. Note any value &gt; 0 means the fragment will be flagged in
     * omari.
     *
     * @param length - String containing float representing length in pixels
     * fragment was flagged.
     */
    public void setLengthInPixelsFlagged(String length) {
        if (length == null) {
            return;
        }

        Float tempFloat = new Float(length);

        mp_LengthInPixelsFlagged = tempFloat.floatValue();
    }

    public float getMarkupCutConfidence() {
        return mp_MarkupCutConfidence;
    }

    /**
     * This method sets the markup cut confidence of the fragment to the right
     * of this fragment. This value ranges from 0 to 1 and is outputted from
     * pathfinder.
     *
     * @param confidence - String containing float representing cut confidence.
     */
    public void setMarkupCutConfidence(String confidence) {
        if (confidence == null) {
            return;
        }

        Float tempFloat = new Float(confidence);

        mp_MarkupCutConfidence = tempFloat.floatValue();
    }

    /**
     * This method sets the length based mass of fragment. The length based mass
     * is calculated in pathfinder2 is calculated by finding a ratio of length
     * to kb of nearby non flagged fragments and using it on this fragment.
     *
     * @param mass - String containing float representing length based mass in
     * kb.
     */
    public void setLengthBasedMassInKb(String mass) {
        if (mass == null) {
            return;
        }

        double kbMassVal = Double.parseDouble(mass);

        kbMassVal = Math.rint((double) 1000.0 * kbMassVal);

        Double bpMassVal = new Double(kbMassVal);

        mp_LengthBasedMass = bpMassVal.intValue();
    }

    public int getLengthBasedMassInBp() {
        return mp_LengthBasedMass;
    }

    public float getStandardWeight() {
        return mp_StandardWeight;
    }

    /**
     * This sets the standard weight of fragment.
     */
    public void setStandardWeight(String weight) {
        if (weight == null) {
            return;
        }

        Float tempFloat = new Float(weight);

        mp_StandardWeight = tempFloat.floatValue();
    }

    /**
     * This method sets the length in pixels of fragment.
     *
     * @param length - String containing float representing length of frag in
     * pixels.
     */
    public void setLengthInPixels(String length) {
        if (length == null) {
            return;
        }

        Float tempFloat = new Float(length);

        mp_LengthInPixels = tempFloat.floatValue();
    }

    public float getLengthInPixels() {
        return mp_LengthInPixels;
    }

    /**
     * This method sets the mass of the fragment in kilobases.
     *
     * @param mass is a String containing the mass in kilobases to set fragment
     * to.
     *
     * @throws Exception if mass passed in is null.
     */
    public void setMassInKb(String mass)
            throws Exception {
        if (mass == null) {
            throw new Exception("value passed to method was null");
        }

        double kbMassVal = (double) Double.parseDouble(mass);
        double onek = (double) 1000.000;

        kbMassVal = Math.rint(onek * kbMassVal);

        Double bpMassVal = new Double(kbMassVal);
        mp_Mass = bpMassVal.intValue();
    }

    /**
     * This method sets the mass of the fragment in base pairs.
     *
     * @param mass is an int containing mass in base pairs
     */
    public void setMassInBp(int mass) {
        mp_Mass = mass;
    }

    /**
     * This method gets the mass of fragment in base pairs. An unset fragment
     * will have a mass of 0.
     *
     * @return int containing mass of fragment in base pairs.
     */
    public int getMassInBp() {
        return mp_Mass;
    }

    public String getFragmentInTextBlock() {
        String fragtxt = Double.toString((double) mp_Mass / (double) 1000.0);
        if (this.mp_ContigCutConfidence != -1) {
            fragtxt += "c" + Float.toString(mp_ContigCutConfidence);
        }
        if (mp_StandardWeight != -1) {
            fragtxt += "W" + Float.toString(mp_StandardWeight);

        }
        if (mp_LengthInPixels != -1) {
            fragtxt += "L" + Float.toString(mp_LengthInPixels);

        }
        if (mp_LengthBasedMass != -1) {
            fragtxt += "C" + Float.toString((float) mp_LengthBasedMass / (float) 1000.0);
        }

        if (mp_MarkupCutConfidence != -1) {
            fragtxt += "X" + Float.toString(mp_MarkupCutConfidence);
        }

        if (mp_LengthInPixelsFlagged != -1) {
            fragtxt += "F" + Float.toString(mp_LengthInPixelsFlagged);
        }

        return fragtxt;
    }

}
