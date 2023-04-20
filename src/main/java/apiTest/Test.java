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
			List<T> l = parser.parse("resources/test.json", sb.toString(), x -> {
				List<JsonPrimitive<?>> arr = (List<JsonPrimitive<?>>) x.el;
				return arr.stream().map(y -> {
					HashMap<String, JsonPrimitive<?>> obj = (HashMap<String, JsonPrimitive<?>>) y.el;
					Number start = (Number) obj.get("open").el;
					Number end = (Number) obj.get("close").el;
					return new T(start.doubleValue(), end.doubleValue());
				}).toList();
			});
			for (Integer i = 0; i < l.size(); i++) {
				System.out.println(i.toString() + ": " + l.get(i).toString());
			}
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
}
