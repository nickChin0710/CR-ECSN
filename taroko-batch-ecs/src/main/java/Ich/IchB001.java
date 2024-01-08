/*****************************************************************************
*                                                                            *
*                             MODIFICATION LOG                               *
*                                                                            *
*    DATE     Version    AUTHOR                       DESCRIPTION            *
* ---------  --------- ----------- ----------------------------------------  *
* 109/01/29  V1.01.00  Lai         program initial                           *
* 109/08/07  V1.01.01  Brian       select_bil_bill增加悠遊卡參數(REF:TscB001)    *
* 109/09/16  V1.01.02  Brian       回饋金額算法以「每卡金額乘以比率後，四捨五入到元再加總」   *
* 109/11/20  V1.00.03  yanghan     修改了變量名稱和方法名稱                                       *
* 112/06/21  V1.00.04  JeffKung    for TCB                                   * 
******************************************************************************/

package Ich;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*悠遊卡簽帳資料檔(STMT)資料處理程式*/
public class IchB001 extends AccessDAO {
    private String progname = "愛金卡簽帳資料檔(b10b)資料處理程式  112/06/21  V1.00.04";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int    debug  = 0;

    String hStmtCrtDate = "";
    String hBusiBusinessDate = "";
    String hBillPrevMonth = "";
    String hBillNextMonth = "";
    String hBillAcctMonth = "";
    String hBillFmDate    = "";
    String hBillToDate    = "";
    String hStmtCrtTime = "";
    String hMBillRealCardNo = "";
    double hMBillDestinationAmt = 0;
    double hBillDestinationAmt = 0;
    int    hBillDestinationCnt = 0;
    double hStmtFeedbackAmt = 0;
    double hDestAllAmt      = 0;
    double hFeedAllAmt      = 0;
    double hAutoaddAllAmt   = 0;
    double hAmt01            = 0;
    double hAmt03            = 0;
    String hBillRealCardNo = "";
	String hRequestDate = "";
	
    String hTspmItemEnameBl = "";
    String hTspmItemEnameIt = "";
    String hTspmItemEnameId = "";
    String hTspmItemEnameCa = "";
    String hTspmItemEnameAo = "";
    String hTspmItemEnameOt = "";
    String hTspmExclMccFlag = "";
    String hTspmExclMchtGroupFlag = "";
    String hTspmExclMchtFlag = "";
    
    int hTempCnt = 0;
    String hTfinRunDay = "";
    long[] nTempRepayAmt = new long[10];
    double[] nTempRepayRate = new double[10];

    int forcFlag = 0;
    int totaCnt = 0;
//************************************************************************************
public int mainProcess(String[] args)
{
 try
  {
   // ====================================
   // 固定要做的
   dateTime();
   setConsoleMode("Y");
   javaProgram = this.getClass().getName();
   showLogMessage("I", "", javaProgram + " " + progname);
   // =====================================
   if (args.length != 0 && args.length != 1 && args.length != 2) {
       comc.errExit("Usage : IchB001 [notify_date] [flag]", "");
   }

   // 固定要做的

   if (!connectDataBase()) {
       comc.errExit("connect DataBase error", "");
   }

   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

   String checkHome = comc.getECSHOME();
   if (comcr.hCallBatchSeqno.length() > 6) {
       if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
           comcr.hCallBatchSeqno = "no-call";
       }
   }

   comcr.hCallRProgramCode = this.getClass().getName();
   String hTempUser = "";
   if (comcr.hCallBatchSeqno.length() == 20) {
       comcr.callbatch(0, 0, 1);
       selectSQL = " user_id ";
       daoTable = "ptr_callbatch";
       whereStr = "WHERE batch_seqno   = ?  ";

       setString(1, comcr.hCallBatchSeqno);
       int recCnt = selectTable();
       hTempUser = getValue("user_id");
   }
   if (hTempUser.length() == 0) {
       hTempUser = comc.commGetUserID();
   }

   hStmtCrtDate = "";
   forcFlag = 0;
   if (args.length == 1) {
       if ((args[0].length() == 1) && (args[0].equals("Y")))
           forcFlag = 1;
       if (args[0].length() == 8)
           hStmtCrtDate = args[0];
   }
   if (args.length == 2) {
       hStmtCrtDate = args[0];
       if (args[1].equals("Y"))
           forcFlag = 1;
   }

   showLogMessage("I", "", "輸入日期="+hStmtCrtDate+","+args.length );

   selectPtrBusinday();
   if(hStmtCrtDate.substring(6, 8).equals("26") == false) {
      exceptExit = 0;
      String stderr = String.format("本程式限每月26日  前執行 [%s]",hStmtCrtDate);
      comcr.errRtn(stderr, "",comcr.hCallBatchSeqno);
   }
   showLogMessage("I", "", String.format("處理月份 [%s]" , hBillAcctMonth));
   if (forcFlag == 0) {
       if (selectIchB10bMonth() != 0) {
    	   exceptExit = 0;
           comcr.errRtn("程式結束", "",comcr.hCallBatchSeqno);
       }
   }

   deleteIchB10bMonth();
   deleteIchStmtHst();

   //selectTscStmtParm();
   //selectIch00Parm();

   selectBilBill();

   insertIchStmtHst();

   // ==============================================
   // 固定要做的
   comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totaCnt + "]";
   showLogMessage("I", "", comcr.hCallErrorDesc);

   if (comcr.hCallBatchSeqno.length() == 20)    comcr.callbatch(1, 0, 1); // 1: 結束

   finalProcess();
   return 0;
  } catch (Exception ex) 
      {
       expMethod = "mainProcess";
       expHandle(ex);
       return exceptExit;
      }
}
/***********************************************************************/
void selectPtrBusinday() throws Exception 
{
  sqlCmd  = "select business_date, ";
  sqlCmd += "to_char(sysdate,'yyyymmdd') h_stmt_crt_date,";
  sqlCmd += "to_char(sysdate,'hh24miss') h_stmt_crt_time, ";
  sqlCmd += "substr(to_char(add_months(sysdate, 1), 'yyyymmdd'), 1,6) || '15' h_request_date";
  sqlCmd += " from ptr_businday  ";

  int recordCnt = selectTable();
  if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_businday not found!", "",comcr.hCallBatchSeqno);
  }
  if(recordCnt > 0) {
     hBusiBusinessDate = getValue("business_date");
     hStmtCrtDate   = hStmtCrtDate.length() == 0 ? getValue("h_stmt_crt_date") : hStmtCrtDate;
     hStmtCrtTime   = getValue("h_stmt_crt_time");
	 hRequestDate    = getValue("h_request_date");
  }

  sqlCmd  = "select to_char(add_months(to_date(cast(? as varchar(10)), 'yyyymmdd'),-1), 'yyyymm') h_bill_prev_month ";
  sqlCmd += "     , to_char(add_months(to_date(cast(? as varchar(10)), 'yyyymmdd'), 1), 'yyyymm') h_bill_next_month  from dual   ";
  setString(1, hStmtCrtDate);
  setString(2, hStmtCrtDate);
if(debug==1)
{
// showLogMessage("I", "", "SQL 2 where= " + sqlCmd);
   showLogMessage("I", "", "h_stmt_crt_date= " + hStmtCrtDate);
}

      recordCnt = selectTable();
  if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_businday not found!", "",comcr.hCallBatchSeqno);
  }
  if (recordCnt > 0) {
      hBillPrevMonth  = getValue("h_bill_prev_month");
      hBillNextMonth  = getValue("h_bill_next_month");
  }
  
  hBillAcctMonth = hStmtCrtDate.substring(0,6);
  hBillFmDate    = hBillPrevMonth + "25";
  hBillToDate    = hBillAcctMonth + "24";
}
/***********************************************************************/
int selectIch00Parm() throws Exception
{
  hAmt01  = 0;
  hAmt03  = 0;
  sqlCmd    = "select amt01, amt03 ";
  sqlCmd   += "  from ich_00_parm ";
  sqlCmd   += " where parm_type = 'ICHM0020' ";
//setString(1, h_bill_acct_month );
    
  int recordCnt = selectTable();
  if (recordCnt > 0) {
      hAmt01   = getValueDouble("amt01");
      hAmt03   = getValueDouble("amt03");
  }

  return (0);
}
/***********************************************************************/
int selectIchB10bMonth() throws Exception 
{
  sqlCmd    = "select 1 cnt ";
  sqlCmd   += " from ich_b10b_month  ";
  sqlCmd   += "where request_date like ? || '%' ";
//sqlCmd   += "  and proc_flag   <> 'Y' ";
  sqlCmd   += "fetch first 1 rows only ";
  setString(1, hBillNextMonth );
     
  int recordCnt = selectTable();
  if (recordCnt > 0) {
      hTempCnt = getValueInt("cnt");
  } else
      return (0);

  showLogMessage("I", "", String.format("本月[%s]簽帳資料已產生, 不可重複執行 , 請通知相關人員處理(error)", hBillNextMonth));
  return (1);
}
/***********************************************************************/
void deleteIchB10bMonth() throws Exception 
{
  daoTable  = "ich_b10b_month";
  whereStr  = "where request_date like ? || '%' ";
  whereStr += "  and proc_flag   <> 'Y' ";
  setString(1, hBillNextMonth);
  deleteTable();

}
/***********************************************************************/
void deleteIchStmtHst() throws Exception
{
  daoTable  = "ich_stmt_hst";
  whereStr  = "where acct_month   like ? || '%' ";
  setString(1, hBillAcctMonth);
  deleteTable();

}

/***********************************************************************/
void selectTscStmtParm() throws Exception {
    hTspmItemEnameBl = "";
    hTspmItemEnameCa = "";
    hTspmItemEnameId = "";
    hTspmItemEnameAo = "";
    hTspmItemEnameIt = "";
    hTspmItemEnameOt = "";
    hTspmExclMchtFlag = "";

    sqlCmd = "select item_ename_bl,";
    sqlCmd += "item_ename_ca,";
    sqlCmd += "item_ename_id,";
    sqlCmd += "item_ename_ao,";
    sqlCmd += "item_ename_it,";
    sqlCmd += "item_ename_ot,";
    sqlCmd += "excl_mcht_flag, ";
    sqlCmd += "excl_mcc_flag, ";
    sqlCmd += "excl_mcht_group_flag ";
    sqlCmd += " from tsc_stmt_parm  ";
    sqlCmd += "where apr_flag = 'Y' fetch first 1 rows only";
    selectTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("select_tsc_stmt_parm not found!", "", comcr.hCallBatchSeqno);
    }
    hTspmItemEnameBl  = getValue("item_ename_bl");
    hTspmItemEnameCa  = getValue("item_ename_ca");
    hTspmItemEnameId  = getValue("item_ename_id");
    hTspmItemEnameAo  = getValue("item_ename_ao");
    hTspmItemEnameIt  = getValue("item_ename_it");
    hTspmItemEnameOt  = getValue("item_ename_ot");
    hTspmExclMchtFlag = getValue("excl_mcht_flag");
    hTspmExclMccFlag  = getValue("excl_mcc_flag");
    hTspmExclMchtGroupFlag = getValue("excl_mcht_group_flag");

}

	/***********************************************************************/
	void selectBilBill() throws Exception {

		sqlCmd = "SELECT a.batch_date,a.ecs_real_card_no,a.ecs_platform_kind,a.mcht_chi_name,    ";
		sqlCmd += "       a.ecs_sign_code,a.mcc_code,a.ecs_tx_code,round(a.dest_amt) dest_amt,   ";
		sqlCmd += "       a.ecs_bill_type                                                        ";
		sqlCmd += "FROM bil_fiscdtl a,                                                           ";
		sqlCmd += "    (select card_no                                                           ";
		sqlCmd += "     from ich_card                                                            ";
		sqlCmd += "     where 1=1                                                                ";
		sqlCmd += "     and autoload_flag  = 'Y'                                                 ";
		sqlCmd += "     and ((current_code ='0' and new_end_date > ? ) or                        ";
		sqlCmd += "          (current_code!='0' and oppost_date  > ? ))                          ";
		sqlCmd += "     and ((decode(lock_date   ,'','30001231',lock_date   ) > ? ) and          ";
		sqlCmd += "          (decode(balance_date,'','30001231',balance_date) > ? ) and          ";
		sqlCmd += "          (decode(return_date ,'','30001231',return_date ) > ? ))             ";
		sqlCmd += "     GROUP by card_no ) c                                                     ";
		sqlCmd += "WHERE a.card_no = c.card_no                                                   ";
		sqlCmd += "AND   a.batch_date    between ?  and ?                                        ";
		sqlCmd += "AND   a.ecs_bill_type = 'FISC'                                                ";
		
		setString(1, hBillToDate);
		setString(2, hBillToDate);
		setString(3, hBillToDate);
		setString(4, hBillToDate);
		setString(5, hBillToDate);
		setString(6, hBillFmDate);
		setString(7, hBillToDate);

		//平台交易
		String[] listOfSkipKind = new String[]
			    {"f1","G1","G2","d1","M1","e1",
			     "10","11","12","13","14",
			     "20","21","22","23","24","25",
			     "V1","V2","V3","V4","V5","V6",
			     "FL","CL"};
		
		//特定MCC
		String[] listOfSkipMCC = new String[]
				{"9311","8398","0000","","0037","5960",
			     "5965","6300"};

		String keepCardNo = "";
		boolean firstCardNo = true;
		
		openCursor();
		while (fetchTable()) {
			
			for (int i = 0; i < listOfSkipKind.length; i++) {
				if (getValue("ecs_platform_kind").equals(listOfSkipKind[i])) {
					continue;
				}
			}
			
			for (int j = 0; j < listOfSkipMCC.length; j++) {
				if (getValue("mcc_code").equals(listOfSkipMCC[j])) {
					continue;
				}
			}
			
			//特店中文內含保險兩個字
			if (getValue("mcht_chi_name").indexOf("保險") > 0 ) continue;
			
			hBillRealCardNo = getValue("ecs_real_card_no");
			if (keepCardNo.equals(hBillRealCardNo) == false ) {
				if (firstCardNo==false) {
					if (hBillDestinationAmt > 0) {
						insertIchB10bMonth(keepCardNo);
					}
				}
				
				keepCardNo = hBillRealCardNo;
				hBillDestinationAmt = 0;
				hBillDestinationCnt = 0;
				firstCardNo = false;
			}
			
			if ("+".equals(getValue("ecs_sign_code"))) {
				hBillDestinationAmt += getValueDouble("dest_amt");
			} else {
				hBillDestinationAmt -= getValueDouble("dest_amt");
			}
			
			hBillDestinationCnt ++;

			hDestAllAmt = hDestAllAmt + hBillDestinationAmt;
			hFeedAllAmt = hFeedAllAmt + Math.round(hBillDestinationAmt * hAmt01 / 100); /* V1.01.01 modify */

			totaCnt++;
		}
		
		if (hBillDestinationAmt > 0) {
			insertIchB10bMonth(keepCardNo);
		}

		closeCursor();

	}

/***********************************************************************/
void insertIchStmtHst() throws Exception
{

 setValue("acct_month"         , hBillAcctMonth  );
 setValueDouble("dest_amt"     , hDestAllAmt);
 setValueDouble("feedback_amt" , hFeedAllAmt);
 setValueDouble("autoadd_amt"  , hAutoaddAllAmt);
 setValueDouble("autoadd_fee"  , (hAutoaddAllAmt * hAmt03 / 100));
 setValue("mod_time"           , sysDate + sysTime);
 setValue("mod_pgm"            , javaProgram);
 daoTable = "ich_stmt_hst";
 insertTable();
 if (dupRecord.equals("Y")) {
	 showLogMessage("E","","insert ich_stmt_hst duplicate!");
  }
}
/***********************************************************************/
void insertIchB10bMonth(String keepCardNo) throws Exception 
{
if(debug==1) showLogMessage("I", "", "  INSERT=["+hBillRealCardNo+"]");
sqlCmd = "select ich_card_no from ich_card where card_no = ? order by crt_date desc ";
setString(1, keepCardNo);
selectTable();

  setValue("request_date" , hBillNextMonth+ "15");
  setValue("request_time" , "153000");
  setValue("ich_card_no"  , getValue("ich_card_no"));
  setValueInt("tx_cnt"    , hBillDestinationCnt);
  setValueDouble("tx_amt" , hBillDestinationAmt);
  setValue("sys_date"     , sysDate);
  setValue("sys_time"     , sysTime);
  setValue("proc_flag"    , "N");
  setValue("ok_flag"      , "N");
  setValue("mod_time"     , sysDate + sysTime);
  setValue("mod_pgm"      , javaProgram);
  daoTable = "ich_b10b_month";
  insertTable();
  if (dupRecord.equals("Y")) {
	  showLogMessage("E","","insert" + daoTable + " "+ keepCardNo + "duplicate!");
  }
}
/***********************************************************************/
public static void main(String[] args) throws Exception 
{
   IchB001 proc = new IchB001();
   int retCode = proc.mainProcess(args);
   proc.programEnd(retCode);
}
/***********************************************************************/
}
