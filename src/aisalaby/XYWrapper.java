/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aisalaby;

/**
 *
 * @author qohelet
 */
public class XYWrapper {

    private final int x;
    private final int y;

    public XYWrapper(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof XYWrapper) {
            if (((XYWrapper) obj).getX() == this.getX()) {
                return ((XYWrapper) obj).getY() == this.getY();
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.x;
        hash = 83 * hash + this.y;
        return hash;
    }

    @Override
    public String toString() {
        return "("+getX()+"/"+getY()+")";
    }
    
}
