/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109/11/23  V1.00.01    shiyuqi   updated for project coding standard       *     
 *  109/11/30  V1.00.02    JeffKung  updated for TCB                           *
 *  111/09/22  V1.00.03    JeffKung  rsk_type重新整理                                                 *
 ******************************************************************************/

package Bil;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*疑異查核作業*/
public class BilA004 extends AccessDAO {
    private String progname = "疑異查核作業   111/09/22 V1.00.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallErrorDesc = "";
    String hTempUser = "";

    String prgmName = "疑異查核作業";
    String rptName = "";
    int tmpInt = 0;
    String errMsg = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hMCurpModPgm = "";
    String hMCurpModTime = "";
    String hMCurpModUser = "";
    long hMCurpModSeqno = 0;

    String hSystemDate = "";
    String hSystemDateF = "";
    String hMCurpBillType = "";
    String hMCurpCardNo = "";
    String hMCurpPurchaseDate = "";
    String hMCurpMerchantNo = "";
    String hMCurpAuthCode = "";
    String hMCurpSignFlag = "";
    String hMCurpAcnoPSeqno = "";
    String hMCurpRowid = "";
    String hMCurpRskType = "";
    String hMCurpRskRsn = "";
    String hMCurpPaymentType = "";
    String hBusinessDate = "";
    int tempCount2 = 0;
    String hCardGpNo = "";
    String hCardCurrentCode = "";
    String hCardNewEndDate = "";
    String hCardOppostDate = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctStatus = "";
    String hAcnoStatusChangeDate = "";
    String hAcnoStmtCycle = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoAutopayAcctNo = "";

    int totalCnt = 0;

    // *********************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            // =====================================
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            commonRtn();
            showLogMessage("I", "", "Process_date (businessDate) = " + hBusinessDate);

            hModPgm = javaProgram;
            hMCurpModPgm = hModPgm;
            hMCurpModTime = hModTime;
            hMCurpModUser = hModUser;
            hMCurpModSeqno = hModSeqno;

            selectBilCurpost();

            // ==============================================
            // 固定要做的
            showLogMessage("I", "", String.format("程式執行結束,筆數=[%d]", totalCnt));
            
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void commonRtn() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += "  from ptr_businday ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
        }
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += "  from dual ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
        }
        hModSeqno = comcr.getModSeq();
        hModUser = comc.commGetUserID();
        hModTime = hSystemDate;

    }

    /***********************************************************************/
    void selectBilCurpost() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "bill_type,";
        sqlCmd += "card_no,";
        sqlCmd += "purchase_date,";
        sqlCmd += "bil_curpost.sign_flag,";
        sqlCmd += "bil_curpost.auth_code,";
        sqlCmd += "bil_curpost.acno_p_seqno,";
        sqlCmd += "bil_curpost.mcht_no as mcht_no,";
        sqlCmd += "doubt_type,";
        sqlCmd += "bil_curpost.rsk_rsn,"; 
        sqlCmd += "bil_curpost.payment_type,"; 
        sqlCmd += "bil_curpost.rowid  rowid ";
        sqlCmd += " from bil_curpost , bil_postcntl ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and decode(err_chk_ok_flag   ,'','N',err_chk_ok_flag)    in ('Y','y') ";
        sqlCmd += "  and decode(format_chk_ok_flag,'','N',format_chk_ok_flag) in ('N','n') ";
        sqlCmd += "  and decode(curr_post_flag    ,'','N',curr_post_flag)     in ('N','n') ";
        sqlCmd += "  and decode(confirm_flag_p    ,'','N',confirm_flag_p)     in ('Y','y') ";
        sqlCmd += "  and decode(manual_upd_flag   ,'','N',manual_upd_flag)    != 'Y' ";
        sqlCmd += "  and batch_date  = substr(bil_curpost.batch_no,1,8) ";
        sqlCmd += "  and batch_unit  = substr(bil_curpost.batch_no,9,2) ";
        sqlCmd += "  and batch_seq   = substr(bil_curpost.batch_no,11,4) ";
        openCursor();
        while (fetchTable()) {
            hMCurpBillType = getValue("bill_type");
            hMCurpCardNo = getValue("card_no");
            hMCurpPurchaseDate = getValue("purchase_date");
            hMCurpMerchantNo = getValue("mcht_no");
            hMCurpSignFlag = getValue("sign_flag");
            hMCurpAuthCode = getValue("auth_code");
            hMCurpAcnoPSeqno = getValue("acno_p_seqno");
            hMCurpRskRsn = getValue("rsk_rsn");
            hMCurpPaymentType = getValue("payment_type");
            hMCurpRowid = getValue("rowid");

            totalCnt++;

            /* debug
                showLogMessage("D", "", "888 Card=" + hMCurpCardNo + "," + hMCurpBillType + ",cnt=" + totalCnt);
            */
            
            if (totalCnt % 5000 == 0 || totalCnt == 1)
                showLogMessage("I", "", "Current Process record=" + totalCnt);

            hMCurpRskType = "";
            chkCrdCard();

            //tempCount2>0代表存在於卡片檔
            if (tempCount2 > 0) { 
                chkActAcno();
            }

            // 請款資料無授權碼 或授權碼值小於六位數且必須是正向交易
            if ((hMCurpRskType.length() == 0) && hMCurpAuthCode.length() < 6 && "-".equals(hMCurpSignFlag) == false ) {
            	hMCurpRskType = "2";
            }

            //tempCount2>0代表存在於卡片檔
            //RSK_FACTORMASTER 風險監控記錄主檔  (rsk_type == "3")
            //若為負向交易不再做別的檢查
            if (hMCurpRskType.length() == 0 && "-".equals(hMCurpSignFlag) == false ) { 
                chkRskFactorMaster();
            }
            
            // 檢查凍結碼 (rsk_type == "4")
            if (hMCurpRskType.length() == 0 && "-".equals(hMCurpSignFlag) == false )  {
            	chkCcaCardAcct();
            }
            
            /* debug
                showLogMessage("D", "", "   888 rsk=" + hMCurpRskType);
            */

            if (hMCurpRskType.length() != 0) {
                daoTable   = "bil_curpost";
                updateSQL  = " rsk_type        = ?,";
                updateSQL += " rsk_rsn         = ?, ";  //若是分期交易,卡號不存在時rsk_rsn放I0
                updateSQL += " mod_time        = sysdate ,";
                updateSQL += " manual_upd_flag = 'N',";
                updateSQL += " err_chk_ok_flag = 'N'";
                whereStr   = "where rowid      = ? ";
                setString(1, hMCurpRskType);
                setString(2, hMCurpRskRsn);
                setRowId(3, hMCurpRowid);
                updateTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("update_bil_curpost not found!","", hMCurpCardNo);
                }
            } else {
                daoTable   = "bil_curpost";
                updateSQL  = " err_chk_ok_flag = 'N',";
                updateSQL += " mod_time        = sysdate ";
                whereStr   = "where rowid      = ? ";
                setRowId(1, hMCurpRowid);
                updateTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("update_bil_curpost not found!","", hMCurpCardNo);
                }
            }
        }
        closeCursor();
    }

    /**********************************************************************/
    void chkCrdCard() throws Exception {
        String hCardPSeqno = "";

        tempCount2 = 0;
        sqlCmd = "select 1 temp_count2,";
        sqlCmd += "current_code,";
        sqlCmd += "oppost_date,";
        sqlCmd += "p_seqno,";
        sqlCmd += "current_code,";
        sqlCmd += "new_end_date, ";
        sqlCmd += "acno_p_seqno ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no  = ? ";
        setString(1, hMCurpCardNo);
        tmpInt = selectTable();
		if (tmpInt > 0) {
			tempCount2 = getValueInt("temp_count2");
			hCardOppostDate = getValue("oppost_date");
			hCardGpNo = getValue("p_seqno");
			hCardCurrentCode = getValue("current_code");
			hCardNewEndDate = getValue("new_end_date");
			hCardPSeqno = getValue("acno_p_seqno");
		}

		if (tempCount2 == 0) {
			hMCurpRskType = "1";
			
			//若是分期付款找不到卡號時,會放I0
			if("I".equals(hMCurpPaymentType)) {
				hMCurpRskRsn = "I0";
			}
			
			return;
		}

		//若為負向交易不再做別的檢查
		if ("-".equals(hMCurpSignFlag) == true) {
			return;
		}
		
		/*
		 * debug showLogMessage("D", "", " 8888 mcht=[" + hMCurpMerchantNo + "]" +
		 * hMCurpBillType);
		 */

		// 0:正常 1:一般>停用 2:掛失 3:強停 4:其他 5: 偽卡
		if (hCardCurrentCode.substring(0, 1).equals("0") == false
				&& (comcr.str2long(hMCurpPurchaseDate) > comcr.str2long(hCardOppostDate))) {
			hMCurpRskType = "2";
			return;
		}

		if (comcr.str2long(hMCurpPurchaseDate) > comcr.str2long(hCardNewEndDate)) {
			hMCurpRskType = "2";
			return;
		}

	}

    /*********************************************************************/
    void chkActAcno() throws Exception {
    	//null 
    }
    
    void chkRskFactorMaster() throws Exception {
        sqlCmd = "select card_no, auth_no, tx_date, tx_time";
        sqlCmd += " from rsk_factormaster  ";
        sqlCmd += "where card_no  = ? ";
        sqlCmd += " and auth_no  = ? ";
        sqlCmd += " and problem_flag = 'Y' ";   //監控表述列問交
        		;
        setString(1, hMCurpCardNo);
        setString(2, hMCurpAuthCode);
        
        tmpInt = selectTable();
		if (tmpInt > 0) {
			hMCurpRskType = "3";
		}
    }
    
    void chkCcaCardAcct() throws Exception {
    	
    	sqlCmd = "  select block_reason1, block_reason2, block_reason3, block_reason4, block_reason5, block_date ";
		sqlCmd += "   from cca_card_acct";
		sqlCmd += "  where acno_p_seqno = ? ";
		sqlCmd += "    and debit_flag = 'N' ";
		setString(1, hMCurpAcnoPSeqno);
		int tmpInt = selectTable();

		/*
		 * debug showLogMessage("D", "", " 8888 ccas=[" + tmp_int + "]" +
		 * getValue("block_status") + "," + getValue("block_date"));
		 */
		
		if (tmpInt > 0 ) {
			if (getValue("block_reason1").length() != 0 || getValue("block_reason2").length() != 0
				|| getValue("block_reason3").length() != 0 || getValue("block_reason4").length() != 0
				|| getValue("block_reason5").length() != 0) {
				if ((comcr.str2long(hMCurpPurchaseDate) > comcr.str2long(getValue("block_date")))
					&& (comcr.str2long(hMCurpPurchaseDate) <= comcr.str2long(hCardNewEndDate))) {
					hMCurpRskType = "4";
					return;
				}
			}
		}
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilA004 proc = new BilA004();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
