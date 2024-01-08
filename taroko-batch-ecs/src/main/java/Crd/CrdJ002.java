/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/01/20  V1.00.01    Pino      新增scrap_qty                              *  
*  109/12/23  V1.00.01   shiyuqi       updated for project coding standard   *
******************************************************************************/

package Crd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*日結製卡明細作業*/
public class CrdJ002 extends AccessDAO {
    private String progname = "日結製卡明細作業    109/12/23  V1.00.01 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    String hTempUser = "";

    String prgmId = "CrdJ002";
    String hModUser = "";
    String hCallBatchSeqno = "";

    String inputDate = "";
    String inputDatePrev = "";
    String hBusinessDate = "";
    String hSystemDate = "";
    String hWhtrCardItem = "";
    String hWhtrPlace = "";
    long hInQty = 0;
    long hOutQty = 0;
    long hScrapQty = 0; //V1.00.01
    double hPreTotalBal = 0;
    String hTxblCardItem = "";
    String hTxblPlace = "";
    long hTxblInQty = 0;
    long hTxblOutQty = 0;
    long hTxblScrapQty = 0; //V1.00.01
    long tempQty = 0;
    int tempInt = 0;
    int totCnt = 0;
    int totCnt1 = 0;

    // ********************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 3) {
                comc.errExit("Usage : CrdJ002 [YYYYMMDD] [batch_seq]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            comcr.hCallRProgramCode = javaProgram;
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            commonRtn();

            inputDate = hBusinessDate;
            if (args.length > 0) {
                if (args[0].length() == 8) {
                    inputDate = args[0];
                }
            }

            sqlCmd = "select to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd') as input_date_prev ";
            sqlCmd += " from dual ";
            setString(1, inputDate);
            if (selectTable() > 0) {
                inputDatePrev = getValue("input_date_prev");
            }

            showLogMessage("I", "", String.format("執行 日期 = [%s][%s]", inputDate, inputDatePrev));

            daoTable = "crd_tx_bal";
            whereStr = "where tx_date = ? ";
            setString(1, inputDate);
            deleteTable();

            selectCrdWhtrans();

            selectCrdWarehouse();

            selectCrdTxBal();

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "][" + totCnt1 + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void commonRtn() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
        }

        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
        }

        hModUser = comc.commGetUserID();
    }

    /***********************************************************************/
    void selectCrdWhtrans() throws Exception {
        sqlCmd  = "select ";
        sqlCmd += "card_item,";
        sqlCmd += "place,";
        sqlCmd += "sum(decode(tns_type,1,use_total,0)) h_in_qty,";
        sqlCmd += "sum(decode(tns_type,2,use_total,0)) h_out_qty,";
        sqlCmd += "sum(decode(tns_type,2,decode(trans_reason,2,use_total,0),0)) h_scrap_qty "; //V1.00.01
        sqlCmd += " from crd_whtrans ";
        sqlCmd += "where warehouse_date = ? ";
        sqlCmd += "  and apr_flag       = 'Y' ";
        sqlCmd += "group by card_item, place ";
        setString(1, inputDate);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hWhtrCardItem = getValue("card_item", i);
            hWhtrPlace     = getValue("place", i);
            hInQty         = getValueLong("h_in_qty", i);
            hOutQty        = getValueLong("h_out_qty", i);
            hScrapQty      = getValueLong("h_scrap_qty", i); //V1.00.01

            totCnt++;

            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("crd Process 1 record=[%d]", totCnt));

            hPreTotalBal = 0;
            sqlCmd = "select total_bal ";
            sqlCmd += " from crd_tx_bal  ";
            sqlCmd += "where tx_date   = ?  ";
            sqlCmd += "  and card_item = ?  ";
            sqlCmd += "  and place     = ? ";
            setString(1, inputDatePrev);
            setString(2, hWhtrCardItem);
            setString(3, hWhtrPlace);
            if (selectTable() > 0) {
                hPreTotalBal = getValueDouble("total_bal");
            } else {
                sqlCmd = "select sum(pre_total + in_qty01 - out_qty01 + in_qty02 - out_qty02 + in_qty03 - out_qty03 + in_qty04 - out_qty04 + in_qty05 - out_qty05 + in_qty06 - out_qty06 + in_qty07 - out_qty07 + in_qty08 - out_qty08 + in_qty09 - out_qty09 + in_qty10 - out_qty10 + in_qty11 - out_qty11 + in_qty12 - out_qty12) h_pre_total_bal ";
                sqlCmd += " from crd_warehouse  ";
                sqlCmd += "where wh_year   = to_char(to_number(substr(?,1,4))-1)  ";
                sqlCmd += "  and card_item = ?  ";
                sqlCmd += "  and place     = ? ";
                setString(1, inputDate);
                setString(2, hWhtrCardItem);
                setString(3, hWhtrPlace);
                if (selectTable() > 0) {
                    hPreTotalBal = getValueDouble("h_pre_total_bal");
                }

                hPreTotalBal = hPreTotalBal - hInQty + hOutQty;
                if (hPreTotalBal < 0)
                    hPreTotalBal = 0;
            }

            hTxblCardItem = hWhtrCardItem;
            hTxblPlace     = hWhtrPlace;
            hTxblInQty    = hInQty;
            hTxblOutQty   = hOutQty;
            tempQty = (long) (hPreTotalBal + hTxblInQty - hTxblOutQty);
            hTxblScrapQty  = hScrapQty; //V1.00.01
            hModUser = "USER1";

            insertCrdTxBal();
        }
    }

    /***********************************************************************/
    void insertCrdTxBal() throws Exception {
        setValue("tx_date"        , inputDate);
        setValue("card_item"      , hTxblCardItem);
        setValue("place"          , hTxblPlace);
        setValueDouble("pre_total", hPreTotalBal);
        setValueLong("in_qty"     , hTxblInQty);
        setValueLong("out_qty"    , hTxblOutQty);
        setValueLong("total_bal"  , tempQty);
        setValue("mod_user"       , hModUser);
        setValue("mod_pgm"        , prgmId);
        setValue("mod_time"       , sysDate + sysTime);
        setValueLong("scrap_qty"  , hTxblScrapQty);        
        daoTable = "crd_tx_bal";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_tx_bal duplicate!", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectCrdWarehouse() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "card_item,";
        sqlCmd += "place,";
        sqlCmd += "sum(pre_total + in_qty01 - out_qty01+ in_qty02 - out_qty02+ in_qty03 - out_qty03+ in_qty04 - out_qty04+ in_qty05 - out_qty05+ in_qty06 - out_qty06+ in_qty07 - out_qty07+ in_qty08 - out_qty08+ in_qty09 - out_qty09+ in_qty10 - out_qty10+ in_qty11 - out_qty11+ in_qty12 - out_qty12) h_out_qty ";
        sqlCmd += "from crd_warehouse ";
        sqlCmd += "where wh_year = to_char(to_number(substr(?,1,4))-1) ";
        sqlCmd += "group by card_item, place ";
        setString(1, inputDate);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hWhtrCardItem = getValue("card_item", i);
            hWhtrPlace     = getValue("place", i);
            hOutQty        = getValueLong("h_out_qty", i);
   
            sqlCmd = "select count(*) temp_int ";
            sqlCmd += " from crd_tx_bal  ";
            sqlCmd += "where tx_date   = ?  ";
            sqlCmd += "  and card_item = ?  ";
            sqlCmd += "  and place     = ? ";
            setString(1, inputDate);
            setString(2, hWhtrCardItem);
            setString(3, hWhtrPlace);
            if (selectTable() > 0) {
                tempInt = getValueInt("temp_int");
            }

            if (tempInt > 0)    continue;

            totCnt1++;

            if (totCnt1 % 1000 == 0 || totCnt1 == 1)
                showLogMessage("I", "", String.format("crd Process 2 record=[%d]", totCnt1));
if(debug == 1) 
   showLogMessage("I", "", "888 Card="+hWhtrCardItem+","+hWhtrPlace+","+hOutQty);


            hTxblCardItem = hWhtrCardItem;
            hTxblPlace     = hWhtrPlace;
            hTxblInQty    = 0;
            hTxblOutQty   = 0;
            hPreTotalBal  = 0;
            hTxblScrapQty = 0; //V1.00.01

            sqlCmd = "select total_bal ";
            sqlCmd += " from crd_tx_bal  ";
            sqlCmd += "where tx_date   = ?  ";
            sqlCmd += "  and card_item = ?  ";
            sqlCmd += "  and place     = ? ";
            setString(1, inputDatePrev);
            setString(2, hWhtrCardItem);
            setString(3, hWhtrPlace);
            if (selectTable() > 0) {
                hPreTotalBal = getValueDouble("total_bal");
                tempQty = (long) (hPreTotalBal + hTxblInQty - hTxblOutQty);
            } else {
                hPreTotalBal = hOutQty;
                tempQty = hOutQty;
            }

            hModUser = "USER2";

            insertCrdTxBal();
        }
    }
    /***********************************************************************/
    void selectCrdTxBal() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "card_item,";
        sqlCmd += "place,";
        sqlCmd += "sum(total_bal) h_out_qty ";
        sqlCmd += "from crd_tx_bal ";
        sqlCmd += "where tx_date = ? ";
        sqlCmd += "group by card_item, place ";
        setString(1, inputDatePrev);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hWhtrCardItem = getValue("card_item", i);
            hWhtrPlace     = getValue("place", i);
            hOutQty        = getValueLong("h_out_qty", i);

            sqlCmd  = "select count(*) temp_int ";
            sqlCmd += " from crd_tx_bal  ";
            sqlCmd += "where tx_date   = ?  ";
            sqlCmd += "  and card_item = ?  ";
            sqlCmd += "  and place     = ? ";
            setString(1, inputDate);
            setString(2, hWhtrCardItem);
            setString(3, hWhtrPlace);
            if (selectTable() > 0) {
                tempInt = getValueInt("temp_int");
            }

            if (tempInt > 0)      continue;

            totCnt1++;

            if (totCnt1 % 1000 == 0 || totCnt1 == 1)
                showLogMessage("I", "", String.format("crd Process 2 record=[%d]\n", totCnt1));

            hTxblCardItem = hWhtrCardItem;
            hTxblPlace     = hWhtrPlace;
            hTxblInQty    = 0;
            hTxblOutQty   = 0;
            hPreTotalBal  = hOutQty;
            tempQty         = hOutQty;
            hTxblScrapQty = 0; //V1.00.01
            hModUser       = "USER3";

            insertCrdTxBal();
        }
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdJ002 proc = new CrdJ002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
