/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson      program initial                          *
*  109/08/07  V1.00.01    shiyuqi    updated for project coding standard      *
*  109/08/26  V1.00.02    JeffKung   remove remarked code                     *
*  112/08/18  V1.00.03    JeffKung   remark report file                       *
******************************************************************************/

package Dbb;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/* 合格化回饋處理程式 */
public class DbbA023 extends AccessDAO {
  private final String progname = "合格化回饋處理程式  112/08/18 V1.00.03";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  String hTempUser = "";
  final int DEBUG = 0;

  String stderr = "";
  String hCallBatchSeqno = "";

  String hBusinessDate = "";
  String hVouchChiDate = "";
  String hSystemDate = "";
  String hSystemTime = "";
  String hRskoReferenceNo = "";
  String hRskoReferenceNoNew = "";
  String hRskoCardNo = "";
  String hRskoRskOrgCardno = "";
  String hRskoRskType = "";
  String hRskoRskMark = "";
  String hRskoStdVouchCd = "";
  String hRskoModUser = "";
  String hRskoModTime = "";
  String hRskoRowid = "";
  String hDcurReferenceNo = "";
  String hDcurIdPSeqno = "";
  String hBusinssChiDate = "";
  String hTempType = "";
  String hVouchCdKind = "";
  String hTAcNo = "";
  int hTSeqno = 0;
  String hTDbcr = "";
  String hTMemo3Kind = "";
  String hTMemo3Flag = "";
  String hTDrFlag = "";
  String hTCrFlag = "";
  String hTempMemo3 = "";
  String hDcurRowid = "";
  String hDcurReferenceNoFeeF = "";
  String hTempx08 = "";
  String hDcurMajorCardNo = "";
  String hDcurCurrentCode = "";
  String hDcurIssueDate = "";
  String hDcurPromoteDept = "";
  String hDcurProdNo = "";
  String hDcurGroupCode = "";
  String hDcurBinType = "";
  String hDcurPSeqno = "";
  String hDcurSourceCode = "";
  String hDcurAcctType = "";
  String hDcurStmtCycle = "";
  String hDcurCardNo = "";
  double hDcurDestAmt = 0;
  String tempX10 = "";
  String hPbtbBinType = "";
  String hCardCardType = "";
  String hCardBinNo = "";
  String hCardBinType = "";
  String hCardPSeqno = "";
  String hAcnoAcctType = "";
  String hAcnoStmtCycle = "";
  String hTNextCloseDay = "";
  String hTNextAcctMonth = "";
  String hTNextPayDate = "";
  String hTThisAcctMonth = "";
  String hPrintName = "";
  String hRptName = "";
  String swPrint = "";

  int totalCount = 0;
  double totalAmt = 0;
  double tempAmt = 0;
  double hRskoRskAmt = 0;
  double hRskoRskOrgAmt = 0;
  double hRskoIssueFee = 0;
  int hRskoIssueFeeTax = 0;
  long tempLong4 = 0;
  long tempLong = 0;

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
        comc.errExit("Usage : DbbA023 batch_seq", "");
      }

      // 固定要做的

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

      swPrint = "N";

      String hSystemDateFull = "";
      sqlCmd = "select to_char(sysdate,'yyyymmddhh24miss') h_system_date_full ";
      sqlCmd += " from dual ";
      int recordCnt = selectTable();
      if (recordCnt > 0) {
        hSystemDateFull = getValue("h_system_date_full");
      }
      comcr.hGsvhModWs = "DBB_A023R0";
      comcr.hGsvhModPgm = javaProgram;
      comcr.reportId = comcr.hGsvhModWs;
      comcr.reportName = comcr.reportId;

      commonRtn();

      selectBilRskok();

      String filename =
          String.format("%s/reports/%s_%s", comc.getECSHOME(), comcr.reportName, hSystemDateFull);
      //comc.writeReport(filename, comcr.lpar);

      showLogMessage("I", "", String.format("程式執行結束,筆數=[%d]", totalCount));
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
  void commonRtn() throws Exception {
    hBusinessDate = "";

    sqlCmd = "select business_date,";
    sqlCmd += "substr(to_char(to_number(vouch_date)- 19110000,'0000000'),2,7) h_vouch_chi_date ";
    sqlCmd += " from ptr_businday  ";
    sqlCmd += "fetch first 1 rows only ";
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
      hBusinessDate = getValue("business_date");
      hVouchChiDate = getValue("h_vouch_chi_date");
    }
    sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date,";
    sqlCmd += "to_char(sysdate,'hh24miss') h_system_time ";
    sqlCmd += " from dual ";
    recordCnt = selectTable();
    if (recordCnt > 0) {
      hSystemDate = getValue("h_system_date");
      hSystemTime = getValue("h_system_time");
    }

  }

  /***********************************************************************/
  void selectBilRskok() throws Exception {

    sqlCmd = "select ";
    sqlCmd += "reference_no,";
    sqlCmd += "reference_no_new,";
    sqlCmd += "card_no,";
    sqlCmd += "rsk_org_cardno,";
    sqlCmd += "rsk_amt,";
    sqlCmd += "rsk_org_amt,";
    sqlCmd += "issue_fee,";
    sqlCmd += "issue_fee_tax,";
    sqlCmd += "rsk_type,";
    sqlCmd += "rsk_mark,";
    sqlCmd += "std_vouch_cd,";
    sqlCmd += "mod_user,";
    sqlCmd += "to_char(mod_time,'yyyymmdd') h_rsko_mod_time,";
    sqlCmd += "rowid as rowid ";
    sqlCmd += " from bil_rskok ";
    sqlCmd += "where 1=1 ";
    sqlCmd += "  and post_flag  != 'Y' ";
    sqlCmd += "  and decode(rsk_type,'','N',rsk_type) = 'D' ";
    openCursor();
    while (fetchTable()) {
      hRskoReferenceNo = getValue("reference_no");
      hRskoReferenceNoNew = getValue("reference_no_new");
      hRskoCardNo = getValue("card_no");
      hRskoRskOrgCardno = getValue("rsk_org_cardno");
      hRskoRskAmt = getValueDouble("rsk_amt");
      hRskoRskOrgAmt = getValueDouble("rsk_org_amt");
      hRskoIssueFee = getValueDouble("issue_fee");
      hRskoIssueFeeTax = getValueInt("issue_fee_tax");
      hRskoRskType = getValue("rsk_type");
      hRskoRskMark = getValue("rsk_mark");
      hRskoStdVouchCd = getValue("std_vouch_cd");
      hRskoModUser = getValue("mod_user");
      hRskoModTime = getValue("h_rsko_mod_time");
      hRskoRowid = getValue("rowid");

      showLogMessage("D", "", "888 select main=[" + hRskoReferenceNo + "]");

      totalCount++;
      
      selectDbbCurpost();
      
      /*    //會計帳先點掉
      if (hRskoStdVouchCd.length() > 0) {
    	  vouchRtn();
      }
      */
      
      showLogMessage("D", "", " 888 new ref_no=[" + hDcurReferenceNo + "]");

      daoTable = "bil_rskok";
      updateSQL = " post_flag         = 'Y',";
      updateSQL += " reference_no_new  = ?";
      whereStr = "where rowid = ? ";
      setString(1, hDcurReferenceNo);
      setRowId(2, hRskoRowid);
      updateTable();
    }
    closeCursor();
  }

  /***********************************************************************/
  void vouchRtn() throws Exception {

    String hTAcNo = "";
    int hTSeqno = 0;
    String hTMemo3Kind = "";
    String hTMemo3Flag = "";
    String hTDbcr = "";
    String hTCrFlag = "";
    String hTDrFlag = "";
    String hBusinssChiDate = "";
    String tMemo3 = "";
    String hVouchCdKind = "";
    String hTempType = "";
    int vouchCnt = 0;
    double tempAmt = 0;

    hDcurIdPSeqno = "";
    sqlCmd = "select id_p_seqno ";
    sqlCmd += " from crd_card  ";
    sqlCmd += "where card_no = ? ";
    setString(1, hRskoCardNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hDcurIdPSeqno = getValue("id_p_seqno");
    }

    sqlCmd =
        "select substr(to_char(to_number(business_date)- 19110000,'0000000'),2,7) h_businss_chi_date ";
    sqlCmd += " from ptr_businday ";
    recordCnt = selectTable();
    if (recordCnt > 0) {
      hBusinssChiDate = getValue("h_businss_chi_date");
    }

    hVouchCdKind = "";

    hVouchCdKind = hRskoStdVouchCd;

    hTempType = "";
    sqlCmd = "select b.gl_code ";
    sqlCmd += " from sec_user a, ptr_dept_code b  ";
    sqlCmd += "where a.usr_id     = ?  ";
    sqlCmd += "  and a.usr_deptno = b.dept_code ";
    setString(1, hRskoModUser);
    recordCnt = selectTable();
    /*
     * lai test if (notFound.equals("Y")) { comcr.err_rtn("select_sec_user not found!",
     * h_rsko_mod_user ,h_call_batch_seqno); }
     */

    if (recordCnt > 0) {
      hTempType = getValue("gl_code");
    }

    comcr.hVoucSysRem = hVouchCdKind;

    comcr.startVouch(hTempType, hVouchCdKind);

    swPrint = "Y";

    sqlCmd = "select ";
    sqlCmd += "gen_sys_vouch.ac_no,";
    sqlCmd += "gen_sys_vouch.dbcr_seq,";
    sqlCmd += "gen_sys_vouch.dbcr,";
    sqlCmd += "gen_acct_m.memo3_kind,";
    sqlCmd += "decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) h_t_memo3_flag,";
    sqlCmd += "decode(gen_acct_m.dr_flag   ,'','N',gen_acct_m.dr_flag) h_t_dr_flag,";
    sqlCmd += "decode(gen_acct_m.cr_flag   ,'','N',gen_acct_m.cr_flag) h_t_cr_flag ";
    sqlCmd += " from gen_sys_vouch,gen_acct_m ";
    sqlCmd += "where std_vouch_cd = ? ";
    sqlCmd += "  and gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
    sqlCmd += "order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr) ";
    setString(1, hVouchCdKind);
    recordCnt = selectTable();
    
    showLogMessage("D", "", " 888 sys cnt=[" + recordCnt + "]" + hVouchCdKind);
    
    for (int i = 0; i < recordCnt; i++) {
      hTAcNo = getValue("ac_no", i);
      hTSeqno = getValueInt("dbcr_seq", i);
      hTDbcr = getValue("dbcr", i);
      hTMemo3Kind = getValue("memo3_kind", i);
      hTMemo3Flag = getValue("h_t_memo3_flag", i);
      hTDrFlag = getValue("h_t_dr_flag", i);
      hTCrFlag = getValue("h_t_cr_flag", i);

      showLogMessage("D", "", " 888 ac_no=[" + hTAcNo + "]" + hTSeqno);

      totalAmt = hRskoRskAmt;
      tempAmt = hRskoRskAmt - hRskoRskOrgAmt + (hRskoIssueFee + hRskoIssueFeeTax);
      tMemo3 = String.format("%3.3s年%2.2s月%2.2s日", hBusinssChiDate, hBusinssChiDate.substring(3),
          hBusinssChiDate.substring(5));

      comcr.hGsvhMemo1 = "";
      comcr.hGsvhMemo2 = tMemo3;
      comcr.hGsvhMemo3 = "";

      if (hVouchCdKind.equals("R042")) {
        if (hTSeqno == 1) {
          totalAmt = hRskoRskAmt;
        }
        /* 借方 */
        if (hTSeqno == 2) {
          if (hRskoRskOrgAmt == 0)
            totalAmt = 0;
          else {
            if (tempAmt < 0)
              totalAmt = tempAmt * (-1);
            else
              totalAmt = 0;
          }
        }
        /* 貸方 */
        if (hTSeqno == 3) {
          if (hRskoRskOrgAmt > 0)
            totalAmt = hRskoRskOrgAmt;
          else
            totalAmt = hRskoRskAmt;
        }
        /* 貸方 */
        if (hTSeqno == 4) {
          if (hRskoRskOrgAmt == 0)
            totalAmt = 0;
          else {
            if (tempAmt > 0)
              totalAmt = tempAmt;
            else
              totalAmt = 0;
          }
          tempLong4 = (long) totalAmt;
          tempLong = (long) (totalAmt / 1.05 + 0.5);
          totalAmt = tempLong;
        }
        /* 貸方 */
        if (hTSeqno == 5) {
          totalAmt = tempLong4 - tempLong;
        }
        /* 借方 */
        if (hTSeqno == 6) {
          comcr.hGsvhMemo1 = hRskoCardNo;
          totalAmt = hRskoIssueFee;
        }
        /* 借方 */
        if (hTSeqno == 7) {
          comcr.hGsvhMemo1 = hRskoCardNo;
          totalAmt = hRskoIssueFeeTax;
        }
      } else if (hVouchCdKind.equals("R-D7")) {
        /* 借方 */
        if (hTSeqno == 1) {
          comcr.hGsvhMemo1 = "特店退款";
          comcr.hGsvhMemo2 = hRskoCardNo;
          totalAmt = hRskoRskAmt - (hRskoIssueFee + hRskoIssueFeeTax);
        }
        /* 借方 */
        if (hTSeqno == 2) {
          totalAmt = hRskoRskAmt;
        }
        /* 借方 */
        if (hTSeqno == 3) {
          comcr.hGsvhMemo1 = hRskoCardNo;
          totalAmt = hRskoIssueFee;
        }
        /* 借方 */
        if (hTSeqno == 4) {
          comcr.hGsvhMemo1 = hRskoCardNo;
          totalAmt = hRskoIssueFeeTax;
        }
      } else if (hVouchCdKind.equals("R-D6")) {
        /* 借方 */
        if (hTSeqno == 1) {
          comcr.hGsvhMemo2 = hRskoCardNo;
        }
        totalAmt = hRskoRskAmt;
      } else {
        totalAmt = hRskoRskAmt;
      }

      if (hTMemo3Flag.equals("Y")) {
        if (((hTDbcr.equals("D")) && (hTCrFlag.equals("Y")))
            || ((hTDbcr.equals("C")) && (hTDrFlag.equals("Y")))) {
          vouchCnt++;
        }
        comcr.hGsvhMemo3 = "";
        if (hTMemo3Kind.substring(0, 1).equals("1")) {
          comcr.hGsvhMemo3 = hRskoCardNo;
        } else {
          if (hTMemo3Kind.substring(0, 1).equals("2")) {
            tMemo3 = String.format("%12.12s%12.12s", "ID_P_SEQNO :", hDcurIdPSeqno);
            comcr.hVoucIdNo = comcr.ufIdnoId(hDcurIdPSeqno);
            comcr.hGsvhMemo3 = tMemo3;
          } else {
            if (hTMemo3Kind.substring(0, 1).equals("3")) {
              tMemo3 = String.format("%6.6s%6.6s%02d", hBusinssChiDate.substring(1),
                  comcr.hVoucRefno, vouchCnt);
              comcr.hGsvhMemo3 = tMemo3;
            }
          }
        }
      }

      /* 銷帳 */
      hTempMemo3 = "";
      sqlCmd = "select memo3 ";
      sqlCmd += " from gen_memo3  ";
      sqlCmd += "where ref_no = ?  ";
      sqlCmd += "and amt  = ? ";
      sqlCmd += "and ac_no  = ? ";
      setString(1, hRskoReferenceNo);
      setDouble(2, hRskoRskAmt);
      setString(3, hTAcNo);
      int recCnt = selectTable();
      if (recCnt > 0) {
        hTempMemo3 = getValue("memo3");
      }

      if (hTempMemo3.length() > 0) {
        comcr.hGsvhMemo3 = hTempMemo3;
      }

      comcr.hGsvhModUser = hRskoReferenceNo;

      if (totalAmt != 0) {
        if (comcr.detailVouch(hTAcNo, hTSeqno, totalAmt, "") != 0) // current_code
                                                                   // not
                                                                   // found
          comcr.errRtn("Error: vouch=" + hTSeqno, hTAcNo, hCallBatchSeqno);
      }

    }

    /*搬到外面執行
    if (!hVouchCdKind.equals("R-D7"))
      selectDbbCurpost();
    */

  }

  /***********************************************************************/
  void selectDbbCurpost() throws Exception {

    hDcurRowid = "";
    hDcurReferenceNoFeeF = "";
    sqlCmd = "select rowid as rowid,";
    sqlCmd += "reference_no_fee_f ";
    sqlCmd += " from dbb_curpost  ";
    sqlCmd += "where reference_no = ? ";
    setString(1, hRskoReferenceNo);
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_dbb_curpost not found!", hRskoReferenceNo, hCallBatchSeqno);
    }
    if (recordCnt > 0) {
      hDcurRowid = getValue("rowid");
      hDcurReferenceNoFeeF = getValue("reference_no_fee_f");
    }

    hTempx08 = "";
    sqlCmd = "select substr(to_char(bil_postseq.nextval,'0000000000'),4,8) h_temp_x08 ";
    sqlCmd += " from dual ";
    recordCnt = selectTable();
    if (recordCnt > 0) {
      hTempx08 = getValue("h_temp_x08");
    }
    tempX10 = String.format("%2.2s%s", hBusinessDate.substring(2), hTempx08);
    hDcurReferenceNo = tempX10;
    hDcurDestAmt = hRskoRskAmt;
    hDcurCardNo = hRskoCardNo;

    if (!hRskoCardNo.equals(hRskoRskOrgCardno)) {
      chkCrdCard();
      daoTable = "dbb_curpost";
      updateSQL = " major_card_no= ?  ,";
      updateSQL += " valid_flag   = 'N' ,";
      updateSQL += " curr_code    = ?   ,";
      updateSQL += " issue_date   = ?   ,";
      updateSQL += " promote_dept = ?   ,";
      updateSQL += " prod_no      = ?   ,";
      updateSQL += " group_code   = ?   ,";
      updateSQL += " bin_type     = ?   ,";
      updateSQL += " p_seqno      = ?   ,";
      updateSQL += " id_p_seqno   = ?   ,";
      updateSQL += " source_code  = ?   ,";
      updateSQL += " acct_type    = ?   ,";
      updateSQL += " stmt_cycle   = ?   ,";
      updateSQL += " rsk_type           = '',";
      updateSQL += " duplicated_flag    = '',";
      updateSQL += " doubt_type         = '',";
      updateSQL += " err_chk_ok_flag    = 'N',";
      updateSQL += " double_chk_ok_flag = 'N',";
      updateSQL += " format_chk_ok_flag = 'N',";
      updateSQL += " curr_post_flag     = 'N',";
      updateSQL += " tx_convt_flag      = 'R' ";
      whereStr = "where rowid         = ? ";
      setString(1, hDcurMajorCardNo);
      setString(2, hDcurCurrentCode);
      setString(3, hDcurIssueDate);
      setString(4, hDcurPromoteDept);
      setString(5, hDcurProdNo);
      setString(6, hDcurGroupCode);
      setString(7, hDcurBinType);
      setString(8, hDcurPSeqno);
      setString(9, hDcurIdPSeqno);
      setString(10, hDcurSourceCode);
      setString(11, hDcurAcctType);
      setString(12, hDcurStmtCycle);
      setRowId(13, hDcurRowid);
      updateTable();
    }

    daoTable = "dbb_curpost";
    updateSQL = " rsk_type           = '',";
    updateSQL += " duplicated_flag    = '',";
    updateSQL += " doubt_type         = '',";
    updateSQL += " format_chk_ok_flag = 'N',";
    updateSQL += " double_chk_ok_flag = 'N',";
    updateSQL += " err_chk_ok_flag    = 'N',";
    updateSQL += " curr_post_flag     = 'N',";
    updateSQL += " card_no            = ?,";
    updateSQL += " dest_amt           = ?,";
    updateSQL += " tx_convt_flag      = 'R' ,";
    updateSQL += " reference_no_original = ?,";
    updateSQL += " reference_no          = ? ";
    whereStr = "where rowid              = ? ";
    setString(1, hDcurCardNo);
    setDouble(2, hDcurDestAmt);
    setString(3, hRskoReferenceNo);
    setString(4, hDcurReferenceNo);
    setRowId(5, hDcurRowid);
    updateTable();

    if (hDcurReferenceNoFeeF.length() > 0) {
      if (!hRskoCardNo.equals(hRskoRskOrgCardno)) {
        chkCrdCard();
        daoTable = "dbb_curpost";
        updateSQL = " major_card_no = ?  ,";
        updateSQL += " valid_flag    = 'N' ,";
        updateSQL += " curr_code     = ?   ,";
        updateSQL += " issue_date    = ?   ,";
        updateSQL += " promote_dept  = ?   ,";
        updateSQL += " prod_no       = ?   ,";
        updateSQL += " group_code    = ?   ,";
        updateSQL += " bin_type      = ?   ,";
        updateSQL += " p_seqno       = ?   ,";
        updateSQL += " id_p_seqno    = ?   ,";
        updateSQL += " source_code   = ?   ,";
        updateSQL += " acct_type     = ?   ,";
        updateSQL += " stmt_cycle    = ?   ,";
        updateSQL += " rsk_type           = '',";
        updateSQL += " duplicated_flag    = '',";
        updateSQL += " doubt_type         = '',";
        updateSQL += " err_chk_ok_flag    = 'N',";
        updateSQL += " double_chk_ok_flag = 'N',";
        updateSQL += " format_chk_ok_flag = 'N',";
        updateSQL += " curr_post_flag     = 'N',";
        updateSQL += " tx_convt_flag      = 'R' ";
        whereStr = "where reference_no  = ? ";
        setString(1, hDcurMajorCardNo);
        setString(2, hDcurCurrentCode);
        setString(3, hDcurIssueDate);
        setString(4, hDcurPromoteDept);
        setString(5, hDcurProdNo);
        setString(6, hDcurGroupCode);
        setString(7, hDcurBinType);
        setString(8, hDcurPSeqno);
        setString(9, hDcurIdPSeqno);
        setString(10, hDcurSourceCode);
        setString(11, hDcurAcctType);
        setString(12, hDcurStmtCycle);
        setString(13, hDcurReferenceNoFeeF);
        updateTable();
      }

      hTempx08 = "";
      sqlCmd = "select substr(to_char(bil_postseq.nextval,'0000000000'),4,8) h_temp_x08 ";
      sqlCmd += " from dual ";
      recordCnt = selectTable();
      if (recordCnt > 0) {
        hTempx08 = getValue("h_temp_x08");
      }
      tempX10 = String.format("%2.2s%s", hBusinessDate + 2, hTempx08);
      daoTable = "dbb_curpost";
      updateSQL = "rsk_type    = '',";
      updateSQL += " duplicated_flag  = '',";
      updateSQL += " doubt_type   = '',";
      updateSQL += " format_chk_ok_flag = 'N',";
      updateSQL += " double_chk_ok_flag = 'N',";
      updateSQL += " err_chk_ok_flag  = 'N',";
      updateSQL += " curr_post_flag  = 'N',";
      updateSQL += " card_no   = ?,";
      updateSQL += " tx_convt_flag= 'R' ,";
      updateSQL += " reference_no_original = ?,";
      updateSQL += " reference_no   = ? ";
      whereStr = "where reference_no   = ? ";
      setString(1, hDcurCardNo);
      setString(2, hRskoReferenceNo);
      setString(3, tempX10);
      setString(4, hDcurReferenceNoFeeF);
      updateTable();

      daoTable = "dbb_curpost";
      updateSQL = "reference_no_fee_f = ?";
      whereStr = "where reference_no   = ? ";
      setString(1, tempX10);
      setString(2, hDcurReferenceNo);
      updateTable();
      if (notFound.equals("Y")) {
        String stderr = "update_dbb_curpost not found!";
        comcr.errRtn(stderr, "", hCallBatchSeqno);
      }
    }

  }

  /**********************************************************************/

  void initCrdCard() {
    hDcurMajorCardNo = "";
    hDcurCurrentCode = "";
    hDcurIssueDate = "";
    hDcurPromoteDept = "";
    hDcurProdNo = "";
    hDcurGroupCode = "";
    hDcurSourceCode = "";
    hCardCardType = "";
    hCardBinType = "";
    hCardBinNo = "";
    hCardPSeqno = "";
    hDcurIdPSeqno = "";
  }

  /**********************************************************************/
  void chkCrdCard() throws Exception {

    sqlCmd = "select major_card_no,";
    sqlCmd += "current_code,";
    sqlCmd += "issue_date,";
    sqlCmd += "oppost_date,";
    sqlCmd += "promote_dept,";
    sqlCmd += "prod_no,";
    sqlCmd += "group_code,";
    sqlCmd += "source_code,";
    sqlCmd += "card_type,";
    sqlCmd += "bin_no,";
    sqlCmd += "bin_type,";
    sqlCmd += "p_seqno,";
    sqlCmd += "id_p_seqno ";
    sqlCmd += " from dbc_card  ";
    sqlCmd += "where card_no  = ? ";
    setString(1, hDcurCardNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hDcurMajorCardNo = getValue("major_card_no");
      hDcurCurrentCode = getValue("current_code");
      hDcurIssueDate = getValue("issue_date");
      hDcurPromoteDept = getValue("promote_dept");
      hDcurProdNo = getValue("prod_no");
      hDcurGroupCode = getValue("group_code");
      hDcurSourceCode = getValue("source_code");
      hCardCardType = getValue("card_type");
      hCardBinNo = getValue("bin_no");
      hCardBinType = getValue("bin_type");
      hCardPSeqno = getValue("p_seqno");
      hDcurIdPSeqno = getValue("id_p_seqno");
    }

    hPbtbBinType = hCardBinType;
    hDcurBinType = hCardBinType;

    hAcnoAcctType = "";
    hAcnoStmtCycle = "";

    sqlCmd = "select acct_type,";
    sqlCmd += "stmt_cycle ";
    sqlCmd += " from dba_acno  ";
    sqlCmd += "where p_seqno  = ? ";
    setString(1, hCardPSeqno);
    recordCnt = selectTable();
    if (recordCnt > 0) {
      hAcnoAcctType = getValue("acct_type");
      hAcnoStmtCycle = getValue("stmt_cycle");
    }

    hDcurPSeqno = hCardPSeqno;
    hDcurAcctType = hAcnoAcctType;
    hDcurStmtCycle = hAcnoStmtCycle;

    hTNextPayDate = "";
    hTNextAcctMonth = "";
    hTThisAcctMonth = "";
    sqlCmd = "select next_close_date,";
    sqlCmd += "next_acct_month,";
    sqlCmd += "next_lastpay_date,";
    sqlCmd += "this_acct_month ";
    sqlCmd += " from ptr_workday  ";
    sqlCmd += "where stmt_cycle = ? ";
    setString(1, hDcurStmtCycle);
    recordCnt = selectTable();
    if (recordCnt > 0) {
      hTNextCloseDay = getValue("next_close_date");
      hTNextAcctMonth = getValue("next_acct_month");
      hTNextPayDate = getValue("next_lastpay_date");
      hTThisAcctMonth = getValue("this_acct_month");
    }

  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    DbbA023 proc = new DbbA023();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }

}
