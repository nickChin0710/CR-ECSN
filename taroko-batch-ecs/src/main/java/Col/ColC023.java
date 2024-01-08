/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/01/03  V1.00.00    Ryan       program initial                                         * 
*  112/01/09  V1.00.01    Ryan       crd_corp ,inner join  改為 left join       * 
*  112/03/01  V1.00.02    Ryan       COL_CS_RPT有資料時先刪除再新增                                                * 
*  112/03/01  V1.00.03    Sunny      調整雙幣卡抓取的帳單總額及最低應繳金額欄位*
*  112/04/27  V1.00.04    Ryan       營業日hBusinessDate -1                     *
*  112/05/23  V1.00.05    Ryan       將InfS009計算後最後一筆協商狀態CPBDUE_CURR_TYPE的動作搬至ColC023提前運算執行 *
*  112/05/24  V1.00.06    Ryan       增加免列報註記判斷 *
*  112/07/06  V1.00.07    Ryan       調整DC_MIN_PAY_BAL > 0 AND COL_CS_INFO不存在才INSERT 
*                                    CLOSE_FLAG != Y AND DC_MIN_PAY_BAL <= 0 才update *
*  112/07/10  V1.00.08    Ryan       bug修正                                                                                                  *
*  112/07/11  V1.00.09    Ryan       修正currDcMinPayBal <= 0  continue                  *   
*  112/08/04  V1.00.10    Ryan       CPBDUE_TYPE => CPBDUE_CURR_TYPE                  *  
*  112/08/07  V1.00.11    Ryan       acno_flag = 1 才需要讀COL_COLLECT_FLAGX                  *  
******************************************************************************/

package Col;

import java.util.HashMap;

import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommString;

public class ColC023 extends AccessDAO {
    private final static boolean DEBUG = false; //debug用
    private String progname = "每日產生逾期名單報表檔   112/08/07 V1.00.11 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommCol comCol = null;
    CommDate comDate = new CommDate();
    String hBusinessDate = "";
   /********col_m0_out & col_cs_out**********/
    String outNum = "";
    String outPSeqno = "";
    Double outTtlAmtBal = Double.valueOf(0.0D);
    Double outStmtOverDueAmt = Double.valueOf(0.0D);
    
    /********act_acno**********/
    String acnoIdPSeqno = "";
    String acnoAcnoPSeqno = "";
    String acnoCorpPSeqno = "";
    String acnoAcctType = "";
    String acnoAcctStatus = "";
    Double acnoLineOfCreditAmt = Double.valueOf(0.0D);
    String acnoPaymentRate1 = "";
    int acnoIntRateMcode = 0;
    String acnoPayByStageFlag = "";
    String acnoNoDelinquentSDate = "";
    String acnoNoDelinquentEDate = "";
    String acnoNoCollectionSDate = "";
    String acnoNoCollectionEDate = "";
    String acnoNoSmsSDate = "";
    String acnoNoSmsEDate = "";
    String acnoFlag = "";
    
    /********act_acct_curr**********/
    String currCurrCode = "";
    String currAutopayAcctBank = "";
    String currAutopayAcctNo = "";
    Double currDcTtlAmt = Double.valueOf(0.0D);
    Double currDcMinPay = Double.valueOf(0.0D);
    Double currDcTtlAmtBal = Double.valueOf(0.0D);
    Double currDcMinPayBal = Double.valueOf(0.0D);
    
    /********crd_corp**********/
    String crdpCorpNo = "";
    String corpChiName = "";
    
    /********crd_idno**********/
    String idnoIdNo = "";
    String idnoChiName = "";
    String idnoCreditLevelNew = "";
    String idnoCreditLevelOld = "";
    String idnoBusinessCode = "";
    String idnoCompanyName = "";
    String idnoJobPosition = "";
    String idnoBirthday = "";
    String idnoEducation = "";
    String idnoServiceYear = "";
    Double idnoAnnualIncome = Double.valueOf(0.0D);
    String idnoHomeAreaCode1 = "";
    String idnoHomeTelNo1 = "";
    String idnoHomeTelExt1 = "";
    String idnoOfficeAreaCode1 = "";
    String idnoOfficeTelNo1 = "";
    String idnoOfficeTelExt1 = "";
    String idnoCellarPhone = "";
    
    /********col_cs_info**********/
    String infoCardNo = "";
    String infoRegBankNo = "";
    String infoIssueBankNo = "";
    String closeFlag = "";
    
    /********ptr_group_code**********/
    String ptrGroupCode = "";
    String ptrGroupName = "";
    
    
    String hDelayDay = "";
    String hCollectFlagx = "";
    String hNoDelinquentFlag = "";
    String hNoTelCollFlag = "";
    String hNoCollectionFlag = "";
    String hNoSmsFlag = "";
    String hRegBankName = "";
    String hRiskBankName = "";
    
    int totalCnt = 0;
    int updColCsInfoCnt = 0;
    
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
            comCol = new CommCol(getDBconnect(), getDBalias());
            
            hBusinessDate = comCol.getBusiDate(); 
            if (args.length == 1)
            	hBusinessDate = args[0];
            
            hBusinessDate = comDate.dateAdd(hBusinessDate, 0, 0, -1);
            
            showLogMessage("I", "", "本日營業前一日 : [" + hBusinessDate + "]");
                        
			if(selectColCsRptByDay()) {
				showLogMessage("I", "", String.format("刪除col_cs_rpt [%s] ..", hBusinessDate));
				deleteColCsRptbyDay();				
			}
			showLogMessage("I", "", "取得各項協商主檔的狀態....");
			selectCpbdueCurrType();
			selectColCpbdue();
//			selectColCollectFlagx();
            selectColM0Out();
            
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
    void selectColM0Out() throws Exception {
    	fetchExtend = "m0out.";
        sqlCmd = "select ROW_NUMBER() OVER(PARTITION BY p_seqno ORDER BY mod_time DESC) as col_num ,* from ( ";
        sqlCmd += "select p_seqno,ttl_amt_bal,stmt_over_due_amt,mod_time from col_m0_out ";
        sqlCmd += "union ";
        sqlCmd += "select p_seqno,ttl_amt_bal,stmt_over_due_amt,mod_time from col_cs_out) ";

        openCursor();
        while (fetchTable()) {
        	initialData();
        	totalCnt++;
        	outNum = getValue("m0out.col_num");
            outPSeqno = getValue("m0out.p_seqno");
// 20230301 sunny mark 此為台幣+外幣的台幣欄位，改從act_acct_curr取得
            outTtlAmtBal = getValueDouble("m0out.ttl_amt_bal");
            outStmtOverDueAmt = getValueDouble("m0out.stmt_over_due_amt");

            //非最大異動時間
            if (!outNum.equals("1"))
                continue;
            
            selectActAcno();
            
    		if ((totalCnt % 1000) == 0) {
        		showLogMessage("I","",String.format("Process w/ ROW = [%s][%s]", hBusinessDate,totalCnt));
    		}
    		
            commitDataBase();
        }
        showLogMessage("I", "", String.format("累計處理筆數[%d] ", totalCnt));
        closeCursor();
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
    	extendField = "actacno.";
        sqlCmd = "select a.id_p_seqno ";
        sqlCmd += ",a.acno_p_seqno ";
        sqlCmd += ",a.corp_p_seqno ";
        sqlCmd += ",a.acct_type ";
        sqlCmd += ",a.acct_status ";
        sqlCmd += ",a.line_of_credit_amt ";
        sqlCmd += ",a.payment_rate1 ";
        sqlCmd += ",a.int_rate_mcode ";
        sqlCmd += ",a.pay_by_stage_flag ";
        sqlCmd += ",a.no_delinquent_s_date ";
        sqlCmd += ",a.no_delinquent_e_date ";
        sqlCmd += ",a.no_collection_s_date ";
        sqlCmd += ",a.no_collection_e_date ";
        sqlCmd += ",a.no_sms_s_date ";
        sqlCmd += ",a.no_sms_e_date ";
        sqlCmd += ",b.corp_no ";
        sqlCmd += ",a.acno_flag ";
        sqlCmd += ",b.chi_name as corp_chi_name ";
        sqlCmd += ",c.id_no ";
        sqlCmd += ",c.chi_name ";
        sqlCmd += ",c.credit_level_new ";
        sqlCmd += ",c.credit_level_old ";
        sqlCmd += ",c.business_code ";
        sqlCmd += ",c.company_name ";
        sqlCmd += ",c.job_position ";
        sqlCmd += ",c.birthday ";
        sqlCmd += ",c.education ";
        sqlCmd += ",c.service_year ";
        sqlCmd += ",c.annual_income ";
        sqlCmd += ",c.home_area_code1 ";
        sqlCmd += ",c.home_tel_no1 ";
        sqlCmd += ",c.home_tel_ext1 ";
        sqlCmd += ",c.office_area_code1 ";
        sqlCmd += ",c.office_tel_no1 ";
        sqlCmd += ",c.office_tel_ext1 ";
        sqlCmd += ",c.cellar_phone ";
        sqlCmd += "from act_acno a left join crd_corp b on a.corp_p_seqno = b.corp_p_seqno ";
        sqlCmd += "inner join crd_idno c on a.id_p_seqno = c.id_p_seqno ";
        sqlCmd += "where a.acno_p_seqno = ? ";
        setString(1,outPSeqno);
        int recordCnt = selectTable();
        if(recordCnt > 0) {
        	acnoIdPSeqno = getValue("actacno.id_p_seqno");
        	acnoAcnoPSeqno = getValue("actacno.acno_p_seqno");
        	acnoCorpPSeqno = getValue("actacno.corp_p_seqno");
        	acnoAcctType = getValue("actacno.acct_type");
        	acnoAcctStatus = getValue("actacno.acct_status");
        	acnoLineOfCreditAmt = getValueDouble("actacno.line_of_credit_amt");
        	acnoPaymentRate1 = getValue("actacno.payment_rate1");
        	acnoIntRateMcode = getValueInt("actacno.int_rate_mcode");
//        	acnoPayByStageFlag = getValue("pay_by_stage_flag");
        	acnoNoDelinquentSDate = getValue("actacno.no_delinquent_s_date");
        	acnoNoDelinquentEDate = getValue("actacno.no_delinquent_e_date");
        	acnoNoCollectionSDate = getValue("actacno.no_collection_s_date");
        	acnoNoCollectionEDate = getValue("actacno.no_collection_e_date");
        	acnoNoSmsSDate = getValue("actacno.no_sms_s_date");
        	acnoNoSmsEDate = getValue("actacno.no_sms_e_date");
        	crdpCorpNo = getValue("actacno.corp_no");
        	corpChiName = getValue("actacno.corp_chi_name");
        	idnoIdNo = getValue("actacno.id_no");
        	idnoChiName = getValue("actacno.chi_name");
        	idnoCreditLevelNew = getValue("actacno.credit_level_new");
        	idnoCreditLevelOld = getValue("actacno.credit_level_old");
        	idnoBusinessCode = getValue("actacno.business_code");
        	idnoCompanyName = getValue("actacno.company_name");
        	idnoJobPosition = getValue("actacno.job_position");
        	idnoBirthday = getValue("actacno.birthday");
        	idnoEducation = getValue("actacno.education");
        	idnoServiceYear = getValue("actacno.service_year");
        	idnoAnnualIncome = getValueDouble("actacno.annual_income");
        	idnoHomeAreaCode1 = getValue("actacno.home_area_code1");
        	idnoHomeTelNo1 = getValue("actacno.home_tel_no1");
        	idnoHomeTelExt1 = getValue("actacno.home_tel_ext1");
        	idnoOfficeAreaCode1 = getValue("actacno.office_area_code1");
        	idnoOfficeTelNo1 = getValue("actacno.office_tel_no1");
        	idnoOfficeTelExt1 = getValue("actacno.office_tel_ext1");
        	idnoCellarPhone = getValue("actacno.cellar_phone");
        	acnoFlag = getValue("actacno.acno_flag");
        	procData();
        	getCpbdueCurrType();
        	getCollectFlagx();
        	selectActAcctCurr();
        }
        
    }
    
    void selectActAcctCurr() throws Exception {
    	extendField = "actcurr.";
    	sqlCmd = "select curr_code,autopay_acct_bank,autopay_acct_no,dc_ttl_amt,dc_min_pay,dc_ttl_amt_bal,dc_min_pay_bal from act_acct_curr where p_seqno = ? ";
    	setString(1,outPSeqno);
    	int recordCnt = selectTable();
    	for(int i=0; i<recordCnt ;i++) {
    		currCurrCode = getValue("actcurr.curr_code",i);
    		currAutopayAcctBank = getValue("actcurr.autopay_acct_bank",i);
    		currAutopayAcctNo = getValue("actcurr.autopay_acct_no",i);
    		currDcTtlAmt = getValueDouble("actcurr.dc_ttl_amt",i); 		//20230301 sunny add,取外幣帳單總應繳金額
    		currDcMinPay = getValueDouble("actcurr.dc_min_pay",i); 		//20230301 sunny add,取外幣帳單最低金額
    		currDcTtlAmtBal = getValueDouble("actcurr.dc_ttl_amt_bal",i);
    		currDcMinPayBal = getValueDouble("actcurr.dc_min_pay_bal",i);
    		
    		//當若DC_MIN_PAY_BAL > 0 AND COL_CS_INFO取不到代表卡號資料(不存在)，則由程式計算現況，以debt現行本金欠款最大為主。
			if (selectColCsInfo() == 1 && currDcMinPayBal.doubleValue() > 0) {
				String[] debtArray = comCol.selectMaxCardDebtAmt(outPSeqno);
				infoCardNo = debtArray[1];
				infoRegBankNo = comCol.selectCardRiskBankNo(infoCardNo);
				infoIssueBankNo = comCol.selectCardIssueRegBankNo(infoCardNo);
				showLogMessage("I","",String.format("COL_CS_INFO NOT FOUND ,p_seqno = [%s] ,curr_code = [%s]",outPSeqno,currCurrCode));
				showLogMessage("I","",String.format("取得歸戶下當下現欠金額最大的卡號  = [%s]", infoCardNo));
				showLogMessage("I","",String.format("取得代表當前卡號所屬之受理行 = [%s]", infoRegBankNo));
				showLogMessage("I","",String.format("取得核卡分行  = [%s]", infoIssueBankNo));
				insertColCsInfo();
			}
			
	   		//檢視CLOSE_FLAG != Y AND DC_MIN_PAY_BAL<=0最低應繳現欠餘額為0 時，則ColCsInfoCloseFlag，將結案註記更新為Y並且更新結案日期，且不需要insert COL_CS_RPT。
    		if(currDcMinPayBal.doubleValue() <= 0) {
    			if(!"Y".equals(closeFlag))
    				updateColCsInfoCloseFlag();
    			continue;
    		}
			
			selectPtrGroupCode();
 
			hRegBankName = comCol.getBranchChiName(infoRegBankNo);
			hRiskBankName = comCol.getBranchChiName(infoIssueBankNo);
//			if(selectColCsRpt()) {
//				deleteColCsRpt();
//				showLogMessage("I","",String.format("deleteColCsRpt ,p_seqno = [%s] ,curr_code = [%s]",outPSeqno,currCurrCode));
//			}
    		insertColCsRpt();    		
    	}
    }

    /***********************************************************************/
    int selectColCsInfo() throws Exception {
    	extendField = "info.";
    	sqlCmd = "select card_no,reg_bank_no,issue_bank_no,close_flag from col_cs_info where p_seqno = ? and curr_code = ? ";
    	setString(1,outPSeqno);
    	setString(2,currCurrCode);
    	int recordCnt = selectTable();
    	closeFlag = "";
    	if(recordCnt > 0) {
    		infoCardNo =  getValue("info.card_no");
    		infoRegBankNo =  getValue("info.reg_bank_no");
    		infoIssueBankNo =  getValue("info.issue_bank_no");
    		closeFlag = getValue("info.close_flag");
    		return 0; 
    	}
    	return 1;
    }
    
    void getCollectFlagx() throws Exception {
    	if("1".equals(acnoFlag))
    		selectColCollectFlagx();
    	
    	setValue("cpbdue.ID_CORP_NO","01".equals(acnoAcctType)?idnoIdNo:crdpCorpNo);
		getLoadData("cpbdue.ID_CORP_NO");
		
		long sumCpbdueTotalPayAmt = getValueLong("cpbdue.SUM_CPBDUE_TOTAL_PAYAMT");
		long sumCpbdueDueCardAmt = getValueLong("cpbdue.SUM_CPBDUE_DUE_CARD_AMT");
    		if(sumCpbdueDueCardAmt > 0) {
    			if((sumCpbdueTotalPayAmt/sumCpbdueDueCardAmt)>=3) {
    				hCollectFlagx = "Y";
    			}
    		}
    
    }
    
    /**
              * 讀取COL_COLLECT_FLAGX 此TABLE，只要ID_NO存在，就算將免列報設定為Y
     * @throws Exception 
     */
    void selectColCollectFlagx() throws Exception{
    	extendField = "flagx.";
    	sqlCmd = " select count(*) as FLAGX_CNT ";
    	sqlCmd += " from COL_COLLECT_FLAGX a LEFT JOIN CRD_CHG_ID c ON a.ID_NO = c.ID_NO ";
    	sqlCmd += " where a.ID_NO = ? OR c.OLD_ID_NO = ? ";
    	setString(1,idnoIdNo);
    	setString(2,idnoIdNo);
    	selectTable();
    	if(getValueInt("flagx.FLAGX_CNT")>0) {
    		hCollectFlagx = "Y";
    	}
    }
    
    /***
     * 讀取COL_CPBDUE檔案，協商狀態為3，即協商成功的資料
       判斷累計繳款金額(CPBDUE_TOTAL_PAYAMT)除以每期信用卡繳款金額(CPBDUE_DUE_CARD_AMT)
       若取得結果>=3，則令col_cs_rpt.COLLECT_FLAGX為Y
     * @throws Exception
     */
    void selectColCpbdue() throws Exception{
    	extendField = "cpbdue.";
    	daoTable    = "COL_CPBDUE";
    	sqlCmd = "SELECT ID_CORP_NO,SUM(CPBDUE_TOTAL_PAYAMT) as SUM_CPBDUE_TOTAL_PAYAMT, SUM(CPBDUE_DUE_CARD_AMT) as SUM_CPBDUE_DUE_CARD_AMT ";
    	sqlCmd += " FROM COL_CPBDUE WHERE CPBDUE_CURR_TYPE = '3' GROUP BY ID_CORP_NO ";
    	int n = loadTable();
  	    setLoadData("cpbdue.ID_CORP_NO");
    	showLogMessage("I", "", "已取得COL_CPBDUE = [" + n +"]筆");
    }
    
    /***********************************************************************/
    int selectPtrGroupCode() throws Exception {
    	extendField = "pgc.";
    	sqlCmd = "SELECT a.group_code,a.group_name from ptr_group_code a,crd_card b where a.group_code = b.group_code and b.card_no = ? ";
    	setString(1,infoCardNo);
    	int recordCnt = selectTable();
    	if(recordCnt > 0) {
    		ptrGroupCode =  getValue("pgc.group_code");
    		ptrGroupName =  getValue("pgc.group_name");
    		return 0; 
    	}
    	return 1;
    }
    
    /***********************************************************************/
    boolean selectColCsRptByDay() throws Exception {
    	extendField = "rptday.";
    	sqlCmd = "select count(*) as rpt_cnt from col_cs_rpt where create_date = ?";
    	setString(1,hBusinessDate);
    	selectTable();
    	int rptCnt = getValueInt("rptday.rpt_cnt");
    	if( rptCnt > 0) {
    		return true; 
    	}
    	return false;
    }
    
    /***********************************************************************/
    void deleteColCsRptbyDay() throws Exception {
    	daoTable = "col_cs_rpt";
    	whereStr = " where create_date = ? ";
    	setString(1,hBusinessDate);
    	deleteTable();
    }
    /***********************************************************************/
    boolean selectColCsRpt() throws Exception {
    	extendField = "rptcnt.";
    	sqlCmd = "select count(*) as rpt_cnt from col_cs_rpt where create_date = ? and acno_p_seqno = ? and curr_code = ? ";
    	setString(1,hBusinessDate);
    	setString(2,acnoAcnoPSeqno);
    	setString(3,currCurrCode);
    	selectTable();
    	int rptCnt = getValueInt("rptcnt.rpt_cnt");
    	if( rptCnt > 0) {
    		return true; 
    	}
    	return false;
    }
    
	/***
	 * 查詢各項協商主檔的狀態，取最後一筆異動日期
	 * @param infS009Data
	 * @throws Exception 
	 */
	void selectCpbdueCurrType() throws Exception {
		daoTable = "CPBDUE_CURR_TYPE";
		extendField = "currtype.";
		sqlCmd = "SELECT * FROM ( ";
		sqlCmd += " select CPBDUE_UPD_DTE,ID_CORP_NO,CPBDUE_ID_P_SEQNO ";
		sqlCmd += " ,CASE WHEN cpbdue_type<>'' AND CPBDUE_CURR_TYPE<>'' AND CPBDUE_CURR_TYPE<>'0' ";
		sqlCmd += " THEN decode(cpbdue_type,'1','1','2','5','3','7','')||decode(CPBDUE_CURR_TYPE,'0','',CPBDUE_CURR_TYPE) ";
		sqlCmd += " ELSE '' END AS CPBDUE_CURR_TYPE ";
		sqlCmd += " from col_cpbdue ";
		sqlCmd += " UNION ";
		sqlCmd += " SELECT a.APPLY_DATE,b.id_no,a.ID_P_SEQNO,'2'||liac_status AS liac_status ";
		sqlCmd += " FROM col_liac_nego a,crd_idno b ";
		sqlCmd += " WHERE a.ID_P_SEQNO=b.ID_P_SEQNO ";
		sqlCmd += " UNION ";
		sqlCmd += " SELECT STATUS_DATE,ID_NO,ID_P_SEQNO,LIAD_TYPE||decode(LIAD_STATUS,'A','1','B','2','C','3','D','4','E','5','F','6','G','7','H','8',LIAD_STATUS) AS LIAD_STATUS FROM ( ";
		sqlCmd += " SELECT ROW_NUMBER() OVER(PARTITION BY A.ID_P_SEQNO ORDER BY A.ID_P_SEQNO,A.STATUS_DATE DESC) AS ROWID, ";
		sqlCmd += " B.ID_NO,A.ID_P_SEQNO,A.LIAD_TYPE,A.LIAD_STATUS,A.STATUS_DATE ";
		sqlCmd += " FROM COL_LIAD_RENEWLIQUI A,CRD_IDNO B ";
		sqlCmd += " WHERE A.ID_P_SEQNO =B.ID_P_SEQNO ";
		sqlCmd += " ) WHERE ROWID='1' ";
		sqlCmd += " ) ";
//				+ "WHERE ID_CORP_NO = ? ";
		sqlCmd += " ORDER BY DECODE(LEFT(CPBDUE_CURR_TYPE,1),'7',1,2) ,CPBDUE_UPD_DTE DESC ";
//		setString(1,"01".equals(acnoAcctType)?idnoIdNo:crdpCorpNo);
    	int n = loadTable();
  	    setLoadData("currtype.ID_CORP_NO");
//		if(selectCnt > 0)
//			acnoPayByStageFlag = getValue("CPBDUE_CURR_TYPE");
		showLogMessage("I", "", "已取得[" + n +"]筆");
	}
	
	/**
	 * @throws Exception *********************************************************************/
	void getCpbdueCurrType() throws Exception {
    	setValue("currtype.ID_CORP_NO","01".equals(acnoAcctType)?idnoIdNo:crdpCorpNo);
		getLoadData("currtype.ID_CORP_NO");
		
		acnoPayByStageFlag = getValue("currtype.CPBDUE_CURR_TYPE");
	}
    
    /***********************************************************************/
    void deleteColCsRpt() throws Exception {
    	daoTable = "col_cs_rpt";
    	whereStr = " where create_date = ? and acno_p_seqno = ? and curr_code = ? ";
    	setString(1,hBusinessDate);
    	setString(2,acnoAcnoPSeqno);
    	setString(3,currCurrCode);
    	deleteTable();
    }

    /***********************************************************************/
    void insertColCsRpt() throws Exception {
    	extendField = "addrpt.";
        setValue("addrpt.CREATE_DATE", hBusinessDate);
        setValue("addrpt.ID_P_SEQNO", acnoIdPSeqno);
        setValue("addrpt.ACNO_P_SEQNO", acnoAcnoPSeqno);
        setValue("addrpt.CORP_P_SEQNO", acnoCorpPSeqno);
        setValue("addrpt.CORP_NO", crdpCorpNo);
        setValue("addrpt.CORP_CHI_NAME", corpChiName);
        setValue("addrpt.ID_NO", idnoIdNo);
        setValue("addrpt.CHI_NAME", idnoChiName);
        setValue("addrpt.CREDIT_LEVEL_NEW", idnoCreditLevelNew);
        setValue("addrpt.CREDIT_LEVEL_OLD", idnoCreditLevelOld);
        setValue("addrpt.BUSINESS_CODE", idnoBusinessCode);
        setValue("addrpt.COMPANY_NAME", idnoCompanyName);
        setValue("addrpt.JOB_POSITION", idnoJobPosition);
        setValue("addrpt.BIRTHDAY", idnoBirthday);
        setValue("addrpt.EDUCATION", idnoEducation);
        setValue("addrpt.SERVICE_YEAR", idnoServiceYear);
        setValueDouble("addrpt.ANNUAL_INCOME", idnoAnnualIncome);
        setValue("addrpt.HOME_AREA_CODE1", idnoHomeAreaCode1);
        setValue("addrpt.HOME_TEL_NO1", idnoHomeTelNo1);
        setValue("addrpt.HOME_TEL_EXT1", idnoHomeTelExt1);
        setValue("addrpt.OFFICE_AREA_CODE1", idnoOfficeAreaCode1);
        setValue("addrpt.OFFICE_TEL_NO1", idnoOfficeTelNo1);
        setValue("addrpt.OFFICE_TEL_EXT1", idnoOfficeTelExt1);
        setValue("addrpt.CELLAR_PHONE", idnoCellarPhone);
        setValue("addrpt.ACCT_TYPE", acnoAcctType);
        setValue("addrpt.ACCT_STATUS", acnoAcctStatus);
        setValueDouble("addrpt.LINE_OF_CREDIT_AMT", acnoLineOfCreditAmt);
        setValue("addrpt.PAYMENT_RATE1", acnoPaymentRate1);
        setValue("addrpt.INT_RATE_MCODE", acnoIntRateMcode>999?"999":acnoIntRateMcode<0?"0":new CommString().int2Str(acnoIntRateMcode));
        setValue("addrpt.DELAY_DAY", hDelayDay);
        setValue("addrpt.PAY_BY_STAGE_FLAG", acnoPayByStageFlag);
        setValue("addrpt.COLLECT_FLAGX", hCollectFlagx);
        setValue("addrpt.NO_DELINQUENT_FLAG", hNoDelinquentFlag);
        setValue("addrpt.NO_TEL_COLL_FLAG", hNoTelCollFlag);
        setValue("addrpt.NO_COLLECTION_FLAG", hNoCollectionFlag);
        setValue("addrpt.NO_SMS_FLAG", hNoSmsFlag);
        setValue("addrpt.CURR_CODE", currCurrCode);
        setValue("addrpt.AUTOPAY_ACCT_BANK", currAutopayAcctBank);
        setValue("addrpt.AUTOPAY_ACCT_NO", currAutopayAcctNo);
        setValueDouble("addrpt.TTL_AMT", currDcTtlAmt);           //外幣帳單總應繳金額
        setValueDouble("addrpt.STMT_OVER_DUE_AMT", currDcMinPay); //外幣帳單最低金額
        setValueDouble("addrpt.DC_TTL_AMT_BAL", currDcTtlAmtBal);
        setValueDouble("addrpt.DC_MIN_PAY_BAL", currDcMinPayBal);
        setValue("addrpt.CARD_NO", infoCardNo);
        setValue("addrpt.GROUP_CODE", ptrGroupCode);
        setValue("addrpt.GROUP_NAME", ptrGroupName);
        setValue("addrpt.REG_BANK_NO", infoRegBankNo);
        setValue("addrpt.REG_BANK_NAME", hRegBankName);
        setValue("addrpt.RISK_BANK_NO", infoIssueBankNo);
        setValue("addrpt.RISK_BANK_NAME", hRiskBankName);
        setValue("addrpt.mod_time", sysDate + sysTime);
        setValue("addrpt.mod_pgm", javaProgram);
        daoTable = "col_cs_rpt";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn(String.format("insert_col_cs_rpt duplicate! ,CREATE_DATE=[%s] ,ACNO_P_SEQNO=[%s] ,CURR_CODE=[%s]",hBusinessDate, acnoAcnoPSeqno,currCurrCode), "", "");
        }
    }
    
	void insertColCsInfo() throws Exception {
		extendField = "addinf.";
		setValue("addinf.p_seqno", outPSeqno);
		setValue("addinf.corp_p_seqno", !acnoAcctType.equals("01") ? acnoCorpPSeqno : "");
		setValue("addinf.id_p_seqno", acnoIdPSeqno);
		setValue("addinf.curr_code", currCurrCode);
		setValue("addinf.card_no", infoCardNo);
		setValue("addinf.reg_bank_no", infoRegBankNo);
		setValue("addinf.issue_bank_no", infoIssueBankNo);
		setValue("addinf.crt_date", sysDate);
		setValue("addinf.crt_time", sysTime);
		setValue("addinf.mod_time", sysDate + sysTime);
		setValue("addinf.mod_pgm", javaProgram);
		daoTable = "col_cs_info";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn(String.format("insertColCsInfo duplicate! ,p_seqno = [%s] ,curr_code = [%s]", outPSeqno,currCurrCode), "", "");
		}
	}
    
    void updateColCsInfoCloseFlag() throws Exception {
        daoTable = "col_cs_info";
        updateSQL = "CLOSE_FLAG = ?, ";
        updateSQL += " CLOSE_DATE = ?, ";
        updateSQL += " mod_time = sysdate, ";
        updateSQL += " mod_pgm = ? ";
        whereStr = "where p_seqno = ? and curr_code = ? ";
        setString(1, "Y");
        setString(2, sysDate);
        setString(3, javaProgram);
        setString(4, outPSeqno);
        setString(5, currCurrCode);
        updateTable();        
        if (notFound.equals("Y")) {
        	
        	updColCsInfoCnt++;        	
        	//最多只印出10筆
        	if (updColCsInfoCnt<=10)
        	showLogMessage("I","",String.format("["+updColCsInfoCnt+"]update ColCsInfo-CloseFlag NOT FOUND,acct_type[%s],id_no[%s],p_seqno[%s],curr_code[%s]",acnoAcctType,idnoIdNo,outPSeqno,currCurrCode));
        	
        	if (updColCsInfoCnt==11)
             showLogMessage("I","","update ColCsInfo-CloseFlag NOT FOUND,超過10筆... ");
        }
            if(DEBUG)
        	showLogMessage("I","",String.format("update ColCsInfo-CloseFlag ,acct_type[%s],p_seqno[%s] ,curr_code[%s] ,id_no[%s]",outPSeqno,currCurrCode,idnoIdNo));
    }
    
    void procData() throws Exception {
//    	 int delayDay = comCol.getDelayDay(outPSeqno);
//    	 hDelayDay = comCol.getDelayDayRange(delayDay);
    	 hCollectFlagx = "N";
    	 hNoDelinquentFlag = checkDate(acnoNoDelinquentSDate,acnoNoDelinquentEDate);
    	 hNoTelCollFlag = checkDate(acnoNoDelinquentSDate,acnoNoDelinquentEDate);
    	 hNoCollectionFlag = checkDate(acnoNoCollectionSDate,acnoNoCollectionEDate);
    	 hNoSmsFlag = checkDate(acnoNoSmsSDate,acnoNoSmsEDate);
    }
    
    String checkDate(String dateS ,String dateE) {
    	if(hBusinessDate.compareTo(dateS)>=0 
    			&& hBusinessDate.compareTo(dateE)<=0) {
    		return "Y";
    	}
    	return "N";
    }
    
    void initialData() {
		outNum = "";
		outPSeqno = "";
		outTtlAmtBal = Double.valueOf(0.0D);
		outStmtOverDueAmt = Double.valueOf(0.0D);

		/******** act_acno **********/
		acnoIdPSeqno = "";
		acnoAcnoPSeqno = "";
		acnoCorpPSeqno = "";
		acnoAcctType = "";
		acnoAcctStatus = "";
		acnoLineOfCreditAmt = Double.valueOf(0.0D);
		acnoPaymentRate1 = "";
		acnoIntRateMcode = 0;
		acnoPayByStageFlag = "";
		acnoNoDelinquentSDate = "";
		acnoNoDelinquentEDate = "";
		acnoNoCollectionSDate = "";
		acnoNoCollectionEDate = "";
		acnoNoSmsSDate = "";
		acnoNoSmsEDate = "";

		/******** act_acct_curr **********/
		currCurrCode = "";
		currAutopayAcctBank = "";
		currAutopayAcctNo = "";
		currDcTtlAmt = Double.valueOf(0.0D);
		currDcMinPay = Double.valueOf(0.0D);
		currDcTtlAmtBal = Double.valueOf(0.0D);
		currDcMinPayBal = Double.valueOf(0.0D);

		/******** crd_corp **********/
		crdpCorpNo = "";
		corpChiName = "";

		/******** crd_idno **********/
		idnoIdNo = "";
		idnoChiName = "";
		idnoCreditLevelNew = "";
		idnoCreditLevelOld = "";
		idnoBusinessCode = "";
		idnoCompanyName = "";
		idnoJobPosition = "";
		idnoBirthday = "";
		idnoEducation = "";
		idnoServiceYear = "";
		idnoAnnualIncome = Double.valueOf(0.0D);
		idnoHomeAreaCode1 = "";
		idnoHomeTelNo1 = "";
		idnoHomeTelExt1 = "";
		idnoOfficeAreaCode1 = "";
		idnoOfficeTelNo1 = "";
		idnoOfficeTelExt1 = "";
		idnoCellarPhone = "";

		/******** col_cs_info **********/
		infoCardNo = "";
		infoRegBankNo = "";
		infoIssueBankNo = "";

		/******** ptr_group_code **********/
		ptrGroupCode = "";
		ptrGroupName = "";

		hDelayDay = "";
		hCollectFlagx = "";
		hNoDelinquentFlag = "";
		hNoTelCollFlag = "";
		hNoCollectionFlag = "";
		hNoSmsFlag = "";
		hRegBankName = "";
		hRiskBankName = "";
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC023 proc = new ColC023();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
