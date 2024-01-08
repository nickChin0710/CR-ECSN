/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112/12/05  V1.00.00  Sunny        initial (copy from RskP183)             *
 *****************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import Cca.CcaOutGoing;

public class ColB102 extends AccessDAO {
	public final boolean debug = false; //debug用
    private String progname = "當日轉催之催收戶轉強停 112/12/05  V1.00.00 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommRoutine    comr     = null;
    CcaOutGoing ccaOutGoing = null;

    final String debitFlag = "N";   //--信用卡
    final String currentCode = "3";   //--強停
    final String oppoReason = "H1";   //--催收戶H1
    
    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";    
//    String hTempSysdate = "";

    String acnoPSeqno = "";
    String cardNo = "";
    String acnoRcUseIndicator = "";
    String acnoStopStatus= "";
    String acnoRowid="";

    String tmpCurrentCode ="";
//    private double hTempBilledEndBal = 0;
//    private int tidAcctSumS1=-1;
//    private int tidDebtS1=-1;

    
    int totalCnt = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : ColB102 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());
            
            showLogMessage("I", "", String.format(" debug = [%s] ", debug));    	     	   
                      
        	selectPtrBusinday();

			if (args.length > 0 && args[0].length() == 8) {
				hBusiBusinessDate = args[0];
			}
				
            showLogMessage("I", "", String.format(" 本日營業日 : [%s],系統日期 : [%s]", hBusiBusinessDate , sysDate));
                    
            procData();

            showLogMessage("I", "", String.format("Total record [%d]筆", totalCnt));
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
    void selectPtrBusinday() throws Exception {

        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "hCallBatchSeqno");
        }
        
        hBusiBusinessDate = getValue("business_date");
 
    }
 
    /***********************************************************************/   
    
    void procData() throws Exception {

    	   sqlCmd = " select A.acno_p_seqno"
    	       +", A.rc_use_indicator "
    	       +", A.stop_status "
    	       +", hex(rowid) as h_acno_rowid "
    	       +" from act_acno A"
    	       +" where a.acct_status='3' and a.status_change_date=? ";
    	   
    	    if(debug)
    	    {
    	    	sqlCmd += "and a.acct_key in ('A275XXXXX460') ";
    	    }

    	   setString(1, hBusiBusinessDate );    	       	   
    	  
    	   daoTable ="act_acno";
    	   
    	   openCursor();
//    	   showLogMessage("I", "", String.format("open-cursor is OK", ""));

    	   while (fetchTable()) {
    	      totalCnt++;
    	      initData();
    	      acnoPSeqno = getValue("acno_p_seqno");
    	      acnoRcUseIndicator = getValue("rc_use_indicator");
    	      acnoStopStatus = getValue("stop_status");
    	      acnoRowid = getValue("h_acno_rowid");
    	      
    	      showLogMessage("I", "", "================================================================");
    	 	  
    	      showLogMessage("I", "", String.format(" p_seqno[%s]", acnoPSeqno));
    	      
    	    //-檢核並確認帳上有欠催收款科目有餘額筆數>0，才繼續處理 --
    	      if (selectActDebt() == 0)
    	      {
    	    	  showLogMessage("I", "", String.format(" 帳上無催收款，欠款餘額為0, p_seqno[%s]", acnoPSeqno));
    	    	  continue;
    	      }
    	    
    		//找出帳戶下最後停卡日的卡片(同一天有多張一併處理)進行強停
    	      selectCrdCard();
    	     
    	      showLogMessage("I", "", String.format(" process count=[%s]", totalCnt));
    	      
    	      if(acnoStopStatus.equals("Y"))
    	      {
    	    	  showLogMessage("I", "", String.format(" 此歸戶層已強停 , acnoRowid[%s]", acnoRowid));
    	    	  continue;    	    	  
              }
    	      
    	      updateActAcno();
    	      
      	      showLogMessage("I", "", "================================================================");
    	   }
    	      
    	   closeCursor();
    	   showLogMessage("I", "", "================================================================");
    	}
  

  //***********************************************************************        
   
    int selectActDebt() throws Exception {
    	
		int hActDebtCnt=0;

		sqlCmd = " SELECT count(*) as db_cnt "
    			+ " from act_debt "
    			+ " where p_seqno =? "
    			+ " and acct_code in ('CI','CC','CB') "
    			+ " and end_bal > 0 ";
    		daoTable ="act_debt";
    		
    		setString(1, acnoPSeqno);
	        
	    int recordCnt = selectTable();
	    if (recordCnt > 0) {   		
	    	hActDebtCnt = getValueInt("db_cnt");
	    	 showLogMessage("I", "", String.format(" ActDebtCnt=[%s]", hActDebtCnt));
	    }

	    if ( hActDebtCnt == 0) return 0;
	    
	    return 1;
    }
	
//***********************************************************************        
  //判斷正卡最大停卡日的卡片，不含附卡
    void selectCrdCard() throws Exception {

 	   sqlCmd  = " select a.card_no,a.current_code ";
 	   sqlCmd += " from crd_card a ";
 	   sqlCmd += " where a.acno_p_seqno = ? ";
 	   sqlCmd += " and a.group_code not in ('1203','1204') "; //排除分期虛擬卡
 	   sqlCmd +=  " and a.oppost_date = (select max(oppost_date) from crd_card b ";
 	   sqlCmd +=  " where a.p_seqno = b.p_seqno and b.card_no=b.major_card_no) ";
	   
 	   setString(1, acnoPSeqno);
 	  
 	   int recordCnt = selectTable();

 	   for (int ii = 0; ii < recordCnt; ii++) {
 	      initDataCard();
 	      tmpCurrentCode = getValue("current_code",ii);
 	      cardNo = getValue("card_no",ii); 	  
 	       	    
 	      //卡片已是強停狀態就不再改變強停註記及強停日期
 	      if(tmpCurrentCode.equals("3"))
 	      {
 	    	  showLogMessage("I", "", String.format(" 此卡片已強停毋須處理 , cardno[%s],current_code[%s]", cardNo,tmpCurrentCode));
 	    	  continue;
 	      } 	   
 	      showLogMessage("I", "", String.format(" 卡片處理強停作業... , cardno[%s],current_code[%s]", cardNo,tmpCurrentCode));
 	      
 	      updateCrdCard();
 	     
//因本程式皆處理已經停卡又重新調整卡況的卡片資料,除非有活卡停用才需要報送OUTGOING
// 	      ccaOutGoing.InsertCcaOutGoing(cardNo, currentCode, sysDate, oppoReason);
 	      insertCrdJcic();
 	      
//寫入停卡記錄檔,供未來追蹤使用
 	      insertCrdStopLog();
 	      
 	      }
 	   }
   
    //***********************************************************************    

    void insertCrdStopLog() throws Exception {
        String hProcSeqno = "";
        int actCnt = 0;

        sqlCmd = "select substr(to_char(ecs_stop.nextval,'0000000000'),2,10) h_proc_seqno ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	hProcSeqno = getValue("h_proc_seqno");
        }

        setValue("proc_seqno"   , hProcSeqno);
        setValue("crt_time"     , sysDate + sysTime);
        setValue("card_no"      , cardNo);
        setValue("current_code" , "3");
        setValue("oppost_reason", oppoReason);
        setValue("oppost_date"  , sysDate);
        setValue("trans_type"   , "");   /* 先空白   */
        setValue("stop_source"  , "C1"); /* 催收強停作業*/
        setValue("send_type"    , "");   /* 先空白   */
        setValue("mod_user"     , javaProgram);
        setValue("mod_time"     , sysDate + sysTime);
        setValue("mod_pgm"      , javaProgram);
        daoTable = "crd_stop_log";
        actCnt = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_stop_log duplicate!", "", cardNo);
        }
    }
    //***********************************************************************    

    void updateCrdCard() throws Exception {
 	   daoTable = "crd_card";
 	   updateSQL = "current_code = '3' "
 	   		+ ", oppost_date = ? "
 	   		+ ", oppost_reason =? , lost_fee_code = 'N' "
 	        + ",mod_user = 'ecs' , mod_pgm = 'ColB102' "
 	        + ",mod_time = sysdate , mod_seqno = nvl(mod_seqno,0)+1 " ; 	  
 	   whereStr = " where card_no = ? ";
 	   setString(1, sysDate);
 	   setString(2, oppoReason);
 	   setString(3, cardNo);
 	   updateTable();
 	   
 	  showLogMessage("I", "", String.format(" 更新卡片檔, cardno[%s],current_code[3],oppoReason[%s],oppoDate[%s]", cardNo,oppoReason,sysDate));
 	   
 	}
    
  //***********************************************************************    

    void updateActAcno() throws Exception {
 	   daoTable = "act_acno";
 	   updateSQL = "stop_reason = 'H1' " //催收戶強停
 	   		+ ", stop_status = 'Y' "
 	        + ",mod_user = 'ecs' , mod_pgm = 'ColB102' "
 	        + ",mod_time = sysdate , mod_seqno = nvl(mod_seqno,0)+1 " ; 	  
 	   whereStr = " where rowid = ? ";
  	   setRowId(1, acnoRowid);
 	   updateTable();
 	    	  
 	  showLogMessage("I", "", String.format(" 更新帳務檔強停註記, acnoRowid[%s]",acnoRowid));
 	   
 	}
    
    //***********************************************************************    

 	void insertCrdJcic() throws Exception {
 	    String hCdjcRowid = "";
 	    String hPaymentDate = "";
// 	    hCdjcRowid = "";
// 	    hPaymentDate = "" ;
 	    
 	    sqlCmd = "select rowid  as h_card_jcic_rowid ";
 	    sqlCmd += " from crd_jcic  ";
 	    sqlCmd += "where card_no  = ?  ";
 	    sqlCmd += "and trans_type = 'C'  ";
 	    sqlCmd += "and to_jcic_date =''  ";
 	    sqlCmd += "fetch first 1 rows only ";
 	    setString(1, cardNo);
 	    int recordCnt = selectTable();
 	    if (recordCnt > 0) {
 	    	hCdjcRowid = getValue("h_card_jcic_rowid");
 	
 	        daoTable   = "crd_jcic";
 	        updateSQL  = " current_code  = ?,";
 	        updateSQL += " oppost_reason = ?,";
 	        updateSQL += " oppost_date   = ?,";
 	        updateSQL += " payment_date  = ?,";
 	        updateSQL += " mod_user      = ?,";
 	        updateSQL += " mod_time      = sysdate,";
 	        updateSQL += " mod_pgm       = ?";
 	        whereStr   = "where rowid    = ? ";
 	        setString(1, currentCode);
 	        setString(2, oppoReason);
 	        setString(3, sysDate);
 	        setString(4, hPaymentDate);
 	        setString(5, javaProgram);
 	        setString(6, javaProgram);
 	        setRowId(7, hCdjcRowid);
 	        updateTable();
 	
 	        return;
 	    }

 	    setValue("card_no"      , cardNo);
 	    setValue("crt_date"     , sysDate);
 	    setValue("crt_user"     , javaProgram);
 	    setValue("trans_type"   , "C");
 	    setValue("current_code" , currentCode);
 	    setValue("oppost_reason", oppoReason);
 	    setValue("oppost_date"  , sysDate);
 	    setValue("payment_date" , hPaymentDate);
 	    setValue("is_rc"        , acnoRcUseIndicator);
 	    setValue("mod_user"     , javaProgram);
 	    setValue("mod_time"     , sysDate + sysTime);
 	    setValue("mod_pgm"      , javaProgram);
 	    daoTable = "crd_jcic";
 	    insertTable();
 	    if (dupRecord.equals("Y")) {
 	        comcr.errRtn("insert_crd_jcic duplicate!", "", comcr.hCallBatchSeqno);
 	    }

 }
    //***********************************************************************
    
        void initData() {
    	   acnoPSeqno = "";
    	   cardNo="";
    	}

    	void initDataCard() {
    	   cardNo = "";
    	   tmpCurrentCode = "";
    	}
    
    //***********************************************************************/
    public static void main(String[] args) throws Exception {
    	ColB102 proc = new ColB102();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
