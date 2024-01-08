/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/12/29  V1.00.00    phopho     program initial                         
*  109/04/13  V1.00.00    Wendy      modify update_act_acno_1 and update_act_acno*
*  109/12/11  V1.00.02    shiyuqi       updated for project coding standard   *
*  112/09/22  V1.00.05    sunny      取消商務卡戶-公司歸戶統一處理邏輯                             *
*  112/10/26  V1.00.06    sunny      執行結果顯示各帳戶狀態更新筆數                                  *
*  112/11/08  V1.00.07    sunny      顯示執行營業日日期                                                     *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommCol;

public class ColB001 extends AccessDAO {
	public final boolean debug = false; //debug用
	public final boolean debug1 = false; //debug用
    private String progname = "每日自動恢復正常戶及正常戶轉逾放戶處理程式  112/11/08  V1.00.07";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;
    CommCol commCol = null;

    String hCallBatchSeqno = "";

    double hCprmExcTtlLmt1 = 0;
    double hCprmExcOweLmt1 = 0;
    double hCprmExcTtlLmt2 = 0;
    double hCprmExcOweLmt2 = 0;
    int hCprmCodeTtlS1 = 0;
    int hCprmCodeTtlE1 = 0;
    int hCprmCodeOweS1 = 0;
    int hCprmCodeOweE1 = 0;
    int hCprmCodeTtl1 = 0;
    int hCprmCodeOwe1 = 0;
    int hCprmCodeTtl2 = 0;
    int hCprmCodeOwe2 = 0;
    int hCprmCodeTtl3 = 0;
    int hCprmCodeOwe3 = 0;
    String hBusiBusinessDate = "";
//    String h_p_seqno = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoStmtCycle = "";
    String hAcnoIdPSeqno = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoPaymentRate1 = "";
    String hAcnoCreditActNo = "";
    String hAcnoFlag = "";
    String hAcnoAcctStatus = "";
//    String h_acno_acct_sub_status = "";
    String hAcnoLegalDelayCode = "";
    String hAcnoLawsuitMark = "";
    String hAcnoLawsuitMarkDate = "";
    String hAcnoNoDelinquentFlag = "";
    String hAcnoNoDelinquentSDate = "";
    String hAcnoNoDelinquentEDate = "";
    String hAcnoNoCollectionFlag = "";
    String hAcnoNoCollectionSDate = "";
    String hAcnoNoCollectionEDate = "";
    String hAcnoOrgDelinquentDate = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoRowid = "";
    double hAcctAcctJrnlBal = 0;
    double hDebtEndBal = 0;
    String hTempAcctStatus = "";
//    String h_temp_acct_sub_status = "";
    String hTempLegalDelayCode = "";
    String hAcnoModUser = "";
    String hAcnoModPgm = "";
    long hAcnoModSeqno = 0;

    int mCode = 0;
    int totalCnt = 0;
    int procCount = 0;
    int nMinMcode = 0;
    int procAcnoStatusCount = 0;
    int procAcnoStatusCount12 = 0;
    int procAcnoStatusCount34 = 0;
    int procAcnoStatusCount1 = 0;
    int procAcnoStatusCount2 = 0;     
    int procAcnoStatusCount3 = 0;
    int procAcnoStatusCount4 = 0; 
    int procAcnoLegalDelayCount = 0;
    int procAcnoLegalDelayCount12 = 0;
    int procAcnoLegalDelayCount34 = 0;
    int procAcnoLegalDelayCount1 = 0;
    int procAcnoLegalDelayCount2 = 0;
    int procAcnoLegalDelayCount3 = 0;
    int procAcnoLegalDelayCount4 = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0) {
                comc.errExit("Usage : ColB001", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            commCol = new CommCol(getDBconnect(), getDBalias());

            hAcnoModUser = comc.commGetUserID();
            hAcnoModPgm = javaProgram;

            selectColParam();
            selectPtrBusinday();
            showLogMessage("I", "", String.format("執行日期[%s]", hBusiBusinessDate));
            selectActAcno();

            // ==============================================
            // 固定要做的
   
            showLogMessage("I", "", "===========================================" );
            showLogMessage("I", "", "處理總筆數 [" + totalCnt + "]");
            showLogMessage("I", "", "更新總筆數  [" + procCount + "]");
            showLogMessage("I", "", " 更新acno Status總筆數  [" + procAcnoStatusCount + "]");
            showLogMessage("I", "", "   正常戶、逾放戶筆數  [" + procAcnoStatusCount12 + "]");
        	showLogMessage("I", "", "      正常戶筆數  [" + procAcnoStatusCount1 + "]");
			showLogMessage("I", "", "      逾放戶筆數  [" + procAcnoStatusCount2 + "]");
			showLogMessage("I", "", "   催收戶、呆帳戶筆數  [" + procAcnoStatusCount34 + "]");
			showLogMessage("I", "", "      催收戶筆數  [" + procAcnoStatusCount3 + "]");
			showLogMessage("I", "", "      呆帳戶筆數  [" + procAcnoStatusCount4 + "]");
			 showLogMessage("I", "", "------------------------------------------" );
            showLogMessage("I", "", " 更新acno LegalDelay筆數  [" + procAcnoLegalDelayCount + "]" );
			showLogMessage("I", "", "   正常戶、逾放戶筆數  [" + procAcnoLegalDelayCount12 + "]");
			showLogMessage("I", "", "      正常戶筆數  [" + procAcnoLegalDelayCount1 + "]");
			showLogMessage("I", "", "      逾放戶筆數  [" + procAcnoLegalDelayCount2 + "]");
			showLogMessage("I", "", "   催收戶、呆帳戶筆數  [" + procAcnoLegalDelayCount34 + "]");
			showLogMessage("I", "", "      催收戶筆數  [" + procAcnoLegalDelayCount3 + "]");
			showLogMessage("I", "", "      呆帳戶筆數  [" + procAcnoLegalDelayCount4 + "]");
            showLogMessage("I", "", "===========================================" );
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
    void selectColParam() throws Exception {
        hCprmExcTtlLmt1 = 0;
        hCprmExcOweLmt1 = 0;
        hCprmExcTtlLmt2 = 0;
        hCprmExcOweLmt2 = 0;
        hCprmCodeTtlS1 = 0;
        hCprmCodeTtlE1 = 0;
        hCprmCodeOweS1 = 0;
        hCprmCodeOweE1 = 0;
        hCprmCodeTtl1 = 0;
        hCprmCodeOwe1 = 0;
        hCprmCodeTtl2 = 0;
        hCprmCodeOwe2 = 0;
        hCprmCodeTtl3 = 0;
        hCprmCodeOwe3 = 0;

        sqlCmd = "select exc_ttl_lmt_1,";
        sqlCmd += "exc_owe_lmt_1,";
        sqlCmd += "exc_ttl_lmt_2,";
        sqlCmd += "exc_owe_lmt_2,";
        sqlCmd += "m_code_ttl_s1,";
        sqlCmd += "m_code_ttl_e1,";
        sqlCmd += "m_code_owe_s1,";
        sqlCmd += "m_code_owe_e1,";
        sqlCmd += "m_code_ttl_1,";
        sqlCmd += "m_code_owe_1,";
        sqlCmd += "m_code_ttl_2,";
        sqlCmd += "m_code_owe_2,";
        sqlCmd += "m_code_ttl_3,";
        sqlCmd += "m_code_owe_3 ";
        sqlCmd += " from col_param ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_col_param not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCprmExcTtlLmt1 = getValueInt("exc_ttl_lmt_1");
            hCprmExcOweLmt1 = getValueInt("exc_owe_lmt_1");
            hCprmExcTtlLmt2 = getValueInt("exc_ttl_lmt_2");
            hCprmExcOweLmt2 = getValueInt("exc_owe_lmt_2");
            hCprmCodeTtlS1 = getValueInt("m_code_ttl_s1");
            hCprmCodeTtlE1 = getValueInt("m_code_ttl_e1");
            hCprmCodeOweS1 = getValueInt("m_code_owe_s1");
            hCprmCodeOweE1 = getValueInt("m_code_owe_e1");
            hCprmCodeTtl1 = getValueInt("m_code_ttl_1");
            hCprmCodeOwe1 = getValueInt("m_code_owe_1");
            hCprmCodeTtl2 = getValueInt("m_code_ttl_2");
            hCprmCodeOwe2 = getValueInt("m_code_owe_2");
            hCprmCodeTtl3 = getValueInt("m_code_ttl_3");
            hCprmCodeOwe3 = getValueInt("m_code_owe_3");
        }

        nMinMcode = hCprmCodeTtlS1;
        if (nMinMcode > hCprmCodeOweS1)
            nMinMcode = hCprmCodeOweS1;
        if (nMinMcode > hCprmCodeTtl1)
            nMinMcode = hCprmCodeTtl1;
        if (nMinMcode > hCprmCodeOwe1)
            nMinMcode = hCprmCodeOwe1;
        if (nMinMcode > hCprmCodeTtl2)
            nMinMcode = hCprmCodeTtl2;
        if (nMinMcode > hCprmCodeOwe2)
            nMinMcode = hCprmCodeOwe2;
        if (nMinMcode > hCprmCodeTtl3)
            nMinMcode = hCprmCodeTtl3;
        if (nMinMcode > hCprmCodeOwe3)
            nMinMcode = hCprmCodeOwe3;
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
    void selectActAcno() throws Exception {

        sqlCmd = "select ";
//        sqlCmd += "a.p_seqno,";       
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "a.stmt_cycle,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "a.corp_p_seqno,";
        sqlCmd += "a.payment_rate1,";
        sqlCmd += "a.credit_act_no,";
        sqlCmd += "a.acno_flag, ";
        sqlCmd += "decode(a.acct_status,'','1',a.acct_status) h_acno_acct_status,";
//        sqlCmd += "decode(a.acct_sub_status,'','x',a.acct_sub_status) h_acno_acct_sub_status,";  //acct_sub_status 不使用
        sqlCmd += "decode(a.legal_delay_code,'','x',a.legal_delay_code) h_acno_legal_delay_code,";
        sqlCmd += "decode(a.lawsuit_mark,'','N',a.lawsuit_mark) h_acno_lawsuit_mark,";
        sqlCmd += "decode(a.lawsuit_mark_date,'','30000101',a.lawsuit_mark_date) h_acno_lawsuit_mark_date,";
        sqlCmd += "decode(a.no_delinquent_flag,'','N',a.no_delinquent_flag) h_acno_no_delinquent_flag,";
        sqlCmd += "decode(a.no_delinquent_s_date,'','30000101',a.no_delinquent_s_date) h_acno_no_delinquent_s_date,";
        sqlCmd += "decode(a.no_delinquent_e_date,'','30000101',a.no_delinquent_e_date) h_acno_no_delinquent_e_date,";
        sqlCmd += "decode(a.no_collection_flag,'','N',a.no_collection_flag) h_acno_no_collection_flag,";
        sqlCmd += "decode(a.no_collection_s_date,'','30000101',a.no_collection_s_date) h_acno_no_collection_s_date,";
        sqlCmd += "decode(a.no_collection_e_date,'','30000101',a.no_collection_e_date) h_acno_no_collection_e_date,";
        sqlCmd += "a.org_delinquent_date,";
        sqlCmd += "a.pay_by_stage_flag,";
        sqlCmd += "a.int_rate_mcode,";
        sqlCmd += "a.rowid as rowid,";
        sqlCmd += "b.acct_jrnl_bal ";
        sqlCmd += "from act_acct b,act_acno a ";
//        sqlCmd += "where a.p_seqno = a.gp_no ";
//        sqlCmd += "and a.p_seqno = b.p_seqno ";
        sqlCmd += "where a.acno_flag <> 'Y' ";
        sqlCmd += "and a.acno_p_seqno = b.p_seqno ";
        if(debug1) {
        //sqlCmd += "and a.p_seqno = '0007000047' ";
        	sqlCmd += "and a.p_seqno = '0007574491' ";
//        sqlCmd += "limit 100 ";
        }

        openCursor();
        while (fetchTable()) {
//            h_p_seqno = getValue("p_seqno");
            hAcnoPSeqno = getValue("acno_p_seqno");
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctKey = getValue("acct_key");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoPaymentRate1 = getValue("payment_rate1");
            hAcnoCreditActNo = getValue("credit_act_no");
            hAcnoFlag = getValue("acno_flag");
            hAcnoAcctStatus = getValue("h_acno_acct_status");
//            h_acno_acct_sub_status = getValue("h_acno_acct_sub_status");
            hAcnoLegalDelayCode = getValue("h_acno_legal_delay_code");
            hAcnoLawsuitMark = getValue("h_acno_lawsuit_mark");
            hAcnoLawsuitMarkDate = getValue("h_acno_lawsuit_mark_date");
            hAcnoNoDelinquentFlag = getValue("h_acno_no_delinquent_flag");
            hAcnoNoDelinquentSDate = getValue("h_acno_no_delinquent_s_date");
            hAcnoNoDelinquentEDate = getValue("h_acno_no_delinquent_e_date");
            hAcnoNoCollectionFlag = getValue("h_acno_no_collection_flag");
            hAcnoNoCollectionSDate = getValue("h_acno_no_collection_s_date");
            hAcnoNoCollectionEDate = getValue("h_acno_no_collection_e_date");
            hAcnoOrgDelinquentDate = getValue("org_delinquent_date");
            hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
            hAcnoRowid = getValue("rowid");
            hAcctAcctJrnlBal = getValueDouble("acct_jrnl_bal");
            
            if(debug1) {
            	 showLogMessage("I", "", " AcnoPSeqno[" + hAcnoPSeqno + "],AcctKey["+hAcnoAcctKey+"],AcctStatus["+hAcnoAcctStatus+"],PaymentRate1["+hAcnoPaymentRate1+"]");
            }
            

            totalCnt++;
            if (totalCnt % 10000 == 0) {
                showLogMessage("I", "", "  目前處理筆數 =[" + totalCnt + "]");
                dateTime();
            }
            
            hTempAcctStatus = "1";
//            h_temp_acct_sub_status = "1";
            hTempLegalDelayCode = "9";
            mCode = getValueInt("int_rate_mcode");
            
            if(debug1) {
           	 showLogMessage("I", "", " int_rate_mcode[" + mCode + "]");
           }

//          m_code = get_M_code(h_acno_p_seqno);
//          m_code = comr.getMcode(h_acno_acct_type, h_acno_p_seqno);

            /*若為公司戶-- 20230830 debug 先disable*/
            
//          if (hAcnoFlag.equals("2")) {
//          	
//          	hAcctAcctJrnlBal = commCol.getCorpAcctJrnlBal(hAcnoCorpPSeqno, hAcnoAcctType);
//          	mCode = commCol.getCorpMaxMcode(hAcnoCorpPSeqno, hAcnoAcctType);
//          }
//          else {
//          	mCode = getValueInt("int_rate_mcode");
//          }
          

            if (hAcnoAcctStatus.toCharArray()[0] >= '3') {
                if (mCode == 0)
                    hAcnoLegalDelayCode = "9";
                if (mCode > 0)
                    hAcnoLegalDelayCode = "0";
                if (mCode > 3)
                    hAcnoLegalDelayCode = "1";
                if (mCode > 6)
                    hAcnoLegalDelayCode = "2";
                if (mCode > 12)
                    hAcnoLegalDelayCode = "3";
                if (mCode > 24)
                    hAcnoLegalDelayCode = "4";
                updateActAcno1();
                              
                continue;
            }

            if ((hAcnoPayByStageFlag.equals("00")) || (hAcnoPayByStageFlag.equals("NW"))) {
                hTempAcctStatus = "1";
//                h_temp_acct_sub_status = "4";
                hTempLegalDelayCode = "9";
                updateActAcno();
                continue;
            }

            if ((hAcnoNoDelinquentFlag.equals("Y"))
                    && (hAcnoNoDelinquentSDate.compareTo(hBusiBusinessDate) <= 0)
                    && (hAcnoNoDelinquentEDate.compareTo(hBusiBusinessDate) >= 0)) {
                hTempAcctStatus = "1";
//                h_temp_acct_sub_status = "4";
                hTempLegalDelayCode = "9";
                updateActAcno();
                continue;
            }

            if ((hAcnoAcctStatus.equals("2")) && (mCode >= hCprmCodeTtl3)
                    && (mCode >= hCprmCodeOwe3) && (hAcnoNoCollectionFlag.equals("Y"))
                    && (hAcnoNoCollectionSDate.compareTo(hBusiBusinessDate) <= 0)
                    && (hAcnoNoCollectionEDate.compareTo(hBusiBusinessDate) >= 0)) {
                hTempAcctStatus = "2";
//                h_temp_acct_sub_status = "3";
                hTempLegalDelayCode = "0";
                if ((mCode >= 4) && (mCode <= 6))
                    hTempLegalDelayCode = "1";
                if ((mCode >= 7) && (mCode <= 12))
                    hTempLegalDelayCode = "2";
                if ((mCode >= 12) && (mCode <= 24))
                    hTempLegalDelayCode = "3";
                if (mCode > 24)
                    hTempLegalDelayCode = "4";
                updateActAcno();
                continue;
            }
            
            showLogMessage("I", "", String.format("001--mCode[%s],nMinMcode[%s],CprmCodeTtl2[%s],DebtEndBal[%s],ExcOweLmt1[%s]\n", mCode,nMinMcode,hCprmCodeTtl2,hDebtEndBal,hCprmExcOweLmt1));


            if (mCode >= nMinMcode) {
//            	if (hAcnoFlag.equals("1")) {
                selectActDebt1();
//            	}
//            	else {
//            		selectActDebtCorp();
//            	}

                if (((mCode >= hCprmCodeTtlS1) && (mCode <= hCprmCodeTtlE1)
                        && (hAcctAcctJrnlBal <= hCprmExcTtlLmt1))
                        && ((mCode >= hCprmCodeOweS1) && (mCode <= hCprmCodeOweE1)
                                && (hDebtEndBal <= hCprmExcOweLmt1))) {
                    hTempAcctStatus = "1";
//                    h_temp_acct_sub_status = "2";
                    hTempLegalDelayCode = "9";
                    updateActAcno();
                    continue;
                }

                if ((mCode >= hCprmCodeTtl1) && (hAcctAcctJrnlBal > hCprmExcTtlLmt1)
                        && ((!hAcnoNoDelinquentFlag.equals("Y"))
                                || (hAcnoNoDelinquentSDate.compareTo(hBusiBusinessDate) > 0)
                                || (hAcnoNoDelinquentEDate.compareTo(hBusiBusinessDate) < 0))) {
                    hTempAcctStatus = "2";
//                    h_temp_acct_sub_status = "2";
                    hTempLegalDelayCode = "1";
                    updateActAcno();
                    continue;
                }
                
                showLogMessage("I", "", String.format("002--mCode[%s],CprmCodeTtl2[%s],DebtEndBal[%s],ExcOweLmt1[%s]\n", mCode,hCprmCodeTtl2,hDebtEndBal,hCprmExcOweLmt1));

                if ((mCode >= hCprmCodeTtl2) && (hDebtEndBal > hCprmExcOweLmt1)
                        && ((!hAcnoNoDelinquentFlag.equals("Y"))
                                || (hAcnoNoDelinquentSDate.compareTo(hBusiBusinessDate) > 0)
                                || (hAcnoNoDelinquentEDate.compareTo(hBusiBusinessDate) < 0))) {
                    hTempAcctStatus = "2";
//                    h_temp_acct_sub_status = "2";
                    hTempLegalDelayCode = "1";
                    showLogMessage("I", "", String.format("test-001[%s]\n", hAcnoPSeqno));
                    updateActAcno();
                    continue;
                }

                if (((mCode >= hCprmCodeOwe1) && (hAcctAcctJrnlBal <= hCprmExcTtlLmt2))
                        || ((mCode >= hCprmCodeOwe2) && (hDebtEndBal <= hCprmExcOweLmt2))) {
                    hTempAcctStatus = "1";
                    if (mCode >= 7) {
//                        h_temp_acct_sub_status = "3";
                    } else {
//                        h_temp_acct_sub_status = "2";
                    }
                    hTempLegalDelayCode = "9";
                    updateActAcno();
                    continue;
                }
            }

            if ((mCode >= 1) && (hAcnoLawsuitMark.equals("Y"))
                    && (hAcnoLawsuitMarkDate.compareTo(hBusiBusinessDate) <= 0)
                    && ((!hAcnoNoDelinquentFlag.equals("Y"))
                            || (hAcnoNoDelinquentSDate.compareTo(hBusiBusinessDate) > 0)
                            || (hAcnoNoDelinquentEDate.compareTo(hBusiBusinessDate) < 0))) {
                hTempAcctStatus = "2";
//                h_temp_acct_sub_status = "1";
                hTempLegalDelayCode = "0";
                updateActAcno();
                continue;
            }
            updateActAcno();
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateActAcno1() throws Exception {
    	
    	 showLogMessage("I", "", String.format("996-updateActAcno1,AcnoPSeqno[%s],hAcnoLegalDelayCode[%s],hTempLegalDelayCode[%s] \n", hAcnoPSeqno,hAcnoLegalDelayCode,hTempLegalDelayCode));   
    	 
        if (hAcnoLegalDelayCode.compareTo(hTempLegalDelayCode) == 0)
            return;

        hAcnoModSeqno = comcr.getModSeq();
        
//        if (hAcnoFlag.equals("2")) {
//        
//        String[] pSeqnoArr = commCol.getCorpPseqno(hAcnoCorpPSeqno, hAcnoAcctType);
//    	for(int i = 0; i < pSeqnoArr.length; i++) {
//    		String str = pSeqnoArr[i];
//
//        daoTable = "act_acno";
//        updateSQL = "legal_delay_code  = ?,";
//        updateSQL += " legal_delay_code_date = ?,";
//        updateSQL += " mod_time  = sysdate,";
//        updateSQL += " mod_user  = ?,";
//        updateSQL += " mod_pgm   = ?,";
//        updateSQL += " mod_seqno  = ?";
//        whereStr = "where p_seqno = ? ";        
//        setString(1, hTempLegalDelayCode);
//        setString(2, hBusiBusinessDate);
//        setString(3, hAcnoModUser);
//        setString(4, hAcnoModPgm);
//        setLong(5, hAcnoModSeqno);
//        setString(6, str);
//        updateTable();
//        if (notFound.equals("Y")) {
//            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
//        }
//        
//        if(debug)
//            showLogMessage("I", "", String.format("996-update ActAcno,rowid[%d],acct_status[%s],status_chage_date[%s],LegalDelayCode[%s],delinquent_date[%s] \n", hAcnoRowid,hTempAcctStatus,hBusiBusinessDate,hTempLegalDelayCode,hAcnoOrgDelinquentDate));                
//
//
//        procCount++;
//           }
//        }
//        
//        else {
        	daoTable = "act_acno";
            updateSQL = "legal_delay_code  = ?,";
            updateSQL += " legal_delay_code_date = ?,";
            updateSQL += " mod_time  = sysdate,";
            updateSQL += " mod_user  = ?,";
            updateSQL += " mod_pgm   = ?,";
            updateSQL += " mod_seqno  = ?";
            whereStr = "where rowid = ? ";
            setString(1, hTempLegalDelayCode);
            setString(2, hBusiBusinessDate);
            setString(3, hAcnoModUser);
            setString(4, hAcnoModPgm+"_1");
            setLong(5, hAcnoModSeqno);
            setRowId(6, hAcnoRowid);
            updateTable();
            
            if (notFound.equals("Y")) {
                comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
            }

            if(debug)
            {
            	//showLogMessage("I", "", String.format("997-update ActAcno LegalDelay,p_seqno[%s],mcode[%s],acct_status[%s],tmp_status[%s],status_chage_date[%s],更新LegalDelayCode[%s],更新LegalDelayCodeDate[%s],delinquent_date[%s] \n", hAcnoPSeqno,mCode,hAcnoAcctStatus,hTempAcctStatus,hBusiBusinessDate,hTempLegalDelayCode,hBusiBusinessDate,hAcnoOrgDelinquentDate));
                
            	showLogMessage("I", "", String.format("997-1.update ActAcno LegalDelay,p_seqno[%s],mcode[%s],acct_status[%s],tmp_status[%s]\n", hAcnoPSeqno,mCode,hAcnoAcctStatus,hTempAcctStatus));
            	showLogMessage("I", "", String.format("997-2.update 更新LegalDelayCode[%s],更新LegalDelayCodeDate[%s] \n", hBusiBusinessDate,hTempLegalDelayCode,hBusiBusinessDate,hAcnoOrgDelinquentDate));
            	showLogMessage("I", "", String.format("================================================================================================================================================================="));
            }
                         
            procCount++;
            procAcnoLegalDelayCount++;    
                       
            if (hAcnoAcctStatus.compareTo("1") == 0 || hAcnoAcctStatus.compareTo("2") == 0){
            	if (hAcnoAcctStatus.compareTo("1") == 0)
            	{
            		procAcnoLegalDelayCount1++;
            	}
            	if (hAcnoAcctStatus.compareTo("2") == 0)
            	{
            		procAcnoLegalDelayCount2++;
            	}
            	procAcnoLegalDelayCount12++;
             }
            if (hAcnoAcctStatus.compareTo("3") == 0 || hAcnoAcctStatus.compareTo("4") == 0){
            	if (hAcnoAcctStatus.compareTo("3") == 0)
            	{
            		procAcnoLegalDelayCount3++;
            	}
            	if (hAcnoAcctStatus.compareTo("4") == 0)
            	{
            		procAcnoLegalDelayCount4++;
            	}
            	procAcnoLegalDelayCount34++;
             }
        }
//    }

    /***********************************************************************/
    void selectActDebt1() throws Exception {
        hDebtEndBal = 0;

        sqlCmd = "select sum(nvl(end_bal,0)) h_debt_end_bal ";
        sqlCmd += " from act_debt ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and acct_code in ('BL','CA','IT','ID','AO','OT','CB') ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_debt_1.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDebtEndBal = getValueDouble("act_debt_1.h_debt_end_bal");
        }
    }
 
    /***********************************************************************/
    void selectActDebtCorp() throws Exception {
    	
    	double hDebtEndBal = 0;
    	String[] pSeqnoArr = commCol.getCorpPseqno(hAcnoCorpPSeqno, hAcnoAcctType);
    	for(int i = 0; i < pSeqnoArr.length; i++) {
    		String str = pSeqnoArr[i];	

        sqlCmd = "select sum(nvl(end_bal,0)) as h_debt_end_bal ";
        sqlCmd += " from act_debt ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and acct_code in ('BL','CA','IT','ID','AO','OT','CB') ";
        setString(1, str);
        
        hDebtEndBal = hDebtEndBal + getValueDouble("h_debt_end_bal");
        
    	}
    	    	        
        extendField = "act_debt_1.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDebtEndBal = getValueDouble("act_debt_1.h_debt_end_bal");
        }
    }

    /***********************************************************************/
    
    
    void updateActAcno() throws Exception {    	
        if ((hAcnoAcctStatus.compareTo(hTempAcctStatus) == 0)
//                && (h_acno_acct_sub_status.compareTo(h_temp_acct_sub_status) == 0)
                && (hAcnoLegalDelayCode.compareTo(hTempLegalDelayCode) == 0))
            return;

        if ((hTempAcctStatus.equals("2")) && (!hAcnoAcctStatus.equals("2"))) {
            hAcnoOrgDelinquentDate = hBusiBusinessDate;
        }
                
        hAcnoModSeqno = comcr.getModSeq();
        
      //  if (hAcnoFlag.equals("2")) {
        
//        String[] pSeqnoArr = commCol.getCorpPseqno(hAcnoCorpPSeqno, hAcnoAcctType);
//    	for(int i = 0; i < pSeqnoArr.length; i++) {
//    		String str = pSeqnoArr[i];
//
//        daoTable = "act_acno";
//        updateSQL = "acct_status   = ?,";
////        updateSQL += " acct_sub_status  = ?,";
//        updateSQL += " status_change_date = ?,";
//        updateSQL += " legal_delay_code  = ?,";
//        updateSQL += " legal_delay_code_date = ?,";
//        updateSQL += " org_delinquent_date = decode(cast(? as varchar(1)),'2',cast(? as varchar(8)),'1','',org_delinquent_date),";
//        updateSQL += " mod_time  = sysdate,";
//        updateSQL += " mod_user  = ?,";
//        updateSQL += " mod_pgm   = ?,";
//        updateSQL += " mod_seqno  = ?";
//        whereStr = "where p_seqno = ? ";
//        setString(1, hTempAcctStatus);
////        setString(2, h_temp_acct_sub_status);
//        setString(2, hBusiBusinessDate);
//        setString(3, hTempLegalDelayCode);
//        setString(4, hBusiBusinessDate);
//        setString(5, hTempAcctStatus);
//        setString(6, hAcnoOrgDelinquentDate);
//        setString(7, hAcnoModUser);
//        setString(8, hAcnoModPgm);
//        setLong(9, hAcnoModSeqno);
//        setString(10,str);
//        
//        
//        updateTable();
//        if (notFound.equals("Y")) {
//            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
//        }
//
//        if(debug)
//            showLogMessage("I", "", String.format("[debug]update ActAcno,p_seqno[%s] acct_status[%s],status_chage_date[%s],LegalDelayCode[%s],delinquent_date[%s] ", hAcnoPSeqno,hTempAcctStatus,hBusiBusinessDate,hTempLegalDelayCode,hAcnoOrgDelinquentDate));                
//           //showLogMessage("I", "", String.format("999-update ActAcno,rowid[%d],acct_status[%s],status_chage_date[%s],LegalDelayCode[%s],delinquent_date[%s] \n", hAcnoRowid,hTempAcctStatus,hBusiBusinessDate,hTempLegalDelayCode,hAcnoOrgDelinquentDate));
//
//        procCount++;
//    	} 
//      }
//        else {
            daoTable = "act_acno";
            updateSQL = "acct_status   = ?,";
//            updateSQL += " acct_sub_status  = ?,";
            updateSQL += " status_change_date = ?,";
            updateSQL += " legal_delay_code  = ?,";
            updateSQL += " legal_delay_code_date = ?,";
            updateSQL += " org_delinquent_date = decode(cast(? as varchar(1)),'2',cast(? as varchar(8)),'1','',org_delinquent_date),";
            updateSQL += " mod_time  = sysdate,";
            updateSQL += " mod_user  = ?,";
            updateSQL += " mod_pgm   = ?,";
            updateSQL += " mod_seqno  = ?";
            whereStr = "where rowid = ? ";
            setString(1, hTempAcctStatus);
//            setString(2, h_temp_acct_sub_status);
            setString(2, hBusiBusinessDate);
            setString(3, hTempLegalDelayCode);
            setString(4, hBusiBusinessDate);
            setString(5, hTempAcctStatus);
            setString(6, hAcnoOrgDelinquentDate);
            setString(7, hAcnoModUser);
            setString(8, hAcnoModPgm);
            setLong(9, hAcnoModSeqno);
            setRowId(10, hAcnoRowid);
                        
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
            }
            
            if(debug)
            {
                //showLogMessage("I", "", String.format("998-update ActAcno AcnoStatus,p_seqno[%s],mcode[%s],acct_status[%s],tmp_status[%s],status_chage_date[%s],LegalDelayCode[%s],delinquent_date[%s] \n", hAcnoPSeqno,mCode,hAcnoAcctStatus,hTempAcctStatus,hBusiBusinessDate,hTempLegalDelayCode,hAcnoOrgDelinquentDate));            

            	showLogMessage("I", "", String.format("998-1.update ActAcno AcnoStatus,p_seqno[%s],mcode[%s],更新acct_status[%s],tmp_status[%s], \n", hAcnoPSeqno,mCode,hAcnoAcctStatus,hTempAcctStatus));
            	showLogMessage("I", "", String.format("998-2.update 更新statusChageDate/LegalDelayDate[%s],更新LegalDelayCode[%s],更新delinquent_date[%s] \n", hBusiBusinessDate,hTempLegalDelayCode,hAcnoOrgDelinquentDate));
            	showLogMessage("I", "", String.format("================================================================================================================================================================="));
            }
            procCount++;
            procAcnoStatusCount++;

             if (hAcnoAcctStatus.compareTo("1") == 0 || hAcnoAcctStatus.compareTo("2") == 0){            	
            	 if (hAcnoAcctStatus.compareTo("1") == 0){
                 	procAcnoStatusCount1++;
                }
         		if (hAcnoAcctStatus.compareTo("2") == 0){
         			procAcnoStatusCount2++;
                }
         		procAcnoStatusCount12++;
            }
             if (hAcnoAcctStatus.compareTo("3") == 0 || hAcnoAcctStatus.compareTo("4") == 0){
            	 if (hAcnoAcctStatus.compareTo("3") == 0){
                    	procAcnoStatusCount3++;
                   }
            	 if (hAcnoAcctStatus.compareTo("4") == 0){
                 	procAcnoStatusCount3++;
                   }
              	procAcnoStatusCount34++;
             }            
              
//        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB001 proc = new ColB001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
