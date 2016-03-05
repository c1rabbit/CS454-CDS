package link_analysis;

import java.util.HashMap;
import java.util.Map;

public class LinkAnalysisMain {

	public static void main(String[] args) {
		int interation = 3;
		boolean show_long_output = false;

		try {
			interation = Integer.parseInt(args[0]);
			show_long_output = Boolean.parseBoolean(args[1]);
		} catch (Exception e) {
			System.out.println("default parameters used");
			System.out
					.println("parameters: <int iteration> <Boolean show_long_output");
		} finally {
			LinkAnalysis analyze = new LinkAnalysis(interation,
					show_long_output);
		}

	}
}
