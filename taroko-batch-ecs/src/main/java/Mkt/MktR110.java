/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110-04-09  V1.00.01  tanwei      新增MktR110程式和相關方法                                                              *
******************************************************************************/
package Mkt;

import com.*;

@SuppressWarnings("unchecked")
public class MktR110 extends AccessDAO
{
 private  String progname = "每月核卡數及消費統計處理  110/04/06  V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;
 
 CommCrd comc = new CommCrd();
 CommCrdRoutine comcr = null;
 
 String hCallBatchSeqno = "";
 String hBusiBusinessDate   = "";
 String hWdayStmtCycle      = "";
 String hWdayThisAcctMonth = "";
 String hBillAcctType = "";
 String hBillGroupCode = "";
 String hBillDbCnt = "";
 String hCardCardType = "";
 String hSta1ProcType = "";
 String lastMonth = "";
 String memberCorpNo = "";
 
 String hCrdCardNo = "";
 String hCrdMajorCardNo = "";
 String hCrdOriCardNo = "";
 String hCrdIdPSeqno = "";
 String hCrdMajorIdPSeqno = "";
 String hCrdRegBankNo = "";
 String hCrdPromoteDept = "";
 String hCrdPromoteEmpNo = "";
 String hCrdIntroduceEmpNo = "";
 
 String hCrdSupFlag = "0";
 
 
 double hSta1AmtBl = 0;
 double hSta1AmtCa = 0;
 double hSta1AmtIt = 0;
 double hSta1AmtId = 0;
 double hSta1AmtOt = 0;
 double hSta1AmtAo = 0;

 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
   MktR110 proc = new MktR110();
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
   
   selectLastMonth(); // 獲取當前年月的上一個月份
   
   showLogMessage("I", "", String.format("CrdCard、DbcCard上個月核卡卡數統計處理....."));
   selectCrdCardCount();
   selectDbcCardCount();
   if (totalCnt > 1) {
     showLogMessage("I", "", String.format("CrdCard上個月核卡處理....."));
     selectCrdCard();
    
     showLogMessage("I", "", String.format("DbcCard上個月核卡處理....."));
     selectDbcCard();
   }

   finalProcess();
   return 0;
  }catch ( Exception ex ){ 
    expMethod = "mainProcess";  expHandle(ex);  
    return exceptExit;  
    }

 } // End of mainProcess

/***
 * 取得crd_card消費筆數
 *
 */
void selectCrdCardBilBill() throws Exception {
    sqlCmd = "SELECT  count(*) db_cnt ";
    sqlCmd += "FROM  bil_bill a ,crd_card b ";
    sqlCmd += "WHERE b.card_no = a.card_no ";
    sqlCmd += "AND a.card_no in (select card_no from crd_card ";
    sqlCmd += "where major_id_p_seqno =? ";
    sqlCmd += "and  decode(?,'1',major_id_p_seqno,id_p_seqno) =? ) ";
    sqlCmd += "and  a.card_type = b.card_type ";
    sqlCmd += "and  decode(a.group_code, '', '0000', a.group_code) = b.group_code ";
    sqlCmd += "AND a.acct_code in ('BL','CA','IT','AO','ID','OT') ";
    sqlCmd += "AND b.card_no = ? AND b.card_type = ?";
    sqlCmd += "AND substr(b.issue_date,1,6) = ? ";  
    sqlCmd += "AND b.acct_type = ? AND b.group_code = ?";
 
    setString(1,hCrdMajorIdPSeqno);
    setString(2,hCrdSupFlag);
    setString(3,hCrdIdPSeqno);
    setString(4,hCrdCardNo);
    setString(5, hCardCardType);
    setString(6, lastMonth);
    setString(7, hBillAcctType);
    setString(8, hBillGroupCode);
    
    int n = selectTable();
    if (n > 0) {
      hBillDbCnt = getValue("db_cnt");
    }
    showLogMessage("I","","selectCrdCardBilBill: ["+n+"]");
    
}

/***
 * 取得dbc_card消費筆數
 *
 */
void selectDbcCardBilBill() throws Exception {
  sqlCmd = "SELECT  count(*) db_cnt, ";
  sqlCmd += "FROM  bil_bill a ,dbc_card b ";
  sqlCmd += "WHERE b.card_no = a.card_no ";
  sqlCmd += "AND   a.card_no in (select card_no from dbc_card ";
  sqlCmd += "WHERE major_id_p_seqno =? ";
  sqlCmd += "AND decode(?,'1',major_id_p_seqno,id_p_seqno) =? ) ";
  sqlCmd += "AND a.card_type = b.card_type ";
  sqlCmd += "AND decode(a.group_code, '', '0000', a.group_code) = b.group_code ";
  sqlCmd += "AND a.acct_code in ('BL','CA','IT','AO','ID','OT') ";
  sqlCmd += "AND b.card_no = ? AND b.card_type = ? ";
  sqlCmd += "AND substr(b.issut_date,1,6) = ? ";  
  sqlCmd += "AND b.acct_type = ? AND b.group_code = ?";
  
  setString(1,hCrdMajorIdPSeqno);
  setString(2,hCrdSupFlag);
  setString(3,hCrdIdPSeqno);
  setString(4,hCrdCardNo);
  setString(5, hCardCardType);
  setString(6, lastMonth);
  setString(7, hBillAcctType);
  setString(8, hBillGroupCode);
  
  int n = selectTable();
  if (n > 0) {
    hBillDbCnt = getValue("db_cnt");
  }
  showLogMessage("I","","selectCrdCardBilBill: ["+n+"]");
}

// 數據存入mkt_issue_statistics表
public int insertMktIssueStatistics() throws Exception {
    setValue("acct_month", lastMonth); // 这里的值文中说要从bil_bill中获取
    setValue("card_no", hCrdCardNo);
    
    setValue("major_card_no", hCrdMajorCardNo);
    setValue("ori_card_no", hCrdOriCardNo);
    setValue("id_p_seqno", hCrdIdPSeqno);
    setValue("major_id_p_seqno", hCrdMajorIdPSeqno);
    setValue("reg_bank_no", hCrdRegBankNo);
    
    setValue("acct_type", hBillAcctType);
    setValue("group_code", hBillGroupCode);
    setValue("card_type", hCardCardType);
    setValue("proc_type", hSta1ProcType);
    setValue("member_id", memberCorpNo); //聯名機構會員代碼
    setValue("valid_cnt",  hBillDbCnt);  //筆數
    
    setValue("promote_dept", hCrdPromoteDept);
    setValue("promote_emp_no", hCrdPromoteEmpNo);
    setValue("introduce_emp_no", hCrdIntroduceEmpNo);
    
    setValueDouble("circulate_num", 1); 
    setValueDouble("amt_bl", hSta1AmtBl);
    setValueDouble("amt_ca", hSta1AmtCa);
    setValueDouble("amt_it", hSta1AmtIt);
    setValueDouble("amt_id", hSta1AmtId);
    setValueDouble("amt_ot", hSta1AmtOt);
    setValueDouble("amt_ao", hSta1AmtAo);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_user", javaProgram);
    setValue("mod_pgm", javaProgram);
    daoTable = "mkt_issue_statistics";
    int n =insertTable();
    return n;
}

 /***
  * 取得crd_card流通卡數()
  *
  * @return
  */
 void selectCrdCard() throws Exception {
     sqlCmd = "select card_no,major_card_no,ori_card_no,id_p_seqno,major_id_p_seqno ,sup_flag ";
     sqlCmd += "acct_type,group_code,card_type,reg_bank_no,promote_dept,promote_emp_no,introduce_emp_no ";
     sqlCmd += "from crd_card  ";
     sqlCmd += "where ori_issue_date = issue_date and acct_type != '90' ";
     sqlCmd += "and substr(issue_date,1,6) = ?";
     // 这里要获取系统年月的上一个月做为参数 
     setString(1, lastMonth);
     int recordCnt = selectTable();
     if (recordCnt > 0) {
       for (int i = 0; i < recordCnt; i++) {
           hCrdCardNo = getValue("card_no");
           hCrdMajorCardNo = getValue("major_card_no");
           hCrdOriCardNo = getValue("ori_card_no");
           hCrdIdPSeqno = getValue("id_p_seqno");
           hCrdSupFlag = getValue("sup_flag");
           
           hCrdMajorIdPSeqno = getValue("major_id_p_seqno");
           hCrdRegBankNo = getValue("reg_bank_no");
           
           hCrdPromoteDept = getValue("promote_dept");
           hCrdPromoteEmpNo = getValue("promote_emp_no");
           hCrdIntroduceEmpNo = getValue("introduce_emp_no");
           
           hBillAcctType = getValue("acct_type");
           hBillGroupCode = getValue("group_code");
           hCardCardType = getValue("card_type");
           
           selectMktMember();
           hSta1ProcType = "1";
           selectCrdCardBilBill();
           selectCrdCardCardConsume();
           
       }
     }
 }
 
 /***
  * 取得dbc_card流通卡數()
  *
  * @return
  */
 void selectDbcCard() throws Exception {
     sqlCmd = "select card_no,major_card_no,ori_card_no,id_p_seqno,major_id_p_seqno , ";
     sqlCmd += "acct_type,group_code,card_type,reg_bank_no ";
     sqlCmd += "from dbc_card ";
     sqlCmd += "where ori_issue_date = issue_date and acct_type = '90' ";
     sqlCmd += "and substr(issue_date,1,6) = ?";
     // 这里要获取系统年月的上一个月做为参数 
     setString(1, lastMonth);
     int recordCnt = selectTable();
     if (recordCnt > 0) {
       for (int i = 0; i < recordCnt; i++) {
           hCrdCardNo = getValue("card_no");
           hCrdMajorCardNo = getValue("major_card_no");
           hCrdOriCardNo = getValue("ori_card_no");
           hCrdIdPSeqno = getValue("id_p_seqno");
           hCrdSupFlag = getValue("sup_flag");
           
           hCrdMajorIdPSeqno = getValue("major_id_p_seqno");
           hCrdRegBankNo = getValue("reg_bank_no");
           
           hBillAcctType = getValue("acct_type");
           hBillGroupCode = getValue("group_code");
           hCardCardType = getValue("card_type");
           
           selectMktMember();
           hSta1ProcType = "2";
           selectDbcCardBilBill();
           selectDbcCardCardConsume();
           
       }
     }
 }

 // 取得crd_card核卡數
 private double selectCrdCardCount () throws Exception{
   double cnt = 0;
   sqlCmd = "select count(*) as cnt ";
   sqlCmd += "from crd_card  ";
   sqlCmd += "where current_code ='0' and ori_card_no = card_no ";
   sqlCmd += "and ori_issue_date = issue_date and acct_type != '90' ";
   sqlCmd += "and substr(issue_date,1,6) = ?";
   setString(1, lastMonth);
   int n = selectTable();
   if (n > 0) {
     cnt = getValueInt("cnt");
   }
   totalCnt++;
   return cnt;
 }
 
 // 取得dbc_card核卡數
 private double selectDbcCardCount() throws Exception{
   double dbCnt = 0;
   sqlCmd = "select count(*) as cnt ";
   sqlCmd += "from dbc_card ";
   sqlCmd += "where current_code ='0' and ori_card_no = card_no ";
   sqlCmd += "and ori_issue_date = issue_date and acct_type = '90' ";
   sqlCmd += "and substr(issue_date,1,6) = ?";
   setString(1, lastMonth);
   int n = selectTable();
   if (n > 0) {
     dbCnt = getValueInt("cnt");
   }
   totalCnt++;
   return dbCnt;
 }
 
 // 獲取當前年月的上一個月份
 void selectLastMonth() throws Exception {
   sqlCmd = "SELECT "
           + "to_char(LAST_DAY(ADD_MONTHS(sysdate, -1)), 'YYYYMM') as last_month "
           + "FROM ptr_businday";
   
   int n = selectTable();
   
   if(n > 0) {
     lastMonth = getValue("last_month");
 } 
 showLogMessage("I","","select last_month: ["+n+"]");
   
 }
 
 /***
  * 取得crd_card消費金額
  *
  * @return
  */
 void selectCrdCardCardConsume() throws Exception {
     sqlCmd = "select sum(consume_bl_amt -sub_bl_amt) as bl_amt ";
     sqlCmd += ",sum(consume_ca_amt - sub_ca_amt) as ca_amt ";
     sqlCmd +=    ",sum(consume_it_amt - sub_it_amt) as it_amt ";
     sqlCmd +=   ",sum(consume_ao_amt - sub_ao_amt) as ao_amt ";
     sqlCmd +=    ",sum(consume_id_amt - sub_id_amt) as id_amt ";
     sqlCmd +=    ",sum(consume_ot_amt - sub_ot_amt) as ot_amt ";
     sqlCmd += " from mkt_card_consume  ";
     sqlCmd += "where ori_card_no in (select ori_card_no from crd_card";
     sqlCmd += " where card_no = ?) and substr(acct_month,1,6) = ? ";
     sqlCmd += "AND acct_type = ? AND group_code = ? AND card_type = ?";
     
     setString(1, hCrdCardNo);
     setString(2, lastMonth);
     setString(3, hBillAcctType);
     setString(4, hBillGroupCode);
     setString(5, hCardCardType);
     
     int n = selectTable();
     if (n > 0) {
       hSta1AmtBl = getValueDouble("bl_amt");
       hSta1AmtCa = getValueDouble("ca_amt");
       hSta1AmtIt = getValueDouble("it_amt");
       hSta1AmtAo = getValueDouble("ao_amt");
       hSta1AmtId = getValueDouble("id_amt");
       hSta1AmtOt = getValueDouble("ot_amt");
     }
     showLogMessage("I","","selectCrdCardCardConsume: ["+n+"]");
     
     insertMktIssueStatistics();
 }
 
 /***
  * 取得dbc_card消費金額
  *
  * @return
  */
 void selectDbcCardCardConsume() throws Exception {
   sqlCmd = "select sum(consume_bl_amt -sub_bl_amt) as bl_amt ";
   sqlCmd += ",sum(consume_ca_amt - sub_ca_amt) as ca_amt ";
   sqlCmd +=    ",sum(consume_it_amt - sub_it_amt) as it_amt ";
   sqlCmd +=   ",sum(consume_ao_amt - sub_ao_amt) as ao_amt ";
   sqlCmd +=    ",sum(consume_id_amt - sub_id_amt) as id_amt ";
   sqlCmd +=    ",sum(consume_ot_amt - sub_ot_amt) as ot_amt ";
   sqlCmd += " from mkt_card_consume  ";
   sqlCmd += "where ori_card_no in (select ori_card_no from dbc_card";
   sqlCmd += " where card_no = ?) and substr(acct_month,1,6) = ? ";
   sqlCmd += "AND b.acct_type = ? AND b.group_code = ? AND AND b.card_type = ?";
   
   setString(1, hCrdCardNo);
   setString(2, lastMonth);
   setString(3, hBillAcctType);
   setString(4, hBillGroupCode);
   setString(5, hCardCardType);
   
   int n = selectTable();
   if (n > 0) {
     hSta1AmtBl = getValueDouble("bl_amt");
     hSta1AmtCa = getValueDouble("ca_amt");
     hSta1AmtIt = getValueDouble("it_amt");
     hSta1AmtAo = getValueDouble("ao_amt");
     hSta1AmtId = getValueDouble("id_amt");
     hSta1AmtOt = getValueDouble("ot_amt");
   }
   showLogMessage("I","","selectCrdCardCardConsume: ["+n+"]");
   
   insertMktIssueStatistics();
    
 }
 
 
 /***
  * 取得聯名機構會員代碼
  *
  * @return
  */
 void selectMktMember() throws Exception {
   sqlCmd = " SELECT member_corp_no "
           + " FROM mkt_member "
           + " WHERE member_corp_no in ("
           + " SELECT member_corp_no "
           + " FROM crd_card a, mkt_member_dtl b "
           + " WHERE a.group_code=b.group_code AND card_no = ?) "
           + " AND apr_flag = 'Y' AND active_status = 'Y'";
   
   setString(1, hCrdCardNo);
   int  n = selectTable();
   if(n > 0) {
       memberCorpNo = getValue("member_corp_no");
   }
   showLogMessage("I","","select mkt_member: ["+n+"]");
 }

}
