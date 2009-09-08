package ml;

public class ANode {

	static int serialCount = 1;

	public final int serial = serialCount++;

	boolean isTip;
	private String tipName;
    private double support;
    private String nodeLabel;
	private int tipSerial;

    public boolean contentEquals( ANode other ) {
        if( this.isTip) {
            return other.isTip && this.tipName.equals(other.tipName) && this.support == other.support;
        } else {
            return !other.isTip && this.support == other.support;
        }
    }



    public ANode() {
    }

    public ANode( ANode other ) {
        this.isTip = other.isTip;
        this.tipName = other.tipName;
        this.support = other.support;
    }

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


    boolean isTip(String taxon) {
        return isTip && getTipName().equals(taxon);
    }



	public void setTipSerial(int serial) {
		this.tipSerial = serial;
	}
	
	public int getTipSerial() {
		return this.tipSerial;
	}
	
	
	public void setNodeLabel( String label ) {
		nodeLabel = label;
	}
	
	public String getNodeLabel() {
		return nodeLabel;
	}
}



