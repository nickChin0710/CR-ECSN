package com;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class UnMaskLogger {
	
	private Base64.Encoder encoder = Base64.getEncoder();
	private Base64.Decoder decoder = Base64.getDecoder();

	protected UnMaskLogger() {
	}
	
	
	/**
	 * 將mask的log文件轉換爲正常log文件
	 * @param inputFile
	 * @param outputFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void unmaskLogFile(File inputFile, File outputFile) throws FileNotFoundException, IOException {
		try (FileReader in = new FileReader(inputFile);
				BufferedReader reader = new BufferedReader(in);
				FileWriter out = new FileWriter(outputFile);
				BufferedWriter writer = new BufferedWriter(out)) {
			String str = reader.readLine();
			while (str != null) {
				str = unmask(str);
				writer.write(str);
				
				str = reader.readLine();
				if (str != null) {
					writer.newLine();
				}
			}
		}
	}
	
	/**
	 * unmask log data
	 * @param message
	 * @return
	 */
	private String unmask(String message) {
		int loc = message.indexOf(";;;~~~");
		if (loc < 0) {
			return message;
		}
		int loc2 = message.indexOf("~~~;;;", loc + 6);
		if (loc2 < 0) {
			return message;
		}
		String s1 = message.substring(0, loc);
		String str = message.substring(loc + 6, loc2);
		try {
			str = new String(decoder.decode(str), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		String s2 = message.substring(loc2 + 6);
		
		return s1 + str + s2;
	}
	
	public static final void main(String... args) {
		UnMaskLogger logger = new UnMaskLogger();
//		String msg = "<caption><font color=red><B>MainControl.java TarokoException : ";
//		msg = logger.mask(msg).toString();
//		System.out.println(msg);
//		msg = "20203343 -34--34  : " + msg;
//		msg = logger.unmask(msg);
//		System.out.println(msg);
		if (args.length == 0) {
			System.out.println("Usage : java UnMaskLogger logfile");
		}
		String inputfile = args[0];
		String outputfile = inputfile + ".log";
		try {
			logger.unmaskLogFile(new File(inputfile), new File(outputfile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
