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

/**
 * 
 * @author Barry DeZonia
 *
 */
public class Main {
	
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
	public static int main(String[] args) {
	
		if (args.length != 4) {
			
			System.out.println("TRCONV: tractography file converter");
			System.out.println("  Usage:");
			System.out.println("    java -jar trconv.jar --to_tck <file_or_path to input .tck> <file name or path name to output .trk>");
			System.out.println("    java -jar trconv.jar --to_trk <file_or_path to input .trk> <file name or path name to output .tck>");
			
			return 1;
		}
		
		return 0;
	}

}
