package ml;

import java.io.Serializable;

class UnorderedPair<T1, T2> implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -4016575602203280524L;
	private final T1			t1;
	private final T2			t2;

	UnorderedPair(T1 t1, T2 t2) {
		this.t1 = t1;
		this.t2 = t2;
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