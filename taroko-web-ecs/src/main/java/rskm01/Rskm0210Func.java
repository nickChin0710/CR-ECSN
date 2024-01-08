package rskm01;

/**
 * 扣款作業維護
 * 2020-0319	JH		二扣JCB理由碼=4
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * V.2018-0511-JH
 */

public class Rskm0210Func extends busi.FuncAction {
busi.CommCurr zzCurr = new busi.CommCurr();
busi.CommBusi zzcomm = new busi.CommBusi();

String kk1; //, kk2;
String isRefnoOri = "", isRefno;
boolean ibDebit = false;
boolean ibOverseaFee = false;

@Override
public int querySelect() {
   // TODO Auto-generated method stub
   return 0;
}

public int dataSelect() throws Exception  {
   isRefno = varsStr("reference_no");
   int liRefSeq = varsInt("reference_seq");

   strSql = "select rsk_chgback.*, "
         + " uf_nvl(curr_code,'901') as curr_code,"
         + " uf_dc_amt(curr_code,fst_twd_amt,fst_dc_amt) as fst_dc_amt,"
         + " uf_dc_amt(curr_code,fst_disb_amt,fst_disb_dc_amt) as fst_disb_dc_amt,"
         + " uf_dc_amt(curr_code,sec_twd_amt,sec_dc_amt) as sec_dc_amt,"
         + " uf_dc_amt(curr_code,sec_disb_amt,sec_disb_dc_amt) as sec_disb_dc_amt,"
         + " uf_idno_name2(id_p_seqno,debit_flag) as idno_name,"
         + " uf_idno_id2(major_id_p_seqno,debit_flag) as major_idno,"
         + " hex(rowid) as rowid"
         + " from rsk_chgback"
         + " where 1=1"
         + " and reference_no =?"
         + " order by reference_seq "
         + commSqlStr.rownum(1)
   ;
   setString(1, isRefno);
   this.sqlSelect(strSql);
   if (sqlRowNum <= 0)
      return 0;

   this.colDataToWpCol("");
   if (colStr("chg_stage").compareTo("3") >= 0 && colEmpty("sec_status") == false) {
      //-一扣資料-
      wp.colSet("fst_status", colStr("sec_status"));
      wp.colSet("fst_reverse_mark ", colStr("sec_reverse_mark "));
      wp.colSet("fst_reverse_date ", colStr("sec_reverse_date "));
      wp.colSet("fst_rebuild_mark ", colStr("sec_rebuild_mark "));
      wp.colSet("fst_rebuild_date ", colStr("sec_rebuild_date "));
      wp.colSet("fst_send_date    ", colStr("sec_send_date    "));
      wp.colSet("fst_send_cnt     ", colStr("sec_send_cnt     "));
      wp.colSet("fst_usage_code   ", colStr("sec_usage_code   "));
      wp.colSet("fst_reason_code  ", colStr("sec_reason_code  "));
      wp.colSet("fst_msg          ", colStr("sec_msg          "));
      wp.colSet("fst_doc_mark     ", colStr("sec_doc_mark     "));
      wp.colSet("fst_amount       ", colStr("sec_amount       "));
      wp.colSet("fst_twd_amt      ", colStr("sec_twd_amt      "));
      wp.colSet("fst_dc_amt       ", colStr("sec_dc_amt       "));
      wp.colSet("fst_part_mark    ", colStr("sec_part_mark    "));
      wp.colSet("fst_expire_date  ", colStr("sec_expire_date  "));
      wp.colSet("fst_add_date     ", colStr("sec_add_date     "));
      wp.colSet("fst_add_user     ", colStr("sec_add_user     "));
      wp.colSet("fst_apr_date     ", colStr("sec_apr_date     "));
      wp.colSet("fst_apr_user     ", colStr("sec_apr_user     "));
      wp.colSet("fst_disb_yn      ", colStr("sec_disb_yn      "));
      wp.colSet("fst_disb_amt     ", colStr("sec_disb_amt     "));
      wp.colSet("fst_disb_dc_amt  ", colStr("sec_disb_dc_amt  "));
      wp.colSet("fst_disb_add_date", colStr("sec_disb_add_date"));
      wp.colSet("fst_disb_add_user", colStr("sec_disb_add_user"));
      wp.colSet("fst_disb_apr_date", colStr("sec_disb_apr_date"));
      wp.colSet("fst_disb_apr_user", colStr("sec_disb_apr_user"));
      //-二扣資料-
      wp.colSet("sec_status", colStr("fst_status"));
      wp.colSet("sec_reverse_mark ", colStr("fst_reverse_mark "));
      wp.colSet("sec_reverse_date ", colStr("fst_reverse_date "));
      wp.colSet("sec_rebuild_mark ", colStr("fst_rebuild_mark "));
      wp.colSet("sec_rebuild_date ", colStr("fst_rebuild_date "));
      wp.colSet("sec_send_date    ", colStr("fst_send_date    "));
      wp.colSet("sec_send_cnt     ", colStr("fst_send_cnt     "));
      wp.colSet("sec_usage_code   ", colStr("fst_usage_code   "));
      wp.colSet("sec_reason_code  ", colStr("fst_reason_code  "));
      wp.colSet("sec_msg          ", colStr("fst_msg          "));
      wp.colSet("sec_doc_mark     ", colStr("fst_doc_mark     "));
      wp.colSet("sec_amount       ", colStr("fst_amount       "));
      wp.colSet("sec_twd_amt      ", colStr("fst_twd_amt      "));
      wp.colSet("sec_dc_amt       ", colStr("fst_dc_amt       "));
      wp.colSet("sec_part_mark    ", colStr("fst_part_mark    "));
      wp.colSet("sec_expire_date  ", colStr("fst_expire_date  "));
      wp.colSet("sec_add_date     ", colStr("fst_add_date     "));
      wp.colSet("sec_add_user     ", colStr("fst_add_user     "));
      wp.colSet("sec_apr_date     ", colStr("fst_apr_date     "));
      wp.colSet("sec_apr_user     ", colStr("fst_apr_user     "));
      wp.colSet("sec_disb_yn      ", colStr("fst_disb_yn      "));
      wp.colSet("sec_disb_amt     ", colStr("fst_disb_amt     "));
      wp.colSet("sec_disb_dc_amt  ", colStr("fst_disb_dc_amt  "));
      wp.colSet("sec_disb_add_date", colStr("fst_disb_add_date"));
      wp.colSet("sec_disb_add_user", colStr("fst_disb_add_user"));
      wp.colSet("sec_disb_apr_date", colStr("fst_disb_apr_date"));
      wp.colSet("sec_disb_apr_user", colStr("fst_disb_apr_user"));
      wp.colSet("rowid", colStr("rowid"));
   }

   return 1;
}

void selectRskChgback() {
   strSql = "select hex(rowid) as rowid"
         + ", A.*"
         + " from rsk_chgback A"
         + " where ctrl_seqno =?"
         + " and reference_no =?"
         + " and mod_seqno =?";
   setString(1, kk1);
   setString(2, isRefno);
   setInt(3, (int) wp.itemNum("mod_seqno"));
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("select rsk_chgback error; ctrl_seqno[%s],reference_no[%s]", kk1, isRefno);
      return;
   }
}

void selectBilBill() {
   daoTid = "bill-";
   if (ibDebit) {
      strSql = "select rsk_ctrl_seqno "
            + " from dbb_bill B"
            + " where B.reference_no =?";
   }
   else {
      strSql = "select rsk_ctrl_seqno, contract_no "
            + " from bil_bill"
            + " where reference_no =?";
   }

   sqlSelect(strSql, new Object[]{isRefno});
   if (sqlRowNum <= 0) {
      errmsg("select BIL[DBB]_BILL no-find, refno=" + isRefno);
   }
}

@Override
public void dataCheck() {
   ibDebit = eqAny(wp.itemStr("debit_flag"), "Y");

   if (wp.itemEmpty("reference_no")) {
      errmsg("帳單參考序號: 不可空白");
      return;
   }

   isRefno = wp.itemStr("reference_no");
   isRefnoOri = wp.itemStr("reference_no_ori");
   kk1 = wp.itemStr("ctrl_seqno");
//	kk2 =wp.itemStr("bin_type");

   if (isDelete()) {
      checkDelete();
      return;
   }

   double lmAmt = 0;
   //-扣款金額.TW-
   lmAmt = zzCurr.dc2twAmt(wp.itemStr("BL_curr_code"),wp.itemNum("BL_dest_amt"), wp.itemNum("BL_dc_dest_amt"), wp.itemNum("fst_dc_amt"));
   wp.itemSet("fst_twd_amt", "" + lmAmt);
   lmAmt = zzCurr.dc2twAmt(wp.itemStr("BL_curr_code"),wp.itemNum("BL_dest_amt") , wp.itemNum("BL_dc_dest_amt"), wp.itemNum("sec_dc_amt"));
   wp.itemSet("sec_twd_amt", "" + lmAmt);
   //-扣款金額.USD-
   lmAmt = zzCurr.tw2usAmt(wp.itemNum("dest_amt")
         , wp.itemNum("BL_source_amt"), wp.itemNum("fst_twd_amt"));
   wp.itemSet("fst_amount", "" + lmAmt);
   lmAmt = zzCurr.tw2usAmt(wp.itemNum("dest_amt")
         , wp.itemNum("BL_source_amt"), wp.itemNum("sec_twd_amt"));
   wp.itemSet("sec_amount", "" + lmAmt);

   if (wp.itemNum("fst_dc_amt") <= 0) {
      errmsg("扣款金額(結算): 不可<=0");
      return;
   }
   if (wp.itemEmpty("BL_payment_type") == false) {
      if (wp.itemNum("BL_dc_dest_amt") == 0) {
         errmsg("消費金額 不正確, 無法執行此作業");
         return;
      }
      if (wp.itemEq("fst_park_mark", "Y") &&
            wp.itemNum("BL_dc_dest_amt") != wp.itemNum("dc_dest_amt")) {
         errmsg("分期付款/紅利扣抵 不可部份扣款");
         return;
      }
      if (zzCurr.isTw(wp.itemStr("BL_curr_code")) == false &&
            wp.itemNum("fst_dc_amt") != wp.itemNum("BL_dc_dest_amt")) {
         errmsg("結算分期付款/紅利扣抵 不可部份扣款");
         return;
      }
   }

   if (isAdd())
      checkInsert();
   else if (isUpdate())
      checkUpdate();
   else errmsg("Action 不是A,U,D");
   return;
}

void checkInsert() {
   selectBilBill();
   if (rc != 1)
      return;

   checkFirst();
}

void checkUpdate() {
   if (empty(kk1) || empty(isRefno)) {
      errmsg("控制流水號, 帳單參考號: 不可空白");
      return;
   }

   selectRskChgback();
   if (rc != 1)
      return;

   if (wp.itemEq("chgb_action", "1")) {
      if (colEq("chg_stage", "1") == false) {
         errmsg("[扣款主階段] 不是一扣, 不可執一扣維護");
         return;
      }
      checkFirst();
   }
   else if (wp.itemEq("chgb_action", "3")) {
      if (pos("|2|3", colStr("chg_stage")) <= 0) {
         errmsg("[扣款主階段] 不是再提示,二扣, 不可執二扣維護");
         return;
      }
      wp.itemSet("chg_stage", "3");
      checkSecond();
   }
   else if (wp.itemEq("chgb_action", "2")) {
      if (colEq("chg_stage", "2") == false) {
         errmsg("[扣款主階段] 不是再提示, 不可執再提示維護");
         return;
      }
      if (colEq("rep_status", "30")) {
         errmsg("再提示: 已覆核, 無法修改!");
         return;
      }
      //wp.item_set("chg_stage","2");
   }

}

void checkFirst() {
   //-第一次扣款----------------------
   if (colEq("fst_status", "30")) {
      errmsg("[一扣]已覆核, 無法修改!");
      return;
   }
   String lsCurr = wp.itemStr("BL_curr_code");
   ibOverseaFee = isOverseaFee();

   //--消費金額-
   //--JH-R98013:國外交易手續費另列--
   double lmDestAmt = 0;
   if (zzCurr.isTw(lsCurr)) {
      lmDestAmt = wp.itemNum("BL_dest_amt");
      if (ibOverseaFee == false) {
         lmDestAmt = lmDestAmt - wp.itemNum("BL_db_mccr")
               - wp.itemNum("BL_db_iccr") - wp.itemNum("BL_issue_fee");
      }
   }
   else {
      lmDestAmt = wp.itemNum("BL_dc_dest_amt");
      if (ibOverseaFee == false) {
         lmDestAmt = lmDestAmt - wp.itemNum("BL_db_mccr")
               - wp.itemNum("BL_db_iccr") - wp.itemNum("BL_issue_fee");
      }
   }
   if (wp.itemEq("fst_part_mark", "Y")) {
//		if (dc_curr.is_tw(lsCurr)) {
//			if (lmDestAmt == wp.item_num("fst_dc_amt")) {
//				errmsg("部份註記, 扣款金額不合理?!");
//				return;
//			}
//		}
//		else 
      if (lmDestAmt == wp.itemNum("fst_dc_amt")) {
         errmsg("部份註記, 扣款金額不合理?!");
         return;
      }
   }
   else {
//		if (dc_curr.is_tw(lsCurr)) {
//			if (lmDestAmt != wp.item_num("fst_twd_amt")) {
//				errmsg("無部份註記, 扣款金額不合理?!");
//				return;
//			}
//		}
//		else 
      if (lmDestAmt != wp.itemNum("fst_dc_amt")) {
         errmsg("無部份註記, 扣款金額不合理?!");
         return;
      }
   }

   if (wp.itemEmpty("fst_msg")) {
      errmsg("請輸入訊息?!");
      return;
   }
   //--check 扣款理由碼----------------------------------
   String lsBillType = wp.itemStr("BL_bill_type");
   String lsReasonCode = wp.itemStr("fst_reason_code");
   if (eqAny(lsBillType, zzcomm.TSCC_bill_type)) {
      if (lsReasonCode.length() != 4) {
         errmsg("悠遊卡自動加值理由碼 須為4碼");
         return;
      }
   }
   else {

      //--05/10 User 要求取消此限制
//		if (wp.item_eq("bin_type","J") && ls_reason_code.length()!=3) {
//			errmsg("JCB理由碼為3碼?!");
//			return;
//		}
//		if (wp.item_eq("bin_type","M") && ls_reason_code.length()!=4) {
//			errmsg("MASTER理由碼為4碼?!");
//			return;
//		}
//		if (pos("|V|N",wp.itemStr("bin_type"))>0 && ls_reason_code.length()!=2) {
//			errmsg("NCCC，VISA理由碼為2碼?!");
//			return;
//		}
   }
}

void checkSecond() {
   //-第二次扣款----------------------
   if (wp.itemEq("sec_status", "30")) {
      errmsg("[二次扣款]已覆核, 無法修改!");
      return;
   }
   String lsCurr = wp.itemStr("BL_curr_code");
   ibOverseaFee = isOverseaFee();

   //--消費金額-
   //--JH-R98013:國外交易手續費另列--
   double lmDestAmt = 0;
   if (zzCurr.isTw(lsCurr)) {
      lmDestAmt = wp.itemNum("BL_dest_amt");
      if (ibOverseaFee == false) {
         lmDestAmt = lmDestAmt - wp.itemNum("BL_db_mccr")
               - wp.itemNum("BL_db_iccr") - wp.itemNum("BL_issue_fee");
      }
   }
   else {
      lmDestAmt = wp.itemNum("BL_dc_dest_amt");
      if (ibOverseaFee == false) {
         lmDestAmt = lmDestAmt - wp.itemNum("BL_db_mccr")
               - wp.itemNum("BL_db_iccr") - wp.itemNum("BL_issue_fee");
      }
   }
   if (wp.itemEq("sec_part_mark", "Y")) {
      if (zzCurr.isTw(lsCurr)) {
         if (lmDestAmt == wp.itemNum("sec_twd_amt")) {
            errmsg("部份註記, 扣款金額不合理?!");
            return;
         }
      }
      else if (lmDestAmt == wp.itemNum("sec_dc_amt")) {
         errmsg("部份註記, 扣款金額不合理?!");
         return;
      }
   }
   else {
      if (zzCurr.isTw(lsCurr)) {
         if (lmDestAmt != wp.itemNum("sec_twd_amt")) {
            errmsg("無部份註記, 扣款金額不合理?!");
            return;
         }
      }
      else if (lmDestAmt != wp.itemNum("sec_dc_amt")) {
         errmsg("無部份註記, 扣款金額不合理?!");
         return;
      }
   }

   if (wp.itemEmpty("sec_msg")) {
      errmsg("請輸入訊息?!");
      return;
   }
   //--check 扣款理由碼----------------------------------
   String lsBillType = wp.itemStr("BL_bill_type");
   String lsReasonCode = wp.itemStr("sec_reason_code");
   if (eqAny(lsBillType, zzcomm.TSCC_bill_type)) {
      if (lsReasonCode.length() != 4) {
         errmsg("悠遊卡自動加值理由碼 須為4碼");
         return;
      }
   }
   else {
      if (wp.itemEq("bin_type", "J") && lsReasonCode.length() != 4) {
         errmsg("JCB 理由碼為 4 碼?!");
         return;
      }
      if (wp.itemEq("bin_type", "M") && lsReasonCode.length() != 4) {
         errmsg("MASTER 理由碼為 4 碼?!");
         return;
      }
      if (pos("|V|N", wp.itemStr("bin_type")) > 0 && lsReasonCode.length() != 2) {
         errmsg("NCCC，VISA 理由碼為 2 碼?!");
         return;
      }
   }
}

void checkDelete() {
   selectRskChgback();
   if (rc != 1)
      return;

   //-一扣取消,刪除--
   if (wp.itemEmpty("chgb_action") || wp.itemEq("chgb_action", "1")) {
      if (colEq("chg_stage", "1") == false || colEq("sub_stage", "10") == false) {
         errmsg("主階段<>1, 狀態<>10: 不可刪除");
         return;
      }
   }
   //-二扣取消--
   if (wp.itemEq("chgb_action", "3")) {
      if (colEq("chg_stage", "3") == false || colEq("sub_stage", "10") == false) {
         errmsg("主階段<>3, 狀態<>10: 不可[取消二扣]");
         return;
      }
   }
}

@Override
public int dbInsert() {
   actionInit("A");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   rskm01.RskChgback oochgb = new rskm01.RskChgback();
   oochgb.setConn(wp);
   if (oochgb.rskm0210Insert() != 1) {
      errmsg(oochgb.getMsg());
      return -1;
   }

   //-update bil_bill-
   kk1 = oochgb.isCtrlSeqno;
   updateBilBill();
   if (rc != 1)
      return rc;

   wp.colSet("ctrl_seqno", oochgb.isCtrlSeqno);
   return rc;
}
public int dbInsertP0015() {
   actionInit("A");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   rskm01.RskChgback oochgb = new rskm01.RskChgback();
   oochgb.setConn(wp);
   oochgb.checkCardBase =false;

   if (oochgb.rskm0210Insert() != 1) {
      errmsg(oochgb.getMsg());
      return -1;
   }

   //-update bil_bill-
   kk1 = oochgb.isCtrlSeqno;
   updateBilBill();
   if (rc != 1)
      return rc;

   wp.colSet("ctrl_seqno", oochgb.isCtrlSeqno);
   return rc;
}
//public int dbInsert_P0015() throws Exception  {
//   actionInit("A");
//   dataCheck();
//   if (rc != 1) {
//      return rc;
//   }
//
//   rskm01.Rsk_chgback oo_chgback = new rskm01.Rsk_chgback();
//   oo_chgback.setConn(wp);
//   oo_chgback.check_cardBase =false;
//
//   if (oo_chgback.rskm0210_Insert() != 1) {
//      errmsg(oo_chgback.getMsg());
//      return -1;
//   }
//
//   //-update bil_bill-
//   kk1 = oo_chgback.is_ctrl_seqno;
//   update_bil_bill();
//   if (rc != 1)
//      return rc;
//
//   wp.col_set("ctrl_seqno", oo_chgback.is_ctrl_seqno);
//   return rc;
//}

@Override
public int dbUpdate() {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   if (eqAny(wp.itemStr("chgb_action"), "1")) {
      wp.itemSet("chg_stage", "1");
      updateFirst();
   }
   else if (eqAny(wp.itemStr("chgb_action"), "2")) {
      wp.itemSet("chg_stage", "2");
      updateRepsent();
   }
   else if (eqAny(wp.itemStr("chgb_action"), "3")) {
      wp.itemSet("chg_stage", "3");
      updateSecond();
   }
//	else if (eq_any(wp.itemStr("chgb_action"),"4")) {
//		update_prearbit();
//	}
//	else if (eq_any(wp.itemStr("chgb_action"),"5")) {
//		update_arbit();
//	}
   return rc;
}

void updateFirst() {
   rskm01.RskChgback oo1 = new rskm01.RskChgback();
   oo1.setConn(wp);
   if (oo1.rskm0210First() != 1) {
      errmsg(oo1.getMsg());
      return;
   }

   varsSet("ctrl_seqno", wp.colStr("ctrl_seqno"));
   return;
}

void updateRepsent() {
   //-w_rskm0245-
}

void updateSecond() {
   rskm01.RskChgback oo1 = new rskm01.RskChgback();
   oo1.setConn(wp);
   String lsDbStage = colStr("chg_stage");
   if (oo1.rskm0210Second(lsDbStage) != 1) {
      errmsg(oo1.getMsg());
      return;
   }

   varsSet("ctrl_seqno", oo1.isCtrlSeqno);
}

@Override
public int dbDelete() {
   actionInit("D");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   rskm01.RskChgback oochgb = new rskm01.RskChgback();
   oochgb.setConn(wp);
   if (wp.itemEmpty("chgb_action") || wp.itemEq("chgb_action", "1")) {
      if (oochgb.dataDelete() != 1) {
         errmsg(oochgb.getMsg());
         return -1;
      }
   }
   //-二扣取消-
   if (wp.itemEq("chgb_action", "3")) {
      if (oochgb.rskm0210SecondCancel() != 1) {
         errmsg(oochgb.getMsg());
         return -1;
      }
   }

   //wp.col_set("ctrl_seqno",oo_chgback.is_ctrl_seqno);
   return rc;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

void updateBilBill() {
   if (ibDebit) {
      strSql = "update dbb_bill set"
            + " rsk_post =decode(rsk_post,'','O',rsk_post), "
            + " rsk_ctrl_seqno =?  "
            + " where reference_no =?"
      ;
   }
   else {
      strSql = "update bil_bill set"
            + " rsk_post =decode(rsk_post,'','O',rsk_post), "
            + " rsk_ctrl_seqno =?  "
            + " where reference_no =?"
      ;
   }
   this.sqlExec(strSql, new Object[]{kk1, wp.itemStr("reference_no")});
   if (sqlRowNum != 1) {
      errmsg("update bil[dbb]_bill error; " + sqlErrtext);
   }
}

boolean isOverseaFee() {
   if (empty(isRefno) && empty(isRefnoOri)) {
      return false;
   }
   if (ibDebit) {
      strSql = "select count(*) as db_cnt"
            + " from dbb_bill"
            + " where reference_no =?"
            + " and reference_no_fee_f <>''";
      setString(1, isRefno);
   }
   else {
      strSql = "select count(*) as db_cnt"
            + " from bil_bill"
            + " where reference_no =?"
            + " and reference_no_fee_f <>''";
      if (empty(isRefnoOri)) {
         setString(1, isRefno);
      }
      else setString(1, isRefnoOri);
   }
   sqlSelect(strSql);
   if (sqlRowNum < 0) {
      wp.log("select bil[dbb]_bill.reference_no_fee_f, ref_no=" + isRefno);
      return false;
   }
   return colNum("db_cnt") > 0;
}

}
