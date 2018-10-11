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
public class Areale {

    final public char FREI;//Symbol für ein freies Feld
    final public char BELEGT;//Symbol für ein belegtes Feld

    private final char[][] labyrinth;
    private static int mindestgröße = 3;
    private boolean nurMindestgröße = false;
    private boolean mezmerize = true;
    private boolean scanlinedreher = false;
    private boolean freilinie = true;

    public static final FlaWrapper DEFFLAWRAP = new FlaWrapper(new XYWrapper(0, 0), new XYWrapper(0, 0));

    private final static boolean nurHorizontalentfernung = false, nurVertikalentfernung = true;

    public static final boolean AUSGABE_FLÄCHEN = false, AUSGABE_EINZELFLÄCHEN = false, AUSGABE_LÖSCHLOG = false;
    private static final boolean AUSGABE_BASIS = false;
    private static final boolean AUSGABE_HORIZONTAL = false;//Rechtecksausgabe wird nicht empfohlen
    final static private boolean AUSGABE_LÖSCHLOG2 = false;

    public Areale(char[][] labyrinthbasis, char frei, char belegt) {
        this.FREI = frei;
        this.BELEGT = belegt;
        this.labyrinth = new char[labyrinthbasis.length][labyrinthbasis[0].length];
        for (int i = 0; i < labyrinthbasis.length; i++) {
            System.arraycopy(labyrinthbasis[i], 0, labyrinth[i], 0, labyrinthbasis[i].length);
        }
    }

    public Areale(int mindestgröße, char[][] labyrinthbasis, char frei, char belegt) {
        this(labyrinthbasis, frei, belegt);
        setMindestgröße(mindestgröße);
    }

    public char[][] entferneFlächen() {
        LinkedList<FlaWrapper> flächen;

        if (!mezmerize) {
            do {
                flächen = getFlächen();
                if (!flächen.isEmpty()) {
                    if (flächen.getFirst() == null) {
                        break;
                    } else {
                        if (flächen.getFirst().equals(DEFFLAWRAP)) {
                            break;
                        }
                        größteFlächeLöschen(flächen);
                    }
                }
            } while (!flächen.isEmpty());
        } else {

            boolean hatergebnis;
            do {

                if (scanlinedreher) {
                    hatergebnis = modScanline();
                    if (hatergebnis == true) {
                        regulärScanline();
                    } else {
                        hatergebnis = regulärScanline();
                    }
                } else {
                    hatergebnis = regulärScanline();
                    if (hatergebnis == true) {
                        modScanline();
                    } else {
                        hatergebnis = modScanline();
                    }
                }

            } while (hatergebnis);

        }
        return labyrinth;
    }

    private boolean regulärScanline() {
        return scanline(labyrinth, BELEGT, FREI);
    }

    private boolean modScanline() {
        return scanline(labyrinth, FREI, BELEGT);
    }

    private boolean scanline(char[][] laby, char bel, char free) {
        FlaWrapper fla;
        boolean hatergebnis = false;
        do {
            Scanline scan = new Scanline(laby, free, bel,freilinie);
            fla = scan.getMaxfläche();
            flächenentferner(laby, fla, bel);
            if (!fla.equals(DEFFLAWRAP)) {
                hatergebnis = true;
            }
        } while (!fla.equals(DEFFLAWRAP));

        return hatergebnis;
    }

    private void flächenentferner(char[][] labyrinth, FlaWrapper fla, char belegung) {
        if (AUSGABE_LÖSCHLOG) {
            System.out.println("---Löschlog für: (" + fla + ")---");
        }
        for (int i = fla.getStartPunkt().getX() + 1; i <= fla.getEndPunkt().getX() - 1; i++) {
            for (int j = fla.getStartPunkt().getY() + 1; j <= fla.getEndPunkt().getY() - 1; j++) {
                labyrinth[i][j] = belegung;
                if (AUSGABE_LÖSCHLOG) {
                    System.out.println("Gesetzt: [" + i + "][" + j + "]:" + labyrinth[i][j]);
                }
            }
        }
        if (AUSGABE_LÖSCHLOG2) {
            System.out.println("Labyrinth neu:");
            System.out.println(Ausgabe.ausgabe(labyrinth));
        }
    }

    private LinkedList<FlaWrapper> getFlächen() {
        LinkedList<FlaWrapper> flächen = new LinkedList<>();
        XYWrapper startpunkt;

        //Es gibt die Variante nur die Mindestquadrate zu finden
        if (!nurMindestgröße) {
            Scanline scan = new Scanline(labyrinth, FREI, BELEGT,freilinie);
            flächen.add(scan.getMaxfläche());
        } else {
            for (int i = 0; i < labyrinth.length; i++) {
                for (int j = 0; j < labyrinth[i].length; j++) {
                    if (labyrinth[i][j] == FREI) {
                        //Zum Flächenentfernen braucht man mindestens ein Quadrat 3x3
                        if (i + mindestgröße - 1 < labyrinth.length - 1 && j + mindestgröße - 1 < labyrinth[i].length - 1) {
                            /**
                             * Überprüfung, ob wir eine Mindestfläche zum
                             * Entfernen zusammenbekommen
                             */
                            boolean hatGröße = hatBasisgröße(labyrinth, i, j);
                            if (AUSGABE_BASIS) {
                                System.out.println("hg: " + hatGröße);
                            }

                            //Unsere erste Fläche
                            if (hatGröße) {
                                startpunkt = new XYWrapper(i, j);
                                flächen.add(new FlaWrapper(startpunkt, new XYWrapper((i + mindestgröße - 1), (j + mindestgröße - 1))));
                                if (AUSGABE_EINZELFLÄCHEN) {
                                    System.out.println("i: " + i + "; j:" + j + "; min:" + mindestgröße);
                                    System.out.println(Ausgabe.einzelFlächenAusgabe(flächen.getLast()));
                                }

                                //Es gibt die Variante nur die Mindestquadrate zu finden
                                if (!nurMindestgröße) {
                                    rechtecksprüfung(i, j, flächen, startpunkt);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (AUSGABE_FLÄCHEN) {
            System.out.println("---Flächen(" + flächen.size() + ")---");
            System.out.println(Ausgabe.flächenAusgabe(flächen));
            System.out.println("---/Flächen--");
        }

        return flächen;
    }

    private void größteFlächeLöschen(LinkedList<FlaWrapper> flächen) {
        FlaWrapper maxFla = DEFFLAWRAP;

        for (FlaWrapper fläche : flächen) {
            if (fläche.istGrößer(maxFla)) {
                maxFla = fläche;
            }
        }

        if (nurMindestgröße) {
            quadratentferner(labyrinth, maxFla);
        } else {
            if (maxFla.isQuadratfläche()) {
                quadratentferner(labyrinth, maxFla);
            } else {
                flächenentferner(labyrinth, maxFla);
            }
        }
    }

    private void quadratentferner(char[][] labyrinth, FlaWrapper fla) {
        for (int i = fla.getStartPunkt().getX() + 1; i <= fla.getEndPunkt().getX() - 1; i++) {
            for (int j = fla.getStartPunkt().getY() + 1; j <= fla.getEndPunkt().getY() - 1; j++) {
                labyrinth[i][j] = BELEGT;
                if (AUSGABE_LÖSCHLOG) {
                    System.out.println("Gesetzt: [" + i + "][" + j + "]:" + labyrinth[i][j]);
                }
            }
        }

    }

    private void flächenentferner(char[][] labyrinth, FlaWrapper fla) {
        if (AUSGABE_LÖSCHLOG) {
            System.out.println("---Löschlog für: (" + fla + ")---");
        }
        for (int i = fla.getStartPunkt().getX() + 1; i <= fla.getEndPunkt().getX() - 1; i++) {
            for (int j = fla.getStartPunkt().getY() + 1; j <= fla.getEndPunkt().getY() - 1; j++) {
                labyrinth[i][j] = BELEGT;
                if (AUSGABE_LÖSCHLOG) {
                    System.out.println("Gesetzt: [" + i + "][" + j + "]:" + labyrinth[i][j]);
                }
            }
        }
        if (AUSGABE_LÖSCHLOG2) {
            System.out.println("Labyrinth neu:");
            System.out.println(Ausgabe.ausgabe(labyrinth));
        }
    }

    private void rechtecksprüfung(int i, int j, LinkedList<FlaWrapper> flächen, XYWrapper startpunkt) {
        if (!nurVertikalentfernung) {
            horizontalFlächenprüfung(i, j, startpunkt, flächen);
        }

        if (!nurHorizontalentfernung) {
            vertikalFlächenprüfung(i, j, startpunkt, flächen);
        }
    }

    private void horizontalFlächenprüfung(int i, int j, XYWrapper startpunkt, LinkedList<FlaWrapper> flächen) {
        for (int breitensummand = i + mindestgröße; breitensummand < labyrinth.length; breitensummand++) {
            for (int längensummand = j; längensummand <= (j + mindestgröße - 1); längensummand++) {
                if (AUSGABE_HORIZONTAL) {
                    System.out.println("labyrinth[" + breitensummand + "][" + längensummand + "]:" + labyrinth[breitensummand][längensummand]);
                }

                if (labyrinth[breitensummand][längensummand] == BELEGT) {
                    if (AUSGABE_HORIZONTAL) {
                        System.out.print(" --> failed\n");
                    }
                    return;
                }
            }
            if (AUSGABE_HORIZONTAL) {
                System.out.println("Rechteck: " + startpunkt + ";" + new XYWrapper(breitensummand, (j + mindestgröße - 1)));
            }
            flächen.add(new FlaWrapper(startpunkt, new XYWrapper(breitensummand, (j + mindestgröße - 1))));
        }
    }

    private void vertikalFlächenprüfung(int i, int j, XYWrapper startpunkt, LinkedList<FlaWrapper> flächen) {
        for (int längensummand = j + mindestgröße; längensummand < labyrinth[0].length; längensummand++) {

            for (int breitensummand = i; breitensummand <= (i + mindestgröße - 1); breitensummand++) {

                if (labyrinth[breitensummand][längensummand] == BELEGT) {
                    return;
                }
            }

            flächen.add(new FlaWrapper(startpunkt, new XYWrapper((i + mindestgröße - 1), längensummand)));
        }
    }

    private boolean hatBasisgröße(char[][] labyrinth, int i, int j) {
        /*
         * if(labyrinth[i+1][j]==AisaLaby.FREI &&
         * labyrinth[i+2][j]==AisaLaby.FREI
         * &&labyrinth[i][j+1]==AisaLaby.FREI &&
         * labyrinth[i][j+2]==AisaLaby.FREI
         * &&labyrinth[i+1][j+1]==AisaLaby.FREI &&
         * labyrinth[i+2][j+1]==AisaLaby.FREI
         * &&labyrinth[i+1][j+2]==AisaLaby.FREI &&
         * labyrinth[i+2][j+2]==AisaLaby.FREI){
         *
         * XYWrapper startpunkt = new XYWrapper(j, j);*/

        for (int länge = i; länge <= i + mindestgröße - 1; länge++) {
            for (int breit = j; breit <= j + mindestgröße - 1; breit++) {
                if (AUSGABE_BASIS) {
                    System.out.println("(" + länge + "/" + breit + "):" + labyrinth[länge][breit]);
                }

                if (labyrinth[länge][breit] != FREI) {
                    if (AUSGABE_BASIS) {
                        System.out.println("false");
                    }
                    return false;
                }
            }

        }
        return true;

    }

    public static int getMindestgröße() {
        return mindestgröße;
    }

    public final void setMindestgröße(int mindestgröße) {
        if (mindestgröße >= 3) {
            Areale.mindestgröße = mindestgröße;
        }
    }

    public boolean isNurMindestgröße() {
        return nurMindestgröße;
    }

    public void setNurMindestgröße(boolean nurMindestgröße) {
        this.nurMindestgröße = nurMindestgröße;
    }

    public boolean isMezmerize() {
        return mezmerize;
    }

    public void setMezmerize(boolean mezmerize) {
        this.mezmerize = mezmerize;
    }

    public boolean isScanlinedreher() {
        return scanlinedreher;
    }

    public void setScanlinedreher(boolean scanlinedreher) {
        this.scanlinedreher = scanlinedreher;
    }

    public boolean isFreilinie() {
        return freilinie;
    }

    public void setFreilinie(boolean freilinie) {
        this.freilinie = freilinie;
    }
    
}
