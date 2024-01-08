//BatchControler AUI070 I701FILE
//BatchControler AUI070 mchtbase.txt
package bank.authbatch.main;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import bank.authbatch.dao.CcaMchtBaseDao;

public class AuthBatchAui070 extends BatchProgBase{

	@Override
	public void startProcess(String[] sP_Parameters) {
		// TODO Auto-generated method stub
		try {
			
			if (sP_Parameters.length<2) {
				writeLog("E", "Please input source file name");
				return;
			}
			
			//sG_CcasInDir = "c:/temp/batch";
			if (sG_CcasInDir.equals("")) {
				writeLog("E", "Please 設定 CCAS_IN_DIR 環境變數!");
				return;
			}
			
			sG_ProgId="Aui070";
			connDb();
			
			String sL_SrcFile = sG_CcasInDir + "/" + sP_Parameters[1];
			System.out.println(sL_SrcFile + "^^");
			processSrcFile(sL_SrcFile);
			
			System.out.println("AuthBatch_Aui070 執行完畢!");
			writeLog("I", "AuthBatch_Aui070 執行完畢!");
			
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
            closeDb();
        }
	}

	private void processSrcFile(String sP_FullPathSrcFile) {
		
		try {

			int nL_Count=0;
			FileInputStream L_FileStream = new FileInputStream(new File(sP_FullPathSrcFile));


			BufferedReader L_BufferReader = new BufferedReader(new InputStreamReader(L_FileStream,"MS950"));

			String sL_StrLine="";
			CcaMchtBaseDao.initPs();
			
			

			
			//Read File Line By Line
			while ((sL_StrLine = L_BufferReader.readLine()) != null)   {

				
				//System.out.println(sL_StrLine);
				CcaMchtBaseDao.parseSrcData(sL_StrLine);
				if (CcaMchtBaseDao.hasData()) {
					CcaMchtBaseDao.updateData();
		 		}
				else {
					CcaMchtBaseDao.insertData(sG_CurDate, sG_ProgId);
				}
				//System.out.println(sL_MccCode+"^^" + sL_CurrentCode);
				
			}
			L_BufferReader.close();

			L_FileStream.close();

			commitDb();
			CcaMchtBaseDao.closePs();
			closeDb();
			/*
			FileInputStream L_FileStream = new FileInputStream(sP_FullPathSrcFile);
			// or using Scaner
			DataInputStream L_DataInStream = new DataInputStream(L_FileStream);
			BufferedReader L_BufferReader = new BufferedReader(new InputStreamReader(L_DataInStream));
			String sL_StrLine="";
			//Read File Line By Line
			while ((sL_StrLine = L_BufferReader.readLine()) != null)   {
			  // split string and call your function
				System.out.println(sL_StrLine);
			}
			L_BufferReader.close();
			L_DataInStream.close();
			L_FileStream.close();
			*/
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public AuthBatchAui070()  throws Exception{
		// TODO Auto-generated constructor stub
	}


}
