/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aisalaby;

import java.util.LinkedList;

/**
 *
 * @author qohelet
 */
public final class Ausgabe {

    public static String ausgabe(char[][] labyrinth) {
        StringBuilder ret = new StringBuilder();
        for (int y = labyrinth[0].length - 1; y >= 0; y--) {
            for (int x = 0; x < labyrinth.length; x++) {
                if (labyrinth[x][y] == AisaLaby.FREI) {

                    if (labyrinth[0].length > 9 || labyrinth.length > 9) {
                        ret.append("|");
                        if (x > 9) {
                            ret.append(x);
                        } else {
                            ret.append(' ');
                            ret.append(x);
                        }
                        ret.append("/");
                        if (y > 9) {
                            ret.append(y);
                        } else {
                            ret.append(' ').append(y);
                        }
                        ret.append("|");
                    } else {
                        ret.append("|").append(x).append("/").append(y).append("|");
                    }
                } else {

                    if (labyrinth[0].length > 9 || labyrinth.length > 9) {
                        ret.append("|").append(AisaLaby.BELEGT).append(AisaLaby.BELEGT).append(AisaLaby.BELEGT).append(AisaLaby.BELEGT).append(AisaLaby.BELEGT).append("|");
                    } else {
                        ret.append("|").append(AisaLaby.BELEGT).append(AisaLaby.BELEGT).append(AisaLaby.BELEGT).append("|");
                    }
                }
            }
            ret.append("\n");
        }

        return ret.toString();
    }

    public static String ausgabeInv(char[][] labyrinth) {
        StringBuilder ret = new StringBuilder();

        for (int i = labyrinth[0].length - 1; i >= 0; i--) {
            for (int j = 0; j < labyrinth.length; j++) {
                if (labyrinth[j][i] == AisaLaby.BELEGT) {

                    if (labyrinth[0].length > 9 || labyrinth.length > 9) {
                        ret.append("|");
                        if (j > 9) {
                            ret.append(j);
                        } else {
                            ret.append(' ');
                            ret.append(j);
                        }
                        ret.append("/");
                        if (i > 9) {
                            ret.append(i);
                        } else {
                            ret.append(' ').append(i);
                        }
                        ret.append("|");
                    } else {
                        ret.append("|").append(j).append("/").append(i).append("|");
                    }
                } else {

                    if (labyrinth[0].length > 9 || labyrinth.length > 9) {
                        ret.append("|").append(AisaLaby.BELEGT).append(AisaLaby.BELEGT).append(AisaLaby.BELEGT).append(AisaLaby.BELEGT).append(AisaLaby.BELEGT).append("|");
                    } else {
                        ret.append("|").append(AisaLaby.BELEGT).append(AisaLaby.BELEGT).append(AisaLaby.BELEGT).append("|");
                    }
                }
            }
            ret.append("\n");
        }

        return ret.toString();
    }

    public static String getArrayAusgabe(int[] array) {
        int maxlänge = 0;
        for (int i = 0; i < array.length; i++) {
            if (String.valueOf(array[i]).length() > maxlänge) {
                maxlänge = String.valueOf(array[i]).length();
            }
        }

        StringBuilder ret = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            ret.append(getLeer(String.valueOf(array[i]).length(), maxlänge));
            ret.append(array[i]);
            ret.append('|');
        }
        return ret.toString();
    }

    private static String getLeer(int valueOf, int maxlänge) {
        StringBuilder ret = new StringBuilder();

        for (int i = 0; i < maxlänge - valueOf; i++) {
            ret.append(" ");
        }

        return ret.toString();
    }

    public static String flächenAusgabe(LinkedList<FlaWrapper> flächen) {
        StringBuilder ret = new StringBuilder("Flächen: \n");
        int i = 1;

        for (FlaWrapper fla : flächen) {
            ret.append("№").append(i);
            ret.append(einzelFlächenAusgabe(fla));
            i++;
        }

        return ret.toString();
    }

    public static String einzelFlächenAusgabe(FlaWrapper fla) {
        return "Start: (" + fla.getStartPunkt().getX() + "/" + fla.getStartPunkt().getY() + ") Ende: (" + fla.getEndPunkt().getX() + "/" + fla.getEndPunkt().getY() + "); Fläche:" + fla.getFläche() + '\n';
    }

    static String billigansicht(char[][] labyrinth) {
        StringBuilder ret = new StringBuilder();
        for (int y = labyrinth[0].length - 1; y >= 0; y--) {
            for (int x = 0; x < labyrinth.length; x++) {
                ret.append(labyrinth[x][y]);

            }
            ret.append("\n");

        }
        return ret.toString();
    }

    static String billigansicht(char[][] labyrinth, XYWrapper neuerPunkt) {
        StringBuilder ret = new StringBuilder();
        for (int y = labyrinth[0].length - 1; y >= 0; y--) {
            for (int x = 0; x < labyrinth.length; x++) {
                if (x == neuerPunkt.getX() && y == neuerPunkt.getY()) {
                    ret.append("░");
                }else
                ret.append(labyrinth[x][y]);

            }
            ret.append("\n");

        }
        return ret.toString();
    }

}
