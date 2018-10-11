/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aisalaby;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author Mario J. Schwaiger <mario.schwaiger at gmail.com>
 */
public class Verzweiger {

    private final char[][] labyrinth;
    private final int normabstand;
    private final char FREI, BELEGT;
    private final boolean AUSGABE_DISTANZEN = false;
    //private int obenuntenwahrscheinlichkeit = 5;
    private Erzeugungsbefehl erzeugungsbefehl = Erzeugungsbefehl.Wahrscheinlichkeit;
    private int erzeugungsbefehlwahrscheinlichkeit = 50;
    private boolean b_benutzeschlüssel = false;
    private int[] verteilungsschlüssel;
    private int intensität = 10;
    private boolean komplexrandentferner = true;
    private boolean erzeugungsoptimierer = true;
    private double erzeugungsoptimiererverhältnis = 3.0;
    private int erzeugungspotimiererdistanz = 2;

    int[] abständeUnten, abständeOben, nullDistanzenUnten, nullDistanzenOben;
    final private boolean AUSGABE_ASIADEFINITION = true, AUSGABE_MAXFLA = false;
    private int mindestgrößeFla = 4;//Die kleinstmögliche gefundene Fläche
    private final boolean AUSGABE_VORANSICHTENTF = false;
    private final boolean AUSGABE_FREISETZER = false;
    private boolean yrandentferner = false;
    private boolean AUSGABE_KOMPLEXRANDENTFERNER = true;
    private boolean komplexrandentfernungskorrektur = true;
    private int miniverzweiger = 1;
    private int durchführungen = 0; //0=bis fertig
    private boolean flächenerhöher = true;

    public Verzweiger(char[][] labyrinth, int normabstand, char frei, char belegt) {
        this.labyrinth = labyrinth;
        this.normabstand = normabstand;
        this.FREI = frei;
        this.BELEGT = belegt;
        verteilungsschlüssel = getStandardSchlüssel(labyrinth.length / normabstand);
    }

    public boolean getVerzweigungen() {

        int verzweigungscounter = 0;

        do {
            verzweigungscounter++;

            abständeUnten = getAbständeUnten(labyrinth);
            abständeOben = getAbständeOben(labyrinth);
            nullDistanzenUnten = getNullDistanzen(abständeUnten);
            nullDistanzenOben = getNullDistanzen(abständeOben);

            if (AUSGABE_DISTANZEN) {
                System.out.println("----Distanzen/Blöcke---");
                System.out.println("Blöcke oben: " + Ausgabe.getArrayAusgabe(abständeOben));
                System.out.println("Blöcke unten: " + Ausgabe.getArrayAusgabe(abständeUnten));
                System.out.println("------------------------------");
                System.out.println("Distanzen oben: " + Ausgabe.getArrayAusgabe(nullDistanzenOben));
                System.out.println("Distanzen unten:" + Ausgabe.getArrayAusgabe(nullDistanzenUnten));
            }

            Scanline s = new Scanline(labyrinth, AisaLaby.BELEGT, AisaLaby.FREI, true);
            s.setMindestgröße(mindestgrößeFla);
            LinkedList<FlaWrapper> prüfflächen = s.getBenutzbareFlächen();

            int streckenteilanzahl = (normabstand);//Länge eines Abschnittes
            /**
             * (Labyrinthabschnitt)/(Flächen)
             */
            ArrayList<LinkedList<FlaWrapper>> flächen = getSortierteFlächen(streckenteilanzahl, prüfflächen);
            //In welchem Abschnitt müssen wir beginnen?

            FlaWrapper maxfla;
            if (b_benutzeschlüssel) {
                int abschnitt = getAbschnitt();
                maxfla = getMaxFla(flächen, abschnitt);
            } else {
                maxfla = getMaxFla(flächen);
            }

            if (maxfla == Areale.DEFFLAWRAP) {
                return false;
            }

            if (AUSGABE_MAXFLA) {
                System.out.println("MaxFla: " + maxfla);
            }

            boolean istNeulabUnter = istNeuLabUnter(maxfla);

            int verbindungsPunktX;
            //Wenn true suchen wir den Punkt mit dem geringsten Abstand nach unten
            //Wenn false suchen wir den Punkt mit dem geringsten Abstand nach oben
            if (istNeulabUnter) {
                verbindungsPunktX = getVerbindungsPunktX(maxfla.getStartPunkt().getX(), maxfla.getEndPunkt().getX(), abständeUnten);
            } else {
                verbindungsPunktX = getVerbindungsPunktX(maxfla.getStartPunkt().getX(), maxfla.getEndPunkt().getX(), abständeOben);
            }

            FlaWrapper mlSPEP = getMinilabSPEP(verbindungsPunktX, maxfla, istNeulabUnter);
            erzeugungsbefehlauswerter(maxfla, mlSPEP);

            char[][] minilab = erzeugeMinilab(getAisa(maxfla, mlSPEP));

            komplexrandentferner(minilab, istNeulabUnter, mlSPEP);

            if (miniverzweiger > 1) {
                for (int i = 0; i < miniverzweiger; i++) {
                    Verzweiger vz = new Verzweiger(minilab, 2, FREI, BELEGT);
                    if (vz.getVerzweigungen() == false) {
                        break;
                    }
                }
            }

            einbauMinilab(maxfla, minilab);

            zusammenführer(verbindungsPunktX, istNeulabUnter, maxfla);
            
            if(flächenerhöher){
            mindestgrößeFla++;
            }

        } while (verzweigungscounter != durchführungen);

        /*
         for (int i = 1; i < labyrinth.length; i++) {
         Setzbar setz = isSetzbar(i);
         if (setz != Setzbar.nirgends) {
         //Setzen oder nichtsetzen?
         if (münzwurf()) {

         //Damit nicht zwei Wege direkt nebeneinander entstehen
         i++;
         wahrscheinlichkeit = normabstand;
         }
         } else {
         if (wahrscheinlichkeit > 1) {
         wahrscheinlichkeit--;
         }
         }
         }
         */
        return true;
    }

    private void zusammenführer(int verbindungsPunktX, boolean istNeulabUnter, FlaWrapper maxfla) {
        int cntCX = verbindungsPunktX;
        int cntCY;

        if (istNeulabUnter) {
            cntCY = maxfla.getEndPunkt().getY() - 1;
        } else {
            cntCY = maxfla.getStartPunkt().getY() + 1;
        }
        if (AUSGABE_VORANSICHTENTF) {
            System.out.println("Vorher:");
            System.out.println(Ausgabe.billigansicht(labyrinth));
        }

        do {
            if (AUSGABE_FREISETZER) {
                System.out.println("Freigesetzt: " + cntCX + "/" + cntCY);
            }
            labyrinth[cntCX][cntCY] = FREI;

            if (istNeulabUnter) {
                if (cntCX + 1 >= labyrinth.length) {
                    break;
                }
                cntCY++;
            } else {
                if (cntCY - 1 <= 0 || cntCY >= labyrinth[0].length) {
                    break;
                }
                cntCY--;
            }
            //System.out.println("cntX: " + cntCX + " cnty: " + cntCY);
            if ((cntCY >= labyrinth[0].length) || cntCX + 1 >= labyrinth.length) {
                break;
            }
        } while (labyrinth[cntCX][cntCY] == BELEGT);

    }

    private void einbauMinilab(FlaWrapper maxfla, char[][] minilab) {
        for (int x = maxfla.getStartPunkt().getX(); x < maxfla.getEndPunkt().getX(); x++) {
            for (int y = maxfla.getStartPunkt().getY(); y <= maxfla.getEndPunkt().getY(); y++) {
                //System.out.println("labyrinth["+x+"]["+y+"] = minilab["+(x - maxfla.getStartPunkt().getX())+"]["+(y - maxfla.getStartPunkt().getY())+"];");
                labyrinth[x][y] = minilab[x - maxfla.getStartPunkt().getX()][y - maxfla.getStartPunkt().getY()];
            }
        }
    }

    private void komplexrandentferner(char[][] minilab, boolean istNeulabUnter, FlaWrapper mlSPEP) {
        if (komplexrandentferner && erzeugungsbefehl == Erzeugungsbefehl.Komplexlabyrinth) {
            for (int x = 0; x < minilab.length; x++) {

                if (!(x == mlSPEP.getStartPunkt().getX())) {
                    minilab[x][minilab[x].length - 1] = BELEGT;
                    minilab[x][0] = BELEGT;
                }
            }

            //Wird empfohlen den auf false zu lassen
            if (yrandentferner) {
                for (int y = 0; y < minilab[0].length; y++) {
                    if (mlSPEP.getStartPunkt().getX() != 0) {
                        minilab[0][y] = BELEGT;
                    }
                    if (mlSPEP.getStartPunkt().getX() != minilab.length - 1) {
                        minilab[minilab.length - 1][y] = BELEGT;
                    }
                }
            }

            if (komplexrandentfernungskorrektur) {
                //
                if (istNeulabUnter) {

                    komplexrandentfernungskorrektur(mlSPEP, minilab, minilab[0].length - 1);

                } //Startpunkt ist auf Nullerlinie
                else {
                    komplexrandentfernungskorrektur(mlSPEP, minilab, 1);
                }
            }

            if (AUSGABE_KOMPLEXRANDENTFERNER) {
                System.out.println(Ausgabe.billigansicht(minilab));
            }

        }
    }

    private void komplexrandentfernungskorrektur(FlaWrapper mlSPEP, char[][] minilab, int val) {
        boolean gefunden = false;
        int pos = mlSPEP.getStartPunkt().getX();
        for (int x = mlSPEP.getStartPunkt().getX(); x < minilab.length; x++) {
            if (minilab[x][val] == FREI) {
                gefunden = true;
                pos = x;
            }
        }
        if (gefunden) {
            for (int x = mlSPEP.getStartPunkt().getX(); x < pos; x++) {
                minilab[x][val] = FREI;
            }
        } else {
            for (int x = mlSPEP.getStartPunkt().getX(); x >= 0; x--) {
                minilab[x][1] = FREI;
                if (minilab[x - 1][val] == FREI) {
                    break;
                }
            }
        }
    }

    private void erzeugungsbefehlauswerter(FlaWrapper maxfla, FlaWrapper mlSPEP) {
        if (erzeugungsoptimierer) {

            if (maxfla.getEndPunktHarmonisiert().getY() * erzeugungsoptimiererverhältnis < maxfla.getEndPunktHarmonisiert().getX()) {

                if (mlSPEP.getStartPunkt().getX() == mlSPEP.getEndPunkt().getX()) {
                    erzeugungsbefehl = Erzeugungsbefehl.Komplexlabyrinth;
                    return;
                }

                //System.out.println(mlSPEP.getStartPunkt().getX()+">"+(mlSPEP.getEndPunkt().getX()-erzeugungspotimiererdistanz)+"&&"+mlSPEP.getStartPunkt().getX()+"<"+mlSPEP.getEndPunkt().getX()+"||"+mlSPEP.getStartPunkt().getX()+">"+mlSPEP.getEndPunkt().getX()+"&&"+mlSPEP.getStartPunkt().getX()+"<"+(mlSPEP.getEndPunkt().getX()+erzeugungspotimiererdistanz));
                if (((mlSPEP.getStartPunkt().getX() > mlSPEP.getEndPunkt().getX() - erzeugungspotimiererdistanz) && (mlSPEP.getStartPunkt().getX() < mlSPEP.getEndPunkt().getX())) || ((mlSPEP.getStartPunkt().getX() > mlSPEP.getEndPunkt().getX()) && (mlSPEP.getStartPunkt().getX() < mlSPEP.getEndPunkt().getX() + erzeugungspotimiererdistanz))) {
                    erzeugungsbefehl = Erzeugungsbefehl.Komplexlabyrinth;
                }

                erzeugungsbefehl = Erzeugungsbefehl.Normlabyrinth;
            } else {
                erzeugungsbefehlauswerter();
            }

        } else {
            erzeugungsbefehlauswerter();
        }
    }

    private void erzeugungsbefehlauswerter() {
        if (erzeugungsbefehl == Erzeugungsbefehl.Wahrscheinlichkeit) {
            if (münzwurf(erzeugungsbefehlwahrscheinlichkeit)) {
                erzeugungsbefehl = Erzeugungsbefehl.Komplexlabyrinth;
            } else {
                erzeugungsbefehl = Erzeugungsbefehl.Normlabyrinth;
            }
        }
    }

    private FlaWrapper getMinilabSPEP(int verbindungsPunktX, FlaWrapper maxfla, boolean istNeulabUnter) {

        int startPunktXminilab = (verbindungsPunktX - maxfla.getStartPunkt().getX()) + 1;
        int yStart = 0;
        int yEnde = 0;
        int xEnde;

        if (istNeulabUnter) {
            yStart = maxfla.getEndPunktHarmonisiert().getY() - 1;
        } else {
            yEnde = maxfla.getEndPunktHarmonisiert().getY() - 1;
        }

        if (startPunktXminilab > (maxfla.getEndPunktHarmonisiert().getX())) {
            xEnde = getZufall((maxfla.getEndPunktHarmonisiert().getX() / 2));
        } else {
            xEnde = maxfla.getEndPunktHarmonisiert().getX() / 2 + getZufall((maxfla.getEndPunktHarmonisiert().getX() / 2));
        }

        if (AUSGABE_ASIADEFINITION) {
            System.out.println("StartP: " + new XYWrapper(startPunktXminilab, yStart));
            System.out.println("EndeP : " + new XYWrapper(xEnde, yEnde));
        }

        return new FlaWrapper(new XYWrapper(startPunktXminilab, yStart), new XYWrapper(xEnde, yEnde));
    }

    private char[][] erzeugeMinilab(AisaLaby aisa) {
        switch (erzeugungsbefehl) {
            case Komplexlabyrinth:
                //boolean überschneidungsvermeidung, boolean kompliziert, boolean flächenentfernung
                return getKomplexLabyrinth(aisa);
            case Normlabyrinth:
                return aisa.getLabyrint(true, false, false);
            default:
            case Wahrscheinlichkeit:
                if (münzwurf(erzeugungsbefehlwahrscheinlichkeit)) {
                    return getKomplexLabyrinth(aisa);
                } else {
                    return aisa.getLabyrint(true, false, false);
                }
        }
    }

    private static char[][] getKomplexLabyrinth(AisaLaby aisa) {
        aisa.setFreilinie(false);
        return aisa.getLabyrint(false, true, true);
    }

    private AisaLaby getAisa(FlaWrapper maxfla, FlaWrapper spep) {

        //Fläche, Startpunkt, Endpunkt        
        AisaLaby aisa = new AisaLaby(new XYWrapper(maxfla.getEndPunktHarmonisiert().getX(), maxfla.getEndPunktHarmonisiert().getY()), spep.getStartPunkt(), spep.getEndPunkt());

        if (spep.getStartPunkt().getX() > spep.getEndPunkt().getX()) {
            aisa.sperreRichtung(2);
        }
        aisa.setNormabstand(0);
        return aisa;
    }

    private int getVerbindungsPunktX(int xStart, int xEnde, int[] prüfelement) {
        int verbindungsPunktX = xStart;

        int anzahl = prüfelement[xStart];

        for (int i = xStart; i < xEnde; i++) {

            if (prüfelement[i] < anzahl) {
                anzahl = prüfelement[i];
                verbindungsPunktX = i;
            }
        }

        return verbindungsPunktX;
    }

    private boolean istNeuLabUnter(FlaWrapper maxfla) {
        boolean istNeulabUnter = false;//Default: Neues Labyrinth ist über altem
        if (maxfla.getStartPunkt().getY() <= 1) {
            istNeulabUnter = true;//Wenn der Startpunkt 0 oder 1 ist, ist es logischerweise darunter...
        } else {
            for (int y = maxfla.getStartPunkt().getY(); y < labyrinth[0].length; y++) {
                if (labyrinth[maxfla.getStartPunkt().getX()][y] == FREI) {
                    istNeulabUnter = true;
                    break;
                }
            }
        }
        return istNeulabUnter;
    }

    private FlaWrapper getMaxFla(ArrayList<LinkedList<FlaWrapper>> flächen, int abschnitt) {
        FlaWrapper maxfla = Areale.DEFFLAWRAP;
        int intab = abschnitt;
        int cnt = 0;
        while (flächen.get(abschnitt).isEmpty()) {
            if (cnt > flächen.size()) {
                return Areale.DEFFLAWRAP;
            }
            intab = getAbschnitt();
            cnt++;
        }
        for (FlaWrapper fla : flächen.get(abschnitt)) {
            if (fla.istGrößer(maxfla)) {
                maxfla = fla;
            }
        }
        return maxfla;
    }

    private FlaWrapper getMaxFla(ArrayList<LinkedList<FlaWrapper>> flächen) {
        FlaWrapper maxfla = Areale.DEFFLAWRAP;
        int cnt = 0;
        for (LinkedList<FlaWrapper> list : flächen) {
            for (FlaWrapper fla : list) {
                if (fla.istGrößer(maxfla)) {
                    maxfla = fla;
                }
            }
        }
        return maxfla;
    }

    private int getAbschnitt() {
        int abschnitt = verteilungsschlüssel[0];
        int abschnittpos = 0;
        int maxschl = 0;
        //int maxpos= 0;
        for (int i = 0; i < verteilungsschlüssel.length; i++) {
            if (verteilungsschlüssel[i] < abschnitt) {
                abschnitt = verteilungsschlüssel[i];
                abschnittpos = i;
            }
            if (verteilungsschlüssel[i] > maxschl) {
                maxschl = verteilungsschlüssel[i];
                //maxpos=i;
            }
        }
        for (int i = 0; i < verteilungsschlüssel.length; i++) {
            verteilungsschlüssel[i]--;
        }
        verteilungsschlüssel[abschnittpos] = maxschl;
        return abschnitt - 1;
    }

    private ArrayList<LinkedList<FlaWrapper>> getSortierteFlächen(int streckenteilanzahl, LinkedList<FlaWrapper> prüfflächen) {
        ArrayList<LinkedList<FlaWrapper>> flächen = flächenarrayinitialisierer(streckenteilanzahl);

        for (FlaWrapper fla : prüfflächen) {
            int[] flächenmenge = arraynullsetzer(streckenteilanzahl);

            for (int i = fla.getStartPunkt().getX(); i < fla.getEndPunkt().getX(); i++) {
                if (((int) (i) / (labyrinth.length / normabstand)) < flächenmenge.length) {
                    flächenmenge[((int) (i) / (labyrinth.length / normabstand))]++;
                }
            }

            int max = 0;
            int pos = 0;
            for (int i = 0; i < flächenmenge.length; i++) {
                if (flächenmenge[i] > max) {
                    max = flächenmenge[i];
                    pos = i;
                }
            }
            flächen.get(pos).add(fla);
        }
        return flächen;
    }

    private int[] getAbständeUnten(char[][] labyrinth) {
        int[] absU = new int[labyrinth.length];

        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[i].length; j++) {
                if (labyrinth[i][j] == FREI) {
                    if (AUSGABE_DISTANZEN) {
                        System.out.println("labyrinth[" + i + "][" + j + "]=" + labyrinth[i][j] + "; Abstand = " + j);
                    }
                    absU[i] = j;
                    break;
                }
                //Wenn der Abstand der Labyrinthhöhe entspricht ergibt es keinen Sinn
                //hier etwas zu setzen. Dann ist nämlich die Position unbelegt
                if (j == (labyrinth[i].length - 1)) {
                    absU[i] = 0;
                }
            }
        }

        return absU;
    }

    private int[] getAbständeOben(char[][] labyrinth) {
        int[] absO = new int[labyrinth.length];

        for (int i = labyrinth.length - 1; i >= 0; i--) {
            for (int j = labyrinth[i].length - 1; j >= 0; j--) {
                if (labyrinth[i][j] == FREI) {
                    absO[i] = labyrinth[i].length - (j + 1);
                    break;
                }
                //Wenn der Abstand der Labyrinthhöhe entspricht ergibt es keinen Sinn
                //hier etwas zu setzen. Dann ist nämlich die Position unbelegt
                if (j == 0) {
                    absO[i] = 0;
                }
            }
        }

        return absO;
    }

    private int[] getNullDistanzen(int[] abständeBasis) {
        int[] abstände = new int[abständeBasis.length + 2];
        int[] nulldistanz = new int[abständeBasis.length];

        int[] nulldistanzlinks = new int[abständeBasis.length];
        int[] nulldistanzrechts = new int[abständeBasis.length];

        abstände[0] = 0;
        System.arraycopy(abständeBasis, 0, abstände, 1, abständeBasis.length);
        abstände[abstände.length - 1] = 0;

        //Hier werden die Abstände zu den Blöcken links gezählt
        int i_zähler = 0;
        for (int i = 0; i < abstände.length; i++) {

            if (abstände[i] != 0) {
                i_zähler++;
                if (i != 0 && i != abstände.length - 1) {
                    nulldistanzlinks[i - 1] = i_zähler;
                }
                continue;
            }

            if (abstände[i] == 0) {
                i_zähler = 0;

                if (i != 0 && i != abstände.length - 1) {
                    nulldistanzlinks[i - 1] = 0;
                }
            }
        }

        //Hier werden die Abstände zu den Blöcken rechts gezählt
        i_zähler = 0;
        for (int i = abstände.length - 1; i >= 0; i--) {

            if (abstände[i] != 0) {
                i_zähler++;
                if (i != 0 && i != abstände.length - 1) {
                    nulldistanzrechts[i - 1] = i_zähler;
                }
                continue;
            }

            if (abstände[i] == 0) {
                i_zähler = 0;

                if (i != 0 && i != abstände.length - 1) {
                    nulldistanzrechts[i - 1] = 0;
                }
            }
        }

        //Die kleinere Zahl wird genommen
        //Die entspricht dann dem Mindestabstand
        for (int i = 0; i < nulldistanzrechts.length; i++) {
            if (nulldistanzlinks[i] < nulldistanzrechts[i]) {
                nulldistanz[i] = nulldistanzlinks[i];
            } else {
                nulldistanz[i] = nulldistanzrechts[i];
            }
        }

        return nulldistanz;
    }

    private boolean münzwurf() {
        return ((int) Math.floor((Math.random() * normabstand))) == 0;
    }

    private Setzbar isSetzbar(int i) {

        return Setzbar.nirgends;
    }

    private int[] arraynullsetzer(int normabstand) {
        int[] array = new int[normabstand];
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
        return array;
    }

    private ArrayList<LinkedList<FlaWrapper>> flächenarrayinitialisierer(int streckenteilanzahl) {
        ArrayList<LinkedList<FlaWrapper>> ret = new ArrayList<>(streckenteilanzahl);
        for (int i = 0; i < streckenteilanzahl; i++) {
            ret.add(new LinkedList<>());
        }
        return ret;
    }

    public Erzeugungsbefehl getErzeugungsbefehl() {
        return erzeugungsbefehl;
    }

    public void setErzeugungsbefehl(Erzeugungsbefehl erzeugungsbefehl) {
        this.erzeugungsbefehl = erzeugungsbefehl;
    }

    public int getErzeugungsbefehlwahrscheinlichkeit() {
        return erzeugungsbefehlwahrscheinlichkeit;
    }

    public void setErzeugungsbefehlwahrscheinlichkeit(int erzeugungsbefehlwahrscheinlichkeit) {
        this.erzeugungsbefehlwahrscheinlichkeit = erzeugungsbefehlwahrscheinlichkeit;
    }

    public int[] getVerteilungsschlüssel() {
        return verteilungsschlüssel;
    }

    public void setVerteilungsschlüssel(int[] verteilungsschlüssel) {
        this.verteilungsschlüssel = verteilungsschlüssel;
    }

    private int[] getStandardSchlüssel(int defAnz) {
        int[] ss = new int[defAnz];
        for (int i = 0; i < defAnz; i++) {
            ss[i] = i + 1;
        }
        return ss;
    }

    public int getIntensität() {
        return intensität;
    }

    public void setIntensität(int intensität) {
        this.intensität = intensität;
    }

    /**
     * Gibt Zufall von 0 bis X-1 zurück
     *
     * @param x Maximalwert +1
     * @return Zufallszahl
     */
    private int getZufall(int x) {
        return (int) Math.floor((Math.random() * x));
    }

    private boolean münzwurf(int wahrscheinlichkeit) {
        return (int) Math.floor((Math.random() * 100)) > wahrscheinlichkeit - 1;
    }

    public boolean isKomplexrandentferner() {
        return komplexrandentferner;
    }

    public void setKomplexrandentferner(boolean komplexrandentferner) {
        this.komplexrandentferner = komplexrandentferner;
    }

    public void setMindestgrößeFla(int mindestgrößeFla) {
        this.mindestgrößeFla = mindestgrößeFla;
    }

    public boolean isB_benutzeschlüssel() {
        return b_benutzeschlüssel;
    }

    public void setB_benutzeschlüssel(boolean b_benutzeschlüssel) {
        this.b_benutzeschlüssel = b_benutzeschlüssel;
    }

    public int getMiniverzweiger() {
        return miniverzweiger;
    }

    public void setMiniverzweiger(int miniverzweiger) {
        this.miniverzweiger = miniverzweiger;
    }

    public int getDurchführungen() {
        return durchführungen;
    }

    public void setDurchführungen(int durchführungen) {
        this.durchführungen = durchführungen;
    }

}
