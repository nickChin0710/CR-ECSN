/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  107/01/01  V1.00.00    Edson     program initial                           *
 *  107/01/22  V1.00.01    Brian     error correction                          *
 *  109-12-04  V1.00.02    tanwei    updated for project coding standard       *
 *  110/10/15  V1.00.03    Castor    Adjust the bonus processing               *
 ******************************************************************************/

package Mkt;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*員工信用卡招攬獎勵統計明細處理程式*/
public class MktB110 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private String progname = "員工信用卡招攬獎勵統計明細處理程式  110/10/15 V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTempThisMonth = "";
    String hTempLastMonth = "";
    String hEmplEmployNo = "";
    String hEmplUnitNo = "";
    String hEmplId = "";
    String hEmplChiName = "";
    int hMidlTotalCardCnt = 0;
    int hTempCurrentCnt = 0;
    int hTempNewCardCnt = 0;
    String hCardIssueDate = "";
    String hCardCardNo = "";
    String hCardStaticMonth = "";
    String hCardIdPSeqno = "";
    String hMilgProgramCode = "";
    double hMifdFeedbackRate = 0;    
    

    String hMilgConsumeFlage = "";
    int    hMilgCurrMonth = 0;
    int    hMilgNextMonth = 0;
    String hMilgCurrTotCond = "";
    double hMilgCurrAmt = 0;
    int    hMilgCurrTotCnt = 0; 
    String hMilgExcludeBank = "";
    String hMilgExcludeFinance = "";
    
    String hMilgSaleCond = ""; 
    int    hMilgConsumeCnt = 0;
    int    hMilgCurrentCnt = 0; 

    
    String hMifdFeedbackType = "";
    double hMifdFeedbackAmt = 0;
    double hMifdFeedbackScore = 0;
    double hMifdFeedbackScoreAmt = 0;   
    

    double hMifdPurchaseScore = 0;    
    
    String hMilgVdFlag = "";
    double hMilgDestinationAmt = 0;
    int hMilgDestinationCnt = 0;
    double hMictPurchaseAmt = 0;
    double hMictRefundAmt = 0;

    int    hMictPurchaseCnt = 0;
    double hMictFeedbackAmt = 0;       
    
    int hMifdNewcardCntMonth = 0;
    int hTempCnt = 0;
    String hMictNewCardFlag = "";
    int hMidlNewCardhdrCnt = 0;
    int hMidlTotalVdCardCnt = 0;
    int hMidlUseCardCnt = 0;
    int hMidlUseVdCardCnt = 0;
    double hMidlUseCardAmt = 0;
    double hMidlUseVdCardAmt = 0;
    double hMidlFeedbackAmt = 0;
    double hMidlFeedbackVdAmt = 0;
    double hMidlTotalFeedbackAmt = 0;

    long hTempCardCnt = 0;
    long[] aMTempCardCnt = new long[250];
    long[] aMTempCurrentCnt = new long[250];
    long[] aMTempNewCardCnt = new long[250];
    long totalCnt = 0;

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
                comc.errExit("Usage : MktB110", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            // h_call_batch_seqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            // comcr.h_call_batch_seqno = h_call_batch_seqno;
            // comcr.h_call_r_program_code = javaProgram;
            //
            // comcr.callbatch(0, 0, 0);

            if (args.length == 1) 
                hBusiBusinessDate = args[0];
            selectPtrBusinday();
            if(execute_chk()==0) {
                exceptExit = 0;
                comcr.errRtn(String.format("本日無獎勵專案執行[%s]!", hBusiBusinessDate), "", hCallBatchSeqno);
            }
//            if (!hBusiBusinessDate.substring(6).equals("01")) {
//                exceptExit = 0;
//                comcr.errRtn(String.format("本程式只在每月一日執行[%s]!", hBusiBusinessDate), "", hCallBatchSeqno);
//            }

            if (selectMktIntrDetail() != 0) {
                exceptExit = 0;
                comcr.errRtn(String.format("本月獎勵已轉基金不可再次執行!"), "", hCallBatchSeqno);
            }
         
            updateMktIntrLog1(); 
            deleteMktIntrDetail(); 
            deleteMktIntrCardlist();  
            commitDataBase();

            showLogMessage("I", "", String.format("Processing Static month [%s]", hTempThisMonth));
            totalCnt = 0;
            showLogMessage("I", "", String.format("Processing use card data"));
            selectMktIntrLog();
            showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));
            totalCnt = 0;
            showLogMessage("I", "", String.format("Processing new card data"));
            selectCrdCard();
            showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));
            totalCnt = 0;
            showLogMessage("I", "", String.format("Processing new VD card data"));
            selectDbcCard();
            showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));

            // ==============================================
            // 固定要做的
            // comcr.callbatch(1, 0, 0);
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
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,?) h_busi_business_date,";
        sqlCmd += " to_char(add_months(to_date(decode(cast(? as varchar(8)),'',business_date, ?),'yyyymmdd'),-1),'yyyymm') h_temp_this_month,";
        sqlCmd += " to_char(add_months(to_date(decode(cast(? as varchar(8)),'',business_date, ?),'yyyymmdd'),-2),'yyyymm') h_temp_last_month ";
        sqlCmd += "  from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        setString(5, hBusiBusinessDate);
        setString(6, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
            hTempThisMonth = getValue("h_temp_this_month");
            hTempLastMonth = getValue("h_temp_last_month");
        }

    }

    /***********************************************************************/
    int selectMktIntrDetail() throws Exception {
        hTempCnt = 0;

        sqlCmd = "select count(*) h_temp_cnt ";
        sqlCmd += " from mkt_intr_detail  ";
        sqlCmd += "where static_month = ?  ";
        sqlCmd += "  and proc_flag = 'Y'  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTempThisMonth);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_mkt_intr_detail not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempCnt = getValueInt("h_temp_cnt");
        }
        return (hTempCnt);
    }

    /***********************************************************************/
    void updateMktIntrLog1() throws Exception {
        daoTable = "mkt_intr_log";
        updateSQL = "static_month = '',";
        updateSQL += " proc_flag = 'N',";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm  = 'MktB110'";
        whereStr = "where acct_month = ? ";
//      whereStr = "where static_month = ? ";
        setString(1, hTempThisMonth);
        updateTable();

    }

    /***********************************************************************/
    void deleteMktIntrDetail() throws Exception {
        daoTable = "mkt_intr_detail";
        whereStr = "where static_month = ? ";
        setString(1, hTempThisMonth);
        deleteTable();

    }

    /***********************************************************************/
    void deleteMktIntrCardlist() throws Exception {
        daoTable = "mkt_intr_cardlist";
        whereStr = "where static_month = ? ";
        setString(1, hTempThisMonth);
        deleteTable();

    }

    /***********************************************************************/
    void selectMktIntrLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.program_code,";
        sqlCmd += " b.id,";
        sqlCmd += " c.feedback_type h_mifd_feedback_type,";
        sqlCmd += " c.feedback_amt h_mifd_feedback_amt,";
        sqlCmd += " c.feedback_score h_mifd_feedback_score,";
        sqlCmd += " c.feedback_score_amt h_mifd_feedback_score_amt,";  
        sqlCmd += " c.consume_flag h_mifd_consume_flag,";
        sqlCmd += " c.curr_month h_mifd_curr_month,";
        sqlCmd += " c.next_month h_mifd_next_month,";
        sqlCmd += " c.curr_tot_cond h_mifd_curr_tot_cond,";
        sqlCmd += " c.curr_amt h_mifd_curr_amt,";
        sqlCmd += " c.curr_tot_cnt h_mifd_curr_tot_cnt,";    
        sqlCmd += " c.exclude_bank h_mifd_exclude_bank,";    
        sqlCmd += " c.exclude_finance h_mifd_exclude_finance,";   
        sqlCmd += " c.sale_cond h_mifd_sale_cond,";   
        sqlCmd += " c.consume_cnt h_mifd_consume_cnt,";   
        sqlCmd += " c.current_cnt h_mifd_current_cnt,";           
        sqlCmd += " min(b.unit_no) h_empl_unit_no,";
        sqlCmd += " min(b.employ_no) h_empl_employ_no,";
        sqlCmd += " min(b.chi_name) h_empl_chi_name,";
        sqlCmd += " min(c.feedback_rate) h_mifd_feedback_rate ";
        sqlCmd += " from mkt_intr_log a,crd_employee b,mkt_intr_fund c ";
        sqlCmd += " where a.program_code = c.program_code ";
        sqlCmd += "  and b.status_id in ('1','7') ";
        sqlCmd += "  and a.employ_no = b.employ_no ";
        sqlCmd += "  and a.acct_month <= ? ";
        sqlCmd += "  and a.proc_flag = 'N' ";
        sqlCmd += "  and ? between decode(c.apply_date_s,'','20100101',c.apply_date_s) and decode(c.apply_date_e,'','30001231',c.apply_date_e) ";
        sqlCmd += "  AND substr( ? ,7,2) = lpad(c.CAL_DATE_DAYS,2,'0')  ";
        sqlCmd += "  and c.REWARD_BANK='Y' ";
        sqlCmd += "  and c.apr_flag='Y' ";
//        sqlCmd += "  and ? between c.apply_date_s and decode(c.apply_date_e,'','30001231',c.apply_date_e) ";
//        sqlCmd += "group by a.program_code,b.id ";
        sqlCmd += "group by a.program_code,b.id ,c.feedback_type,c.feedback_amt,c.feedback_score ,c.feedback_score_amt,";
        sqlCmd += "c.consume_flag,c.curr_month,c.next_month,c.curr_tot_cond,c.curr_amt,c.curr_tot_cnt,c.exclude_bank,c.exclude_finance,c.sale_cond,c.consume_cnt,c.current_cnt ";
        setString(1, hTempThisMonth);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hMilgProgramCode = getValue("program_code");
            hEmplId = getValue("id");
            hEmplUnitNo = getValue("h_empl_unit_no");
            hEmplEmployNo = getValue("h_empl_employ_no");
            hEmplChiName = getValue("h_empl_chi_name");
            hMifdFeedbackRate = getValueDouble("h_mifd_feedback_rate");            
            hMifdFeedbackType = getValue("h_mifd_feedback_type");
            hMifdFeedbackAmt = getValueDouble("h_mifd_feedback_amt");
            hMifdFeedbackScore  = getValueDouble("h_mifd_feedback_score");
            hMifdFeedbackScoreAmt = getValueDouble("h_mifd_feedback_score_amt");            
       	    hMilgConsumeFlage = getValue("h_mifd_consume_flag");
       	    hMilgCurrMonth = getValueInt("h_mifd_curr_month");
            hMilgNextMonth = getValueInt("h_mifd_next_month");
       	    hMilgCurrTotCond = getValue("h_mifd_curr_tot_cond");
       	    hMilgCurrAmt = getValueDouble("h_mifd_curr_amt");
       	    hMilgCurrTotCnt = getValueInt("h_mifd_curr_tot_cnt"); 
       	    hMilgExcludeBank = getValue("h_mifd_exclude_bank");
       	    hMilgExcludeFinance = getValue("h_mifd_exclude_finance");
          	hMilgSaleCond  = getValue("h_mifd_sale_cond");
       	    hMilgConsumeCnt = getValueInt("h_mifd_consume_cnt"); 
       	    hMilgCurrentCnt = getValueInt("h_mifd_current_cnt"); 
            
            totalCnt++;
            if ((totalCnt % 1000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }

            hMidlUseCardCnt = 0;
            hMidlUseCardAmt = 0;
            hMidlUseVdCardCnt = 0;
            hMidlUseVdCardAmt = 0;
            hMidlTotalFeedbackAmt = 0;
            hMidlFeedbackAmt = 0;
            hMidlFeedbackVdAmt = 0;
            hMidlNewCardhdrCnt = 0;
            hMidlTotalCardCnt = 0;

            selectMktIntrLog1();
            if (hMidlTotalFeedbackAmt <= 0)
                continue;
                  
            insertMktIntrDetail();
//            updateMktIntrLog();
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void selectMktIntrLog1() throws Exception {
        long tempLong = 0;
   
        sqlCmd = "select ";
        sqlCmd += " a.card_no,";
        sqlCmd += " min(a.vd_flag) h_milg_vd_flag,";
        sqlCmd += " min(a.issue_date) h_card_issue_date,";
        sqlCmd += " sum(a.dest_amt) h_milg_destination_amt,";
        sqlCmd += " sum(a.dest_cnt) h_milg_destination_cnt,";
        sqlCmd += " sum(decode(sign(a.dest_amt),1,a.dest_amt,0)) h_mict_purchase_amt,";
        sqlCmd += " sum(decode(sign(a.dest_amt),-1,0-a.dest_amt,0)) h_mict_refund_amt ";
        sqlCmd += "from mkt_intr_log a ";
        sqlCmd += "where a.program_code = ? ";
        sqlCmd += "  and a.employ_no = ? ";
        sqlCmd += "  and a.acct_month <= ? ";
        sqlCmd += "  and a.proc_flag = 'N' ";
        sqlCmd += "  and a.acct_month <= decode(?,'2',to_char(add_months(to_date(a.issue_date,'yyyymmdd'),12-integer(substr(a.issue_date,5,2))+ ? ),'yyyymm'),to_char(add_months(to_date(a.issue_date,'yyyymmdd'), ? -1),'yyyymm')) ";        
        sqlCmd += "group by a.card_no ";
        setString(1, hMilgProgramCode);
        setString(2, hEmplEmployNo);
        setString(3, hTempThisMonth);
        setString(4, hMilgConsumeFlage);
        if (hMilgConsumeFlage.equals("2") &&  hMilgNextMonth==0) {
        	hMilgNextMonth = 1;
        }
        if (hMilgConsumeFlage.equals("1") && hMilgCurrMonth==0) {
        	hMilgCurrMonth = 1;
        }
        setInt(5, hMilgNextMonth);
        setInt(6, hMilgCurrMonth);
        int recordCnt = selectTable();
               
        for (int i = 0; i < recordCnt; i++) {
            hCardCardNo = getValue("card_no", i);
            hMilgVdFlag = getValue("h_milg_vd_flag", i);
            hCardIssueDate = getValue("h_card_issue_date", i);
            hMilgDestinationAmt = getValueDouble("h_milg_destination_amt", i);
            hMilgDestinationCnt = getValueInt("h_milg_destination_cnt", i);
            hMictPurchaseAmt = getValueDouble("h_mict_purchase_amt", i);
            hMictRefundAmt = getValueDouble("h_mict_refund_amt", i);
            
            
 	       if ( selectCardlistCardNo(hCardCardNo) > 0 ) {
 	    	   continue;
 	       }
           
 	       //EXCLUDE_BANK
 	       if (hMilgExcludeBank.equals("Y")) {
 	    	  if ( selectExcludeBank(hMilgVdFlag,hCardCardNo) > 0 ) {
 	 	    	   continue;
 	 	       }
 	       }
 	       //EXCLUDE_FINANCE
 	      if (hMilgExcludeFinance.equals("Y")) {
 	    	  if ( selectExcludeFinance(hMilgVdFlag,hCardCardNo) > 0 ) {
	 	    	   continue;
	 	       }
	       }
 	      
 	      //SALE_COND
 	      if (hMilgSaleCond.equals("Y")) {
 	    	  
 	    	  int hTempConsumeCnt = 0;
 	    	  int hTempCurrentCnt = 0;
 	    	 
 	    	  hTempConsumeCnt = selectConsumeCnt(hEmplEmployNo);
 	    	  hTempCurrentCnt = selectCurrentCnt(hEmplEmployNo); 	    	 
 	    	  if (hTempConsumeCnt < hMilgConsumeCnt || hTempCurrentCnt < hMilgCurrentCnt) {
	 	    	   continue;
	 	       }
	       } 	       
 	       hMictFeedbackAmt = 0;
 	       hMictPurchaseCnt = 0;
 	      
            switch (hMifdFeedbackType) {
			case "1":
				tempLong = (long) ((hMilgDestinationAmt * hMifdFeedbackRate) / 100.0 + 0.5);
				break;
			case "2":
				tempLong = (long) hMifdFeedbackAmt ;
				break;
			case "3":
				tempLong = (long) (hMifdFeedbackScore * hMifdFeedbackScoreAmt) ;
				break;
            }
//            tempLong = (long) ((hMilgDestinationAmt * hMifdFeedbackRate) / 100.0 + 0.5);
            
             
            if (tempLong <= 0) {
               continue;
            }
            hMictFeedbackAmt = tempLong; 
            hMidlTotalFeedbackAmt = hMidlTotalFeedbackAmt + tempLong;            

	        if (hMilgCurrTotCond.equals("Y") ) {	            	
	           if(hMilgDestinationAmt <  hMilgCurrAmt && hMilgDestinationCnt < hMilgCurrTotCnt ) {
	        	  continue;
	           }
	            	
	        }else {	            	
	           if(hMilgDestinationAmt <  hMilgCurrAmt   ) {
	        	  continue;
	           }	            	
	        }
	        
	        hMictPurchaseCnt = selectMktIntrLogCrdRowCount(hCardCardNo);
	        hMifdPurchaseScore = hMifdPurchaseScore + hMifdFeedbackScore ;
	        
            if (hMilgVdFlag.equals("N")) {
                hMidlUseCardAmt = hMidlUseCardAmt + hMilgDestinationAmt;
                hMidlUseCardCnt++;
                hMidlFeedbackAmt = hMidlFeedbackAmt + tempLong;
            } else {
                hMidlUseVdCardAmt = hMidlUseVdCardAmt + hMilgDestinationAmt;
                hMidlUseVdCardCnt++;
                hMidlFeedbackVdAmt = hMidlFeedbackVdAmt + tempLong;
            }
            
            //Max static_month
            selectStaticMonth(hCardCardNo);
         
            hMictNewCardFlag = "N";
            
            insertMktIntrCardlist();
            updateMktIntrLog1(hCardCardNo);
        }

    }
  
    /***********************************************************************/
    void updateMktIntrLog() throws Exception {
        daoTable = "mkt_intr_log";
        updateSQL = "proc_flag = '1',";
        updateSQL += " proc_date = ?,";
        updateSQL += " static_month = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm = 'MktB110'";
        whereStr = "where employ_no = ?  ";
        whereStr += "and acct_month <= ?  ";
        whereStr += "and proc_flag = 'N'  ";
        whereStr += "and decode(program_code,'','x',program_code) = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hTempThisMonth);
        setString(3, hEmplEmployNo);
        setString(4, hTempThisMonth);
        setString(5, hMilgProgramCode);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_mkt_intr_log not found!", "", hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    void updateMktIntrLog1(String cardNo) throws Exception {
        daoTable = "mkt_intr_log";
        updateSQL = "proc_flag = '1',";
        updateSQL += " proc_date = ?,";
        updateSQL += " static_month = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm = 'MktB110'";
        whereStr = "where employ_no = ?  ";
        whereStr += "and acct_month <= ?  ";
        whereStr += "and proc_flag = 'N'  ";
        whereStr += "and decode(program_code,'','x',program_code) = ? ";
        whereStr += "and card_no = ? ";
        whereStr += "and acct_month <= decode(?,'2',to_char(add_months(to_date(issue_date,'yyyymmdd'),12-integer(substr(issue_date,5,2))+ ? ),'yyyymm'),to_char(add_months(to_date(issue_date,'yyyymmdd'), ? -1),'yyyymm')) ";        
       
        setString(1, hBusiBusinessDate);
//        setString(2, hTempThisMonth);
        setString(2, hCardStaticMonth);
        setString(3, hEmplEmployNo);
        setString(4, hTempThisMonth);
        setString(5, hMilgProgramCode);
        setString(6, cardNo);        
        setString(7, hMilgConsumeFlage);
        if (hMilgConsumeFlage.equals("2") &&  hMilgNextMonth==0) {
        	hMilgNextMonth = 1;
        }
        if (hMilgConsumeFlage.equals("1") && hMilgCurrMonth==0) {
        	hMilgCurrMonth = 1;
        }
        setInt(8, hMilgNextMonth);
        setInt(9, hMilgCurrMonth); 
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_mkt_intr_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " b.employ_no,";
        sqlCmd += " min(b.unit_no) h_empl_unit_no,";
        sqlCmd += " min(b.id) h_empl_id,";
        sqlCmd += " min(b.chi_name) h_empl_chi_name,";
        sqlCmd += " count(*) h_midl_total_card_cnt,";
        sqlCmd += " sum(decode(a.current_code,'0',1,0)) h_temp_current_cnt,";
        sqlCmd += " sum(decode(sign(a.issue_date-( ?||'00')),1,1,0)) h_temp_new_card_cnt ";
        sqlCmd += " from crd_card a,crd_employee b ";
//        sqlCmd += "where a.promote_emp_no = b.employ_no ";
        sqlCmd += "where a.introduce_emp_no = b.employ_no ";
        sqlCmd += "  and length(trim(a.introduce_emp_no)) > 0   "; 
        sqlCmd += "  and b.status_id in ('1','7') ";
        sqlCmd += "  and a.issue_date <= ? || '31' ";
        sqlCmd += "group by b.employ_no ";
        setString(1, hTempThisMonth);
        setString(2, hTempThisMonth);
        hCardStaticMonth = hTempThisMonth;
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hEmplEmployNo = getValue("employ_no");
            hEmplUnitNo = getValue("h_empl_unit_no");
            hEmplId = getValue("h_empl_id");
            hEmplChiName = getValue("h_empl_chi_name");
            hMidlTotalCardCnt = getValueInt("h_midl_total_card_cnt");
            hTempCurrentCnt = getValueInt("h_temp_current_cnt");
            hTempNewCardCnt = getValueInt("h_temp_new_card_cnt");

            totalCnt++;
            if ((totalCnt % 1000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }
            hMidlNewCardhdrCnt = 0;
            if (hTempNewCardCnt > 0)
                selectCrdCard1();

            hMidlUseVdCardCnt = 0;
            hMidlUseCardCnt = 0;
            hMidlUseVdCardAmt = 0;
            hMidlUseCardAmt = 0;
            hMidlFeedbackAmt = 0;
            hMidlFeedbackVdAmt = 0;
            hMidlTotalFeedbackAmt = 0;
            hMifdFeedbackRate = 0;            
            hMifdFeedbackType = "";
            hMifdFeedbackAmt = 0;
            hMifdFeedbackScore = 0;
            hMifdFeedbackScoreAmt = 0;            
            hMifdPurchaseScore = 0; 

            if (hMidlNewCardhdrCnt > 0) {
                selectMktIntrFund(1);   
            } else {
                if (hTempCurrentCnt > 0)
                    selectMktIntrFund(3); 
            }
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void selectCrdCard1() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.issue_date,";
        sqlCmd += " a.card_no,";
        sqlCmd += " a.id_p_seqno ";
        sqlCmd += "from crd_card a ";
//        sqlCmd += "where a.promote_emp_no = ? ";
        sqlCmd += "where a.introduce_emp_no = ? ";
        sqlCmd += "  and length(trim(a.introduce_emp_no)) > 0 ";
        sqlCmd += "  and decode(a.issue_date,'','x',a.issue_date) <= ? || '31' ";
//        sqlCmd += "  and decode(a.issue_date,'','x',a.issue_date)  > ? || '01'  ";
        sqlCmd += "  and decode(a.issue_date,'','x',a.issue_date)  >= ? || '01'  ";
//        sqlCmd += "  and old_card_no = '' ";
        sqlCmd += "  and a.old_card_no = '' ";
        sqlCmd += "  and a.current_code='0' ";
        setString(1, hEmplEmployNo);
        setString(2, hTempThisMonth);
        setString(3, hTempThisMonth);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCardIssueDate = getValue("issue_date", i);
            hCardCardNo = getValue("card_no", i);
            hCardIdPSeqno = getValue("id_p_seqno", i);

            if (selectCrdCard2() != 0)
                continue;
            hMidlNewCardhdrCnt++;
            hMictPurchaseAmt = 0;
            hMictRefundAmt = 0;
            hMictPurchaseCnt = 0 ;
            hMifdFeedbackScore = 0 ;
            hMifdFeedbackType = "";
            hMictFeedbackAmt =0 ;                        
            hMictNewCardFlag = "Y";
            selectMktIntrFund(2); 
        }

    }

    /***********************************************************************/
    int selectCrdCard2() throws Exception {
        hTempCnt = 0;

        sqlCmd = "select count(*) h_temp_cnt ";
        sqlCmd += " from crd_card h  ";
        sqlCmd += "where h.id_p_seqno = ?  ";
        sqlCmd += "  and decode(h.issue_date,'','x',h.issue_date) < ?  ";
        sqlCmd += "  and decode(h.oppost_date, '', '30001231', h.oppost_date) >= to_char(add_months(to_date(?,'yyyymmdd'),-1*?),'yyyymmdd')  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hCardIdPSeqno);
        setString(2, hCardIssueDate);
        setString(3, hCardIssueDate);
        setInt(4, hMifdNewcardCntMonth);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_card_2 not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempCnt = getValueInt("h_temp_cnt");
        }
        return (hTempCnt);
    }

    /***********************************************************************/
    void selectDbcCard() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " b.employ_no,";
        sqlCmd += " min(b.unit_no) h_empl_unit_no,";
        sqlCmd += " min(b.id) h_empl_id,";
        sqlCmd += " min(b.chi_name) h_empl_chi_name,";
        sqlCmd += " count(*) h_midl_total_card_cnt,";
        sqlCmd += " sum(decode(sign(substr(a.issue_date,1,6)-(?)),1,1,0)) h_temp_new_card_cnt ";
        sqlCmd += "from dbc_card a,crd_employee b ";
//        sqlCmd += "where a.promote_emp_no = b.employ_no ";
        sqlCmd += "where a.introduce_emp_no = b.employ_no ";
        sqlCmd += "  and length(trim(a.introduce_emp_no)) > 0 ";
        sqlCmd += "  and b.status_id in ('1','7') ";
        sqlCmd += "  and a.current_code = '0' ";
        sqlCmd += "  and a.issue_date <= ?||'31' ";   
        sqlCmd += "group by b.employ_no ";
        setString(1, hTempThisMonth);
        setString(2, hTempThisMonth);
        hCardStaticMonth = hTempThisMonth;
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hEmplEmployNo = getValue("employ_no");
            hEmplUnitNo = getValue("h_empl_unit_no");
            hEmplId = getValue("h_empl_id");
            hEmplChiName = getValue("h_empl_chi_name");
            hMidlTotalVdCardCnt = getValueInt("h_midl_total_card_cnt");
            hTempNewCardCnt = getValueInt("h_temp_new_card_cnt");
            hMidlTotalCardCnt = 0;

            totalCnt++;
            if ((totalCnt % 1000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }
            hMidlNewCardhdrCnt = 0;
            if (hTempNewCardCnt > 0)
                selectDbcCard1();

            hMidlUseVdCardCnt = 0;
            hMidlUseCardCnt = 0;
            hMidlUseCardAmt = 0;
            hMidlUseVdCardAmt = 0;
            hMidlFeedbackAmt = 0;
            hMidlFeedbackAmt = 0;
            hMifdFeedbackRate = 0;
            hMifdPurchaseScore = 0; 

            selectMktIntrFund(3); 
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void selectDbcCard1() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.issue_date,";
        sqlCmd += " a.card_no,";
        sqlCmd += " a.id_p_seqno ";
        sqlCmd += "from dbc_card a ";
//        sqlCmd += "where a.promote_emp_no = ? ";
        sqlCmd += "where a.introduce_emp_no = ? ";
        sqlCmd += "  and length(trim(a.introduce_emp_no)) > 0 ";
        sqlCmd += "  and a.issue_date like ? || '%' ";
        sqlCmd += "  and a.current_code = '0' ";
        sqlCmd += "  and old_card_no = '' ";
        setString(1, hEmplEmployNo);
        setString(2, hTempThisMonth);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCardIssueDate = getValue("issue_date", i);
            hCardCardNo = getValue("card_no", i);
            hCardIdPSeqno = getValue("id_p_seqno", i);

            if (selectDbcCard2() != 0)
                continue;
            hMidlNewCardhdrCnt++;
            hMictPurchaseAmt = 0;
            hMictRefundAmt = 0; 
            hMictPurchaseCnt = 0 ;
            hMifdFeedbackScore = 0 ;
            hMifdFeedbackType = "";
            hMictFeedbackAmt =0 ;            
            hMictNewCardFlag = "Y";
            selectMktIntrFund(2); 
        }
    }

    /***********************************************************************/
    int selectDbcCard2() throws Exception {
        hTempCnt = 0;

        sqlCmd = "select count(*) h_temp_cnt ";
        sqlCmd += " from dbc_card h  ";
        sqlCmd += "where h.id_p_seqno = ?  ";
        sqlCmd += "  and decode(h.issue_date,'','x',h.issue_date) < ?  ";
        sqlCmd += "  and decode(h.oppost_date, '', '30001231', h.oppost_date) >= to_char(add_months(to_date(?,'yyyymmdd'),-1*?),'yyyymmdd')  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hCardIdPSeqno);
        setString(2, hCardIssueDate);
        setString(3, hCardIssueDate);
        setInt(4, hMifdNewcardCntMonth);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dbc_card_2 not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempCnt = getValueInt("h_temp_cnt");
        }

        return (hTempCnt);
    }

    /***********************************************************************/
    void selectMktIntrFund(int hInt1) throws Exception {

        sqlCmd = "select ";
        sqlCmd += " program_code,";
        sqlCmd += " newcard_cnt_month ";
        sqlCmd += "from mkt_intr_fund ";
        sqlCmd += "where ? between apply_date_s and decode(apply_date_e,'','30001231',apply_date_e) ";
        sqlCmd += " and apr_flag='Y' ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hMilgProgramCode = getValue("program_code", i);
            hMifdNewcardCntMonth = getValueInt("newcard_cnt_month", i);

            if (hInt1 == 1)
                if (insertMktIntrDetail() != 0)
                    updateMktIntrDetail(); 
            		  
            if (hInt1 == 2)  
                if (insertMktIntrCardlist() != 0)    
                    updateMktIntrCardlist();  
            if (hInt1 == 3)             	
                   updateMktIntrDetail(); 
            		
        }

    }

    /***********************************************************************/
    int insertMktIntrDetail() throws Exception {
//        setValue("static_month", hTempThisMonth);
        setValue("static_month", hCardStaticMonth);
        setValue("program_code", hMilgProgramCode);
        setValue("unit_no", hEmplUnitNo);
        setValue("employ_id", hEmplId);
        setValue("employ_no", hEmplEmployNo);
        setValue("chi_name", hEmplChiName);
        setValueInt("new_cardhdr_cnt", hMidlNewCardhdrCnt);
        setValueInt("total_card_cnt", hMidlTotalCardCnt);
        setValueInt("total_vd_card_cnt", hMidlTotalVdCardCnt);
        setValueInt("use_card_cnt", hMidlUseCardCnt);
        setValueInt("use_vd_card_cnt", hMidlUseVdCardCnt);
        setValueDouble("use_card_amt", hMidlUseCardAmt);
        setValueDouble("use_vd_card_amt", hMidlUseVdCardAmt);
        setValueDouble("feedback_amt", hMidlFeedbackAmt);
        setValueDouble("feedback_vd_amt", hMidlFeedbackVdAmt);
        setValueDouble("total_feedback_amt", hMidlTotalFeedbackAmt);
        setValueDouble("feedback_rate", hMifdFeedbackRate);        
        setValueDouble("purchase_score", hMifdPurchaseScore);   
        setValue("crt_date", hBusiBusinessDate);
        setValue("crt_time", sysTime);     
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        setValueInt("newcard_cnt_month", hMifdNewcardCntMonth);
        daoTable = "mkt_intr_detail";
        insertTable();
        if (dupRecord.equals("Y")) {
            return -1;
        }
        return 0;
    }

    /***********************************************************************/
    int insertMktIntrCardlist() throws Exception {
//        setValue("static_month", hTempThisMonth);
        setValue("static_month", hCardStaticMonth);
        setValue("program_code", hMilgProgramCode);
        setValue("unit_no", hEmplUnitNo);
        setValue("employ_id", hEmplId);
        setValue("employ_no", hEmplEmployNo);
        setValue("chi_name", hEmplChiName);
        setValue("card_no", hCardCardNo);
        setValue("issue_date", hCardIssueDate);
        setValueDouble("purchase_amt", hMictPurchaseAmt);
        setValueDouble("refund_amt", hMictRefundAmt); 
        setValueInt("purchase_cnt", hMictPurchaseCnt);
        setValueDouble("purchase_score", hMifdFeedbackScore);
        setValue("feedback_type", hMifdFeedbackType);
        setValueDouble("feedback_amt", hMictFeedbackAmt);        
        setValue("new_card_flag", hMictNewCardFlag);
        setValue("crt_date", hBusiBusinessDate);
        setValue("crt_time", sysTime);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "mkt_intr_cardlist";
        insertTable();
        if (dupRecord.equals("Y")) {
            return -1;
        }

        return 0;
    }

    /***********************************************************************/
    void updateMktIntrCardlist() throws Exception {
        daoTable = "mkt_intr_cardlist";
        updateSQL = "new_card_flag = 'Y',";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm  = 'MktB110'";
        whereStr = "where static_month = ?  ";
        whereStr += "and program_code = ?  ";
        whereStr += "and employ_no  = ?  ";
        whereStr += "and card_no  = ? ";
//        setString(1, hTempThisMonth);
        setString(1, hCardStaticMonth);
        setString(2, hMilgProgramCode);
        setString(3, hEmplEmployNo);
        setString(4, hCardCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_mkt_intr_cardlist not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateMktIntrDetail() throws Exception {
        daoTable = "mkt_intr_detail";
        updateSQL = "new_cardhdr_cnt = new_cardhdr_cnt + ?,";
        updateSQL += " total_card_cnt = total_card_cnt + ?,";
        updateSQL += " total_vd_card_cnt = total_vd_card_cnt + ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm   = 'MktB110',";
        updateSQL += " newcard_cnt_month = ?";
        whereStr = "where static_month = ?  ";
        whereStr += "and program_code = ?  ";
        whereStr += "and employ_no  = ? ";
        setInt(1, hMidlNewCardhdrCnt);
        setInt(2, hMidlTotalCardCnt);
        setInt(3, hMidlTotalVdCardCnt);
        setInt(4, hMifdNewcardCntMonth);
        if (hCardStaticMonth.equals(""))
        	hCardStaticMonth = hTempThisMonth;
//        setString(5, hTempThisMonth);
        setString(5, hCardStaticMonth);
        setString(6, hMilgProgramCode);
        setString(7, hEmplEmployNo);
        updateTable();
        if (notFound.equals("Y"))
            insertMktIntrDetail();
    }
  
    /***********************************************************************/
   
    int selectMktIntrDetail1() throws Exception {
        hTempCnt = 0;
                 
        sqlCmd = "select max(static_month) static_month, count(*) h_temp_cnt ";
        sqlCmd += " from mkt_intr_detail  ";
        sqlCmd += "where program_code = ?  ";
        sqlCmd += "  and employ_no = ?  ";
        setString(1, hMilgProgramCode);
        setString(2, hEmplEmployNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_mkt_intr_detail not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempCnt = getValueInt("h_temp_cnt");
            hCardStaticMonth = getValue("static_month");
        }
        return (hTempCnt);
    }
   
    /***********************************************************************/
    
    int execute_chk() throws Exception {
    	
        hTempCnt = 0;
        String calDateType="";

        sqlCmd = "select cal_date_type ";
        sqlCmd += " from mkt_intr_fund  ";
        sqlCmd += " WHERE apr_flag='Y'  ";
        sqlCmd += " AND ? <= to_char(add_months(to_date(decode(apply_date_e,'','30001231',apply_date_e),'yyyymmdd'),12),'yyyymmdd')   ";
        sqlCmd += " AND substr(?,7,2) = lpad(CAL_DATE_DAYS,2,'0') ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        openCursor();
        while (fetchTable()) {
        	if (hTempCnt == 1) {
        		break;
        	}
        	calDateType = getValue("cal_date_type");
			 switch (calDateType) {
	         case "1": //M
	        	 hTempCnt = 1;
	        	 break; 
	         case "2": //Q(1,4,7,10)
	        	if(hBusiBusinessDate.substring(4,6).equals("01")) {		                   
	        	     hTempCnt = 1;
	        	 }else if (hBusiBusinessDate.substring(4,6).equals("04")) {		                   
		             hTempCnt = 1;
	        	 }else if (hBusiBusinessDate.substring(4,6).equals("07")) {		                   
			         hTempCnt = 1;	   
	        	 }else if (hBusiBusinessDate.substring(4,6).equals("10")) {		                   
			         hTempCnt = 1;	   
	        	 }else {
	        		 hTempCnt = 0;	 
                 }
	        	 break; 
	         case "3": //H(1,7)
	        	if(hBusiBusinessDate.substring(4,6).equals("01")) {		                   
	 	             hTempCnt = 1;
	 	        }else if (hBusiBusinessDate.substring(4,6).equals("07")) {		                   
	 			     hTempCnt = 1;	   
	 	        }else {
	 	             hTempCnt = 0;	 
	            }
	        	 break; 
	         case "4"://Y(2)
		        if(hBusiBusinessDate.substring(4,6).equals("02")) {		                   
		 	         hTempCnt = 1;
			    }else {
			 	     hTempCnt = 0;	 
			    }
	        	break; 
			 }
        }
        closeCursor();
        return hTempCnt ;
    }

    /***********************************************************************/
    void selectStaticMonth(String cardNo) throws Exception {        
        
        sqlCmd = "select ";
        sqlCmd += " a.acct_month,";
        sqlCmd += " a.dest_amt,";
        sqlCmd += " a.dest_cnt";        
        sqlCmd += " from mkt_intr_log a ";
        sqlCmd += " where a.program_code = ? ";
        sqlCmd += "  and a.employ_no = ? ";
        sqlCmd += "  and a.acct_month <= ? ";
        sqlCmd += "  and a.card_no = ? ";
        sqlCmd += "  and a.proc_flag = 'N' ";
        sqlCmd += "  and a.acct_month <= decode(?,'2',to_char(add_months(to_date(a.issue_date,'yyyymmdd'),12-integer(substr(a.issue_date,5,2))+ ? ),'yyyymm'),to_char(add_months(to_date(a.issue_date,'yyyymmdd'), ? -1),'yyyymm')) ";        
        sqlCmd += "ORDER BY a.acct_month ";
        setString(1, hMilgProgramCode);
        setString(2, hEmplEmployNo);
        setString(3, hTempThisMonth);
        setString(4, cardNo);
        setString(5, hMilgConsumeFlage);
        if (hMilgConsumeFlage.equals("2") &&  hMilgNextMonth==0) {
        	hMilgNextMonth = 1;
        }
        if (hMilgConsumeFlage.equals("1") && hMilgCurrMonth==0) {
        	hMilgCurrMonth = 1;
        }
        setInt(6, hMilgNextMonth);
        setInt(7, hMilgCurrMonth);
        
        
        int recordCnt = selectTable();
        hCardStaticMonth = hTempThisMonth ;
        
        String hTempAcctMonth ="";
        double  hTempDestAmt = 0;
        int hTempDestCnt = 0;        
        double  hStackDestAmt = 0;
        int hStackDestCnt = 0;
        
        for (int j = 0; j < recordCnt; j++) {        	
                   	
        	hTempAcctMonth = getValue("acct_month",j);
        	hTempDestAmt = getValueDouble("dest_amt",j);
        	hTempDestCnt = getValueInt("dest_cnt",j);
        	
        	hCardStaticMonth = hTempAcctMonth ;
        	hStackDestAmt = hStackDestAmt + hTempDestAmt;
        	hStackDestCnt = hStackDestCnt + hTempDestCnt;
        	
    	   if (hMilgCurrTotCond.equals("Y") ) {	     
    		   if(hMilgCurrTotCnt==0) {
    			   hMilgCurrTotCnt = 1;
    		   }
	           if(hStackDestAmt >= hMilgCurrAmt  ||  hStackDestCnt >= hMilgCurrTotCnt  ) {
	              return;
	           }
	            	
	        }else {	            	
	           if(hStackDestAmt >= hMilgCurrAmt   ) {
	              return;
	           }	            	
	        }
        	           	   
        }
       
    }
    /***********************************************************************/
   int selectCardlistCardNo(String cardNo) throws Exception {
    	
    	hTempCnt = 0;
        
        sqlCmd = "select count(*) h_temp_cnt ";
        sqlCmd += " from mkt_intr_cardlist  ";
        sqlCmd += "where card_no = ? ";      
        sqlCmd += "and feedback_amt > 0 ";      
        setString(1, cardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	comcr.errRtn("select_mkt_intr_cardlist not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempCnt = getValueInt("h_temp_cnt");
        }
        return (hTempCnt);               
    }
   
   /***********************************************************************/
   int selectExcludeBank(String vdFlag ,String cardNo) throws Exception {
    	
    	hTempCnt = 0;
        
        sqlCmd = "select count(*) h_temp_cnt ";
        sqlCmd += "  FROM (  ";
        sqlCmd += "     SELECT ci.ID_NO  ";
        sqlCmd += "       FROM CRD_CARD cc ,CRD_IDNO ci ,CRD_EMPLOYEE ce  ";
        sqlCmd += "      WHERE cc.ID_P_SEQNO =ci.ID_P_SEQNO ";
        sqlCmd += "        AND ci.ID_NO = ce.ID  ";
        sqlCmd += "        AND cc.CARD_NO = ?  ";
        sqlCmd += "        AND ci.STAFF_FLAG ='Y'  ";
        sqlCmd += "        AND ce.status_id in ('1','7')  ";
        sqlCmd += "        AND ? ='N'  ";
        sqlCmd += "     UNION ALL ";      
        sqlCmd += "     SELECT di.ID_NO  ";
        sqlCmd += "       FROM DBC_CARD dc ,DBC_IDNO di ,CRD_EMPLOYEE ce  ";
        sqlCmd += "      WHERE dc.ID_P_SEQNO =di.ID_P_SEQNO ";
        sqlCmd += "        AND di.ID_NO = ce.ID  ";
        sqlCmd += "        AND dc.CARD_NO = ?  ";
        sqlCmd += "        AND di.STAFF_FLAG ='Y'  ";
        sqlCmd += "        AND ce.status_id in ('1','7')  ";
        sqlCmd += "        AND ? ='Y' ) ";
        setString(1, cardNo);   
        setString(2, vdFlag);
        setString(3, cardNo);
        setString(4, vdFlag);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	comcr.errRtn("select_crd_employee not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempCnt = getValueInt("h_temp_cnt");
        }
        return (hTempCnt);               
    }
   /***********************************************************************/
   int selectExcludeFinance(String vdFlag ,String cardNo) throws Exception {
    	
    	hTempCnt = 0;
        
        sqlCmd = "select count(*) h_temp_cnt ";
        sqlCmd += "  FROM (  ";
        sqlCmd += "     SELECT ci.ID_NO  ";
        sqlCmd += "       FROM CRD_CARD cc ,CRD_IDNO ci ,CRD_EMPLOYEE_A cea  ";
        sqlCmd += "      WHERE cc.ID_P_SEQNO =ci.ID_P_SEQNO ";
        sqlCmd += "        AND ci.ID_NO = cea.ID  ";
        sqlCmd += "        AND cc.CARD_NO = ?  ";
        sqlCmd += "        AND ci.STAFF_FLAG ='Y'  ";
        sqlCmd += "        AND cea.status_id in ('1','7')  ";
        sqlCmd += "        AND ? ='N'  ";
        sqlCmd += "     UNION ALL ";      
        sqlCmd += "     SELECT di.ID_NO  ";
        sqlCmd += "       FROM DBC_CARD dc ,DBC_IDNO di ,CRD_EMPLOYEE_A cea  ";
        sqlCmd += "      WHERE dc.ID_P_SEQNO =di.ID_P_SEQNO ";
        sqlCmd += "        AND di.ID_NO = cea.ID  ";
        sqlCmd += "        AND dc.CARD_NO = ?  ";
        sqlCmd += "        AND di.STAFF_FLAG ='Y'  ";
        sqlCmd += "        AND cea.status_id in ('1','7')  ";
        sqlCmd += "        AND ? ='Y' ) ";
        setString(1, cardNo);   
        setString(2, vdFlag);
        setString(3, cardNo);
        setString(4, vdFlag);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	comcr.errRtn("select_crd_employee_a not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempCnt = getValueInt("h_temp_cnt");
        }
        return (hTempCnt);               
    }
   /***********************************************************************/
   int selectConsumeCnt(String employNo) throws Exception {
    	
    	hTempCnt = 0;
        
        sqlCmd = "select count(*) h_temp_cnt ";
        sqlCmd += "  FROM (  ";
        sqlCmd += "       SELECT CARD_NO ";
        sqlCmd += "         FROM ( ";
        sqlCmd += "              SELECT INTRODUCE_EMP_NO,ISSUE_DATE,CARD_NO,CURRENT_CODE ";
        sqlCmd += "                FROM CRD_CARD cc ";
        sqlCmd += "               WHERE cc.INTRODUCE_EMP_NO !=' ' ";
        sqlCmd += "                 AND CURRENT_CODE = '0'  ";
        sqlCmd += "                 AND INTRODUCE_EMP_NO = ? ";
        sqlCmd += "                 AND issue_date  <= decode( ? ,'2',to_char(add_months(to_date(issue_date,'yyyymmdd'),12-integer(substr(issue_date,5,2)) + ? +1 ) -1 ,'yyyymmdd'),to_char(add_months(to_date(issue_date,'yyyymmdd'), ? ) -1 ,'yyyymmdd')) ";
        sqlCmd += "              UNION all ";      
        sqlCmd += "              SELECT INTRODUCE_EMP_NO,ISSUE_DATE,CARD_NO,dc.CURRENT_CODE ";
        sqlCmd += "                FROM DBC_CARD dc ";
        sqlCmd += "               WHERE INTRODUCE_EMP_NO !=' ' ";
        sqlCmd += "                 AND dc.CURRENT_CODE = '0' ";
        sqlCmd += "                 AND INTRODUCE_EMP_NO = ? ";
        sqlCmd += "                 AND issue_date  <= decode( ? ,'2',to_char(add_months(to_date(issue_date,'yyyymmdd'),12-integer(substr(issue_date,5,2)) + ? +1 ) -1 ,'yyyymmdd'),to_char(add_months(to_date(issue_date,'yyyymmdd'), ? ) -1 ,'yyyymmdd')) ";
        sqlCmd += "         ) ";
        sqlCmd += "         GROUP BY CARD_NO ) ";
        setString(1, employNo);   
        setString(2, hMilgConsumeFlage);
        if (hMilgConsumeFlage.equals("2") &&  hMilgNextMonth==0) {
        	hMilgNextMonth = 1;
        }
        if (hMilgConsumeFlage.equals("1") && hMilgCurrMonth==0) {
        	hMilgCurrMonth = 1;
        }
        setInt(3, hMilgNextMonth);
        setInt(4, hMilgCurrMonth);
        setString(5, employNo);   
        setString(6, hMilgConsumeFlage);
        setInt(7, hMilgNextMonth);
        setInt(8, hMilgCurrMonth);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	comcr.errRtn("select_crd_card not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempCnt = getValueInt("h_temp_cnt");
        }
        return (hTempCnt);               
    }
   /***********************************************************************/
   int selectCurrentCnt(String employNo) throws Exception {
    	
    	hTempCnt = 0;
        
        sqlCmd = "select count(*) h_temp_cnt ";
        sqlCmd += "  FROM (  ";
        sqlCmd += "SELECT a.card_no ";
        sqlCmd += "  FROM mkt_intr_log a ";
        sqlCmd += " WHERE a.acct_month <= ?  ";
        sqlCmd += "   AND a.employ_no= ? ";
        sqlCmd += "   AND a.employ_no IN (  ";
        sqlCmd += "       SELECT cc.INTRODUCE_EMP_NO ";
        sqlCmd += "         FROM CRD_CARD cc ";
        sqlCmd += "        WHERE cc.INTRODUCE_EMP_NO !=' ' ";      
        sqlCmd += "          AND cc.CURRENT_CODE = '0' ";
        sqlCmd += "          AND cc.INTRODUCE_EMP_NO = ? ";
        sqlCmd += "          AND cc.issue_date  <= decode( ? ,'2',to_char(add_months(to_date(cc.issue_date,'yyyymmdd'),12-integer(substr(cc.issue_date,5,2))+ ? +1 ) -1 ,'yyyymmdd'),to_char(add_months(to_date(cc.issue_date,'yyyymmdd'), ? ) -1 ,'yyyymmdd')) ";
        sqlCmd += "        UNION all ";
        sqlCmd += "       SELECT dc.INTRODUCE_EMP_NO ";
        sqlCmd += "         FROM DBC_CARD dc ";
        sqlCmd += "        WHERE dc.INTRODUCE_EMP_NO !=' ' ";
        sqlCmd += "          AND dc.CURRENT_CODE = '0' ";
        sqlCmd += "          AND dc.INTRODUCE_EMP_NO = ? ";
        sqlCmd += "          AND dc.issue_date  <= decode( ? ,'2',to_char(add_months(to_date(dc.issue_date,'yyyymmdd'),12-integer(substr(dc.issue_date,5,2))+ ? +1 ) -1 ,'yyyymmdd'),to_char(add_months(to_date(dc.issue_date,'yyyymmdd'), ? ) -1 ,'yyyymmdd')) ";
        sqlCmd += "        ) ";
        sqlCmd += "   AND a.acct_month <= decode( ? ,'2',to_char(add_months(to_date(a.issue_date,'yyyymmdd'),12-integer(substr(a.issue_date,5,2))+ ? ),'yyyymm'),to_char(add_months(to_date(a.issue_date,'yyyymmdd'), ? -1),'yyyymm')) ";
        sqlCmd += "  GROUP BY  a.card_no ) ";
        setString(1, hTempThisMonth);
        setString(2, employNo);   
        setString(3, employNo);   
        setString(4, hMilgConsumeFlage);
        if (hMilgConsumeFlage.equals("2") &&  hMilgNextMonth==0) {
        	hMilgNextMonth = 1;
        }
        if (hMilgConsumeFlage.equals("1") && hMilgCurrMonth==0) {
        	hMilgCurrMonth = 1;
        }
        setInt(5, hMilgNextMonth);
        setInt(6, hMilgCurrMonth);
        setString(7, employNo);   
        setString(8, hMilgConsumeFlage);
        setInt(9, hMilgNextMonth);
        setInt(10, hMilgCurrMonth);
        setString(11, hMilgConsumeFlage);
        setInt(12, hMilgNextMonth);
        setInt(13, hMilgCurrMonth);
       
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	comcr.errRtn("select_crd_card not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempCnt = getValueInt("h_temp_cnt");
        }
        return (hTempCnt);               
    }
   
   /***********************************************************************/
   int selectMktIntrLogCrdRowCount(String cardNo) throws Exception {
       hTempCnt = 0;

       sqlCmd = "select count(*) h_temp_cnt ";
       sqlCmd += "from mkt_intr_log a ";
       sqlCmd += "where a.program_code = ? ";
       sqlCmd += "  and a.employ_no = ? ";
       sqlCmd += "  and a.card_no = ? ";
       sqlCmd += "  and a.acct_month <= ? ";
       sqlCmd += "  and a.proc_flag = 'N' ";
       sqlCmd += "  and a.acct_month <= decode(?,'2',to_char(add_months(to_date(a.issue_date,'yyyymmdd'),12-integer(substr(a.issue_date,5,2))+ ? ),'yyyymm'),to_char(add_months(to_date(a.issue_date,'yyyymmdd'), ? -1),'yyyymm')) ";        
       sqlCmd += "group by a.card_no ";
       setString(1, hMilgProgramCode);
       setString(2, hEmplEmployNo);
       setString(3, cardNo);
       setString(4, hTempThisMonth);
       setString(5, hMilgConsumeFlage);
       if (hMilgConsumeFlage.equals("2") &&  hMilgNextMonth==0) {
       	hMilgNextMonth = 1;
       }
       if (hMilgConsumeFlage.equals("1") &&  hMilgCurrMonth==0) {
       	hMilgCurrMonth = 1;
       }
       setInt(6, hMilgNextMonth);
       setInt(7, hMilgCurrMonth);
       int recordCnt = selectTable();
       if (notFound.equals("Y")) {
           comcr.errRtn("select_mkt_intr_log not found!", "", hCallBatchSeqno);
       }
       if (recordCnt > 0) {
           hTempCnt = getValueInt("h_temp_cnt");
       }
       return (hTempCnt);
   }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktB110 proc = new MktB110();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
