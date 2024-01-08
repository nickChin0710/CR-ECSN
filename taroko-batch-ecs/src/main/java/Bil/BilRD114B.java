/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/06/07  V1.00.01    JeffKung  program initial                           *
******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*列印公用事業處理明細表*/
public class BilRD114B extends AccessDAO {
    private String progname = "列印長期使用循環信用持卡人轉換機制情形明細表程式  112/11/01  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRD114B";
    String prgmName = "列印長期使用循環信用持卡人轉換機制情形明細表程式";
    
    String rptNameD114A = "長期使用循環信用持卡人轉換機制情形明細表";
    String rptIdD114A = "CRD114";
    int rptSeqD114A = 0;
    List<Map<String, Object>> lparD114A = new ArrayList<Map<String, Object>>();
    
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hIdnoChiName = "";
    String hPrintName = "";
    String hRptName = "";
    String hBusinssDate = "";
    
    CommDate commDate = new CommDate();
    String hBusDateTw = "";
    String hBusDateTwYear = "";
    String hBusDateMonth = "";
    String hBusDateDay = "";
    String sysTwDate = StringUtils.leftPad(commDate.twDate(), 7, "0");

    int totalCnt = 0;
    int indexCnt = 0;
    int pageCnt = 0;

    int listCnt = 0;
    int transCnt = 0;
    
    double totalAmt = 0;
    double totalTransAmt = 0;
    
    int    CRD114ZvalidCardCnt = 0;
    int    CRD114ZaliveCardCnt = 0;
    int    CRD114ZmonthIssCardCnt = 0;
    double CRD114ZtotalMonthAmt = 0;
    
    int lineCnt = 0;

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
            } 
            
            if (chkRunDate(hBusinssDate) != 0) {
            	showLogMessage("I", "", "每個星期日執行, 本日非執行日!!");
        		return 0;
            }
            
            showLogMessage("I", "", "資料日期=[" + hBusinssDate + "]");
            
            //轉換民國年月日
            hBusDateTw = StringUtils.leftPad(commDate.toTwDate(hBusinssDate), 7, "0");
            hBusDateTwYear = hBusDateTw.substring(0, hBusDateTw.length() - 4);
            hBusDateMonth = hBusDateTw.substring(hBusDateTw.length() - 4).substring(0, 2);
            hBusDateDay = hBusDateTw.substring(hBusDateTw.length() - 2);

            showLogMessage("I", "", "程式開始執行......");
           
            //openFile
            String fileFolder = Paths.get(comc.getECSHOME(), "reports/").toString();
            String datFilePath = Paths.get(fileFolder, "RCRD114.1.TXT").toString();
    		boolean isOpen = openBinaryOutput(datFilePath);
    		if (isOpen == false) {
    			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
    			return -1;
    		}
            
            //一律產出報表
            initCnt();
            showLogMessage("I", "", "產生報表檔......");
            generateReport();
            
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);
            ftpMput("RCRD114.1.TXT");
            
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
    
    public int selectPtrWorkday() throws Exception {
		extendField = "workday.";
    	selectSQL = "";
		daoTable = "ptr_workday";

		int recCnt = selectTable();

		return (recCnt);
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

    //檢查是否為執行日期(0:是,否則:不是)
    int chkRunDate(String hBusindate) {
    	SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat dtFmt = new SimpleDateFormat("yyyyMMdd");
		Calendar date1 = Calendar.getInstance();
		Calendar date2 = Calendar.getInstance();
		Pattern pattern1 = Pattern.compile("[0-9]*");

		if (hBusindate == null || hBusindate.trim().length() == 0 ||
			pattern1.matcher(hBusindate.trim()).matches() == false) {
			showLogMessage("I", "", String.format("輸入值格式錯誤:[%s]",hBusindate));
			return -1;
		}

		try {
			date1.setTime(dtFmt.parse(hBusindate));
		} catch (ParseException ex) {
			showLogMessage("I", "", String.format("日期解析錯誤:[%s]",hBusindate));
			return -1;
		}
		int dayOfWeek = date1.get(Calendar.DAY_OF_WEEK);
		
		//每個星期日執行
		if (dayOfWeek==1) {
			return 0;
		} else {
			return -1;
		}
    }
    
	/***********************************************************************/
	void generateReport() throws Exception {

		String keepRegBankNo = "";

		sqlCmd = " select * ";
		sqlCmd += " from bil_revolver_list ";
		sqlCmd += " where 1=1 ";
		sqlCmd += " and data_date like ? ";
		sqlCmd += " order by reg_bank_no ";

		setString(1, comc.getSubString(hBusinssDate, 0, 6) + "%");

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			totalCnt++;

			if (totalCnt % 5000 == 0 || totalCnt == 1) {
				showLogMessage("I", "", "Current Process record=" + totalCnt);
			}

			if (keepRegBankNo.equals(getValue("reg_bank_no")) == false) {
				if (indexCnt != 0) {
					printFooterD114A();
				}

				selectGenBrn(); // 取分行名稱
				printHeaderD114A();
				keepRegBankNo = getValue("reg_bank_no");
				indexCnt = 0;

			    listCnt = 0;
			    transCnt = 0;
			    totalAmt = 0;
			    totalTransAmt = 0;
			}

			selectCrdIdno();

			listCnt++;
			totalAmt = totalAmt + getValueDouble("revolve_amt");
			
			if ("".equals(getValue("trans_type"))==false) {
				transCnt++;
				totalTransAmt = totalTransAmt + getValueDouble("trans_amt");
			}
			printDetailD114A();
		}

		closeCursor();

		if (indexCnt != 0) {
			printFooterD114A();
		}

	}

    /**
     * @throws Exception 
     * @throws UnsupportedEncodingException *********************************************************************/
    void printHeaderD114A() throws UnsupportedEncodingException, Exception {
        pageCnt++;
        
        buf = "";
        buf = comc.fixLeft(getValue("reg_bank_no"), 10) + comc.fixLeft("CRD114", 16) + comc.fixLeft(hBusDateTw + rptNameD114A, 88) + comc.fixLeft("R", 8);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        buf = "";
        buf = comcr.insertStr(buf, "分行代號: "+getValue("reg_bank_no")+getValue("full_chi_name") ,  1);
        buf = comcr.insertStr(buf, ""              + rptNameD114A             , 50);
        buf = comcr.insertStr(buf, "保存年限: 五年"                         ,100);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
      
        buf = "";
        String strDate = String.format("%3.3s年%2.2s月%2.2s日", hBusDateTwYear,hBusDateMonth, hBusDateDay);
        buf = comcr.insertStr(buf, "報表代號: CRD114     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + strDate                      , 50);
        buf = comcr.insertStr(buf, "頁    次:" + String.format("%4d", pageCnt) ,100);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        buf = "";
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        buf = "";
        buf = comcr.insertStr(buf, "                         （Ｃ）                         （Ｅ）", 1);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        buf = "";
        buf = comcr.insertStr(buf, "正卡ＩＤ   持卡人     全部符合轉換    已轉換   轉換   累積已轉換   行動電話      住家電話     公司電話         原循環  約定利率", 1);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        buf = "";
        buf = comcr.insertStr(buf, "           姓  名     循環信用餘額     日期    方案      金額                                                   利率", 1);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "=";
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
    }

    /**
     * @throws Exception *********************************************************************/
    void printFooterD114A() throws Exception {
    	buf = "";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);

        buf = "";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);

        StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft("", 2));    
        sb.append(comc.fixLeft("總計：", 6));
        sb.append(comc.fixLeft("", 15));
        sb.append(comc.fixRight(comcr.commFormat("3z,3z,3#",  totalAmt), 11));  
        sb.append(comc.fixLeft("", 19));
        sb.append(comc.fixRight(comcr.commFormat("3z,3z,3#",  totalTransAmt), 11));
        buf = sb.toString();
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	buf = "";
    	buf = "符合轉換戶數      已轉換戶數      未轉換戶數      轉換戶數比率       當期符合未轉換循環餘額           轉換金額比率";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
    
    	buf = "";
    	buf = "（Ａ）               （Ｂ）      （Ｆ）＝Ａ—Ｂ        Ｂ／Ａ          （Ｇ）＝ Ｃ—Ｅ                     Ｅ／Ｃ";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        sb = new StringBuffer();
        sb.append(comc.fixLeft("", 1));   
        szTmp = String.format("%3d", listCnt);
        sb.append(comc.fixRight(szTmp, 3));
        sb.append(comc.fixLeft("", 18));
        szTmp = String.format("%3d", transCnt);
        sb.append(comc.fixRight(szTmp, 3));
        sb.append(comc.fixLeft("", 13));
        szTmp = String.format("%3d", (listCnt-transCnt));
        sb.append(comc.fixRight(szTmp, 3));
        sb.append(comc.fixLeft("", 12));
        
        //轉換戶數比率
        double tailTransRate = 0;
        if (listCnt > 0) {
        	tailTransRate = ((double) transCnt)*100/ ((double) listCnt);
        } else {
        	tailTransRate = 0;
        }
        sb.append(comc.fixRight(String.format("%.2f",  tailTransRate), 5)+"%"); 

        sb.append(comc.fixLeft(" ", 13));
        
        sb.append(comc.fixRight(comcr.commFormat("3z,3z,3#",  (totalAmt - totalTransAmt)), 11));  
        sb.append(comc.fixLeft("", 21));
        
        //轉換金額比率
        double tailTransAmtRate = 0;
        if (totalAmt > 0) {
        	tailTransAmtRate = totalTransAmt*100/totalAmt;
        } else {
        	tailTransAmtRate = 0;
        }
        sb.append(comc.fixRight(String.format("%.2f",  tailTransAmtRate), 5)+"%"); 
        
        buf = sb.toString();
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);

        
    	buf = "";
    	buf = "說明：1.轉換方案: A:信用卡分期  B:信用卡貸款";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	buf = "";
    	buf = "      2.若同一正卡ＩＤ名下有２張以上卡片以最近一次開戶分行歸屬分行";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    }

    /***********************************************************************/
    void printDetailD114A() throws Exception {
        lineCnt++;
        indexCnt++;
        
        StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft(getValue("crdidno.id_no"), 10));    //正卡持卡人身份證字號
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixLeft(getValue("crdidno.chi_name"),10 )); //持卡人姓名
        sb.append(comc.fixLeft(" ", 2));  //空白分隔
        sb.append(comc.fixRight(comcr.commFormat("3z,3z,3#",  getValueDouble("revolve_amt")), 11));  //循環信用餘額
        sb.append(comc.fixLeft(" ", 3));  //空白分隔
        sb.append(comc.fixLeft(getValue("trans_date"), 8));   //轉換日期
        sb.append(comc.fixLeft(" ", 3));  //空白分隔
        sb.append(comc.fixLeft(getValue("trans_type"), 1));   //轉換方案
        sb.append(comc.fixLeft(" ", 4));  //空白分隔
        sb.append(comc.fixRight(comcr.commFormat("3z,3z,3#",  getValueDouble("trans_amt")), 11));   //累計轉換金額
        sb.append(comc.fixLeft(" ", 3));  //空白分隔
        sb.append(comc.fixLeft(getValue("crdidno.cellar_phone"), 10));     //手機號碼
        sb.append(comc.fixLeft(" ", 2));  //空白分隔
        sb.append(comc.fixLeft(getValue("crdidno.home_tel"), 12));         //住家電話
        sb.append(comc.fixLeft(" ", 2));  //空白分隔
        sb.append(comc.fixLeft(getValue("crdidno.office_tel"), 12));       //公司電話
        sb.append(comc.fixLeft(" ", 6));  //空白分隔
        sb.append(comc.fixRight(String.format("%.2f",  getValueDouble("revolve_rate")), 5)+"%"); //原循環利率
        sb.append(comc.fixLeft(" ", 3));  //空白分隔
        sb.append(comc.fixRight(String.format("%.2f",  getValueDouble("trans_rate")), 5)+"%"); //約定利率
        
        buf = sb.toString();

        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
    }
    
    /***********************************************************************/
    int ftpMput(String filename) throws Exception {
        String procCode = "";

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = javaProgram; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "BREPORT"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/reports/", comc.getECSHOME());
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = "BREPORT";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        procCode = "mput " + filename;

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            comcr.errRtn(String.format("%s FTP =[%s]無法連線 error", javaProgram, procCode), "", hCallBatchSeqno);
        }
        return (0);
    }
    
    void initCnt() {
    	totalCnt = 0;
        indexCnt = 0;
        pageCnt = 0;
        lineCnt = 0;
    }
    
    public static Double doubleMul(Double v1,Double v2){

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.multiply(b2).doubleValue();

	}
    
    /**********************************************************************/
    void selectGenBrn() throws Exception {
    	
        sqlCmd = "select full_chi_name ";
        sqlCmd += "from gen_brn  ";
        sqlCmd += "where branch = ? ";
        setString(1, getValue("reg_bank_no"));
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValue("full_chi_name","");
        }
    }
   
    /**********************************************************************/
    void selectCrdIdno() throws Exception {
    	
    	extendField="crdidno.";
        sqlCmd =  "select id_no,(home_area_code1||home_tel_no1) as home_tel, ";
        sqlCmd += "       (office_area_code1||office_tel_no1) as office_tel, cellar_phone, ";
        sqlCmd += "       chi_name ";
        sqlCmd += "from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, getValue("id_p_seqno"));
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValue("crdidno.id_no","");
            setValue("crdidno.home_tel","");
            setValue("crdidno.office_tel","");
            setValue("crdidno.cellar_phone","");
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilRD114B proc = new BilRD114B();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
