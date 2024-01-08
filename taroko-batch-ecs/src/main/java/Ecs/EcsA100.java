/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
* 112/11/27  V1.00.01  Simon      initial                                    *
* 112/11/27  V1.00.02  Simon      控制剛好等份時之 limit ?,1                 *
******************************************************************************/
package Ecs;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class EcsA100 extends AccessDAO {
	private String PROGNAME = "設定批次程式分批執行控制檔 112/11/27 V1.00.02";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	String checkHome = "";
//String hBusiBusinessDate = "";
  String wdayKey="wday.stmt_cycle";
  String hBusiDate="",hProgramId="",hStmtCycle="",hNextAcctMonth="",
         hProgramName="",hShellId="";
  int    hDivideSeq=0, hBatchSeq=0, hPSeqnoCnt=0, hEachPartCnt=0,
         hPriorityLevel=0;
  String hBeginKey="",hStopKey="";
  int[] aPartSeq     = new int[100];
  String[] aBeginKey = new String[100];
  String[] aStopKey  = new String[100];

	// ************************************************************************
	public static void main(String[] args) throws Exception {
		EcsA100 proc = new EcsA100();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
		return;
	}

	// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);
			checkHome = System.getenv("PROJ_HOME");

			// 固定要做的
			if (!connectDataBase()) {
        showLogMessage("I","","connect DataBase Error!");
        return 1;
			}

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

      if (args.length < 2 || args.length > 3) {
          showLogMessage("I","","請輸入 2 or 3 個適合的參數:");
          showLogMessage("I","","PARM 1 : [BUSINESS_DATE]/[PROGRAM_ID]");
          showLogMessage("I","","PARM 2 : [PROGRAM_ID]/[DIVIDE_SEQ]");
          showLogMessage("I","","PARM 3 : [DIVIDE_SEQ]");
          return(0);
      }
 
      if ( args.length == 2 ) {
      	if (Arrays.asList("ActE004","ActE010")
      	  .contains(args[0])) {
          hProgramId = args[0];
        } 
      	if (args[1].length()<=2) {
          hDivideSeq = comcr.str2int(args[1]);
        }
      } else if ( args.length == 3 ) {
      	if (args[0].length()==8 && args[0].chars().allMatch( Character::isDigit)) {
          hBusiDate = args[0]; 
        }
      	if (Arrays.asList("ActE004","ActE010")
      	  .contains(args[1])) {
          hProgramId = args[1];
        } 
      	if (args[2].length()<=2) {
          hDivideSeq = comcr.str2int(args[2]);
        }
      }
      
			selectPtrBusinday();
			
    	if (!Arrays.asList("ActE004","ActE010")
     	  .contains(hProgramId) || hDivideSeq < 2 || hDivideSeq > 9) {
        showLogMessage("I","","請輸入 2 or 3 個適合的參數:");
        showLogMessage("I","","PARM 1 : [BUSINESS_DATE]/[PROGRAM_ID]");
        showLogMessage("I","","PARM 2 : [PROGRAM_ID]/[DIVIDE_SEQ]");
        showLogMessage("I","","PARM 3 : [DIVIDE_SEQ]");
        return(0);
      }
      
      if (hProgramId.equals("ActE010")) {
        if (loadPtrWorkday()==0){
        	return 0;
        }
      }

 			deleteEcsBatchSegment();
		
			int n1=setBatchBeginStopKey();
      if (n1==0) {
      //showLogMessage("I","","*** Warning! 不符分批執行規格");
        return 0;
      }

			int n2=setBatchPriorityLevel();
      if (n2==0) {
      //showLogMessage("I","","*** Warning! 不符排程控制規格");
        return 0;
      }

			finalProcess();
			return 0;
		}

		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess

  /******************* SELECT *************/
  public int  selectPtrBusinday() throws Exception
  {
    daoTable    = "ptr_businday";
    extendField = "busi.";
    selectSQL   = "decode( cast(? as varchar(8)) ,'',business_date, ? ) "
                + "as business_date ";
    setString(1, hBusiDate);
    setString(2, hBusiDate);
    whereStr    = " fetch first 1 rows only ";
    int n = selectTable();
    if ( n == 0 )
       { showLogMessage("E","","select_ptr_businday ERROR "); exitProgram(3); }
    hBusiDate = getValue("busi.business_date");
    return n;
  }
	
	/*******************************************************************/
	void deleteEcsBatchSegment() throws Exception {

		int idxParm = 0;
		
		daoTable = "ecs_batch_segment";
		whereStr = " where program_id = ? and  business_date = ? ";

		setString(++idxParm, hProgramId);
		setString(++idxParm, hBusiDate);
		
		deleteTable();
		
	}

/******************* LOAD *************/
  public int  loadPtrWorkday() throws Exception
  {
     daoTable    = "ptr_workday";
     extendField = "wday.";
     selectSQL   = "stmt_cycle,"
                 + "this_acct_month,"
                 + "last_acct_month,"
                 + "next_acct_month,"
                 + "this_close_date,"
                 + "last_close_date,"
                 + "next_close_date ";
     whereStr    = "order by stmt_cycle";
     int n = loadTable();
     setLoadData(wdayKey);
     for ( int i=0; i<n; i++ )  {
       if ( getList("wday.next_close_date",i).equals(getValue("busi.business_date")) ) {
         hStmtCycle = getList("wday.stmt_cycle",i);
         hNextAcctMonth = getList("wday.next_acct_month",i);
         showLogMessage("I","","TODAY IS AN CYCLE_DATE : "+hNextAcctMonth+hStmtCycle);
       }
     }
 
     if ( hStmtCycle.length() == 0 )
        { showLogMessage("I","","TODAY NOT AN CYCLE_DATE ！ "); return 0; }
     return n;
  }

  /****************************************************/
  public int  setBatchBeginStopKey() throws Exception
  {
    hPSeqnoCnt = 0;

    if (hProgramId.equals("ActE010")) {
      hProgramName="卡人台幣基金銷帳處理程式";
      sqlCmd  = " select count(*) cnt_p_seqno from ";
      sqlCmd += "   (select distinct a.p_seqno from ";
      sqlCmd += "     (select p_seqno,fund_code from mkt_cashback_dtl ";
      sqlCmd += "      where p_seqno in ";
      sqlCmd += "      (select p_seqno from act_acno where stmt_cycle = ? ) ";
      sqlCmd += "      group by P_SEQNO,FUND_CODE having sum(end_tran_amt) > 0 ";
      sqlCmd += "     ) a ";
      sqlCmd += "   ) ";
      setString(1, hStmtCycle);
    } else if (hProgramId.equals("ActE004")) {
      hProgramName="卡人繳款銷帳處理程式";
      sqlCmd  = " select count(*) cnt_p_seqno from ";
      sqlCmd += "   (select distinct p_seqno from act_debt_cancel ";
      sqlCmd += "    WHERE process_flag != 'Y' ) ";
    }
    int recordCnt1 = selectTable();
    if (recordCnt1 > 0) {
      hPSeqnoCnt = getValueInt("cnt_p_seqno");
    }

    if (hPSeqnoCnt < hDivideSeq) {
      showLogMessage("I","","cnt_divide_seq = "+hDivideSeq);
      showLogMessage("I","","cnt_p_seqno = "+hPSeqnoCnt);
      showLogMessage("I","","*** Warning! 不符分批執行規格");
      return 0;
    }

	//double tempDouble = (double) (hPSeqnoCnt/hDivideSeq);
		hEachPartCnt = (int)Math.floor(hPSeqnoCnt/hDivideSeq);

  	for (int i =1; i<=hDivideSeq ; i++) {
      aPartSeq[i] = hEachPartCnt*i;
 	  }

  	for (int ii=1; ii<=hDivideSeq ; ii++) {
      if (hProgramId.equals("ActE010")) {
        sqlCmd  = "  select distinct a.p_seqno as p_seqno from ";
        sqlCmd += "    (select p_seqno,fund_code from mkt_cashback_dtl ";
        sqlCmd += "     where p_seqno in ";
        sqlCmd += "     (select p_seqno from act_acno where stmt_cycle = ? ) ";
        sqlCmd += "     group by P_SEQNO,FUND_CODE having sum(end_tran_amt) > 0 ";
        sqlCmd += "    ) a ";
        sqlCmd += "   order by a.p_seqno limit ?,1 ";
        setString(1, hStmtCycle);
        setInt(2, aPartSeq[ii]-1);
   	  } else if (hProgramId.equals("ActE004")) {
        sqlCmd  = "   select distinct p_seqno from act_debt_cancel ";
        sqlCmd += "    where process_flag != 'Y' ";
        sqlCmd += "    order by p_seqno limit ?,1 ";
        setInt(1, aPartSeq[ii]-1);
   	  }
      int recordCnt2 = selectTable();
      if (recordCnt2 > 0) {
        if (ii == hDivideSeq) {
          aBeginKey[ii]=aStopKey[ii-1];
          aStopKey[ii] ="9999999999";
        } else if (ii == 1) {
          aBeginKey[ii]="";
          aStopKey[ii] =getValue("p_seqno");
        } else {
          aBeginKey[ii]=aStopKey[ii-1];
          aStopKey[ii] =getValue("p_seqno");
        }
      }
    }

  	for (int ii=1; ii<=hDivideSeq ; ii++) {
      hBatchSeq=ii;
      hBeginKey=aBeginKey[ii];
      hStopKey =aStopKey[ii];
      insertEcsBatchSegment();
    }
 
    return hEachPartCnt;
  }
	
	//************************************************************************
	public void insertEcsBatchSegment() throws Exception {
		daoTable = "ecs_batch_segment";

		setValue("program_id", hProgramId);
		setValue("business_date", hBusiDate);
		setValueInt("batch_seq", hBatchSeq);
		setValue("program_name", hProgramName);
		setValue("begin_key", hBeginKey);
		setValue("stop_key", hStopKey);
		setValue("in_proc_flag", "N");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		setValue("mod_seqno", "1");

		insertTable();

		if (dupRecord.equals("Y")) {
      showLogMessage("I","","program_id = "+hProgramId);
      showLogMessage("I","","business_date = "+hBusiDate);
      showLogMessage("I","","batch_seq = "+hBatchSeq);
			showLogMessage("I", "", "insert_ecs_batch_segment error[dupRecord]");
			return;
		}
		return;
	}

  /****************************************************/
  public int  setBatchPriorityLevel() throws Exception
  {
    hPriorityLevel = 0;

   	if (Arrays.asList("ActE004","ActE010")
    	  .contains(hProgramId)) {
       hShellId= "cr_d_act001A";
    }
      
    sqlCmd  = " select * from ";
    sqlCmd += "   ecs_batch_ctl ";
    sqlCmd += "  where batch_date = ? and shell_id = ? ";
    sqlCmd += "    and pgm_id = ? ";
    sqlCmd += "  order by priority_level ";
    sqlCmd += "  fetch first 1 rows only ";
    setString(1, hBusiDate);
    setString(2, hShellId);
    setString(3, hProgramId);
    int recordCnt1 = selectTable();
    if (recordCnt1 > 0) {
      hPriorityLevel = getValueInt("priority_level");
    }

    if (hPriorityLevel > 0) {
 			deleteEcsBatchCtl();
    } else {
      sqlCmd  = " select * from ";
      sqlCmd += "   ecs_batch_setting ";
      sqlCmd += "  where shell_id = ? ";
      sqlCmd += "    and pgm_id = ? ";
      sqlCmd += "  order by priority_level ";
      sqlCmd += "  fetch first 1 rows only ";
      setString(1, hShellId);
      setString(2, hProgramId);
      int recordCnt2 = selectTable();
      if (recordCnt2 > 0) {
        hPriorityLevel = getValueInt("priority_level");
      }
    }

    if (hPriorityLevel==0) {
      showLogMessage("I","","shell_id = "+hShellId);
      showLogMessage("I","","pgm_id   = "+hProgramId);
      showLogMessage("I","","*** Warning! 不符排程控制規格");
      return 0;
    }

  	for (int ii=1; ii<=hDivideSeq ; ii++) {
      if (ii > 1) {
        hPriorityLevel++;
      }
      hBatchSeq=ii;
      insertEcsBatchCtl();
    }
 
    return hPriorityLevel;
  }

 
	/*******************************************************************/
	void deleteEcsBatchCtl() throws Exception {

		int idxParm = 0;
		
		daoTable = "ecs_batch_ctl";
		whereStr = " where batch_date = ? and shell_id = ? "
		         + " and pgm_id = ? ";

		setString(++idxParm, hBusiDate);
		setString(++idxParm, hShellId);
		setString(++idxParm, hProgramId);
		
		deleteTable();
		
	}

	//************************************************************************
	public void insertEcsBatchCtl() throws Exception {
		daoTable = "ecs_batch_ctl";

		setValue("batch_date", hBusiDate);
		setValue("batch_time", sysTime);
		setValue("shell_id", getValue("shell_id"));
		setValueInt("priority_level", hPriorityLevel);
		setValue("pgm_id", getValue("pgm_id"));
		setValue("pgm_desc", getValue("pgm_desc"));
		setValue("key_parm1", ""+hBatchSeq);
		setValue("key_parm2", "");
		setValue("key_parm3", "");
		setValue("key_parm4", "");
		setValue("key_parm5", "");
		setValue("wait_flag", "N");
		setValue("proc_flag", "N");
		setValue("proc_desc", "");
		setValue("repeat_code", "");
		setValue("rerun_proc", "");
		setValue("normal_code", "0");
		setValue("call_duty_ind", "Y");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_user", "system");
		setValue("mod_pgm", javaProgram);

		insertTable();

		if (dupRecord.equals("Y")) {
      showLogMessage("I","","batch_date = "+hBusiDate);
      showLogMessage("I","","shell_id = "+getValue("shell_id"));
      showLogMessage("I","","pgm_id = "+getValue("pgm_id"));
      showLogMessage("I","","priority_level = "+hPriorityLevel);
			showLogMessage("I", "", "insert_ecs_batch_ctl error[dupRecord]");
			return;
		}
		return;
	}
} // End of class
