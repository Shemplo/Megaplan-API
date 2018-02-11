package ru.shemplo.exception;


public class RequestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3621825854764962272L;

	public RequestException (String message) { super (message); }
	
	public RequestException (String message, Throwable reason) { super (message, reason); }
	
}
