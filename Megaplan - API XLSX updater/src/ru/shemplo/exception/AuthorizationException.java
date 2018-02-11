package ru.shemplo.exception;


public class AuthorizationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 913245759239607641L;

	public AuthorizationException (String message) { super (message); }
	
	public AuthorizationException (String message, Throwable reason) { super (message, reason); }
	
}
