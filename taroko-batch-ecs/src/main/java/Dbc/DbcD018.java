/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109/11/13  V1.00.02  yanghan       修改了變量名稱和方法名稱
 *  109/12/24  V1.00.03  yanghan       修改了變量名稱和方法名稱            *
 *  112/06/14  V1.00.04  Wilson      日期不為一號不當掉                                                                                 *
 ******************************************************************************/

package Dbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*庫存月結計算(For*/
public class DbcD018 extends AccessDAO {
    private String progname = "庫存月結計算(For Debit Only)  112/06/14 V1.00.04";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "DbcD018";
    String hCallBatchSeqno = "";

    String hMonth = "";
    String hPreDate = "";
    String hCurDate = "";
    String hMonthLike = "";
    String hCardItem = "";
    String hGroupCode = "";
    String hCardType = "";
    int hInCnt = 0;
    int hOutCnt = 0;
    double hAvgAmt = 0;
    String hUnitCode = "";
    String hWarehouseNo = "";
    int hRemainCnt = 0;
    String hTmp = "";
    String hBegWarehouse = "";
    String hEndWarehouse = "";
    String hDd = "";
    String hChiYear = "";
    String hStr = "";
    int totalCnt = 0;
    int insertCnt1 = 0;
    int insertCnt2 = 0;
    private String hModUser = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hModUser = comc.commGetUserID();

            hMonth = "";
            hDd = sysDate.substring(6, 8);
            hMonth = "";
            hPreDate = "";
            hCurDate = "";

            hChiYear = "";
            if (chinDate.length() < 7)
                hChiYear = String.format("%2.2s", chinDate);
            else
                hChiYear = String.format("%3.3s", chinDate);

            if (args.length == 1) {
                hMonth = args[0];
                hPreDate = String.format("%6.6s01", hMonth);
                hCurDate = hPreDate;
            } else {
                if (comcr.str2int(hDd) != 1) {
                	showLogMessage("I", "", String.format("今天日期不為一號，程式執行結束"));
                	finalProcess();
                	return 0;
                }

                sqlCmd = "select to_char(to_date(to_char(sysdate,'yyyymm')||'01','yyyymmdd') - 1 days,'yyyymm') h_month,";
                sqlCmd += "to_char(to_date(to_char(sysdate,'yyyymm')||'01','yyyymmdd') - 1 days,'yyyymmdd') h_pre_date,";
                sqlCmd += "to_char(sysdate,'yyyymmdd') h_cur_date ";
                sqlCmd += " from dual ";
                int recordCnt = selectTable();
                if (recordCnt > 0) {
                    hMonth = getValue("h_month");
                    hPreDate = getValue("h_pre_date");
                    hCurDate = getValue("h_cur_date");
                }
            }
            process();
            showLogMessage("I", "", String.format("程式執行結束,總筆數=[%d],[%d],[%d]", totalCnt, insertCnt1, insertCnt2));
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

    /***********************************************************************/
    void process() throws Exception {
        String hStr1 = "";
        double hRemainCnt = 0;

        hMonthLike = "";
        hMonthLike = String.format("%s%s", hMonth, "%");

        sqlCmd = "select ";
        sqlCmd += "a.card_item,";
//        sqlCmd += "a.group_code,";
        sqlCmd += "a.card_type,";
        sqlCmd += "a.unit_code,";
        sqlCmd += "sum(decode(a.tns_type,1,a.use_total,0)) h_in_cnt,";
        sqlCmd += "sum(decode(a.tns_type,2,a.use_total,0)) h_out_cnt,";
        sqlCmd += "sum(decode(a.tns_type,1,a.item_amt,0) * a.use_total )/sum(decode(a.tns_type,1,a.use_total)) h_avg_amt ";
        sqlCmd += "from crd_whtrans a, dbp_prod_type b ";
        sqlCmd += "where a.place = '1' ";
//        sqlCmd += "and b.group_code = a.group_code ";
        sqlCmd += "and b.card_type = a.card_type ";
        sqlCmd += "and a.warehouse_date like ? ";
        sqlCmd += "group by a.card_item,a.card_type,a.unit_code ";
        setString(1, hMonthLike);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCardItem = getValue("card_item", i);
//            h_group_code = getValue("group_code", i);
            hCardType = getValue("card_type", i);
            hUnitCode = getValue("unit_code");
            hInCnt = getValueInt("h_in_cnt", i);
            hOutCnt = getValueInt("h_out_cnt", i);
            hAvgAmt = getValueDouble("h_avg_amt", i);

            totalCnt++;

            hRemainCnt = hInCnt - hOutCnt;

            if (hRemainCnt > 0) {
                hStr = String.format("%.03f", hAvgAmt);

                if (comcr.str2int(hStr1) >= 5)
                    hAvgAmt = hAvgAmt + 0.005;
                getWarehouseno();

                insertCnt1++;
                setValue("WAREHOUSE_NO", hWarehouseNo);
                setValue("CARD_ITEM", hCardItem);
                setValue("WAREHOUSE_DATE", hPreDate);
                setValue("CARD_TYPE", hCardType);
//                setValue("GROUP_CODE", h_group_code);
                setValue("UNIT_CODE", hUnitCode);
                setValue("TNS_TYPE", "2");
                setValue("PLACE", "1");
                setValueDouble("PREV_TOTAL", 0);
                setValueDouble("USE_TOTAL", hRemainCnt);
                setValue("CRT_DATE", sysDate);
                setValue("TRANS_REASON", "4");
                setValue("CRT_user", hModUser);
                setValue("MOD_USER", hModUser);
                setValue("MOD_TIME", sysDate + sysTime);
                setValue("MOD_PGM", prgmId);
                setValueDouble("ITEM_AMT", hAvgAmt);
                daoTable = "CRD_WHTRANS";
                insertTable();
                if (dupRecord.equals("Y")) {
                    comcr.errRtn("insert_CRD_WHTRANS duplicate!", "", hCallBatchSeqno);
                }

                getWarehouseno();
                insertCnt2++;
                setValue("WAREHOUSE_NO", hWarehouseNo);
                setValue("CARD_ITEM", hCardItem);
                setValue("WAREHOUSE_DATE", hCurDate);
                setValue("CARD_TYPE", hCardType);
//                setValue("GROUP_CODE", h_group_code);
                setValue("UNIT_CODE", hUnitCode);
                setValue("TNS_TYPE", "1");
                setValue("PLACE", "1");
                setValueDouble("PREV_TOTAL", 0);
                setValueDouble("USE_TOTAL", hRemainCnt);
                setValue("CRT_DATE", sysDate);
                setValue("TRANS_REASON", "6");
                setValue("CRT_USER", hModUser);
                setValue("MOD_USER", hModUser);
                setValue("MOD_TIME", sysDate + sysTime);
                setValue("MOD_PGM", prgmId);
                setValueDouble("ITEM_AMT", hAvgAmt);
                daoTable = "CRD_WHTRANS";
                insertTable();
                if (dupRecord.equals("Y")) {
                    comcr.errRtn("insert_CRD_WHTRANS duplicate!", "", hCallBatchSeqno);
                }

            }
        }
    }

    /***********************************************************************/
    void getWarehouseno() throws Exception {
        String tmpStr = "";
        String hTmp = "";

        hWarehouseNo = "";
        hBegWarehouse = String.format("%s00001", hChiYear);
        hEndWarehouse = String.format("%s99999", hChiYear);
        hTmp = "";

        sqlCmd = "select max(warehouse_no) h_tmp ";
        sqlCmd += " from crd_whtrans  ";
        sqlCmd += "where warehouse_no between ?  and ? ";
        setString(1, hBegWarehouse);
        setString(2, hEndWarehouse);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTmp = getValue("h_tmp");
        }

        tmpStr = String.format("%d", comcr.str2long(hTmp));
        hWarehouseNo = tmpStr;

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        DbcD018 proc = new DbcD018();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
