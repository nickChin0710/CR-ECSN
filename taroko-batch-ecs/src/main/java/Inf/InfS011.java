/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/04/10  V1.00.00   NickChin   program initial                          *
*  112/04/12  V1.00.01   NickChin   優惠別非租賃車時填入原本值、DAT只顯示資料            *
*  112/04/13  V1.00.02   NickChin   日期欄西元年轉民國年不足3碼時左補0                 *
*  112/06/20  V1.00.03   NickChin   加參數  			                         *
*  112/06/21  V1.00.04   Kirin      Fix Date     	                         *
*  112/07/04  V1.00.05   NickChin   加複製檔案									 *
*  112/07/10  V1.00.06   NickChin   調整複製檔案								 *
*  112/08/15  V1.00.07   NickChin   取消隱碼(身分證字號、卡號、車號)	    			 *
*  112/10/06  V1.00.08   NickChin   調整複製檔案								 *
*  112/11/23  V1.00.09   Kirin     0筆要產空檔                                  *
*****************************************************************************/
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

public class InfS011 extends AccessDAO {
    private static final int OUTPUT_BUFF_SIZE = 5000;
    private final String progname = "車籍登錄資料檔產製名單程式(客服) 112/10/06 V1.00.09";
    private static final String CRM_FOLDER = "/media/crm/";
    private static final String NEWCENTER_FOLDER = "/crdatacrea/NEWCENTER/";
    private static final String DATA_FORM = "CCTPCY7X";
    private static final String COL_SEPERATOR = "\006";//區隔號
	private final static String FTP_FOLDER = "NEWCENTER";
    private final String lineSeparator = System.lineSeparator();
    private String busiDate = "";

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
            showLogMessage("I", "", javaProgram + ": 每日產製整檔名單檔."+busiDate);
            showLogMessage("I", "", String.format("執行日期[%s]", sysDate));

            // get the name and the path of the .DAT file
            String datFileName = String.format("%s_%s%s", DATA_FORM, busiDate,CommTxInf.DAT_EXTENSION);
            String fileFolder = Paths.get(/*FOLDER*/commCrd.getECSHOME(), CRM_FOLDER).toString();

            // 產生主要檔案.DAT
            int dataCount = generateDatFile(fileFolder, datFileName);
            
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

            int totalCnt = selectCmsRoadmasterListData();
//            if (totalCnt == 0) {
//                commCrd.errExit("沒有符合資料可產檔", "");
//            }
            for (int i = 0; i < totalCnt; i++) {
                String rowOfDAT = getRowOfDAT(i);
                sb.append(rowOfDAT);
				rowCount++;
                countInEachBuffer++;
                if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                    showLogMessage("I", "",
                            String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
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
    	
    	//身分證字號: 取ID_NO前10碼,5~7碼用X取代
        String rmCarmainId = getValue("RM_CARMANID", i);
        String rm_carmainid = rmCarmainId;//0815不隱碼 rmCarmainId.substring(0, 4) + "XXX" + rmCarmainId.substring(7);
        
        //卡號:取CARD_NO前16碼，9~13碼用X取代
        String cardNo = getValue("CARD_NO", i);
        String card_no = cardNo;//0815不隱碼 cardNo.substring(0, 8) + "XXXXX" + cardNo.substring(13);
        
        //車號: 取RM_CARNO前7碼，2~4碼用X取代
        String rmCarno = getValue("RM_CARNO", i);
        String rm_carno = rmCarno;//0815不隱碼 "";
//        if(rmCarno.length()>5) {
//        	rm_carno = rmCarno.substring(0, 1) + "XXX" + rmCarno.substring(4);
//        }else if(rmCarno.length()>=4){
//        	rm_carno = rmCarno.substring(0, 1) + "XXX";
//        }else {
//        	rm_carno = rmCarno;
//        }
        
        //優惠別:取RDS_PCARD的值,如為租賃車寫入L,非租賃車填入原來的值,租賃車規則:
		//1.車號長度是6碼且前2碼或後2碼是重複的英文字母或數字
		//2.車號長度是7碼且第1碼是R或r
        String rdsPcard = getValue("RDS_PCARD", i);
        String rds_pcard = rdsPcard.trim();
        if(rmCarno.length()==6) {
        	if(rmCarno.substring(0, 1).equals(rmCarno.substring(1, 2))||rmCarno.substring(5, 6).equals(rmCarno.substring(6))) {
            	rds_pcard = "L";
            }
        }else if(rmCarno.length()==7) {
        	if(rmCarno.substring(0, 1).toUpperCase().equals("R")){
            	rds_pcard = "L";
            }
        }
        
        //建檔/更新日期: 取 RM_MODDATE轉民國年(7碼)
        String rmModDate = commDate.toTwDate(getValue("RM_MODDATE", i));
        String rm_modDate = rmModDate;
        if(rmModDate.length()==6) {
        	rm_modDate = "0"+rmModDate;
        }
        
        
        //控管碼:如果CURRENT_CODE=0,寫入空白,否則寫入實際值
        String currentCode = getValue("CURRENT_CODE", i);
        String current_code = " ";
        if(!currentCode.equals("0")) {
        	current_code = currentCode;
        }
        
        //控管碼異動日期:ISSUE_DATE轉民國年月日(7碼:YYYMMDD)
        String issueDate = commDate.toTwDate(getValue("ISSUE_DATE", i));
        String issue_date = issueDate;
        if(issueDate.length()==6) {
        	issue_date = "0"+issueDate;
        }
        
        StringBuffer sb = new StringBuffer();
        
        sb.append(commCrd.fixLeft(rm_carmainid, 10)); // 身份字號
        sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
        sb.append(commCrd.fixLeft(card_no, 16)); // 卡號
        sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
        sb.append(commCrd.fixLeft(rm_carno, 7)); // 車號
        sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
        sb.append(commCrd.fixLeft(rds_pcard, 1)); // 優惠別
        sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
        sb.append(commCrd.fixLeft(rm_modDate, 7)); // 建檔/更新日期
        sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
        sb.append(commCrd.fixLeft(current_code, 1)); // 控管碼
        sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
        sb.append(commCrd.fixLeft(issue_date, 7)); // 控管碼異動日期
        sb.append(lineSeparator);
        return sb.toString();
    }

    // 讀取資料
    private int selectCmsRoadmasterListData() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("select b.RM_CARMANID,b.CARD_NO ,b.RM_CARNO,b.RDS_PCARD,b.RM_MODDATE,");
        sb.append("a.CURRENT_CODE , a.ISSUE_DATE ");
        sb.append("from cms_roadmaster b ");
        sb.append("left join crd_card a ON b.card_no =a.card_no");

        sqlCmd = sb.toString();
        return selectTable();
    }
    
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
        InfS011 proc = new InfS011();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }

}
