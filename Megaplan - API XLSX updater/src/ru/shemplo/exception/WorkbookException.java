package ru.shemplo.exception;


public class WorkbookException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 846957975845779145L;
	
	public WorkbookException (String message) { super (message); }
	
	public WorkbookException (String message, Throwable reason) { super (message, reason); }

}
