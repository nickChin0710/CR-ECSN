/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 110/07/02  V1.00.12  Allen Ho   dbm_M030                                   *
 * 111/11/07  V1.00.13  jiangyigndong  updated for project coding standard    *
 * 112/10/30  V1.00.14  Ryan             增加group by p_seqno處理                                             *
 ******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM030 extends AccessDAO
{
    private final  String PROGNAME = "Debit紅利-紅利效期到期移除處理程式 112/10/30 V1.00.14";
    CommFunction comm = new CommFunction();
    CommRoutine comr = null;

    String businessDate = "";
    String tranSeqno = "";

    long    totalCnt=0;
    // ************************************************************************
    public static void main(String[] args) throws Exception
    {
        DbmM030 proc = new DbmM030();
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

            if ( !connectDataBase() )
                return(1);

            comr = new CommRoutine(getDBconnect(),getDBalias());

            selectPtrBusinday();

            if (selectDbmBonusDtl0()>0)   // if no this check, will not right
            {
                commitDataBase();
                showLogMessage("I","","啟動 DbmM020 執行後續動作 .....");
                showLogMessage("I","","===============================");

                String[] hideArgs = new String[1];
                try {
                    hideArgs[0] = "";

                    DbmM020 dbmM020 = new DbmM020();
                    int rtn = dbmM020.mainProcess(hideArgs);
                    if(rtn < 0)   return (1);
                    showLogMessage("I","","DbmM020 執行結束");
                    showLogMessage("I","","===============================");
                } catch (Exception ex)
                {
                    showLogMessage("I","","無法執行 DbmM020 ERROR!");
                }
            }

            showLogMessage("I","","=========================================");
            selectDbmSysparm();
            if (getValue("dbmp.corp_delete_flag").equals("Y"))
            {
                showLogMessage("I","","處理公司戶移除資料...");
                updateDbmBonusDtl0();
            }
            showLogMessage("I","","=========================================");
            showLogMessage("I","","處理效期到期資料...");
            selectDbmBonusDtl();
            showLogMessage("I","","處理筆數 : ["+totalCnt+"]");
            showLogMessage("I","","=========================================");

            finalProcess();
            return(0);
        }

        catch ( Exception ex )
        { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

    } // End of mainProcess
    // ************************************************************************
    void selectPtrBusinday() throws Exception
    {
        daoTable  = "PTR_BUSINDAY";
        whereStr  = "FETCH FIRST 1 ROW ONLY";

        int recordCnt = selectTable();

        if ( notFound.equals("Y") )
        {
            showLogMessage("I","","select ptr_businday error!" );
            exitProgram(1);
        }

        if (businessDate.length()==0)
            businessDate =  getValue("BUSINESS_DATE");
        showLogMessage("I","","本日營業日 : ["+ businessDate +"]");
    }
    // ************************************************************************
    int selectDbmBonusDtl0() throws Exception
    {
        selectSQL = "count(*) as data_cnt";
        daoTable  = "dbm_bonus_dtl";
        whereStr  = "where  end_tran_bp != 0 "
                + "group by id_p_seqno "
                + "having (sum(decode(sign(end_tran_bp),1,end_tran_bp,0))!=0 "
                + " and    sum(decode(sign(end_tran_bp),-1,end_tran_bp,0))!=0) "
                + "and count(*) > 0 "
        ;

        showLogMessage("I","","===============================");
        showLogMessage("I","","檢查是否已重整 ...");

        int recCnt = selectTable();

        if (recCnt==0)
        {
            showLogMessage("I","","資料已重整");
            return(0);
        }

        showLogMessage("I","","資料要重整");

        return(1);
    }
    // ************************************************************************
    void selectDbmBonusDtl() throws Exception
    {
        selectSQL = "id_p_seqno,"
        		+ "p_seqno,"
        		+ "acct_type,"
                + "effect_e_date,"
                + "bonus_type,"
                + "sum(end_tran_bp) as end_tran_bp, "
//                + "max(acct_type) as acct_type,"
                + "sum(decode(tax_flag,'Y',end_tran_bp,0)) as tax_tran_bp";
        daoTable  = "dbm_bonus_dtl";
        whereStr  = "where end_tran_bp > 0 "
                + "and   effect_e_date between ? and ? "
                + "group by id_p_seqno,p_seqno,acct_type,effect_e_date,bonus_type "
        ;

        if (businessDate.substring(6,8).equals("01"))
            setString(1 , comm.nextMonthDate(businessDate, -36));
        else
            setString(1 , comm.nextNDate(businessDate, -7));

        setString(2 , comm.nextNDate(businessDate, -1));

        if (businessDate.substring(6,8).equals("01"))
        {
            showLogMessage("I","","判斷日期 : ["
                    + comm.nextMonthDate(businessDate, -36)
                    +"]-["
                    + comm.nextNDate(businessDate, -1)
                    + "]");
        }
        else
        {
            showLogMessage("I","","判斷日期 : ["
                    + comm.nextNDate(businessDate, -7)
                    +"]-["
                    + comm.nextNDate(businessDate, -1)
                    + "]");
        }

        openCursor();

        totalCnt=0;
        while( fetchTable() )
        {
            totalCnt++;

            insertDbmBonusDtl();
            updateDbmBonusDtl();

//  showLogMessage("I","","id_p_seqno : ["+ getValue("id_p_seqno") +"]");

            processDisplay(100000); // every 10000 display message
        }
        closeCursor();
        return;
    }
    // ************************************************************************
    int insertDbmBonusDtl() throws Exception
    {
        tranSeqno = comr.getSeqno("ECS_DBMSEQ");

        setValue("ddtl.acct_type"            , getValue("acct_type"));
        setValue("ddtl.id_p_seqno"           , getValue("id_p_seqno"));
        setValue("ddtl.p_seqno"              , getValue("p_seqno"));
        setValue("ddtl.active_code"          , "");
        setValue("ddtl.active_name"          , "效期到期移除");
        setValue("ddtl.bonus_type"           , getValue("bonus_type"));
        setValue("ddtl.tran_code"            , "6");
        setValue("ddtl.tran_pgm"             , javaProgram);
        setValueInt("ddtl.beg_tran_bp"       , getValueInt("end_tran_bp")*-1);
        setValue("ddtl.end_tran_bp"          , "0");
        setValue("ddtl.tax_tran_bp"          , getValue("tax_tran_bp"));
        setValue("ddtl.mod_desc"             , "效期到期日"+getValue("effect_e_date"));
        setValue("ddtl.tax_flag"             , "N");
        setValue("ddtl.tran_seqno"           , tranSeqno);
        setValue("ddtl.acct_date"            , businessDate);
        setValue("ddtl.acct_month"           , businessDate.substring(0,6));
        setValue("ddtl.tran_date"            , sysDate);
        setValue("ddtl.tran_time"            , sysTime);
        setValue("ddtl.crt_date"             , sysDate);
        setValue("ddtl.crt_user"             , "SYSTEM");
        setValue("ddtl.apr_date"             , businessDate);
        setValue("ddtl.apr_user"             , "SYSTEM");
        setValue("ddtl.apr_flag"             , "Y");
        setValue("ddtl.mod_user"             , "SYSTEM");
        setValue("ddtl.mod_time"             , sysDate+sysTime);
        setValue("ddtl.mod_pgm"              , javaProgram);

        extendField = "ddtl.";
        daoTable  = "dbm_bonus_dtl";

        insertTable();

        return(0);
    }
    // ************************************************************************
    int updateDbmBonusDtl() throws Exception
    {
        updateSQL = "end_tran_bp  = 0,"
                + "link_seqno   = ?,"
                + "link_tran_bp = end_tran_bp,"
                + "effect_flag  = decode(effect_flag,'3','3','1'), "
                + "remove_date  = ?, "
                + "mod_memo     = ?||decode(effect_flag,'3','公司戶',''),"
                + "mod_pgm      = ?, "
                + "mod_time     = sysdate";
        daoTable  = "dbm_bonus_dtl";
        whereStr  = "WHERE id_p_seqno  = ? "
        		+ "and p_seqno = ? "
        		+ "and acct_type = ? "
                + "and effect_e_date = ? "
                + "and bonus_type    = ? "
                + "and end_tran_bp  >  0 ";

        setString(1 , tranSeqno);
        setString(2 , sysDate);
        setString(3 , "移除序號["+ tranSeqno +"]");
        setString(4 , javaProgram);
        setString(5 , getValue("id_p_seqno"));
        setString(6 , getValue("p_seqno"));
        setString(7 , getValue("acct_type"));
        setString(8 , getValue("effect_e_date"));
        setString(9 , getValue("bonus_type"));

        int cnt = updateTable();

        return(0);
    }
    // ************************************************************************
    int updateDbmBonusDtl0() throws Exception
    {
        updateSQL = "effect_e_date  = ?, "
                + "effect_flag    = '3'";
        daoTable  = "dbm_bonus_dtl";
        whereStr  = "WHERE (acct_type,id_p_seqno,p_seqno) in ( "
                + "      select acct_type,id_p_seqno,p_seqno "
                + "      from    dbc_card "
                + "      WHERE corp_no != '' "
                + "      group by acct_type,p_seqno,id_p_seqno ) "
                + "and   end_tran_bp > 0 "
                + "and   (effect_e_date > ? "
                + " or    effect_e_date = '') "
        ;

        setString(1, businessDate);
        setString(2, businessDate);

        int cnt = updateTable();

        showLogMessage("I","","VD 公司戶將移除 " + cnt +" 筆");

        return(0);
    }
    // ************************************************************************
    int selectDbmSysparm() throws Exception
    {
        extendField = "dbmp.";
        selectSQL = "corp_delete_flag";
        daoTable  = "dbm_sysparm";
        whereStr  = "WHERE parm_type = '01' "
                + "and   apr_date !='' ";

        int recCnt = selectTable();

        if ( notFound.equals("Y") )
        {
            showLogMessage("I","","select dbm_sysparm error!" );
            exitProgram(0);
        }
        return(0);
    }
// ************************************************************************

}  // End of class FetchSample

