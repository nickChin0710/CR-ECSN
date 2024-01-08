/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-18  V1.00.02  Alex        order by                                  *
* 108-11-28  V1.00.01  Alex        fix queryFunc                             *
*109-04-20  V1.00.02  shiyuqi       updated for project coding standard     *
******************************************************************************/

package secm01;

import ofcapp.AppMsg;
import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Secq1010 extends BaseQuery {
  String bultDtime = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();

  }

  @Override
  public void initPage() {
    if (empty(strAction)) {
      wp.colSet("ex_eff_date", wp.sysDate);
      wp.itemSet("ex_eff_date", wp.sysDate);
      try {
        queryFunc();
      } catch (Exception ex) {
      }
      wp.respCode = "00";
      wp.notFound = "";
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1"
        + sqlCol(wp.itemStr("ex_eff_date"), "uf_nvl(eff_date1,to_char(sysdate,'yyyymmdd'))", "<=")
        + sqlCol(wp.itemStr("ex_eff_date"), "uf_nvl(eff_date2,to_char(sysdate,'yyyymmdd'))", ">=");


    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        "bult_dtime, " + "bult_subject," + "eff_date1, " + "eff_date2," + "crt_date," + "crt_user";
    wp.daoTable = "SEC_BULLETIN";
    wp.whereOrder = " order by crt_date Desc ";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    bultDtime = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(bultDtime)) {
      bultDtime = wp.itemStr("bult_dtime");
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "bult_dtime,   " + "bult_subject, "
        + "bult_mesg, " + "eff_date1," + "eff_date2," + "crt_date," + "crt_user," + "mod_user,"
        + "to_char(mod_time,'yyyymmdd') as mod_date," + "mod_pgm," + "mod_seqno";
    wp.daoTable = "SEC_BULLETIN";
    wp.whereStr = " where 1=1" + sqlCol(bultDtime, "bult_dtime");
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + bultDtime);
      return;
    }
  }

}
