package rskr03;
/**
 * 2019-1224:  Alex  fix queryWhere order by
 * 2019-1224:  Alex  fix queryWhere
 * 2019-1223:  Alex  order by fix
 * 2019-0624:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Rskq3130 extends BaseQuery {

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
public void initPage() {
   try {
      wp.colSet("ex_day_1", "0");
      wp.colSet("ex_day_2", "7");
//			wp.colSet("ex_mm_4", "1");
      wp.colSet("ex_day_3", "0");
   }
   catch (Exception ex) {
   }
}

@Override
public void queryFunc() throws Exception {
   if (wp.itemEq("ex_type", "1")) {
      if (empty(wp.itemStr("ex_day_1")) || wp.itemEq("ex_day_1", "0")) {
         alertErr("N 不可空白,不可等於 0");
         return;
      }
   }

   if (wp.itemEq("ex_type", "2")) {
      if (empty(wp.itemStr("ex_day_2")) || wp.itemEq("ex_day_2", "0")) {
         alertErr("X 不可空白,不可等於 0");
         return;
      }
   }

   if (wp.itemEq("ex_type", "4")) {
      if (empty(wp.itemStr("ex_day_3")) || wp.itemEq("ex_day_3", "0")) {
         alertErr("Y 不可空白,不可等於 0");
         return;
      }
   }

   if (chkStrend(wp.itemStr("ex_case_date1"), wp.itemStr("ex_case_date2")) == false) {
      alertErr("立案日期起迄錯誤");
      return;
   }

   wp.whereStr = " where 1=1"
         + sqlCol(wp.itemStr("ex_case_date1"), "case_date", ">=")
         + sqlCol(wp.itemStr("ex_case_date2"), "case_date", "<=")
   ;
   if (wp.itemEq("ex_type", "1")) {
      wp.whereStr += " and (select max(call_date) from rsk_ctfi_call_log where card_no = A.card_no ) < to_char(current date - " + wp.itemNum("ex_day_1") + " days,'yyyymmdd') ";
      wp.whereStr += " and survey_close <> 'Y' ";
      wp.whereStr += " and case_close_flag <> 'Y' ";
   }
   if (wp.itemEq("ex_type", "2")) {
      wp.whereStr += " and survey_user <>'' and nvl(survey_ing,'N')<>'Y' ";
      wp.whereStr += " and case_date <= decode(survey_date1,'',to_char(current date - " + wp.itemNum("ex_day_2") + " days,'yyyymmdd'),uf_date_add(survey_date1,0,0,-" + wp.itemNum("ex_day_2") + ")) "
            + " and case_close_flag <> 'Y' ";
   }
   if (wp.itemEq("ex_type", "3")) {
      wp.whereStr += " and debit_card_date <>'' and case_close_flag<>'Y' ";
   }
   if (wp.itemEq("ex_type", "4")) {
      //--續轉法務有打勾時不顯示查詢資料，無打勾/僅輸入日期，需顯示資料
      wp.whereStr += " and survey_date8 <> '' and turn_legal_flag <> 'Y' "
            + " and uf_month_between(to_char(sysdate,'yyyymmdd'),survey_date8) >= " + wp.itemNum("ex_day_3");
   }

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = " A.card_no, "
         + " A.case_date, "
         + " A.case_seqno, "
         + " A.case_type, "
         + " A.case_source, "
         + " A.fraud_ok_cnt, "
         + " A.fraud_ok_amt, "
         + " A.survey_user,"
         + " A.debit_card_date,"
         + " uf_card_name(A.card_no) as db_id_name,"
         + " uf_idno_id2(A.card_no,'') as id_no , "
         + " '' as db_log_date"
   ;
   wp.daoTable = "rsk_ctfi_case A";
   wp.whereOrder += " order by A.case_seqno Asc ";
   pageQuery();

   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }

   wp.setListCount(1);
   wp.setPageValue();
   queryAfter();
}

void queryAfter() throws Exception  {
   int il_select_cnt = 0;
   il_select_cnt = wp.selectCnt;

   //--最近敘實日
   String sql1 = " select max(call_date) as call_date from rsk_ctfi_call_log where card_no = ? ";


   for (int ii = 0; ii < il_select_cnt; ii++) {
      sqlSelect(sql1, new Object[]{wp.colStr(ii, "card_no")});
      if (sqlRowNum > 0) {
         wp.colSet(ii, "db_log_date", sqlStr("call_date"));
      }
   }


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
