package rskr03;
/**
 * 2019-0624:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Rskq3120 extends BaseQuery {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   rc = 1;

   strAction = wp.buttonCode;
   //ddd("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
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
}

@Override
public void queryFunc() throws Exception {
   wp.whereStr = " where 1=1"
         + sqlCol(wp.itemStr("ex_fr_name"), "B.frman_name", "like%")
         + sqlCol(wp.itemStr("ex_fr_idno"), "B.frman_idno")
         + sqlCol(wp.itemStr("ex_card_no"), "A.card_no")
   ;
   if (wp.itemEmpty("ex_idno") == false) {
      wp.whereStr += " and A.id_p_seqno in "
            + " (select id_p_seqno from crd_idno where 1=1 " + sqlCol(wp.itemStr("ex_idno"), "id_no")
            + " union select id_p_seqno from dbc_idno where 1=1 " + sqlCol(wp.itemStr("ex_idno"), "id_no")
            + " )";
   }
   if (wp.itemEmpty("ex_id_name") == false) {
      wp.whereStr += " and A.card_no in "
            + " (select C.card_no from crd_card C join crd_idno D on C.id_p_seqno = D.id_p_seqno where 1=1 " + sqlCol(wp.itemStr("ex_id_name"), "D.chi_name", "like%")
            + " union select E.card_no from dbc_card E join dbc_idno F on E.id_p_seqno = F.id_p_seqno where 1=1 " + sqlCol(wp.itemStr("ex_id_name"), "F.chi_name", "like%")
            + " )";
   }


   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = " A.card_no, "
         + " uf_idno_id(A.id_p_seqno) as id_no, "
         + " A.case_type, "
         + " A.fraud_ok_amt, "
         + " A.survey_user, "
         + " A.case_close_flag, "
         + " uf_card_name(A.card_no) as db_id_cname, "
         + " A.case_seqno, "
         + " B.frman_name ,"
         + " B.frman_idno ,"
         + " B.frman_birdate ,"
         + " B.fr_remark "
   ;
   wp.daoTable = "rsk_ctfi_case A join rsk_ctfi_frman B on A.card_no=B.card_no";
   wp.whereOrder = " order by A.card_no ";
   pageQuery();


   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }
   wp.setListCount(1);
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

}
