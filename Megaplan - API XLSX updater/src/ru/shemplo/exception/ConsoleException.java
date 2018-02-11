package ru.shemplo.exception;


public class ConsoleException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4538313011694554077L;

	public ConsoleException (String message) { super (message); }
	
	public ConsoleException (String message, Throwable reason) { super (message, reason); }
	
}
