/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/02/07  V1.00.00    phopho     program initial                          *
* 109/12/12  V1.00.01    shiyuqi       updated for project coding standard   *
* 112/08/05  V1.00.03    sunny      執行錯誤訊息加強顯示                                                    *
* 112/11/30  V1.00.04    sunny      修正執行參數程式錯誤
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColB014 extends AccessDAO {
    private String progname = "每月逾催呆統計處理程式 112/11/30  V1.00.04 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    String hCallBatchSeqno = "";

    String hStatProcMonth = "";
    String hMndaProcMonth = "";
    String hMndaPSeqno = "";
    String hMndaId = "";
    String hMndaTransType = "";
    String hCbdlTransDate = "";
    double hTempRoundAmt = 0;
    double hTempDelAmt = 0;  //add by phopho
    String hStatTransType = "";
    String hStatSubTransType = "";
    String hStatModUser = "";
    String hStatModPgm = "";
    int hStatAcctCnt = 0;
    long hStatAcctJrnlBal = 0;
    long hStatRecvAiAmt = 0;  //add by phopho
    String hMndaProcDate = "";
    String hMndaRecourseMark = "";
    long hMndaMcode = 0;
    long hMndaAcctJrnlBal = 0;
    long hMndaDelAmt = 0;  //add by phopho
    String hTempProcMonth = "";
    String hTempPSeqno = "";
    String hBusiBusinessDate = "";
    long hCbdlEndBal = 0;
    String hTempTransType = "";
    String hTempPProcDate = "";
    String hTempNProcDate = "";

    String hTempPTransType = "";
    String hTempPRecourseMark = "";
    double hTempPAcctJrnlBal = 0;
    double hTempP1AcctJrnlBal = 0;
    double hTempP2AcctJrnlBal = 0;
    long hTempPMcode = 0;
    String hTempNTransType = "";
    String hTempNRecourseMark = "";
    double hTempNAcctJrnlBal = 0;
    double hTempN1AcctJrnlBal = 0;
    double hTempN2AcctJrnlBal = 0;
    double hTempNMcode = 0;
    double hTempNDelAmt = 0;  //add by phopho
    double hTempAcctJrnlBal = 0;
    double hTemp1EndBal = 0;
    double hTemp2EndBal = 0;
    double hTemp3EndBal = 0;
    String tempMonth = "";
    String temstr                  = "";
    double a1                      = 0;
    double a2                      = 0;
    double a3                      = 0;
    double a4                      = 0;
    double hJrnlDelAmt             = 0; //add
    int    hJrnlDelCnt             = 0; //add
    
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
                comc.errExit("Usage : ColB014 [month]", "                 1.month : YYYMM ");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hStatModUser = comc.commGetUserID();
            hStatModPgm = javaProgram;

            comcr.callbatch(0, 0, 0);

            selectPtrBusinday();
            if (args.length == 0) {
                tempMonth = comc.getSubString(hBusiBusinessDate,0,6);
//                temstr = comcr.get_businday(temp_month, -2);
                temstr = comcr.getBusinday(hBusiBusinessDate, -2);
                if (!comc.getSubString(hBusiBusinessDate,0,8).equals(temstr)) {
                	exceptExit = 0;
                    comcr.errRtn("本報表只能在每月最後營業日前一日執行", String.format("最後營業日前一日[%s]", temstr), hCallBatchSeqno);
                }
                hStatProcMonth = tempMonth;
            } else {            	
            	 if (args[0].length() != 6) {
                 	exceptExit = 0;
                 	 comcr.errRtn(String.format("參數錯誤[%s]，請輸入執行月份(YYYYMM)", args[0]), "", hCallBatchSeqno);
                 }
                //hStatProcMonth = String.format("%6d", comcr.str2long(args[0]) + 191100);
            	 hStatProcMonth = args[0];
            }

            deleteColStaticrate();
            commitDataBase();

            selectProcMonth();
            procProcDate();
            selectColMonthData0();
            selectColBadDebt();
            selectColBadDetail0();
            insertColStaticrate3();
            selectColMonthData1();
            selectColMonthData2();
            //因應 colr0040中，增加一筆資料，紀錄帳外息(AI)還款總金額。
//            selectColMonthData7();
//            insertColStaticrate7();
            selectActJrnl();
            updateColStaticrate41();
            updateColStaticrate50();

            comcr.hCallErrorDesc = "程式執行結束";
            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
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
        hBusiBusinessDate = "";

        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void deleteColStaticrate() throws Exception {
        daoTable = "col_staticrate";
        whereStr = "where proc_month = ? ";
        setString(1, hStatProcMonth);
        deleteTable();
    }

    /***********************************************************************/
    void selectProcMonth() throws Exception { /* get 上月月份 */
        hMndaProcMonth = "";

        sqlCmd = "select to_char(add_months(to_date(?,'yyyymm'),-1), 'yyyymm') h_mnda_proc_month ";
        sqlCmd += " from dual ";
        setString(1, hStatProcMonth);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_proc_month not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hMndaProcMonth = getValue("h_mnda_proc_month");
        }
    }

    /***********************************************************************/
    void procProcDate() throws Exception {
        hTempPProcDate = "";
        sqlCmd = "select proc_date ";
        sqlCmd += " from col_month_data  ";
        sqlCmd += "where proc_month = ?  ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hMndaProcMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempPProcDate = getValue("proc_date");
        } else
            hTempPProcDate = "20000101";

        hTempNProcDate = "";
        sqlCmd = "select proc_date ";
        sqlCmd += " from col_month_data  ";
        sqlCmd += "where proc_month = ?  ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hStatProcMonth);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempNProcDate = getValue("proc_date");
        } else
            hTempNProcDate = "30000101";
    }

    /***********************************************************************/
    void selectColMonthData0() throws Exception { /* 目前 */
        long tempAmt;

        sqlCmd = "select ";
        sqlCmd += "p_seqno ";
        sqlCmd += "from col_month_data ";
        sqlCmd += "where proc_month = ? ";
        sqlCmd += "and trans_type > '1' ";
        setString(1, hStatProcMonth);

        openCursor();
        while (fetchTable()) {
            hTempPSeqno = getValue("p_seqno");

            hTempProcMonth = hStatProcMonth;
            selectColMonthData();
            hStatTransType = hMndaTransType;
            if (hStatTransType.equals("4")) {
                tempAmt = hMndaAcctJrnlBal;
            } else {
                tempAmt = hMndaAcctJrnlBal; /* 仟元(*0.001 +0.5)-->元 */
            }
            hStatAcctJrnlBal = tempAmt;

            if (hStatTransType.equals("1"))
                continue;
            if (!hStatTransType.equals("4")) {
                hStatSubTransType = "0";
                updateColStaticrate1();
                if (hStatTransType.equals("3")) {
                    if (hMndaMcode <= 12)
                        hStatSubTransType = "1";
                    if ((hMndaMcode > 12) && (hMndaMcode <= 24))
                        hStatSubTransType = "2";
                    if (hMndaMcode > 24)
                        hStatSubTransType = "3";
                    updateColStaticrate1();
                }
            } else {
                if (hMndaRecourseMark.equals("Y")) {
                    hStatSubTransType = "1";
                    updateColStaticrate1();
                } else {
                    hStatSubTransType = "0";
                    updateColStaticrate1();
                }
                hStatSubTransType = "0";
                hStatTransType = "5";
                updateColStaticrate1();
            }

        }
        closeCursor();
    }

    /***********************************************************************/
    void updateColStaticrate1() throws Exception {
        hTempRoundAmt = hStatAcctJrnlBal;
        daoTable = "col_staticrate";
        updateSQL = "acct_cnt  = acct_cnt + 1,";
        updateSQL += " acct_jrnl_bal = acct_jrnl_bal + ?";
        whereStr = "where proc_month = ?  ";
        whereStr += "and trans_type = ?  ";
        whereStr += "and sub_trans_type = ? ";
        setDouble(1, hTempRoundAmt);
        setString(2, hStatProcMonth);
        setString(3, hStatTransType);
        setString(4, hStatSubTransType);
        updateTable();
        if (notFound.equals("Y")) {
        	daoTable = "col_staticrate";
        	extendField = daoTable + ".";
            setValue(extendField+"proc_month", hStatProcMonth);
            setValue(extendField+"trans_type", hStatTransType);
            setValue(extendField+"sub_trans_type", hStatSubTransType);
            setValueInt(extendField+"acct_cnt", 1);
            setValueDouble(extendField+"acct_jrnl_bal", hTempRoundAmt);
            setValue(extendField+"mod_user", hStatModUser);
            setValue(extendField+"mod_time", sysDate + sysTime);
            setValue(extendField+"mod_pgm", hStatModPgm);
            
            insertTable();
            if (dupRecord.equals("Y")) {
                comcr.errRtn("insert_col_staticrate duplicate!", "", hCallBatchSeqno);
            }
        }
        return;
    }

    /***********************************************************************/
    void selectColBadDebt() throws Exception {
        hStatAcctCnt = 0;

        sqlCmd = "select count(*) h_stat_acct_cnt ";
        sqlCmd += " from col_bad_debt  ";
        sqlCmd += "where trans_type = '4'  ";
        sqlCmd += "and trans_date > ?  ";
        sqlCmd += "and trans_date <= ?  ";
        sqlCmd += "and src_amt > 0 ";
        setString(1, hTempPProcDate);
        setString(2, hTempNProcDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_bad_debt not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hStatAcctCnt = getValueInt("h_stat_acct_cnt");
        }

    }

    /***********************************************************************/
    void selectColBadDetail0() throws Exception {
        hStatAcctJrnlBal = 0;

        sqlCmd = "select sum(end_bal) h_stat_acct_jrnl_bal ";
        sqlCmd += " from col_bad_detail  ";
        sqlCmd += "where trans_type = '4'  ";
        sqlCmd += "and trans_date > ?  ";
        sqlCmd += "and trans_date <= ? ";
        setString(1, hTempPProcDate);
        setString(2, hTempNProcDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_bad_detail not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hStatAcctJrnlBal = getValueLong("h_stat_acct_jrnl_bal");
        }

    }

    /***********************************************************************/
    void insertColStaticrate3() throws Exception { /* 新增 */
    	daoTable = "col_staticrate";
    	extendField = daoTable + ".";
        setValue(extendField+"proc_month", hStatProcMonth);
        setValue(extendField+"trans_type", "6");
        setValue(extendField+"sub_trans_type", "0");
        setValueInt(extendField+"add_acct_cnt", hStatAcctCnt);
        setValueLong(extendField+"add_acct_amt", hStatAcctJrnlBal);
        setValueInt(extendField+"acct_cnt", hStatAcctCnt);
        setValueLong(extendField+"acct_jrnl_bal", hStatAcctJrnlBal);
        setValue(extendField+"mod_user", hStatModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hStatModPgm);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_staticrate duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectColMonthData1() throws Exception {
        long tempAmt;

        sqlCmd = "select ";
        sqlCmd += "p_seqno,";
        sqlCmd += "id_no ";
        sqlCmd += "from col_month_data ";
        sqlCmd += "where proc_month = ? ";
        sqlCmd += "and trans_type > '1' ";
        sqlCmd += "UNION ";
        sqlCmd += "select p_seqno, ";
        sqlCmd += "id_no ";
        sqlCmd += "from col_month_data ";
        sqlCmd += "where proc_month = ? ";
        sqlCmd += "and trans_type > '1' ";
        sqlCmd += "order by 2 ";
        setString(1, hMndaProcMonth);
        setString(2, hStatProcMonth);

        openCursor();
        while (fetchTable()) {
            hMndaPSeqno = getValue("p_seqno");
            hMndaId = getValue("id_no");

            hTempPSeqno = hMndaPSeqno;

            hTempProcMonth = hStatProcMonth; /* 本月 */
            selectColMonthData();
            hTempNTransType = hMndaTransType;
            hTempNRecourseMark = hMndaRecourseMark;
            hTempNMcode = hMndaMcode;
            hTempNDelAmt = hMndaDelAmt;  //D檔金額 (只計算本月,不計算上月?)

            tempAmt = hMndaAcctJrnlBal;
            hTempN1AcctJrnlBal = tempAmt;
            tempAmt = hMndaAcctJrnlBal; /* 仟元(*0.001 +0.5)-->元 */
            hTempN2AcctJrnlBal = tempAmt;

            hTempProcMonth = hMndaProcMonth; /* 上月 */
            selectColMonthData();
            hTempPTransType = hMndaTransType;
            hTempPRecourseMark = hMndaRecourseMark;
            hTempPAcctJrnlBal = hMndaAcctJrnlBal;
            hTempPMcode = hMndaMcode;

            tempAmt = hMndaAcctJrnlBal;
            hTempP1AcctJrnlBal = tempAmt;
            tempAmt = hMndaAcctJrnlBal; /* 仟元(*0.001 +0.5)-->元 */
            hTempP2AcctJrnlBal = tempAmt;

            /************************ 逾放處理 *****************************/
            procTransType2();
            /************************ 催收處理 *****************************/
            procTransType3();
            /************************ 呆帳處理 *****************************/
            procTransType41();
            /************************ 追索債權 *****************************/
            procTransType42();
            /************************ 呆帳追索 *****************************/
            procTransType43();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectColMonthData() throws Exception {
        hMndaProcDate = "";
        hMndaTransType = "";
        hMndaRecourseMark = "";
        hMndaMcode = 0;
        hMndaAcctJrnlBal = 0;
        hMndaDelAmt = 0;

        sqlCmd = "select proc_date,";
        sqlCmd += "trans_type,";
        sqlCmd += "decode(recourse_mark,'','N',recourse_mark) h_mnda_recourse_mark,";
        sqlCmd += "mcode,";
        sqlCmd += "decode(trans_type, '2',billed_1_amt+unbill_1_amt, '3',billed_1_amt+unbill_1_amt+ billed_2_amt+unbill_2_amt+ billed_3_amt+unbill_3_amt, '4',billed_1_amt,acct_jrnl_bal) h_mnda_acct_jrnl_bal,";
        sqlCmd += "del_amt ";  //add by phopho
        sqlCmd += " from col_month_data  ";
        sqlCmd += "where proc_month = ?  ";
        sqlCmd += "and p_seqno = ? ";
        setString(1, hTempProcMonth);
        setString(2, hTempPSeqno);
        
        extendField = "col_month_data.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hMndaProcDate = getValue("col_month_data.proc_date");
            hMndaTransType = getValue("col_month_data.trans_type");
            hMndaRecourseMark = getValue("col_month_data.h_mnda_recourse_mark");
            hMndaMcode = getValueLong("col_month_data.mcode");
            hMndaAcctJrnlBal = getValueLong("col_month_data.h_mnda_acct_jrnl_bal");
            hMndaDelAmt = getValueLong("col_month_data.del_amt");
        } else {
            hMndaProcDate = "20000101";
            hMndaTransType = "1";
            hMndaRecourseMark = "N";
            hMndaAcctJrnlBal = 0;
            hMndaDelAmt = 0;
        }
    }

    /***********************************************************************/
    void procTransType2() throws Exception { /* 逾放處理 */
        double tempSubAmt = 0;

        hTempPAcctJrnlBal = hTempP2AcctJrnlBal;
        hTempNAcctJrnlBal = hTempN2AcctJrnlBal;
        selectColBadDetail2(3);

        hStatTransType = "2";
        hStatSubTransType = "0";
        if (hTempNTransType.equals("2")) {
            if (hTempPTransType.equals("2")) {
                hTempAcctJrnlBal = hTempNAcctJrnlBal - hTempPAcctJrnlBal;
            } else {
                hTempAcctJrnlBal = hTempNAcctJrnlBal;
            }

            if (hTempAcctJrnlBal > 0)
                updateColStaticrate3(); /* 逾放新增 */
        }

        if (hTempPTransType.equals("2")) {
            if (hTempNTransType.equals("2")) {
                hTempAcctJrnlBal = hTempPAcctJrnlBal - hTempNAcctJrnlBal;
            } else {
                hTempAcctJrnlBal = hTempPAcctJrnlBal;
            }
            if (hTempAcctJrnlBal > 0)
                updateColStaticrate2(); /* 逾放減少 */

            if (hTemp3EndBal == 0) {
                if (hTempNTransType.equals("1")) {
                    tempSubAmt = tempSubAmt + hTempPAcctJrnlBal;
                }
                if (hTempNTransType.toCharArray()[0] >= '2') {
                    hTempAcctJrnlBal = hTempPAcctJrnlBal - hTempNAcctJrnlBal;
                    if (hTempAcctJrnlBal > 0)
                        tempSubAmt = tempSubAmt + hTempAcctJrnlBal;
                }
            } else {
                hTempAcctJrnlBal = hTempPAcctJrnlBal - hTemp3EndBal;
                if (hTempAcctJrnlBal > 0)
                    tempSubAmt = tempSubAmt + hTempAcctJrnlBal;
            }
            hTempAcctJrnlBal = tempSubAmt;
            if (hTempAcctJrnlBal > 0) {
                updateColStaticrate4(); /* 逾放還款 */
                a1 = a1 + hTempAcctJrnlBal;
            }
        }
    }

    /***********************************************************************/
    void selectColBadDetail2(int balType) throws Exception {
        hCbdlEndBal = 0;

        sqlCmd = "select sum(end_bal) h_cbdl_end_bal ";
        sqlCmd += " from col_bad_detail  ";
        sqlCmd += "where trans_type = '3'  ";
        sqlCmd += "and new_acct_code = 'CB'  "; // new_item_ename
        sqlCmd += "and trans_date > ?  ";
        sqlCmd += "and trans_date <= ?  ";
        sqlCmd += "and p_seqno = ? ";
        setString(1, hTempPProcDate);
        setString(2, hTempNProcDate);
        setString(3, hTempPSeqno);
        
        extendField = "col_bad_detail_2.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_bad_detail_2 not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCbdlEndBal = getValueLong("col_bad_detail_2.h_cbdl_end_bal");
        }

        if (balType != 4) {
            hTemp3EndBal = hCbdlEndBal; /* 仟元(*0.001 +0.5)-->元 */
        } else {
            hTemp3EndBal = hCbdlEndBal;
        }
    }

    /***********************************************************************/
    void procTransType3() throws Exception {
        hTempPAcctJrnlBal = hTempP2AcctJrnlBal;
        hTempNAcctJrnlBal = hTempN2AcctJrnlBal;

        hStatTransType = "3";
        if ((hTempPTransType.equals("3")) && (hTempNTransType.equals("3"))) {
            hTempTransType = "3";
            selectColBadDetail(3);
            hStatSubTransType = "0";

            hTempAcctJrnlBal = hTemp2EndBal;

            if (hTempNAcctJrnlBal >= hTemp2EndBal) { /* 催收新增數 */
                hTempAcctJrnlBal = hTempNAcctJrnlBal;
                hStatSubTransType = "0";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
                if (hTempNMcode <= 12)
                    hStatSubTransType = "1";
                if ((hTempNMcode > 12) && (hTempNMcode <= 24))
                    hStatSubTransType = "2";
                if (hTempNMcode > 24)
                    hStatSubTransType = "3";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
            } else {
                hTempAcctJrnlBal = hTemp2EndBal;
                hStatSubTransType = "0";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
                if (hTempNMcode <= 12)
                    hStatSubTransType = "1";
                if ((hTempNMcode > 12) && (hTempNMcode <= 24))
                    hStatSubTransType = "2";
                if (hTempNMcode > 24)
                    hStatSubTransType = "3";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();

                hTempAcctJrnlBal = hTemp2EndBal - hTempNAcctJrnlBal;
                if (hTempAcctJrnlBal > 0) {
                    hStatSubTransType = "0";
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2(); /* 減少 */
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4(); /* 還款 */
                    if (hTempNMcode <= 12)
                        hStatSubTransType = "1";
                    if ((hTempNMcode > 12) && (hTempNMcode <= 24))
                        hStatSubTransType = "2";
                    if (hTempNMcode > 24)
                        hStatSubTransType = "3";
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2();
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4();
                }
            }
        }

        if ((hTempPTransType.equals("3")) && (hTempNTransType.equals("3"))) {
            hTempTransType = "3";
            selectColBadDetail(3);
            hStatTransType = "3";
            hStatSubTransType = "0";
            if (hTempNAcctJrnlBal >= hTemp2EndBal + hTempPAcctJrnlBal) {
                hTempAcctJrnlBal = hTempNAcctJrnlBal - hTempPAcctJrnlBal;
                hStatSubTransType = "0";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
                if (hTempNMcode <= 12)
                    hStatSubTransType = "1";
                if ((hTempNMcode > 12) && (hTempNMcode <= 24))
                    hStatSubTransType = "2";
                if (hTempNMcode > 24)
                    hStatSubTransType = "3";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
            } else {
                hTempAcctJrnlBal = hTemp2EndBal;
                hStatSubTransType = "0";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
                if (hTempNMcode <= 12)
                    hStatSubTransType = "1";
                if ((hTempNMcode > 12) && (hTempNMcode <= 24))
                    hStatSubTransType = "2";
                if (hTempNMcode > 24)
                    hStatSubTransType = "3";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();

                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTempNAcctJrnlBal;
                if (hTempAcctJrnlBal > 0) {
                    hStatSubTransType = "0";
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2(); /* 減少 */
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4(); /* 還款 */
                    if (hTempNMcode <= 12)
                        hStatSubTransType = "1";
                    if ((hTempNMcode > 12) && (hTempNMcode <= 24))
                        hStatSubTransType = "2";
                    if (hTempNMcode > 24)
                        hStatSubTransType = "3";
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2();
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4();
                }
            }
        }

        if ((hTempPTransType.equals("3")) && (!hTempNTransType.equals("3"))) {
            hTempTransType = "3";
            selectColBadDetail(3);
            if (hTempNTransType.equals("4")) {
                hTempTransType = "4";
                selectColBadDetail1(3);
                hTempAcctJrnlBal = hTemp1EndBal - hTempPAcctJrnlBal;
                if (hTempAcctJrnlBal > 0) {
                    hStatSubTransType = "0";
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                    if (hTempPMcode <= 12)
                        hStatSubTransType = "1";
                    if ((hTempPMcode > 12) && (hTempPMcode <= 24))
                        hStatSubTransType = "2";
                    if (hTempPMcode > 24)
                        hStatSubTransType = "3";
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                }
            } else {
                if (hTemp2EndBal > 0) {
                    hTempAcctJrnlBal = hTemp2EndBal;
                    hStatSubTransType = "0";
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                    if (hTempPMcode <= 12)
                        hStatSubTransType = "1";
                    if ((hTempPMcode > 12) && (hTempPMcode <= 24))
                        hStatSubTransType = "2";
                    if (hTempPMcode > 24)
                        hStatSubTransType = "3";
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                }
            }

            if (hTempNTransType.equals("4")) {
                hTempAcctJrnlBal = hTemp1EndBal;
                hStatSubTransType = "0";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate2();
                if (hTempPMcode <= 12)
                    hStatSubTransType = "1";
                if ((hTempPMcode > 12) && (hTempPMcode <= 24))
                    hStatSubTransType = "2";
                if (hTempPMcode > 24)
                    hStatSubTransType = "3";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate2();
            } else {
                hTempAcctJrnlBal = hTemp2EndBal + hTempPAcctJrnlBal;
                hStatSubTransType = "0";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate2();
                if (hTempPMcode <= 12)
                    hStatSubTransType = "1";
                if ((hTempPMcode > 12) && (hTempPMcode <= 24))
                    hStatSubTransType = "2";
                if (hTempPMcode > 24)
                    hStatSubTransType = "3";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate2();
            }

            if (hTempNTransType.equals("1"))
                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal;
            if (hTempNTransType.equals("2"))
                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTempNAcctJrnlBal;
            if (hTempNTransType.equals("4"))
                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTemp1EndBal;
            if (hTempAcctJrnlBal > 0) {
                hStatSubTransType = "0";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate4();
                if (hTempPMcode <= 12)
                    hStatSubTransType = "1";
                if ((hTempPMcode > 12) && (hTempPMcode <= 24))
                    hStatSubTransType = "2";
                if (hTempPMcode > 24)
                    hStatSubTransType = "3";
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate4();
            }
        }
    }

    /***********************************************************************/
    void procTransType41() throws Exception {
        hTempPAcctJrnlBal = hTempP1AcctJrnlBal;
        hTempNAcctJrnlBal = hTempN1AcctJrnlBal;

        hStatTransType = "4";
        hStatSubTransType = "0";

        if (((hTempPTransType.toCharArray()[0] < '4')
                || ((hTempPTransType.equals("4")) && (hTempPRecourseMark.equals("Y"))))
                && (hTempNTransType.equals("4")) && (hTempNRecourseMark.equals("N"))) {
            hTempTransType = "4";
            selectColBadDetail(4);

            hTempAcctJrnlBal = hTemp2EndBal;

            if (hTempPTransType.equals("4")) {
                if (hTempNAcctJrnlBal >= hTemp2EndBal + hTempPAcctJrnlBal) {
                    hTempAcctJrnlBal = hTempNAcctJrnlBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                } else {
                    hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();

                    hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTempNAcctJrnlBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2();
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4();
                }
            } else {
                if (hTempNAcctJrnlBal >= hTemp2EndBal) { /* 呆帳新增數 */
                    hTempAcctJrnlBal = hTempNAcctJrnlBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                } else {
                    hTempAcctJrnlBal = hTemp2EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();

                    hTempAcctJrnlBal = hTemp2EndBal - hTempNAcctJrnlBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2(); /* 減少 */
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4(); /* 還款 */
                }
            }
        }

        if ((hTempPTransType.equals("4")) && (hTempPRecourseMark.equals("N")) && (hTempNTransType.equals("4"))
                && (hTempNRecourseMark.equals("N"))) {
            hTempTransType = "4";
            selectColBadDetail(4);
            if (hTempNAcctJrnlBal >= hTemp2EndBal + hTempPAcctJrnlBal) {
                hTempAcctJrnlBal = hTempNAcctJrnlBal - hTempPAcctJrnlBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
            } else {
                hTempAcctJrnlBal = hTemp2EndBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();

                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTempNAcctJrnlBal;
                if (hTempAcctJrnlBal > 0) {
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2(); /* 減少 */
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4(); /* 還款 */
                }
            }
        }

        if (((hTempNTransType.toCharArray()[0] < '4')
                || ((hTempNTransType.equals("4")) && (!hTempNRecourseMark.equals("N"))))
                && (hTempPTransType.equals("4")) && (hTempPRecourseMark.equals("N"))) {
            if (hTempNTransType.equals("4")) {
                hTempAcctJrnlBal = hTempPAcctJrnlBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate2();
            }
            if (hTempNTransType.equals("3")) {
                hTempTransType = "4";
                selectColBadDetail(4);
                hTempTransType = "3";
                selectColBadDetail1(4);
                if (hTemp1EndBal > hTempPAcctJrnlBal + hTemp2EndBal) {
                    hTempAcctJrnlBal = hTemp1EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2();
                    hTempAcctJrnlBal = hTemp1EndBal - hTempPAcctJrnlBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                } else {
                    hTempAcctJrnlBal = hTemp2EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                    hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2();
                    hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTemp1EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4();
                }
            }

            if (hTempNTransType.toCharArray()[0] <= '2') {
                hTempTransType = "4";
                selectColBadDetail(4);
                hTempAcctJrnlBal = hTemp2EndBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate2();
                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTempNAcctJrnlBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate4();
            }
        }
    }

    /***********************************************************************/
    void procTransType42() throws Exception {
        hTempPAcctJrnlBal = hTempP1AcctJrnlBal;
        hTempNAcctJrnlBal = hTempN1AcctJrnlBal;

        hStatTransType = "4";
        hStatSubTransType = "1";

        if ((!hTempPRecourseMark.equals("Y")) && (hTempNTransType.equals("4"))
                && (hTempNRecourseMark.equals("Y"))) {
            hTempTransType = "4";
            selectColBadDetail(4);

            hTempAcctJrnlBal = hTemp2EndBal;

            if (hTempPTransType.equals("4")) {
                if (hTempNAcctJrnlBal >= hTemp2EndBal + hTempPAcctJrnlBal) {
                    hTempAcctJrnlBal = hTempNAcctJrnlBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                } else {
                    hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();

                    hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTempNAcctJrnlBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2();
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4();
                }
            } else {
                if (hTempNAcctJrnlBal >= hTemp2EndBal) { /* 呆帳新增數 */
                    hTempAcctJrnlBal = hTempNAcctJrnlBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                } else {
                    hTempAcctJrnlBal = hTemp2EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();

                    hTempAcctJrnlBal = hTemp2EndBal - hTempNAcctJrnlBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2(); /* 減少 */
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4(); /* 還款 */
                }
            }
        }

        if ((hTempPTransType.equals("4")) && (hTempPRecourseMark.equals("Y")) && (hTempNTransType.equals("4"))
                && (hTempNRecourseMark.equals("Y"))) {
            hTempTransType = "4";
            selectColBadDetail(4);
            if (hTempNAcctJrnlBal >= hTemp2EndBal + hTempPAcctJrnlBal) {
                hTempAcctJrnlBal = hTempNAcctJrnlBal - hTempPAcctJrnlBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
            } else {
                hTempAcctJrnlBal = hTemp2EndBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();

                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTempNAcctJrnlBal;
                if (hTempAcctJrnlBal > 0) {
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2(); /* 減少 */
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4(); /* 還款 */
                }
            }
        }

        if ((hTempPTransType.equals("4")) && (hTempPRecourseMark.equals("Y"))
                && (!hTempNRecourseMark.equals("Y"))) {
            if (hTempNTransType.equals("4")) {
                hTempAcctJrnlBal = hTempPAcctJrnlBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate2();
            }
            if (hTempNTransType.equals("3")) {
                hTempTransType = "4";
                selectColBadDetail(4);
                hTempTransType = "3";
                selectColBadDetail1(4);
                if (hTemp1EndBal > hTempPAcctJrnlBal + hTemp2EndBal) {
                    hTempAcctJrnlBal = hTemp1EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2();
                    hTempAcctJrnlBal = hTemp1EndBal - hTempPAcctJrnlBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                } else {
                    hTempAcctJrnlBal = hTemp2EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                    hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2();
                    hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTemp1EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4();
                }
            }

            if (hTempNTransType.toCharArray()[0] <= '2') {
                hTempTransType = "4";
                selectColBadDetail(4);
                hTempAcctJrnlBal = hTemp2EndBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate2();
                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTempNAcctJrnlBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate4();
            }
        }
    }

    /***********************************************************************/
    void procTransType43() throws Exception {
        hTempPAcctJrnlBal = hTempP1AcctJrnlBal;
        hTempNAcctJrnlBal = hTempN1AcctJrnlBal;

        hStatTransType = "5";
        hStatSubTransType = "0";

        if ((hTempPTransType.equals("4")) && (hTempNTransType.equals("4"))) {
            hTempTransType = "4";
            selectColBadDetail(4);

            hTempAcctJrnlBal = hTemp2EndBal;

            if (hTempNAcctJrnlBal >= hTemp2EndBal) /* 呆帳新增數 */
            {
                hTempAcctJrnlBal = hTempNAcctJrnlBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
            } else {
                hTempAcctJrnlBal = hTemp2EndBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();

                hTempAcctJrnlBal = hTemp2EndBal - hTempNAcctJrnlBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate2(); /* 減少 */
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate4(); /* 還款 */
            }
        }

        if ((hTempPTransType.equals("4")) && (hTempNTransType.equals("4"))) {
            hTempTransType = "4";
            selectColBadDetail(4);
            if (hTempNAcctJrnlBal >= hTemp2EndBal + hTempPAcctJrnlBal) {
                hTempAcctJrnlBal = hTempNAcctJrnlBal - hTempPAcctJrnlBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
            } else {
                hTempAcctJrnlBal = hTemp2EndBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();

                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTempNAcctJrnlBal;
                if (hTempAcctJrnlBal > 0) {
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2(); /* 減少 */
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4(); /* 還款 */
                }
            }
        }

        if ((hTempNTransType.equals("4")) && (hTempPTransType.equals("4"))) {
            if (hTempNTransType.equals("3")) {
                hTempTransType = "4";
                selectColBadDetail(4);
                hTempTransType = "3";
                selectColBadDetail1(4);
                if (hTemp1EndBal > hTempPAcctJrnlBal + hTemp2EndBal) {
                    hTempAcctJrnlBal = hTemp1EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2();
                    hTempAcctJrnlBal = hTemp1EndBal - hTempPAcctJrnlBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                } else {
                    hTempAcctJrnlBal = hTemp2EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate3();
                    hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate2();
                    hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTemp1EndBal;
                    if (hTempAcctJrnlBal > 0)
                        updateColStaticrate4();
                }
            }

            if (hTempNTransType.toCharArray()[0] <= '2') {
                hTempTransType = "4";
                selectColBadDetail(4);
                hTempAcctJrnlBal = hTemp2EndBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate3();
                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate2();
                hTempAcctJrnlBal = hTempPAcctJrnlBal + hTemp2EndBal - hTempNAcctJrnlBal;
                if (hTempAcctJrnlBal > 0)
                    updateColStaticrate4();
            }
        }
    }

    /***********************************************************************/
    void selectColBadDetail1(int balType) throws Exception {
        hCbdlEndBal = 0;

        sqlCmd = "select sum(end_bal) h_cbdl_end_bal ";
        sqlCmd += " from col_bad_detail  ";
        sqlCmd += "where trans_type = ?  ";
        sqlCmd += "and trans_date = (select max(trans_date) from col_bad_debt where trans_type = ?  ";
        sqlCmd += " and p_seqno = ?  ";
        sqlCmd += " and trans_date > ?  ";
        sqlCmd += " and trans_date <= ?)  ";
        sqlCmd += "and p_seqno = ? ";
        setString(1, hTempTransType);
        setString(2, hTempPSeqno);
        setString(3, hTempPProcDate);
        setString(4, hTempNProcDate);
        setString(5, hTempNProcDate);
        setString(6, hTempPSeqno);
        
        extendField = "col_bad_detail_1.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_bad_detail not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCbdlEndBal = getValueLong("col_bad_detail_1.h_cbdl_end_bal");
        }

        if (balType != 4) {
            hTemp1EndBal = hCbdlEndBal; /* 仟元(*0.001 +0.5)-->元 */
        } else {
            hTemp1EndBal = hCbdlEndBal;
        }
    }

    /***********************************************************************/
    void selectColMonthData2() throws Exception {
        double tempAmt;

        sqlCmd = "select ";
        sqlCmd += "p_seqno,";
        sqlCmd += "trans_type,";
        sqlCmd += "max(trans_date) h_cbdl_trans_date ";
        sqlCmd += "from col_bad_detail a ";
        sqlCmd += "where trans_date > (select proc_date from col_month_data ";
        sqlCmd += "where proc_month = ? ";
        sqlCmd += "and trans_type = a.trans_type ";
        sqlCmd += "fetch first 1 row only ) ";
        sqlCmd += "and trans_date <=(select proc_date from col_month_data ";
        sqlCmd += "where proc_month = ? ";
        sqlCmd += "and trans_type = a.trans_type ";
        sqlCmd += "fetch first 1 row only ) ";
        sqlCmd += "and 0  = (select count(*) from col_month_data ";
        sqlCmd += "where (proc_month = ? ";
        sqlCmd += "or proc_month = ?) ";
        sqlCmd += "and trans_type = a.trans_type ";
        sqlCmd += "and p_seqno =a.p_seqno) ";
        sqlCmd += "group by p_seqno,trans_type ";
        setString(1, hMndaProcMonth);
        setString(2, hStatProcMonth);
        setString(3, hMndaProcMonth);
        setString(4, hStatProcMonth);

        openCursor();
        while (fetchTable()) {
            hTempPSeqno = getValue("p_seqno");
            hStatTransType = getValue("trans_type");
            hTempTransType = getValue("trans_type");
            hCbdlTransDate = getValue("h_cbdl_trans_date");

            selectColBadDetail(comcr.str2int(hStatTransType));

            hTempNDelAmt = 0;  //add by phopho  //D檔金額歸0
            hTempAcctJrnlBal = hTemp2EndBal;
            if (hTempAcctJrnlBal > 0) {
                if (hStatTransType.equals("3")) {
                    hTempTransType = "4";
                    selectColBadDetail(3);
                    tempAmt = hTemp2EndBal;
                    hTemp2EndBal = hTempAcctJrnlBal;
                    hTempAcctJrnlBal = hTemp2EndBal;
                    hStatSubTransType = "0";
                    updateColStaticrate2();
                    updateColStaticrate3();
                    hStatSubTransType = "1";
                    updateColStaticrate2();
                    updateColStaticrate3();
                    hTempAcctJrnlBal = hTemp2EndBal - tempAmt;

                    if (hTempAcctJrnlBal > 0) {
                        hStatSubTransType = "0";
                        updateColStaticrate4();
                        hStatSubTransType = "1";
                        updateColStaticrate4();
                    }
                } else {
                    selectColBadDebt1();
                    updateColStaticrate2();
                    updateColStaticrate3();
                    updateColStaticrate4();
                    hStatTransType = "5";
                    hStatSubTransType = "0";
                    updateColStaticrate2();
                    updateColStaticrate3();
                    updateColStaticrate4();
                }
            }
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectColBadDetail(int balType) throws Exception {
        hCbdlEndBal = 0;

        sqlCmd = "select sum(end_bal) h_cbdl_end_bal ";
        sqlCmd += " from col_bad_detail  ";
        sqlCmd += "where trans_type = ?  ";
        sqlCmd += "and trans_date > ?  ";
        sqlCmd += "and trans_date <= ?  ";
        sqlCmd += "and p_seqno = ? ";
        setString(1, hTempTransType);
        setString(2, hTempPProcDate);
        setString(3, hTempNProcDate);
        setString(4, hTempPSeqno);
        
        extendField = "col_bad_detail.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_bad_detail not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCbdlEndBal = getValueLong("col_bad_detail.h_cbdl_end_bal");
        }

        if (balType != 4) {
            hTemp2EndBal = hCbdlEndBal; /* 仟元(*0.001 +0.5)-->元 */
        } else {
            hTemp2EndBal = hCbdlEndBal;
        }
    }

    /***********************************************************************/
    void selectColBadDebt1() throws Exception {
        hStatSubTransType = "";

        sqlCmd = "select decode(recourse_mark,'Y','1','0') h_stat_sub_trans_type ";
        sqlCmd += " from col_bad_debt  ";
        sqlCmd += "where trans_type = '4'  ";
        sqlCmd += "and trans_date = (select max(trans_date) from col_bad_debt where trans_date <=?  ";
        sqlCmd += "    and p_seqno = ?  ";
        sqlCmd += "    and trans_type = '4')  ";
        sqlCmd += "and p_seqno = ? ";
        setString(1, hCbdlTransDate);
        setString(2, hTempPSeqno);
        setString(3, hTempPSeqno);
        
        extendField = "col_bad_debt_1.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hStatSubTransType = getValue("col_bad_debt_1.h_stat_sub_trans_type");
        }
    }

    /***********************************************************************/
    void updateColStaticrate2() throws Exception { /* 減少 */
        hTempRoundAmt = hTempAcctJrnlBal;
        daoTable = "col_staticrate";
        updateSQL = "sub_acct_cnt = sub_acct_cnt + 1,";
        updateSQL += " sub_acct_amt = sub_acct_amt + ?";
        whereStr = "where proc_month = ?  ";
        whereStr += "and trans_type = ?  ";
        whereStr += "and sub_trans_type = ? ";
        setDouble(1, hTempRoundAmt);
        setString(2, hStatProcMonth);
        setString(3, hStatTransType);
        setString(4, hStatSubTransType);
        updateTable();
        if (notFound.equals("Y")) {
        	daoTable = "col_staticrate";
        	extendField = daoTable + ".";
            setValue(extendField+"proc_month", hStatProcMonth);
            setValue(extendField+"trans_type", hStatTransType);
            setValue(extendField+"sub_trans_type", hStatSubTransType);
            setValueInt(extendField+"sub_acct_cnt", 1);
            setValueDouble(extendField+"sub_acct_amt", hTempRoundAmt);
            setValue(extendField+"mod_user", hStatModUser);
            setValue(extendField+"mod_time", sysDate + sysTime);
            setValue(extendField+"mod_pgm", hStatModPgm);
            
            insertTable();
            if (dupRecord.equals("Y")) {
                comcr.errRtn("insert_col_staticrate duplicate!", "", hCallBatchSeqno);
            }
        }
        return;
    }

    /***********************************************************************/
    void updateColStaticrate3() throws Exception { /* 新增 */
        hTempRoundAmt = hTempAcctJrnlBal;
        daoTable = "col_staticrate";
        updateSQL = "add_acct_cnt = add_acct_cnt + 1,";
        updateSQL += " add_acct_amt = add_acct_amt + ?";
        whereStr = "where proc_month = ?  ";
        whereStr += "and trans_type = ?  ";
        whereStr += "and sub_trans_type = ? ";
        setDouble(1, hTempRoundAmt);
        setString(2, hStatProcMonth);
        setString(3, hStatTransType);
        setString(4, hStatSubTransType);
        updateTable();
        if (notFound.equals("Y")) {
        	daoTable = "col_staticrate";
        	extendField = daoTable + ".";
            setValue(extendField+"proc_month", hStatProcMonth);
            setValue(extendField+"trans_type", hStatTransType);
            setValue(extendField+"sub_trans_type", hStatSubTransType);
            setValueInt(extendField+"add_acct_cnt", 1);
            setValueDouble(extendField+"add_acct_amt", hTempRoundAmt);
            setValue(extendField+"mod_user", hStatModUser);
            setValue(extendField+"mod_time", sysDate + sysTime);
            setValue(extendField+"mod_pgm", hStatModPgm);
            
            insertTable();
            if (dupRecord.equals("Y")) {
                comcr.errRtn("insert_col_staticrate duplicate!", "", hCallBatchSeqno);
            }
        }
        return;
    }

    /***********************************************************************/
    void updateColStaticrate4() throws Exception { /* 還款 */
        hTempRoundAmt = hTempAcctJrnlBal;
        hTempDelAmt = hTempNDelAmt;
        
        daoTable = "col_staticrate";
        updateSQL = "sub_jrnl_bal = sub_jrnl_bal + ?,";
        updateSQL += "report_sub_amt = report_sub_amt + ?,";
        updateSQL += "del_amt = del_amt + ?,";
        updateSQL += "sub_jrnl_bal_actual = sub_jrnl_bal_actual + ? ";
        whereStr = "where proc_month = ? ";
        whereStr += "and sub_trans_type = ? ";
        whereStr += "and trans_type = ? ";
        setDouble(1, hTempRoundAmt);
        setDouble(2, hTempRoundAmt);
        setDouble(3, hTempDelAmt);
        setDouble(4, hTempRoundAmt - hTempDelAmt);
        setString(5, hStatProcMonth);
        setString(6, hStatSubTransType);
        setString(7, hStatTransType);
        updateTable();
        if (notFound.equals("Y")) {
        	daoTable = "col_staticrate";
        	extendField = daoTable + ".";
            setValue(extendField+"proc_month", hStatProcMonth);
            setValue(extendField+"trans_type", hStatTransType);
            setValue(extendField+"sub_trans_type", hStatSubTransType);
            setValueDouble(extendField+"sub_jrnl_bal", hTempRoundAmt);
            setValueDouble(extendField+"report_sub_amt", hTempRoundAmt);
            setValueDouble(extendField+"del_amt", hTempDelAmt);
            setValueDouble(extendField+"sub_jrnl_bal_actual", hTempRoundAmt - hTempDelAmt);
            setValue(extendField+"mod_user", hStatModUser);
            setValue(extendField+"mod_time", sysDate + sysTime);
            setValue(extendField+"mod_pgm", hStatModPgm);
            
            insertTable();
            if (dupRecord.equals("Y")) {
                comcr.errRtn("insert_col_staticrate duplicate!", "", hCallBatchSeqno);
            }
        }
    }
    
    /***********************************************************************/
    void selectColMonthData7() throws Exception {
    //  b.	加總有被處理的col_month_data.recv_ai_amt。寫入col_staticrate.sub_jrnl_bal (現金還款)。
    	hStatRecvAiAmt = 0;
    	
    	sqlCmd = "select nvl(sum(recv_ai_amt),0) as recv_ai_amt ";
        sqlCmd += "from col_month_data ";
        sqlCmd += "where proc_month = ? ";
        sqlCmd += "and trans_type > '1' ";
        setString(1, hStatProcMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	hStatRecvAiAmt = getValueLong("recv_ai_amt");
        }
    }
    
    /***********************************************************************/
    void insertColStaticrate7() throws Exception { /* 新增 帳外息(AI)還款總金額 */
//      col_staticrate.trans_type 設定為7
//      col_staticrate.sub_trans_type設定為0
    	daoTable = "col_staticrate";
    	extendField = daoTable + ".";
        setValue(extendField+"proc_month", hStatProcMonth);
        setValue(extendField+"trans_type", "7");
        setValue(extendField+"sub_trans_type", "0");
        setValueLong(extendField+"sub_jrnl_bal", hStatRecvAiAmt);
        setValue(extendField+"mod_user", hStatModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", hStatModPgm);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_staticrate_7 duplicate!", "", hCallBatchSeqno);
        }
        
        
        sqlCmd += "from act_jrnl ";
        sqlCmd += "where substr(crt_date,1,6) = ? ";
        sqlCmd += "and dr_cr = 'D' and tran_class = 'A' and acct_code = 'DB' ";

        setString(1, hStatProcMonth);
        
        extendField = "act_jrnl_1.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hJrnlDelAmt = getValueDouble("act_jrnl_1.del_amt");
            hJrnlDelCnt = getValueInt("act_jrnl_1.del_cnt");
        }
    }

    /***********************************************************************/
    void selectActJrnl() throws Exception {     /* D檔金額 */
        hJrnlDelAmt = 0;
        sqlCmd = "select nvl(sum(transaction_amt),0) as del_amt, count(distinct p_seqno) as del_cnt ";
        sqlCmd += "from act_jrnl ";
        sqlCmd += "where substr(crt_date,1,6) = ? ";
        sqlCmd += "and dr_cr = 'D' and tran_class = 'A' and acct_code = 'DB' ";

        setString(1, hStatProcMonth);
        
        extendField = "act_jrnl_1.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	hJrnlDelAmt = getValueDouble("act_jrnl_1.del_amt");
        	hJrnlDelCnt = getValueInt("act_jrnl_1.del_cnt");
        }
    }
    /***********************************************************************/
    void updateColStaticrate41() throws Exception{
        daoTable = "col_staticrate";
        updateSQL += "del_amt = ?,";
        updateSQL += "sub_jrnl_bal_actual = sub_jrnl_bal - ? ";
//        updateSQL += ",del_cnt = ? ";
        whereStr = "where proc_month = ? ";
        whereStr += "and trans_type = '4' ";
        whereStr += "and sub_trans_type = '1' ";
        setDouble(1, hJrnlDelAmt);
        setDouble(2, hJrnlDelAmt);
        setString(3, hStatProcMonth);
//        setDouble(3, hJrnlDelCnt);
//        setString(4, hStatProcMonth);
        updateTable();
        if (notFound.equals("Y")) {
//          comcr.err_rtn("update_col_staticrate_41 not found!", "", h_call_batch_seqno);
        }
    }
    
    /***********************************************************************/
    void updateColStaticrate50() throws Exception{
        daoTable = "col_staticrate";
        updateSQL  = "del_amt = ?,";
        updateSQL += "sub_jrnl_bal_actual = sub_jrnl_bal - ? ";
//        updateSQL += ",del_cnt = ? ";
        whereStr = "where proc_month = ? ";
        whereStr += "and trans_type = '5' ";
        whereStr += "and sub_trans_type = '0' ";
        setDouble(1, hJrnlDelAmt);
        setDouble(2, hJrnlDelAmt);
        setString(3, hStatProcMonth);
//        setDouble(3, hJrnlDelCnt);
//        setString(4, hStatProcMonth);

        updateTable();
        if (notFound.equals("Y")) {
//          comcr.err_rtn("update_col_staticrate_50 not found!", "", h_call_batch_seqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB014 proc = new ColB014();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
