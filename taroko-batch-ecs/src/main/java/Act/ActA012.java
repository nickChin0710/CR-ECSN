/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/06/21  V1.00.00    David     program initial                           *
*  109/11/11  V1.00.02    shiyuqi   updated for project coding standard       *
*  109-12-30  V1.00.03    Zuwei     “兆豐國際商銀信用卡”改為”合作金庫銀行信用卡”     *
*  111/10/25  V1.00.04    Simon     sync codes with mega                      *
*  111/12/10  V1.00.05    Simon     get parameter overpayment_lmt             *
*  112/10/18  V1.00.06    Simon     1.fixed sqlCmd in selectActAcct()         *
*                                   2.修改為 by acct_type 讀取 ptr_actgeneral_n*
*                                   3.出報表檔案更改為出 online report "ptr_batch_rpt"*
*  112/10/19  V1.00.07    Simon     表報"最早往來日" crd_idno.card_since 改抓 min(crd_card.ori_issue_date)*
******************************************************************************/

package Act;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ActA012 extends AccessDAO {

  //private String progname = "防制洗錢客戶及美國Fatca規定溢繳款額大xx萬元之報表處理程式  111/10/25  V1.00.04";
    private String progname = "防制洗錢客戶及溢繳款額大xxx萬元之報表處理程式  112/10/19  V1.00.07";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptName1 = "ACT_A012R1";
    int rptSeq1 = 0;
    String hCallBatchSeqno = "";
    private String hBusiBusinessDate = "";
    private String hLastDate = "";
    private String hLastDate1 = "";
    private int hAgenPaymentLmt = 0;
    private int hPtrOverPayLmt = 0;
    private long totalCnt = 0;
    private String hAcctPSeqno = "";
    private String hAcctAcctType = "";
    private String hAcctAcctKey = "";
    private String hAcctIdPSeqno = "";
    private double hAcctEndBalOp = 0;
    private String hCorpChiName = "";
    private String hIdnoCardSince = "";
    private int pageCnt = 0;
    private int lineCnt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================

            if ((args.length != 0) && (args.length != 1)) {
                comc.errExit("Usage : ActA012 [business_date]", "");
            }
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            hBusiBusinessDate = "";
            if (args.length == 1 && args[0].length() == 8) {
                if (args[0].chars().allMatch( Character::isDigit ) ) {
                	hBusiBusinessDate = args[0];
                } 
            }
            selectPtrBusinday();

            showLogMessage("I", "",
                    String.format("今日[%s],  月底[%s],  月底前一天[%s]. \n", hBusiBusinessDate, hLastDate, hLastDate1));
            if (hBusiBusinessDate.equals(hLastDate) == false) {
                showLogMessage("I", "", "今日非月底最後一天, 不執行 \n");
                finalProcess();
                return 0;
            }
          //selectPtrActgeneraln();
          //printHeader();
            selectActAcct();
          //printPeroration();
          //String filename = String.format("%s/reports/ACT_A012_%s", comc.getECSHOME(),
          //        comc.getSubString(hBusiBusinessDate, 4));
          //comc.writeReport(filename, lpar1);
            if (totalCnt > 0) {
              printPeroration();
              comcr.insertPtrBatchRpt(lpar1); /* online報表 */
            }

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /*****************************************************************************/
    void selectPtrBusinday() throws Exception {

        sqlCmd = "select business_date from ptr_businday";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (hBusiBusinessDate.length() == 0)
            hBusiBusinessDate = getValue("business_date");
        // ============================================================
        sqlCmd = "select to_char(last_day(to_date(?, 'yyyymmdd'))-1 days, 'yyyymmdd') as h_last_date1,  "
                + " to_char(last_day(to_date(?, 'yyyymmdd')), 'yyyymmdd') as h_last_date " + " from dual";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday 2 not found!", "", hCallBatchSeqno);
        }
        hLastDate1 = getValue("h_last_date1");
        hLastDate = getValue("h_last_date");
    }

    /*****************************************************************************/
    void selectActAcct() throws Exception {

        sqlCmd = "SELECT  a.p_seqno, ";
        sqlCmd += " d.overpayment_lmt, ";
        sqlCmd += " a.acct_type, ";
        sqlCmd += " c.acct_key, ";
        sqlCmd += "  a.id_p_seqno, ";
        sqlCmd += " (a.end_bal_op + a.end_bal_lk) as end_bal , ";
        sqlCmd += "  b.chi_name, ";
        sqlCmd += " b.card_since ";
        sqlCmd += " FROM   act_acct a, crd_idno b, act_acno c, ptr_actgeneral_n d ";
        sqlCmd += " WHERE a.end_bal_op + a.end_bal_lk >= d.overpayment_lmt * 10000 ";
        sqlCmd += " AND a.id_p_seqno = b.id_p_seqno ";
        sqlCmd += " AND a.acct_type  = d.acct_type ";
        sqlCmd += " AND c.acno_p_seqno = a.p_seqno ";
        sqlCmd += " AND d.overpayment_lmt > 0 ";
/***
        sqlCmd += " UNION ALL ";
        sqlCmd += " SELECT  a.p_seqno, ";
        sqlCmd += " a.acct_type, ";
        sqlCmd += " c.acct_key, ";
        sqlCmd += " a.id_p_seqno, ";
        sqlCmd += " (a.end_bal_op + a.end_bal_lk) as end_bal , ";
        sqlCmd += " b.chi_name, ";
        sqlCmd += " b.card_since ";
        sqlCmd += " FROM   act_acct a, crd_corp b, act_acno c ";
        sqlCmd += " WHERE a.end_bal_op + a.end_bal_lk >= ? * 10000 ";
        sqlCmd += " AND a.corp_p_seqno = b.corp_p_seqno ";
        sqlCmd += " AND c.acno_p_seqno = a.p_seqno ";
        sqlCmd += " AND ? > 0 ";
***/
/***
        setInt(1, hPtrOverPayLmt);
        setInt(2, hPtrOverPayLmt);
        setInt(3, hPtrOverPayLmt);
        setInt(4, hPtrOverPayLmt);
***/

        openCursor();
        while (fetchTable()) {

            hPtrOverPayLmt = getValueInt("overpayment_lmt");
            hAcctPSeqno = getValue("p_seqno");
            hAcctAcctType = getValue("acct_type");
            hAcctAcctKey = getValue("acct_key");
            hAcctIdPSeqno = getValue("id_p_seqno");
            hAcctEndBalOp = getValueDouble("end_bal");
            hCorpChiName = getValue("chi_name");
          //hIdnoCardSince = getValue("card_since");
            selectCrdCard();

            totalCnt++;
            if (totalCnt == 1) {
              printHeader();
            }
            showLogMessage("I", "", String.format("acct_key[%s]", hAcctAcctKey));
            printDetail();

        }
        closeCursor();
    }

    void selectCrdCard() throws Exception {
  		hIdnoCardSince = "";
	  	sqlCmd = "select min(ori_issue_date) as min_ori_issue_date ";
		  sqlCmd += " from crd_card  ";
		  sqlCmd += "where p_seqno = ? ";
		  setString(1, hAcctPSeqno);
		  selectTable();
  	  hIdnoCardSince = getValue("min_ori_issue_date");
    }

    /*****************************************************************************/
    void selectPtrActgeneraln() throws Exception {
        hAgenPaymentLmt = 0;

      //sqlCmd = "select decode(payment_lmt, 0, 150, payment_lmt) as h_agen_payment_lmt ";
        sqlCmd = "select overpayment_lmt ";
        sqlCmd += " from ptr_actgeneral_n ";
        sqlCmd += " fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actgeneral_n not found!", "", hCallBatchSeqno);
        }

      //hAgenPaymentLmt = getValueInt("h_agen_payment_lmt");
        hPtrOverPayLmt = getValueInt("overpayment_lmt");
    }

    /*****************************************************************************/
    void printHeader() {
        pageCnt++;

        String buf = "";
        buf = comcr.insertStr(buf, " " + comcr.bankName + " ", 26);
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱:" + rptName1, 1);
        buf = comcr.insertStr(buf, "合作金庫銀行信用卡", 27);
        buf = comcr.insertStr(buf, "頁    次:", 67);
        String temp = String.format("%8d", pageCnt);
        buf = comcr.insertStr(buf, temp, 77);
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
      //buf = comcr.insertStr(buf, "防制洗錢客戶及美國Fatca規定溢繳款額大於50萬元報表", 10);
        buf = comcr.insertStr(buf, "防制洗錢客戶及溢繳款額大於", 10);
        temp = String.format("%3d", hPtrOverPayLmt);
        buf = comcr.insertStr(buf, temp, 36);
        buf = comcr.insertStr(buf, "萬元報表", 39);
        buf = comcr.insertStr(buf, "印表日期:", 67);
        buf = comcr.insertStr(buf, hBusiBusinessDate, 77);
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", ""));

        buf = "";
        buf = comcr.insertStr(buf, "  客戶ID帳戶                  姓   名　   　             溢 繳 金 額        最早往來日", 1);
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
        buf = "";
        buf = comcr.insertStr(buf,
                "=================  ================================  =================  =========== ", 1);
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt = 5;
    }

    /*************************************************************************/
    void printDetail() {

        String buf = "";
        buf = comcr.insertStr(buf, hAcctAcctType, 2);
        buf = comcr.insertStr(buf, hAcctAcctKey, 5);
        buf = comcr.insertStr(buf, hCorpChiName, 21);
        String szTmp = comcr.commFormat("3$,3$,3$,3$", hAcctEndBalOp);
        buf = comcr.insertStr(buf, szTmp, 55);
        buf = comcr.insertStr(buf, hIdnoCardSince, 74);
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt++;

        if (lineCnt >= 45) {
            lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", "\f"));
            printHeader();
        }
    }

    /*****************************************************************************/
    void printPeroration() {
        String buf = "";
        buf = comcr.insertStr(buf, "合  計:", 2);
        String szTmp = String.format("%4d", totalCnt);
        buf = comcr.insertStr(buf, szTmp, 9);
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", "~\n"));
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        ActA012 proc = new ActA012();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
