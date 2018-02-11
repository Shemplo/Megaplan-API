package ru.shemplo.exception;


public class UserProfileException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6147747097003091186L;
	
	public UserProfileException (String message) { super (message); }
	
	public UserProfileException (String message, Throwable reason) { super (message, reason); }

}
