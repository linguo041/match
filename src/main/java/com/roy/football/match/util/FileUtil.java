package com.roy.football.match.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {
	/**
	 * write the content to fileName
	 * @param content
	 * @param fileName
	 */
	public static void writeToFile(String content, String fileName) {
		try {
			File file = new File(fileName);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			FileWriter fileWritter = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.write(content);
			bufferWritter.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		writeToFile("hi", "c:/test.txt");
	}
}
