/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 107/11/08  V1.00.00  Allen Ho   cyc_a410                                   *
 * 109-12-21  V1.00.01  tanwei      updated for project coding standard       *
*  109/12/30  V1.00.03  yanghan       修改了部分无意义的變量名稱          *
 ******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycB405 extends AccessDAO
{
    private  String progname = "年費-累積消費次數金額處理程式  112/02/13 V1.00.01";
    CommFunction comm = new CommFunction();

    boolean swDebug = true;
    String hBusiBusinessDate   = "";
    String hWdayStmtCycle      = "";
    String hWdayThisAcctMonth = "";

    String reasonCode     = "";

    double billInterest   = 0;
    double stmtNewAddBp = 0;

    int    totalPurchaseCnt   = 0;
    double totalPurchaseAmt   = 0;
    int    cardPurchaseCnt   = 0;
    int    totalCardPurchaseCnt   = 0;
    int    totalCaPurchaseCnt   = 0;
    double totalCardPurchaseAmt   = 0;
    double[]  monthCaAmt = new double[12];
    int[]    monthCaCnt = new int[12];
    int[]    monthCnt = new int[12];
    double[] monthAmt = new double[12];
    int[]    reasonCnt = new int[10];

    long    totalCnt=0,updateCnt=0;
    int inti,parmCnt=0,cnt1=0;
    int cnt2=0;
    // ************************************************************************
    public static void main(String[] args) throws Exception
    {
        CycB405 proc = new CycB405();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
    }
    // ************************************************************************
    public int mainProcess(String[] args) {
        try
        {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I","",javaProgram+" "+progname);

            if (comm.isAppActive(javaProgram))
            {
                showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
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

            if ( !connectDataBase() )
                return(0);

            selectPtrBusinday();

            if (selectPtrWorkday()!=0)
            {
                showLogMessage("I","","本日非關帳日次一日, 不需執行");
                return(0);
            }

            showLogMessage("I","","this_acct_month["+hWdayThisAcctMonth+"]");
            showLogMessage("I","","=========================================");

            selectCycAfee();

            showLogMessage("I","","處理 ["+totalCnt+"] 筆, 更新 ["+updateCnt+"] 筆");

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
        daoTable  = "ptr_workday";
        whereStr  = "where this_close_date = ? ";

        setString(1,comm.lastDate(hBusiBusinessDate));

        int recCnt = selectTable();

        if ( notFound.equals("Y") ) return(1);

        hWdayStmtCycle      =  getValue("STMT_CYCLE");
        hWdayThisAcctMonth =  getValue("this_acct_month");

        return(0);
    }
    // ************************************************************************
    public void  selectCycAfee() throws Exception
    {
        selectSQL  = "a.card_no, a.p_seqno, a.rowid as rowid, ";
        selectSQL += "a.purch_review_month_beg, a.purch_review_month_end ";
        daoTable   = "cyc_afee a";
        whereStr   = " where  a.stmt_cycle = ? ";
        whereStr  += " and    a.purch_review_month_beg <= ? ";
        whereStr  += " and    a.purch_review_month_end >= ? ";

        setString(1 , hWdayStmtCycle);
        setString(2 , hWdayThisAcctMonth);
        setString(3 , hWdayThisAcctMonth);

        openCursor();

        totalCnt=0;

        while( fetchTable() )
        {
            totalCnt++;
            selectMktCardConsume();
            updateCycAfee(getValue("rowid"));
            
            if (totalCnt % 5000 == 0 ) {
                showLogMessage("I", "", "Current Process record=" + totalCnt);
                commitDataBase();
            }
            
        }
        closeCursor();
        return;
    }
    
    // ************************************************************************
    void selectMktCardConsume() throws Exception
    {
        extendField = "come.";
         selectSQL = "p_seqno,"
                   + "acct_month,"
                   + "card_no,"
                   + "group_code,"
                   + "card_type,"
                   + "source_code,"
                   + "consume_bl_cnt,"
                   + "consume_ca_cnt,"
                   + "consume_it_cnt,"
                   + "consume_id_cnt,"
                   + "consume_ao_cnt,"
                   + "consume_ot_cnt,"
                   + "consume_bl_amt,"
                   + "consume_it_amt,"
                   + "consume_id_amt,"
                   + "consume_ao_amt,"
                   + "consume_ot_amt,"
                   + "consume_ca_amt ";
         daoTable  = "mkt_card_consume ";
         whereStr  = "WHERE p_seqno  = ? "
                   + "AND   acct_month between ? and  ? "
                   ;

         setString(1,getValue("p_seqno"));
         setString(2,getValue("purch_review_month_beg"));
         setString(3,getValue("purch_review_month_end"));

         cnt2 = selectTable();

         totalCardPurchaseCnt=0;
         totalCardPurchaseAmt=0;
         totalPurchaseCnt=0;
         totalPurchaseAmt=0;
         int tmpCnt = 0;
         double tmpAmt =0.0;

         for ( int intc=0; intc<cnt2; intc++ )
         { 
        	 tmpCnt = getValueInt("come.consume_bl_cnt",intc) 
  			        + getValueInt("come.consume_ca_cnt",intc) 
  			        + getValueInt("come.consume_it_cnt",intc) 
  			        + getValueInt("come.consume_id_cnt",intc)
  			        + getValueInt("come.consume_ao_cnt",intc)
  			        + getValueInt("come.consume_ot_cnt",intc);
        	 tmpAmt = getValueDouble("come.consume_bl_amt",intc) 
			    	+ getValueDouble("come.consume_ca_amt",intc) 
			        + getValueDouble("come.consume_it_amt",intc) 
			        + getValueDouble("come.consume_id_amt",intc)
			        + getValueDouble("come.consume_ao_amt",intc)
			        + getValueDouble("come.consume_ot_amt",intc);
        	 
             if (getValue("come.card_no",intc).equals(getValue("card_no")))  {
            	 totalCardPurchaseCnt = totalCardPurchaseCnt + tmpCnt;
                 totalCardPurchaseAmt = totalCardPurchaseAmt + tmpAmt;
             }
       
             totalPurchaseCnt = totalPurchaseCnt + tmpCnt;
             totalPurchaseAmt = totalPurchaseAmt + tmpAmt;
         }
    }

    // ************************************************************************
    public void updateCycAfee(String rowid) throws Exception
    {
        updateSQL = "card_pur_cnt = ?,"
                  + "card_pur_amt = ?,"
                  + "sum_pur_cnt  = ?,"
                  + "sum_pur_amt  = ?,"
                  + "mod_pgm      = ?,"
                  + "mod_time     = timestamp_format(?,'yyyymmddhh24miss')";
        daoTable  = "cyc_afee";
        whereStr  = "WHERE  rowid = ? ";

        setInt(1 , totalCardPurchaseCnt);
        setDouble(2 , totalCardPurchaseAmt);
        setInt(3 , totalPurchaseCnt);
        setDouble(4 , totalPurchaseAmt);
        setString(5 , javaProgram);
        setString(6 , sysDate+sysTime);
        setRowId(7  , rowid);

        updateTable();

        updateCnt++;
        return;
    }

}  // End of class FetchSample

