/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/02/01  V1.00.00    Ryan       program initial                          * 
*  112/02/16  V1.00.01    Sunny      insertColBadJrnlExt.add tran_class='B'   * 
*  112/08/21  V1.00.02    Ryan       insertColBadDebtExthst 先排除 REG_BANK_NO欄位  *
*  112/09/13  V1.00.03    Sunny      insertColBadDebtExthst 加回REG_BANK_NO欄位  *  
******************************************************************************/

package Col;

import java.util.HashMap;

import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

public class ColB010 extends AccessDAO {
    private String progname = "轉催外帳主檔處理程式   112/09/13  V1.00.03 ";
    CommFunction comm = new CommFunction();
    CommString comStr = new CommString();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommCol comCol = null;
    String hBusinessDate = "";
   /********col_bad_debt**********/
    String debtIdPSeqno = "";
    String debtIdNo = "";
    String debtStmtCycle = "";
    String debtPSeqno = "";
    String debtCorpPSeqno = "";
    
    /********col_bad_debt_ext**********/
    String debtExtRowid = "";
    
    /********col_bad_detail_ext**********/
    String detailExtRowid = "";
    double detailExtSumEndBal = 0.0;
    double detailExtSumActJrnlBal = 0.0;
    
    /********ptr_actgeneral_n**********/
    double nRevolvingInterest2 = 0.0;
    
    /********crd_corp**********/
    String crdCorpPSeqno = "";
    
    /********crd_idno**********/
    String crdIdPSeqno = "";
    
    /********col_cs_info**********/
    String infoIssueBankNo = "";
    
    /********col_bad_detail**********/
    String detailPSeqno = "";
    String detailAcctCode = "";
    double detailEndBal = 0.0;
    
    String extAcctType = "";
    boolean chkParmIdNo = false;
    boolean chkParmCorpNo = false;
    int idTotalCnt = 0;
    int corpTotalCnt = 0;
    int jrnlEnqSeqno = 0;
    
    String[] acctCodes = { "SF", "CC" ,"CI" ,"AI" ,"CB"};
    HashMap<String,Integer> detailExtMap = null;
    String debtIdPSeqnoTmp = "";
    String detailPSeqnoTmp = "";

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
            
            if (args.length > 2) {
                comc.errExit("Usage : ColB010 [trans_date][id_no/corp_no]", "1.trans_date : yyyymmdd 2.id(10碼) or corp_no(8碼或11碼)");
            }
         
            hBusinessDate = comCol.getBusiDate(); 
            if (args.length >= 1)
            	if(args[0].length()==8)
            		hBusinessDate = args[0];
            if (args.length == 2) {
            	if(args[1].length()==10)
            		getIdPSeqno(args[1]);
            	if(args[1].length()==8 || args[1].length()==11)
            		getCorpPSeqno(args[1]);            	   
            	showLogMessage("I", "", String.format("處理指定id_no/corp_no[%s]...", args[1].toString()));
            }
            
    		showLogMessage("I", "", String.format("處理日期[%s]...", hBusinessDate));
    		
            selectColBadDebtPerson();
            selectColBadDebtCompanyBase();
            
            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "執行結束");
            showLogMessage("I", "", String.format("處理個人累計處理筆數[%d] ", idTotalCnt));
            showLogMessage("I", "", String.format("處理公司累計處理筆數[%d] ", corpTotalCnt));
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /************************處理個人—id_p_seqno、身份證字號(10碼)***********************************************/
    void selectColBadDebtPerson() throws Exception {
    	sqlCmd = " select id_p_seqno,id_no,stmt_cycle,p_seqno from col_bad_debt a where a.trans_type='3' AND a.ACCT_TYPE='01' ";
    	sqlCmd += " AND NOT EXISTS (SELECT 1 FROM col_bad_debt b WHERE a.P_SEQNO = b.P_SEQNO AND ";
    	sqlCmd += " TRANS_TYPE='4' AND A.TRANS_DATE<=B.TRANS_DATE) AND a.TRANS_DATE = ? and a.src_amt>0 ";
    	setString(1,hBusinessDate);
     	if(chkParmIdNo) {
    		sqlCmd += " and a.id_p_seqno = ? ";
    		setString(2,crdIdPSeqno);    		
    	}
        openCursor();
        while (fetchTable()) {
        	idTotalCnt++;
        	initialData();
        	debtIdPSeqno = getValue("id_p_seqno");
        	debtIdNo = getValue("id_no");
        	debtStmtCycle = getValue("stmt_cycle");
        	debtPSeqno = getValue("p_seqno");
        	procIdPSeqno();
        	
        	showLogMessage("I", "", String.format("處理id_p_seqno[%s]...", debtIdPSeqno));
        	showLogMessage("I", "", String.format("處理id_no[%s]...", debtIdNo));
        
            commitDataBase();
        }
        closeCursor();
    }
    
    /**********************處理公司—corp_p_seqno，統一編號(8碼)或(11碼)*************************************************/
    void selectColBadDebtCompanyBase() throws Exception {
    	sqlCmd = " Select DISTINCT corp_p_seqno,stmt_cycle from col_bad_debt a ";
    	sqlCmd += " where a.trans_type='3' AND a.ACCT_TYPE='03' ";
    	sqlCmd += " AND NOT EXISTS (SELECT 1 FROM col_bad_debt b WHERE a.P_SEQNO = b.P_SEQNO  ";
    	sqlCmd += "AND TRANS_TYPE='4' AND A.TRANS_DATE<=B.TRANS_DATE) and a.src_amt>0 ";
    	sqlCmd += " AND a.TRANS_DATE = ? ";
    	setString(1,hBusinessDate);
     	if(chkParmCorpNo) {
    		sqlCmd += " and a.corp_p_seqno = ? ";
    		setString(2,crdCorpPSeqno);
    		showLogMessage("I", "", String.format("處理corp_p_seqno[%s]...", crdCorpPSeqno));
    	}
        openCursor();
        while (fetchTable()) {
        	corpTotalCnt++;
        	initialData();
        	debtCorpPSeqno = getValue("corp_p_seqno");
        	debtStmtCycle = getValue("stmt_cycle");
        	procCorpPSeqno();
        
            commitDataBase();
        }
        closeCursor();
    }
    
    /***********************************************************************/
    void procIdPSeqno() throws Exception {
    	extAcctType = "01";
    	//如果col_bad_debt_ext.count(*)>0表示資料已存在，則先將該筆資料搬至歷史檔col_bad_debt_exthst後，再刪除col_bad_debt_ext原有那筆資料，
    	//最後再將本次要處理的資料insert至col_bad_debt_ext。
    	int debtExtResult =  selectColBadDebtExt(debtIdPSeqno);
    	if(debtExtResult == 0) {
    	 	showLogMessage("I", "", String.format("select col_bad_debt_ext已存在舊資料,id_p_seqno[%s]...", debtIdPSeqno));
    		insertColBadDebtExthst();
    		deleteColBadDebtExt();
    	}
    	selectPtrActgeneralN(extAcctType);
		selectColCsInfo();
		insertColBadDebtExt(debtIdPSeqno);
		
		//如果col_bad_detail_ext.count(*)>0表示資料已存在，則先將該筆資料搬至歷史檔col_bad_detail_exthst後，再刪除col_bad_detail_ext原有的資料，
		//最後再將本次要處理的資料insert至col_bad_debt_ext。
		String sqlWhere = " where id_p_seqno = ? and acct_type='01' ";
		int detailExtResult = selectColBadDetailExt(debtIdPSeqno,sqlWhere);
		if(detailExtResult == 0) {
			insertColBadDetailExthst(debtIdPSeqno,sqlWhere);
    		deleteColBadDetailExt(debtIdPSeqno,sqlWhere);
		}
		selectColBadDetail();
    }
    
    /***********************************************************************/
    void procCorpPSeqno() throws Exception {
    	extAcctType = "03";
    	//如果col_bad_debt_ext.count(*)>0表示資料已存在，則先將該筆資料搬至歷史檔col_bad_debt_exthst後，再刪除col_bad_debt_ext原有那筆資料，
    	//最後再將本次要處理的資料insert至col_bad_debt_ext。
    	int debtExtResult =  selectColBadDebtExt(debtCorpPSeqno);
    	if(debtExtResult == 0) {
    	 	showLogMessage("I", "", String.format("select col_bad_debt_ext已存在舊資料,corp_p_seqno[%s]...", debtCorpPSeqno));
    		insertColBadDebtExthst();
    		deleteColBadDebtExt();
    	}
    	selectPtrActgeneralN(extAcctType);
		selectColCsInfo2();
		insertColBadDebtExt(debtCorpPSeqno);
		
		//如果col_bad_detail_ext.count(*)>0表示資料已存在，則先將該筆資料搬至歷史檔col_bad_detail_exthst後，再刪除col_bad_detail_ext原有的資料，
		//最後再將本次要處理的資料insert至col_bad_debt_ext。
		String sqlWhere = " where corp_p_seqno = ? and acct_type='03' ";
		int detailExtResult = selectColBadDetailExt(debtCorpPSeqno,sqlWhere);
		if(detailExtResult == 0) {
			insertColBadDetailExthst(debtCorpPSeqno,sqlWhere);
    		deleteColBadDetailExt(debtCorpPSeqno,sqlWhere);
		}
		selectColBadDetail2();
    }
    
    /***********************************************************************/
    void getIdPSeqno(String idNo) throws Exception {
    	sqlCmd = "select id_p_seqno from crd_idno where id_no = ? ";
    	setString(1,idNo);
      	extendField = "crd_idno.";
    	int recordCnt = selectTable();
    	if(recordCnt>0) {
    		crdIdPSeqno = getValue("crd_idno.id_p_seqno");
    		chkParmIdNo = true;
    		showLogMessage("I", "", String.format("處理id_no/corp_no[%s]...", idNo));
    		showLogMessage("I", "", String.format("取得id_p_seqno[%s]...", crdIdPSeqno));
    	}else {
    		comc.errExit("select crd_idno notfound ,參數2 身分證號不存在", "");
    	}
    
    }
    
    /***********************************************************************/
    void getCorpPSeqno(String corpNo) throws Exception {
    	sqlCmd = "select corp_p_seqno from crd_corp where corp_no = ? ";
    	setString(1,corpNo);
    	int recordCnt = selectTable();
    	if(recordCnt>0) {
    		crdCorpPSeqno = getValue("corp_p_seqno");
    		chkParmCorpNo = true;
    		showLogMessage("I", "", String.format("處理指定id_no/corp_no[%s]...", corpNo));
    		showLogMessage("I", "", String.format("取得corp_p_seqno[%s]...", crdCorpPSeqno));
    	}else {
    		comc.errExit("select crd_corp notfound ,參數2 統一編號不存在", "");
    	}
    
    }
    
    /***********************************************************************/
    void getIdSumEndBal() throws Exception {
    	sqlCmd = "select sum(end_bal) as id_end_bal from col_bad_detail_ext where id_p_seqno = ? ";
    	setString(1,debtIdPSeqno);
    	int recordCnt = selectTable();
    	if(recordCnt>0) {
    		detailExtSumEndBal = getValueDouble("id_end_bal");
    	}
    }
    
    /***********************************************************************/
    void getCorpSumEndBal() throws Exception {
    	sqlCmd = "select sum(end_bal) as corp_end_bal from col_bad_detail_ext where corp_p_seqno = ? ";
    	setString(1,debtCorpPSeqno);
    	int recordCnt = selectTable();
    	if(recordCnt>0) {
    		detailExtSumEndBal = getValueDouble("corp_end_bal");
    	}
    }
    
    /***********************************************************************/
    void getIdSumActEndBal() throws Exception {
    	sqlCmd = "select sum(acct_jrnl_bal) as id_acct_jrnl_bal from act_acct where id_p_seqno = ? and acct_type = '01'";
    	setString(1,debtIdPSeqno);
    	int recordCnt = selectTable();
    	if(recordCnt>0) {
    		detailExtSumActJrnlBal = getValueDouble("id_acct_jrnl_bal");
    	}
    }
    
    /***********************************************************************/
    void getCorpSumActEndBal() throws Exception {
    	sqlCmd = "select sum(acct_jrnl_bal) as corp_acct_jrnl_bal from act_acct where corp_p_seqno = ? and acct_type = '03'";
    	setString(1,debtCorpPSeqno);
    	int recordCnt = selectTable();
    	if(recordCnt>0) {
    		detailExtSumActJrnlBal = getValueDouble("corp_end_bal");
    	}
    }
    
    /************轉催後使用的利率(日利率)。***********************************************************/
    void selectPtrActgeneralN(String acctType) throws Exception {
    	sqlCmd = "select revolving_interest2 from ptr_actgeneral_n where acct_type = ? ";
    	setString(1,acctType);
    	int recordCnt = selectTable();
    	if(recordCnt>0) {
    		nRevolvingInterest2 = getValueDouble("revolving_interest2");
    	}else {
    		comc.errExit(String.format("select ptr_actgeneral_n notfound ,acct_type = [%s]", acctType), "");
    	}
    
    }
    
    /***********************************************************************/
    int selectColCsInfo() throws Exception {
    	sqlCmd = " select issue_bank_no from col_cs_info where curr_code = '901' and p_seqno = ? ";
    	setString(1,debtPSeqno);
    	int recordCnt = selectTable();
    	if(recordCnt > 0) {
    		infoIssueBankNo = getValue("issue_bank_no");
    		return 0; 
    	}
    	return 1;
    } 
    
    /***********************************************************************/
    int selectColCsInfo2() throws Exception {
    	sqlCmd = " SELECT CORP_P_SEQNO,ISSUE_BANK_NO,count(*) FROM col_cs_info WHERE CORP_P_SEQNO = ? AND ISSUE_BANK_NO <> '' ";
    	sqlCmd += " GROUP BY CORP_P_SEQNO,ISSUE_BANK_NO ORDER BY count(*) desc ";
    	sqlCmd += " fetch first 1 rows only ";
    	setString(1,debtPSeqno);
    	int recordCnt = selectTable();
    	if(recordCnt > 0) {
    		infoIssueBankNo = getValue("issue_bank_no");
    		return 0; 
    	}
    	return 1;
    } 


    /*************************檢核col_bad_debt_ext資料是否已存在*********************************************/
    int selectColBadDebtExt(String idCorpPSeqno) throws Exception {
    	sqlCmd = " select rowid as rowid from col_bad_debt_ext where id_corp_p_seqno = ? ";
    	setString(1,idCorpPSeqno);
    	int recordCnt = selectTable();
    	if(recordCnt > 0) {
    		debtExtRowid = getValue("rowid");
    		return 0; 
    	}
    	return 1;
    }
    
    /***********************檢核col_bad_detail_ext是否已存在資料************************************************/
    int selectColBadDetailExt(String parm1 ,String colSql) throws Exception {
    	sqlCmd = " select count(*) ext_cnt from col_bad_detail_ext ";
    	sqlCmd += colSql;
    	setString(1,parm1);
    	selectTable();
    	int extCnt = getValueInt("ext_cnt");
    	if(extCnt > 0) {
    		return 0; 
    	}
    	return 1;
    }
    
    /**說明：因col_bad_detail沒有歷史檔，需要JOIN col_bad_debt並以TRANS_DATE為查詢條件，如無此條件，
           * 可能會造成將過去歷史的col_bad_detail(TRANS_DATE不同時間)的金額進行累計
    **/
    void selectColBadDetail() throws Exception {
    	putDetailExtMap(debtIdPSeqno);
    	sqlCmd = " SELECT B.ID_P_SEQNO,a.P_SEQNO,b.ACCT_TYPE,A.NEW_ACCT_CODE,sum(a.END_BAL) AS SUM_END_BAL,max(a.REFERENCE_NO) AS REFERENCE_NO ";
    	sqlCmd += " FROM col_bad_detail a,col_bad_debt b ";
    	sqlCmd += " WHERE a.P_SEQNO = b.P_SEQNO ";
    	sqlCmd += " AND a.TRANS_DATE = b.TRANS_DATE ";
    	sqlCmd += " AND b.ACCT_TYPE = '01' ";
    	sqlCmd += " AND b.ID_P_SEQNO = ? ";
    	sqlCmd += " And b.TRANS_DATE = ? ";
    	sqlCmd += " GROUP BY B.ID_P_SEQNO,a.P_SEQNO,b.ACCT_TYPE,a.NEW_ACCT_CODE ";
    	setString(1,debtIdPSeqno);
    	setString(2,hBusinessDate);
    	extendField = "col_bad_detail.";
    	int recordCnt = selectTable();
    	if(recordCnt <= 0) {
    		showLogMessage("I","",String.format("selectColBadDetail not found! ,ID_P_SEQNO = [%s] ,TRANS_DATE = [%s]", debtIdPSeqno,hBusinessDate));
    		return;
    	}
    	for(int i = 0; i<recordCnt ;i++) {
    		detailPSeqno = getValue("col_bad_detail.p_seqno",i);
        	detailAcctCode = getValue("col_bad_detail.new_acct_code",i);
        	detailEndBal = getValueDouble("col_bad_detail.sum_end_bal",i);
			insertColBadDetailExt(extAcctType,debtIdPSeqno,true);
			getIdSumEndBal();
			getIdSumActEndBal();
			insertColBadJrnlExt(extAcctType);
    	}
		for (String keyStr : detailExtMap.keySet()) {
			if (detailExtMap.get(keyStr) != null && detailExtMap.get(keyStr) == 0) {
				String[] dataStr = keyStr.split(",");
				debtIdPSeqno = dataStr[0];
				detailAcctCode = dataStr[1];
				detailEndBal = 0;
   			    detailPSeqno = detailPSeqnoTmp;
				insertColBadDetailExt(extAcctType, debtIdPSeqno, false);
			}
		}
    }
    
    /**說明：因col_bad_detail沒有歷史檔，需要JOIN col_bad_debt並以TRANS_DATE為查詢條件，如無此條件，
            * 可能會造成將過去歷史的col_bad_detail(TRANS_DATE不同時間)的金額進行累計
    **/
    void selectColBadDetail2() throws Exception {
    	putDetailExtMap(debtCorpPSeqno);
    	sqlCmd = " SELECT B.corp_p_seqno,B.ID_P_SEQNO,a.P_SEQNO,b.ACCT_TYPE,A.NEW_ACCT_CODE,sum(a.END_BAL) AS SUM_END_BAL,max(a.REFERENCE_NO) AS REFERENCE_NO ";
    	sqlCmd += " FROM col_bad_detail a,col_bad_debt b ";
    	sqlCmd += " WHERE a.P_SEQNO = b.P_SEQNO ";
    	sqlCmd += " AND a.TRANS_DATE = b.TRANS_DATE ";
    	sqlCmd += " AND b.ACCT_TYPE = '03' ";
    	sqlCmd += " AND b.corp_p_seqno = ? ";
    	sqlCmd += " And b.TRANS_DATE = ? ";
    	sqlCmd += " AND b.TRANS_TYPE='3' ";
    	sqlCmd += " GROUP BY B.corp_p_seqno,B.ID_P_SEQNO,a.P_SEQNO,b.ACCT_TYPE,a.NEW_ACCT_CODE ";
    	sqlCmd += " order by  B.corp_p_seqno,B.ID_P_SEQNO ,a.P_SEQNO,sum(a.END_BAL) desc ";
    	setString(1,debtCorpPSeqno);
    	setString(2,hBusinessDate);
    	extendField = "col_bad_detail2.";
    	int recordCnt = selectTable();
    	if(recordCnt <= 0) {
    		showLogMessage("I","",String.format("selectColBadDetail2 notfound! ,corp_p_seqno = [%s] ,TRANS_DATE = [%s]", debtCorpPSeqno,hBusinessDate));
    		return;
    	}
    	for(int i = 0;i<recordCnt ;i++) {
    	   	debtIdPSeqno = getValue("col_bad_detail2.id_p_seqno",i);
        	detailPSeqno = getValue("col_bad_detail2.p_seqno",i);
        	detailAcctCode = getValue("col_bad_detail2.new_acct_code",i);
        	detailEndBal = getValueDouble("col_bad_detail2.sum_end_bal",i);
    		insertColBadDetailExt(extAcctType,debtCorpPSeqno,true);
			getCorpSumEndBal();
			getCorpSumActEndBal();
			insertColBadJrnlExt(extAcctType);
    	}
    	 for(String keyStr : detailExtMap.keySet()) {
    		 if(detailExtMap.get(keyStr) != null && detailExtMap.get(keyStr) == 0) {
    			 String[] dataStr = keyStr.split(",");
    			 debtCorpPSeqno = dataStr[0];
    			 detailAcctCode = dataStr[1];
    			 detailEndBal = 0;
    			 debtIdPSeqno = debtIdPSeqnoTmp;
    			 detailPSeqno = detailPSeqnoTmp;
    			 insertColBadDetailExt(extAcctType,debtCorpPSeqno,false);
    		 }
    	 }
    }
    
    /***********************************************************************/
    void insertColBadDebtExthst() throws Exception {
    	
    	sqlCmd = " insert into col_bad_debt_exthst (id_corp_p_seqno,trans_type,trans_date,org_trans_date,last_pay_date,int_rate_day,reg_bank_no,issue_bank_no,mod_time,mod_user,mod_pgm,crt_time,crt_user,mod_seqno) ";
    	sqlCmd	+= " select id_corp_p_seqno,trans_type,trans_date,org_trans_date,last_pay_date,int_rate_day,reg_bank_no,issue_bank_no,mod_time,mod_user,mod_pgm,crt_time,crt_user,mod_seqno ";
    	sqlCmd  += " from col_bad_debt_ext where rowid = ? ";
    	setRowId(1, debtExtRowid);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insertColBadDebtExthst duplicate!", "", "");
        }
    }
    
    /***********************************************************************/
    void insertColBadDetailExthst(String parm1,String colSql) throws Exception {
    	sqlCmd = " insert into col_bad_detail_exthst select * from col_bad_detail_ext ";
    	sqlCmd += colSql;
    	setString(1, parm1);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insertColBadDetailExthst duplicate!", "", "");
        }
    }
    
    /***********************************************************************/   
    void deleteColBadDebtExt() throws Exception {
    	daoTable = "col_bad_debt_ext";
    	whereStr = "where rowid = ? ";
    	setRowId(1, debtExtRowid);
        deleteTable();
    }
    
    /***********************************************************************/   
    void deleteColBadDetailExt(String parm1,String colSql) throws Exception {
    	daoTable = "col_bad_detail_ext";
    	whereStr = colSql;
    	setString(1, parm1);
        deleteTable();
    }
    
    /***********************************************************************/
	int insertColBadDebtExt(String idCorpPSeqno) throws Exception {
		extendField = "debt_ext.";
		setValue("debt_ext.ID_CORP_P_SEQNO", idCorpPSeqno);
		setValue("debt_ext.TRANS_TYPE", "3");
		setValue("debt_ext.TRANS_DATE", hBusinessDate);
		setValue("debt_ext.LAST_PAY_DATE", comStr.left(hBusinessDate, 6) + debtStmtCycle);
		setValueDouble("debt_ext.INT_RATE_DAY", nRevolvingInterest2);
		setValue("debt_ext.ISSUE_BANK_NO", infoIssueBankNo);
		setValue("debt_ext.crt_user", javaProgram);
		setValue("debt_ext.crt_time", sysDate + sysTime);
		setValue("debt_ext.mod_time", sysDate + sysTime);
		setValue("debt_ext.mod_user", javaProgram);
		setValue("debt_ext.mod_pgm", javaProgram);
		setValueInt("debt_ext.mod_seqno", 0);
		daoTable = "col_bad_debt_ext";
		insertTable();
		if (dupRecord.equals("Y")) {
			showLogMessage("I","",String.format("insertColBadDebtExt duplicate! ,ID_CORP_P_SEQNO = [%s]", idCorpPSeqno));
			return 1;
		}
		return 0;
	}
	
    /***********************************************************************/
	int insertColBadDetailExt(String acctType,String idCorpPSeqno,boolean emptyData) throws Exception {
		extendField = "detail_ext.";
		setValue("detail_ext.ID_CORP_P_SEQNO", idCorpPSeqno);
		setValue("detail_ext.ID_P_SEQNO", debtIdPSeqno);
		setValue("detail_ext.CORP_P_SEQNO", debtCorpPSeqno);
		setValue("detail_ext.P_SEQNO", detailPSeqno);
		setValue("detail_ext.ACCT_TYPE", acctType);
		setValue("detail_ext.ACCT_CODE", detailAcctCode);
		setValueDouble("detail_ext.BEG_BAL", detailEndBal);
		setValueDouble("detail_ext.END_BAL", detailEndBal);
		setValue("detail_ext.crt_user", javaProgram);
		setValue("detail_ext.crt_time", sysDate + sysTime);
		setValue("detail_ext.mod_time", sysDate + sysTime);
		setValue("detail_ext.mod_user", javaProgram);
		setValue("detail_ext.mod_pgm", javaProgram);
		setValueInt("detail_ext.mod_seqno", 0);
		daoTable = "col_bad_detail_ext";
		insertTable();
		if (dupRecord.equals("Y")) {
			showLogMessage("I","",String.format("insertColBadDetailExt duplicate! ,ID_CORP_P_SEQNO = [%s] ,ACCT_CODE = [%s]", idCorpPSeqno,detailAcctCode));
			daoTable = "col_bad_detail_ext";
			updateSQL = "BEG_BAL = BEG_BAL + ?,";
			updateSQL += "END_BAL = END_BAL + ?";
			whereStr = " where ID_CORP_P_SEQNO = ? and ACCT_CODE = ? ";
			setDouble(1,detailEndBal);
			setDouble(2,detailEndBal);
			setString(3,idCorpPSeqno);
			setString(4,detailAcctCode);
			updateTable();
			if (!notFound.equals("Y")) {
				showLogMessage("I","",String.format("update col_bad_detail_ext OK ,ID_CORP_P_SEQNO = [%s] ,ACCT_CODE = [%s]", idCorpPSeqno,detailAcctCode));
			}
			 
		}else {
			if(emptyData) {
				debtIdPSeqnoTmp = debtIdPSeqno;
				detailPSeqnoTmp = detailPSeqno;
				detailExtMap.put(idCorpPSeqno+","+detailAcctCode, 1);
			}
		}
		
		return 0;
	}
	
    /***********************************************************************/
	void putDetailExtMap(String idCorpPSeqno) {
		detailExtMap = new HashMap<String,Integer>();
		for(int i=0 ; i<acctCodes.length ; i++) {
			detailExtMap.put(idCorpPSeqno+","+acctCodes[i], 0);
		}
	}
	
    /***********************************************************************/
	int insertColBadJrnlExt(String acctType) throws Exception {
		extendField = "jrnl_ext.";
		setValueInt("jrnl_ext.ENQ_SEQNO", jrnlEnqSeqno++);
		setValue("jrnl_ext.P_SEQNO", detailPSeqno);
		setValue("jrnl_ext.ACCT_TYPE", acctType);
		setValue("jrnl_ext.ID_P_SEQNO", debtIdPSeqno);
		setValue("jrnl_ext.CORP_P_SEQNO", debtCorpPSeqno);
		setValue("jrnl_ext.ACCT_DATE", hBusinessDate);
		setValue("jrnl_ext.ACCT_CLASS", "B");
		setValue("jrnl_ext.TRAN_CLASS", "B");
		setValue("jrnl_ext.TRAN_TYPE", detailAcctCode+"01");
		setValue("jrnl_ext.ACCT_CODE", detailAcctCode);
		setValue("jrnl_ext.DR_CR", "C");
		setValueDouble("jrnl_ext.TRANSACTION_AMT", detailEndBal);
		setValueDouble("jrnl_ext.JRNL_BAL", detailExtSumEndBal);
		setValueDouble("jrnl_ext.ACT_JRNL_BAL", detailExtSumActJrnlBal);
		setValueDouble("jrnl_ext.ITEM_BAL", detailEndBal);
		setValue("jrnl_ext.ITEM_DATE", hBusinessDate);
		setValue("jrnl_ext.INTEREST_DATE", "");
		setValue("jrnl_ext.ADJ_REASON_CODE", ""); //新增時放空值 sunny change 20230206
		setValue("jrnl_ext.ADJ_COMMENT", "轉催入主檔");
		setValue("jrnl_ext.REVERSAL_FLAG", "");
		setValueDouble("jrnl_ext.PAYMENT_REV_AMT", 0);
		setValue("jrnl_ext.REFERENCE_NO", "");
		setValue("jrnl_ext.PAY_ID", "");
		setValue("jrnl_ext.STMT_CYCLE", "");
		setValue("jrnl_ext.C_DEBT_KEY", "");
		setValueInt("jrnl_ext.ORDER_SEQ", 0);
		setValue("jrnl_ext.JRNL_SEQNO", "");
		setValue("jrnl_ext.BATCH_NO", "");
		setValueDouble("jrnl_ext.INT_RATE", 0);
		setValue("jrnl_ext.MEMO", "");
		setValue("jrnl_ext.crt_date", sysDate);
		setValue("jrnl_ext.crt_user", javaProgram);
		setValue("jrnl_ext.crt_time", sysTime);
		setValue("jrnl_ext.UPDATE_DATE", "");
		setValue("jrnl_ext.UPDATE_USER", "");
		setValue("jrnl_ext.mod_time", sysDate + sysTime);
		setValue("jrnl_ext.mod_user", javaProgram);
		setValue("jrnl_ext.mod_pgm", javaProgram);
		setValueInt("jrnl_ext.mod_seqno", 0);
		daoTable = "col_bad_jrnl_ext";
		insertTable();
		if (dupRecord.equals("Y")) {
			showLogMessage("I","","insertColBadJrnlExt duplicate!");
			return 1;
		}
		return 0;
	}
    
    void initialData() {
		/******** col_bad_debt **********/
		debtIdPSeqno = "";
		debtIdNo = "";
		debtStmtCycle = "";
		debtPSeqno = "";
		debtCorpPSeqno = "";

		/******** col_bad_debt_ext **********/
		debtExtRowid = "";

		/******** col_bad_detail_ext **********/
		detailExtRowid = "";
		detailExtSumEndBal = 0.0;
		detailExtSumActJrnlBal = 0.0;

		/******** ptr_actgeneral_n **********/
		nRevolvingInterest2 = 0.0;

		/******** col_cs_info **********/
		infoIssueBankNo = "";

		/******** col_bad_detail **********/
		detailPSeqno = "";
		detailAcctCode = "";
		detailEndBal = 0.0;
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB010 proc = new ColB010();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
