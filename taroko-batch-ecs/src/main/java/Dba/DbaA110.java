/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version     AUTHOR              DESCRIPTION                     *
*  --------  ---------- ---------  ------------------------------------------*
* 109/02/10  V1.00.00     Rou      initial(Reference CycA040 program)        *
* 109-11-11  V1.01.02   yanghan    修改了變量名稱和方法名稱                  *
* 112/08/16  V1.00.03     Ryan     每月01才執行                              *
* 112/08/18  V1.00.04     Ryan     dbm_month_stat where 條件增加  ACCT_MONTH=  BUSINESS_DATE(前6碼)*
* 112-08-23  V1.00.05   Simon      1.改以ptr_businday.this_close_date判斷當日是否為關帳日*
*                                  2.改以loadtable 讀取 dbm_month_stat       *
*                                  3.帳單 bonus display 調整                 *
* 112/08/25  V1.00.06     Ryan     改為讀取     DBA_ACNO.E_MAIL_EBILL   寫入 DBA_ACMM.E_MAIL_ADDR                              *
* 112-08-26  V1.00.07   Simon      1.擷取DBA_ACNO.E_MAIL_ADDR 更正為 DBA_ACNO.E_MAIL_EBILL*
*                                  2.帳單VD紅利調整點數需包含 diff_bp        *
* 112/08/30  V1.00.08     Ryan     執行前先DELETE DBA_ACMM,DBA_ABEM          *  
* 112/09/18  V1.00.09     Ryan     新增欄位DBA_ACMM.UNPRINT_FLAG_REGULAR
* 112/09/25  V1.00.10   Simon      shell cyc002、cyc003並行執行日期控制      *
******************************************************************************/
package Dba;

import com.*;

import Dxc.Util.SecurityUtil;

import java.io.File;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("unchecked")
public class DbaA110 extends AccessDAO
{
	private String progname = "產生VD對帳單抬頭單筆資料區處理程式 112/09/25 "
	                        + "V1.00.10";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString comStr = new CommString();
	CommCrdRoutine comcr = null;

	String hInputExeDateFlag = "";
	String hBusiBusinessDate = "";
	String hCurrBusinessDate = "";
	String hWdayStmtCycle = "";
	String hAcnoPSeqno = "";
	String hAcnoStmtCycle = "";
	String hAcnoAcctNo = "";
	String hAcnoAcctType = "";
	String hAcnoAcctKey = "";
	String hAcnoAcctStatus = "";
	String hAcnoIdPSeqno = "";
	String hAcnoBillSendingZip = "";
	String hAcnoBillSendingAddr1 = "";
	String hAcnoBillSendingAddr2 = "";
	String hCardCardNo = "";
	String hIdnoNation = "";
	String hIdnoEMailAddr = "";
	String hIdnoChineseName = "";
	String hIdnoSex = "";
	String hIdnoBirthday = "";
	String ptrBusinessDate = "";
	String hUnprintFlagRegular = "";
	String hStatSendInternet = "";
	String hStatSendSMonth = "";
	String hStatSendEMonth = "";
	int hDmsLastMonthBp;
	int hDmsNewAddBp;
	int hDmsTempBp;
	int hDmsAdjustBp;
	int hDmsAdjBp;
	int hDmsUseBp;
	int hDmsGivBp;
	int hDmsRemBp;
	int hDmsMovBp;
	int hDmsInpBp;
	int hDmsThisMonthBp;
	int hDmsDiffBp;

  String hLastAcctMonth = "";
  CommDate  commDate = new CommDate();
	long totalCnt = 0, currCnt = 0;
	int debut = 1;
	int paymentAmt = 0, insertCnt = 0, updateCnt = 0, pcodCnt = 0;
	int[] checkInt = new int[10];

// ************************************************************************
	public static void main(String[] args) throws Exception {
		DbaA110 proc = new DbaA110();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (args.length > 1) {
				showLogMessage("I", "", "請輸入參數:");
				showLogMessage("I", "", "PARM 1 : [business_date]");
				return (1);
			}

			if (!connectDataBase())
				return (1);
			
		  selectPtrBusinday();
			showLogMessage("I", "", "本日營業日 : ["+hCurrBusinessDate+"]");

			if ((args.length >= 1) && (args[0].length() == 8)) {
				hBusiBusinessDate = args[0];
				hInputExeDateFlag = "Y";
			}

			if (hInputExeDateFlag.equals("Y")) {
  			showLogMessage("I", "", "人工執行關帳日 : ["+hBusiBusinessDate+"]");
			} else {
  			showLogMessage("I", "", "系統執行關帳日 : ["+hBusiBusinessDate+"]");
			}
			
			/** Check whether businessDate is workday **/
			if (isWordDay(hBusiBusinessDate) == false) {
				showLogMessage("I", "", "本日非符合執行關帳日, 程式結束");
				return (0);
			}

			selectLastAcctMonth();
      loadDbmMonthStat();
		  deleteDbaAcmm();
		  deleteDbaAbem();
			dataProcess();
      
			showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "]");

			finalProcess();
			return (0);
		}

		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess
// ************************************************************************
//private String selectPtrBusinday() throws Exception {
	void selectPtrBusinday() throws Exception {
		selectSQL = " BUSINESS_DATE,THIS_CLOSE_DATE ";
		daoTable = "PTR_BUSINDAY";
		whereStr = "FETCH FIRST 1 ROW ONLY";

		selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select ptr_businday error!");
			exitProgram(1);
		}

	  hCurrBusinessDate = getValue("BUSINESS_DATE");
		hBusiBusinessDate = getValue("THIS_CLOSE_DATE");
		return;
	}
	
	// ************************************************************************ 
		public boolean isWordDay(String businessDate) throws Exception {
			CommString  commStr = new CommString();
			if(!"01".equals(commStr.right(businessDate, 2))) {
				return false;
			}
			extendField = "wday.";
			selectSQL = "stmt_cycle";
			daoTable = "ptr_workday";
			whereStr = "where this_close_date = ? ";

			setString(1, businessDate);

			selectTable();

			if (notFound.equals("Y"))
				return false;

			return true;
		}

// ************************************************************************
	void dataProcess() throws Exception {
		sqlCmd = "select a.p_seqno,";
		sqlCmd += "a.stmt_cycle,";
		sqlCmd += "a.acct_no,";
		sqlCmd += "a.acct_type,";
		sqlCmd += "a.acct_key,";
		sqlCmd += "a.acct_status,";
		sqlCmd += "a.id_p_seqno,";
		sqlCmd += "a.bill_sending_zip,";
		sqlCmd += "a.bill_sending_zip,";
		sqlCmd += "a.bill_sending_addr1,";
		sqlCmd += "a.bill_sending_addr2,";
		sqlCmd += "a.stat_send_internet,";//訂閱對帳單寄送方式旗標-網際網路
		sqlCmd += "a.stat_send_s_month,";
		sqlCmd += "a.stat_send_e_month,";
		sqlCmd += "b.card_no,";
		sqlCmd += "c.nation,";
	//sqlCmd += "a.e_mail_addr,";
		sqlCmd += "a.e_mail_ebill,";
		sqlCmd += "c.chi_name,";
		sqlCmd += "c.sex,";
		sqlCmd += "c.birthday ";
		sqlCmd += "from dba_acno a, dbc_card b, dbc_idno c ";
		sqlCmd += "where a.acct_type = '90' ";
		sqlCmd += "and a.id_p_seqno = b.id_p_seqno ";
		sqlCmd += "and a.id_p_seqno = c.id_p_seqno ";
		sqlCmd += "and a.acct_status = '1' ";
	//sqlCmd += "fetch first 1 rows only";
		int recordCnt = openCursor();

		while (fetchTable(recordCnt)) {
			hAcnoPSeqno = getValue("p_seqno");
			hAcnoStmtCycle = getValue("stmt_cycle");
			hAcnoAcctNo = getValue("acct_no");
			hAcnoAcctType = getValue("acct_type");
			hAcnoAcctKey = getValue("acct_key");
			hAcnoAcctStatus = getValue("acct_status");
			hAcnoIdPSeqno = getValue("id_p_seqno");
			hAcnoBillSendingZip = getValue("bill_sending_zip");
			hAcnoBillSendingAddr1 = getValue("bill_sending_addr1");
			hAcnoBillSendingAddr2 = getValue("bill_sending_addr2");
			hCardCardNo = getValue("card_no");
			hIdnoNation = getValue("nation");
		//hIdnoEMailAddr = getValue("e_mail_addr");
			hIdnoEMailAddr = getValue("e_mail_ebill");
			hIdnoChineseName = getValue("chi_name");
			hIdnoSex = getValue("sex");
			hIdnoBirthday = getValue("birthday");
			//showLogMessage("I", "", "id_p_seqno = [" + hAcnoIdPSeqno + "]");
			//showLogMessage("I", "", "card_no = [" + hCardCardNo + "]");
			hStatSendInternet = getValue("stat_send_internet");
			hStatSendSMonth = getValue("stat_send_s_month");
			hStatSendEMonth = getValue("stat_send_e_month");
//			if (!hAcnoAcctStatus.equals("1"))
//				continue;

			//if (selectDbmMonthStat() == 0)
			//	continue;
		//selectDbmMonthStat();

      setValue("dmstat.p_seqno"    ,hAcnoPSeqno);
      int cnt1 = getLoadData("dmstat.p_seqno");
      if (cnt1<=0) {
  			hDmsLastMonthBp = 0;
  			hDmsNewAddBp = 0;
  			hDmsAdjBp = 0;
  			hDmsUseBp = 0;
  			hDmsGivBp = 0;
  			hDmsRemBp = 0;
  			hDmsMovBp = 0;
  			hDmsInpBp = 0;
  		  hDmsThisMonthBp = 0;
  			hDmsDiffBp = 0;
      } else {
  			hDmsLastMonthBp = getValueInt("dmstat.last_month_bp");
  			hDmsNewAddBp = getValueInt("dmstat.new_bp");
  			hDmsAdjBp = getValueInt("dmstat.adj_bp");
  			hDmsUseBp = getValueInt("dmstat.use_bp");
  			hDmsGivBp = getValueInt("dmstat.giv_bp");
  			hDmsRemBp = getValueInt("dmstat.rem_bp");
  			hDmsMovBp = getValueInt("dmstat.mov_bp");
  			hDmsInpBp = getValueInt("dmstat.inp_bp");
  		  hDmsThisMonthBp = getValueInt("dmstat.this_month_bp");
  			hDmsDiffBp = getValueInt("dmstat.diff_bp");
      }

			totalCnt++;
			insertDbaAcmm();
		}
		closeCursor(recordCnt);
		return;
	}

//************************************************************************
/***
	int selectDbmMonthStat() throws Exception {
		sqlCmd = "select id_p_seqno,";
		sqlCmd += "last_month_bp,";
		sqlCmd += "new_bp,";
		sqlCmd += "adj_bp,";
		sqlCmd += "use_bp,";
		sqlCmd += "giv_bp,";
		sqlCmd += "rem_bp,";
		sqlCmd += "mov_bp,";
		sqlCmd += "inp_bp,";
		sqlCmd += "diff_bp,";
		sqlCmd += "this_month_bp,";
		sqlCmd += "diff_bp ";
		sqlCmd += "from dbm_month_stat ";
		sqlCmd += "where p_seqno = ? and acct_month = ? ";
		setString(1, hAcnoPSeqno);
		setString(2, comStr.left(hBusiBusinessDate, 6));
		int recordCnt = selectTable();

		if (recordCnt > 0) {
			hDmsLastMonthBp = getValueInt("last_month_bp");
			hDmsNewAddBp = getValueInt("new_bp");
			hDmsAdjBp = getValueInt("adj_bp");
			hDmsUseBp = getValueInt("use_bp");
			hDmsGivBp = getValueInt("giv_bp");
			hDmsRemBp = getValueInt("rem_bp");
			hDmsMovBp = getValueInt("mov_bp");
			hDmsInpBp = getValueInt("inp_bp");
		  hDmsThisMonthBp = getValueInt("this_month_bp");
			hDmsDiffBp = getValueInt("diff_bp");
			return 1;
		}
		return 0;
	}
***/
//************************************************************************
  void loadDbmMonthStat() throws Exception
  {
   extendField = "dmstat.";
   selectSQL = "id_p_seqno,"
             + "p_seqno,"
             + "last_month_bp,"
             + "new_bp,"
             + "adj_bp,"
             + "use_bp,"
             + "giv_bp,"
             + "rem_bp,"
             + "mov_bp,"
             + "inp_bp,"
             + "diff_bp,"
             + "this_month_bp ";
   daoTable  = "dbm_month_stat";
   whereStr  = "where acct_month = ? "
             ;
 
	 setString(1, comStr.left(hBusiBusinessDate, 6));
 
   int  n = loadTable();
   setLoadData("dmstat.p_seqno");
 
   showLogMessage("I","","Load dbm_month_stat Count: ["+n+"]");
  }

// ************************************************************************
	int insertDbaAcmm() throws Exception {
		/* ptrBusinessDate = "";
		CommDate  commDate = new CommDate();
		sqlCmd = "select substr(business_date, 1, 6) business_date ";
		sqlCmd += "from ptr_businday";
		int recordCnt = selectTable();
		if (recordCnt > 0)
			  ptrBusinessDate = getValue("business_date");  */
		hUnprintFlagRegular = getUnprintFlagRegular();
		
		setValue("p_seqno", hAcnoPSeqno);
		setValue("acct_month", hLastAcctMonth);
		setValue("stmt_cycle", hAcnoStmtCycle);
		setValue("acct_no", hAcnoAcctNo);
		setValue("acct_type", hAcnoAcctType);
		setValue("acct_key", hAcnoAcctKey);
		setValue("id_p_seqno", hAcnoIdPSeqno);
		setValue("zip_code", hAcnoBillSendingZip);
		setValue("zip_code_chin", hAcnoBillSendingZip);
		setValue("mail_addr_1", hAcnoBillSendingAddr1);
		setValue("mail_addr_2", hAcnoBillSendingAddr2);
		setValue("card_no", hCardCardNo);
		setValue("nation", hIdnoNation);
		setValue("e_mail_addr", hIdnoEMailAddr);
		setValue("chinese_name", hIdnoChineseName);
		setValue("chi_title", hIdnoSex);
		setValue("birthday", hIdnoBirthday);
		setValueInt("last_month_bonus", hDmsLastMonthBp);
	//setValueInt("new_add_bonus", hDmsNewAddBp);
	  hDmsTempBp = hDmsGivBp + hDmsNewAddBp + hDmsInpBp;
		setValueInt("new_add_bonus", hDmsTempBp);
	//hDmsAdjustBp = hDmsGivBp + hDmsAdjBp + hDmsRemBp + hDmsMovBp + hDmsInpBp + hDmsDiffBp;
	//setValueInt("adjust_bonus", hDmsAdjustBp);
	  hDmsTempBp = hDmsAdjBp + hDmsMovBp + hDmsDiffBp;
		setValueInt("adjust_bonus", hDmsTempBp);
		setValueInt("use_bonus", hDmsUseBp);
		setValueInt("net_bonus", hDmsThisMonthBp);
		setValueInt("erase_bonus", hDmsRemBp);//到期移除點數
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", "DbaA110");
		setValue("unprint_flag_regular",hUnprintFlagRegular);

		daoTable = "dba_acmm";

		insertTable();

		return (0);
	}

//************************************************************************ 
    void selectLastAcctMonth() throws Exception {
        hLastAcctMonth = "";
        hLastAcctMonth = commDate.dateAdd(hBusiBusinessDate, 0, -1, 0).substring(0,6) ;
        /* 
        sqlCmd = "select to_char(add_months(to_date(business_date,'yyyymmdd'),-1),'yyyymm') LastAcctMonth ";
		    sqlCmd += "from ptr_businday fetch first 1 rows only";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hLastAcctMonth = getValue("LastAcctMonth");
        }  */
    }
    
   void deleteDbaAcmm() throws Exception {
		daoTable = "DBA_ACMM";
    	whereStr = " where ACCT_MONTH = ? ";
    	setString(1,hLastAcctMonth);
    	int n = deleteTable();
    	showLogMessage("I","","DELETE  DBA_ACMM COUNT: ["+n+"]");
   }

   void deleteDbaAbem() throws Exception {
		daoTable = "DBA_ABEM";
    	whereStr = " where ACCT_MONTH = ? ";
    	setString(1,hLastAcctMonth);
    	int n = deleteTable();
    	showLogMessage("I","","DELETE  DBA_ABEM COUNT: ["+n+"]");
   }
   
   String getUnprintFlagRegular(){
	   String hBusiBusinessMonth = comStr.left(hBusiBusinessDate, 6);
	   if("Y".equals(hStatSendInternet) &&
		   comStr.ss2int(hStatSendSMonth) <= comStr.ss2int(hBusiBusinessMonth) && 
		   comStr.ss2int(comStr.empty(hStatSendEMonth)?"999999":hStatSendEMonth) >= comStr.ss2int(hBusiBusinessMonth)) {
		   return "N";
	   }
	   return "Y";
   }
   
} // End of class FetchSample
