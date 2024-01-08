/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/07/19  V1.00.01    JeffKung  program initial                           *
*  112/10/18  V1.00.02    JeffKung  修改檔案長度->243+crlf                     *
******************************************************************************/

package Mkt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*產生高三信信用卡當月消費金額檔*/
public class MktA260 extends AccessDAO {
    private String progname = "產生高三信信用卡當月消費金額檔程式  112/10/18  V1.00.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "MktA260";
    String prgmName = "列印個人信用卡消費情形日報表程式";
    
    String rptNameM18H = "高雄市第三信用合作社聯名卡企業回饋金明細表";
    String rptIdM18H = "CRM18H";
    int rptSeqM18H = 0;
    int pageCntM18H = 0;
    List<Map<String, Object>> lparM18H = new ArrayList<Map<String, Object>>();
    
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hIdnoChiName = "";
    String hPrintName = "";
    String hRptName = "";
    String hBusinssDate = "";
    
    CommDate commDate = new CommDate();
    String dataMonth = "";
    String hBusDateTw = "";
    String hBusDateTwYear = "";
    String hBusDateMonth = "";
    String hBusDateDay = "";
    String sysTwDate = StringUtils.leftPad(commDate.twDate(), 7, "0");

    int totalCnt = 0;
    int realCnt = 0;
    int pageCnt = 0;
    
    double totalAmt = 0;
    double cardAmt = 0;
    int    cardCnt = 0;

    int lineCnt = 0;
    
    Map<String, String> hm = null;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();
            
            showLogMessage("I", "", "營業日期=[" + hBusinssDate + "]");
            
            if (args.length == 1 && args[0].length() == 8) {
            	hBusinssDate = args[0];
            } else {
            	if (!"02".equals(comc.getSubString(hBusinssDate,6))) {
            		showLogMessage("I", "", "每月2日執行, 本日非執行日!!");
            		return 0;
            	}
            }
            
            dataMonth = comm.lastMonth(hBusinssDate, 1);
            showLogMessage("I", "", "資料年月=[" + dataMonth + "]");
            
            //轉換民國年月日
            hBusDateTw = StringUtils.leftPad(commDate.toTwDate(hBusinssDate), 7, "0");
            hBusDateTwYear = hBusDateTw.substring(0, hBusDateTw.length() - 4);
            hBusDateMonth = hBusDateTw.substring(hBusDateTw.length() - 4).substring(0, 2);
            hBusDateDay = hBusDateTw.substring(hBusDateTw.length() - 2);

            showLogMessage("I", "", "程式開始執行......");

            //openFile
            String fileFolder = Paths.get(comc.getECSHOME(), "media/mkt/").toString();
            String fileName = String.format("MTXTCB_0%s.TXT", hBusDateTw);
            String datFilePath = Paths.get(fileFolder, fileName).toString();
    		boolean isOpen = openBinaryOutput(datFilePath);
    		if (isOpen == false) {
    			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
    			return -1;
    		}

    		selectBilFiscdtl();

            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],符合消費筆數:["+ realCnt+"]");
            ftpMput(fileName);

            commitDataBase();
            closeBinaryOutput();
            
            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void commonRtn() throws Exception {
        hBusinssDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void selectBilFiscdtl() throws Exception {

    	sqlCmd =  "select b.ecs_platform_kind, b.ecs_sign_code,b.source_curr,b.source_amt, ";
    	sqlCmd += "       round(b.dest_amt) as dest_amt, b.dest_curr, c.card_no,";
    	sqlCmd += "       c.group_code,b.mcc_code,b.mcht_chi_name,mcht_eng_name ";
		sqlCmd += "from bil_fiscdtl b ";
		sqlCmd += "join crd_card c on b.ecs_real_card_no = c.card_no ";
		sqlCmd += "               and c.group_code = '1683' ";  //高三信聯名卡
		sqlCmd += "where b.ecs_tx_code in ('05','06','25','26') ";
		sqlCmd += " and  b.batch_date like ? "; 
		sqlCmd += " and  substr(b.mcc_code,1,1) <> '9' ";   
		sqlCmd += "order by card_no ";
		
		setString(1,dataMonth+"%");

		String keepCardNo = "";
		String chkTxnType = "";
        String[] arraySkipCode= new String[] 
        		{"f1","G1","G2","d1","M1","b1","e1","V1","V2",
        		 "V3","V4","V5","V6","FL","CL","10","11","12",
        		 "13","14","20","21","22","23","24","25"};

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;
            
            if (totalCnt % 5000 == 0 ) {
                showLogMessage("I", "", "Current Process record=" + totalCnt);
            }

            chkTxnType = getValue("ecs_platform_kind");
            //排除交易踢除
			if (ArrayUtils.contains(arraySkipCode, chkTxnType)) {
				continue;
			}
			
            if (keepCardNo.equals(getValue("card_no")) == false) {
            	if (realCnt == 0) {
            		printHeaderD97A();
            	} else {
            		printDetailD97A(keepCardNo);
            	}
                keepCardNo = getValue("card_no");
                cardAmt = 0;
            }

            if ("-".equals(getValue("ecs_sign_code"))) {
            	cardAmt = cardAmt - getValueDouble("dest_amt");
            } else {
            	cardAmt = cardAmt + getValueDouble("dest_amt");
            }
            
        	realCnt++;
           
        }

        if (realCnt != 0) {
    		printDetailD97A(keepCardNo);
        	printFooterD97A();
            printCRRM18H();
        }
            
    }

    /**
     * @throws Exception 
     * @throws UnsupportedEncodingException *********************************************************************/
    void printCRRM18H() throws UnsupportedEncodingException, Exception {
        pageCntM18H++; 
        
        buf = "";
        buf = comcr.insertStr(buf, "分行代號:  3144信用卡部" ,  1);
        buf = comcr.insertStr(buf, ""              + rptNameM18H             , 35);
        buf = comcr.insertStr(buf, "保存年限: 二年"                             ,97);
        lparM18H.add(comcr.putReport(rptIdM18H, rptNameM18H, sysDate, ++rptSeqM18H, "0", buf));
        
        buf = "";
        String strDate = String.format("%3.3s年%2.2s月%2.2s日", hBusDateTwYear,hBusDateMonth, hBusDateDay);
        buf = comcr.insertStr(buf, "報表代號: CRM18H     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + strDate                      , 46);
        buf = comcr.insertStr(buf, "頁    次:" + String.format("%4d", pageCntM18H) ,97);
        lparM18H.add(comcr.putReport(rptIdM18H, rptNameM18H, sysDate, ++rptSeqM18H, "0", buf));

        buf = "";
        lparM18H.add(comcr.putReport(rptIdM18H, rptNameM18H, sysDate, ++rptSeqM18H, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "高雄三信帳戶             消費總額             0.2% 回饋金", 1);
        lparM18H.add(comcr.putReport(rptIdM18H, rptNameM18H, sysDate, ++rptSeqM18H, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "=";
        lparM18H.add(comcr.putReport(rptIdM18H, rptNameM18H, sysDate, ++rptSeqM18H, "0", buf));
        
        
        StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft("0340701000035", 13));    
        sb.append(comc.fixLeft(" ", 4));
        szTmp = comcr.commFormat("3z,3z,3z,3z", totalAmt);
        sb.append(comc.fixLeft(szTmp, 16));
        sb.append(comc.fixLeft(" ", 11));
        long totalFeedbackAmt = Math.round(totalAmt * 0.002);
        szTmp = comcr.commFormat("z,3z,3z,3z", totalFeedbackAmt);
        sb.append(comc.fixLeft(szTmp, 14));
        
        buf = sb.toString();
        lparM18H.add(comcr.putReport(rptIdM18H, rptNameM18H, sysDate, ++rptSeqM18H, "0", buf));
        
        buf = "";
        lparM18H.add(comcr.putReport(rptIdM18H, rptNameM18H, sysDate, ++rptSeqM18H, "0", buf));

        buf = "";
        lparM18H.add(comcr.putReport(rptIdM18H, rptNameM18H, sysDate, ++rptSeqM18H, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparM18H.add(comcr.putReport(rptIdM18H, rptNameM18H, sysDate, ++rptSeqM18H, "0", buf));

        
        buf = "";
        buf = comcr.insertStr(buf, "說明：新增一般消費金額計算方式 :TXCode:40+42-41 惟需排除各類稅款、規費、學雜費及代收公共事業、富邦壽", 1);
        lparM18H.add(comcr.putReport(rptIdM18H, rptNameM18H, sysDate, ++rptSeqM18H, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "      保險專案及電子票證自動加值等所產生帳款。 ", 1);
        lparM18H.add(comcr.putReport(rptIdM18H, rptNameM18H, sysDate, ++rptSeqM18H, "0", buf));

        comcr.insertPtrBatchRpt(lparM18H);
        //comc.writeReport("/cr/ecs/media/mkt/CRM18H.txt", lparM18H);
    }
    
    /**
     * @throws Exception 
     * @throws UnsupportedEncodingException *********************************************************************/
    void printHeaderD97A() throws UnsupportedEncodingException, Exception {
        pageCnt++;
        
        StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft(String.format("BOF0%s",hBusDateTw), 11));    
        sb.append(comc.fixLeft(" ", 17));
        sb.append(comc.fixLeft(" ", 215)); //補到243byte
        sb.append(comc.fixLeft("\r\n", 2));
        
        buf = sb.toString();

        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
    }

    /**
     * @throws Exception *********************************************************************/
    void printFooterD97A() throws Exception {
    	
        StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft(String.format("EOF%08d",cardCnt), 11));    
        sb.append(comc.fixLeft(" ", 17));
        sb.append(comc.fixLeft(" ", 215)); //補到243byte
        sb.append(comc.fixLeft("\r\n", 2));
        
        buf = sb.toString();

        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length); 

    }

    /***********************************************************************/
    void printDetailD97A(String keepCardNo) throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft("1", 1));            //固定字元
        sb.append(comc.fixLeft(keepCardNo, 16));    //卡號
        if (cardAmt < 0) {
        	sb.append(comc.fixLeft("-", 1));        //sign
        } else {
        	sb.append(comc.fixLeft("+", 1));
        }
        sb.append(comc.fixRight(String.format("%010.0f",  Math.abs(cardAmt)), 10));
        sb.append(comc.fixLeft(" ", 215)); //補到243byte
        sb.append(comc.fixLeft("\r\n", 2));
        
        buf = sb.toString();

        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        totalAmt = totalAmt + cardAmt;
        cardCnt = cardCnt + 1 ;

    }
    
    /***********************************************************************/
    int ftpMput(String filename) throws Exception {
        String procCode = "";

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = javaProgram; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "NCR2TCB"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/mkt/", comc.getECSHOME());
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = "NCR2TCB";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        procCode = "mput " + filename;

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            comcr.errRtn(String.format("%s FTP =[%s]無法連線 error", javaProgram, procCode), "", hCallBatchSeqno);
        }
        return (0);
    }
    
    public static Double doubleMul(Double v1,Double v2){

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.multiply(b2).doubleValue();

	}
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktA260 proc = new MktA260();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
