/*
------------------------------------------------------------------------
JavaANPR - Automatic Number Plate Recognition System for Java
------------------------------------------------------------------------

This file is a part of the JavaANPR, licensed under the terms of the
Educational Community License

Copyright (c) 2006-2007 Ondrej Martinsky. All rights reserved

This Original Work, including software, source code, documents, or
other related items, is being provided by the copyright holder(s)
subject to the terms of the Educational Community License. By
obtaining, using and/or copying this Original Work, you agree that you
have read, understand, and will comply with the following terms and
conditions of the Educational Community License:

Permission to use, copy, modify, merge, publish, distribute, and
sublicense this Original Work and its documentation, with or without
modification, for any purpose, and without fee or royalty to the
copyright holder(s) is hereby granted, provided that you include the
following on ALL copies of the Original Work or portions thereof,
including modifications or derivatives, that you make:

# The full text of the Educational Community License in a location
viewable to users of the redistributed or derivative work.

# Any pre-existing intellectual property disclaimers, notices, or terms
and conditions.

# Notice of any changes or modifications to the Original Work,
including the date the changes were made.

# Any modifications of the Original Work must be distributed in such a
manner as to avoid any confusion with the Original Work of the
copyright holders.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

The name and trademarks of copyright holder(s) may NOT be used in
advertising or publicity pertaining to the Original or Derivative Works
without specific, written prior permission. Title to copyright in the
Original Work and any associated documentation will at all times remain
with the copyright holders. 

If you want to alter upon this work, you MUST attribute it in 
a) all source files
b) on every place, where is the copyright of derivated work
exactly by the following label :

---- label begin ----
This work is a derivate of the JavaANPR. JavaANPR is a intellectual 
property of Ondrej Martinsky. Please visit http://javaanpr.sourceforge.net 
for more info about JavaANPR. 
----  label end  ----

------------------------------------------------------------------------
                                         http://javaanpr.sourceforge.net
------------------------------------------------------------------------
*/


package javaanpr.imageanalysis;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javaanpr.intelligence.Intelligence;


public class PlateVerticalGraph extends Graph {
    private static double peakFootConstant =// 0.42;  /* CONSTANT*/
            Intelligence.configurator.getDoubleProperty("plateverticalgraph_peakfootconstant");
    
    Plate handle;
    
    public PlateVerticalGraph(Plate handle) {
        this.handle = handle;
    }
    
    public class PeakComparer implements Comparator {
        PlateVerticalGraph graphHandle = null;
        
        public PeakComparer(PlateVerticalGraph graph) {
            this.graphHandle = graph;
        }
        
        private float getPeakValue(Object peak) {
            // heuristika : aky vysoky (siroky na gragfe) je kandidat na pismeno
            // preferuju sa vyssie
            //return ((Peak)peak).getDiff();
            
            // vyska peaku
            return this.graphHandle.yValues.elementAt(((Peak)peak).getCenter());
            
            // heuristika : 
            // ako daleko od stredu je kandidat
//            int peakCenter = (  ((Peak)peak).getRight() + ((Peak)peak).getLeft()  )/2;
//            return Math.abs(peakCenter - this.graphHandle.yValues.size()/2);
        }
        
        public int compare(Object peak1, Object peak2) { // Peak
            double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
            if (comparison < 0) return -1;
            if (comparison > 0) return 1;
            return 0;
        }
    }
    
    public Vector<Peak> findPeak(int count) {
        
        // znizime peak
        for (int i=0; i<this.yValues.size();i++)
            this.yValues.set(i,this.yValues.elementAt(i) - this.getMinValue());
        
        Vector<Peak> outPeaks = new Vector<Peak>();
        
        for (int c=0; c<count; c++) { // for count
            float maxValue = 0.0f;
            int maxIndex = 0;
            for (int i=0; i<this.yValues.size(); i++) { // zlava doprava
                if (allowedInterval(outPeaks, i)) { // ak potencialny vrchol sa nachadza vo "volnom" intervale, ktory nespada pod ine vrcholy
                    if (this.yValues.elementAt(i) >= maxValue) {
                        maxValue = this.yValues.elementAt(i);
                        maxIndex = i;
                    }
                }
            } // end for int 0->max
            // nasli sme najvacsi peak
            
            if (yValues.elementAt(maxIndex) < 0.05 * super.getMaxValue()) break;//0.4
            
            int leftIndex = indexOfLeftPeakRel(maxIndex,peakFootConstant);
            int rightIndex = indexOfRightPeakRel(maxIndex,peakFootConstant);
            
            outPeaks.add(new Peak(
                    Math.max(0,leftIndex),
                    maxIndex,
                    Math.min(this.yValues.size()-1,rightIndex)
                    ));
        }
        
        Collections.sort(outPeaks, (Comparator<? super Graph.Peak>)
                                   new PeakComparer(this));
        super.peaks = outPeaks;
        return outPeaks;
    }
    
    

    
}
