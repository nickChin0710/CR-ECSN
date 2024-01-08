/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------- *
*  109/04/21  V1.00.01    Pino      program initial                         *
*  109/06/10  V1.00.02    Pino      select onbat_2ecs                       *
*  109/12/23  V1.00.03   shiyuqi       updated for project coding standard  *
*  110/01/12  V1.00.04   Wilson     mark票證掛失Insert table                   *
*  110/09/16  V1.00.05   Wilson     新增son_card_flag、indiv_crd_lmt          *
*  112/01/18  V1.00.06   Wilson     trans_type增加'5'(使變更停掛原因報送JCIC)        *
*  112/02/02  V1.00.07   Wilson     insert apr_user、apr_date改為空白                           *
*  112/04/22  V1.00.08   Wilson     增加extendField                          *
*  112/06/27  V1.00.09   Wilson     add electronic_code_old                 *
*  112/08/18  V1.00.10   Wilson     信用卡掛失、偽卡停用不補發，連動註銷貴賓卡                                *
*  112/12/03  V1.00.11   Wilson     crd_item_unit不判斷卡種                                                        *
*  113/01/04  V1.00.12   Wilson     重複執行不當掉                                                                                    *
****************************************************************************/

package Crd;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

//import Tsc.Tsc0302;

/*停掛卡資料寫入主檔作業*/
public class CrdG007 extends AccessDAO {
    private String progname = "信用卡停掛重製處理程式 113/01/04  V1.00.12";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    String prgmId = "CrdG007";
    String cardNo = "";
    String oppType = "";
    String oppReason = "";
    String oppDate = "";
    String isRenew = "";
    String mailBranch = "";
    String pgmName = "";
    String negOppReason = "";
    String hDebitFlag = "";
    int hStatus = 0;
    String hOnbaTscAutoloadFlag = "";
    String hOnbaTransType = "";
    String hOnbaRowid = "";
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
    String hCardBankActno = "";
    String hCardNewCardNo = "";
    String hMbtmAcctType = "";
    String hMbtmCardType = "";
    String hMbtmSupFlag = "";
    String hMbtmUnitCode = "";
    String hMbtmBinNo = "";
    String hMbtmRegBankNo = "";
    String hMbtmEngName = "";
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
    String hCardBinType = "";
    String hCardNegDelDate = "";   
    String hCardSonCardFlag = "";
    int hCardIndivCrdLmt = 0;
    String hCardElectronicCode = "";
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
    String pSupFlag = "";
    String pComboIndicator = "";
    String pComboAcctNo = "";
    int hPtrExtnYear = 0;
    int hPtrReissueExtMm = 0;
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
    String hMbosEmbossSource = "";
    String hMbosEmbossReason = "";
    String hTempSource = "";
    String hRowid = "";
    String hPaymentDate = "";
    String hSystemDate  = "";
    String hNextDate    = "";
    String hBusiBusinessDate = "";
    int hSystemDd = 0;
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
    // *****************************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if(comm.isAppActive(javaProgram)) {
				showLogMessage("I", "", "本程式" + prgmId + "已有另依程序啟動中, 不執行..");
				return (0);
            }
            if (args.length > 1) {
                comc.errExit("Usage : CrdG007 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            selectOnbat2ecs();
//            if (tscCnt > 0)
//                showLogMessage("I", "", String.format("處理送智慧卡公司筆數=[%d]", tscCnt));

//20210917 悠遊卡連線掛失已改至授權程式處理
//            try {
//                String[] newArgs = {};
//                Tsc0302 tsc0302 = new Tsc0302();
//                tsc0302.mainProcess(newArgs);
//            } catch (Exception ex) {
//                showLogMessage("I", "", "無法執行 Tsc0302 ERROR!");
 //           }
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
void selectPtrBusinday() throws Exception 
{
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd')      h_system_date,";
        sqlCmd += "to_char(sysdate+1,'yyyymmdd')    h_next_date,";
        sqlCmd += "to_number(to_char(sysdate,'dd')) h_system_dd ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hSystemDate        = getValue("h_system_date");
            hNextDate          = getValue("h_next_date");
            hSystemDd          = getValueInt("h_system_dd");
        }
}
/***************************************************************/
void getBatchno() throws Exception 
{
   String hTmpDate = "";
   String hTmpbatchno = "";
   long hTmpRecno=0;
   int  hTmpNo=0;
  

   sqlCmd  = " select to_number(nvl(substr(max(batchno),7,2),0)) as h_tmp_no, ";
   sqlCmd += "        nvl(max(recno),0) as h_tmp_recno, ";
   sqlCmd += "        to_char(sysdate+1,'yymmdd') as h_tmp_date ";
   sqlCmd += "   from crd_emboss_tmp ";
   sqlCmd += "  where substr(batchno,1,6) = to_char(sysdate+1,'yymmdd') ";
   if(selectTable() > 0 ) {
	   hTmpNo    = getValueInt("h_tmp_no");
	   hTmpRecno = getValueLong("h_tmp_recno");
	   hTmpDate  = getValue("h_tmp_date");
   }

   if(hTmpNo == 0)
   {
     hTmpNo = 1;
     hTmpRecno = 0;
   }
   hTmpbatchno = String.format("%s%02d", hTmpDate, hTmpNo);
   hBatchno = hTmpbatchno; 
   hRecno=hTmpRecno;

if(debug==1) showLogMessage("I", "", String.format("BATCHNO [%s] RECNO [%d]",hBatchno,hRecno));

}

	void selectOnbat2ecs() throws Exception {
		int errorFlag = 0;
		pgmName = "CrdG007";
        sqlCmd = "select ";
        sqlCmd += "card_no,";
        sqlCmd += "opp_type,";
        sqlCmd += "opp_reason,";
        sqlCmd += "opp_date,";
        sqlCmd += "decode(is_renew,'','N',is_renew) h_onba_is_renew,";
        sqlCmd += "mail_branch,";
        sqlCmd += "tsc_autoload_flag,";
        sqlCmd += "trans_type,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from onbat_2ecs ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and to_which    = '1' ";
        sqlCmd += "  and trans_type  in ('5','6') ";
        sqlCmd += "  and proc_status = '0' ";
        sqlCmd += "order by card_no,dog ";
        extendField = "onbat.";
        int recordCnt = selectTable();
        if (debug == 1)
            showLogMessage("I", "", "888 CCA_ONBAT cnt= " + recordCnt);
        for (int i = 0; i < recordCnt; i++) {
        	cardNo    = getValue("onbat.card_no", i);
        	oppType   = getValue("onbat.opp_type", i);
        	oppReason = getValue("onbat.opp_reason", i);
        	oppDate   = getValue("onbat.opp_date", i);
        	isRenew   = getValue("onbat.h_onba_is_renew", i);
        	mailBranch = getValue("onbat.mail_branch", i);
        	hOnbaTscAutoloadFlag = getValue("onbat.tsc_autoload_flag", i);
        	hOnbaTransType = getValue("onbat.trans_type", i);
            hOnbaRowid = getValue("onbat.rowid", i);
            if (debug == 1)
                showLogMessage("I", "", " 888 card_no=" + cardNo);
            hStatus = 0;
            if ((comc.getSubString(cardNo, 0, 2).equals("  ")) || (cardNo.length() == 0)) {
                showLogMessage("I", "", String.format("card_no is null "));
                hStatus = 2;
                updateOnbat2ecs();
                continue;
            }
            hDebitFlag = "Y";
            sqlCmd = "select 'N' as  h_debit_flag ";
            sqlCmd += "  from crd_card  a ";
            sqlCmd += " where a.card_no   = ? ";
            setString(1, cardNo);
            if (selectTable() > 0) {
                hDebitFlag = getValue("h_debit_flag");
            }

            if (debug == 1)
                showLogMessage("I", "", " 888 debit_flag=" + hDebitFlag);

            if (hDebitFlag.equals("Y")) {
                continue;
            }
		
        selectPtrBusinday();
        getBatchno();
		hInsOk = 0;
		hDelOk = 0;

		errorFlag = 0;
		errorFlag = getCardData();
		if (debug == 1)
			showLogMessage("I", "", " 888 chk card=" + errorFlag);
		if (errorFlag != 0) {
            hStatus = 2;
			if (errorFlag == 2) {
                hStatus = 1;
                updateOnbat2ecs();
                continue;
			}
			
		}
		/********************************************
		 * 2001/07/02 掛失和偽卡需檢核是否有有效卡, 若無,則UPDATE act_acno
		 ********************************************/
		if ((oppType.equals("2")) || (oppType.equals("5"))) {
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
		
		hPaymentDate = "";
		if (comc.getSubString(oppReason, 0, 1).equals("U") && hAcnoDebtCloseDate.length() > 0) {
			hPaymentDate = hAcnoDebtCloseDate;
			insertCrdJcic();

		}			
		else {
			insertCrdJcic();
		}
		
		if(hOnbaTransType.equals("5")) {
			hStatus = 1;
            updateOnbat2ecs();
            continue;
		}
		
		if (debug == 1)
			showLogMessage("I", "", " 888 renew   =" + isRenew);
		/******************************************
		 * 掛失及偽卡若以重製過不可再補發 (current_code != '0') modified by shu 2002/08/25
		 ******************************************/
		if (isRenew.equals("Y")) {
			/******************************************************************
			 * 只接收opp_type='2' or opp_type='5'才可做重製 (2002/01/02)
			 *****************************************************************/
			hRecno++;
			/** 若已重製過不可再補發或重製 **/
			if (hCardNewCardNo.length() == 0) {
				if (!hCardReissueStatus.equals("2")) {
					insertCrdEmbossTmp();
				}
			}

		}
		/********************************************************************
		 * 改成不重製(不管current_code為何)delete crd_Emboss_tmpe 2001/10/22
		 ********************************************************************/
		if (isRenew.equals("N")) {
			delCrdEmbossTmp();
		}

		/* 1:一般停用 2:掛失停用 3:強制停用 4:其他停用 5:偽卡停用 */
		/* 0: 是不是恢復正常 */
		if ((oppType.equals("0")) || (oppType.equals("2")) || (oppType.equals("5"))) {
			if ((oppType.equals("0")) || (oppType.equals("2"))) {
				selectTscCard();
				selectIchCard(1);
				if ((oppType.equals("0")) && hIardIchCardNo.length() > 0) {
					deleteIchRtn();
				}
			}
			if ((oppType.equals("2")) || (oppType.equals("5"))) {
				selectIpsCard();
				hIardIchCardNo = "";
				selectIchCard(3);
				if (oppType.equals("5")) {
					hB09bBalRsn = "02";
				} else {
					hB09bBalRsn = "03";
				}
				if (hIardIchCardNo.length() > 0) {
					insertIchB04bSpecial();
					insertIchB09bBal();
				}
			}
			tscCnt++;
		} else {
			hIardIchCardNo = "";
			selectIchCard(2);
			hB09bBalRsn = "02";
			if (oppType.equals("5") && oppReason.equals("AI") && hIardIchCardNo.length() > 0) {
				hB09bBalRsn = "01";
			}
			if (hIardIchCardNo.length() > 0) {
				insertIchB04bSpecial();
				insertIchB09bBal();
			}

		}
		
		//信用卡掛失、偽卡停用不補發，連動註銷貴賓卡
		if ((oppType.equals("2")) || (oppType.equals("5"))) {
			if(isRenew.equals("N")) {
				rtn = selectCrdCardPp();
				if (rtn == 0) {
					updateCrdCardPp();
				}					
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
//V1.00.02刪除		
//		if(!oppType.equals("4"))
//			insert_cca_outgoing();
		
		commitDataBase();
        }
	}
        /***********************************************************************/
        void updateOnbat2ecs() throws Exception {
            daoTable   = "onbat_2ecs";
            updateSQL  = " proc_status = ?,";
            updateSQL += " proc_date   = to_char(sysdate,'yyyymmdd'),";
            updateSQL += " dop         = sysdate";
            whereStr = "where rowid  = ? ";
            setInt(1, hStatus);
            setRowId(2, hOnbaRowid);
            updateTable();
        }
    /***********************************************************************/
    int delCrdEmbossTmp() throws Exception {
        daoTable = "crd_emboss_tmp";
        whereStr = "where old_card_no = ?  ";
        whereStr += "and emboss_source = '5' ";
        setString(1, cardNo);
        deleteTable();
        if (notFound.equals("Y")) {
            return (0);
        } else {
            hDelOk = 1;
            return (0);
		}
    }

    /***********************************************************************/
    void insertHceStatusTxn() throws Exception {
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("v_card_no", hHstnVCardNo);
        setValue("card_no", cardNo);
        setValue("status_code", hHstnStatusCode);
        setValue("change_code", "4");
        setValue("from_pgm", pgmName);
        setValue("sir_user", hHstnSirUser);
        setValue("sir", hHstnSir);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", prgmId);
        daoTable = "hce_status_txn";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_hce_status_txn duplicate!", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void chkStopStatus() throws Exception {
        if (hAcnoStopStatus.equals("Y")) {
            daoTable  = "act_acno";
            updateSQL = "stop_status    = ''";
            whereStr  = "where acno_p_seqno  = ? ";
            setString(1, hCardPSeqno);
            updateTable();
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
        setString(1, cardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hHstnVCardNo   = getValue("v_card_no");
            hHstnStatusCode = getValue("status_code");
            hHstnSirUser    = getValue("sir_user");
            hHstnSir         = getValue("sir");
        } else {
            return 1;
        }

        return (0);

    }

    /***********************************************************************/    
    int selectCrdCardPp() throws Exception {
    	int tmpcnt = 0;

        sqlCmd  = "select count(*) as cnt ";
        sqlCmd += " from crd_card_pp ";
        sqlCmd += "where card_no     = ?   ";
        setString(1, cardNo);
        selectTable();
        tmpcnt = getValueInt("cnt");
        if (tmpcnt > 0) {
        	return (0);
        } 
        else {
            return (1);
        }
    }

    /***********************************************************************/
   void updateCrdCardPp() throws Exception {

    		daoTable   = "crd_card_pp";
            updateSQL  = " current_code = '1', ";
            updateSQL += " oppost_date = to_char(sysdate,'yyyymmdd'),";
            updateSQL += " oppost_reason = 'P3', ";
            updateSQL += " mod_pgm = ?, ";
            updateSQL += " mod_user = ?, ";
            updateSQL += " mod_time = sysdate ";
            whereStr   = " where card_no = ? ";
            setString(1, prgmId);
            setString(2, pgmName);
            setString(3, cardNo);
            updateTable();            
   }
    
    /***********************************************************************/   
    int insertCrdEmbossTmp() throws Exception {
        int hCount = 0;
        /* 刪除續卡or毀損重製待製卡 */
        sqlCmd = "select count(*) h_count ";
        sqlCmd += " from crd_emboss_tmp  ";
        sqlCmd += "where old_card_no     = ?  ";
		sqlCmd += "  and (emboss_source <> '5' or (emboss_source = '5'  ";
        sqlCmd += "  and emboss_reason not in ('1','3'))) ";
        setString(1, hMbtmOldCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCount = getValueInt("h_count");
        }
        if (hCount > 0) {
            daoTable  = "crd_emboss_tmp";
            whereStr  = "where old_card_no = ?  ";
            whereStr += "  and (emboss_source <> '5' or (emboss_source = '5'  ";
            whereStr += "  and emboss_reason not in ('1','3'))) ";
            setString(1, hMbtmOldCardNo);
            deleteTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("delete_crd_emboss_tmp not found!", "", comcr.hCallBatchSeqno);
            }

            daoTable = "crd_card";
            updateSQL  = " expire_chg_flag = decode(change_status ,'1','',expire_chg_flag),";
            updateSQL += " expire_reason   = decode(change_status ,'1','',expire_reason),";
            updateSQL += " expire_chg_date = decode(change_status ,'1','',expire_chg_date),";
            updateSQL += " change_status   = decode(change_status ,'1','',change_status),";
            updateSQL += " change_reason   = decode(change_status ,'1','',change_reason),";
            updateSQL += " change_date     = decode(change_status ,'1','',change_date),";
            updateSQL += " reissue_status  = decode(reissue_status,'1','',reissue_status),";
            updateSQL += " reissue_reason  = decode(reissue_status,'1','',reissue_reason),";
            updateSQL += " reissue_date    = decode(reissue_status,'1','',reissue_date),";
            updateSQL += " mod_user        = ?,";
            updateSQL += " mod_pgm         = ?,";
            updateSQL += " mod_time        = sysdate";
            whereStr   = "where card_no    = ? ";
            setString(1, pgmName);
            setString(2, prgmId);
            setString(3, hMbtmOldCardNo);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_card not found!", "", comcr.hCallBatchSeqno);
            }
        }
        /* 已存在不可重複入檔 */
        sqlCmd = "select count(*) h_count ";
        sqlCmd += " from crd_emboss_tmp  ";
        sqlCmd += "where old_card_no = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hMbtmOldCardNo);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hCount = getValueInt("h_count");
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

        switch (comcr.str2int(oppType)) { 
        case 2:
            hMbtmEmbossReason = "1";
            break;
        case 4:
            hMbtmEmbossReason = "2";
            break;
        case 5:
            hMbtmEmbossReason = "3";
            break;
        }
        hMbtmToNcccCode = "Y";

        hMbtmComboIndicator = "";
        sqlCmd = "select decode(combo_indicator,'','N',combo_indicator) h_mbtm_combo_indicator ";
        sqlCmd += " from ptr_group_code  ";
        sqlCmd += "where group_code = decode(cast(? as varchar(10)),'','0000',?) ";
        setString(1, hMbtmGroupCode);
        setString(2, hMbtmGroupCode);
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_group_code not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hMbtmComboIndicator = getValue("h_mbtm_combo_indicator");
        }
        hMbosMnoId = "";
        hMbosMsisdn = "";
        hMbosServiceId = "";
        hMbosSeId = "";
        hMbosServiceVer = "";
        hMbosServiceType = "";
        hMbosSirNo = "";
        if (comc.getSubString(hGcrdCardMoldFlag, 0, 1).equals("M")) {
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
            setString(1, cardNo);
            setString(2, cardNo);
            recordCnt = selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("select_mob_card not found!", "", comcr.hCallBatchSeqno);
            }
            if (recordCnt > 0) {
                hMbosMnoId = getValue("mno_id");
                hMbosMsisdn = getValue("msisdn");
                hMbosServiceId = getValue("service_id");
                hMbosSeId = getValue("se_id");
                hMbosServiceVer = getValue("service_ver");
                hMbosServiceType = getValue("service_type");
                hMbosSirNo = getValue("sir_no");
            }
        }

        setValue("batchno", hMbtmBatchno);
        setValueLong("recno", hMbtmRecno);
        setValue("emboss_source", hMbtmEmbossSource);
        setValue("emboss_reason", hMbtmEmbossReason);
        setValue("resend_note", hMbtmResendNote);
        setValue("to_nccc_code", hMbtmToNcccCode);
        setValue("reg_bank_no", hMbtmRegBankNo);
        setValue("risk_bank_no", hMbtmRiskBankNo);
        setValue("card_type", hMbtmCardType);
        setValue("bin_no", hMbtmBinNo);
        setValue("unit_code", hMbtmUnitCode);
        setValue("acct_type", hMbtmAcctType);

        setValue("acct_key", comcr.ufAcnoKey(hCardPSeqno));

        setValue("card_no", "");
        setValue("old_card_no", hMbtmOldCardNo);
        setValue("status_code", hMbtmStatusCode);
        setValue("reason_code", hMbtmReasonCode);
        setValue("sup_flag", hMbtmSupFlag);
        setValue("apply_id", hMbtmApplyId);
        setValue("apply_id_code", hMbtmApplyIdCode);

        String[] info = comcr.getIDInfo(hCardMajorIdPSeqno);
        setValue("pm_id", info[0]);
        setValue("pm_id_code", info[1]);

        setValue("group_code", hMbtmGroupCode);
        setValue("source_code", hMbtmSourceCode);
        setValue("corp_no", hMbtmCorpNo);
        setValue("corp_no_code", hMbtmCorpNoCode);
        setValue("chi_name", hMbtmChiName);
        setValue("eng_name", hMbtmEngName);
        setValue("birthday", hMbtmBirthday);
        setValue("force_flag", hMbtmForceFlag);
        setValue("valid_fm", hMbtmValidFm);
        setValue("valid_to", hMbtmValidTo);
        setValue("major_card_no", hMbtmMajorCardNo);
        setValue("major_valid_fm", hMbtmMajorValidFm);
        setValue("major_valid_to", hMbtmMajorValidTo);
        setValue("chg_addr_flag", hMbtmChgAddrFlag);
        setValue("mail_type", hMbtmMailType);
        setValue("credit_lmt", hMbtmCreditLmt);
        setValue("emboss_4th_data", hMbtmEmboss4thData);
        setValue("old_beg_date", hMbtmOldBegDate);
        setValue("old_end_date", hMbtmOldEndDate);
        setValue("crt_date", sysDate);
        setValue("apr_user", "");
        setValue("apr_date", "");
        setValue("emboss_date", hMbtmEmbossDate);
        setValue("nccc_batchno", hMbtmNcccBatchno);
        setValueInt("nccc_recno", hMbtmNcccRecno);
        setValue("nccc_type", hMbtmNcccType);
        setValue("ic_flag", hMbtmIcFlag);
        setValue("combo_indicator", hMbtmComboIndicator);
        setValue("curr_code", hCardCurrCode);
        setValue("jcic_score", hCardJcicScore);
        setValue("mno_id", hMbosMnoId);
        setValue("msisdn", hMbosMsisdn);
        setValue("service_id", hMbosServiceId);
        setValue("se_id", hMbosSeId);
        setValue("service_ver", hMbosServiceVer);
        setValue("service_type", hMbosServiceType);
        setValue("sir_no", "");
        setValue("mail_branch", mailBranch);
        setValue("mod_user", pgmName);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", prgmId);
        setValueDouble("mod_seqno", hMbtmModSeqno);
        setValue("tsc_autoload_flag", hOnbaTscAutoloadFlag);
        setValue("son_card_flag", hCardSonCardFlag);
        setValueInt("indiv_crd_lmt", hCardIndivCrdLmt);
        setValue("electronic_code_old", hCardElectronicCode);

        daoTable = "crd_emboss_tmp";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_emboss_tmp duplicate!", hMbtmBatchno, comcr.hCallBatchSeqno);
        }

        hInsOk = 1;
        return (0);
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

        setString(1, cardNo);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hIardIpsCardNo  = getValue("ips_card_no", i);
            hIardCurrentCode = getValue("current_code", i);
            hIardNewEndDate = getValue("new_end_date", i);

//            insertIpsOppostLog();
        }
    }

    /***********************************************************************/
//    void insertIpsOppostLog() throws Exception {
//        String updateCode = "X";
//        if (oppType.equals("2"))
//            updateCode = "1";
//        if (oppType.equals("0"))
//            updateCode = "3";
//
//        setValue("crt_date", sysDate);
//        setValue("crt_time", sysTime);
//        setValue("ips_card_no", hIardIpsCardNo);
//        setValue("card_no", cardNo);
//        setValue("current_code", hIardCurrentCode);
//        setValue("new_end_date", hIardNewEndDate);
//        setValue("oppost_date", oppDate);
//        setValue("oppost_reason", oppReason);
//        setValue("update_code", updateCode);
//        setValue("proc_date", "00000000");
//        setValue("mod_time", sysDate + sysTime);
//        setValue("mod_pgm", prgmId);
//        daoTable = "ips_oppost_log";
//        insertTable();
//        if (dupRecord.equals("Y")) {
//            comcr.errRtn("insert_ips_oppost_log duplicate!", "", comcr.hCallBatchSeqno);
//        }
//
//    }

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
        setString(1, cardNo);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTardTscCardNo = getValue("tsc_card_no", i);
            hTardCurrentCode = getValue("current_code", i);
            hTardNewEndDate = getValue("new_end_date", i);

//            insertTscOppostLog();
        }

    }

    /***********************************************************************/
//    void insertTscOppostLog() throws Exception {
//        String updateCode = "X";
//        if (oppType.equals("2"))
//            updateCode = "1";
//        if (oppType.equals("0"))
//            updateCode = "3";
//
//        setValue("crt_date", sysDate);
//        setValue("crt_time", sysTime);
//        setValue("tsc_card_no", hTardTscCardNo);
//        setValue("card_no", cardNo);
//        setValue("current_code", hTardCurrentCode);
//        setValue("new_end_date", hTardNewEndDate);
//        setValue("oppost_date", oppDate);
//        setValue("oppost_reason", oppReason);
//        setValue("update_code", updateCode);
//        setValue("proc_date", "00000000");
//        setValue("mod_time", sysDate + sysTime);
//        setValue("mod_pgm", prgmId);
//        daoTable = "tsc_oppost_log";
//        insertTable();
//        if (dupRecord.equals("Y")) {
//            comcr.errRtn("insert_tsc_oppost_log duplicate!", "", comcr.hCallBatchSeqno);
//        }
//    }

    /***********************************************************************/
    void insertCrdJcic() throws Exception {
        String hRowid = "";

        hRowid = "";

        sqlCmd = "select rowid  as rowid ";
        sqlCmd += " from crd_jcic  ";
        sqlCmd += "where card_no  = ?  ";
        sqlCmd += "and trans_type = 'C'  ";
        sqlCmd += "and to_jcic_date =''  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, cardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hRowid = getValue("rowid");

            daoTable   = "crd_jcic";
            updateSQL  = " current_code  = ?,";
            updateSQL += " oppost_reason = ?,";
            updateSQL += " oppost_date   = ?,";
            updateSQL += " payment_date  = ?,";
            updateSQL += " mod_user      = ?,";
            updateSQL += " mod_time      = sysdate,";
            updateSQL += " mod_pgm       = ?";
            whereStr   = "where rowid    = ? ";
            setString(1, oppType);
            setString(2, oppReason);
            setString(3, oppDate);
            setString(4, hPaymentDate);
            setString(5, pgmName);
            setString(6, prgmId);
            setRowId(7, hRowid);
            updateTable();

            return;
        }

        setValue("card_no"      , cardNo);
        setValue("crt_date"     , hSystemDate);
        setValue("crt_user"     , pgmName);
        setValue("trans_type"   , "C");
        setValue("current_code" , oppType);
        setValue("oppost_reason", oppReason);
        setValue("oppost_date"  , oppDate);
        setValue("payment_date" , hPaymentDate);
        setValue("is_rc"        , hIsRc);
        setValue("mod_user"     , pgmName);
        setValue("mod_time"     , sysDate + sysTime);
        setValue("mod_pgm"      , prgmId);
        daoTable = "crd_jcic";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_jcic duplicate!", "", comcr.hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int updateActAcno() throws Exception {

        if (hAcnoRcUseIndicator.equals("1")) {
            daoTable   = "act_acno";
            updateSQL  = " rc_use_b_adj     = ?,";
            updateSQL += " rc_use_indicator = '2',";
            updateSQL += " rc_use_s_date    = ?,";
            updateSQL += " rc_use_e_date    = to_char(add_months(to_date(?,'yyyymmdd'),3),'yyyymmdd'),";
            updateSQL += " mod_time      = sysdate,";
            updateSQL += " mod_pgm       = ?,";
            updateSQL += " mod_user      = ?";
            whereStr   = "where acno_p_seqno  = ? ";
            setString(1, hAcnoRcUseIndicator);
            setString(2, oppDate);
            setString(3, oppDate);
            setString(4, prgmId);
            setString(5, pgmName);
            setString(6, hCardPSeqno);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_act_acno not found!", "", comcr.hCallBatchSeqno);
            }

        }

        return (0);
    }

    /***********************************************************************/
    int chkValidCard() throws Exception {
        int count = 0;
        String pCardNo = "";
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
        sqlCmd += " order by sup_flag ";
        setString(1, hCardAcctType);
        setString(2, hCardPSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            pCardNo = getValue("card_no", i);
            pCurrentCode = getValue("current_code", i);
            pSupFlag = getValue("sup_flag", i);
            pComboIndicator = getValue("combo_indicator", i);
            pComboAcctNo = getValue("combo_acct_no", i);

            if ((pCurrentCode.equals("0")) && (!pCardNo.equals(cardNo))) {
                count++;
            }
        }

        if (count <= 0)
            return (1);

        return (0);
    }

    /***********************************************************************/
    int getCardData() throws Exception {

        sqlCmd = "select ";
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
        sqlCmd += "card_type,";
        sqlCmd += "sup_flag,";
        sqlCmd += "unit_code,";
        sqlCmd += "bin_no,";
        sqlCmd += "reg_bank_no,";
        sqlCmd += "eng_name,";
        sqlCmd += "member_note,";
        sqlCmd += "member_id,";
        sqlCmd += "change_reason,";
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
        sqlCmd += "bin_type,";
        sqlCmd += "uf_date_add(new_end_date,0,0,1) as neg_del_date,";
        sqlCmd += "mod_seqno, ";
        sqlCmd += "son_card_flag, ";
		sqlCmd += "indiv_crd_lmt, ";
		sqlCmd += "electronic_code  ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no = ? ";
        setString(1, cardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCardIdPSeqno = getValue("id_p_seqno");
            hCardPSeqno = getValue("acno_p_seqno");
            hCardGpNo = getValue("p_seqno");
            hMbtmIcFlag = getValue("ic_flag");
            hCardComboIndicator = getValue("h_card_combo_indicator");
            hCardComboAcctNo   = getValue("combo_acct_no");
            hCardReissueReason  = getValue("reissue_reason");
            hCardReissueStatus  = getValue("reissue_status");
            hCardReissueDate    = getValue("reissue_date");
            hCardCurrentCode    = getValue("current_code");
            hCardOldBankActno  = getValue("old_bank_actno");
            hCardBankActno      = getValue("bank_actno");
            hCardNewCardNo     = getValue("new_card_no");
            hMbtmAcctType       = getValue("acct_type");
            hMbtmCardType       = getValue("card_type");
            hMbtmSupFlag        = getValue("sup_flag");
            hMbtmUnitCode       = getValue("unit_code");
            hMbtmBinNo          = getValue("bin_no");
            hMbtmRegBankNo     = getValue("reg_bank_no");
            hMbtmEngName        = getValue("eng_name");
            hMbtmMemberNote     = getValue("member_note");
            hMbtmMemberId       = getValue("member_id");
            hMbtmChangeReason    = getValue("change_reason");
            hCardMajorIdPSeqno = getValue("major_id_p_seqno");
            hMbtmMajorCardNo   = getValue("major_card_no");
            hMbtmGroupCode      = getValue("group_code");
            hMbtmSourceCode     = getValue("source_code");
            hMbtmCorpNo         = getValue("corp_no");
            hMbtmCorpNoCode    = getValue("corp_no_code");
            hMbtmForceFlag      = getValue("force_flag");
            hMbtmOldBegDate    = getValue("new_beg_date");
            hMbtmOldEndDate    = getValue("new_end_date");
            hMbtmEmboss4thData = getValue("emboss_data");
            hCardMailType       = getValue("mail_type");
            hCardMailNo         = getValue("mail_no");
            hCardMailBranch     = getValue("mail_branch");
            hCardMailProcDate  = getValue("mail_proc_date");
            hCardExpireChgFlag = getValue("expire_chg_flag");
            hCardExpireChgDate = getValue("expire_chg_date");
            hCardExpireReason   = getValue("expire_reason");
            hCardCurrCode       = getValue("curr_code");
            hCardJcicScore      = getValue("jcic_score");
            hMbtmCreateDate     = getValue("crt_date");
            hMbtmSuperId        = getValue("apr_user");
            hMbtmSuperDate      = getValue("apr_date");
            hCardBinType        = getValue("bin_type");
            hCardNegDelDate    = getValue("neg_del_date");
            hMbtmModSeqno       = getValueDouble("mod_seqno");
            hCardSonCardFlag    = getValue("son_card_flag");
			hCardIndivCrdLmt    = getValueInt("indiv_crd_lmt");
			hCardElectronicCode    = getValue("electronic_code");
        }
//V1.00.02刪除
//        } else {
//            sqlCmd = "select count(*) temp_int ";
//            sqlCmd += " from ecs_crd_card  ";
//            sqlCmd += "where card_no = ? ";
//            setString(1, cardNo);
//            recordCnt = selectTable();
//            if (recordCnt > 0) {
//                temp_int = getValueInt("temp_int");
//            }
//
//            if (temp_int > 0)
//                return (2);
//
//            return (1);
//        }

        hCardAcctType = hMbtmAcctType;
        /*************** 抓取正卡效期 ************************/
        if (hMbtmSupFlag.equals("1")) {
            sqlCmd = "select new_beg_date,";
            sqlCmd += "new_end_date ";
            sqlCmd += " from crd_card  ";
            sqlCmd += "where card_no = ? ";
            setString(1, hMbtmMajorCardNo);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hMbtmMajorValidFm = getValue("new_beg_date");
                hMbtmMajorValidTo = getValue("new_end_date");
            } else {
                return 1;
            }
        }

        sqlCmd = "select birthday,";
        sqlCmd += "chi_name ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno  = ?  ";
        setString(1, hCardIdPSeqno);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hMbtmBirthday = getValue("birthday");
            hMbtmChiName = getValue("chi_name");
        } else {
            return (1);
        }

        hAcnoStopStatus = "";
        hAcnoGpNo = "";
        hAcnoPSeqno = "";
        hAcnoRcUseIndicator = "";
        hAcnoDebtCloseDate = "";
        hMbtmCardNo = cardNo;
        hMbtmOldCardNo = cardNo;
        hAcnoComboIndicator = "";
        hAcnoComboAcctNo = "";
        getPtrExtn();
        if (hCardExpireChgFlag.length() == 0) {
            getValidDate();
        } else {
            hMbtmValidFm = hMbtmOldBegDate;
            if(oppType.equals("4")) {
                hMbtmValidTo = comm.lastdateOfmonth(comm.nextMonthDate(hMbtmOldEndDate,hPtrReissueExtMm));
            }
            else {
            	hMbtmValidTo = hMbtmOldEndDate;
            }
        }

        if (hSystemDd >= 25) {
            sqlCmd = "select to_char(add_months(to_date(?,'yyyymmdd'), 1) ,'yyyymmdd') h_mbtm_valid_fm ";
            sqlCmd += " from dual ";
            setString(1, hMbtmValidFm);
            if (selectTable() > 0) {
                hMbtmValidFm = getValue("h_mbtm_valid_fm");
            }
        }

        String[] info = comcr.getIDInfo(hCardIdPSeqno);
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
        setString(1, hCardPSeqno);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hMbtmRiskBankNo = getValue("risk_bank_no");
            hMbtmCreditLmt = getValue("line_of_credit_amt");
            hAcnoComboIndicator = getValue("combo_indicator");
            hAcnoComboAcctNo = getValue("combo_acct_no");
            hAcnoStopStatus = getValue("stop_status");
            hIsRc = getValue("rc_use_indicator");
            hAcnoGpNo = getValue("p_seqno");
            hAcnoPSeqno = getValue("acno_p_seqno");
            hAcnoRcUseIndicator = getValue("rc_use_indicator");
            hAcnoDebtCloseDate = getValue("debt_close_date");
        } else {
            return 1;
        }

        return (0);
    }

    /***********************************************************************/
    int getPtrExtn() throws Exception {
        /* 取得展期參數 */
        hPtrExtnYear = 0;
//V1.00.02刪除       
//        h_gcrd_card_mold_flag = "";
//        sqlCmd = "select card_mold_flag ";
//        sqlCmd += " from ptr_group_card  ";
//        sqlCmd += "where group_code = ?  ";
//        sqlCmd += "  and card_type  = ? ";
//        setString(1, h_mbtm_group_code);
//        setString(2, h_mbtm_card_type);
//        int recordCnt = selectTable();
//        if (recordCnt > 0) {
//            h_gcrd_card_mold_flag = getValue("card_mold_flag");
//        }

        sqlCmd = "select extn_year,reissue_extn_mm ";
        sqlCmd += " from crd_item_unit   ";
        sqlCmd += "where unit_code = ?  ";
        setString(1, hMbtmUnitCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hPtrExtnYear = getValueInt("extn_year");
            hPtrReissueExtMm = getValueInt("reissue_extn_mm");
        } else {
            hPtrExtnYear = 2;
            hPtrReissueExtMm = 0;
        }

        return (0);
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
        if ((isRenew.equals("Y")) && (hInsOk == 1)) {
            hReissueStatus = "1";
            hReissueReason = hMbtmEmbossReason;
            hReissueDate = sysDate;
        }
        /******************************************************************
         * 若由重製卡變成不重製卡時,必須先檢核是否在crd_Emboss_tmp,
         * 若存在,且確定刪除成功後才可將,reissue_status等清空,否則不變
         ******************************************************************/
        if ((isRenew.equals("N")) && (hDelOk == 1)) {
            hReissueStatus = "";
            hReissueReason = "";
            hReissueDate = "";
        }
        daoTable   = "crd_card";
//V1.00.02刪除
//        updateSQL  = " current_code   = ?,";
//        updateSQL += " oppost_reason  = ?,";
//        updateSQL += " oppost_date    = ?,";
        updateSQL += " reissue_status = ?,";
        updateSQL += " reissue_reason = ?,";
        updateSQL += " reissue_date   = ?,";
        updateSQL += " mod_user       = ?,";
        updateSQL += " mod_time       = sysdate,";
        updateSQL += " mod_pgm        = ?";
        whereStr   = "where card_no   = ? ";
//        setString(1, oppType);
//        setString(2, oppReason);
//        setString(3, oppDate);
        setString(1, hReissueStatus);
        setString(2, hReissueReason);
        setString(3, hReissueDate);
        setString(4, pgmName);
        setString(5, prgmId);
        setString(6, cardNo);
        updateTable();

        hMbosEmbossSource = "";
        hMbosEmbossReason = "";
        hTempSource = "";
        sqlCmd = "select emboss_source,";
        sqlCmd += "emboss_reason ";
        sqlCmd += " from crd_emboss  ";
        sqlCmd += "where card_no = ?  ";
        sqlCmd += "and crt_date in ( select max(crt_date) from crd_emboss where card_no = ?) ";
        setString(1, cardNo);
        setString(2, cardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hMbosEmbossSource = getValue("emboss_source");
            hMbosEmbossReason = getValue("emboss_reason");
        }

        switch (comcr.str2int(hMbosEmbossSource)) {
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
            switch (comcr.str2int(hMbosEmbossReason)) {
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
        if (oppReason.length() > 0) {
            setValue("to_nccc_date", "");
            setValue("batchno", prgmId);
            setValueDouble("recno", 1);
            setValue("card_no"   , cardNo);
            setValue("chi_name"  , hMbtmChiName);
            setValue("group_code", hMbtmGroupCode);
            setValue("emboss_source", hMbosEmbossSource);
            setValue("unit_code"    , hMbtmUnitCode);
            setValue("emboss_reason", hMbosEmbossReason);
            setValue("nccc_rtn_date", "");
            setValue("nccc_source", hTempSource);
            setValue("nccc_input_kind", "2"); /* 1: 新 2: 停 */
            setValue("stop_kind", oppType);
            setValue("stop_rsn", oppReason.length() > 0 ? oppReason.substring(0, 1) : "");
            setValue("to_send_flag", "Y");
            setValue("mod_time", sysDate + sysTime);
            setValue("mod_pgm", prgmId);
            daoTable = "crd_nccc_ic";
            insertTable();

        } else {
            daoTable = "crd_nccc_ic";
            whereStr = "where card_no  = ?  ";
            whereStr += "and mod_pgm  = ?  ";
            whereStr += "and to_nccc_date ='' ";
            setString(1, cardNo);
            setString(2, prgmId);
            deleteTable();

        }

        return;
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
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDiffDate = getValue("h_diff_date");
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
            setString(1, hEndDate);
            setInt(2, hPtrExtnYear);
            if (selectTable() > 0) {
                hValidFm = getValue("h_valid_fm");
                hValidTo = getValue("h_valid_to");
            }
            hMbtmValidFm = hValidFm;
            hMbtmValidTo = hValidTo;
        } else {
            /************************************************
             * 本月一號
             ************************************************/
            sqlCmd = "select to_char(sysdate,'yyyymm')||'01' h_valid_fm ";
            sqlCmd += " from dual ";
            if (selectTable() > 0) {
                hValidFm = getValue("h_valid_fm");
            }

            hMbtmValidFm = hValidFm;
            hMbtmValidTo = hEndDate;
        }

        return;
    }

/**************************************************************************/
void deleteIchRtn() throws Exception 
{
  daoTable  = "ich_b04b_special ";
  whereStr  = "where ich_card_no = ?   ";
  whereStr += "  and proc_flag  <> 'Y' ";
  setString(1, hIardIchCardNo);
  deleteTable();
  if(notFound.equals("Y")) {}

  daoTable  = "ich_b09b_bal ";
  whereStr  = "where ich_card_no = ?   ";
  whereStr += "  and proc_flag  <> 'Y' ";
  setString(1, hIardIchCardNo);
  deleteTable();
  if(notFound.equals("Y")) {}
}
/**************************************************************************/
void selectIchCard(int idx) throws Exception 
{

if(debug == 1)
   showLogMessage("I", "", String.format("select_ich_card[%s][%s][%d]", cardNo, hBusiBusinessDate,idx));

        sqlCmd  = " SELECT ich_card_no, ";
        sqlCmd += "        current_code, ";
        sqlCmd += "        new_end_date ";
        sqlCmd += "   FROM ich_card ";
        sqlCmd += "  where card_no = ? ";
        sqlCmd += "    and substr(new_end_date,1,6) > substr(?,1,6) ";
        setString(1, cardNo);
        setString(2, hBusiBusinessDate);

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hIardIchCardNo  = getValue("ich_card_no", i);
            hIardCurrentCode = getValue("current_code", i);
            hIardNewEndDate = getValue("new_end_date", i);

            if(debug == 1) showLogMessage("I", "", String.format("step c ich_card_no[%s]", hIardIchCardNo));
//            if(idx == 1) insertIchOppostLog();
        }

}
/**************************************************************************/
//void insertIchOppostLog() throws Exception 
//{
//   if(debug==1) showLogMessage("I", "", String.format("step d tsc_card_no[%s]", hTardTscCardNo));
//
//   String updateCode = "X";
//   if(oppType.equals("2"))
//      updateCode = "1";
//   if(oppType.equals("0"))
//      updateCode = "3";
//
//   setValue("crt_date"     , sysDate);
//   setValue("crt_time"     , sysTime);
//   setValue("ich_card_no"  , hIardIchCardNo);
//   setValue("card_no"      , cardNo);
//   setValue("current_code" , hIardCurrentCode);
//   setValue("new_end_date" , hIardNewEndDate);
//   setValue("oppost_date"  , oppDate);
//   setValue("oppost_reason", oppReason);
//   setValue("update_code"  , updateCode);
//   setValue("proc_date"    , "00000000");
//   setValue("mod_time"     , sysDate + sysTime);
//   setValue("mod_pgm"      , prgmId);
//   daoTable = "ich_oppost_log";
//   insertTable();
//   if (dupRecord.equals("Y")) {
//       comcr.errRtn("insert_ich_oppost_log duplicate!", "", comcr.hCallBatchSeqno);
//   }
//}
/**************************************************************************/
void selectIchB04bSpecial() throws Exception 

{
  tempInt04=0;

  sqlCmd  = " SELECT count(*) as all_cnt ";
  sqlCmd += "   FROM ich_b04b_special ";
  sqlCmd += "  where ich_card_no = ? ";
  setString(1, hIardIchCardNo);

  int recordCnt = selectTable();
  if (recordCnt > 0) {
      tempInt04 = getValueInt("all_cnt");
    }

  if(debug == 1) showLogMessage("I", "", String.format("select_ich_b04b_special cnt=[%d]", tempInt04));
}
/**************************************************************************/
void insertIchB04bSpecial() throws Exception 
{
   if (debug==1)
       showLogMessage("I", "", String.format("step insert_ich_b04b_special[%s]", hIardIchCardNo));

   selectIchB04bSpecial();
   if(tempInt04>0)  return;         

   setValue("ich_card_no"    , hIardIchCardNo);
   setValue("proc_type"      , "5");
   setValue("sys_date"       , sysDate);
   setValue("sys_time"       , sysTime);
   setValue("effc_send_date" , hNextDate);
   setValue("proc_flag"      , "N");
   setValue("ok_flag"        , "N");
   setValue("mod_time"       , sysDate + sysTime);
   setValue("mod_pgm"        , prgmId);
   daoTable = "ich_b04b_special";
   insertTable();
   if (dupRecord.equals("Y")) {
       comcr.errRtn("insert_ich_b04b_special duplicate!", "", comcr.hCallBatchSeqno);
   }
}
/**************************************************************************/
void selectIchB09bBal()     throws Exception 
{
  tempInt09=0;

  sqlCmd  = " SELECT count(*) as all_cnt ";
  sqlCmd += "   FROM ich_b09b_bal ";
  sqlCmd += "  where ich_card_no = ? ";
  setString(1, hIardIchCardNo);

  int recordCnt = selectTable();
  if (recordCnt > 0) {
      tempInt09 = getValueInt("all_cnt");
    }

  if(debug == 1) showLogMessage("I", "", String.format("select_ich_b09b_bal cnt=[%d]", tempInt09));
}
/**************************************************************************/
void insertIchB09bBal()     throws Exception 
{
   if (debug==1)
       showLogMessage("I", "", String.format("step insert_ich_b09b_bal[%s]", hIardIchCardNo));

   selectIchB09bBal();
   if(tempInt09>0)  return;

   setValue("ich_card_no"    , hIardIchCardNo);
   setValue("card_no"        , cardNo);
   setValue("bal_rsn"        , hB09bBalRsn);
   setValue("loss_date"      , sysDate);
   setValue("loss_time"      , sysTime);
   setValue("sys_date"       , sysDate);
   setValue("sys_time"       , sysTime);
   setValue("effc_send_date" , hNextDate);
   setValue("proc_flag"      , "N");
   setValue("ok_flag"        , "N");
   setValue("mod_time"       , sysDate + sysTime);
   setValue("mod_pgm"        , prgmId);
   daoTable = "ich_b09b_bal";
   insertTable();
   if (dupRecord.equals("Y")) {
       comcr.errRtn("insert_ich_b09b_bal duplicate!", "", comcr.hCallBatchSeqno);
   }
}
/**************************************************************************/
void insertCcaOutgoing() throws Exception {
	selectCcaOppTypeReason();
	   setValue("crt_date"        , sysDate);
	   setValue("crt_time"        , sysTime);
	   setValue("card_no"         , cardNo);
	   setValue("key_value"       , "FISC");
	   setValue("key_table"       , "OPPOSITION");
	   setValue("bitmap"          , "");
	   setValue("act_code"        , "1");
	   setValue("crt_user"        , pgmName);
	   setValue("proc_flag"       , "1");
	   setValue("send_times"      , "1");
	   setValue("proc_date"       , sysDate);
	   setValue("proc_time"       , sysTime);
	   setValue("proc_user"       , pgmName);
	   setValue("data_from"       , "1");
	   setValue("resp_code"       , "");
	   setValue("data_type"       , "OPPO");
	   setValue("bin_type"        , hCardBinType);
	   setValue("reason_code"     , negOppReason);
	   setValue("del_date"        , hCardNegDelDate);
	   setValue("bank_acct_no"    , "");
	   setValue("vmj_regn_data"   , "");
	   setValue("vip_amt"         , "0");
	   setValue("mod_time"        , sysDate + sysTime);
	   setValue("mod_pgm"         , prgmId);
	   daoTable = "cca_outgoing";
	   insertTable();
	   if (dupRecord.equals("Y")) {
	       comcr.errRtn("insert_cca_outgoing duplicate!", "", comcr.hCallBatchSeqno);
	   }

	}
/**************************************************************************/
void selectCcaOppTypeReason() throws Exception 
{
  sqlCmd  = " SELECT neg_opp_reason ";
  sqlCmd += "   FROM cca_opp_type_reason ";
  sqlCmd += "  where opp_status = ? ";
  setString(1, oppReason);

  int recordCnt = selectTable();
  if (recordCnt > 0) {
	  negOppReason = getValue("neg_opp_reason");
    }

  if(debug == 1) showLogMessage("I", "", String.format("select_cca_opp_type_reason neg_opp_reason=[%s]", negOppReason));
}
/***********************************************************************/
public static void main(String[] args) throws Exception {
       CrdG007 proc = new CrdG007();
       int retCode = proc.mainProcess(args);
       proc.programEnd(retCode);
}
/***********************************************************************/
}
