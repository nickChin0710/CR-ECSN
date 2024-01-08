/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109/11/23  V1.00.01    shiyuqi   updated for project coding standard       * 
 *  109/11/30  V1.00.02    JeffKung  updated for TCB.                          *
 *  111/09/22  V1.00.03    JeffKung  rsk_type changed to "5"                   *
 *******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*重覆帳單查核*/
public class BilA005 extends AccessDAO {
    private String progname = "重覆帳單查核   111/09/22 V1.00.03 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallErrorDesc = "";
    String hTempUser = "";

    String prgmId = "BilA005";
    String prgmName = "重覆帳單查核";
    String rptName = "";
    int tmpInt = 0;
    int recordCnt = 0;
    int actCnt = 0;
    List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq = 0;
    int errCnt = 0;
    String errMsg = "";
    String buf = "";
    String szTmp = "";
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
    String hCurpCardNo = "";
    String hCurpFilmNo = "";
    String hCurpPurchaseDate = "";
    String hCurpTransactionCode = "";
    double hCurpDestinationAmt = 0;
    String hCurpBillType = "";
    String hCurpBatchNo = "";
    String hCurpReferenceNo = "";
    String hCurpAuthorization = "";
    String hCurpMcsNum = "";
    String hCurpMcsCnt = "";
    String hCurpRowid = "";
    String hCurpRskType = "";
    String hCurpDoubtType = "";
    String hCurpDuplicatedFlag = "";
    String hCurpPromoteDept = "";
    int tempCount = 0;
    String hTempX10 = "";

    int totCnt = 0;

    // *********************************************************

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
            commonRtn();
            
            showLogMessage("I", "", "Process_date = " + hBusinessDate);

            hModPgm = prgmId;
            hCurpModPgm = hModPgm;
            hCurpModTime = hModTime;
            hCurpModUser = hModUser;
            hCurpModSeqno = hModSeqno;

            selectBilCurpost();


            // ==============================================
            // 固定要做的
            showLogMessage("I", "", String.format("程式執行結束,總筆數=[%d]", totCnt));

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
        sqlCmd += "  from ptr_businday ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
        }
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += "  from dual ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
        }
        hModSeqno = comcr.getModSeq();
        hModUser = comc.commGetUserID();
        hModTime = hSystemDate;
    }

    /***********************************************************************/
    void selectBilCurpost() throws Exception
    {
        sqlCmd = "select ";
        sqlCmd += "card_no,";
        sqlCmd += "film_no,";
        sqlCmd += "purchase_date,";
        sqlCmd += "txn_code,";
        sqlCmd += "dest_amt,";
        sqlCmd += "bill_type,";
        sqlCmd += "bil_curpost.rsk_type,";
        sqlCmd += "bil_curpost.batch_no,";
        sqlCmd += "reference_no,";
        sqlCmd += "auth_code,";
        sqlCmd += "decode(mcs_num,'','0',mcs_num) as mcs_num ,";
        sqlCmd += "decode(mcs_cnt,'','0',mcs_cnt) as mcs_cnt ,";
        sqlCmd += "bil_curpost.rowid   as rowid ";
        sqlCmd += " from bil_curpost,bil_postcntl ";
        sqlCmd += "where decode(double_chk_ok_flag,'','N',double_chk_ok_flag) in ('Y','y') ";
        sqlCmd += "  and decode(format_chk_ok_flag,'','N',format_chk_ok_flag) in ('N','n') ";
        sqlCmd += "  and decode(err_chk_ok_flag   ,'','N',err_chk_ok_flag)    in ('N','n') ";
        sqlCmd += "  and decode(curr_post_flag    ,'','N',curr_post_flag)     in ('N','n') ";
        sqlCmd += "  and decode(confirm_flag_p    ,'','N',confirm_flag_p)     in ('Y','y') ";
        sqlCmd += "  and decode(manual_upd_flag   ,'','N',manual_upd_flag)    != 'Y' ";

        //全部都要處理
        //sqlCmd += "  and decode(rsk_type,'','N',rsk_type )      not in ('1','2','3') ";
        
        sqlCmd += "  and batch_date  = substr(bil_curpost.batch_no, 1,8) ";
        sqlCmd += "  and batch_unit  = substr(bil_curpost.batch_no, 9,2) ";
        sqlCmd += "  and batch_seq   = substr(bil_curpost.batch_no,11,4) ";
        sqlCmd += "order by card_no, purchase_date, film_no ";

        openCursor();
        while (fetchTable()) {
            hCurpCardNo = getValue("card_no");
            hCurpFilmNo = getValue("film_no");
            hCurpPurchaseDate = getValue("purchase_date");
            hCurpTransactionCode = getValue("txn_code");
            hCurpDestinationAmt = getValueDouble("dest_amt");
            hCurpBillType = getValue("bill_type");
            hCurpRskType = getValue("rsk_type");
            hCurpBatchNo = getValue("batch_no");
            hCurpReferenceNo = getValue("reference_no");
            hCurpAuthorization = getValue("auth_code");
            hCurpMcsNum = getValue("mcs_num");
            if(hCurpMcsNum.length() == 0) hCurpMcsNum = "0";
            hCurpMcsCnt = getValue("mcs_cnt");
            if(hCurpMcsCnt.length() == 0) hCurpMcsCnt = "0";
            hCurpRowid = getValue("rowid");

            totCnt++;
            if (totCnt % 5000 == 0 || totCnt == 1)
                showLogMessage("I", "", "Current Process record=" + totCnt);

            /* debug
                showLogMessage("D", "", "888 Card=" + hCurpCardNo + "," + hCurpTransactionCode + ",cnt=" + totCnt +","+ hCurpReferenceNo);
                showLogMessage("D", "", "    amt =" + hCurpDestinationAmt +","+ hCurpFilmNo +","+ hCurpMcsNum +","+ hCurpMcsCnt);
            */
            
            hCurpDoubtType = "";
           
            //沒有rsk_type的資料才做重覆帳單的比對
            if ("".equals(hCurpRskType)) {
            	chkBilCurpost();
            	chkBilBill();
            }

            /* debug
                showLogMessage("D", "", "    RSK=" + hCurpRskType);
            */
            
            if (hCurpRskType.equals("5")) {
                hCurpDuplicatedFlag = "Y";
                daoTable   = "bil_curpost";
                updateSQL  = " rsk_type     = ?  ,";
                updateSQL += " mod_pgm      = 'BilA005',";
                updateSQL += " mod_time     = sysdate,";
                updateSQL += " doubt_type   = ?  ,";
                updateSQL += " duplicated_flag    = ?  ,";
                updateSQL += " promote_dept       = ?  ,";
                updateSQL += " manual_upd_flag    = 'N',";
                updateSQL += " double_chk_ok_flag = 'N',";
                updateSQL += " acctitem_convt_flag= 'N' ";
                whereStr   = "where rowid   = ? ";
                setString(1, hCurpRskType);
                setString(2, hCurpDoubtType);
                setString(3, hCurpDuplicatedFlag);
                setString(4, hCurpPromoteDept);
                setRowId(5, hCurpRowid);
                actCnt = updateTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("update_bil_curpost not found!", "", hCurpReferenceNo);
                }
            } else {
                daoTable   = "bil_curpost";
                updateSQL  = " double_chk_ok_flag = 'N',";
                updateSQL += " acctitem_convt_flag= 'N',";
                updateSQL += " mod_pgm      = 'BilA005',";
                updateSQL += " mod_time     = sysdate ";
                whereStr   = "where rowid   = ? ";
                setRowId(1, hCurpRowid);
                actCnt = updateTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("update_bil_curpost not found!", "", hCurpReferenceNo);
                }
            }
        }
        closeCursor();
    }
    /***********************************************************************/
    void chkBilCurpost() throws Exception {
        hTempX10 = "";
        tempCount = 0;
        sqlCmd = "select 1 temp_count,";
        sqlCmd += "reference_no , film_no";
        sqlCmd += "  from bil_curpost  ";
        sqlCmd += " where card_no       = ?  ";
        sqlCmd += "   and purchase_date = ?  ";
        sqlCmd += "   and auth_code     = ?  ";
        sqlCmd += "   and txn_code      = ?  ";
        sqlCmd += "   and dest_amt      = ?  ";
        sqlCmd += "   and decode(mcs_num ,'','0',mcs_num )  = ? ";
        sqlCmd += "   and decode(mcs_cnt ,'','0',mcs_cnt )  = ? ";
        sqlCmd += "   and reference_no != ?  ";
        sqlCmd += "   and film_no   = (select min(film_no) from bil_curpost ";
        sqlCmd += "                     where card_no       = ?  ";
        sqlCmd += "                       and purchase_date = ?  ";
        sqlCmd += "                       and auth_code     = ?  ";
        sqlCmd += "                       and txn_code      = ?  ";
        sqlCmd +=                     "   and decode(mcs_num ,'','0',mcs_num )  = ? ";
        sqlCmd +=                     "   and decode(mcs_cnt ,'','0',mcs_cnt )  = ? ";
        sqlCmd += "                       and dest_amt      = ?  )";
        setString( 1, hCurpCardNo);
        setString( 2, hCurpPurchaseDate);
        setString( 3, hCurpAuthorization);
        setString( 4, hCurpTransactionCode);
        setDouble( 5, hCurpDestinationAmt);
        setString( 6, hCurpMcsNum);
        setString( 7, hCurpMcsCnt);
        setString( 8, hCurpReferenceNo);
        setString( 9, hCurpCardNo);
        setString(10, hCurpPurchaseDate);
        setString(11, hCurpAuthorization);
        setString(12, hCurpTransactionCode);
        setString(13, hCurpMcsNum);
        setString(14, hCurpMcsCnt);
        setDouble(15, hCurpDestinationAmt);
        tmpInt = selectTable();
       
        /* debug
            showLogMessage("D", "", "   1 =" + hCurpCardNo +","+ hCurpPurchaseDate);
            showLogMessage("D", "", "   2 =" + hCurpAuthorization);
            showLogMessage("D", "", "   3 =" + hCurpPurchaseDate);
            showLogMessage("D", "", "   4 =" + hCurpTransactionCode);
            showLogMessage("D", "", "   5 =" + hCurpDestinationAmt);
            showLogMessage("D", "", "   6 =" + hCurpReferenceNo);
            showLogMessage("D", "", "   chk_bil_curpost=" + tmpInt);
        */

        if (tmpInt > 0) {
            tempCount = getValueInt("temp_count");
            hTempX10 = getValue("reference_no");
        }

        if (tempCount > 0) {
            hCurpRskType = "5";
            hCurpPromoteDept = hTempX10;
        }
    }

    /***********************************************************************/
    void chkBilBill() throws Exception {
        tempCount = 0;

        hTempX10 = "";
        sqlCmd = "select 1 temp_count,";
        sqlCmd += "reference_no ";
        sqlCmd += "  from bil_bill  ";
        sqlCmd += " where card_no       = ?  ";
        sqlCmd += "   and auth_code     = ?  ";
        sqlCmd += "   and purchase_date = ?  ";
        sqlCmd += "   and txn_code      = ?  ";
        sqlCmd += "   and dest_amt      = ?  ";
        sqlCmd += "   and decode(mcs_num ,'','0',mcs_num )  = ? ";
        sqlCmd += "   and decode(mcs_cnt ,'','0',mcs_cnt )  = ? ";
        setString(1, hCurpCardNo);
        setString(2, hCurpAuthorization);
        setString(3, hCurpPurchaseDate);
        setString(4, hCurpTransactionCode);
        setDouble(5, hCurpDestinationAmt);
        setString(6, hCurpMcsNum);
        setString(7, hCurpMcsCnt);
        tmpInt = selectTable();
        
        /* debug
            showLogMessage("D", "", "   chk_bil_bill=" + tmpInt);
        */
            
        if (tmpInt > 0) {
            tempCount = getValueInt("temp_count");
            hTempX10 = getValue("reference_no");
        }
        if (tempCount > 0) {
            hCurpRskType = "5";
            hCurpPromoteDept = hTempX10;
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilA005 proc = new BilA005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
