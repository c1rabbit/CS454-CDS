package hw0;

import java.io.File;
import java.io.IOException;

import jdk.internal.org.xml.sax.SAXException;

import org.apache.tika.exception.TikaException;

public class Main {
	public static void main(String[] args) throws IOException, SAXException,
			TikaException {

		File directory = new File("C:/Users/ASPIRE/Desktop/test");
		Crawler crawler = new Crawler();
		crawler.run(directory);
		// System.out.println(crawler.parse(new File(
		// "C:/Users/ASPIRE/Desktop/text.docx")));

	}
}
