/*
	Author: Hasan Niftiyev
	To compile: javac sys1.java Predictor.java
	To run: 	java sys1 -filepath-  [-v]
*/


public class sys1{

		public static void main(String[] args) {
			if(args.length < 1){
				System.out.println("ERROR: check your arguments!");
				return;
			}

			boolean verbose = false;
			if(args.length > 1 && (args[1].equals("-v") || args[1].equals("[-v]"))){
				verbose = true;

			}
			Predictor predictor = new Predictor(args[0], verbose);
			String output = predictor.branchProcessor();
			System.out.println(output);
	}
}

