/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/06/19  V1.00.01    Jeff Kung    update for TCB requirements.
*  109/07/03  V1.00.02    Zuwei     coding standard, rename field method & format     *
*  109/07/22  V1.00.03    shiyuqi     coding standard,                                                                           *
*  109/09/13  V1.00.04    JeffKung   第一階段負向交易暫不查核                          *
*  109/09/14  V1.00.05    JeffKung   rsk_type_special assigned        *
*  109/09/15  V1.00.06    JeffKung   unlock_flag in ('','N')      *
*  109/09/21  V1.00.07    JeffKung   cca_card_acct.debit_flag = 'Y'    * 
*  109/09/24  V1.00.08    JeffKung   rsk_type '2' adjust         *
*  109/12/24  V1.00.09    yanghan       修改了變量名稱和方法名稱            *
*  110/03/11  V1.00.10    Justin         add UNLOCK_FLAG = 'Y'           *
*  111/03/10  V1.00.11    JeffKung   增加第二次授權比對及第三次授權比對          *
*  111/03/29  V1.00.12    JeffKung   5542自動加油增加nt_amt=1500的判斷     *
*  111/05/30  V1.00.13    JeffKung   改成逐筆commit                                    *
*  111/10/09  V1.00.14    Alex       增加寫入dba_acaj.adj_reason_code 調整原因 , 調整原因 02 : 請款比對系統解圈 *
*  111/12/12  V1.00.15    JeffKung   日本加油5541授權金額為0的問題處理
*  112/03/02  V1.00.16    JeffKung   增加VD悠遊卡授權比對邏輯
******************************************************************************/

package Dbb;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*疑異查核作業*/
public class DbbA004 extends AccessDAO {
  private final String progname = "疑異查核作業 112/03/02  V1.00.16";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  String hTempUser = "";
  final int debug = 0;

  String prgmId = "DbbA004";
  String stderr = "";
  long hModSeqno = 0;
  String hModUser = "";
  String hModTime = "";
  String hModPgm = "";
  String hCallBatchSeqno = "";
  String hCurpModPgm = "";
  String hCurpModTime = "";
  String hCurpModUser = "";
  long hCurpModSeqno = 0;

  String hSystemDate = "";
  String hSystemDateF = "";
  String hDcurBillBype = "";
  String hDcurTxnCode = "";
  String hDcurAcqMemberId = "";
  String hDcurDestCurr = "";
  double hDcurSourceAmt = 0;
  String hDcurSourceCurr = "";
  String hDcurMchtEngName = "";
  String hDcurMchtCity = "";
  String hDcurMchtCategory = "";
  String hDcurMchtZip = "";
  String hDcurMchtState = "";
  String hDcurAuthCode = "";
  String hDcurBatchNo = "";
  String hDcurProcessDate = "";
  String hDcurMchtNo = "";
  String hDcurMchtChiName = "";
  String hDcurContractNo = "";
  String hDcurTerm = "";
  String hDcurTotalTerm = "";
  String hDcurAcctEngShortName = "";
  String hDcurAcctChiShortName = "";
  String hDcurDoubtType = "";
  String hDcurDuplicatedFlag = "";
  String hDcurAcctType = "";
  String hDcurCurrCode = "";
  String hDcurPromoteDept = "";
  String hDcurProdNo = "";
  String hDcurGroupCode = "";
  String hDcurBinType = "";
  String hDcurPSeqno = "";
  String hDcurIdPSeqno = "";
  String hDcurReferenceNoOriginal = "";
  String hDcurRskTypeSpecial = "";
  String hDcurRskType = "";
  String hDcurRowid = "";
  int tempCount1 = 0;
  String hDcurCardNo = "";
  int tempCount2 = 0;
  String hCardMajorCardNo = "";
  String hCardCurrentCode = "";
  String hCardOppostDate = "";
  String hCardPromoteDept = "";
  String hCardProdNo = "";
  String hCardGroupCode = "";
  String hCardCardType = "";
  String hCardGpNo = "";
  String hCardIdPSeqno = "";
  String hCardAcctType = "";
  String hCardPSeqno = "";
  String hCardAcnoPSeqno = "";
  String hCardDebitAcctNo = "";
  String hCardBlockDate = "";
  String hCardBlockStatus = "";
  String hCardNewBegDate = "";
  String hCardNewEndDate = "";
  String hRskGroup = "";
  String hTempReferenceNo = "";
  String hDcurFilmNo = "";
  String hDcurPurchaseDate = "";
  String hDcurPosEntryMode = "";
  String dateF = "";
  String dateT = "";
  String dateO = "";
  String hDcasRowid = "";
  String hDcasRskType = "";
  String hDcasReferenceNo = "";
  double hDcurDestAmt = 0;
  String hDcurReferenceNo = "";
  String hDcurMchtCountry = "";
  String hDcasAuthCode = "";
  double hDcasDestAmt = 0;
  String hDcasPurchaseDate = "";
  String hDbilReferenceNo = "";
  String hDbilRowid = "";

  String hBusinessDate = "";
  String hDcurModPgm = "";
  String hDcurModTime = "";
  String hDcurModUser = "";
  String hDcurModWs = "";
  long hDcurModSeqno = 0;
  String hDcurModLog = "";
  int indexCnt = 0;
  int totalCount = 0;
  int rtn = 0;

  // for check VD Auth
  com.CommDate commDate = new com.CommDate();
  com.CommString commString = new com.CommString();
  com.CommSqlStr commSqlStr = new com.CommSqlStr();
  double vdHighRate = 0.0;
  double vdLowRate = 0.0;
  double vdHighDay = 0;
  double vdLowDay = 0;
  String hTxAuthTxSeq = "";
  double hTxAuthNtAmt = 0;
  double hTxAuthVdLockNtAmt = 0;

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
      if (args.length != 0 && args.length != 1) {
        String errMsg2 = "        1.batch_no : 批次號碼\n";
        errMsg2 += "            a.yyyymmdd:西元日期\n";
        errMsg2 += "            b.請款來源前二碼:'NC','OB','OU'\n";
        errMsg2 += "            c.序號: 4 碼\n";
        comc.errExit("Usage : DbbA004 batch_seq", errMsg2);
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

      // 取VD授權比對參數
      getAuthParm();

      selectDbbCurpost();

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
    sqlCmd = "select ";
    sqlCmd += "reference_no,";
    sqlCmd += "bill_type,";
    sqlCmd += "txn_code,";
    sqlCmd += "card_no,";
    sqlCmd += "film_no,";
    sqlCmd += "acq_member_id,";
    sqlCmd += "purchase_date,";
    sqlCmd += "dest_amt,";
    sqlCmd += "dest_curr,";
    sqlCmd += "source_amt,";
    sqlCmd += "source_curr,";
    sqlCmd += "mcht_eng_name,";
    sqlCmd += "mcht_city,";
    sqlCmd += "mcht_country,";
    sqlCmd += "mcht_category,";
    sqlCmd += "mcht_zip,";
    sqlCmd += "mcht_state,";
    sqlCmd += "auth_code,";
    sqlCmd += "dbb_curpost.batch_no,";
    sqlCmd += "process_date,";
    sqlCmd += "dbb_curpost.mcht_no,";
    sqlCmd += "mcht_chi_name,";
    sqlCmd += "contract_no,";
    sqlCmd += "term,";
    sqlCmd += "total_term,";
    sqlCmd += "acct_eng_short_name,";
    sqlCmd += "acct_chi_short_name,";
    sqlCmd += "doubt_type,";
    sqlCmd += "duplicated_flag,";
    sqlCmd += "acct_type,";
    sqlCmd += "curr_code,";
    sqlCmd += "promote_dept,";
    sqlCmd += "prod_no,";
    sqlCmd += "group_code,";
    sqlCmd += "bin_type,";
    sqlCmd += "rsk_type,";
    sqlCmd += "p_seqno,";
    sqlCmd += "id_p_seqno,";
    sqlCmd += "reference_no_original,";
    sqlCmd += "pos_entry_mode,";
    sqlCmd += "dbb_curpost.rowid as rowid ";
    sqlCmd += " from dbb_curpost , bil_postcntl ";
    sqlCmd += "where 1=1 ";
    sqlCmd += "  and decode(err_chk_ok_flag   ,'','N',err_chk_ok_flag)    in ('Y','y') ";
    sqlCmd += "  and decode(double_chk_ok_flag,'','N',double_chk_ok_flag) in ('N','n') ";
    sqlCmd += "  and decode(format_chk_ok_flag,'','N',format_chk_ok_flag) in ('N','n') ";
    sqlCmd += "  and decode(curr_post_flag    ,'','N',curr_post_flag)     in ('N','n') ";
    sqlCmd += "  and decode(confirm_flag_p    ,'','N',confirm_flag_p)     in ('Y','y') ";
    sqlCmd += "  and decode(manual_upd_flag   ,'','N',manual_upd_flag)    != 'Y' ";
    sqlCmd += "  and batch_date  = substr(dbb_curpost.batch_no,1,8) ";
    sqlCmd += "  and batch_unit  = substr(dbb_curpost.batch_no,9,2) ";
    sqlCmd += "  and trim(to_char(batch_seq,'0000'))=substr(dbb_curpost.batch_no,11,4) ";
    openCursor();
    while (fetchTable()) {
      hDcurRskTypeSpecial = "";
      hDcurReferenceNo = getValue("reference_no");
      hDcurBillBype = getValue("bill_type");
      hDcurTxnCode = getValue("txn_code");
      hDcurCardNo = getValue("card_no");
      hDcurFilmNo = getValue("film_no");
      hDcurAcqMemberId = getValue("acq_member_id");
      hDcurPurchaseDate = getValue("purchase_date");
      hDcurDestAmt = getValueDouble("dest_amt");
      hDcurDestCurr = getValue("dest_curr");
      hDcurSourceAmt = getValueDouble("source_amt");
      hDcurSourceCurr = getValue("source_curr");
      hDcurMchtEngName = getValue("mcht_eng_name");
      hDcurMchtCity = getValue("mcht_city");
      hDcurMchtCountry = getValue("mcht_country");
      hDcurMchtCategory = getValue("mcht_category");
      hDcurMchtZip = getValue("mcht_zip");
      hDcurMchtState = getValue("mcht_state");
      hDcurAuthCode = getValue("auth_code");
      hDcurBatchNo = getValue("batch_no");
      hDcurProcessDate = getValue("process_date");
      hDcurMchtNo = getValue("mcht_no");
      hDcurMchtChiName = getValue("mcht_chi_name");
      hDcurContractNo = getValue("contract_no");
      hDcurTerm = getValue("term");
      hDcurTotalTerm = getValue("total_term");
      hDcurAcctEngShortName = getValue("acct_eng_short_name");
      hDcurAcctChiShortName = getValue("acct_chi_short_name");
      hDcurDoubtType = getValue("doubt_type");
      hDcurDuplicatedFlag = getValue("duplicated_flag");
      hDcurAcctType = getValue("acct_type");
      hDcurCurrCode = getValue("curr_code");
      hDcurPromoteDept = getValue("promote_dept");
      hDcurProdNo = getValue("prod_no");
      hDcurGroupCode = getValue("group_code");
      hDcurBinType = getValue("bin_type");
      hDcurPSeqno = getValue("p_seqno");
      hDcurIdPSeqno = getValue("id_p_seqno");
      hDcurReferenceNoOriginal = getValue("reference_no_original");
      hDcurRskType = getValue("rsk_type");
      hDcurPosEntryMode = getValue("pos_entry_mode");
      hDcurRowid = getValue("rowid");

      totalCount++;

      //showLogMessage("D", "", "Read card_no[" + totalCount + "]" + hDcurCardNo + "," + hDcurReferenceNo);
      //showLogMessage("D", "", "     tx_code=" + hDcurTxnCode + ",rsk=" + hDcurRskType);

      // 費用類不作查核
      if (hDcurBillBype.equals("FIFC")) {
        daoTable = "dbb_curpost";
        updateSQL = " rsk_type         = '', ";
        updateSQL += " rsk_type_special = '', ";
        updateSQL += " mod_time         = sysdate,";
        updateSQL += " mod_pgm          = ?, ";
        updateSQL += " manual_upd_flag  = 'N', ";
        updateSQL += " err_chk_ok_flag  = 'N'  ";
        whereStr = "where rowid       = ? ";
        setString(1, prgmId);
        setRowId(2, hDcurRowid);
        updateTable();
        if (notFound.equals("Y")) {
          String stderr = "update_dbb_curpost not found!";
          comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
        continue;
      }

      chkPtrBintable();

      //showLogMessage("D", "", "  chk_ptr_bin=" + hDcurRskType);
      hTxAuthTxSeq = "";
      hTxAuthNtAmt = 0;
      hTxAuthVdLockNtAmt = 0;

      // 請款資料無授權碼 或授權碼值小於六位數且不為票證交易
      if ((hDcurRskType.length() == 0) && hDcurAuthCode.length() < 6 && 
    		  hDcurBillBype.equals("TSCC")==false  ) {
        hDcurRskType = "6";
        hDcurRskTypeSpecial = hDcurRskType;
      }

      //showLogMessage("D", "", "  chk_dbb_ccas_all=" + hDcurRskType + "," + tempCount2);

      //第一次 授權比對
      int cva = 0;
      
      
      if (tempCount2 > 0 && hDcurRskType.length() == 0) {
    	  if (hDcurBillBype.equals("TSCC") ) {
    		  //VDUA自動加值才比對, 沒有授權碼所以要寫另外的邏輯
        	  if ("VDUA".equals(comc.getSubString(hDcurMchtEngName,0,4))) {
        		  cva = checkVdTSCCAuth();
        	  } else {
        		  cva = 0; 
        	  }
        	  
          } else {
        	  cva = checkVdAuth();
          }
      }
      
      // 第二次 授權比對(第一次比對不成功才執行)
      if (cva == 1) {
    	  //正向交易才比對
    	  if (commString.pos("|05|26|07|08|09", hDcurTxnCode) > 0) {    
    		  cva = checkVdAuth2();
    	  } else {
    		  cva = 0;
    		  hDcurRskType = "8";  //負向交易
    	      hDcurRskTypeSpecial = hDcurRskType;
    	  }
    	  
      }
      
   // 第三次 授權比對(第二次比對不成功才執行)
      if (cva == 1) {
    	  cva = checkVdAuth3();
      }
      
      // 停卡日及效期檢核
      if (tempCount2 > 0 && hDcurRskType.length() == 0) {
        checkCardStatus();
      }

      /*第一階段負向交易先不查核(2020/9/13)
      // 退貨時比對原始交易是否已扣款
      if (hDcurRskType.length() == 0 && (hDcurTxnCode.equals("06") || hDcurTxnCode.equals("25")
          || hDcurTxnCode.equals("27") || hDcurTxnCode.equals("28") || hDcurTxnCode.equals("29"))) {
        checkNegatvieTxn();
      }
      
      */

      if (hDcurRskType.length() != 0) {
        daoTable = "dbb_curpost";
        updateSQL = " rsk_type         = ?,";
        updateSQL += " duplicated_flag  = ?,";
        updateSQL += " rsk_type_special = ?,";
        updateSQL += " mod_time         = sysdate,";
        updateSQL += " mod_pgm          = ?,";
        updateSQL += " manual_upd_flag  = 'N',";
        updateSQL += " err_chk_ok_flag  = 'N', ";
        updateSQL += " tx_seq  = ? , ";
        updateSQL += " auth_nt_amt  = ? , ";
        updateSQL += " vd_lock_nt_amt  = ?  ";
        whereStr = "where rowid       = ? ";
        setString(1, hDcurRskType);
        setString(2, hDcurDuplicatedFlag);
        setString(3, hDcurRskTypeSpecial);
        setString(4, prgmId);
        setString(5, hTxAuthTxSeq);
        setDouble(6,  hTxAuthNtAmt);
        setDouble(7, hTxAuthVdLockNtAmt);
        setRowId(8, hDcurRowid);
        updateTable();
        if (notFound.equals("Y")) {
          String stderr = "update_dbb_curpost not found!";
          comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
      } else {
        daoTable = "dbb_curpost";
        updateSQL = " rsk_type         = ?, ";
        updateSQL += " duplicated_flag  = ?, ";
        updateSQL += " rsk_type_special = ?, ";
        updateSQL += " mod_pgm          = ?, ";
        updateSQL += " mod_time         = sysdate,";
        updateSQL += " err_chk_ok_flag  = 'N' , ";
        updateSQL += " tx_seq  = ? , ";
        updateSQL += " auth_nt_amt  = ? , ";
        updateSQL += " vd_lock_nt_amt  = ?  ";
        whereStr = "where rowid       = ? ";
        setString(1, hDcurRskType);
        setString(2, hDcurDuplicatedFlag);
        setString(3, hDcurRskTypeSpecial);
        setString(4, prgmId);
        setString(5, hTxAuthTxSeq);
        setDouble(6,  hTxAuthNtAmt);
        setDouble(7, hTxAuthVdLockNtAmt);
        setRowId(8, hDcurRowid);
        updateTable();
        if (notFound.equals("Y")) {
          String stderr = "update_dbb_curpost not found!";
          comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
      }
      commitDataBase();     //改成逐筆commit
    }
    closeCursor();
  }

  /***************************************************************************/
  void initCrdCard() {
    hCardMajorCardNo = "";
    hCardCurrentCode = "";
    hCardOppostDate = "";
    hCardPromoteDept = "";
    hCardProdNo = "";
    hCardGroupCode = "";
    hCardCardType = "";
    hCardGpNo = "";
    hCardAcctType = "";
    hCardPSeqno = "";
    hCardAcnoPSeqno = "";
    hCardIdPSeqno = "";
    hCardDebitAcctNo = "";
    hCardBlockDate = "";
    hCardBlockStatus = "";
    hCardNewBegDate = "";
    hCardNewEndDate = "";
  }

  /***********************************************************************/
  void chkPtrBintable() throws Exception {

    initCrdCard();
    hCardDebitAcctNo = "";
    tempCount2 = 0;
    sqlCmd = "select 1 temp_count2,";
    sqlCmd += "major_card_no,";
    sqlCmd += "current_code,";
    sqlCmd += "oppost_date,";
    sqlCmd += "promote_dept,";
    sqlCmd += "prod_no,";
    sqlCmd += "group_code,";
    sqlCmd += "card_type,";
    sqlCmd += "acct_type,";
    sqlCmd += "p_seqno,";
    sqlCmd += "id_p_seqno,";
    sqlCmd += "acct_no,";
    sqlCmd += "new_beg_date,";
    sqlCmd += "new_end_date ";
    sqlCmd += " from dbc_card  ";
    sqlCmd += "where card_no  = ? ";
    setString(1, hDcurCardNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      tempCount2 = getValueInt("temp_count2");
      hCardMajorCardNo = getValue("major_card_no");
      hCardCurrentCode = getValue("current_code");
      hCardOppostDate = getValue("oppost_date");
      hCardPromoteDept = getValue("promote_dept");
      hCardProdNo = getValue("prod_no");
      hCardGroupCode = getValue("group_code");
      hCardCardType = getValue("card_type");
      hCardGpNo = getValue("p_seqno");
      hCardAcctType = getValue("acct_type");
      hCardPSeqno = getValue("p_seqno");
      hCardAcnoPSeqno = hCardPSeqno;
      hCardIdPSeqno = getValue("id_p_seqno");
      hCardDebitAcctNo = getValue("acct_no");
      hCardNewBegDate = getValue("new_beg_date");
      hCardNewEndDate = getValue("new_end_date");
    }

    if (tempCount2 == 0) {
      hDcurRskType = "1";
      return;
    }
    
  }
  
  /***********************************************************************
   *VD卡悠遊卡授權比對
   * 
   */
  int checkVdTSCCAuth() throws Exception {
    String hRowid = "";

    sqlCmd =
        " select tx_seq , nt_amt, vd_lock_nt_amt, hex(rowid) as rowid from cca_auth_txlog where card_no = ? ";
    sqlCmd += " and tx_date = ? and abs(nt_amt) = ? and trans_code = 'VA' ";
    sqlCmd += " and unlock_flag in ('','N')   and tx_seq <> '' and mtch_flag <> 'Y' "; // 排除人工解圈M或系統解圈1

    sqlCmd += commSqlStr.rownum(1);

    setString(1, hDcurCardNo);
    setString(2, hDcurPurchaseDate);
    setDouble(3, hDcurDestAmt);

    int r = selectTable();

    // 授權記錄檔比不到
    if (r <= 0) {
    	hDcurRskType = "7";
        hDcurRskTypeSpecial = hDcurRskType;
        return 0;
    }

    hRowid = getValue("rowid");
    hTxAuthTxSeq = getValue("tx_seq");
    hTxAuthNtAmt = getValueDouble("nt_amt");
    hTxAuthVdLockNtAmt = getValueDouble("vd_lock_nt_amt");

    if (commString.pos("|05|26|07|08|09", hDcurTxnCode) > 0) {
      updateCcaAuthTxlog(hRowid);
    }
    
    return 0;
  }



  /***********************************************************************
   *第一次授權比對
   * 
   */
  int checkVdAuth() throws Exception {
    String lsHighDate = "", lsLowDate = "";
    Double ldHighAmt = 0.0, ldLowAmt = 0.0;
    String hRowid = "";

    /*國內交易沒有模糊比對 */
    if (hDcurMchtCountry.length() >= 2 && hDcurMchtCountry.substring(0,2).equalsIgnoreCase("TW")) {
    	lsHighDate = hDcurPurchaseDate;
    	lsLowDate = hDcurPurchaseDate;
    	ldHighAmt = hDcurDestAmt;
    	ldLowAmt = hDcurDestAmt;
    } else {
    	lsHighDate = commDate.dateAdd(hDcurPurchaseDate, 0, 0, (int) vdHighDay);
        lsLowDate = commDate.dateAdd(hDcurPurchaseDate, 0, 0, (int) -vdLowDay);
        ldHighAmt = Math.abs(hDcurDestAmt) * (1 + vdHighRate / 100);
        ldLowAmt = Math.abs(hDcurDestAmt) * (1 - vdLowRate / 100);
    }
    

    sqlCmd =
        " select tx_seq , nt_amt, vd_lock_nt_amt, hex(rowid) as rowid from cca_auth_txlog where card_no = ? and auth_no = ? ";
    sqlCmd += " and tx_date >= ? and tx_date <= ? and abs(nt_amt) >= ? and abs(nt_amt) <= ? ";
    sqlCmd += " and unlock_flag in ('','N')   and tx_seq <> '' "; // 排除人工解圈M或系統解圈1

    if (commString.pos("|05|26|07|08|09", hDcurTxnCode) > 0) {
      sqlCmd += " and mtch_flag <> 'Y' and nt_amt >0 ";
    } else if (commString.pos("|25|06|27|28|29", hDcurTxnCode) > 0) {
      sqlCmd += " and mtch_flag = 'Y' ";
    }

    /*
     * if(h_dcur_dest_amt>0) sqlCmd += " and nt_amt <0 "; else if (h_dcur_dest_amt<0) sqlCmd +=
     * " and nt_amt >0 ";
     */

    sqlCmd += commSqlStr.rownum(1);

    setString(1, hDcurCardNo);
    setString(2, hDcurAuthCode);
    setString(3, lsLowDate);
    setString(4, lsHighDate);
    setDouble(5, ldLowAmt);
    setDouble(6, ldHighAmt);

    int r = selectTable();

    // 授權記錄檔比不到
    if (r <= 0) {
      return 1;
    }

    hRowid = getValue("rowid");
    hTxAuthTxSeq = getValue("tx_seq");
    hTxAuthNtAmt = getValueDouble("nt_amt");
    hTxAuthVdLockNtAmt = getValueDouble("vd_lock_nt_amt");

    if (commString.pos("|05|26|07|08|09", hDcurTxnCode) > 0) {
      updateCcaAuthTxlog(hRowid);
    }
    
    return 0;
  }

  
  /***********************************************************************
   *第二次授權比對 :	授權日期與請款檔的<交易日期不一致>, 如住宿, 網路購物, <金額相符> 及 <授權碼相符>
   * 
   */
  int checkVdAuth2() throws Exception {
    String lsHighDate = "", lsLowDate = "";
    Double ldHighAmt = 0.0, ldLowAmt = 0.0;
    String hRowid = "";

    lsHighDate = hDcurPurchaseDate;
    lsLowDate = hDcurPurchaseDate;
    ldHighAmt = hDcurDestAmt;
    ldLowAmt = hDcurDestAmt;

    sqlCmd =
        " select tx_seq , nt_amt, vd_lock_nt_amt, hex(rowid) as rowid from cca_auth_txlog where card_no = ? and auth_no = ? ";
    sqlCmd += " and abs(nt_amt) >= ? and abs(nt_amt) <= ? ";
    sqlCmd += " and unlock_flag in ('','N')  and tx_seq <> '' "; // 排除人工解圈M或系統解圈E

    if (commString.pos("|05|26|07|08|09", hDcurTxnCode) > 0) {
      sqlCmd += " and mtch_flag <> 'Y' and nt_amt >0 ";
    } else if (commString.pos("|25|06|27|28|29", hDcurTxnCode) > 0) {
      sqlCmd += " and mtch_flag = 'Y'  ";
    }

    sqlCmd += commSqlStr.rownum(1);

    setString(1, hDcurCardNo);
    setString(2, hDcurAuthCode);
    setDouble(3, ldLowAmt);
    setDouble(4, ldHighAmt);

    int r = selectTable();

    // 授權記錄檔比不到
    if (r <= 0) {
      return 1;
    }

    hRowid = getValue("rowid");
    hTxAuthTxSeq = getValue("tx_seq");
    hTxAuthNtAmt = getValueDouble("nt_amt");
    hTxAuthVdLockNtAmt = getValueDouble("vd_lock_nt_amt");

    if (commString.pos("|05|26|07|08|09", hDcurTxnCode) > 0) {
      updateCcaAuthTxlog(hRowid);
    }
    
    return 0;
  }

  /***********************************************************************
   *第三次授權比對 :	(1)	自助加油交易: (mcc=5542),  <授權碼相符>
   *                       (2)    請款交易與授權交易的金額不同 <授權碼相符>
   * 
   * 寫1筆批次解圈(dba_acaj), tx_seq設成空白-> 補圈/解圈扣款
   */
  int checkVdAuth3() throws Exception {
    String hRowid = "";

    //增加5541原始授權金額為0的交易比對
    sqlCmd =   " select ref_no,tx_date,tx_seq , nt_amt, vd_lock_nt_amt, hex(rowid) as rowid from cca_auth_txlog where card_no = ? and auth_no = ? ";
    sqlCmd += " and unlock_flag in ('','N')  and tx_seq <> ''  "; // 排除人工解圈M或系統解圈E
    if ("5542".equals(hDcurMchtCategory) || "5541".equals(hDcurMchtCategory)) {
    	sqlCmd += " and (vd_lock_nt_amt = 1500 or vd_lock_nt_amt = 1 ) and  ( nt_amt <= 1 or nt_amt =1500 ) ";
    }
    sqlCmd += " and mtch_flag <> 'Y' and vd_lock_nt_amt > 0 ";
    sqlCmd += commSqlStr.rownum(1);

    setString(1, hDcurCardNo);
    setString(2, hDcurAuthCode);

    int r = selectTable();

    // 授權記錄檔比不到
    if (r <= 0) {
      hDcurRskType = "7";
      hDcurRskTypeSpecial = hDcurRskType;
      return 1;
    }

    showLogMessage("I", "", "  checkVdAuth3= [" +hDcurCardNo + "],[" + hDcurAuthCode + "],["+hDcurMchtCategory+ "],["+getValueDouble("vd_lock_nt_amt") + "]");
    
    hRowid = getValue("rowid");
    hTxAuthTxSeq = "";
    hTxAuthNtAmt = 0;
    hTxAuthVdLockNtAmt = 0;

   	insertDbaAcaj();
    updateCcaAuthTxlog2(hRowid);
     
    return 0;
    
  }
  
  void updateCcaAuthTxlog(String lsRowid) throws Exception {

    daoTable = "cca_auth_txlog";
    updateSQL = "mtch_flag = 'Y' , ";
    updateSQL += "unlock_flag = 'Y' , ";
    updateSQL += "mtch_date = ? ,";
    updateSQL += "mod_user = ? , ";
    updateSQL += "mod_pgm = ? , ";
    updateSQL += "mod_time = sysdate ";
    whereStr = "where rowid = ? ";
    setString(1, hBusinessDate);
    setString(2, "system");
    setString(3, prgmId);
    setRowId(4, lsRowid);
    updateTable();
  }
  
  void updateCcaAuthTxlog2(String lsRowid) throws Exception {

	    daoTable = "cca_auth_txlog";
	    updateSQL = "mtch_flag = 'Y' , ";
	    updateSQL += "mtch_date = ? ,";
	    updateSQL += "mod_user = ? , ";
	    updateSQL += "mod_pgm = ? , ";
	    updateSQL += "mod_time = sysdate ";
	    whereStr = "where rowid = ? ";
	    setString(1, hBusinessDate);
	    setString(2, "system");
	    setString(3, prgmId);
	    setRowId(4, lsRowid);
	    updateTable();
	  }
  
  void insertDbaAcaj() throws Exception {

      setValue("da.crt_date"    , hBusinessDate);
      setValue("da.crt_time"       , sysTime);
      setValue("da.p_seqno"        , hDcurPSeqno);
      setValue("da.acct_type"         , hDcurAcctType);
      setValue("da.acct_no"   , getBankAcctNo());  
      setValue("da.adjust_type"   , "RE10"); 
      setValue("da.reference_no"   , getValue("ref_no")); 
      setValueDouble("da.orginal_amt"   , getValueDouble("vd_lock_nt_amt")); 
      setValue("da.func_code"   , "U"); 
      setValue("da.card_no"   , hDcurCardNo); 
      setValue("da.purchase_date"   , getValue("tx_date")); 
      setValue("da.proc_flag"   , "N"); 
      setValue("da.txn_code"   , ""); 
      setValue("da.tx_seq"   , getValue("tx_seq")); 
      setValue("da.mcht_no"   , hDcurMchtNo);
      setValue("da.adj_reason_code","02");	//--2022.10.09 增加寫入調整原因 : 調整原因:02 : 請款比對系統解圈
      setValue("da.apr_flag"   , "Y");
      setValue("da.apr_date"   , hBusinessDate);
      setValue("da.apr_user"   , "system");
      setValue("da.mod_user"   , "system");
      setValue("da.mod_time"   , sysDate + sysTime);
      setValue("da.mod_pgm"   , javaProgram);

      extendField = "da.";
	  daoTable = "dba_acaj ";
	  
      insertTable();

  }

  void getAuthParm() throws Exception {
    String sysId = "REPORT", sysKey = "";

    // --金額上限百分比
    sysKey = "VD_U_LIMIT";
    sqlCmd = "select sys_data1 as vd_u_limit from cca_sys_parm1 where sys_id = ? and sys_key = ? ";
    setString(1, sysId);
    setString(2, sysKey);

    int recordCnt = selectTable();

    if (recordCnt > 0) {
      vdHighRate = getValueDouble("vd_u_limit");
    } else {
      vdHighRate = 5;
    }

    // --金額下限百分比
    sysKey = "VD_L_LIMIT";
    sqlCmd = "select sys_data1 as vd_l_limit from cca_sys_parm1 where sys_id = ? and sys_key = ? ";
    setString(1, sysId);
    setString(2, sysKey);

    recordCnt = selectTable();

    if (recordCnt > 0) {
      vdLowRate = getValueDouble("vd_l_limit");
    } else {
      vdLowRate = 5;
    }

    // --天數 +
    sysKey = "VD_U_DAY";
    sqlCmd = "select sys_data1 as vd_u_day from cca_sys_parm1 where sys_id = ? and sys_key = ? ";
    setString(1, sysId);
    setString(2, sysKey);

    recordCnt = selectTable();

    if (recordCnt > 0) {
      vdHighDay = getValueDouble("vd_u_day");
    } else {
      vdHighDay = 1;
    }

    // --天數 -
    sysKey = "VD_L_DAY";
    sqlCmd = "select sys_data1 as vd_l_day from cca_sys_parm1 where sys_id = ? and sys_key = ? ";
    setString(1, sysId);
    setString(2, sysKey);

    recordCnt = selectTable();

    if (recordCnt > 0) {
      vdLowDay = getValueDouble("vd_l_day");
    } else {
      vdLowDay = 8;
    }

  }

  void checkNegatvieTxn() throws Exception {
    // 比對正向交易是否落入問交
    sqlCmd =
        " select reference_no from dbb_bill where tx_seq = ? and rsk_type in ('1','2','3','5','6','7') ";
    sqlCmd += commSqlStr.rownum(1);

    setString(1, hTxAuthTxSeq);

    int getCnt = selectTable();

    if (getCnt > 0) {
      hDcurRskType = "8";
      return;
    }

    // 比對正向交易是否已扣款成功
    sqlCmd = " select end_bal from dba_debt where tx_seq = ?  ";
    sqlCmd += commSqlStr.rownum(1);

    setString(1, hTxAuthTxSeq);

    getCnt = selectTable();

    if (getCnt > 0) {
      if (getValueDouble("end_bal") > 0) {
        hDcurRskType = "8";
      }
    } else {
      hDcurRskType = "8";
    }
  }

  /***********************************************************************/
  void checkCardStatus() throws Exception {

	sqlCmd =
        "  select block_reason1, block_reason2, block_reason3, block_reason4, block_reason5, block_date ";
    sqlCmd += "   from cca_card_acct";
    sqlCmd += "  where acno_p_seqno = ? ";
    sqlCmd += "    and debit_flag = 'Y' ";
    setString(1, hCardPSeqno);
    selectTable();

    // 消費日期大於效期迄日
    if (comcr.str2long(hDcurPurchaseDate) > comcr.str2long(hCardNewEndDate)) {
      hDcurRskType = "2";
      return;
    }

    // 卡無效 且消費日期大於停卡日
    if (hCardCurrentCode.equals("0") == false) {
      tempCount2 = 0;
      if (hDcurPurchaseDate.compareTo(hCardOppostDate) > 0) {
        hDcurRskType = "2";
        return;
      }
    }

    // 消費日期大於凍結日且小於等於效期迄日
    if (getValue("block_reason1").length() != 0 || getValue("block_reason2").length() != 0
        || getValue("block_reason3").length() != 0 || getValue("block_reason4").length() != 0
        || getValue("block_reason5").length() != 0) {
      if (getValue("block_date").length() > 0 && hDcurPurchaseDate.compareTo(getValue("block_date")) > 0
          && (comcr.str2long(hDcurPurchaseDate) <= comcr.str2long(hCardNewEndDate))) {
        hDcurRskType = "4";
        return;
      }
    }
  }
  
  String getBankAcctNo() throws Exception {

	  sqlCmd = "  select acct_no from dbc_card where card_no = ?  ";
	  
	  setString(1, hDcurCardNo);
	  
	  selectTable();
	  
	  if (notFound.equals("Y"))
	  {
		  return "";
	  }
	  
	  return getValue("acct_no");
	  
	  }
  
  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    DbbA004 proc = new DbbA004();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }
  /***********************************************************************/
}
