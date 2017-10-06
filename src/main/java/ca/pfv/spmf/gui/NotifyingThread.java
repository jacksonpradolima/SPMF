package ca.pfv.spmf.gui;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * This class is a Java thread that will notify another thead when it has terminated
 * its task. It is used in the SPMF GUI by the user interface to get notified when
 * an algorithm that was launched by the user has terminated.
 * 
 * This class is implemented using the "listener" design pattern.
 * 
 * This code is adapted from public code from StackOverflow
 * http://stackoverflow.com/questions/702415/how-to-know-if-other-threads-have-finished
 * 
 * @author Philippe Fournier-Viger
 */
public abstract class NotifyingThread extends Thread {
	/** The listeners **/
	private final Set<ThreadCompleteListener> listeners = new CopyOnWriteArraySet<ThreadCompleteListener>();

	/**
	 * Method to add a listener for the completion of this thread
	 * @param listener the listener
	 */
	public final void addListener(final ThreadCompleteListener listener) {
		listeners.add(listener);
	}

	/**
	 * Method to remove a listener.
	 */
	public final void removeListener(final ThreadCompleteListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Method to notify the listeners that this thread has completed its task
	 */
	private final void notifyListeners(boolean succeed) {
		// for each listener
		for (ThreadCompleteListener listener : listeners) {
			// notify the listener that this thread has terminated
			listener.notifyOfThreadComplete(this, succeed);
		}
	}

	/**
	 * Method to run the thread
	 */
	@Override
	public final void run() {
		boolean succeed = false;
		try {
			// We run the task
			doRun();
			// If it terminates properly, we set succeed to true
			succeed = true;
		}catch(Exception e){
			e.printStackTrace();
			// if some error happens we catch and throw the exception
			throw new RuntimeException(e + System.lineSeparator()+ System.lineSeparator());
		}
		finally {
			// when the thread terminates, we will notify the listeners
			// about whether the thread succeeded or failed
			notifyListeners(succeed);
		}
	}

	/**
	 * This method should be implemented by subclasses of this class.
	 * In this method subclasses should put the code for the task to be done by
	 * the thread
	 * @throws Exception throw an exception if some error occurs.
	 */
	public abstract void doRun() throws Exception;
}