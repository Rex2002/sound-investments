package dataAnalyzer;

import java.util.List;
import dataRepo.Price;
import dataAnalyzer.Blurr;

public class FlagFormationAnalyzer {
	public static boolean[] analyze(List<Price> priceList) {
		boolean[] out = new boolean[priceList.size()];
		out[0] = false;
		double[] blurredValues = new double[out.length];
		blurredValues = gaussianBlur(priceList);


		int formationLength = 0;
    	boolean isRising = false;
    	boolean isFalling = false;
    	boolean findFlag = false;
    	boolean isFlag = false;

		for (int i = 1; i < priceList.size(); i++) {
			double values = blurredValues[i];
			if (values > blurredValues[i - 1] && !isFalling) {
				findFlag = true;
				isRising = true;
				formationLength++;
			  } else if (values < blurredValues[i - 1] && findFlag) {
				isRising = false;
				isFalling = true;
				formationLength++;
				isFlag = true;
			  }

			  if (values > blurredValues[i - 1] && isFlag && formationLength >= 2 && formationLength <= 60) {
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
