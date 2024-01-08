package bank.authbatch.main;


import java.io.Writer;
import java.sql.ResultSet;

import bank.authbatch.dao.AuthTxLogDao;

public class AuthBatchAuo030 extends BatchProgBase{

	@Override
	public void startProcess(String[] sP_Parameters) {
		// TODO Auto-generated method stub
		
		//sG_CcasInDir = "c:/temp/batch";
		if (sG_CcasOutDir.equals("")) {
			writeLog("E", "Please 設定 CCAS_OUT_DIR 環境變數!");
			return;
		}

		sG_ProgId="Auo030";
		
		
		try {
		    connDb();
			
			AuthTxLogDao.initPs();
			genFile();
			
			
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
            closeDb();
        }
	}

	
	private void genFile() {
		
		try {
			String sL_CurDate = HpeUtil.getCurDateStr("");
			String sL_CurTime = HpeUtil.getCurTimeStr();
			String sL_LogFile = sG_CcasOutDir + "/" + "ccasBatch030." + sL_CurDate;
			String sL_TmpFileName = "voicfile." +sL_CurDate ;
			String sL_TxtFile = sG_CcasOutDir +"O" + "702" + "0171117" + sL_CurDate.substring(6, 8);

			Writer L_Writer = FileUtil.getFileWriter(sL_TxtFile, "MS950");
			if (null==L_Writer) {
				//System.out.println("could not init file writer! File name :" + sL_TxtFile);
				return;
			}

			int nL_TotalDataLen = 0, nL_TotalRecCount=0;
			//down, gen File header
			String sL_FileHeader = "017" + "012" + HpeUtil.fillCharOnRight("AUTHLOG", 8, " ") + sL_CurDate.substring(2, 8) + sL_CurTime + HpeUtil.fillCharOnLeft("2006", 6, "0") 
					+ HpeUtil.fillCharOnLeft("", 6, "0")+ HpeUtil.fillCharOnLeft("", 6, "0")+ HpeUtil.fillCharOnLeft("", 6, "0")
					+ HpeUtil.fillCharOnLeft("", 30, " ");
			//Sample header data => "017012AUTHLOG 180309003001002006000000000000000000                              "
			FileUtil.writeData(L_Writer, sL_FileHeader, true);
			//up, gen File header
			
			ResultSet L_AuthTxLogRs = AuthTxLogDao.getData4Auo030();
			
			int nL_NtAmt=0;
			String sL_Tmp="", sL_CardNo="", sL_TxDate="", sL_TxTime="", sL_AuthNo="";
			String sL_MchtNo="", sL_TransType="", sL_NtAmt="";
			String sL_AuthStatusCode="", sL_EffDateEnd="", sL_WholeData="";
			while(L_AuthTxLogRs.next()) {
				sL_WholeData="";
				//sample data => "   01712155466010000054986180308132332610156AP0005168500           BK022202     "
				//sample data(Howard): => "   0171215^5466010000054986^180308^132332^610156^AP^0005168500^          ^ ^BK02^2202     "
				sL_MchtNo = L_AuthTxLogRs.getString("MCHT_NO").trim();
				sL_MchtNo = HpeUtil.fillCharOnLeft(sL_MchtNo, 10, " ");
				nL_TotalDataLen +=10;
				sL_WholeData = sL_WholeData + sL_MchtNo; 
						
				sL_CardNo = L_AuthTxLogRs.getString("CARD_NO").trim();
				sL_CardNo = HpeUtil.fillCharOnLeft(sL_CardNo, 16, " ");
				nL_TotalDataLen +=16;
				sL_WholeData = sL_WholeData + sL_CardNo;
				/*
				if (sL_CardNo.equals("5433838303335607"))
					System.out.println(sL_CardNo);
				*/
				sL_TxDate = L_AuthTxLogRs.getString("TX_DATE").trim();
				sL_TxDate = sL_TxDate.substring(2, 8);
				sL_TxDate = HpeUtil.fillCharOnLeft(sL_TxDate, 6, " ");
				nL_TotalDataLen +=6;
				sL_WholeData = sL_WholeData + sL_TxDate;
				
				sL_TxTime = L_AuthTxLogRs.getString("TX_TIME").trim();
				sL_TxTime = HpeUtil.fillCharOnLeft(sL_TxTime, 6, " ");
				nL_TotalDataLen +=6;
				sL_WholeData = sL_WholeData + sL_TxTime;
				
				
				sL_AuthNo = L_AuthTxLogRs.getString("AUTH_NO").trim();
				sL_AuthNo = HpeUtil.fillCharOnRight(sL_AuthNo, 6, " ");
				
				if (sL_AuthNo.substring(0,6).equals("******"))
					sL_AuthNo = HpeUtil.fillCharOnLeft("", 6, " ");
				sL_AuthNo = sL_AuthNo.substring(0, 6);
				nL_TotalDataLen +=6;
				sL_WholeData = sL_WholeData + sL_AuthNo;
				
				sL_TransType = L_AuthTxLogRs.getString("TRANS_TYPE").trim();
				sL_TransType = HpeUtil.fillCharOnLeft(sL_TransType, 2, " ");
				nL_TotalDataLen +=2;
				sL_WholeData = sL_WholeData + sL_TransType;
				
				nL_NtAmt = L_AuthTxLogRs.getInt("NT_AMT")*100;
				sL_NtAmt = HpeUtil.fillCharOnLeft(""+nL_NtAmt, 10, "0");
				nL_TotalDataLen +=10;
				sL_WholeData = sL_WholeData + sL_NtAmt;
				
				sL_Tmp = HpeUtil.fillCharOnLeft("", 10, " ");
				nL_TotalDataLen +=10;
				sL_WholeData = sL_WholeData + sL_Tmp;
				
				sL_AuthStatusCode = L_AuthTxLogRs.getString("AUTH_STATUS_CODE").trim();
				if ("C".equals(sL_AuthStatusCode.substring(0,1)))
					sL_Tmp = "R";
				else
					sL_Tmp =" ";
				nL_TotalDataLen +=1;
				sL_WholeData = sL_WholeData + sL_Tmp;
				
				nL_TotalDataLen +=4;
				sL_WholeData = sL_WholeData + "BK02";
				
				sL_EffDateEnd = L_AuthTxLogRs.getString("EFF_DATE_END").trim();
				sL_EffDateEnd = sL_EffDateEnd.substring(2, 6);
				nL_TotalDataLen +=4;
				sL_WholeData = sL_WholeData + sL_EffDateEnd;
				
				sL_WholeData = sL_WholeData + "     ";
				nL_TotalDataLen +=5;
				nL_TotalRecCount++;
				
				FileUtil.writeData(L_Writer, sL_WholeData,true);
			}
			
			//down, gen File tailer
			String sL_FileTailer = "017" + "012" + HpeUtil.fillCharOnRight("AUTHLOG", 8, " ") + sL_CurDate.substring(2, 8) + sL_CurTime + HpeUtil.fillCharOnLeft("2006", 6, "0") 
					+ HpeUtil.fillCharOnLeft(""+nL_TotalDataLen, 6, "0")+ HpeUtil.fillCharOnLeft("", 6, "0")+ HpeUtil.fillCharOnLeft("" + nL_TotalRecCount, 6, "0")
					+ HpeUtil.fillCharOnLeft("", 30, " ");
			//Sample tailer data => "017012AUTHLOG 180309003002002006001920000000000024                              "
			FileUtil.writeData(L_Writer, sL_FileTailer, true);
			//up, gen File tailer

			FileUtil.closeFileWriter(L_Writer);
			L_AuthTxLogRs.close();
			
			commitDb();
			AuthTxLogDao.closePs();
			closeDb();
			String sL_Tmp2 = "AUO030處理完成，總資料筆數是" + nL_TotalRecCount;
			writeLog("I",sL_Tmp2);
			//System.out.println(sL_Tmp2);
			
		} catch (Exception e) {
			// TODO: handle exception
			//System.out.println("Exception=>" + e.getMessage());
		}
		
	}
	public AuthBatchAuo030() throws Exception{
		// TODO Auto-generated constructor stub
	}

}
