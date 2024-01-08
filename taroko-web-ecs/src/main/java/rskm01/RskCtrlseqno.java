package rskm01;
/**
 * 爭議款狀態
 * 2020-0430   JH    PRBL.clo_result
 * 2019-0907   JH    initial
 */
public class RskCtrlseqno extends busi.FuncBase {
public boolean ibDebit = false;
public String debitFlag = "";
public String referenceNo = "";
public String referenceNoOri = "";
public String ctrlSeqno = "";
public String prblMark = "";
public String prblSrcCode = "";
public String prblStatus = "";
public String prblCloResult = "";
public String chgbStage1 = "";
public String chgbStage2 = "";
public String chgbClose = "";
public String reptStatus = "";
public String arbitStatus = "";
public String complStatus = "";

public String getCtrlSeqno(String aRefNo) {
   ctrlSeqno = "";
   ibDebit = false;
   if (notEmpty(aRefNo)) {
      strSql = "select 'N' as debitFlag, rsk_ctrl_seqno from bil_bill" +
            " where rsk_ctrl_seqno<>'' and (reference_no=? or reference_no_original=?)" +
            " union select 'Y' as debitFlag, rsk_ctrl_seqno from dbb_bill" +
            " where rsk_ctrl_seqno<>'' and (reference_no=?)";  // or reference_no_original=?)";
      setString(aRefNo);
      setString(aRefNo);
      setString(aRefNo);
//      setString(a_ref_no);
      sqlSelect(strSql);
      if (sqlRowNum > 0) {
         ctrlSeqno = colStr("rsk_ctrl_seqno");
         if (notEmpty(ctrlSeqno))
            return ctrlSeqno;
      }
   }

   strSql = "select " + commSqlStr.rskCtrlseqno + " as ctrl_seqno" + " from " + commSqlStr.sqlDual;
   sqlSelect(strSql);
   if (sqlRowNum > 0) {
      ctrlSeqno = colStr("ctrl_seqno");
      ibDebit = colEq("debitFlag", "Y");
      return ctrlSeqno;
   }

   errmsg("無法取得: 控制流水號");
   return "";
}

public boolean checkCtrlSeqNo(String aCtrlSeqNo) {
   ibDebit = false;
   debitFlag = "";
   referenceNo = "";
   referenceNoOri = "";
   if (empty(aCtrlSeqNo)) return false;

   strSql = "select reference_no, 'N' as debit_flag, reference_no_original" +
         " from bil_bill where rsk_ctrl_seqno =?" +
         " union select reference_no, 'Y' as debit_flag, reference_no_original" +
         " from dbb_bill where rsk_ctrl_seqno =?";
   sqlSelect(strSql, new Object[]{aCtrlSeqNo, aCtrlSeqNo});
   if (sqlRowNum > 0) {
      debitFlag = colStr("debitFlag");
      referenceNo = colStr("reference_no");
      referenceNoOri = colStr("reference_no_original");
      ibDebit = colEq("debit_flag", "Y");
      return true;
   }
   return false;
}

public void selectXxxBill(String aRefno, String aDebit) {

   dataInit();
   if (empty(aRefno)) return;

   if (eq(aDebit, "Y")) {
      strSql = "select * from Vrsk_ctrlseqno_dbb where reference_no =?";
   }
   else {
      strSql = "select * from Vrsk_ctrlseqno_bil where reference_no =?";
   }
   sqlSelect(strSql, new Object[]{aRefno});
   if (sqlRowNum > 0) {
      prblMark = colStr("prbl_mark");
      prblSrcCode = colStr("prbl_src_code");
      prblStatus = colStr("prbl_status");
      prblCloResult =colStr("prbl_clo_result");
      chgbStage1 = colStr("chgb_stage1");
      chgbStage2 = colStr("chgb_stage2");
      chgbClose = colStr("chgb_close");
      reptStatus = colStr("rept_status");
      complStatus = colStr("compl_mark");
      arbitStatus = colStr("arbit_mark");
   }
}

public void selectProblem(String aRefno) throws Exception  {
   prblSrcCode = "";
   prblStatus = "";
   prblMark = "";

   if (empty(aRefno))
      return;

   strSql = "select prb_mark, prb_src_code, prb_status" +
         " from rsk_problem" +
         " where reference_no =?" +
         " order by reference_seq" + commSqlStr.rownum(1);
   sqlSelect(strSql, new Object[]{aRefno});
   if (sqlRowNum > 0) {
      prblMark = colStr("prb_mark");
      prblSrcCode = colStr("prb_src_code");
      prblStatus = colStr("prb_starus");
   }
}

public void selectChgback(String aRefno) throws Exception  {
   chgbStage1 = "";
   chgbStage2 = "";
   chgbClose = "";

   if (empty(aRefno))
      return;

   strSql = "select chg_stage, sub_stage, final_close" +
         " from rsk_chgback" +
         " where reference_no =?" +
         " order by reference_seq " + commSqlStr.rownum(1);
   sqlSelect(strSql, new Object[]{aRefno});
   if (sqlRowNum > 0) {
      chgbStage1 = colStr("chg_stage");
      chgbStage2 = colStr("sub_stage");
      chgbClose = colStr("final_close");
   }
}

public void selectReceipt(String aRefno) throws Exception  {
   reptStatus = "";

   if (empty(aRefno))
      return;

   strSql = "select REPT_STATUS" +
         " from rsk_receipt" +
         " where reference_no =?" +
         " order by reference_seq " + commSqlStr.rownum(1);
   sqlSelect(strSql, new Object[]{aRefno});
   if (sqlRowNum > 0) {
	   reptStatus = colStr("rept_status");
   }
}

public void selectPrecompl(String aRefno) throws Exception  {
   complStatus = "";

   if (empty(aRefno))
      return;

   strSql = "select compl_times||decode(compl_times,2,com_status,pre_status) as com_status" +
         " from rsk_precompl" +
         " where reference_no =?" +
         " order by reference_seq " + commSqlStr.rownum(1);
   sqlSelect(strSql, new Object[]{aRefno});
   if (sqlRowNum > 0) {
	   complStatus = colStr("com_status");
   }
}

public void selectPrearbit(String aRefno) throws Exception  {
   arbitStatus = "";

   if (empty(aRefno))
      return;

   strSql = "select arbit_times||decode(arbit_times,2,arb_status,pre_status) as arb_status" +
         " from rsk_prearbit" +
         " where reference_no =?" +
         " order by reference_seq " + commSqlStr.rownum(1);
   sqlSelect(strSql, new Object[]{aRefno});
   if (sqlRowNum > 0) {
	   arbitStatus = colStr("arb_status");
   }
}

public void dataInit() {
   ctrlSeqno = "";
   prblMark = "";
   prblSrcCode = "";
   prblStatus = "";
   prblCloResult ="";
   chgbStage1 = "";
   chgbStage2 = "";
   chgbClose = "";
   reptStatus = "";
   arbitStatus = "";
   complStatus = "";
}

}
