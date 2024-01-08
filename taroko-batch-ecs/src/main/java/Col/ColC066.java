/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/12  V1.00.00    phopho     program initial                          *
*  109/12/15  V1.00.01    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.*;

public class ColC066 extends AccessDAO {
    private String progname = "LGD 表二檔案資料產生處理 109/12/15  V1.00.01 ";

    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    String hBusiBusinessDate = "";
    String hCallBatchSeqno = "";

    String hLgd2IdCorpPSeqno = "";
    String hLgd2IdCorpNo = "";
    String hLgd2IdCorpType = "";
    String hLgd2LgdSeqno = "";
    String hLgd2AcctPSeqno = "";
    String hLgd2EarlyYm = "";
    double hLgd2RiskAmt = 0;
    String hLgd2OverdueYm = "";
    double hLgd2OverdueAmt = 0;
    String hLgd2CollYm = "";
    double hLgd2CollAmt = 0;
    double hLgd2RecvSelfAmt = 0;
    double hLgd2RecvRelaAmt = 0;
    double hLgd2RecvOthAmt = 0;
    double hLgd2CostsAmt = 0;
    String hLgd2CostsYm = "";
    double hLgd2RevolRate = 0;
    String hLgd2CardRelaType = "";
    String hLgd2CloseReason = "";
    String hClddPSeqno = "";
    int hClddJrnlCnt = 0;
    String hClddLgdRowid = "";
    String hClddRowid = "";
    double hAgenRevolvingInterest1 = 0;

    long totalCnt = 0;
    String hSysDate = "";

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColC066 proc = new ColC066();
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

//            if (comm.isAppActive(javaProgram)) {
//            	exceptExit = 0;
//                comc.err_exit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
//            }

            // 檢查參數
            if (args.length > 1) {
                comc.errExit("Usage : ColC066 [business_date]", "");
            }
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            hBusiBusinessDate = "";
            if (args.length >= 1)
                hBusiBusinessDate = args[0];

            selectPtrBusinday();
            selectPtrActgeneralN();
            
            showLogMessage("I", "", "=========================================");
            showLogMessage("I", "", "處理案件已結案");
            updateColLgd9011();
            showLogMessage("I", "", "     Total process records[" + totalCnt + "]");
            showLogMessage("I", "", "-----------------------------------------");

            showLogMessage("I", "", "個人案件暫存處理");
            insertColLgdTmp1();
            showLogMessage("I", "", "     Total process records[" + totalCnt + "]");
            showLogMessage("I", "", "-----------------------------------------");

            showLogMessage("I", "", "公司案件暫存處理");
            insertColLgdTmp2();
            showLogMessage("I", "", "     Total process records[" + totalCnt + "]");
            showLogMessage("I", "", "-----------------------------------------");

            showLogMessage("I", "", "處理[A1]案件");
            updateColLgdTmp1();
            showLogMessage("I", "", "     Total process records[" + totalCnt + "]");
            showLogMessage("I", "", "-----------------------------------------");

            showLogMessage("I", "", "處理[B2]案件");
            updateColLgdTmp2();
            showLogMessage("I", "", "     Total process records[" + totalCnt + "]");
            showLogMessage("I", "", "-----------------------------------------");

            showLogMessage("I", "", "處理[A2]案件, 檢核步驟");
            totalCnt = 0;
            updateColLgdTmp3();
            showLogMessage("I", "", "     Total process records[" + totalCnt + "]");
            if (totalCnt > 0) {
                showLogMessage("I", "", "-----------------------------------------");
                showLogMessage("I", "", "處理[A2]案件, 執行步驟");
                updateColLgdTmp4();
                showLogMessage("I", "", "     Total process records[" + totalCnt + "]");
            }
            showLogMessage("I", "", "-----------------------------------------");

            showLogMessage("I", "", "清除暫存未結案案件");
            deleteColLgdTmp();
            showLogMessage("I", "", "     Total process records[" + totalCnt + "]");
            showLogMessage("I", "", "-----------------------------------------");

            /*
             * ECSprintf(stderr,"處理月份匯整案件資料\n"); update_col_lgd_tmp_5();
             * ECSprintf(stderr,"     Total process records[%d]\n",total_cnt);
             * ECSprintf(stderr,"------------------------------------------\n");
             */

            showLogMessage("I", "", "處理金額匯整案件資料");
            updateColLgdTmp6();
            showLogMessage("I", "", "     Total process records[" + totalCnt + "]");
            showLogMessage("I", "", "-----------------------------------------");

            totalCnt = 0;
            showLogMessage("I", "", "開始結案案件資料");
            selectColLgdTmp();
            showLogMessage("I", "", "     Total process records[" + totalCnt + "]");
            showLogMessage("I", "", "-----------------------------------------");

            insertColLgdBatch();

            // ==============================================
            // 固定要做的
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
        selectSQL = "decode(cast(? as varchar(8)), '',business_date, cast(? as varchar(8))) business_date, "
        		+ "to_char(sysdate,'yyyymmdd') sys_date ";
        daoTable = "ptr_businday";
        whereStr = "fetch first 1 row only";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
        hSysDate = getValue("sys_date");
    }

    // ************************************************************************
    private void selectPtrActgeneralN() throws Exception {
        sqlCmd = "SELECT max(round(revolving_interest1*365/100,2)) revolving_interest1 ";
        sqlCmd += "from ptr_actgeneral_n ";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actgeneral_n not found!", "", hCallBatchSeqno);
        }
        hAgenRevolvingInterest1 = getValueDouble("revolving_interest1");
    }

    // ************************************************************************
    private void updateColLgd9011() throws Exception {
        daoTable = "col_lgd_901 a";
        updateSQL = "close_flag ='Y', close_date = ? ";
        whereStr = "WHERE decode(close_flag,'','x',close_flag)<>'Y' and apr_date <> '' "
                + "and   exists (select 1 from col_lgd_902 where lgd_seqno =a.lgd_seqno) ";
        setString(1, hSysDate);

        totalCnt = updateTable();
    }

    // ************************************************************************
    private void insertColLgdTmp1() throws Exception {
//        sqlCmd = "insert into col_lgd_tmp (";
//        sqlCmd += "id_corp_type,";
//        sqlCmd += "id_corp_p_seqno,";  //phopho add
//        sqlCmd += "id_corp_no,";
//        sqlCmd += "interest_date,";
//        sqlCmd += "crt_901_date,";
//        sqlCmd += "p_seqno,";
//        sqlCmd += "lgd_early_ym,";
//        sqlCmd += "payment_rate1_cnt,";
//        sqlCmd += "acct_status,";
//        sqlCmd += "status_cnt,";
//        sqlCmd += "data_flag,";
//        sqlCmd += "lgd_rowid)";
//        sqlCmd += " select ";
//        sqlCmd += "max(a.id_corp_type),";
//        sqlCmd += "a.id_corp_p_seqno,";  //phopho add
//        sqlCmd += "a.id_corp_no,";
//        sqlCmd += "max(decode(b.last_pay_date,'','',";
//        sqlCmd += "    to_char(add_months(to_date(b.last_pay_date,'yyyymmdd'),";
//        sqlCmd += "    decode(sign(b.stmt_cycle-substr(b.last_pay_date,7,2)),-1";
//        sqlCmd += "    ,1,0)),'yyyymmdd'))),";
//        sqlCmd += "max(crt_901_date),";
//        sqlCmd += "b.p_seqno,";
//        sqlCmd += "min(a.lgd_early_ym),";
//        sqlCmd += "max(decode(b.payment_rate1,'0A',1,'0B',1,'0E',1,0)),";
//        sqlCmd += "max(b.acct_status),";
//        sqlCmd += "max(decode(sign(sysdate-add_months(to_date(decode(b.status_change_date,'','20000101',b.status_change_date),";
//        sqlCmd += "    'yyyymmdd'),24)),-1,0,1)),";
//        sqlCmd += "max(decode(data_table,'nego','1','renew','1','liqu','1','0')),";
//        sqlCmd += "max(a.rowid) ";
//        sqlCmd += "from   col_lgd_901 a, act_acno b,ptr_workday c, crd_idno d ";
//        sqlCmd += "where  b.p_seqno = b.gp_no ";
//        sqlCmd += "and    d.id_p_seqno = b.id_p_seqno ";
////        sqlCmd += "and    b.id_no = a.id_corp_no ";
//        sqlCmd += "and    b.id_p_seqno = a.id_corp_p_seqno ";
//        sqlCmd += "and    b.stmt_cycle = c.stmt_cycle ";
//        sqlCmd += "and    decode(a.close_flag,'','x',a.close_flag)<>'Y' ";
//        sqlCmd += "and    a.apr_date <> '' ";
//        sqlCmd += "and    a.id_corp_type = '1' ";
//        sqlCmd += "GROUP BY a.id_corp_p_seqno,a.id_corp_no,b.p_seqno ";
        
    	sqlCmd = "insert into col_lgd_tmp (";
        sqlCmd += "id_corp_type,";
        sqlCmd += "id_corp_p_seqno,";  //phopho add
        sqlCmd += "id_corp_no,";
        sqlCmd += "interest_date,";
        sqlCmd += "crt_901_date,";
        sqlCmd += "p_seqno,";
        sqlCmd += "lgd_early_ym,";
        sqlCmd += "payment_rate1_cnt,";
        sqlCmd += "acct_status,";
        sqlCmd += "status_cnt,";
        sqlCmd += "data_flag,";
        sqlCmd += "lgd_rowid)";
        sqlCmd += " select ";
        sqlCmd += "max(id_corp_type), ";
        sqlCmd += "id_corp_p_seqno, ";
        sqlCmd += "id_corp_no, ";
        sqlCmd += "max(last_pay_date), ";
        sqlCmd += "max(crt_901_date), ";
        sqlCmd += "p_seqno, ";
        sqlCmd += "min(lgd_early_ym), ";
        sqlCmd += "max(payment_rate1), ";
        sqlCmd += "max(acct_status), ";
        sqlCmd += "max(status_change_date), ";
        sqlCmd += "max(data_table), ";
        sqlCmd += "max(lgdrowid) ";
        sqlCmd += " from ( ";
        sqlCmd += "  select a.id_corp_type, ";
        sqlCmd += "      a.id_corp_p_seqno, ";
        sqlCmd += "      a.id_corp_no, ";
        sqlCmd += "      decode(b.last_pay_date,'','',to_char(add_months(to_date(b.last_pay_date,'yyyymmdd'), ";
        sqlCmd += "          decode(sign(b.stmt_cycle-substr(b.last_pay_date,7,2)),-1 ";
        sqlCmd += "          ,1,0)),'yyyymmdd')) last_pay_date, ";
        sqlCmd += "      a.crt_901_date, ";
//        sqlCmd += "      b.p_seqno, ";
        sqlCmd += "      b.acno_p_seqno as p_seqno, ";
        sqlCmd += "      a.lgd_early_ym, ";
        sqlCmd += "      decode(b.payment_rate1,'0A',1,'0B',1,'0E',1,0) payment_rate1, ";
        sqlCmd += "      b.acct_status, ";
        sqlCmd += "      decode(sign(sysdate-add_months(to_date(decode(b.status_change_date,'','20000101',b.status_change_date), ";
        sqlCmd += "          'yyyymmdd'),24)),-1,0,1) status_change_date, ";
        sqlCmd += "      decode(data_table,'nego','1','renew','1','liqu','1','0') data_table, ";
        sqlCmd += "      a.rowid lgdrowid ";
        sqlCmd += "  from col_lgd_901 a, act_acno b,ptr_workday c, crd_idno d "; 
//        sqlCmd += "  where  b.p_seqno = b.gp_no ";
        sqlCmd += "  where  b.acno_flag <> 'Y' ";
        sqlCmd += "  and    d.id_p_seqno = b.id_p_seqno ";
        sqlCmd += "  and    b.stmt_cycle = c.stmt_cycle ";
        sqlCmd += "  and    b.id_p_seqno = a.id_corp_p_seqno ";
        sqlCmd += "  and    decode(a.close_flag,'','x',a.close_flag)<>'Y' ";
        sqlCmd += "  and    a.apr_date <> '' ";
        sqlCmd += "  and    a.id_corp_type = '1' ";
        /*sunny add start*/
        sqlCmd += "  and    a.lgd_early_ym >= '201607' ";
        sqlCmd += "  and    substr(?,1,6) >= to_char(add_months(to_date(a.lgd_early_ym,'yyyymm'),24),'yyyymm') ";
        /*sunny add end*/
        sqlCmd += "  ) TT ";
        sqlCmd += " group by id_corp_p_seqno, id_corp_no, p_seqno ";
        setString(1, hSysDate);

        totalCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_lgd_tmp_1 error[dupRecord]", "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void insertColLgdTmp2() throws Exception {
//        sqlCmd = "insert into col_lgd_tmp (";
//        sqlCmd += "id_corp_type,";
//        sqlCmd += "id_corp_p_seqno,";  //phopho add
//        sqlCmd += "id_corp_no,";
//        sqlCmd += "interest_date,";
//        sqlCmd += "crt_901_date,";
//        sqlCmd += "p_seqno,";
//        sqlCmd += "lgd_early_ym,";
//        sqlCmd += "payment_rate1_cnt,";
//        sqlCmd += "acct_status,";
//        sqlCmd += "status_cnt,";
//        sqlCmd += "data_flag,";
//        sqlCmd += "lgd_rowid)";
//        sqlCmd += " select ";
//        sqlCmd += "max(a.id_corp_type),";
//        sqlCmd += "a.id_corp_p_seqno,";  //phopho add
//        sqlCmd += "a.id_corp_no,";
//        sqlCmd += "max(decode(b.last_pay_date,'','',";
//        sqlCmd += "    to_char(add_months(to_date(b.last_pay_date,'yyyymmdd'),";
//        sqlCmd += "    decode(sign(b.stmt_cycle-substr(b.last_pay_date,7,2)),-1";
//        sqlCmd += "    ,1,0)),'yyyymmdd'))),";
//        sqlCmd += "max(crt_901_date),";
//        sqlCmd += "b.p_seqno,";
//        sqlCmd += "min(a.lgd_early_ym),";
//        sqlCmd += "max(decode(b.payment_rate1,'0A',1,'0B',1,'0E',1,0)),";
//        sqlCmd += "max(b.acct_status),";
//        sqlCmd += "max(decode(sign(sysdate-add_months(to_date(b.status_change_date,'yyyymmdd'),24)),-1,0,1)),";
//        sqlCmd += "max(decode(data_table,'nego','1','renew','1','liqu','1','0')),";
//        sqlCmd += "max(a.rowid) ";
//        sqlCmd += "from   col_lgd_901 a, act_acno b ";
//        sqlCmd += "where  b.p_seqno = b.gp_no ";
//        sqlCmd += "and    b.acct_key = a.id_corp_no||'000' ";
//        sqlCmd += "and    a.id_corp_type != '1' ";
//        sqlCmd += "and    decode(a.close_flag,'','x',a.close_flag)<>'Y' ";
//        sqlCmd += "and    a.apr_date <> '' ";
////        sqlCmd += "GROUP BY a.id_corp_no,b.p_seqno ";
//        sqlCmd += "GROUP BY a.id_corp_p_seqno,a.id_corp_no,b.p_seqno ";
        
        sqlCmd = "insert into col_lgd_tmp (";
        sqlCmd += "id_corp_type,";
        sqlCmd += "id_corp_p_seqno,";  //phopho add
        sqlCmd += "id_corp_no,";
        sqlCmd += "interest_date,";
        sqlCmd += "crt_901_date,";
        sqlCmd += "p_seqno,";
        sqlCmd += "lgd_early_ym,";
        sqlCmd += "payment_rate1_cnt,";
        sqlCmd += "acct_status,";
        sqlCmd += "status_cnt,";
        sqlCmd += "data_flag,";
        sqlCmd += "lgd_rowid)";
        sqlCmd += " select ";
        sqlCmd += "max(id_corp_type), ";
        sqlCmd += "id_corp_p_seqno, ";
        sqlCmd += "id_corp_no, ";
        sqlCmd += "max(last_pay_date), ";
        sqlCmd += "max(crt_901_date), ";
        sqlCmd += "p_seqno, ";
        sqlCmd += "min(lgd_early_ym), ";
        sqlCmd += "max(payment_rate1), ";
        sqlCmd += "max(acct_status), ";
        sqlCmd += "max(status_change_date), ";
        sqlCmd += "max(data_table), ";
        sqlCmd += "max(lgdrowid) ";
        sqlCmd += " from ( ";
        sqlCmd += "  select a.id_corp_type, ";
        sqlCmd += "      a.id_corp_p_seqno, ";
        sqlCmd += "      a.id_corp_no, ";
        sqlCmd += "      decode(b.last_pay_date,'','',to_char(add_months(to_date(b.last_pay_date,'yyyymmdd'), ";
        sqlCmd += "          decode(sign(b.stmt_cycle-substr(b.last_pay_date,7,2)),-1 ";
        sqlCmd += "          ,1,0)),'yyyymmdd')) last_pay_date, ";
        sqlCmd += "      a.crt_901_date, ";
//        sqlCmd += "      b.p_seqno, ";
        sqlCmd += "      b.acno_p_seqno as p_seqno, ";
        sqlCmd += "      a.lgd_early_ym, ";
        sqlCmd += "      decode(b.payment_rate1,'0A',1,'0B',1,'0E',1,0) payment_rate1, ";
        sqlCmd += "      b.acct_status, ";
        sqlCmd += "      decode(sign(sysdate-add_months(to_date(b.status_change_date,'yyyymmdd'),24)),-1,0,1) status_change_date, ";
        sqlCmd += "      decode(data_table,'nego','1','renew','1','liqu','1','0') data_table, ";
        sqlCmd += "      a.rowid lgdrowid ";
        sqlCmd += "  from   col_lgd_901 a, act_acno b ";
//        sqlCmd += "  where  b.p_seqno = b.gp_no ";
        sqlCmd += "  where  b.acno_flag <> 'Y' ";
        sqlCmd += "  and    b.acct_key = a.id_corp_no||'000' ";
        sqlCmd += "  and    decode(a.close_flag,'','x',a.close_flag)<>'Y' ";
        sqlCmd += "  and    a.apr_date <> '' ";
        sqlCmd += "  and    a.id_corp_type != '1' ";
        /*sunny add start*/
        sqlCmd += "  and    a.lgd_early_ym >= '201607' ";
        sqlCmd += "  and    substr(?,1,6) >= to_char(add_months(to_date(a.lgd_early_ym,'yyyymm'),24),'yyyymm') ";
		/* and    substr(to_char(sysdate,'YYYYMMDD'),1,6) >= to_char(add_months(to_date(a.lgd_early_ym,'yyyymm'),24),'yyyymm')*/
        /*sunny add end*/
        sqlCmd += "  ) TT ";
        sqlCmd += " group by id_corp_p_seqno, id_corp_no, p_seqno ";
        setString(1, hSysDate);

        totalCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_lgd_tmp_2 error[dupRecord]", "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void updateColLgdTmp1() throws Exception {
        daoTable = "col_lgd_tmp a";
        updateSQL = "close_reason = 'A1' ";
        whereStr = "WHERE exists (select 1 from  col_lgd_tmp      where id_corp_type = a.id_corp_type "
                + "      and   id_corp_no   = a.id_corp_no      group by id_corp_type,id_corp_no "
                + "      having count(*) = sum(payment_rate1_cnt)) ";

        totalCnt = updateTable();
    }

    // ************************************************************************
    private void updateColLgdTmp2() throws Exception {
//        daoTable = "col_lgd_tmp a";
//        updateSQL = "close_reason = 'B2' ";
//        whereStr = "WHERE exists (select 1 from  col_lgd_tmp      where id_corp_type = a.id_corp_type "
//                + "      and   id_corp_no   = a.id_corp_no     and   acct_status = '4' "
//                + "      and   to_char(add_months(to_date(interest_date,'yyyymmdd'),6),'yyyymmdd')< ? "
//                + "      group by id_corp_type,id_corp_no    having count(*) = sum(status_cnt)) "
//                + "and   close_reason = '' ";
//        setString(1, h_busi_business_date);
        
    	//yyyymmdd ERROR
        daoTable = "col_lgd_tmp a";
        updateSQL = "close_reason = 'B2' ";
        whereStr = "WHERE exists (select 1  from col_lgd_tmp  where id_corp_type = a.id_corp_type "
                + "      and   id_corp_no = a.id_corp_no  and acct_status = '4' "
                + "      and   to_char(add_months(to_date(decode(interest_date,'',?,interest_date),'yyyymmdd'),6),'yyyymmdd')< ? "
                /*sunny add start*/
                + "      and   lgd_early_ym >= '201607' "
                + "      and   substr(?,1,6) >= to_char(add_months(to_date(lgd_early_ym,'yyyymm'),24),'yyyymm') "
                /*and substr(to_char(sysdate,'YYYYMMDD'),1,6) >= to_char(add_months(to_date(lgd_early_ym,'yyyymm'),24),'yyyymm')*/
                /*sunny add end*/
                + "      group by id_corp_type,id_corp_no  having count(*) = sum(status_cnt)) "
                + "and   close_reason = '' ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hSysDate);

        totalCnt = updateTable();
    }

    // ************************************************************************
    private void updateColLgdTmp3() throws Exception {
        daoTable = "col_lgd_tmp a";
        updateSQL = "jrnl_cnt = (select count(*) from cyc_pyaj   where  p_seqno =a.p_seqno "
                + "    and    class_code='P'   and    payment_date <= a.crt_901_date "
                + "    and    payment_date >= to_char(add_months(to_date(a.crt_901_date,'yyyymmdd'),-6),'yyyymmdd') "
                + "    and    payment_amt > 0) ";
        whereStr = "WHERE close_reason = '' and   data_flag = '1' and   crt_901_date <> '' ";
    	
        totalCnt = updateTable();
    }

    // ************************************************************************
//    private void update_col_lgd_tmp_3x() throws Exception {
//        daoTable = "col_lgd_tmp a";
//        updateSQL = "jrnl_cnt = (select count(*) from act_jrnl   where  p_seq =a.p_seqno "
//                + "    and    (tran_class='P' or tran_type='DR11')    and    acct_date <= a.crt_901_date "
//                + "    and    acct_date >= to_char(add_months(to_date(a.crt_901_date,'yyyymmdd'),-6),'yyyymmdd') "
//                + "    and    transaction_amt > 0) ";
//        whereStr = "WHERE close_reason = '' and   data_flag = '1' and   crt_901_date <> '' ";
//
//        total_cnt = updateTable();
//
//    }
//
//    // ************************************************************************
//    private void update_col_lgd_tmp_3y() throws Exception {
//        selectSQL = "a.p_seqno, max(b.rowid) rowid, count(*) jrnl_cnt ";
//        daoTable = "act_jrnl a,col_lgd_tmp b";
//        whereStr = "where a.p_seqno = b.p_seqno and   b.close_reason = '' and   b.data_flag = '1' "
//                + "and   b.crt_901_date <> '' and   (a.tran_class='P' or a.tran_type='DR11') "
//                + "and   a.acct_date <= b.crt_901_date "
//                + "and   a.acct_date >= to_char(add_months(to_date(b.crt_901_date,'yyyymmdd'),-6),'yyyymmdd') "
//                + "and   a.transaction_amt > 0 group by a.p_seqno ";
//
//        openCursor();
//
//        while (fetchTable()) {
//            h_cldd_p_seqno = getValue("p_seqno");
//            h_cldd_rowid = getValue("rowid");
//            h_cldd_jrnl_cnt = getValueInt("jrnl_cnt");
//
//            total_cnt++;
//            if (total_cnt % 10000 == 0)
//                showLogMessage("I", "", "    目前處理筆數 =[" + total_cnt + "]");
//
//            update_col_lgd_tmp_7();
//        }
//        closeCursor();
//    }

    // ************************************************************************
//    private void update_col_lgd_tmp_7() throws Exception {
//        daoTable = "col_lgd_tmp";
//        updateSQL = "jrnl_cnt = ? ";
//        whereStr = "WHERE rowid = ? ";
//        setInt(1, h_cldd_jrnl_cnt);
//        setRowId(2, h_cldd_rowid);
//
//        updateTable();
//
//        if (notFound.equals("Y")) {
//            comcr.err_rtn("update_col_lgd_tmp_7 error!", "", h_call_batch_seqno);
//        }
//    }

    // ************************************************************************
    private void updateColLgdTmp4() throws Exception {
        daoTable = "col_lgd_tmp a";
        updateSQL = "close_reason = 'A2' ";
        whereStr = "WHERE exists (select 1 from col_lgd_tmp      where id_corp_type = a.id_corp_type "
                + "      and   id_corp_no   = a.id_corp_no      group by id_corp_type,id_corp_no "
                + "      having  sum(jrnl_cnt)>=6) and   close_reason = '' and   data_flag = '1' "
                + "and   crt_901_date <> '' ";

        totalCnt = updateTable();
    }

    // ************************************************************************
//    private void update_col_lgd_tmp_5() throws Exception {
//        daoTable = "col_lgd_tmp a";
//        updateSQL = "interest_date = (select max(interest_date) from act_jrnl "
//                + "                 where  p_seqno =a.p_seqno) ";
//
//        total_cnt = updateTable();
//
//    }

    // ************************************************************************
    private void updateColLgdTmp6() throws Exception {
        daoTable = "col_lgd_tmp a";
        updateSQL = "acct_jrnl_bal = (select nvl(sum(decode(sign(stmt_this_ttl_amt), "
                + "                 -1,0,stmt_this_ttl_amt)),0)     from act_acct_hst "
                + "                 where  acct_month = to_char(add_months(to_date( "
//                + "                        substr(a.interest_date,1,6),'yyyymm'),-1),'yyyymm') "
                + "                        substr(decode(a.interest_date,'','20000101',a.interest_date),1,6),'yyyymm'),-1),'yyyymm') "
                + "                 and    min_pay_bal = 0     and    p_seqno = a.p_seqno) ";
        
        totalCnt = updateTable();
    }

    // ************************************************************************
    private void deleteColLgdTmp() throws Exception {
        daoTable = "col_lgd_tmp";
        whereStr = "where close_reason = '' ";

        totalCnt = deleteTable();
    }

    // ************************************************************************
    private void selectColLgdTmp() throws Exception {
		selectSQL = "id_corp_p_seqno, id_corp_no, max(hex(lgd_rowid)) lgd_rowid, min(lgd_early_ym) lgd_early_ym, "
				+ "max(substr(interest_date,1,6)) interest_date, max(close_reason) close_reason ";
		daoTable = "col_lgd_tmp";
//		whereStr = "group by id_corp_no ";
		/*sunny add start*/
		whereStr = "where lgd_early_ym >= '201607' "
				+ "   and substr(?,1,6) >= to_char(add_months(to_date(lgd_early_ym,'yyyymm'),24),'yyyymm') "
				/*and   substr(to_char(sysdate,'YYYYMMDD'),1,6) > to_char(add_months(to_date(lgd_early_ym,'yyyymm'),24),'yyyymm')*/
				+ " group by id_corp_p_seqno, id_corp_no ";
		setString(1, hSysDate);
		/*sunny add end*/

		openCursor();
        while (fetchTable()) {
        	hLgd2IdCorpPSeqno = getValue("id_corp_p_seqno");
            hLgd2IdCorpNo = getValue("id_corp_no");
            hClddLgdRowid = getValue("lgd_rowid");
            hLgd2EarlyYm = getValue("lgd_early_ym");
            hLgd2CostsYm = getValue("interest_date");
            hLgd2CloseReason = getValue("close_reason");

            totalCnt++;
            if (totalCnt % 20000 == 0)
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");

            selectColLgd9011();
            selectColLgd9012();
            selectColLgdTmp1();
            selectColLgdJrnl();

            if (hLgd2RecvSelfAmt + hLgd2RecvRelaAmt + hLgd2RecvOthAmt > 0)
                updateColLgdJrnl();

            hLgd2CardRelaType = "N";
            if (hLgd2IdCorpType.equals("1"))
                selectCrdRela();

            selectActAcnoRate();

            insertColLgd902();
            updateColLgd901();
        }
        closeCursor();
    }

    // ************************************************************************
    private void selectColLgd9011() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "lgd_seqno, ";
        sqlCmd += "id_corp_type, ";
        sqlCmd += "acct_p_seqno, ";
        sqlCmd += "overdue_ym, ";
        sqlCmd += "overdue_amt, ";
        sqlCmd += "coll_ym, ";
        sqlCmd += "coll_amt ";
        sqlCmd += "from col_lgd_901 ";
        sqlCmd += "where rowid = ? ";
        setRowId(1, hClddLgdRowid);
        
        extendField = "col_lgd_901_1.";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_lgd_901_1 error!", "", hCallBatchSeqno);
        }
        hLgd2LgdSeqno = getValue("col_lgd_901_1.lgd_seqno");
        hLgd2IdCorpType = getValue("col_lgd_901_1.id_corp_type");
        hLgd2AcctPSeqno = getValue("col_lgd_901_1.acct_p_seqno");
        hLgd2OverdueYm = getValue("col_lgd_901_1.overdue_ym");
        hLgd2OverdueAmt = getValueDouble("col_lgd_901_1.overdue_amt");
        hLgd2CollYm = getValue("col_lgd_901_1.coll_ym");
        hLgd2CollAmt = getValueDouble("col_lgd_901_1.coll_amt");
    }

    // ************************************************************************
    private void selectColLgd9012() throws Exception {
        hLgd2RiskAmt = 0;
        sqlCmd = "select risk_amt ";
        sqlCmd += "from col_lgd_901 ";
//        sqlCmd += "where id_corp_no = ? ";
        sqlCmd += "where id_corp_p_seqno = ? ";
        sqlCmd += "and   lgd_early_ym = ? ";
        sqlCmd += "and   nvl(risk_amt,0) >0 ";
        sqlCmd += "and   apr_date <> '' ";
        sqlCmd += "and   close_date = '' ";
        sqlCmd += "and   decode(lgd_seqno,'','x', lgd_seqno) =? ";
        sqlCmd += "fetch first 1 row only ";
//        setString(1, h_lgd2_id_corp_no);
        setString(1, hLgd2IdCorpPSeqno);
        setString(2, hLgd2EarlyYm);
        setString(3, hLgd2LgdSeqno.length() == 0 ? "x" : hLgd2LgdSeqno);

        extendField = "col_lgd_901_2.";
        
        if (selectTable() > 0) {
            hLgd2RiskAmt = getValueDouble("col_lgd_901_2.risk_amt");
        }
    }

    // ************************************************************************
    private void selectColLgdTmp1() throws Exception {
        hLgd2CostsAmt = 0;
        sqlCmd = "select sum(acct_jrnl_bal) acct_jrnl_bal ";
        sqlCmd += "from col_lgd_tmp ";
//        sqlCmd += "where id_corp_no = ? ";
        sqlCmd += "where id_corp_p_seqno = ? ";
        sqlCmd += "and   substr(interest_date,1,6) = ? ";
//        setString(1, h_lgd2_id_corp_no);
        setString(1, hLgd2IdCorpPSeqno);
        setString(2, hLgd2CostsYm);

        extendField = "col_lgd_tmp_1.";
        
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_lgd_tmp_1 error!", "", hCallBatchSeqno);
        }
        hLgd2CostsAmt = getValueDouble("col_lgd_tmp_1.acct_jrnl_bal");
    }

    // ************************************************************************
    private void selectColLgdJrnl() throws Exception {
        hLgd2RecvSelfAmt = 0;
        hLgd2RecvRelaAmt = 0;
        hLgd2RecvOthAmt = 0;
        sqlCmd = "select ";
        sqlCmd += "sum(decode(lgd_coll_flag,'1',nvl(trans_amt,0),0)) self_amt, ";
        sqlCmd += "sum(decode(lgd_coll_flag,'2',nvl(trans_amt,0),0)) rela_amt, ";
        sqlCmd += "sum(decode(lgd_coll_flag,'3',nvl(trans_amt,0),0)) oth_amt ";
        sqlCmd += "from col_lgd_jrnl ";
//        sqlCmd += "where id_corp_no = ? ";
        sqlCmd += "where id_corp_p_seqno = ? ";
        sqlCmd += "and   apr_date <> '' ";
        sqlCmd += "and   close_date = '' ";
//        setString(1, h_lgd2_id_corp_no);
        setString(1, hLgd2IdCorpPSeqno);
        
        extendField = "col_lgd_jrnl.";

        if (selectTable() > 0) {
            hLgd2RecvSelfAmt = getValueDouble("col_lgd_jrnl.self_amt");
            hLgd2RecvRelaAmt = getValueDouble("col_lgd_jrnl.rela_amt");
            hLgd2RecvOthAmt = getValueDouble("col_lgd_jrnl.oth_amt");
        }
    }

    // ************************************************************************
    private void updateColLgdJrnl() throws Exception {
        daoTable = "col_lgd_jrnl";
        updateSQL = "lgd_seqno = ?, close_date = ? ";
//        whereStr = "WHERE id_corp_no = ? and apr_date <> '' and close_date = '' ";
        whereStr = "WHERE id_corp_p_seqno = ? and apr_date <> '' and close_date = '' ";
        setString(1, hLgd2LgdSeqno);
        setString(2, hSysDate);
//        setString(3, h_lgd2_id_corp_no);
        setString(3, hLgd2IdCorpPSeqno);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_lgd_jrnl error!", "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void selectCrdRela() throws Exception {
        int hCnt = 0;
//        sqlCmd = "select 1 hcnt ";
//        sqlCmd += "from crd_rela a, crd_idno b ";
//        sqlCmd += "where b.id_p_seqno = a.id_p_seqno ";
//        sqlCmd += "and b.id_no = ? ";
//        sqlCmd += "and rela_type='1' ";
//        sqlCmd += "fetch first 1 row only ";
//        setString(1, h_lgd2_id_corp_no);
        
        sqlCmd = "select 1 hcnt ";
        sqlCmd += "from crd_rela ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and rela_type = '1' ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hLgd2IdCorpPSeqno);
        
        extendField = "crd_rela.";

        if (selectTable() > 0) {
            hCnt = getValueInt("crd_rela.hcnt");
        }

        if (hCnt > 0)
            hLgd2CardRelaType = "Y";
    }

    // ************************************************************************
    private void selectActAcnoRate() throws Exception {
        int hCnt = 0;
        hLgd2RevolRate = 0;
        sqlCmd = "select a.real_int_rate ";
        sqlCmd += "from cyc_acno_rate a, col_lgd_tmp b ";
        sqlCmd += "where a.p_seqno = b.p_seqno ";
//        sqlCmd += "and   b.id_corp_no = ? ";
        sqlCmd += "and   b.id_corp_p_seqno = ? ";
        sqlCmd += "ORDER by a.acct_month desc, a.real_int_rate desc ";
//        setString(1, h_lgd2_id_corp_no);
        setString(1, hLgd2IdCorpPSeqno);
        
        extendField = "act_acno_rate.";

        hCnt = selectTable();
        if (hCnt > 0) {
            hLgd2RevolRate = getValueInt("act_acno_rate.real_int_rate");
        }

        if ((hLgd2RevolRate == 0) || (hCnt == 0))
            hLgd2RevolRate = hAgenRevolvingInterest1;
    }

    // ************************************************************************
    private void insertColLgd902() throws Exception {
    	daoTable = "col_lgd_902";
    	extendField = daoTable + ".";
    	setValue(extendField+"id_corp_p_seqno", hLgd2IdCorpPSeqno);
        setValue(extendField+"id_corp_no", hLgd2IdCorpNo);
        setValue(extendField+"id_corp_type", hLgd2IdCorpType);
        setValue(extendField+"close_date", hSysDate);
        setValue(extendField+"lgd_seqno", hLgd2LgdSeqno);
        setValue(extendField+"acct_p_seqno", hLgd2AcctPSeqno);
        setValue(extendField+"aud_code", "A");
        setValue(extendField+"send_ym", "");
        setValue(extendField+"lgd_type", "F");
        setValue(extendField+"early_ym", hLgd2EarlyYm);
        setValueDouble(extendField+"risk_amt", hLgd2RiskAmt);
        setValue(extendField+"overdue_ym", hLgd2OverdueYm);
        setValueDouble(extendField+"overdue_amt", hLgd2OverdueAmt);
        setValue(extendField+"coll_ym", hLgd2CollYm);
        setValueDouble(extendField+"coll_amt", hLgd2CollAmt);
        setValueDouble(extendField+"recv_self_amt", hLgd2RecvSelfAmt);
        setValueDouble(extendField+"recv_rela_amt", hLgd2RecvRelaAmt);
        setValueDouble(extendField+"recv_oth_amt", hLgd2RecvOthAmt);
        setValueDouble(extendField+"costs_amt", hLgd2CostsAmt);
        setValue(extendField+"costs_ym", hLgd2CostsYm);
        setValueDouble(extendField+"revol_rate", hLgd2RevolRate);
        setValue(extendField+"crdt_charact", "");
        setValue(extendField+"assure_type", "C");
        setValue(extendField+"crdt_use_type", "4");
        setValue(extendField+"syn_loan_yn", "N");
        setValue(extendField+"syn_loan_date", "");
        setValue(extendField+"fina_commit_yn", "N");
        setValue(extendField+"ecic_case_type", "");
        setValueDouble(extendField+"fina_commit_prct", 0.00);
        setValue(extendField+"card_rela_type", hLgd2CardRelaType);
        setValueDouble(extendField+"recv_trade_amt", 0);
        setValueDouble(extendField+"recv_collat_amt", 0);
        setValueDouble(extendField+"recv_fina_amt", 0);
        setValue(extendField+"close_reason", hLgd2CloseReason);
        setValue(extendField+"collat_yn", "");
        setValue(extendField+"send_flag", "N");
        setValue(extendField+"send_date", "");
        setValue(extendField+"from_type", "2");
        setValue(extendField+"crt_user", "SYSTEM");
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"apr_date", sysDate);
        setValue(extendField+"apr_user", "SYSTEM");
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_lgd_902 error[dupRecord]", "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void updateColLgd901() throws Exception {
        daoTable = "col_lgd_901";
        updateSQL = "close_flag ='Y', close_date = ? ";
//        whereStr = "WHERE id_corp_no = ? and decode(close_flag,'','x',close_flag)<>'Y' and apr_date <> '' ";
        whereStr = "WHERE id_corp_p_seqno = ? and decode(close_flag,'','x',close_flag)<>'Y' and apr_date <> '' ";
        setString(1, hSysDate);
//        setString(2, h_lgd2_id_corp_no);
        setString(2, hLgd2IdCorpPSeqno);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_lgd_901 error!", "", hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void insertColLgdBatch() throws Exception {
    	daoTable = "col_lgd_batch";
    	extendField = daoTable + ".";
    	setValue(extendField+"pgm_id", javaProgram);
        setValue(extendField+"pgm_parm", hBusiBusinessDate);
        setValue(extendField+"exec_time_s", sysDate);
        setValue(extendField+"exec_time_e", sysDate);
        setValueLong(extendField+"tot_cnt", totalCnt);
        setValue(extendField+"ok_flag", "Y");
        setValue(extendField+"run2_flag", "N");

        insertTable();
    }
    // ************************************************************************
}