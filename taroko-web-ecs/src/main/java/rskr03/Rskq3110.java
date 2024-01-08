package rskr03;
/**
 * 2020-0916   JH    %ex_mcht_addr%
 * */
import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Rskq3110 extends BaseQuery {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   rc = 1;

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "X":
         strAction = "new";
         clearFunc(); break;
      case "Q":
         queryFunc(); break;
      case "R":
         dataRead(); break;
      case "M":
         queryRead(); break;
      case "S":
         querySelect(); break;
      case "L":
         strAction = "";
         clearFunc(); break;
      default:
         alertErr("未指定 actionCode 執行Method, action[%s]",wp.buttonCode);
   }

   dddwSelect();
   initButton();
}

@Override
public void queryFunc() throws Exception {
   if (empty(wp.itemStr("ex_mcht_no"))
         && empty(wp.itemStr("ex_term_id"))
         && empty(wp.itemStr("ex_mcht_addr"))
         && empty(wp.itemStr("ex_ip_addr"))
         && empty(wp.itemStr("ex_mcht_name"))
   ) {
      alertErr("條件不可全部空白");
      return;
   }

   wp.whereStr = " where 1=1"
         + sqlCol(wp.itemStr("ex_mcht_no"), "mcht_no", "like%")
         + sqlCol(wp.itemStr("ex_term_id"), "term_id", "like%")
         + sqlCol(wp.itemStr("ex_mcht_addr"), "mcht_addr", "%like%")
         + sqlCol(wp.itemStr("ex_ip_addr"), "ip_addr", "like%")
         + sqlCol(wp.itemStr("ex_mcht_name"), "mcht_name", "%like%")
   ;

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = " card_no, "
         + " mcht_no, "
         + " term_id, "
         + " mcht_category, "
         + " mcht_name, "
         + " mcht_addr, "
         + " mcht_tel_no, "
         + " arq_bank_no, "
         + " ip_addr "
   ;
   wp.daoTable = " rsk_ctfi_txn";
   wp.whereOrder = " order by mcht_no ";
   pageQuery();


   wp.setListCount(1);
   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }
   wp.setListCount(0);
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
