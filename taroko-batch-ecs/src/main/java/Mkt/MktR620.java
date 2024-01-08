/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/03/03  V1.00.00    Ryan     program initial                           *
 ******************************************************************************/

package Mkt;

import java.math.BigDecimal;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

/*推卡達成率報表月統計處理程式*/
public class MktR620 extends AccessDAO {

    private String progname = "推卡達成率報表月統計處理程式 112/03/03 V1.00.00";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommString comStr = new CommString();
    CommCrdRoutine comcr = null;

    String hModUser = "";
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String hCallRProgramCode = "";

    String hBusiBusinessDate = "";
    String hLastMonth = "";
    String hLastYear = "";
    
    /********TABLE:MKT_MCARD_STATIC_REACH*************/
    String reachAcctMonth = "";
    String reachBranch = "";
    String reachIntroduceEmpNo = "";
    long reachTargetCardCnt = 0;
    double reachBranchCnt = 0;
    double reachEmployeeCnt = 0;
    long reachMActCardCnt = 0;
    long reachMNoactCardCnt = 0;
    long reachMStopCardCnt = 0;
    double reachMSumCnt = 0;
    double reachMRate = 0;
    long reachMSort = 0;
    double reachCommonFees = 0;
    double reachCaFees = 0;
    double reachCfFees = 0;
    long reachNewCardCnt = 0;
    long reachOldCardCnt = 0;
    long reachConsumeCardCnt = 0;
    double reachYearFees = 0;
    long reachYConsumeCardCnt = 0;
    long reachYActCardCnt = 0;
    long reachYNoactCardCnt = 0;
    long reachYStopCardCnt = 0;
    double reachYSumCnt = 0;
    double reachYRate = 0;
    long reachYSort = 0;
    long reachHActCardCnt = 0;
    long reachHNoactCardCnt = 0;
    long reachHStopCardCnt = 0;
    long reachHSumCnt = 0;
    double reachHYearFees = 0;
    long reachHConsumeCardCnt = 0;
    
    
    /*********mkt_year_target*******/
	String targetAcctYear = "";
	String targetAcctTypeFlag = "";
	String targetCardTypeFlag = "";
	String targetGroupCodeFlag = "";
	String targetBranchFlag = "";
	
    /*********mkt_year_dtl*******/	
	String dtlAcctYear = "";
	String dtlDataType = "";
	String dtlDataCode1 = "";
    
	/*********crd_card*******/	
    String cardCurrentCode = "";
    String cardActivateFlag = "";
    String cardIssueDate = "";
    String cardRegBankNo = "";
    String cardIntroduceEmpNo = "";
    String cardOldCardNo = "";
    String cardIdPSeqno = "";
    String cardAcctType = "";
    String cardOppostDate = "";
    String cardBranch = "";
    String cardCardNo = ""; 
    
	/********bil_bill********/
	String billEcsCusMchtNo = "";
	double billDestAmt = 0;

    String hMyttModUser = "";
    String hMyttModPgm = "";
    String tmpstr = "";
    int totalCount = 0;

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
                comc.errExit("Usage : MktR620 [acct_month]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);

            hModUser = comc.commGetUserID();
            hMyttModUser = hModUser;
            hMyttModPgm = javaProgram;

            selectPtrBusinday();
           
            if(args.length == 1) {
                if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
                	exceptExit = 0;
                    showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
                    return 0;
                }
                hBusiBusinessDate = args[0];
                tmpstr = String.format("%6.6s", args[0]);
                hLastMonth = tmpstr;
                tmpstr = String.format("%4.4s", args[0]);
                hLastYear = tmpstr;
            }
            showLogMessage("I", "", String.format("本日營業日[%s]", hBusiBusinessDate));
            
            if (!"01".equals(hBusiBusinessDate.substring(6))) {
            	exceptExit = 0;
                showLogMessage("I", "", "本程式為每月1日執行");
                return 0;
            }
            
            showLogMessage("I", "", String.format("Will process month [%s]", hLastMonth));
            if(selectMktMcardStaticReach()) {
            	exceptExit = 0;
                showLogMessage("I", "", "資料已存在,不可重覆統計");
                return 0;
            }
      
            if(selectMktYearTargetCnt()<=0) {
            	exceptExit = 0;
                showLogMessage("I", "", String.format("select mkt_year_target not found ,acct_year = [%s]", hLastYear));
                return 0;
            }
            
            selectCrdCard();

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
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
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        hLastMonth = "";
        hLastYear = "";

        sqlCmd = "select business_date,";
        sqlCmd += " to_char(to_date(business_date,'yyyymmdd')-1 month,'yyyymm') h_last_month,";
        sqlCmd += " to_char(to_date(business_date,'yyyymmdd')-1 month,'yyyy') h_last_year ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        selectTable();
        if ("Y".equals(notFound)) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
        hLastMonth = getValue("h_last_month");
        hLastYear = getValue("h_last_year");

    }

    /***********************************************************************/
    void selectMktYearTarget() throws Exception {
    	extendField = "MKT_YEAR_TARGET.";
        sqlCmd = "select acct_year,branch,target_card_cnt,branch_cnt,employee_cnt ,acct_type_flag,card_type_flag,group_code_flag,branch_flag ";
        sqlCmd += "from mkt_year_target ";
        sqlCmd += " where apr_flag = 'Y' and acct_year = ? ";
        setString(1,hLastYear);
        int recordCnt = selectTable();
        if(recordCnt>0) {
        	targetAcctYear = getValue("MKT_YEAR_TARGET.acct_year");
        	reachBranch = getValue("MKT_YEAR_TARGET.branch");
        	reachTargetCardCnt = getValueLong("MKT_YEAR_TARGET.target_card_cnt");
        	reachBranchCnt = getValueDouble("MKT_YEAR_TARGET.branch_cnt");
        	reachEmployeeCnt = getValueDouble("MKT_YEAR_TARGET.employee_cnt");
        	targetAcctTypeFlag = getValue("MKT_YEAR_TARGET.acct_type_flag");
        	targetCardTypeFlag = getValue("MKT_YEAR_TARGET.card_type_flag");
        	targetGroupCodeFlag = getValue("MKT_YEAR_TARGET.group_code_flag");
        	targetBranchFlag = getValue("MKT_YEAR_TARGET.branch_flag");
        }
    }
    
    /***********************************************************************/
    boolean selectMktMcardStaticReach() throws Exception {

        sqlCmd = "select count(*) reach_cnt ";
        sqlCmd += " from mkt_mcard_static_reach ";
        sqlCmd += " where acct_month = ? ";
        setString(1,hLastMonth);
        selectTable();
        int reachCnt = getValueInt("reach_cnt");
        if(reachCnt>0) {
        	return true;
        }
        return false;
    }
    
    /***********************************************************************/
    boolean selectMktMcardStaticReachCnt() throws Exception {
        sqlCmd = "select count(*) reach_cnt2 ";
        sqlCmd += " from mkt_mcard_static_reach ";
        sqlCmd += " where acct_month = ? and branch = ? and introduce_emp_no = ?";
        setString(1, hLastMonth);
        setString(2, cardRegBankNo);
        setString(3, cardIntroduceEmpNo);
        selectTable();
        int reachCnt = getValueInt("reach_cnt2");
        if(reachCnt>0) {
        	return true;
        }
        return false;
    }
    
    /***********************************************************************/
    void selectMktYearDtl() throws Exception {
       	extendField = "mkt_year_dtl.";
        sqlCmd = "select acct_year,data_type,data_code1 ";
        sqlCmd += "from mkt_year_dtl ";
        sqlCmd += " where acct_year = ? ";
        setString(1,targetAcctYear);
        int recordCnt = selectTable();
        if(recordCnt>0) {
        	dtlAcctYear = getValue("mkt_year_dtl.acct_year");
        	dtlDataType = getValue("mkt_year_dtl.data_type");
        	dtlDataCode1 = getValue("mkt_year_dtl.data_code1");
        }
    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {
        sqlCmd = "select ";
        sqlCmd += " a.current_code,";
        sqlCmd += " a.activate_flag,";
        sqlCmd += " a.issue_date,";
        sqlCmd += " a.reg_bank_no,";
        sqlCmd += " decode(a.introduce_emp_no,'','XXXX',a.introduce_emp_no) h_card_introduce_emp_no,";
        sqlCmd += " a.old_card_no, ";
        sqlCmd += " a.id_p_seqno , a.acct_type, a.oppost_date , a.branch , a.card_no, ";
        sqlCmd += " a.acct_type, a.card_type, a.group_code ";
        sqlCmd += "from crd_card a ,gen_brn b ";
        sqlCmd += "  where a.reg_bank_no != '' ";
        sqlCmd += "  and a.reg_bank_no = b.branch ";
        sqlCmd += "  and substr(a.issue_date,1,6) between to_char(add_months(to_date(?||'01','yyyymmdd'), -11), 'yyyymm') and ? ";  //取前十二個月
      //  sqlCmd += "  and substr(a.issue_date,1,6) = ? "; 
        sqlCmd += "  and old_card_no = '' "; 
        sqlCmd += " order by issue_date desc ";
        setString(1, hLastMonth);
        setString(2, hLastMonth);
        int cursorIndex = openCursor();
        while(fetchTable(cursorIndex)) {
        	initData();
//        	showLogMessage("I", "", String.format("   Processed [%d] Records", totalCount));
            cardCurrentCode = getValue("current_code");
            cardActivateFlag = getValue("activate_flag");
            cardIssueDate = getValue("issue_date");
            cardRegBankNo = getValue("reg_bank_no");
            cardIntroduceEmpNo = getValue("h_card_introduce_emp_no");
            cardOldCardNo = getValue("old_card_no");
            cardIdPSeqno  = getValue("id_p_seqno");
            cardAcctType   = getValue("acct_type");
            cardOppostDate = getValue("oppost_date");
            cardBranch      = getValue("branch");
            cardCardNo     = getValue("card_no");
            
            selectMktYearTarget();

            if(selectMktYearDtlCnt()<=0) {
            	columnCount();
            }else
            if("0".equals(targetCardTypeFlag)&&"0".equals(targetGroupCodeFlag)&&"0".equals(targetBranchFlag)) {
            	columnCount();
            }else 
            if("1".equals(targetCardTypeFlag)&&"1".equals(targetGroupCodeFlag)&&"1".equals(targetBranchFlag)) {
            	selectMktYearDtl();
            	
            	if(dtlDataCode1.equals(cardAcctType) && "01".equals(dtlDataType)) {
            		columnCount();
            	}else
            	if(dtlDataCode1.equals(cardAcctType) && "02".equals(dtlDataType)) {
            		columnCount();
            	}else
            	if(dtlDataCode1.equals(cardAcctType) && "03".equals(dtlDataType)) {
            		columnCount();
            	}else
            		continue;
            }else {
            	continue;
            }
            
          
			if (comStr.left(cardIssueDate, 6).equals(hLastMonth)) {
				// 2.9.8. 月達成率=當月新卡數/員工目標數 (M_RATE=M_SUM_CNT/EMPLOYEE_CNT)
				// 2.12.7. 年達成率:年新增卡數/員工目標數 (Y_RATE= Y_SUM_CNT /EMPLOYEE_CNT)
				reachMRate = div(reachMSumCnt, reachEmployeeCnt);
				reachYRate = div(reachYSumCnt, reachEmployeeCnt);
				// 2.7.3. employee_cnt=0不需計算月達成率(M_RATE),年達成率(Y_RATE),Y_RATE、M_RATE:填入0
				if (reachEmployeeCnt == 0) {
					reachMRate = 0;
					reachYRate = 0;
				}
			}
        	
        	reachCaFees = 0;
 
           // 2.9.10.	月招攬全新卡數: NEW_CARD_CNT:填入,如果【crd_card】ISSUE_DATE=上個月的筆數
            if(comStr.left(cardIssueDate, 6).equals(hLastMonth)) {
            	reachNewCardCnt++;
            } 
           // 2.9.11.	月招攬舊卡數: OLD_CARD_CNT:填入0
            reachOldCardCnt = 0;
            
            //2.9.12.	月一般消費: COMMON_FEES填入值,月預借現金消費: CA_FEES填入值,月有消費卡數 : CONSUME_CARD_CNT填入值
            selectMktCardConsumeMonth(comStr.left(cardIssueDate, 6));
           
            //2.10.1.	依據取得card_no查是否存在【bil_bill】where條件CARD_NO=? and ACCT_MONTH =上個月
            selectBilBill(comStr.left(cardIssueDate, 6));

            if(selectMktMcardStaticReachCnt()) {
            	updateMktMcardStaticReach();
            }else {
            	insertMktMcardStaticReach();
            }
            totalCount++;
            if (totalCount % 1000 == 0) {
                commitDataBase();
                showLogMessage("I", "", String.format("   Processed [%d] Records", totalCount));
            }
        }
        closeCursor(cursorIndex);
    }
    
    void columnCount() {
    	//2.9.4.	月新增卡數-已開卡: M_ACT_CARD_CNT=如果crd_card. ACTIVATE_FLAG=2,值=1,再加總. 
    	if("2".equals(cardActivateFlag)) {
    		reachHActCardCnt++;
    		if(comStr.left(cardIssueDate, 6).equals(hLastMonth)) 
    			reachMActCardCnt++;
    		if(comStr.left(cardIssueDate, 6).compareTo(hLastYear+"01")>=0)
    			reachYActCardCnt++;
    	}
    	//2.9.5.	月新增卡數-未開卡:如果crd_card.ACTIVATE_FLAG=1, M_NOACT_CARD_CNT=1,再加總
    	if("1".equals(cardActivateFlag)) {
    		reachHNoactCardCnt++;
    		if(comStr.left(cardIssueDate, 6).equals(hLastMonth)) 
    			reachMNoactCardCnt++;
    		if(comStr.left(cardIssueDate, 6).compareTo(hLastYear+"01")>=0)
    			reachYNoactCardCnt++;
     	}
    	
    	//2.9.6.	月新增卡數-停卡:如果crd_card.CURRENT_CODE <>0, M_STOP_CARD_CNT=1,再加總 
    	if(!"0".equals(cardCurrentCode)) {
    		reachHStopCardCnt++;
    		if(comStr.left(cardIssueDate, 6).equals(hLastMonth)) 
    			reachMStopCardCnt++;
    		if(comStr.left(cardIssueDate, 6).compareTo(hLastYear+"01")>=0)
    			reachYStopCardCnt++;
     	}
        //2.9.7.	月新增卡數-合計: M_SUM_CNT=M_ACT_CARD_CNT+ M_NOACT_CARD_CNT
    	reachHSumCnt = reachHActCardCnt + reachHNoactCardCnt + reachHStopCardCnt;
    	if(comStr.left(cardIssueDate, 6).equals(hLastMonth)) 
    		reachMSumCnt = reachMActCardCnt + reachMNoactCardCnt + reachMStopCardCnt;
 		if(comStr.left(cardIssueDate, 6).compareTo(hLastYear+"01")>=0)
 			reachYSumCnt = reachYActCardCnt + reachYNoactCardCnt + reachYStopCardCnt;

    }
    
    int selectMktYearTargetCnt() throws Exception {
        sqlCmd  = " select count(*) target_cnt from mkt_year_target where apr_flag = 'Y' and acct_year = ?  ";
        setString(1, hLastYear);
        selectTable();
        int targetCnt = getValueInt("target_cnt");
        return targetCnt;
    }
    
    int selectMktYearDtlCnt() throws Exception {
    	 sqlCmd = " SELECT count(*) dtl_cnt FROM MKT_YEAR_DTL a left join MKT_YEAR_TARGET b on b.acct_year=a.acct_year ";
    	 sqlCmd += " where b.apr_flag='Y' and (b.ACCT_TYPE_FLAG='1' or b.ACCT_TYPE_FLAG='2') ";
    	 sqlCmd += " and (b.CARD_TYPE_FLAG='1' or b.CARD_TYPE_FLAG='2') ";
    	 sqlCmd += " and (b.GROUP_CODE_FLAG='1' or b.GROUP_CODE_FLAG='2') ";
    	 sqlCmd += " and (b.BRANCH_FLAG='1' or b.BRANCH_FLAG='2') ";
    	 sqlCmd += " AND b.acct_year = ?";
         setString(1, hLastYear);
         selectTable();
         int dtlCnt = getValueInt("dtl_cnt");
         return dtlCnt;
    }

    
    void updateMktMcardStaticReach() throws Exception {
    	int i = 1;
    	   daoTable = "mkt_mcard_static_reach";
           updateSQL = " m_act_card_cnt     = m_act_card_cnt   + ?,";
           updateSQL += " m_noact_card_cnt   = m_noact_card_cnt + ?,";
           updateSQL += " m_stop_card_cnt    = m_stop_card_cnt  + ?,";
           updateSQL += " m_sum_cnt     = m_sum_cnt   + ?,";
           updateSQL += " m_rate   = m_rate + ?,";
           updateSQL += " m_sort    = m_sort  + ?,";
           updateSQL += " common_fees     = common_fees   + ?,";
           updateSQL += " ca_fees   = ca_fees + ?,";
           updateSQL += " cf_fees    = cf_fees  + ?,";
           updateSQL += " new_card_cnt       = new_card_cnt  + ?,";
           updateSQL += " old_card_cnt       = old_card_cnt  + ?,";
           updateSQL += " consume_card_cnt   = consume_card_cnt  + ?,";//12
           
           //年
           updateSQL += " year_fees          = year_fees  + ?,";
           updateSQL += " y_consume_card_cnt = y_consume_card_cnt  + ?,";
           updateSQL += " y_act_card_cnt     = y_act_card_cnt  + ?,";
           updateSQL += " y_noact_card_cnt   = y_noact_card_cnt  + ?,";
           updateSQL += " y_stop_card_cnt    = y_stop_card_cnt  + ?,";
           updateSQL += " y_sum_cnt          = y_sum_cnt  + ?,";
           updateSQL += " y_rate             = y_rate  + ?, ";
           updateSQL += " y_sort             = y_sort  + ?, ";//8
//           
//           //近一年
           updateSQL += " h_act_card_cnt = h_act_card_cnt  + ?, ";
           updateSQL += " h_noact_card_cnt = h_noact_card_cnt  + ?, ";
           updateSQL += " h_stop_card_cnt = h_stop_card_cnt  + ?, ";
           updateSQL += " h_sum_cnt = h_sum_cnt  + ?, ";
           updateSQL += " h_year_fees = h_year_fees  + ?, ";
           updateSQL += " h_consume_card_cnt = h_consume_card_cnt  + ? ";//6

           whereStr = "where acct_month       = ?  ";
           whereStr += "and branch           = ?  ";
           whereStr += "and introduce_emp_no = ? ";

           setLong(i++, reachMActCardCnt);
           setLong(i++, reachMNoactCardCnt);
           setLong(i++, reachMStopCardCnt);
           setDouble(i++, reachMSumCnt);
           setDouble(i++, reachMRate);
           setLong(i++, reachMSort);
           setDouble(i++, reachCommonFees);
           setDouble(i++, reachCaFees);
           setDouble(i++, reachCfFees);
           setDouble(i++, reachNewCardCnt);
           setDouble(i++, reachOldCardCnt);
           setDouble(i++, reachConsumeCardCnt);
           
           //年
           setDouble(i++, reachYearFees);
           setLong(i++, reachYConsumeCardCnt);
           setLong(i++, reachYActCardCnt);
           setLong(i++, reachYNoactCardCnt);
           setLong(i++, reachYStopCardCnt);
           setDouble(i++, reachYSumCnt);
           setDouble(i++, reachYRate);
           setDouble(i++, reachYSort);
           
           //近一年
           setLong(i++, reachHActCardCnt);
           setLong(i++, reachHNoactCardCnt);
           setLong(i++, reachHStopCardCnt);
           setLong(i++, reachHSumCnt);
           setDouble(i++, reachHYearFees);
           setLong(i++, reachHConsumeCardCnt);
           
           setString(i++, hLastMonth);
           setString(i++, cardRegBankNo);
           setString(i++, cardIntroduceEmpNo);
           
           updateTable();

           if ("Y".equals(notFound)) {
               showLogMessage("I", "", String.format("update mkt_mcard_static_reach not found, acct_month[%s],branch[%s],introduce_emp_no[%s]",hLastMonth,cardRegBankNo,cardIntroduceEmpNo));
           }
    }
    
    /***********************************************************************/
    void insertMktMcardStaticReach() throws Exception {

        setValue("acct_month", hLastMonth);
        setValue("branch", cardRegBankNo);
        setValue("introduce_emp_no", cardIntroduceEmpNo);
        setValueLong("target_card_cnt", reachTargetCardCnt);
        setValueDouble("branch_cnt", reachBranchCnt);
        setValueDouble("employee_cnt", reachEmployeeCnt);
        setValueLong("m_sort", reachMSort);
   
        //上個月欄位
        setValueLong("m_act_card_cnt", reachMActCardCnt);
        setValueLong("m_noact_card_cnt", reachMNoactCardCnt);
        setValueLong("m_stop_card_cnt", reachMStopCardCnt);
        setValueDouble("m_sum_cnt", reachMSumCnt);
        setValueDouble("m_rate", reachMRate);
        setValueDouble("m_sort", reachMSort);
        setValueDouble("common_fees", reachCommonFees);
        setValueDouble("ca_fees", reachCaFees);
        setValueDouble("cf_fees", reachCfFees);
        setValueDouble("new_card_cnt", reachNewCardCnt);
        setValueDouble("old_card_cnt", reachOldCardCnt);
        setValueDouble("consume_card_cnt", reachConsumeCardCnt);
        
        //年
        setValueDouble("year_fees", reachYearFees);
        setValueLong("y_consume_card_cnt", reachYConsumeCardCnt);
        setValueLong("y_act_card_cnt", reachYActCardCnt);
        setValueLong("y_noact_card_cnt", reachYNoactCardCnt);
        setValueLong("y_stop_card_cnt", reachYStopCardCnt);
        setValueDouble("y_sum_cnt", reachYSumCnt);
        setValueDouble("y_rate", reachYRate);
        setValueLong("y_sort", reachYSort);
        
        //近一年
        setValueLong("h_act_card_cnt", reachHActCardCnt);
        setValueLong("h_noact_card_cnt", reachHNoactCardCnt);
        setValueLong("h_stop_card_cnt", reachHStopCardCnt);
        setValueLong("h_sum_cnt", reachHSumCnt);
        setValueDouble("h_year_fees", reachHYearFees);
        setValueLong("h_consume_card_cnt", reachHConsumeCardCnt);
 
        setValue("mod_user", hMyttModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hMyttModPgm);
        setValueDouble("mod_seqno", 0);
        
        daoTable = "MKT_MCARD_STATIC_REACH";
        insertTable();

        if("Y".equals(dupRecord)) {
        	showLogMessage("I", "", String.format("insert mkt_mcard_static_reach duplicate, acct_month[%s],branch[%s],introduce_emp_no[%s]",hLastMonth,cardRegBankNo,cardIntroduceEmpNo));
        }
        
    }


    /***********************************************************************/
    void selectBilBill(String month) throws Exception {
    	extendField = "bil_bill.";
        sqlCmd  = " SELECT ecs_cus_mcht_no,dest_amt ";
        sqlCmd += "   FROM bil_bill ";
        sqlCmd += "  WHERE acct_month = ? ";
        sqlCmd += "    AND card_no = ? ";
        setString(1, month);
        setString(2, cardCardNo);
        int recordCnt = selectTable();
        for (int i = 0; i<recordCnt ;i++) {
            billEcsCusMchtNo = getValue("bil_bill.ecs_cus_mcht_no");
            billDestAmt     = getValueDouble("bil_bill.dest_amt");
            
            if(!comStr.empty(billEcsCusMchtNo)) {
            	//2.10.2.	依取得ECS_CUS_MCHT_NO檢核是否存在【MKT_MCHTGP_DATA】LEFT JOIN【mkt_mcht_gp】如果筆數>0,表示這一筆要排除
            	if(selectMktMchtGp() > 0) {
            		//2.10.2.2.	card_no的消費金額不計算,也就是COMMON_FEES要扣掉該筆消費金額, CONSUME_CARD_CNT要扣掉1卡(筆).
            		reachHYearFees = reachHYearFees - billDestAmt;
                 	if(comStr.left(month, 6).equals(hLastMonth)) 
                 		reachCommonFees = reachCommonFees - billDestAmt;
             		if(comStr.left(month, 6).compareTo(hLastYear+"01")>=0)
             			reachYearFees = reachYearFees - billDestAmt;
            	}
            }	
        }
    }
    
    
    int selectMktMchtGp() throws Exception {
        sqlCmd  = " SELECT count(*) gp_cnt ";
        sqlCmd += "   FROM mkt_mchtgp_data a,mkt_mcht_gp b on b.mcht_gourp_id = a.data_key";
        sqlCmd += "  WHERE b.mcht_gourp_id = 'MKTR00001' ";
        sqlCmd += "    AND b.platfrom_flag = '2' ";
        sqlCmd += "    AND a.table_name = 'MKT_MCHT_GP' ";
        sqlCmd += "    AND a.data_code = ? ";
        setString(1, billEcsCusMchtNo);
        selectTable();
        int gpCnt = getValueInt("gp_cnt");
        return gpCnt;
    }

    void selectMktCardConsumeMonth(String month) throws Exception {
        // 上個月消費金額
    	extendField = "mkt_card_consume.";
        sqlCmd = " SELECT sum(consume_bl_amt+consume_it_amt+consume_ot_amt+consume_id_amt+consume_ao_amt) as h_mmsc_common_fees, "; // 一般消費金額
        sqlCmd += "       sum(consume_ca_amt) as h_mmsc_ca_fees  "; // 有預借現金消費金額
        sqlCmd += "   FROM mkt_card_consume ";
        sqlCmd += "  WHERE acct_month = ? ";
        sqlCmd += "    AND card_no = ? ";
        sqlCmd += "    AND acct_type = ? ";
        setString(1, month);
        setString(2, cardCardNo);
        setString(3, cardAcctType);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	reachCommonFees = getValueLong("mkt_card_consume.h_mmsc_common_fees");
        	reachCaFees = getValueLong("mkt_card_consume.h_mmsc_ca_fees");
        	
            if (reachCommonFees + reachCaFees > 0) {
            	reachHConsumeCardCnt++;
            	if(comStr.left(month, 6).equals(hLastMonth)) 
            		reachConsumeCardCnt++;
         		if(comStr.left(month, 6).compareTo(hLastYear+"01")>=0)
         			reachYConsumeCardCnt++;
            } 
        }
        if(!comStr.left(month, 6).equals(hLastMonth)) {
        	 reachCommonFees = 0;
             reachCaFees = 0;
        }
    }

    
	public Double div(Double v1, Double v2) {

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP).doubleValue();

	}
    
    void initData() {
		cardCurrentCode = "";
		cardActivateFlag = "";
		cardIssueDate = "";
		cardRegBankNo = "";
		cardIntroduceEmpNo = "";
		cardOldCardNo = "";
		cardIdPSeqno = "";
		cardAcctType = "";
		cardOppostDate = "";
		cardBranch = "";
		cardCardNo = "";

		reachAcctMonth = "";
		reachBranch = "";
		reachIntroduceEmpNo = "";
		reachTargetCardCnt = 0;
		reachBranchCnt = 0;
		reachEmployeeCnt = 0;
		reachMActCardCnt = 0;
		reachMNoactCardCnt = 0;
		reachMStopCardCnt = 0;
		reachMSumCnt = 0;
		reachMRate = 0;
		reachMSort = 0;
		reachCommonFees = 0;
		reachCaFees = 0;
		reachCfFees = 0;
		reachNewCardCnt = 0;
		reachOldCardCnt = 0;
		reachConsumeCardCnt = 0;
		reachYearFees = 0;
		reachYConsumeCardCnt = 0;
		reachYActCardCnt = 0;
		reachYNoactCardCnt = 0;
		reachYStopCardCnt = 0;
		reachYSumCnt = 0;
		reachYRate = 0;
		reachYSort = 0;
		reachHActCardCnt = 0;
		reachHNoactCardCnt = 0;
		reachHStopCardCnt = 0;
		reachHSumCnt = 0;
		reachHYearFees = 0;
		reachHConsumeCardCnt = 0;

		targetAcctYear = "";
		targetAcctTypeFlag = "";
		targetCardTypeFlag = "";
		targetGroupCodeFlag = "";
		targetBranchFlag = "";

		dtlAcctYear = "";
		dtlDataType = "";
		dtlDataCode1 = "";
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktR620 proc = new MktR620();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
