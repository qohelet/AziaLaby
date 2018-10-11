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
public class AisaLaby {

    static final public char FREI = ' ';//Symbol für ein freies Feld
    static final public char BELEGT = '█';//Symbol für ein belegtes Feld

    private char[][] prelabyrinth;

    final private XYWrapper fläche;
    final private XYWrapper startPunkt;
    final private XYWrapper endPunkt;

    private boolean überschneidungsvermeidung = false;
    private boolean kompliziert = false;
    private boolean flächenentfernung = false;
    private boolean freilinie = true;
    private boolean AUSGABE_VERMEIDUNG = false;
    private boolean AUSGABE_LABYRINTHNEUBAU = false;
    private final boolean AUSGABE_VORLÖSCHANSICHT = false;
    private final static boolean AUSGABE_VORHER_NACHHER = false;
    final private boolean AUSGABE_KOORDINATEN = false;
    private final boolean AUSGABE_FREIPUNKT = false;
    private final boolean AUSGABE_SETZEPUNKTEPROTOKOLL = false;

    private boolean mezmerize = true;

    private boolean b_nichtzahlenausschluss = true;
    private int zahlenausschluss = 0;
    private int normabstand = 2;

    public AisaLaby(XYWrapper fläche, XYWrapper startPunkt, XYWrapper endPunkt) {
        this.fläche = fläche;
        this.startPunkt = startPunkt;
        this.endPunkt = endPunkt;
        prelabyrinth = null;
    }

    public AisaLaby(char[][] prelabyrinth) {
        this.prelabyrinth = prelabyrinth;
        this.fläche = new XYWrapper(prelabyrinth.length, prelabyrinth[0].length);
        this.startPunkt = null;
        this.endPunkt = null;
    }

    /**
     * Gibt ein Labyrinth zurück
     *
     * @param überschneidungsvermeidung Vermeidet dass sich sich der Weg
     * überschneidet
     * @param kompliziert false = labyrinth geht von links nach rechts true =
     * labyrinth sucht sich seinen eigenen Weg von startPunkt nach endPunkt
     * @param abstand Der durchschnittliche Abstand zwischen zwei Verzweigungen
     * (>1)
     * @param verzweigungen
     * @param flächenentfernung
     * @return
     */
    public char[][] getLabyrint(boolean überschneidungsvermeidung, boolean kompliziert, boolean flächenentfernung) {
        char[][] labyrinth = getLeerLabyrinth(fläche.getX(), fläche.getY(), BELEGT);
        this.überschneidungsvermeidung = überschneidungsvermeidung;
        this.kompliziert = kompliziert;
        this.flächenentfernung = flächenentfernung;

        if (AUSGABE_VORHER_NACHHER) {
            System.out.println("Leerlaby");
            System.out.println(Ausgabe.billigansicht(labyrinth));
        }

        setzePunkte(labyrinth);

        if (AUSGABE_VORHER_NACHHER) {
            System.out.println(Ausgabe.billigansicht(labyrinth));
        }

        return erzeugung(labyrinth);
    }

    public char[][] benutzeLabyrint(boolean überschneidungsvermeidung, boolean kompliziert, boolean flächenentfernung) {
        this.überschneidungsvermeidung = überschneidungsvermeidung;
        this.kompliziert = kompliziert;
        this.flächenentfernung = flächenentfernung;

        if (AUSGABE_VORHER_NACHHER) {
            System.out.println(Ausgabe.billigansicht(prelabyrinth));
        }

        return erzeugung(prelabyrinth);
    }

    private char[][] erzeugung(char[][] labyrinth) {

        if (normabstand > 1) {
            Verzweiger v = new Verzweiger(labyrinth, normabstand, FREI, BELEGT);
            v.getVerzweigungen();
        }

        if (flächenentfernung) {
            if ((Areale.AUSGABE_FLÄCHEN || Areale.AUSGABE_EINZELFLÄCHEN) || AUSGABE_VORLÖSCHANSICHT) {
                System.out.println(Ausgabe.ausgabe(labyrinth));
            }
            Areale a = new Areale(labyrinth, FREI, BELEGT);
            a.setFreilinie(freilinie);
            if (mezmerize) {
                a.setMezmerize(true);
            } else {
                a.setMezmerize(false);
            }
            labyrinth = a.entferneFlächen();
        }

        return labyrinth;

    }

    public char getFrei() {
        return FREI;
    }

    public char getBelegt() {
        return BELEGT;
    }

    public static char[][] getLeerLabyrinth(int xFla, int yFla, char belegung) {
        char[][] labyrinth = new char[xFla][yFla];
        for (char[] labyrinth1 : labyrinth) {
            for (int j = 0; j < labyrinth1.length; j++) {
                labyrinth1[j] = belegung;
            }
        }
        return labyrinth;
    }

    private void setzePunkte(char[][] labyrinth) {

        labyrinth[startPunkt.getX()][startPunkt.getY()] = getFrei();//Definiere den Startpunkt als Startpunkt
        setzePunkt(labyrinth, startPunkt, startPunkt);

    }

    public void setzePunkt(char[][] labyrinth, XYWrapper letzterPunkt, XYWrapper vorLetzterPunkt) {
        /**
         * links: 0 --> x-1 oben: 1 --> y+1 rechts: 2 --> x+1 unten: 3 --> y-1
         */
        int richtung;
        XYWrapper neuerPunkt = new XYWrapper(-1, -1);
        boolean überschneidung;
        boolean freipunkt = false;

        if (AUSGABE_SETZEPUNKTEPROTOKOLL) {
            System.out.println("Wir fangen mit den PUnkten an");
        }

        //Wir machen das Wegesuchen so lange bis der neue Punkt nicht identisch mit dem vorherigen ist
        do {
            //Falls die Nichtüberschneidungsoption aktiviert ist, ist diese Do-While-Schleife egal
            do {

                if (letzterPunkt.getX() == endPunkt.getX()) {
                    if (letzterPunkt.getY() > endPunkt.getY()) {
                        //Jetzt müssen wir nur noch hinunter zum Endpunkt
                        richtung = 3;
                    } else {
                        //Jetzt müssen wir nur noch hinauf zum Endpunkt
                        richtung = 1;
                    }
                    if (AUSGABE_SETZEPUNKTEPROTOKOLL) {
                        System.out.println("Richtung: " + richtung);
                    }
                } else {
                    if (kompliziert && b_nichtzahlenausschluss) {
                        richtung = (int) Math.floor((Math.random() * 4));
                    } else {

                        if (!b_nichtzahlenausschluss) {
                            richtung = getRichtung();
                        } else {
                            richtung = (int) Math.floor((Math.random() * 3) + 1);
                        }
                    }
                    if (AUSGABE_SETZEPUNKTEPROTOKOLL) {
                        System.out.println("Richtung(V2): " + richtung);
                    }
                }

                //Damit die Arraygrenzen nicht überschritten werden
                while (grenzüberschreitung(richtung, letzterPunkt)) {
                    if (kompliziert && b_nichtzahlenausschluss) {
                        richtung = (int) Math.floor((Math.random() * 4));
                    } else {

                        if (!b_nichtzahlenausschluss) {
                            richtung = getRichtung();
                        } else {
                            richtung = (int) Math.floor((Math.random() * 3) + 1);
                        }
                    }
                    if (AUSGABE_SETZEPUNKTEPROTOKOLL) {
                        System.out.println("while(" + b_nichtzahlenausschluss + "): " + richtung + " ;Ausschluss:" + zahlenausschluss + "  ; " + letzterPunkt);
                    }
                }

                if (AUSGABE_KOORDINATEN) {
                    System.out.println("Wird gesetzt: " + letzterPunkt + ' ' + richtung);
                }

                neuerPunkt = getNeuerPunkt(letzterPunkt, richtung);

                if (AUSGABE_KOORDINATEN) {
                    System.out.println(Ausgabe.billigansicht(labyrinth, neuerPunkt));
                    System.out.print(" .gesetzt");
                }

                //Point of no return...
                if (isLinksRechtsErlaubt(neuerPunkt, labyrinth) && (neuerPunkt.getY() + 1 < labyrinth[0].length) && (neuerPunkt.getY() - 1 >= 0)) {
                    if (isLinksrechtsfrei(labyrinth, neuerPunkt) && (labyrinth[neuerPunkt.getX()][neuerPunkt.getY() + 1] == FREI) && (labyrinth[neuerPunkt.getX()][neuerPunkt.getY() - 1] == FREI)) {
                        freipunkt = true;
                        neuerPunkt = getFreipunkt(labyrinth, "Freipunkt 1 hinzugefügt ");
                    }
                }
                if (isLinksRechtsErlaubt(neuerPunkt, labyrinth)) {
                    if (isLinksrechtsfrei(labyrinth, neuerPunkt)) {
                        if (neuerPunkt.getY() + 1 == labyrinth[0].length && labyrinth[neuerPunkt.getX()][neuerPunkt.getY() - 1] == FREI) {
                            freipunkt = true;
                            neuerPunkt = getFreipunkt(labyrinth, "Freipunkt 2/1 hinzugefügt ");

                        }

                        if (neuerPunkt.getY() == 0 && labyrinth[neuerPunkt.getX()][neuerPunkt.getY() + 1] == FREI) {
                            freipunkt = true;
                            neuerPunkt = getFreipunkt(labyrinth, "Freipunkt 2/2 hinzugefügt ");
                        }
                    }
                }

                //Wir sind fertig
                if (neuerPunkt.equals(endPunkt)) {
                    labyrinth[endPunkt.getX()][endPunkt.getY()] = FREI;
                    return;
                }

                if (überschneidungsvermeidung && neuerPunkt.getX() != endPunkt.getX()) {
                    if (neuerPunkt.getX() - 1 >= 0) {
                        if (labyrinth[neuerPunkt.getX() - 1][neuerPunkt.getY()] == FREI && !(new XYWrapper(neuerPunkt.getX() - 1, neuerPunkt.getY())).equals(letzterPunkt)) {
                            if (AUSGABE_VERMEIDUNG) {
                                System.out.println("Vermieden: ");
                                System.out.print("X: " + neuerPunkt.getX() + '\n' + "Y: " + neuerPunkt.getY() + '\n');
                            }
                            überschneidung = true;
                        } else {
                            überschneidung = false;
                        }
                    } else {
                        überschneidung = false;
                    }
                } else {
                    überschneidung = false;
                }
            } while (überschneidung && !freipunkt);
            //Das geht dann wieder an den Ausgangspunkt... Wollen wir nicht, also alles neu
        } while (neuerPunkt.equals(vorLetzterPunkt) && !freipunkt);

        labyrinth[neuerPunkt.getX()][neuerPunkt.getY()] = this.getFrei();

        if (AUSGABE_LABYRINTHNEUBAU) {
            System.out.println("X: " + neuerPunkt.getX() + '\n' + "Y: " + neuerPunkt.getY() + '\n');
            System.out.println(Ausgabe.ausgabe(labyrinth));
            System.out.println("-------------------------");
        }
        setzePunkt(labyrinth, neuerPunkt, letzterPunkt);

    }

    private static boolean isLinksRechtsErlaubt(XYWrapper neuerPunkt, char[][] labyrinth) {
        return (neuerPunkt.getX() + 1 < labyrinth.length) && (neuerPunkt.getX() - 1 >= 0);
    }

    private static boolean isLinksrechtsfrei(char[][] labyrinth, XYWrapper neuerPunkt) {
        return (labyrinth[neuerPunkt.getX() + 1][neuerPunkt.getY()] == FREI) && (labyrinth[neuerPunkt.getX() - 1][neuerPunkt.getY()] == FREI);
    }

    private boolean grenzüberschreitung(int richtung, XYWrapper letzterPunkt) {
        switch (richtung) {
            case 0://links
                if (letzterPunkt.getX() - 1 < 0) {
                    return true;
                }
                return false;
            case 1://oben
                if (letzterPunkt.getY() + 1 >= fläche.getY()) {
                    return true;
                }
                return false;
            case 2://rechts
                if (letzterPunkt.getX() + 1 >= fläche.getX()) {
                    return true;
                }
                return false;
            case 3://unten
                if (letzterPunkt.getY() - 1 < 0) {
                    return true;
                }
                return false;
            default://Darf es nicht geben
                System.err.println("Richtung=" + richtung);
                return true;
        }
    }

    private XYWrapper getNeuerPunkt(XYWrapper letzterPunkt, int richtung) {
        switch (richtung) {
            case 0://links
                return new XYWrapper(letzterPunkt.getX() - 1, letzterPunkt.getY());
            case 1://oben
                return new XYWrapper(letzterPunkt.getX(), letzterPunkt.getY() + 1);
            case 2://rechts
                return new XYWrapper(letzterPunkt.getX() + 1, letzterPunkt.getY());
            case 3://unten
                return new XYWrapper(letzterPunkt.getX(), letzterPunkt.getY() - 1);
            default://Darf es nicht geben
                System.err.println("Falsche Richtung. Setze rechts");
                return new XYWrapper(letzterPunkt.getX() + 1, letzterPunkt.getY());
        }
    }

    public boolean isAUSGABE_VERMEIDUNG() {
        return AUSGABE_VERMEIDUNG;
    }

    public void setAUSGABE_VERMEIDUNG(boolean AUSGABE_VERMEIDUNG) {
        this.AUSGABE_VERMEIDUNG = AUSGABE_VERMEIDUNG;
    }

    public boolean isAUSGABE_LABYRINTHNEUBAU() {
        return AUSGABE_LABYRINTHNEUBAU;
    }

    public void setAUSGABE_LABYRINTHNEUBAU(boolean AUSGABE_LABYRINTHNEUBAU) {
        this.AUSGABE_LABYRINTHNEUBAU = AUSGABE_LABYRINTHNEUBAU;
    }

    public boolean isMezmerize() {
        return mezmerize;
    }

    public void setMezmerize(boolean mezmerize) {
        this.mezmerize = mezmerize;
    }

    public boolean isKompliziert() {
        return kompliziert;
    }

    public void setKompliziert(boolean kompliziert) {
        this.kompliziert = kompliziert;
    }

    public boolean isB_nichtzahlenausschluss() {
        return b_nichtzahlenausschluss;
    }

    public void setB_nichtzahlenausschluss(boolean b_nichtzahlenausschluss) {
        this.b_nichtzahlenausschluss = b_nichtzahlenausschluss;
    }

    public int getZahlenausschluss() {
        return zahlenausschluss;
    }

    public void setZahlenausschluss(int zahlenausschluss) {
        this.zahlenausschluss = zahlenausschluss;
    }

    public int getNormabstand() {
        return normabstand;
    }

    public void setNormabstand(int normabstand) {
        this.normabstand = normabstand;
    }

    private int getRichtung() {
        int ret;
        do {
            ret = (int) Math.floor((Math.random() * 4));
        } while (ret == zahlenausschluss);
        return ret;
    }

    private XYWrapper getFreipunkt(char[][] labyrinth, String msg) {
        XYWrapper punkt = Areale.DEFFLAWRAP.getStartPunkt();

        if (startPunkt.getY() == labyrinth[0].length - 1 && endPunkt.getY() == 0) {
            if (startPunkt.getY() < labyrinth[0].length / 2) {
                for (int y = 0; y < labyrinth[0].length; y++) {
                    boolean nichtsfrei = true;

                    for (int x = 0; x < labyrinth.length; x++) {
                        if (labyrinth[x][y] == FREI) {
                            punkt = new XYWrapper(x, y);
                            nichtsfrei = false;
                        }
                    }
                    if (nichtsfrei) {
                        break;
                    }
                }
            } else {
                for (int y = labyrinth[0].length - 1; y > 0; y--) {
                    boolean nichtsfrei = true;

                    for (int x = 0; x < labyrinth.length; x++) {
                        if (labyrinth[x][y] == FREI) {
                            punkt = new XYWrapper(x, y);
                            nichtsfrei = false;
                        }
                    }
                    if (nichtsfrei) {
                        break;
                    }
                }
            }
        } else {

            if (startPunkt.getX() < labyrinth.length / 2) {
                for (int x = 0; x < labyrinth.length; x++) {
                    boolean nichtsfrei = true;
                    for (int y = 0; y < labyrinth[x].length; y++) {
                        if (labyrinth[x][y] == FREI) {
                            punkt = new XYWrapper(x, y);
                            nichtsfrei = false;
                        }
                    }
                    if (nichtsfrei) {
                        break;
                    }
                }
            } else {
                for (int x = labyrinth.length - 1; x >= 0; x--) {
                    boolean nichtsfrei = true;
                    for (int y = 0; y < labyrinth[x].length; y++) {
                        if (labyrinth[x][y] == FREI) {
                            punkt = new XYWrapper(x, y);
                            nichtsfrei = false;
                        }
                    }
                    if (nichtsfrei) {
                        break;
                    }
                }

            }
        }
        if (AUSGABE_FREIPUNKT) {
            System.out.println(msg + new XYWrapper(punkt.getX() + 1, punkt.getY()));
        }

        return new XYWrapper(punkt.getX() + 1, punkt.getY());
    }

    public boolean isFreilinie() {
        return freilinie;
    }

    public void setFreilinie(boolean freilinie) {
        this.freilinie = freilinie;
    }

    /**
     * links: 0 --> x-1 <br>
     * oben: 1 --> y+1 <br>
     * rechts: 2 --> x+1 <br>
     * unten: 3 --> y-1
     */
    void sperreRichtung(int richtung) {
        b_nichtzahlenausschluss = false;
        zahlenausschluss = richtung;
    }

}
