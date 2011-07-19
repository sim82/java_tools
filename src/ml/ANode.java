/*
 * Copyright (C) 2009 Simon A. Berger
 * 
 *  This program is free software; you may redistribute it and/or modify its
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 */
package ml;

public class ANode {

    static int serialCount = 1;

    public final int serial = serialCount++;
    int localSerial = -1;

    boolean isTip;
    private String tipName;
    private double support;
    private String nodeLabel;
    private int tipSerial;

    // a multi purpose sequence field, for use in special/hacky code like
    // sequence simulation

    public byte[] seq;

    public boolean contentEquals(ANode other) {
	if (this.isTip) {
	    return other.isTip && this.tipName.equals(other.tipName)
		    && this.support == other.support;
	} else {
	    return !other.isTip && this.support == other.support;
	}
    }

    public ANode() {
    }

    public ANode(ANode other) {
	this.isTip = other.isTip;
	this.tipName = other.tipName;
	this.support = other.support;
    }

    public void setTipName(String name) {
	assert (!isTip && name != null);
	tipName = name;
    }

    public String getTipName() {

	// assert( isTip );

	if (!isTip) {
	    throw new RuntimeException("getTipName called for non tip");
	}
	return tipName;
    }

    public String toString() {
	if( isTip ) {
	    return "TIP:" + tipName;
	} else {
	    return "NODE:" + serial;
	}
	
    }
    
    void setSupport(double support) {
	this.support = support;
    }

    // note: the support ist not really a property of the nodes, but it is
    // convenient for the parser to store them in the nodes.
    double getSupport() {
	return support;
    }

    public boolean isTip(String taxon) {
	return isTip && getTipName().equals(taxon);
    }

    public void setTipSerial(int serial) {
	this.tipSerial = serial;
    }

    public int getTipSerial() {
	return this.tipSerial;
    }

    public void setNodeLabel(String label) {
	nodeLabel = label;
    }

    public String getNodeLabel() {
	return nodeLabel;
    }
}
