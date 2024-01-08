/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 110/08/05  V1.00.03  Allen Ho                                              *
 * 111/11/11  V1.00.04  jiangyigndong  updated for project coding standard    *
 *                                                                            *
 ******************************************************************************/
package Mkt;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class MktH300 extends AccessDAO
{
    private final String PROGNAME = "基金-台幣基金分期贈送轉分期餘額處理程式  110/08/05 V1.00.03";
    CommFunction comm = new CommFunction();

    String business_date  = "";

    long    totalCnt=0,updateCnt=0;
    // ************************************************************************
    public static void main(String[] args) throws Exception
    {
        MktH300 proc = new MktH300();
        int  retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
        return;
    }
    // ************************************************************************
    public int mainProcess(String[] args) {
        try
        {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I","",javaProgram+" "+PROGNAME);

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
            { business_date = args[0]; }

            if ( !connectDataBase() ) return(1);

            selectPtrBusinday();

            if ( args.length == 0 )
            {
                if (selectPtrWorkday()!=0)
                {
                    showLogMessage("I","","本日非關帳日不處理...");
                    return(0);
                }
            }
            else
            {
                if (selectPtrWorkday1()!=0)
                {
                    showLogMessage("I","","本日非關帳日不處理...");
                    return(0);
                }
            }

            showLogMessage("I","","next_acct_month : ["+ getValue("wday.next_acct_month") +"]");

            showLogMessage("I","","=========================================");
            showLogMessage("I","","處理資料");
            selectMktCashbackDtl();

            showLogMessage("I","","處理 ["+totalCnt+"] 筆, 更新 ["+updateCnt+"] 筆");
            showLogMessage("I","","=========================================");

            finalProcess();
            return(0);
        }

        catch ( Exception ex )
        { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

    } // End of mainProcess
    // ************************************************************************
    public void selectPtrBusinday() throws Exception
    {
        daoTable  = "PTR_BUSINDAY";
        whereStr  = "FETCH FIRST 1 ROW ONLY";

        int recordCnt = selectTable();

        if ( notFound.equals("Y") )
        {
            showLogMessage("I","","select ptr_businday error!" );
            exitProgram(1);
        }

        if (business_date.length()==0)
            business_date   =  getValue("BUSINESS_DATE");
        showLogMessage("I","","本日營業日 : ["+business_date+"]");
    }
    // ************************************************************************
    int selectPtrWorkday() throws Exception
    {
        extendField = "wday.";
        selectSQL = "";
        daoTable  = "ptr_workday";
        whereStr  = "where next_close_date = ? "
        ;

        setString(1, business_date);

        int recCnt = selectTable();

        if ( notFound.equals("Y") ) return(1);

        return(0);
    }
    // ************************************************************************ int  select_ptr_workday() throws Exception
    int selectPtrWorkday1() throws Exception
    {
        extendField = "wday.";
        selectSQL = "stmt_cycle";
        daoTable  = "ptr_workday";
        whereStr  = "where stmt_cycle = ? "
        ;

        setString(1, business_date.substring(6,8));

        int recCnt = selectTable();

        if ( notFound.equals("Y") ) return(1);

        setValue("wday.next_acct_month", business_date.substring(0,6));

        return(0);
    }
    // ************************************************************************
    void selectMktCashbackDtl() throws Exception
    {
        selectSQL = "res_tran_amt,"
                + "res_total_cnt,"
                + "res_tran_cnt,"
                + "res_s_month,"
                + "p_seqno,"
                + "rowid as rowid";
        daoTable  = "mkt_cashback_dtl";
        whereStr  = "where  res_tran_amt != 0 "
                + "and    res_s_month != '' "
                + "and    res_s_month <= ? "
        ;

        setString(1 , getValue("wday.next_acct_month"));

        openCursor();

        totalCnt=0;
        int avgAmt  = 0;
        int tranAmt = 0;

        while( fetchTable() )
        {

//showLogMessage("I","","res_s_month  : ["+ getValue("res_s_month") +"][" +getValueInt("res_tran_cnt")+"]" );
//showLogMessage("I","","work_month   : ["+
//       comm.nextMonth(getValue("res_s_month"),
//                     getValueInt("res_tran_cnt")) +"]");

            if (!comm.nextMonth(getValue("res_s_month"),
                    getValueInt("res_tran_cnt")).equals(getValue("wday.next_acct_month")))
                continue;

//showLogMessage("I",""," wirking...");

            totalCnt++;
            if (selectActAcno()!=0) continue;

            if (getValueInt("res_tran_cnt")==0)
            {
                avgAmt = (int)Math.floor(getValueInt("res_tran_amt")
                        / getValueInt("res_total_cnt"));

                tranAmt = getValueInt("res_tran_amt")
                        - (avgAmt * (getValueInt("res_total_cnt")-1));
            }
            else
            {
                tranAmt = (int)(getValueInt("res_tran_amt")
                        / (getValueInt("res_total_cnt")
                        -  getValueInt("res_tran_cnt")));
            }
            updateMktCashbackDtl(tranAmt);
        }
        closeCursor();
    }
    // ************************************************************************
    void updateMktCashbackDtl(int tranAmt) throws Exception
    {
        dateTime();
        updateSQL = "end_tran_amt  = end_tran_amt + ?, "
                + "res_tran_amt  = res_tran_amt - ?, "
                + "res_tran_cnt  = res_tran_cnt + 1, "
                + "res_upd_date  = ?,"
                + "mod_pgm        = ?,"
                + "mod_time       = sysdate";
        daoTable  = "mkt_cashback_dtl";
        whereStr  = "WHERE rowid         = ? "
        ;

        setInt(1    , tranAmt);
        setInt(2    , tranAmt);
        setString(3 , business_date);
        setString(4 , javaProgram);
        setRowId(5  , getValue("rowid"));

        updateTable();

        return;
    }
    // ************************************************************************
    int selectActAcno() throws Exception
    {
        extendField = "acno.";
        selectSQL = "";
        daoTable  = "act_acno";
        whereStr  = "where p_seqno    = ? "
                + "and   stmt_cycle = ? "
        ;

        setString(1 , getValue("p_seqno"));
        setString(2 , getValue("wday.stmt_cycle"));

        int recCnt = selectTable();

        if ( notFound.equals("Y") ) return(1);

        return(0);
    }
// ************************************************************************


}  // End of class FetchSample
