/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 110/03/25  V1.00.14  Allen Ho   dbm_m040                                   *
 * 111/11/07  V1.00.15  jiangyigndong  updated for project coding standard    *
 *                                                                            *
 ******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM020 extends AccessDAO
{
    private final String PROGNAME = "Debit紅利-紅利重整處理程式 110/03/25 V1.00.14";
    CommFunction comm = new CommFunction();
    CommDBonus comd = null;

    String businessDate = "";

    long    totalCnt=0;
    // ************************************************************************
    public static void main(String[] args) throws Exception
    {
        DbmM020 proc = new DbmM020();
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
            { businessDate = args[0]; }

            if ( !connectDataBase() ) return(1);

            comd = new CommDBonus(getDBconnect(),getDBalias());
            comd.modPgm = javaProgram;

            showLogMessage("I","","=========================================");
            showLogMessage("I","","處理資料");

            selectDbmBonusDtl();

            showLogMessage("I","","處理 ["+totalCnt+"] 筆");
            showLogMessage("I","","=========================================");

            finalProcess();
            return(0);
        }

        catch ( Exception ex )
        { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

    } // End of mainProcess
    // ************************************************************************
    void selectDbmBonusDtl() throws Exception
    {
        selectSQL = "id_p_seqno";
        daoTable  = "dbm_bonus_dtl";
        whereStr  = "where  end_tran_bp !=0 "
                + "group by id_p_seqno "
                + "having sum(decode(sign(end_tran_bp),-1,end_tran_bp,0))!=0 "
                + "and    sum(decode(sign(end_tran_bp), 1,end_tran_bp,0))!=0 "
        ;

        openCursor();

        totalCnt =0;
        while( fetchTable() )
        {
            totalCnt++;
            showLogMessage("I","","id_p_seqno : ["+ getValue("id_p_seqno") +"]");

            selectDbmBonusDtl1();

            processDisplay(10000); // every 10000 display message

        }
        closeCursor();
    }
    // ************************************************************************
    int selectDbmBonusDtl1() throws Exception
    {
        selectSQL = "tran_seqno";
        daoTable  = "dbm_bonus_dtl";
        whereStr  = "WHERE  id_p_seqno     = ? "
                + "and   end_tran_bp     < 0 "
                + "order by  decode(effect_e_date,'','99999999',effect_e_date),tran_date ";
        ;
        setString(1 , getValue("id_p_seqno"));

        int recCnt = selectTable();

        for ( int inti=0; inti<recCnt; inti++ )
        {
            comd.dBonusFunc(getValue("tran_seqno",inti));
        }
        return(1);
    }
// ************************************************************************

}  // End of class FetchSample

