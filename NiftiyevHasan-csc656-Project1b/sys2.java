/*
	Author: Hasan Niftiyev
	To compile: javac sys2.java DynamicPredictor.java
	To run: 	java sys2 -filepath- N M [-v]
*/

public class sys2{

		public static void main(String[] args) {


			boolean verbose = false;
			int M = 0;
			int N = 0;
			if(args.length>1){
				M = Integer.parseInt(args[2]);
				N = Integer.parseInt(args[1]);

				if (args.length == 4 && (args[3].equals("-v") || args[3].equals("[-v]")))
					verbose = true;

			} else {
				System.out.println("ERROR: check your arguments!");
				return;
			}

			DynamicPredictor predictor = new DynamicPredictor(args[0],N ,M , verbose);
			String output = predictor.branchProcessor();
			System.out.println(output);
	}
}

