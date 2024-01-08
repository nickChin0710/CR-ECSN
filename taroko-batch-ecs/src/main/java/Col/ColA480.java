/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/07/21  V1.01.01    phopho     Initial                                  *
*  108/11/29  V1.01.02    phopho     fix err_rtn bug                          *
*  109/12/10  V1.00.03    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import java.util.ArrayList;
import java.util.List;

import com.*;
          
public class ColA480 extends AccessDAO {
    private String progname = "未有強停即辦理前置協商處理程式  109/12/10  V1.00.03 ";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    int debugD = 1;
    String hBusiBusinessDate = "";

    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoIdPSeqno = "";
    double hJrnlTransactionAmt = 0;
    String hClhtId = "";
    String hClhtIdPSeqno = "";
    String hClhtApplyDate = "";
    String hClolId = "";
    String hClolPSeqno = "";
    String hClolIdPSeqno = "";
    String hClolPayType = "";
    String hClolCardNo = "";
    String hClolIssueDate = "";
    double hClolAcctJrnlBal = 0;
    double hClolAcctJrnlBal2 = 0;
    String hClolRowid = "";
    String hCardIssueDate = "";
    String hCardOppostDate = "";
    String hCallBatchSeqno = "";
    List<String> aCardCardNo = new ArrayList<String>();
    List<String> aCardIssueDate = new ArrayList<String>();
    List<String> aCardOldCardNo = new ArrayList<String>();
    List<String> aCardOppostDate = new ArrayList<String>();
    List<String> aCardCurrentCode = new ArrayList<String>();
    List<Integer> aTempCount = new ArrayList<Integer>();

    int  int1a        = 0;
    int  int2a        = 0;
    int  int3a        = 0;
    long totalCnt     = 0;
    int crdCardCnt = 0;

    String hTempOppostDate = "";
    int hTempCount = 0;
    String hTempIssueDate = "";
    String hTempNewCardDate = "";
    int hTempCurrentCnt = 0;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColA480 proc = new ColA480();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            // 檢查參數
            if (args.length > 1) {
                comc.errExit("Usage : ColA480 [business_date]", "");
            }
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            hBusiBusinessDate = "";
            if (args.length == 1 && args[0].length() == 8) {
                hBusiBusinessDate = args[0];
            }

            selectPtrBusinday();
            
            if (hBusiBusinessDate.substring(6).compareTo("01") != 0) {
            	exceptExit = 0;
                comcr.errRtn("本程式須在每月1日執行, 本日 " + hBusiBusinessDate + " !", "", hCallBatchSeqno);
            }

            totalCnt = 0;
            showLogMessage("I", "", "新增帳戶開始處理...");
            selectColLiacNegoHst();
            showLogMessage("I", "", "新增帳戶筆數 : [" + totalCnt + "]");
            totalCnt = 0;
            showLogMessage("I", "", "本月資料更新開始處理...");
            selectColLiacOppdtl();
            showLogMessage("I", "", "處理帳戶筆數 : [" + totalCnt + "]");

            showLogMessage("I", "", "程式執行結束");

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    } // End of mainProcess
      // ************************************************************************

    private void selectPtrBusinday() throws Exception {
        selectSQL = "decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) as business_date ";
        daoTable = "ptr_businday";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        if (selectTable() > 0) {
            hBusiBusinessDate = getValue("BUSINESS_DATE");
        }

        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday error!", "", hCallBatchSeqno);
        }

    }

    // ************************************************************************
    private void selectColLiacNegoHst() throws Exception {

        sqlCmd = "SELECT id_no,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "apply_date,";
        sqlCmd += "to_char(add_months(to_date(apply_date,'yyyymmdd'),-12),'yyyymmdd') new_card_date "; /* 申請日視為營業日 */
        sqlCmd += "FROM   col_liac_nego_hst ";
        sqlCmd += "WHERE  liac_status = '1' ";
        sqlCmd += "AND    to_char(add_months(to_date(apply_date,'yyyymmdd'),12),'yyyymmdd') >= ? "; /* 12個月內申請前協 */
        sqlCmd += "GROUP  by id_no, id_p_seqno, apply_date, ";
        sqlCmd += "to_char(add_months(to_date(apply_date,'yyyymmdd'),-12),'yyyymmdd') ";
        sqlCmd += "MINUS ";
        sqlCmd += "SELECT id_no,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "apply_date,";
        sqlCmd += "to_char(add_months(to_date(apply_date,'yyyymmdd'),-12),'yyyymmdd') new_card_date ";
        sqlCmd += "FROM   col_liac_oppdtl ";
        sqlCmd += "GROUP  by id_no, id_p_seqno, apply_date, ";
        sqlCmd += "to_char(add_months(to_date(apply_date,'yyyymmdd'),-12),'yyyymmdd') ";
        sqlCmd += "ORDER  by 1,2,3,4 ";
        setString(1, hBusiBusinessDate);
        
        openCursor();
        while (fetchTable()) {
            hClhtId = getValue("id_no");
            hClhtIdPSeqno = getValue("id_p_seqno");
            hClhtApplyDate = getValue("apply_date");
            hTempNewCardDate = getValue("new_card_date");

            int1a = selectCrdCard(); /* 新卡友 */
            if (debug == 1)
                showLogMessage("I", "",
                        "step int1a=[" + int1a + "] id_no[" + hClhtId + "] apply_date[" + hClhtApplyDate + "]");

            if (int1a != 2)
                continue;
            int2a = selectCrdCard1(); /* 未強停 */
            if (debug == 1)
                showLogMessage("I", "", "step int2a=[" + int2a + "]");

            if (int2a != 0)
                continue;
            selectActAcno();
        }
        closeCursor();
    }

    // ************************************************************************
    private void selectActAcno() throws Exception {
//        selectSQL = "a.id_p_seqno, a.p_seqno, a.acct_type, a.acct_key ";
//        selectSQL = "a.id_p_seqno, a.acno_p_seqno, a.acct_type, a.acct_key ";
//        daoTable = "act_acno a, crd_idno c";
//        whereStr = "where c.id_no = ?  and a.id_p_seqno = c.id_p_seqno ";
//        setString(1, h_clht_id);
        selectSQL = "a.id_p_seqno, a.acno_p_seqno, a.acct_type, a.acct_key ";
        daoTable = "act_acno a ";
        whereStr = "where a.id_p_seqno = ? ";
        setString(1, hClhtIdPSeqno);
        
        extendField = "act_acno.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoIdPSeqno = getValue("act_acno.id_p_seqno", i);
//            h_acno_p_seqno = getValue("p_seqno", i);
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno", i);
            hAcnoAcctType = getValue("act_acno.acct_type", i);
            hAcnoAcctKey = getValue("act_acno.acct_key", i);

            int3a = selectCrdCard2(); /* 發卡1年內申請前協 */
            if (debug == 1)
                showLogMessage("I", "", "step int3a=[" + int3a + "]");

            if (int3a != 0)
                continue;
            if (debug == 1)
                showLogMessage("I", "", "step 4 符合id =[" + hAcnoAcctKey + "]");

            selectActJrnl();
            hClolPayType = "1"; /* 有繳款 */
            if (hJrnlTransactionAmt == 0)
                hClolPayType = "0";
            if (debug == 1)
                showLogMessage("I", "", "step 5 pay_type[" + hClolPayType + "] issue_date[" + hTempIssueDate
                        + "] apply_date[" + hClhtApplyDate + "]");

            totalCnt++;
            if ((totalCnt % 1000) == 0) {
                showLogMessage("I", "", "處理筆數 : [" + totalCnt + "]");
                commitDataBase();
            }
            // processDisplay(1000); // every nnnnn display message
            insertColLiacOppdtl();
        }
    }

    // 0731 start!
    // ************************************************************************
    private void selectColLiacOppdtl() throws Exception {
        selectSQL = "id_no, p_seqno, id_p_seqno, rowid as rowid";
        daoTable = "col_liac_oppdtl";
        whereStr = "where proc_mark = '0' order by id_no";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hClolId = getValue("id_no", i);
            hClolPSeqno = getValue("p_seqno", i);
            hClolIdPSeqno = getValue("id_p_seqno", i);
            hClolRowid = getValue("rowid", i);

            if (debug == 1)
                showLogMessage("I", "", "step 6 舊資料 =[" + hClolId + "]");

            selectActAcct();
            selectActAcct1();
            totalCnt++;
            if ((totalCnt % 10000) == 0) {
                showLogMessage("I", "", "處理筆數 : [" + totalCnt + "]");
                commitDataBase();
            }
            updateColLiacOppdtl();
        }
    }

    // ************************************************************************
    private int selectCrdCard() throws Exception {
        int oppostFlag = 0;
        int int7b;int int7a = 0;
        int int7 = 0;
        int oldCardTag = 0, validCardTag = 0, newCardTag = 0;
        hCardIssueDate = "";
        hCardOppostDate = "";
        aCardCardNo.clear();
        aCardIssueDate.clear();
        aCardOldCardNo.clear();
        aCardOppostDate.clear();
        aCardCurrentCode.clear();
        aTempCount.clear();
        hTempCount = 0;
//        sqlCmd = "select card_no, issue_date, old_card_no, ";
//        sqlCmd += "decode(sign( ? -oppost_date),-1,'',oppost_date) oppost_date, "; /* 申請日小於停卡日 */
//        sqlCmd += "decode(sign( ? -oppost_date),-1,'0',";
//        sqlCmd += "decode(current_code,'0','0','2','2','5','2',current_code)) current_code, ";
//                /* 0 : 正常   1 : 一般停用    2 : 掛失  3 : 強停  4 : 其他  5: 偽卡 */
//        sqlCmd += "months_between(to_date(substr( ? ,1,6),'yyyymm'),";
//        sqlCmd += "to_date(substr(decode(sign( ? -nvl(oppost_date, ? )),";
//        sqlCmd += "1,oppost_date,''),1,6),'yyyymm')) temp_count "; /*  申請日大於停用日時 ，間隔月數  */
//        sqlCmd += "from crd_card where id_p_seqno = ? and card_no = major_card_no ";
//        sqlCmd += "and nvl(issue_date,'x') < ? ";
//        sqlCmd += "and acct_type in ('01','05','06') order by issue_date desc";
//        setString(1, h_clht_apply_date);
//        setString(2, h_clht_apply_date);
//        setString(3, h_clht_apply_date);
//        setString(4, h_clht_apply_date);
//        setString(5, h_clht_apply_date);
////        setString(6, h_clht_id);
//        setString(6, h_clht_id_p_seqno);
//        setString(7, h_clht_apply_date);
        
        //oppost_date = '' 時會產生ERROR, 修改SQL避掉這段
        sqlCmd = "select card_no, issue_date, old_card_no, ";
        sqlCmd += "decode(sign( ? - decode(oppost_date,'','0',oppost_date)),-1,'',oppost_date) oppost_date, "; /* 申請日小於停卡日 */
        sqlCmd += "decode(sign( ? - decode(oppost_date,'','0',oppost_date)),-1,'0',";
        sqlCmd += "decode(current_code,'0','0','2','2','5','2',current_code)) current_code, ";
                /* 0 : 正常   1 : 一般停用    2 : 掛失  3 : 強停  4 : 其他  5: 偽卡 */
        sqlCmd += "months_between(to_date(substr( ? ,1,6),'yyyymm'),";
        sqlCmd += "to_date(substr(decode(sign( ? - decode(oppost_date,'',?,oppost_date)),";
        sqlCmd += "1,oppost_date,?),1,6),'yyyymm')) temp_count "; /*  申請日大於停用日時 ，間隔月數  */
        sqlCmd += "from crd_card where id_p_seqno = ? and card_no = major_card_no ";
        sqlCmd += "and decode(issue_date,'','x',issue_date) < ? ";
        sqlCmd += "and acct_type in ('01','05','06') order by issue_date desc";
        setString(1, hClhtApplyDate);
        setString(2, hClhtApplyDate);
        setString(3, hClhtApplyDate);
        setString(4, hClhtApplyDate);
        setString(5, hClhtApplyDate);
        setString(6, hClhtApplyDate);
        setString(7, hClhtIdPSeqno);
        setString(8, hClhtApplyDate);

        crdCardCnt = selectTable();
        for (int i = 0; i < crdCardCnt; i++) {
            aCardCardNo.add(getValue("card_no", i));
            aCardIssueDate.add(getValue("issue_date", i));
            aCardOldCardNo.add(getValue("old_card_no", i));
            aCardOppostDate.add(getValue("oppost_date", i));
            aCardCurrentCode.add(getValue("current_code", i));
            aTempCount.add(getValueInt("temp_count", i));
        }

        if (crdCardCnt == 0)
            return (0);
        hTempOppostDate = hClhtApplyDate; /* 申請(營業)日視為停卡日 */
        hTempIssueDate = aCardIssueDate.get(crdCardCnt - 1); /* 最早發卡日 */
        if ((int7b = selectDualDate()) < 12)
            newCardTag = 1; /* 最早發卡日未超過1年視為新卡友 */
        if (debug == 1)
            showLogMessage("I", "", String.format("**********************************************"));
        if (debug == 1)
            showLogMessage("I", "", String.format("[step 7b1] issue_date[%s] apply_date[%s]-[%d]", hTempIssueDate,
                    hTempOppostDate, int7b));

        hTempOppostDate = "00000000";
        for (int7 = 0; int7 < crdCardCnt; int7++) {
            if (debug == 1)
                showLogMessage("I", "",
                        String.format("[step 7b2] card_no[%s] issue_date[%s] oppost_date[%s] current[%s] cnt[%d]",
                                aCardCardNo.get(int7), aCardIssueDate.get(int7), aCardOppostDate.get(int7),
                                aCardCurrentCode.get(int7), aTempCount.get(int7)));

            hCardIssueDate = aCardIssueDate.get(int7);
            hCardOppostDate = aCardOppostDate.get(int7);
            if ((aCardCurrentCode.get(int7).equals("0"))
                    || ((aCardCurrentCode.get(int7).equals("2")) && (aTempCount.get(int7) <= 3))) {
                if ((aCardCurrentCode.get(int7).equals("2")) && (aTempCount.get(int7) <= 3)) {
                    aCardCurrentCode.add(int7, "0");
                    aCardOppostDate.add(int7, "");
                }

                if (aCardCurrentCode.get(int7).equals("0")) {
                    hTempCurrentCnt++;
                } else {
                    if (aCardOldCardNo.get(int7).length() == 0)
                        hTempCurrentCnt++;
                }
                validCardTag = 1; /* 有效卡 */
            }
        }
        for (int7 = 0; int7 < crdCardCnt - 1; int7++)
            for (int7a = int7 + 1; int7a < crdCardCnt; int7a++)
                if ((aCardOldCardNo.get(int7).length() != 0) && (aCardCurrentCode.get(int7).equals("0"))
                        && (aCardCardNo.get(int7a).compareTo(aCardOldCardNo.get(int7)) == 0))
                    aCardCurrentCode.add(int7a, "0");

        for (int7 = 0; int7 < crdCardCnt; int7++)
            if (((aCardCurrentCode.get(int7).equals("0"))
                    && (hTempNewCardDate.compareTo(aCardIssueDate.get(int7)) > 0))
                    || ((aCardCurrentCode.get(int7).equals("0"))
                            && (hTempNewCardDate.compareTo(aCardIssueDate.get(int7)) > 0)
                            && (hTempNewCardDate.compareTo(aCardOppostDate.get(int7)) <= 0))) {
                if (debug == 1)
                    showLogMessage("I", "",
                            String.format("[step 7b3] current_code[%s] new_date[%s]-issue_date[%s]-oppost[%s]",
                                    aCardCurrentCode.get(int7), hTempNewCardDate, aCardIssueDate.get(int7),
                                    aCardOppostDate.get(int7)));

                oldCardTag = 1; /* 舊卡友(發卡超過1年且停卡日在申請日1年內) */
                break;
            }
        if (debug == 1)
            showLogMessage("I", "", String.format("[step 7b4] old_card_flag[%d]", oldCardTag));
        if ((oldCardTag == 0) && (newCardTag == 0)) {
            hTempIssueDate = "";
            for (int7 = crdCardCnt - 1; int7 >= 0; int7--) /* 活卡最早發卡日 */
                if (aCardCurrentCode.get(int7).equals("0")) {
                    hTempIssueDate = aCardIssueDate.get(int7);
                    hTempOppostDate = "00000000";
                    break;
                }

            for (int7 = 0; int7 < crdCardCnt; int7++) /* 死卡停卡日>活卡最早發卡日 */
            { /* 死卡發卡日又>活卡最早發卡日 */
                /* 則死卡發卡日視為活卡最早發卡日 */
                if (aCardCurrentCode.get(int7).equals("0"))
                    continue;
                if (aCardOppostDate.get(int7).compareTo(hTempIssueDate) < 0)
                    continue;
                if (aCardIssueDate.get(int7).compareTo(hTempIssueDate) <= 0)
                    continue;
                hTempIssueDate = aCardIssueDate.get(int7);

                if (debug == 1)
                    showLogMessage("I", "", String.format("[step 7b4-1] issue[%s] oppost[%s] 活卡最早發卡日[%s]",
                            aCardIssueDate.get(int7), aCardOppostDate.get(int7), hTempIssueDate));
            }

            for (int7 = 0; int7 < crdCardCnt; int7++) /* 在申請日1年內之發卡日小於活卡最早發卡日 */
            { /* 則該發卡日視為活卡最早發卡日 */
                if (hTempNewCardDate.compareTo(aCardIssueDate.get(int7)) > 0)
                    continue; /* 發卡超過1年 */
                if (aCardIssueDate.get(int7).compareTo(hTempIssueDate) < 0)
                    hTempIssueDate = aCardIssueDate.get(int7);

                if (debug == 1)
                    showLogMessage("I", "",
                            String.format("[step 7b4-2] new_date[%s] 活卡最早發卡日[%s]", hTempNewCardDate, hTempIssueDate));

                break;
            }

            for (int7 = 0; int7 < crdCardCnt; int7++) /* 活卡最早發卡日之前最晚停卡日 */
            {
                if (aCardIssueDate.get(int7).compareTo(hTempIssueDate) >= 0)
                    continue;
                if ((aCardOppostDate.get(int7).length() != 0)
                        && (aCardOppostDate.get(int7).compareTo(hTempOppostDate) > 0)) {
                    hTempOppostDate = aCardOppostDate.get(int7);
                    oppostFlag = 1;
                }
            }
            if (debug == 1)
                showLogMessage("I", "",
                        String.format("[step 7b4-3] 活卡最早發卡日[%s] 最晚停卡日[%s]", hTempIssueDate, hTempOppostDate));
            if ((oppostFlag == 1) && (oldCardTag == 0))
                if ((int7b = selectDualDate()) <= 6)
                    oldCardTag = 1; /* 最後停卡日與活卡最早發卡日小於6個月 */
            if (debug == 1)
                showLogMessage("I", "", String.format("[step 7b5] issue_date[%s] oppost_date[%s]-[%d]", hTempIssueDate,
                        hTempOppostDate, int7b));
        }
        if (debug == 1)
            showLogMessage("I", "", String.format("[step 7b6] new_card_tag[%d] old_card_tag[%d] valid_card_tag[%d]",
                    newCardTag, oldCardTag, validCardTag));
        if (debug == 1)
            showLogMessage("I", "", String.format("**********************************************"));

        if ((newCardTag == 1) && (validCardTag == 1))
            return (2); /* 新卡友 有效卡 */
        if ((oldCardTag == 1) && (validCardTag == 1))
            return (0); /* 舊卡友 有效卡 */
        if ((oldCardTag == 1) && (validCardTag == 0))
            return (1); /* 舊卡友 無效卡 */
        if ((oldCardTag == 0) && (validCardTag == 1))
            return (2); /* 新卡友 有效卡 */
        if ((oldCardTag == 0) && (validCardTag == 0))
            return (1); /* 舊卡友 無效卡 */
        return (3);
    }

    // ************************************************************************
    private int selectDualDate() throws Exception {
        int hTempCnt = 0;
        sqlCmd = "select abs(months_between(to_date(?,'yyyymmdd'),to_date(?,'yyyymmdd'))) cnt ";
        sqlCmd += "from dual";
        setString(1, hTempOppostDate);
        setString(2, hTempIssueDate);

        if (selectTable() > 0) {
            hTempCnt = getValueInt("cnt");
        }
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dual_date not found!", "", hCallBatchSeqno);
        }
        return hTempCnt;
    }

    // ************************************************************************
    private int selectCrdCard1() throws Exception {
        int hCnt = 0;
        sqlCmd = "select sum(decode(current_code,'3',1,0)) cnt ";
        sqlCmd += "from crd_card where id_p_seqno = ? and id_p_seqno = major_id_p_seqno ";
        sqlCmd += "and oppost_date between to_char(add_months(to_date( ? ,'yyyymmdd'),-12),'yyyymmdd') and ? ";
//        setString(1, h_clht_id);
        setString(1, hClhtIdPSeqno);
        setString(2, hClhtApplyDate);
        setString(3, hClhtApplyDate);

        if (selectTable() > 0) {
            hCnt = getValueInt("cnt");
        }
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_card_2 not found!", "", hCallBatchSeqno);
        }
        return hCnt;
    }

    // ************************************************************************
    private int selectCrdCard2() throws Exception {
        sqlCmd = "select card_no, issue_date ";
//        sqlCmd += "from crd_card where p_seqno = ? and id_p_seqno = major_id_p_seqno ";
        sqlCmd += "from crd_card where acno_p_seqno = ? and id_p_seqno = major_id_p_seqno ";
        sqlCmd += "and issue_date between to_char(add_months(to_date( ? ,'yyyymmdd'),-12),'yyyymmdd') and ? ";
        sqlCmd += "order by issue_date,card_no ";
        setString(1, hAcnoPSeqno);
        setString(2, hClhtApplyDate);
        setString(3, hClhtApplyDate);

        crdCardCnt = selectTable();
        for (int i = 0; i < crdCardCnt; i++) {
            aCardCardNo.add(getValue("card_no", i));
            aCardIssueDate.add(getValue("issue_date", i));
        }

        if (notFound.equals("Y"))
            return 1;
        hClolCardNo = aCardCardNo.get(0);
        hClolIssueDate = aCardIssueDate.get(0);
        return 0;
    }

    // ************************************************************************
    private void insertColLiacOppdtl() throws Exception {
        dateTime();
        daoTable = "col_liac_oppdtl";
        extendField = daoTable + ".";
        setValue(extendField+"id_no", hClhtId);
        setValue(extendField+"apply_date", hClhtApplyDate);
        setValue(extendField+"pay_type", hClolPayType);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);  //no column
        setValue(extendField+"card_no", hClolCardNo);
        setValue(extendField+"issue_date", hClolIssueDate);
        setValue(extendField+"proc_mark", "0");
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();
    }

    // ************************************************************************
    private void selectActAcct() throws Exception {
        hClolAcctJrnlBal2 = 0;
//        sqlCmd = "select sum(acct_jrnl_bal) acct_jrnl_bal ";
//        sqlCmd += "from act_acct where acct_key like ?||'%' ";
        sqlCmd = "select sum(acct_jrnl_bal) acct_jrnl_bal ";
        sqlCmd += "from act_acct ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hClolIdPSeqno);
        
        extendField = "act_acct.";

        if (selectTable() > 0) {
            hClolAcctJrnlBal2 = getValueDouble("act_acct.acct_jrnl_bal");
        }
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_act_acct not found!", "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void selectActAcct1() throws Exception {
        hClolAcctJrnlBal = 0;
        sqlCmd = "select sum(acct_jrnl_bal) acct_jrnl_bal ";
        sqlCmd += "from act_acct where p_seqno = ? ";
        setString(1, hClolPSeqno);
        
        extendField = "act_acct_1.";

        if (selectTable() > 0) {
            hClolAcctJrnlBal = getValueDouble("act_acct_1.acct_jrnl_bal");
        }
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_act_acct_1 not found!", "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void updateColLiacOppdtl() throws Exception {
        updateSQL = "acct_jrnl_bal = ?, acct_jrnl_bal2 = ?, proc_mark = decode(cast(? as double),0,"
                + "            decode(sign(to_char(add_months(to_date(issue_date,'yyyymmdd')"
                + "            ,12),'yyyymmdd')-?),-1,'1','0'),'0'), " /* 發卡超過12個月 =1 */
                + "mod_time = sysdate, mod_pgm = ? ";
        daoTable = "col_liac_oppdtl";
        whereStr = "WHERE rowid = ? ";
        setDouble(1, hClolAcctJrnlBal);
        setDouble(2, hClolAcctJrnlBal2);
        setDouble(3, hClolAcctJrnlBal);
        setString(4, hBusiBusinessDate);
        setString(5, javaProgram);
        setRowId(6, hClolRowid);

        updateTable();

        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("update_col_liac_oppdtl error!", "rowid=[" + hClolRowid + "]", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void selectActJrnl() throws Exception {
        hJrnlTransactionAmt = 0;
        sqlCmd = "select sum(transaction_amt) transaction_amt ";
        sqlCmd += "from act_jrnl where p_seqno = ? ";
        sqlCmd += "and tran_class = 'P' and acct_date between ? and ? ";
        setString(1, hAcnoPSeqno);
        setString(2, hTempIssueDate);
        setString(3, hClhtApplyDate);
        
        extendField = "act_jrnl.";

        if (selectTable() > 0) {
            hJrnlTransactionAmt = getValueDouble("act_jrnl.transaction_amt");
        }if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_act_jrnl not found!", "", hCallBatchSeqno);
        }
    }
    // ************************************************************************
}