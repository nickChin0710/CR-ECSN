package rskm03;
/* 系統參數對照表維護 V.2019-1206
 * 2019-1206:  Alex  add initButton
 * 2018-0322:	JH		++id_code,id_code2
 *
 * */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

import java.util.Arrays;

public class Rskm3150 extends BaseEdit {
Rskm3150Func func;
String kk1 = "IDTAB", kk2 = "", kk3 = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   msgOK();

   strAction = wp.buttonCode;
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) {
      //-資料讀取-
      strAction = "R";
      dataRead();
   }
   else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
   }
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
   }
   else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page*/
      queryRead();
   }
   else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
   }
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   }

   dddwSelect();
   initButton();
   if (wp.respHtml.indexOf("_detl") > 0) {
      showScreen_detl();
   }

}

void showScreen_detl() {
   //-set new-
   int rr = 0;
   rr = wp.listCount[0];
   wp.colSet(0, "IND_NUM", "" + rr);
}

@Override
public void queryFunc() throws Exception {
   wp.whereStr = " where wf_parm ='IDTAB'"
         + sqlCol(wp.itemStr("ex_type"), "wf_key", "like%")
   ;

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = "wf_parm, "
         + "wf_key,"
         + "wf_desc, "
         + "wf_value5, "
         + "wf_value6 "
   ;
   wp.daoTable = "ptr_sys_parm";
   wp.whereOrder = " order by wf_key ";
   pageQuery();


   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }
   wp.totalRows = wp.dataCnt;
   wp.listCount[1] = wp.dataCnt;
   wp.setListCount(1);
   wp.setPageValue();
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   dataRead();
}

@Override
public void dataRead() throws Exception {
//		if(empty(kk1)){
//			kk1=wp.itemStr("A_wf_parm");
//		}
   if (empty(kk2)) {
      kk2 = wp.itemStr("A_wf_key");
   }

   if (empty(kk2)) {
      alertErr("資料類別不可空白");
      return;
   }

   this.daoTid = "A_";
   wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
         + "wf_key,   "
         + "wf_desc , "
         + "wf_value5,"
         + "wf_value6,"
         + "mod_seqno,"
         + "mod_user, " + "uf_2ymd(mod_time) as mod_date "
         + ", mod_pgm";
   wp.daoTable = "ptr_sys_parm";
   wp.whereStr = "where 1=1"
         + sqlCol(kk1, "wf_parm")
         + sqlCol(kk2, "wf_key");

   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1 + ", " + kk2);
      return;
   }
   dataRead_detl();
}

void dataRead_detl() throws Exception {
   this.selectNoLimit();
   //wp.sel.varRows = 99999;
   wp.daoTable = "ptr_sys_idtab";
   wp.selectSQL = "hex(rowid) as b_rowid"
         + ", wf_id     "
         + ", wf_desc "
         + ", id_code, id_code2"
   ;
   wp.whereStr = "WHERE 1=1"
         + sqlCol(kk2, "wf_type");
   wp.whereOrder = " order by wf_id";
   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";
}


@Override
public void saveFunc() throws Exception {
   func = new Rskm3150Func();
   func.setConn(wp);
   func.modPgm = wp.modPgm();
   func.modUser = wp.loginUser;

   int ll_ok = 0, ll_err = 0;
   //String ls_opt="";

   String[] aa_id = wp.itemBuff("wf_id");
   String[] aa_desc = wp.itemBuff("wf_desc");
   String[] aa_code = wp.itemBuff("id_code");
   String[] aa_code2 = wp.itemBuff("id_code2");
   String[] aa_opt = wp.itemBuff("opt");
   wp.listCount[0] = aa_id.length;
   wp.colSet("IND_NUM", "" + aa_id.length);

   //-check duplication-
   for (int ll = 0; ll < aa_id.length; ll++) {
      wp.colSet(ll, "ok_flag", "");
      //-option-ON-
      if (checkBoxOptOn(ll, aa_opt)) {
         continue;
      }
      if (empty(aa_id[ll])) {
         continue;
      }

      if (ll != Arrays.asList(aa_id).indexOf(aa_id[ll])) {
         wp.colSet(ll, "ok_flag", "!");
         ll_err++;
         alertErr("資料值重複: " + ll_err);
         break;
      }

      //-desc<>''-
      if (empty(aa_desc[ll])) {
         ll_err++;
         alertErr("說明:不可空白 " + ll_err);
         break;
      }

   }
   if (ll_err > 0) {
      return;
   }

   //-delete no-approve-
   if (func.dbDelete() < 0) {
      alertErr(func.getMsg());
      return;
   }

   //-insert-
   for (int ll = 0; ll < aa_id.length; ll++) {
      if (empty(aa_id[ll])) {
         continue;
      }

      //-option-ON-
      if (checkBoxOptOn(ll, aa_opt)) {
         continue;
      }

      func.varsSet("wf_id", aa_id[ll]);
      func.varsSet("wf_desc", aa_desc[ll]);
      func.varsSet("id_code", aa_code[ll]);
      func.varsSet("id_code2", aa_code2[ll]);

      if (func.dbInsert() == 1) {
         ll_ok++;
      }
      else {
         ll_err++;
      }
   }
   if (ll_ok > 0) {
      sqlCommit(1);
   }
   alertMsg("資料存檔處理完成; OK=" + ll_ok + ", ERR=" + ll_err);
   dataRead();
}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) this.btnModeAud(wp.colStr("A_rowid"));

}
}
