package ca.diro.javadocindexer;

/**
 * empty exception class to differentiate from all other non managed exception 
 * that could be thrown so that the program will know it's ok when thrown
 * 
 * @author aramesim
 *
 */
public class ManagedException extends Exception {

	public ManagedException(String string) {
		super(string);
	}

}
