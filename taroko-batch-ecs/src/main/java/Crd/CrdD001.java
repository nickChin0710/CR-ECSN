/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/06/21  V1.01.01  Lai        Initial                                    *
* 108/11/25  V2.01.01  Pino       Initial                                    *
* 109/04/10  V2.01.02  Wilson     insert crd_emboss新增branch、mail_branch     *
* 109/04/13  V2.01.03  Wilson     insert crd_emboss取消branch                 *
* 109/04/13  V2.01.04  Wilson     insert crd_emboss新增crt_bank_no、vd_bank_no *
* 109/10/05  V2.01.05  Wilson     insert crd_emboss新增curr_code              *
* 109/12/18  V2.00.06   shiyuqi       updated for project coding standard   *
* 112/01/16  V2.00.07  Wilson     insert crd_emboss增加send_pwd_flag、clerk_id *
* 112/02/01  V2.00.08  Wilson     insert crd_emboss增加sms_amt                *
* 112/02/20  V2.00.09  Wilson     insert crd_emboss增加ap1_apply_date         *
* 112/03/01  V2.00.10  Wilson     insert crd_emboss增加revolve_int_rate_year  *
* 112/03/10  V1.00.11  Wilson     insert crd_emboss add revolve_int_rate_year_code*
* 112/08/26  V1.00.12  Wilson     修正弱掃問題                                                                                                *
* 112/11/27  V1.00.13  Wilson     增加處理凸字第四行                                                                                    *
* 112/11/29  V1.00.14  Wilson     寫入鍵檔人員代號                                                                                        *
* 112/12/03  V1.00.15  Wilson     crd_item_unit不判斷卡種                                                                *
*****************************************************************************/
package Crd;

import com.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class CrdD001 extends AccessDAO {
    private String progname = "接收送製卡資料處理     112/12/03  V1.00.15";
    private Map<String, Object> resultMap;

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    int debugD = 0;

    String checkHome = "";
    String hCallErrorDesc = "";
    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";
    String hTempUser = "";
    String hBusiBusinessDate = "";
    String hBusiChiDate = "";
    int totalCnt = 0;
    String tmpChar1 = "";
    String tmpChar = "";
    double tmpDoub = 0;
    long tmpLong = 0;
    int tmpInt = 0;

    String hBatchno = "";
    String hEmbossSource = "";
    String hSuperId = "";
    String hTransId = "";
    String emapRowid = "";
    String combRowid = "";
    
    String hENews = "";
    String hPromoteEmpNo = "";
    String hPromoteDept = "";
    String hCardRefNum = "";
    String hIntroduceEmpNo = "";
    String hUrFlag = "";
    String hSpouseIdNo = "";
    String hSpouseBirthday = "";
    String hResidentNoExpireDate = "";
    String hPassportDate = "";
    String hRoadsideAssistApply = "";
    String hBillApplyFlag = "";
    double hRevolveIntRate = 0.0;
    String hCompanyZip = "";
    double hSpecialCardRate = 0.0;
    String hAutopayAcctBank = "";
    String hInstFlag = "";
    String hFeeCodeI = "";
    String hGraduationElementarty = "";
    String hSpouseName = "";
    String hCompanyAddr1 = "";
    String hCompanyAddr2 = "";
    String hCompanyAddr3 = "";
    String hCompanyAddr4 = "";
    String hCompanyAddr5 = "";
    String hCurrChangeAccout = "";
    String hCreditLevelNew = "";
    String hBranch = "";
    String hCrtBankNo = "";
    String hVdBankNo = "";
    String hCurrCode = "";
    String hSendNumFlag = "";
    String hClerkId = "";
    int hSmsAmt = 0;
    String hAp1ApplyDate = "";
    String hRevolveIntRateYearCode = "";
    double hRevolveIntRateYear = 0.0;
    String hEmboss4thData = "";

    String mbosDiffCode = "";
    String mbosToNcccCode = "";
    String mbosComboIndicator = "";
    String mbosOnlineMark = "";
    String mbosCardNo = "";
    String mbosBatchno = "";
    double mbosRecno = 0;
    String mbosSourceBatchno = "";
    double mbosSourceRecno = 0;
    int mbosCreditLmt = 0;
    String mbosPoliceNo1 = "";
    String mbosPoliceNo2 = "";
    String mbosEmbossSource = "";
    String mbosEmbossReason = "";
    String mbosServiceVer = "";
    String mbosCardType = "";
    String mbosGroupCode = "";
    String mbosBinNo = "";
    String mbosUnitCode = "";
    String mbosServiceId = "";
    String mbosAcctKey = "";
    String mbosCardcat = "";
    String mbosElectronicCode = "";
    String gcrdCardMoldFlag = "";
    String hTransType = "";

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        CrdD001 proc = new CrdD001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {

            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            checkHome = comc.getECSHOME();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (args.length != 4 && args.length != 5 && args.length != 0) {
                String err1 = "CrdD001 00092701 1 shu trans_id [seq_no]\n";
                String err2 = "CrdD001 批號 製卡來源 super_id trans_id [seq_no]";
                System.out.println(err1 + ",arg=" + args.length);
                comc.errExit(err1, err2);
            }

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

            hSuperId = "batch";
            hTransId = "batch";
            if (args.length > 0) {
                hBatchno = args[0];
                hEmbossSource = args[1];
                hSuperId = args[2];
                hTransId = args[3];
            }

            showLogMessage("I", "", "批號=" + hBatchno + " 製卡來源=" + hEmbossSource);

            dateTime();
            selectPtrBusinday();

            totalCnt = 0;
            getNcccBatchno();

            selectCrdEmapTmp();

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        }

        catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }

    } // End of mainProcess
      // ************************************************************************

    public void selectPtrBusinday() throws Exception {
        selectSQL = "business_date   , " + "to_char(sysdate,'yyyymmdd')    as SYSTEM_DATE ";
        daoTable = "PTR_BUSINDAY";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_businday error!";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        hBusiBusinessDate = getValue("SYSTEM_DATE");
        long hLongChiDate = Long.parseLong(hBusiBusinessDate) - 19110000;
        hBusiChiDate = Long.toString(hLongChiDate);

        showLogMessage("I", "", "本日處理日 : [" + hBusiBusinessDate + "] [" + hBusiChiDate + "]");
    }
// ************************************************************************
public void selectCrdEmapTmp() throws Exception 
{

  selectSQL = "   batchno         " + " , a.recno     " + " , a.source    "
            + " , a.aps_batchno     " + " , a.aps_recno       " + " , a.seqno     "
            + " , a.apply_id        " + " , decode(a.apply_id_code,'','0',a.apply_id_code)  as apply_id_code "
            + " , a.pm_id           " + " , decode(a.pm_id_code   ,'','0',a.pm_id_code)  as pm_id_code "
            + " , a.card_type       " + " , a.acct_type       " + " , a.class_code      "
            + " , a.group_code      " + " , a.source_code     " + " , a.channel_code    "
            + " , a.unit_code       " + " , a.corp_no         " + " , a.corp_no_code    "
            + " , a.corp_act_flag   " + " , a.corp_assure_flag      " + " , a.reg_bank_no     "
            + " , a.risk_bank_no    " + " , a.chi_name        " + " , a.eng_name        "
            + " , a.birthday        " + " , a.marriage        " + " , a.rel_with_pm     "
            + " , a.service_year    " + " , a.education       " + " , a.nation    "
            + " , a.salary    " + " , a.resident_zip    " + " , a.resident_addr1  "
            + " , a.resident_addr2  " + " , a.resident_addr3  " + " , a.resident_addr4  "
            + " , a.resident_addr5  " + " , a.mail_zip        " + " , a.mail_addr1      "
            + " , a.mail_addr2      " + " , a.mail_addr3      " + " , a.mail_addr4      "
            + " , a.mail_addr5      " + " , a.company_name    " + " , a.job_position    "
            + " , a.home_area_code1       " + " , a.home_tel_no1    " + " , a.home_tel_ext1   "
            + " , a.home_area_code2       " + " , a.home_tel_no2    " + " , a.home_tel_ext2   "
            + " , a.office_area_code1     " + " , a.office_tel_no1  " + " , a.office_tel_ext1       "
            + " , a.office_area_code2     " + " , a.office_tel_no2  " + " , a.office_tel_ext2       "
            + " , a.cellar_phone    " + " , a.e_mail_addr     " + " , a.act_no    "
            + " , a.vip       " + " , a.fee_code        " + " , a.force_flag      "
            + " , a.business_code   " + " , a.introduce_no    " + " , a.valid_fm        "
            + " , a.valid_to        " + " , a.sex       " + " , a.value     "
            + " , a.accept_dm       " + " , a.apply_no        " 
            + " , a.cardcat         " + " , a.electronic_code       "
            + " , a.mail_type       " + " , a.introduce_id    " + " , a.introduce_name  "
            + " , a.salary_code     " + " , a.student         " + " , a.credit_lmt      "
            + " , a.police_no1      " + " , a.police_no2      " + " , a.police_no3      "
            + " , a.apply_id_ecode  " + " , a.corp_no_ecode   " + " , a.pm_id_ecode     "
            + " , a.pm_cash         " + " , a.sup_cash        "
            + " , decode(a.online_mark,'','0',a.online_mark) as online_mark " + " , a.emboss_4th_data       "
            + " , a.member_id       " + " , a.stmt_cycle      " + " , a.credit_flag     "
            + " , a.comm_flag       " + " , a.resident_no     " + " , a.other_cntry_code      "
            + " , a.passport_no     " + " , a.staff_flag      " + " , a.reject_code     "
            + " , a.reject_date     " + " , a.pm_birthday     " + " , a.sup_birthday    "
            + " , a.major_card_no   " + " , a.major_valid_fm  " + " , a.major_valid_to  "
            + " , a.major_chg_flag  " + " , a.card_no         " + " , a.bin_no    "
            + " , a.old_card_no     " + " , a.service_code    " + " , a.pvv       "
            + " , a.cvv       " + " , a.cvv2      " + " , a.pvki      "
            + " , a.standard_fee    " + " , a.final_fee_code  " + " , a.fee_reason_code       "
            + " , a.annual_fee      " + " , a.cardno_code     "
            + " , a.oth_chk_code    " + " , a.emboss_date     " + " , a.nccc_batchno    "
            + " , a.nccc_recno      " + " , a.nccc_type       " + " , a.son_card_flag   "
            + " , a.org_indiv_crd_lmt " + " , a.indiv_crd_lmt   " + " , a.ic_flag         "
            + " , a.branch    " + " , a.mail_attach1    " + " , a.mail_attach2    "
            + " , a.vendor    " + " , a.filename        " + " , a.csc       "
            + " , a.contactor1_name       " + " , a.contactor1_relation   " + " , a.contactor1_area_code  "
            + " , a.contactor1_tel  " + " , a.contactor1_ext  " + " , a.contactor2_name       "
            + " , a.contactor2_relation   " + " , a.contactor2_area_code  " + " , a.contactor2_tel  "
            + " , a.contactor2_ext  " + " , a.est_graduate_month    " + " , a.market_agree_base     "
            + " , a.vacation_code   " + " , a.market_agree_act      " + " , a.fancy_limit_flag      "
            + " , a.combo_indicator       " + " , a.stat_send_internet    " + " , a.dc_indicator    "
            + " , a.curr_code       " + " , a.act_no_f        " + " , a.act_no_f_ind    "
            + " , a.agree_l_ind     " + " , a.act_no_l        " + " , a.act_no_l_ind    "
            + " , a.send_pwd_flag   " + " , a.jcic_score      " + " , a.mno_id    "
            + " , a.service_type    " + " , a.msisdn    " + " , a.se_id     "
            + " , a.last_mail_zip   " + " , a.last_mail_addr1       " + " , a.last_mail_addr2       "
            + " , a.last_mail_addr3 " + " , a.last_mail_addr4       " + " , a.last_mail_addr5       "
            + " , a.sms_amt         " + " , b.combo_indicator       " + " , c.card_mold_flag        "
        //  + " , c.service_id    " move to crd_item_unit 
            + " , a.rowid      as rowid "
            + " , a.e_news    " + " , a.promote_emp_no    " + " , a.promote_dept     "
            + " , a.card_ref_num    " + " , a.introduce_emp_no    " + " , a.ur_flag     "
            + " , a.spouse_id_no    " + " , a.spouse_birthday    " + " , a.resident_no_expire_date     "
            + " , a.passport_date    " + " , a.roadside_assist_apply    " + " , a.bill_apply_flag     "
            + " , a.revolve_int_rate    " + " , a.company_zip    " + " , a.special_card_rate     "
            + " , a.autopay_acct_bank    " + " , a.inst_flag    " + " , a.fee_code_i     "
            + " , a.graduation_elementarty    " + " , a.spouse_name    " + " , a.company_addr1     "
            + " , a.company_addr2    " + " , a.company_addr3    " + " , a.company_addr4     "
            + " , a.company_addr5    " + " , a.curr_change_accout    " + " , a.credit_level_new     "
            + " , a.crt_bank_no      " + " , a.vd_bank_no    " + " ,a.clerk_id " + " ,a.ap1_apply_date "
            + " , a.revolve_int_rate_year_code " + " , a.revolve_int_rate_year ";
  daoTable = "ptr_group_card c, crd_emap_tmp a, ptr_group_code b ";
  whereStr = "where a.batchno      like  ? " 
           + "  and a.source       like  ? "
           + "  and b.group_code   = a.group_code " 
           + "  and c.group_code   = a.group_code "
           + "  and c.card_type    = a.card_type  " 
           + "  and a.emboss_date  = '' " 
           + "  and a.nccc_batchno = '' "
           + "  and a.card_no     <> '' " 
           + "  and a.check_code     = '000' " 
           + "  and a.oth_chk_code   = '0' "
           + " order by a.acct_type,a.card_type,a.unit_code,a.card_no ";

  setString(1, hBatchno + "%");
  setString(2, hEmbossSource + "%");

  openCursor();

  while (fetchTable()) {
      initRtn();

      gcrdCardMoldFlag = getValue("card_mold_flag");
      mbosComboIndicator = getValue("combo_indicator");
      mbosSourceBatchno = getValue("batchno");
      mbosEmbossSource = getValue("source");
      mbosSourceRecno = getValueDouble("recno");
      mbosCreditLmt = getValueInt("credit_lmt");
      mbosPoliceNo1 = getValue("police_no1");
      mbosPoliceNo2 = getValue("police_no2");      
      mbosCardNo = getValue("card_no");
      mbosCardType = getValue("card_type");
      mbosGroupCode = getValue("group_code");
      mbosBinNo = getValue("bin_no");
      mbosUnitCode = getValue("unit_code");
      mbosOnlineMark = getValue("online_mark");
      mbosCardcat = getValue("cardcat");
      mbosElectronicCode = getValue("electronic_code");

      mbosEmbossReason = "1";
      mbosAcctKey = getValue("apply_id") + getValue("apply_id_code");
      if (getValue("pm_id").trim().compareTo(getValue("apply_id")) != 0) {
          mbosAcctKey = getValue("pm_id") + getValue("pm_id_code");
      }
      emapRowid = getValue("rowid");
      hENews = getValue("e_news");
      hPromoteEmpNo = getValue("promote_emp_no");
      hPromoteDept = getValue("promote_dept");
      hCardRefNum = getValue("card_ref_num");
      hIntroduceEmpNo = getValue("introduce_emp_no");
      hUrFlag = getValue("ur_flag");
      hSpouseIdNo = getValue("spouse_id_no");
      hSpouseBirthday = getValue("spouse_birthday");
      hResidentNoExpireDate = getValue("resident_no_expire_date");
      hPassportDate = getValue("passport_date");
      hRoadsideAssistApply = getValue("roadside_assist_apply");
      hBillApplyFlag = getValue("bill_apply_flag");
      hRevolveIntRate = getValueDouble("revolve_int_rate");
      hCompanyZip = getValue("company_zip");
      hSpecialCardRate = getValueDouble("special_card_rate");
      hAutopayAcctBank = getValue("autopay_acct_bank");
      hInstFlag = getValue("inst_flag");
      hFeeCodeI = getValue("fee_code_i");
      hGraduationElementarty = getValue("graduation_elementarty");
      hSpouseName = getValue("spouse_name");
      hCompanyAddr1 = getValue("company_addr1");
      hCompanyAddr2 = getValue("company_addr2");
      hCompanyAddr3 = getValue("company_addr3");
      hCompanyAddr4 = getValue("company_addr4");
      hCompanyAddr5 = getValue("company_addr5");
      hCurrChangeAccout = getValue("curr_change_accout");
      hCreditLevelNew = getValue("credit_level_new");
      hBranch = getValue("branch");
      hCrtBankNo = getValue("crt_bank_no");
      hVdBankNo = getValue("vd_bank_no");
      hCurrCode = getValue("curr_code");
      hSendNumFlag = getValue("send_pwd_flag");
      hClerkId = getValue("clerk_id");
      hSmsAmt = getValueInt("sms_amt");
      hAp1ApplyDate = getValue("ap1_apply_date");
      hRevolveIntRateYearCode = getValue("revolve_int_rate_year_code");
      hRevolveIntRateYear = getValueDouble("revolve_int_rate_year");
      hEmboss4thData = getValue("emboss_4th_data");

      mbosRecno++;

      totalCnt++;
      processDisplay(5000); // every nnnnn display message
      if (debug == 1) {
          showLogMessage("I", "", "  888 Card=[" + mbosCardNo + "]" + mbosGroupCode);
          showLogMessage("I", "", "  888  key=[" + mbosAcctKey + "]");
      }
      getCrdItemUnit();

      mbosToNcccCode = "Y"; // default 非緊急
      switch (mbosOnlineMark.trim()) {
      case "0":
          mbosToNcccCode = "Y";
          break;
      case "1":
      case "2":
          mbosToNcccCode = "N";
          break;
      }
      if (debug == 1)
          showLogMessage("I", "", "  888 code=[" + mbosToNcccCode +","+ mbosOnlineMark);

      tmpInt = insertCrdEmboss();

      /**********************************************************
      * 正卡combo卡申請第三軌資料 COMBO_INDICATOR 改不等於N 加入三合一卡
      **********************************************************/
      if (mbosComboIndicator.trim().compareTo("N") != 0) {
          mbosDiffCode = "1";
      /**********************************************
      * 緊急卡/重送件不需申請第三軌
      **********************************************/
          if (mbosOnlineMark.trim().equals("0")) {
              checkCrdCombo();
          }
      }
      deleteCrdEmapTmp();
/*  lai test
*/
  }
}
// ************************************************************************
    public int getNcccBatchno() throws Exception {
    //  String batchno_h = h_busi_business_date.substring(2, 8);
        String batchnoH = sysDate.substring(2,8);

        selectSQL = "max(batchno) as max_batchno ";
        daoTable  = "crd_emboss ";
        whereStr  = "WHERE batchno    like  ?  " 
                  + "  and nccc_type     = '1' " 
                  + "  and to_nccc_date  = ''  ";

        setString(1, batchnoH + "%");

        tmpInt = selectTable();

        mbosBatchno = getValue("max_batchno");
        if (debug == 1)
            showLogMessage("I", "", " 888 get bat=[" + mbosBatchno + "]" + batchnoH);
        if (mbosBatchno.length() > 0) {
            selectSQL = "max(recno)   as recno ";
            daoTable  = "crd_emboss ";
            whereStr  = "WHERE batchno       =  ?  ";
            setString(1, mbosBatchno);

            tmpInt = selectTable();

            mbosRecno = getValueDouble("recno");

            return (0);
        }

        /* data not found */
        selectSQL = "max(batchno) as max_batchno ";
        daoTable  = "crd_emboss ";
        whereStr  = "WHERE batchno    like  ?  ";

        setString(1, batchnoH + "%");

        tmpInt = selectTable();

        mbosBatchno = getValue("max_batchno");

        if (mbosBatchno.length() > 0) {
            tmpLong = Long.parseLong(mbosBatchno.trim()) + 1;
            mbosBatchno = String.format("%08d", tmpLong);
        } else {
            mbosBatchno = batchnoH + "01";
        }

if (debug == 1) showLogMessage("I", "", " 888 return bat=[" + mbosBatchno + "]");

        return (0);
    }

    // ************************************************************************
    public int getCrdItemUnit() throws Exception {
        selectSQL = " service_id     ";
        daoTable = "crd_item_unit";
        whereStr = "WHERE unit_code =  ? ";
        setString(1, mbosUnitCode);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_crd_item_unit  error! =" + mbosCardType +","+ mbosUnitCode;
            comcr.errRtn(err1, mbosUnitCode, hCallBatchSeqno);
        }

        mbosServiceId = getValue("service_id");

        return (0);
    }

    // ************************************************************************
    public int getPtServiceVer() throws Exception {
        selectSQL = "service_ver       ";
        daoTable  = "ptr_service_ver a, ptr_bintable b ";
        whereStr  = "where a.bin_type   = b.bin_type " 
                  + "  and b.bin_no     = ?          " 
                  + "FETCH FIRST 1 ROW ONLY";

        setString(1, mbosBinNo);

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_service_ver error!";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        mbosServiceVer = getValue("service_ver");

        if (debug == 1)
            showLogMessage("I", "", " Type =[" + mbosCardType + "]=" + mbosServiceVer);

        return (0);
    }

    // ************************************************************************
    public int insertCrdEmboss() throws Exception {

        /*
         * lai test gcrd_card_mold_flag = "M";
         */

        if (debug == 1)
            showLogMessage("I", "", "   MOLD flag=["+ gcrdCardMoldFlag +"]" + mbosCreditLmt +","+ mbosToNcccCode);

        if (gcrdCardMoldFlag.trim().compareTo("M") == 0) {
            getPtServiceVer();
        }

        setValue("batchno"           , mbosBatchno);
        setValueDouble("recno"       , mbosRecno);
        setValue("source_batchno"    , mbosSourceBatchno);
        setValueDouble("source_recno", mbosSourceRecno);
        setValueInt("credit_lmt"     , mbosCreditLmt);
        setValue("service_ver"       , mbosServiceVer);
        setValue("org_emboss_data"   , hEmboss4thData);
        setValue("emboss_source"     , mbosEmbossSource); // 新製卡
        setValue("acct_key", mbosAcctKey);
        setValue("sup_flag", "0");
        if (getValue("pm_id").trim().compareTo(getValue("apply_id")) != 0)
            setValue("sup_flag"   , "1");
        setValue("service_id"     , mbosServiceId);
        setValue("cardcat"        , mbosCardcat);
        setValue("electronic_code", mbosElectronicCode);
        setValue("to_nccc_code"   , mbosToNcccCode);
        setValue("to_vendor_date" , "29991231");
        setValue("status_code"    , ""        );
        setValue("bin_no"         , mbosBinNo);
        setValue("e_news"         , hENews);
        setValue("promote_emp_no"        , hPromoteEmpNo);
        setValue("promote_dept"          , hPromoteDept);
        setValue("card_ref_num"          , hCardRefNum);
        setValue("introduce_emp_no"      , hIntroduceEmpNo);
        setValue("ur_flag"               , hUrFlag);
        setValue("spouse_id_no"          , hSpouseIdNo);
        setValue("spouse_birthday"       , hSpouseBirthday);
        setValue("resident_no_expire_date"         , hResidentNoExpireDate);
        setValue("passport_date"         , hPassportDate);
        setValue("roadside_assist_apply"           , hRoadsideAssistApply);
        setValue("bill_apply_flag"       , hBillApplyFlag);
        setValueDouble("revolve_int_rate"          , hRevolveIntRate);
        setValue("company_zip"           , hCompanyZip);
        setValueDouble("special_card_rate"         , hSpecialCardRate);
        setValue("autopay_acct_bank"     , hAutopayAcctBank);
        setValue("inst_flag"             , hInstFlag);
        setValue("fee_code_i"            , hFeeCodeI);
        setValue("graduation_elementarty"         , hGraduationElementarty);
        setValue("spouse_name"           , hSpouseName);
        setValue("company_addr1"         , hCompanyAddr1);
        setValue("company_addr2"         , hCompanyAddr2);
        setValue("company_addr3"         , hCompanyAddr3);
        setValue("company_addr4"         , hCompanyAddr4);
        setValue("company_addr5"         , hCompanyAddr5);
        setValue("curr_change_accout"    , hCurrChangeAccout);
        setValue("credit_level_new"      , hCreditLevelNew);
        setValue("branch"                , hBranch);
        setValue("mail_branch"           , hBranch);
        setValue("crt_bank_no"           , hCrtBankNo);
        setValue("vd_bank_no"            , hVdBankNo);
        setValue("curr_code"             , hCurrCode);
        setValue("send_pwd_flag"         , hSendNumFlag);
        setValue("clerk_id"              , hClerkId);
        setValueInt("sms_amt"            , hSmsAmt);
        setValue("ap1_apply_date"        , hAp1ApplyDate);
        setValue("revolve_int_rate_year_code"  , hRevolveIntRateYearCode);
        setValueDouble("revolve_int_rate_year" , hRevolveIntRateYear);
        setValue("emboss_4th_data"       , hEmboss4thData);
        setValue("police_no1"            , mbosPoliceNo1);
        setValue("police_no2"            , mbosPoliceNo2);
        
        setValue("crt_date", sysDate);
        setValue("apr_date", sysDate);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm" , javaProgram);

        daoTable = "crd_emboss";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_emboss    error[dupRecord]=" + mbosBatchno;
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }
        return 0;

    }

    // ************************************************************************
    public int checkCrdCombo() throws Exception {
        switch (mbosEmbossSource.trim()) {
        case "1":
            hTransType = "01";
            break; // * 新製卡
        case "3":
        case "4":
            hTransType = "08";
            break; // * 續卡
        case "5":
            if (mbosEmbossReason.equals("1"))
                hTransType = "02"; // * 掛失重製
            if (mbosEmbossReason.equals("2"))
                hTransType = "07"; // * 毀損重製
            if (mbosEmbossReason.equals("3"))
                hTransType = "04"; // * 偽卡重製
            break;
        }

        selectSQL = "rowid  as comb_rowid ";
        daoTable = "crd_combo";
        whereStr = "where card_no    = ?  ";

        setString(1, mbosCardNo);

        int recordCnt = selectTable();

        combRowid = getValue("comb_rowid");

        if (recordCnt > 0) {
            updateCrdCombo();
        } else {
            insertCrdCombo();
        }

        return 0;
    }

    // ************************************************************************
    public int updateCrdCombo() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " update combo  =[" + "" + "]");
        updateSQL = "apply_date          =  ? , " + "trans_type          =  ? , " 
                  + "batchno             =  ? , " + "recno               =  ? , " 
                  + "old_card_no         =  ? , " + "saving_actno        =  ? , "
                  + "third_data          = '' , " + "ic_pin              = '' , "
                  + "to_ibm_date         =  ? , " + "rtn_ibm_date        =  ? , "
                  + "rtn_code            = '000' , " + "fail_proc_code      = '' , "
                  + "fail_proc_date      = '' , " + "to_nccc_date        = '' , "
                  + "rtn_nccc_date       = '' , " + "reject_code         = '' , "
                  + "emboss_code         = '' , " + "emboss_date         = '' , "
                  + "end_ibm_date        = '' , " + "end_rtn_code        = '' , "
                  + "end_rtn_date        = '' , " + "send_prn_date       = '' , "
                  + "bank_actno          = '' , " + "mod_user            = ? , "
                  + "mod_pgm             =  ? , " 
                  + "mod_time            = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS')";
        daoTable = "crd_combo";
        whereStr = "where rowid   = ? ";

        setString(1, sysDate);
        setString(2, hTransType);
        setString(3, mbosBatchno);
        setDouble(4, mbosRecno);
        setString(5, getValue("old_card_no"));
        setString(6, getValue("act_no"));
        setString(7, sysDate);
        setString(8, sysDate);
        setString(9, comc.commGetUserID());
        setString(10, javaProgram);
        setString(11, sysDate + sysTime);
        setRowId(12, combRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_combo     error[notFind]";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public int insertCrdCombo() throws Exception {

        if (debug == 1)
            showLogMessage("I", "", " insert combo  =[" + "" + "]");
        setValue("sup_flag", "0");
        if (getValue("pm_id").trim().compareTo(getValue("apply_id")) != 0)
            setValue("sup_flag", "1");

        setValue("card_no"       , mbosCardNo);
        setValue("apply_id"      , getValue("apply_id"));
        setValue("apply_id_code" , getValue("apply_id_code"));
        setValue("birthday"      , getValue("birthday"));
        setValue("pm_id"         , getValue("pm_id"));
        setValue("pm_id_code"    , getValue("pm_id_code"));
        setValue("old_card_no"   , getValue("old_card_no"));
        setValue("apply_date"    , sysDate);
        setValue("batchno"       , mbosBatchno);
        setValueDouble("recno"   , mbosRecno);
        setValue("trans_type"    , hTransType);
        setValue("saving_actno"  , getValue("act_no"));
        setValue("third_data"    , "");
        setValue("ic_pin"        , "");
        setValue("to_ibm_date"   , sysDate);
        setValue("rtn_ibm_date"  , sysDate);
        setValue("rtn_code"      , "000");
        setValue("fail_proc_code", "");
        setValue("fail_proc_date", "");
        setValue("to_nccc_date"  , "");
        setValue("rtn_nccc_date" , "");
        setValue("reject_code"   , "");
        setValue("emboss_code"   , "");
        setValue("emboss_date"   , "");
        setValue("end_ibm_date"  , "");
        setValue("end_rtn_code"  , "");
        setValue("end_rtn_date"  , "");
        setValue("send_prn_date" , "");
        setValue("bank_actno"    , "");
        setValue("mod_user"      , comc.commGetUserID()); 
        setValue("crt_date"      , sysDate);
        setValue("mod_time"      , sysDate + sysTime);
        setValue("mod_pgm"       , javaProgram);

        daoTable = "crd_combo";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_combo    error[dupRecord]";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public int deleteCrdEmapTmp() throws Exception {

        if (debug == 1)
            showLogMessage("I", "", " delete emap   =[" + "ori" + "]");

        daoTable = "crd_emap_tmp";
        whereStr = "WHERE rowid    = ? ";

        setRowId(1, emapRowid);

        int recCnt = deleteTable();

        if (notFound.equals("Y")) {
            String err1 = "delete_crd_emap_tmp error[not find]";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public void initRtn() throws Exception {
        hBatchno = "";
        hEmbossSource = "";
        hSuperId = "";
        hTransId = "";
        emapRowid = "";
        combRowid = "";
        
        hENews = "";
        hPromoteEmpNo = "";
        hPromoteDept = "";
        hCardRefNum = "";
        hIntroduceEmpNo = "";
        hUrFlag = "";
        hSpouseIdNo = "";
        hSpouseBirthday = "";
        hResidentNoExpireDate = "";
        hPassportDate = "";
        hRoadsideAssistApply = "";
        hBillApplyFlag = "";
        hRevolveIntRate = 0.0;
        hCompanyZip = "";
        hSpecialCardRate = 0.0;
        hAutopayAcctBank = "";
        hInstFlag = "";
        hFeeCodeI = "";
        hGraduationElementarty = "";
        hSpouseName = "";
        hCompanyAddr1 = "";
        hCompanyAddr2 = "";
        hCompanyAddr3 = "";
        hCompanyAddr4 = "";
        hCompanyAddr5 = "";
        hCurrChangeAccout = "";
        hCreditLevelNew = "";
        hBranch = "";
        hCrtBankNo = "";
        hVdBankNo = "";

        mbosDiffCode = "";
        mbosToNcccCode = "";
        mbosComboIndicator = "";
        mbosOnlineMark = "";
        mbosCardNo = "";
        mbosSourceBatchno = "";
        mbosSourceRecno = 0;
        mbosServiceVer = "";
        mbosEmbossSource = "";
        mbosEmbossReason = "";
        mbosServiceVer = "";
        mbosCardType = "";
        mbosUnitCode = "";
        mbosServiceId = "";
        mbosAcctKey = "";
        mbosCreditLmt = 0;
        mbosPoliceNo1 = "";
        mbosPoliceNo2 = "";
        mbosCardcat = "";
        mbosElectronicCode = "";
        gcrdCardMoldFlag = "";
        hTransType = "";
    }
    // ************************************************************************

} // End of class FetchSample
