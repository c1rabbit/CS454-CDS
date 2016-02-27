package link_analysis;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LinkAnalysis {
	private int threads = 1;
	private List<Link> link;
	private Map<String, Double> tempRank;
	private List<Map> tempRankList;

	public LinkAnalysis() {
		tempRankList = new ArrayList<Map>();

		// set file db from
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader("sample_db.json"));
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject json = (JSONObject) obj;
		//System.out.println(json.toJSONString());

	}

	public void run() {

		// get total number of documents
		int docCount = 0;

	}

}
