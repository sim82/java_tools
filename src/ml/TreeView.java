package ml;

import java.awt.Color;
import java.awt.Container;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;




class MyBranch {
	UnorderedPair<Integer, Integer> nodes;
}

public class TreeView {

	
//	
//	public TreeView() {
//		final File basedir = new File("/space/raxml/VINCENT/");
//		
//		final String filename = "RAxML_bipartitions.714.BEST.WITH";
//		final File f = new File(basedir, filename);
//
//		final TreeParser tp = new TreeParser(f);
//
//
//		final LN n = tp.parse();
//
//		LN[] list = LN.getAsList(n);
//
//		// generate branch set
//		
//		Set<UnorderedPair<Integer,Integer>> branches = new HashSet<UnorderedPair<Integer,Integer>>();
//		LN[] flat = new LN[ANode.serialCount];
//		
//		for( LN ln : list ) {
//			if( ln.back != null ) {
//				branches.add(new UnorderedPair<Integer, Integer>(ln.data.serial, ln.back.data.serial));
//			}
//			
//			flat[ln.data.serial] = ln;
//		}
//		
//		//UndirectedSparseGraph<Integer, MyBranch> g = new UndirectedSparseGraph<Integer, MyBranch>();
//		OrderedKAryTree<Integer, MyBranch> g = new OrderedKAryTree<Integer, MyBranch>(2);
//		
//		for( UnorderedPair<Integer, Integer> b : branches ) {
//			MyBranch e = new MyBranch();
//			e.nodes = b;
//			
//			g.addEdge(e, b.get1(), b.get2());
//			
//			System.out.printf( "add edge: %d %d\n", b.get1(), b.get2() ); 
//		}
//		
//		
//	//	ISOMLayout<Integer, MyBranch> layout = new ISOMLayout<Integer, MyBranch>(g);
//	//	Forest<Integer, MyBranch> f = new Forest<Integer, MyBranch>();
//		TreeLayout<Integer, MyBranch> layout = new TreeLayout<Integer, MyBranch>(g);
//		
//		DefaultVisualizationModel<Integer, MyBranch> visualizationModel = new DefaultVisualizationModel<Integer, MyBranch>(layout);
//		VisualizationViewer<Integer, MyBranch> vv = new VisualizationViewer<Integer, MyBranch>(visualizationModel);
//		VertexLabelAsShapeRenderer<Integer, MyBranch> vlasr = new VertexLabelAsShapeRenderer<Integer, MyBranch>(vv.getRenderContext());
//
//		// customize the render context
//		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Integer>());
//
//		vv.getRenderContext().setVertexShapeTransformer(vlasr);
//		vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.red));
//		vv.getRenderer().setVertexLabelRenderer(vlasr);
//		GraphMouse graphMouse = new PluggableGraphMouse();
//
//		
//		vv.setGraphMouse(graphMouse);
//		// this.vv.addKeyListener(graphMouse.getModeKeyListener());
//
//		JFrame jf = new JFrame();
//		
//		Container content = jf.getContentPane();
//		final GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
//		content.add(gzsp);
//		jf.pack();
//
//		jf.setVisible(true);
//		
//	}
//	
//	public static void main(String[] args) {
//		new TreeView();
//	}
}
