package ml;


public class LN {

	ANode data;
	LN next;
	LN back;
	double backLen;
    String backLabel;
    double backSupport;

	public LN( ANode data ) {
		this.data = data;
	}

	public static LN create() {
		ANode data = new ANode();

		LN n = new LN(data);
		n.next = new LN(data);
		n.next.next = new LN(data);
		n.next.next.next = n;
		return n;
	}

    public static LN[] getAsList(LN n) {
		int nNodes = countNodes(n);
		LN[] list = new LN[nNodes];



		int xpos = insertDFS(n, list, 0, true );

		if (xpos != nNodes) {
			throw new RuntimeException("xpos != nNodes");
		}

		return list;
	}

    private static int countNodes(LN n) {
        return countNodes(n, true);
    }
    private static int countNodes(LN n, boolean back) {
//		if (n.data.isTip) {
//			return 1;
//		} else {
//			return 1 + countNodes(n.next.back) + countNodes(n.next.next.back);
//		}

        if( n == null ) {
            return 0;
        } else {
            if( back ) {
                return 1 + countNodes(n.back, false ) + countNodes(n.next.back, false) + countNodes(n.next.next.back, false);
            } else {
                return 1 + countNodes(n.next.back, false) + countNodes(n.next.next.back, false);
            }
        }

	}

	private static int insertDFS(LN n, LN[] list, int pos) {
//		if (n.data.isTip) {
//			list[pos] = n;
//			return pos + 1;
//		} else {
//
//			pos = insertDFS(n.next.back, list, pos);
//			pos = insertDFS(n.next.next.back, list, pos);
//			list[pos] = n;
//			return pos + 1;
//		}

        return insertDFS(n, list, pos, true );
	}

    private static int insertDFS(LN n, LN[] list, int pos, boolean back ) {
        if( n != null ) {
            if( back ) {
                pos = insertDFS( n.back, list, pos, false );
            }
            pos = insertDFS(n.next.back, list, pos, false );
			pos = insertDFS(n.next.next.back, list, pos, false );
			list[pos] = n;
			return pos + 1;
        } else {
            return pos;
        }

    }

    public static LN getTowardsTree( LN ln ) {
        if( !ln.data.isTip ) {
            throw new RuntimeException( "this method must only be called for tip LNs" );
        }

        for( int i = 0; i < 3; i++, ln = ln.next ) {
            if( ln.back != null ) {
                return ln;
            }
        }

        throw new RuntimeException( "this seems to be a completely unlinked LN (all LNs in ring have no back pointer" );
    }
}
