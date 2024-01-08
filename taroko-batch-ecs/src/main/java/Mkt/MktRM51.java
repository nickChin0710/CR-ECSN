/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/07/29  V1.00.01    JeffKung  program initial                           *
*  112/10/26  V1.00.02    kirin     change sql where introduce_id-> clerk_id   *
*  112/10/30  V1.00.03    kirin     change reg_bank_no -->promote_dept
******************************************************************************/

package Mkt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/*列印當年度推廣信用卡統計表*/
public class MktRM51 extends AccessDAO {
    private String progname = "列印當年度推廣信用卡統計表程式  112/10/30  V1.00.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "MktRM51";
    String prgmName = "列印當年度推廣信用卡統計表程式";
    
    String rptNameM51A = "當年度推廣信用卡統計表";
    String rptIdM51A = "CRM51";
    int rptSeqM51A = 0;
    List<Map<String, Object>> lparM51A = new ArrayList<Map<String, Object>>();
    
    String rptNameM52A = "當年度行員推廣信用卡業績統計表";
    String rptIdM52A = "CRM52A";
    int rptSeqM52A = 0;
    List<Map<String, Object>> lparM52A = new ArrayList<Map<String, Object>>();
    
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hIdnoChiName = "";
    String hPrintName = "";
    String hRptName = "";
    String hBusinssDate = "";
    String hDataYear = "";
    String hDataMonth = "";
    
    CommDate commDate = new CommDate();
    String hBusDateTw = "";
    String hBusDateTwYear = "";
    String hBusDateMonth = "";
    String hBusDateDay = "";
    String sysTwDate = StringUtils.leftPad(commDate.twDate(), 7, "0");

    int totalCnt = 0;
    int indexCntM51 = 0;
    int indexCntM52 = 0;
    int pageCntM51 = 0;
    int pageCntM52 = 0;

    int aliveCardCnt = 0;
    int validCardCnt = 0;
    int validCardCntM51 = 0;
    int exAliveCardCnt = 0;
    int exValidCardCnt = 0;

    
    double totalAmtBal = 0;
    double totalMonthAmt = 0;
    double totalYearAmt = 0;
    
    int    crM52AvalidCardCnt = 0;
    int    crM52AaliveCardCnt = 0;
    int    crM52AmonthIssCardCnt = 0;
    int    crM52IdCnt = 0;
    double crM52AtotalMonthAmt = 0;
    
    double bilBillMonthAmt = 0;
    double bilBillYearAmt = 0;
    double bilContractMonthAmt = 0;
    double bilContractYearAmt = 0;
    
    int lineCntM51 = 0;
    int lineCntM52 = 0;
    
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
            	showLogMessage("I", "", "參數日期=[" + hBusinssDate + "]");
            } else {
            	if (!"01".equals(comc.getSubString(hBusinssDate,6))) {
            		showLogMessage("I", "", "每月01日執行, 本日非執行日!!");
            		return 0;
            	}
            }

            hDataYear = comc.getSubString(comm.lastMonth(hBusinssDate, 1),0,4);
            hDataMonth = comc.getSubString(comm.lastMonth(hBusinssDate, 1),0,6);
            showLogMessage("I", "", "資料年度=[" + hDataYear + "],資料年月=["+ hDataMonth + "]");
            rptNameM51A = String.format("當年度(%d)推廣信用卡統計表",comc.str2int(hDataYear)-1911); //轉民國年
            
            
            //轉換民國年月日
            hBusDateTw = StringUtils.leftPad(commDate.toTwDate(hBusinssDate), 7, "0");
            hBusDateTwYear = hBusDateTw.substring(0, hBusDateTw.length() - 4);
            hBusDateMonth = hBusDateTw.substring(hBusDateTw.length() - 4).substring(0, 2);
            hBusDateDay = hBusDateTw.substring(hBusDateTw.length() - 2);

            showLogMessage("I", "", "程式開始執行......");
            
            //openFile
            String fileFolder = Paths.get(comc.getECSHOME(), "reports/").toString();
            String datFilePath = Paths.get(fileFolder, "RCRM51.1.TXT").toString();
    		boolean isOpen = openBinaryOutput(datFilePath);
    		if (isOpen == false) {
    			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
    			return -1;
    		}
            
            selectCrdCard();  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCntM51);
            ftpMput("RCRM51.1.TXT");
            
    		//comcr.insertPtrBatchRpt(lparM52A);
    		String filename = String.format("%s/reports/RCRM52A.1.TXT", comc.getECSHOME(), sysDate);
    		comc.writeReport(filename, lparM52A);
    		ftpMput("RCRM52A.1.TXT");
        
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
    void selectCrdCard() throws Exception {

    	sqlCmd =  "select a.reg_bank_no,a.promote_dept,a.card_no,a.id_p_seqno,a.p_seqno,a.ori_issue_date,a.current_code, ";
    	sqlCmd += " a.group_code, a.major_id_p_seqno, a.acno_p_seqno, a.card_type, a.last_consume_date, ";
    	sqlCmd += " (select nvl(chi_name,'') from crd_idno where id_p_seqno = a.id_p_seqno) as chi_name, ";
//    	sqlCmd += " a.introduce_id, b.chi_name as introduce_chi_name, acct_type, corp_p_seqno";
    	sqlCmd += " a.clerk_id, b.chi_name as introduce_chi_name, acct_type, corp_p_seqno";
    	sqlCmd += " from crd_card a , crd_employee b ";
		sqlCmd += "where 1=1 ";
		sqlCmd += " and a.ori_issue_date like ? ";
		sqlCmd += " and a.sup_flag  = '0' ";
		sqlCmd += " and a.promote_dept  <> '' ";
//		sqlCmd += " and a.reg_bank_no  <> '' ";
		sqlCmd += " and a.clerk_id <> '' ";
		sqlCmd += " and a.clerk_id = b.id  ";
//		sqlCmd += " and a.introduce_id <> '' ";
//		sqlCmd += " and a.introduce_id = b.id  ";
//		sqlCmd += "order by a.reg_bank_no, a.introduce_id ";
		sqlCmd += "order by a.promote_dept, a.clerk_id ";

		setString(1, hDataYear+"%");
		
//		String keepRegBankNo = "";
		String keepPromoteDept = "";
//		String keepIntroduceId = "";
		String keepClerkId = "";
		String keepIntroduceName = "";
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;
            
            if (totalCnt % 5000 == 0 ) {
                showLogMessage("I", "", "Current Process record=" + totalCnt);
            }

            
/*
            bilBillMonthAmt = 0;
        	bilBillYearAmt = 0;
            setValue("bilbill.card_no",getValue("card_no"));
            int billCnt = getLoadData("bilbill.card_no");
            
            //debug
            //showLogMessage("I", "", "card_no=" + getValue("card_no"));
            //showLogMessage("I", "", "bil_card_no=" + getValue("bilbill.card_no"));
            //showLogMessage("I", "", "billCnt=" + billCnt);
            //showLogMessage("I", "", "month_post_amt=" + getValueDouble("bilbill.month_post_amt",0));
            
            for (int i=0; i<billCnt ; i++) {
        		bilBillYearAmt += getValueDouble("bilbill.month_post_amt",i);
        		if (getValue("bilbill.post_month",i).equals(comc.getSubString(hBusinssDate,0,6))) {
        			bilBillMonthAmt += getValueDouble("bilbill.month_post_amt",i);
        		}
        	}
            
            bilContractMonthAmt = 0;
        	bilContractYearAmt = 0;
        	setValue("bilcontract.card_no",getValue("card_no"));
            int billContractCnt = getLoadData("bilcontract.card_no");
            for (int j=0; j<billContractCnt ; j++) {
            		bilContractYearAmt += getValueDouble("bilcontract.month_post_amt",j);
            		if (getValue("bilcontract.post_month",j).equals(comc.getSubString(hBusinssDate,0,6))) {
            			bilContractMonthAmt += getValueDouble("bilcontract.month_post_amt",j);
            		}
            }
            
            double yearPurchaseAmt = bilBillYearAmt + bilContractYearAmt;
            
            //停卡不列印且年度無消費
            if ("0".equals(getValue("current_code")) == false && yearPurchaseAmt == 0 ) 
            	continue;
*/            
//          if (keepRegBankNo.equals(getValue("reg_bank_no")) == false) {
            if (keepPromoteDept.equals(getValue("promote_dept")) == false) {	
            	if (indexCntM51 != 0) {

            		printDetailM52A(keepIntroduceName);
            		printFooterM51A();
                    printFooterM52A();
            	}
            	
            	selectGenBrn();  //取分行名稱
                printHeaderM51A();
        		printHeaderM52A();

//              keepRegBankNo = getValue("reg_bank_no");
        		keepPromoteDept = getValue("promote_dept");
//              keepIntroduceId = getValue("introduce_id");
                keepClerkId = getValue("clerk_id");
                keepIntroduceName = getValue("introduce_chi_name");
                indexCntM51 = 0;
                indexCntM52 = 0;
                lineCntM51 = 0;
                lineCntM52 = 0;

                aliveCardCnt = 0;
                validCardCnt = 0;
                validCardCntM51 = 0;
                exAliveCardCnt = 0;
                exValidCardCnt = 0;
                totalYearAmt = 0;
            }

//          if (keepIntroduceId.equals(getValue("introduce_id")) == false) {
            if (keepClerkId.equals(getValue("clerk_id")) == false) {    
            	printDetailM52A(keepIntroduceName);

//            	keepIntroduceId = getValue("introduce_id");
            	keepClerkId = getValue("clerk_id");
                keepIntroduceName = getValue("introduce_chi_name");

                aliveCardCnt = 0;
                validCardCnt = 0;
                exAliveCardCnt = 0;
                exValidCardCnt = 0;
                totalYearAmt = 0;

            }

            if ("01".equals(getValue("acct_type"))==false) {
            	selectCrdCorp();
            }
            selectCrdIdno();
            selectBilBill();
            selectBilContract();
            selectNewNewCard();   //檢查全新戶
            selectGiftFlag();   //檢查全新戶

            //統計卡數
            if ("0".equals(getValue("current_code"))) {
            	aliveCardCnt ++;
//            	if (getValue("introduce_id").equals(getValue("crdidno.id_no"))==false) {
            	if (getValue("keepClerkId").equals(getValue("crdidno.id_no"))==false) {	
    				exAliveCardCnt++;
    			}
            }
            
            printDetailM51A();

		}

        if (indexCntM51 != 0) {
        	printFooterM51A();
            printDetailM52A(keepIntroduceName);
        	printFooterM52A();

        }
            
    }

    /**
     * @throws Exception 
     * @throws UnsupportedEncodingException *********************************************************************/
    void printHeaderM52A() throws UnsupportedEncodingException, Exception {
        pageCntM52++; 

        buf = "";
        buf = comc.fixLeft(getValue("reg_bank_no"), 10) + comc.fixLeft("CRM52A", 16) + comc.fixLeft(hBusDateTw + rptNameM52A, 88) + comc.fixLeft("R", 8);
        lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "分行代號: "+getValue("reg_bank_no")+getValue("full_chi_name") ,  1);
        buf = comcr.insertStr(buf, ""              + rptNameM52A             , 50);
        buf = comcr.insertStr(buf, "保存年限: 一年"                         ,100);
        lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));

        buf = "";
        String strDate = String.format("%3.3s年%2.2s月%2.2s日", hBusDateTwYear,hBusDateMonth, hBusDateDay);
        buf = comcr.insertStr(buf, "報表代號: CRM52A    科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + strDate                      , 50);
        buf = comcr.insertStr(buf, "頁    次:" + String.format("%4d", pageCntM52) ,100);
        lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));

        buf = "";
        lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "推廣員工         推廣流通卡數        推廣有效卡數         推廣流通卡數        推廣有效卡數      本年度推卡之累計簽帳金額 ", 1);
        lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "姓名              (含員工卡)          (含員工卡)          (不含員工卡)        (不含員工卡)      ", 1);
        lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "=";
        lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));

    }
    
    /**
     * @throws UnsupportedEncodingException *********************************************************************/
    void printFooterM52A() throws UnsupportedEncodingException {
    	buf = "";
        lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));
        
    	buf = "";
    	lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));
    	
    	StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft("      總計：", 12));                    
        sb.append(comc.fixLeft(" ", 3));  //空白分隔
        szTmp = String.format("%,5d筆", lineCntM52);
        sb.append(comc.fixRight(szTmp, 8));
        buf = sb.toString();
    	lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));

    	buf = "";
    	lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));
    	
    	buf = "";
    	buf = "  １．本表列示本年度新申請之個人及法人卡。";
    	lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));
    
    	buf = "";
    	buf = "  ２．卡數統計不含控管碼不為空者。";
    	lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));
    	
    }

    /***********************************************************************/
    void printDetailM52A(String keepIntroduceName) throws Exception {
    	lineCntM52++;
        indexCntM52++;
        
        StringBuffer sb = new StringBuffer();
        
        sb.append(comc.fixLeft(keepIntroduceName, 10));                    //分行代號
        sb.append(comc.fixLeft(" ", 10));  //空白分隔
        szTmp = String.format("%5d", aliveCardCnt);
        sb.append(comc.fixRight(szTmp, 5));
        sb.append(comc.fixLeft(" ", 15));  //空白分隔
        szTmp = String.format("%5d", validCardCnt);
        sb.append(comc.fixRight(szTmp, 5));
        sb.append(comc.fixLeft(" ", 17));  //空白分隔
        szTmp = String.format("%5d", exAliveCardCnt);
        sb.append(comc.fixRight(szTmp, 5));
        sb.append(comc.fixLeft(" ", 15));  //空白分隔
        szTmp = String.format("%5d", exValidCardCnt);
        sb.append(comc.fixRight(szTmp, 5));
        sb.append(comc.fixLeft(" ", 14));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", totalYearAmt);
        sb.append(comc.fixRight(szTmp, 14));
        buf = sb.toString();

    	lparM52A.add(comcr.putReport(rptIdM52A, rptNameM52A, sysDate, ++rptSeqM52A, "0", buf));

    }

    /**
     * @throws Exception 
     * @throws UnsupportedEncodingException *********************************************************************/
    void printHeaderM51A() throws UnsupportedEncodingException, Exception {
        pageCntM51++;
        
        buf = "";
        buf = comc.fixLeft(getValue("reg_bank_no"), 10) + comc.fixLeft("CRM51", 16) + comc.fixLeft(hBusDateTw + rptNameM51A, 88) + comc.fixLeft("R", 8);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "分行代號: "+getValue("reg_bank_no")+getValue("full_chi_name") ,  1);
        buf = comcr.insertStr(buf, ""              + rptNameM51A             , 50);
        buf = comcr.insertStr(buf, "保存年限: 一年"                         ,100);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
      
        //lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));
        
        buf = "";
        String strDate = String.format("%3.3s年%2.2s月%2.2s日", hBusDateTwYear,hBusDateMonth, hBusDateDay);
        buf = comcr.insertStr(buf, "報表代號: CRM51     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + strDate                      , 50);
        buf = comcr.insertStr(buf, "頁    次:" + String.format("%4d", pageCntM51) ,100);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));

        buf = "";
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "推廣人    正卡身分證     持卡人        卡  號 申請  最後   本    月   本 年 度 有 控 全                                       首刷", 1);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "姓  名    ／公司統編     姓  名   卡別 末六碼 日期  交易   簽帳金額   簽帳金額 效 管 新 住家電話     公司電話     手機號碼    達標", 1);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "=";
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));
    }

    /**
     * @throws Exception *********************************************************************/
    void printFooterM51A() throws Exception {
    	buf = "";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
    	//lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));
        
    	buf = "";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
    	//lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));
    	
    	StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft("", 7));    
        sb.append(comc.fixLeft("合計：", 6));
        szTmp = String.format("%,6d筆", lineCntM51);
        sb.append(comc.fixRight(szTmp, 9));
        sb.append(comc.fixLeft("", 15));
        sb.append(comc.fixLeft("有效卡數：", 12));
        szTmp = String.format("%,6d筆", validCardCntM51);
        sb.append(comc.fixRight(szTmp, 9));
        buf = sb.toString();

        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	//lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));
    	
    	buf = "";
    	buf = "備註：１．有效卡註記:＊係最近半年內實際有交易且未辦理停用或強制停卡者，即控管碼為空之持卡人。";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	//lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));
    
    	buf = "";
    	buf = "      ２．本表列示本年度申辦之個人卡及法人卡，不含ＶＩＳＡ金融卡。";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	//lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));

    	buf = "";
    	buf = "      ３．控管碼：１＝申請停用；２：掛失停用；３：強制停用；４，５：偽冒停用；７：到期不續卡。";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	//lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));

    	buf = "";
    	buf = "      ４．個人卡首刷禮（不含頂級卡、漢來卡及iCash卡)說明:";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	//lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));

        buf = "";
    	buf = "          全新卡45天內累積一般消費達2999元(連接電子票證者需自動加值1次)，可獲得刷卡金３００元。";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    }

    /***********************************************************************/
    void printDetailM51A() throws Exception {
    	lineCntM51++;
        indexCntM51++;
        
        StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft(getValue("introduce_chi_name"), 8));    //推廣員姓名
        sb.append(comc.fixLeft(" ", 2));  //空白分隔
        if ("01".equals(getValue("acct_type"))==false) {
            sb.append(comc.fixLeft(getValue("crdcorp.corp_no"), 8));        //統編
            sb.append(comc.fixLeft(getValue("crdcorp.chi_name"), 6));       //公司名
            sb.append(comc.fixLeft(" ", 1));  //空白分隔
        } else {
            sb.append(comc.fixLeft(getValue("crdidno.id_no"), 10));        //正卡持卡人身份證字號
            sb.append(comc.fixLeft(" ", 5));  //空白分隔
        }
        sb.append(comc.fixLeft(getValue("chi_name"),8 ));              //持卡人姓名
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixLeft(getValue("group_code"), 4));            //團代(卡別)
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixLeft(comc.getSubString(getValue("card_no"), 10), 6));  //卡號末6碼
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        String strMMDD = "";
        int intMM = comc.str2int(comc.getSubString(getValue("ori_issue_date"),4,6));
        int intDD = comc.str2int(comc.getSubString(getValue("ori_issue_date"),6,8));
       	strMMDD = String.format("%02d/%02d", intMM, intDD);
        sb.append(comc.fixLeft(strMMDD, 5));         //申請日期
        sb.append(comc.fixLeft(" ", 1));  //空白分隔

        intMM = comc.str2int(comc.getSubString(getValue("last_consume_date"),4,6));
        intDD = comc.str2int(comc.getSubString(getValue("last_consume_date"),6,8));
       	strMMDD = String.format("%02d/%02d", intMM, intDD);
        sb.append(comc.fixLeft(strMMDD, 5));         //最後交易
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        
        sb.append(comc.fixRight(String.format("%.0f",  bilBillMonthAmt+bilContractMonthAmt), 9));     //本月簽帳金額
        sb.append(comc.fixLeft(" ", 2));  //空白分隔
        sb.append(comc.fixRight(String.format("%.0f",  bilBillYearAmt+bilContractYearAmt), 9));       //本年度簽帳金額
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        
        totalAmtBal   += getValueDouble("acctcurr.ttl_amt_bal");
        totalMonthAmt += (bilBillMonthAmt+bilContractMonthAmt);
        totalYearAmt  += (bilBillYearAmt+bilContractYearAmt);
        
        //--判斷流通卡且6個月內有無消費
        String consumeFlag = "";
		if("0".equals(getValue("current_code"))==true 
		   && commDate.monthsBetween(hBusinssDate, getValue("last_consume_date")) <= 6) {
			consumeFlag = "＊";
			validCardCnt++;
			validCardCntM51++;
//			if (getValue("introduce_id").equals(getValue("crdidno.id_no"))==false) {
			if (getValue("clerk_id").equals(getValue("crdidno.id_no"))==false) {
				exValidCardCnt++;
			}
				
		}
        sb.append(comc.fixLeft(consumeFlag, 2));     //有效卡註記
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        String blockCode = "";
        if ("0".equals(getValue("current_code"))==false) {
        	blockCode = getValue("current_code");
        }
        sb.append(comc.fixLeft(blockCode, 2));     //控管碼
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixLeft(getValue("newnew.new_new_flag"), 2));     //全新戶註記
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
       	sb.append(comc.fixLeft(getValue("crdidno.home_tel"), 12));       //電話號碼
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
       	sb.append(comc.fixLeft(getValue("crdidno.office_tel"), 12));     //電話號碼
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixLeft(getValue("crdidno.cellar_phone"), 10));   //手機號碼
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixLeft(getValue("gift.gift_flag"), 2));          //首刷達標
        
        //debug
        //if (getValue("gift.gift_flag").length() > 0 ) {
        //	showLogMessage("I","","gift_flag=" + getValue("crdidno.id_no"));
        //}
        
        buf = sb.toString();

        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparM51A.add(comcr.putReport(rptIdM51A, rptNameM51A, sysDate, ++rptSeqM51A, "0", buf));

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
        sqlCmd =  "select id_no,(replace(home_area_code1,'-','')||'-'||home_tel_no1) as home_tel, ";
        sqlCmd += "       (replace(office_area_code1,'-','')||'-'||office_tel_no1) as office_tel, cellar_phone ";
        sqlCmd += "from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, getValue("major_id_p_seqno"));
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValue("crdidno.id_no","");
            setValue("crdidno.home_tel","");
            setValue("crdidno.office_tel","");
            setValue("crdidno.cellar_phone","");
        }
    }

    /**********************************************************************/
    void selectCrdCorp() throws Exception {
    	
    	extendField="crdcorp.";
        sqlCmd =  "select corp_no,chi_name ";
        sqlCmd += "from crd_corp  ";
        sqlCmd += "where corp_p_seqno = ? ";
        setString(1, getValue("corp_p_seqno"));
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValue("crdcorp.corp_no","");
            setValue("crdcorp.chi_name","");
        }
    }

    /**********************************************************************/
    void selectNewNewCard() throws Exception {
    	
    	extendField="newnew.";
        sqlCmd =  "select max(oppost_date) as oppost_date, sum(decode(current_code,'0',1,0)) as alive_cnt ";
        sqlCmd += "from crd_card  ";
        sqlCmd += "where major_id_p_seqno = ? and sup_flag='0' and acct_type='01' ";
        sqlCmd += "and ori_issue_date < ?";
        setString(1, getValue("major_id_p_seqno"));
        setString(2, getValue("ori_issue_date"));
        int tmpInt = selectTable();
        int months = 0;
        if (tmpInt == 0) {
            setValue("newnew.new_new_flag","＊");
            return;
        } 
        
        //有別的流通卡
        if (getValueInt("newnew.alive_cnt") > 0) {
        	setValue("newnew.new_new_flag","");
        	return;
        } 
        
        //停卡期間是否超過6個月
       	if (getValue("newnew.oppost_date").length() == 8) {
        	if (getValue("newnew.oppost_date").compareTo(getValue("ori_issue_date")) > 0)  {
        		setValue("newnew.new_new_flag","");
        	} else {
        		months  = monthsBetween(getValue("newnew.oppost_date"),getValue("ori_issue_date"));
        		if (months > 6) {
        			setValue("newnew.new_new_flag","＊");
        		} else {
        			setValue("newnew.new_new_flag","");
        		}
        	}
        } else {
        	setValue("newnew.new_new_flag","");
        }
        		
    }

    /**********************************************************************/
    void selectGiftFlag() throws Exception {
    	
    	extendField="gift.";
        sqlCmd =  "select '＊' as gift_flag ";
        sqlCmd += "from mkt_fstp_carddtl_h ";  //cardlink的首刷禮名單
        sqlCmd += "where id_p_seqno = ?  ";
        sqlCmd += "and feedback_date_other > ?";
        setString(1, getValue("major_id_p_seqno"));
        setString(2, getValue("ori_issue_date"));
        int tmpInt = selectTable();
        int months = 0;
        if (tmpInt == 0) {
            setValue("gift.gift_flag","");
        } 
        
    }

    /**********************************************************************/
    void selectBilBill() throws Exception {
    	
    	bilBillMonthAmt = 0;
    	bilBillYearAmt = 0;
    	
    	extendField="bilbill.";
        sqlCmd =  "select substr(post_date,1,6) AS post_month, nvl(sum(decode(sign_flag,'+',dest_amt,dest_amt*-1)),0) as month_post_amt ";
        sqlCmd += "from bil_bill ";
        sqlCmd += "where card_no = ? ";
        sqlCmd += "and   acct_code in ('BL','CA','AO','ID','OT') ";
        sqlCmd += "and   post_date like ? ";
        sqlCmd += "group by substr(post_date,1,6) ";
        setString(1, getValue("card_no"));
        setString(2, hDataYear+"%");
        int tmpInt = selectTable();
        if (tmpInt > 0) {
        	for (int i=0; i<tmpInt ; i++) {
        		bilBillYearAmt += getValueDouble("bilbill.month_post_amt",i);
        		if (getValue("bilbill.post_month",i).equals(hDataMonth)) {
        			bilBillMonthAmt += getValueDouble("bilbill.month_post_amt",i);
        		}
        	}
        }
    }
    
    /**********************************************************************/
    void selectBilContract() throws Exception {
    	
    	bilContractMonthAmt = 0;
    	bilContractYearAmt = 0;
    	
    	extendField="bilcontract.";
    	sqlCmd =  "select substr(first_post_date,1,6) AS post_month, ";
        sqlCmd += "       nvl(sum(tot_amt),0) as month_post_amt  ";
        sqlCmd += "from bil_contract ";
        sqlCmd += "where card_no = ? ";
        sqlCmd += "and   first_post_date like ? ";
        sqlCmd += "and   contract_kind   = '1' ";
        sqlCmd += "and   mcht_no not in ('106000000005','106000000007') ";
        sqlCmd += "and   installment_kind <> 'N' ";
        sqlCmd += "group by substr(first_post_date,1,6) ";
        setString(1, getValue("card_no"));
        setString(2, hDataYear+"%");
        int tmpInt = selectTable();
        if (tmpInt > 0) {
        	for (int i=0; i<tmpInt ; i++) {
        		bilContractYearAmt += getValueDouble("bilcontract.month_post_amt",i);
        		if (getValue("bilcontract.post_month",i).equals(hDataMonth)) {
        			bilContractMonthAmt += getValueDouble("bilcontract.month_post_amt",i);
        		}
        	}
        }
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktRM51 proc = new MktRM51();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
