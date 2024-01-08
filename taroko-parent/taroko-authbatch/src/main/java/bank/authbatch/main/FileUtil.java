package bank.authbatch.main;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class FileUtil {

	public FileUtil() {
		// TODO Auto-generated constructor stub
	}

	
	public static boolean closeFileWriter(Writer P_Writer) {
		boolean bL_Result = true;
		
		try {
			P_Writer.flush();
			P_Writer.close();
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}
	public static boolean writeData(Writer P_Writer, String sP_Data, boolean bL_AppendNewLineFlag) {
		boolean bL_Result = true;
		
		try {
			P_Writer.write(sP_Data);
			if (bL_AppendNewLineFlag)
				P_Writer.write("\n");
			
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}
	public static Writer getFileWriter(String sP_FullPathFileName, String sP_CharsetName) {
		//sP_CharsetName => MS950. utf-8
		
		Writer L_Writer = null;
		try {
			L_Writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(sP_FullPathFileName), sP_CharsetName));
		} catch (Exception e) {
			// TODO: handle exception
			L_Writer = null;
			
		}
		
		return L_Writer;
		
	}
	private void writeTestFile() {
		String sL_FileName= "c:/test/abc.txt";
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              //new FileOutputStream(sL_FileName), "utf-8"))) {
				new FileOutputStream(sL_FileName), "MS950"))) {
			writer.write("something" + "\n");
			
			writer.write("這試測試喔" + "\n");
			writer.write("abcd" + "\n");
		}
		catch (Exception e ) {
			//System.out.println("failed---"+ e.getMessage());
			return;
		}
		System.out.println("completed");

	}
}
