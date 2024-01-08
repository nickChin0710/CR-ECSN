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

/*列印公用事業處理明細表*/
public class BilRD97 extends AccessDAO {
    private String progname = "列印個人信用卡消費情形日報表程式  112/06/07  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRD97";
    String prgmName = "列印個人信用卡消費情形日報表程式";
    
    String rptNameD97A = "個人信用卡消費情形日報表";
    String rptIdD97A = "CRD97";
    int rptSeqD97A = 0;
    List<Map<String, Object>> lparD97A = new ArrayList<Map<String, Object>>();
    
    String rptNameD97Z = "個人信用卡消費情形統計表";
    String rptIdD97Z = "CRD97Z";
    int rptSeqD97Z = 0;
    int pageCntD97Z = 0;
    List<Map<String, Object>> lparD97Z = new ArrayList<Map<String, Object>>();
    
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
    
    int    crd97ZvalidCardCnt = 0;
    int    crd97ZaliveCardCnt = 0;
    int    crd97ZmonthIssCardCnt = 0;
    double crd97ZtotalMonthAmt = 0;
    
    double bilBillMonthAmt = 0;
    double bilBillYearAmt = 0;
    double bilContractMonthAmt = 0;
    double bilContractYearAmt = 0;
    
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
            	if (!"8".equals(comc.getSubString(hBusinssDate,7))) {
            		showLogMessage("I", "", "每月8,18,28日執行, 本日非執行日!!");
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
            loadGroupCodeName();
            showLogMessage("I", "", "Loading bil_bill......");
            loadBilBill();
            showLogMessage("I", "", "Loading bil_contract......");
            loadBilContract();
            
            //openFile
            String fileFolder = Paths.get(comc.getECSHOME(), "reports/").toString();
            String datFilePath = Paths.get(fileFolder, "RCRD97.1.TXT").toString();
    		boolean isOpen = openBinaryOutput(datFilePath);
    		if (isOpen == false) {
    			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
    			return -1;
    		}
    		
    		//統計報表的表頭
    		printHeaderD97Z();
            
            selectCrdCard();  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);
            ftpMput("RCRD97.1.TXT");
            
            //統計報表的表尾
    		printFooterD97Z();
    		comcr.insertPtrBatchRpt(lparD97Z);
        
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

    	sqlCmd =  "select reg_bank_no,card_no,id_p_seqno,p_seqno,ori_issue_date,current_code, ";
    	sqlCmd += " group_code, major_id_p_seqno, acno_p_seqno, card_type, last_consume_date, ";
    	sqlCmd += " (select nvl(chi_name,'') from crd_idno where id_p_seqno = crd_card.id_p_seqno) as chi_name ";
		sqlCmd += " from crd_card ";
		sqlCmd += "where 1=1 ";
		sqlCmd += " and reg_bank_no  <> '' ";
		sqlCmd += " and acct_type  = '01' ";
		//testing
		//sqlCmd += " and reg_bank_no  in ('0670') ";
		sqlCmd += "order by reg_bank_no ";
		//testing
		//sqlCmd += "fetch first 500 rows only ";

		String keepRegBankNo = "";
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;
            
            if (totalCnt % 5000 == 0 ) {
                showLogMessage("I", "", "Current Process record=" + totalCnt);
            }

            
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
            
            if (keepRegBankNo.equals(getValue("reg_bank_no")) == false) {
            	if (indexCnt != 0) {
                    printFooterD97A();
                    printDetailD97Z(keepRegBankNo);
            	}
            	
            	selectGenBrn();  //取分行名稱
                printHeaderD97A();
                keepRegBankNo = getValue("reg_bank_no");
                indexCnt = 0;
                
                aliveCardCnt = 0;
                monthIssCardCnt = 0;
                validCardCnt = 0;
                totalAmtBal = 0;
                totalMonthAmt = 0;
                totalYearAmt = 0;
            }
            
            //統計卡數
            if ("0".equals(getValue("current_code"))) {
            	aliveCardCnt ++;
            	//當月發卡
            	if (comc.getSubString(hBusinssDate, 0,6).equals(comc.getSubString(getValue("ori_issue_date"),0,6))) {
            		monthIssCardCnt ++;
            	}
            }

            selectPtrGroupCode();
            selectCrdIdno();
            selectActAcno();
            selectActAcctCurr();
            //selectBilBill();
            //selectBilContract();

            printDetailD97A();
        }

        if (indexCnt != 0) {
        	printFooterD97A();
            printDetailD97Z(keepRegBankNo);
        }
            
    }

    /**
     * @throws Exception 
     * @throws UnsupportedEncodingException *********************************************************************/
    void printHeaderD97Z() throws UnsupportedEncodingException, Exception {
        pageCntD97Z++; 
        
        buf = "";
        buf = comcr.insertStr(buf, "分行代號:  3144信用卡部" ,  1);
        buf = comcr.insertStr(buf, ""              + rptNameD97Z             , 35);
        buf = comcr.insertStr(buf, "保存年限: 一年"                             ,67);
        lparD97Z.add(comcr.putReport(rptIdD97Z, rptNameD97Z, sysDate, ++rptSeqD97Z, "0", buf));
        
        buf = "";
        String strDate = String.format("%3.3s年%2.2s月%2.2s日", hBusDateTwYear,hBusDateMonth, hBusDateDay);
        buf = comcr.insertStr(buf, "報表代號: CRD97Z     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + strDate                      , 36);
        buf = comcr.insertStr(buf, "頁    次:" + String.format("%4d", pageCntD97Z) ,67);
        lparD97Z.add(comcr.putReport(rptIdD97Z, rptNameD97Z, sysDate, ++rptSeqD97Z, "0", buf));

        buf = "";
        lparD97Z.add(comcr.putReport(rptIdD97Z, rptNameD97Z, sysDate, ++rptSeqD97Z, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "分行代號     分行名稱    有效卡數     流通卡數    當月發卡數       當月簽帳金額", 1);
        lparD97Z.add(comcr.putReport(rptIdD97Z, rptNameD97Z, sysDate, ++rptSeqD97Z, "0", buf));
        

        buf = "";
        for (int i = 0; i < 100; i++)
            buf += "=";
        lparD97Z.add(comcr.putReport(rptIdD97Z, rptNameD97Z, sysDate, ++rptSeqD97Z, "0", buf));
    }

    /**
     * @throws UnsupportedEncodingException *********************************************************************/
    void printFooterD97Z() throws UnsupportedEncodingException {
    	buf = "";
        lparD97Z.add(comcr.putReport(rptIdD97Z, rptNameD97Z, sysDate, ++rptSeqD97Z, "0", buf));
        
    	buf = "";
    	lparD97Z.add(comcr.putReport(rptIdD97Z, rptNameD97Z, sysDate, ++rptSeqD97Z, "0", buf));
    	
    	StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft("合計：", 10));                    
        sb.append(comc.fixLeft(" ", 3));  //空白分隔
        sb.append(comc.fixLeft("", 10));  
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = String.format("%,7d", crd97ZvalidCardCnt);
        sb.append(comc.fixRight(szTmp, 9));
        sb.append(comc.fixLeft(" ", 4));  //空白分隔
        szTmp = String.format("%,7d", crd97ZaliveCardCnt);
        sb.append(comc.fixRight(szTmp, 9));
        sb.append(comc.fixLeft(" ", 5));  //空白分隔
        szTmp = String.format("%,7d", crd97ZmonthIssCardCnt);
        sb.append(comc.fixRight(szTmp, 9));
        sb.append(comc.fixLeft(" ", 5));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", crd97ZtotalMonthAmt);
        sb.append(comc.fixRight(szTmp, 14));
        
        buf = sb.toString();

    	lparD97Z.add(comcr.putReport(rptIdD97Z, rptNameD97Z, sysDate, ++rptSeqD97Z, "0", buf));

    }

    /***********************************************************************/
    void printDetailD97Z(String keepRegBankNo) throws Exception {
        lineCnt++;
        indexCnt++;
        
        StringBuffer sb = new StringBuffer();
        
        sb.append(comc.fixLeft(keepRegBankNo, 4));                    //分行代號
        sb.append(comc.fixLeft(" ", 9));  //空白分隔
        sb.append(comc.fixLeft(getValue("full_chi_name"), 10));    //分行名稱
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        szTmp = String.format("%,7d", validCardCnt);
        sb.append(comc.fixRight(szTmp, 9));
        sb.append(comc.fixLeft(" ", 4));  //空白分隔
        szTmp = String.format("%,7d", aliveCardCnt);
        sb.append(comc.fixRight(szTmp, 9));
        sb.append(comc.fixLeft(" ", 5));  //空白分隔
        szTmp = String.format("%,7d", monthIssCardCnt);
        sb.append(comc.fixRight(szTmp, 9));
        sb.append(comc.fixLeft(" ", 5));  //空白分隔
        szTmp = comcr.commFormat("zz,3z,3z,3#", totalMonthAmt);
        sb.append(comc.fixRight(szTmp, 14));

        crd97ZvalidCardCnt += validCardCnt;
        crd97ZaliveCardCnt += aliveCardCnt;
        crd97ZmonthIssCardCnt += monthIssCardCnt;
        crd97ZtotalMonthAmt += totalMonthAmt;
       
        buf = sb.toString();

    	lparD97Z.add(comcr.putReport(rptIdD97Z, rptNameD97Z, sysDate, ++rptSeqD97Z, "0", buf));

    }

    /**
     * @throws Exception 
     * @throws UnsupportedEncodingException *********************************************************************/
    void printHeaderD97A() throws UnsupportedEncodingException, Exception {
        pageCnt++;
        
        buf = "";
        buf = comc.fixLeft(getValue("reg_bank_no"), 10) + comc.fixLeft("CRD97", 16) + comc.fixLeft(hBusDateTw + rptNameD97A, 88) + comc.fixLeft("R", 8);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "分行代號: "+getValue("reg_bank_no")+getValue("full_chi_name") ,  1);
        buf = comcr.insertStr(buf, ""              + rptNameD97A             , 50);
        buf = comcr.insertStr(buf, "保存年限: 一個月"                         ,100);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
      
        //lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));
        
        buf = "";
        String strDate = String.format("%3.3s年%2.2s月%2.2s日", hBusDateTwYear,hBusDateMonth, hBusDateDay);
        buf = comcr.insertStr(buf, "報表代號: CRD97     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + strDate                      , 50);
        buf = comcr.insertStr(buf, "頁    次:" + String.format("%4d", pageCnt) ,100);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));

        buf = "";
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "正      卡 持卡人     Ｃ         (末六位) 開戶             本月止帳單    本    月    本 年 度 有 控 委託                       電子", 1);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "身分證字號 姓  名     Ｂ 卡別    卡    號 年份   信用額度    未繳金額    簽帳金額    簽帳金額 效 管 扣繳   電話號碼   手機號碼 帳單", 1);
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "=";
        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));
    }

    /**
     * @throws Exception *********************************************************************/
    void printFooterD97A() throws Exception {
    	buf = "";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
    	//lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));
        
    	buf = "";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
    	//lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));
    	
    	StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft("", 7));    
        sb.append(comc.fixLeft("合計：流通卡數：", 16));
        szTmp = String.format("%,6d", aliveCardCnt);
        sb.append(comc.fixRight(szTmp, 7));
        sb.append(comc.fixLeft("", 3));
        sb.append(comc.fixLeft("當月發卡數：", 12));
        szTmp = String.format("%,6d", monthIssCardCnt);
        sb.append(comc.fixRight(szTmp, 7));
        sb.append(comc.fixLeft("", 3));
        szTmp = comcr.commFormat("z,3z,3z,3#", totalAmtBal);
        sb.append(comc.fixRight(szTmp, 13));
        sb.append(comc.fixLeft("", 1));
        szTmp = comcr.commFormat("z,3z,3z,3#", totalMonthAmt);
        sb.append(comc.fixRight(szTmp, 13));
        sb.append(comc.fixLeft("", 1));
        szTmp = comcr.commFormat("z,3z,3z,3#", totalYearAmt);
        sb.append(comc.fixRight(szTmp, 13));
        sb.append(comc.fixLeft("", 3));
        sb.append(comc.fixLeft("有效卡數：", 12));
        szTmp = String.format("%,6d", validCardCnt);
        sb.append(comc.fixRight(szTmp, 7));
        
        buf = sb.toString();

        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	//lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));
    	
    	buf = "";
    	buf = "備註：流通卡為截至目前未停用之卡片，即控管碼為空者。";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	//lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));
    
    	buf = "";
    	buf = "      有效卡為最近六個月有消費記錄，且控管碼為空者。";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	//lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));

    	buf = "";
    	buf = "      當月發卡為當月新申請卡片，且控管碼為空者。";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	//lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));

    	buf = "";
    	buf = "      控管碼：１＝申請停用；２：掛失停用；３：強制停用；４，５：偽冒停用；７：到期不續卡。";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	//lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));

    	buf = "";
    	//buf = "      本表列出個人卡流通卡或現欠金額大於零之卡片資料。      委託扣繳額度：００＝不扣繳；１０＝扣最低應繳金額；２０＝全額扣繳";
    	buf = "      本表列出個人卡流通卡之卡片資料。       委託扣繳額度：００＝不扣繳；１０＝扣最低應繳金額；２０＝全額扣繳";
    	buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
    	//lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));

    }

    /***********************************************************************/
    void printDetailD97A() throws Exception {
        lineCnt++;
        indexCnt++;
        
        StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft(getValue("crdidno.id_no"), 10));    //正卡持卡人身份證字號
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixLeft(getValue("chi_name"),10 )); //持卡人姓名
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixLeft(getValue("ptrgroupcode.combo_indicator"), 1));  //Combo註記
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        //showLogMessage("I","",getValue("group_code")+getValue("ptrgroupcode.group_name"));
        sb.append(comc.fixLeft(getValue("ptrgroupcode.group_name"), 10));   //卡別
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixLeft(comc.getSubString(getValue("card_no"), 10), 6));  //卡號末6碼
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        String strYYY = "";
        int intYYYY = comc.str2int(comc.getSubString(getValue("ori_issue_date"),0,4));
        if (intYYYY > 0) {
        	strYYY = String.format("%03d", intYYYY-1911);
        }
        sb.append(comc.fixLeft(strYYY, 4));   //開戶年份
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixRight(comcr.commFormat("2z,3z,3#",  getValueDouble("actacno.line_of_credit_amt")), 10));  //信用額度
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixRight(String.format("%.0f",  getValueDouble("acctcurr.ttl_amt_bal")), 11));     //未繳金額
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixRight(String.format("%.0f",  bilBillMonthAmt+bilContractMonthAmt), 11));     //本月簽帳金額
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixRight(String.format("%.0f",  bilBillYearAmt+bilContractYearAmt), 11));     //本年度簽帳金額
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        
        totalAmtBal   += getValueDouble("acctcurr.ttl_amt_bal");
        totalMonthAmt += (bilBillMonthAmt+bilContractMonthAmt);
        totalYearAmt  += (bilBillYearAmt+bilContractYearAmt);
        
        //--判斷流通卡且6個月內有無消費
        String consumeFlag = "";
		if("0".equals(getValue("current_code"))==true 
		   && commDate.monthsBetween(hBusinssDate, getValue("last_consume_date")) <= 6) {
			consumeFlag = "*";
			validCardCnt++;
		}
        sb.append(comc.fixLeft(consumeFlag, 2));     //有效卡註記
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        String blockCode = "";
        if ("0".equals(getValue("current_code"))==false) {
        	blockCode = getValue("current_code");
        }
        sb.append(comc.fixLeft(blockCode, 2));     //控管碼
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        String payType = "00";
        if ("".equals(getValue("acctcurr.autopay_acct_bank"))==false) {
        	if ("1".equals(getValue("acctcurr.autopay_indicator"))) {
        		payType = "20"; //扣全額
        	} else {
        		payType = "10"; //扣最低
        	}
        }
        sb.append(comc.fixLeft(payType, 2));     //委託扣繳
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        if ("".equals(comc.getSubString(getValue("crdidno.office_tel"),0,3).trim()) 
        	|| "000".equals(comc.getSubString(getValue("crdidno.office_tel"),0,3)) ) {
        	sb.append(comc.fixLeft(getValue("crdidno.home_tel"), 12));     //電話號碼
        } else {
        	sb.append(comc.fixLeft(getValue("crdidno.office_tel"), 12));     //電話號碼
        }
       
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixLeft(getValue("crdidno.cellar_phone"), 10));     //手機號碼
        sb.append(comc.fixLeft(" ", 1));  //空白分隔
        sb.append(comc.fixLeft(getValue("actacno.ebill_flag"), 1));     //電子帳單註記
        
        buf = sb.toString();

        buf += "\n";
        writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
        
        //lparD97A.add(comcr.putReport(rptIdD97A, rptNameD97A, sysDate, ++rptSeqD97A, "0", buf));

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
    void selectPtrGroupCode() throws Exception {
    	
    	extendField = "ptrgroupcode.";
        sqlCmd = "select combo_indicator ";
        sqlCmd += "from ptr_group_code  ";
        sqlCmd += "where group_code = ? ";
        setString(1, getValue("group_code"));
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValue("ptrgroupcode.combo_indicator","N");
            setValue("ptrgroupcode.group_name","");
        } else {
        	if ("".equals(hm.get(getValue("group_code")))||null==hm.get(getValue("group_code"))) {
        		if ("V".equals(comc.getSubString(getValue("card_type"),0,1))) {
        			setValue("ptrgroupcode.group_name","ＶＩＳＡ卡");
        		} else if ("M".equals(comc.getSubString(getValue("card_type"),0,1))) {
        			setValue("ptrgroupcode.group_name","ＭＡＳＴ卡");
        		} else if ("J".equals(comc.getSubString(getValue("card_type"),0,1))) {
        			setValue("ptrgroupcode.group_name","ＪＣＢ卡");
        		} else {
        			setValue("ptrgroupcode.group_name","卡別錯誤");
        		}
        	} else {
        		setValue("ptrgroupcode.group_name",hm.get(getValue("group_code")));
        	}
        }
    }
    
    /**********************************************************************/
    void selectCrdIdno() throws Exception {
    	
    	extendField="crdidno.";
        sqlCmd =  "select id_no,(home_area_code1||home_tel_no1) as home_tel, ";
        sqlCmd += "       (office_area_code1||office_tel_no1) as office_tel, cellar_phone ";
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
    void selectActAcno() throws Exception {
    	
    	extendField="actacno.";
        sqlCmd = "select line_of_credit_amt, "; 
        sqlCmd += "  case when stat_send_internet = 'Y' and decode(stat_send_e_month2,'','999912',stat_send_e_month2) >= ? then 'Y' else 'N' end as ebill_flag ";
        sqlCmd += "from act_acno  ";
        sqlCmd += "where acno_p_seqno = ? ";
        setString(1, comc.getSubString(hBusinssDate,0,6));
        setString(2, getValue("acno_p_seqno"));
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValueDouble("actacno.line_of_credit_amt",0.0);
            setValue("actacno.ebill_flag","N");
        }
    }
    
    /**********************************************************************/
    void selectActAcctCurr() throws Exception {
    	
    	extendField="acctcurr.";
        sqlCmd = "select autopay_indicator,autopay_acct_bank,ttl_amt_bal ";
        sqlCmd += "from act_acct_curr  ";
        sqlCmd += "where p_seqno = ? and curr_code='901' ";
        setString(1, getValue("p_seqno"));
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValueDouble("acctcurr.ttl_amt_bal",0.0);
            setValue("acctcurr.autopay_indicator","");
            setValue("acctcurr.autopay_acct_bank","");
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
        setString(2, comc.getSubString(hBusinssDate,0,4)+"%");
        int tmpInt = selectTable();
        if (tmpInt > 0) {
        	for (int i=0; i<tmpInt ; i++) {
        		bilBillYearAmt += getValueDouble("bilbill.month_post_amt",i);
        		if (getValue("bilbill.post_month",i).equals(comc.getSubString(hBusinssDate,0,6))) {
        			bilBillMonthAmt += getValueDouble("bilbill.month_post_amt",i);
        		}
        	}
        }
    }
    
    /**********************************************************************/
    void loadBilBill() throws Exception {
    	daoTable = "bil_bill";
    	extendField="bilbill.";
        sqlCmd =  "select card_no,substr(post_date,1,6) as post_month, ";
        sqlCmd += "  nvl(sum(decode(sign_flag,'+',dest_amt,dest_amt*-1)),0) as month_post_amt  ";
        sqlCmd += "from bil_bill ";
        sqlCmd += "where post_date like ? ";
        sqlCmd += "and   acct_code in ('BL','CA','AO','ID','OT') ";
        sqlCmd += "group by card_no,substr(post_date,1,6) ";
        
        setString(1, comc.getSubString(hBusinssDate,0,4)+"%");
        
        loadTable();
        int billLoadCnt = getLoadCnt();
        showLogMessage("I","","Load bil_bill Count: ["+billLoadCnt+"]");
        setLoadData("bilbill.card_no");
    }

    /**********************************************************************/
    void selectBilContract() throws Exception {
    	
    	bilContractMonthAmt = 0;
    	bilContractYearAmt = 0;
    	
    	extendField="bilcontract.";
    	sqlCmd =  "select card_no,substr(first_post_date,1,6) AS post_month, ";
        sqlCmd += "       nvl(sum(tot_amt),0) as month_post_amt  ";
        sqlCmd += "from bil_contract ";
        sqlCmd += "where card_no = ? ";
        sqlCmd += "and   first_post_date like ? ";
        sqlCmd += "and   contract_kind   = '1' ";
        sqlCmd += "and   mcht_no not in ('106000000005','106000000007') ";
        sqlCmd += "and   installment_kind <> 'N' ";
        sqlCmd += "group by substr(first_post_date,1,6) ";
        setString(1, getValue("card_no"));
        setString(2, comc.getSubString(hBusinssDate,0,4)+"%");
        int tmpInt = selectTable();
        if (tmpInt > 0) {
        	for (int i=0; i<tmpInt ; i++) {
        		bilContractYearAmt += getValueDouble("bilcontract.month_post_amt",i);
        		if (getValue("bilcontract.post_month",i).equals(comc.getSubString(hBusinssDate,0,6))) {
        			bilContractMonthAmt += getValueDouble("bilcontract.month_post_amt",i);
        		}
        	}
        }
    }
    
    /**********************************************************************/
    void loadBilContract() throws Exception {
    	daoTable = "bil_contract";
    	extendField="bilcontract.";
        sqlCmd =  "select card_no,substr(first_post_date,1,6) AS post_month,nvl(sum(tot_amt),0) as month_post_amt ";
        sqlCmd += "from bil_contract ";
        sqlCmd += "where first_post_date like ? ";
        sqlCmd += "and   contract_kind   = '1' ";
        sqlCmd += "and   mcht_no not in ('106000000005','106000000007') ";
        sqlCmd += "and   installment_kind <> 'N' ";
        sqlCmd += "group by card_no,substr(first_post_date,1,6) ";
        
        setString(1, comc.getSubString(hBusinssDate,0,4)+"%");
        
        int billContractLoadCnt = loadTable();
        showLogMessage("I","","Load bil_contract Count: ["+billContractLoadCnt+"]");
        setLoadData("bilcontract.card_no");
    }
    
    /**********************************************************************/
    void selectMktPostConsume() throws Exception {
    	
    	extendField="mktpostconsume.";
        sqlCmd = "sum(consume_bl_amt+consume_ca_amt+consume_it_amt+consume_id_amt+consume_ao_amt+consume_ot_amt) AS year_purchase_amt ";
        sqlCmd += "from mkt_post_consume  ";
        sqlCmd += "where card_no = ? and acct_month like ? ";
        setString(1, getValue("card_no"));
        setString(2, comc.getSubString(hBusinssDate,0,4)+"%");
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValueDouble("mktpostconsume.year_purchase_amt",0.0);
        }
    }
    
    void  loadGroupCodeName() throws Exception
    {
    	hm = new HashMap<String,String>() {{
    		put("1100", "ＩＣ信用卡");
    		put("1200", "ＶＩＳ普卡");
    		put("1300", "ＶＩＳ金卡");
    		put("1600", "ＶＩＳ白金");
    		put("1400", "ＭＡＳ普卡");
    		put("1500", "ＭＡＳ金卡");
    		put("1220", "Ｖ友愛普卡");
    		put("1320", "Ｖ友愛金卡");
    		put("1240", "Ｖ９２１普");
    		put("1340", "Ｖ９２１金");
    		put("1230", "Ｖ花農普卡");
    		put("1330", "Ｖ花農金卡");
    		put("1310", "Ｖ尊爵金卡");
    		put("1510", "Ｍ尊爵金卡");
    		put("1420", "Ｍ北醫普卡");
    		put("1520", "Ｍ北醫金卡");
    		put("1430", "Ｍ運動普卡");
    		put("1530", "Ｍ運動金卡");
    		put("1250", "Ｖ朝陽普卡");
    		put("1350", "Ｖ朝陽金卡");
    		put("1450", "ＣＯＭＢ普");
    		put("1451", "ＣＯＭＢ普");
    		put("1550", "ＣＯＭＢ金");
    		put("1551", "ＣＯＭＢ金");
    		put("1650", "ＣＯＭ白金");
    		put("1651", "ＣＯＭ白金");
    		put("1460", "ＣＯ惠眾普");
    		put("1461", "ＣＯ惠眾普");
    		put("1560", "ＣＯ惠眾金");
    		put("1561", "ＣＯ惠眾金");
    		put("1660", "ＣＯ惠眾白");
    		put("1661", "ＣＯ惠眾白");
    		put("1599", "ｍ政府採購");
    		put("1260", "Ｖ馥蓁普卡");
    		put("1360", "Ｖ馥蓁金卡");
    		put("1470", "ＣＯ創價普");
    		put("1471", "ＣＯ創價普");
    		put("1570", "ＣＯ創價金");
    		put("1571", "ＣＯ創價金");
    		put("1900", "ＪＣＢ普卡");
    		put("1800", "ＪＣＢ金卡");
    		put("1880", "ＪＣＢ白金");
    		put("1440", "Ｍ台大普卡");
    		put("1540", "Ｍ台大金卡");
    		put("1640", "Ｍ台大白金");
    		put("1641", "ＭＡＳ白金");
    		put("1441", "Ｍ吉柿普卡");
    		put("1541", "Ｍ吉柿金卡");
    		put("1442", "Ｍ創價普卡");
    		put("1542", "Ｍ創價金卡");
    		put("1270", "ＥＭＡ普卡");
    		put("1370", "ＥＭＡ金卡");
    		put("1443", "Ｍ安旅普卡");
    		put("1543", "Ｍ安旅金卡");
    		put("1480", "Ｃ三重普卡");
    		put("1481", "Ｃ三重普卡");
    		put("1580", "Ｃ三重金卡");
    		put("1581", "Ｃ三重金卡");
    		put("1588", "Ｍ中華食物");
    		put("3700", "ＭＲ商務卡");
    		put("3750", "ＭＲ商務卡");
    		put("3760", "ＭＲ商務卡");
    		put("3790", "ＭＲ商務卡");
    		put("3701", "Ｍ安旅商務");
    		put("1444", "Ｍ靈鷲普卡");
    		put("1544", "Ｍ靈鷲金卡");
    		put("1644", "Ｍ靈鷲白金");
    		put("1642", "Ｍ晶片白金");
    		put("1662", "Ｍ房貸白金");
    		put("3702", "Ｍ雄獅商務");
    		put("3751", "Ｍ高市採購");
    		put("1280", "Ｖ銀行員普");
    		put("1380", "Ｖ銀行員金");
    		put("1201", "Ｖ農銀普卡");
    		put("1301", "Ｖ農銀金卡");
    		put("1296", "Ｖ聯電普卡");
    		put("1396", "Ｖ聯電金卡");
    		put("1391", "Ｖ源遠金卡");
    		put("1392", "Ｖ漢翔卡");
    		put("1293", "農銀墾丁普");
    		put("1393", "農銀墾丁金");
    		put("1394", "農銀大衛金");
    		put("1395", "農銀興和金");
    		put("1453", "ＣＯ農銀普");
    		put("1553", "ＣＯ農銀金");
    		put("1202", "農銀Ｕ卡");
    		put("1601", "Ｖ聯電白金");
    		put("1602", "Ｖ源遠白金");
    		put("1203", "Ｖ寶發普卡");
    		put("1603", "Ｖ北醫白金");
    		put("1297", "Ｖ心兒普卡");
    		put("1604", "Ｖ心兒白金");
    		put("1545", "Ｍ旺來卡");
    		put("3780", "ＥＧＯ採購");
    		put("1620", "Ｖ無限卡");
    		put("1621", "Ｖ北醫無限");
    		put("1605", "Ｖ北護白金");
    		put("1445", "Ｍ個ＰＣＨ");
    		put("3704", "Ｍ企ＰＣＨ");
    		put("1670", "Ｍ個人商旅");
    		put("1610", "Ｖ御璽商旅");
    		put("3782", "Ｖ臺酒法人");
    		put("1299", "Ｖ臺酒個人");
    		put("1606", "新時代聯名");
    		put("1221", "愛的世界普");
    		put("1321", "愛的世界金");
    		put("1607", "愛的世界白");
    		put("1608", "Ｖ悠遊白金");
    		put("1653", "ＶＣＯ悠遊");
    		put("1654", "鈦金卡");
    		put("1881", "Ｊ悠遊白金");
    		put("1663", "Ｍ手機卡");
    		put("1630", "Ｍ世界卡");
    		put("1631", "Ｍ漢來世界");
    		put("1655", "Ｍ鈦金台大");
    		put("1657", "Ｍ享樂卡");
    		put("1656", "Ｍ鈦公版醫");
    		put("1679", "Ｍ鈦悠鹿港");
    		put("1890", "Ｊ晶緻悠遊");
    		put("1891", "Ｊ晶緻悠哆");
    		put("1892", "Ｊ晶緻利害");
    		put("1893", "Ｊ晶緻漢來");
    		put("1894", "Ｊ晶通鹿港");
    		put("1680", "Ｍ宜蘭信合");
    		put("1681", "Ｍ愛金卡");
    		put("1682", "Ｍ彰化五信");
    		put("1683", "Ｍ高雄三信");
    		put("1684", "Ｍ卡娜藍");
    		put("1685", "Ｍ卡娜粉");
    		put("1686", "Ｍ台北五信");
    		put("1687", "Ｍ台中科大");
    		put("1688", "Ｍ花蓮二信");
    		put("1689", "Ｍ台中二信");
    		put("1690", "Ｍ花蓮一信");
    		put("1691", "Ｍ台南三信");
    		put("1692", "Ｍ嘉義三信");
    		put("1511", "Ｍ兆福個人");
    		put("3752", "Ｍ兆福法人");
    		put("1613", "Ｖ活力御璽");
    		put("1614", "Ｖ樂活卡");
    		put("1615", "Ｖ房貸御璽");
    		put("1616", "Ｖ享樂御璽");
    		put("1622", "Ｖ金鑽無限");
    		put("1693", "Ｍｉ運動");
    		put("1694", "Ｍ卡娜悠遊");
    	}};

    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilRD97 proc = new BilRD97();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
