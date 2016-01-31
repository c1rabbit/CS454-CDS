package hw0;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.simple.JSONObject;

public class Crawler {
	private List<File> paths;
	private Set<File> visited;
	private String db_path = Paths.get(".").toAbsolutePath().normalize()
			.toString()
			+ "/data/db_record.txt";

	public static void main(String[] args) throws IOException {
		File directory = new File("C:/Users/ASPIRE/Desktop/test");
		Crawler crawler = new Crawler(directory);
		crawler.run();
	}

	public Crawler(File directory) {

		paths = new LinkedList<File>();
		paths.add(directory);
		System.out.println("Root Directory:\t" + directory.toString());
		visited = new HashSet<File>();

		// clear db file
		try {
			FileWriter fw = new FileWriter(db_path, false);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// PrintWriter pw = new PrintWriter(Paths.get(".").toAbsolutePath()
		// .normalize().toString()
		// + "/src/data/db_record.txt");
		// pw.close();

	}

	public void run() throws IOException {
		File file = paths.get(0);
		// breadth first search
		while (!paths.isEmpty()) {
			file = paths.get(0);// reset pointer
			if (file.isFile() && !visited.contains(file)) {
				parse(file);
				visited.add(file);
				System.out.println("parsed:\t" + file.toPath());
			} else if (file.isDirectory() && !visited.contains(file)) {
				for (final File f : file.listFiles()) {
					if (!visited.contains(f)) {
						if (f.isDirectory()) {
							paths.add(f);
						} else {
							paths.add(0, f);
						}

						System.out.println("queued:\t" + f.toPath());
					}

				}
			}
			paths.remove(0);
			visited.add(file);
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

			json.put("content", handler.toString());
			// System.out.print(".");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writeJSON(json);
		}
	}

	public void writeJSON(JSONObject json) {
		try {

			FileWriter fw = new FileWriter(db_path, true); // the true will
															// append the new
															// data
			fw.write(json.toString() + "\n");// appends the string to the file
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}
}
