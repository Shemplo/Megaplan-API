package ru.shemplo.exception;


public class GUIException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2768503153296805829L;
	
	public GUIException (String message) { super (message); }
	
	public GUIException (String message, Throwable reason) { super (message, reason); }

}
