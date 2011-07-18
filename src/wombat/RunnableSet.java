package wombat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunnableSet {
    Map<Integer, MyRunnable> currentlyRunning = new HashMap<Integer, MyRunnable>();

    void add(MyRunnable r) {
	synchronized (currentlyRunning) {
	    currentlyRunning.put(r.getSerial(), r);
	}
    }

    void remove(MyRunnable r) {
	synchronized (currentlyRunning) {
	    if (currentlyRunning.remove(r.getSerial()) == null) {
		System.out.printf("strange: remove called by non member\n");
	    }
	}
    }

    void killAll() {
	// synchronized (currentlyRunning) {
	// for( MyRunnable r: currentlyRunning.values()) {
	// System.out.printf( "killing %d\n", r.getSerial() );
	// r.killProcess();
	// }
	// }
    }

    /**
     * Get a list of the currently running MyRunner objects
     * 
     * @return
     */
    List<MyRunnable> getRunning() {
	ArrayList<MyRunnable> l = new ArrayList<MyRunnable>();

	synchronized (currentlyRunning) {
	    for (MyRunnable r : currentlyRunning.values()) {
		l.add(r);
	    }
	}

	return l;
    }
}