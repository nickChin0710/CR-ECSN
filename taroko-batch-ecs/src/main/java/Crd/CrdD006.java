/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  106/06/01  V1.00.00    Edson     program initial                          *
*  106/12/07  V1.00.01    SUP       error correction                         *
*  107/05/22  V1.11.01    黃繼民    RECS-s1070502-034 新增制裁名單reject code*
*  107/06/15  V1.11.02    黃繼民    RECS-s1070502-034 制裁名單改善SQL效能    *
*  107/11/12  V1.12.01    吳伯榮    RECS-s1070522-042 英文名檢核reject code  *
*  108/01/21  V1.13.01    Lai       RECS-1081108-003 ICH                     *
*  108/01/25  V1.13.01    Brian     transfer to java                         *
*  109/02/20  V1.14.01    Wilson    修正insert crd_nccc_stat抓錯欄位                                   *
*  109/04/21  V1.15.01    Wilson    刪除reject_code                            *
* 109/12/18  V1.00.02   shiyuqi       updated for project coding standard   **
*  110/01/22  V1.17.01    Wilson    017 change to 006                        *
*  112/03/10  V1.17.02    Wilson    票證調整為判斷electronic_code                *
*  112/04/14  V1.17.03    Wilson    讀參數判斷是否由新系統編列票證卡號                                                 *
*  112/05/24  V1.17.04    Wilson    調整effc_date值                                                                             *
*  112/07/13  V1.17.05    Wilson    mark insertCrdProcTmp1                   *
*  112/12/03  V1.17.06    Wilson    crd_item_unit不判斷卡種                                                          *
*****************************************************************************/
package Crd;


import com.*;

/*製卡回饋資料轉入處理*/
public class CrdD006 extends AccessDAO {
    public static final boolean debugMode = true;
    private String progname = "製卡回饋資料轉入處理  112/12/03  V1.17.06  ";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;

    String prgmId = "CrdD006";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hTempUser = "";
    String hChiDate = "";
    String hCardType = "";
    String hBinNo = "";
    String hDembRejectCode = "";
    String hhBatchno = "";
    long hhRecno = 0;
    String xMbosCardNo = "";
    String xMbosCardType = "";
    String xMbosGroupCode = "";
    String xMbosUnitCode = "";
    String xMbosEmbossSource = "";
    String xMbosEmbossReason = "";
    String hComboRowid = "";
    String hDembCardNo = "";
    String hMbosEmbossSource = "";
    String hMbosEmbossReason = "";
    String hMbosCardNo = "";
    String hMbosUnitCode = "";
    int count = 0;
    String hhRowid = "";
    String hEmbossResult = "";
    long hUpperLmt = 0;
    long hUpperLmtAcmm = 0;
    double hSeqnoCurrent = 0;
    String hRowid = "";
    String hTempX09 = "";
    String hTempX10 = "";
    String hTempX16 = "";
    String hIpsKind = "";
    String hTempX11 = "";
    String hTsccFlag = "";
    String hIpsFlag = "";
    String hIchFlag = "";
    String hMbosRowid = "";
    String hMbosBatchno = "";
    String hMbosRecno = "";
    String hMbosMajorCardNo = "";
    String hMbosApplyId = "";
    String hMbosApplyIdCode = "";
    String hMbosPmId = "";
    String hMbosPmIdCode = "";
    String hMbosSourceCode = "";
    String hMbosBirthday = "";
    String hMbosNcccType = "";
    String hMbosIcFlag = "";
    String hMbosChiName = "";
    String hMbosVendor = "";
    String hMbosCorpNo = "";
    String hMbosCorpNoCode = "";
    String hMbosRejectCode = "";
    String hMbosCardType = "";
    String hMbosGroupCode = "";
    String hCallRProgramCode = "";
    String hDembModUser = "";
    String hDembModWs = "";
    String hDembModPgm = "";
    String hMbosValidTo = "";
    String hMbosToNcccDate ="";
    String hMbosAcctKey ="";
    String hMbosEngName = "";
    String hMbosElectronicCode = "";
    String tmpWfValue = "";
    int chkDig = 0;
    int errFlag = 0;
    int hNewFailCnt = 0;
    int hNewSucCnt = 0;
    int hSucRecVendorCnt = 0;
    int hFailRecVendorCnt = 0;
    int hVendorRecordCnt = 0;
    int embossOk = 0;
    String hBusiBusinessDate = "";
    int hSeqnoIch = 0;
    String ichCardNo = "";
    String hCardCode = "";
// ********************************************************************************
public int mainProcess(String[] args) 
{
 try
  {
   // ====================================
   // 固定要做的
   dateTime();
   setConsoleMode("Y");
   javaProgram = this.getClass().getName();
   showLogMessage("I", "", javaProgram + " " + progname);
   // =====================================
   if (args.length > 2) {
       comc.errExit("Usage : CrdD006 [callbatch_seqno]", "");
   }

   // 固定要做的

   if (!connectDataBase()) {
       comc.errExit("connect DataBase error", "");
   }

   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
   comcr.hCallBatchSeqno = hCallBatchSeqno;

   String checkHome = comc.getECSHOME();
   if (comcr.hCallBatchSeqno.length() > 6) {
       if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
           comcr.hCallBatchSeqno = "no-call";
       }
   }
   comcr.hCallRProgramCode = this.getClass().getName();
   hTempUser = "";
   if (comcr.hCallBatchSeqno.length() == 20) {
       comcr.hCallParameterData = javaProgram;
       for (int i = 0; i < args.length; i++) {
           comcr.hCallParameterData = comcr.hCallParameterData + " " + args[i];
       }
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
   
   hBusiBusinessDate = "";
   sqlCmd = " select business_date from ptr_businday";
   if (selectTable() > 0)
       hBusiBusinessDate = getValue("business_date");
   else
       comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);

   hChiDate = "";
   sqlCmd = "select to_char(to_number(to_char(sysdate,'yyyymmdd'))-19110000) h_chi_date ";
   sqlCmd += " from dual ";
   int recordCnt = selectTable();
   if (recordCnt > 0) {
       hChiDate = getValue("h_chi_date");
   }
   hModUser = comc.commGetUserID();
   hDembModUser = hModUser;
   hDembModPgm = javaProgram;

   hNewFailCnt = 0;
   hNewSucCnt = 0;

   processVendor();

   commitDataBase();

   // ==============================================
   // 固定要做的

   comcr.hCallErrorDesc = "程式執行結束,筆數=[" + hVendorRecordCnt + "]";
   showLogMessage("I", "", comcr.hCallErrorDesc);

   if (comcr.hCallBatchSeqno.length() == 20)
       comcr.callbatch(1, 0, 1); // 1: 結束

   finalProcess();
   return 0;
  } catch (Exception ex) 
      { expMethod = "mainProcess";
        expHandle(ex);
        return exceptExit;
      }
}
/***********************************************************************/
    void processVendor() throws Exception {
        int stRtn = 0;

        sqlCmd = "select ";
        sqlCmd += " a.batchno,";
        sqlCmd += " a.recno,";
        sqlCmd += " a.emboss_source,";
        sqlCmd += " a.emboss_reason,";
        sqlCmd += " a.card_no,";
        sqlCmd += " a.major_card_no,";
        sqlCmd += " a.apply_id,";
        sqlCmd += " decode(a.apply_id_code, '', '0', a.apply_id_code) h_mbos_apply_id_code,";
        sqlCmd += " a.pm_id,";
        sqlCmd += " decode(a.pm_id_code   , '', '0', a.pm_id_code)    h_mbos_pm_id_code,";
        sqlCmd += " a.group_code,";
        sqlCmd += " a.source_code,";
        sqlCmd += " a.card_type,";
        sqlCmd += " a.birthday,";
        sqlCmd += " a.rowid rowid,";
        sqlCmd += " a.nccc_type,";
        sqlCmd += " a.ic_flag,";
        sqlCmd += " a.chi_name,";
        sqlCmd += " a.vendor,";
        sqlCmd += " a.corp_no,";
        sqlCmd += " a.corp_no_code,";
        sqlCmd += " a.reject_code,";
        sqlCmd += " a.unit_code,";
        sqlCmd += " a.to_nccc_date,";
        sqlCmd += " a.acct_key,";
        sqlCmd += " a.eng_name, ";
        sqlCmd += " a.electronic_code, ";
        sqlCmd += " a.valid_to ";
        sqlCmd += " from crd_emboss a ";
        sqlCmd += "where a.card_no       != '' ";
        sqlCmd += "  and a.in_main_date   = '' ";
        sqlCmd += "  and a.to_nccc_date  != '' ";
        sqlCmd += "  and a.rtn_nccc_date  = '' ";
        sqlCmd += "  and a.chk_nccc_flag  = 'Y' ";
        sqlCmd += "  and a.reject_code    = '' ";
        sqlCmd += "  and a.nccc_filename != '' ";
        sqlCmd += "order by a.card_type, a.group_code, a.batchno, a.recno ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hMbosBatchno = getValue("batchno");
            hMbosRecno = getValue("recno");
            hMbosEmbossSource = getValue("emboss_source");
            hMbosEmbossReason = getValue("emboss_reason");
            hMbosCardNo = getValue("card_no");
            hMbosMajorCardNo = getValue("major_card_no");
            hMbosApplyId = getValue("apply_id");
            hMbosApplyIdCode = getValue("h_mbos_apply_id_code");
            hMbosPmId = getValue("pm_id");
            hMbosPmIdCode = getValue("h_mbos_pm_id_code");
            hMbosGroupCode = getValue("group_code");
            hMbosSourceCode = getValue("source_code");
            hMbosCardType = getValue("card_type");
            hMbosBirthday = getValue("birthday");
            hMbosRowid = getValue("rowid");
            hMbosNcccType = getValue("nccc_type");
            hMbosIcFlag = getValue("ic_flag");
            hMbosChiName = getValue("chi_name");
            hMbosVendor = getValue("vendor");
            hMbosCorpNo = getValue("corp_no");
            hMbosCorpNoCode = getValue("corp_no_code");
            hMbosRejectCode = getValue("reject_code");
            hMbosUnitCode = getValue("unit_code");
            hMbosToNcccDate = getValue("to_nccc_date");
            hMbosAcctKey = getValue("acct_key");
            hMbosEngName = getValue("eng_name");
            hMbosElectronicCode = getValue("electronic_code");
            hMbosValidTo = getValue("valid_to");

            hVendorRecordCnt++;

            stRtn = checkRejectCode();
            proc_crd_emboss3();

if(debug == 1)
  {
   showLogMessage("I","", "888 read="+ hMbosCardNo +","+ hMbosIcFlag +","+ hMbosEmbossSource);
   showLogMessage("I","", "    st_rtn="+stRtn+","+ hMbosGroupCode +","+ hMbosUnitCode);
  }
            if (stRtn != 0) {
                hFailRecVendorCnt++;
                continue;
            }

            if (!hMbosElectronicCode.equals("00")) {
            	selectPtrSysParm();
            	if(tmpWfValue.equals("Y")) {
                    errFlag = getGroupCard();
                    hUpperLmt = 0;
                    hUpperLmtAcmm = 0;
                    if (hTsccFlag.equals("Y")) {
                        insertTscCdrpLog();
                    }
                    if (hIpsFlag.equals("Y")) {
                        insertIpsCdrpLog();
                    }
                    if (hIchFlag.equals("Y")) {
                        insertIchB07bCard();
                    }
            	}            	
            }
            updateCrdEmbossVendor();
        }
        closeCursor(cursorIndex);

        showLogMessage("I", "", String .format(String.format(" 總回饋SUCC筆數[%d] FAIL筆數[%d]"
                          , hSucRecVendorCnt, hFailRecVendorCnt)));

        return;
    }

    /***********************************************************************/
    void proc_crd_emboss3() throws Exception {

        if (hDembRejectCode.equals("")) {
            hNewSucCnt++;
        } else {
            if ((hhBatchno.length() > 0) && (embossOk == 0)) {
//                insertCrdProcTmp1();
                hNewFailCnt++;
            }
        }

        updateCrdCombo();

        return;
    }

    /***********************************************************************/
//    void insertCrdProcTmp1() throws Exception {
//        sqlCmd = "insert into crd_proc_tmp (";
//        sqlCmd += " batchno,";
//        sqlCmd += " recno,";
//        sqlCmd += " emboss_source,";
//        sqlCmd += " emboss_reason,";
//        sqlCmd += " resend_note,";
//        sqlCmd += " source_batchno,";
//        sqlCmd += " source_recno,";
//        sqlCmd += " aps_batchno,";
//        sqlCmd += " aps_recno,";
//        sqlCmd += " seqno,";
//        sqlCmd += " to_nccc_code,";
//        sqlCmd += " card_type,";
//        sqlCmd += " acct_type,";
//        sqlCmd += " acct_key,";
//        sqlCmd += " class_code,";
//        sqlCmd += " sup_flag,";
//        sqlCmd += " unit_code,";
//        sqlCmd += " card_no,";
//        sqlCmd += " major_card_no,";
//        sqlCmd += " major_valid_fm,";
//        sqlCmd += " major_valid_to,";
//        sqlCmd += " major_chg_flag,";
//        sqlCmd += " old_card_no,";
//        sqlCmd += " change_reason,";
//        sqlCmd += " status_code,";
//        sqlCmd += " reason_code,";
//        sqlCmd += " apply_id,";
//        sqlCmd += " apply_id_code,";
//        sqlCmd += " pm_id,";
//        sqlCmd += " pm_id_code,";
//        sqlCmd += " group_code,";
//        sqlCmd += " source_code,";
//        sqlCmd += " corp_no,";
//        sqlCmd += " corp_no_code,";
//        sqlCmd += " corp_act_flag,";
//        sqlCmd += " corp_assure_flag,";
//        sqlCmd += " reg_bank_no,";
//        sqlCmd += " risk_bank_no,";
//        sqlCmd += " chi_name,";
//        sqlCmd += " eng_name,";
//        sqlCmd += " birthday,";
//        sqlCmd += " marriage,";
//        sqlCmd += " rel_with_pm,";
//        sqlCmd += " service_year,";
//        sqlCmd += " education,";
//        sqlCmd += " nation,";
//        sqlCmd += " salary,";
//        sqlCmd += " mail_zip,";
//        sqlCmd += " mail_addr1,";
//        sqlCmd += " mail_addr2,";
//        sqlCmd += " mail_addr3,";
//        sqlCmd += " mail_addr4,";
//        sqlCmd += " mail_addr5,";
//        sqlCmd += " resident_zip,";
//        sqlCmd += " resident_addr1,";
//        sqlCmd += " resident_addr2,";
//        sqlCmd += " resident_addr3,";
//        sqlCmd += " resident_addr4,";
//        sqlCmd += " resident_addr5,";
//        sqlCmd += " company_name,";
//        sqlCmd += " job_position,";
//        sqlCmd += " home_area_code1,";
//        sqlCmd += " home_tel_no1,";
//        sqlCmd += " home_tel_ext1,";
//        sqlCmd += " home_area_code2,";
//        sqlCmd += " home_tel_no2,";
//        sqlCmd += " home_tel_ext2,";
//        sqlCmd += " office_area_code1,";
//        sqlCmd += " office_tel_no1,";
//        sqlCmd += " office_tel_ext1,";
//        sqlCmd += " office_area_code2,";
//        sqlCmd += " office_tel_no2,";
//        sqlCmd += " office_tel_ext2,";
//        sqlCmd += " e_mail_addr,";
//        sqlCmd += " cellar_phone,";
//        sqlCmd += " act_no,";
//        sqlCmd += " vip,";
//        sqlCmd += " fee_code,";
//        sqlCmd += " force_flag,";
//        sqlCmd += " business_code,";
//        sqlCmd += " introduce_no,";
//        sqlCmd += " valid_fm,";
//        sqlCmd += " valid_to,";
//        sqlCmd += " sex,";
//        sqlCmd += " value,";
//        sqlCmd += " accept_dm,";
//        sqlCmd += " apply_no,";
//        sqlCmd += " electronic_code,";
//        sqlCmd += " mail_type,";
//        sqlCmd += " mail_no,";
//        sqlCmd += " introduce_id,";
//        sqlCmd += " introduce_name,";
//        sqlCmd += " salary_code,";
//        sqlCmd += " student,";
//        sqlCmd += " credit_lmt,";
//        sqlCmd += " apply_id_ecode,";
//        sqlCmd += " corp_no_ecode,";
//        sqlCmd += " pm_id_ecode,";
//        sqlCmd += " police_no1,";
//        sqlCmd += " police_no2,";
//        sqlCmd += " police_no3,";
//        sqlCmd += " pm_cash,";
//        sqlCmd += " sup_cash,";
//        sqlCmd += " online_mark,";
//        sqlCmd += " reject_code,";
//        sqlCmd += " emboss_4th_data,";
//        sqlCmd += " member_id,";
//        sqlCmd += " stmt_cycle,";
//        sqlCmd += " pvv,";
//        sqlCmd += " cvv,";
//        sqlCmd += " cvv2,";
//        sqlCmd += " pvki,";
//        sqlCmd += " pin_block,";
//        sqlCmd += " open_passwd,";
//        sqlCmd += " voice_passwd,";
//        sqlCmd += " cht_passwd,";
//        sqlCmd += " credit_flag,";
//        sqlCmd += " comm_flag,";
//        sqlCmd += " pm_birthday,";
//        sqlCmd += " sup_birthday,";
//        sqlCmd += " standard_fee,";
//        sqlCmd += " final_fee_code,";
//        sqlCmd += " fee_reason_code,";
//        sqlCmd += " annual_fee,";
//        sqlCmd += " chg_addr_flag,";
//        sqlCmd += " service_code,";
//        sqlCmd += " cntl_area_code,";
//        sqlCmd += " stock_no,";
//        sqlCmd += " old_beg_date,";
//        sqlCmd += " old_end_date,";
//        sqlCmd += " nccc_type,";
//        sqlCmd += " to_nccc_date,";
//        sqlCmd += " nccc_filename,";
//        sqlCmd += " emboss_result,";
//        sqlCmd += " diff_code,";
//        sqlCmd += " credit_error,";
//        sqlCmd += " auth_credit_lmt,";
//        sqlCmd += " fail_proc_code,";
//        sqlCmd += " mail_code,";
//        sqlCmd += " ic_flag,";
//        sqlCmd += " branch,";
//        sqlCmd += " mail_attach1,";
//        sqlCmd += " mail_attach2,";
//        sqlCmd += " contactor1_name,";
//        sqlCmd += " contactor1_relation,";
//        sqlCmd += " contactor1_area_code,";
//        sqlCmd += " contactor1_tel,";
//        sqlCmd += " contactor1_ext,";
//        sqlCmd += " contactor2_name,";
//        sqlCmd += " contactor2_relation,";
//        sqlCmd += " contactor2_area_code,";
//        sqlCmd += " contactor2_tel,";
//        sqlCmd += " contactor2_ext,";
//        sqlCmd += " est_graduate_month,";
//        sqlCmd += " market_agree_base,";
//        sqlCmd += " vacation_code,";
//        sqlCmd += " market_agree_act,";
//        sqlCmd += " fancy_limit_flag,";
//        sqlCmd += " crt_date,";
//        sqlCmd += " dc_indicator,";
//        sqlCmd += " curr_code,";
//        sqlCmd += " act_no_f,";
//        sqlCmd += " act_no_l,";
//        sqlCmd += " act_no_f_ind,";
//        sqlCmd += " agree_l_ind,";
//        sqlCmd += " act_no_l_ind,";
//        sqlCmd += " mno_id,";
//        sqlCmd += " msisdn,";
//        sqlCmd += " service_id,";
//        sqlCmd += " se_id,";
//        sqlCmd += " service_ver,";
//        sqlCmd += " service_type,";
//        sqlCmd += " sir_no,";
//        sqlCmd += " activation_code,";
//        sqlCmd += " track2_dek,";
//        sqlCmd += " pin_mobile,";
//        sqlCmd += " send_pwd_flag,";
//        sqlCmd += " jcic_score)";
//        sqlCmd += " select ";
//        sqlCmd += " batchno,";
//        sqlCmd += " recno,";
//        sqlCmd += " emboss_source,";
//        sqlCmd += " emboss_reason,";
//        sqlCmd += " resend_note,";
//        sqlCmd += " source_batchno,";
//        sqlCmd += " source_recno,";
//        sqlCmd += " aps_batchno,";
//        sqlCmd += " aps_recno,";
//        sqlCmd += " seqno,";
//        sqlCmd += " to_nccc_code,";
//        sqlCmd += " card_type,";
//        sqlCmd += " acct_type,";
//        sqlCmd += " acct_key,";
//        sqlCmd += " class_code,";
//        sqlCmd += " sup_flag,";
//        sqlCmd += " unit_code,";
//        sqlCmd += " card_no,";
//        sqlCmd += " major_card_no,";
//        sqlCmd += " major_valid_fm,";
//        sqlCmd += " major_valid_to,";
//        sqlCmd += " major_chg_flag,";
//        sqlCmd += " old_card_no,";
//        sqlCmd += " change_reason,";
//        sqlCmd += " status_code,";
//        sqlCmd += " reason_code,";
//        sqlCmd += " apply_id,";
//        sqlCmd += " apply_id_code,";
//        sqlCmd += " pm_id,";
//        sqlCmd += " pm_id_code,";
//        sqlCmd += " group_code,";
//        sqlCmd += " source_code,";
//        sqlCmd += " corp_no,";
//        sqlCmd += " corp_no_code,";
//        sqlCmd += " corp_act_flag,";
//        sqlCmd += " corp_assure_flag,";
//        sqlCmd += " reg_bank_no,";
//        sqlCmd += " risk_bank_no,";
//        sqlCmd += " chi_name,";
//        sqlCmd += " eng_name,";
//        sqlCmd += " birthday,";
//        sqlCmd += " marriage,";
//        sqlCmd += " rel_with_pm,";
//        sqlCmd += " service_year,";
//        sqlCmd += " education,";
//        sqlCmd += " nation,";
//        sqlCmd += " salary,";
//        sqlCmd += " mail_zip,";
//        sqlCmd += " mail_addr1,";
//        sqlCmd += " mail_addr2,";
//        sqlCmd += " mail_addr3,";
//        sqlCmd += " mail_addr4,";
//        sqlCmd += " mail_addr5,";
//        sqlCmd += " resident_zip,";
//        sqlCmd += " resident_addr1,";
//        sqlCmd += " resident_addr2,";
//        sqlCmd += " resident_addr3,";
//        sqlCmd += " resident_addr4,";
//        sqlCmd += " resident_addr5,";
//        sqlCmd += " company_name,";
//        sqlCmd += " job_position,";
//        sqlCmd += " home_area_code1,";
//        sqlCmd += " home_tel_no1,";
//        sqlCmd += " home_tel_ext1,";
//        sqlCmd += " home_area_code2,";
//        sqlCmd += " home_tel_no2,";
//        sqlCmd += " home_tel_ext2,";
//        sqlCmd += " office_area_code1,";
//        sqlCmd += " office_tel_no1,";
//        sqlCmd += " office_tel_ext1,";
//        sqlCmd += " office_area_code2,";
//        sqlCmd += " office_tel_no2,";
//        sqlCmd += " office_tel_ext2,";
//        sqlCmd += " e_mail_addr,";
//        sqlCmd += " cellar_phone,";
//        sqlCmd += " act_no,";
//        sqlCmd += " vip,";
//        sqlCmd += " fee_code,";
//        sqlCmd += " force_flag,";
//        sqlCmd += " business_code,";
//        sqlCmd += " introduce_no,";
//        sqlCmd += " valid_fm,";
//        sqlCmd += " valid_to,";
//        sqlCmd += " sex,";
//        sqlCmd += " value,";
//        sqlCmd += " accept_dm,";
//        sqlCmd += " apply_no,";
//        sqlCmd += " electronic_code,";
//        sqlCmd += " mail_type,";
//        sqlCmd += " mail_no,";
//        sqlCmd += " introduce_id,";
//        sqlCmd += " introduce_name,";
//        sqlCmd += " salary_code,";
//        sqlCmd += " student,";
//        sqlCmd += " credit_lmt,";
//        sqlCmd += " apply_id_ecode,";
//        sqlCmd += " corp_no_ecode,";
//        sqlCmd += " pm_id_ecode,";
//        sqlCmd += " police_no1,";
//        sqlCmd += " police_no2,";
//        sqlCmd += " police_no3,";
//        sqlCmd += " pm_cash,";
//        sqlCmd += " sup_cash,";
//        sqlCmd += " online_mark,";
//        sqlCmd += " ?,";
//        sqlCmd += " org_emboss_data,";
//        sqlCmd += " member_id,";
//        sqlCmd += " stmt_cycle,";
//        sqlCmd += " pvv,";
//        sqlCmd += " cvv,";
//        sqlCmd += " cvv2,";
//        sqlCmd += " pvki,";
//        sqlCmd += " pin_block,";
//        sqlCmd += " open_passwd,";
//        sqlCmd += " voice_passwd,";
//        sqlCmd += " cht_passwd,";
//        sqlCmd += " credit_flag,";
//        sqlCmd += " comm_flag,";
//        sqlCmd += " pm_birthday,";
//        sqlCmd += " sup_birthday,";
//        sqlCmd += " standard_fee,";
//        sqlCmd += " final_fee_code,";
//        sqlCmd += " fee_reason_code,";
//        sqlCmd += " annual_fee,";
//        sqlCmd += " chg_addr_flag,";
//        sqlCmd += " service_code,";
//        sqlCmd += " cntl_area_code,";
//        sqlCmd += " stock_no,";
//        sqlCmd += " old_beg_date,";
//        sqlCmd += " old_end_date,";
//        sqlCmd += " nccc_type,";
//        sqlCmd += " to_nccc_date,";
//        sqlCmd += " nccc_filename,";
//        sqlCmd += " emboss_result,";
//        sqlCmd += " diff_code,";
//        sqlCmd += " credit_error,";
//        sqlCmd += " auth_credit_lmt,";
//        sqlCmd += " fail_proc_code,";
//        sqlCmd += " mail_code,";
//        sqlCmd += " ic_flag,";
//        sqlCmd += " branch,";
//        sqlCmd += " mail_attach1,";
//        sqlCmd += " mail_attach2,";
//        sqlCmd += " contactor1_name,";
//        sqlCmd += " contactor1_relation,";
//        sqlCmd += " contactor1_area_code,";
//        sqlCmd += " contactor1_tel,";
//        sqlCmd += " contactor1_ext,";
//        sqlCmd += " contactor2_name,";
//        sqlCmd += " contactor2_relation,";
//        sqlCmd += " contactor2_area_code,";
//        sqlCmd += " contactor2_tel,";
//        sqlCmd += " contactor2_ext,";
//        sqlCmd += " est_graduate_month,";
//        sqlCmd += " market_agree_base,";
//        sqlCmd += " vacation_code,";
//        sqlCmd += " market_agree_act,";
//        sqlCmd += " fancy_limit_flag,";
//        sqlCmd += " crt_date,";
//        sqlCmd += " dc_indicator,";
//        sqlCmd += " curr_code,";
//        sqlCmd += " act_no_f,";
//        sqlCmd += " act_no_l,";
//        sqlCmd += " act_no_f_ind,";
//        sqlCmd += " agree_l_ind,";
//        sqlCmd += " act_no_l_ind,";
//        sqlCmd += " mno_id,";
//        sqlCmd += " msisdn,";
//        sqlCmd += " service_id,";
//        sqlCmd += " se_id,";
//        sqlCmd += " service_ver,";
//        sqlCmd += " service_type,";
//        sqlCmd += " sir_no,";
//        sqlCmd += " activation_code,";
//        sqlCmd += " track2_dek,";
//        sqlCmd += " pin_mobile,";
//        sqlCmd += " send_pwd_flag,";
//        sqlCmd += " jcic_score ";
//        sqlCmd += " from crd_emboss ";
//        sqlCmd += "where batchno = ? ";
//        sqlCmd += "  and recno   = ? ";
//        setString(1, hDembRejectCode);
//        setString(2, hhBatchno);
//        setLong(3, hhRecno);
//        insertTable();
//        if (dupRecord.equals("Y")) {
//            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
//        }
//    }

    /***********************************************************************/
    void updateCrdCombo() throws Exception {
        String hComboRowid = "";

        hComboRowid = "";
        sqlCmd  = "select rowid rowid ";
        sqlCmd += "  from crd_combo  ";
        sqlCmd += "where card_no = ? ";
        setString(1, hDembCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hComboRowid = getValue("rowid");

            // if(h_demb_reject_code.substring(0, 1).equals(""))
            if (hDembRejectCode.length() == 0) {
                daoTable   = "crd_combo";
                updateSQL  = " rtn_nccc_date = to_char(sysdate, 'yyyymmdd'),";
                updateSQL += " mod_time      = sysdate,";
                updateSQL += " mod_pgm       = 'CrdD006'";
                whereStr   = "where rowid    =  ? ";
                setRowId(1, hComboRowid);
                updateTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("update_crd_combo not found!","", hCallBatchSeqno);
                }
            } else {
                daoTable   = "crd_combo";
                updateSQL  = "rtn_nccc_date = to_char(sysdate, 'yyyymmdd'),";
                updateSQL += " emboss_code  = '1',";
                updateSQL += " emboss_date  = to_char(sysdate, 'yyyymmdd'),";
                updateSQL += " mod_time     = sysdate,";
                updateSQL += " mod_pgm      = 'CrdD006'";
                whereStr = "where rowid   =  ? ";
                setRowId(1, hComboRowid);
                updateTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("update_crd_combo not found!","", hCallBatchSeqno);
                }
            }
        }

        return;
    }
/***********************************************************************/
    void selectPtrSysParm() throws Exception 
    {
      tmpWfValue = "N";
      
      sqlCmd  = "select wf_value ";
      sqlCmd += "  from ptr_sys_parm   ";
      sqlCmd += " where wf_parm = 'SYSPARM'  ";
      sqlCmd += "   and wf_key = 'ELEC_CARD_NO' ";
      int recordCnt = selectTable();
      if (recordCnt > 0) {
    	  tmpWfValue = getValue("wf_value");
      }
      return;
    }
    /***********************************************************************/
// change to crd_item_unit
int getGroupCard() throws Exception 
{
  hTsccFlag = "";
  hIpsFlag = "";
  hIpsKind = "";
  hIchFlag = "";
  sqlCmd  = "select decode(ELECTRONIC_CODE , '01' , 'Y', 'N')  as tscc_flag,";
  sqlCmd += "       decode(ELECTRONIC_CODE , '02' , 'Y', 'N')  as ips_flag ,";
  sqlCmd += "       decode(ELECTRONIC_CODE , '03' , 'Y', 'N')  as ich_flag ,";
  sqlCmd += " ips_kind ";
  sqlCmd += "  from crd_item_unit ";
  sqlCmd += " where unit_code  = ?  ";
  setString(1, hMbosUnitCode);
  int recordCnt = selectTable();
if(debug == 1) showLogMessage("I", "", "   888 chk crd_item_unit cnt="+recordCnt);
  if (recordCnt > 0) {
      hTsccFlag = getValue("tscc_flag");
      hIpsFlag = getValue("ips_flag");
      hIpsKind = getValue("ips_kind");
      hIchFlag = getValue("ich_flag");
  }
  else
     comcr.errRtn("select_"+daoTable+" not found!", hMbosUnitCode + hMbosCardType
               , hCallBatchSeqno);
if(debug == 1) showLogMessage("I", "", "   888 chk flag="+ hTsccFlag +","+ hTsccFlag);
  return 0;
}
/***********************************************************************/
int insertTscCdrpLog() throws Exception 
{
        String hRowid = "";
        double hSeqnoCurrent = 0;

        hBinNo = "";
        sqlCmd  = "select a.tsc_bin_no ,b.seq_no_current ";
        sqlCmd += "  from tsc_bintable a ";
        sqlCmd += "  left outer join tsc_bin_curr b";
        sqlCmd += "    on b.tsc_bin_no = a.tsc_bin_no ";
        sqlCmd += " where decode(a.card_type,  '','00'  , a.card_type ) = ";
        sqlCmd +=       " decode(cast(? as varchar(10)) , '', '00'  , ?) ";
        sqlCmd += "   and decode(a.group_code, '','0000', a.group_code) = ";
        sqlCmd +=       " decode(cast(? as varchar(10)) , '', '0000', ?) ";
        setString(1, hMbosCardType);
        setString(2, hMbosCardType);
        setString(3, hMbosUnitCode);
        setString(4, hMbosUnitCode);
        int recordCnt = selectTable();
if(debug == 1)
  showLogMessage("I","","  sel tsc_bin="+ hMbosCardType +",unit="+ hMbosUnitCode +","+recordCnt);
        if (recordCnt > 0) {
            hBinNo = getValue("tsc_bin_no");
            hSeqnoCurrent = getValueDouble("seq_no_current");
        } else {
           comcr.errRtn("select_tsc_bintable  found 1 !="+ hMbosCardType +",", hMbosGroupCode
                         , hCallBatchSeqno);
        }

        hTempX09 = "";
        sqlCmd = "select substr(to_char( cast(? as double) + 1,'000000000'),2,10) as h_temp_x09";
        sqlCmd += " from dual ";
        setDouble(1, hSeqnoCurrent);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempX09 = getValue("h_temp_x09");
        }

        hTempX10 = "";
        sqlCmd = "select vendor_tscc ";
        sqlCmd += " from ptr_vendor_setting  ";
        sqlCmd += "where vendor = ? ";
        setString(1, hMbosVendor);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempX10 = getValue("vendor_tscc");
        }

        chkDig = comc.chgnRtn(String.format("%6.6s%9.9s", hBinNo, hTempX09));
        hTempX16 = String.format("%6.6s%9.9s%1d", hBinNo, hTempX09, chkDig);
if(debug == 1)
   showLogMessage("I", "", "   888 card =["+ hTempX16 +"]" + hBinNo +","+ hTempX09);

        daoTable  = "tsc_bin_curr ";
        updateSQL = "seq_no_current = ? + 1";
        whereStr  = "where tsc_bin_no   = ? ";
        setDouble(1, hSeqnoCurrent);
        setString(2, hBinNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_bin_curr  not found!", hBinNo, hCallBatchSeqno);
        }

        daoTable = "tsc_cdrp_log";
        setValue("tsc_card_no"       , hTempX16);
        setValue("card_no"           , hMbosCardNo);
        setValue("tsc_emboss_rsn"    , hMbosEmbossSource);
        setValue("tsc_vendor_cd"     , hTempX10);
        setValue("emboss_date"       , sysDate);
        setValueLong("upper_lmt"     , hUpperLmt);
        setValueLong("upper_lmt_acmm", hUpperLmtAcmm);
        setValue("mod_time"          , sysDate + sysTime);
        setValue("mod_pgm"           , "CrdD006");
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_cdrp_log duplicate!", "", hCallBatchSeqno);
        }

        return (0);
}
/***********************************************************************/
int insertIpsCdrpLog() throws Exception {
        double hSeqnoCurrent = 0;

        // * 1:DEBIT卡 2:金融卡 3:GIFT卡 7:信用卡 9:SD 卡
        sqlCmd  = "select seq_no ";
        sqlCmd += "  from ips_card_seqno  ";
        sqlCmd += " where bank_no   = '006'  ";
        sqlCmd += "   and card_kind = ? ";
        setString(1, hIpsKind);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hSeqnoCurrent = getValueDouble("seq_no");
        } else {
            hSeqnoCurrent = 0;
            daoTable = "ips_card_seqno";
            setValue("bank_no", "006");
            setValue("card_kind", "7");
            setValue("seq_no", "1");
            setValueDouble("seq_dig", hSeqnoCurrent);
            setValue("mod_time", sysDate + sysTime);
            setValue("mod_pgm", "CrdD006");
            insertTable();
            if (dupRecord.equals("Y")) {
                comcr.errRtn("insert_ips_card_seqno duplicate!", "", hCallBatchSeqno);
            }
        }

        hSeqnoCurrent++;

        hTempX10 = "";
        sqlCmd = "select vendor_tscc ";
        sqlCmd += " from ptr_vendor_setting  ";
        sqlCmd += "where vendor = ? ";
        setString(1, hMbosVendor);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempX10 = getValue("vendor_tscc");
        }

        hTempX11 = String.format("006%s%06.0f%1d", hIpsKind, hSeqnoCurrent, 0);

        daoTable  = "ips_card_seqno ";
        updateSQL = " seq_no         =  ? ";
        whereStr  = "where bank_no   = '006' ";
        whereStr += "  and card_kind =  ? ";
        setDouble(1, hSeqnoCurrent);
        setString(2, hIpsKind);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_card_seqno  not found!", "", hCallBatchSeqno);
        }

        daoTable = "ips_cdrp_log";
        setValue("ips_card_no"  , hTempX11);
        setValue("card_no"      , hMbosCardNo);
        setValue("personal_id"  , hMbosApplyId);
        setValue("personal_name", comc.getSubString(hMbosChiName, 0, 16));
        setValue("mod_time"     , sysDate + sysTime);
        setValue("mod_pgm"      , "CrdD006");
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ips_cdrp_log duplicate!", "", hCallBatchSeqno);
        }

        return (0);
    }
/*************************************************************************/
int insertIchB07bCard() throws Exception {

    if (debugMode)
        showLogMessage("I", "", String.format(" *** ICH=[%s]", hDembCardNo));

    hSeqnoIch = 0;
    hCardCode = "";

    sqlCmd = " select seq_no_curr , a.card_code ";
    sqlCmd += "     from ich_card_parm b, crd_item_unit a ";
    sqlCmd += "    where a.unit_code = ? ";
    sqlCmd += "      and b.card_code = a.card_code ";

    setString(1, hMbosUnitCode);
    if (selectTable() > 0) {
        hSeqnoIch = getValueInt("seq_no_curr");
        hCardCode = getValue("card_code");
    } else {
        comcr.errRtn(String.format("select_ich_card_code  error"), String.format("unit=[%s]", hMbosUnitCode)
                                                                  , hCallBatchSeqno);
    }

    hSeqnoIch++;
    /*
     * lai test h_card_code = "671716101"; 
     * h_seqno_ich = 161081;
     */

    getIchCardNo();

    daoTable = "ich_card_parm";
    updateSQL = " seq_no_curr = ? ";
    whereStr = " where card_code = ? ";
    setInt(1, hSeqnoIch);
    setString(2, hCardCode);
    updateTable();
    if (notFound.equals("Y")) {
        comcr.errRtn(String.format("update ich_card_parm error [%s]", hCardCode), "", hCallBatchSeqno);
    }

    daoTable = "ich_b07b_card";
    setValue("ich_card_no", ichCardNo);
    setValue("card_no"    , hDembCardNo);
    setValue("effc_date"  , hMbosValidTo);
    setValue("issue_date" , hBusiBusinessDate);
    setValue("online_add" , "1");
    setValue("offline_add", "0");
    setValue("mod_time"   , sysDate + sysTime);
    setValue("mod_pgm"    , javaProgram);
    insertTable();
    if (dupRecord.equals("Y")) {
        comcr.errRtn("insert_ich_b07b_card duplicate!", "", hCallBatchSeqno);
    }
    return (0);
}

/***********************************************************************/
int getIchCardNo() throws Exception 
{
  int[] tmpArr = new int[15];
  int Modulus = 10;
  int[] Weight = { 3, 7, 9, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4 };
  int Total = 0;

    for (int i = 0; i <= 8; i++) {
        tmpArr[i] = comc.str2int(hCardCode.substring(i, i + 1));
    }
    for (int i = 9; i <= 14; i++) {
        tmpArr[i] = comc.str2int(comm.fillZero(Integer.toString(hSeqnoIch), 6).substring(i - 9, i - 9 + 1));
    }
    int diff = ((tmpArr[14] << 4) | (tmpArr[13])) ^ ((tmpArr[13] << 4) | (tmpArr[14]));
    for (int i = 0; i <= 14; i++) {
        Total += tmpArr[i] * Weight[(i + diff) % 15];
    }
    int tmpInt = Total % 10; /* 餘數 */
    int chk = ( Modulus - tmpInt ) % 10;

    ichCardNo = String.format("%9.9s%1d%06d", hCardCode, chk, hSeqnoIch);

if(debug ==1)  showLogMessage("I", "", String.format("    ICH card=[%s]", ichCardNo));

  return 0;

}
/***********************************************************************/
void updateCrdEmbossVendor() throws Exception {
        int founRec = 0;
        int actCnt = 0;

if(debug == 1)
   showLogMessage("I", "", "   888  type=[" + hMbosNcccType + "]" + hMbosEmbossSource);
        dateTime();
        if (hMbosNcccType.equals("1")) {
            if ((hMbosEmbossSource.equals("1")) || (hMbosEmbossSource.equals("2"))) {
                daoTable   = "crd_emboss";
                updateSQL  = " rtn_nccc_date = ?,";
                updateSQL += " error_code    = '',";
                updateSQL += " reject_code   = '',";
                updateSQL += " emboss_result = '0',";
                updateSQL += " mod_time      = sysdate,";
                updateSQL += " mod_pgm       = 'CrdD006'";
                whereStr   = "where rowid    = ? ";
                setString(1, sysDate);
                setRowId(2, hMbosRowid);
                actCnt = updateTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("update_crd_emboss not found!", "", hCallBatchSeqno);
                }
                founRec = 1;
            }
        }
        if (hMbosNcccType.equals("2")) {
            if ((hMbosEmbossSource.equals("3")) || (hMbosEmbossSource.equals("4"))) {
                daoTable   = "crd_emboss";
                updateSQL  = " rtn_nccc_date = ?,";
                updateSQL += " error_code    = '',";
                updateSQL += " reject_code   = '',";
                updateSQL += " emboss_result = '0',";
                updateSQL += " mod_time      = sysdate,";
                updateSQL += " mod_pgm       = 'CrdD006'";
                whereStr   = "where rowid    = ? ";
                setString(1, sysDate);
                setRowId(2, hMbosRowid);
                actCnt = updateTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("update_crd_emboss not found!", "", hCallBatchSeqno);
                }
                founRec = 1;
            }
        }

        if (hMbosNcccType.equals("3")) {
            if ((hMbosEmbossSource.compareTo("5") >= 0)) {
                daoTable   = "crd_emboss";
                updateSQL  = " rtn_nccc_date = to_char(sysdate, 'yyyymmdd'),";
                updateSQL += " error_code    = '',";
                updateSQL += " reject_code   = '',";
                updateSQL += " emboss_result = '0',";
                updateSQL += " mod_time      = sysdate,";
                updateSQL += " mod_pgm       = 'CrdD006'";
                whereStr = "where rowid    = ? ";
                setRowId(1, hMbosRowid);
                actCnt = updateTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("update_crd_emboss not found!","", hCallBatchSeqno);
                }
                founRec = 1;
            }
        }

        if (actCnt == 0)
            hFailRecVendorCnt++;
        else {
            if (founRec == 1)
                hSucRecVendorCnt++;
            else
                hFailRecVendorCnt++;
        }

        updateCrdComboVendor();

        return;
    }

    /***********************************************************************/
    void updateCrdComboVendor() throws Exception {

        String hComboRowid = "";
        hComboRowid = "";
        sqlCmd  = "select rowid   as rowid";
        sqlCmd += "  from crd_combo  ";
        sqlCmd += " where card_no = ? ";
        setString(1, hMbosCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hComboRowid = getValue("rowid");

            daoTable   = "crd_combo";
            updateSQL  = " rtn_nccc_date = to_char(sysdate, 'yyyymmdd'),";
            updateSQL += " mod_time      = sysdate,";
            updateSQL += " mod_pgm       = 'CrdD006'";
            whereStr   = "where rowid    = ? ";
            setRowId(1, hComboRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_combo not found!", "", hCallBatchSeqno);
            }
        }
        return;
    }
    /***********************************************************************/
    int checkRejectCode() throws Exception {

        hDembCardNo = hMbosCardNo;
        hDembRejectCode = "";
        xMbosCardNo = "";
        xMbosCardType = "";
        xMbosGroupCode = "";
        xMbosUnitCode = "";
        xMbosEmbossSource = "";
        xMbosEmbossReason = "";

        count = 0;
        hhBatchno = "";
        hhRecno = 0;
        hhRowid = "";
        embossOk = 0;

        /*檢查是否有同卡種、團代、ID,且為活卡*/
        /*20150416 需同時檢查crd_emboss是否有相同條件待製卡 有可能新制跟重制同時進來 */

        sqlCmd = "select count(*) cnt";
        sqlCmd += " from crd_card a, crd_idno b ";
        sqlCmd += "where a.card_type    = ?  ";
        sqlCmd += "  and decode(a.group_code , '',  '0000'   , a.group_code )  ";
        sqlCmd += "    = decode(cast(? as varchar(10)), '', '0000', ?)  ";
        sqlCmd += "  and decode(a.corp_no    , '', 'XXXXXXXX', a.corp_no)  ";
        sqlCmd += "    = decode(cast(? as varchar(10)), '', 'XXXXXXXX', ?)  ";
        sqlCmd += "  and b.id_no      = ?  ";
        sqlCmd += "  and a.id_p_seqno = b.id_p_seqno  ";
        sqlCmd += "  and a.current_code = '0' ";
       setString(1, hMbosCardType);
        setString(2, hMbosGroupCode);
        setString(3, hMbosGroupCode);
        setString(4, hMbosCorpNo);
        setString(5, hMbosCorpNo);
        setString(6, hMbosApplyId);
        int recordCnt = selectTable();
if(debug == 1) showLogMessage("I","", " 888 reject 0 cnt=["+recordCnt+"]"+ hMbosCardType +","+ hMbosGroupCode +","+ hMbosCorpNo);
        if (notFound.equals("Y")) {}
       else
          {
            count = getValueInt("cnt");
if(debug == 1) showLogMessage("I","", " 888 reject 01 cnt=["+count+"]");
            if (count == 0) {
                sqlCmd = "select count(*) cnt";
                sqlCmd += " from crd_emboss  ";
                sqlCmd += "where card_type     = ?  ";
                sqlCmd += "  and decode(group_code            , '', '0000'    , group_code ) ";
                sqlCmd += "    = decode(cast(? as varchar(10)), '', '0000'    , ?)  ";
                sqlCmd += "  and apply_id      = ?  ";
                sqlCmd += "  and decode(corp_no               , '', 'XXXXXXXX', corp_no) ";
                sqlCmd += "    = decode(cast(? as varchar(10)), '', 'XXXXXXXX', ?)  ";
                sqlCmd += "  and in_main_date  = ''  ";
                sqlCmd += "  and in_main_error = ''  ";
                sqlCmd += "  and reject_code   = ''  ";
                sqlCmd += "  and card_no      != ? ";
                setString(1, hMbosCardType);
               setString(2, hMbosGroupCode);
                setString(3, hMbosGroupCode);
                setString(4, hMbosApplyId);
                setString(5, hMbosCorpNo);
                setString(6, hMbosCorpNo);
                setString(7, hMbosCardNo);
                recordCnt = selectTable();
                if (recordCnt > 0) {
                    count = getValueInt("cnt");
                }
            }
        }
        if (debug == 1)
            showLogMessage("I", "", " 888 reject 1  end=[" + recordCnt + "]" + hMbosCardNo + "," + hMbosNcccType + ","
                    + hMbosEmbossSource + "," + count);
        if (recordCnt > 0) {
            /* for 新製卡檢核 */
//            if (h_mbos_nccc_type.equals("1")) {
//                if ((h_mbos_emboss_source.equals("1")) || (h_mbos_emboss_source.equals("2"))) {
//                    if (count != 0) {
//                        h_demb_reject_code = "17";
//                    }
//                }
//            } else {
//            }
            /* V1.12.01 for 新製卡英文名檢核 */
//            int englen = 0;
//            for (englen = 0; englen < h_mbos_eng_name.length(); englen++) {
//                if (h_mbos_eng_name.toCharArray()[englen] >= 65 && h_mbos_eng_name.toCharArray()[englen] <= 90
//                        || h_mbos_eng_name.toCharArray()[englen] == 32 || h_mbos_eng_name.toCharArray()[englen] == 0
//                        || h_mbos_eng_name.toCharArray()[englen] == 39 || h_mbos_eng_name.toCharArray()[englen] == 44
//                       || h_mbos_eng_name.toCharArray()[englen] == 45 || h_mbos_eng_name.toCharArray()[englen] == 46
//                        || h_mbos_eng_name.toCharArray()[englen] == 47) {
//                } else {
//                    h_demb_reject_code = "50";
//                    break;
//                }
//            }
            /* V1.11.01 新增制裁名單reject code */
//            sqlCmd = " SELECT count(*) count ";
//            sqlCmd += "   FROM ptr_rpt_list ";
//            sqlCmd += "  WHERE list_batch_no = 'CRDS'  ";
//            sqlCmd += "    AND substr(trim(value1),1,10)  in (select apply_id ";
//            sqlCmd += "                                          from crd_emboss ";
//            sqlCmd += "                                         where to_nccc_date = ? ";
//            sqlCmd += "                                           and acct_key = ?) ";
//            setString(1, h_mbos_to_nccc_date);
//            setString(2, h_mbos_acct_key);
//            if (selectTable() > 0) {
//                count = getValueInt("count");
//                if (count != 0) {
//                    h_demb_reject_code = "48";
//                }
//            }
            /*V1.11.01 新增制裁名單reject code*/
        
            hEmbossResult = "";
            if (hDembRejectCode.length() == 0) {
                hEmbossResult = "0"; /* 成功 */
            } else {
                hEmbossResult = "1"; /* 失敗 */
            }

            sqlCmd = "select batchno,";
            sqlCmd += " recno,";
            sqlCmd += " rowid rowid,";
            sqlCmd += " card_type,";
            sqlCmd += " group_code,";
            sqlCmd += " unit_code,";
            sqlCmd += " emboss_source,";
            sqlCmd += " emboss_reason ";
            sqlCmd += "  from crd_emboss ";
            sqlCmd += " where rowid = ? ";
            setRowId(1, hMbosRowid);
            int recordCnt2 = selectTable();
            if (recordCnt2 > 0) {
                hhBatchno = getValue("batchno");
                hhRecno = getValueLong("recno");
                hhRowid = getValue("rowid");
                xMbosCardType = getValue("card_type");
                xMbosGroupCode = getValue("group_code");
                xMbosUnitCode = getValue("unit_code");
                xMbosEmbossSource = getValue("emboss_source");
                xMbosEmbossReason = getValue("emboss_reason");

                daoTable   = "crd_emboss";
                updateSQL  = " rtn_nccc_date = to_char(sysdate, 'yyyymmdd'),";
                updateSQL += " reject_code   = ?,";
                updateSQL += " emboss_result = ?,";
                updateSQL += " mod_time      = sysdate,";
                updateSQL += " mod_pgm       = 'CrdD006'";
                whereStr   = "where rowid    = ? ";
                setString(1, hDembRejectCode);
                setString(2, hEmbossResult);
                setRowId(3, hhRowid);
                updateTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("update_crd_emboss not found!", "", hCallBatchSeqno);
                }

                insertCrdNcccStat();
            } else {
                embossOk = 1;
            }

            if(hDembRejectCode.length() != 0) {
               return 1;
              }
           }

        return 0;
    }
/***********************************************************************/
void insertCrdNcccStat() throws Exception 
{
  hMbosCardNo = hDembCardNo;

  daoTable = "crd_nccc_stat";
  setValue("batchno"      , hhBatchno);
  setValueLong("recno"    , hhRecno);
  setValue("card_no"      , hMbosCardNo);
  setValue("card_type"    , hMbosCardType);
  setValue("group_code"   , hMbosGroupCode);
  setValue("unit_code"    , hMbosUnitCode);
  setValue("emboss_source", hMbosEmbossSource);
  setValue("emboss_reason", hMbosEmbossReason);
  setValue("nccc_rtn_date", sysDate);
  setValue("reject_code"  , hDembRejectCode);
  setValue("credit_note"  , "");
  setValue("mod_user"     , "CrdD006");
  setValue("mod_time"     , sysDate + sysTime);
  setValue("mod_pgm"      , "CrdD006");
  insertTable();
  if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_crd_nccc_stat duplicate!", "", hCallBatchSeqno);
  }
}
/***********************************************************************/
public static void main(String[] args) throws Exception {
  CrdD006 proc = new CrdD006();
  int retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
}
/***********************************************************************/
}
