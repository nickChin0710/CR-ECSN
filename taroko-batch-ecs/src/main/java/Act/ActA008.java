/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/08  V1.00.01    SUP       error correction                          *
 *  109/11/11  V1.00.02    shiyuqi       updated for project coding standard   *
 *  112/10/17  V1.00.03    Simon     1.fixed sqlCmd in selectActDebtCancel()   *
 *                                   2.參數檔 ptr_actgeneral 改抓 ptr_actgeneral_n*
 *  112/10/18  V1.00.04    Simon     修改 lpar.rptName1="ACT_A008R1"           *
 ******************************************************************************/

package Act;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*防制洗錢客戶繳款金額大於xxx萬元之報表處理程式*/
public class ActA008 extends AccessDAO {

    private final boolean debug = false;

    private String progname = "防制洗錢客戶繳款金額大於xxx萬元之報表處理程式 "
                            + "112/10/18  V1.00.04 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActA008";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String hCallRProgramCode = "";

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
  //String rptName1 = "act_a008";
    String rptName1 = "ACT_A008R1";
    String prgName1 = "防制洗錢客戶繳款金額大於xxx萬元之報表";

    int rptSeq1 = 0;
    String buf = "";
    String szTmp = "";

    String hBusiBusinessDate = "";
    double hAgenPaymentLmt = 0;
    double hAdclPayAmt = 0;
    String hAdclAcctKey = "";
    String hAdclAcctType = "";
    String dbChiCode = ""; /* 本國人 繳款編號4-5 */
    String dbChiKey = ""; /* 本國人 繳款編號6-14 */
    String dbForeignKey = ""; /* 外國人 繳款編號4-10 */
    String dbForeignCode1 = ""; /* 外國人 繳款編號11-12 */
    String dbForeignCode2 = ""; /* 外國人 繳款編號13-14 */
    String hPaymentNo2 = "";
    String hIdnoChiName = "";
    String hPaccAtmCode = "";
    String hPrintName = "";
    String hRptName = "";

    String hAdclModUser = "";
    String hAdclModPgm = "";
    String hExecDate = "";
    int rowsCount = 0;
    double payAmtTotal = 0;
    String dbPaymentNotice = "";
    int lineCnt = 0;
    int pageCnt = 0;
    int nColumnPerPage = 27;  //設定一頁顯示行數

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : ActA008 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            hModUser = comc.commGetUserID();
            hAdclModUser = hModUser;
            hAdclModPgm = javaProgram;

            selectPtrBusinday();

            String temstr = String.format("%s/reports/ACT_A008_%s", comc.getECSHOME(),
                    hBusiBusinessDate.substring(4));
            temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
            showLogMessage("I", "", String.format("報表名稱 : %s\n", temstr));

          //selectPtrActgeneral();
            selectActDebtCancel();

//            comc.writeReport(temstr, lpar1);
            comcr.insertPtrBatchRpt(lpar1); /* online報表 */
            
          //lpRtn("ACT_A008");

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

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        String strb = "";

        hBusiBusinessDate = "";

        sqlCmd = "select business_date "; /* 今天營業日 */
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

        strb = String.format("%07d", comcr.str2long(hBusiBusinessDate) - 19110000);
        hExecDate = strb;
    }

    /***********************************************************************/
    void selectPtrActgeneral() throws Exception {
        hAgenPaymentLmt = 0;

        sqlCmd = "select decode(payment_lmt, 0, 150, payment_lmt) h_agen_payment_lmt ";
        sqlCmd += " from ptr_actgeneral  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actgeneral not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAgenPaymentLmt = getValueDouble("h_agen_payment_lmt");
        }

    }

    /***********************************************************************/
    void selectActDebtCancel() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " d.payment_lmt,";
        sqlCmd += " a.pay_amt h_adcl_pay_amt,";
        sqlCmd += " b.acct_key,";
        sqlCmd += " a.acct_type,";
        sqlCmd += " decode(substr(b.acct_key, 1,1),'A','10','B','11','C','12','D','13','E','14'"
                + ",'F','15','G','16','H','17','I','18','J','19'" + ",'K','20','L','21','M','22','N','23','O','24'"
                + ",'P','25','Q','26','R','27','S','28','T','29'" + ",'U','30','V','31','W','32','X','33','Y','34'"
                + ",'Z','35','36') db_chi_code,";
        sqlCmd += " substr(b.acct_key,2,9) db_chi_key,";
        sqlCmd += " substr(b.acct_key,2,7) db_foreign_key,";
        sqlCmd += " decode(substr(b.acct_key, 9,1),'A','10','B','11','C','12','D','13','E','14'"
                + ",'F','15','G','16','H','17','I','18','J','19'" + ",'K','20','L','21','M','22','N','23','O','24'"
                + ",'P','25','Q','26','R','27','S','28','T','29'" + ",'U','30','V','31','W','32','X','33','Y','34'"
                + ",'Z','35','36') db_foreign_code1,";
        sqlCmd += " decode(substr(b.acct_key,10,1),'A','10','B','11','C','12','D','13','E','14'"
                + ",'F','15','G','16','H','17','I','18','J','19'" + ",'K','20','L','21','M','22','N','23','O','24'"
                + ",'P','25','Q','26','R','27','S','28','T','29'" + ",'U','30','V','31','W','32','X','33','Y','34'"
                + ",'Z','35','36') db_foreign_code2,";
        sqlCmd += " b.payment_no_ii,  ";
        sqlCmd += " uf_hi_cname(c.chi_name) h_idno_chi_name ";
        sqlCmd += "  from act_debt_cancel a, crd_idno c, act_acno b, ptr_actgeneral_n d ";
      //sqlCmd += "  from crd_idno c, act_debt_cancel a ";
		  //sqlCmd += " left join act_acno b on a.id_p_seqno = b.id_p_seqno ";
        sqlCmd += " where a.id_p_seqno = c.id_p_seqno ";
        sqlCmd += "   and a.p_seqno = b.acno_p_seqno ";
        sqlCmd += "   and b.acct_type = d.acct_type ";
        sqlCmd += "   and a.process_flag != 'Y' ";
        sqlCmd += "   and a.pay_amt > d.payment_lmt * 10000 ";
      //sqlCmd += "   and a.pay_amt > ? * 10000 ";
      //setDouble(1, hAgenPaymentLmt);
        boolean bNoRecord = true;
        openCursor();
        while(fetchTable()) {
            bNoRecord = false;
            hAgenPaymentLmt = getValueDouble("payment_lmt");
            hAdclPayAmt = getValueDouble("h_adcl_pay_amt"); /* 金額 */
            hAdclAcctKey = getValue("acct_key");
            hAdclAcctType = getValue("acct_type");
            dbChiCode = getValue("db_chi_code"); /* 本國人 繳款編號4-5 */
            dbChiKey = getValue("db_chi_key"); /* 本國人 繳款編號6-14 */
            dbForeignKey = getValue("db_foreign_key"); /* 外國人 繳款編號4-10 */
            dbForeignCode1 = getValue("db_foreign_code1"); /* 外國人 繳款編號11-12 */
            dbForeignCode2 = getValue("db_foreign_code2"); /* 外國人 繳款編號13-14 */
            hPaymentNo2 = getValue("payment_no_ii");
            hIdnoChiName = getValue("h_idno_chi_name"); /* 中文名稱 */
         
            payAmtTotal = payAmtTotal + hAdclPayAmt; /* 總金額 */

            if (debug) showLogMessage("I", "", String.format("acct_key[%s]", hAdclAcctKey));
            selectPtrAcctType();

/***
            if (dbChiCode.equals("36")) {
                dbPaymentNotice = hPaccAtmCode + dbForeignKey + dbForeignCode1 + dbForeignCode2;
            } else {
                dbPaymentNotice = hPaccAtmCode + dbChiCode + dbChiKey;
            }
***/
            dbPaymentNotice = hPaymentNo2;
            if (lineCnt > nColumnPerPage) {
                lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", "##PPP"));
                lineCnt = 0;
            }
            if (lineCnt==0)
            {
                  printHeader();
            }
            printDetail();
        }
        closeCursor();
        if (bNoRecord) {
            printHeader();
        }
        printPeroration();
    }

    /***********************************************************************/
    void selectPtrAcctType() throws Exception {
        hPaccAtmCode = "";

        sqlCmd = "select atm_code ";
        sqlCmd += " from ptr_acct_type  ";
        sqlCmd += "where acct_type = ? ";
        setString(1, hAdclAcctType);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_acct_type not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPaccAtmCode = getValue("atm_code");
        }

    }

    /***********************************************************************/
    void printHeader() throws Exception {
        String temp = "";
        pageCnt++;
        
        
        buf = "";
        buf = comcr.insertStr(buf, " " + comcr.bankName + " ", 26);
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱:ACT_A008R1", 2);
        buf = comcr.insertStr(buf, "防制洗錢客戶繳款金額大於", 22);
        temp = String.format("%4.0f", hAgenPaymentLmt);
        buf = comcr.insertStr(buf, temp, 46);
        buf = comcr.insertStr(buf, "萬元之報表", 50);
        buf = comcr.insertStr(buf, "頁次:", 68);
        temp = String.format("%04d", pageCnt);
        buf = comcr.insertStr(buf, temp, 73);
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "執行日期:", 2);
        buf = comcr.insertStr(buf, hExecDate, 11);
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "繳款代號", 6);
        buf = comcr.insertStr(buf, "姓名", 28);
        buf = comcr.insertStr(buf, "ACCT_KEY", 46);
        buf = comcr.insertStr(buf, "金額", 68);
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));

        buf = "================================================================================";
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));
        return;
    }

    /***********************************************************************/
    void printDetail() throws Exception {
        buf = "";

        buf = comcr.insertStr(buf, dbPaymentNotice, 3);
        buf = comcr.insertStr(buf, hIdnoChiName, 28);
        buf = comcr.insertStr(buf, hAdclAcctKey, 43);

        szTmp = comcr.commFormat("3$,3$,3$,3$", hAdclPayAmt);
        buf = comcr.insertStr(buf, szTmp, 60);

        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt++;
        rowsCount++;
        return;
    }

    /***********************************************************************/
    void printPeroration() throws Exception {
        buf = "";
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "合計筆數:", 2);
        szTmp = comcr.commFormat("3z,3z,3z,3z", rowsCount);
        buf = comcr.insertStr(buf, szTmp, 11);

        buf = comcr.insertStr(buf, "金額:", 55);
        szTmp = comcr.commFormat("3$,3$,3$,3$", payAmtTotal);
        buf = comcr.insertStr(buf, szTmp, 60);

        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));
        return;
    }

    /*****************************************************************************/
    void lpRtn(String lstr) throws Exception {
        String hPrintName = "";
        String hRptName = "";
        String lpStr = "";

        hRptName = lstr;

        sqlCmd = "select print_name ";
        sqlCmd += "  from bil_rpt_prt";
        sqlCmd += " where report_name like ? || '%'";
        sqlCmd += " fetch first 1 rows only";
        setString(1, hRptName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
          //comcr.err_rtn("select_bil_rpt_ptr not found!", "", h_call_batch_seqno);
            return;
        }
        if (recordCnt > 0) {
            hPrintName = getValue("print_name");
        }

        if (hPrintName.length() > 0) {
            lpStr = String.format("lp -d %s %s/reports/%s_%s", hPrintName, comc.getECSHOME(), lstr,
                    hBusiBusinessDate.substring(4));
            lpStr = Normalizer.normalize(lpStr, java.text.Normalizer.Form.NFKD);
            // comc.systemCmd(lp_str);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActA008 proc = new ActA008();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
