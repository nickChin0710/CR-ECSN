/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 107/07/02  V1.00.00  Allen Ho   cyc_a470                                   *
 * 109-12-22  V1.00.01  tanwei      updated for project coding standard       *
 * 110-02-21  V1.00.02  JeffKung   updated for TCB requirement                *
 * 112/03/21  V1.00.03  JeffKung   世界卡依歸戶消費金額每2萬折1000元年費                   *
 ******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycB600 extends AccessDAO
{
    private  String progname = "免年費-年費費用加檔處理程式  112/03/21  V1.00.03";
    CommFunction comm = new CommFunction();
    CommRoutine comr = null;
    CommBonus comb = null;

    boolean debug = true;
    String hBusiBusinessDate   = "";
    String hWdayStmtCycle      = "";
    String hWdayThisAcctMonth  = "";
    String hWdayLastAcctMonth  = "";

    long    totalCnt=0;
    long    sysexpCnt=0;
    // ************************************************************************
    public static void main(String[] args) throws Exception
    {
        CycB600 proc = new CycB600();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
    }
    // ************************************************************************
    public int  mainProcess(String[] args) {
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

            selectPtrBusinday();

            if (selectPtrWorkday()!=0)
            {
                showLogMessage("I","","本日非關帳日前一日, 不需執行");
                return(0);
            }

            showLogMessage("I","","stmt_cycle["+hWdayStmtCycle+"] 處理開戶月份:["+hWdayLastAcctMonth+"]");
            showLogMessage("I","","=========================================");

            selectCycAfee();

            showLogMessage("I","","處理 ["+totalCnt+"] 筆");
            showLogMessage("I","","Insert_Sysexp ["+sysexpCnt+"] 筆");

            finalProcess();
            return(0);
        }

        catch ( Exception ex )
        { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

    } // End of mainProcess
    // ************************************************************************
    public void  selectPtrBusinday() throws Exception
    {
        daoTable  = "PTR_BUSINDAY";
        whereStr  = "FETCH FIRST 1 ROW ONLY";

        int recordCnt = selectTable();

        if ( notFound.equals("Y") )
        {
            showLogMessage("I","","select ptr_businday error!" );
            exitProgram(1);
        }

        if (hBusiBusinessDate.length()==0)
            hBusiBusinessDate   =  getValue("BUSINESS_DATE");
        showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
    }
    // ************************************************************************
    public int  selectPtrWorkday() throws Exception
    {
        selectSQL = "";
        daoTable  = "PTR_WORKDAY";
        whereStr  = "WHERE NEXT_CLOSE_DATE = ? ";

        setString(1,comm.nextDate(hBusiBusinessDate));

        int recCnt = selectTable();

        if ( notFound.equals("Y") ) return(1);

        hWdayStmtCycle      =  getValue("STMT_CYCLE");
        hWdayThisAcctMonth  =  getValue("this_acct_month");
        hWdayLastAcctMonth  =  getValue("last_acct_month");

        return(0);
    }
    // ************************************************************************
    public void  selectCycAfee() throws Exception
    {
        selectSQL = "a.card_no,"
                + "a.p_seqno,"
                + "a.acct_type,"
                + "a.corp_p_seqno,"
                + "a.expire_date,"
                + "a.data_type,"
                + "a.card_type,"
                + "a.reason_code,"
                + "a.org_annual_fee,"
                + "a.rcv_annual_fee,"
                + "a.card_fee_date,"
                + "a.sum_pur_amt,"
                + "b.card_note,"
                + "b.bin_type,"
                + "a.rowid as rowid";
        daoTable  = "cyc_afee a,crd_card b ";
        whereStr  = "where a.stmt_cycle              = ? "
                + "and   a.card_no = b.card_no "
                + "and   maintain_code          != 'Y' "
        ;

        setString(1 , hWdayStmtCycle);

        openCursor();

        while( fetchTable() )
        {
            totalCnt++;
            if (getValue("data_type").equals("2")) /* 1.首年年費  2.非首年年費 */
            {
                if (!getValue("card_fee_date").equals(hWdayLastAcctMonth)) continue; 
            }
            
            int rcvAnnualFee = getValueInt("rcv_annual_fee");
            String reasonCode = getValue("reason_code");
            if (selectCrdCard()==0)
            {
                if (getValueInt("org_annual_fee") != 0) {
                	//世界卡消費滿2萬可以折1000元
                	if ("MI".equals(getValue("card_type")) && rcvAnnualFee > 0) {
                		rcvAnnualFee = rcvAnnualFee - (1000 *(int) (getValueDouble("sum_pur_amt") / 20000));
                		reasonCode = "MI";
                	}
                	
                	//收取年費不能負數
                	if (rcvAnnualFee < 0) {
                		rcvAnnualFee = 0;
                	}
                	
                	insertBilSysexp(rcvAnnualFee,reasonCode);
                }
            }

            updateCycAfee(rcvAnnualFee,reasonCode);

            if (totalCnt % 5000 == 0 ) {
                showLogMessage("I", "", "Current Process record=" + totalCnt);
                commitDataBase();
            }
        }
        closeCursor();
        return;
    }

    /*************************************************************************/
    int selectCrdCard() throws Exception
    {
        String hCurrentCode = "";
        selectSQL = "current_code ";
        daoTable  = "crd_card ";
        whereStr  = "where card_no   = ? "
        ;
        setString(1, getValue("card_no"));
        int tmpInt = selectTable();
        if (tmpInt > 0) {
            hCurrentCode = getValue("current_code");
        }

        if (hCurrentCode.equals("0") || hCurrentCode.equals("2") || hCurrentCode.equals("5"))
        {
            return (0);
        }  else
        {
            return(1);
        }
    }

    // ************************************************************************
    public int insertBilSysexp(int rcvAnnualFee, String reasonCode) throws Exception
    {
        dateTime();
        setValue("card_no"            , getValue("card_no"));
        setValue("acct_type"          , getValue("acct_type"));
        setValue("p_seqno"            , getValue("p_seqno"));
        setValue("bill_type"          , "OSSG");
        setValue("txn_code"           , "AF");
        setValue("purchase_date"      , hBusiBusinessDate);
        setValue("src_type"           , reasonCode);
        setValueDouble("dest_amt"     , rcvAnnualFee);
        setValue("dest_curr"          , "901");
        setValueDouble("src_amt"      , getValueDouble("org_annual_fee"));
        setValue("bill_desc"          , "");
        setValue("post_flag"          , "U");
        setValue("mod_user"           , javaProgram);
        setValue("mod_time"           , sysDate+sysTime);
        setValue("mod_pgm"            , javaProgram);

        daoTable  = "bil_sysexp";

        insertTable();
        sysexpCnt+=1;
        return(0);
    }
    
    // ************************************************************************
    public void updateCycAfee(int rcvAnnualFee, String reasonCode) throws Exception
    {
        dateTime();
        updateSQL = "maintain_code = 'Y',"
        		  + "rcv_annual_fee = ? ,"
        		  + "reason_code = ? ,"
                  + "mod_pgm        = ?, "
                  + "mod_time       = timestamp_format(?,'yyyymmddhh24miss')";
        daoTable  = "cyc_afee";
        whereStr  = "WHERE rowid  = ? ";

        setDouble(1 , rcvAnnualFee);
        setString(2 , reasonCode);
        setString(3 , javaProgram);
        setString(4 , sysDate+sysTime);
        setRowId(5,getValue("rowid"));

        updateTable();
        return;
    }
// ************************************************************************

}  // End of class FetchSample
