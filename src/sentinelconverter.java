import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class sentinelconverter {
	public static void main(String[] args) throws IOException {
		File dir;
		if(args.length == 0) {
			dir = new File(".");
		}
		else {
			dir = new File(args[0]);
		}

		FileInputStream fis;
		BufferedReader in = null;
 
		FileWriter fstream;
		BufferedWriter out = null;
		
		String aLine = null;
		String pageName;
		String parentClassName;
		String elementName;
		String elementType;
		String locatorType;
		String locatorValue;
		String extension = "";
		String fileName;
		boolean fileExists = false;
	    Pattern pageNamePattern = Pattern.compile("public\\s+class\\s+(.*?)\\s+extends\\s+(.*?)\\s+\\{.*");
	    Pattern elementDetailsPattern = Pattern.compile("\\s*public\\s+(.*?)\\s+(.*?)\\(\\).*?new\\s+(.*?)\\((.*?),\\s*([\"'].*['\"]).*");
		
        File[] filesInFolder = dir.listFiles();
	    
		for (File fin : filesInFolder) {
			fileName = fin.getName();
			int i = fileName.lastIndexOf('.');
			if (i > 0) {
			    extension = fileName.substring(i+1);
			} else {
				extension = "";
			}
			
			if ("java".equalsIgnoreCase(extension)) {
				System.out.format("Processing %s%n",fin.toString());
				fis = new FileInputStream(fin);
				in = new BufferedReader(new InputStreamReader(fis));
			    while ((aLine = in.readLine()) != null) {
					if (aLine.contains("extends")) {
					//if a line contains extends
						Matcher m = pageNamePattern.matcher(aLine);
						m.matches();
						pageName = m.group(1);
						parentClassName = m.group(2);
						String dest = dir.getCanonicalPath() + File.separator + pageName + ".yml";
						fileExists = new File(dest).exists();
						fstream = new FileWriter(dest, true);
						out = new BufferedWriter(fstream);
						if (fileExists) {
							out.newLine();
						}
						if (!"Page".equalsIgnoreCase(parentClassName)) {
							out.write(String.format("include: %s%n", parentClassName));
						}
						out.write(String.format("elements:"));
						//if the line has class in it, the next word is the page name
						//And we check to see if it has a parent object
					}
					else if (aLine.contains("public")) {
						try {
							//otherwise the next word is the elementType
							//the next word, minus the () is the elemnt name
							//after the next use of elementType the next word minus ( and downcased is the start of a line
							//the value in quotes is the value for the above line after a colon
							Matcher m = elementDetailsPattern.matcher(aLine);
							m.matches();
							elementType = m.group(1);
							elementName = m.group(2);
							locatorType = m.group(4);
							locatorValue = m.group(5);
							out.write(String.format("%n  %s:", elementName));
							out.write(String.format("%n    elementType: %s", elementType));
							out.write(String.format("%n    %s: %s", locatorType.toLowerCase(), locatorValue));
						} catch (java.lang.IllegalStateException e) {
							//Swallow this line because it isn't properly formatted (likely commented out
						}
					}
				}
			    in.close();
				out.close();
				fin.delete(); //We don't want to keep the old page object around.
			}
		}
	}
}
