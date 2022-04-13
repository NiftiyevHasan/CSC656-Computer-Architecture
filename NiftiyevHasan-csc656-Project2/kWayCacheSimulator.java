import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.math.BigInteger;
import java.util.HashMap;

public class kWayCacheSimulator {

	final int DEFAULT_BLOCK_LENGTH = 16;
	final int DIRECT_MAPPED_CACHE = 1;
	final int DEFAULT_VALID_BIT = 0;
	final int MISS_PENALTY = 80;

	final int VALID_BIT = 0;
	final int DIRTY_BIT = 1;
	final int TAG = 2;
	final int LRU = 3;
	final int DATA = 4;

	String filePath;
	int cacheSize;
	int setAssociativity;
	boolean verbose;
	int lowerLimit;
	int upperLimit;

	public kWayCacheSimulator(String filePath, int cacheSize, int setAssociativity, boolean verbose, int lowerLimit,
		int upperLimit) {
		this.filePath = filePath;
		this.cacheSize = cacheSize;
		this.setAssociativity = setAssociativity;
		this.verbose = verbose;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;

	}

	public String cacheProcessor() {

		int loads, stores, rmiss, wmiss, dirtyRmiss, dirtyWmiss, bytesRead, bytesWritten, readTime, writeTime,
		lineNumber;

		loads = stores = rmiss = wmiss = dirtyRmiss = dirtyWmiss = bytesRead = bytesWritten = readTime = writeTime = lineNumber = 0;

		String caseNumber = null;

		File file = new File(this.filePath);

		int cacheSetSize = cacheBlockAmountCalculator(cacheSize, DEFAULT_BLOCK_LENGTH, setAssociativity);
		int cacheBuffSize = cacheSetSize * setAssociativity;
		int[][] cacheBuffer = new int[cacheBuffSize][5];
		HashMap<String, ArrayList<String>> memoryMap = new HashMap<String, ArrayList<String>>();

		try (Scanner scanner = new Scanner(file)) {
			scanner.useDelimiter("\\t+");
			String[] line;

			while (scanner.hasNext()) {
				StringBuilder verboseOutput = null;
				line = scanner.nextLine().split("[ ]+");

				// String instructionAddress =
				// hexToBin(line[0].substring(0,line[0].length()-1));
				// // Long instructAddress = Long.parseLong(instructionAddress,16);

				boolean isRead = line[1].equals("R");
				String memAddress = hexChopper(line[2]);
				// int numOfBytes = Integer.parseInt(line[3]);
				String memData = "12345";// hexChopper(line[4]);

				// Put this in class file
				int offset = offsetAccessor(memAddress, DEFAULT_BLOCK_LENGTH);
				int tag = tagAccessor(memAddress, cacheSetSize, DEFAULT_BLOCK_LENGTH);
				int index = indexAccessor(memAddress, cacheSetSize, DEFAULT_BLOCK_LENGTH);

				if (this.verbose && inRange(lineNumber)) {
					verboseOutput = new StringBuilder();
					verboseOutput.append(
						lineNumber + " " + Integer.toString(index, 16) + " " + Integer.toString(tag, 16) + " ");
				}

				// Store in memory
				if (memoryMap.containsKey(memAddress)) {
					ArrayList<String> temp = memoryMap.get(memAddress);
					temp.add(memData);
					memoryMap.put(memAddress, temp);
				} else {
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(memData);
					memoryMap.put(memAddress, temp);
				}

				if (searchCacheForHit(cacheBuffer, index, tag) >= 0) {
					int blockID = searchCacheForHit(cacheBuffer, index, tag);
					if (isRead) {
						loads++;
						readTime++;
					} else {
						cacheBuffer[index * setAssociativity + blockID][DIRTY_BIT] = 1;
						stores++;
						writeTime++;
					}
					int lastUsed = cacheBuffer[index * setAssociativity + blockID][LRU];

					cacheBuffer[index * setAssociativity + blockID][LRU] = lineNumber;
					caseNumber = "1";

					if (this.verbose && inRange(lineNumber)) {
						verboseOutput.append(cacheBuffer[index * setAssociativity + blockID][VALID_BIT] + " " + blockID
							+ " " + Integer.toString(lastUsed, 10) + " "
							+ Integer.toString(cacheBuffer[index * setAssociativity + blockID][TAG], 16) + " "
							+ cacheBuffer[index * setAssociativity + blockID][DIRTY_BIT] + " " + 1 + " "
							+ caseNumber);

					}

				} else if (searchCacheForEmptySet(cacheBuffer, index, tag) < 0) {
					int blockID = searchCacheForDirtyMiss(cacheBuffer, index, tag);
					boolean isDirty = false;
					if (cacheBuffer[index * setAssociativity + blockID][DIRTY_BIT] == 1) {

						ArrayList<String> temp2 = new ArrayList<String>();
						temp2.add(Integer.toString(cacheBuffer[index * setAssociativity + blockID][DATA]));
						memoryMap.put(memAddress, temp2);
						isDirty = true;
					}

					ArrayList<String> temp = memoryMap.get(memAddress);

					if (isRead) {
						updateCache(cacheBuffer, temp.get(0), index * setAssociativity + blockID, tag, isRead,
							lineNumber);
						loads++;
						rmiss++;
						if (isDirty) {
							dirtyRmiss++;
							readTime += MISS_PENALTY;
							bytesWritten += 16;// line.numOfBytes;
						}
						readTime += (1 + MISS_PENALTY);

					} else {
						updateCache(cacheBuffer, temp.get(0), index * setAssociativity + blockID, tag, isRead,
							lineNumber);
						stores++;
						wmiss++;
						if (isDirty) {
							dirtyWmiss++;
							writeTime += MISS_PENALTY;
							bytesWritten += 16;// line.numOfBytes;
						}
						writeTime += (1 + MISS_PENALTY);
					}

					bytesRead += 16;// line.numOfBytes;

					caseNumber = "2b";
					temp.remove(0);
					memoryMap.put(memAddress, temp);

					if (this.verbose && inRange(lineNumber)) {
						verboseOutput.append(cacheBuffer[index * setAssociativity + blockID][VALID_BIT] + " " + blockID
							+ " " + Integer.toString(cacheBuffer[index * setAssociativity + blockID][LRU], 10) + " "
							+ Integer.toString(cacheBuffer[index * setAssociativity + blockID][TAG], 16) + " "
							+ cacheBuffer[index * setAssociativity + blockID][DIRTY_BIT] + " " + 1 + " "
							+ caseNumber);

					}

				} else {// clean cache miss
					int blockID = searchCacheForEmptySet(cacheBuffer, index, tag);
					if (this.verbose && inRange(lineNumber)) {
						verboseOutput.append(cacheBuffer[index * setAssociativity + blockID][VALID_BIT]);
						verboseOutput.append(" " + 0 + " " + 0 + " " + 0 + " ");
					}

					ArrayList<String> temp = memoryMap.get(memAddress);
					if (isRead) {
						updateCache(cacheBuffer, temp.get(0), index * setAssociativity + blockID, tag, isRead,
							lineNumber);
						rmiss++;
						loads++;
						readTime += (1 + MISS_PENALTY);
					} else {
						updateCache(cacheBuffer, temp.get(0), index * setAssociativity + blockID, tag, isRead,
							lineNumber);
						wmiss++;
						stores++;
						writeTime += (1 + MISS_PENALTY);
					}
					bytesRead += 16;// numOfBytes;

					caseNumber = "2a";
					temp.remove(0);
					memoryMap.put(memAddress, temp);

					if (this.verbose && inRange(lineNumber)) {
						verboseOutput.append(caseNumber);
					}

				}

				if (this.verbose && inRange(lineNumber)) {

					System.out.println(verboseOutput.toString());
				}

				lineNumber++;

			}

		} catch (Exception ex) {

			return ex.toString();
		}

		String output = outputConstructor(cacheSize, loads, stores, rmiss, wmiss, dirtyRmiss, dirtyWmiss, bytesRead,
			bytesWritten, readTime, writeTime);

		return output;
	}

	public String outputConstructor(int cacheSize, int loads, int stores, int rmiss, int wmiss, int dirtyRmiss,
		int dirtyWmiss, int bytesRead, int bytesWritten, int readTime, int writeTime) {
		return "\n" + setAssociativity + "-way, writeback, size = " + cacheSize + "KB" + "\nloads " + loads + " stores "
		+ stores + " total " + (loads + stores) + "\nrmiss " + rmiss + " wmiss " + wmiss + " total "
		+ (rmiss + wmiss) + "\ndirty rmiss " + dirtyRmiss + " dirty wmiss " + dirtyWmiss + "\nbytes read "
		+ bytesRead + " bytes written " + bytesWritten + "\nread time " + readTime + " write time " + writeTime
		+ "\ntotal time " + (readTime + writeTime) + "\nmiss rate "
		+ ((float) (rmiss + wmiss) / (loads + stores));
	}

	public void updateCache(int[][] cacheBuffer, String memData, int index, int tag, boolean dirty, int lineNumber) {
		cacheBuffer[index][DATA] = Integer.parseInt(memData);
		cacheBuffer[index][VALID_BIT] = 1;
		cacheBuffer[index][TAG] = tag;
		cacheBuffer[index][LRU] = lineNumber;
		cacheBuffer[index][DIRTY_BIT] = (dirty) ? 0 : 1;
	}

	private int searchCacheForHit(int[][] cache, int index, int tag) {
		int hitIndex = -1;
		for (int i = 0; i < setAssociativity; i++) {
			if (cache[index * setAssociativity + i][VALID_BIT] == 1) {
				if (cache[index * setAssociativity + i][TAG] == tag) {
					hitIndex = i;
				}
			}
		}
		return hitIndex;
	}

// THIS ONE RETURNS THE D in HANDOUT
	private int searchCacheForEmptySet(int[][] cache, int index, int tag) {

		int missIndex = -1;
		for (int i = 0; i < setAssociativity; i++) {
			if (cache[index * setAssociativity + i][VALID_BIT] == 0) {
				missIndex = i;
				return missIndex;
			}
		}
		return missIndex;
	}

// returns index of LRU block
	private int searchCacheForDirtyMiss(int[][] cache, int index, int tag) {
		int missIndex = 0;
		int minLRU = 0;
		int currentLRU = 0;
		for (int i = 0; i < setAssociativity; i++) {
			if (cache[index * setAssociativity + i][TAG] != tag) {

				minLRU = cache[index * setAssociativity + missIndex][LRU];
				currentLRU = cache[index * setAssociativity + i][LRU];

				if (currentLRU < minLRU) {
					missIndex = i;
				}

			}

		}
		return missIndex;

	}

	private boolean inRange(int lineNumber) {
		return (lineNumber >= lowerLimit) && (lineNumber <= upperLimit);
	}

	private static int cacheBlockAmountCalculator(int cacheSize, int blockLength, int kWay) {
		return (cacheSize * 1024) / (blockLength * kWay);
	}

	private static int log2(int number) {
		return (int) (Math.log(number) / Math.log(2));
	}

	private static String hexToBin(String s) {
		return new BigInteger(s, 16).toString(2);
	}

	private static int offsetAccessor(String address, int blockLength) {

		String binAddress = hexToBin(address);
		int size = binAddress.length();
		String offset = binAddress.substring(size - log2(blockLength), size);
		return Integer.parseInt(offset, 2);
	}

	private static int indexAccessor(String address, int blockSize, int blockLength) {

		String binAddress = hexToBin(address);
		int size = binAddress.length();
		String index = binAddress.substring(size - log2(blockSize) - log2(blockLength), size - log2(blockLength));
		return Integer.parseInt(index, 2);
	}

	private static int tagAccessor(String address, int blockSize, int blockLength) {

		String binAddress = hexToBin(address);
		int size = binAddress.length();
		String tag = binAddress.substring(0, size - log2(blockLength) - log2(blockSize));
		return Integer.parseInt(tag, 2);

	}

	private static String hexChopper(String hexAddress) {
		return (hexAddress.length() > 2 && hexAddress != null) ? hexAddress.substring(2) : "0";
	}
}
