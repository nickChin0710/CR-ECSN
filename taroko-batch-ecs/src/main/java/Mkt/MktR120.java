/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/04/08  V1.00.00   yanghan              初始化                        *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktR120 extends AccessDAO
{
 private  String progname = "當月核卡消費統計處理110/04/08 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;
 CommCrd comc = new CommCrd();
 String hBusiBusinessDate   = "";
 String hWdayStmtCycle      = "";
 String hWdayThisAcctMonth = "";
 String hPrevMonth="";
 String hModUser="";
 String hCrdCardNo = "";
 String hMemberId = "";
 String hCrdMajorCardNo = "";
 String hCrdOriCardNo = "";
 String hCrdIdPSeqno = "";
 String hCrdMajorIdPSeqno = "";
 String hCrdRegBankNo = "";
 String hCrdPromoteDept = "";
 String hCrdPromoteEmpNo = "";
 String hCrdIntroduceEmpNo = "";
 String hBillAcctType = "";
 String hBillGroupCode = "";
 String hCardCardType = "";
 double hBillDbCnt=0;
 double hBlAmt=0;
 double hCaAmt=0;
 double hItAmt=0;
 double hAoAmt=0;
 double hIdAmt=0;
 double hOtAmt=0;

 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktR120 proc = new MktR120();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("N");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+progname);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程序啟動中, 不執行..");
       return(0);
      }

   if (args.length > 1)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       return(1);
      }

   if ( args.length == 1 )
      { hBusiBusinessDate = args[0]; }
   
   if ( !connectDataBase() ) exitProgram(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());
   hModUser = comc.commGetUserID();
   selectPtrBusinday();
   delMktIissueStatisticsCurrent();

   showLogMessage("I", "", String.format("CrdCard、DbcCard本月核卡卡數統計處理....."));
   int crdCnt=selectCrdCardCount();
   if(crdCnt>0) {
	   selectCrdCard();
   }
   int dbcCnt=selectDbcCardCount();
   if(dbcCnt>0) {
	   selectDbcCard();
   }

   finalProcess();
   return 0;
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void  selectPtrBusinday() throws Exception
 {
	    sqlCmd = "select ";
	    sqlCmd +=" to_char(add_days(to_date(decode(cast(? as varchar(8)),'',business_date, ?),'yyyymmdd'),-1),'yyyymmdd') h_busi_business_date ";
	    sqlCmd += " from ptr_businday ";
	    setString(1, hBusiBusinessDate);
	    setString(2, hBusiBusinessDate);
	    int recordCnt = selectTable();
	    if (recordCnt > 0) {
	    	 hBusiBusinessDate = getValue("h_busi_business_date");	      
	     	 hPrevMonth =hBusiBusinessDate.substring(0,6);
	    }
     	 showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");

 }
 //刪除
// ************************************************************************
 void delMktIissueStatisticsCurrent() throws Exception{
     daoTable = "mkt_issue_statistics_current";
     whereStr = "where acct_month>= to_char(add_months(to_date(?,'yyyymmdd'),-3),'yyyymmdd') ";
     setString(1, hBusiBusinessDate);
     deleteTable();
    showLogMessage("I", "", String.format("刪除資料 mkt_issue_statistics_current"));
 }
 
 int selectCrdCardCount() throws Exception{
	 showLogMessage("I", "", String.format("CrdCard本月核卡處理....."));
	    sqlCmd = "SELECT count(*) as cnt  "
                + "FROM crd_card where current_code='0' "
                + "and ori_card_no=card_no "
                + "and ori_issue_date=issue_date "
                + "and acct_type!='90' "
                + "and substr(issue_date,1,6)=? ";
	    setString(1, hPrevMonth);
        selectTable();
        int cnt=getValueInt("cnt");
     return cnt;
 }
 int selectDbcCardCount() throws Exception{
	    sqlCmd = "SELECT count(*) as dbc_cnt  "
             + "FROM dbc_card where current_code='0' "
             + "and ori_card_no=card_no "
             + "and ori_issue_date=issue_date "
             + "and acct_type='90' "
             + "and substr(issue_date,1,6)=? ";
	    setString(1, hPrevMonth);
     selectTable();
     int cnt=getValueInt("dbc_cnt");
  return cnt;
}
 
 void selectCrdCard() throws Exception{
	    sqlCmd = "SELECT * "
             + "FROM crd_card where current_code='0' "
             + "and ori_card_no=card_no "
             + "and ori_issue_date=issue_date "
             + "and acct_type!='90' "
             + "and substr(issue_date,1,6)=? ";
	    setString(1, hPrevMonth);
	    openCursor();
	    while (fetchTable()) {
	    	  hCrdCardNo = "";
	    	  hCrdMajorCardNo = "";
	    	  hCrdOriCardNo = "";
	    	  hCrdIdPSeqno = "";
	    	  hCrdMajorIdPSeqno = "";
	    	  hCrdRegBankNo = "";
	    	  hCrdPromoteDept = "";
	    	  hCrdPromoteEmpNo = "";
	    	  hCrdIntroduceEmpNo = "";
	    	  hBillAcctType = "";
	    	  hBillGroupCode = "";
	    	  hCardCardType = "";
	         hCrdCardNo = getValue("card_no");
	         hCrdMajorCardNo = getValue("major_card_no");
	         hCrdOriCardNo = getValue("ori_card_no");
	         hCrdIdPSeqno = getValue("id_p_seqno");
	         hCrdMajorIdPSeqno = getValue("major_id_p_seqno");
	         hCrdRegBankNo = getValue("reg_bank_no");
	         
	         hCrdPromoteDept = getValue("promote_dept");
	         hCrdPromoteEmpNo = getValue("promote_emp_no");
	         hCrdIntroduceEmpNo = getValue("introduce_emp_no");
	         
	         hBillAcctType = getValue("acct_type");
	         hBillGroupCode = getValue("group_code");
	         hCardCardType = getValue("card_type");
	         getMemberNo();//獲取member id
	         selectCrdCardBilBillCnt();
	       }
	     closeCursor();
      
}
 
 void selectDbcCard() throws Exception{
	 showLogMessage("I", "", String.format("DbcCard本月核卡處理....."));
	    sqlCmd = "SELECT * "
          + "FROM dbc_card where current_code='0' "
          + "and ori_card_no=card_no "
          + "and ori_issue_date=issue_date "
          + "and acct_type='90' "
          + "and substr(issue_date,1,6)=? ";
	    setString(1, hPrevMonth);
	    openCursor();
	    while (fetchTable()) {
	    	  hCrdCardNo = "";
	    	  hCrdMajorCardNo = "";
	    	  hCrdOriCardNo = "";
	    	  hCrdIdPSeqno = "";
	    	  hCrdMajorIdPSeqno = "";
	    	  hCrdRegBankNo = "";
	    	  hCrdPromoteDept = "";
	    	  hCrdPromoteEmpNo = "";
	    	  hCrdIntroduceEmpNo = "";
	    	  hBillAcctType = "";
	    	  hBillGroupCode = "";
	    	  hCardCardType = "";
	         hCrdCardNo = getValue("card_no");
	         hCrdMajorCardNo = getValue("major_card_no");
	         hCrdOriCardNo = getValue("ori_card_no");
	         hCrdIdPSeqno = getValue("id_p_seqno");
	         hCrdMajorIdPSeqno = getValue("major_id_p_seqno");
	         hCrdRegBankNo = getValue("reg_bank_no");
	         
	         hCrdPromoteDept = getValue("promote_dept");
	         hCrdPromoteEmpNo = getValue("promote_emp_no");
	         hCrdIntroduceEmpNo = getValue("introduce_emp_no");
	         
	         hBillAcctType = getValue("acct_type");
	         hBillGroupCode = getValue("group_code");
	         hCardCardType = getValue("card_type");
	         getMemberNo();//獲取member id
	         selectDbcCardBilBillCnt();
	       }
	     closeCursor();
   
}
 /***********************************************************************/
 
 /***
  * 取得crd_card消費筆數
  *
  */
 void selectCrdCardBilBillCnt() throws Exception {
	 showLogMessage("I", "", String.format("CrdCard本月核卡、前一日消費資料處理....."));
     sqlCmd = "   SELECT  count(*) db_cnt , "
    	     + "sum(decode(acct_code,'BL',decode(sign_flag,'+',dest_amt-abs(curr_adjust_amt),dest_amt*-1))) as h_bl_amt, "
    	     + "sum(decode(acct_code,'CA',decode(sign_flag,'+',dest_amt-abs(curr_adjust_amt),dest_amt*-1))) as h_ca_amt, "
    	     + "sum(decode(acct_code,'IT',decode(sign_flag,'+',dest_amt-abs(curr_adjust_amt),dest_amt*-1))) as h_it_amt, "
    	     + "sum(decode(acct_code,'AO',decode(sign_flag,'+',dest_amt-abs(curr_adjust_amt),dest_amt*-1))) as h_ao_amt, "
    	     + "sum(decode(acct_code,'ID',decode(sign_flag,'+',dest_amt-abs(curr_adjust_amt),dest_amt*-1))) as h_id_amt, "
    	     + "sum(decode(acct_code,'OT',decode(sign_flag,'+',dest_amt-abs(curr_adjust_amt),dest_amt*-1))) as h_ot_amt  ";
     sqlCmd += "    FROM  bil_bill a ,crd_card b ";
     sqlCmd += "    WHERE b.card_no = a.card_no ";
     sqlCmd += "     AND a.acct_code in ('BL','CA','IT','AO','ID','OT')";  
     sqlCmd += "     AND b.card_no=? ";  
     sqlCmd += "     AND a. purchase_date=? "; 
     sqlCmd += "     AND a.acct_month=?  and    rsk_type not in ('1','2','3') ";
     sqlCmd += "     group by b.card_no ";     
     setString(1,hCrdCardNo);
     setString(2,hBusiBusinessDate);
     setString(3,hPrevMonth);
     selectTable();
    	  hBillDbCnt = 0;
          hBlAmt=0;
          hCaAmt=0;
          hItAmt=0;
          hAoAmt=0;
          hIdAmt=0;
          hOtAmt=0;
          hBillDbCnt = getValueDouble("db_cnt");
          hBlAmt=getValueDouble("h_bl_amt");
          hCaAmt=getValueDouble("h_ca_amt");
          hItAmt=getValueDouble("h_it_amt");
          hAoAmt=getValueDouble("h_ao_amt");
          hIdAmt=getValueDouble("h_id_amt");
          hOtAmt=getValueDouble("h_ot_amt");
          checkMktIssueStatisticsCurrentExist("1");
     
 }
 
/***********************************************************************/
 
 /***
  * 取得dbc_card消費筆數
  *
  */
 void selectDbcCardBilBillCnt() throws Exception {
	 showLogMessage("I", "", String.format("DbcCard本月核卡、前一日消費資料處理....."));
     sqlCmd = "   SELECT  count(*) db_cnt , "
    	     + "sum(decode(acct_code,'BL',decode(sign_flag,'+',dest_amt-abs(curr_adjust_amt),dest_amt*-1))) as h_bl_amt, "
    	     + "sum(decode(acct_code,'CA',decode(sign_flag,'+',dest_amt-abs(curr_adjust_amt),dest_amt*-1))) as h_ca_amt, "
    	     + "sum(decode(acct_code,'IT',decode(sign_flag,'+',dest_amt-abs(curr_adjust_amt),dest_amt*-1))) as h_it_amt, "
    	     + "sum(decode(acct_code,'AO',decode(sign_flag,'+',dest_amt-abs(curr_adjust_amt),dest_amt*-1))) as h_ao_amt, "
    	     + "sum(decode(acct_code,'ID',decode(sign_flag,'+',dest_amt-abs(curr_adjust_amt),dest_amt*-1))) as h_id_amt, "
    	     + "sum(decode(acct_code,'OT',decode(sign_flag,'+',dest_amt-abs(curr_adjust_amt),dest_amt*-1))) as h_ot_amt  ";
     sqlCmd += "    FROM  bil_bill a ,dbc_card b ";
     sqlCmd += "    WHERE b.card_no = a.card_no ";
     sqlCmd += "     AND a.acct_code in ('BL','CA','IT','AO','ID','OT')";  
     sqlCmd += "     AND b.card_no=? ";  
     sqlCmd += "     AND a. purchase_date=? "; 
     sqlCmd += "     AND a.acct_month=?  and    rsk_type not in ('1','2','3') ";
     sqlCmd += "     group by b.card_no ";     
     setString(1,hCrdCardNo);
     setString(2,hBusiBusinessDate);
     setString(3,hPrevMonth);
     selectTable();
    	  hBillDbCnt = 0;
          hBlAmt=0;
          hCaAmt=0;
          hItAmt=0;
          hAoAmt=0;
          hIdAmt=0;
          hOtAmt=0;
          hBillDbCnt = getValueDouble("db_cnt");
          hBlAmt=getValueDouble("h_bl_amt");
          hCaAmt=getValueDouble("h_ca_amt");
          hItAmt=getValueDouble("h_it_amt");
          hAoAmt=getValueDouble("h_ao_amt");
          hIdAmt=getValueDouble("h_id_amt");
          hOtAmt=getValueDouble("h_ot_amt");
          checkMktIssueStatisticsCurrentExist("2");
     
 }
 //獲取MEMBER_CORP_NO
 void getMemberNo() throws Exception {
	 hMemberId="";
	 sqlCmd = "     SELECT  a.MEMBER_CORP_NO as hMemberId from "
	 		+ "mkt_member a left join mkt_member_dtl b  on a.member_corp_no=b.member_corp_no "
	 		+ "where b.group_code=? and a.APR_FLAG='Y' and a.ACTIVE_STATUS='Y' ";
	    setString(1, hBillGroupCode);
	    selectTable();
     hMemberId = getValue("a.MEMBER_CORP_NO");

 }
 //
void checkMktIssueStatisticsCurrentExist(String proc) throws Exception{
	 sqlCmd = "     SELECT  count(*) as m_cnt from  "
		 		+ "mkt_issue_statistics_current  "
		 		+ "where circulate_num=0 "
		 		+ " and card_no=? and purchase_date!='' and PROC_TYPE=? and acct_month = ?";
		    setString(1, hCrdCardNo);
		    setString(2, proc);
		    setString(3, hPrevMonth);
		    selectTable();
		    int cntMkt = getValueInt("m_cnt");
		    if(cntMkt>0) {//更新
		    	updateMktIssueStatisticsCurrent(proc);
		    }else {//插入
//		    	if(hBillDbCnt>0) {
//			    	 insertMktIssueStatisticsCurrent(proc);
//		    	}
		    	insertMktIssueStatisticsCurrent(proc);
		    }
	    
 }
void updateMktIssueStatisticsCurrent(String proc) throws Exception {	    
    updateSQL = " acct_month = ?, ";
    updateSQL += " REG_BANK_NO = ?, ";
    updateSQL += " MEMBER_ID = ?, "; 
    updateSQL += " CIRCULATE_NUM = 1, ";
    updateSQL += "PROC_TYPE = ?, ";   
    updateSQL += "VALID_CNT = ?, ";
    updateSQL += "purchase_date = ?, ";
    updateSQL += "post_date = ?, ";       
    updateSQL += "major_card_no = ?, ";
    updateSQL += "ori_card_no = ?, ";
    updateSQL += "id_p_seqno = ?, ";
    updateSQL += "major_id_p_seqno = ?, ";    
    updateSQL += "acct_type = ?, ";
    updateSQL += "group_code = ?, ";
    updateSQL += "card_type = ?, ";
    
    updateSQL += "amt_bl = ?, ";
    updateSQL += "amt_ca = ?, ";    
    updateSQL += "amt_it = ?, ";
    updateSQL += "amt_id = ?, ";
    updateSQL += "amt_ot = ?, ";
    updateSQL += "amt_ao = ?, ";

    updateSQL += " mod_user = ?,";//
    updateSQL += " mod_time = sysdate,";
    updateSQL += " mod_pgm = 'MktR120'";
    whereStr = "where card_no = ? and CIRCULATE_NUM=0  and PROC_TYPE=?";
    whereStr += " and purchase_date <>''  and acct_month = ? ";
    setString(1, hPrevMonth);
    setString(2, hCrdRegBankNo);
    setString(3, hMemberId);//2.21.5.	CIRCULATE_NUM
    setString(4, proc);
    setDouble(5, hBillDbCnt);
    setString(6, hBusiBusinessDate);
    setString(7, hBusiBusinessDate);
    
    setString(8, hCrdMajorCardNo);
    setString(9, hCrdOriCardNo);
    setString(10, hCrdIdPSeqno);
    setString(11, hCrdMajorIdPSeqno);
    
    setString(12, hBillAcctType);
    setString(13, hBillGroupCode);
    setString(14, hCardCardType);
    
    setDouble(15, hBlAmt);
    setDouble(16, hCaAmt);
    setDouble(17, hItAmt);
    setDouble(18, hIdAmt);
    setDouble(19, hOtAmt);
    setDouble(20, hAoAmt);
    setString(21, hModUser);
    setString(22, hCrdCardNo);
    setString(23, proc);
    setString(24, hPrevMonth);
    daoTable = "mkt_issue_statistics_current";
    updateTable();
}

//************************************************************************
public int insertMktIssueStatisticsCurrent(String porc) throws Exception
{
	   setValue("acct_month", hPrevMonth); 
	    setValue("card_no", hCrdCardNo);
	    setValue("purchase_date", hBusiBusinessDate); 
	    setValue("post_date", hBusiBusinessDate);
	    setValue("major_card_no", hCrdMajorCardNo);
	    setValue("ori_card_no", hCrdOriCardNo);
	    setValue("id_p_seqno", hCrdIdPSeqno);
	    setValue("major_id_p_seqno", hCrdMajorIdPSeqno);
	    setValue("reg_bank_no", hCrdRegBankNo);
	    
	    setValue("acct_type", hBillAcctType);
	    setValue("group_code", hBillGroupCode);
	    setValue("card_type", hCardCardType);
	    setValue("proc_type", porc);
	    setValue("member_id", hMemberId); //聯名機構會員代碼
	    setValueDouble("valid_cnt",  hBillDbCnt);  //筆數

	    // 20180517 add fields
	    setValueDouble("circulate_num", 1); /* 核卡卡數 */
	    setValueDouble("amt_bl", hBlAmt);
	    setValueDouble("amt_ca", hCaAmt);
	    setValueDouble("amt_it", hItAmt);
	    setValueDouble("amt_id", hIdAmt);
	    setValueDouble("amt_ot", hOtAmt);
	    setValueDouble("amt_ao", hAoAmt);
	    setValue("mod_time", sysDate + sysTime);
	    setValue("mod_user", hModUser);
	    setValue("mod_pgm", javaProgram);
	    daoTable = "mkt_issue_statistics_current";
	    insertTable();

	    return(0);
	}
}  // End of class FetchSample
