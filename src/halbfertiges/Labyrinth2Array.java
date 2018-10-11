/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package halbfertiges;


/**
 *
 * @author Mario J. Schwaiger <mario.schwaiger at gmail.com>
 */
public class Labyrinth2Array {

    private static char frei, belegt;

    public static char[][] getArray(String slab, char frei, char belegt) {
        Labyrinth2Array.frei = frei;
        Labyrinth2Array.belegt = belegt;
        String easylab = getEasylab(slab);
        //System.out.println(easylab);

        char[][] rotlab = getRotlab(easylab.replaceAll("[|]", ""));

        //System.out.println("TEST:");
        /*
        for (int y = rotlab[0].length - 1; y >= 0; y--) {
            for (int x = 0; x < rotlab.length; x++) {
                System.out.print(rotlab[x][y]);
            }
            System.out.println("");
        }
*/
        //System.out.println("Rotlab:\n" + Ausgabe.ausgabe(rotlab));
        return getCharLab(rotlab);
    }

    private static String getEasylab(String slab) {
        StringBuilder easylab = new StringBuilder("|");
        for (int i = 0; i < slab.length(); i++) {
            if (slab.charAt(i) == '|' && easylab.charAt(easylab.length() - 1) != '|') {
                easylab.append("|");
            }
            if (slab.charAt(i) == belegt && easylab.charAt(easylab.length() - 1) == '|') {
                easylab.append(belegt);
            }

            if (slab.charAt(i) == '\n') {
                easylab.append('\n');
            }

            if (slab.charAt(i) != belegt && slab.charAt(i) != '|' && slab.charAt(i) != '\n' && easylab.charAt(easylab.length() - 1) == '|') {
                easylab.append(frei);
            }

        }
        return easylab.toString();
    }

    private static char[][] getRotlab(String easylab) {

        //System.out.println(easylab);

        int länge = 0;
        for (int i = 0; i < easylab.length(); i++) {
            if (easylab.charAt(i) == '\n') {
                länge = i;
                break;
            }
        }
        easylab = easylab.replace("\n", "");
        //System.out.println(easylab);

        //System.out.println("[länge][easylab.length() / länge]: [" + länge + "][" + (easylab.length() / länge) + "]");
        char[][] rot = new char[länge][easylab.length() / länge];
        /**
         * length: 300 <br>
         * Zeilenlänge: 20 (länge)<br>
         * Zeilenhöhe: 15
         */

        for (int i = 0; i < easylab.length(); i++) {

                                rot[    ((i % (länge)))    ][    (i / (( länge)))    ] = easylab.charAt(i);
            //System.out.println("rot[" + ((i % (länge))) + "][" + (i / (( länge))) + "](" + i + ") = " + easylab.charAt(i) + "/" + rot[((i / (easylab.length() / länge)))][(i % (easylab.length() / länge))]);
            //System.out.println("rot[" + ((länge-1) - ((i / (easylab.length() / länge)))) + "][" + (((easylab.length() / länge)-1) - (i % (easylab.length() / länge))) + "](" + i + ") = " + easylab.charAt(i) + "/" + rot[((i / (easylab.length() / länge)))][(i % (easylab.length() / länge))]);
            //System.out.println("---");
            //System.out.println("["+(i%länge)+"]"+"["+(i%(easylab.length() / länge))+"]"+"["+(i/(easylab.length() / länge))+"]"+"["+(i%länge)+"]");
        }

        return rot;
    }

    private static char[][] getCharLab(char[][] rotlab) {
        char[][] lab = new char[rotlab.length][rotlab[0].length];

        for(int i=0;i<rotlab.length;i++){
            for(int j=0;j<rotlab[i].length;j++){
                lab[i][(rotlab[i].length-j)-1]=rotlab[i][j];
            
            }
        }

        return lab;
    }

}
