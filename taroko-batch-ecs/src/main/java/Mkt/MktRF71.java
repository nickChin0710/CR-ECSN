/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/08/07 V1.01.01  Zuwei Su    program initial                            *
*  112/08/07 V1.01.02  Kirin       media/mkt                                                                              *
*  112/08/25 V1.01.03  Zuwei Su    move to backup,copy to report,layout調整   *
*  112/08/28 V1.01.04  Zuwei Su    報表header調整   *
*  112/08/29 V1.01.05  Zuwei Su    報表header bug修正   *
*  112/09/04 V1.01.06  Kirin       change introduce_emp_no                   * 
*  112/11/24 V1.01.07  Kirin       每月2日執行 專案代碼=2023010001                  *  
******************************************************************************/
package Mkt;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codehaus.plexus.util.StringUtils;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommDate;

public class MktRF71 extends AccessDAO {
    final private String PROGNAME = "員工推展「萬事達漢來美食卡」獎勵統計表  112/09/04 V1.01.06";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;
    CommDate comDate = new CommDate();

    int DEBUG = 0;
    int DEBUG_F = 0;
    String prgmId = "MktRF71";
    String hTempUser = "";

    int reportPageLine = 45;
//    String PgmCd = "2023010002";
    String PgmCd = "2023010001";    

    String rptIdR1 = "RCRF71";
    String rptName1 = "員工推展「萬事達漢來美食卡」獎勵統計表";
    int pageCnt1 = 0, lineCnt1 = 0;
    int rptSeq1 = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf = "";
    int totCnt = 0;

    String hBusiBusinessDate = "";
    String hCallBatchSeqno = "";
    String hChiYymmdd = "";
    String hBegDateCur = "";
    String hEndDateCur = "";
    String hBegDateBil = "";
    String hEndDateBil = "";
    String applyDateS = "";
    String applyDateE = "";

    String emplId = "";
    String emplEmployNo = "";
    String emplIdPSeqno = "";
    String cardCardNo = "";
    String cardAcnoPSeqno = "";
    String cardIdPSeqno = "";
    String cardCurrentCode = "";
    String cardIssueDate = "";
    String cardIssueDatePrev = "";
    String emplUNitNo = "";
    String emplBrnChiName = "";
    String emplChiName = "";
    int brnCnt = 0;
    int brnApplyCnt = 0;
    int brnNoApplyCnt = 0;
    int brnEffcCnt = 0;
    int brnNoConsumeCnt = 0;
    int chkCnt = 0;
    int bilCnt = 0;

    int All1 = 0;
    int All2 = 0;
    int All3 = 0;
    int All0 = 0;
    int sumAll1 = 0;
    int sumAll2 = 0;
    int sumAll3 = 0;
    int sumAll4 = 0;
    String tempBrn = "";
    String tempName = "";
    int loadBilCnt = 0;

    int arrayX = 10000;
    int arrayS = 11;
    int arrayI = 11;

    String[][] allDataStr = new String[arrayX][arrayS];
    int[][] allDataInt = new int[arrayX][arrayI];

    String tmp = "";
    String temstr = "";
    String tmpstr = "";
    String tmpstr1 = "";

    buft htail = new buft();
    buf1 data = new buf1();

    private int fptr1 = -1;
    String filenameO = "";

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
                comc.errExit("Usage : MktRF71 [PROGRAM_CODE] [yyyymmdd] [seq_no] ", "");
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
                PgmCd = args[0];
            }
            if (args.length == 2 && args[1].length() == 8) {
            	hBusiBusinessDate = args[1];
            } 
            
            checkOpen();
            selectPtrBusinday();
            
        	if (!"02".equals(comc.getSubString(hBusiBusinessDate,6))) {
        		showLogMessage("I", "", "每月2日執行, 本日非執行日!!");
        		return 0;
        	}
            selectMktIntrFund();

            loadBilBill();

            initAllArray();

            selectCrdEmployee();

            if (totCnt > 0) {
                tempBrn = emplUNitNo;
                tempName = emplBrnChiName;
                // writeHead(3);
                writeDetail();
                writeTail(2);
                finalTail();
            }
            closeOutputText(fptr1);

//          String filename = String.format("%s/media/crd/%s.txt", comc.getECSHOME(), prgmId);
            String filename = String.format("/crdatacrea/BREPORT/%s.1.txt", rptIdR1);
            filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
//            comc.writeReport(filename, lpar1);
            comc.fileCopy(filenameO, filename);

            // move to backup
            String backupFilename = String.format("%s/media/mkt/backup/%s.1.txt", comc.getECSHOME(), rptIdR1);
            comc.fileMove(filenameO, backupFilename);

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(1, 0, 1); // 1: 結束
            }

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
            for (int j = 0; j < arrayS; j++) {
                allDataStr[i][j] = "";
            }
            for (int k = 0; k < arrayI; k++) {
                allDataInt[i][k] = 0;
            }
        }
    }

    // ************************************************************************
    void loadBilBill() throws Exception {
        extendField = "bill.";
        daoTable = "bil_bill";
        selectSQL = "card_no,       "
                + "id_p_seqno,    "
                + "acct_month,    "
                + "purchase_date, "
                + " case when a.txn_code in ('06','25','27','28','29') "
                + "      then a.dest_amt*-1 else a.dest_amt end  dest_amt ";
        daoTable = "bil_bill a ";
        whereStr = "where acct_month between ? and ? "
                + "  and acct_code  in ('BL','CA','IT','ID','AO','OT') "
                + "  and card_no in ( select card_no from   crd_card a , crd_idno b "
                + " where a.id_p_seqno  = b.id_p_seqno  "
                + "   and a.clerk_id   <> '') "
                + " order by id_p_seqno ";

        setString(1, hBegDateBil.substring(0, 6));
        setString(2, hEndDateBil.substring(0, 6));

        int n = loadTable();
        // setLoadData("bill.card_no"); // set key
        setLoadData("bill.id_p_seqno"); // set key

        // int lind = getLoadIndex();
        /*
         * lai test for (int k = 0; k < n; k++) {
         * showLogMessage("I","","   Load bil_bill card_no:["+n+"]"+getList("bill.card_no" ,
         * k)+","+getList("bill.id_p_seqno" , k)+","+getList("bill.acct_month" ,
         * k)+","+getListDouble("bill.dest_amt" , k)); }
         */

        showLogMessage("I", "",
                "Load bil_bill end Count: ["
                        + n
                        + "]"
                        + hBegDateBil.substring(0, 6)
                        + ","
                        + hEndDateBil.substring(0, 6));
    }

    // ************************************************************************
    void chkBillMonth() throws Exception {
        String billPurchaseDate = "";
        String billAcctMonth = "";
        int billDestAmt = 0;

        bilCnt = 0;
        for (int g = 0; g < loadBilCnt; g++) {
            billPurchaseDate = getValue("bill.purchase_date", g);
            billAcctMonth = getValue("bill.acct_month", g);
            billDestAmt = (int) getValueDouble("bill.dest_amt", g);
            // 全新戶有效卡:6個月內沒有任一張有效卡 非全新戶有效卡:有任一張有效卡
            // 有效卡是半年內有消費
            if (billAcctMonth.compareTo(cardIssueDatePrev.substring(0, 6)) >= 0
                    && billAcctMonth.compareTo(cardIssueDate.substring(0, 6)) <= 0) {
                bilCnt++;
            }
            if (DEBUG_F == 1) {
                showLogMessage("I", "",
                        "    888 bill="
                                + billAcctMonth
                                + ","
                                + cardIssueDate
                                + ","
                                + billDestAmt
                                + ","
                                + billPurchaseDate
                                + ",消費="
                                + bilCnt
                                + ","
                                + getValue("bill.acct_month", g)
                                + ",G="
                                + g);
            }
        }
    }

    /***********************************************************************/
    public void selectMktIntrFund() throws Exception {
        sqlCmd = "select apply_date_s ";
        sqlCmd += "     , apply_date_e ";
        sqlCmd += " from mkt_intr_fund ";
        sqlCmd += "where program_code = ? ";
        sqlCmd += "  and apr_flag     = 'Y' ";
        setString(1, PgmCd);

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            applyDateS = getValue("apply_date_s");
            applyDateE = getValue("apply_date_e");
        }
        if (notFound.equals("Y")) {
            comcr.errRtn("select mkt_intr_fund not found!", PgmCd, hCallBatchSeqno);
        }
        showLogMessage("I", "", String.format("專案代碼=[%s][%s][%s]", PgmCd, applyDateS, applyDateE));
//        hBegDateBil = comDate.dateAdd(applyDateS, 0, -6, 0).substring(0, 6) + "01";
//        hEndDateBil = applyDateE;
        hBegDateBil = comDate.dateAdd(hBusiBusinessDate, 0, -6, 0).substring(0, 6) + "01";
        hEndDateBil = comDate.dateAdd(hBusiBusinessDate, 0, -1, 0).substring(0, 6) + "31";
        
        showLogMessage("I", "", String.format("    帳單最大區間[%s][%s]", hBegDateBil, hEndDateBil));
    }

    /***********************************************************************/
    void checkOpen() throws Exception {
//      filenameO = String.format("%s/media/crd/%s.txt", comc.getECSHOME(), rptIdR1 + ".1");
        filenameO = String.format("%s/media/mkt/%s.1.txt", comc.getECSHOME(), rptIdR1);
        filenameO = Normalizer.normalize(filenameO, java.text.Normalizer.Form.NFKD);
        comc.mkdirsFromFilenameWithPath(filenameO);
        fptr1 = openOutputText(filenameO, "MS950");
        showLogMessage("I", "", String.format("Open file=[%s]", filenameO));
        if (fptr1 == -1) {
            comc.errExit("在程式執行目錄下沒有權限讀寫", filenameO);
        }
    }

    /***********************************************************************/
    public int selectPtrBusinday() throws Exception {

        sqlCmd = "select to_char(sysdate,'yyyymmdd') as business_date";
        sqlCmd +=
                "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                    : hBusiBusinessDate;
        }

        sqlCmd = "select to_char(add_months(to_date(?,'yyyymmdd'),-6),'yyyymm')||'01' hBegDateBil ";
        sqlCmd +=
                "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') hEndDateBil ";
        sqlCmd += "     , to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm')||'01' h_beg_date ";
        sqlCmd +=
                "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') h_end_date ";
        sqlCmd += " from dual ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);

        recordCnt = selectTable();
        if (recordCnt > 0) {
            hEndDateCur = getValue("h_end_date");
            hBegDateCur = hEndDateCur.substring(0, 4) + "0101";
            hBegDateBil = getValue("hBegDateBil");
            hEndDateBil = getValue("hEndDateBil");
        }

        hChiYymmdd = getValue("h_chi_yymmdd");
        showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s]", hBusiBusinessDate,
                hChiYymmdd, hBegDateCur, hEndDateCur, hBegDateBil, hEndDateBil));

        return 0;
    }

    /***********************************************************************/
    void chkCrdCard() throws Exception {
        sqlCmd = " select count(*) as chkCnt  ";
        sqlCmd += "   from crd_card a  ";
        sqlCmd += "  where a.id_p_seqno = ? ";
        sqlCmd += "    and a.card_no   != ? ";
        sqlCmd += "    and a.issue_date between ? and ? ";

        setString(1, emplIdPSeqno);
        setString(2, cardCardNo);
        setString(3, cardIssueDate);
        setString(4, cardIssueDatePrev);

        int recordCnt = selectTable();

        chkCnt = getValueInt("chkCnt");

    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {

        sqlCmd = " select ";
        sqlCmd += "  c.card_no             ,c.id_p_seqno        , ";
        sqlCmd += "  c.acno_p_seqno        ,c.issue_date         ";
        sqlCmd += "   from crd_card c  ";
//      sqlCmd += "  where c.clerk_id     = ? ";
        sqlCmd += "  where c.introduce_emp_no     = ? ";
        sqlCmd += "    and c.issue_date   between ? and ?  ";
        sqlCmd += "    and c.group_code   in ('1631', '1677')  ";
        /*
         * lai test sqlCmd += "    and c.group_code   in ('1684','1685') "; sqlCmd +=
         * "    and c.current_code = '0' ";
         */
        sqlCmd += "  order by c.issue_date desc ";

        setString(1, emplEmployNo);
        setString(2, applyDateS);
        setString(3, applyDateE);

        int recordCnt = selectTable();
        if (DEBUG_F == 1) {
            showLogMessage("I", "", " clerk=" + emplEmployNo + " ," + recordCnt);
        }

        for (int k = 0; k < recordCnt; k++) {
            cardCardNo = getValue("card_no", k);
            cardAcnoPSeqno = getValue("acno_p_seqno", k);
            cardIdPSeqno = getValue("id_p_seqno", k);
            cardCurrentCode = getValue("current_code", k);
            cardIssueDate = getValue("issue_date", k);
            cardIssueDatePrev = comDate.dateAdd(cardIssueDate, 0, -6, 0).substring(0, 6) + "01"; // 6個月內

            chkCrdCard();

            loadBilCnt = 0;
            setValue("bill.id_p_seqno", cardIdPSeqno);
            loadBilCnt = getLoadData("bill.id_p_seqno");
            if (DEBUG_F == 1) {
                showLogMessage("I", "", "   888 bill cnt=" + cardIdPSeqno + "," + loadBilCnt);
            }
            chkBillMonth();

            if (DEBUG_F == 1) {
                showLogMessage("I", "",
                        "  Card="
                                + cardCardNo
                                + " ,"
                                + cardIdPSeqno
                                + " , idx="
                                + k
                                + ",C="
                                + chkCnt
                                + ",B="
                                + bilCnt);
            }
            // 全新戶有效卡:6個月內沒有任一張有效卡 非全新戶有效卡:有任一張有效卡
            // 有效卡是半年內有消費
            if (chkCnt < 1 || bilCnt < 1) {
                allDataInt[brnCnt][1]++;
            }
            if (chkCnt > 0 && bilCnt > 0) {
                allDataInt[brnCnt][2]++;
            }
        }

    }

    /***********************************************************************/
    void selectCrdEmployee() throws Exception {
        fetchExtend = "main.";
        sqlCmd = "select ";
        sqlCmd += "  a.unit_no             ,a.employ_no         , a.id  , ";
        sqlCmd += "  b.id_p_seqno          ,b.chi_name          , ";
        sqlCmd += "  c.brief_chi_name                             ";
        sqlCmd += "  from gen_brn c,  crd_employee a ";
        sqlCmd += "  full outer join crd_idno b on b.id_no  = a.id "; // and b.id_no_code =
                                                                      // a.id_code ";
        sqlCmd += " where c.branch       = a.unit_no ";
        sqlCmd += "   and a.employ_no   <> ''        ";
        sqlCmd += " order by a.unit_no ,b.id_p_seqno ";

        openCursor();

        while (fetchTable()) {
            initRtn();
            totCnt++;

            emplId = getValue("main.id");
            emplEmployNo = getValue("main.employ_no");
            emplIdPSeqno = getValue("main.id_p_seqno");
            emplUNitNo = getValue("main.unit_no");
            emplChiName = getValue("main.chi_name");
            emplBrnChiName = getValue("main.brief_chi_name");
            if (DEBUG == 1) {
                showLogMessage("I", "",
                        "Read brn="
                                + emplUNitNo
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
                showLogMessage("I", "", String.format("MktRF71 Process 1 record=[%d]\n", totCnt));
            }

            if (totCnt == 1) {
                tempBrn = emplUNitNo;
                tempName = emplBrnChiName;
                writeRptHead(0);
                writeHead(0);
            }
            if (tempBrn.compareTo(emplUNitNo) != 0) {
                writeDetail();

                writeTail(1);

                writeRptHead(1);
                writeHead(1);

                initAllArray();

                tempBrn = emplUNitNo;
                tempName = emplBrnChiName;
            }


            brnCnt++;

            allDataStr[brnCnt][0] = emplId; // 姓名
            allDataStr[brnCnt][1] = comc.fixLeft(emplChiName, 20); // 姓名

            selectCrdCard();

//            兌換獎勵：A + B
//            A = 2*全新戶有效卡數
//            B = 非全新戶有效卡數/2  取整數
            allDataInt[brnCnt][3] = 2 * allDataInt[brnCnt][1] + allDataInt[brnCnt][2] / 2;
            if (DEBUG == 1) {
                showLogMessage("I", "",
                        "   Dtl="
                                + brnCnt
                                + ","
                                + allDataInt[brnCnt][1]
                                + ","
                                + allDataInt[brnCnt][2]
                                + ","
                                + allDataInt[brnCnt][3]);
            }
        }

        showLogMessage("I", "", " Read end=" + totCnt);

    }

    /***********************************************************************/
    void initRtn() throws Exception {
        cardCardNo = "";
        cardCurrentCode = "";
        cardAcnoPSeqno = "";
        emplBrnChiName = "";
        emplChiName = "";
        emplIdPSeqno = "";
        emplUNitNo = "";
    }

    /***********************************************************************/
    void writeRptHead(int idx) throws Exception {
        buf = "";
        String tmpStr1 = "";
        String tmpStr2 = "";

        tmpStr1 = tempBrn;
        tmpStr2 = hChiYymmdd + rptName1;
        if (idx == 1) {
            tmpStr1 = emplUNitNo;
        }


        buf = comc.fixLeft(tmpStr1, 10) + comc.fixLeft(rptIdR1, 16) + comc.fixLeft(tmpStr2, 88)
                + comc.fixLeft("N", 8);

        writeTextFile(fptr1, buf + "\n");
    }

    /***********************************************************************/
    void writeHead(int idx) throws Exception {
        String temp = "";

        pageCnt1++;
        if (pageCnt1 > 1) {
            lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));
        }

        if (DEBUG_F == 1) {
            showLogMessage("I", "",
                    "   Write Head="
                            + idx
                            + ",Page="
                            + pageCnt1
                            + ","
                            + tempBrn
                            + ","
                            + emplUNitNo);
        }

        buf = "";
        
        buf = comcr.insertStr(buf,"分行代號: 3144 信用卡部", 1);
        buf = comcr.insertStr(buf, "" + comc.fixLeft(rptName1, 50), 45);
        buf = comcr.insertStr(buf, "保存年限: 二年", 100);
        writeFile(buf);

        buf = "";
        tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
                hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
        buf = comcr.insertStr(buf, "報表代號: CRF71    科目代號:", 1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp, 52);
        temp = String.format("%4d", pageCnt1);
        buf = comcr.insertStr(buf, "頁    次:" + temp, 100);
        writeFile(buf);

        writeFile(StringUtils.leftPad("", 116, "="));
        if (idx == 1) {
            buf = comcr.insertStr(buf,
                    "薪資單位: " + emplUNitNo + " " + comc.fixLeft(emplBrnChiName, 10), 1);
        } else {
            buf = comcr.insertStr(buf, "薪資單位: " + tempBrn + " " + comc.fixLeft(tempName, 10), 1);
        }
        writeFile(buf);

        buf = "員工ＩＤ   推廣員工姓名           Ｍ漢來美食卡   Ｍ漢來美食卡   兌換獎勵 ";
        writeFile(buf);

        buf = "                                    全新戶　　　　　非全新戶　　（漢來來拌麵）";
        writeFile(buf);

        buf = "========== ==================== ============== ================ ============== ================";
        writeFile(buf);

        lineCnt1 = 6;
    }

    /***********************************************************************/
    void writeFile(String buf) throws Exception {
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf + "\r"));

        writeTextFile(fptr1, buf + "\n");
    }

    /***********************************************************************/
    void writeTail(int idx) throws Exception {
        String tmp1 = "";

        htail.str01 = "    小  計 : " + comcr.commFormat("3z,3z", brnCnt);
        htail.int01 = comcr.commFormat("3z,3z,3z", sumAll1);
        htail.int02 = comcr.commFormat("3z,3z,3z", sumAll2);
        htail.int03 = comcr.commFormat("3z,3z,3z", sumAll3);
        buf = htail.allText();
        writeFile(buf);

        writeFile(" ");

        All0 += brnCnt;
        All1 += sumAll1;
        All2 += sumAll2;
        All3 += sumAll3;
    }

    /***********************************************************************/
    void finalTail() throws Exception {

        htail.str01 = "    總  計 : " + comcr.commFormat("3z,3z", All0);
        htail.int01 = comcr.commFormat("3z,3z,3z", All1);
        htail.int02 = comcr.commFormat("3z,3z,3z", All2);
        htail.int03 = comcr.commFormat("3z,3z,3z", All3);
        buf = htail.allText();
        writeFile(buf);

        writeFile(" ");


        buf = "說 明:";
        writeFile(buf);

        buf = " １、活動期間: " + applyDateS + " ～ " + applyDateE;
        writeFile(buf);

        buf = " ２、符合資格條件:";
        writeFile(buf);

        buf = " （１） 全新戶: 每推廣ＭａｓｔｅｒＣａｒｄ漢來美食鈦金卡（６７７）或ＭａｓｔｅｒＣａｒｄ漢來美食世界卡（６３１）";
        writeFile(buf);
        buf = "                之任１卡全新戶，可獲「漢來美食來拌麵」１組，且須為有效卡．";
        writeFile(buf);

        buf = " （２） 非全新戶: 每推廣ＭａｓｔｅｒＣａｒｄ漢來美食鈦金卡（６７７）或ＭａｓｔｅｒＣａｒｄ漢來美食世界卡（６３１）";
        writeFile(buf);
        buf = "                　之任２卡非全新戶，可獲「漢來美食來拌麵」１組，且須為有效卡．";
        writeFile(buf);

    }

    /***********************************************************************/
    void writeDetail() throws Exception {
        String tmp = "";

        if (DEBUG_F == 1) {
            showLogMessage("I", "", "   Write Dtl=" + brnCnt + "," + tempBrn + "," + emplUNitNo);
        }

        for (int i = 1; i < brnCnt + 1; i++) {
            if (DEBUG_F == 1) {
                showLogMessage("I", "",
                        "   Write Max="
                                + reportPageLine
                                + ","
                                + lineCnt1
                                + ","
                                + allDataStr[i][0]);
            }
            if (lineCnt1 > reportPageLine) {
                writeRptHead(2);
                writeHead(2);
            }

            data = null;
            data = new buf1();

            data.str01 = String.format("%s", allDataStr[i][0]);
            data.str02 = String.format("%s", allDataStr[i][1]);
            data.int01 = comcr.commFormat("3z,3z,3z", allDataInt[i][1]);
            data.int02 = comcr.commFormat("3z,3z,3z", allDataInt[i][2]);
            data.int03 = comcr.commFormat("3z,3z,3z", allDataInt[i][3]);

            buf = data.allText();
            writeFile(buf);

            lineCnt1 = lineCnt1 + 1;

            sumAll1 += allDataInt[i][1];
            sumAll2 += allDataInt[i][2];
            sumAll3 += allDataInt[i][3];
        }

        return;
    }

    /************************************************************************/
    public static void main(String[] args) throws Exception {
        MktRF71 proc = new MktRF71();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /************************************************************************/
    class buft {
        String filler01;
        String str01;
        String str02;
        String int01;
        String int02;
        String int03;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(str01, 31 + 1);
            rtn += fixLeft(int01, 16 + 1);
            rtn += fixLeft(int02, 16 + 1);
            rtn += fixLeft(int03, 16 + 1);
            return rtn;
        }


    }
    class buf1 {
        String str01;
        String str02;
        String int01;
        String int02;
        String int03;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(str01, 10 + 1);
            rtn += fixLeft(str02, 20 + 1);
            rtn += fixLeft(int01, 16 + 1);
            rtn += fixLeft(int02, 16 + 1);
            rtn += fixLeft(int03, 16 + 1);
            // rtn += fixLeft(len , 1);
            return rtn;
        }


    }

    String fixLeft(String str, int len) throws UnsupportedEncodingException {
        int size = (Math.floorDiv(len, 100) + 1) * 100;
        String spc = "";
        for (int i = 0; i < size; i++) {
            spc += " ";
        }
        if (str == null) {
            str = "";
        }
        str = str + spc;
        byte[] bytes = str.getBytes("MS950");
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, 0, vResult, 0, len);

        return new String(vResult, "MS950");
    }

}
