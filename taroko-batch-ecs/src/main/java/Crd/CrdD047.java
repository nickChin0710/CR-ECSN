/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/09/09  V1.00.01    Wilson    bug修正                                                                                                    *
*  109/10/15  V1.00.02    Wilson    營業日改系統日                                                                                          *
*  109-10-19  V1.00.03    shiyuqi       updated for project coding standard     *
*  112/08/04  V1.00.04    Wilson    停用原因碼調整                                                                                         *
*  112/10/16  V1.00.05    Wilson    不為該月一日不執行                                                                                  *
******************************************************************************/

package Crd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.*;

public class CrdD047 extends AccessDAO {
    private String progname = "信用卡效期到期申停處理  112/10/16 V1.00.05";

    CommFunction comm = new CommFunction();
    CommCrd         comc = new CommCrd();
    CommRoutine     comr = null;
    CommCrdRoutine comcr = null;

    int debug = 0;
    int degugd = 0;
    int tmpInt = 0;
    long totalCnt = 0;
    String hTempUser = "";
    String hFirstDay = "";
    String hProcDate = "";

    String rptName1 = "";
    int recordCnt = 0;
    int actCnt = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq1 = 0;
    int errCnt = 0;
    String ErrMsg = "";
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

    String hSysdate = "";
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
    String hCardExpireChgFlag = "";
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
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : CrdD047 [yyyymm]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hFirstDay = "";
            sqlCmd = "select to_char(sysdate,'yyyymm')||'01' h_first_date,";
            sqlCmd += "to_char(sysdate,'yyyymmdd') h_proc_date ";
            sqlCmd += " from dual ";
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hFirstDay = getValue("h_first_date");
                hProcDate = getValue("h_proc_date");
                hSysdate = hProcDate;
            }
            if (args.length == 0) {
                if (!hFirstDay.equals(hProcDate)) {
                	showLogMessage("I", "", String.format("今天[%s]不為本月一號,不需跑程式，程式執行結束", hProcDate));                  
                	finalProcess();                  
                	return 0;              	
                }
            }
            
            if (args.length == 1) {
                if (args[0].length() != 6) {
                    comcr.errRtn("Usage : CrdD047 [yyyymm]", "", hCallBatchSeqno);
                }
                hProcDate = String.format("%6.6s01", args[0]);
            }

            showLogMessage("I", "", String.format("執行日期=[%s] ", hProcDate));
            
            hModUser = comc.commGetUserID();

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
    void selectCrdCard() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.new_card_no,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.CURRENT_CODE,";
        sqlCmd += "a.OPPOST_REASON,";
        sqlCmd += "a.OPPOST_DATE,";
        sqlCmd += "decode(a.combo_indicator,'','N',a.combo_indicator) h_card_combo_indicator,";
        sqlCmd += "a.sup_flag,";
        sqlCmd += "a.corp_no,";
        sqlCmd += "a.corp_no_code,";
        sqlCmd += "a.card_type,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.group_code,";
        sqlCmd += "a.mail_type,";
        sqlCmd += "a.mail_branch,";
        sqlCmd += "a.mail_no,";
        sqlCmd += "a.mail_proc_date,";
        sqlCmd += "a.expire_reason,";
        sqlCmd += "a.expire_chg_flag,";
        sqlCmd += "a.new_end_date,";
        sqlCmd += "a.electronic_code,";
        sqlCmd += "a.bin_type,";
        sqlCmd += "a.lost_fee_code,";
        sqlCmd += "b.id_no        as id,";
        sqlCmd += "b.id_no_code   as id_code,";
        sqlCmd += "b.birthday     as birthday,";
        sqlCmd += "b.chi_name     as chi_name,";
        sqlCmd += "c.id_no        as major_id,";
        sqlCmd += "c.id_no_code   as major_id_code,";
        sqlCmd += "c.birthday     as major_birthday,";
        sqlCmd += "c.chi_name     as major_chi_name,";
        sqlCmd += "a.rowid  as rowid ";
        sqlCmd += " from crd_idno c, crd_idno b, crd_card a ";
        sqlCmd += "where a.new_end_date < ?   ";
        sqlCmd += "  and a.current_code = '0' ";
        sqlCmd += "  and b.id_p_seqno   = a.id_p_seqno ";
        sqlCmd += "  and c.id_p_seqno   = a.id_p_seqno ";

        setString(1, hProcDate);

        recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCardCardNo         = getValue("card_no", i);
            hCardNewCardNo     = getValue("new_card_no", i);
            hCardPSeqno         = getValue("acno_p_seqno", i);
            hCardCurrentCode    = getValue("CURRENT_CODE", i);
            hCardOppostReason   = getValue("OPPOST_REASON", i);
            hCardOppostDate     = getValue("OPPOST_DATE", i);
            hCardComboIndicator = getValue("h_card_combo_indicator", i);
            hCardSupFlag        = getValue("sup_flag", i);
            hCardId              = getValue("id", i);
            hCardIdCode         = getValue("id_code", i);
            hCardBirthday        = getValue("birthday", i);
            hCardChiName        = getValue("chi_name", i);
            hCardMajorId        = getValue("major_id", i);
            hCardMajorIdCode   = getValue("major_id_code", i);
            hCardMajorBirthday  = getValue("major_birthday", i);
            hCardMajorChiName  = getValue("major_chi_name", i);
            hCardCorpNo         = getValue("corp_no", i);
            hCardCorpNoCode    = getValue("corp_no_code", i);
            hCardCardType       = getValue("card_type", i);
            hCardAcctType       = getValue("acct_type", i);
            hCardGroupCode      = getValue("group_code", i);
            hCardMailType       = getValue("mail_type", i);
            hCardMailBranch     = getValue("mail_branch", i);
            hCardMailNo         = getValue("mail_no", i);
            hCardMailProcDate  = getValue("mail_proc_date", i);
            hCardExpireReason   = getValue("expire_reason", i);
            hCardExpireChgFlag = getValue("expire_chg_flag", i);
            hCardRowid           = getValue("rowid", i);

            totalCnt++;
            if (debug == 1)
                showLogMessage("I", "", "Read card=[" + hCardCardNo + "]" + totalCnt);

            updateCrdCard();
            
            insertOnbat();

            //process_apscard();
            
            /* 20150413 三合一卡 */
            if (!hCardComboIndicator.equals("N")) {
                insertCrdStopLog();
            }
            tmpInt = checkCrJcic();
            if (debug == 1)
                showLogMessage("I", "", "  check jcic=[" + tmpInt + "]");
            if (tmpInt == 0) {
                insertCrdJcic();
            } else {
                updateCrdJcic();
            }
        }
    }

    /***********************************************************************/
    void updateCrdCard() throws Exception {
        hOppostReason = "B3";
        if (hCardExpireChgFlag.compareTo("0") > 0) {
            switch (Integer.parseInt(hCardExpireChgFlag)) {
            case 1:
            	hOppostReason = "B3";
                break;
            case 2:
            	hOppostReason = "B1";
                break;
            case 3:
            	hOppostReason = "B2";
                break;
            }
        }

        daoTable = "crd_card";
        updateSQL = " oppost_reason = ?,";
        updateSQL += " oppost_date   = ?,";
        updateSQL += " current_code  = '1',";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_pgm       = ?";
        whereStr = "where rowid      = ? ";
        setString(1, hOppostReason);
        setString(2, hSysdate);
        setString(3, hCardModPgm);
        setRowId(4, hCardRowid);
        actCnt = updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_card not found!", hCardRowid, comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertOnbat() throws Exception {
        setValue("TRANS_TYPE"       , "6");
        setValueInt("TO_WHICH"      , 2);
        setValue("DOG"              , sysDate + sysTime);
        setValue("DOP"              , "");
        setValue("PROC_MODE"        , "B");
        setValueInt("PROC_STATUS"   , 0);
        setValue("CARD_NO"          , hCardCardNo);
        setValue("OPP_TYPE"         , "1");
        setValue("OPP_REASON"       , "R3");
        setValue("OPP_DATE"         , sysDate);
        daoTable = "onbat_2ccas";
        actCnt = insertTable();
        if(dupRecord.equals("Y")) {
           comcr.errRtn("insert_onbat_2ccas duplicate!", hCardCardNo, comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void processApscard() throws Exception {
        String hRowid = "";

        hApscStopReason = "3";
        hApscStatusCode = "";

        hApscStopDate = hSysdate;

        sqlCmd = "select rowid as rowid";
        sqlCmd += " from crd_apscard  ";
        sqlCmd += "where card_no     = ?  ";
        sqlCmd += "  and to_aps_date = '' ";

        setString(1, hCardCardNo);

        tmpInt = selectTable();
        if (tmpInt > 0) {
            hRowid = getValue("rowid");

            daoTable   = "crd_apscard";
            updateSQL  = " stop_reason = ?,";
            updateSQL += " stop_date   = ?,";
            updateSQL += " status_code = ?,";
            updateSQL += " mod_time    = sysdate,";
            updateSQL += " mod_pgm     = ? ";
            whereStr   = "where rowid  = ? ";
            setString(1, hApscStopReason);
            setString(2, hApscStopDate);
            setString(3, hApscStatusCode);
            setString(4, javaProgram);
            setRowId(5, hRowid);
            actCnt = updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_apscard not found!", hCardCardNo, comcr.hCallBatchSeqno);
            }
        } else {
            insertApscard();
        }
    }

    /***********************************************************************/
    void insertApscard() throws Exception {

        hApscCardNo     = hCardCardNo;
        hApscValidDate  = hSysdate;
        hApscPmId       = hCardMajorId;
        hApscPmIdCode  = hCardMajorIdCode;
        hApscPmBirthday = hCardMajorBirthday;
        hApscPmName     = hCardMajorChiName;

        if (!hCardSupFlag.substring(0, 1).equals("0")) {
            hApscSupLostStatus = "0";
            hApscSupId          = hCardId;
            hApscSupIdCode     = hCardIdCode;
            hApscSupBirthday    = hCardBirthday;
            hApscSupName        = hCardChiName;
        }

        hApscCorpNo      = hCardCorpNo;
        hApscCorpNoCode = hCardCorpNoCode;
        hApscCardType    = hCardCardType;
        hApscGroupCode   = hCardGroupCode;
        hApscMailType    = hCardMailType;
        hApscMailBranch  = hCardMailBranch;
        hApscMailNo      = hCardMailNo;
        hApscMailDate    = hCardMailProcDate;

        setValue("crt_datetime"   , sysDate + sysTime);
        setValue("card_no"        , hApscCardNo);
        setValue("valid_date"     , hApscValidDate);
        setValue("stop_date"      , hApscStopDate);
        setValue("reissue_date"   , hApscReissueDate);
        setValue("stop_reason"    , hApscStopReason);
        setValue("mail_type"      , hApscMailType);
        setValue("mail_no"        , hApscMailNo);
        setValue("mail_branch"    , hApscMailBranch);
        setValue("mail_date"      , hApscMailDate);
        setValue("pm_id"          , hApscPmId);
        setValue("pm_id_code"     , hApscPmIdCode);
        setValue("pm_birthday"    , hApscPmBirthday);
        setValue("sup_id"         , hApscSupId);
        setValue("sup_id_code"    , hApscSupIdCode);
        setValue("sup_birthday"   , hApscSupBirthday);
        setValue("corp_no"        , hApscCorpNo);
        setValue("corp_no_code"   , hApscCorpNoCode);
        setValue("card_type"      , hApscCardType);
        setValue("pm_name"        , hApscPmName);
        setValue("sup_name"       , hApscSupName);
        setValue("sup_lost_status", hApscSupLostStatus);
        setValue("status_code"    , hApscStatusCode);
        setValue("group_code"     , hApscGroupCode);
        setValue("mod_time"       , sysDate + sysTime);
        setValue("mod_pgm"        , javaProgram);
        daoTable = "crd_apscard";
        actCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_apscard duplicate!",hCardCardNo, comcr.hCallBatchSeqno);
        }
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
        setValue("oppost_date"  , hSysdate);
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
    int checkCrJcic() throws Exception {
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
        setValue("CURRENT_CODE" , "1");
        setValue("OPPOST_REASON", "B3");
   //   setValue("OPPOST_DATE"  , h_card_oppost_date);
        setValue("OPPOST_DATE"  , hSysdate);
        setValue("MOD_TIME"     , sysDate + sysTime);
        setValue("MOD_PGM"      , javaProgram);
        daoTable = "crd_jcic";
        actCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_jcic duplicate!", hCardCardNo, comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateCrdJcic() throws Exception {
        daoTable   = "crd_jcic";
        updateSQL  = " CURRENT_CODE  = ?,";
        updateSQL += " OPPOST_REASON = ?,";
        updateSQL += " OPPOST_DATE   = ?,";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_pgm       = ?";
        whereStr   = "where card_no  = ? ";
        setString(1, "1");
        setString(2, "B3");
        setString(3, hSysdate);
        setString(4, hCardModPgm);
        setString(5, hCardCardNo);
        actCnt = updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_jcic not found!", hCardCardNo, comcr.hCallBatchSeqno);
        }
    }
    

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdD047 proc = new CrdD047();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
