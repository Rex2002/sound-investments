package dataAnalyzer;

import java.util.List;
import dataRepo.Price;

public class FlagFormationAnalyzer {
	public static int MIN_FORMATION_LENGTH = 2;
	public static int MAX_FORMATION_LENGTH = 60;

	public static boolean[] analyze(List<Price> priceList) {
		boolean[] out = new boolean[priceList.size()];
		out[0] = false;
		double[] blurredValues = Blur.averageBlur(priceList);

		int formationLength = 0;
    	boolean isRising = false;
    	boolean isFalling = false;
    	boolean findFlag = false;
    	boolean isFlag = false;

		for (int i = 1; i < priceList.size(); i++) {
			double value = blurredValues[i];
			if (value > blurredValues[i - 1] && !isFalling) {
				findFlag = true;
				isRising = true;
				formationLength++;
			} else if (value < blurredValues[i - 1] && findFlag) {
				isRising = false;
				isFalling = true;
				formationLength++;
				isFlag = true;
			}

			if (value > blurredValues[i - 1] && isFlag && formationLength >= MIN_FORMATION_LENGTH && formationLength <= MAX_FORMATION_LENGTH) {
				for (int k = 0; k < formationLength; k++) {
					out[i - k] = true;
				}
				formationLength = 0;
				isFlag = false;
			} else {
				out[i] = false;
			}
		}

		return out;
	}

}
