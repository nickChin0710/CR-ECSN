/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/08  V1.00.00    phopho     program initial                          *
*  109/12/09  V1.00.01    shiyuqi       updated for project coding standard   *
*  109/12/30  V1.00.02    Zuwei       “icbcecs”改為”system”                    *
*  112/07/10  V1.00.03    Sunny      insertActPayBatch增加日結覆核人員、日期、時間      *
*  112/11/02  V1.00.04    Sunny      調整只處理一般個人帳戶(不含商務卡)                                *
******************************************************************************/

package Col;

import java.util.ArrayList;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColA411 extends AccessDAO {
    private String progname = "前置協商繳款清算資料回灌入帳處理程式  112/11/02  V1.00.04 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";

    String hClpyLiacSeqno = "";
    String hClpyPaySeqno = "";
    String hClpyFileDate = "";
    String hClpyFileType = "";
    String hClpyId = "";
    String hClpyLiacRemark = "";
    String hClpyRegBankNo = "";
    String hClpyAllocateDate = "";
    double hClpyAllocateAmt = 0;
    String hClpyProcFlag = "";
    String hClpyRowid = "";
    String hClplPSeqno = "";
    String hClplAcctType = "";
    String hClplAcctKey = "";
    double hClplAllocateAmt = 0;
    String hClplAllocateDate = "";
    String hClplRowid = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoStmtCycle = "";
    String hAcnoIdPSeqno = "";
    String hApbtBatchNo = "";
    long hApbtBatchTotCnt = 0;
    double hApbtBatchTotAmt = 0;
    String hApdlSerialNo = "";
    String hApdlPaymentType = "";
    String hClpyIdPSeqno = "";
    String hClnoRegBankNo = "";
    double hClctPerAllocateAmt = 0;
    String tmpstr="";

    List<String> aDebtPSeqno = new ArrayList<String>();
    List<String> aDebtAcctType = new ArrayList<String>();
    List<String> aDebtAcctKey = new ArrayList<String>();
    List<Double> aDebtEndBal = new ArrayList<Double>();
    List<String> aAcnoCardIndicator = new ArrayList<String>();
    List<String> aCardPSeqno = new ArrayList<String>();
    List<String> aCardCurrentCode = new ArrayList<String>();
    List<String> aCardAcctType = new ArrayList<String>();
    List<String> aCardAcctKey = new ArrayList<String>();

    int  totalCnt = 0;
    int  errorCnt = 0;
    int actDebtCnt, crdCardCnt;
    long nSerialNo;
    long hTempBatchNoSeq;

    public int mainProcess(String[] args) {
        try {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (comm.isAppActive2(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }

            // 檢查參數
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : ColA411", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();
            selectColLiacPay0();
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
        sqlCmd += "from ptr_businday ";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void selectColLiacPay0() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "file_date, ";
        sqlCmd += "file_type ";
        sqlCmd += "from col_liac_pay ";
        sqlCmd += "where proc_flag = '0' ";
        sqlCmd += "group by file_date, file_type ";

        openCursor();
        while (fetchTable()) {
            hClpyFileDate = getValue("file_date");
            hClpyFileType = getValue("file_type");

            selectActPayBatch();
            showLogMessage("I", "", "Batch no [" + hApbtBatchNo + "]");
            nSerialNo = 1;
            totalCnt = 0;
            selectColLiacPay();
            showLogMessage("I", "", "Total 清算 process record[" + totalCnt + "] error[" + errorCnt + "]");

            hApbtBatchTotCnt = 0;
            hApbtBatchTotAmt = 0;
            totalCnt = 0;
            selectActAcno();
            if (hApbtBatchTotCnt != 0)
                insertActPayBatch();
            showLogMessage("I", "", "Total 帳務 process record[" + totalCnt + "]");
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectColLiacPay() throws Exception {
        long tempSumLong, tempLong;
        double totalEndBal = 0;
        int int0a = 0;
        int int0b = 0;
        List<Integer> pSeqnoIdx = new ArrayList<Integer>();
        List<Double> settleAmt = new ArrayList<Double>();
        sqlCmd = "select ";
        sqlCmd += "liac_seqno, ";
        sqlCmd += "pay_seqno, ";
        sqlCmd += "id_no, ";
        sqlCmd += "id_p_seqno, ";
        sqlCmd += "allocate_date, ";
        sqlCmd += "allocate_amt, ";
        sqlCmd += "reg_bank_no, ";
        sqlCmd += "liac_remark, ";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_liac_pay ";
        sqlCmd += "where proc_flag = '0' ";
        sqlCmd += "and   file_date = ? ";
        sqlCmd += "and   file_type = ? ";
        setString(1, hClpyFileDate);
        setString(2, hClpyFileType);

        extendField = "col_liac_pay.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hClpyLiacSeqno = getValue("col_liac_pay.liac_seqno", i);
            hClpyPaySeqno = getValue("col_liac_pay.pay_seqno", i);
            hClpyId = getValue("col_liac_pay.id_no", i);
            hClpyIdPSeqno = getValue("col_liac_pay.id_p_seqno", i);
            hClpyRegBankNo = getValue("col_liac_pay.reg_bank_no", i);
            hClpyLiacRemark = getValue("col_liac_pay.liac_remark", i);
            hClpyAllocateDate = getValue("col_liac_pay.allocate_date", i);
            hClpyAllocateAmt = getValueDouble("col_liac_pay.allocate_amt", i);
            hClpyRowid = getValue("col_liac_pay.rowid", i);
            hClpyProcFlag = "1";

//            showLogMessage("I", "", "PayDate[" + hClpyAllocateDate + "]");
//            showLogMessage("I", "", " PayAmt[" + hClpyAllocateAmt + "]");
            
          //Debug-Log
            tmpstr = String.format("Major-ID[%s],RegBankNo[%s],Remark[%s],PayDate[%s],PayAmt[%8.0f]", hClpyId, hClpyRegBankNo,hClpyLiacRemark,hClpyAllocateDate,hClpyAllocateAmt);
            showLogMessage("I", "", String.format("%s", tmpstr));
            
            totalCnt++;
            if (totalCnt % 1000 == 0) {
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");
            }

            selectActDebt();
            if (actDebtCnt == 0) {
                hClpyProcFlag = "A";
                insertActPayError();
                updateColLiacPay();
                continue;
            }

            totalEndBal = 0;
            for (int0a = 0; int0a < actDebtCnt; int0a++)
                totalEndBal = totalEndBal + aDebtEndBal.get(int0a);

            if (totalEndBal == 0) {
                selectCrdCard();

                for (int0a = 0; int0a < crdCardCnt; int0a++)
                    if (aCardCurrentCode.get(int0a).equals("0"))
                        break;
                if (int0a < crdCardCnt) {
                    hClplPSeqno = aCardPSeqno.get(int0a);
                    hClplAcctType = aCardAcctType.get(int0a);
                    hClplAcctKey = aCardAcctKey.get(int0a);
                } else {
                    hClplPSeqno = aCardPSeqno.get(0);
                    hClplAcctType = aCardAcctType.get(0);
                    hClplAcctKey = aCardAcctKey.get(0);
                }
                hClplAllocateAmt = hClpyAllocateAmt;
                insertColLiacPayDtl();
            } else {
                tempSumLong = int0b = 0;
                for (int0a = 0; int0a < actDebtCnt; int0a++) {
                    if (aDebtEndBal.get(int0a) == 0)
                        continue;
                    tempLong = (long) ((hClpyAllocateAmt * (aDebtEndBal.get(int0a) * 1.0) / totalEndBal) + 0.5);

                    pSeqnoIdx.add(int0b, int0a);
                    settleAmt.add(int0b, (double) tempLong);
                    tempSumLong = tempSumLong + tempLong;
                    int0b++;
                }
                settleAmt.add(0, settleAmt.get(0) + hClpyAllocateAmt - tempSumLong); /* 四捨五入差值補第一個 */
                for (int0a = 0; int0a < int0b; int0a++) {
                    hClplPSeqno = aDebtPSeqno.get(pSeqnoIdx.get(int0a));
                    hClplAcctType = aDebtAcctType.get(pSeqnoIdx.get(int0a));
                    hClplAcctKey = aDebtAcctKey.get(pSeqnoIdx.get(int0a));
                    hClplAllocateAmt = settleAmt.get(int0a);
                    insertColLiacPayDtl();
                }
            }
            updateColLiacPay();
        }
    }

    /***********************************************************************/
    void selectActDebt() throws Exception {
        aDebtPSeqno.clear();
        aDebtAcctType.clear();
        aDebtAcctKey.clear();
        aDebtEndBal.clear();
        aAcnoCardIndicator.clear();
        
//        sqlCmd = "select a.p_seqno,";
//        sqlCmd += "max(a.acct_type) acct_type, ";
//        sqlCmd += "max(b.acct_key) acct_key, ";
//        sqlCmd += "sum(a.end_bal) end_bal, ";
//        sqlCmd += "max(b.card_indicator) card_indicator ";
//        sqlCmd += "from crd_idno c, act_acno b ";
//        sqlCmd += "left join act_debt a ";
////        sqlCmd += "on   a.p_seqno   = b.p_seqno ";
//        sqlCmd += "on   a.p_seqno   = b.acno_p_seqno ";
//        sqlCmd += "WHERE  c.id_no = ? "; // b.acct_holder_id
//        sqlCmd += "and b.id_p_seqno = c.id_p_seqno "; // find id_no
//        sqlCmd += "and b.acno_flag <> 'Y' ";
//        sqlCmd += "GROUP  BY a.p_seqno ";
//        sqlCmd += "ORDER  BY max(b.card_indicator),max(a.acct_type) ";
//        setString(1, h_clpy_id);
        
        sqlCmd = "select a.p_seqno,";
        sqlCmd += "max(a.acct_type) acct_type, ";
        sqlCmd += "max(b.acct_key) acct_key, ";
        sqlCmd += "sum(a.end_bal) end_bal, ";
        sqlCmd += "max(b.card_indicator) card_indicator ";
        sqlCmd += "from act_acno b ";
        sqlCmd += "left join act_debt a ";
        sqlCmd += "on   a.p_seqno   = b.acno_p_seqno ";
        sqlCmd += "WHERE b.id_p_seqno = ? ";
        //sqlCmd += "and b.acno_flag <> 'Y' ";
        sqlCmd += "and b.acno_flag ='1' "; //限個人
        sqlCmd += "GROUP  BY a.p_seqno ";
        sqlCmd += "ORDER  BY max(b.card_indicator),max(a.acct_type) ";
        setString(1, hClpyIdPSeqno);
        
        extendField = "act_debt.";
        
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            aDebtPSeqno.add(getValue("act_debt.p_seqno", i));
            aDebtAcctType.add(getValue("act_debt.acct_type", i));
            aDebtAcctKey.add(getValue("act_debt.acct_key", i));
            aDebtEndBal.add(getValueDouble("act_debt.end_bal", i));
            aAcnoCardIndicator.add(getValue("act_debt.card_indicator", i));
        }

        actDebtCnt = recordCnt;
    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {
        aCardPSeqno.clear();
        aCardCurrentCode.clear();
        aCardAcctType.clear();
        aCardAcctKey.clear();
        aAcnoCardIndicator.clear();
//        sqlCmd = "select a.p_seqno,";
//        sqlCmd += "max(a.current_code) current_code, ";
//        sqlCmd += "max(a.acct_type) acct_type, ";
//        sqlCmd += "max(b.acct_key) acct_key, ";
//        sqlCmd += "max(b.card_indicator) card_indicator ";
//        sqlCmd += "from crd_card a,act_acno b, crd_idno d ";
//        sqlCmd += "where a.p_seqno = b.p_seqno ";
//        sqlCmd += "and d.id_no = ? "; //b.acct_holder_id
//        sqlCmd += "and d.id_p_seqno = b.id_p_seqno "; // find id_no
//        sqlCmd += "GROUP  BY a.p_seqno ";
//        sqlCmd += "ORDER  BY max(b.card_indicator),max(a.acct_type) ";
//        setString(1, h_clpy_id);
//        // setString(1, h_clpy_id_p_seqno);
        
//        sqlCmd = "select a.acno_p_seqno,";
//        sqlCmd += "max(a.current_code) current_code, ";
//        sqlCmd += "max(a.acct_type) acct_type, ";
//        sqlCmd += "max(b.acct_key) acct_key, ";
//        sqlCmd += "max(b.card_indicator) card_indicator ";
//        sqlCmd += "from crd_card a,act_acno b, crd_idno d ";
//        sqlCmd += "where a.acno_p_seqno = b.acno_p_seqno ";
//        sqlCmd += "and d.id_no = ? ";
//        sqlCmd += "and d.id_p_seqno = b.id_p_seqno ";
//        sqlCmd += "GROUP  BY a.acno_p_seqno ";
//        sqlCmd += "ORDER  BY max(b.card_indicator),max(a.acct_type) ";
//        setString(1, h_clpy_id);
        
        sqlCmd = "select a.acno_p_seqno,";
        sqlCmd += "max(a.current_code) current_code, ";
        sqlCmd += "max(a.acct_type) acct_type, ";
        sqlCmd += "max(b.acct_key) acct_key, ";
        sqlCmd += "max(b.card_indicator) card_indicator ";
        sqlCmd += "from crd_card a,act_acno b ";
        sqlCmd += "where a.acno_p_seqno = b.acno_p_seqno ";
        sqlCmd += "and b.id_p_seqno = ? ";
        sqlCmd += "and b.acno_flag ='1' "; //限個人卡
        sqlCmd += "GROUP  BY a.acno_p_seqno ";
        sqlCmd += "ORDER  BY max(b.card_indicator),max(a.acct_type) ";
        setString(1, hClpyIdPSeqno);

        extendField = "crd_card.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            aCardPSeqno.add(getValue("crd_card.acno_p_seqno", i));
            aCardCurrentCode.add(getValue("crd_card.current_code", i));
            aCardAcctType.add(getValue("crd_card.acct_type", i));
            aCardAcctKey.add(getValue("crd_card.acct_key", i));
            aAcnoCardIndicator.add(getValue("crd_card.card_indicator", i));
        }

        crdCardCnt = recordCnt;
    }

    // 增加欄位per_allocate_amt(本行每期可分配金額(清算當下))、reg_bank_no(受理行(清算當下))。
    /***********************************************************************/
    void insertColLiacPayDtl() throws Exception {
        dateTime();
        daoTable = "col_liac_pay_dtl";
        extendField = daoTable + ".";
        setValue(extendField+"liac_seqno", hClpyLiacSeqno);
        setValue(extendField+"pay_seqno", hClpyPaySeqno);
        setValue(extendField+"id_no", hClpyId);
        setValue(extendField+"id_p_seqno", hClpyIdPSeqno);
        setValue(extendField+"p_seqno", hClplPSeqno);
        setValue(extendField+"acct_type", hClplAcctType);
        setValue(extendField+"reg_bank_no", hClnoRegBankNo);
        setValueDouble(extendField+"per_allocate_amt", hClctPerAllocateAmt);
        setValue(extendField+"file_date", hClpyFileDate);
        setValue(extendField+"file_type", hClpyFileType);
        setValueDouble(extendField+"allocate_amt", hClplAllocateAmt);
        setValue(extendField+"allocate_date", hClpyAllocateDate);
        setValue(extendField+"proc_flag", "0");
        setValue(extendField+"proc_date", hBusiBusinessDate);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liac_pay_dtl duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateColLiacPay() throws Exception {
        daoTable = "col_liac_pay";
        updateSQL = "proc_flag = ?, ";
        updateSQL += "proc_date = ?,";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hClpyProcFlag);
        setString(2, hBusiBusinessDate);
        setString(3, javaProgram);
        setRowId(4, hClpyRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liac_pay not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertActPayBatch() throws Exception {
        dateTime();
        daoTable = "act_pay_batch";
        extendField = daoTable + ".";
        setValue(extendField+"batch_no", hApbtBatchNo);
        setValueDouble(extendField+"batch_tot_cnt", hApbtBatchTotCnt);
        setValueDouble(extendField+"batch_tot_amt", hApbtBatchTotAmt);
        setValue(extendField+"create_user", "system");
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"trial_user", "system");
        setValue(extendField+"trial_date", sysDate);
        setValue(extendField+"trial_time", sysTime);
        setValue(extendField+"confirm_user", javaProgram); //20230710 add,org:"AIX"
        setValue(extendField+"confirm_date", sysDate);     //20230710 add,
        setValue(extendField+"confirm_time", sysTime);     //20230710 add,
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        
        //當duplitkey時才做update
        if (dupRecord.equals("Y")) {
//            return;
        	daoTable = "act_pay_batch";
            updateSQL = "batch_tot_cnt = batch_tot_cnt + ?, ";
            updateSQL += "batch_tot_amt = batch_tot_amt + ?,";
            updateSQL += "mod_time = sysdate, ";
            updateSQL += "mod_pgm  = ? ";
            whereStr = "where batch_no = ? ";
            setDouble(1, hApbtBatchTotCnt);
            setDouble(2, hApbtBatchTotAmt);
            setString(3, javaProgram);
            setString(4, hApbtBatchNo);

            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_act_pay_batch not found!", "", hCallBatchSeqno);
            }
        }
    }

    /***********************************************************************/
    void selectActPayBatch() throws Exception {
        sqlCmd = "select to_number(substr(max(batch_no),13,4)) batch_no ";
        sqlCmd += "from act_pay_batch ";
        sqlCmd += "where batch_no like ?||'1006%' ";
        setString(1, hBusiBusinessDate);
        
        extendField = "act_pay_batch.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempBatchNoSeq = getValueLong("act_pay_batch.batch_no");
        }

        hTempBatchNoSeq++;
        hApbtBatchNo = String.format("%8.8s1006%04d", hBusiBusinessDate, hTempBatchNoSeq);
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        sqlCmd = "select ";
//        sqlCmd += "b.p_seqno, ";
        sqlCmd += "b.acno_p_seqno, ";
        sqlCmd += "b.acct_type, ";
        sqlCmd += "b.acct_key, ";
        sqlCmd += "b.id_p_seqno, ";
        sqlCmd += "b.stmt_cycle, ";
        sqlCmd += "c.allocate_amt, ";
        sqlCmd += "c.allocate_date, ";
        sqlCmd += "decode(c.file_type,'E','AUT7','AUT8') file_type, ";
        sqlCmd += "c.rowid as rowid ";
        sqlCmd += "from act_acno b, col_liac_pay_dtl c  ";
        sqlCmd += "where b.acno_flag = '1' "; //個人卡
//        sqlCmd += "where b.acno_flag <> 'Y' ";
//        sqlCmd += "and b.p_seqno = c.p_seqno ";
        sqlCmd += "and b.acno_p_seqno = c.p_seqno ";
        sqlCmd += "and c.proc_flag = '0' ";
        sqlCmd += "and c.file_date = ? ";
        sqlCmd += "and c.file_type = ? ";
        sqlCmd += "order by b.acct_type ";
        setString(1, hClpyFileDate);
        setString(2, hClpyFileType);
        extendField = "act_acno.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
//            h_acno_p_seqno = getValue("p_seqno", i);
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno", i);
            hAcnoAcctType = getValue("act_acno.acct_type", i);
            hAcnoAcctKey = getValue("act_acno.acct_key", i);
            hAcnoIdPSeqno = getValue("act_acno.id_p_seqno", i);
            hAcnoStmtCycle = getValue("act_acno.stmt_cycle", i);
            hClplAllocateAmt = getValueDouble("act_acno.allocate_amt", i);
            hClplAllocateDate = getValue("act_acno.allocate_date", i);
            hApdlPaymentType = getValue("act_acno.file_type", i);
            hClplRowid = getValue("act_acno.rowid", i);
    	
            insertActPayDetail();
            updateColLiacPayDtl();
            totalCnt++;
            if (totalCnt % 1000 == 0) {
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");
            }

            hApbtBatchTotCnt++;
            hApbtBatchTotAmt = hApbtBatchTotAmt + hClplAllocateAmt;

            if (hApbtBatchTotCnt > 99999) {
                insertActPayBatch();
                hApbtBatchTotCnt = 0;
                hApbtBatchTotAmt = 0;
                nSerialNo = 1;
                selectActPayBatch();
            }
        }
    }

    /***********************************************************************/
    void insertActPayDetail() throws Exception {
        dateTime();
        hApdlSerialNo = String.format("%05d", nSerialNo);
        nSerialNo++;
        
        daoTable = "act_pay_detail";
        extendField = daoTable + ".";
        setValue(extendField+"batch_no", hApbtBatchNo);
        setValue(extendField+"serial_no", hApdlSerialNo);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"acno_p_seqno", hAcnoPSeqno);  //phopho add
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"acct_key", hAcnoAcctKey);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValueDouble(extendField+"pay_amt", hClplAllocateAmt);
        setValue(extendField+"pay_date", hClplAllocateDate);
        setValue(extendField+"payment_type", hApdlPaymentType);
        setValue(extendField+"update_date", sysDate);
        setValue(extendField+"update_time", sysTime);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_detail duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateColLiacPayDtl() throws Exception {
        daoTable = "col_liac_pay_dtl";
        updateSQL = "proc_flag = '1', ";
        updateSQL += "proc_date = ?,";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hClplRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liac_pay_dtl not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertActPayError() throws Exception {
        dateTime();
        errorCnt++;
        hApdlSerialNo = String.format("%05d", nSerialNo);
        nSerialNo++;

        daoTable = "act_pay_error";
        extendField = daoTable + ".";
        setValue(extendField+"batch_no", hApbtBatchNo);
        setValue(extendField+"serial_no", hApdlSerialNo);
        setValue(extendField+"p_seqno", "");
        setValue(extendField+"acno_p_seqno", "");  //phopho add
        setValue(extendField+"acct_type", "");
        setValueDouble(extendField+"pay_amt", hClpyAllocateAmt);
        setValue(extendField+"pay_date", hClpyAllocateDate);
        setValue(extendField+"payment_type", hClpyFileType.equals("E") ? "AUT7" : "AUT8");
        setValue(extendField+"error_reason", "302");
        setValue(extendField+"error_remark", hClpyFileType.equals("E") ? "AUT7 前協分配" : "AUT8 前協分配");
        setValue(extendField+"crt_user", "system");
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"id_no", hClpyId);
        setValue(extendField+"mod_user", "system");
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_error duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectColLiacNego() throws Exception {
        hClnoRegBankNo = "";
        sqlCmd = "select reg_bank_no ";
        sqlCmd += "from col_liac_nego ";
        sqlCmd += "where liac_seqno = ? ";
        setString(1, hClpyLiacSeqno);
        
        extendField = "col_liac_nego.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hClnoRegBankNo = getValue("col_liac_nego.reg_bank_no");
        }
    }

    /***********************************************************************/
    void selectColLiacContract() throws Exception {
        hClctPerAllocateAmt = 0;
        sqlCmd = "select per_allocate_amt ";
        sqlCmd += "from col_liac_contract ";
        sqlCmd += "where liac_seqno = ? ";
        sqlCmd += "order by file_date desc ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hClpyLiacSeqno);
        
        extendField = "col_liac_contract.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hClctPerAllocateAmt = getValueDouble("col_liac_contract.per_allocate_amt");
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColA411 proc = new ColA411();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
