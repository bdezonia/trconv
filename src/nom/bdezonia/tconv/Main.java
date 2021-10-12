/*
	MIT License
	
	Copyright (c) 2021 Barry DeZonia
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
 */
package nom.bdezonia.tconv;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Barry DeZonia
 *
 */
public class Main {
	
	private static enum FileType {
		Unknown,
		TRK,
		TCK
	}
	
	static class FileMapping {
		String full_path_input_name;
		String full_path_output_name;
	}
	
	private static List<FileMapping> mappings = new ArrayList<>();
	
	/**
	 * Example command lines:
	 * 
	 *   trconv --to-trk /home/fred/data/inputs/input.tck /data/outputs/output 
	 *   trconv --to-trk /home/fred/data/inputs /data/outputs/output 
	 *   trconv --to-trk input.tck . 
	 *   trconv --to-trk input.tck output.trk 
	 * 
	 *   trconv --to-tck /home/fred/data/inputs/input.trk /data/outputs/output 
	 *   trconv --to-tck /home/fred/data/inputs /data/outputs/output 
	 *   trconv --to-tck input.trk . 
	 *   trconv --to-tck input.trk output.tck 
	 */
	public static void main(String[] args) {
	
		if (args.length != 3) {
			
			System.out.println("TRCONV: tractography file converter (pre-alpha version)");
			System.out.println("  Compiled with Java 11");
			System.out.println("  Download the latest trconv.jar from https://github.com/bdezonia/trconv/releases");
			usage();
			
			System.exit(1);
		}
		
		FileType readType = getReadType(args[0]);

		if (readType == FileType.Unknown) {
			
			System.out.println("TRCONV: unsupported output type flag: " + args[0]);
			System.exit(2);
		}

		if (isExistingFile(args[1]) && isExistingPath(args[2])) {
			
			String outName = args[2];
			
			if (!outName.endsWith(File.separator)) {
				outName = outName + File.separator;
			}
			
			// out name = base in name minus extension

			String fname = Path.of(args[1]).getFileName().toString();

			int extPos = fname.lastIndexOf('.');
			
			fname = fname.substring(0, extPos);
			
			if (readType == FileType.TCK)
				outName = outName + fname + ".trk";
			else
				outName = outName + fname + ".tck";

			FileMapping mapping = new FileMapping();
			mapping.full_path_input_name = args[1];
			mapping.full_path_output_name = outName;
			mappings.add(mapping);
		}
		else if (isExistingPath(args[1]) && isExistingPath(args[2])) {

	        try {
	        	File f = new File(args[1]);

	            FilenameFilter filter = new FilenameFilter() {
	            	
	            	@Override
	                public boolean accept(File f, String name) {

	            		// We want to find only .c files
	                    return name.endsWith(".trk") || name.endsWith(".tck"); 
	                }
	            };

	            // Note that this time we are using a File class as an array,
	            // instead of String
	            File[] files = f.listFiles(filter);

	            // Get the names of the files by using the .getName() method
	            for (int i = 0; i < files.length; i++) {
	            	String inFileName = files[i].getName();
	    			String outName = args[2];
	    			
	    			if (!outName.endsWith(File.separator)) {
	    				outName = outName + File.separator;
	    			}
	    			if (readType == FileType.TCK)
	    				outName = outName + ".tck";
	    			else
	    				outName = outName + ".trk";

	    			FileMapping mapping = new FileMapping();
	    			mapping.full_path_input_name = inFileName;
	    			mapping.full_path_output_name = outName;
	    			mappings.add(mapping);
	            }
	        } catch (Exception e) {
	        	System.out.print("Exception "+e);
	            System.exit(3);
	        }
		}
		else if (isExistingFile(args[1]) && isLegalFileName(args[2])) {
			
			FileMapping mapping = new FileMapping();
			mapping.full_path_input_name = args[1];
			mapping.full_path_output_name = args[2];
			mappings.add(mapping);
		}
		else {

			System.out.println("TRCONV: inconsistent inputs: must follow one of the following conventions");
			usage();
			System.exit(4);
		}
		
		for (FileMapping mapping : mappings) {
			
			System.out.println("Converting "+mapping.full_path_input_name+" to "+mapping.full_path_output_name);
			
			if (readType == FileType.TCK) {
				
				TckToTrk.convert(mapping.full_path_input_name, mapping.full_path_output_name);
				
			}
			else if (readType == FileType.TRK) {

				TrkToTck.convert(mapping.full_path_input_name, mapping.full_path_output_name);
			}
			else {
				
				System.out.println("Error: unknown file type: "+mapping.full_path_input_name);
				System.exit(5);
			}
		}
		
		System.exit(0);
	}

	private static void usage() {
		System.out.println("  Usage:");
		System.out.println("    java -jar trconv.jar --to_tck <file_or_path name to input .tck> <file_or_path name to output .trk>");
		System.out.println("    java -jar trconv.jar --to_trk <file_or_path name to input .trk> <file_or_path name to output .tck>");
	}
	
	private static FileType getReadType(String flag) {

		if (flag.equalsIgnoreCase("--to-tck")) {
			
			return FileType.TRK;
		}
		else if (flag.equalsIgnoreCase("--to-trk")) {
			
			return FileType.TCK;
		}
		else {
			
			return FileType.Unknown;
		}
	}
	
	private static boolean isLegalFileName(String name) {
		
		try {

			Path path = Path.of(name);
			
			Files.createFile(path);
			
			Files.delete(path);
			
			return true;
		}
		catch (FileAlreadyExistsException e) {

			return true;
		}
		catch (Exception e) {
			
			System.out.println("Illegal filename specification: "+e);
			return false;
		}
	}
	
	private static boolean isExistingFile(String name) {
		
		Path path = Path.of(name);
		
		return Files.exists(path);
		
	}
	
	private static boolean isExistingPath(String name) {
		
		Path path = Path.of(name);
		
		return Files.isDirectory(path);
	}
}
