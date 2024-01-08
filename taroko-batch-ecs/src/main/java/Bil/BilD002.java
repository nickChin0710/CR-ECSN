/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/08/27  V1.00.01    JeffKung  program initial.                       * 
******************************************************************************/

package Bil;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*分期停卡且無活卡一次到期處理程式*/
public class BilD002 extends AccessDAO {
    private String progname = "分期停卡且無活卡一次到期處理程式  112/08/27  V1.00.01";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilD002";
    String prgmName = "分期停卡且無活卡一次到期處理程式";

    String hBusiBusinessDate = "";
    String hPreBusinessDate = "";
    String hTempContractNo = "";
    int totalCnt = 0;
    // *************************************************************************

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : BilD002 [[business_date] [contract_no]]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            hTempContractNo = "";
            if (args.length == 1) {
                if (args[0].length() == 8) {
                    hBusiBusinessDate = args[0];
                }
            }

            selectPtrBusinday();

            totalCnt = 0;
            selectBilContract();
            showLogMessage("I", "", String.format("合約檔處理筆數 [%d]", totalCnt));

            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /**********************************************************************/
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select decode(cast(? as varchar(10)),'',business_date,cast(? as varchar(10))) as h_busi_business_date,";
        sqlCmd += "to_char(to_date(decode(cast(? as varchar(10)),'',business_date,cast(? as varchar(10))),'yyyymmdd')-1 days,'yyyymmdd') as h_pre_business_date ";
        sqlCmd += " from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
            hPreBusinessDate = getValue("h_pre_business_date");
        }
    }

    void selectBilContract() throws Exception {
        String acnoPSeqno = "";
        String acctType = "";
        String cardNo = "";
        String currentCode = "";
        String rowId = "";

        sqlCmd = "select ";
        sqlCmd += "a.acno_p_seqno,a.acct_type,b.card_no,b.current_code,";
        sqlCmd += "a.rowid  as rowid ";
        sqlCmd += "from bil_contract a, crd_card b";
        sqlCmd += " where  1=1 ";
        sqlCmd += " and a.card_no = b.card_no ";
        sqlCmd += " and a.all_post_flag <> 'Y' ";
        sqlCmd += " and a.forced_post_flag <> 'Y' ";
        sqlCmd += " and a.refund_apr_flag <> 'Y' ";
        sqlCmd += " and b.current_code <> '0' ";
        /*** 排除RC轉帳分 ***/
		sqlCmd += " and decode(a.mcht_no,'',' ', a.mcht_no) not in ";
		sqlCmd += "     ( select mcht_no ";
		sqlCmd += "       from bil_merchant ";
		sqlCmd += "       where decode(trans_flag,'','N',trans_flag) in ('y','Y')) ";

        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
        	
        	acnoPSeqno = getValue("acno_p_seqno");
        	acctType = getValue("acct_type");
        	cardNo = getValue("card_no");
        	currentCode = getValue("current_code");
            rowId = getValue("rowid");

            totalCnt++;
            
            if (totalCnt == 1 || totalCnt%500==0 ) {
            	showLogMessage("I","","processCnt="+totalCnt);
            }
            
            if (selectCrdCard(acctType,acnoPSeqno) > 0) {
            	continue;
            } else {
            	//沒有活卡就一次到期
                updateBilContract(rowId);
            }

        }
        
        closeCursor(cursorIndex);

    }

	/***********************************************************************/
	int selectCrdCard(String acctType, String acnoPSeqno) throws Exception {
		int crdCardCnt = 0;
		sqlCmd = "select count(*) crd_card_cnt ";
		sqlCmd += " from crd_card  ";
		sqlCmd += "where acct_type     = ?  ";
		sqlCmd += "  and acno_p_seqno  = ?  ";
		sqlCmd += "  and (current_code = '0' or reissue_status in ('1','2'))  ";
		setString(1, acctType);
		setString(2, acnoPSeqno);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("E","","select crd_card not found!,acno_p_seqno=["+acnoPSeqno+"]");
			crdCardCnt = 1;
		}
		if (recordCnt > 0) {
			crdCardCnt = getValueInt("crd_card_cnt");
		}
		
		return crdCardCnt;
		
	}

    /**********************************************************************/
    void updateBilContract(String rowId) throws Exception {
    	
        daoTable   = "bil_contract";
        updateSQL  = " forced_post_flag = 'Y', ";
        updateSQL += " clt_forced_post_flag = 'Y', ";
        updateSQL += " apr_flag = 'Y', ";
        updateSQL += " post_cycle_dd   = ?, ";
        updateSQL += " mod_time        = sysdate, ";
        updateSQL += " mod_pgm         = 'BilD002' ";
        whereStr   = "where rowid      = ? ";
        setString(1, comc.getSubString(hBusiBusinessDate,6,8));  //異動入帳日,後續BilD003會接著把剩餘期數入帳
        setRowId(2, rowId);
        updateTable();
        if (notFound.equals("Y")) {
            showLogMessage("E","","update_bil_contract not found!,acno_p_seqno=["+getValue("acno_p_seqno")+"]");
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilD002 proc = new BilD002();
        int  retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
