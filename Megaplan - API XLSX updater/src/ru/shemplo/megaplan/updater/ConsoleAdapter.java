package ru.shemplo.megaplan.updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ru.shemplo.exception.ConsoleException;

public class ConsoleAdapter {

	private static final BufferedReader BR;
	static {
		Reader r = new InputStreamReader (System.in, 
							StandardCharsets.UTF_8);
		BR = new BufferedReader (r);
	}
	
	public static String getVariantedAnswer (String question, int attempts, String... possibleAnswers)
		throws ConsoleException {
		
		if (possibleAnswers == null || possibleAnswers.length == 0) {
			String message = "(Console) no possible answers";
			throw new ConsoleException (message);
		}
		
		try {
			while (attempts > 0) {
				if (question != null && question.length () > 0) {
					System.out.println (question + ": ");
				}
				
				String line = BR.readLine ().toLowerCase ();
				StringTokenizer st = new StringTokenizer (line);
				
				String answer = st.nextToken (); int index = -1;
				for (int i = 0; i < possibleAnswers.length; i ++) {
					if (answer.equals (possibleAnswers [i])) {
						index = i; break;
					}
				}
				
				if (index != -1) {
					return possibleAnswers [index];
				} else {
					System.out.print ("Select one of possible answers: ");
					System.out.println (Arrays.toString (possibleAnswers));
				}
				
				attempts--;
			}
			
			String message = "(Console) attempts expired";
			throw new ConsoleException (message);
		} catch (IOException ioe) {
			String message = "(Console) failed to read answer";
			throw new ConsoleException (message, ioe);
		}
	}
	
	public static String getFormattedAnswer (String question, int attempts, String regexp) 
		throws ConsoleException {
		
		Pattern pattern = null;
		
		try {
			pattern = Pattern.compile (regexp, Pattern.UNICODE_CHARACTER_CLASS);
		} catch (PatternSyntaxException pse) {
			String message = "(REGEXP) bad regular expression";
			throw new ConsoleException (message, pse);
		}
		
		try {
			while (attempts > 0) {
				if (question != null && question.length () > 0) {
					System.out.println (question + ": ");
				}
				
				String line = BR.readLine ().trim ();
				Matcher matcher = pattern.matcher (line);
				if (matcher.find ()) { return line; } 
				
				System.out.println ("Wrong answer format");
				attempts--;
			}
			
			String message = "(Console) attempts expired";
			throw new ConsoleException (message);
		} catch (IOException ioe) {
			String message = "(Console) failed to read answer";
			throw new ConsoleException (message, ioe);
		}
	}
	
}
