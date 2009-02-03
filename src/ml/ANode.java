package ml;

public class ANode {

	static int serialCount = 1;

	public final int serial = serialCount++;

	boolean isTip;
	private String tipName;
    private double support;
	public void setTipName(String name) {
		assert( !isTip && name != null );
		tipName = name;
	}

	public String getTipName() {

		//assert( isTip );

        if( !isTip ) {
            throw new RuntimeException( "getTipName called for non tip" );
        }
		return tipName;
	}


    void setSupport(double support) {
        this.support = support;
    }

    // note: the support ist not really a property of the nodes, but it is convenient for the parser to store them in the nodes.
    double getSupport() {
        return support;
    }
}



