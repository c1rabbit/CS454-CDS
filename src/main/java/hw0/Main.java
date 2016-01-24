package hw0;

import java.io.File;
import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException,
			org.xml.sax.SAXException {

		File directory = new File("C:/Users/ASPIRE/Desktop/test");
		Crawler crawler = new Crawler();
		crawler.run(directory);
		// System.out.println(crawler.parse(new File(
		// "C:/Users/ASPIRE/Desktop/text.docx")));

	}
}
