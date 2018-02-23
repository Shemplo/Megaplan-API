package ru.shemplo.megaplan.updater;

import java.util.LinkedList;
import java.util.Queue;

public class AsyncTaskExecutor {

	private static final Queue <Runnable> TASKS;
	private static final Thread [] POOL;
	
	static {
		TASKS = new LinkedList <> ();
		POOL = new Thread [4];
		
		for (int i = 0; i < POOL.length; i ++) {
			POOL [i] = new Thread (() -> {
				while (true) {
					Runnable task;
					synchronized (TASKS) {
						try {
							while (TASKS.isEmpty ()) { TASKS.wait (); }
						} catch (InterruptedException e) {
							return; // Stopping thread
						}
						
						task = TASKS.poll ();
					}
					
					try {
						task.run ();
					} catch (Exception e) { /* To prevent stopping of executor */ }
				}
			},"Async-executor-" + i);
			POOL [i].start ();
		}
	}
	
	public static void execute (Runnable task) {
		if (task == null) { return; }
		
		synchronized (TASKS) {
			TASKS.add (task);
			TASKS.notify ();
		}
	}
	
	public static void stop () {
		for (int i = 0; i < POOL.length; i ++) {
			POOL [i].interrupt ();
		}
	}

}
