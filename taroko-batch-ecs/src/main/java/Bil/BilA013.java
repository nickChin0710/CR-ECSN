/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109/11/23  V1.00.01   shiyuqi       updated for project coding standard   *
 ******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class BilA013 extends AccessDAO {
    private String progname = "更新 CRD_CARD 作業   109/11/23  V1.00.01 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    String hCallErrorDesc = "";
    int tmpInt = 0;
    String hTempUser = "";

    String prgmId   = "BilA013";
    String prgmName = "更新 CRD_CARD 作業 ";
    int recordCnt = 0;
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int errCnt = 0;
    String errMsg = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String iFileName = "";
    String iPostDate = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    long hCurpModSeqno = 0;

    String hBusinessDate = "";
    String hSystemDate = "";
    String hTempSource = "";
    String hCurpCardNo = "";
    double hCurpDestinationAmt = 0;
    String hCurpPurchaseDate = "";
    String hCurpAuthorization = "";
    String hCurpReferenceNo = "";
    String hCurpMerchantNo = "";
    String hCurpMerchantCategory = "";
    String hCurpTransactionCode = "";
    String hCurpSignFlag = "";
    String hCurpContractNo = "";
    String hCurpContractSeqNo = "";
    String hCurpRowid = "";

    int totalCnt = 0;

    // *********************************************************
    public int mainProcess(String[] args)
    {
        try
        {
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : BilA013 too many parms input !!", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTempUser = "";

            commonRtn();

            if(args.length >= 1)
            {
                if(args[0].length() == 8)
                {
                    hBusinessDate = args[0];
                }
            }
            showLogMessage("I", "", "Process_date = " + hBusinessDate);

            totalCnt=0;
            selectBilCurpost();

            showLogMessage("I", "", String.format("處理BilCurpost總筆數=[%d]", totalCnt));
            
            totalCnt=0;
            selectDbbCurpost();

            showLogMessage("I", "", String.format("處理DbbCurpost總筆數=[%d]", totalCnt));


            finalProcess();
            return 0;
        } catch (Exception ex)
        { expMethod = "mainProcess"; expHandle(ex); return exceptExit; }
    }
    
    /***********************************************************************/
    void commonRtn() throws Exception
    {
        sqlCmd = "select business_date,to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += "  from ptr_businday ";
        recordCnt = selectTable();
        if(recordCnt > 0) {
            hBusinessDate = getValue("business_date");
            hSystemDate   = getValue("h_system_date");
        }

        hModUser = comc.commGetUserID();
        hModTime = hSystemDate;
    }
    
    /***********************************************************************/
    void selectBilCurpost() throws Exception
    {
        sqlCmd  = "select ";
        sqlCmd += " card_no,";
        sqlCmd += " dest_amt,";
        sqlCmd += " purchase_date ";
        sqlCmd += " from bil_curpost ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and this_close_date  = ? ";
        sqlCmd += "  and acct_code  in ('BL','CA') ";
        sqlCmd += "  and decode(curr_post_flag,'','N',curr_post_flag) = 'Y' ";
        sqlCmd += "  and decode(contract_flag ,'','N',contract_flag)  = 'P' ";
        sqlCmd += "  and sign_flag = '+' ";
        sqlCmd += "  and rsk_type not in ('1','2','3')  ";
        
        setString(1, hBusinessDate);
        
        openCursor();
        
        while( fetchTable() )
        {
            totalCnt++;
            
            selectCrdCard();

            if (totalCnt % 5000 == 0 ) {
                showLogMessage("I", "", "Current Process record=" + totalCnt);
                commitDataBase();
            }
        }
        closeCursor();
    }
    
    /**********************************************************************/
    void selectCrdCard() throws Exception
    {
    	String updFlag = "N";
    	String frstConsumeDate = "";
    	String lastConsumeDate = "";
    	double highestConsumeAmt = 0;
    	
        daoTable  = "crd_card ";
        selectSQL = "frst_consume_date,last_consume_date,highest_consume_amt";
        whereStr  = "where card_no    = ?  ";
        setString(1, getValue("card_no"));
        selectTable();
        if(notFound.equals("Y")) {
        	showLogMessage("E","","select crd_card not found! , card_no=[" + getValue("card_no") + "]");
        	return;
        }
        
        frstConsumeDate = getValue("frst_consume_date");
        lastConsumeDate = getValue("last_consume_date");
        highestConsumeAmt = getValueDouble("highest_consume_amt");
        
        if ("".equals(frstConsumeDate)) {
        	updFlag = "Y";
        	frstConsumeDate = getValue("purchase_date");
        }
        
        if (lastConsumeDate.compareTo(getValue("purchase_date")) < 0) {
        	updFlag = "Y";
        	lastConsumeDate = getValue("purchase_date");
        }
        
        if (highestConsumeAmt < getValueDouble("dest_amt")) {
        	updFlag = "Y";
        	highestConsumeAmt = getValueDouble("dest_amt");
        }
        
        if ("Y".equals(updFlag)==false) {
        	return;
        }
        
        daoTable  = "crd_card ";
        updateSQL = "frst_consume_date = ? , last_consume_date = ? , highest_consume_amt = ?  ";
        whereStr  = "where card_no    = ?  ";
        setString(1, frstConsumeDate);
        setString(2, lastConsumeDate);
        setDouble(3, highestConsumeAmt);
        setString(4, getValue("card_no"));
        updateTable();
        if(notFound.equals("Y")) {
        	showLogMessage("E","","update crd_card not found! , card_no=[" + getValue("card_no") + "]");
        }
    }
    
    /***********************************************************************/
    void selectDbbCurpost() throws Exception
    {
        sqlCmd  = "select ";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.dest_amt,";
        sqlCmd += "a.purchase_date ";
        sqlCmd += " from dbb_curpost a ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and this_close_date  = ? ";
        sqlCmd += "  and acct_code  in ('BL','CA') ";
        sqlCmd += "  and decode(curr_post_flag,'','N',curr_post_flag) = 'Y' ";
        sqlCmd += "  and sign_flag = '+' ";
        sqlCmd += "  and rsk_type <> '1'  ";
        
        setString(1, hBusinessDate);
        
        openCursor();
        
        while( fetchTable() )
        {
            totalCnt++;
            
            selectDbcCard();

            if (totalCnt % 5000 == 0 ) {
                showLogMessage("I", "", "Current Process record=" + totalCnt);
                commitDataBase();
            }
        }
        closeCursor();
    }
    
    /**********************************************************************/
    void selectDbcCard() throws Exception
    {
    	String updFlag = "N";
    	String frstConsumeDate = "";
    	String lastConsumeDate = "";
    	double highestConsumeAmt = 0;
    	
        daoTable  = "dbc_card ";
        selectSQL = "frst_consume_date,last_consume_date,highest_consume_amt";
        whereStr  = "where card_no    = ?  ";
        setString(1, getValue("card_no"));
        selectTable();
        if(notFound.equals("Y")) {
        	showLogMessage("E","","select dbc_card not found! , card_no=[" + getValue("card_no") + "]");
        	return;
        }
        
        frstConsumeDate = getValue("frst_consume_date");
        lastConsumeDate = getValue("last_consume_date");
        highestConsumeAmt = getValueDouble("highest_consume_amt");
        
        
        if ("".equals(frstConsumeDate)) {
        	updFlag = "Y";
        	frstConsumeDate = getValue("purchase_date");
        }
        
        if (lastConsumeDate.compareTo(getValue("purchase_date")) < 0) {
        	updFlag = "Y";
        	lastConsumeDate = getValue("purchase_date");
        }
        
        if (highestConsumeAmt < getValueDouble("dest_amt")) {
        	updFlag = "Y";
        	highestConsumeAmt = getValueDouble("dest_amt");
        }
        
        if ("Y".equals(updFlag)==false) {
        	return;
        }
        
        daoTable  = "dbc_card ";
        updateSQL = "frst_consume_date = ? , last_consume_date = ? , highest_consume_amt = ?  ";
        whereStr  = "where card_no    = ?  ";
        setString(1, frstConsumeDate);
        setString(2, lastConsumeDate);
        setDouble(3, highestConsumeAmt);
        setString(4, getValue("card_no"));

        updateTable();
        if(notFound.equals("Y")) {
        	showLogMessage("E","","update dbc_card not found! , card_no=[" + getValue("card_no") + "]");
        }
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilA013 proc = new BilA013();
        int retCode  = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
