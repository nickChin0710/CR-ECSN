/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/22  V1.00.00    phopho     program initial                          *
*  108/11/29  V1.00.01    phopho     fix err_rtn bug                          *
*  109/12/13  V1.00.02    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import java.util.ArrayList;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColB030 extends AccessDAO {
    private String progname = "強停報告表資料處理程式   109/12/13  V1.00.02 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";

    String hCoscStaticType = "";
    String hCoscStaticMode = "";
    String hCoscAcctMonth = "";
    String hCoscOppostId = "";
    String hCoscOppostPSeqno = "";
    double hCoscAcctJrnlBal = 0;
    double hCoscAcctJrnlBal2 = 0;
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoCardIndicator = "";
    String hCardCardNo = "";
    String hCardOldCardNo = "";
    String hCardCurrentCode = "";
    String hCardIssueDate = "";
    String hCardOppostDate = "";
    String hCardPSeqno = "";
    double hAnsbHisPayAmt = 0;

    int    totalCnt             = 0;
    String hTempAcctMonth = "";
    String hTempIssueDate = "";
    int hTempCalMonths;
    String hTempBusinessDate = "";

    List<String> aCardCardNo = new ArrayList<String>();
    List<String> aCardOldCardNo = new ArrayList<String>();
    List<String> aCardOppostDate = new ArrayList<String>();
    List<String> aCardIssueDate = new ArrayList<String>();
    List<String> aCardCurrentCode = new ArrayList<String>();
    List<String> aTempIssueDate = new ArrayList<String>();

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : ColB030 [date]", " 1.date  : 目前日期(yyyymmdd)");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            if (args.length != 0)
                hBusiBusinessDate = args[0];
            selectPtrBusinday();

            if (hBusiBusinessDate.substring(6).compareTo("01") != 0) {
            	exceptExit = 0;
                String err1 = "今日[" + hBusiBusinessDate + "]非本月[" + hCoscAcctMonth + "]之最後一天,不能執行該作業";
                String err2 = "";
                comcr.errRtn(err1, err2, hCallBatchSeqno);
            }

            selectCrdCard0();

            showLogMessage("I", "", "程式執行結束");

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
    void selectPtrBusinday() throws Exception {
        hTempBusinessDate = "";
        hTempIssueDate = "";
        hTempAcctMonth = "";
        hCoscAcctMonth = "";
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,?) business_date, ";
        sqlCmd += "to_char(to_date(decode(cast(? as varchar(8)),'',business_date,?),'YYYYMMDD')+1,'YYYYMMDD') temp_date, ";
        sqlCmd += "to_char(add_months(to_date(decode(cast(? as varchar(8)),'',business_date,?),'yyyymmdd'),-13),'yyyymmdd') issue_date, ";
        sqlCmd += "to_char(add_months(to_date(decode(cast(? as varchar(8)),'',business_date,?),'yyyymmdd'),-1),'yyyymm') acct_month, ";
        sqlCmd += "substr(decode(cast(? as varchar(8)),'',business_date,?),1,6) cosc_month ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        setString(5, hBusiBusinessDate);
        setString(6, hBusiBusinessDate);
        setString(7, hBusiBusinessDate);
        setString(8, hBusiBusinessDate);
        setString(9, hBusiBusinessDate);
        setString(10, hBusiBusinessDate);

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempBusinessDate = getValue("temp_date");
            hTempIssueDate = getValue("issue_date");
            hTempAcctMonth = getValue("acct_month");
            hCoscAcctMonth = getValue("cosc_month");
        }
    }

    /***********************************************************************/
    void selectCrdCard0() throws Exception {
//        sqlCmd = "select distinct ";
//        sqlCmd += "p_seqno ";
//        sqlCmd += "from crd_card a ";
//        sqlCmd += "where current_code   = '3' ";
//        sqlCmd += "and   p_seqno        = gp_no ";
//        sqlCmd += "and   p_seqno not in (select p_seqno from col_oppost_static ";
//        sqlCmd += "                      where  p_seqno = a.p_seqno ";
//        sqlCmd += "                      and    acct_month > substr(?,1,6) ";
//        sqlCmd += "                      fetch first 1 row only) ";
////        sqlCmd += "and   nvl(oppost_date ,'00000000') >= ? ";
//        sqlCmd += "and   decode(oppost_date,'','00000000',oppost_date) >= ? ";
//        sqlCmd += "and   issue_date >= ? ";
//        setString(1, h_temp_issue_date);
//        setString(2, h_temp_issue_date);
//        setString(3, h_temp_issue_date);
        sqlCmd = "select distinct ";
        sqlCmd += "acno_p_seqno ";
        sqlCmd += "from crd_card a ";
        sqlCmd += "where current_code   = '3' ";
        sqlCmd += "and   acno_p_seqno   = p_seqno ";
        sqlCmd += "and   acno_p_seqno not in (select p_seqno from col_oppost_static ";
        sqlCmd += "                      where  p_seqno = a.acno_p_seqno ";
        sqlCmd += "                      and    acct_month > substr(?,1,6) ";
        sqlCmd += "                      fetch first 1 row only) ";
        sqlCmd += "and   decode(oppost_date,'','00000000',oppost_date) >= ? ";
        sqlCmd += "and   issue_date >= ? ";
        setString(1, hTempIssueDate);
        setString(2, hTempIssueDate);
        setString(3, hTempIssueDate);

        openCursor();
        while (fetchTable()) {
            hCardPSeqno = getValue("acno_p_seqno");

            selectCrdCard();
            if (hTempCalMonths >= 12)
                continue;

            selectActAcno();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
//        sqlCmd = "select ";
//        sqlCmd += "a.acct_type, ";
//        sqlCmd += "a.acct_key, ";
//        sqlCmd += "a.p_seqno, ";
//        sqlCmd += "a.card_indicator, ";
//        sqlCmd += "b.acct_jrnl_bal, ";
//        sqlCmd += "decode(a.corp_p_seqno,'',a.id_p_seqno,a.corp_p_seqno) oppost_p_seqno, ";
//        sqlCmd += "decode(a.corp_p_seqno,'',d.id_no,e.corp_no) oppost_id ";
//        sqlCmd += "from act_acno a,act_acct b, crd_idno d ";
//        sqlCmd += "left join crd_corp e on a.corp_p_seqno = e.corp_p_seqno ";
//        sqlCmd += "where a.p_seqno = a.gp_no ";
//        sqlCmd += "and   a.p_seqno = b.p_seqno ";
//        sqlCmd += "and   d.id_p_seqno = a.id_p_seqno ";
//        sqlCmd += "and   a.p_seqno = ? ";
//        sqlCmd += "and   b.acct_jrnl_bal > 0 ";
//        setString(1, h_card_p_seqno);
        
        sqlCmd = "select ";
        sqlCmd += "a.acct_type, ";
        sqlCmd += "a.acct_key, ";
        sqlCmd += "a.acno_p_seqno, ";
        sqlCmd += "a.card_indicator, ";
        sqlCmd += "b.acct_jrnl_bal, ";
        sqlCmd += "decode(a.corp_p_seqno,'',a.id_p_seqno,a.corp_p_seqno) oppost_p_seqno, ";
        sqlCmd += "decode(a.corp_p_seqno,'',d.id_no,e.corp_no) oppost_id ";
        sqlCmd += "from act_acno a,act_acct b, crd_idno d ";
        sqlCmd += "left join crd_corp e on a.corp_p_seqno = e.corp_p_seqno ";
        sqlCmd += "where a.acno_flag <> 'Y' ";
        sqlCmd += "and   a.acno_p_seqno = b.p_seqno ";
        sqlCmd += "and   d.id_p_seqno = a.id_p_seqno ";
        sqlCmd += "and   a.acno_p_seqno = ? ";
        sqlCmd += "and   b.acct_jrnl_bal > 0 ";
        setString(1, hCardPSeqno);
        
        extendField = "act_acno.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoAcctType = getValue("act_acno.acct_type", i);
            hAcnoAcctKey = getValue("act_acno.acct_key", i);
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno", i);
            hAcnoCardIndicator = getValue("act_acno.card_indicator", i);
            hCoscAcctJrnlBal = getValueDouble("act_acno.acct_jrnl_bal", i);
            hCoscOppostPSeqno = getValue("act_acno.oppost_p_seqno", i);
            hCoscOppostId = getValue("act_acno.oppost_id", i);

            selectActAcct();
            selectActAnalSub();

            hCoscStaticMode = "1";
            if (hTempCalMonths >= 6)
                hCoscStaticMode = "2";
            hCoscStaticType = "1";

            insertColOppostStatic();
        }
    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {
        int int2 = 0;
        int int2a = 0;
        aCardCardNo.clear();
        aCardOldCardNo.clear();
        aCardOppostDate.clear();
        aCardIssueDate.clear();
        aCardCurrentCode.clear();
        aTempIssueDate.clear();

        sqlCmd = "select ";
        sqlCmd += "card_no, ";
        sqlCmd += "old_card_no, ";
        sqlCmd += "oppost_date, ";
        sqlCmd += "issue_date, ";
        sqlCmd += "current_code ";
        sqlCmd += "from crd_card ";
//        sqlCmd += "where gp_no = ? ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "order by issue_date ";
        setString(1, hCardPSeqno);
        
        extendField = "crd_card.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            aCardCardNo.add(getValue("crd_card.card_no", i));
            aCardOldCardNo.add(getValue("crd_card.old_card_no", i));
            aCardOppostDate.add(getValue("crd_card.oppost_date", i));
            aCardIssueDate.add(getValue("crd_card.issue_date", i));
            aCardCurrentCode.add(getValue("crd_card.current_code", i));
            aTempIssueDate.add("");
        }

        for (int2 = 0; int2 < recordCnt; int2++) {
            if ((aCardOldCardNo.get(int2).length() == 0) || (int2 == 0)) {
                aTempIssueDate.add(int2, aCardIssueDate.get(int2));
            }
        }

        for (int2 = 0; int2 < recordCnt - 1; int2++)
            for (int2a = 0; int2a < recordCnt; int2a++) {
                if (aCardCardNo.get(int2).compareTo(aCardOldCardNo.get(int2a)) == 0) {
                    aTempIssueDate.add(int2a, aTempIssueDate.get(int2));
                }
            }

        for (int2 = 0; int2 < recordCnt; int2++) {
            if (aCardCurrentCode.get(int2).equals("3")) {
                hCardIssueDate = aTempIssueDate.get(int2);
                hCardOppostDate = aCardOppostDate.get(int2);
                break;
            }
        }

        if (int2 > recordCnt) {
            hTempCalMonths = 12;
        } else {
        	if (hCardIssueDate.trim().equals("")) return;  //Data Error
        	
            sqlCmd = "select months_between(to_date(decode(cast(? as varchar(8)),'','30001231',cast(? as varchar(8))),'yyyymmdd'),";
            sqlCmd += "to_date(cast(? as varchar(8)) ,'yyyymmdd')) cal_months from dual ";
            setString(1, hCardOppostDate);
            setString(2, hCardOppostDate);
            setString(3, hCardIssueDate);
        	
            extendField = "h_temp.";
            
            int recordCnt1 = selectTable();
            if (notFound.equals("Y")) {
                showLogMessage("I", "", String.format("select dual error!"));
                showLogMessage("I", "",
                        String.format("issue_date[%s] oppost_date[%s]", hCardIssueDate, hCardOppostDate));
                comcr.errRtn("", "", hCallBatchSeqno);
            }
            if (recordCnt1 > 0) {
                hTempCalMonths = getValueInt("h_temp.cal_months");
            }
        }
    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hCoscAcctJrnlBal2 = 0;
        sqlCmd = "select sum(acct_jrnl_bal) acct_jrnl_bal ";
        sqlCmd += "from act_acct ";
//        sqlCmd += "where acct_key = ? ";
//        setString(1, h_acno_acct_key);
        sqlCmd += "where p_seqno = ? ";
        setString(1, hAcnoPSeqno);

        extendField = "act_acct.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCoscAcctJrnlBal2 = getValueDouble("act_acct.acct_jrnl_bal");
        }
    }

    /***********************************************************************/
    void selectActAnalSub() throws Exception {
        hAnsbHisPayAmt = 0;
        sqlCmd = "select sum(his_pay_amt) his_pay_amt ";
        sqlCmd += "from act_anal_sub ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and   decode(acct_month,'','x',acct_month) >= substr(?,1,6) ";
        setString(1, hAcnoPSeqno);
        setString(2, hCardIssueDate);
        
        extendField = "act_anal_sub.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAnsbHisPayAmt = getValueDouble("act_anal_sub.his_pay_amt");
        }
    }

    /***********************************************************************/
    void insertColOppostStatic() throws Exception {
    	daoTable = "col_oppost_static";
    	extendField = daoTable + ".";
    	setValue(extendField+"static_type", hCoscStaticType);
        setValue(extendField+"static_mode", hCoscStaticMode);
        setValue(extendField+"acct_month", hCoscAcctMonth);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"oppost_id", hCoscOppostId);
        setValue(extendField+"oppost_p_seqno", hCoscOppostPSeqno);
        setValue(extendField+"card_indicator", hAcnoCardIndicator);
        setValue(extendField+"issue_date", hCardIssueDate);
        setValue(extendField+"oppost_date", hCardOppostDate);
        setValueDouble(extendField+"acct_jrnl_bal", hCoscAcctJrnlBal);
        setValueDouble(extendField+"acct_jrnl_bal2", hCoscAcctJrnlBal2);
        setValueDouble(extendField+"his_pay_amt", hAnsbHisPayAmt);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("insert_col_oppost_static duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB030 proc = new ColB030();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
