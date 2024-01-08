/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/02/20  V1.00.00   Justin        program initial                        *
 ******************************************************************************/

package Icu;

import java.util.ArrayList;

import com.AccessDAO;
import com.CommCrd;
import com.CommFTP;
import com.CommRoutine;

import Dxc.Util.SecurityUtil;


public class IcuD999 extends AccessDAO {
	private static final int COUNT_EACH_BATCH = 10000;
	CommCrd comc = new CommCrd();
	private final String progname = "ID_P_SEQNO compare list  111/02/20  V1.00.02";
	ArrayList<IcuD999Data> list = new ArrayList<IcuD999Data>();
	String outputFolderPath = "/media/icu/error/";
	String outputBackupFolderPath = "/media/icu/backup/";
	String fileName = "idPSeqnoCompareList";

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
						
			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			readCrdCard();
			readDbcCard();
			
			if (list.isEmpty() == false) {
				String outputFileName = fileName + "." + sysDate + sysTime + ".txt";
				String outputIdnoFilePath = comc.getECSHOME() + outputFolderPath + outputFileName;
				outputIdnoFilePath = SecurityUtil.verifyPath(outputIdnoFilePath);
				int outputIdnoFileIndex = openOutputText(outputIdnoFilePath);
				writeTextFile(outputIdnoFileIndex, getTitile());
				for (int j = 0; j < list.size(); j++) {
					writeTextFile(outputIdnoFileIndex, list.get(j).convertToString());
				}
				closeOutputText(outputIdnoFileIndex);
				
				CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
				CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
			    procFTP(outputFileName, commFTP, comr);
			    renameFile1(outputFileName);
				
			}

			// ==============================================
			// 固定要做的
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}
	
	void renameFile1(String fileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + outputFolderPath + fileName;
		String tmpstr2 = comc.getECSHOME() + outputBackupFolderPath + fileName;
		
		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
	}
	
	void procFTP(String outputFileName, CommFTP commFTP, CommRoutine comr) throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;

	      showLogMessage("I", "", "mput " + outputFileName + " 開始傳送....");
	      int errCode = commFTP.ftplogName("NCR2EMP", "mput " + outputFileName);
	      
	      if (errCode != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + outputFileName + " 資料"+" errcode:"+errCode);       
	      }
	  }



	private String getTitile() {
		StringBuilder sb = new StringBuilder();
		sb.append("IS_DEBIT").append(IcuD999Data.SEPA)
		  .append("ID_P_SEQNO").append(IcuD999Data.SEPA)
		  .append("CARD_NO").append(IcuD999Data.SEPA)
		  .append("CORP_NO").append(IcuD999Data.SEPA)
		  .append("CORP_P_SEQNO").append(IcuD999Data.SEPA)
		  .append("GROUP_CODE").append(IcuD999Data.SEPA)
		  .append("MOD_PGM").append(IcuD999Data.SEPA)
		  .append("MOD_TIME").append(IcuD999Data.SEPA)
		  .append("\r\n");
		return sb.toString();
	}



	private void readDbcCard() throws Exception {
		showLogMessage("I", "", "開始處理DBC_CARD");
		
		sqlCmd = " SELECT ID_P_SEQNO, "
				   + "CARD_NO, "
				   + "CORP_NO, "
				   + "CORP_P_SEQNO, "
				   + "GROUP_CODE, "
				   + "MOD_PGM, "
				   + "MOD_TIME "
				   + "FROM DBC_CARD ";
			int cnt = 0;
			int dbcCardCur = openCursor();
			
			while (fetchTable(dbcCardCur)) {
				cnt++;
				IcuD999Data data = new IcuD999Data();
				data.isDebit = "Y";
				data.idPSeqno = getValue("ID_P_SEQNO");
				data.cardNo = getValue("CARD_NO");
				data.corpNo = getValue("CORP_NO");
				data.corpPSeqno = getValue("CORP_P_SEQNO");
				data.groupCode = getValue("GROUP_CODE");
				data.modPgm = getValue("MOD_PGM");
				data.modTime = getValue("MOD_TIME");
				
				if (isIdPSeqnoInDbcIdno(data.idPSeqno) == false) {
					list.add(data);
				}
				
				if (cnt % COUNT_EACH_BATCH == 0) {
					showLogMessage("I", "", String.format("已處理[%d]筆", cnt));
				}
				
			}
			
			
			
			closeCursor(dbcCardCur);
		
	}



	private void readCrdCard() throws Exception {
		showLogMessage("I", "", "開始處理DBC_CARD");
		sqlCmd = " SELECT ID_P_SEQNO, "
			   + "CARD_NO, "
			   + "CORP_NO, "
			   + "CORP_P_SEQNO, "
			   + "GROUP_CODE, "
			   + "MOD_PGM, "
			   + "MOD_TIME "
			   + "FROM CRD_CARD ";
		int cnt = 0;
		int crdCardCur = openCursor();
		
		while (fetchTable(crdCardCur)) {
			cnt ++;
			IcuD999Data data = new IcuD999Data();
			data.isDebit = "N";
			data.idPSeqno = getValue("ID_P_SEQNO");
			data.cardNo = getValue("CARD_NO");
			data.corpNo = getValue("CORP_NO");
			data.corpPSeqno = getValue("CORP_P_SEQNO");
			data.groupCode = getValue("GROUP_CODE");
			data.modPgm = getValue("MOD_PGM");
			data.modTime = getValue("MOD_TIME");
			
			if (isIdPSeqnoInCrdIdno(data.idPSeqno) == false) {
				list.add(data);
			}
			
			if (cnt % 10000 == 0) {
				showLogMessage("I", "", String.format("已處理[%d]筆", cnt));
			}
		}
		
		closeCursor(crdCardCur);

	}



	private boolean isIdPSeqnoInCrdIdno(String idPSeqno) throws Exception {
		sqlCmd = " SELECT 1 FROM CRD_IDNO WHERE ID_P_SEQNO = ? ";
		setString(1, idPSeqno);
		return selectTable() > 0;
	}
	
	private boolean isIdPSeqnoInDbcIdno(String idPSeqno) throws Exception {
		sqlCmd = " SELECT 1 FROM DBC_IDNO WHERE ID_P_SEQNO = ? ";
		setString(1, idPSeqno);
		return selectTable() > 0;
	}



	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		IcuD999 proc = new IcuD999();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
	
	class IcuD999Data{
		public static final String SEPA = "||";
		String isDebit = ""; 
		String idPSeqno = "";
		String cardNo = "";
		String corpNo = "";
		String corpPSeqno = "";
		String groupCode = "";
		String modPgm = "";
		String modTime = "";
		
		public String convertToString(){
			StringBuilder sb = new StringBuilder();
			sb.append(isDebit).append(SEPA)
			  .append(idPSeqno).append(SEPA)
			  .append(cardNo).append(SEPA)
			  .append(corpNo).append(SEPA)
			  .append(corpPSeqno).append(SEPA)
			  .append(groupCode).append(SEPA)
			  .append(modPgm).append(SEPA)
			  .append(modTime).append(SEPA)
			  .append("\r\n");
			return sb.toString();
		}
	}

	/***********************************************************************/
}
