package rskm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Rskm0520 extends BaseEdit {

String kk1 = "", kk2 = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   rc = 1;

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "L":
         strAction = "";
         clearFunc(); break;
      case "X":
         strAction = "new";
         clearFunc(); break;
      case "Q":
         queryFunc(); break;
      case "M":
         queryRead(); break;
      case "S":
         querySelect(); break;
      case "R":
         dataRead(); break;
      case "A":
         insertFunc(); break;
      case "U":
         updateRetrieve = true;
         updateFunc(); break;
      case "D":
         deleteFunc(); break;
      default:
         alertErr("未指定 actionCode 執行功能, action[%s]",wp.buttonCode);
   }

   dddwSelect();
   initButton();
}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud();
   }
}

@Override
public void dataRead() throws Exception {
   if (isEmpty(kk1)) {
      kk1 = itemKk("wf_type");
   }
   if (isEmpty(kk2)) {
      kk2 = itemKk("wf_id");
   }

   if (isEmpty(kk1) || empty(kk2)) {
      alertErr("[代號類別, 代碼值] 不可空白");
      return;
   }

   wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
         + "wf_type ,uf_tt_idtab('RSKM0520',wf_type) as tt_wf_type, "
         + "wf_id , "
         + "wf_desc , "
         + "id_code , "
         + "id_code2 , "
         + "id_desc2 "
         + ", to_char(mod_time,'yyyymmdd') as mod_date "
         + ", mod_user as mod_user  ";
   wp.daoTable = "ptr_sys_idtab";
   wp.whereStr = "where 1=1"
         + sqlCol(kk1, "wf_type")
         + sqlCol(kk2, "wf_id");
   pageSelect();
   if (sqlRowNum <= 0) {
      alertErr("查無資料, key=" + kk1 + "; " + kk2);
      return;
   }
   //detl_wkdata();
}

//void detl_wkdata() {
//	String ss = deCode_rsk.rsk_iddesc(wp.colStr("wf_type"));
//	wp.col_set("tt_wf_type", ss);
//}

@Override
public void saveFunc() throws Exception  {

   if (checkApproveZz() == false) return;

   Rskm0520Func func = new Rskm0520Func();
   func.setConn(wp);

   rc = func.dbSave(strAction);
   if (rc == -1) {
      alertErr(func.mesg());
   }
   sqlCommit(rc);
}

@Override
public void queryFunc() throws Exception {
   String ls_id_key = wp.itemStr("ex_id_key");
   if (isEmpty(ls_id_key)) {
      alertErr("代碼類別  不可空白");
      return;
   }

   wp.whereStr = "WHERE 1=1"
         + sqlCol(ls_id_key, "wf_type")
   ;
   wp.whereOrder = " order by wf_type, wf_id ";

   //-page control-
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = "wf_type, wf_id, id_code, wf_desc, id_code2, id_desc2"
   ;
   wp.daoTable = "ptr_sys_idtab";
   if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1,2";
   }

   pageQuery();
   //list_wkdata();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.totalRows = wp.dataCnt;
   wp.setPageValue();
}

//void list_wkdata() {
//	String ss = "";
//	for (int ii = 0; ii < wp.selectCnt; ii++) {
//		ss = wp.colStr(ii, "wf_type");
//		wp.col_set(ii, "tt_wf_type", deCode_rsk.rsk_iddesc(ss));
//	}
//}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");

   dataRead();
}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskm0520")) {
         wp.optionKey = wp.colStr("ex_id_key");
//         dddw_list("dddw_id_key","ptr_sys_idtab"
//               ,"wf_id","wf_desc","where wf_type='RSKM0520'");
         dddwList("dddw_id_key", "select wf_id as db_code, wf_desc||'_'||wf_id as db_desc " +
               " from ptr_sys_idtab where wf_type='RSKM0520'");
      }

      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.optionKey = wp.colStr("kk_wf_type");
         dddwList("dddw_kk_idkey", "select wf_id as db_code, wf_desc||'_'||wf_id as db_desc " +
               " from ptr_sys_idtab where wf_type='RSKM0520'");
      }
   }
   catch (Exception ex) {
   }

}

}
