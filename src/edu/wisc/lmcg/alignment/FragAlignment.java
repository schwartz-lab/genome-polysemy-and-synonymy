package edu.wisc.lmcg.alignment;

/**
 * This class defines an alignment of a fragment.
 */
public class FragAlignment {

    private int mp_Left;
    private int mp_Right;

    /**
     * Constructor that creates FragAlignment.
     *
     * @param left - String containing int representing index of left fragment
     * this FragAlignment aligns to.
     * @param right - String containing int representing index of right fragment
     * this FragAlignment aligns to.
     */
    public FragAlignment(String left, String right) {
        Integer leftInt = new Integer(left);
        Integer rightInt = new Integer(right);

        mp_Left = leftInt;
        mp_Right = rightInt;
    }

    /**
     * Constructor that creates FragAlignment.
     *
     * @param left - containing int representing index of left fragment this
     * FragAlignment aligns to.
     * @param right - containing int representing index of right fragment this
     * FragAlignment aligns to.
     */
    public FragAlignment(int left, int right) {

        mp_Left = left;
        mp_Right = right;

    }

    /**
     * sets the left index of fragment this fragalignment aligns too.
     * @param val
     */
    public void setLeftAlignment(int val) {
        mp_Left = val;
    }

    /**
     * Gets the left index of fragment this FragAlignment aligns to.
     *
     * @return int
     */
    public int getLeftAlignment() {
        return mp_Left;
    }

    public void setRightAlignment(int val) {
        mp_Right = val;
    }

    /**
     * Gets the right index of fragment this FragAlignment aligns to.
     *
     * @return int
     */
    public int getRightAlignment() {
        return mp_Right;
    }

    public boolean isEqual(FragAlignment rfrag) {
        return mp_Left == rfrag.mp_Left && mp_Right == rfrag.mp_Right;
    }

    /**
     * shifts the alignment by value specified. if left or right fragment is -1
     * then the other value after shifting is used.
     * @param shiftval
     * @return 
     */
    public boolean shiftAlignment(int shiftval) {

        if (mp_Left <= -1
                && mp_Right <= -1) {
            return false;
        }

        if (mp_Left <= -1) {
            mp_Left = mp_Right;
        }

        if (mp_Right <= -1) {
            mp_Right = mp_Left;
        }

        if ((shiftval + mp_Left < 0)
                || (mp_Right + shiftval < 0)) {
            return false;
        }

        mp_Left += shiftval;
        mp_Right += shiftval;
        return true;
    }

}
