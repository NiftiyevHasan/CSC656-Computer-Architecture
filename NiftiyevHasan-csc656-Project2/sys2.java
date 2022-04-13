public class sys2{

	public static void main(String[] args) {

		boolean verbose = false;
		int kWay = 1;
		int ic1 = 0;
		int ic2 = 100000; 
		int N = 0;

		if(args.length < 3){
			System.out.println("\nERROR: check your arguments!\n");
			return;
		} 

		N = Integer.parseInt(args[1]);
		kWay = Integer.parseInt(args[2]);
		

		if(args.length > 3 && (args[3].equals("-v") || args[3].equals("[-v]"))){
			if(args.length < 6){
				System.out.println("\nERROR: Incorrect number of arguments for Verbose Mode ! \nFollow: [-v ic1 ic2] structure !\n");
				return;
			} 
			verbose = true;
			ic1 = Integer.parseInt(args[4]);
			ic2 = Integer.parseInt(args[5]);
		}

		kWayCacheSimulator setAssociative = new kWayCacheSimulator(args[0], N , kWay, verbose, ic1, ic2);
		String output = setAssociative.cacheProcessor();
		System.out.println(output);
	}
}

