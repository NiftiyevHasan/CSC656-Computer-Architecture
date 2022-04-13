public class sys1{

	public static void main(String[] args) {


		boolean verbose = false;
		int ic1 = 0;
		int ic2 = 100000; 
		int N = 0;


		if(args.length < 2){
			System.out.println("\nERROR: check your arguments!\n");
			return;
		} 
		N = Integer.parseInt(args[1]);

		if(args.length > 2 && (args[2].equals("-v") || args[2].equals("[-v]"))){
			if(args.length < 5){
				System.out.println("\nERROR: Incorrect number of arguments for Verbose Mode ! \nFollow: [-v ic1 ic2] structure !\n");
				return;
			} 
			verbose = true;
			ic1 = Integer.parseInt(args[3]);
			ic2 = Integer.parseInt(args[4]);

		}

		CacheSimulator directMapped = new CacheSimulator(args[0], N , verbose, ic1, ic2);
		String output = directMapped.cacheProcessor();
		System.out.println(output);
	}
}

