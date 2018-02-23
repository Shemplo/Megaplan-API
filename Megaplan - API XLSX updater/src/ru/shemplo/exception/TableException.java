package ru.shemplo.exception;


public class TableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 846957975845779145L;
	
	public TableException (String message) { super (message); }
	
	public TableException (String message, Throwable reason) { super (message, reason); }

}
