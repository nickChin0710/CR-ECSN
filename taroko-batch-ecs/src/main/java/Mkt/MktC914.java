/*****************************************************************************************************
 *                                                                                                   *
 *                              MODIFICATION LOG                                                     *
 *                                                                                                   *
 *     DATE     Version    AUTHOR                   DESCRIPTION                                      *
 *  ---------  --------- ----------- --------------------------------------------------------------  *
 *  112/10/23  V1.00.00  Ryan        program initial                                                 *
 *  112/11/07  V1.00.01  Grace       fix record length                                               *
 *****************************************************************************************************/
package Mkt;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.AccessDAO;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class MktC914 extends AccessDAO {
    private final String PROGNAME = "漢來美食館內外各廳 消費資料產檔處理  112/10/23 V1.00.00 ";
    CommFunction comm = new CommFunction();
    private CommCrd comc = new CommCrd();
    private CommDate commDate = new CommDate();
    private CommString commStr = new CommString();
    private CommFTP commFTP = null;
    private CommRoutine comr;


    private static final String PATH_FOLDER = "/media/mkt/";
    private static final int OUTPUT_BUFF_SIZE = 100000;
    private static final String HILAIFOOD_IO_YMD = "HILAIFOOD_IO.006.YYYYMMDD.txt";
    private static final String HILAIFOOD_PREOFFICE_YMD = "HILAIFOOD_PREOFFICE.006.YYYYMMDD.txt";
    private static final String HILAI_OFFICEAREA_YMD = "HILAI_OFFICEAREA.006.YYYYMMDD.txt";
    private static final String HILAIFOOD_IO_YM = "HILAIFOOD_IO.006.YYYYMM.txt";
    private static final String HILAIFOOD_PREOFFICE_YM = "HILAIFOOD_PREOFFICE.006.YYYYMM.txt";
    private static final String HILAI_OFFICEAREA_YM = "HILAI_OFFICEAREA.006.YYYYMM.txt";
	private final static String COL_SEPERATOR = ",";
	private final static String LINE_SEPERATOR = System.lineSeparator();
    
    private String hBusinessDate = "";
    private String hBusinessMonth = "";
    private String hLastBusinessMonth = "";
    private boolean runYmd = false;
    private boolean runYm = false;
    private String runYmdFlag = "6";

    int tolCnt = 0;

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            showLogMessage("I", "", "Usage MktC914 [business_date]");

            // =====================================

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();
            
            
            String parmDate = "";
            if (args.length >= 1) {
            	parmDate = args[0];
                showLogMessage("I", "", String.format("程式參數1: [%s]", parmDate));
                if (!commDate.isDate(parmDate)) {
                    showLogMessage("I", "", "請傳入參數合格值: YYYYMMDD");
                    return -1;
                }
                hBusinessDate = parmDate;
                hBusinessMonth = commStr.left(parmDate, 6);
                hLastBusinessMonth = commDate.monthAdd(parmDate, -1);
            }
            
            if (commStr.right(hBusinessDate, 2).equals("01")) {
            	showLogMessage("I", "", "營業日[" + hBusinessDate +"]每月1日，程式執行月報");
            	showLogMessage("I", "", String.format("月報 ,執行年月為[%s]", hLastBusinessMonth));
            	runYm = true;
            } 
            if("1".equals(getWeek(hBusinessDate))) {
             	showLogMessage("I", "", "營業日[" + hBusinessDate +"]每周一，程式執行周報");
            	if(commStr.pos(",01,02,03,04,05", commStr.right(hBusinessDate, 2))>0) {
            		runYmdFlag = "1";
            		showLogMessage("I", "", String.format("週報 ,執行年月為[%s]", hLastBusinessMonth));
            	}else 
            		showLogMessage("I", "", String.format("週報 ,執行年月為[%s]", hBusinessMonth));
            	runYmd = true;
            }
            if(runYm == false && runYmd == false){
            	showLogMessage("I", "", "營業日[" + hBusinessDate +"]非執行日，程式不執行");
            	return 0;
            }

            generateDatFile(PATH_FOLDER,HILAIFOOD_IO_YMD);
            generateDatFile(PATH_FOLDER,HILAIFOOD_IO_YM);
            generateDatFile(PATH_FOLDER,HILAIFOOD_PREOFFICE_YMD);
            generateDatFile(PATH_FOLDER,HILAIFOOD_PREOFFICE_YM);
            generateDatFile(PATH_FOLDER,HILAI_OFFICEAREA_YMD);
            generateDatFile(PATH_FOLDER,HILAI_OFFICEAREA_YM);
            
            // ==============================================
            showLogMessage("I", "", "執行結束");
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }finally {
        	finalProcess();
        }
    }

    private void selectPtrBusinday() throws Exception {
        sqlCmd = "select BUSINESS_DATE,uf_month_add(BUSINESS_DATE,-1) AS LAST_BUSINESS_MONTH , LEFT(BUSINESS_DATE,6) AS BUSINESS_MONTH from PTR_BUSINDAY ";
        selectTable();

        if (notFound.equals("Y")) {
            comc.errExit("執行結束, 營業日為空!!", "");
        }

        hBusinessDate = getValue("BUSINESS_DATE");
        hBusinessMonth = getValue("BUSINESS_MONTH");
        hLastBusinessMonth = getValue("LAST_BUSINESS_MONTH");
    }
    
	 
	 private int selectMktData(String fileName) throws Exception {
			switch(fileName) {
			case HILAIFOOD_IO_YMD:
				if(runYmd == true)
					return selectMktC914Data1("6".equals(runYmdFlag) ? hBusinessMonth : hLastBusinessMonth);
				else
					break;
			case HILAIFOOD_IO_YM:
				if(runYm == true)
					return selectMktC914Data1(hLastBusinessMonth);
				else
					break;
			case HILAIFOOD_PREOFFICE_YMD:
				if(runYmd == true)
					return selectMktC914Data2("6".equals(runYmdFlag) ? hBusinessMonth : hLastBusinessMonth);
				else
					break;
			case HILAIFOOD_PREOFFICE_YM:
				if(runYm == true)
					return selectMktC914Data2(hLastBusinessMonth);
				else
					break;
			case HILAI_OFFICEAREA_YMD:
				if(runYmd == true)
					return selectMktC914Data3("6".equals(runYmdFlag) ? hBusinessMonth : hLastBusinessMonth);
				else
					break;
			case HILAI_OFFICEAREA_YM:
				if(runYm == true)
					return selectMktC914Data3(hLastBusinessMonth);
				else
					break;
			}
			return -1;
	 }
	
	 /***
	      *    漢來美食館內館外消費請款明細
	  * @throws Exception
	  */
    private int selectMktC914Data1(String selectDate) throws Exception {
    	fetchExtend = "data1.";
        sqlCmd = " SELECT c.ID_NO, d.ID_NO AS MAJOR_ID_NO,decode(b.SUP_FLAG, '0', 'PP', 'NP') as DB_SUP_FLAG, ";
        sqlCmd += " a.CARD_NO, SUBSTRING(a.GROUP_CODE,2,3) AS DB_GROUP_CODE, ";
        sqlCmd += " decode((a.MCHT_CHI_NAME LIKE '漢來美食%' OR a.MCHT_CHI_NAME LIKE '漢來大飯店%'), true, 'I','O') AS DB_MCHT_FLAG, ";
        sqlCmd += " sum(decode(a.SIGN_FLAG,'-', -1*a.DEST_AMT, a.DEST_AMT)) AS DB_DEST_AMT, ";
        sqlCmd += " substr(a.POST_DATE, 1,6) AS DB_POST_DATE ";
        sqlCmd += " FROM BIL_bill a, CRD_CARD b, CRD_IDNO c, CRD_IDNO d ";
        sqlCmd += " WHERE a.card_no=b.card_no ";
        sqlCmd += " AND a.ID_P_SEQNO=c.ID_P_SEQNO ";
        sqlCmd += " AND a.MAJOR_ID_P_SEQNO=d.ID_P_SEQNO ";
        sqlCmd += " AND b.group_code IN ('1677','1631','1893') ";
        sqlCmd += " AND left(a.POST_DATE,6) = ? ";
        sqlCmd += " GROUP BY a.CARD_NO,c.ID_NO, d.ID_NO,decode(b.SUP_FLAG, '0', 'PP', 'NP'),SUBSTRING(a.GROUP_CODE,2,3), ";
        sqlCmd += " decode((a.MCHT_CHI_NAME LIKE '漢來美食%' OR a.MCHT_CHI_NAME LIKE '漢來大飯店%'), true, 'I','O'),substr(a.POST_DATE, 1,6) ";
        sqlCmd += " ORDER BY a.CARD_NO,decode((a.MCHT_CHI_NAME LIKE '漢來美食%' OR a.MCHT_CHI_NAME LIKE '漢來大飯店%'), true, 'I','O') ";

        setString(1, selectDate);
        return openCursor();
    }
    
	 /***
	  *    漢來美食館內各廳消費請款明細
	 * @throws Exception
	 */
    private int selectMktC914Data2(String selectDate) throws Exception {
    	fetchExtend = "data2.";
        sqlCmd = " SELECT SUBSTRING(a.onus_ter_no, 9, 8) as db_onus_ter_no, SUBSTRING(a.MCHT_CHI_NAME,1,14) as db_MCHT_CHI_NAME,  ";
        sqlCmd += " sum(decode(a.ECS_SIGN_CODE,'-', -1*a.DEST_AMT, a.DEST_AMT)) as db_DEST_AMT, ";
        sqlCmd += " left(a.BATCH_DATE,6) as db_BATCH_DATE ";
        sqlCmd += " FROM BIL_FISCDTL a, CRD_CARD b ";
        sqlCmd += " WHERE a.card_no=b.card_no ";
        sqlCmd += " AND b.group_code IN ('1677','1631','1893') ";
        sqlCmd += " AND left(a.BATCH_DATE,6) = ? ";
        sqlCmd += " AND (a.MCHT_CHI_NAME LIKE '漢來美食%' OR a.MCHT_CHI_NAME LIKE '漢來大飯店%')  ";
        sqlCmd += " GROUP BY SUBSTRING(a.onus_ter_no, 9, 8), SUBSTRING(a.MCHT_CHI_NAME,1,14),left(a.BATCH_DATE,6) ";
        
        setString(1, selectDate);
        return openCursor();
    }
    
	 /***
	  *   漢來各公司消費請款明細
	 * @throws Exception
	 */
   private int selectMktC914Data3(String selectDate) throws Exception {
   	fetchExtend = "data3.";
       sqlCmd = " SELECT SUBSTRING(a.onus_ter_no, 1, 8) as db_onus_ter_no, SUBSTRING(a.MCHT_CHI_NAME,1,14) db_MCHT_CHI_NAME,   ";
       sqlCmd += " sum(decode(a.ECS_SIGN_CODE,'-', -1*a.DEST_AMT, a.DEST_AMT)) as db_DEST_AMT, ";
       sqlCmd += " left(a.BATCH_DATE,6) as db_BATCH_DATE ";
       sqlCmd += " FROM BIL_FISCDTL a, CRD_CARD b ";
       sqlCmd += " WHERE a.card_no=b.card_no ";
       sqlCmd += " AND b.group_code IN ('1677','1631','1893') ";
       sqlCmd += " AND left(a.BATCH_DATE,6) = ? ";
       sqlCmd += " AND a.MCHT_CHI_NAME LIKE '漢來%'  ";
       sqlCmd += " GROUP BY SUBSTRING(a.onus_ter_no, 1, 8), SUBSTRING(a.MCHT_CHI_NAME,1,14),left(a.BATCH_DATE,6) ";
       sqlCmd += " ORDER BY SUBSTRING(a.onus_ter_no, 1, 8) ";
       
       setString(1, selectDate);
       return openCursor();
   }
    
    private MktC914Data getMktData(String fileName) throws Exception {
		switch(fileName) {
		case HILAIFOOD_IO_YMD:
		case HILAIFOOD_IO_YM:
			return getMktData1();
		case HILAIFOOD_PREOFFICE_YMD:
		case HILAIFOOD_PREOFFICE_YM:
			return getMktData2();
		case HILAI_OFFICEAREA_YMD:
		case HILAI_OFFICEAREA_YM:
			return getMktData3();
		}
		return null;
    }
    
    private MktC914Data getMktData1() throws Exception {
    	MktC914Data mktC916Data = new MktC914Data();
    	mktC916Data.idNo = getValue("data1.ID_NO");
    	mktC916Data.majorIdNo = getValue("data1.MAJOR_ID_NO");
    	mktC916Data.dbSupFlag = getValue("data1.DB_SUP_FLAG");
    	mktC916Data.cardNo = getValue("data1.CARD_NO");
    	mktC916Data.dbGroupCode = getValue("data1.DB_GROUP_CODE");
    	mktC916Data.dbMchtFlag = getValue("data1.DB_MCHT_FLAG");
    	mktC916Data.dbDestAmt = getValueLong("data1.DB_DEST_AMT");
    	mktC916Data.dbPostDate = getValue("data1.DB_POST_DATE");
    	return mktC916Data;
    }
    
    private MktC914Data getMktData2() throws Exception {
    	MktC914Data mktC916Data = new MktC914Data();
    	mktC916Data.dbOnusTerNo = getValue("data2.DB_ONUS_TER_NO");
    	mktC916Data.dbMchtChiName = getValue("data2.DB_MCHT_CHI_NAME");
    	mktC916Data.dbDestAmt = getValueLong("data2.DB_DEST_AMT");
    	mktC916Data.dbBatchDate = getValue("data2.DB_BATCH_DATE");
    	return mktC916Data;
    }
    
    private MktC914Data getMktData3() throws Exception {
    	MktC914Data mktC916Data = new MktC914Data();
    	mktC916Data.dbOnusTerNo = getValue("data3.DB_ONUS_TER_NO");
    	mktC916Data.dbMchtChiName = getValue("data3.DB_MCHT_CHI_NAME");
    	mktC916Data.dbDestAmt = getValueLong("data3.DB_DEST_AMT");
    	mktC916Data.dbBatchDate = getValue("data3.DB_BATCH_DATE");
    	return mktC916Data;
    }

    private String getRowOfDAT(MktC914Data mktC916Data,String fileName) throws Exception{
    	switch(fileName) {
		case HILAIFOOD_IO_YMD:
		case HILAIFOOD_IO_YM:
			return getRowOfDAT1(mktC916Data);
		case HILAIFOOD_PREOFFICE_YMD:
		case HILAIFOOD_PREOFFICE_YM:
			return getRowOfDAT2(mktC916Data);
		case HILAI_OFFICEAREA_YMD:
		case HILAI_OFFICEAREA_YM:
			return getRowOfDAT3(mktC916Data);
		}
    	return "";
    }

    /***
     * 檔案名稱：HILAIFOOD_IO.006.YYYYMMDD.ZIP、HILAIFOOD_IO.006.YYYYMM.ZIP。
     * 檔案型態：TXT檔，並以逗號分隔欄位。檔案內容(總長度為100bytes)。
     * @param mktC916Data
     * @return
     * @throws Exception
     */
    private String getRowOfDAT1(MktC914Data mktC916Data) throws Exception{
    	StringBuffer sb = new StringBuffer();
    	sb.append(comc.fixLeft(mktC916Data.idNo, 10)).append(COL_SEPERATOR);
    	sb.append(comc.fixLeft(mktC916Data.majorIdNo,10)).append(COL_SEPERATOR);
    	sb.append(comc.fixLeft(mktC916Data.dbSupFlag, 2)).append(COL_SEPERATOR);
        sb.append(comc.fixLeft(mktC916Data.cardNo, 16)).append(COL_SEPERATOR);
        sb.append(comc.fixLeft(mktC916Data.dbGroupCode, 3)).append(COL_SEPERATOR);
        sb.append(comc.fixLeft(mktC916Data.dbMchtFlag, 1)).append(COL_SEPERATOR);
        sb.append(comc.fixLeft(String.format("%010d", mktC916Data.dbDestAmt), 10)).append(COL_SEPERATOR);
        sb.append(comc.fixLeft(mktC916Data.dbPostDate, 6)).append(COL_SEPERATOR);
        sb.append(comc.fixLeft(" ", 34)).append(LINE_SEPERATOR);
        return sb.toString();
    }
    
	/***
	 * 檔案名稱：HILAIFOOD_PREOFFICE.006.YYYYMMDD.ZIP、HILAIFOOD_PREOFFICE.006.YYYYMM.ZIP。
	 * 檔案型態：TXT檔，並以逗號分隔欄位。 檔案內容(總長度為100bytes)。
	 * @param mktC916Data
	 * @return
	 * @throws Exception
	 */
	private String getRowOfDAT2(MktC914Data mktC916Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft(mktC916Data.dbOnusTerNo, 15)).append(COL_SEPERATOR);
		sb.append(comc.fixLeft(mktC916Data.dbMchtChiName, 28)).append(COL_SEPERATOR);
		sb.append(comc.fixLeft(String.format("%010d", mktC916Data.dbDestAmt), 10)).append(COL_SEPERATOR);
		sb.append(comc.fixLeft(mktC916Data.dbBatchDate, 6)).append(COL_SEPERATOR);
		sb.append(comc.fixLeft(" ", 37)).append(LINE_SEPERATOR);
		return sb.toString();
	} 
    
	/***
	 * 檔案名稱：HILAI_OFFICEAREA.006.YYYYMMDD.ZIP、HILAI_OFFICEAREA.006.YYYYMM.ZIP。
	 * 檔案型態：TXT檔，並以逗號分隔欄位。 檔案內容(總長度為100bytes)。
	 * @param mktC916Data
	 * @return
	 * @throws Exception
	 */
	private String getRowOfDAT3(MktC914Data mktC916Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft(mktC916Data.dbOnusTerNo, 15)).append(COL_SEPERATOR);
		sb.append(comc.fixLeft(mktC916Data.dbMchtChiName, 28)).append(COL_SEPERATOR);
		sb.append(comc.fixLeft(String.format("%010d", mktC916Data.dbDestAmt), 10)).append(COL_SEPERATOR);
		sb.append(comc.fixLeft(mktC916Data.dbBatchDate, 6)).append(COL_SEPERATOR);
		sb.append(comc.fixLeft(" ", 37)).append(LINE_SEPERATOR);
		return sb.toString();
	} 
    
	private int generateDatFile(String fileFolder, String fileName) throws Exception {

		int resultCursor = selectMktData(fileName);
		if(resultCursor == -1) return 0;
		String datFileName = fmtName(fileName);

		String datFilePath = Paths.get(comc.getECSHOME(),fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", String.format("========================================================="));
			showLogMessage("I", "", String.format("開始產生.TXT檔......[%s]", datFileName));
			while (fetchTable()) {
				MktC914Data mktC916Data = getMktData(fileName);
				if(mktC916Data == null) return 0;
				String rowOfDAT = getRowOfDAT(mktC916Data,fileName);
				sb.append(rowOfDAT);
				rowCount++;
				countInEachBuffer++;
				if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
					showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
					byte[] tmpBytes = sb.toString().getBytes();
					writeBinFile(tmpBytes, tmpBytes.length);
					sb = new StringBuffer();
					countInEachBuffer = 0;
				}
			}
			
			closeCursor(resultCursor);
			// write the rest of bytes on the file 
			if (countInEachBuffer > 0) {
				showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes();
				writeBinFile(tmpBytes, tmpBytes.length);
			}
			
			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入.TXT檔");
			}else {
				showLogMessage("I", "", String.format("產生.TXT檔完成！，共產生%d筆資料", rowCount));
			}
		}finally {
			closeBinaryOutput();
		}
		if(rowCount > 0)
			ftpProc(datFileName);
		
		return rowCount;
	}

    // ************************************************************************
    private void ftpProc(String filename) throws Exception {
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = comc.getECSHOME() + PATH_FOLDER; // 相關目錄皆同步
        commFTP.hEflgModPgm = javaProgram;

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        showLogMessage("I", "", "put %s " + filename + " 開始上傳....");

        String tmpChar = "put " + filename;

        int errCode = commFTP.ftplogName("NCR2TCB", tmpChar);

        if (errCode != 0) {
            showLogMessage("I", "", "檔案傳送 " + "NCR2TCB" + " 有誤(error), 請通知相關人員處理");
            showLogMessage("I", "", "MktC914 執行完成 傳送  NCR2TCB 失敗[" + filename + "]");
            commFTP.insertEcsNotifyLog(filename, "3", javaProgram, sysDate, sysTime);
            return;
        }

        showLogMessage("I", "", "FTP完成.....");

        // 刪除檔案 put 不用刪除
        renameFile(filename);
    }

    // ************************************************************************
    private void renameFile(String removeFileName) throws Exception {
    	String tmpStr1 = Paths.get(comc.getECSHOME(),PATH_FOLDER, removeFileName).toString();
    	String tmpStr2 = Paths.get(comc.getECSHOME(),PATH_FOLDER, "/backup" , removeFileName + "." + sysDate + sysTime).toString();

        if (!comc.fileRename2(tmpStr1, tmpStr2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpStr2 + "]");
    }
    
	 private String getWeek(String dateStr) throws Exception{
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			Date date = format.parse(dateStr);
			String[] weeks = { "7", "1", "2", "3", "4", "5", "6" };
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
			if (week_index < 0) {
				week_index = 0;
			}
			return weeks[week_index];
		}
	 
	 private String fmtName(String fileName) {
		    switch(fileName) {
		    case HILAIFOOD_IO_YMD:
		    case HILAIFOOD_PREOFFICE_YMD:
		    case HILAI_OFFICEAREA_YMD:
		    	return fileName.replace("YYYYMMDD", hBusinessDate);
		    case HILAIFOOD_IO_YM:
		    case HILAIFOOD_PREOFFICE_YM:
		    case HILAI_OFFICEAREA_YM:
		    	return fileName.replace("YYYYMM", hBusinessMonth);
		    }
		    return fileName;
	 }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktC914 proc = new MktC914();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}

class MktC914Data{
	String idNo = "";
	String majorIdNo = "";
	String dbSupFlag = "";
	String cardNo = "";
	String dbGroupCode = "";
	String dbMchtFlag = "";
	long dbDestAmt = 0;
	String dbPostDate = "";
	String dbOnusTerNo = "";
	String dbMchtChiName = "";
	String dbBatchDate = "";
}
