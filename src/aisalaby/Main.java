/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aisalaby;

import halbfertiges.Labyrinth2Array;
import halbfertiges.Labyrinthe;

/**
 *
 * @author qohelet
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        int schalter = 2;
        char[][] labyrinth;
        AisaLaby aisa;
        switch (schalter) {

            case 0:
                //Erzeugt neues zufallsgeneriertes, komplexes, mesmerisierendes Labyrint
                aisa = new AisaLaby(new XYWrapper(20, 15), new XYWrapper(0, 3), new XYWrapper(19, 12));
                labyrinth = aisa.getLabyrint(false, true, true);
                Ausgabe.ausgabe(labyrinth);

                break;
            case 1:
                //Erzeugt Labyrinth aus den Archiven heraus
                labyrinth = Labyrinth2Array.getArray(Labyrinthe.treppenUndFlächenDarüber, AisaLaby.FREI, AisaLaby.BELEGT);
                aisa = new AisaLaby(labyrinth);
                System.out.println(Ausgabe.ausgabe(aisa.benutzeLabyrint(false, true, true)));
                break;

            case 2:
                aisa = new AisaLaby(new XYWrapper(150, 20), new XYWrapper(0, 3), new XYWrapper(149, 12));
                aisa.setMezmerize(true);
                aisa.setNormabstand(2);
                System.out.println(Ausgabe.billigansicht(aisa.getLabyrint(true, true, true)));
                break;
            default:
        }
    }

}
