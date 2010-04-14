package cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

interface Obj {
	float dist( Obj other );
}

//class Point implements Obj {
//	float x, y;
//	
//	Point( float x, float y ) {
//		this.x = x;
//		this.y = y;
//	}
//	
//	@Override
//	public float dist(Obj _other) {
//		Point other = (Point)_other;
//		
//		float dx = x - other.x;
//		float dy = y - other.y;
//		
//		float d = dx * dx + dy * dy;
//		
//		return (float) Math.sqrt(d);
//	}
//	
//}

class Point implements Obj {
	float x;
	
	Point( float x ) {
		this.x = x;
		
	}
	
	@Override
	public float dist(Obj _other) {
		Point other = (Point)_other;
		
		return Math.abs(x - other.x);
	}

}


class OsMember implements Comparable<OsMember> {
	int obj;
	float rDist;
	
	OsMember( int obj, float rDist ) {
		this.obj = obj;
		this.rDist = rDist;
	}

	@Override
	public int compareTo(OsMember o) {
		
		return (int)Math.signum(rDist - o.rDist);
	}
	public boolean equals( Object o ) {
		if( o instanceof OsMember ) {
			return obj == ((OsMember)o).obj;
		} else {
			return false;
		}
	}
	
}

public class Optics {
	static Obj[] objSet;
	static boolean[] processed;
	static float[] reachDist;
	static float[] coreDist;
	
	static int[] orderedSet;
	static int osNext = 0;
	
	static PriorityQueue<OsMember> orderSeeds;
	
	
	static void optics() {
		final int N = 11;
		objSet = new Obj[N];
		processed = new boolean[N];
		reachDist = new float[N];
		coreDist = new float[N];
		
		Arrays.fill( reachDist, Float.NaN);
		Arrays.fill( coreDist, Float.NaN);
		
		
		orderedSet = new int[N];
		
		orderSeeds = new PriorityQueue<OsMember>();
		
		Random rnd = new Random();
//		for( int i = 0; i < N; i++ ) {
//			objSet[i] = new Point( rnd.nextFloat(), rnd.nextFloat() );
//		}
		
		double[] p = {1,4,6,2,5,7,1.5,3,4.5,6.5,1.7};
		for( int i = 0; i < N; i++ ) {
			objSet[i] = new Point( (float)p[i] );
		//	objSet[i] = new Point( rnd.nextFloat() );
		}
		
		
		
		for( int i = 0; i < N; i++ ) {
			if( !processed[i] ) {
				expandClusterOrder( i, 1.1f, 1 );
			}
		}
	}

	
	private static int[] getNeighbors( int obj, float eps ) {
		ArrayList<Integer> l = new ArrayList<Integer>();
		Obj o = objSet[obj];
		for( int i = 0; i < objSet.length; i++ ) {
			if( i != obj ) {
				if( o.dist(objSet[i]) <= eps ) {
					l.add(i);
				}
			}
		}
		
		int[] ll = new int[l.size()];
		for( int i = 0; i < ll.length; i++ ) {
			ll[i] = l.get(i);
		}
		//System.out.printf( "neighbors %d: %d\n", obj, ll.length );
		return ll;
	}
	
	
	private static void setCoreDistance(int obj, int[] neighbors, float eps,
			int minPts) {
	
		if( neighbors.length < minPts ) {
			coreDist[obj] = Float.NaN;
		} else {
			Obj o = objSet[obj];
			float[] ndists = new float[neighbors.length];
			for( int i = 0; i < neighbors.length; i++ ) {
				ndists[i] = o.dist(objSet[neighbors[i]]);
			}
			
			Arrays.sort(ndists);
			
			coreDist[obj] = ndists[minPts-1];
		}
		
		//System.out.printf( "coreDist %d: %f\n", obj, coreDist[obj] );
	}

	
	private static void orderSeedsUpdate(int[] neighbors, int centerObj) {
		float cDist = coreDist[centerObj];
		assert( isDefined(cDist ));
		
		Obj co = objSet[centerObj];
		
		for( int obj : neighbors ) {
			if( !processed[obj] ) {
				Obj o = objSet[obj];
				
				float newRDist = Math.max( cDist, co.dist(o));
				
				if( !isDefined(reachDist[obj]) ) {
					reachDist[obj] = newRDist;
					
					OsMember osm = new OsMember(obj, newRDist);
					orderSeeds.offer(osm);
				} else {
					//System.out.printf( "new r dist: %f %f\n", newRDist, reachDist[obj] );
					
					
					if( newRDist < reachDist[obj] ) {
						reachDist[obj] = newRDist;
						
						OsMember osm = new OsMember(obj, newRDist);
						orderSeeds.remove(osm);
						orderSeeds.offer(osm);
					}
				}
			}
		}
	}

	
	private static boolean isDefined( float f ) {
		return (f==f);
	}
	
	private static void expandClusterOrder(int obj, float eps, int minPts ) {
		int[] neighbors = getNeighbors( obj, eps );
		processed[obj] = true;
		
		reachDist[obj] = Float.NaN;
		setCoreDistance( obj, neighbors, eps, minPts );
		orderedSet[osNext] = obj;
		osNext++;
		
		if( isDefined(coreDist[obj]) ) {
			orderSeedsUpdate( neighbors, obj );
			
			while( !orderSeeds.isEmpty() ) {
				OsMember cosm = orderSeeds.remove();
				int currentObj = cosm.obj;
				
				neighbors = getNeighbors(currentObj, eps);
				processed[currentObj] = true;
				setCoreDistance(currentObj, neighbors, eps, minPts);
				
				orderedSet[osNext] = currentObj;
				osNext++;
				
				if( isDefined(coreDist[currentObj])) {
					orderSeedsUpdate(neighbors, currentObj);
				}
				
			}
		}
	}


	public static void main(String[] args) {
//		PriorityQueue<OsMember> pq = new PriorityQueue<OsMember>();
//		
//		pq.offer( new OsMember(1, 1.0f ));
//		pq.offer( new OsMember(2, 1.1f ));
//		pq.offer( new OsMember(3, 0.5f ));
//		pq.remove( new OsMember(3, 0.5f ));
//		pq.offer( new OsMember(3, 0.7f ));
//		
//	
//		System.out.printf( "%s\n", (new OsMember(3, 0.5f )).equals(new OsMember(3, 0.7f )));
//		
//		while( !pq.isEmpty() ) {
//			OsMember n = pq.remove();
//			System.out.printf( "%d %f\n", n.obj, n.rDist );
//		}
	
		optics();
		for( int o : orderedSet ) {
			System.out.printf( "os: %d %f\n", o, reachDist[o] );
		}
	}

}
