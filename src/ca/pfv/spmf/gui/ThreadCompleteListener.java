package ca.pfv.spmf.gui;


/**
 * This interface should be implemented by classes that want to listen to a NotifyingThread.
 * A NotifyingThread is a thread that notify its listeners when it terminates.
 * It is used in the GUI of SPMF so that the GUI get notified when an algorithm terminates.
 * 
 * This class follows the "listener" design pattern.
 * 
 * This code is adapted from public code from StackOverflow
 * http://stackoverflow.com/questions/702415/how-to-know-if-other-threads-have-finished
 * 
 * @author Philippe Fournier-Viger
 */
public interface ThreadCompleteListener {
    void notifyOfThreadComplete(final Thread thread, boolean succeed);
}