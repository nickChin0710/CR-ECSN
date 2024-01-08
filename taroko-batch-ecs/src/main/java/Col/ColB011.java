/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/02/05  V1.00.00    phopho     program initial                          *
*  109/03/17  V1.00.01    phopho     fix bug: if (m_code > 99) m_code = 99;   *
*  109/12/12  V1.00.02    shiyuqi       updated for project coding standard   *
*  112/08/05  V1.00.03    sunny      執行錯誤訊息加強顯示                                                    *
******************************************************************************/

package Col;

import com.AccessDAO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;

public class ColB011 extends AccessDAO {
    private String progname = "每月明細報表資料處理程式    112/08/05  V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
//    long h_draw_mcode_x = 0;
//    long h_draw_mcode_y = 0;
//    long h_draw_mcode_1 = 0;
//    long h_draw_mcode_2 = 0;
//    long h_draw_mcode_3 = 0;
//    long h_draw_mcode_4 = 0;
//    double h_draw_mcode_x_rate = 0;
//    double h_draw_mcode_y_rate = 0;
//    double h_draw_mcode_1_rate = 0;
//    double h_draw_mcode_2_rate = 0;
//    double h_draw_mcode_3_rate = 0;
//    double h_draw_mcode_4_rate = 0;
    //mod by phopho 改欄位名稱 2019.2.22
    long hDrawMcode1 = 0;
    long hDrawMcode2 = 0;
    long hDrawMcode3 = 0;
    long hDrawMcode4 = 0;
    long hDrawMcode5 = 0;
    long hDrawMcode6 = 0;
    double hDrawMcode1Rate = 0;
    double hDrawMcode2Rate = 0;
    double hDrawMcode3Rate = 0;
    double hDrawMcode4Rate = 0;
    double hDrawMcode5Rate = 0;
    double hDrawMcode6Rate = 0;
    double hDrawNormalRate = 0;
    double hDrawDelinquentRate = 0;
    double hDrawCollectionRate = 0;
    double hDrawStageRate = 0;
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctHolderId = "";
    String hAcnoAcctHolderIdCode = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCorpNo = "";
    String hAcnoCorpNoCode = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoAcctStatus = "";
    String hAcnoRecourseMark = "";
    String hAcnoCreditActNo = "";
    String hAcnoCorpActFlag = "";
    String hAcnoOrgDelinquentDate = "";
    String hAcnoLawsuitProcessLog = "";
    double hAcnoLineOfCreditAmt = 0;
    String hAcnoPayByStageFlag = "";
    String hWdayThisAcctMonth = "";
    String hWdayNextAcctMonth = "";
    String hMndaProcMonth = "";
    String hMndaTransDate = "";
    long hAcctAcctJrnlBal = 0;
    double hCrdrBilled1Amt = 0;
    double hCrdrUnbill1Amt = 0;
    double hCrdrBilled2Amt = 0;
    double hCrdrUnbill2Amt = 0;
    double hCrdrBilled3Amt = 0;
    double hCrdrUnbill3Amt = 0;
    int hCrdrMcode = 0;
    double hCrdrMcodeRate = 0;
    String hTempProcMonth = "";
    String hMndaPSeqno = "";
    String hAcctPSeqno = "";
    String hCbdtTransDate = "";
    String hCrdrTransType = "";
    double hCrdrTypeCount = 0;
    double hCrdrDrawAmt = 0;
    double hJrnlDelAmt = 0;
    double hJrnlRecvAiAmt = 0;

    String tempMonth = "";
    String temstr = "";
    long readCnt = 0;
    int inta1 = 0;
    int inta2 = 0;
    int inta3 = 0;
    double[][][] tColArr = new double[10][200][20];

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

                showLogMessage("I", "", String.format("Usage : ColB011 [year_month [business_date]]"));
                showLogMessage("I", "", String.format("               1.year_month    : 處理年月(yyyymm)"));
                showLogMessage("I", "", String.format("               2.business_date : 營業日(yyyymmdd)"));
                comc.errExit("", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();
            if (args.length == 0) {
                tempMonth = comc.getSubString(hBusiBusinessDate,0,6);
//                temstr = comcr.get_businday(temp_month, -2);
                temstr = comcr.getBusinday(hBusiBusinessDate, -2);
                if (!comc.getSubString(hBusiBusinessDate,0,8).equals(temstr)) {
                	exceptExit = 0;
                    comcr.errRtn("本報表只能在每月最後營業日前一日執行", String.format("最後營業日前一日[%s]", temstr), hCallBatchSeqno);
                }
                hMndaProcMonth = tempMonth;
            } else {
                if (args[0].length() != 6) {
                	exceptExit = 0;
                    comcr.errRtn(String.format("參數錯誤[%s]，請輸入執行月份(YYYYMM)", args[0]), "", hCallBatchSeqno);
                }
                hMndaProcMonth = args[0];
            }

            if (args.length == 2)
                hBusiBusinessDate = args[1];

            showLogMessage("I", "", String.format("營業日 [%s]", hBusiBusinessDate));

            deleteColMonthData();

            for (inta1 = 0; inta1 < 10; inta1++)
                for (inta2 = 0; inta2 < 200; inta2++)
                    for (inta3 = 0; inta3 < 20; inta3++)
                        tColArr[inta1][inta2][inta3] = 0;

            selectColDrawrate();
            // 因為提存方式改變，col_r_drawrate、col_m_drawrate 兩個TABLE，不用處理。
            // delete_col_r_drawrate();
            // delete_col_m_drawrate();
            selectActAcno();
            showLogMessage("I", "", String.format("Total process record[%d]", readCnt));

            for (inta1 = 0; inta1 < 5; inta1++) {
                temstr = String.format("%1d", inta1);
                hCrdrTransType = temstr;
                for (inta2 = 0; inta2 < 200; inta2++) {
                    if (tColArr[inta1][inta2][7] <= 0)
                        continue;
                    hCrdrMcode = inta2;
                    hCrdrBilled1Amt = tColArr[inta1][inta2][1];
                    hCrdrUnbill1Amt = tColArr[inta1][inta2][2];
                    hCrdrBilled2Amt = tColArr[inta1][inta2][3];
                    hCrdrUnbill2Amt = tColArr[inta1][inta2][4];
                    hCrdrBilled3Amt = tColArr[inta1][inta2][5];
                    hCrdrUnbill3Amt = tColArr[inta1][inta2][6];
                    hCrdrDrawAmt = tColArr[inta1][inta2][7];
                    hCrdrTypeCount = tColArr[inta1][inta2][8];
                    hCrdrMcodeRate = tColArr[inta1][inta2][9];

                    // 因為提存方式改變，col_r_drawrate、col_m_drawrate 兩個TABLE，不用處理。
                    // insert_col_r_drawrate();
                    // insert_col_m_drawrate();
                }
            }

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
    void deleteColMonthData() throws Exception {
        daoTable = "col_month_data";
        whereStr = "where proc_month = ? ";
        setString(1, hMndaProcMonth);
        deleteTable();
    }

    /***********************************************************************/
    void selectColDrawrate() throws Exception {
        hDrawMcode1 = 0;
        hDrawMcode2 = 0;
        hDrawMcode3 = 0;
        hDrawMcode4 = 0;
        hDrawMcode5 = 0;
        hDrawMcode6 = 0;
        hDrawMcode1Rate = 0;
        hDrawMcode2Rate = 0;
        hDrawMcode3Rate = 0;
        hDrawMcode4Rate = 0;
        hDrawMcode5Rate = 0;
        hDrawMcode6Rate = 0;
        hDrawNormalRate = 0;
        hDrawDelinquentRate = 0;
        hDrawCollectionRate = 0;
        hDrawStageRate = 0;

        sqlCmd = "select ";
        sqlCmd += "mcode_1,";
        sqlCmd += "mcode_2,";
        sqlCmd += "mcode_3,";
        sqlCmd += "mcode_4,";
        sqlCmd += "mcode_5,";
        sqlCmd += "999 mcode_6,";
        sqlCmd += "mcode_1_rate,";
        sqlCmd += "mcode_2_rate,";
        sqlCmd += "mcode_3_rate,";
        sqlCmd += "mcode_4_rate,";
        sqlCmd += "mcode_5_rate,";
        sqlCmd += "mcode_6_rate,";
        sqlCmd += "normal_rate,";
        sqlCmd += "delinquent_rate,";
        sqlCmd += "collection_rate,";
        sqlCmd += "stage_rate ";
        sqlCmd += " from col_drawrate ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_drawrate not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hDrawMcode1 = getValueLong("mcode_1");
            hDrawMcode2 = getValueLong("mcode_2");
            hDrawMcode3 = getValueLong("mcode_3");
            hDrawMcode4 = getValueLong("mcode_4");
            hDrawMcode5 = getValueLong("mcode_5");
            hDrawMcode6 = getValueLong("mcode_6");
            hDrawMcode1Rate = getValueDouble("mcode_1_rate");
            hDrawMcode2Rate = getValueDouble("mcode_2_rate");
            hDrawMcode3Rate = getValueDouble("mcode_3_rate");
            hDrawMcode4Rate = getValueDouble("mcode_4_rate");
            hDrawMcode5Rate = getValueDouble("mcode_5_rate");
            hDrawMcode6Rate = getValueDouble("mcode_6_rate");
            hDrawNormalRate = getValueDouble("normal_rate");
            hDrawDelinquentRate = getValueDouble("delinquent_rate");
            hDrawCollectionRate = getValueDouble("collection_rate");
            hDrawStageRate = getValueDouble("stage_rate");
        }
    }

    /***********************************************************************/
    void deleteColRDrawrate() throws Exception {
        daoTable = "col_r_drawrate";
        whereStr = "where proc_date = to_char(to_date(?,'yyyymmdd')+1 days,'yyyymmdd') ";
        setString(1, hBusiBusinessDate);
        deleteTable();
    }

    /***********************************************************************/
    void deleteColMDrawrate() throws Exception {
        daoTable = "col_drawrate";
        whereStr = "where proc_month = ? ";
        setString(1, hMndaProcMonth);
        deleteTable();
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        int mCode = 0;

        sqlCmd = "select ";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "c.id_no acct_holder_id,"; //a.acct_holder_id
        sqlCmd += "c.id_no_code acct_holder_id_code,"; //a.acct_holder_id_code
//        sqlCmd += " case when acno_flag in ('1','3') then substr(acct_key,1,10) ";
//        sqlCmd += "      when acno_flag = '2' then '' ";
//        sqlCmd += "      when acno_flag = 'Y' then (select id_no from crd_idno where id_p_seqno = a.id_p_seqno) else '' end as acct_holder_id, ";
//        sqlCmd += " case when acno_flag in ('1','3') then substr(acct_key,11,1)  ";
//        sqlCmd += "      when acno_flag = '2' then '' ";
//        sqlCmd += "      when acno_flag = 'Y' then (select id_no_code from crd_idno where id_p_seqno = a.id_p_seqno) else '' end as acct_holder_id_code, ";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "d.corp_no,";
//        sqlCmd += "d.corp_no_code,";
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "a.acct_status,";
        sqlCmd += "a.recourse_mark,";
        sqlCmd += "a.credit_act_no,";
        sqlCmd += "a.corp_act_flag,";
        sqlCmd += "a.org_delinquent_date,";
        sqlCmd += "a.lawsuit_process_log,";
        sqlCmd += "a.line_of_credit_amt,";
        sqlCmd += "decode(a.acct_status,'4','',a.pay_by_stage_flag) h_acno_pay_by_stage_flag,";
        sqlCmd += "b.this_acct_month,";
        sqlCmd += "b.next_acct_month,";
        sqlCmd += "a.int_rate_mcode ";
        sqlCmd += "from act_acno a,ptr_workday b, crd_idno c ";
//       sqlCmd += "from act_acno a,ptr_workday b ";
        sqlCmd += "  left join crd_corp d on d.corp_p_seqno = a.corp_p_seqno "; //find corp_no in crd_corp
        sqlCmd += "Where acno_flag <> 'Y' ";
        sqlCmd += "and a.id_p_seqno = c.id_p_seqno ";
        sqlCmd += "and (decode(a.acct_status,'','x',a.acct_status) in ('2','3','4') ";
        sqlCmd += "or a.pay_by_stage_flag <> '' ";
        sqlCmd += "or (decode(a.acct_status,'','x',a.acct_status)='1' ";
        sqlCmd += "and decode(decode(a.payment_rate1,'','00',a.payment_rate1),'0A','00','0B','00','0C','00', ";
        sqlCmd += "'0D','00','0E','00',a.payment_rate1) >= '01')) ";
        sqlCmd += "and a.stmt_cycle = b.stmt_cycle ";

        openCursor();
        while (fetchTable()) {
//            h_acno_p_seqno = getValue("p_seqno");
            hAcnoPSeqno = getValue("acno_p_seqno");
            hAcctPSeqno = getValue("p_seqno");
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctKey = getValue("acct_key");
            hAcnoAcctHolderId = getValue("acct_holder_id");
            hAcnoAcctHolderIdCode = getValue("acct_holder_id_code");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoCorpNo = getValue("corp_no");
//            h_acno_corp_no_code = getValue("corp_no_code");
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoRecourseMark = getValue("recourse_mark");
            hAcnoCreditActNo = getValue("credit_act_no");
            hAcnoCorpActFlag = getValue("corp_act_flag");
            hAcnoOrgDelinquentDate = getValue("org_delinquent_date");
            hAcnoLawsuitProcessLog = getValue("lawsuit_process_log");
            hAcnoLineOfCreditAmt = getValueDouble("line_of_credit_amt");
            hAcnoPayByStageFlag = getValue("h_acno_pay_by_stage_flag");
            hWdayThisAcctMonth = getValue("this_acct_month");
            hWdayNextAcctMonth = getValue("next_acct_month");
            hCrdrBilled1Amt = 0;
            hCrdrBilled2Amt = 0;
            hCrdrBilled3Amt = 0;
            hCrdrUnbill1Amt = 0;
            hCrdrUnbill2Amt = 0;
            hCrdrUnbill3Amt = 0;

//            m_code = comr.getMcode(h_acno_acct_type, h_acno_p_seqno);
            mCode = getValueInt("int_rate_mcode");

            if ((mCode <= 0) && (hAcnoAcctStatus.equals("1")))
                continue;

            selectActAcct();
            
            //新增insert_col_month_data，寫入COL_MONTH_DATA. DEL_AMT、COL_MONTH_DATA. RECV_AI_AMT。  2019.2.20 add by phopho
            selectActJrnl1();  //D檔金額
            selectActJrnl2();  //科目AI回收金額

            readCnt++;
            if ((readCnt % 5000) == 0)
                showLogMessage("I", "", String.format("Process record[%d]", readCnt));

            if (mCode > 99) mCode = 99;  //phopho mod 2020.3.17
            hCrdrMcode = mCode;

//            if (h_draw_mcode_3 == 0) {
//                h_draw_mcode_3 = 99;
//                h_draw_mcode_4_rate = 100;
//            }
//            if (h_draw_mcode_2 == 0) {
//                h_draw_mcode_2 = 99;
//                h_draw_mcode_3_rate = 100;
//            }
//            if (h_draw_mcode_1 == 0) {
//                h_draw_mcode_1 = 99;
//                h_draw_mcode_2_rate = 100;
//            }
//
//            if (h_acno_pay_by_stage_flag.length() != 0) {
//                h_crdr_mcode_rate = h_draw_stage_rate;
//            } else if (m_code <= h_draw_mcode_x) {
//                h_crdr_mcode_rate = h_draw_mcode_x_rate;
//            } else if (m_code <= h_draw_mcode_y) {
//                h_crdr_mcode_rate = h_draw_mcode_y_rate;
//            } else if (m_code <= h_draw_mcode_1) {
//                h_crdr_mcode_rate = h_draw_mcode_1_rate;
//            } else if (m_code <= h_draw_mcode_2) {
//                h_crdr_mcode_rate = h_draw_mcode_2_rate;
//            } else if (m_code <= h_draw_mcode_3) {
//                h_crdr_mcode_rate = h_draw_mcode_3_rate;
//            } else {
//                h_crdr_mcode_rate = h_draw_mcode_4_rate;
//            }
            
            if (hDrawMcode5 == 0) {
                hDrawMcode5 = 99;
                hDrawMcode6Rate = 100;
            }
            if (hDrawMcode4 == 0) {
                hDrawMcode4 = 99;
                hDrawMcode5Rate = 100;
            }
            if (hDrawMcode3 == 0) {
                hDrawMcode3 = 99;
                hDrawMcode4Rate = 100;
            }
            
            if (hAcnoPayByStageFlag.length() != 0) {
                hCrdrMcodeRate = hDrawStageRate;
            } else if (mCode <= hDrawMcode1) {
                hCrdrMcodeRate = hDrawMcode1Rate;
            } else if (mCode <= hDrawMcode2) {
                hCrdrMcodeRate = hDrawMcode2Rate;
            } else if (mCode <= hDrawMcode3) {
                hCrdrMcodeRate = hDrawMcode3Rate;
            } else if (mCode <= hDrawMcode4) {
                hCrdrMcodeRate = hDrawMcode4Rate;
            } else if (mCode <= hDrawMcode5) {
                hCrdrMcodeRate = hDrawMcode5Rate;
            } else {
                hCrdrMcodeRate = hDrawMcode6Rate;
            }

            hCrdrBilled1Amt = hCrdrUnbill1Amt = hCrdrBilled2Amt = hCrdrUnbill2Amt = hCrdrBilled3Amt = hCrdrUnbill3Amt = 0;

            if (hAcnoPayByStageFlag.length() != 0) {
                hCrdrMcodeRate = hDrawStageRate;
                selectActDebt2();
            } else if (hAcnoAcctStatus.equals("3")) {
                selectActDebt3();
            } else if (hAcnoAcctStatus.equals("4")) {
                selectActDebt4();
            } else {
                selectActDebt1();
            }

            if ((hCrdrBilled1Amt + hCrdrBilled2Amt + hCrdrBilled3Amt + hCrdrUnbill1Amt
                    + hCrdrUnbill2Amt + hCrdrUnbill3Amt) <= 0)
                continue;

            if (hAcnoAcctStatus.equals("2")) {
                hMndaTransDate = hAcnoOrgDelinquentDate;
            } else if (hAcnoAcctStatus.toCharArray()[0] > '2') {
                selectColBadDebt();
                hMndaTransDate = hCbdtTransDate;
            }

            insertColMonthData();

            if ((hAcnoAcctStatus.equals("4")) || (mCode <= 0))
                continue;

            procColRDrawrate();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hAcctAcctJrnlBal = 0;
        sqlCmd = "select acct_jrnl_bal ";
        sqlCmd += " from act_acct ";
        sqlCmd += "where p_seqno = ? ";
        setString(1, hAcctPSeqno);
        
        extendField = "act_acct.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcctAcctJrnlBal = getValueLong("act_acct.acct_jrnl_bal");
        }
    }
    
    /***********************************************************************/
    void selectActJrnl1() throws Exception {     /* D檔金額 */
    	hJrnlDelAmt = 0;
    	sqlCmd = "select nvl(sum(transaction_amt),0) as del_amt ";
        sqlCmd += "from act_jrnl ";
        sqlCmd += "where substr(crt_date,1,6) = ? ";
        sqlCmd += "and dr_cr = 'D' and tran_class = 'A' and acct_code = 'DB' ";
        sqlCmd += "and p_seqno = ? and acct_type = ? ";
        setString(1, hMndaProcMonth);
        setString(2, hAcnoPSeqno);
        setString(3, hAcnoAcctType);
        
        extendField = "act_jrnl_1.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	hJrnlDelAmt = getValueDouble("act_jrnl_1.del_amt");
        }
    }
    
    /***********************************************************************/
    void selectActJrnl2() throws Exception {     /* 科目AI回收金額 */
    	hJrnlRecvAiAmt = 0;
    	sqlCmd = "select nvl(sum(transaction_amt),0) as recv_ai_amt ";
        sqlCmd += "from act_jrnl ";
        sqlCmd += "where substr(crt_date,1,6) = ? ";
        sqlCmd += "and dr_cr = 'D' and tran_class = 'D' and acct_code = 'AI' ";
        sqlCmd += "and p_seqno = ? and acct_type = ? ";
        setString(1, hMndaProcMonth);
        setString(2, hAcnoPSeqno);
        setString(3, hAcnoAcctType);
        
        extendField = "act_jrnl_2.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	hJrnlRecvAiAmt = getValueDouble("act_jrnl_2.recv_ai_amt");
        }
    }

    /***********************************************************************/
    void selectActDebt2() throws Exception {     /* 分期 */
        sqlCmd = "select sum(decode(sign(acct_month - ?),1,0, decode(acct_code,'BL',end_bal,'CA',end_bal,'CB',end_bal, 'IT',end_bal,'ID',end_bal,'AO',end_bal,'OT',end_bal,0))) h_crdr_billed_1_amt,";
        sqlCmd += "sum(decode(acct_code,'CC',end_bal,0)) h_crdr_billed_3_amt,";
        sqlCmd += "sum(decode(acct_code,'RI',end_bal,'CI',end_bal)) h_crdr_billed_2_amt,";
        sqlCmd += "sum(decode(sign(acct_month - ?),0, decode(acct_code,'BL',end_bal,'CA',end_bal,'CB',end_bal,'IT',end_bal,'ID',end_bal,'AO',end_bal,'OT',end_bal,0),0)) h_crdr_unbill_1_amt ";
        sqlCmd += " from act_debt ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and  end_bal > 0 ";
        setString(1, hWdayThisAcctMonth);
        setString(2, hWdayNextAcctMonth);
        setString(3, hAcnoPSeqno);
        
        extendField = "act_debt_2.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCrdrBilled1Amt = getValueDouble("act_debt_2.h_crdr_billed_1_amt");
            hCrdrBilled3Amt = getValueDouble("act_debt_2.h_crdr_billed_3_amt");
            hCrdrBilled2Amt = getValueDouble("act_debt_2.h_crdr_billed_2_amt");
            hCrdrUnbill1Amt = getValueDouble("act_debt_2.h_crdr_unbill_1_amt");
        }
    }

    /***********************************************************************/
    void selectActDebt3() throws Exception {     /* 催收 */
        sqlCmd = "select sum(decode(acct_code,'CB',end_bal,0)) h_crdr_billed_1_amt,";
        sqlCmd += "sum(decode(acct_code,'CI',end_bal,0)) h_crdr_billed_2_amt,";
        sqlCmd += "sum(decode(acct_code,'CC',end_bal,0)) h_crdr_billed_3_amt ";
        sqlCmd += " from act_debt  ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and  acct_code in ('CB','CI','CC')  ";
        sqlCmd += "and  end_bal > 0 ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_debt_3.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCrdrBilled1Amt = getValueDouble("act_debt_3.h_crdr_billed_1_amt");
            hCrdrBilled2Amt = getValueDouble("act_debt_3.h_crdr_billed_2_amt");
            hCrdrBilled3Amt = getValueDouble("act_debt_3.h_crdr_billed_3_amt");
        }
    }

    /***********************************************************************/
    void selectActDebt4() throws Exception {      /* 呆帳 */
        sqlCmd = "select sum(end_bal) h_crdr_billed_1_amt ";
        sqlCmd += " from act_debt  ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and  acct_code = 'DB' ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_debt_4.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCrdrBilled1Amt = getValueDouble("act_debt_4.h_crdr_billed_1_amt");
        }
    }

    /***********************************************************************/
    void selectActDebt1() throws Exception {
        sqlCmd = "select sum(decode(sign(acct_month - ?),1,0, decode(acct_code,'BL',end_bal,'CA',end_bal,'IT',end_bal,'ID',end_bal,'AO',end_bal,'OT',end_bal,0))) h_crdr_billed_1_amt,";
        sqlCmd += "sum(decode(acct_code,'RI',end_bal)) h_crdr_billed_2_amt,";
        sqlCmd += "sum(decode(sign(acct_month - ?),0, decode(acct_code,'BL',end_bal,'CA',end_bal,'IT',end_bal,'ID',end_bal,'AO',end_bal,'OT',end_bal,0),0)) h_crdr_unbill_1_amt ";
        sqlCmd += " from act_debt  ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and  end_bal > 0 ";
        setString(1, hWdayThisAcctMonth);
        setString(2, hWdayNextAcctMonth);
        setString(3, hAcnoPSeqno);
        
        extendField = "act_debt_1.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCrdrBilled1Amt = getValueDouble("act_debt_1.h_crdr_billed_1_amt");
            hCrdrBilled2Amt = getValueDouble("act_debt_1.h_crdr_billed_2_amt");
            hCrdrUnbill1Amt = getValueDouble("act_debt_1.h_crdr_unbill_1_amt");
        }
    }

    /***********************************************************************/
    void selectColBadDebt() throws Exception {
        hCbdtTransDate = "";

        sqlCmd = "select max(trans_date) h_cbdt_trans_date ";
        sqlCmd += " from col_bad_debt ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and  trans_type = ?  ";
        sqlCmd += "and  decode(trans_date,'','x',trans_date) <= ? ";
        setString(1, hAcnoPSeqno);
        setString(2, hAcnoAcctStatus);
        setString(3, hBusiBusinessDate);
        
        extendField = "col_bad_debt.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCbdtTransDate = getValue("col_bad_debt.h_cbdt_trans_date");
        }
    }

    /***********************************************************************/
    void insertColMonthData() throws Exception {
    	daoTable = "col_month_data";
    	extendField = daoTable + ".";
        setValue(extendField+"proc_month", hMndaProcMonth);
        setValue(extendField+"proc_date", hBusiBusinessDate);
        setValue(extendField+"trans_type", hAcnoAcctStatus);
        setValue(extendField+"trans_date", hMndaTransDate);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);  //no column
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"id_no", hAcnoAcctHolderId);
        setValue(extendField+"id_no_code", hAcnoAcctHolderIdCode);
        setValue(extendField+"corp_p_seqno", hAcnoCorpPSeqno);
        setValue(extendField+"corp_no", hAcnoCorpNo);
        setValue(extendField+"corp_no_code", hAcnoCorpNoCode);
        setValueDouble(extendField+"line_of_credit_amt", hAcnoLineOfCreditAmt);
        setValue(extendField+"credit_act_no", hAcnoCreditActNo);
        setValue(extendField+"recourse_mark", hAcnoRecourseMark);
        setValue(extendField+"corp_act_flag", hAcnoCorpActFlag);
        setValueLong(extendField+"acct_jrnl_bal", hAcctAcctJrnlBal);
        setValue(extendField+"lawsuit_process_log", hAcnoLawsuitProcessLog);
        setValueDouble(extendField+"billed_1_amt", hCrdrBilled1Amt);
        setValueDouble(extendField+"unbill_1_amt", hCrdrUnbill1Amt);
        setValueDouble(extendField+"billed_2_amt", hCrdrBilled2Amt);
        setValueDouble(extendField+"unbill_2_amt", hCrdrUnbill2Amt);
        setValueDouble(extendField+"billed_3_amt", hCrdrBilled3Amt);
        setValueDouble(extendField+"unbill_3_amt", hCrdrUnbill3Amt);
        setValueDouble(extendField+"del_amt", hJrnlDelAmt);          //add by phopho
        setValueDouble(extendField+"recv_ai_amt", hJrnlRecvAiAmt);  //add by phopho
        setValueInt(extendField+"mcode", hCrdrMcode);
        setValueDouble(extendField+"mcode_rate", hCrdrMcodeRate);
//        setValueLong("mcode_x", h_draw_mcode_x);          //mark by phopho
//        setValueLong("mcode_y", h_draw_mcode_y);
//        setValueLong("mcode_1", h_draw_mcode_1);
//        setValueLong("mcode_2", h_draw_mcode_2);
//        setValueLong("mcode_3", h_draw_mcode_3);
//        setValueDouble("mcode_x_rate", h_draw_mcode_x_rate);
//        setValueDouble("mcode_y_rate", h_draw_mcode_y_rate);
//        setValueDouble("mcode_1_rate", h_draw_mcode_1_rate);
//        setValueDouble("mcode_2_rate", h_draw_mcode_2_rate);
//        setValueDouble("mcode_3_rate", h_draw_mcode_3_rate);
//        setValueDouble("mcode_4_rate", h_draw_mcode_4_rate);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        setValue(extendField+"pay_by_stage_flag", hAcnoPayByStageFlag);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_month_data duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void procColRDrawrate() throws Exception {
        int inta;

        hCrdrDrawAmt = (hCrdrBilled1Amt + hCrdrBilled2Amt + hCrdrBilled3Amt + hCrdrUnbill1Amt
                + hCrdrUnbill2Amt + hCrdrUnbill3Amt) * hCrdrMcodeRate * 0.01;

        if (hCrdrDrawAmt <= 0)
            return;

        if (hAcnoPayByStageFlag.length() != 0)
            inta = 0;
        else
            inta = comcr.str2int(hAcnoAcctStatus);

//        showLogMessage("I", "", "h_crdr_mcode="+h_crdr_mcode);
//        showLogMessage("I", "", "inta="+inta);
//        showLogMessage("I", "", "h_crdr_billed_1_amt="+h_crdr_billed_1_amt);
//        showLogMessage("I", "", "h_crdr_unbill_1_amt="+h_crdr_unbill_1_amt);
//        showLogMessage("I", "", "h_crdr_billed_2_amt="+h_crdr_billed_2_amt);
//        showLogMessage("I", "", "h_crdr_unbill_2_amt="+h_crdr_unbill_2_amt);
//        showLogMessage("I", "", "h_crdr_billed_3_amt="+h_crdr_billed_3_amt);
//        showLogMessage("I", "", "h_crdr_unbill_3_amt="+h_crdr_unbill_3_amt);
//        showLogMessage("I", "", "h_crdr_draw_amt="+h_crdr_draw_amt);
//        showLogMessage("I", "", "h_crdr_mcode_rate="+h_crdr_mcode_rate);
        
        tColArr[inta][hCrdrMcode][1] += hCrdrBilled1Amt;
        tColArr[inta][hCrdrMcode][2] += hCrdrUnbill1Amt;
        tColArr[inta][hCrdrMcode][3] += hCrdrBilled2Amt;
        tColArr[inta][hCrdrMcode][4] += hCrdrUnbill2Amt;
        tColArr[inta][hCrdrMcode][5] += hCrdrBilled3Amt;
        tColArr[inta][hCrdrMcode][6] += hCrdrUnbill3Amt;
        tColArr[inta][hCrdrMcode][7] += hCrdrDrawAmt;
        tColArr[inta][hCrdrMcode][8] += 1;
        tColArr[inta][hCrdrMcode][9] = hCrdrMcodeRate;
    }

    /***********************************************************************/
    void insertColRDrawrate() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate parsedDate = LocalDate.parse(hBusiBusinessDate,formatter);
        
        daoTable = "col_r_drawrate";
        extendField = daoTable + ".";
        setValue(extendField+"proc_date", parsedDate.plusDays(1).format(formatter));
        setValue(extendField+"trans_type", hCrdrTransType.equals("0") ? "A" : hCrdrTransType);
        setValueInt(extendField+"mcode", hCrdrMcode);
        setValueDouble(extendField+"mcode_rate", hCrdrMcodeRate);
        setValueDouble(extendField+"type_count", hCrdrTypeCount);
        setValueDouble(extendField+"billed_1_amt", hCrdrBilled1Amt);
        setValueDouble(extendField+"unbill_1_amt", hCrdrUnbill1Amt);
        setValueDouble(extendField+"billed_2_amt", hCrdrBilled2Amt);
        setValueDouble(extendField+"unbill_2_amt", hCrdrUnbill2Amt);
        setValueDouble(extendField+"billed_3_amt", hCrdrBilled3Amt);
        setValueDouble(extendField+"unbill_3_amt", hCrdrUnbill3Amt);
        setValueDouble(extendField+"draw_amt", hCrdrDrawAmt);
        setValue(extendField+"mod_time", sysDate + sysTime);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_r_drawrate duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertColMDrawrate() throws Exception {
    	daoTable = "col_drawrate";
    	extendField = daoTable + ".";
        setValue(extendField+"proc_month", hMndaProcMonth);
        setValue(extendField+"trans_type", hCrdrTransType.equals("0") ? "A" : hCrdrTransType);
        setValueInt(extendField+"mcode", hCrdrMcode);
        setValueDouble(extendField+"mcode_rate", hCrdrMcodeRate);
        setValueDouble(extendField+"type_count", hCrdrTypeCount);
        setValueDouble(extendField+"billed_1_amt", hCrdrBilled1Amt);
        setValueDouble(extendField+"unbill_1_amt", hCrdrUnbill1Amt);
        setValueDouble(extendField+"billed_2_amt", hCrdrBilled2Amt);
        setValueDouble(extendField+"unbill_2_amt", hCrdrUnbill2Amt);
        setValueDouble(extendField+"billed_3_amt", hCrdrBilled3Amt);
        setValueDouble(extendField+"unbill_3_amt", hCrdrUnbill3Amt);
        setValueDouble(extendField+"draw_amt", hCrdrDrawAmt);
        setValue(extendField+"mod_time", sysDate + sysTime);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_drawrate duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB011 proc = new ColB011();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
