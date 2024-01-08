/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/03/28  V1.00.01    JeffKung  program initial                           *
 ******************************************************************************/

package Bil;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*政府採購卡請款資料處理*/
public class BilE195 extends AccessDAO {

    public final boolean DEBUG_MODE = false;

    private String PROGNAME = "政府採購卡請款資料處理  112/03/28 V1.00.01" ;
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    final int DEBUG = 0;

    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hTempVouchDate = "";
    String hTempVouchChiDate = "";
    String hBusiVouchDate = "";
    String chiDate = "";

    String pgmName = "";
    int seqCnt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : BilE195, this program need only one parameter  ", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            String runDate = "";
            if (args.length == 1) {
            	runDate = args[0];
            }
            	
            selectPtrBusinday(runDate);
            
            /*******************************************************************************/
            showLogMessage("I", "", String.format("政府採購卡請款資料處理......."));
            pgmName = String.format("BilE195");

            procGovPurchaseData();

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void selectPtrBusinday(String runDate) throws Exception {
        hBusiBusinessDate = "";
        hTempVouchDate = "";
        hTempVouchChiDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += " vouch_date,";
        sqlCmd += " substr(to_char(to_number(vouch_date) - 19110000,'0000000'),2,7) h_temp_vouch_chi_date,";
        sqlCmd += " substr(to_char(to_number(vouch_date) - 19110000,'00000000'),4,6) h_busi_vouch_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempVouchDate = getValue("vouch_date");
            hTempVouchChiDate = getValue("h_temp_vouch_chi_date");
            hBusiVouchDate = getValue("h_busi_vouch_date");
        }
        
        showLogMessage("I", "", String.format("本日營業日期=[%s]",hBusiBusinessDate));
        
        if (runDate.length() == 8) {
        	hBusiBusinessDate = runDate;
        }

        showLogMessage("I", "", String.format("程式處理日期=[%s]",hBusiBusinessDate));

    }

	/***********************************************************************/
	void procGovPurchaseData() throws Exception {

		sqlCmd =  "select ('F01_'||substr(media_name,21,8)) as file_name,";
		sqlCmd += "       card_no,dest_amt,purchase_date,procure_tot_term, ";
		sqlCmd += "       procure_bank_fee,procure_cht_fee,procure_pay_amt, ";
		sqlCmd += "       ecs_tx_code,ecs_sign_code,mcht_no,auth_code,source_curr,source_amt, ";
		sqlCmd += "       procure_name,procure_uniform,procure_receipt_no,procure_tx_num, ";
		sqlCmd += "       rowid  as rowid ";
		sqlCmd += " from bil_fiscdtl ";
		sqlCmd += "where batch_date = ? ";
		sqlCmd += " and  doc_ind = '' ";
		sqlCmd += " and  media_name LIKE 'F00600000.ICF01%' ";
		sqlCmd += " order by media_name ";

		setString(1,hBusiBusinessDate);
		
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			insertBilGovPurchase();
			
			updateBilFiscdtl();
			
		}
		closeCursor(cursorIndex);

	}
	
	/***********************************************************************/
    void insertBilGovPurchase() throws Exception {
        setValue("file_name", getValue("file_name"));
        setValueInt("line_no", ++seqCnt);
        setValue("settl_date", hBusiBusinessDate);
        setValue("mcht_no", getValue("mcht_no"));
        setValue("txn_seqno", getValue("procure_tx_num"));
        setValue("purchase_date", getValue("purchase_date"));
        setValue("card_no", getValue("card_no"));
        setValue("txn_code", getValue("ecs_tx_code"));
        setValue("auth_code", getValue("auth_code"));
        setValue("settl_amt", String.format("%10d", getValueInt("dest_amt")));
        setValue("bank_fee_amt", getValue("procure_bank_fee"));
        setValue("cht_fee_amt", getValue("procure_cht_fee"));
        setValue("payment_amt", getValue("procure_pay_amt"));
        setValue("order_term", getValue("procure_tot_term"));
        setValue("mcht_name", getValue("procure_name"));
        setValue("invoice_no", getValue("procure_receipt_no"));
        setValue("source_curr", getValue("source_curr"));
        setValue("source_amt", String.format("%10d", getValueInt("source_amt")));
        setValue("mod_time", sysDate + sysTime);
        daoTable = "bil_govpurchase";
        insertTable();
        if (dupRecord.equals("Y")) {
        	showLogMessage("E", "", String.format("insert_bil_govpurchase duplicate,file_name=[%s],card_no=[%s]",getValue("file_name"),getValue("card_no")));
        }
    }

    void updateBilFiscdtl() throws Exception {
    	daoTable   = "bil_fiscdtl";
        updateSQL  = " doc_ind = 'Y'";
        whereStr   = "where rowid         = ? ";
        setRowId(1, getValue("rowid"));
        updateTable();
        if (notFound.equals("Y")) {
        	showLogMessage("E", "", String.format("update_bil_fiscdtl not found, card_no=[%s]",getValue("card_no")));
        }
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        BilE195 proc = new BilE195();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
