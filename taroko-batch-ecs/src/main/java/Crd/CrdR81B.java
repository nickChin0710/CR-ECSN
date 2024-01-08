/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/02/24 V1.01.01  Lai         program initial                            *
*  112/03/15 V1.01.02  Zuwei Su    產出 pdf                                   *
*  112/03/17 V1.01.03  Zuwei Su    產出 報表檔和pdf 格式調整                  *
*  112/07/06 V1.01.04  Wilson      mark 產出 pdf                              *
*  112/07/17 V1.01.05  Wilson      調整為on demand格式                        *
*  112/07/28 V1.01.06  Lai         check select bil_bill                      *
*  112/08/04 V1.01.07  lai         modify                                     *
*  112/08/14 V1.01.08  lai         modify by ID統計                           *
*  112/09/03 V1.01.09  Wilson      調整欄位邏輯                                                                                                *
*  112/09/10 V1.01.10  Wilson      換行符號0A                                   *
*  112/11/08 V1.01.11  Wilson      日期減一天                                                                                                    *
******************************************************************************/
package Crd;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
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
//import com.TarokoJasperUtils;

public class CrdR81B extends AccessDAO {
    private final String PROGNAME = "各分行員工使用信用卡統計表  112/11/08 V1.01.11";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommDate commDate = new CommDate();
    CommCrd commCrd = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;
    
    private final String CRM_FOLDER = String.format("%s/media/crd", comc.getECSHOME());
    private final String DATA_FORM = "RCRM81B";

    int debug   = 0;
    int DEBUG_F = 0;
    String hTempUser = "";

    int reportPageLine = 55;
    String prgmId = "CrdR81B";
    String rptIdR1 = "CRM81B";
    String rptName1 = "各分行員工使用信用卡統計表";
    int pageCnt1 = 0, lineCnt1 = 0;
    int rptSeq1 = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lparPdf = new ArrayList<Map<String, Object>>();

    String buf = "";
    int totCnt = 0;

    String hBusiBusinessDate = "";
    String hCallBatchSeqno = "";
    String hChiYymmdd  = "";
    String hBegDate =  "";
    String hEndDate =  "";
    String hBegYearDate = "";
    String hEndYearDate = "";
    String hBegDateBil = "";
    String hEndDateBil = "";
    String hLastDay = "";

    String cardCardNo = "";
    String cardCurrentCode = "";
    String cardAcnoPSeqno = "";
    String cardIdPSeqno = "";
    String cardBranch  = "";
    String cardAccountingNo = "";
    String cardLastConsumeDate = "";
    String emplId = "";
    String emplIdPSeqno = "";
    int sumAll1 = 0;
    int sumAll2 = 0;
    int sumAll3 = 0;
    int sumAll4 = 0;
    int sumAll5 = 0;
    int sumAll6 = 0;
    int sumAll7 = 0;
    int sumAll8 = 0;
    int sumAll9 = 0;
    int sumAll10 = 0;
    int acnoCurrentCnt = 0;
    int acnoEffcCnt = 0;
    int acnoCurrentSum = 0;
    int acnoEffcSum = 0;
    String tempBranch  = "";
    String tempName = "";
    int loadBilCnt = 0;
    int loadAcnoCnt = 0;

    int arrayEffc = 0;
    int arryConsume = 0;
    int arrayMax = 500000;
    int arrayX = 500;
    int arrayY = 16;
    int brnCnt = 0;
    int brnPoint = 0;

    String[][] allDataH = new String[arrayX][3];
    int[][] allData = new int[arrayX][arrayY];
    String[] groupAcnoPSeqno = new String[arrayMax];
    String[] groupIdPSeqno   = new String[arrayMax];

    String tmp = "";
    String temstr = "";
    String tmpstr = "";
    String tmpstr1 = "";

    buft htail = new buft();
    buf1 data = new buf1();

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
                comc.errExit("Usage : CrdR81B [yyyymmdd] [seq_no] ", "");
            }

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
            initArrayPseq();

            selectGenBrn();

            if (debug == 1)
                showLogMessage("I", "", "*** ALL Brn=" + brnCnt);

            selectCrdCard(); 
            
if(debug==1) showLogMessage("I","","    Chk Acno0="+brnPoint+","+allData[brnPoint][8]+","+allData[brnPoint][7]);

            chkBrn();
            
            headFile();
            writeFile();
            tailFile();

            String filename = String.format("%s/media/crd/%s.1.TXT", checkHome, DATA_FORM);
            filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
            comc.writeReport(filename, lpar1);
            
            // run FTP
            procFTP(fileFolder, filename1, filename1);
            
            // backup
            backup(filename1);

            // 產生PDF
//            String reportTemplateFilename = String.format("%s/reportTemplate/CrdR81B.jrxml", checkHome);
//            TarokoJasperUtils.compileReport(reportTemplateFilename);
//            String pdfTemplateFilename = String.format("%s/reportTemplate/CrdR81B.jasper", checkHome);
//            String pdfFilename = filename.substring(0, filename.lastIndexOf(".txt")) + ".pdf";
//            TarokoJasperUtils.exportPdf(pdfTemplateFilename, pdfFilename, new HashMap<String, Object>(), lparPdf);

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
    void initArrayPseq() throws Exception {

        for (int i = 0; i < arrayMax; i++) {
            groupAcnoPSeqno[i] = "";
            groupIdPSeqno[i]   = "";
        }
    }

    // ************************************************************************
    void initAllArray() throws Exception {
        for (int i = 0; i < arrayX; i++) {
            groupAcnoPSeqno[i] = "";
            groupIdPSeqno[i]   = "";
            allDataH[i][0] = "";
            allDataH[i][1] = "";
            allDataH[i][2] = "";
            for (int j = 0; j < arrayY; j++)
                allData[i][j] = 0;
        }
    }

    // ************************************************************************
    void loadBilBill() throws Exception {
        extendField = "bill.";
        selectSQL   = "card_no," 
                    + "id_p_seqno," 
                    + "post_date,  "
                    + "decode(sign_flag, '+', dest_amt, dest_amt *-1) as purchase_amt ";
        daoTable    = "bil_bill ";
        whereStr    = "where post_date between ? and ? "
                    + "  and acct_code  in ('BL','IT','ID','AO','OT') "
                    + "  and id_p_seqno in (select id_p_seqno from crd_idno where id_no in (select id from crd_employee)) "
                    + " order by card_no ";
        	
        setString(1, hBegYearDate);           
        	
        showLogMessage("I","","  get bill 1 date_fm="+hBegYearDate);
        
        setString(2, hEndYearDate);

        showLogMessage("I","","  get bill 2 date_to="+hEndYearDate);
       
        int n = loadTable();
        setLoadData("bill.card_no");      // set key
        setLoadData("bill.id_p_seqno");   // set key

        showLogMessage("I", "", "Load bil_bill end Count: [" + n + "]" + hBegYearDate + "," + hEndYearDate);
    }
    // ************************************************************************
    void chkBillMonth() throws Exception {
        String billPostDate = "";
        int billDestAmt = 0;
        
        loadBilCnt = 0;
        setValue("bill.card_no", cardCardNo);
        loadBilCnt = getLoadData("bill.card_no");
        
        for (int k = 0; k < loadBilCnt; k++) {
            billPostDate = getValue("bill.post_date", k);
            billDestAmt   = (int) getValueDouble("bill.purchase_amt", k);
            
 if(DEBUG_F==1) showLogMessage("I",""," 888 bill="+billPostDate+","+billDestAmt+","+brnPoint);
 
            if (comc.getSubString(hEndYearDate, 0, 4).equals(comc.getSubString(billPostDate, 0, 4))) {
                allData[brnPoint][10] = allData[brnPoint][10] + billDestAmt;   // 當年度累積消費額
                
                if (hEndDate.substring(0, 6).equals(billPostDate.substring(0, 6))) {
                    allData[brnPoint][8] = allData[brnPoint][8] + billDestAmt; // 本月消費金額
                }
            }
        }
    }

    /***********************************************************************/
    public int selectPtrBusinday() throws Exception {

        sqlCmd = "select to_char(add_days(sysdate,-1),'yyyymmdd') as business_date";
        sqlCmd += "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
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
    void selectCrdIdnoCard(String tmpIdPSeqno) throws Exception {

    	int tmpCnt = 0;
    	String tmpPostDate = "";
    	       	            
        sqlCmd = " select count(*) as cnt ";
        sqlCmd += "   from crd_card ";
        sqlCmd += "  where id_p_seqno = ? ";
        sqlCmd += "  and last_consume_date >= ? ";
        setString(1, tmpIdPSeqno);
        setString(2, hBegDateBil);
        int recordCnt =  selectTable();
        
        tmpCnt = getValueInt("cnt");
        
        if(tmpCnt > 0) {
        	arrayEffc++;
        }            	
                           	
        loadAcnoCnt = 0;
        setValue("bill.id_p_seqno", tmpIdPSeqno);
        loadAcnoCnt = getLoadData("bill.id_p_seqno");                                 
        
        for (int k = 0; k < loadAcnoCnt; k++) {
            tmpPostDate = getValue("bill.post_date", k);
            
            if (hEndDate.substring(0, 6).equals(tmpPostDate.substring(0, 6))) {
            	arryConsume++;
            	break;
            } 
        }                        
        
    }

    /***********************************************************************/
    int selectCrdEmployee() throws Exception {
    	arrayEffc = 0;
    	arryConsume = 0;
    	
        sqlCmd = " select a.id, ";
        sqlCmd += "       b.id_p_seqno ";
        sqlCmd += "  from crd_employee a ";
        sqlCmd += "  full outer join crd_idno b on b.id_no = a.id ";
        sqlCmd += " where a.accounting_no = ? ";

        setString(1, allDataH[brnCnt][0]);

        int recordCnt = selectTable();

        for (int k = 0; k < recordCnt; k++) {
            emplId = getValue("id", k);
            emplIdPSeqno = getValue("id_p_seqno", k);
            
            if (emplIdPSeqno.length() < 1) {
            	allData[brnCnt][2]++; // 未申辦信用卡戶數
            }
            else {
                selectCrdIdnoCard(emplIdPSeqno);
            }           
            
            allData[brnCnt][1]++; // 員工數
        }
        
        allData[brnCnt][5] = allData[brnCnt][5] + arrayEffc; // 有效戶數
        
        allData[brnCnt][6] = allData[brnCnt][1] - allData[brnCnt][2] - allData[brnCnt][5];  // 非有效戶數
        
        allData[brnCnt][7] = allData[brnCnt][1] - allData[brnCnt][2] - arryConsume; // 本月未消費員工數

        return (allData[brnCnt][1]);
    }

    /***********************************************************************/
    void selectGenBrn() throws Exception {
        sqlCmd = " select  ";
        sqlCmd += " accounting_no, ";
        sqlCmd += " branch, ";
        sqlCmd += " dep_chi_name ";
        sqlCmd += "  from ptr_accounting_no ";
        sqlCmd += " where 1=1 ";
        sqlCmd += " order by branch, length(accounting_no), accounting_no  ";
        openCursor();

        while (fetchTable()) {
            brnCnt++;
            tmp = String.format("%s", comc.fixLeft(getValue("dep_chi_name"), 30));
            allDataH[brnCnt][0] = getValue("accounting_no");
            allDataH[brnCnt][1] = getValue("branch");
            allDataH[brnCnt][2] = tmp;

 if(debug == 1) showLogMessage("I","","Read BRN="+brnCnt+",C="+getValue("accounting_no")+","+getValue("branch"));
            selectCrdEmployee();
        }

        closeCursor();
    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {
        String tmp = "";

        fetchExtend = "main.";
        selectSQL = " a.card_no             ,a.current_code        , "
                  + " a.acno_p_seqno        ,a.id_p_seqno          , "
                  + " d.branch              ,d.accounting_no       , "
                  + " c.id                  ,a.last_consume_date     ";
        daoTable  = " crd_idno b, crd_card a, crd_employee c, ptr_accounting_no d";
        whereStr  = "where 1=1 "
                  + "  and a.id_p_seqno     = b.id_p_seqno "
                  + "  and b.id_no          = c.id         "
                  + "  and c.accounting_no  = d.accounting_no "
                  + "order by d.branch,length(d.accounting_no),d.accounting_no, b.id_p_seqno ";
 
        openCursor();

        while (fetchTable()) {
            initRtn();
            totCnt++;

            cardCardNo      = getValue("main.card_no");
            cardCurrentCode = getValue("main.current_code");
            cardAcnoPSeqno  = getValue("main.acno_p_seqno");
            cardIdPSeqno    = getValue("main.id_p_seqno");
            cardBranch      = getValue("main.branch");
            cardAccountingNo = getValue("main.accounting_no");
            cardLastConsumeDate = getValue("main.last_consume_date");

            chkBrn();    
                   
            if (totCnt % 5000 == 0 || totCnt == 1)                        	
            	showLogMessage("I", "", String.format("R81B Process 1 record=[%d]\n", totCnt));                
            
            if(cardCurrentCode.equals("0")) {
            	allData[brnPoint][3]++; // 流通卡數
            }

            if(comcr.str2long(cardLastConsumeDate) >= comcr.str2long(hBegDateBil)) {
            	allData[brnPoint][4]++; // 有效卡數
            }
            
            chkBillMonth();
            
        }
        
        showLogMessage("I", "", " Read end=" + totCnt);
    }
    /***********************************************************************/
    void chkBrn() throws Exception {
        String existFlag = "N";

        for (int i = 0; i <= brnCnt; i++) {
            if (cardAccountingNo.compareTo(allDataH[i][0]) == 0) {
                brnPoint  = i;
                existFlag = "Y";
                
                i = brnCnt + 1; // 跳開
            }
        }
        
        if (existFlag.equals("N")) {
            brnPoint  = 0;          
        }
           
if(debug == 1)
   showLogMessage("I", "", "  CHK Brn1=" + brnPoint+ ", tempBranch="+tempBranch+" ,A="+cardAccountingNo+",H="+allDataH[brnPoint][1]+","+allDataH[brnPoint][0]+",brnCnt="+brnCnt+",exist="+existFlag);
    }

    /***********************************************************************/
    void initRtn() throws Exception {
        cardCardNo = "";
        cardCurrentCode = "";
        cardAcnoPSeqno = "";
        cardIdPSeqno = "";
        cardBranch = "";
        cardAccountingNo = "";
        cardLastConsumeDate = "";
    }

    /***********************************************************************/
    void headFile() throws Exception {
        String temp = "";

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
        lparPdf.add(lpar1.get(lpar1.size() - 1));
        
        buf = "";
        buf = comcr.insertStr(buf, "合作金庫商業銀行", 55);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

        buf = "";
        buf = comcr.insertStr(buf, "分行代號: " + "3144  信用卡部", 1);
        buf = comcr.insertStr(buf, "" + rptName1, 55);
        buf = comcr.insertStr(buf, "保存年限: 五年", 117);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

        buf = "";
        tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
                hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
        buf = comcr.insertStr(buf, "報表代號: CRM81B   ", 1);
        buf = comcr.insertStr(buf, "科目代號:", 27);
        buf = comcr.insertStr(buf, "中華民國 " + tmp, 55);
        temp = String.format("%04d", pageCnt1);
        buf = comcr.insertStr(buf, "第" + temp + "頁", 121);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

        buf = "";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

        buf = "                                     未申辦信                                     本月未消    本月　 　 本月員工平 當 年 度 累積";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

        buf = "分行／部處代號                員工數 用卡戶數 流通卡數 有效卡數 有效戶數 無效戶數 費員工數 消 費 金 額  均消費金額 消  費  金 額";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

        buf = "============================= ====== ======== ======== ======== ======== ======== ======== =========== =========== =============";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

        lineCnt1 = 6;
    }

    /***********************************************************************/
    void tailFile() throws UnsupportedEncodingException {
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", ""));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

        htail.filler01 = "    合  計 :";
        tmp = String.format("%8d", sumAll1);
        htail.emplNum = tmp;
        tmp = String.format("%8d", sumAll2);
        htail.noApply = tmp;
        tmp = String.format("%8d", sumAll3);
        htail.currentCard = tmp;
        tmp = String.format("%8d", sumAll4);
        htail.effcCard = tmp;
        tmp = String.format("%8d", sumAll5);
        htail.effcIdno = tmp;
        tmp = String.format("%8d", sumAll6);
        htail.effcNoIdno = tmp;
        tmp = String.format("%8d", sumAll7);
        htail.emplNoConsume = tmp;
        tmp = comcr.commFormat("$2z,3z,3z", sumAll8);
        htail.consumeAmt = tmp;
        tmp = comcr.commFormat("$2z,3z,3z", 0);
        if (sumAll1 > 0)
            tmp = comcr.commFormat("$2z,3z,3z", sumAll8 / sumAll1);
        htail.consumeAve = tmp;
        tmp = comcr.commFormat("$z,3z,3z,3z", sumAll10);
        htail.consumeAccu = tmp;

        buf = htail.allText();
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lparPdf.add(lpar1.get(lpar1.size() - 1));


        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", ""));
        lparPdf.add(lpar1.get(lpar1.size() - 1));
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", ""));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

        buf = "";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

        buf = "分行／部處代號     員工數 用卡戶數 流通卡數 有效卡數 有效戶數 無效戶數 費員工數 消 費 金額 均消費金額 消 費 金額";
        buf = "備 註: １、本月員工平均消費金額＝本月員工消費金額／員工數";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

        buf = "       ２、本月未消費員工數未含未申辦信用卡之員工數";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

        buf = "       ３、本月員工總平均消費金額＝本月消費金額合計／員工數合計";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lparPdf.add(lpar1.get(lpar1.size() - 1));

    }

    /***********************************************************************/
    void writeFile() throws Exception {
        String tmp = "";

        if (debug == 1) showLogMessage("I", "", " Write  ALL=" + brnCnt);

        for (int i = 1; i < brnCnt+1; i++) {
            data = null;
            data = new buf1();

            if(allDataH[i][0].length() == 4 && allDataH[i][1].equals("0010")) {
            	tmp = String.format("%s %s", allDataH[i][0], comc.fixLeft(allDataH[i][2], 24));
            }
            else {
            	tmp = String.format("%s %s", allDataH[i][1], comc.fixLeft(allDataH[i][2], 24));
            }
            
            data.name = tmp;

            tmp = String.format("%6d", allData[i][1]);
            data.emplNum = tmp;
            tmp = String.format("%8d", allData[i][2]);
            data.noApply = tmp;
            tmp = String.format("%8d", allData[i][3]);
            data.currentCard = tmp;
            tmp = String.format("%8d", allData[i][4]);
            data.effcCard = tmp;
            tmp = String.format("%8d", allData[i][5]);
            data.effcIdno = tmp;
            tmp = String.format("%8d", allData[i][6]);
            data.effcNoIdno = tmp;
            tmp = String.format("%8d", allData[i][7]);
            data.emplNoConsume = tmp;
            tmp = comcr.commFormat("$2z,3z,3z", allData[i][8]);
            data.consumeAmt = tmp;
            tmp = comcr.commFormat("$2z,3z,3z", 0);
            if (allData[i][1] > 0) {
                int tmpInt = (int) (allData[i][8] / allData[i][1] + 0.5);
                tmp = comcr.commFormat("$2z,3z,3z", tmpInt);
            }
            data.consumeAve = tmp;
            tmp = comcr.commFormat("$z,3z,3z,3z", allData[i][10]);
            data.consumeAccu = tmp;

            buf = data.allText();
            lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
            lparPdf.add(lpar1.get(lpar1.size() - 1));

            lineCnt1 = lineCnt1 + 1;

            sumAll1 = sumAll1 + allData[i][1];
            sumAll2 = sumAll2 + allData[i][2];
            sumAll3 = sumAll3 + allData[i][3];
            sumAll4 = sumAll4 + allData[i][4];
            sumAll5 = sumAll5 + allData[i][5];
            sumAll6 = sumAll6 + allData[i][6];
            sumAll7 = sumAll7 + allData[i][7];
            sumAll8 = sumAll8 + allData[i][8];
            sumAll9 = sumAll9 + allData[i][9];
            sumAll10 = sumAll10 + allData[i][10];
            
            if (lineCnt1%56 == 0) {
                headFile();
            }
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

        // 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
        String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

        showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
        int errCode = commFTP.ftplogName("BREPORT", ftpCommand);

        if (errCode != 0) {
            showLogMessage("I", "",
                    String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
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
        CrdR81B proc = new CrdR81B();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /************************************************************************/
    class buft {
        String filler01;
        String emplNum;
        String noApply;
        String currentCard;
        String effcCard;
        String effcIdno;
        String effcNoIdno;
        String emplNoConsume;
        String consumeAmt;
        String consumeAve;
        String consumeAccu;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(filler01, 29 + 1);
            rtn += fixLeft(emplNum, 8 + 1);
            rtn += fixLeft(noApply, 8 + 1);
            rtn += fixLeft(currentCard, 8 + 1);
            rtn += fixLeft(effcCard, 8 + 1);
            rtn += fixLeft(effcIdno, 8 + 1);
            rtn += fixLeft(effcNoIdno, 8 + 1);
            rtn += fixLeft(emplNoConsume, 8 + 1);
            rtn += fixLeft(consumeAmt, 11 + 1);
            rtn += fixLeft(consumeAve, 11 + 1);
            rtn += fixLeft(consumeAccu, 15 + 1);
            // rtn += fixLeft(len, 1);
            return rtn;
        }


    }
    class buf1 {
        String name;
        String emplNum;
        String noApply;
        String currentCard;
        String effcCard;
        String effcIdno;
        String effcNoIdno;
        String emplNoConsume;
        String consumeAmt;
        String consumeAve;
        String consumeAccu;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(name, 29 + 1);
            rtn += fixLeft(emplNum, 6 + 1);
            rtn += fixLeft(noApply, 8 + 1);
            rtn += fixLeft(currentCard, 8 + 1);
            rtn += fixLeft(effcCard, 8 + 1);
            rtn += fixLeft(effcIdno, 8 + 1);
            rtn += fixLeft(effcNoIdno, 8 + 1);
            rtn += fixLeft(emplNoConsume, 8 + 1);
            rtn += fixLeft(consumeAmt, 11 + 1);
            rtn += fixLeft(consumeAve, 11 + 1);
            rtn += fixLeft(consumeAccu, 15 + 1);
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
