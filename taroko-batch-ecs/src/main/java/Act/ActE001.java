/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  107/02/23  V1.00.01    Brian     error correction 
 *  111-10-14  V1.00.02    Machao    sync from mega & updated for project coding standard *                         *
 *  112-09-28  V1.00.03    Simon     add debug display                         *
 ******************************************************************************/

package Act;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*繳款資料查核錯誤處理程式*/
public class ActE001 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private final String PROGNAME = "繳款資料查核錯誤處理程式  112-09-28  V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActE001";
    String rptId1 = "ACTE001";
    String rptName1 = "繳款相同金額第二筆以上檢核表";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq1 = 0;
    String buf = "";
    String szTmp = "";
    String szTmp1 = "";
    String hCallBatchSeqno = "";

    String hApdlBatchNo = "";
    String hApdlPSeqno = "";
    String hApdlAcnoPSeqno = "";
    String hApdlIdPSeqno = "";
    String hApdlPayCardNo = "";
    String hApdlPayDate = "";
    String hApdlPaymentType = "";
    double hApdlPayAmt = 0;
    String hApdlUpdateDate = "";
    String hApdlDuplicateMark = "";
    String hApdlAcctType = "";
    String hApdlAcctKey = "";
    String hApdlSerialNo = "";
    String hApdlRowid = "";
    String hCardCardNo = "";
    int hApbtBatchTotCnt = 0;
    String hApbtConfirmUser = "";
    String hApbtRowid = "";
    String hAperErrorReason = "";
    String hAperErrorRemark = "";
    String hAcnoIdPSeqno = "";
    int hTempTotIdCnt = 0;
    int hTempDupIdCnt = 0;
    String hBusiBusinessDate = "";
    String hApdlPaymentTypeChiname = "";
    String hPrintName = "";
    String hRptName = "";
    String hApdlChiName = "";
    String dispDate = "";
    int pageCnt = 0;
    int totalCnt = 0;
    int pageLineCnt = 0;
    int chkStatus = 0;
    double pageAmt = 0; /* 計算每頁的繳款金額加總 */
    String temstr = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : ActE001", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            checkOpen();
            selectActPayDetail();
            if (totalCnt == 0) {
                printNoDetail();
            } else {
              if (pageLineCnt > 32) { /* 每頁顯示35筆，footer 佔3筆 */
                 lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));//跳頁
                 printHeader();
                 printFooter();
              } else {
                 printFooter();
              }
            }
            comc.writeReport(temstr, lpar1);
          //lp_rtn("ACT_E001");
            comcr.insertPtrBatchRpt(lpar1);

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

    /** 開檔 ***********************************************************************/
    void checkOpen() throws Exception {
        hBusiBusinessDate = ""; /* 取營業日 */

        sqlCmd = " select business_date ";
        sqlCmd += " from   ptr_businday ";
        sqlCmd += " fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
        temstr = String.format("%s/reports/ACT_E001_%s", comc.getECSHOME(), hBusiBusinessDate);
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", String.format("File_name[%s]", temstr));

    }

    /***********************************************************************/
    void selectActPayDetail() throws Exception {
        printHeader();
        pageLineCnt = 5;

        sqlCmd = "select ";
        sqlCmd += " distinct batch_no ";
        sqlCmd += "  from act_pay_detail ";
        sqlCmd += " where proc_mark = '' ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hApdlBatchNo = getValue("batch_no");

            selectActPayBatch(); /*
                                     * check act_pay_batch confirm_user length,
                                     * if len==0 then continue for
                                     */
        showLogMessage("I", "", String.format("hApdlBatchNo[%s]", hApdlBatchNo));

            if (hApbtConfirmUser.length() == 0)
                continue;

            chkStatus = 0;
            if (selectActPayDetail02() > (selectActPayDetail01() * 0.05) + 0.5)
                chkStatus = 1;

        showLogMessage("I", "", String.format("chkStatus[%s]", chkStatus));

            selectActPayDetail1();
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void selectActPayBatch() throws Exception {
        hApbtBatchTotCnt = 0;
        hApbtConfirmUser = "";
        hApbtRowid = "";

        sqlCmd = "select batch_tot_cnt,";
        sqlCmd += " confirm_user,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_pay_batch  ";
        sqlCmd += " where batch_no = ? ";
        setString(1, hApdlBatchNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hApbtBatchTotCnt = getValueInt("batch_tot_cnt");
            hApbtConfirmUser = getValue("confirm_user");
            hApbtRowid = getValue("rowid");
        }

    }

    /***********************************************************************/
    double selectActPayDetail02() throws Exception {
        double hTempDupIdCnt = 0;

        sqlCmd = "select sum(cnt) as h_temp_dup_id_cnt ";
        sqlCmd += " from ( ";
        sqlCmd += "select count(*) as cnt ";
        sqlCmd += " from act_pay_detail  ";
        sqlCmd += "where batch_no = ?  ";
        sqlCmd += "and proc_mark = ''  ";
        sqlCmd += "and pay_amt between 300  and  150000  ";
        sqlCmd += "and (decode(duplicate_mark, '','N',duplicate_mark) != 'Y' or  substr(batch_no,9,4) not in ('9005','9007','9008','9009')) ";
        sqlCmd += "group by batch_no,p_seqno,acno_p_seqno,pay_card_no,pay_amt,pay_date,payment_type Having count(*) > 1 ";
        sqlCmd += " ) ";
        setString(1, hApdlBatchNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempDupIdCnt = getValueDouble("h_temp_dup_id_cnt");
        }

        return (hTempDupIdCnt);
    }

    /***********************************************************************/
    double selectActPayDetail01() throws Exception {
        double hTempTotIdCnt = 0;

        sqlCmd = "select count(*) h_temp_tot_id_cnt ";
        sqlCmd += " from act_pay_detail  ";
        sqlCmd += "where batch_no = ?  ";
        sqlCmd += "and proc_mark = ''  ";
        sqlCmd += "and pay_amt between 300  and  150000  ";
        sqlCmd += "and (decode(duplicate_mark, '','N',duplicate_mark) != 'Y' or  substr(batch_no,9,4) not in ('9005','9007','9008','9009')) ";
        setString(1, hApdlBatchNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempTotIdCnt = getValueDouble("h_temp_tot_id_cnt");
        }

        return (hTempTotIdCnt);
    }

    /***********************************************************************/
    void selectActPayDetail1() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " p_seqno,";
        sqlCmd += " acno_p_seqno,";
        sqlCmd += " id_p_seqno,";
        sqlCmd += " pay_card_no,";
        sqlCmd += " pay_date,";
        sqlCmd += " payment_type,";
        sqlCmd += " pay_amt,";
        sqlCmd += " crt_date,";
        sqlCmd += " duplicate_mark ";
        sqlCmd += "  from act_pay_detail ";
        sqlCmd += " where batch_no = ? ";
        sqlCmd += "   and proc_mark = '' ";
        sqlCmd += " group by p_seqno,acno_p_seqno,id_p_seqno,pay_card_no,pay_date,payment_type,pay_amt,crt_date,duplicate_mark ";
        setString(1, hApdlBatchNo);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hApdlPSeqno = getValue("p_seqno", i);
            hApdlAcnoPSeqno = getValue("acno_p_seqno", i);
            hApdlIdPSeqno = getValue("id_p_seqno", i);
            hApdlPayCardNo = getValue("pay_card_no", i);
            hApdlPayDate = getValue("pay_date", i);
            hApdlPaymentType = getValue("payment_type", i);
            hApdlPayAmt = getValueDouble("pay_amt", i);
            hApdlUpdateDate = getValue("crt_date", i);
            hApdlDuplicateMark = getValue("duplicate_mark", i);

 		      	if ((i % 5000) == 0) {
        showLogMessage("I", "", String.format("selectActPayDetail1 proc_cnt[%d]", i));
            }

            selectActPayDetail2();
        }
    }

    /***********************************************************************/
    void selectActPayDetail2() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.acct_type,";
        sqlCmd += " b.acct_key,";
        sqlCmd += " a.serial_no,";
        sqlCmd += " a.rowid as rowid ";
        sqlCmd += "  from act_pay_detail a, act_acno b ";
        sqlCmd += " where a.batch_no = ? ";
        sqlCmd += "   and a.p_seqno = ? ";
        sqlCmd += "   and decode(a.pay_card_no, '','x',a.pay_card_no) = decode(cast(? as varchar(20)), '','x',cast(? as varchar(20))) ";
        sqlCmd += "   and a.pay_date = ? ";
        sqlCmd += "   and decode(a.payment_type,'','x',a.payment_type) = ? ";
        sqlCmd += "   and a.pay_amt = ? ";
        sqlCmd += "   and decode(a.duplicate_mark, '','x',a.duplicate_mark) = decode(cast(? as varchar(10)), '','x',cast(? as varchar(10))) ";
        sqlCmd += "   and decode(a.duplicate_mark,'','N',a.duplicate_mark) != 'Y' ";
        sqlCmd += "   and a.proc_mark = '' ";
        sqlCmd += "   and b.acno_p_seqno = a.p_seqno ";
        setString(1, hApdlBatchNo);
        setString(2, hApdlPSeqno);
        setString(3, hApdlPayCardNo);
        setString(4, hApdlPayCardNo);
        setString(5, hApdlPayDate);
        setString(6, hApdlPaymentType);
        setDouble(7, hApdlPayAmt);
        setString(8, hApdlDuplicateMark);
        setString(9, hApdlDuplicateMark);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hApdlAcctType = getValue("acct_type", i);
            hApdlAcctKey = getValue("acct_key", i);
            hApdlSerialNo = getValue("serial_no", i);
            hApdlRowid = getValue("rowid", i);

            if ((hApdlPayCardNo.length() != 0) && (selectCrdCard() != 0)) {
                hAperErrorReason = "902";
                hAperErrorRemark = "Card no not found";
                insertActPayError();
                updateActPayBatch();
                deleteActPayDetail();
                continue;
            }

            if (selectActAcno() != 0) {
                hAperErrorReason = "901";
                hAperErrorRemark = "ACCT NO not found";
                insertActPayError();
                updateActPayBatch();
                deleteActPayDetail();
                continue;
            }

            if (checkActPayDetail() != 0) { /* check 是否有重覆的 */
                selectCrdIdno(); /* select chi_name, add by San */

                if ((chkStatus == 1) && (hApdlPayAmt > 300) && (i > 0)) {
                  //h_aper_error_reason = "905";
                    hAperErrorReason = "903";
                  //h_aper_error_remark = "pay_amt > 1500000";
                    hAperErrorRemark = "Duplicate data";
                    insertActPayError();
                    updateActPayBatch();
                    deleteActPayDetail();
                    /* print_detail(); add by San */
                    continue;
                }
                if ((hApdlPayAmt > 150000) && (i > 0)) {
                    /*
                     * str2var(h_aper_error_reason , "905");
                     * str2var(h_aper_error_remark , "pay_amt > 1500000");
                     * insert_act_pay_error(); delete_act_pay_detail();
                     * continue; update_act_pay_batch(); mark for
                     * RECS-s1031210-089
                     */
                    printDetail(); /* add by San */
                }
            }
            updateActPayDetail();
        }
    }

    /***********************************************************************/
    int selectCrdCard() throws Exception {
        hCardCardNo = "";

        sqlCmd = "select card_no ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no = decode(cast(? as varchar(20)),'','x',cast(? as varchar(20))) ";
        setString(1, hApdlPayCardNo);
        setString(2, hApdlPayCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCardCardNo = getValue("card_no");
        } else
            return (1);

        return (0);
    }

    /***********************************************************************/
    int selectActAcno() throws Exception {
        hAcnoIdPSeqno = "";

        sqlCmd = "select id_p_seqno ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno  = ?  ";
        sqlCmd += "  and acno_p_seqno = p_seqno "; // p_seqno = acct_p_seqno <--> acno_flag <> 'Y'
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hApdlPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcnoIdPSeqno = getValue("id_p_seqno");
        } else
            return (1);

        return (0);
    }

    /***********************************************************************/
    int checkActPayDetail() throws Exception {
        if ((hApdlDuplicateMark.equals("Y")) || (hApdlBatchNo.substring(8, 12).equals("9005"))
                || (hApdlBatchNo.substring(8, 12).equals("9007"))
                || (hApdlBatchNo.substring(8, 12).equals("9008"))
                || (hApdlBatchNo.substring(8, 12).equals("9009")))
            return (0);

        return (1);
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hApdlChiName = "";

        sqlCmd = "select chi_name ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hApdlIdPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            showLogMessage("I", "", String.format("chi_name not found, id_p_seqno=[%s]", hApdlIdPSeqno));
        }
        if (recordCnt > 0) {
            hApdlChiName = getValue("chi_name");
        }

    }

    /***********************************************************************/
    void insertActPayError() throws Exception {
        setValue("batch_no", hApdlBatchNo);
        setValue("serial_no", hApdlSerialNo);
        setValue("p_seqno", hApdlPSeqno);
        setValue("acno_p_seqno", hApdlAcnoPSeqno);
        setValue("acct_type", hApdlAcctType);
        setValue("pay_card_no", hApdlPayCardNo);
        setValueDouble("pay_amt", hApdlPayAmt);
        setValue("pay_date", hApdlPayDate);
      //setValue("payment_type", "OTHR");
        setValue("payment_type", hApdlPaymentType);
        setValue("error_reason", hAperErrorReason);
        setValue("error_remark", hAperErrorRemark);
        setValue("crt_user", javaProgram);
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("mod_user", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "act_pay_error";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_pay_error duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActPayBatch() throws Exception {
        hApbtBatchTotCnt--;

        daoTable = "act_pay_batch";
        updateSQL = "batch_tot_cnt = batch_tot_cnt - 1,";
        updateSQL += " batch_tot_amt = batch_tot_amt - ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm   = ?";
        whereStr = "where batch_no  = ? ";
        setDouble(1, hApdlPayAmt);
        setString(2, javaProgram);
        setString(3, javaProgram);
        setString(4, hApdlBatchNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_pay_batch not found!", "", hCallBatchSeqno);
        }

        if (hApbtBatchTotCnt == 0)
            deleteActPayBatch();
    }

    /***********************************************************************/
    void deleteActPayBatch() throws Exception {
        daoTable = "act_pay_batch";
        whereStr = "where rowid = ? ";
        setRowId(1, hApbtRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_act_pay_batch not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void deleteActPayDetail() throws Exception {
        daoTable = "act_pay_detail";
        whereStr = "where rowid = ? ";
        setRowId(1, hApdlRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_act_pay_detail not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void printDetail() throws Exception {
        /** 將繳款代碼轉換成中文,可參考hard code在acti0040->tran_type->edit裡的 **/
        hApdlPaymentTypeChiname = "";
      //sqlCmd = "select decode(payment_type,'AUT1','ICBC自動扣繳','AUT2','它行自動扣繳','COU1','它行臨櫃繳款','TIKT','郵局劃撥' "
      //        + ",'IBC1','自行臨櫃繳款-現金','IBC2','自行臨櫃繳款-轉帳','IBC3','自行臨櫃繳款-票據' ,"
      //        + "'IBA1','金融卡ATM轉帳-自行現金','IBA2','金融卡ATM轉帳-自行轉帳','IBA3','金融卡ATM轉帳-它行轉帳' ,"
      //        + "'TEBK','自行/它行電話銀行繳款','INBK','自行網路銀行繳款','IBOT','IBM 繳款-其他' ,"
      //        + "'REFU','退貨或Reversal轉Payment','MIST','誤入帳補正','COMA','ATM繳款手續費轉Payment' ,"
      //        + "'COMB','Fancy卡退 手續費轉Payment','BON1','年費回饋','BON2','拉卡獎金轉Payment' ,"
      //        + "'WAIP','D檔 退利息、違約金','BACK','Back Date 退利息、違約金','DUMY','虛擬繳款' ,'OTHR','其他繳款',"
      //        + "'COBO','Fancy 卡基金','AUT3','本行帳戶自動抵銷款' ,'AUT4','債務協商入帳-系統比例',"
      //        + "'AUT5','債務協商入帳-欠款比例','COU2','統一超商臨櫃繳款' ,'COU3','全家便利商店繳款',"
      //        + "'COU4','福客多便利商店繳款','COU5','萊爾富便利商店繳款' ,'ACH1','ACH他行自動扣款',"
      //        + "'IBC4','自行臨櫃-其他票據','IBA4','本行金融卡-它行轉帳-全國繳稅費平台' ,'EPAY','本行-E化繳費平台',"
      //        + "'AUT6','債務協商入帳-本行最大債權','COU6','來來超商臨櫃繳款' ,'AUT7','前協-本行最大債權行',"
      //        + "'AUT8','前協-他行最大債權行','IBC5','個人信用貸款' ,'AUT9','更生-他行最大債權行',"
      //        + "'AUT0','更生-本行最大債權行') as ptname ";
        sqlCmd = "select ptr_payment.bill_desc as ptname ";
        sqlCmd += " from ACT_PAY_DETAIL, ptr_payment ";
        sqlCmd += "where batch_no = ?  ";
        sqlCmd += "and p_seqno = ?  ";
        sqlCmd += "and serial_no = ?  ";
        sqlCmd += "and ACT_PAY_DETAIL.payment_type = ? ";
    		sqlCmd += "and ptr_payment.payment_type = ACT_PAY_DETAIL.payment_type ";
        setString(1, hApdlBatchNo);
        setString(2, hApdlPSeqno);
        setString(3, hApdlSerialNo);
        setString(4, hApdlPaymentType);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hApdlPaymentTypeChiname = getValue("ptname");
        }

        totalCnt++; /* 總筆數 */
        pageLineCnt++; 
        if (pageLineCnt >= 36) {/* 當每頁達35筆即再印header */
            lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));//跳頁
            printHeader();
            pageLineCnt = 6;
        }

        buf = "";
        /* 處理日期, 西元->民國 (有值再處理日期格式化) */
        if (hApdlUpdateDate.length() != 0) {
            szTmp1 = String.format("%4.4s", hApdlUpdateDate);
            szTmp = String.format("%03d/%2.2s/%2.2s", comcr.str2long(szTmp1) - 1911, hApdlUpdateDate.substring(4),
                    hApdlUpdateDate.substring(6));
            buf = comcr.insertStr(buf, szTmp, 2);
        }

        /* 帳戶-帳號 */
        szTmp1 = String.format("%2.2s-%s", hApdlAcctType, hApdlAcctKey);
        buf = comcr.insertStr(buf, szTmp1, 13);

        /* 繳款卡號 */
        buf = comcr.insertStr(buf, hApdlPayCardNo, 30);

        /* 中文姓名 */
        buf = comcr.insertStr(buf, hApdlChiName, 55);

        /* 繳款(交易)日期, 西元->民國 (有值再處理日期格式化) */
        if (hApdlPayDate.length() != 0) {
            szTmp1 = String.format("%4.4s", hApdlPayDate);
            szTmp = String.format("%03d/%2.2s/%2.2s", comcr.str2long(szTmp1) - 1911, hApdlUpdateDate.substring(4),
                    hApdlUpdateDate.substring(6));
            buf = comcr.insertStr(buf, szTmp, 85);
        }

        /* 繳款金額 */
        szTmp = comcr.commFormat("2$,3$,3$,3$", hApdlPayAmt);
        buf = comcr.insertStr(buf, szTmp, 100);

        /* 繳款途徑-繳款途徑中文 */
        szTmp1 = String.format("%s-%s", hApdlPaymentType, hApdlPaymentTypeChiname);
        buf = comcr.insertStr(buf, szTmp1, 116);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        pageAmt = pageAmt + hApdlPayAmt;/* 每頁的[繳款金額]加總 */

    }

    /***********************************************************************/
    void printHeader() throws Exception {
        pageCnt++; /* 頁數控制 */

        buf = "";
        buf = comcr.insertStr(buf, "ACT_E001", 1);
        szTmp = comcr.bankName;
        buf = comcr.insertStrCenter(buf, szTmp, 132);
        buf = comcr.insertStr(buf, "列印表日 :", 112);
        dispDate = comc.convDates(sysDate, 1);
        buf = comcr.insertStr(buf, dispDate, 123);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStrCenter(buf, "繳款相同金額第二筆以上檢核表", 137);
        buf = comcr.insertStr(buf, "列印頁數 :", 112);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 125);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "幣        別 :TWD", 10);
        buf = comcr.insertStr(buf, "(謹送卡務作業)", 114);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, " 處理日期 ", 1);
        buf = comcr.insertStr(buf, "帳戶帳號 ", 13);
        buf = comcr.insertStr(buf, "繳款卡號", 30);
        buf = comcr.insertStr(buf, "中文姓名", 55);
        buf = comcr.insertStr(buf, "繳款日期", 85);
        buf = comcr.insertStr(buf, "繳款金額", 100);
        buf = comcr.insertStr(buf, "繳款途徑", 118);

        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        for (int i = 0; i < 136; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void updateActPayDetail() throws Exception {
        daoTable = "act_pay_detail";
        updateSQL = "proc_mark  = 'Y',";
        updateSQL += " id_p_seqno  = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_user  = ?,";
        updateSQL += " mod_pgm   = ?";
        whereStr = "where rowid = ? ";
        setString(1, hAcnoIdPSeqno);
        setString(2, javaProgram);
        setString(3, javaProgram);
        setRowId(4, hApdlRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_pay_detail not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void printNoDetail() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "本日無資料列印！！", 55);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
        buf = "";
        buf = comcr.insertStr(buf, "主管:                                                  經辦:", 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void printFooter() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "筆數:", 1);
        szTmp = String.format("%6d", totalCnt);
        /* 合計 */
        szTmp = comcr.commFormat("2$,3$,3$,3$", pageAmt);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "主管:                                                  經辦:", 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
        pageAmt = 0;
    }

    /*****************************************************************************/
    void lp_rtn(String lstr) throws Exception {
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
            comcr.errRtn("select_TABLE_NAME not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPrintName = getValue("print_name");
        }

        if (hPrintName.length() > 0) { /* 當表單代號及對應印表機存在bil_rpt_prt時才印 */
            lpStr = String.format("lp -d %s %s", hPrintName, temstr);
            // comc.systemCmd(lp_str);
        } else {
            showLogMessage("I", "", String.format("[print_name] not fund? check bil_rpt_prt table "));
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActE001 proc = new ActE001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
