/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/06/07  V1.00.01    JeffKung  program initial                           *
******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*列印公用事業處理明細表*/
public class BilRD114 extends AccessDAO {
    private String progname = "符合長期使用循環信用持卡人轉換機制情形明細表程式  112/11/01  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hBusinssDate = "";
    
    CommDate commDate = new CommDate();
    int totalCnt = 0;
    int indexCnt = 0;
    int pageCnt = 0;
    int lineCnt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();
            
            showLogMessage("I", "", "營業日期=[" + hBusinssDate + "]");
            
            if (args.length == 1 && args[0].length() == 8) {
            	hBusinssDate = args[0];
            } 
            
            if (!"02".equals(comc.getSubString(hBusinssDate,6))) {
            	showLogMessage("I", "", "每月02執行, 本日非執行日!!");
           		return 0;
           	}
            
            showLogMessage("I", "", "資料日期=[" + hBusinssDate + "]");
            
            showLogMessage("I", "", "程式開始執行,重跑時刪除本月資料....");
   			resetBilRevolverList();
    		commitDataBase();
			
    		//處理名單資料
    		showLogMessage("I", "", "產生名單資料(insertTable)......");
    		initCnt();
    		selectActAcctHst();
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "]");
           
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
    
    public int selectPtrWorkday() throws Exception {
		extendField = "workday.";
    	selectSQL = "";
		daoTable = "ptr_workday";

		int recCnt = selectTable();

		return (recCnt);
	}

    /***********************************************************************/
    void commonRtn() throws Exception {
        hBusinssDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
        }
        
        
    }

    /***********************************************************************/
    void selectActAcctHst() throws Exception {
    	

    	String hWdayStmtCycle = "";
    	String hWdayThisAcctMonth = "";
    	String beginAcctMonth = "";
    	String endAcctMonth = "";
    	String keepRegBankNo = "";

    	int stmtCnt = selectPtrWorkday();
    	for (int s=0 ; s<stmtCnt ; s++) {
    		hWdayStmtCycle = getValue("workday.stmt_cycle",s);
    		hWdayThisAcctMonth = getValue("workday.this_acct_month",s);
    		beginAcctMonth = comm.nextMonth(hWdayThisAcctMonth, -12);
    		endAcctMonth = comm.nextMonth(hWdayThisAcctMonth, -1);
    		
			showLogMessage("I", "", String.format("帳單週期=[%s],起迄帳務年月為[%s]~[%s]", hWdayStmtCycle, beginAcctMonth, endAcctMonth));
			keepRegBankNo = "";
			
			sqlCmd  = " select p_seqno ";
			sqlCmd += " from act_acct_hst ";
			sqlCmd += " where 1=1 ";
			sqlCmd += " and acct_month  = ? ";
			sqlCmd += " and stmt_cycle  = ? ";
			sqlCmd += " and bill_interest  > 0 ";
			sqlCmd += " and acct_type   = '01' ";

			setString(1,endAcctMonth);
			setString(2, hWdayStmtCycle);

			int cursorIndex = openCursor();
			while (fetchTable(cursorIndex)) {

				totalCnt++;

				if (totalCnt % 5000 == 0 || totalCnt == 1) {
					showLogMessage("I", "", "Current Process record=" + totalCnt);
				}

				int interestCnt = selectActAcctHst12(beginAcctMonth,endAcctMonth);
				
				//不是12期都有利息-skip
				if (interestCnt < 12) continue;
				
				selectActAcno();
				
				//若期間內有收取違約金跳過
				if (selectBilBill(beginAcctMonth,hWdayThisAcctMonth)==1) continue;
				
				selectCrdCard();
				// 無業績分行跳過
				if ("".equals(getValue("crdcard.reg_bank_no"))) continue;
				
				selectActAcct(); 
				
				// 無欠款跳過
				if (getValueDouble("actacct.ttl_amt") <= 0) continue;
				
				insertBilRevolverList();

			}

			closeCursor();

		}

	}
    
    //************************************************************************
    int insertBilRevolverList() throws Exception {

    	setValue("data_date"           , hBusinssDate);
        setValue("reg_bank_no"         , getValue("crdcard.reg_bank_no"));
        setValue("id_p_seqno"          , getValue("actacno.id_p_seqno"));
        setValueDouble("revolve_amt"   , getValueDouble("actacct.ttl_amt"));
        setValueDouble("revolve_rate"  , getValueDouble("actacno.rcrate_year"));
        setValue("mod_time"            , sysDate + sysTime);
        setValue("mod_pgm"             , javaProgram);

        daoTable = "bil_revolver_list";

        insertTable();

        if (dupRecord.equals("Y")) {
        	showLogMessage("I", "", " insert_bil_revolver_list error(dupRecord) , id_p_seqno=[" + getValue("actacno.id_p_seqno") + "]");
        }

        return (0);
    }

    void initCnt() {
    	totalCnt = 0;
        indexCnt = 0;
        pageCnt = 0;
        lineCnt = 0;
    }
    
    public static Double doubleMul(Double v1,Double v2){

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.multiply(b2).doubleValue();

	}
    
    /**********************************************************************/
    int selectActAcctHst12(String beginAcctMonth, String endAcctMonth) throws Exception {
    	
    	extendField="actaccthst12.";
        sqlCmd =  "select bill_interest ";
        sqlCmd += "from act_acct_hst ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and acct_month >= ? ";
        sqlCmd += "and acct_month <= ? ";
        sqlCmd += "and bill_interest  > 0 ";
        		
        setString(1, getValue("p_seqno"));
        setString(2, beginAcctMonth);
        setString(3, endAcctMonth);
        
        int tmpInt = selectTable();
        
        return tmpInt;
    }
    
    /**********************************************************************/
    int selectBilBill(String beginAcctMonth, String endAcctMonth) throws Exception {
    	
        sqlCmd =  "select 1 ";
        sqlCmd += "from bil_bill  ";
        sqlCmd += "where p_seqno = ? and acct_type = '01' ";
        sqlCmd += "and   acct_code = 'PN' ";
        sqlCmd += "and   acct_month between ? and ? ";
        
        setString(1, getValue("p_seqno"));
        setString(2, beginAcctMonth);
        setString(3, endAcctMonth);
        
        int tmpInt = selectTable();
        
        //無收取違約金記錄
        if (tmpInt == 0) {
            return 0;
        }
        
        return 1;
    }
    
    /**********************************************************************/
    void selectActAcct() throws Exception {
    	
    	extendField="actacct.";
        sqlCmd =  "select ttl_amt ";
        sqlCmd += "from act_acct  ";
        sqlCmd += "where p_seqno = ? and acct_type = '01' ";
        setString(1, getValue("p_seqno"));
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValueDouble("actacct.ttl_amt",0);
        }
    }
    
    /**********************************************************************/
    void selectActAcno() throws Exception {
    	
    	extendField="actacno.";
        sqlCmd =  "select id_p_seqno,rcrate_year,acct_status ";
        sqlCmd += "from act_acno  ";
        sqlCmd += "where p_seqno = ? and acct_type='01' ";
        setString(1, getValue("p_seqno"));
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValue("actacno.id_p_seqno","");
            setValue("actacno.acct_status","1");
            setValueDouble("actacno.rcrate_year",14.75);
        }
    }
    
    /**********************************************************************/
    void selectCrdCard() throws Exception {
    	
    	extendField="crdcard.";
        sqlCmd =  "select reg_bank_no ";
        sqlCmd += "from crd_card  ";
        sqlCmd += "where p_seqno = ? and acct_type='01' ";
        sqlCmd += "and reg_bank_no <> '' and group_code not in ('1203','1204') ";
        sqlCmd += "order by ori_issue_date desc ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, getValue("p_seqno"));
        int tmpInt = selectTable();
        if (tmpInt == 0) {
            setValue("crdcard.reg_bank_no","");
        }
    }
    
    /***********************************************************************/
	void resetBilRevolverList() throws Exception {
		int deleteCnt = 0;
		while(true) {
			
			daoTable  = "bil_revolver_list a";
			whereStr  = "WHERE data_date like ? " ;
			whereStr += "fetch first 5000 rows only ";

			setString(1, comc.getSubString(hBusinssDate, 0, 6) + "%");

			deleteCnt = deleteTable();
			commitDataBase();
			
			if (deleteCnt == 0 )	break;  
		}

		return;
	}
   
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilRD114 proc = new BilRD114();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
