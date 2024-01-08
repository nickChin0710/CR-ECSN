/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
 *  109/07/03  V1.00.01    Zuwei     coding standard, rename field method & format                   *
 *  109/07/22  V1.00.02    shiyuqi     coding standard,                   *
 *  111/03/28  V1.00.03    JeffKung  remarked showMessage(Debug)                                        *
******************************************************************************/

package Dbb;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*重覆帳單查核*/
public class DbbA005 extends AccessDAO {
  private final String progname = "重覆帳單查核  111/03/28  V1.00.03";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  String hTempUser = "";
  final int DEBUG = 0;

  String prgmId = "DbbA005";
  String stderr = "";
  long hModSeqno = 0;
  String hModUser = "";
  String hModTime = "";
  String hModPgm = "";
  String hCallBatchSeqno = "";
  String hCurpModPgm = "";
  String hCurpModTime = "";
  String hCurpModUser = "";
  String hCurpMcsNum = "";
  String hCurpMcsCnt = "";
  long hCurpModSeqno = 0;

  String hBusinessDate = "";
  String hSystemDate = "";
  String hDcurCardNo = "";
  String hDcurFilmNo = "";
  String hDcurPurchaseDate = "";
  String hDcurTxnCode = "";
  double hDcurDestAmt = 0;
  String hDcurBillType = "";
  String hDcurBatchNo = "";
  String hDcurReferenceNo = "";
  String hDcurAuthCode = "";
  String hDcurRowid = "";
  String hDcurRskType = "";
  String hDcurRskTypeSpecial = "";
  String hDcurDoubtType = "";
  String hDcurDuplicatedFlag = "";
  String hDcurModPgm = "";
  String hDcurModTime = "";
  String hDcurModUser = "";
  long hDcurModSeqno = 0;
  int tempCount = 0;
  int totalCount = 0;
  int subCount = 0;
  String hTempX10 = "";

  // ***********************************************************

  public int mainProcess(String[] args) {
    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname);
      // =====================================
      if (args.length > 1) {
        String errMsg2 = "        1.batch_no : 批次號碼\n";
        errMsg2 += "            a.yyyymmdd:西元日期\n";
        errMsg2 += "            b.請款來源前二碼:'NC','OB','OU'\n";
        errMsg2 += "            c.序號: 4 碼\n";
        comc.errExit("Usage : DbbA005 ", errMsg2);
      }

      // 固定要做的

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

      comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

      String checkHome = comc.getECSHOME();
      if (comcr.hCallBatchSeqno.length() > 6) {
        if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6)
            .equals(comc.getSubString(checkHome, 0, 6))) {
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
      hModPgm = javaProgram;
      hDcurModPgm = hModPgm;
      hDcurModTime = hModTime;
      hDcurModUser = hModUser;

      selectDbbCurpost();
      showLogMessage("I", "", String.format("** dbb_curpost 總筆數   =[%d]", totalCount));
      // ==============================================
      // 固定要做的
      comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCount + "]";
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
      comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
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
    hModSeqno = comcr.getModSeq();
    hModUser = comc.commGetUserID();
    hModTime = hSystemDate;
  }

  /***********************************************************************/
  void selectDbbCurpost() throws Exception {
    daoTable = "dbb_curpost";
    updateSQL = "ACCTITEM_CONVT_FLAG = 'P'";
    whereStr = "where decode(double_chk_ok_flag,'','N',double_chk_ok_flag) in ('Y','y')  ";
    whereStr += "  and decode(err_chk_ok_flag   ,'','N',err_chk_ok_flag)    in ('Y','y')  ";
    whereStr += "  and decode(format_chk_ok_flag,'','N',format_chk_ok_flag) in ('N','n')  ";
    whereStr += "  and decode(curr_post_flag    ,'','N',curr_post_flag)     in ('N','n')  ";
    whereStr += "  and decode(manual_upd_flag   ,'','N',manual_upd_flag)    != 'Y' ";
    updateTable();

    sqlCmd = "select ";
    sqlCmd += "card_no,";
    sqlCmd += "film_no,";
    sqlCmd += "purchase_date,";
    sqlCmd += "txn_code,";
    sqlCmd += "dest_amt,";
    sqlCmd += "bill_type,";
    sqlCmd += "dbb_curpost.batch_no,";
    sqlCmd += "reference_no,";
    sqlCmd += "auth_code,";
    sqlCmd += "decode(mcs_num,'','0',mcs_num) as mcs_num ,";
    sqlCmd += "decode(mcs_cnt,'','0',mcs_cnt) as mcs_cnt ,";
    sqlCmd += "dbb_curpost.rowid as rowid ";
    sqlCmd += " from dbb_curpost,bil_postcntl ";
    sqlCmd += "where 1=1 ";
    sqlCmd += "  and decode(double_chk_ok_flag,'','N',double_chk_ok_flag) in ('Y','y') ";
    sqlCmd += "  and decode(err_chk_ok_flag   ,'','N',err_chk_ok_flag)    in ('Y','y') ";
    sqlCmd += "  and decode(format_chk_ok_flag,'','N',format_chk_ok_flag) in ('N','n') ";
    sqlCmd += "  and decode(curr_post_flag    ,'','N',curr_post_flag)     in ('N','n') ";
    sqlCmd += "  and decode(confirm_flag_p    ,'','N',confirm_flag_p)     in ('Y','y') ";
    sqlCmd += "  and decode(manual_upd_flag   ,'','N',manual_upd_flag)    != 'Y' ";
    sqlCmd += "  and decode(rsk_type,'' ,'N',rsk_type) not in ('1','2','3') ";
    sqlCmd += "  and bil_postcntl.batch_no  = dbb_curpost.batch_no ";
    sqlCmd += "order by card_no, purchase_date, film_no ";
    openCursor();
    while (fetchTable()) {
      hDcurCardNo = getValue("card_no");
      hDcurFilmNo = getValue("film_no");
      hDcurPurchaseDate = getValue("purchase_date");
      hDcurTxnCode = getValue("txn_code");
      hDcurDestAmt = getValueDouble("dest_amt");
      hDcurBillType = getValue("bill_type");
      hDcurBatchNo = getValue("batch_no");
      hDcurReferenceNo = getValue("reference_no");
      hDcurAuthCode = getValue("auth_code");
      hCurpMcsNum = getValue("mcs_num");
      hCurpMcsCnt = getValue("mcs_cnt");
      hDcurRowid = getValue("rowid");

      //showLogMessage("D", "","888  main card=[" + totalCount + "]" + hDcurCardNo + "," + hDcurAuthCode);

      totalCount = totalCount + 1;
      subCount++;
      if (subCount >= 5000) {
        subCount = 0;
        showLogMessage("I", "", String.format("Process record=[%d]\n", totalCount));
      }

      hDcurRskType = "";
      hDcurDoubtType = "";

      if (!hDcurAuthCode.equals("000000") && !hDcurAuthCode.equals("00000Y")
          && !hDcurAuthCode.equals("00000N") && !hDcurAuthCode.equals("Y")
          && !hDcurAuthCode.equals("N") && hDcurAuthCode.length() != 0) {
        chkDbbCurpost();
        if (hDcurRskType.length() < 1)
          chkDbbBill();
      } else {
        chkDbbCurpost1();
        if (hDcurRskType.length() < 1)
          chkDbbBill1();
      }

      if (hDcurRskType.equals("3")) {
        hDcurDuplicatedFlag = "Y";
        daoTable = "dbb_curpost";
        updateSQL = " rsk_type            = ?,";
        updateSQL += " rsk_type_special    = ?,";
        updateSQL += " mod_time            = sysdate,";
        updateSQL += " mod_pgm             = 'DbbA005',";
        updateSQL += " doubt_type          = ?,";
        updateSQL += " duplicated_flag     = ?,";
        updateSQL += " manual_upd_flag     = 'N',";
        updateSQL += " double_chk_ok_flag  = 'N',";
        updateSQL += " ACCTITEM_CONVT_FLAG = 'N' ";
        whereStr = "where rowid = ? ";
        setString(1, hDcurRskType);
        setString(2, hDcurRskTypeSpecial);
        setString(3, hDcurDoubtType);
        setString(4, hDcurDuplicatedFlag);
        setRowId(5, hDcurRowid);
        updateTable();
        if (notFound.equals("Y")) {
          comcr.errRtn("update_dbb_curpost not found!", "", hCallBatchSeqno);
        }
      } else {
        daoTable = "dbb_curpost";
        updateSQL = " double_chk_ok_flag  = 'N',";
        updateSQL += " mod_time            = sysdate,";
        updateSQL += " mod_pgm             = 'DbbA005',";
        updateSQL += " ACCTITEM_CONVT_FLAG = 'N' ";
        whereStr = "where rowid = ? ";
        setRowId(1, hDcurRowid);
        updateTable();
        if (notFound.equals("Y")) {
          comcr.errRtn("update_dbb_curpost not found!", "", hCallBatchSeqno);
        }
      }
    }
    closeCursor();
  }

  /***********************************************************************/
  void chkDbbCurpost() throws Exception {
    hTempX10 = "";
    tempCount = 0;
    sqlCmd = "select 1 temp_count,";
    sqlCmd += "reference_no ";
    sqlCmd += " from dbb_curpost  ";
    sqlCmd += "where card_no       = ?  ";
    sqlCmd += "  and auth_code     = ?  ";
    sqlCmd += "  and purchase_date = ?  ";
    sqlCmd += "  and txn_code      = ?  ";
    sqlCmd += "  and dest_amt      = ?  ";
    sqlCmd += "  and decode(ACCTITEM_CONVT_FLAG,'','N',ACCTITEM_CONVT_FLAG) != 'P'  ";
    sqlCmd += "  and decode(mcs_num ,'','0',mcs_num )  = ? ";
    sqlCmd += "  and decode(mcs_cnt ,'','0',mcs_cnt )  = ? ";
    sqlCmd += "  and rowid   != ?  ";
    sqlCmd += "  and film_no  = (select min(film_no) from dbb_curpost where card_no = ? ";
    sqlCmd += " and auth_code     = ?  ";
    sqlCmd += " and purchase_date = ?  ";
    sqlCmd += " and txn_code      = ?  ";
    sqlCmd += "   and decode(mcs_num ,'','0',mcs_num )  = ? ";
    sqlCmd += "   and decode(mcs_cnt ,'','0',mcs_cnt )  = ? ";
    sqlCmd += " and dest_amt      = ?  ";
    sqlCmd += " and decode(ACCTITEM_CONVT_FLAG,'','N',ACCTITEM_CONVT_FLAG) != 'P') ";
    sqlCmd += "fetch first 1 rows only ";
    setString(1, hDcurCardNo);
    setString(2, hDcurAuthCode);
    setString(3, hDcurPurchaseDate);
    setString(4, hDcurTxnCode);
    setDouble(5, hDcurDestAmt);
    setString(6, hCurpMcsNum);
    setString(7, hCurpMcsCnt);
    setRowId(8, hDcurRowid);
    setString(9, hDcurCardNo);
    setString(10, hDcurAuthCode);
    setString(11, hDcurPurchaseDate);
    setString(12, hDcurTxnCode);
    setString(13, hCurpMcsNum);
    setString(14, hCurpMcsCnt);
    setDouble(15, hDcurDestAmt);

    int recordCnt = selectTable();
    if (recordCnt > 0) {
      tempCount = getValueInt("temp_count");
      hTempX10 = getValue("reference_no");
    }

    //showLogMessage("D", "", " 888 chk_dbb_curpost=[" + tempCount + "]R=" + recordCnt);
    
    if (tempCount > 0) {
      hDcurRskType = "3";
    }
  }

  /***********************************************************************/
  void chkDbbBill() throws Exception {
    tempCount = 0;

    hTempX10 = "";
    sqlCmd = "select 1 temp_count,";
    sqlCmd += "reference_no ";
    sqlCmd += " from dbb_bill  ";
    sqlCmd += "where card_no          = ?  ";
    sqlCmd += "  and auth_code        = ?  ";
    sqlCmd += "  and purchase_date    = ?  ";
    sqlCmd += "  and txn_code         = ?  ";
    sqlCmd += "  and dest_amt         = ?  ";
    sqlCmd += "  and nvl(mcs_num,'0') = ?  ";
    sqlCmd += "  and nvl(mcs_cnt,'0') = ?  ";
    sqlCmd += "  and film_no  in (select min(film_no) from dbb_bill where card_no  = ? ";
    sqlCmd += "and auth_code        = ?  ";
    sqlCmd += "and purchase_date    = ?  ";
    sqlCmd += "and txn_code         = ?  ";
    sqlCmd += "and nvl(mcs_num,'0') = ? ";
    sqlCmd += "and nvl(mcs_cnt,'0') = ? ";
    sqlCmd += "and dest_amt         = ? )  ";
    sqlCmd += "fetch first 1 rows only ";
    setString(1, hDcurCardNo);
    setString(2, hDcurAuthCode);
    setString(3, hDcurPurchaseDate);
    setString(4, hDcurTxnCode);
    setDouble(5, hDcurDestAmt);
    setString(6, hCurpMcsNum);
    setString(7, hCurpMcsCnt);
    setString(8, hDcurCardNo);
    setString(9, hDcurAuthCode);
    setString(10, hDcurPurchaseDate);
    setString(11, hDcurTxnCode);
    setString(12, hCurpMcsNum);
    setString(13, hCurpMcsCnt);
    setDouble(14, hDcurDestAmt);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      tempCount = getValueInt("temp_count");
      hTempX10 = getValue("reference_no");
    }

    //showLogMessage("D", "", " 888 chk_dbb_bill =[" + tempCount + "]R=" + recordCnt);
    	
    if (tempCount > 0) {
      hDcurRskType = "3";
    }
  }

  /***********************************************************************/
  void chkDbbCurpost1() throws Exception {
    hTempX10 = "";
    tempCount = 0;
    sqlCmd = "select 1 temp_count,";
    sqlCmd += "reference_no ";
    sqlCmd += " from dbb_curpost  ";
    sqlCmd += "where card_no       = ?  ";
    sqlCmd += "  and film_no       = ?  ";
    sqlCmd += "  and purchase_date = ?  ";
    sqlCmd += "  and txn_code      = ?  ";
    sqlCmd += "  and dest_amt      = ?  ";
    sqlCmd += "  and decode(acctitem_convt_flag,'','N',acctitem_convt_flag) != 'P' ";
    sqlCmd += "  and rowid        != ?  ";
    sqlCmd += "fetch first 1 rows only ";
    setString(1, hDcurCardNo);
    setString(2, hDcurFilmNo);
    setString(3, hDcurPurchaseDate);
    setString(4, hDcurTxnCode);
    setDouble(5, hDcurDestAmt);
    setRowId(6, hDcurRowid);

    int recordCnt = selectTable();
    if (recordCnt > 0) {
      tempCount = getValueInt("temp_count");
      hTempX10 = getValue("reference_no");
    }

    //showLogMessage("D", "", " 888 chk_dbb_curpost1=[" + tempCount + "]R=" + recordCnt);
    
    if (tempCount > 0) {
      hDcurRskType = "3";
    }
  }

  /***********************************************************************/
  void chkDbbBill1() throws Exception {

    hTempX10 = "";
    tempCount = 0;

    sqlCmd = "select 1 temp_count,";
    sqlCmd += "reference_no ";
    sqlCmd += " from dbb_bill  ";
    sqlCmd += "where card_no       = ?  ";
    sqlCmd += "  and film_no       = ?  ";
    sqlCmd += "  and purchase_date = ?  ";
    sqlCmd += "  and txn_code      = ?  ";
    sqlCmd += "  and dest_amt      = ?  ";
    sqlCmd += "fetch first 1 rows only ";
    setString(1, hDcurCardNo);
    setString(2, hDcurFilmNo);
    setString(3, hDcurPurchaseDate);
    setString(4, hDcurTxnCode);
    setDouble(5, hDcurDestAmt);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      tempCount = getValueInt("temp_count");
      hTempX10 = getValue("reference_no");
    }

    //showLogMessage("D", "", " 888 chk_dbb_bill1=[" + tempCount + "]R=" + recordCnt);
      
    if (tempCount > 0) {
      hDcurRskType = "3";
    }
  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    DbbA005 proc = new DbbA005();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }
}
