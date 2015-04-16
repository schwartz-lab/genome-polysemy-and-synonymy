package edu.wisc.lmcg.gui;

public class Line {

    private double x1, y1, x2, y2; //coordinates of the two ends of this line
    private FragRectangle alRect, refRect; //rectangles at the ends of this molecule

    public Line(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public Line(FragRectangle alRect, FragRectangle refRect, boolean isReversed) {
        this.alRect = alRect;
        this.refRect = refRect;

        x1 = isReversed ? alRect.getMaxX() : alRect.getX();
        y1 = alRect.getY();
        x2 = refRect.getX();
        y2 = refRect.getY();
    }

    public void changePoint1(double x, double y) {
        x1 = x;
        y1 = y;
    }

    public void changePoint2(double x, double y) {
        x2 = x;
        y2 = y;
    }

    public void reposition() {

        x1 = alRect.getX();
        y1 = alRect.getY();
        x2 = refRect.getX();
        y2 = refRect.getY();
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }
}
