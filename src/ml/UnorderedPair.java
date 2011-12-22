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

import java.io.Serializable;

public class UnorderedPair<T1, T2> implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -4016575602203280524L;
    private final T1 t1;
    private final T2 t2;

    public UnorderedPair(T1 t1, T2 t2) {
	this.t1 = t1;
	this.t2 = t2;
    }

    @SuppressWarnings("unchecked")
    public UnorderedPair(Object[] t) {
	// great stuff: the elegant syntax of java combined with the type-safety
	// of perl!
	this.t1 = (T1) t[0];
	this.t2 = (T2) t[1];
    }

    // HBondTypePair(HBondParameter par) {
    // this(par.t1, par.t2);
    // }

    @Override
    public int hashCode() {
	return this.t1.hashCode() + this.t2.hashCode();
    }

    @Override
    public boolean equals(Object o) {
	if (o instanceof UnorderedPair) {
	    // looks strange, but seems to be the right way to get rid of
	    // the unchecked cast warning
	    final UnorderedPair<?, ?> other = (UnorderedPair<?, ?>) o;

	    return (this.t1.equals(other.t1) && this.t2.equals(other.t2))
		    || (this.t1.equals(other.t2) && this.t2.equals(other.t1));
	} else {
	    return false;
	}
    }

    public T1 get1() {
	return this.t1;
    }

    public T2 get2() {
	return this.t2;
    }
}