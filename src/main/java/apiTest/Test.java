package apiTest;

import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner; // Import the Scanner class to read text files

import json.*;

public class Test {
	public static void main(String[] args) {
		try {
			File myObj = new File("resources/test.json");
			StringBuilder sb = new StringBuilder(256);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				sb.append(data);
				sb.append('\n');
			}
			myReader.close();

			Parser parser = new Parser();
			List<T> l = parser.parseList("resources/test.json", sb.toString(), x -> {
				HashMap<String, JsonPrimitive<?>> obj = x.asMap();
				Double start = obj.get("open").asDouble();
				Double end = obj.get("close").asDouble();
				return new T(start, end);
			});
			for (Integer i = 0; i < l.size(); i++) {
				System.out.println(i.toString() + ": " + l.get(i).toString());
			}
		} catch (

		FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
}
