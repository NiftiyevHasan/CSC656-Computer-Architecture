import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Arrays;
import java.math.BigInteger;



public class DynamicPredictor {

    enum STATUS {
        STRONG_NOT_TAKEN,
        WEAK_NOT_TAKEN,
        WEAK_TAKEN,
        STRONG_TAKEN
    }

	
	String filePath;
	int N,M;
    boolean verbose;

	public DynamicPredictor(String filePath, int N, int M, boolean verbose){
		this.filePath = filePath;
		this.N = N;
		this.M = M;
		this.verbose = verbose;
	}

	public String branchProcessor(){
		int numOfCondBranches,numOfForwardBranches,numOfBackwardBranches,
            numOfForwardTakenBranches,numOfBackwardTakenBranches,
            numOfMispredictedBranches,bTBmisses,bTBhits,orderOfType;
        float mispredictionRateForAll = 0;
            numOfCondBranches = numOfForwardBranches = numOfBackwardBranches =
            numOfForwardTakenBranches = numOfBackwardTakenBranches =
            numOfMispredictedBranches = bTBmisses = bTBhits = orderOfType = 0;

		File file = new File(this.filePath);
		int[] predictionBuffer = new int[this.N];
		int[][] branchTargetBuffer = new int[this.M][3];
		Arrays.fill(predictionBuffer, STATUS.WEAK_NOT_TAKEN.ordinal());
		
		try(Scanner scanner = new Scanner(file)) {
			scanner.useDelimiter(",\\s*");
			String[] line;

			while(scanner.hasNext()) {
                StringBuilder verboseOutput = null;
            
                line = scanner.nextLine().split(" ");
                int branchType = Integer.parseInt(line[1]);
                String currentBranchAddress = hexToBin(line[0]);
                long currentBrAddr = Long.parseLong(line[0],16);

                int branchTag = bTBtagAccessor(currentBranchAddress, M);
                int bTBindex = nBitsAccessor(currentBranchAddress, M);
                int predictorIndex = nBitsAccessor(currentBranchAddress, N);
                
                int targetBranchAddress = Integer.parseInt(line[2],16);
                boolean actualBranchTaken = line[3].equals("1");

                if (branchType == 1) {
                    numOfCondBranches++;

                    if (this.verbose) {
	                    verboseOutput = new StringBuilder();
	                    verboseOutput.append(orderOfType + " " +
	                    					Integer.toString(predictorIndex,16) + " ");
	                    orderOfType++;
                	}

                    if (isInRange(predictorIndex, this.N)) {
                        int prediction = predictionBuffer[predictorIndex];
                        if (this.verbose) {
                            verboseOutput.append(prediction);
                            verboseOutput.append(" ");
                        }
                        boolean branchTaken = prediction > STATUS.WEAK_NOT_TAKEN.ordinal();

                        if (branchTaken && isInRange(bTBindex,this.M)){
                            bTBhits++;

                            if (!(branchTargetBuffer[bTBindex][0]==1 && branchTargetBuffer[bTBindex][1] == branchTag)){
                                bTBmisses++;
                            }
                        }
                        if(branchTaken != actualBranchTaken){
                            numOfMispredictedBranches++;
                        }

                        updatePrediction(prediction,predictorIndex,predictionBuffer,actualBranchTaken);
                        if(actualBranchTaken){
                            branchTargetBuffer[bTBindex] = updateBTBbuffer(branchTag,targetBranchAddress);
                        }
                    } else {
                        updatePrediction(STATUS.WEAK_NOT_TAKEN.ordinal(),predictorIndex,predictionBuffer,actualBranchTaken);
                        branchTargetBuffer[bTBindex] = updateBTBbuffer(branchTag,targetBranchAddress);
                    }
                    
                    if (currentBrAddr < targetBranchAddress) {
                        numOfForwardBranches++;
                        if (actualBranchTaken) numOfForwardTakenBranches++;
                    }
                    else if (actualBranchTaken) {
                        numOfBackwardTakenBranches++;
                    }
                    
                    if(this.verbose){
                        verboseOutput.append(predictionBuffer[predictorIndex] + " " +
                                            Integer.toString(bTBindex,16) + " " +
                                            Integer.toString(branchTag,16) + " " +
                                            bTBhits + " " +
                                            bTBmisses);
                        System.out.println(verboseOutput.toString());
                    }
                }
            }
		} catch(Exception ex){
			return "File not found !";
		}


		String output = outputConstructor(numOfCondBranches,numOfForwardBranches,numOfBackwardBranches,
            numOfForwardTakenBranches,numOfBackwardTakenBranches,numOfMispredictedBranches,bTBmisses,bTBhits);

		return output;
	}

	public String outputConstructor(int numCB,int numFB, int numBB,int numFTB,int numBTB,int numMB,int bTBmisses,int bTBhits){
		return "\nNumber of branches = " + numCB +
    	"\nNumber of forward branches = " + numFB +
    	"\nthe number of forward taken branches = " + numFTB +
    	"\nNumber of backward branches = " + (numCB - numFB) +
    	"\nNumber of backward taken branches = " + numBTB +
		"\nNumber of mispredictions = " + numMB + " " + ((float) numMB / (float) numCB) +
		"\nNumber of BTB misses = " + bTBmisses + " " + ((float) bTBmisses/(float) bTBhits);
	}

	public void updatePrediction(int prediction, int predictorIndex, int[] predictionBuffer,boolean actualBranchTaken){
        if(actualBranchTaken && prediction< STATUS.STRONG_TAKEN.ordinal() ){
            predictionBuffer[predictorIndex] = ++prediction;
        } else if (!actualBranchTaken && prediction> STATUS.STRONG_NOT_TAKEN.ordinal()){
            predictionBuffer[predictorIndex] = --prediction;
        }
    }

	private static int log2(int number){
		return (int) (Math.log(number)/Math.log(2));
	}

	private static int bTBtagAccessor(String binAddress, int m){
		int size = binAddress.length();
		String tag = binAddress.substring(0, size - log2(m) - 2);
        return Integer.parseInt(tag, 2);
	}

	private static int nBitsAccessor(String binAddress, int n){
		int size = binAddress.length();
		String predNBit = binAddress.substring(size-log2(n)-2, size-2);
        return Integer.parseInt(predNBit,2);
	}

	private static String hexToBin(String s) {
  		return new BigInteger(s, 16).toString(2);
	}

	private static boolean isInRange(int index, int limit){
		return (index>=0 && index<=limit);
	}

	private static int[] updateBTBbuffer( int branchTag, int targetBranchAddress) {
        int[] bufferComponents = {1,branchTag,targetBranchAddress};
        return bufferComponents;  
    }
}







