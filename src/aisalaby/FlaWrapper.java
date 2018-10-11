/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aisalaby;

import java.util.Objects;

/**
 *
 * @author qohelet
 */
public class FlaWrapper {

    private final XYWrapper startPunkt, startPunktHarmonisiert;
    private final XYWrapper endPunkt, endPunktHarmonisiert;

    //private final int fläche;
    public FlaWrapper(XYWrapper startPunkt, XYWrapper endPunkt) {
        this.startPunkt = startPunkt;
        this.endPunkt = endPunkt;

        this.startPunktHarmonisiert = new XYWrapper(0, 0);
        this.endPunktHarmonisiert = new XYWrapper((endPunkt.getX() - startPunkt.getX()) + 1, (endPunkt.getY() - startPunkt.getY()) + 1);
        //fläche = getFläche();
    }

    public final int getFläche() {
        int länge = endPunktHarmonisiert.getX();
        int breite = endPunktHarmonisiert.getY();
        
        if(startPunkt.equals(endPunkt))
            return 0;
        
        return länge * breite;
    }

    public XYWrapper getStartPunkt() {
        return startPunkt;
    }

    public XYWrapper getStartPunktHarmonisiert() {
        return startPunktHarmonisiert;
    }

    public XYWrapper getEndPunkt() {
        return endPunkt;
    }

    public XYWrapper getEndPunktHarmonisiert() {
        return endPunktHarmonisiert;
    }

    public boolean istGrößer(FlaWrapper obj) {
        return this.getFläche() > obj.getFläche();
    }

    public boolean isQuadratfläche() {
        return mathematik.Mathematik.isPerfectSquare(Long.parseLong(String.valueOf(getFläche())));
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("Startpunkt: ").append(startPunkt);
        ret.append("\nEndpunkt:   ").append(endPunkt);
        ret.append("\nFläche:     ").append(getFläche());
        return ret.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FlaWrapper) {
            if (((FlaWrapper) (obj)).getStartPunkt().equals(this.getStartPunkt())) {
                return ((FlaWrapper) (obj)).getEndPunkt().equals(this.getEndPunkt());

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
        hash = 41 * hash + Objects.hashCode(this.startPunkt);
        hash = 41 * hash + Objects.hashCode(this.endPunkt);
        return hash;
    }

}
