package banker;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Main class for creating input file and running the program
 * @author Yutong Zhou
 *
 */
public class main {

	public static void main(String[] args) throws FileNotFoundException {

		File in = new File(args[0]);
		Scanner input = new Scanner(in);
		Optimistic.Optimistic(input);
		
		Scanner input2 = new Scanner(in);
		Banker.Banker(input2);
	}

}
