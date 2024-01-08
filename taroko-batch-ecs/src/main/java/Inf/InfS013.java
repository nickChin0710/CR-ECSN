/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/03/28  V1.00.00   Zuwei      program initial                          *
*  112/05/03  V1.00.01   Kirin      add FOLDER path string                   *
*  112/06/20  V1.00.02   NickChin   加參數  			                         *
*  112/06/21  V1.00.03   Kirin      Fix Date     	                         *
*  112/06/30  V1.00.04   Kirin      adjust where ACCT_MONTH                  *
*  112/07/04  V1.00.05   NickChin   加複製檔案									 *
*  112/07/16  V1.00.06   NickChin   調整複製檔案								 *
*  112/08/15  V1.00.07   NickChin   取消隱碼(身分證字號、手機號碼)	    			 *
*  112/08/28  V1.00.08   NickChin   selectMktThsrUpgradeListData刪除<			 *
*  112/09/07  V1.00.09   NickChin   調整金額取值								 *
*  112/09/12  V1.00.10   kirin      sql條件use_month    						 *
*  112/10/06  V1.00.11   NickChin   調整複製檔案								 *
*  112/11/23  V1.00.12   Kirin      0筆要產空檔                                  *
******************************************************************************/
package Inf;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.AccessDAO;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommTxInf;
import Cca.CalBalance;

public class InfS013 extends AccessDAO {
    private static final int OUTPUT_BUFF_SIZE = 5000;
    private final String progname = "高鐵升等產製名單檔程式(客服) 112/10/06 V1.00.12";
    private static final String CRM_FOLDER = "/media/crm/";
    private static final String NEWCENTER_FOLDER = "/crdatacrea/NEWCENTER/";
    private static final String DATA_FORM = "CCTHSRX";
	private static final String COL_SEPERATOR = "\006";// 區隔號
	private final static String FTP_FOLDER = "NEWCENTER";
    private final String lineSeparator = System.lineSeparator();
    private String busiDate = "";
    private String busiMonth = "";  //++
    private String businessDate = "";

    CommCrd commCrd = new CommCrd();
    CommDate commDate = new CommDate();
    CommTxInf commTxInf = null;
    CalBalance calBalance = null;

    public int mainProcess(String[] args) {
        try {
            calBalance = new CalBalance(conn, getDBalias());
            commTxInf = new CommTxInf(getDBconnect(), getDBalias());
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();

            if (!connectDataBase()) {
                commCrd.errExit("connect DataBase error", "");
            }
            // =====================================
            
            if (args.length >0) {
            	busiMonth = args[0].substring(0, 6);
            	Date bDate = new SimpleDateFormat("yyyyMMdd").parse(args[0]);
            	Calendar calendar = Calendar.getInstance();
            	calendar.setTime(bDate);
            	// 日期减一天
            	calendar.add(Calendar.DATE, -1);

            	busiDate = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
            	System.out.println("參數日期 : " + busiDate);
            }else {
            	selectPtrBusinday();
            }

            showLogMessage("I", "", javaProgram + " " + progname);
            showLogMessage("I", "", String.format("執行日期[%s]", sysDate));

            // get the name and the path of the .DAT file
            String datFileName = String.format("%s_%s%s", DATA_FORM, busiDate,CommTxInf.DAT_EXTENSION);
            String fileFolder = Paths.get(commCrd.getECSHOME()/*FOLDER*/, CRM_FOLDER).toString();

            // 產生主要檔案 .DAT
            int dataCount = generateDatFile(fileFolder, datFileName);
//            updateMktThsrUpgradeList();
            
            dateTime(); // update the system date and time
            boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, sysDate, sysDate, sysTime.substring(0,4), dataCount);
            if (isGenerated == false) {
            	commCrd.errExit("產生HDR檔錯誤!", "");
            }
            
            String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION); 

            // run FTP
            procFTP(fileFolder, datFileName, hdrFileName);
            
            showLogMessage("I", "", "執行結束");
            return 0;
        } catch (Exception e) {
            expMethod = "mainProcess";
            expHandle(e);
            return exceptExit;
        } finally {
            finalProcess();
        }
    }
    
    void copyFile(String datFileName1, String fileFolder1 ,String datFileName2, String fileFolder2) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder2, datFileName2).toString();

		if (commCrd.fileCopy(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName2 + "]copy失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已copy至 [" + tmpstr2 + "]");
	}

    private void selectPtrBusinday() throws Exception {
        busiDate = "";
        sqlCmd = " select business_date, " +
                " to_char(to_date(business_date, 'yyyymmdd') -1 days, 'yyyymmdd') as h_prev_date " +
                " from ptr_businday";

        selectTable();
        if (notFound.equals("Y")) {
            commCrd.errExit("select_ptr_businday not found!", "");
        }
        busiDate = getValue("h_prev_date");
        businessDate = getValue("business_date");
        busiMonth = businessDate.substring(0,6);
//        busiMonth = busiDate.substring(0,6);
//        busiPrevMonth = getValue("h_prev_month");
    }

    /**
     * generate a .Dat file
     * 
     * @param fileFolder 檔案的資料夾路徑
     * @param datFilename .dat檔的檔名
     * @return the number of rows written. If the returned value is -1, it means the path or the
     *         file does not exist.
     * @throws Exception
     */
    private int generateDatFile(String fileFolder, String datFilename) throws Exception {

        String datFilePath = Paths.get(fileFolder, datFilename).toString();
        boolean isOpen = openBinaryOutput(datFilePath);
        if (isOpen == false) {
            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
            return -1;
        }

        int rowCount = 0;
        int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a
                                   // specified value
        try {
            StringBuffer sb = new StringBuffer();
            showLogMessage("I", "", "開始產生.DAT檔......");

            // 處理信用卡資料
            showLogMessage("I", "", "開始處理信用卡資料檔......");
            int totalCnt = selectMktThsrUpgradeListData();
//            if (totalCnt == 0) {
//                commCrd.errExit("沒有符合資料可產檔傳送", "");
//            }
            
            for (int i = 0; i < totalCnt; i++) {
                String rowOfDAT = getRowOfDAT(i);
                sb.append(rowOfDAT);
				rowCount++;
                countInEachBuffer++;
                if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                    showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
					byte[] tmpBytes = sb.toString().getBytes("UTF-8");
                    writeBinFile(tmpBytes, tmpBytes.length);
                    sb = new StringBuffer();
                    countInEachBuffer = 0;
                }
            }
            
            // write the rest of bytes on the file 
            if (countInEachBuffer > 0) {
                showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes("UTF-8");
                writeBinFile(tmpBytes, tmpBytes.length);
            }

            if (rowCount == 0) {
                showLogMessage("I", "", "無資料可寫入.DAT檔");
            } else {
                showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
            }

        } finally {
            closeBinaryOutput();
        }

        return rowCount;
    }

    /**
     * 產生檔案
     * 
     * @return String
     * @throws Exception
     */
    private String getRowOfDAT(int i) throws Exception {
        String idNo = getValue("ID_NO", i);
        String cardNo = getValue("CARD_NO", i);
        String strIdNo = idNo;//0815不隱碼 idNo.substring(0,4) + "XXX" + idNo.substring(6, 10);
        String groupCode = getValue("GROUP_CODE", i);
        String strGroupCode = groupCode.substring(groupCode.length() - 3);
        String cellarPhone = getValue("CELLAR_PHONE", i);
        String strCellarPhone = cellarPhone;
        //0815不隱碼
//        if (cellarPhone.length() >= 3) {
//            strCellarPhone = cellarPhone.substring(0, 3) + "XXX";
//            if (cellarPhone.length() >= 6) {
//                if (cellarPhone.length() >= 10) {
//                    strCellarPhone += cellarPhone.substring(6, 10);
//                } else {
//                    strCellarPhone += cellarPhone.substring(6);
//                }
//            }
//        }
        Double totAmt = getValueDouble("TOT_AMT",i);
        String strTotAmt = String.format("%014.2f", totAmt);

        StringBuffer sb = new StringBuffer();
        sb.append(commCrd.fixLeft(strIdNo, 10)); // 身份字號
        sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
        sb.append(commCrd.fixLeft(cardNo, 16)); // 卡號
        sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
        sb.append(commCrd.fixLeft(strGroupCode, 3)); // 卡別
        sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
        sb.append(commCrd.fixLeft(strCellarPhone, 10)); // 手機號碼
        sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
        sb.append(commCrd.fixLeft(strTotAmt, 14)); // 消費金額
        sb.append(lineSeparator);

        return sb.toString();
    }

    // 讀取資料
    private int selectMktThsrUpgradeListData() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT * ");
        sb.append(" FROM MKT_THSR_UPGRADE_LIST ");
//        sb.append(" WHERE to_date(ACCT_MONTH, 'yyyymm') = to_date(?, 'yyyymm')");
        sb.append(" WHERE to_date(USE_MONTH, 'yyyymm') = to_date(?, 'yyyymm')");
//        sb.append("  FETCH FIRST 100000 ROWS ONLY");
//        sb.append("     (SEND_DATE IS NULL OR SEND_DATE = '') ");
//        sb.append("     AND ACCT_MONTH = ? "); // 上個月
//        sb.append("     AND USE_MONTH <= ? "); // business_date的年月 or 傳入YYYYMM
//        sb.append("     to_date(USE_MONTH, 'yyyymm') <= to_date(?, 'yyyymm') ");

        sqlCmd = sb.toString();
//        setString(1, busiPrevMonth); // USE_MONTH
//        setString(2, busiMonth); // USE_MONTH
        setString(1, busiMonth); 
//nick        setString(1, busiDate.substring(0, 6));
        return selectTable();
    }

    // 更新資料
//    private int updateMktThsrUpgradeList() throws Exception {
//        StringBuffer sb = new StringBuffer();
//        sb.append(" UPDATE ");
//        sb.append("     MKT_THSR_UPGRADE_LIST ");
//        sb.append(" SET  ");
//        sb.append("     SEND_DATE = ?, ");
//        sb.append("     MOD_TIME = sysdate, ");
//        sb.append("     MOD_PGM = ? ");
//        sb.append(" WHERE ");
////        sb.append("     (SEND_DATE IS NULL OR SEND_DATE = '') ");
////        sb.append("     AND ACCT_MONTH = ? "); // 上個月
////        sb.append("     AND USE_MONTH <= ? "); // business_date的年月 or 傳入YYYYMM
//        sb.append("     to_date(USE_MONTH, 'yyyymm') <= to_date(?, 'yyyymm') ");
//
//        sqlCmd = sb.toString();
//        setString(1, sysDate); // 
////        setString(2, sysDatesysTime); // 
//        setString(2, javaProgram); // 
////        setString(3, busiPrevMonth); // USE_MONTH
////        setString(4, busiMonth); // USE_MONTH
//        setString(3, busiDate.substring(0, 6));
//        return updateTable();
//    }

    void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = FTP_FOLDER; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName(FTP_FOLDER, ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
			commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
		}
	}
    
    public static void main(String[] args) {
        InfS013 proc = new InfS013();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }

}
