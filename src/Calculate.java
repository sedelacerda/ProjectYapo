import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class Calculate {

	private static double quartileN(ArrayList<Integer> data, int n) {

		double[] dataArray = new double[data.size()];

		for (Integer i = 0; i < data.size(); i++) {
			dataArray[i] = data.get(i);
		}
		Arrays.sort(dataArray);

		if (0 <= n && n <= 4) {
			if (n == 0) {
				return dataArray[0];
			} else {
				DescriptiveStatistics da = new DescriptiveStatistics(dataArray);
				return da.getPercentile(25 * n);
			}
		}

		else {
			return -1;
		}
	}

	public static double quartile0(ArrayList<Integer> data) {

		return quartileN(data, 0);
	}

	public static double quartile1(ArrayList<Integer> data) {

		return quartileN(data, 1);
	}

	public static double quartile2(ArrayList<Integer> data) {

		return quartileN(data, 2);
	}

	public static double quartile3(ArrayList<Integer> data) {

		return quartileN(data, 3);
	}

	public static double quartile4(ArrayList<Integer> data) {

		return quartileN(data, 4);
	}

}