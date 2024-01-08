package rskm01;
/** 調單作業結案維護
 * 2020-1117   JH    modify
 * 2019-1206:  Alex  add initButton
 * 2018-0329:	JH		modify
 *
 * */

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Rskm0120 extends BaseProc {
int ii_err = 0, ii_ok = 0;

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "L":
         strAction = "";
         clearFunc(); break;
      case "X":
//         is_action = "new";
//         clearFunc();
         break;
      case "Q":
         queryFunc(); break;
      case "M":
         queryRead(); break;
      case "S":
         querySelect(); break;
      case "R":
         dataRead(); break;
      case "C":
         dataProcess(); break;
      default:
         alertErr("未指定 actionCode 執行功能, action[%s]",wp.buttonCode);
   }

   dddwSelect();
   initButton();
}

@Override
public void queryFunc() throws Exception {
   if (empty(wp.itemStr("ex_ctrl_seqno"))
         && empty(wp.itemStr("ex_card_no"))
         && empty(wp.itemStr("ex_date1"))
         && empty(wp.itemStr("ex_date2"))
         && empty(wp.itemStr("ex_user"))) {
      alertErr("條件不可全部空白");
      return;
   }

   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("調單日期起迄：輸入錯誤");
      return;
   }

   wp.whereStr = " where 1=1 and close_apr_date =''"
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
         + sqlCol(wp.itemStr("ex_card_no"), "card_no")
         + sqlCol(wp.itemStr("ex_user"), "add_user")
         + sqlCol(wp.itemStr("ex_date1"),"add_date",">=")
         + sqlCol(wp.itemStr("ex_date2"),"add_date","<=")         
   ;

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = "hex(rowid) as rowid, mod_seqno,"
         + "bin_type, "
         + "ctrl_seqno, add_date,"
         + "card_no, "
         + "decode(length(v_card_no),0,'N','Y') as wk_tpan, "
         + "proc_result, "
         + "recv_date, "
         + commSqlStr.sqlDebitFlag + ","
         + "payment_type, "
         + "rept_status, "
         + "close_add_date, "
         + "v_card_no, "
         + "reference_no, "
         + "rept_seqno, "
         + "close_add_user,"
         + " uf_nvl(fees_flag,'N') as fees_flag,"
         + " fees_amt";
   wp.daoTable = "rsk_receipt";
   wp.whereOrder = " order by ctrl_seqno ";

   pageQuery();
   wp.listCount[0] = sqlRowNum;
   if (sqlRowNum <= 0) {
      alertErr(appMsg.errCondNodata);
      return;
   }
   wp.setPageValue();
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
public void dataProcess() throws Exception {

   Rskm0120Func func = new Rskm0120Func();
   func.setConn(wp);
   String[] opt = wp.itemBuff("db_opt");
   String[] rowid = wp.itemBuff("rowid");
   wp.listCount[0] = wp.itemRows("rowid");
   if (optToIndex(opt[0]) < 0) {
      alertErr("請點選欲結案資料");
      return;
   }

   int rr = -1;
   for (int ii = 0; ii < opt.length; ii++) {
      rr = this.optToIndex(opt[ii]);
      if (rr < 0) {
         continue;
      }

      String lsProc = wp.colStr(rr, "proc_result");
      String lsRdate = wp.colStr(rr, "recv_date");
      double lmFamt = wp.colNum(rr, "fees_amt");
      String lsCtrlseq = wp.colStr(rr, "ctrl_seqno");
      String lsBintype = wp.colStr(rr, "bin_type");
      String lsModseq = wp.colStr(rr, "mod_seqno");
      String lsFflag = wp.colStr(rr, "fees_flag");

      setProcDesc(rr, "");

      if (!empty(lsProc) && empty(lsRdate)) {
         setProcDesc(rr, "回單日期: 不可空白");
         continue;
      }
      if (eqIgno(lsFflag, "Y") && lmFamt == 0) {
         setProcDesc(rr, "調單費不可=0");
         continue;
      }

      func.varsClear();
      func.varsSet("rowid", rowid[rr]);
      func.varsSet("mod_seqno", lsModseq);
      func.varsSet("proc_result", lsProc);
      func.varsSet("recv_date", lsRdate);
      if (eqIgno(lsFflag, "Y")) {
         func.varsSet("fees_flag", lsFflag);
         func.varsSet("fees_amt", "" + lmFamt);
      }
      else {
         func.varsSet("fees_flag", lsFflag);
         func.varsSet("fees_amt", "0");
      }
      func.varsSet("ctrl_seqno", lsCtrlseq);
      func.varsSet("bin_type", lsBintype);
      func.varsSet("reference_no", wp.colStr("reference_no"));

      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         wp.colSet(rr, "opt_edit", "disabled"); // -ckbox:hide-
         ii_ok++;
         continue;
      }
      ii_err++;
      wp.colSet(rr, "ok_flag", "X");
      wp.colSet(rr, "db_desc", func.getMsg());
   }

   // -re-Query-
   // queryRead();
   alertMsg("執行處理: 成功筆數=" + ii_ok + "; 失敗筆數=" + ii_err);
}

void setProcDesc(int rr, String s1) {
   if (empty(s1)) {
      wp.colSet(rr, "ok_flag", "-");
      wp.colSet(rr, "db_desc", s1);
      return;
   }

   ii_err++;
   wp.colSet(rr, "ok_flag", "X");
   wp.colSet(rr, "db_desc", s1);
}

@Override
public void initButton() {
   btnModeAud("XX");
}

}
