package hw0;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.simple.JSONObject;

public class Crawler {
	public Crawler() {
		try {
			PrintWriter pw = new PrintWriter(Paths.get(".").toAbsolutePath()
					.normalize().toString()
					+ "/src/data/db_record.txt");
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void run(File directory) throws IOException {
		for (final File file : directory.listFiles()) {
			if (file.isDirectory()) {
				run(file);
			} else {
				parse(file);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void parse(File file) throws IOException {
		AutoDetectParser parser = new AutoDetectParser();
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		JSONObject json = new JSONObject();

		json.put("filename", file.getName());
		json.put("directory", file.getAbsolutePath());
		// System.out.println(file.toString());

		try (InputStream stream = new FileInputStream(file)) {
			parser.parse(stream, handler, metadata);
			// json.append("Content-Type", metadata).get("Content-Type");
			// System.out.println(TikaCoreProperties.METADATA_DATE);
			// System.out.println(metadata.get(TikaCoreProperties.METADATA_DATE
			// .getName()));
			// json.append(TikaCoreProperties.TITLE.getName(),
			// metadata.get(TikaCoreProperties.TITLE));
			// String[] metadataNames = metadata.names();

			// for (String name : metadataNames) {
			// json.append(name, metadata.get(name));
			// System.out.println(name + ": " + metadata.get(name));
			// }
			json.put("content", handler.toString());
			System.out.print(".");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writeJSON(json);
		}
	}

	public void writeJSON(JSONObject json) {
		try {

			String filename = Paths.get(".").toAbsolutePath().normalize()
					.toString()
					+ "/src/data/db_record.txt";
			FileWriter fw = new FileWriter(filename, true); // the true will
															// append the new
															// data
			fw.write(json.toString() + "\n");// appends the string to the file
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}
}
