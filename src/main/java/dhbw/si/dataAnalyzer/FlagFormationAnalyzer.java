package dhbw.si.dataAnalyzer;

import java.util.List;
import dhbw.si.dataRepo.Price;

public class FlagFormationAnalyzer {
	public static final int MIN_FORMATION_LENGTH = 2;
	public static final int MAX_FORMATION_LENGTH = 60;

	public static boolean[] analyze(List<Price> priceList) {
		boolean[] out = new boolean[priceList.size()];
		out[0] = false;
		double[] blurredValues = Blur.averageBlur(priceList);
		int formationLength=0;

		boolean isFalling = false;
    	boolean findFlag = false;
    	boolean isFlag = false;

		for (int i = 1; i < priceList.size(); i++) {
			double value = blurredValues[i];
			if (value > blurredValues[i - 1] && !isFalling) {
				findFlag = true;
				formationLength++;
			} else if (value < blurredValues[i - 1] && findFlag) {
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
