import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Predictor {

    String filePath;
    boolean verbose;
    
    public Predictor(String filePath, boolean verbose) {
        this.filePath = filePath;
        this.verbose = verbose;
    }
    
    public String branchProcessor(){

        int numOfCondBranches,numOfForwardBranches,numOfBackwardBranches,
            numOfForwardTakenBranches,numOfBackwardTakenBranches,
            numOfMispredictedBranches;
        float mispredictionRateForAll = 0;
            numOfCondBranches = numOfForwardBranches = numOfBackwardBranches =
            numOfForwardTakenBranches = numOfBackwardTakenBranches =
            numOfMispredictedBranches = 0;

        File file = new File(this.filePath);
        
        try (Scanner scanner = new Scanner(file)) {
            scanner.useDelimiter("//s+");
            String[] line;
            
            while(scanner.hasNext()){
                line = scanner.nextLine().split(" ");
                
                
                final long currentBranchAddress = Long.parseLong(line[0],16);
                final int branchType = Integer.parseInt(line[1]);
                final long targetBranchAddress = Long.parseLong(line[2],16);
                final int branchTaken = Integer.parseInt(line[3]);
                
                if (branchType == 1){
                    if (this.verbose) {
                    System.out.println(line[0] + " " + line[1] + " " + line[2] + " " + line[3]);
                }
                
                    numOfCondBranches++;
                    
                    if (currentBranchAddress < targetBranchAddress){
                        numOfForwardBranches++;
                        
                        if (branchTaken == 1 ){
                            numOfForwardTakenBranches++;
                            numOfMispredictedBranches++;
                        }
                        
                    }
                    else{
                        numOfBackwardBranches++;
                        
                        if (branchTaken == 1 ){
                            numOfBackwardTakenBranches++;
                        } else{
                            numOfMispredictedBranches++;
                        }
                    }
                }
            }
            mispredictionRateForAll = (float) numOfMispredictedBranches / numOfCondBranches;
        } catch(FileNotFoundException ex){
            return "File not found !";
        }

        String output = outputConstructor(numOfCondBranches,numOfForwardBranches,numOfBackwardBranches,
            numOfForwardTakenBranches,numOfBackwardTakenBranches,
            numOfMispredictedBranches,mispredictionRateForAll);

        return output;
	}

	public String outputConstructor(int numCB,int numFB, int numBB,int numFTB,int numBTB,int numMB, float mispredRate){
		return "\nNumber of branches = " + numCB +
        "\nNumber of forward branches = " + numFB +
        "\nNumber of forward taken branches = " + numFTB +
        "\nNumber of backward branches = " + numBB +
        "\nNumber of backward taken branches = " + numBTB +
        "\nNumber of mispredictions = " + numMB + " " + mispredRate;
	}
}

