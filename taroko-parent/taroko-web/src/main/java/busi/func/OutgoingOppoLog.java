/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-06-16  V1.00.01  Ryan       updated for project coding standard      *
 *  109/12/30  V1.00.02    Zuwei       “icbcecs”改為”system”     *
* 111-01-18  V1.00.03 JustinWu    fix Throw Inside Finally                  *
* 111-04-12  V1.00.04 JustinWu    "select" -> "update" crd_card set         * 
******************************************************************************/
package busi.func;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import busi.FuncBase;

public class OutgoingOppoLog extends FuncBase {

    String prgmId = modPgm;

    String hOnbaCardNo = "";
    String hOnbaOppType = "";
    String hOnbaOppReason = "";
    String hOnbaOppDate = "";
    String hOnbaIsRenew = "";
    String hOnbaIsEm = "";
    String hOnbaDog = "";
    String hOnbaRowid = "";
    String hDebitFlag = "";
    String hCardIdPSeqno = "";
    String hCardPSeqno = "";
    String hCardGpNo = "";
    String hMbtmIcFlag = "";
    String hCardComboIndicator = "";
    String hCardComboAcctNo = "";
    String hCardReissueReason = "";
    String hCardReissueStatus = "";
    String hCardReissueDate = "";
    String hCardCurrentCode = "";
    String hCardOldBankActno = "";
    String hCarBankActno = "";
    String hCardNewCardNo = "";
    String hMbtmAcctType = "";
    String hMbtmCardType = "";
    String hMbtmSupFlag = "";
    String hMbtmUnitCode = "";
    String hMbtmBinNo = "";
    String hMbtmRegBankNo = "";
    String hBbtmEngName = "";
    String hMbtmMemberNote = "";
    String hMbtmMemberId = "";
    String hMbtmChangeReason = "";
    String hCardMajorIdPSeqno = "";
    String hMbtmMajorCardNo = "";
    String hMbtmGroupCode = "";
    String hMbtmSourceCode = "";
    String hMbtmCorpNo = "";
    String hMbtmCorpNoCode = "";
    String hMbtmForceFlag = "";
    String hMbtmOldBegDate = "";
    String hMbtmOldEndDate = "";
    String hMbtmEmboss4thData = "";
    String hCardMailType = "";
    String hCardMailNo = "";
    String hCardMailBranch = "";
    String hCardMailProcDate = "";
    String hCardExpireChgFlag = "";
    String hCardExpireChgDate = "";
    String hCardExpireReason = "";
    String hCardCurrCode = "";
    String hCardJcicScore = "";
    String hMbtmCreateDate = "";
    String hMbtmSuperId = "";
    String hMbtmSuperDate = "";
    double hMbtmModSeqno = 0;
    int tempInt = 0;
    String hMbtmMajorValidFm = "";
    String hMbtmMajorValidTo = "";
    String hMbtmBirthday = "";
    String hMbtmChiName = "";
    String hMbtmValidFm = "";
    String hMbtmRiskBankNo = "";
    String hMbtmCreditLmt = "";
    String hAcnoComboIndicator = "";
    String hAcnoComboAcctNo = "";
    String hAcnoStopStatus = "";
    String hIsRc = "";
    String hAcnoGpNo = "";
    String hAcnoPSeqno = "";
    String hAcnoRcUseIndicator = "";
    String hAcnoDebtCloseDate = "";
    String hCardAcctType = "";
    String pCardNo = "";
    String pCurrentCode = "";
    String pSupFlag = "";
    String pComboIndicator = "";
    String pComboAcctNo = "";
    int hPtrExtnYear = 0;
    String hDiffDate = "";
    String hEndDate = "";
    String hValidFm = "";
    String hValidTo = "";
    int hTmpNo = 0;
    long hTmpRecno = 0;
    String hTmpDate = "";
    int hCount = 0;
    String hMbtmOldCardNo = "";
    String hMbtmComboIndicator = "";
    String hMbosMnoId = "";
    String hMbosMsisdn = "";
    String hMbosServiceId = "";
    String hMbosSeId = "";
    String hMbosServiceVer = "";
    String hMbosServiceType = "";
    String hMbosSirNo = "";
    String hMbtmBatchno = "";
    long hMbtmRecno = 0;
    String hMbtmEmbossSource = "";
    String hMbtmEmbossReason = "";
    String hMbtmResendNote = "";
    String hMbtmToNcccCode = "";
    String hMbtmStatusCode = "";
    String hMbtmReasonCode = "";
    String hMbtmApplyId = "";
    String hMbtmApplyIdCode = "";
    String hMbtmValidTo = "";
    String hMbtmChgAddrFlag = "";
    String hMbtmMailType = "";
    String hMbtmEmbossDate = "";
    String hMbtmNcccBatchno = "";
    int hMbtmNcccRecno = 0;
    String hMbtmNcccType = "";
    String hReissueStatus = "";
    String hReissueReason = "";
    String hReissueDate = "";
    String hMbosEmbossSource = "";
    String hMbosEmbossReason = "";
    String hTempSource = "";
    String hRowid = "";
    String hPaymentDate = "";
    String hSystemDate  = "";
    String hNextDate    = "";
    String hApscStopReason = "";
    String hApscStopDate = "";
    String hApscStatusCode = "";
    String pmChiName = "";
    String pmBirthday = "";
    String hApscCardNo = "";
    String hApscValidDate = "";
    String hApscReissueDate = "";
    String hApscMailType = "";
    String hApscMailNo = "";
    String hApscMailBranch = "";
    String hApscMailDate = "";
    String hApscPmId = "";
    String hApscPmIdCode = "";
    String hApscPmBirthday = "";
    String hApscSupId = "";
    String hApscSupIdCode = "";
    String hApscSupBirthday = "";
    String hApscCorpNo = "";
    String hApscCorpNoCode = "";
    String hApscCardType = "";
    String hApscPmName = "";
    String hApscSupName = "";
    String hApscSupLostStatus = "";
    String hApscGroupCode = "";
    int hStatus = 0;
    String hProcSeqno = "";
    String pTransType = "";
    String hStopBankActno = "";
    String hProcCode = "";
    String hBusiBusinessDate = "";
    int hSystemDd = 0;
    String hEriaRefIp = "";
    String hEriaPortNo = "";
    String hEriaEcsIp = "";
    String hHstnVCardNo = "";
    String hHstnStatusCode = "";
    String hHstnSirUser = "";
    String hHstnSir = "";
    String hIardIpsCardNo = "";
    String hIardIchCardNo = "";
    String hIardCurrentCode = "";
    String hIardNewEndDate = "";
    String hMbtmCardNo = "";
    String hGcrdCardMoldFlag = "";
    String hBatchno = "";
    String hTardTscCardNo  = "";
    String hTardCurrentCode = "";
    String hTardNewEndDate = "";
    String hB09bBalRsn      = "";
    String hRetrRefNo       = "";
    int     tempInt04=0;
    int     tempInt09=0;

    int hInsOk = 0;
    int hDelOk = 0;
    long hRecno = 0;
    int tscCnt = 0;
    int totCnt = 0;
    int rtn = 0;
    
    String sqlCmd = "";
    String cardNo ="";
    
    public void insertOppoLog(String cardNo,String actCode) throws Exception{
    	this.cardNo = cardNo;
    	if(!(actCode.equals("1")||actCode.equals("2"))){
    		return;
    	}

        selectPtrBusinday();
        selectEcsRefIpAddr();
        getBatchno();
        selectOnbat2ecs();

    }
    
    /***********************************************************************/
    void selectPtrBusinday() throws Exception 
    {
            sqlCmd = " select business_date ";
            sqlCmd += "to_char(sysdate,'yyyymmdd')       h_system_date,";
            sqlCmd += "to_char(sysdate+1,'yyyymmdd')     h_next_date,";
            sqlCmd += "to_number(to_char(sysdate,'dd'))  h_system_dd ";
            sqlCmd += " from ptr_businday ";

            sqlSelect(sqlCmd);
            if (sqlRowNum > 0) {
                hBusiBusinessDate = colStr("business_date");
                hSystemDate        = colStr("h_system_date");
                hNextDate          = colStr("h_next_date");
                hSystemDd          = colInt("h_system_dd");;
            }
    }
    
    /***********************************************************************/
    void selectEcsRefIpAddr() throws Exception {

        hEriaEcsIp = "";

        sqlCmd = "select ref_ip, port_no";
        sqlCmd += "  from ecs_ref_ip_addr  ";
        sqlCmd += " where ref_ip_code = 'APPC' ";
        sqlSelect(sqlCmd);

        if (sqlRowNum > 0) {
            hEriaRefIp = colStr("ref_ip");
            hEriaPortNo = colStr("port_no");
        }
        
    }
    
    /***************************************************************/
    void getBatchno() throws Exception 
    {
       String hTmpDate = "";
       String hTmpBatchno = "";
       long hTmpRecno=0;
       int  hTmpNo=0;
      

       sqlCmd  = " select to_number(nvl(substr(max(batchno),7,2),0)) as h_tmp_no, ";
       sqlCmd += "        nvl(max(recno),0) as h_tmp_recno, ";
       sqlCmd += "        to_char(sysdate+1,'yymmdd') as h_tmp_date ";
       sqlCmd += "   from crd_emboss_tmp ";
       sqlCmd += "  where substr(batchno,1,6) = to_char(sysdate+1,'yymmdd') ";
       sqlSelect(sqlCmd);
       
       if(sqlRowNum > 0 ) {
    	   hTmpNo    = colInt("h_tmp_no");
    	   hTmpRecno = Long.parseLong(empty(colStr("h_tmp_recno"))?"0":colStr("h_tmp_recno"));
    	   hTmpDate  = colStr("h_tmp_date");
       }

       if(hTmpNo == 0)
       {
         hTmpNo = 1;
         hTmpRecno = 0;
       }
       hTmpBatchno = String.format("%s%02d", hTmpDate, hTmpNo);
       hBatchno = hTmpBatchno; 
       hRecno=hTmpRecno;

    }
    
    /***********************************************************************/
    void selectOnbat2ecs() throws Exception {
        int errorFlag = 0;
        HashMap<String,String>  onbat2ecsData  = new HashMap<String,String>();
        
        sqlCmd = "select ";
        sqlCmd += "card_no,";
        sqlCmd += "opp_type,";
        sqlCmd += "opp_reason,";
        sqlCmd += "opp_date,";
        sqlCmd += "decode(is_renew,'','N',is_renew) h_onba_is_renew,";
        sqlCmd += "is_em,";
        sqlCmd += "to_char(dog,'hh24miss') h_onba_dog,";
        sqlCmd += "hex(rowid)  as rowid ";
        sqlCmd += " from onbat_2ecs ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and to_which    = '1' ";
        sqlCmd += "  and trans_type  = '6' ";
        sqlCmd += "  and proc_status = '0' ";
        sqlCmd += "  and card_no = ? ";
        //sqlCmd += "  and opp_date = to_char(sysdate,'yyyymmdd') ";
        sqlCmd += "order by card_no,dog ";
        setString2(1,cardNo);
        sqlSelect(sqlCmd);
        
        int recordCnt = sqlRowNum;
        
        for (int i = 0; i < recordCnt; i++) {
        	onbat2ecsData.put("card_no"+i, colStr(i,"card_no"));
        	onbat2ecsData.put("opp_type"+i, colStr(i,"opp_type"));
        	onbat2ecsData.put("opp_reason"+i, colStr(i,"opp_reason"));
        	onbat2ecsData.put("opp_date"+i, colStr(i,"opp_date"));
        	onbat2ecsData.put("h_onba_is_renew"+i, colStr(i,"h_onba_is_renew"));
        	onbat2ecsData.put("is_em"+i, colStr(i,"is_em"));
        	onbat2ecsData.put("h_onba_dog"+i, colStr(i,"h_onba_dog"));
        	onbat2ecsData.put("rowid"+i, colStr(i,"rowid"));
        }

        for (int i = 0; i < recordCnt; i++) {
            hOnbaCardNo    = onbat2ecsData.get("card_no"+i);
            hOnbaOppType   = onbat2ecsData.get("opp_type"+i);
            hOnbaOppReason = onbat2ecsData.get("opp_reason"+i);
            hOnbaOppDate   = onbat2ecsData.get("opp_date"+i);
            hOnbaIsRenew   = onbat2ecsData.get("h_onba_is_renew"+i);
            hOnbaIsEm      = onbat2ecsData.get("is_em"+i);
            hOnbaDog        = onbat2ecsData.get("h_onba_dog"+i);
            hOnbaRowid      = onbat2ecsData.get("rowid"+i);
            if (empty((strMid(hOnbaCardNo, 0, 2))) || (hOnbaCardNo.length() == 0)) {
                hStatus = 2;
                updateOnbat2ecs();
                continue;
            }
            hDebitFlag = "Y";
            sqlCmd = "select 'N' as  h_debit_flag ";
            sqlCmd += "  from crd_card  a ";
            sqlCmd += " where a.card_no   = ? ";
            setString2(1, hOnbaCardNo);
            sqlSelect(sqlCmd);
            if (sqlRowNum > 0) {
                hDebitFlag = colStr("h_debit_flag");
            }
            if (hDebitFlag.equals("Y")) {
                continue;
            }

            hInsOk = 0;
            hDelOk = 0;

            hStatus = 0;
            errorFlag = 0;
            errorFlag = getCardData();
           
            if (errorFlag != 0) {
                hStatus = 2;
                if (errorFlag == 2)
                    hStatus = 1;
                updateOnbat2ecs();
                continue;
            }
            /********************************************
             * 2001/07/02 掛失和偽卡需檢核是否有有效卡, 若無,則UPDATE act_acno
             ********************************************/
            if ((hOnbaOppType.equals("2")) || (hOnbaOppType.equals("5"))) {
                errorFlag = chkValidCard();
               
                if (errorFlag != 0) {
                    errorFlag = updateActAcno();
                  
                    if (errorFlag != 0) {
                        hStatus = 2;
                        updateOnbat2ecs();
                        continue;
                    }
                }
            }
    
            /******************************************
             * 掛失及偽卡若以重製過不可再補發 (current_code != '0') modified by shu 2002/08/25
             ******************************************/
            if (hOnbaIsRenew.equals("Y")) {
                /******************************************************************
                 * 只接收opp_type='2' or opp_type='5'才可做重製 (2002/01/02)
                 *****************************************************************/
                if ((hOnbaOppType.equals("2")) || (hOnbaOppType.equals("5"))) {
                    hRecno++;
                    /** 若已重製過不可再補發或重製 **/
                    if (hCardNewCardNo.length() == 0) {
                        if (!hCardReissueStatus.equals("2")) {
                            insertCrdEmbossTmp();
                        }
                    }
                }
            }
           
            /********************************************************************
             * 改成不重製(不管current_code為何)delete crd_Emboss_tmpe 2001/10/22
             ********************************************************************/
           
            if (hOnbaIsRenew.equals("N")) {
                delCrdEmbossTmp();
            }
          
            processApscard();
            
            hPaymentDate = "";
            if (strMid(hOnbaOppReason, 0, 1).equals("U")) {
                if (hAcnoDebtCloseDate.length() > 0) {
                    hPaymentDate = hAcnoDebtCloseDate;
                    insertCrdJcic();
                }
            } else {
                insertCrdJcic();
            }
            
            /* 1:一般停用 2:掛失停用 3:強制停用 4:其他停用 5:偽卡停用 */
            /* 0: 是不是恢復正常 */
            
            if((hOnbaOppType.equals("0")) || (hOnbaOppType.equals("2")) || (hOnbaOppType.equals("5"))) 
              {
                if ((hOnbaOppType.equals("0")) || (hOnbaOppType.equals("2"))) {
                    selectTscCard();
                    selectIchCard(1);
                    if((hOnbaOppType.equals("0")) && hIardIchCardNo.length() > 0)
                      {
                       deleteIchRtn();
                      }
                }
                if ((hOnbaOppType.equals("2")) || (hOnbaOppType.equals("5"))) {
                    selectIpsCard();
                    hIardIchCardNo  = "";
                    selectIchCard(3);
                    if(hOnbaOppType.equals("5"))
                      { hB09bBalRsn   = "02"; }
                    else
                      { hB09bBalRsn   = "03"; }
                    if(hIardIchCardNo.length() > 0)
                      {
                       insertIchB04bSpecial();
                       insertIchB09bBal();
                      }
                }
                tscCnt++;
              }
            else
              {
               hIardIchCardNo  = "";
               selectIchCard(2);
               hB09bBalRsn   = "02"; 
               if(hOnbaOppType.equals("5") && hOnbaOppReason.equals("AI") &&
                  hIardIchCardNo.length() > 0)
                 { hB09bBalRsn   = "01"; }
               if(hIardIchCardNo.length() > 0)
                 {
                  insertIchB04bSpecial();
                  insertIchB09bBal();
                 }

              }
            
            chkStopStatus();
            totCnt++;

            rtn = selectHceCard();
            if (rtn == 0)
                insertHceStatusTxn();

            updateCrdCard();
            hStatus = 1;
            updateOnbat2ecs();
            
//            /* 20150413 三合一卡  call APPC  */
//          
//            if (!h_card_combo_indicator.equals("N")) {
//                process_appc();
//            }
         
        }
        
    }
    
    /***********************************************************************/
    int getCardData() throws Exception {

        sqlCmd = "select ";
        // sqlCmd += "id,";
        // sqlCmd += "id_code,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "p_seqno,";
        sqlCmd += "ic_flag,";
        sqlCmd += "decode(combo_indicator,'','N',combo_indicator) h_card_combo_indicator,";
        sqlCmd += "combo_acct_no,";
        sqlCmd += "reissue_reason,";
        sqlCmd += "reissue_status,";
        sqlCmd += "reissue_date,";
        sqlCmd += "current_code,";
        sqlCmd += "old_bank_actno,";
        sqlCmd += "bank_actno,";
        sqlCmd += "new_card_no,";
        sqlCmd += "acct_type,";
        // sqlCmd += "acct_key,";
        sqlCmd += "card_type,";
        sqlCmd += "sup_flag,";
        sqlCmd += "unit_code,";
        sqlCmd += "reg_bank_no,";
        sqlCmd += "eng_name,";
        sqlCmd += "member_note,";
        sqlCmd += "member_id,";
        sqlCmd += "change_reason,";
        // sqlCmd += "major_id,";
        // sqlCmd += "major_id_code,";
        sqlCmd += "major_id_p_seqno,";
        sqlCmd += "major_card_no,";
        sqlCmd += "group_code,";
        sqlCmd += "source_code,";
        sqlCmd += "corp_no,";
        sqlCmd += "corp_no_code,";
        sqlCmd += "force_flag,";
        sqlCmd += "new_beg_date,";
        sqlCmd += "new_end_date,";
        sqlCmd += "emboss_data,";
        sqlCmd += "mail_type,";
        sqlCmd += "mail_no,";
        sqlCmd += "mail_branch,";
        sqlCmd += "mail_proc_date,";
        sqlCmd += "expire_chg_flag,";
        sqlCmd += "expire_chg_date,";
        sqlCmd += "expire_reason,";
        sqlCmd += "curr_code,";
        sqlCmd += "jcic_score,";
        sqlCmd += "crt_date,";
        sqlCmd += "apr_user,";
        sqlCmd += "apr_date,";
        sqlCmd += "mod_seqno ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no = ? ";
        setString2(1, hOnbaCardNo);
        sqlSelect(sqlCmd);
        int recordCnt = sqlRowNum;
        if (recordCnt > 0) {
            hCardIdPSeqno = colStr("id_p_seqno");
            hCardPSeqno = colStr("acno_p_seqno");
            hCardGpNo = colStr("p_seqno");
            hMbtmIcFlag = colStr("ic_flag");
            hCardComboIndicator = colStr("h_card_combo_indicator");
            hCardComboAcctNo   = colStr("combo_acct_no");
            hCardReissueReason  = colStr("reissue_reason");
            hCardReissueStatus  = colStr("reissue_status");
            hCardReissueDate    = colStr("reissue_date");
            hCardCurrentCode    = colStr("current_code");
            hCardOldBankActno  = colStr("old_bank_actno");
            hCarBankActno      = colStr("bank_actno");
            hCardNewCardNo     = colStr("new_card_no");
            hMbtmAcctType       = colStr("acct_type");
            hMbtmCardType       = colStr("card_type");
            hMbtmSupFlag        = colStr("sup_flag");
            hMbtmUnitCode       = colStr("unit_code");
            hMbtmBinNo          = colStr("bin_no");
            hMbtmRegBankNo     = colStr("reg_bank_no");
            hBbtmEngName        = colStr("eng_name");
            hMbtmMemberNote     = colStr("member_note");
            hMbtmMemberId       = colStr("member_id");
            hMbtmChangeReason    = colStr("change_reason");
            hCardMajorIdPSeqno = colStr("major_id_p_seqno");
            hMbtmMajorCardNo   = colStr("major_card_no");
            hMbtmGroupCode      = colStr("group_code");
            hMbtmSourceCode     = colStr("source_code");
            hMbtmCorpNo         = colStr("corp_no");
            hMbtmCorpNoCode    = colStr("corp_no_code");
            hMbtmForceFlag      = colStr("force_flag");
            hMbtmOldBegDate    = colStr("new_beg_date");
            hMbtmOldEndDate    = colStr("new_end_date");
            hMbtmEmboss4thData = colStr("emboss_data");
            hCardMailType       = colStr("mail_type");
            hCardMailNo         = colStr("mail_no");
            hCardMailBranch     = colStr("mail_branch");
            hCardMailProcDate  = colStr("mail_proc_date");
            hCardExpireChgFlag = colStr("expire_chg_flag");
            hCardExpireChgDate = colStr("expire_chg_date");
            hCardExpireReason   = colStr("expire_reason");
            hCardCurrCode       = colStr("curr_code");
            hCardJcicScore      = colStr("jcic_score");
            hMbtmCreateDate     = colStr("crt_date");
            hMbtmSuperId        = colStr("apr_user");
            hMbtmSuperDate      = colStr("apr_date");
            hMbtmModSeqno       = colNum("mod_seqno");

        } else {
            sqlCmd = "select count(*) temp_int ";
            sqlCmd += " from ecs_crd_card  ";
            sqlCmd += "where card_no = ? ";
            sqlSelect(sqlCmd);
            recordCnt = sqlRowNum;
            if (recordCnt > 0) {
                tempInt = colInt("temp_int");
            }

            if (tempInt > 0)
                return (2);

            return (1);
        }

        hCardAcctType = hMbtmAcctType;
        /*************** 抓取正卡效期 ************************/
        if (hMbtmSupFlag.equals("1")) {
            sqlCmd = "select new_beg_date,";
            sqlCmd += "new_end_date ";
            sqlCmd += " from crd_card  ";
            sqlCmd += "where card_no = ? ";
            setString2(1, hMbtmMajorCardNo);
            sqlSelect(sqlCmd);
            recordCnt = sqlRowNum;
            if (recordCnt > 0) {
                hMbtmMajorValidFm = colStr("new_beg_date");
                hMbtmMajorValidTo = colStr("new_end_date");
            } else {
                return 1;
            }
        }

        sqlCmd = "select birthday,";
        sqlCmd += "chi_name ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno  = ?  ";
        setString2(1, hCardIdPSeqno);
        sqlSelect(sqlCmd);
        recordCnt = sqlRowNum;
        if (recordCnt > 0) {
            hMbtmBirthday = colStr("birthday");
            hMbtmChiName = colStr("chi_name");
        } else {
            return (1);
        }

        hAcnoStopStatus = "";
        hAcnoGpNo = "";
        hAcnoPSeqno = "";
        hAcnoRcUseIndicator = "";
        hAcnoDebtCloseDate = "";
        hMbtmCardNo = hOnbaCardNo;
        hMbtmOldCardNo = hOnbaCardNo;
        hAcnoComboIndicator = "";
        hAcnoComboAcctNo = "";
        getPtrExtn();
        if (hCardExpireChgFlag.length() == 0) {
            getValidDate();
        } else {
            hMbtmValidFm = hMbtmOldBegDate;
            hMbtmValidTo = hMbtmOldEndDate;
        }

        if (hSystemDd >= 25) {
            sqlCmd = "select to_char(add_months(to_date(?,'yyyymmdd'), 1) ,'yyyymmdd') h_mbtm_valid_fm ";
            sqlCmd += " from dual ";
            setString2(1, hMbtmValidFm);
            sqlSelect(sqlCmd);
            recordCnt = sqlRowNum;
            if (recordCnt > 0) {
                hMbtmValidFm = colStr("h_mbtm_valid_fm");
            }
        }

        String[] info = getIDInfo(hCardIdPSeqno);
        hMbtmApplyId = info[0];
        hMbtmApplyIdCode = info[1];
        hIsRc = "";
        sqlCmd = "select risk_bank_no,";
        sqlCmd += "line_of_credit_amt,";
        sqlCmd += "combo_indicator,";
        sqlCmd += "combo_acct_no,";
        sqlCmd += "stop_status,";
        sqlCmd += "rc_use_indicator,";
        sqlCmd += "p_seqno,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "rc_use_indicator,";
        sqlCmd += "debt_close_date ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno = ? ";
        setString2(1, hCardPSeqno);
        sqlSelect(sqlCmd);
        recordCnt = sqlRowNum;
        if (recordCnt > 0) {
            hMbtmRiskBankNo = colStr("risk_bank_no");
            hMbtmCreditLmt = colStr("line_of_credit_amt");
            hAcnoComboIndicator = colStr("combo_indicator");
            hAcnoComboAcctNo = colStr("combo_acct_no");
            hAcnoStopStatus = colStr("stop_status");
            hIsRc = colStr("rc_use_indicator");
            hAcnoGpNo = colStr("p_seqno");
            hAcnoPSeqno = colStr("acno_p_seqno");
            hAcnoRcUseIndicator = colStr("rc_use_indicator");
            hAcnoDebtCloseDate = colStr("debt_close_date");
        } else {
            return 1;
        }

        return (0);
    }
    
    /***********************************************************************/
    public String[] getIDInfo(String idPSeqno) {
        String[] rtn = new String[2];
        try {
            sqlCmd = "select id_no, id_no_code  from crd_idno  where id_p_seqno = ?";
            setString2(1, idPSeqno);
            sqlSelect(sqlCmd);

            if (sqlRowNum > 0) {
                rtn[0] = colStr("id_no");
                rtn[1] = colStr("id_no_code");
            }
        } catch (Exception ex) {
        }

        return rtn;
    }
    
    /***********************************************************************/
    int getPtrExtn() throws Exception {
        /* 取得展期參數 */
        hPtrExtnYear = 0;
        hGcrdCardMoldFlag = "";
        sqlCmd = "select card_mold_flag ";
        sqlCmd += " from ptr_group_card  ";
        sqlCmd += "where group_code = ?  ";
        sqlCmd += "  and card_type  = ? ";
        setString2(1, hMbtmGroupCode);
        setString2(2, hMbtmCardType);
        sqlSelect(sqlCmd);
        int recordCnt = sqlRowNum;
        if (recordCnt > 0) {
            hGcrdCardMoldFlag = colStr("card_mold_flag");
        }

        sqlCmd = "select extn_year ";
        sqlCmd += " from crd_item_unit   ";
        sqlCmd += "where unit_code = ?  ";
        sqlCmd += "  and card_type = ? ";
        setString2(1, hMbtmUnitCode);
        setString2(2, hMbtmCardType);
        sqlSelect(sqlCmd);
        recordCnt = sqlRowNum;

        if (recordCnt > 0) {
            hPtrExtnYear = colInt("extn_year");
        } else {
            hPtrExtnYear = 2;
        }

        return (0);
    }

    /***********************************************************************/
    void getValidDate() throws Exception {
        String hValidFm = "";
        String hValidTo = "";
        String hDiffDate = "";
        String hEndDate = "";

        hValidFm = "";
        hValidTo = "";
        hDiffDate = "";
        hEndDate = "";
        /****************************************************
         * 效期大於今日,在六個月以內
         ****************************************************/
        sqlCmd = "select to_char(add_months(sysdate,6),'yyyymmdd') h_diff_date ";
        sqlCmd += " from dual ";
        sqlSelect(sqlCmd);
        int recordCnt = sqlRowNum;
        if (recordCnt > 0) {
            hDiffDate = colStr("h_diff_date");
        }

        if (hMbtmSupFlag.equals("1")) {
            hEndDate = hMbtmMajorValidTo;
        } else {
            hEndDate = hMbtmOldEndDate;
        }

        if (hEndDate.compareTo(hDiffDate) <= 0) {
            /************************************************
             * 本月一號 展期後
             ************************************************/
            sqlCmd = "select to_char(sysdate,'yyyymm')||'01' as h_valid_fm,";
            sqlCmd += "to_char(add_months(to_date(?,'yyyymmdd'), cast(? as int)*12),'yyyymmdd') as h_valid_to ";
            sqlCmd += " from dual ";
            setString2(1, hEndDate);
            setInt2(2, hPtrExtnYear);
            sqlSelect(sqlCmd);
            recordCnt = sqlRowNum;
            if (recordCnt > 0) {
                hValidFm = colStr("h_valid_fm");
                hValidTo = colStr("h_valid_to");
            }
            hMbtmValidFm = hValidFm;
            hMbtmValidTo = hValidTo;
        } else {
            /************************************************
             * 本月一號
             ************************************************/
            sqlCmd = "select to_char(sysdate,'yyyymm')||'01' h_valid_fm ";
            sqlCmd += " from dual ";
            sqlSelect(sqlCmd);
            recordCnt = sqlRowNum;
            if (recordCnt > 0) {
                hValidFm = colStr("h_valid_fm");
            }

            hMbtmValidFm = hValidFm;
            hMbtmValidTo = hEndDate;
        }

        return;
    }
    
    /***********************************************************************/
    void updateOnbat2ecs() throws Exception {
    	busi.SqlPrepare sp = new busi.SqlPrepare();
    	sp.sql2Update("onbat_2ecs");
    	sp.ppint2("proc_status", hStatus);
    	sp.ppymd("proc_date");
    	sp.ppdate("dop");
    	sp.sql2Where("where rowid =?", wp.hexStrToByteArr(hOnbaRowid));
        sqlExec(sp.sqlStmt(), sp.sqlParm());

        if (sqlRowNum == -1) {
        	System.out.println("update onbat_2ecs err");
        }
    }
    
    /***********************************************************************/
    int chkValidCard() throws Exception {
        int count = 0;
        String pCurrentCode = "";

        sqlCmd = "select ";
        sqlCmd += "card_no,";
        sqlCmd += "current_code,";
        sqlCmd += "sup_flag,";
        sqlCmd += "combo_indicator,";
        sqlCmd += "combo_acct_no ";
        sqlCmd += " from crd_card ";
        sqlCmd += "where acct_type = ? ";
        sqlCmd += "  and acno_p_seqno   = ? ";
        sqlCmd += "  and card_no   = ? ";
        sqlCmd += " order by sup_flag ";
        setString2(1, hCardAcctType);
        setString2(2, hCardPSeqno);
        setString2(3, hOnbaCardNo);
        sqlSelect(sqlCmd);
        int recordCnt = sqlRowNum;
        if(recordCnt>0){
        	pCurrentCode = colStr("current_code");
        	pSupFlag = colStr("sup_flag");
        	pComboIndicator = colStr("combo_indicator");
        	pComboAcctNo = colStr("combo_acct_no");
        }
      
        if ((pCurrentCode.equals("0"))) {
                count++;
        }

        if (count <= 0)
            return (1);

        return (0);
    }

    /***********************************************************************/
    int updateActAcno() throws Exception {

        if (hAcnoRcUseIndicator.equals("1")) {
        	sqlCmd  = " update act_acno set ";
        	sqlCmd += " rc_use_b_adj     = ?,";
        	sqlCmd += " rc_use_indicator = '2',";
        	sqlCmd += " rc_use_s_date    = ?,";
        	sqlCmd += " rc_use_e_date    = to_char(add_months(to_date(?,'yyyymmdd'),3),'yyyymmdd'),";
        	sqlCmd += " mod_time      = sysdate,";
        	sqlCmd += " mod_pgm       = ?,";
        	sqlCmd += " mod_user      = 'system'";
        	sqlCmd += " where acno_p_seqno  = ? ";
            setString2(1, hAcnoRcUseIndicator);
            setString2(2, hOnbaOppDate);
            setString2(3, hOnbaOppDate);
            setString2(4, prgmId);
            setString2(5, hCardPSeqno);
            sqlExec(sqlCmd);
  
            if (sqlRowNum == -1) {
            	System.out.println("update act_acno err");
            }
        }

        return (0);
    }

    /***********************************************************************/
    int insertCrdEmbossTmp() throws Exception {
        int hCount = 0;
        /* 刪除續卡or毀損重製待製卡 */
        sqlCmd = " select count(*) h_count ";
        sqlCmd += " from crd_emboss_tmp  ";
        sqlCmd += " where old_card_no     = ?  ";
		sqlCmd += "  and (emboss_source <> '5' or (emboss_source = '5'  ";
        sqlCmd += "  and emboss_reason not in ('1','3'))) ";
        setString2(1, hMbtmOldCardNo);
        sqlSelect(sqlCmd);
        int recordCnt = sqlRowNum;

        if (recordCnt > 0) {
            hCount = colInt("h_count");
        }
        if (hCount > 0) {
        	sqlCmd  = " delete crd_emboss_tmp ";
        	sqlCmd += " where old_card_no = ?  ";
        	sqlCmd += " and (emboss_source <> '5' or (emboss_source = '5'  ";
        	sqlCmd += " and emboss_reason not in ('1','3'))) ";
            setString2(1, hMbtmOldCardNo);
            sqlExec(sqlCmd);            
 
            if (sqlRowNum == -1) {
            	System.out.println("delete_crd_emboss_tmp err");
            	return sqlRowNum;
            }

            sqlCmd = " update crd_card set ";
            sqlCmd += " expire_chg_flag = decode(change_status ,'1','',expire_chg_flag),";
            sqlCmd += " expire_reason   = decode(change_status ,'1','',expire_reason),";
            sqlCmd += " expire_chg_date = decode(change_status ,'1','',expire_chg_date),";
            sqlCmd += " change_status   = decode(change_status ,'1','',change_status),";
            sqlCmd += " change_reason   = decode(change_status ,'1','',change_reason),";
            sqlCmd += " change_date     = decode(change_status ,'1','',change_date),";
            sqlCmd += " reissue_status  = decode(reissue_status,'1','',reissue_status),";
            sqlCmd += " reissue_reason  = decode(reissue_status,'1','',reissue_reason),";
            sqlCmd += " reissue_date    = decode(reissue_status,'1','',reissue_date),";
            sqlCmd += " mod_pgm         = ?,";
            sqlCmd += " mod_time        = sysdate ";
            sqlCmd += " where card_no    = ? ";
            setString2(1, prgmId);
            setString2(2, hMbtmOldCardNo);
            sqlExec(sqlCmd);

            if (sqlRowNum == -1) {
            	System.out.println("update crd_card err");
            	return sqlRowNum;
            }
        }
        /* 已存在不可重複入檔 */
        sqlCmd = " select count(*) h_count ";
        sqlCmd += " from crd_emboss_tmp  ";
        sqlCmd += " where old_card_no = ?  ";
        sqlCmd += " fetch first 1 rows only ";
        setString2(1, hMbtmOldCardNo);
        sqlSelect(sqlCmd);
        recordCnt = sqlRowNum;
        if (recordCnt > 0) {
            hCount = colInt("h_count");
        }

        if (hCount > 0) {
            return (1);
        }
        hMbtmBatchno = hBatchno;
        hMbtmRecno = hRecno;
        if (hMbtmSupFlag.equals("0")) {
            hMbtmMajorCardNo = "";
            hMbtmMajorValidFm = "";
            hMbtmMajorValidTo = "";
        }
        hMbtmEmbossSource = "5";
        hMbtmEmbossReason = "";
        hMbtmNcccType = "1";

        switch (str2int(hOnbaOppType)) {
        case 2:
            hMbtmEmbossReason = "1";
            break;
        case 5:
            hMbtmEmbossReason = "3";
            break;
        }
        if (hOnbaIsEm.equals("Y")) {
            hMbtmToNcccCode = "N";
            hMbtmReasonCode = "3";
        } else {
            hMbtmToNcccCode = "Y";
        }

        hMbtmComboIndicator = "";
        sqlCmd = "select decode(combo_indicator,'','N',combo_indicator) h_mbtm_combo_indicator ";
        sqlCmd += " from ptr_group_code  ";
        sqlCmd += "where group_code = decode(cast(? as varchar(10)),'','0000',?) ";
        setString(1, hMbtmGroupCode);
        setString(2, hMbtmGroupCode);
        sqlSelect(sqlCmd);
        recordCnt = sqlRowNum;
        if (sqlCode==-1) {
        	errmsg("select_ptr_group_code not found!");
        	return sqlCode;
        }
 
        if (recordCnt > 0) {
            hMbtmComboIndicator = colStr("h_mbtm_combo_indicator");
        }
        hMbosMnoId = "";
        hMbosMsisdn = "";
        hMbosServiceId = "";
        hMbosSeId = "";
        hMbosServiceVer = "";
        hMbosServiceType = "";
        hMbosSirNo = "";
        if (strMid(hGcrdCardMoldFlag, 0, 1).equals("M")) {
            sqlCmd = "select mno_id,";
            sqlCmd += "msisdn,";
            sqlCmd += "service_id,";
            sqlCmd += "se_id,";
            sqlCmd += "service_ver,";
            sqlCmd += "service_type,";
            sqlCmd += "sir_no ";
            sqlCmd += " from mob_card  ";
            sqlCmd += "where card_no  = ?  ";
            sqlCmd += "and new_beg_date in ( select max(new_beg_date) from mob_card where card_no = ?) ";
            setString2(1, hOnbaCardNo);
            setString2(2, hOnbaCardNo);
            sqlSelect(sqlCmd);
            recordCnt = sqlRowNum;
            if (sqlCode==-1) {
            	errmsg("select_mob_card not found!");
            	return sqlCode;
            }

            if (recordCnt > 0) {
                hMbosMnoId = colStr("mno_id");
                hMbosMsisdn = colStr("msisdn");
                hMbosServiceId = colStr("service_id");
                hMbosSeId = colStr("se_id");
                hMbosServiceVer = colStr("service_ver");
                hMbosServiceType = colStr("service_type");
                hMbosSirNo = colStr("sir_no");
            }
        }

        busi.SqlPrepare sp = new busi.SqlPrepare();
        sp.sql2Insert("crd_emboss_tmp");
        
        sp.ppstr2("batchno", hMbtmBatchno);
        sp.ppstr2("recno", Long.toString(hMbtmRecno));
        sp.ppstr2("emboss_source", hMbtmEmbossSource);
        sp.ppstr2("emboss_reason", hMbtmEmbossReason);
        sp.ppstr2("resend_note", hMbtmResendNote);
        sp.ppstr2("to_nccc_code", hMbtmToNcccCode);
        sp.ppstr2("reg_bank_no", hMbtmRegBankNo);
        sp.ppstr2("risk_bank_no", hMbtmRiskBankNo);
        sp.ppstr2("card_type", hMbtmCardType);
        sp.ppstr2("bin_no", hMbtmBinNo);
        sp.ppstr2("unit_code", hMbtmUnitCode);
        sp.ppstr2("acct_type", hMbtmAcctType);
        
        sp.ppstr2("acct_key", ufAcnoKey(hCardPSeqno));
        
        sp.ppstr2("card_no", "");
        sp.ppstr2("old_card_no", hMbtmOldCardNo);
        sp.ppstr2("status_code", hMbtmStatusCode);
        sp.ppstr2("reason_code", hMbtmReasonCode);
        sp.ppstr2("sup_flag", hMbtmSupFlag);
        sp.ppstr2("apply_id", hMbtmApplyId);
        sp.ppstr2("apply_id_code", hMbtmApplyIdCode);

        String[] info = getIDInfo(hCardMajorIdPSeqno);
        sp.ppstr2("pm_id", info[0]);
        sp.ppstr2("pm_id_code", info[1]);

        sp.ppstr2("group_code", hMbtmGroupCode);
        sp.ppstr2("source_code", hMbtmSourceCode);
        sp.ppstr2("corp_no", hMbtmCorpNo);
        sp.ppstr2("corp_no_code", hMbtmCorpNoCode);
        sp.ppstr2("chi_name", hMbtmChiName);
        sp.ppstr2("eng_name", hBbtmEngName);
        sp.ppstr2("birthday", hMbtmBirthday);
        sp.ppstr2("force_flag", hMbtmForceFlag);
        sp.ppstr2("valid_fm", hMbtmValidFm);
        sp.ppstr2("valid_to", hMbtmValidTo);
        sp.ppstr2("major_card_no", hMbtmMajorCardNo);
        sp.ppstr2("major_valid_fm", hMbtmMajorValidFm);
        sp.ppstr2("major_valid_to", hMbtmMajorValidTo);
        sp.ppstr2("chg_addr_flag", hMbtmChgAddrFlag);
        sp.ppstr2("mail_type", hMbtmMailType);
        sp.ppstr2("credit_lmt", hMbtmCreditLmt);
        sp.ppstr2("emboss_4th_data", hMbtmEmboss4thData);
        sp.ppstr2("old_beg_date", hMbtmOldBegDate);
        sp.ppstr2("old_end_date", hMbtmOldEndDate);
        sp.ppstr2("crt_date", sysDate);
        sp.ppstr2("apr_user", hMbtmSuperId);
        sp.ppstr2("apr_date", hMbtmSuperDate);
        sp.ppstr2("emboss_date", hMbtmEmbossDate);
        sp.ppstr2("nccc_batchno", hMbtmNcccBatchno);
        sp.ppint2("nccc_recno", hMbtmNcccRecno);
        sp.ppstr2("nccc_type", hMbtmNcccType);
        sp.ppstr2("ic_flag", hMbtmIcFlag);
        sp.ppstr2("combo_indicator", hMbtmComboIndicator);
        sp.ppstr2("curr_code", hCardCurrCode);
        sp.ppstr2("jcic_score", hCardJcicScore);
        sp.ppstr2("mno_id", hMbosMnoId);
        sp.ppstr2("msisdn", hMbosMsisdn);
        sp.ppstr2("service_id", hMbosServiceId);
        sp.ppstr2("se_id", hMbosSeId);
        sp.ppstr2("service_ver", hMbosServiceVer);
        sp.ppstr2("service_type", hMbosServiceType);
        sp.ppstr2("sir_no", "");
        sp.ppstr2("mod_user", "system");
        sp.ppdate("mod_time");
        sp.ppstr2("mod_pgm", prgmId);
        sp.ppnum("mod_seqno", hMbtmModSeqno);
        sqlExec(sp.sqlStmt(), sp.sqlParm());

        if (sqlRowNum == -1) {
        	System.out.println("insert_crd_emboss_tmp err");
        	return sqlRowNum;
        }

        hInsOk = 1;
        return (0);
    }
    
    // ************************************************************************
    public int str2int(String val) {
        int rtn = 0;
        try {
            rtn = Integer.parseInt(val.replaceAll(",", "").trim());
        } catch (Exception e) {
            rtn = 0;
        }
        return rtn;
    }

    // ************************************************************************
    public String ufAcnoKey(String pSeqno) throws Exception {
        sqlCmd = "select nvl(uf_acno_key(?), '') as acct_key from sysibm.sysdummy1 ";
        setString2(1, pSeqno);
        sqlSelect(sqlCmd);
        int recordCnt = sqlRowNum;
        if (recordCnt > 0)
            return colStr("acct_key");
        return "";
    }
    
    // ************************************************************************
    int delCrdEmbossTmp() throws Exception {
    	sqlCmd = " delete crd_emboss_tmp ";
    	sqlCmd += " where old_card_no = ?  ";
    	sqlCmd += "and emboss_source = '5' ";
        setString2(1, hOnbaCardNo);
        sqlExec(sqlCmd);

        if (sqlRowNum == -1) {
        	System.out.println("delete crd_emboss_tmp err");
        }
        if (sqlRowNum==-1) {
        	return (0);
        }else {
            hDelOk = 1;
            return (0);
		}
    }
    
    /***********************************************************************/
    void processApscard() throws Exception {
        String hRowid = "";

        hRowid = "";
        hApscStopReason = "";
        hApscStopDate = "";
        hApscStatusCode = "";
        /***** 停用原因 ***********/
        switch (str2int(hOnbaOppType)) {
        case 1:
            hApscStopReason = "3";
            break;
        case 2:
            hApscStopReason = "2";
            break;
        case 3:
            hApscStopReason = "1";
            break;
        case 4:
            hApscStopReason = "3";
            break;
        case 5:
            hApscStopReason = "5";
            break;
        }

        hApscStopDate = hOnbaOppDate;
        if ((!hCardCurrentCode.equals("0")) && (hOnbaOppType.equals("0"))) {
            hApscStatusCode = "5";
        }
  
        sqlCmd  = "select hex(rowid)  as rowid ";
        sqlCmd += "  from crd_apscard  ";
        sqlCmd += " where card_no     = ?  ";
        sqlCmd += "   and to_aps_date = '' ";
        setString2(1, hOnbaCardNo);
        sqlSelect(sqlCmd);
     
        int recordCnt = sqlRowNum;
        if (recordCnt > 0) {
        	
            hRowid = colStr("rowid");

            sqlCmd  = " update crd_apscard set ";
            sqlCmd += " stop_reason = ?,";
            sqlCmd += " stop_date   = ?,";
            sqlCmd += " status_code = ?,";
            sqlCmd += " mod_time    = sysdate,";
            sqlCmd += " mod_user    = 'system',";
            sqlCmd += " mod_pgm     = ?";
            sqlCmd += " where rowid  = ? ";
            setString2(1, hApscStopReason);
            setString2(2, hApscStopDate);
            setString2(3, hApscStatusCode);
            setString2(4, prgmId);
            setRowId2(5,hRowid);
            sqlExec(sqlCmd);

            if (sqlRowNum == -1) {
            	System.out.println("update crd_apscard err");
            }

        } else {
 
            insertApscard();

        }
       
        return;
    }
    
    /***********************************************************************/
    void insertApscard() throws Exception {
        String pmBirthday = "";
        String pmChiName = "";

        hApscCardNo = hOnbaCardNo;
        hApscValidDate = hMbtmOldEndDate;

        String[] info = getIDInfo(hCardMajorIdPSeqno);
        hApscPmId = info[0];
        hApscPmIdCode = info[1];

        if (hMbtmSupFlag.equals("0")) {
            hApscPmBirthday = hMbtmBirthday;
            hApscPmName = hMbtmChiName;
        }
        if (hMbtmSupFlag.equals("1")) {
            hApscSupLostStatus = "0";
            hApscSupBirthday = hMbtmBirthday;
            hApscSupName = hMbtmChiName;
            info = getIDInfo(hCardIdPSeqno);
            hApscSupId = info[0];
            hApscSupIdCode = info[1];
            pmBirthday = "";
            pmChiName = "";
            sqlCmd = "select chi_name,";
            sqlCmd += "birthday ";
            sqlCmd += " from crd_idno  ";
            sqlCmd += "where id_p_seqno = ? ";
            setString(1, hCardMajorIdPSeqno);
            sqlSelect(sqlCmd);
            int recordCnt = sqlRowNum;
            if (sqlCode==-1) {
            	errmsg("select_crd_idno not found!");
            	return;
            }
            if (recordCnt > 0) {
                pmChiName = colStr("chi_name");
                pmBirthday = colStr("birthday");
            }

            hApscPmBirthday = pmBirthday;
            hApscPmName = pmChiName;
        }

        hApscCorpNo      = hMbtmCorpNo;
        hApscCorpNoCode = hMbtmCorpNoCode;
        hApscCardType    = hMbtmCardType;
        hApscGroupCode   = hMbtmGroupCode;
        hApscMailType    = hCardMailType;
        hApscMailBranch  = hCardMailBranch;
        hApscMailNo      = hCardMailNo;
        hApscMailDate    = hCardMailProcDate;
        
        busi.SqlPrepare sp = new busi.SqlPrepare();
        sp.sql2Insert("crd_apscard");
        sp.ppstr("crt_datetime", sysDate + sysTime);
        sp.ppstr("card_no"     , hApscCardNo);
        sp.ppstr("valid_date"  , hApscValidDate);
        sp.ppstr("stop_date"   , hApscStopDate);
        sp.ppstr("reissue_date", hApscReissueDate);
        sp.ppstr("stop_reason" , hApscStopReason);
        sp.ppstr("mail_type"   , hApscMailType);
        sp.ppstr("mail_no"     , hApscMailNo);
        sp.ppstr("mail_branch" , hApscMailBranch);
        sp.ppstr("mail_date"   , hApscMailDate);
        sp.ppstr("pm_id"       , hApscPmId);
        sp.ppstr("pm_id_code"     , hApscPmIdCode);
        sp.ppstr("pm_birthday"    , hApscPmBirthday);
        sp.ppstr("sup_id"         , hApscSupId);
        sp.ppstr("sup_id_code"    , hApscSupIdCode);
        sp.ppstr("sup_birthday"   , hApscSupBirthday);
        sp.ppstr("corp_no"        , hApscCorpNo);
        sp.ppstr("corp_no_code"   , hApscCorpNoCode);
        sp.ppstr("card_type"      , hApscCardType);
        sp.ppstr("pm_name"        , hApscPmName);
        sp.ppstr("sup_name"       , hApscSupName);
        sp.ppstr("sup_lost_status", hApscSupLostStatus);
        sp.ppstr("status_code"    , hApscStatusCode);
        sp.ppstr("group_code"     , hApscGroupCode);
        sp.ppstr("mod_user"       , "system");
        sp.ppdate("mod_time");
        sp.ppstr("mod_pgm"        , prgmId);
        sqlExec(sp.sqlStmt(), sp.sqlParm());

        if (sqlRowNum == -1) {
        	System.out.println("insert_crd_apscard duplicate!:");
        }

        return;
    }
    
    /***********************************************************************/
    void insertCrdJcic() throws Exception {
        String hRowid = "";

        hRowid = "";

        sqlCmd = "select hex(rowid)  as rowid ";
        sqlCmd += " from crd_jcic  ";
        sqlCmd += "where card_no  = ?  ";
        sqlCmd += "and trans_type = 'C'  ";
        sqlCmd += "and to_jcic_date =''  ";
        sqlCmd += "fetch first 1 rows only ";
        setString2(1, hOnbaCardNo);
        sqlSelect(sqlCmd);
        int recordCnt = sqlRowNum;
        if (recordCnt > 0) {
            hRowid = colStr("rowid");

            sqlCmd  = " update crd_jcic set ";
            sqlCmd += " current_code  = ?, ";
            sqlCmd += " oppost_reason = ?, ";
            sqlCmd += " oppost_date   = ?, ";
            sqlCmd += " payment_date  = ?, ";
            sqlCmd += " mod_user      = 'system', ";
            sqlCmd += " mod_time      = sysdate, ";
            sqlCmd += " mod_pgm       = ? ";
            sqlCmd += " where rowid    = ? ";
            setString2(1, hOnbaOppType);
            setString2(2, hOnbaOppReason);
            setString2(3, hOnbaOppDate);
            setString2(4, hPaymentDate);
            setString2(5, prgmId);
            setRowId2(6, hRowid);
            sqlExec(sqlCmd);

            if (sqlRowNum == -1) {
            	System.out.println("update crd_jcic err");
            }
            return;
        }

        busi.SqlPrepare sp = new busi.SqlPrepare();
        sp.sql2Insert("crd_jcic");
        sp.ppstr2("card_no"      , hOnbaCardNo);
        sp.ppstr2("crt_date"     , hSystemDate);
        sp.ppstr2("crt_user"     , "system");
        sp.ppstr2("trans_type"   , "C");
        sp.ppstr2("current_code" , hOnbaOppType);
        sp.ppstr2("oppost_reason", hOnbaOppReason);
        sp.ppstr2("oppost_date"  , hOnbaOppDate);
        sp.ppstr2("payment_date" , hPaymentDate);
        sp.ppstr2("is_rc"        , hIsRc);
        sp.ppstr2("mod_user"     , "system");
        sp.ppdate("mod_time");
        sp.ppstr2("mod_pgm"      , prgmId);
        sqlExec(sp.sqlStmt(), sp.sqlParm());

        if (sqlRowNum == -1) {
        	System.out.println("insert_crd_jcic err");
        }
    }
    
    /***********************************************************************/
    void selectTscCard() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "current_code,";
        sqlCmd += "new_end_date ";
        sqlCmd += "from tsc_card ";
        sqlCmd += "where card_no = ? ";
        sqlCmd += "and substr(new_end_date,1,6) > substr(?,1,6) ";
        sqlCmd += "and current_code = '0' ";
        sqlCmd += "and tsc_sign_flag = 'Y' ";
        setString2(1, hOnbaCardNo);
        setString2(2, hBusiBusinessDate);
        sqlSelect(sqlCmd);
        int recordCnt = sqlRowNum;

        for (int i = 0; i < recordCnt; i++) {
            hTardTscCardNo = colStr(i,"tsc_card_no");
            hTardCurrentCode = colStr(i,"current_code");
            hTardNewEndDate = colStr(i,"new_end_date");

            insertTscOppostLog();
        }
    }
    
    /***********************************************************************/
    void insertTscOppostLog() throws Exception {
        String updateCode = "X";
        if (hOnbaOppType.equals("2"))
            updateCode = "1";
        if (hOnbaOppType.equals("0"))
            updateCode = "3";
        
        busi.SqlPrepare sp = new busi.SqlPrepare();
        sp.sql2Insert("tsc_oppost_log");
        sp.ppstr2("crt_date", sysDate);
        sp.ppstr2("crt_time", hOnbaDog);
        sp.ppstr2("tsc_card_no", hTardTscCardNo);
        sp.ppstr2("card_no", hOnbaCardNo);
        sp.ppstr2("current_code", hTardCurrentCode);
        sp.ppstr2("new_end_date", hTardNewEndDate);
        sp.ppstr2("oppost_date", hOnbaOppDate);
        sp.ppstr2("oppost_reason", hOnbaOppReason);
        sp.ppstr2("update_code", updateCode);
        sp.ppstr2("proc_date", "00000000");
        sp.ppdate("mod_time");
        sp.ppstr2("mod_pgm", prgmId);
        sqlExec(sp.sqlStmt(), sp.sqlParm());

        if (sqlRowNum == -1) {
        	System.out.println("insert tsc_oppost_log err");
        }
    }
    
    /**************************************************************************/
    void selectIchCard(int idx) throws Exception 
    {

		sqlCmd  = " SELECT ich_card_no, ";
		sqlCmd += " current_code, ";
		sqlCmd += " new_end_date ";
		sqlCmd += " FROM ich_card ";
		sqlCmd += " where card_no = ? ";
		sqlCmd += " and substr(new_end_date,1,6) > substr(?,1,6) ";
		setString2(1, hOnbaCardNo);
		setString2(2, hBusiBusinessDate);
		sqlSelect(sqlCmd);
        int recordCnt = sqlRowNum;
		for (int i = 0; i < recordCnt; i++) {
			hIardIchCardNo = colStr(i,"ich_card_no");
			hIardCurrentCode = colStr(i,"current_code");
			hIardNewEndDate = colStr(i,"new_end_date");

			if (idx == 1)
				insertIchOppostLog();
		}
    }
    
    /**************************************************************************/
    void insertIchOppostLog() throws Exception 
    {
       String updateCode = "X";
       if(hOnbaOppType.equals("2"))
          updateCode = "1";
       if(hOnbaOppType.equals("0"))
          updateCode = "3";
       
       busi.SqlPrepare sp = new busi.SqlPrepare();
       sp.sql2Insert("ich_oppost_log");
       sp.ppstr2("crt_date"     , sysDate);
       sp.ppstr2("crt_time"     , hOnbaDog);
       sp.ppstr2("ich_card_no"  , hIardIchCardNo);
       sp.ppstr2("card_no"      , hOnbaCardNo);
       sp.ppstr2("current_code" , hIardCurrentCode);
       sp.ppstr2("new_end_date" , hIardNewEndDate);
       sp.ppstr2("oppost_date"  , hOnbaOppDate);
       sp.ppstr2("oppost_reason", hOnbaOppReason);
       sp.ppstr2("update_code"  , updateCode);
       sp.ppstr2("proc_date"    , "00000000");
       sp.ppdate("mod_time");
       sp.ppstr2("mod_pgm"      , prgmId);
       sqlExec(sp.sqlStmt(), sp.sqlParm());
 
       if (sqlRowNum == -1) {
       	System.out.println("insert_ich_oppost_log err");
       }
    }
    
    /**************************************************************************/
    void deleteIchRtn() throws Exception 
    {
    	sqlCmd  = " delete ich_b04b_special ";
    	sqlCmd += " where ich_card_no = ?   ";
    	sqlCmd += " and proc_flag  <> 'Y' ";
    	setString2(1, hIardIchCardNo);
    	sqlExec(sqlCmd);

        if (sqlRowNum == -1) {
        	System.out.println("delete ich_b04b_special err");
        }
    	sqlCmd  = " delete ich_b09b_bal ";
    	sqlCmd += " where ich_card_no = ?   ";
    	sqlCmd += " and proc_flag  <> 'Y' ";
    	setString2(1, hIardIchCardNo);
    	sqlExec(sqlCmd);

        if (sqlRowNum == -1) {
        	System.out.println("delete ich_b09b_bal err");
        }
    }
    
    /***********************************************************************/
    void selectIpsCard() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "ips_card_no,";
        sqlCmd += "current_code,";
        sqlCmd += "new_end_date ";
        sqlCmd += "from ips_card ";
        sqlCmd += "where card_no = ? ";
        sqlCmd += "and substr(new_end_date,1,6) > substr(?,1,6) ";

        setString(1, hOnbaCardNo);
        setString(2, hBusiBusinessDate);
    	sqlSelect(sqlCmd);
    
        int recordCnt = sqlRowNum;
        for (int i = 0; i < recordCnt; i++) {
            hIardIpsCardNo  = colStr(i,"ips_card_no");
            hIardCurrentCode = colStr(i,"current_code");
            hIardNewEndDate = colStr(i,"new_end_date");
            
            insertIpsOppostLog();
        }
    }
    
    /***********************************************************************/
    void insertIpsOppostLog() throws Exception {
        String updateCode = "X";
        if (hOnbaOppType.equals("2"))
            updateCode = "1";
        if (hOnbaOppType.equals("0"))
            updateCode = "3";

        busi.SqlPrepare sp = new busi.SqlPrepare();
        sp.sql2Insert("ips_oppost_log");
        sp.ppstr2("crt_date", sysDate);
        sp.ppstr2("crt_time", hOnbaDog);
        sp.ppstr2("ips_card_no", hIardIpsCardNo);
        sp.ppstr2("card_no", hOnbaCardNo);
        sp.ppstr2("current_code", hIardCurrentCode);
        sp.ppstr2("new_end_date", hIardNewEndDate);
        sp.ppstr2("oppost_date", hOnbaOppDate);
        sp.ppstr2("oppost_reason", hOnbaOppReason);
        sp.ppstr2("update_code", updateCode);
        sp.ppstr2("proc_date", "00000000");
        sp.ppdate("mod_time");
        sp.ppstr2("mod_pgm", prgmId);
        sqlExec(sp.sqlStmt(), sp.sqlParm());

        if (sqlRowNum == -1) {
        	System.out.println("insert_ips_oppost_log err");
        }
    }

    /**************************************************************************/
    void insertIchB04bSpecial() throws Exception 
    {
       selectIchB04bSpecial();
       if(tempInt04>0)  return;   
       
       busi.SqlPrepare sp = new busi.SqlPrepare();
       sp.sql2Insert("ich_b04b_special");
       sp.ppstr2("ich_card_no"    , hIardIchCardNo);
       sp.ppstr2("proc_type"      , "5");
       sp.ppstr2("sys_date"       , sysDate);
       sp.ppstr2("sys_time"       , sysTime);
       sp.ppstr2("effc_send_date" , hNextDate);
       sp.ppstr2("proc_flag"      , "N");
       sp.ppstr2("ok_flag"        , "N");
       sp.ppdate("mod_time");
       sp.ppstr2("mod_pgm"        , prgmId);
       sqlExec(sp.sqlStmt(), sp.sqlParm());
  
       if (sqlRowNum == -1) {
       	System.out.println("insert_ich_b04b_special err");
       }
    }
    
    /**************************************************************************/
    void selectIchB04bSpecial() throws Exception 
    {
      tempInt04=0;

      sqlCmd  = " SELECT count(*) as all_cnt ";
      sqlCmd += "   FROM ich_b04b_special ";
      sqlCmd += "  where ich_card_no = ? ";
      setString2(1, hIardIchCardNo);
      sqlSelect(sqlCmd);
      int recordCnt = sqlRowNum;
      if (recordCnt > 0) {
          tempInt04 = colInt("all_cnt");
        }
    }
    
    /**************************************************************************/
    void insertIchB09bBal()     throws Exception 
    {
       selectIchB09bBal();
       if(tempInt09>0)  return;

       busi.SqlPrepare sp = new busi.SqlPrepare();
       sp.sql2Insert("ich_b09b_bal");
       sp.ppstr2("ich_card_no"    , hIardIchCardNo);
       sp.ppstr2("card_no"        , hOnbaCardNo);
       sp.ppstr2("bal_rsn"        , hB09bBalRsn);
       sp.ppstr2("loss_date"      , sysDate);
       sp.ppstr2("loss_time"      , sysTime);
       sp.ppstr2("sys_date"       , sysDate);
       sp.ppstr2("sys_time"       , sysTime);
       sp.ppstr2("effc_send_date" , hNextDate);
       sp.ppstr2("proc_flag"      , "N");
       sp.ppstr2("ok_flag"        , "N");
       sp.ppdate("mod_time");
       sp.ppstr2("mod_pgm"        , prgmId);
       sqlExec(sp.sqlStmt(), sp.sqlParm());

       if (sqlRowNum == -1) {
       	System.out.println("insert_ich_b09b_bal err");
       }
    }
    
    /**************************************************************************/
    void selectIchB09bBal()     throws Exception 
    {
      tempInt09=0;

      sqlCmd  = " SELECT count(*) as all_cnt ";
      sqlCmd += "   FROM ich_b09b_bal ";
      sqlCmd += "  where ich_card_no = ? ";
      setString2(1, hIardIchCardNo);
      sqlSelect(sqlCmd);
      int recordCnt = sqlRowNum;
      if (recordCnt > 0) {
          tempInt09 = colInt("all_cnt");
        }
    }
    
    /***********************************************************************/
    void chkStopStatus() throws Exception {
        if (hAcnoStopStatus.equals("Y")) {
        	sqlCmd = " update act_acno set ";
        	sqlCmd += " stop_status    = ''";
        	sqlCmd += " where acno_p_seqno  = ? ";
            setString2(1, hCardPSeqno);
            sqlExec(sqlCmd);
        }

        return;
    }
    
    /***********************************************************************/
    int selectHceCard() throws Exception {
        hHstnVCardNo = "";
        hHstnStatusCode = "";
        hHstnSirUser = "";
        hHstnSir = "";
        sqlCmd  = "select v_card_no,";
        sqlCmd += "status_code,";
        sqlCmd += "sir_user,";
        sqlCmd += "sir ";
        sqlCmd += " from hce_card  ";
        sqlCmd += "where card_no     = ?   ";
        sqlCmd += "  and STATUS_CODE = '0' ";
        setString(1, hOnbaCardNo);
        sqlSelect(sqlCmd);
        int recordCnt = sqlRowNum;
        if (recordCnt > 0) {
            hHstnVCardNo   = colStr("v_card_no");
            hHstnStatusCode = colStr("status_code");
            hHstnSirUser    = colStr("sir_user");
            hHstnSir         = colStr("sir");
        } else {
            return 1;
        }

        return (0);
    }
    
    /***********************************************************************/
    void insertHceStatusTxn() throws Exception {
    	busi.SqlPrepare sp = new busi.SqlPrepare();
        sp.sql2Insert("hce_status_txn");
        sp.ppstr2("crt_date", sysDate);
        sp.ppstr2("crt_time", sysTime);
        sp.ppstr2("v_card_no", hHstnVCardNo);
        sp.ppstr2("card_no", hOnbaCardNo);
        sp.ppstr2("status_code", hHstnStatusCode);
        sp.ppstr2("change_code", "4");
        sp.ppstr2("from_pgm", prgmId);
        sp.ppstr2("sir_user", hHstnSirUser);
        sp.ppstr2("sir", hHstnSir);
        sp.ppdate("mod_time");
        sp.ppstr2("mod_pgm", prgmId);
        sqlExec(sp.sqlStmt(), sp.sqlParm());

        if (sqlRowNum == -1) {
        	System.out.println("insert_hce_status_txn err");
        }
    }
    
    /***********************************************************************/
    void updateCrdCard() throws Exception {
        String hReissueStatus = "";
        String hReissueReason = "";
        String hReissueDate = "";

        hReissueStatus = "";
        hReissueReason = "";
        hReissueDate = "";
        hReissueStatus = hCardReissueStatus;
        hReissueReason = hCardReissueReason;
        hReissueDate = hCardReissueDate;
        if ((hOnbaIsRenew.equals("Y")) && (hInsOk == 1)) {
            hReissueStatus = "1";
            hReissueReason = hMbtmEmbossReason;
            hReissueDate = sysDate;
        }
        /******************************************************************
         * 若由重製卡變成不重製卡時,必須先檢核是否在crd_Emboss_tmp,
         * 若存在,且確定刪除成功後才可將,reissue_status等清空,否則不變
         ******************************************************************/
        if ((hOnbaIsRenew.equals("N")) && (hDelOk == 1)) {
            hReissueStatus = "";
            hReissueReason = "";
            hReissueDate = "";
        }
        sqlCmd  = " update crd_card set ";  // fix a bug : " select crd_card set "
        sqlCmd += " current_code   = ?, ";
        sqlCmd += " oppost_reason  = ?, ";
        sqlCmd += " oppost_date    = ?, ";
        sqlCmd += " reissue_status = ?, ";
        sqlCmd += " reissue_reason = ?, ";
        sqlCmd += " reissue_date   = ?, ";
        sqlCmd += " mod_user       = 'system', ";
        sqlCmd += " mod_time       = sysdate, ";
        sqlCmd += " mod_pgm        = ? ";
        sqlCmd += " where card_no   = ? ";
        setString2(1, hOnbaOppType);
        setString2(2, hOnbaOppReason);
        setString2(3, hOnbaOppDate);
        setString2(4, hReissueStatus);
        setString2(5, hReissueReason);
        setString2(6, hReissueDate);
        setString2(7, prgmId);
        setString2(8, hOnbaCardNo);
        sqlExec(sqlCmd);

        hMbosEmbossSource = "";
        hMbosEmbossReason = "";
        hTempSource = "";
        sqlCmd = "select emboss_source,";
        sqlCmd += "emboss_reason ";
        sqlCmd += " from crd_emboss  ";
        sqlCmd += "where card_no = ?  ";
        sqlCmd += "and crt_date in ( select max(crt_date) from crd_emboss where card_no = ?) ";
        setString(1, hOnbaCardNo);
        setString(2, hOnbaCardNo);
        sqlSelect(sqlCmd);
        int recordCnt = sqlRowNum;
        if (recordCnt > 0) {
            hMbosEmbossSource = colStr("emboss_source");
            hMbosEmbossReason = colStr("emboss_reason");
        }

        switch (str2int(hMbosEmbossSource)) {
        case 1:
        case 2:
            hTempSource = "1";
            break;
        case 3:
        case 4:
            hTempSource = "4";
            break;
        case 5:
            hTempSource = "3";
            switch (str2int(hMbosEmbossReason)) {
            case 1: /**** 掛失重製中 ********/
                hTempSource = "2";
                break;
            case 2: /**** 毀損重製中 ********/
                hTempSource = "3";
                break;
            case 3: /**** 偽卡重製中 ********/
                hTempSource = "2";
                break;
            }
            break;
        case 6:
        case 7:
            hTempSource = "2";
            break;
        }
        if (hOnbaOppReason.length() > 0) {
        	busi.SqlPrepare sp = new busi.SqlPrepare();
            sp.sql2Insert("crd_nccc_ic");
            sp.ppstr2("to_nccc_date", "");
            sp.ppstr2("batchno", prgmId);
            sp.ppint2("recno", 1);
            sp.ppstr2("card_no"   , hOnbaCardNo);
            sp.ppstr2("chi_name"  , hMbtmChiName);
            sp.ppstr2("group_code", hMbtmGroupCode);
            sp.ppstr2("emboss_source", hMbosEmbossSource);
            sp.ppstr2("unit_code"    , hMbtmUnitCode);
            sp.ppstr2("emboss_reason", hMbosEmbossReason);
            sp.ppstr2("nccc_rtn_date", "");
            sp.ppstr2("nccc_source", hTempSource);
            sp.ppstr2("nccc_input_kind", "2"); /* 1: 新 2: 停 */
            sp.ppstr2("stop_kind", hOnbaOppType);
            sp.ppstr2("stop_rsn", hOnbaOppReason.length() > 0 ? hOnbaOppReason.substring(0, 1) : "");
            sp.ppstr2("to_send_flag", "Y");
            sp.ppdate("mod_time");
            sp.ppstr2("mod_pgm", prgmId);
            sqlExec(sp.sqlStmt(), sp.sqlParm());

        } else {
        	sqlCmd  = " delete crd_nccc_ic ";
            sqlCmd += " where card_no  = ?  ";
            sqlCmd += " and mod_pgm  = ?  ";
            sqlCmd += " and to_nccc_date ='' ";
            setString2(1, hOnbaCardNo);
            setString2(2, prgmId);
            sqlExec(sqlCmd);

            if (sqlRowNum == -1) {
            	System.out.println("delete crd_nccc_ic err");
            }
        }

        return;
    }
    
    /***********************************************************************/
    int processAppc() throws Exception {
 
        pTransType = "";
        if ((hOnbaOppType.equals("0")) || (hOnbaOppType.length() == 0)) {
            pTransType = "06";
        }
        if (hOnbaOppType.equals("1")) {
            pTransType = "01";
        }
        /**** 掛失不補發為'11',掛失補發為 '02' ***/
        if (hOnbaOppType.equals("2")) {
            if (hOnbaIsRenew.equals("Y")) {
                pTransType = "02";
            } else {
                pTransType = "11";
            }
        }
        if (hOnbaOppType.equals("4"))
            pTransType = "05";
        if (hOnbaOppType.equals("5"))
            pTransType = "04";

        if (pTransType.equals("06")) {
            if (hCarBankActno.length() > 0) {
                callAppc(hCarBankActno);
            }
 
        } else {
     
            /** old banck_actno **/
            if (hCardOldBankActno.length() > 0) {
                callAppc(hCardOldBankActno);
            }

            /** current banck_actno **/
            if (hCarBankActno.length() > 0) {
                callAppc(hCarBankActno);
            }

        }
        return (0);
    }

    /***********************************************************************/
    void callAppc(String bankno) throws Exception {
        String rtnCode = "";
        String actno = "";

        actno = bankno;
        String sendBuf = String.format("ATMP4%-8.8s%-11.11s%1s%1s%-2.2s%1s%-2.2s%-5.5s"
                        , actno, hCardComboAcctNo, "5", "F", pTransType, "C", " ", " ");

        hStopBankActno = bankno;

        hProcSeqno = "";
        hProcCode = "";
        sqlCmd = "select substr(to_char(ecs_stop.nextval,'0000000000'),2,10) h_proc_seqno ";
        sqlCmd += " from dual ";
        sqlSelect(sqlCmd);
        int recordCnt = sqlRowNum;
        if (recordCnt > 0) {
            hProcSeqno = colStr("h_proc_seqno");
        }
        String rcvbuf = commAppc(sendBuf, hEriaRefIp, Integer.parseInt(hEriaPortNo), 1024);

        String hRtnStr = "";
        if (rcvbuf.length() > 5)
            hRtnStr = rcvbuf.substring(4, 6);
        // String h_rtn_desc = rcvbuf.substring(56,116);
        if (hRtnStr.compareTo("  ") == 0)
            hRtnStr = "00";

        if (rcvbuf.length() == 0 || hRtnStr.compareTo("00") != 0) {
            hProcCode = "97";
        } else {
            rtnCode = strMid(rcvbuf, 0, 4);
            if ((rtnCode.equals("    ")) || (rtnCode.equals(" ")) || (rtnCode.length() == 0)) {
                rtnCode = "";
            }
            hProcCode = rtnCode;
            if (hProcCode.substring(0, 1).equals("T"))
                hProcCode = "00";
            if (hProcCode.length() == 0)
                hProcCode = "98";
        }

        insertCrdStopLog();
    }

  //************************************************************************
    public String commAppc(String sendbuf, String server, int port, int recvbufLen) throws IOException {
        String recvbuf = "";
        try (Socket socket = new Socket(server, port);){

            
            DataInputStream  input  = null;
            DataOutputStream output = null;

            System.out.println("Starting...");
            System.out.println("sendData : " + sendbuf);
            try {
                while (true) {
                    output = new DataOutputStream(socket.getOutputStream());
                    System.out.println("Send data : [" + sendbuf + "]");
                    output.write(sendbuf.getBytes());
                    output.flush();

                    input = new DataInputStream(socket.getInputStream());
                    int inputLen = 0;
                    byte[] inData = new byte[recvbufLen];

                    inputLen = input.read(inData, 0, inData.length);
                    if (inputLen > 0)
                        recvbuf = new String(inData, 0, inputLen, "big5");
                    
                    break;
                }
            } catch (Exception e) {
            } finally {
                if (input != null)
                    input.close();
                if (output != null)
                    output.close();
                System.out.println("Terminated");
            }
        } catch (IOException e) {
            System.out.println("Exception : " + e.getMessage());
            e.printStackTrace();
        }

        return recvbuf;
  }
    
    /***********************************************************************/
    void insertCrdStopLog() throws Exception {
    	busi.SqlPrepare sp = new busi.SqlPrepare();
        sp.sql2Insert("crd_stop_log");
        sp.ppstr2("proc_seqno", hProcSeqno);
        sp.ppstr2("crt_time", sysDate + sysTime);
        sp.ppstr2("card_no", hOnbaCardNo);
        sp.ppstr2("current_code", hOnbaOppType);
        sp.ppstr2("oppost_reason", hOnbaOppReason);
        sp.ppstr2("oppost_date", hOnbaOppDate);
        sp.ppstr2("trans_type", pTransType);
        sp.ppstr2("stop_source", "1");
        sp.ppstr2("send_type", "1");
        sp.ppstr2("bank_actno", hStopBankActno);
        sp.ppstr2("to_ibm_date", sysDate);
        sp.ppstr2("proc_code", hProcCode);
        sp.ppstr2("proc_date", sysDate);
        sp.ppstr2("mod_user", "system");
        sp.ppdate("mod_time");
        sp.ppstr2("mod_pgm", prgmId);
        sqlExec(sp.sqlStmt(), sp.sqlParm());

        if (sqlRowNum == -1) {
        	System.out.println("insert_crd_stop_log err");
        }
    }

}
