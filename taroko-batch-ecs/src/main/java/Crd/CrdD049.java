/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/01/13  V1.00.00    Rou         Initial(Reference CrdD047 program)      *
*  109/12/21  V1.00.01   shiyuqi       updated for project coding standard    *
*  112/07/11  V1.00.02   Wilson       update crd_return條件增加退卡編號                                 *
*  112/10/17  V1.00.03   Wilson       調整為判斷退卡日期                                                                            *
*  112/12/27  V1.00.04   Wilson       調整為每月最後一天執行                                                                     *
******************************************************************************/

package Crd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.*;

public class CrdD049 extends AccessDAO {
    private String progname = "信用卡退卡180天批次停卡程式  112/12/27  V1.00.04 ";

    CommFunction comm = new CommFunction();
    CommCrd         comc = new CommCrd();
    CommRoutine     comr = null;
    CommCrdRoutine comcr = null;

    int debug = 1;
    int debugD = 1;
    int tmpInt = 0;
    long totalCnt = 0;
    String hTempUser = "";

    String rptName1 = "";
    int recordCnt = 0;
    int actCnt = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq1 = 0;
    int errCnt = 0;
    String errmsg = "";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hModWs = "";
    String hModLog = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String iPostDate = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    String hCurpModWs = "";
    long hCurpModSeqno = 0;
    String hCurpModLog = "";
    String hCallRProgramCode = "";

    String hBusiBusinessDate = "";
    String hCardCardNo = "";
    String hCardNewCardNo = "";
    String hCardPSeqno = "";
    String hCardCurrentCode = "";
    String hCardOppostReason = "";
    String hCardOppostDate = "";
    String hCardComboIndicator = "";
    String hCardSupFlag = "";
    String hCardId = "";
    String hCardIdCode = "";
    String hCardBirthday = "";
    String hCardChiName = "";
    String hCardMajorId = "";
    String hCardMajorIdCode = "";
    String hCardMajorBirthday = "";
    String hCardMajorChiName = "";
    String hCardCorpNo = "";
    String hCardCorpNoCode = "";
    String hCardCardType = "";
    String hCardAcctType = "";
    String hCardGroupCode = "";
    String hCardMailType = "";
    String hCardMailBranch = "";
    String hCardMailNo = "";
    String hCardMailProcDate = "";
    String hCardExpireReason = "";
    String hCardReturnSeqno = "";
    String hCardRowid = "";
    String hMoveStatusOld = "";
    String hOppostReason = "";
    String hCardModUser = "";
    String hCardModPgm = "";
    int hCount = 0;
    String hAcnoRcUseIndicator = "";
    String hCardModWs = "";
    String hProcSeqno = "";
    String hAcctType = "";
    String hType = "";
    String hRowid = "";
    String hApscStopReason = "";
    String hApscStopDate = "";
    String hApscStatusCode = "";
    String hApscPmBirthday = "";
    String hApscPmName = "";
    String hApscPmId = "";
    String hApscPmIdCode = "";
    String hApscSupId = "";
    String hApscSupIdCode = "";
    String hApscCardNo = "";
    String hApscValidDate = "";
    String hApscReissueDate = "";
    String hApscMailType = "";
    String hApscMailNo = "";
    String hApscMailBranch = "";
    String hApscMailDate = "";
    String hApscSupBirthday = "";
    String hApscCorpNo = "";
    String hApscCorpNoCode = "";
    String hApscCardType = "";
    String hApscSupName = "";
    String hApscSupLostStatus = "";
    String hApscGroupCode = "";
    String hEndDate = "";
    String hOppostMonth = "";
    String hLastDay = "";
    
    // ************************************************************************

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
                comc.errExit("Usage : CrdD049 [date] [callbatch_seqno]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                comcr.hCallBatchSeqno = "no-call";
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

            hModUser = comc.commGetUserID();
            hCardModUser = hModUser;
            hCardModWs = "ECS_SERVER";
            hCardModPgm = javaProgram;

            if (args.length > 2) {
                String err1 = "CrdD049 [date] [seq_no]\n";
                comcr.errRtn("CrdD049 [date] [seq_no] ", "", comcr.hCallBatchSeqno);
            }

            if (args.length > 0) {
                if (args[0].length() == 8) {
                    hBusiBusinessDate = args[0];
                }
            }
            
            getBusinessDay();

            showLogMessage("I", "", "執行日期 = [" + hBusiBusinessDate + "]");
            
            hLastDay = hEndDate;
            
            if (!hBusiBusinessDate.equals(hLastDay)) {
        		showLogMessage("E", "", "執行日期不為該月最後一天,不執行此程式");
        		return 0;
            }

            selectCrdCard();

            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void getBusinessDay() throws Exception {

    	sqlCmd = "select to_char(sysdate,'yyyymmdd') as business_date ";
        sqlCmd += " from ptr_businday ";
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                                 : hBusiBusinessDate;
        }
        
        sqlCmd = "select to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date ";
        sqlCmd += "    , to_char(add_months(to_date(?,'yyyymmdd'),-6),'yyyymm') h_oppost_month ";
        sqlCmd += " from dual ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);

        recordCnt = selectTable();
        if(recordCnt > 0) {
           hEndDate = getValue("h_end_date");  
           hOppostMonth = getValue("h_oppost_month");           
        }
    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {
    	
    	//Get 180 days ago
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//		Date dateNow = sdf.parse(sysDate);
//		Date getDate = new Date();
//		Calendar calendar = Calendar.getInstance(); 
//		calendar.setTime(dateNow);
//		calendar.add(Calendar.DATE, -180);
//		getDate = calendar.getTime();		
//		String h180daysAgo = sdf.format(getDate);
		
//		showLogMessage("I", "", "前180天 = [" + h180daysAgo + "]");
		showLogMessage("I", "", "前180天 = [" + hOppostMonth + "]");
    	
        sqlCmd = "select ";
        sqlCmd += "b.card_no,";
        sqlCmd += "b.new_card_no,";
        sqlCmd += "b.acno_p_seqno,";
        sqlCmd += "b.issue_date,";
        sqlCmd += "b.reissue_date,";
        sqlCmd += "b.change_date,";
        sqlCmd += "b.current_code,";
        sqlCmd += "b.oppost_reason,";
        sqlCmd += "b.oppost_date,";
        sqlCmd += "decode(b.combo_indicator,'','N',b   .combo_indicator) h_card_combo_indicator,";
        sqlCmd += "b.sup_flag,";
        sqlCmd += "b.corp_no,";
        sqlCmd += "b.corp_no_code,";
        sqlCmd += "b.card_type,";
        sqlCmd += "b.acct_type,";
        sqlCmd += "b.group_code,";
        sqlCmd += "b.mail_type,";
        sqlCmd += "b.mail_branch,";
        sqlCmd += "b.mail_no,";
        sqlCmd += "b.mail_proc_date,";
        sqlCmd += "b.expire_reason,";
        sqlCmd += "a.proc_status,";
        sqlCmd += "a.return_date, ";
        sqlCmd += "a.return_seqno ";
        sqlCmd += " from crd_return a, crd_card b ";
        sqlCmd += " where a.card_no = b.card_no ";
        sqlCmd += " and a.return_date like ? ";   
        sqlCmd += " and b.current_code = '0' ";
        sqlCmd += " and a.proc_status in ('1', '2', '7') ";
        sqlCmd += " order by b.issue_date ";

        setString(1, hOppostMonth + '%');        

        recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCardCardNo = getValue("card_no", i);
            hCardNewCardNo = getValue("new_card_no", i);
            hCardPSeqno = getValue("acno_p_seqno", i);
            hCardCurrentCode = getValue("current_code", i);
            hCardOppostReason = getValue("oppost_reason", i);
            hCardOppostDate = getValue("oppost_date", i);
            hCardComboIndicator = getValue("h_card_combo_indicator", i);
            hCardSupFlag = getValue("sup_flag", i);
            hCardId = getValue("id", i);
            hCardIdCode = getValue("id_code", i);
            hCardBirthday = getValue("birthday", i);
            hCardChiName = getValue("chi_name", i);
            hCardMajorId = getValue("major_id", i);
            hCardMajorIdCode = getValue("major_id_code", i);
            hCardMajorBirthday = getValue("major_birthday", i);
            hCardMajorChiName = getValue("major_chi_name", i);
            hCardCorpNo = getValue("corp_no", i);
            hCardCorpNoCode = getValue("corp_no_code", i);
            hCardCardType = getValue("card_type", i);
            hCardAcctType = getValue("acct_type", i);
            hCardGroupCode = getValue("group_code", i);
            hCardMailType = getValue("mail_type", i);
            hCardMailBranch = getValue("mail_branch", i);
            hCardMailNo = getValue("mail_no", i);
            hCardMailProcDate = getValue("mail_proc_date", i);
            hCardExpireReason = getValue("expire_reason", i);
            hCardReturnSeqno = getValue("return_seqno", i);

            totalCnt++;
            if (debug == 1)
                showLogMessage("I", "", "Read card=[" + hCardCardNo + "]" + totalCnt);

            updateCrdCard();

            /* 20150413 三合一卡 */
            if (!hCardComboIndicator.equals("N")) {
                insertCrdStopLog();
            }
            tmpInt = checkCrdJcic();
            if (debug == 1)
                showLogMessage("I", "", "  check jcic=[" + tmpInt + "]");
            if (tmpInt == 0) {
                insertCrdJcic();
            } else {
                updateCrdJcic();
            }
            updateCrdReturn();
        }
    }

    /***********************************************************************/
    void updateCrdCard() throws Exception {          
        daoTable = "crd_card";
        updateSQL =  " oppost_reason = 'ED', ";
        updateSQL += " oppost_date   = ?, ";
        updateSQL += " current_code  = '4', ";
        updateSQL += " mod_time      = sysdate, ";
        updateSQL += " mod_pgm       = ? ";
        whereStr = "where card_no    = ? ";
        setString(1, sysDate);
        setString(2, hCardModPgm);
        setString(3, hCardCardNo);
        actCnt = updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_card not found!", hCardRowid, comcr.hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
    void updateCrdReturn() throws Exception {          
        daoTable = "crd_return";
        updateSQL += " proc_status   = '4', ";
        updateSQL += " mod_user      = 'batch', ";
        updateSQL += " mod_time      = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
        updateSQL += " mod_pgm       = ? ";
        whereStr   = " where card_no      = ? ";
        whereStr  += "and return_seqno = ? ";
        whereStr  += " and proc_status in ('1', '2', '7') ";
        setString(1, sysDate + sysTime);
        setString(2, hCardModPgm);
        setString(3, hCardCardNo);
        setString(4, hCardReturnSeqno);
        actCnt = updateTable();
    }     

    /**********************************************************************/
    void insertCrdStopLog() throws Exception {
        String hProcSeqno = "";

        hProcSeqno = "";
        sqlCmd  = "select substr(to_char(ecs_stop.nextval,'0000000000'),2,10) h_proc_seqno ";
        sqlCmd += " from dual ";
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hProcSeqno = getValue("h_proc_seqno");
        }

        hType = "01";
        if (hCardNewCardNo.length() > 0) {
            hAcctType = "";
            sqlCmd  = "select acct_type ";
            sqlCmd += " from crd_card  ";
            sqlCmd += "where card_no = ? ";
            setString(1, hCardNewCardNo);
            tmpInt = selectTable();
            if (tmpInt > 0) {
                hAcctType = getValue("acct_type");
            }
            if (!hCardAcctType.equals(hAcctType)) {
                hType = "12";
            }
        } else { /* 不轉 (05) 不續 (無新卡號) */
            if (hCardAcctType.equals("05"))
                hType = "12";
        }

        hType = "12";
        if(hCardExpireReason.equals("z1") || hCardExpireReason.equals("zz") ||
           hCardExpireReason.equals("ZZ") || hCardExpireReason.equals("Z1")) {
            hType = "08";
        }

        setValue("proc_seqno"   , hProcSeqno);
        setValue("crt_time"     , sysDate + sysTime);
        setValue("card_no"      , hCardCardNo);
        setValue("current_code" , "1");
        setValue("oppost_reason", "R3");
        setValue("oppost_date"  , sysDate);
        setValue("trans_type"   , hType);
        setValue("send_type"    , "2");
        setValue("stop_source"  , "2");
        setValue("mod_time"     , sysDate + sysTime);
        setValue("mod_pgm"      , hCardModPgm);
        daoTable = "crd_stop_log";
        actCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_stop_log duplicate!", hCardCardNo
                                                              , comcr.hCallBatchSeqno);
        }
    }
    /***********************************************************************/
    int checkCrdJcic() throws Exception {
        int hCount = 0;

        sqlCmd = "select count(*) h_count ";
        sqlCmd += " from crd_jcic  ";
        sqlCmd += "where card_no      = ?  ";
        sqlCmd += "  and trans_type  != 'A'  ";
        sqlCmd += "  and to_jcic_date = '' ";
        setString(1, hCardCardNo);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hCount = getValueInt("h_count");
        }

        if (hCount > 0)
            return 1;
        else
            return 0;
    }

    /***********************************************************************/
    void insertCrdJcic() throws Exception {
        hAcnoRcUseIndicator = "";
        sqlCmd = "select rc_use_indicator ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno  = ?  ";
        setString(1, hCardPSeqno);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hAcnoRcUseIndicator = getValue("rc_use_indicator");
        }

        setValue("CARD_NO"      , hCardCardNo);
        setValue("CRT_DATE"     , sysDate);
        setValue("CRT_USER"     , javaProgram);
        setValue("TRANS_TYPE"   , "C");
        setValue("IS_RC"        , hAcnoRcUseIndicator);
        setValue("current_code" , "1");
        setValue("oppost_reason", "B3");
        setValue("oppost_date"  , sysDate);
        setValue("mod_time"     , sysDate + sysTime);
        setValue("mod_pgm"      , javaProgram);
        daoTable = "crd_jcic";
        actCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_jcic duplicate!", hCardCardNo, comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateCrdJcic() throws Exception {
        daoTable   = "crd_jcic";
        updateSQL  = " current_code  = ?,";
        updateSQL += " oppost_reason = ?,";
        updateSQL += " oppost_date   = ?,";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_pgm       = ?";
        whereStr   = "where card_no  = ? ";
        setString(1, "1");
        setString(2, "B3");
        setString(3, sysDate);
        setString(4, hCardModPgm);
        setString(5, hCardCardNo);
        actCnt = updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_jcic not found!", hCardCardNo, comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdD049 proc = new CrdD049();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
