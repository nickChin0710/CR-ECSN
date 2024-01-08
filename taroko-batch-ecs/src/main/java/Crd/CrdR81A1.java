/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/07/26 V1.01.01  Lai         program initial                            *
*  112/08/01 V1.01.02  Lai         modify clerk_id                            *
*  112/08/09 V1.01.03  Zuwei Su    按分行分頁處理                                                                                            *
*  112/09/03 V1.01.04  Wilson      判斷消費改讀入帳日期                                                                                 *
*  112/09/10 V1.01.05  Wilson      換行符號0A                                    *
*  112/09/13 V1.01.06  Wilson      調整為卡部報表                                                                                            *
*  112/11/08 V1.01.07  Wilson      日期減一天                                                                                                    *
******************************************************************************/
package Crd;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class CrdR81A1 extends AccessDAO {
    private String PROGNAME = "員工使用信用卡情形明細表(卡部)  112/11/08 V1.01.07";
    CommFunction   comm  = new CommFunction();
    CommCrd comc  = new CommCrd();
    CommCrd commCrd = new CommCrd();
    CommDate commDate  = new CommDate();
    CommCrdRoutine comcr = null;
    CommRoutine comr  = null;
    
    private final String CRM_FOLDER = String.format("%s/media/crd", comc.getECSHOME());
    private final String DATA_FORM = "RCRM81A1";

    int    DEBUG    = 0;
    int    DEBUG_F  = 0;
    String hTempUser = "";

    int    Report_Page_Line = 55;
    String prgmId    = "CrdR81A";

    String rptIdR1  = "CRM81A";
    String rptName1  = "員工使用信用卡情形明細表";
    int    pageCnt1 = 0, lineCnt1 = 0;
    int    rptSeq1   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf = "";
    int    totCnt = 0;
    int    totCnt1 = 0;

    String hBusiBusinessDate = "";
    String hBusiBusinessDateTw = "";
    String hCallBatchSeqno   = "";
    String hChiYymmdd         = "";
    String hBegDate =  "";
    String hEndDate =  "";
    String hBegYearDate       = "";
    String hEndYearDate       = "";
    String hBegDateBil       = "";
    String hEndDateBil       = "";
    String hLastDay          = "";

    String emplId               = "";
    String emplIdPSeqno         = "";
    String cardCardNo           = "";
    String cardIdPSeqno         = "";
    String cardAcnoPSeqno       = "";
    String cardCurrentCode      = "";
    String cardStatMail         = "";
    String cardLastConsumeDate  = "";
    String emplUNitNo           = "";
    String emplBrnChiName       = "";
    String emplChiName          = "";
    String emplAccountingNo     = "";
    int    brnCnt               = 0;
    int    brnApplyCnt          = 0;
    int    brnNoApplyCnt        = 0;
    int    brnEffcCnt           = 0;
    int    brnNoConsumeCnt      = 0;

    int    sumAll1             = 0;
    int    sumAll2             = 0;
    int    sumAll3             = 0;
    int    sumAll4             = 0;
    String tempAccNo            = "";
    String tempUNitNo           = "";
    String tempName             = "";
    int    loadBilCnt         = 0;

    int    arrayX              = 10000;
    int    arrayS              = 11;
    int    arrayI              = 11;

    String[][] allDataStr = new String [arrayX][arrayS];
    int[][]    allDataInt = new int [arrayX][arrayI];

    String tmp     = "";
    String temstr  = "";
    String tmpstr  = "";
    String tmpstr1 = "";

    buft htail = new buft();
    buf1 data  = new buf1();
    /***********************************************************************/
    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME + " Args=[" + args.length + "]");
    
            // 固定要做的
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            // =====================================
            if (args.length > 3) {
                comc.errExit("Usage : CrdR81A1 [yyyymmdd] [seq_no] ", "");
            }
            /*
             * if(comm.isAppActive(javaProgram))
             * comc.errExit("Error!! Someone is running this program now!! =["+javaProgram+"]" ,
             * "Please wait a moment to run again!!");
             */
    
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
    
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            if (comc.getSubString(hCallBatchSeqno, 0, 8)
                    .equals(comc.getSubString(comc.getECSHOME(), 0, 8))) {
                hCallBatchSeqno = "no-call";
            }
    
            String checkHome = comc.getECSHOME();
            if (hCallBatchSeqno.length() > 6) {
                if (comc.getSubString(hCallBatchSeqno, 0, 6)
                        .equals(comc.getSubString(checkHome, 0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }
    
            comcr.hCallRProgramCode = javaProgram;
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";
    
                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }
    
            if (args.length > 0) {
                hBusiBusinessDate = "";
                if (args[0].length() == 8) {
                    hBusiBusinessDate = args[0];
                } else {
                    String ErrMsg = String.format("指定營業日[%s]", args[0]);
                    comcr.errRtn(ErrMsg, "營業日長度錯誤[yyyymmdd], 請重新輸入!", hCallBatchSeqno);
                }
            }
            selectPtrBusinday();
            hBusiBusinessDateTw = commDate.toTwDate(hBusiBusinessDate);
            hLastDay = hEndDate;
            
            if (!hBusiBusinessDate.equals(hLastDay)) {
				showLogMessage("E", "", "報表日不為該月最後一天,不執行此程式");
				return 0;
            }
            
            // get the name and the path of the .DAT file
            String filename1 = String.format("%s.1.TXT", DATA_FORM);
            String fileFolder = Paths.get(CRM_FOLDER).toString();
    
            loadBilBill();
    
            initAllArray();
    
            selectCrdEmployee();
    
//            if (totCnt > 0) {
//                tempAccNo = emplUNitNo;
//                tempName = emplBrnChiName;
//                // writeHead(3);
//                writeDetail();
//                writeTail();
//            }
    
            String filename = String.format("%s/media/crd/%s.1.TXT", comc.getECSHOME(), DATA_FORM);
            filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
            comc.writeReport(filename, lpar1);
            
            // run FTP
            procFTP(fileFolder, filename1, filename1);
            
            // backup
            backup(filename1);
    
            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束
    
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
    
    // ************************************************************************
    void initAllArray() throws Exception {
        brnCnt = 0;
        brnApplyCnt = 0;
        brnNoApplyCnt = 0;
        brnEffcCnt = 0;
        brnNoConsumeCnt = 0;
        sumAll1 = 0;
        sumAll2 = 0;
        sumAll3 = 0;
        sumAll4 = 0;
        for (int i = 0; i < arrayX; i++) {
            for (int j = 0; j < arrayS; j++)
                allDataStr[i][j] = "";
            for (int k = 0; k < arrayI; k++)
                allDataInt[i][k] = 0;
        }
    }
    
    // ************************************************************************
    void loadBilBill() throws Exception {
        extendField = "bill.";
        selectSQL = "card_no,       "
                  + "post_date,     "
                  + "decode(sign_flag, '+', dest_amt, dest_amt *-1) as purchase_amt   ";
        daoTable  = "bil_bill ";
        whereStr  = "where post_date between ? and ? "
                  + "  and acct_code  in ('BL','IT','ID','AO','OT') "
                  + "  and id_p_seqno in (select id_p_seqno from crd_idno where id_no in (select id from crd_employee)) "
                  + " order by card_no ";
    
        setString(1, hBegYearDate);           
    	
        showLogMessage("I","","  get bill 1 date_fm="+hBegYearDate);
        
        setString(2, hEndYearDate);

        showLogMessage("I","","  get bill 2 date_to="+hEndYearDate);
    
        int n = loadTable();
        setLoadData("bill.card_no"); // set key
    
        // int lind = getLoadIndex();
    
        showLogMessage("I", "", "Load bil_bill end Count: [" + n + "]" + hBegYearDate + "," + hEndYearDate);
    }
    
    // ************************************************************************
    void chkBillMonth() throws Exception {
        String billPostDate = "";
        int billDestAmt = 0;
        
        loadBilCnt = 0;
        setValue("bill.card_no", cardCardNo);
        loadBilCnt = getLoadData("bill.card_no");
               
        if (DEBUG_F == 1)
            showLogMessage("I", "", "   888 bill cnt=" + loadBilCnt);
        
        for (int k = 0; k < loadBilCnt; k++) {
            billPostDate = getValue("bill.post_date", k);
            billDestAmt = (int) getValueDouble("bill.purchase_amt", k);
            
            if (comc.getSubString(hEndYearDate, 0, 4).equals(comc.getSubString(billPostDate, 0, 4))) {
                allDataInt[brnCnt][4] = allDataInt[brnCnt][4] + billDestAmt; // 當年度累積消費金額
                
                if (hEndDate.substring(0, 6).equals(billPostDate.substring(0, 6))) {
                    allDataInt[brnCnt][3] = allDataInt[brnCnt][3] + billDestAmt; // 本月消費金額
                }                
            }
            
            if (DEBUG_F == 1)
                showLogMessage("I", "",
                        "    888 bill="
                                + billPostDate
                                + ","
                                + billDestAmt
                                + ",年消費="
                                + allDataInt[brnCnt][4]);
        }
    }
    
    /***********************************************************************/
    public int selectPtrBusinday() throws Exception {
    
        sqlCmd = "select to_char(add_days(sysdate,-1),'yyyymmdd') as business_date";
        sqlCmd +=
                "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate =
                    hBusiBusinessDate.length() == 0 ? getValue("business_date") : hBusiBusinessDate;
        }
    
        sqlCmd = "select to_char(add_months(to_date(?,'yyyymmdd'),-5),'yyyymm')||'01' h_beg_date_bil ";
        sqlCmd += "     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date_bil ";
 	    sqlCmd += "     , to_char(to_date(?,'yyyymmdd'),'yyyymm')||'01' h_beg_date ";
 	    sqlCmd += "     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date ";
        sqlCmd += "     , to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm')||'01' h_beg_year_date ";
        sqlCmd += "     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_year_date ";
        sqlCmd += " from dual ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        setString(5, hBusiBusinessDate);
        setString(6, hBusiBusinessDate);
    
        recordCnt = selectTable();
        if (recordCnt > 0) {
        	hBegDateBil = getValue("h_beg_date_bil");
            hEndDateBil = getValue("h_end_date_bil");
  	        hBegDate = getValue("h_beg_date");
  	        hEndDate = getValue("h_end_date"); 
            hEndYearDate = getValue("h_end_year_date");
            hBegYearDate = hEndYearDate.substring(0, 4) + "0101";            
        }
    
        hChiYymmdd = commDate.toTwDate(hBusiBusinessDate);
        showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s][%s][%s]", hBusiBusinessDate,
                hChiYymmdd, hBegDateBil, hEndDateBil, hBegDate, hEndDate, hBegYearDate, hEndYearDate));
    
        return 0;
    }
    
    /***********************************************************************/
    void selectCrdCard() throws Exception {
        String fileLastConsumeDate = "";
    	
        sqlCmd = " select ";
        sqlCmd += "  c.card_no             ,c.id_p_seqno        , ";
        sqlCmd += "  c.acno_p_seqno        ,c.current_code      , ";
        sqlCmd += "  b.stat_send_internet  ,c.last_consume_date   ";
        sqlCmd += "   from act_acno b , crd_card c  ";
        sqlCmd += "  where c.id_p_seqno   = ? ";
        sqlCmd += "    and b.acno_p_seqno = c.acno_p_seqno ";
    
        setString(1, emplIdPSeqno);
    
        int recordCnt = selectTable();
    
        for (int k = 0; k < recordCnt; k++) {
        	totCnt1++;
        	
            cardCardNo = getValue("card_no", k);
            cardIdPSeqno = getValue("id_p_seqno", k);
            cardAcnoPSeqno = getValue("acno_p_seqno", k);
            cardCurrentCode = getValue("current_code", k);
            cardStatMail = getValue("stat_send_internet", k);
            cardLastConsumeDate = getValue("last_consume_date", k);
            
            if (DEBUG == 1)
                showLogMessage("I", "", "  Card=" + cardCardNo + " ," + cardStatMail + " , idx=" + k);
            
            if (totCnt1 % 5000 == 0 || totCnt1 == 1) {
                showLogMessage("I", "", String.format("R81A Process 2 record=[%d]\n", totCnt1));
            }
            
            if (cardCurrentCode.equals("0")) {
                allDataInt[brnCnt][1]++; // 流通卡數
            }
    
            allDataStr[brnCnt][5] = "N";
            if (cardStatMail.length() > 0)
                allDataStr[brnCnt][5] = "Y"; // 電子帳單
            
            if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil)) {
            	allDataInt[brnCnt][2]++; // 有效卡數
            }
                            
            chkBillMonth();
            
            if (cardLastConsumeDate.length() == 8 && comcr.str2long(cardLastConsumeDate) > comcr.str2long(fileLastConsumeDate)){
            	fileLastConsumeDate = cardLastConsumeDate;
                allDataStr[brnCnt][3] = fileLastConsumeDate.substring(0, 4)
                        + "/" // 最後消費日期
                        + fileLastConsumeDate.substring(4, 6)
                        + "/"
                        + fileLastConsumeDate.substring(6, 8);
            }
        }
    }
    
    /***********************************************************************/
    void selectCrdEmployee() throws Exception {
        fetchExtend = "main.";
        sqlCmd = "select ";
        sqlCmd += "  a.unit_no             ,a.id                , ";
        sqlCmd += "  b.id_p_seqno          ,a.chi_name          , ";
        sqlCmd += "  c.dep_chi_name      ,c.accounting_no       ";
        sqlCmd += "  from ptr_accounting_no c,  crd_employee a ";
        sqlCmd += "  full outer join crd_idno b on b.id_no  = a.id ";
        sqlCmd += " where c.accounting_no = a.accounting_no ";
        sqlCmd += "  order by c.branch,length(c.accounting_no),c.accounting_no,b.id_p_seqno ";
    
        openCursor();
    
        while (fetchTable()) {
            initRtn();
            totCnt++;
    
            emplId = getValue("main.id");
            emplIdPSeqno = getValue("main.id_p_seqno");
            emplUNitNo = getValue("main.unit_no");
            emplChiName = getValue("main.chi_name");
            emplBrnChiName = getValue("main.dep_chi_name");
            emplAccountingNo = getValue("main.accounting_no");
           
            if (DEBUG == 1) {
                showLogMessage("I", "",
                        "Read brn="
                                + emplUNitNo
                                + " accno="
                                + emplAccountingNo
                                + " ID="
                                + emplId
                                + ","
                                + emplIdPSeqno
                                + ",C="
                                + emplChiName
                                + ",Cnt="
                                + totCnt);
            }
    
            if (totCnt % 1000 == 0 || totCnt == 1) {
                showLogMessage("I", "", String.format("R81A Process 1 record=[%d]\n", totCnt));
            }
    
            if (!tempAccNo.equals(emplAccountingNo)) {
                if (tempAccNo.length() > 0) {
                    writeHead(0);
                    writeDetail();
                    writeTail();
                }
                initAllArray();
                tempAccNo = emplAccountingNo;
                tempUNitNo = emplUNitNo;
                tempName = emplBrnChiName;
            }
    
            // if (totCnt == 1) {
            // tempAccNo = emplUNitNo;
            // tempName = emplBrnChiName;
            // writeHead(0);
            // }
            // if (tempAccNo.compareTo(emplUNitNo) != 0) {
            // writeDetail();
            //
            // writeTail();
            //
            // writeHead(1);
            //
            // initAllArray();
            //
            // tempAccNo = emplUNitNo;
            // tempName = emplBrnChiName;
            // }
    
            brnCnt++;
            if (emplIdPSeqno.length() < 1) {
                allDataStr[brnCnt][1] = "N"; // 申辦註記
                brnNoApplyCnt++; // 未申辦數
            } else {
                allDataStr[brnCnt][1] = "Y"; // 申請註記
                brnApplyCnt++; // 申辦數
            }
            if (DEBUG == 1) {
                showLogMessage("I", "",
                        "    申辦=" + emplIdPSeqno + "," + brnApplyCnt + "," + brnNoApplyCnt);
            }
    
            allDataStr[brnCnt][0] = comc.fixLeft(emplChiName, 20); // 姓名
            if (emplChiName.length() < 1) {
                allDataStr[brnCnt][0] = emplId; // 姓名
//                continue;
            }
    
            selectCrdCard();
    
            /*
             * lai test allData_int[BrnCnt][2] = 5;
             */
    
            allDataStr[brnCnt][2] = "";
            if (allDataInt[brnCnt][2] > 0) { // 有效卡數
                brnEffcCnt++;
                allDataStr[brnCnt][2] = "*"; // 有效戶註記
            }
            allDataStr[brnCnt][4] = emplUNitNo + emplBrnChiName; // 分行代號
            if (allDataInt[brnCnt][3] < 1) { // 本月消費
                brnNoConsumeCnt++;
            }
    
            if (DEBUG == 1) {
                showLogMessage("I", "", "    有效=[" + allDataInt[brnCnt][2] + "," + brnEffcCnt);
            }
        }
        if (tempAccNo.length() > 0) {
            writeHead(0);
            writeDetail();
            writeTail();
        }
    
        showLogMessage("I", "", " Read end=" + totCnt);
    
    }
    
    /***********************************************************************/
    void initRtn() throws Exception {
        cardCardNo = "";
        cardIdPSeqno = "";
        cardAcnoPSeqno = "";
        cardCurrentCode = "";
        emplBrnChiName = "";
        emplChiName = "";
        emplIdPSeqno = "";
        emplUNitNo = "";
        emplAccountingNo = "";
    }
    
    /***********************************************************************/
    void writeHead(int idx) throws Exception {
        String temp = "";
        if (DEBUG_F == 1) {
            showLogMessage("I", "", "   Write Head=" + idx);
        }
    
        pageCnt1++;
        if (pageCnt1 > 1) {
            lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", " "));
        }
        
        buf = "";
        buf = comcr.insertStr(buf, "3144", 1);
        buf = comcr.insertStr(buf, rptIdR1, 11);
        buf = comcr.insertStr(buf, hChiYymmdd + rptName1, 27);
        buf = comcr.insertStr(buf, "N", 115);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "合作金庫商業銀行", 55);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
    
        buf = "";
        if (idx == 1) {
            buf = comcr.insertStr(buf, "分行代號: " + emplUNitNo + " " + emplBrnChiName, 1);
        } else {
            buf = comcr.insertStr(buf, "分行代號: " + tempUNitNo + " " + tempName, 1);
        }
        buf = comcr.insertStr(buf, "" + rptName1, 55);
        buf = comcr.insertStr(buf, "保存年限: 五年", 117);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
    
        buf = "";
        tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
                hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
        buf = comcr.insertStr(buf, "報表代號: CRM81A    科目代號:", 1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp, 55);
        temp = String.format("%04d", pageCnt1);
        buf = comcr.insertStr(buf, "第" + temp + "頁", 121);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
    
        buf = "";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
    
        buf = "姓  名               申請 流通 有效 有效戶     本月      當年度累積         最後                分行 ／                申請      備註   ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
    
        buf = "                     註記 卡數 卡數  註記    消費金額     消費金額         消費日               部處代號             電子帳單            ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
    
        buf = "==================== ==== ==== ==== ====== ============ ============== ============== ============================== ======== ========== ";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
    
        lineCnt1 = 6;
    }
    
    /***********************************************************************/
    void writeTail() throws UnsupportedEncodingException {
        String tmp1 = "";

        htail.filler01 = "    合  計 :";
        htail.int01 = String.format("%4d", sumAll1);
        htail.int02 = String.format("%4d", sumAll2);
        htail.int03 = comcr.commFormat("$3z,3z,3z", sumAll3);
        htail.int04 = comcr.commFormat("$1z,3z,3z,3z", sumAll4);
        buf = htail.allText();
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        tmp = String.format("%4d", brnCnt);
        buf = comcr.insertStr(buf, "員工總數 :" + tmp + "人   未申辦 :", 1);
        tmp = String.format("%4d", brnNoApplyCnt);
        buf = comcr.insertStr(buf, tmp + "戶   有效戶 :", 27);
        tmp = String.format("%4d", brnEffcCnt);
        tmp1 = String.format("%4d", brnCnt - brnNoApplyCnt - brnEffcCnt);
        buf = comcr.insertStr(buf, tmp + "戶   非有效戶 :" + tmp1 + "戶", 40);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        tmp = String.format("%4d", brnNoConsumeCnt);
        buf = comcr.insertStr(buf, "本月未消費員工數 :" + tmp + "人   本月員工平均消費金額 :", 1);
        tmp = "";
        if (sumAll3 > 0) {
            int tmpInt = (int) (sumAll3 / brnCnt + 0.5);
            tmp = comcr.commFormat("$2z,3z,3z", tmpInt);
        }
        buf = comcr.insertStr(buf, tmp + "元", 51);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "說 明:";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = " １、申請註記: N 未申辦信用卡    Y 已申辦信用卡";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = " ２、有效卡: 最近六個月有消費之卡片且卡片控管碼不為停卡者";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = " ３、有效戶: 正卡ID歸戶下至少有一卡為有效卡者";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = " ４、本月員工總平均消費金額＝本月員工消費總額／員工總數";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = " ５、本月員工未消費數不含未申辦信用卡之員工";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

    }

    /***********************************************************************/
    void writeDetail() throws Exception {
        String tmp = "";

        if (DEBUG_F == 1) {
            showLogMessage("I", "", "   Write Dtl=" + brnCnt);
        }

        for (int i = 1; i < brnCnt + 1; i++) {
            if (DEBUG_F == 1) {
                showLogMessage("I", "", "   Write Max=" + Report_Page_Line + "," + lineCnt1);
            }
            if (lineCnt1 > Report_Page_Line) {
                writeHead(0);
            }

            data = null;
            data = new buf1();

            data.name = String.format("%s", allDataStr[i][0]);
            data.str01 = String.format("%s", " " + allDataStr[i][1]);
            data.int01 = String.format("%4d", allDataInt[i][1]);
            data.int02 = String.format("%4d", allDataInt[i][2]);
            data.str02 = String.format("%s", "  " + allDataStr[i][2]);
            data.int03 = comcr.commFormat("$3z,3z,3z", allDataInt[i][3]);
            data.int04 = comcr.commFormat("$1z,3z,3z,3z", allDataInt[i][4]);
            data.str03 = String.format("%s", "  " + allDataStr[i][3]);
            data.str04 = String.format("%s", "  " + allDataStr[i][4]);
            data.str05 = String.format("%s", "   " + allDataStr[i][5]);

            buf = data.allText();
            lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

            lineCnt1 = lineCnt1 + 1;

            sumAll1 += allDataInt[i][1];
            sumAll2 += allDataInt[i][2];
            sumAll3 += allDataInt[i][3];
            sumAll4 += allDataInt[i][4];
        }

        return;
    }

    /************************************************************************/
    void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "BREPORT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = fileFolder;
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;
//        commFTP.hEriaLocalDir = String.format("%s/media/crm", comc.getECSHOME());
//        commFTP.hEriaRemoteDir = "crdatacrea/NEWCENTER";

        // 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
        String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

        showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
        int errCode = commFTP.ftplogName("BREPORT", ftpCommand);

        if (errCode != 0) {
            showLogMessage("I", "",
                    String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
//            commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
//            commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
        }
    }

    /****************************************************************************/
    private void backup(String removeFileName) throws Exception {
        String tmpstr1 = CRM_FOLDER + "/" + removeFileName;
        String backupFilename = String.format(DATA_FORM + "_%s.1.TXT", hBusiBusinessDate);
        String tmpstr2 = CRM_FOLDER + "/backup/" + backupFilename;

        if (commCrd.fileRename2(tmpstr1, tmpstr2) == false) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
    }
    
    /****************************************************************************/
    public static void main(String[] args) throws Exception {
        CrdR81A1 proc = new CrdR81A1();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /************************************************************************/
    class buft {
        String filler01;
        String int01;
        String int02;
        String filler02;
        String int03;
        String int04;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(filler01, 25 + 1);
            rtn += fixLeft(int01, 4 + 1);
            rtn += fixLeft(int02, 4 + 1);
            rtn += fixLeft(filler02, 6 + 1);
            rtn += fixLeft(int03, 12 + 1);
            rtn += fixLeft(int04, 14 + 1);
            // rtn += fixLeft(len, 1);
            return rtn;
        }


    }
    class buf1 {
        // 姓 名 申請 流通 有效 有效戶 本 月 當 年 度 累 積 最 後 分行 ／ 部處代號 申請電 備註
        String name;
        String str01;
        String int01;
        String int02;
        String str02;
        String int03;
        String int04;
        String str03;
        String str04;
        String str05;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(name, 20 + 1);
            rtn += fixLeft(str01, 4 + 1);
            rtn += fixLeft(int01, 4 + 1);
            rtn += fixLeft(int02, 4 + 1);
            rtn += fixLeft(str02, 6 + 1);
            rtn += fixLeft(int03, 12 + 1);
            rtn += fixLeft(int04, 14 + 1);
            rtn += fixLeft(str03, 14 + 1);
            rtn += fixLeft(str04, 30 + 1);
            rtn += fixLeft(str05, 8 + 1);
            // rtn += fixLeft(len , 1);
            return rtn;
        }


    }

    String fixLeft(String str, int len) throws UnsupportedEncodingException {
        int size = (Math.floorDiv(len, 100) + 1) * 100;
        String spc = "";
        for (int i = 0; i < size; i++)
            spc += " ";
        if (str == null)
            str = "";
        str = str + spc;
        byte[] bytes = str.getBytes("MS950");
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, 0, vResult, 0, len);

        return new String(vResult, "MS950");
    }

}
