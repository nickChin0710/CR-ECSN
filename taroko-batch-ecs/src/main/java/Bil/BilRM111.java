/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/06/28  V1.00.01    JeffKung  program initial                           *
******************************************************************************/

package Bil;

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

/*本行各單位商務卡使用情形月報表*/
public class BilRM111 extends AccessDAO {
    private String progname = "列印本行各單位商務卡使用情形月報表程式  112/06/28 V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRM111";
    String prgmName = "列印本行各單位商務卡使用情形月報表程式";
    
    String rptNameM111Z = "本行各單位商務卡使用情形月報表";
    String rptIdM111Z = "CRM111";
    int rptSeqM111Z = 0;
    int pageCntM111Z = 0;
    List<Map<String, Object>> lparM111Z = new ArrayList<Map<String, Object>>();
    
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

    int aliveCardCnt = 0;
    int monthIssCardCnt = 0;
    int validCardCnt = 0;
    
    double totalAmtBal = 0;
    double totalMonthAmt = 0;
    double totalYearAmt = 0;
    
    int    crM111ZvalidCardCnt = 0;
    int    crM111ZaliveCardCnt = 0;
    int    crM111ZmonthIssCardCnt = 0;
    double crM111ZtotalMonthAmt = 0;
    double crM111ZtotalYearAmt = 0;
    double crM111ZtotalEMonthAmt = 0;
    double crM111ZtotalEYearAmt = 0;
    
    double bilBillMonthAmt = 0;
    double bilBillYearAmt = 0;
    double bilBillEMonthAmt = 0;
    double bilBillEYearAmt = 0;
    
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
            	if (!"26".equals(comc.getSubString(hBusinssDate,6))) {
            		showLogMessage("I", "", "每月26日執行, 本日非執行日!!");
            		return 0;
            	}
            }
            
            showLogMessage("I", "", "資料日期=[" + hBusinssDate + "]");
            
            //轉換民國年月日
            hBusDateTw = StringUtils.leftPad(commDate.toTwDate(hBusinssDate), 7, "0");
            hBusDateTwYear = hBusDateTw.substring(0, hBusDateTw.length() - 4);
            hBusDateMonth = hBusDateTw.substring(hBusDateTw.length() - 4).substring(0, 2);
            hBusDateDay = hBusDateTw.substring(hBusDateTw.length() - 2);

            showLogMessage("I", "", "程式開始執行......");
            initCnt();
    		
    		//統計報表的表頭
    		printHeaderM111Z();
            
            selectCrdCard();  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCntM111Z);

            //統計報表的表尾
    		printFooterM111Z();
    		comcr.insertPtrBatchRpt(lparM111Z);

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

    	sqlCmd  = "SELECT b.REG_BANK_NO,a.card_no,nvl(a.acct_month,'') AS acct_month, ";
    	sqlCmd += "       nvl(a.dest_amt,0) AS dest_amt, ";
    	sqlCmd += "       nvl(a.txn_code,'') AS txn_code, nvl(a.acct_code,'') AS acct_code ";
    	sqlCmd += "FROM (SELECT d.wf_desc AS reg_bank_no, d.wf_id AS corp_no, nvl(c.corp_p_seqno,'XXXXXXXXXX') AS corp_p_seqno ";
    	sqlCmd += "      FROM ptr_sys_idtab d ";
    	sqlCmd += "      LEFT JOIN crd_corp c ON c.corp_no = d.wf_id ";
    	sqlCmd += "      WHERE d.wf_type = 'CRM111' ORDER BY d.wf_desc ) b ";
    	sqlCmd += "LEFT JOIN ";
    	sqlCmd += "     (select crd.corp_p_seqno,crd.card_no,nvl(bil.acct_month,'') AS acct_month, ";
    	sqlCmd += "             decode(nvl(bil.sign_flag,'+'),'+',nvl(bil.dest_amt,0), nvl(bil.dest_amt,0)*-1) AS dest_amt, ";
    	sqlCmd += "             nvl(bil.txn_code,'') AS txn_code, nvl(bil.acct_code,'') AS acct_code ";
    	sqlCmd += "      from   crd_card crd, bil_bill bil ";
    	sqlCmd += "      where  crd.corp_p_seqno in ";
    	sqlCmd += "	        (SELECT f.corp_p_seqno ";
    	sqlCmd += "              FROM ptr_sys_idtab g,crd_corp f ";
    	sqlCmd += "              WHERE f.corp_no = g.wf_id ";
    	sqlCmd += "              AND   g.wf_type = 'CRM111')  ";
    	sqlCmd += "      AND    crd.card_no = bil.card_no ";
    	sqlCmd += "      AND    bil.acct_month >= ? AND bil.BILLED_FLAG='B' ";
    	sqlCmd += "      AND    bil.acct_type IN ('03','06','05') ";
    	sqlCmd += "      AND    bil.acct_code IN ('BL','IT') ) a ";
    	sqlCmd += "	  on a.corp_p_seqno = b.corp_p_seqno ";
    	sqlCmd += "ORDER BY b.REG_BANK_NO ";

    	/*
    	if ("02".compareTo(comc.getSubString(hBusinssDate, 4,6)) >=0 ) {
    		setString(1,(comc.getSubString(hBusinssDate, 0,4)+"02"));  //取當年度2月為切點
    	} else {
    		setString(1,(comc.getSubString(comm.lastMonth(hBusinssDate, -1), 0,4)+"02")); //取前年度2月為切點
    	}
    	*/
    	
		setString(1,(comc.getSubString(hBusinssDate, 0,4)+"01"));  //取當年

		String keepRegBankNo = "";
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;
            
            if (totalCnt % 5000 == 0 ) {
                showLogMessage("I", "", "Current Process record=" + totalCnt);
            }
            
            if (keepRegBankNo.equals(getValue("reg_bank_no")) == false) {
            	if (indexCnt != 0) {
                    printDetailM111Z(keepRegBankNo);
            	}
            	
            	selectGenBrn();  //取分行名稱
                keepRegBankNo = getValue("reg_bank_no");
                indexCnt = 0;
                
                bilBillMonthAmt = 0;
            	bilBillYearAmt = 0;
            	bilBillEMonthAmt = 0;
            	bilBillEYearAmt = 0;
            }
            
            indexCnt++;
            
            //含總行
            if ("".equals(getValue("reg_bank_no")) || "0000".equals(getValue("reg_bank_no"))) {
            	//當月消費
            	if (comc.getSubString(hBusinssDate, 0,6).equals(comc.getSubString(getValue("acct_month"),0,6))) {
            		bilBillMonthAmt += getValueDouble("dest_amt");
            	}
            	
            	//年度消費
            	bilBillYearAmt += getValueDouble("dest_amt");
            	
            } else {
            	//當月消費
            	if (comc.getSubString(hBusinssDate, 0,6).equals(comc.getSubString(getValue("acct_month"),0,6))) {
            		bilBillMonthAmt += getValueDouble("dest_amt");
            		bilBillEMonthAmt += getValueDouble("dest_amt");
            	}
            	
            	//年度消費
            	bilBillYearAmt += getValueDouble("dest_amt");
            	bilBillEYearAmt += getValueDouble("dest_amt");
            }
        }

        if (indexCnt != 0) {
            printDetailM111Z(keepRegBankNo);
        }
            
    }

    /**
     * @throws Exception 
     * @throws UnsupportedEncodingException *********************************************************************/
    void printHeaderM111Z() throws UnsupportedEncodingException, Exception {
        pageCntM111Z++; 
        
        buf = "";
        buf = comcr.insertStr(buf, "分行代號:  3144信用卡部" ,  1);
        buf = comcr.insertStr(buf, ""              + rptNameM111Z             , 35);
        buf = comcr.insertStr(buf, "保存年限: 一年"                             ,67);
        lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));
        
        buf = "";
        String strDate = String.format("%3.3s年%2.2s月%2.2s日", hBusDateTwYear,hBusDateMonth, hBusDateDay);
        buf = comcr.insertStr(buf, "報表代號: CRM111     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + strDate                      , 36);
        buf = comcr.insertStr(buf, "頁    次:" + String.format("%4d", pageCntM111Z) ,67);
        lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));

        buf = "";
        lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "分行代號     分行名稱                 本月消費金額情形     本年度累計消費金額", 1);
        lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));
        

        buf = "";
        for (int i = 0; i < 100; i++)
            buf += "=";
        lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));
    }

    /**
     * @throws UnsupportedEncodingException *********************************************************************/
    void printFooterM111Z() throws UnsupportedEncodingException {
    	
    	long avgMonthAmt =0;
    	long avgYearAmt =0;
    	long avgEMonthAmt =0;
    	long avgEYearAmt =0;
    	
    	if (lineCnt > 0) {
    		avgMonthAmt = Math.round(crM111ZtotalMonthAmt/lineCnt);
    		avgYearAmt = Math.round(crM111ZtotalYearAmt/lineCnt);
    		avgEMonthAmt = Math.round(crM111ZtotalEMonthAmt/lineCnt);
    		avgEYearAmt = Math.round(crM111ZtotalEYearAmt/lineCnt);
    	} else {
    		avgMonthAmt = 0;
    		avgYearAmt = 0;
    		avgEMonthAmt = 0;
    		avgEYearAmt = 0;
    	}
    	
    	buf = "";
        lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));
        
    	buf = "";
    	lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));
    	
    	buf = "";
    	lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));

    	buf = "";
    	lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));

    	buf = "";
    	lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));

    	StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft("總交易金額（含總行）", 40));                    
        szTmp = comcr.commFormat("zz,3z,3z,3#", crM111ZtotalMonthAmt);
        sb.append(comc.fixRight(szTmp, 14));
        sb.append(comc.fixLeft(" ", 9));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", crM111ZtotalYearAmt);
        sb.append(comc.fixRight(szTmp, 14));
        sb.append(comc.fixLeft(" ", 3));  //空白分隔
        
        buf = sb.toString();

    	lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));

    	sb = new StringBuffer();
        sb.append(comc.fixLeft("各單位平均交易金額（含總行）", 40));                    
        szTmp = comcr.commFormat("zz,3z,3z,3#", avgMonthAmt);
        sb.append(comc.fixRight(szTmp, 14));
        sb.append(comc.fixLeft(" ", 9));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", avgYearAmt);
        sb.append(comc.fixRight(szTmp, 14));
        sb.append(comc.fixLeft(" ", 3));  //空白分隔
        
        buf = sb.toString();

    	lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));

    	sb = new StringBuffer();
        sb.append(comc.fixLeft("總交易金額（不含總行）", 40));                    
        szTmp = comcr.commFormat("zz,3z,3z,3#", crM111ZtotalEMonthAmt);
        sb.append(comc.fixRight(szTmp, 14));
        sb.append(comc.fixLeft(" ", 9));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", crM111ZtotalEYearAmt);
        sb.append(comc.fixRight(szTmp, 14));
        sb.append(comc.fixLeft(" ", 3));  //空白分隔
        
        buf = sb.toString();

    	lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));

    	sb = new StringBuffer();
        sb.append(comc.fixLeft("各單位平均交易金額（不含總行）", 40));
        szTmp = comcr.commFormat("zz,3z,3z,3#", avgEMonthAmt);
        sb.append(comc.fixRight(szTmp, 14));
        sb.append(comc.fixLeft(" ", 9));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", avgEYearAmt);
        sb.append(comc.fixRight(szTmp, 14));
        sb.append(comc.fixLeft(" ", 3));  //空白分隔
        
        buf = sb.toString();

    	lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));

    }

    /***********************************************************************/
    void printDetailM111Z(String keepRegBankNo) throws Exception {
        lineCnt++;
        indexCnt++;
        
        StringBuffer sb = new StringBuffer();
        
        sb.append(comc.fixLeft(keepRegBankNo, 4));                    //分行代號
        sb.append(comc.fixLeft(" ", 9));  //空白分隔
        sb.append(comc.fixLeft(getValue("full_chi_name"), 10));    //分行名稱
        sb.append(comc.fixLeft(" ", 17));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", bilBillMonthAmt);
        sb.append(comc.fixRight(szTmp, 14));
        sb.append(comc.fixLeft(" ", 9));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", bilBillYearAmt);
        sb.append(comc.fixRight(szTmp, 14));

        crM111ZtotalMonthAmt += bilBillMonthAmt;
        crM111ZtotalYearAmt += bilBillYearAmt;
        crM111ZtotalEMonthAmt += bilBillEMonthAmt;
        crM111ZtotalEYearAmt += bilBillEYearAmt;
       
        buf = sb.toString();

    	lparM111Z.add(comcr.putReport(rptIdM111Z, rptNameM111Z, sysDate, ++rptSeqM111Z, "0", buf));

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
            if ("0000".equals(getValue("reg_bank_no"))) {
            	setValue("full_chi_name","總行");
            } else {
            	setValue("full_chi_name","");
            }
        }
        
        if ("0000".equals(getValue("reg_bank_no"))) {
        	setValue("full_chi_name","總行");
        }
        
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilRM111 proc = new BilRM111();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
