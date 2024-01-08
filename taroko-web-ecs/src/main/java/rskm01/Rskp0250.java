package rskm01;
/**
 * 2020-1019   JH    bug-fix
 * 2020-0528   JH    ++approve_all()
 * 2020-0512   JH    apr_date
 * 2019-1212   JH    appr_dept
 * 預備仲裁/仲裁登錄覆核 V.2018-0409
 */

import ofcapp.BaseAction;

public class Rskp0250 extends BaseAction {
 Rskp0250Func func=null;

@Override
public void userAction() throws Exception {
   switch (wp.buttonCode) {
      case "C2":
         doApproveAll(); break;
      default:
         defaultAction();
   }
}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_add_date1"), wp.itemStr("ex_add_date2")) == false) {
      alertErr("建檔日期起迄：輸入錯誤");
      return;
   }
   String ss = wp.itemStr("ex_ctrl_seqno");
   if (!empty(ss) && ss.length() < 4) {
      alertErr("[控制流水號] 至少4碼");
      return;
   }

   String lsWhere = " where decode(arbit_times,1,pre_apr_date,arb_apr_date)='' "
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
         + sqlCol(wp.itemStr("ex_user_id"), "decode(arbit_times,1,pre_add_user,arb_add_user)")
         + sqlCol(wp.itemStr("ex_add_date1"), "pre_add_date", ">=")
         + sqlCol(wp.itemStr("ex_add_date2"), "pre_add_date", "<=");

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
   wp.colSet("sum_cnt", wp.totalRows);
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " hex(rowid) as rowid, mod_seqno "
         + ", ctrl_seqno, bin_type, arbit_times"
         + ", card_no"
         + ", purchase_date"
         + ", mcht_country"
         + ", film_no"
         + ", uf_dc_curr(curr_code) as curr_code "
         + ", uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt "
         + ", pre_event_date"
//		+", viol_date"
         + ", pre_apply_date"
         + ", pre_result"
         + ", arb_apply_date"
         + ", arb_result"
         + ", arb_add_user"
         + ", debit_flag "
   +", decode(arbit_times,1,pre_add_user,arb_add_user) as can_user"
   ;
   wp.daoTable = " rsk_prearbit";
   wp.whereOrder = " order by ctrl_seqno, reference_no ";

   pageQuery();
   wp.setListCount(1);

   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();
//   set_canAppr("can_user");

}

@Override
public void querySelect() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void dataRead() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void saveFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void procFunc() throws Exception {
   int ll_ok = 0, ll_err = 0;
   func = new rskm01.Rskp0250Func();
   func.setConn(wp);

   String[] aaOpt = wp.itemBuff("opt");
   String[] aaRowid = wp.itemBuff("rowid");
   String[] aaModseq = wp.itemBuff("mod_seqno");
   wp.listCount[0] = wp.itemRows("rowid");
   if (optToIndex(aaOpt[0])<0) {
      alertErr(appMsg.optApprove);
      return;
   }

   int rr = -1;
   for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = this.optToIndex(aaOpt[ii]);
      if (rr < 0)
         continue;
      optOkflag(rr);
//      String ls_user = wp.itemStr(rr, "can_user");
//      if (!appr_bankUnit(ls_user)) {
//         ll_err++;
//         opt_okflag(rr, -1);
//         continue;
//      }

      wp.colSet(rr, "ok_flag", "-");
      func.varsSet("rowid", aaRowid[rr]);
      func.varsSet("mod_seqno", aaModseq[rr]);
      if (func.dataProc() == 1) {
         wp.colSet(rr, "ok_flag", "V");
         ll_ok++;
         continue;
      }
      else {
         wp.colSet(rr, "ok_flag", "X");
         ll_err++;
         continue;
      }
   }
   if (ll_ok > 0) {
      sqlCommit(1);
   }
   alertMsg("覆核完成; 成功筆數=" + ll_ok + ", 錯誤筆數=" + ll_err);
}

void doApproveAll() throws Exception {
   wp.listCount[0] =wp.itemRows("rowid");
   wp.pageControl();

   String sql1 ="select hex(rowid) as rowid, mod_seqno"+
         ", decode(arbit_times,1,pre_add_user,arb_add_user) as can_user"+
         " from rsk_prearbit "+wp.queryWhere+
         " and decode(arbit_times,1,pre_apr_date,arb_apr_date) ='' "+  //待覆核--
         " order by ctrl_seqno";
   sqlSelect(sql1);
   if (sqlRowNum <=0) {
      alertErr("無資料可覆核");
      return;
   }

   func = new Rskp0250Func();
   func.setConn(wp);

   int ll_ok=0, ll_err=0;
   int ll_nrow =sqlRowNum;
   for (int ii = 0; ii < ll_nrow; ii++) {
      if (!apprBankUnit(sqlStr(ii,"can_user"),wp.loginUser)) {
         ll_err++;
         continue;
      }

      func.varsSet("rowid", sqlStr(ii,"rowid"));
      func.varsSet("mod_seqno", sqlStr(ii,"mod_seqno"));
      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
         ll_ok++;
         continue;
      }
      else ll_err++;
   }
   //--
   wp.listCount[0] =0;
   queryFunc();
//   wp.alert_Clear();
   wp.alertMesg("覆核處理: 成功筆數=" + ll_ok + "; 失敗筆數=" + ll_err);
}

@Override
public void initButton() {
   if (wp.colNum("sum_cnt") <=wp.pageRows) {
      buttonOff("btnproc2_off");
   }
}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

}
