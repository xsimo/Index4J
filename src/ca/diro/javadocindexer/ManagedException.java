package ca.diro.javadocindexer;

/**
 * empty exception class to differentiate from all other potential exception that could be thrown
 * our program will know it's ok when thrown
 * 
 * @author simon
 *
 */
public class ManagedException extends Exception {

	public ManagedException(String string) {
		super(string);
	}

}
