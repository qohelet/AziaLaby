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
public class Scanline {

    final public char FREI;//Symbol für ein freies Feld
    final public char BELEGT;//Symbol für ein belegtes Feld

    private final boolean  freilinie;//Setzt eine imaginäre freie Linie um das Labyrinth um größere Rechtecke bilden zu können
    private final static boolean RECHTECKAUSGABE = false, REMOVERAUSGABE = false;
    private final char[][] labyrinth;
    private final boolean MINIMALDEBUG = false;
        private int mindestgröße = 3;


    public Scanline(char[][] labyrinth, char frei, char belegt, boolean freilinie) {
        this.FREI = frei;
        this.BELEGT = belegt;
        this.freilinie = freilinie;
        if (!freilinie) {
            this.labyrinth = labyrinth;
        } else {
            this.labyrinth = getFreilabyrinth(labyrinth);
        }
    }

    /**
     * ScanLine-Algorithmus
     */
    public FlaWrapper getMaxfläche() {
        FlaWrapper maxfla = Areale.DEFFLAWRAP;

        LinkedList<FlaWrapper> prüfflächen = getFlächen();

        if (!prüfflächen.isEmpty()) {
            if (RECHTECKAUSGABE) {
                System.out.println("Anzahl der Flächen: " + prüfflächen.size());
            }
            for (FlaWrapper freifläche : prüfflächen) {
                if (freifläche.getFläche() > maxfla.getFläche() && (freifläche.getEndPunktHarmonisiert().getX() >= mindestgröße && freifläche.getEndPunktHarmonisiert().getY() >= mindestgröße)) {
                    maxfla = freifläche;
                }
            }
            if (RECHTECKAUSGABE) {
                System.out.println(Ausgabe.einzelFlächenAusgabe(maxfla));
            }
            if (freilinie) {

                if (maxfla == Areale.DEFFLAWRAP) {
                    return Areale.DEFFLAWRAP;
                } else {
                    return new FlaWrapper(new XYWrapper(maxfla.getStartPunkt().getX() - 1, maxfla.getStartPunkt().getY() - 1), new XYWrapper(maxfla.getEndPunkt().getX() - 1, maxfla.getEndPunkt().getY() - 1));
                }
            } else {
                return maxfla;
            }
        } else {
            if (freilinie) {
                if (maxfla == Areale.DEFFLAWRAP) {
                    return Areale.DEFFLAWRAP;
                } else {
                    return new FlaWrapper(new XYWrapper(maxfla.getStartPunkt().getX() - 1, maxfla.getStartPunkt().getY() - 1), new XYWrapper(maxfla.getEndPunkt().getX() - 1, maxfla.getEndPunkt().getY() - 1));
                }
            } else {
                return maxfla;
            }

        }

    }

    public LinkedList<FlaWrapper> getBenutzbareFlächen() {
        LinkedList<FlaWrapper> sendflächen = new LinkedList<>();

        LinkedList<FlaWrapper> prüfflächen = getFlächen();

        if (!prüfflächen.isEmpty()) {
            if (RECHTECKAUSGABE) {
                System.out.println("Anzahl der Flächen: " + prüfflächen.size());
            }
            for (FlaWrapper freifläche : prüfflächen) {
                if ((freifläche.getEndPunktHarmonisiert().getX() >= mindestgröße && freifläche.getEndPunktHarmonisiert().getY() >= mindestgröße)) {
                    if (!freilinie) {
                        sendflächen.add(freifläche);
                    } else {
                        sendflächen.add(new FlaWrapper(new XYWrapper(freifläche.getStartPunkt().getX() - 1, freifläche.getStartPunkt().getY() - 1), new XYWrapper(freifläche.getEndPunkt().getX() - 1, freifläche.getEndPunkt().getY() - 1)));
                    }
                }
            }

        }
        return sendflächen;
    }

    private static char[][] getFreilabyrinth(char[][] origilab) {
        char[][] freilab = new char[origilab.length + 2][origilab[0].length + 2];

        freilab = AisaLaby.getLeerLabyrinth(freilab.length, freilab[0].length, AisaLaby.FREI);

        for (int i = 0; i < origilab.length; i++) {
            for (int j = 0; j < origilab[i].length; j++) {
                freilab[i + 1][j + 1] = origilab[i][j];
            }
        }

        return freilab;
    }

    private void folgereihenprüfung(XYWrapper neustartpunkt, XYWrapper endpunkt_nulllinie, int y_läufer, LinkedList<FlaWrapper> flächen) {
        for (int y_achse = y_läufer; y_achse < labyrinth[0].length; y_achse++) {
            for (int x_achse = neustartpunkt.getX(); x_achse < endpunkt_nulllinie.getX(); x_achse++) {
                if (labyrinth[x_achse][y_achse] == BELEGT) {
                    if (y_achse - 1 > y_läufer) {
                        FlaWrapper flawa = new FlaWrapper(neustartpunkt, new XYWrapper(endpunkt_nulllinie.getX(), y_achse - 1));

                        if (hindernisfrei(flawa.getStartPunkt(), flawa.getEndPunkt()) && flawa.getFläche() >= 9) {
                            flächen.add(flawa);
                            if (RECHTECKAUSGABE) {
                                System.out.println("Hinzufügen größerer Fläche oberhalb");
                                System.out.println(flächen.getLast());
                                System.out.println("----/Plus-----");
                            }
                        }
                        return;
                    }
                }
            }
        }
    }

    private boolean hindernisfrei(XYWrapper startpunkt, XYWrapper endpunkt) {
        for (int i = startpunkt.getX(); i <= endpunkt.getX(); i++) {
            for (int j = startpunkt.getY(); j <= endpunkt.getY(); j++) {
                if (labyrinth[i][j] == BELEGT) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hindernisfrei(FlaWrapper flawa) {
        return hindernisfrei(flawa.getStartPunkt(), flawa.getEndPunkt());
    }

    private LinkedList<FlaWrapper> getFlächen() {
        LinkedList<FlaWrapper> prüfflächen = new LinkedList<>();
        for (int primärY = 0; primärY < labyrinth[0].length; primärY++) {
            for (int primärX = 0; primärX < labyrinth.length; primärX++) {

                if (MINIMALDEBUG) {
                    System.out.println("PrimärX: " + primärX + " PrimärY: " + primärY);
                }

                XYWrapper startpunkt = new XYWrapper(-1, -1);
                boolean b_reihenlesung = false;

                for (int y_achse = primärY; y_achse < labyrinth[0].length; y_achse++) {
                    for (int x_achse = primärX; x_achse < labyrinth.length; x_achse++) {

                        /**
                         * Gegeben der Fall das letzte Element in einer Zeile
                         * ist frei. Die Reihenlesung wird aktiviert und der
                         * Zähler kommt in die nächste Zeile Dort existiert auch
                         * ein freies Element aber das muss neu definiert werden
                         * ---Braucht es das?---
                         * if(b_reihenlesung&&labyrinth[x_achse][y_achse]==FREI&&startpunkt.getY()!=y_achse&&x_achse==x0){
                         * startpunkt=new XYWrapper(-1, -1);
                         * b_reihenlesung=false; }
                         *
                         */
                        //Der erste freie Punkt wurde gefunden, wir starten die 
                        if (labyrinth[x_achse][y_achse] == FREI && b_reihenlesung == false) {
                            startpunkt = new XYWrapper(x_achse, y_achse);

                            b_reihenlesung = true;
                        }

                        /**
                         * Die Reihenlesung ist aktiviert, wir sind auf ein
                         * belegtes Element gestoßen und haben bereits einen
                         * Startpunkt.
                         *
                         * Auf die Y-Achse müssen wir nicht überprüfen. Der
                         * Zähler kann einfach durchgehen. Wenn auf der X-Achse
                         * auf ein Hindernis gestoßen wird, wird ohnehin dieses
                         * verwendet.
                         *
                         * Aufpassen, wenn Hindernis auf X=0
                         */
                        if (b_reihenlesung
                                && (!startpunkt.equals(new XYWrapper(-1, -1)))
                                && ((labyrinth[x_achse][y_achse] == BELEGT)
                                || x_achse == labyrinth.length - 1)) {//  && startpunkt.getY()==y_achse) || (x_achse==labyrinth.length-1))) {
                            int hindernis_X;
                            int y_achsenstart;

                            if (x_achse == 0) {
                                hindernis_X = labyrinth.length - 1;
                                y_achsenstart = y_achse;
                            } else {
                                hindernis_X = x_achse - 1;
                                y_achsenstart = y_achse + 1;
                            }

                            if (x_achse == labyrinth.length - 1) {
                                hindernis_X = labyrinth.length - 1;
                            }

                            boolean b_neustartlesung = false;
                            XYWrapper neustartpunkt = new XYWrapper(-1, -1);
                            /*
                             if (x_achse == 0) {
                             prüfflächen.add(new FlaWrapper(startpunkt, new XYWrapper(labyrinth.length - 1, y_achse - 1)));

                             if (RECHTECKAUSGABE) {
                             Systtem.out.println("Größmögliche Rechteckfläche gefunden und hinzugefügt: " + prüfflächen.getLast().toString());
                             }

                             } else {*/
                            /**
                             * Wir gehen Y+1 und laufen bis zum Hindernispunkt
                             * durch
                             */

                            außenschleife:
                            for (int y_läufer = y_achsenstart; y_läufer < labyrinth[0].length; y_läufer++) {
                                for (int x_läufer = startpunkt.getX(); x_läufer <= hindernis_X; x_läufer++) {

                                    //Freifeld gefunden, aber es ist durch eine Zeile getrennt
                                    //Y ist höher, lesung hat nicht stattgefunden, Feld ist frei
                                    if (y_läufer > (y_achsenstart) && b_neustartlesung == false && labyrinth[x_läufer][y_läufer] == FREI && (!startpunkt.equals(new XYWrapper(-1, -1)))) {
                                        b_reihenlesung = false;
                                        break außenschleife;
                                    }

                                    //Wenn ein Freifeld gefunden wird, setzt man einen Neustartpunkt
                                    //Dessen X sollte möglichst nahe am Erst-X sein
                                    if (labyrinth[x_läufer][y_läufer] == FREI && b_neustartlesung == false) {
                                        neustartpunkt = new XYWrapper(x_läufer, y_läufer - 1);
                                        b_neustartlesung = true;
                                    }

                                    //Zählung bis auf Hindernis gestoßen
                                    if (labyrinth[x_läufer][y_läufer] == BELEGT && b_neustartlesung && (!startpunkt.equals(new XYWrapper(-1, -1)))) {

                                        XYWrapper hindernis2 = new XYWrapper(x_läufer - 1, y_läufer);

                                        if (hindernis2.getX() >= neustartpunkt.getX() || x_läufer == 0) {
                                            //Speichern des regulären Elements
                                            //Dieses geht bis zum Hindernis
                                            FlaWrapper regulär;
                                            if (x_läufer == 0) {
                                                regulär = new FlaWrapper(neustartpunkt, new XYWrapper(hindernis_X, hindernis2.getY() - 1));
                                                if (RECHTECKAUSGABE) {
                                                    System.out.println("X=0");
                                                }
                                            } else {
                                                regulär = new FlaWrapper(neustartpunkt, hindernis2);
                                            }
                                            if (regulär.getFläche() > 1 && (regulär.getEndPunkt().getX() - regulär.getStartPunkt().getX() >= 2) && (regulär.getEndPunkt().getY() - regulär.getStartPunkt().getY() >= 2)) {
                                                prüfflächen.add(regulär);
                                                if (RECHTECKAUSGABE) {
                                                    System.out.println("Regulär gespeichert: " + regulär);
                                                }
                                            } else {
                                                if (REMOVERAUSGABE) {
                                                    System.out.println("</Regulär verworfen: " + regulär + "/>");
                                                }
                                            }
                                        } else {
                                            if (REMOVERAUSGABE) {
                                                System.out.println("Hindernis liegt hinter dem Neustartpunkt: " + hindernis2 + ":" + neustartpunkt);
                                            }

                                        }
                                        //Speichern des Vorelements
                                        //Das Vorelement ist y-1 tiefer und hat als X den Punkt des Hindernisses
                                        //Das Vorelement ist bis zum Regulärelement die größte vorhandene Fläche
                                        //Und kann das Regulärelement sogar überragen
                                        FlaWrapper vorregulär = new FlaWrapper(neustartpunkt, new XYWrapper(hindernis_X, hindernis2.getY() - 1));
                                        if (vorregulär.getFläche() > 1 && (vorregulär.getEndPunkt().getX() - vorregulär.getStartPunkt().getX() >= 2) && (vorregulär.getEndPunkt().getY() - vorregulär.getStartPunkt().getY() >= 2)) {
                                            prüfflächen.add(vorregulär);
                                            if (RECHTECKAUSGABE) {
                                                System.out.println("Vorregulär gespeichert: " + vorregulär);
                                            }

                                        } else {
                                            if (REMOVERAUSGABE) {
                                                System.out.println("</Vorregulär verworfen: " + vorregulär + "/>");
                                            }
                                        }
                                        //neustartpunkt, new XYWrapper(hindernis_X, hindernis2.getY() - 1)

                                        /**
                                         * Erweiterungen nach oben. Wir nehmen
                                         * das Regulärelement und erweitern es
                                         * nach oben So lange, bis wir auf
                                         * Hindernisse stoßen
                                         */
                                        int erweiterungsstopp = hindernis_X + 1;
                                        erweitertschleife:
                                        for (int y_erweitert = hindernis2.getY(); y_erweitert < labyrinth[0].length; y_erweitert++) {
                                            for (int x_erweitert = neustartpunkt.getX(); x_erweitert < erweiterungsstopp; x_erweitert++) {

                                                //1. Element in der Zeile ist eine Belegung
                                                if (labyrinth[x_erweitert][y_erweitert] == BELEGT && x_erweitert == neustartpunkt.getX()) {
                                                    if (y_erweitert == hindernis2.getY()) {
                                                        break erweitertschleife;
                                                    }
                                                    prüfflächen.add(new FlaWrapper(neustartpunkt, new XYWrapper(erweiterungsstopp - 1, y_erweitert - 1)));

                                                    if (RECHTECKAUSGABE) {
                                                        System.out.println("Erweiterung hinzugefügt (Zeile hinunter): " + prüfflächen.getLast());
                                                        break erweitertschleife;
                                                    }
                                                }
                                                /**
                                                 * Wenn über der höchsten
                                                 * Regulärelementezeile freie
                                                 * Zeilen sind, so werden die
                                                 * hier geholt
                                                 */
                                                if (labyrinth[x_erweitert][y_erweitert] == BELEGT) {
                                                    erweiterungsstopp = x_erweitert;
                                                    XYWrapper neuhindernis = new XYWrapper(x_erweitert - 1, y_erweitert);

                                                    //Speichern des regulären Elements
                                                    FlaWrapper regulär2 = new FlaWrapper(neustartpunkt, neuhindernis);

                                                    if (regulär2.getFläche() > 1 && (regulär2.getEndPunkt().getX() - regulär2.getStartPunkt().getX() >= 2) && (regulär2.getEndPunkt().getY() - regulär2.getStartPunkt().getY() >= 2)) {
                                                        prüfflächen.add(regulär2);
                                                        if (RECHTECKAUSGABE) {
                                                            System.out.println("Regulärerweiterung gespeichert: " + regulär2);
                                                        }

                                                        break;
                                                    } else {
                                                        if (REMOVERAUSGABE) {
                                                            System.out.println("</Regulärerweiterung verworfen: " + regulär2 + "/>");
                                                        }
                                                    }

                                                    /**
                                                     * Selbes Prinzip wie beim
                                                     * normalen Vorelement. Nur
                                                     * eben mit der erweiterung
                                                     */
                                                    FlaWrapper vorregulär2 = new FlaWrapper(neustartpunkt, new XYWrapper(erweiterungsstopp - 1, neuhindernis.getY() - 1));
                                                    if (vorregulär2.getFläche() > 1 && (vorregulär2.getEndPunkt().getX() - vorregulär2.getStartPunkt().getX() >= 2) && (vorregulär2.getEndPunkt().getY() - vorregulär2.getStartPunkt().getY() >= 2)) {
                                                        prüfflächen.add(vorregulär2);
                                                        if (RECHTECKAUSGABE) {
                                                            System.out.println("VorregulärErweiterung gespeichert: " + vorregulär2);
                                                        }

                                                    } else {
                                                        if (REMOVERAUSGABE) {
                                                            System.out.println("</VorregulärErweiterung verworfen: " + vorregulär2 + "/>");
                                                        }
                                                    }
                                                }

                                                if (y_erweitert == labyrinth[0].length - 1 && x_erweitert == erweiterungsstopp - 1 && !neustartpunkt.equals(new XYWrapper(-1, -1))) {

                                                    FlaWrapper letzterPunkt = new FlaWrapper(neustartpunkt, new XYWrapper(x_erweitert, y_erweitert));
                                                    if (letzterPunkt.getStartPunkt().getX() == -1) {
                                                    }

                                                    if (letzterPunkt.getFläche() > 1 && (letzterPunkt.getEndPunkt().getX() - letzterPunkt.getStartPunkt().getX() >= 2) && (letzterPunkt.getEndPunkt().getY() - letzterPunkt.getStartPunkt().getY() >= 2)) {
                                                        prüfflächen.add(letzterPunkt);
                                                        if (RECHTECKAUSGABE) {
                                                            System.out.println("Als Erweiterung des letzten Punktes gespeichert: " + letzterPunkt);
                                                        }
                                                    } else {
                                                        if (REMOVERAUSGABE) {
                                                            System.out.println("</Die Erweiterung des letzten Punktes verworfen: " + letzterPunkt + "/>");
                                                        }
                                                    }
                                                }

                                            }
                                        }

                                        startpunkt = new XYWrapper(-1, -1);
                                        b_reihenlesung = false;
                                        break außenschleife;
                                    }

                                    if (y_läufer == labyrinth[0].length - 1 && x_läufer == hindernis_X && !neustartpunkt.equals(new XYWrapper(-1, -1))) {

                                        FlaWrapper letzterPunkt = new FlaWrapper(neustartpunkt, new XYWrapper(x_läufer, y_läufer));
                                        if (letzterPunkt.getStartPunkt().getX() == -1) {
                                        }

                                        if (letzterPunkt.getFläche() > 1 && (letzterPunkt.getEndPunkt().getX() - letzterPunkt.getStartPunkt().getX() >= 2) && (letzterPunkt.getEndPunkt().getY() - letzterPunkt.getStartPunkt().getY() >= 2)) {
                                            prüfflächen.add(letzterPunkt);
                                            if (RECHTECKAUSGABE) {
                                                System.out.println("Als letzten Punkt gespeichert: " + letzterPunkt);
                                            }
                                        } else {
                                            if (REMOVERAUSGABE) {
                                                System.out.println("</Den letzten Punkt verworfen: " + letzterPunkt + "/>");
                                            }
                                        }
                                        startpunkt = new XYWrapper(-1, -1);
                                        b_reihenlesung = false;
                                    }

                                }

                            }

                            //}
                        }
                    }
                }
            }
        }
        return prüfflächen;
    }

    public int getMindestgröße() {
        return mindestgröße;
    }

    public void setMindestgröße(int mindestgröße) {
        this.mindestgröße = mindestgröße;
    }

    
}
