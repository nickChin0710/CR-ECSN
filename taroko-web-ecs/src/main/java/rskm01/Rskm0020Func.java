package rskm01;
/**
 * 2021-0722   JH    52,53,55,I1,I2+sign<0
 * 2020-0521   JH    ++結案理由(D7,D8)
 * 2020-0414	JH		++結案理由(I1,I2,I3)
 */

public class Rskm0020Func extends busi.FuncEdit {
busi.CommCurr zzCurr = new busi.CommCurr();

String kk1 = "", kk2 = "", isRefno = "";
boolean ibOversea = false, ibDebit = false, ibTwCurr = true;

@Override
public int querySelect() {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

@Override
public int dataSelect() {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

void selectRskProblem() {
   daoTid = "A.";
   strSql = "select "
         + " A.*"
         + " from rsk_problem A"
         + " where 1=1"
//         + CommSqlStr.col(kk1, "ctrl_seqno")
//         + CommSqlStr.col(kk2, "bin_type")
//         + CommSqlStr.mod_seqno(wp.mod_seqno())
   		 ;
   int ii=0;
   if(empty(kk1) == false) {
	   strSql += " and ctrl_seqno = ? ";
	   ii++;
	   setString(ii,kk1);
   }
   
   if(empty(kk2) == false) {
	   strSql += " and bin_type = ? ";
	   ii++;
	   setString(ii,kk2);
   }
   
   if(empty(wp.itemStr("mod_seqno")) == false) {
	   strSql += " and nvl(mod_seqno,0) = ? ";
	   ii++;
	   setString(ii,wp.modSeqno());	   
   }
   
   this.sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg(errOtherModify);
   }
}

int selectRskChgback() {
   daoTid = "B.";
   strSql = "SELECT chg_stage, sub_stage, "
         + " fst_twd_amt as fst_amt, fst_dc_amt as dc_fst_amt"
         + " from rsk_chgback"
         + " where ctrl_seqno =?"
         + " and bin_type =?";
   setString(1, kk1);
   setString(2, kk2);
   sqlSelect(strSql);
   return sqlRowNum;
}

void dataCheckDelete() {
   kk1 = wp.itemStr("ctrl_seqno");
   kk2 = wp.itemStr("bin_type");
   isRefno = wp.itemStr("reference_no");
   selectRskProblem();
   if (rc != 1)
      return;

   if (colEq("A.prb_status", "60") == false) {
      errmsg("狀態碼不是 結案待覆核, 不可取消結案");
   }
   return;
}

@Override
public void dataCheck() {
   kk1 = wp.itemStr("ctrl_seqno");
   kk2 = wp.itemStr("bin_type");
   isRefno = wp.itemStr("reference_no");
   selectRskProblem();
   if (rc != 1)
      return;

   if (colStr("A.prb_status").compareTo("30") < 0 ||
         colStr("A.prb_status").compareTo("60") > 0) {
      errmsg("狀態碼不符, 不可結案？！");
      return;
   }

   ibDebit = wp.itemEq("debit_flag", "Y");
   ibTwCurr = zzCurr.isTw(wp.itemStr("db_curr_code"));

   double lmAmt = 0;
   double lmMchtRepay = wp.itemNum("dc_mcht_repay");

   lmAmt = zzCurr.dc2twAmt(colNum("A.dest_amt"),colNum("A.dc_dest_amt"), lmMchtRepay);
   wp.itemSet("mcht_repay", "" + lmAmt);

   //--特店退款金額--
   String lsCloResult=wp.itemStr("clo_result");
   if (!eq(lsCloResult, "52")) {
      if (lmMchtRepay <= 0) {
         errmsg("結案金額需大於 0 ");
         return;
      }
   }
   //-JH(R98013):國外交易手續費-
   BilBill bill = new BilBill();
   bill.setConn(wp);
   bill.debitFlag(wp.itemStr("debit_flag"));
   ibOversea = bill.isOverseaFee(isRefno, wp.itemStr("reference_no_ori"));
   if (ibOversea && wp.itemNum("mcht_close_fee") != 0) {
      errmsg("國外交易之手續費, 不可輸入");
      return;
   }

   if (pos("|03|21", lsCloResult) > 0) {
      if (ibTwCurr) {
         lmAmt = wp.itemNum("mcht_repay") + wp.itemNum("mcht_close_fee");
         if (lmAmt > wp.itemNum("prb_amount")) {
            errmsg("結案碼為03,21時; 結案金額+手續費不可大於問題金額");
            return;
         }
      }
      else {
    	  lmAmt = wp.itemNum("dc_mcht_repay");
         if (lmAmt > wp.itemNum("dc_prb_amount")) {
            errmsg("結案碼為03,21時; 結算結案金額不可大於結算問題金額");
            return;
         }
      }
   }
   if (pos("|03|04|05", lsCloResult) > 0) {
      if (selectCrdCard() <= 0) {
         errmsg("請輸入正確卡號？！");
         return;
      }
   }
   else {
      wp.itemSet("org_card_no", "");
      wp.itemSet("rsk_err_nr", "");
   }

   int li_sign = (int) wp.itemNum("BL_tx_sign");
   if (ibDebit) {
      if (li_sign < 0 && pos("|N0|N5", lsCloResult) <= 0) {
         errmsg("Visa Debit之負項交易結案碼需為 N0,N5");
         return;
      }
      if (li_sign > 0 && pos("|N0|N5", lsCloResult) > 0) {
         errmsg("Visa Debit之正項交易結案碼不可為 N0,N5");
         return;
      }
   }
   else {
      if (li_sign<0 && !commString.strIn(lsCloResult,"|08|52|53|55|I1|I2")) {
         errmsg("本結案處理結果，交易類別限負項？！");
         return;
      }
      if (commString.strIn(lsCloResult,",86,I3") && li_sign <0) {
         errmsg("本結案處理結果，交易類別限正項？！");
         return;
      }
//      if (wp.item_eq("prb_mark", "Q")) {
//         if (pos("|52|53|55|I1|I2", ls_cloResult) > 0) {
//            if (li_sign > 0) {
//               errmsg("本結案處理結果，交易類別限負項？！");
//               return;
//            }
//         }
//         else {
//            if (li_sign < 0 && pos(",86,I3", ls_cloResult) > 0) {
//               errmsg("本結案處理結果，交易類別限正項？！");
//               return;
//            }
//         }
//      }
//      else
      if (wp.itemEq("prb_mark", "E")) {
         if (pos("|08|09|D4|I1|I2", lsCloResult) > 0) {
            if (li_sign > 0) {
               errmsg("本結案處理結果，交易類別限負項？！");
               return;
            }
         }
         else {
            if (li_sign < 0) {
               errmsg("本結案處理結果，交易類別限正項？！");
               return;
            }
         }
      }
      else if (wp.itemEq("prb_mark", "S")) {
         if (pos("|92|93", lsCloResult) > 0) {
            if (li_sign < 0) {
               errmsg("本結案處理結果，交易類別限正項？！");
               return;
            }
         }
      }
   }
   //-------------------------------------------
   if (ibDebit && eqIgno(lsCloResult, "N5")) {
      if (wp.itemNe("prb_src_code", "SQ") || li_sign >= 0) {
         errmsg("結案碼 N5, 須為系統列問交之負項交易");
         return;
      }
   }
   if (!ibDebit && eqIgno(lsCloResult, "55")) {
      if (wp.itemNe("prb_src_code", "SQ") || li_sign >= 0) {
         errmsg("結案碼 55, 須為系統列問交之負項交易");
         return;
      }
   }
   //-JH2020-0414-
   if (!ibDebit && pos("|I1|I2", lsCloResult) > 0) {
      if (wp.itemNe("prb_src_code", "SE") || li_sign >= 0) {
         errmsg("結案碼 I1,I2, 須為系統列不合格之負項交易");
         return;
      }
   }
   if (!ibDebit && pos("|I3", lsCloResult) > 0) {
      if (wp.itemNe("prb_src_code", "SE") || li_sign < 0) {
         errmsg("結案碼 I3 須為系統列不合格之正項交易");
         return;
      }
   }
   //--扣款成功-------------------------------------
   if (pos("|06|18|93|D8", lsCloResult) > 0) {
      if (selectRskChgback() <= 0) {
         errmsg("本交易，查無 CHGBACK 資料？！");
         return;
      }
      if (zzCurr.isTw(wp.itemStr("db_curr_code"))) {
         if (wp.itemNum("mcht_repay") != colNum("B.fst_amt")) {
            errmsg("扣款金額與問交金額不相等？！");
            return;
         }
      }
      else {
         if (wp.itemNum("dc_mcht_repay") != colNum("B.dc_fst_amt")) {
            errmsg("結算扣款金額與結算問交金額不相等？！");
            return;
         }
      }
      if (pos("|1|3", colStr("B.chg_stage")) > 0 &&
            colEq("B.sub_stage", "30") == false) {
         errmsg("扣款狀態碼不符？！");
         return;
      }
   }
   if (pos("|51|52", lsCloResult) > 0) {
      if (wp.itemNe("prb_src_code", "SQ")) {
         errmsg("本結案處理結果，限系統列問交？！");
         return;
      }
   }
   if (wp.itemNum("mcht_close_fee") > 0) {
      if (pos("|01|02|03|06|07|11|12|13|14|18|20|21|51|61|62|63|D1|D2|D3|D4|D5|D7|D8|D9|DA", lsCloResult) <= 0) {
         errmsg("問交結案碼 不可輸入手續費");
         return;
      }
   }
//	ldc_fee = dw_data.of_getitem(1,"db_mccr")	&
//			+ dw_data.of_getitem(1,"db_iccr")	&
//			+ dw_data.of_getitem(1,"db_issue_fee")
//if dec(dw_data.of_getitem(1,"merchant_fee"))>ldc_fee then
//	f_errmsg("手續費 輸入錯誤~")
//	Return -1
//end if
   if (eqAny(lsCloResult, "34") && wp.itemEq("prb_src_code", "SQ")) {
      if (pos("|I|E", wp.itemStr("BL_payment_type")) > 0) {
         errmsg("系統列問交之分期交易, 不可以 34 結案");
         return;
      }
   }

   //-JH:21:取消-
   if (pos("|55|N5|15|16|17|D0|D6|D7|D8", lsCloResult) > 0) {
      if (wp.itemNum("dc_mcht_repay") != wp.itemNum("dc_prb_amount")) {
         errmsg("結算結案金額 須等於 結算問交金額 (55,N5,15,16,17,D0,D6,D7,D8)");
         return;
      }
   }

//   if (pos("|18|20|D8|DA|06|07|I1", lsCloResult) > 0) {
//      if (wp.itemEmpty("prb_glmemo3")) {
//         errmsg("應付款-其他銷帳鍵值: 不可空白");
//         return;
//      }
//   }

   String ls_contr_no = wp.itemStr("contract_no");
   if (empty(ls_contr_no)) {
      if (eq(lsCloResult, "I1")) {
         errmsg("申請書編號: 不可空白");
         return;
      }
   }
   else {
      strSql = "select count(*) as xx_cnt from bil_contract where contract_no =?";
      sqlSelect(strSql, ls_contr_no);
      if (colNum("xx_cnt") <= 0) {
         errmsg("申請書編號: 輸入錯誤");
         return;
      }
   }

   //-手續費: 已外加不可輸入-
   double lm_repay =wp.num("dc_mcht_repay"); //+wp.num("mcht_close_fee");
   if (lm_repay!=wp.num("dc_prb_amount")) {
      errmsg("結案金額: 須等於問交金額");
      return;
   }
   
   if(ibDebit) {
	   if("Y".equals(colStr("A.back_flag")) && colEmpty("A.back_status")) {
		   errmsg("回存尚未回覆: 不可進行結案");
		   return ;
	   }
   }
   
   return;
}

int selectCrdCard() {
   if (wp.itemEmpty("org_card_no")) {
      return 0;
   }

   if (ibDebit == false) {
      strSql = "select count(*) as db_cnt"
            + " from crd_card"
            + " where card_no =?";
   }
   else {
      strSql = "select count(*) as db_cnt"
            + " from dbc_card"
            + " where card_no =?";
   }
   setString(1, wp.itemStr("org_card_no"));
   sqlSelect(strSql);
   return colInt("db_cnt");
}

@Override
public int dbInsert() {
   errmsg("不提供[新增] 功能");
   return -1;
}

@Override
public int dbUpdate() {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }
   //--
   rskm01.RskProblem ooprbl = new rskm01.RskProblem();
   ooprbl.setConn(wp);

   ooprbl.isCtrlSeqno = wp.itemStr("ctrl_seqno");
   ooprbl.isReferNo = wp.itemStr("reference_no");
   if (ooprbl.rskm0020Update() != 1) {
      errmsg(ooprbl.getMsg());
      return rc;
   }

   return 1;
}

@Override
public int dbDelete() {
   msgOK();
   actionInit("D");
   dataCheckDelete();
   if (rc != 1) {
      return rc;
   }
   //--
   rskm01.RskProblem ooprbl = new rskm01.RskProblem();
   ooprbl.setConn(wp);
   ooprbl.isCtrlSeqno = wp.itemStr("ctrl_seqno");
   ooprbl.isReferNo = wp.itemStr("reference_no");
   if (ooprbl.rskm0020Delete() != 1) {
      errmsg(ooprbl.getMsg());
      return rc;
   }

   return 1;
}

}
