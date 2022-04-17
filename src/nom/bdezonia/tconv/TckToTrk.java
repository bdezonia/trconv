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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Barry DeZonia
 *
 */
public class TckToTrk {

	private enum DataType{ 
		Unknown, 	// unknown data type
		Float32, 	// 32-bit floating-point (native endian-ness)
		Float32BE, 	// 32-bit floating-point (big-endian)
		Float32LE, 	// 32-bit floating-point (little-endian)
		Float64,	// 64-bit (double) floating-point (native endian-ness)
		Float64BE, 	// 64-bit (double) floating-point (big-endian)
		Float64LE; 	// 64-bit (double) floating-point (little-endian)
	}

	private static int numBytes(DataType type) {
		switch (type) {
			case Float32:
			case Float32BE:
			case Float32LE:
				return 4;
			case Float64:
			case Float64BE:
			case Float64LE:
				return 8;
			default:
				return 0;
		}
	}
	
	public static void convert(String inFileName, String outFileName) {
		
		File inFile = new File(inFileName);
		
		File outFile = new File(outFileName);
		
		FileInputStream inStream = null;
		
		PushbackInputStream pbStream = null;
		
		try {
		
			inStream = new FileInputStream(inFile);
			
			pbStream = new PushbackInputStream(inStream);
		
		} catch (FileNotFoundException e) {
		
			System.err.println("EXITING: FILE NOT FOUND: " + inFileName);
			
			System.exit(1);  // return error condition
		}
		
		try {
			DataType dataType = readHeader(pbStream, inStream);

			// position file point past possible garbage bytes due to data alignment issues
			
			long pos = inStream.getChannel().position();
			long remaining = inFile.length() - pos;
			long cruft = remaining % (3 * numBytes(dataType));
			inStream.getChannel().position(pos+cruft);
			
			BufferedInputStream bufInStr = new BufferedInputStream(inStream);
			
			DataInputStream dataInStream = new DataInputStream(bufInStr);
			
			FileOutputStream outStream = new FileOutputStream(outFile);
			
			BufferedOutputStream bufOutStr = new BufferedOutputStream(outStream);
			
			DataOutputStream dataOutStream = new DataOutputStream(bufOutStr);
			
			writeOutputHeader(dataOutStream);
			
			long numTracks = 0;
			long numPoints = 0;
			
			float x = getBigEndianFloat(dataInStream, dataType);
			float y = getBigEndianFloat(dataInStream, dataType);
			float z = getBigEndianFloat(dataInStream, dataType);
	
			while( ! (Float.isInfinite(x) && Float.isInfinite(y) && Float.isInfinite(z)) ) {
				
				ArrayList<RealPoint> pts = new ArrayList<>();
			
				long thisTrackSize = 0;
				
				while ( ! (Float.isNaN(x) && Float.isNaN(y) && Float.isNaN(z)) ) {

					RealPoint pt = new RealPoint();
					pt.x = x;
					pt.y = y;
					pt.z = z;
					
					pts.add(pt);
		
					thisTrackSize++;
					
					x = getBigEndianFloat(dataInStream, dataType);
					y = getBigEndianFloat(dataInStream, dataType);
					z = getBigEndianFloat(dataInStream, dataType);
				}
				
				if (thisTrackSize > 0) {
					
					// write a track
					
					dataOutStream.writeInt(pts.size());
					for (int i = 0; i < pts.size(); i++) {
						RealPoint pt = pts.get(i);
						dataOutStream.writeFloat(pt.x);
						dataOutStream.writeFloat(pt.y);
						dataOutStream.writeFloat(pt.z);
					}
		
					numPoints += thisTrackSize;
					numTracks++;
					
				}
				
				// finished a track
				
				x = getBigEndianFloat(dataInStream, dataType);
				y = getBigEndianFloat(dataInStream, dataType);
				z = getBigEndianFloat(dataInStream, dataType);
				
			}
			
			// finished the file
		
			dataInStream.close();
			
			dataOutStream.close();
			
			BigDecimal avgTrackSize = BigDecimal.ZERO;
			
			if (numTracks > 0) {
				
				MathContext context = new MathContext(2);
			
				avgTrackSize = BigDecimal.valueOf(numPoints).divide(BigDecimal.valueOf(numTracks), context);
			}
			
			System.out.println("totalTracks    = " + numTracks);
			System.out.println("totalPoints    = " + numPoints);
			System.out.println("avg track size = " + avgTrackSize.doubleValue());
		}
		catch (IOException e) {
			
			System.out.println("exception " + e);
		}
	}
	
	private static DataType readHeader(PushbackInputStream pbStream, FileInputStream fstream) throws IOException {
		
		DataType dataType = DataType.Unknown;
		
		Pattern p = null; 
		
		boolean done = false;
		
		while (!done) {
			
			// is it end of input?
		
			String line = readLine(pbStream);
		
			line = line.trim();

			if (line.equalsIgnoreCase("end")) {
				done = true;
			}
			else {
		
				// is it dataType?
				
				p = Pattern.compile("(.+)\\:(.+)");
				Matcher m = p.matcher(line);
				
				if (m.matches()) {
					if (m.groupCount() == 2) {
						String key = m.group(1);
						if (key.trim().equalsIgnoreCase("datatype")) {
							String value = m.group(2).trim();
							
							if (value.equalsIgnoreCase("float32"))
								dataType = DataType.Float32;
							else if (value.equalsIgnoreCase("float32be"))
								dataType = DataType.Float32BE;
							else if (value.equalsIgnoreCase("float32le"))
								dataType = DataType.Float32LE;
							else if (value.equalsIgnoreCase("float64"))
								dataType = DataType.Float64;
							else if (value.equalsIgnoreCase("float64be"))
								dataType = DataType.Float64BE;
							else if (value.equalsIgnoreCase("float64le"))
								dataType = DataType.Float64LE;
							
							//System.out.println("data type set to "+dataType);
						}
					}
				}
			}
		}

		return dataType;
	}
	
	private static float getBigEndianFloat(DataInputStream data, DataType dataType) throws IOException {
		
		long b0, b1, b2, b3, b4, b5, b6, b7;
		
		switch (dataType) {
			
			case Float32:
			case Float32BE:
			
				return data.readFloat();
			
			case Float32LE:
			
				b0 = data.readByte() & 0xff;
				b1 = data.readByte() & 0xff;
				b2 = data.readByte() & 0xff;
				b3 = data.readByte() & 0xff;
	
				int intBits = (int) ((b3 << 24) | (b2 << 16) | (b1 << 8) | (b0 << 0));
				
				return Float.intBitsToFloat(intBits);
	
			case Float64:
			case Float64BE:
			
				return (float) data.readDouble();
			
			case Float64LE:

				b0 = data.readByte() & 0xff;
				b1 = data.readByte() & 0xff;
				b2 = data.readByte() & 0xff;
				b3 = data.readByte() & 0xff;
				b4 = data.readByte() & 0xff;
				b5 = data.readByte() & 0xff;
				b6 = data.readByte() & 0xff;
				b7 = data.readByte() & 0xff;
				
				long longBits = (b7 << 56) | (b6 << 48) | (b5 << 40) | (b4 << 32) | (b3 << 24) | (b2 << 16) | (b1 << 8) | (b0 << 0);
				
				return (float) Double.longBitsToDouble(longBits);
	
			
			default:
				return Float.POSITIVE_INFINITY;
		}
	}

	private static String readLine(PushbackInputStream pbStream) throws IOException {

		StringBuilder sb = new StringBuilder();
		boolean done = false;
		while (!done) {
			int b = pbStream.read();
			if (b == 0x0A) {  // line feed by self
				done = true;
			}
			else if (b == 0x0D) {  // carriage return by self
				int b2 = pbStream.read();
				if (b2 != 0x0A) {  // or also followed by a line feed
					pbStream.unread(b2);
				}
				done = true;
			}
			else {
				sb.append((char) b);
			}
		}
		return sb.toString();
	}
	
	private static void writeOutputHeader(DataOutputStream oStr) throws IOException {

		// write id string
		
		oStr.writeByte((byte) 'T');
		oStr.writeByte((byte) 'R');
		oStr.writeByte((byte) 'A');
		oStr.writeByte((byte) 'C');
		oStr.writeByte((byte) 'K');
		oStr.writeByte((byte) 0);
		
		// write dimensions
		
		// TODO: what should the dimensions be?
		
		oStr.writeShort(0);
		oStr.writeShort(0);
		oStr.writeShort(0);
		
		// write the voxel sizes
		oStr.writeFloat(1);
		oStr.writeFloat(1);
		oStr.writeFloat(1);
		
		// write the origin: always 0 in spec
		oStr.writeFloat(0);
		oStr.writeFloat(0);
		oStr.writeFloat(0);

		// write scalar count
		oStr.writeShort(0);
		
		// write scalar names
		for (int i = 0; i < 200; i++) {
			oStr.writeByte(0);
		}
		
		// write property count
		oStr.writeShort(0);
		
		// write property names
		for (int i = 0; i < 200; i++) {
			oStr.writeByte(0);
		}
		
		// write transformation matrix
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				oStr.writeFloat(0);
			}			
		}
		
		// fill reserved space
		for (int i = 0; i < 444; i++) {
			oStr.writeByte(0);
		}
		
		// fill voxel order  TODO what would be best?
		for (int i = 0; i < 4; i++) {
			oStr.writeByte(0);
		}
		
		// fill pad2
		for (int i = 0; i < 4; i++) {
			oStr.writeByte(0);
		}
		
		// fill orientation TODO what is best here?
		for (int i = 0; i < 6; i++) {
			oStr.writeFloat(0);
		}

		// fill pad1
		for (int i = 0; i < 2; i++) {
			oStr.writeByte(0);
		}
		
		// fill internal flags
		for (int i = 0; i < 6; i++) {
			oStr.writeByte(0);
		}

		// fill number of tracks in this file: 0 is allowed
		oStr.writeInt(0);

		// fill version number
		oStr.writeInt(2);

		// fill header size
		oStr.writeInt(1000);
	}

}
