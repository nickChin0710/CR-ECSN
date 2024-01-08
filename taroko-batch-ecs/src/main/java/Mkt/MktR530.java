/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/02/14  V1.00.00    Zuwei Su  program initial                           *
 *  112/03/29  V1.00.01    Zuwei Su  business_date的日期的DD與【mkt_jointly_parm 聯名機構推卡獎勵參數主檔】ANY_DAY的值的比對邏輯修改
 ******************************************************************************/

package Mkt;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*聯名機構每月推卡統計表處理程式*/
public class MktR530 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private String progname = "聯名機構每月推卡統計表處理程式  112/02/14 V1.00.00";
    private CommFunction comm = new CommFunction();
    private CommCrd comc = new CommCrd();
    private CommCrdRoutine comcr = null;

    private String hCallBatchSeqno = "";

    private String hBusiBusinessDate = "";
    private String hTempThisMonth = "";
    private String hTempLastMonth = "";
    private String memberId = "";
    private String staffBranchNo = "";
    private String hEmplId = "";
    private String hEmplChiName = "";
    private int totalCardCnt = 0;
    private int tempCurrentCnt = 0;
    private int tempNewCardCnt = 0;
    private String cardIssueDate = "";
    private String cardNo = "";
    private String hCardStaticMonth = "";
    private String cardIdPSeqno = "";
    private String projCode = "";
    private double feedbackRate = 0;    


    private String feedbackType = "";
    private int    hMilgCurrMonth = 0;
    private int    hMilgNextMonth = 0;
    private String hMilgCurrTotCond = "";
    private double hMilgCurrAmt = 0;
    private int    hMilgCurrTotCnt = 0; 
//    private String hMilgExcludeBank = "";
//    private String hMilgExcludeFinance = "";

//    private String hMilgSaleCond = ""; 
//    private int    hMilgConsumeCnt = 0;
//    private int    hMilgCurrentCnt = 0; 


    private String hMifdFeedbackType = "";
    private double feedbackAmt = 0;
    private double hMifdFeedbackScore = 0;
    private double hMifdFeedbackScoreAmt = 0;   
    

    private double hMifdPurchaseScore = 0;    

    private String minVdFlag = "";
    private double destinationAmtSum = 0;
    private int hMilgDestinationCnt = 0;
    private double hMictPurchaseAmt = 0;
    private double hMictRefundAmt = 0;

    private int    hMictPurchaseCnt = 0;
    private double hMictFeedbackAmt = 0;       

    private int currPreDay = 0;
    private int hTempCnt = 0;
    private String hMictNewCardFlag = "";
    private int hMidlNewCardhdrCnt = 0;
    private int hMidlTotalVdCardCnt = 0;
    private int hMidlUseCardCnt = 0;
    private int hMidlUseVdCardCnt = 0;
    private double hMidlUseCardAmt = 0;
    private double hMidlUseVdCardAmt = 0;
    private double hMidlFeedbackAmt = 0;
    private double hMidlFeedbackVdAmt = 0;
    private double hMidlTotalFeedbackAmt = 0;

//    private long hTempCardCnt = 0;
//    private long[] aMTempCardCnt = new long[250];
//    private long[] aMTempCurrentCnt = new long[250];
//    private long[] aMTempNewCardCnt = new long[250];
    private long totalCnt = 0;

//    private double totalFeedbackAmt;

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
                comc.errExit("Usage : MktR530 YYYYMMDD", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            if (args.length == 1) {
                hBusiBusinessDate = args[0];
                hTempThisMonth = hBusiBusinessDate.substring(0, 6);
            } else {
                selectPtrBusinday();
                // 取得business_date的日期(YYYYMMDD)的DD，再取得【mkt_jointly_parm 聯名機構推卡獎勵參數主檔】ANY_DAY的值,比對符合
                if(checkMktJointlyParm()==0) {
                    exceptExit = 0;
                    comcr.errRtn(String.format("本日無獎勵專案執行[%s]!", hBusiBusinessDate), "", hCallBatchSeqno);
                }
            }
//            if (!hBusiBusinessDate.substring(6).equals("01")) {
//                exceptExit = 0;
//                comcr.errRtn(String.format("本程式只在每月一日執行[%s]!", hBusiBusinessDate), "", hCallBatchSeqno);
//            }

            if (checkMktMemberDetail() != 0) {
                exceptExit = 0;
                comcr.errRtn(String.format("本月獎勵已轉基金不可再次執行!"), "", hCallBatchSeqno);
            }
         
            updateMktMemberLogFlag(); 
            deleteMktMemberDetail(); 
            deleteMktMemberCardlist();  
            commitDataBase();

            showLogMessage("I", "", String.format("Processing Static month [%s]", hTempThisMonth));
            totalCnt = 0;
            showLogMessage("I", "", String.format("Processing use card data"));
            selectMktMemberLog();
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
    // 檢查【mkt_member_detail 招攬信用卡獎勵金統計檔】的static_month = ? 如果已存在,程式結束
    int checkMktMemberDetail() throws Exception {
        hTempCnt = 0;

        sqlCmd = "select count(*) h_temp_cnt ";
        sqlCmd += " from mkt_member_detail  ";
        sqlCmd += "where static_month = ?  ";
        sqlCmd += "  and proc_flag = 'Y'  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTempThisMonth);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_mkt_member_detail not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempCnt = getValueInt("h_temp_cnt");
        }
        return (hTempCnt);
    }

    /***********************************************************************/
    void updateMktMemberLogFlag() throws Exception {
        daoTable = "mkt_member_log";
        updateSQL = "static_month = '',";
        updateSQL += " proc_flag = 'N',";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm  = 'MktR530'";
        whereStr = "where acct_month = ? ";
        setString(1, hTempThisMonth);
        updateTable();

    }

    /***********************************************************************/
    void deleteMktMemberDetail() throws Exception {
        daoTable = "mkt_member_detail";
        whereStr = "where static_month = ? ";
        setString(1, hTempThisMonth);
        deleteTable();

    }

    /***********************************************************************/
    void deleteMktMemberCardlist() throws Exception {
        daoTable = "mkt_member_cardlist";
        whereStr = "where static_month = ? ";
        setString(1, hTempThisMonth);
        deleteTable();

    }

    /***********************************************************************/
    void selectMktMemberLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.proj_code,";
        sqlCmd += " a.member_id,";
        sqlCmd += " a.staff_branch_no,";
        sqlCmd += " c.feedback_type,"; 
        sqlCmd += " c.feedback_rate,"; 
        sqlCmd += " c.feedback_amt"; 
        sqlCmd += " from mkt_member_log a,mkt_jointly_parm c ";
        sqlCmd += " where a.proj_code = c.proj_code ";
        sqlCmd += "  and (a.staff_branch_no != '' or a.member_id !='') ";
        sqlCmd += "  and a.acct_month <= ? ";
        sqlCmd += "  and a.proc_flag = 'N' ";
        sqlCmd += "  and ? between decode(c.proj_date_s,'','20100101',c.proj_date_s) and decode(c.proj_date_e,'','30001231',c.proj_date_e) ";
        sqlCmd += "  AND substr( ? ,7,2) = lpad(c.any_day,2,'0')  ";
        sqlCmd += "  and c.apr_flag='Y' ";
        setString(1, hTempThisMonth);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            projCode = getValue("proj_code");
            memberId = getValue("member_id");
            staffBranchNo = getValue("staff_branch_no");
            feedbackType = getValue("feedback_type");
            feedbackRate = getValueDouble("feedback_rate");            
            feedbackAmt = getValueDouble("feedback_amt");
//            hMifdFeedbackScore  = getValueDouble("h_mifd_feedback_score");
//            hMifdFeedbackScoreAmt = getValueDouble("h_mifd_feedback_score_amt");            
//       	    hMilgCurrMonth = getValueInt("h_mifd_curr_month");
//            hMilgNextMonth = getValueInt("h_mifd_next_month");
//       	    hMilgCurrTotCond = getValue("h_mifd_curr_tot_cond");
//       	    hMilgCurrAmt = getValueDouble("h_mifd_curr_amt");
//       	    hMilgCurrTotCnt = getValueInt("h_mifd_curr_tot_cnt"); 
//       	    hMilgExcludeBank = getValue("h_mifd_exclude_bank");
//       	    hMilgExcludeFinance = getValue("h_mifd_exclude_finance");
//          	hMilgSaleCond  = getValue("h_mifd_sale_cond");
//       	    hMilgConsumeCnt = getValueInt("h_mifd_consume_cnt"); 
//       	    hMilgCurrentCnt = getValueInt("h_mifd_current_cnt"); 
            
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
            totalCardCnt = 0;

            selectMktMemberLogCount();
            if (hMidlTotalFeedbackAmt <= 0)
                continue;
                  
            insertMktMemberDetail();
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void selectMktMemberLogCount() throws Exception {
        long tempLong = 0;
   
        sqlCmd = "select ";
        sqlCmd += " a.card_no,";
        sqlCmd += " min(a.vd_flag) vd_flag,";
        sqlCmd += " min(a.issue_date) issue_date,";
        sqlCmd += " sum(a.dest_amt) dest_amt";
        sqlCmd += " from mkt_member_log a,mkt_jointly_parm c ";
        sqlCmd += " where a.proj_code = c.proj_code ";
        sqlCmd += "  and a.proj_code = ? ";
        sqlCmd += "  and a.member_id = ? ";
        sqlCmd += "  and a.acct_month <= ? ";
        sqlCmd += "  and a.proc_flag = 'N' ";
        sqlCmd += "  and a.acct_month <= decode(?,'2',to_char(add_months(to_date(a.issue_date,'yyyymmdd'),12-integer(substr(a.issue_date,5,2))+ ? ),'yyyymm'),to_char(add_months(to_date(a.issue_date,'yyyymmdd'), ? -1),'yyyymm')) ";        
        sqlCmd += "group by a.card_no ";
        setString(1, projCode);
        setString(2, memberId);
        setString(3, hTempThisMonth);
        setString(4, feedbackType);
        if (feedbackType.equals("2") &&  hMilgNextMonth==0) {
        	hMilgNextMonth = 1;
        }
        if (feedbackType.equals("1") && hMilgCurrMonth==0) {
        	hMilgCurrMonth = 1;
        }
        setInt(5, hMilgNextMonth);
        setInt(6, hMilgCurrMonth);
        int recordCnt = selectTable();
               
        for (int i = 0; i < recordCnt; i++) {
            cardNo = getValue("card_no", i);
            minVdFlag = getValue("vd_flag", i);
            cardIssueDate = getValue("issue_date", i);
            destinationAmtSum = getValueDouble("dest_amt", i);
            
 	       if ( selectCardlistCardNo(cardNo) > 0 ) {
 	    	   continue;
 	       }
           
// 	       //EXCLUDE_BANK
// 	       if (hMilgExcludeBank.equals("Y")) {
// 	    	  if ( selectExcludeBank(minVdFlag,cardNo) > 0 ) {
// 	 	    	   continue;
// 	 	       }
// 	       }
// 	       //EXCLUDE_FINANCE
// 	      if (hMilgExcludeFinance.equals("Y")) {
// 	    	  if ( selectExcludeFinance(minVdFlag,cardNo) > 0 ) {
//	 	    	   continue;
//	 	       }
//	       }
// 	      
// 	      //SALE_COND
// 	      if (hMilgSaleCond.equals("Y")) {
// 	    	  
// 	    	  int hTempConsumeCnt = 0;
// 	    	  int hTempCurrentCnt = 0;
// 	    	 
// 	    	  hTempConsumeCnt = selectConsumeCnt(memberId);
// 	    	  hTempCurrentCnt = selectCurrentCnt(memberId); 	    	 
// 	    	  if (hTempConsumeCnt < hMilgConsumeCnt || hTempCurrentCnt < hMilgCurrentCnt) {
//	 	    	   continue;
//	 	       }
//	       } 	       
 	       hMictFeedbackAmt = 0;
 	       hMictPurchaseCnt = 0;
 	      
            switch (feedbackType) {
			case "1":
				tempLong = (long) ((destinationAmtSum * feedbackRate) / 100.0 + 0.5);
				break;
			case "2":
				tempLong = (long) feedbackAmt ;
				break;
			case "3":
				tempLong = (long) (hMifdFeedbackScore * hMifdFeedbackScoreAmt) ;
				break;
            }
             
            if (tempLong <= 0) {
               continue;
            }
            hMictFeedbackAmt = tempLong; 
            hMidlTotalFeedbackAmt = hMidlTotalFeedbackAmt + tempLong;            

	        if (hMilgCurrTotCond.equals("Y") ) {	            	
	           if(destinationAmtSum <  hMilgCurrAmt && hMilgDestinationCnt < hMilgCurrTotCnt ) {
	        	  continue;
	           }
	            	
	        }else {	            	
	           if(destinationAmtSum <  hMilgCurrAmt   ) {
	        	  continue;
	           }	            	
	        }
	        
	        hMictPurchaseCnt = selectMktMemberLogCrdRowCount(cardNo);
	        hMifdPurchaseScore = hMifdPurchaseScore + hMifdFeedbackScore ;
	        
            if (minVdFlag.equals("N")) {
                hMidlUseCardAmt = hMidlUseCardAmt + destinationAmtSum;
                hMidlUseCardCnt++;
                hMidlFeedbackAmt = hMidlFeedbackAmt + tempLong;
            } else {
                hMidlUseVdCardAmt = hMidlUseVdCardAmt + destinationAmtSum;
                hMidlUseVdCardCnt++;
                hMidlFeedbackVdAmt = hMidlFeedbackVdAmt + tempLong;
            }
            
            //Max static_month
            selectStaticMonth(cardNo);
         
            hMictNewCardFlag = "N";
            
            insertMktMemberCardlist();
            updateMktMemberLog1(cardNo);
        }

    }
  
    /***********************************************************************/
//    void updateMktMemberLog() throws Exception {
//        daoTable = "mkt_member_log";
//        updateSQL = "proc_flag = '1',";
//        updateSQL += " proc_date = ?,";
//        updateSQL += " static_month = ?,";
//        updateSQL += " mod_time = sysdate,";
//        updateSQL += " mod_pgm = 'MktR530'";
//        whereStr = "where member_id = ?  ";
//        whereStr += "and acct_month <= ?  ";
//        whereStr += "and proc_flag = 'N'  ";
//        whereStr += "and decode(proj_code,'','x',proj_code) = ? ";
//        setString(1, hBusiBusinessDate);
//        setString(2, hTempThisMonth);
//        setString(3, memberId);
//        setString(4, hTempThisMonth);
//        setString(5, projCode);
//        updateTable();
//        if (notFound.equals("Y")) {
//            comcr.errRtn("update_mkt_member_log not found!", "", hCallBatchSeqno);
//        }
//
//    }
    /***********************************************************************/
    void updateMktMemberLog1(String cardNo) throws Exception {
        daoTable = "mkt_member_log";
        updateSQL = "proc_flag = '1',";
        updateSQL += " proc_date = ?,";
        updateSQL += " static_month = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm = 'MktR530'";
        whereStr = "where member_id = ?  ";
        whereStr += "and acct_month <= ?  ";
        whereStr += "and proc_flag = 'N'  ";
        whereStr += "and decode(proj_code,'','x',proj_code) = ? ";
        whereStr += "and card_no = ? ";
        whereStr += "and acct_month <= decode(?,'2',to_char(add_months(to_date(issue_date,'yyyymmdd'),12-integer(substr(issue_date,5,2))+ ? ),'yyyymm'),to_char(add_months(to_date(issue_date,'yyyymmdd'), ? -1),'yyyymm')) ";        
       
        setString(1, hBusiBusinessDate);
//        setString(2, hTempThisMonth);
        setString(2, hCardStaticMonth);
        setString(3, memberId);
        setString(4, hTempThisMonth);
        setString(5, projCode);
        setString(6, cardNo);        
        setString(7, feedbackType);
        if (feedbackType.equals("2") &&  hMilgNextMonth==0) {
        	hMilgNextMonth = 1;
        }
        if (feedbackType.equals("1") && hMilgCurrMonth==0) {
        	hMilgCurrMonth = 1;
        }
        setInt(8, hMilgNextMonth);
        setInt(9, hMilgCurrMonth); 
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_mkt_member_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " count(*) total_card_cnt,";
        sqlCmd += " sum(decode(a.current_code,'0',1,0)) temp_current_cnt,";
        sqlCmd += " sum(decode(sign(a.issue_date-( ?||'00')),1,1,0)) temp_new_card_cnt ";
        sqlCmd += " from crd_card a ";
        sqlCmd += "where a.issue_date <= ? || '31' ";
        setString(1, hTempThisMonth);
        setString(2, hTempThisMonth);
        hCardStaticMonth = hTempThisMonth;
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            totalCardCnt = getValueInt("total_card_cnt");
            tempCurrentCnt = getValueInt("temp_current_cnt");
            tempNewCardCnt = getValueInt("temp_new_card_cnt");

            totalCnt++;
            if ((totalCnt % 1000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }
            hMidlNewCardhdrCnt = 0;
            if (tempNewCardCnt > 0)
                selectCrdCard1();

            hMidlUseVdCardCnt = 0;
            hMidlUseCardCnt = 0;
            hMidlUseVdCardAmt = 0;
            hMidlUseCardAmt = 0;
            hMidlFeedbackAmt = 0;
            hMidlFeedbackVdAmt = 0;
            hMidlTotalFeedbackAmt = 0;
            feedbackRate = 0;            
            hMifdFeedbackType = "";
            feedbackAmt = 0;
            hMifdFeedbackScore = 0;
            hMifdFeedbackScoreAmt = 0;            
            hMifdPurchaseScore = 0; 

            if (hMidlNewCardhdrCnt > 0) {
                selectMktJointlyParm(1);   
            } else {
                if (tempCurrentCnt > 0)
                    selectMktJointlyParm(3); 
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
        sqlCmd += "where a.member_id = ? ";
        sqlCmd += "  and decode(a.issue_date,'','x',a.issue_date) <= ? || '31' ";
        sqlCmd += "  and decode(a.issue_date,'','x',a.issue_date)  >= ? || '01'  ";
        sqlCmd += "  and a.old_card_no = '' ";
        setString(1, memberId);
        setString(2, hTempThisMonth);
        setString(3, hTempThisMonth);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            cardIssueDate = getValue("issue_date", i);
            cardNo = getValue("card_no", i);
            cardIdPSeqno = getValue("id_p_seqno", i);

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
            selectMktJointlyParm(2); 
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
        setString(1, cardIdPSeqno);
        setString(2, cardIssueDate);
        setString(3, cardIssueDate);
        setInt(4, currPreDay);
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
        sqlCmd += " min(staff_branch_no) staff_branch_no, member_id,";
        sqlCmd += " count(*) h_midl_total_card_cnt,";
        sqlCmd += " sum(decode(a.current_code,'0',1,0)) h_temp_current_cnt,";
        sqlCmd += " sum(decode(sign(a.issue_date-( '202212'||'00')),1,1,0)) h_temp_new_card_cnt ";
        sqlCmd += "from dbc_card a ";
        sqlCmd += "where (a.staff_branch_no != '' or member_id !='') ";
        sqlCmd += "  and a.issue_date <= ?||'31' ";   
        sqlCmd += "group by member_id ";
        setString(1, hTempThisMonth);
        hCardStaticMonth = hTempThisMonth;
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            memberId = getValue("member_id");
            staffBranchNo = getValue("staff_branch_no");
            hMidlTotalVdCardCnt = getValueInt("h_midl_total_card_cnt");
            tempNewCardCnt = getValueInt("h_temp_new_card_cnt");
            totalCardCnt = 0;

            totalCnt++;
            if ((totalCnt % 1000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }
            hMidlNewCardhdrCnt = 0;
            if (tempNewCardCnt > 0)
                selectDbcCard1();

            hMidlUseVdCardCnt = 0;
            hMidlUseCardCnt = 0;
            hMidlUseCardAmt = 0;
            hMidlUseVdCardAmt = 0;
            hMidlFeedbackAmt = 0;
            hMidlFeedbackAmt = 0;
            feedbackRate = 0;
            hMifdPurchaseScore = 0; 

            selectMktJointlyParm(3); 
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
        sqlCmd += "where a.member_id = ? ";
        sqlCmd += "  and length(trim(a.introduce_emp_no)) > 0 ";
        sqlCmd += "  and a.issue_date like ? || '%' ";
        sqlCmd += "  and a.current_code = '0' ";
        sqlCmd += "  and old_card_no = '' ";
        setString(1, memberId);
        setString(2, hTempThisMonth);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            cardIssueDate = getValue("issue_date", i);
            cardNo = getValue("card_no", i);
            cardIdPSeqno = getValue("id_p_seqno", i);

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
            selectMktJointlyParm(2); 
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
        setString(1, cardIdPSeqno);
        setString(2, cardIssueDate);
        setString(3, cardIssueDate);
        setInt(4, currPreDay);
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
    void selectMktJointlyParm(int hInt1) throws Exception {

        sqlCmd = "select ";
        sqlCmd += " proj_code,";
        sqlCmd += " curr_pre_day ";
        sqlCmd += "from mkt_jointly_parm ";
        sqlCmd += "where ? between proj_date_s and decode(proj_date_e,'','30001231',proj_date_e) ";
        sqlCmd += " and apr_flag='Y' ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            projCode = getValue("proj_code", i);
            currPreDay = getValueInt("curr_pre_day", i);

            if (hInt1 == 1)
                if (insertMktMemberDetail() != 0)
                    updateMktMemberDetail(); 
            		  
            if (hInt1 == 2)  
                if (insertMktMemberCardlist() != 0)    
                    updateMktMemberCardlist();  
            if (hInt1 == 3)             	
                   updateMktMemberDetail(); 
            		
        }

    }

    /***********************************************************************/
    int insertMktMemberDetail() throws Exception {
//        setValue("static_month", hTempThisMonth);
        setValue("static_month", hCardStaticMonth);
        setValue("proj_code", projCode);
        setValue("staff_branch_no", staffBranchNo);
        setValue("employ_id", hEmplId);
        setValue("member_id", memberId);
        setValue("chi_name", hEmplChiName);
        setValueInt("new_cardhdr_cnt", hMidlNewCardhdrCnt);
        setValueInt("total_card_cnt", totalCardCnt);
        setValueInt("total_vd_card_cnt", hMidlTotalVdCardCnt);
        setValueInt("use_card_cnt", hMidlUseCardCnt);
        setValueInt("use_vd_card_cnt", hMidlUseVdCardCnt);
        setValueDouble("use_card_amt", hMidlUseCardAmt);
        setValueDouble("use_vd_card_amt", hMidlUseVdCardAmt);
        setValueDouble("feedback_amt", hMidlFeedbackAmt);
        setValueDouble("feedback_vd_amt", hMidlFeedbackVdAmt);
        setValueDouble("total_feedback_amt", hMidlTotalFeedbackAmt);
        setValueDouble("feedback_rate", feedbackRate);        
        setValueDouble("purchase_score", hMifdPurchaseScore);   
        setValue("crt_date", hBusiBusinessDate);
        setValue("crt_time", sysTime);     
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        setValueInt("newcard_cnt_month", currPreDay);
        daoTable = "mkt_member_detail";
        insertTable();
        if (dupRecord.equals("Y")) {
            return -1;
        }
        return 0;
    }

    /***********************************************************************/
    int insertMktMemberCardlist() throws Exception {
        setValue("static_month", hCardStaticMonth);
        setValue("proj_code", projCode);
        setValue("staff_branch_no", staffBranchNo);
        setValue("employ_id", hEmplId);
        setValue("member_id", memberId);
        setValue("chi_name", hEmplChiName);
        setValue("card_no", cardNo);
        setValue("issue_date", cardIssueDate);
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
        daoTable = "mkt_member_cardlist";
        insertTable();
        if (dupRecord.equals("Y")) {
            return -1;
        }

        return 0;
    }

    /***********************************************************************/
    void updateMktMemberCardlist() throws Exception {
        daoTable = "mkt_member_cardlist";
        updateSQL = "new_card_flag = 'Y',";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm  = 'MktR530'";
        whereStr = "where static_month = ?  ";
        whereStr += "and proj_code = ?  ";
        whereStr += "and member_id  = ?  ";
        whereStr += "and card_no  = ? ";
        setString(1, hCardStaticMonth);
        setString(2, projCode);
        setString(3, memberId);
        setString(4, cardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_mkt_member_cardlist not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateMktMemberDetail() throws Exception {
        daoTable = "mkt_member_detail";
        updateSQL = "new_cardhdr_cnt = new_cardhdr_cnt + ?,";
        updateSQL += " total_card_cnt = total_card_cnt + ?,";
        updateSQL += " total_vd_card_cnt = total_vd_card_cnt + ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm   = 'MktR530',";
        updateSQL += " newcard_cnt_month = ?";
        whereStr = "where static_month = ?  ";
        whereStr += "and proj_code = ?  ";
        whereStr += "and member_id  = ? ";
        setInt(1, hMidlNewCardhdrCnt);
        setInt(2, totalCardCnt);
        setInt(3, hMidlTotalVdCardCnt);
        setInt(4, currPreDay);
        if (hCardStaticMonth.equals(""))
        	hCardStaticMonth = hTempThisMonth;
        setString(5, hCardStaticMonth);
        setString(6, projCode);
        setString(7, memberId);
        updateTable();
        if (notFound.equals("Y"))
            insertMktMemberDetail();
    }
  
    /***********************************************************************/
   
    int selectMktMemberDetail1() throws Exception {
        hTempCnt = 0;
                 
        sqlCmd = "select max(static_month) static_month, count(*) h_temp_cnt ";
        sqlCmd += " from mkt_member_detail  ";
        sqlCmd += "where proj_code = ?  ";
        sqlCmd += "  and member_id = ?  ";
        setString(1, projCode);
        setString(2, memberId);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_mkt_member_detail not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempCnt = getValueInt("h_temp_cnt");
            hCardStaticMonth = getValue("static_month");
        }
        return (hTempCnt);
    }
   
    /***********************************************************************/
    
    int checkMktJointlyParm() throws Exception {
    	
        hTempCnt = 0;
        String feedbackType="";

        sqlCmd = "select feedback_type ";
        sqlCmd += " from mkt_jointly_parm  ";
        sqlCmd += " WHERE apr_flag='Y'  ";
        sqlCmd += " AND ? <= to_char(add_months(to_date(decode(proj_date_e,'','30001231',proj_date_e),'yyyymmdd'),12),'yyyymmdd')   ";
        sqlCmd += " AND substr(?,7,2) = lpad(any_day,2,'0') ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        openCursor();
        while (fetchTable()) {
        	if (hTempCnt == 1) {
        		break;
        	}
        	feedbackType = getValue("feedback_type");
			 switch (feedbackType) {
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
        sqlCmd += " from mkt_member_log a ";
        sqlCmd += " where a.proj_code = ? ";
        sqlCmd += "  and a.member_id = ? ";
        sqlCmd += "  and a.acct_month <= ? ";
        sqlCmd += "  and a.card_no = ? ";
        sqlCmd += "  and a.proc_flag = 'N' ";
        sqlCmd += "  and a.acct_month <= decode(?,'2',to_char(add_months(to_date(a.issue_date,'yyyymmdd'),12-integer(substr(a.issue_date,5,2))+ ? ),'yyyymm'),to_char(add_months(to_date(a.issue_date,'yyyymmdd'), ? -1),'yyyymm')) ";        
        sqlCmd += "ORDER BY a.acct_month ";
        setString(1, projCode);
        setString(2, memberId);
        setString(3, hTempThisMonth);
        setString(4, cardNo);
        setString(5, feedbackType);
        if (feedbackType.equals("2") &&  hMilgNextMonth==0) {
        	hMilgNextMonth = 1;
        }
        if (feedbackType.equals("1") && hMilgCurrMonth==0) {
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
        sqlCmd += " from mkt_member_cardlist  ";
        sqlCmd += "where card_no = ? ";      
        sqlCmd += "and feedback_amt > 0 ";      
        setString(1, cardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	comcr.errRtn("select_mkt_member_cardlist not found!", "", hCallBatchSeqno);
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
//   int selectConsumeCnt(String employNo) throws Exception {
//    	
//    	hTempCnt = 0;
//        
//        sqlCmd = "select count(*) h_temp_cnt ";
//        sqlCmd += "  FROM (  ";
//        sqlCmd += "       SELECT CARD_NO ";
//        sqlCmd += "         FROM ( ";
//        sqlCmd += "              SELECT INTRODUCE_EMP_NO,ISSUE_DATE,CARD_NO,CURRENT_CODE ";
//        sqlCmd += "                FROM CRD_CARD cc ";
//        sqlCmd += "               WHERE cc.INTRODUCE_EMP_NO !=' ' ";
//        sqlCmd += "                 AND CURRENT_CODE = '0'  ";
//        sqlCmd += "                 AND INTRODUCE_EMP_NO = ? ";
//        sqlCmd += "                 AND issue_date  <= decode( ? ,'2',to_char(add_months(to_date(issue_date,'yyyymmdd'),12-integer(substr(issue_date,5,2)) + ? +1 ) -1 ,'yyyymmdd'),to_char(add_months(to_date(issue_date,'yyyymmdd'), ? ) -1 ,'yyyymmdd')) ";
//        sqlCmd += "              UNION all ";      
//        sqlCmd += "              SELECT INTRODUCE_EMP_NO,ISSUE_DATE,CARD_NO,dc.CURRENT_CODE ";
//        sqlCmd += "                FROM DBC_CARD dc ";
//        sqlCmd += "               WHERE INTRODUCE_EMP_NO !=' ' ";
//        sqlCmd += "                 AND dc.CURRENT_CODE = '0' ";
//        sqlCmd += "                 AND INTRODUCE_EMP_NO = ? ";
//        sqlCmd += "                 AND issue_date  <= decode( ? ,'2',to_char(add_months(to_date(issue_date,'yyyymmdd'),12-integer(substr(issue_date,5,2)) + ? +1 ) -1 ,'yyyymmdd'),to_char(add_months(to_date(issue_date,'yyyymmdd'), ? ) -1 ,'yyyymmdd')) ";
//        sqlCmd += "         ) ";
//        sqlCmd += "         GROUP BY CARD_NO ) ";
//        setString(1, employNo);   
//        setString(2, feedbackType);
//        if (feedbackType.equals("2") &&  hMilgNextMonth==0) {
//        	hMilgNextMonth = 1;
//        }
//        if (feedbackType.equals("1") && hMilgCurrMonth==0) {
//        	hMilgCurrMonth = 1;
//        }
//        setInt(3, hMilgNextMonth);
//        setInt(4, hMilgCurrMonth);
//        setString(5, employNo);   
//        setString(6, feedbackType);
//        setInt(7, hMilgNextMonth);
//        setInt(8, hMilgCurrMonth);
//        int recordCnt = selectTable();
//        if (notFound.equals("Y")) {
//        	comcr.errRtn("select_crd_card not found!", "", hCallBatchSeqno);
//        }
//        if (recordCnt > 0) {
//            hTempCnt = getValueInt("h_temp_cnt");
//        }
//        return (hTempCnt);               
//    }
   /***********************************************************************/
//   private int selectCurrentCnt(String employNo) throws Exception {
//    	
//    	hTempCnt = 0;
//        
//        sqlCmd = "select count(*) h_temp_cnt ";
//        sqlCmd += "  FROM (  ";
//        sqlCmd += "SELECT a.card_no ";
//        sqlCmd += "  FROM mkt_member_log a ";
//        sqlCmd += " WHERE a.acct_month <= ?  ";
//        sqlCmd += "   AND a.member_id= ? ";
//        sqlCmd += "   AND a.member_id IN (  ";
//        sqlCmd += "       SELECT cc.INTRODUCE_EMP_NO ";
//        sqlCmd += "         FROM CRD_CARD cc ";
//        sqlCmd += "        WHERE cc.INTRODUCE_EMP_NO !=' ' ";      
//        sqlCmd += "          AND cc.CURRENT_CODE = '0' ";
//        sqlCmd += "          AND cc.INTRODUCE_EMP_NO = ? ";
//        sqlCmd += "          AND cc.issue_date  <= decode( ? ,'2',to_char(add_months(to_date(cc.issue_date,'yyyymmdd'),12-integer(substr(cc.issue_date,5,2))+ ? +1 ) -1 ,'yyyymmdd'),to_char(add_months(to_date(cc.issue_date,'yyyymmdd'), ? ) -1 ,'yyyymmdd')) ";
//        sqlCmd += "        UNION all ";
//        sqlCmd += "       SELECT dc.INTRODUCE_EMP_NO ";
//        sqlCmd += "         FROM DBC_CARD dc ";
//        sqlCmd += "        WHERE dc.INTRODUCE_EMP_NO !=' ' ";
//        sqlCmd += "          AND dc.CURRENT_CODE = '0' ";
//        sqlCmd += "          AND dc.INTRODUCE_EMP_NO = ? ";
//        sqlCmd += "          AND dc.issue_date  <= decode( ? ,'2',to_char(add_months(to_date(dc.issue_date,'yyyymmdd'),12-integer(substr(dc.issue_date,5,2))+ ? +1 ) -1 ,'yyyymmdd'),to_char(add_months(to_date(dc.issue_date,'yyyymmdd'), ? ) -1 ,'yyyymmdd')) ";
//        sqlCmd += "        ) ";
//        sqlCmd += "   AND a.acct_month <= decode( ? ,'2',to_char(add_months(to_date(a.issue_date,'yyyymmdd'),12-integer(substr(a.issue_date,5,2))+ ? ),'yyyymm'),to_char(add_months(to_date(a.issue_date,'yyyymmdd'), ? -1),'yyyymm')) ";
//        sqlCmd += "  GROUP BY  a.card_no ) ";
//        setString(1, hTempThisMonth);
//        setString(2, employNo);   
//        setString(3, employNo);   
//        setString(4, feedbackType);
//        if (feedbackType.equals("2") &&  hMilgNextMonth==0) {
//        	hMilgNextMonth = 1;
//        }
//        if (feedbackType.equals("1") && hMilgCurrMonth==0) {
//        	hMilgCurrMonth = 1;
//        }
//        setInt(5, hMilgNextMonth);
//        setInt(6, hMilgCurrMonth);
//        setString(7, employNo);   
//        setString(8, feedbackType);
//        setInt(9, hMilgNextMonth);
//        setInt(10, hMilgCurrMonth);
//        setString(11, feedbackType);
//        setInt(12, hMilgNextMonth);
//        setInt(13, hMilgCurrMonth);
//       
//        int recordCnt = selectTable();
//        if (notFound.equals("Y")) {
//        	comcr.errRtn("select_crd_card not found!", "", hCallBatchSeqno);
//        }
//        if (recordCnt > 0) {
//            hTempCnt = getValueInt("h_temp_cnt");
//        }
//        return (hTempCnt);               
//    }
   
   /***********************************************************************/
   private int selectMktMemberLogCrdRowCount(String cardNo) throws Exception {
       hTempCnt = 0;

       sqlCmd = "select count(*) h_temp_cnt ";
       sqlCmd += "from mkt_member_log a ";
       sqlCmd += "where a.proj_code = ? ";
       sqlCmd += "  and a.member_id = ? ";
       sqlCmd += "  and a.card_no = ? ";
       sqlCmd += "  and a.acct_month <= ? ";
       sqlCmd += "  and a.proc_flag = 'N' ";
       sqlCmd += "  and a.acct_month <= decode(?,'2',to_char(add_months(to_date(a.issue_date,'yyyymmdd'),12-integer(substr(a.issue_date,5,2))+ ? ),'yyyymm'),to_char(add_months(to_date(a.issue_date,'yyyymmdd'), ? -1),'yyyymm')) ";        
       sqlCmd += "group by a.card_no ";
       setString(1, projCode);
       setString(2, memberId);
       setString(3, cardNo);
       setString(4, hTempThisMonth);
       setString(5, feedbackType);
       if (feedbackType.equals("2") &&  hMilgNextMonth==0) {
       	hMilgNextMonth = 1;
       }
       if (feedbackType.equals("1") &&  hMilgCurrMonth==0) {
       	hMilgCurrMonth = 1;
       }
       setInt(6, hMilgNextMonth);
       setInt(7, hMilgCurrMonth);
       int recordCnt = selectTable();
       if (notFound.equals("Y")) {
           comcr.errRtn("select_mkt_member_log not found!", "", hCallBatchSeqno);
       }
       if (recordCnt > 0) {
           hTempCnt = getValueInt("h_temp_cnt");
       }
       return (hTempCnt);
   }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktR530 proc = new MktR530();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
