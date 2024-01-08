/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-5   V1.00.00  Andy       program initial                            *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
* 112-01-06  V1.00.02  Zuwei Su   update gen_memo3 之前, 先確認有資料需update, 否則 不用不update , 也不show message        *
* 112-07-05  V1.00.03  Zuwei Su   "更改後起帳日" 無傳票資料,繼續往後執行        *
******************************************************************************/

package genm01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Genm0140 extends BaseEdit {
  String mBusinessDate;
  String mVouchDate;
  String mIncident;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      strAction = "U";
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "L";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 存檔 */
      strAction = "S2";
      saveFunc();
    }

    // dddw_select();
    initButton();
  }

  @Override
  public void initPage() {
    if (empty(strAction)) {
      wp.pageControl();

      wp.sqlCmd = "select l.business_date, " + "l.vouch_date, "
          + "nvl(substr (r.refno, 1, 3),'') ref, " + "nvl(max (substr (r.refno, 4)),'') ref_cnt "
          + "from ptr_businday l left join gen_vouch r on l.business_date = r.tx_date "
          + "where 1 = 1 " + "group by l.business_date, l.vouch_date,  substr (r.refno, 1, 3) "
          + "order by 3 asc";

      wp.pageCountSql = "select count(*) from (" + wp.sqlCmd + ")";
      pageQuery();

      try {
        wp.setListCount(1);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if (sqlNotFind()) {
        alertErr(appMsg.errCondNodata);
        return;
      }

      wp.totalRows = wp.dataCnt;
      wp.listCount[1] = wp.dataCnt;
      try {
        wp.setPageValue();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  @Override
  public void updateFunc() throws Exception {

  }

  @Override
  public void queryFunc() throws Exception {

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.sqlCmd = "select l.business_date, " + "l.vouch_date,  "
        + "nvl(substr (r.refno, 1, 3),'') ref, " + "nvl(max (substr (r.refno, 4)),'') ref_cnt  "
        + "from ptr_businday l left join gen_vouch r on l.business_date = r.tx_date "
        + "where 1 = 1 " + "group by l.business_date, l.vouch_date,  substr (r.refno, 1, 3) "
        + "order by 3 asc";

    wp.pageCountSql = "select count(*) from (" + wp.sqlCmd + ")";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_digest_cd =wp.item_ss("");
    // dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {
    mIncident = wp.itemStr("incident");
    mBusinessDate = wp.itemStr("business_date");
    mVouchDate = wp.itemStr("vouch_date");
    int llOk = 0, llErr = 0;
    String[] aaRef = wp.itemBuff("ref");
    String[] aaRefCnt = wp.itemBuff("ref_cnt");
    String dsSql = "", usSql = "", isSql = "", lsSql = "";
    wp.listCount[0] = aaRef.length;

    // delete gen_vouch_chg
    dsSql = "delete gen_vouch_chg where 1=1 ";
    dsSql += sqlCol(mBusinessDate, "old_tx_date");
    sqlExec(dsSql);
    if (sqlRowNum < 0) {
      errmsg("資料處理錯誤!!");
      return;
    }

    for (int ll = 0; ll < aaRef.length; ll++) {
      // update gen_vouch
      lsSql = "select count(*) ct  from gen_vouch "
          + "where tx_date =:ls_new_date and refno like :ls_ref||'%' ";
      setString("ls_new_date", mVouchDate);
      setString("ls_ref", aaRef[ll]);
      sqlSelect(lsSql);
      if (sqlInt("ct") > 0) {
        usSql = "update gen_vouch "
            + "set refno  = substr(refno,1,3)||lpad(to_number(substr(refno,4))+:ls_ref_cnt,3,0) "
            + "where tx_date =:ls_new_date and refno like :ls_ref||'%' ";
        setString("ls_ref_cnt", aaRefCnt[ll]);
        setString("ls_new_date", mVouchDate);
        setString("ls_ref", aaRef[ll]);
        sqlExec(usSql);
        if (sqlRowNum <= 0) {
          errmsg("update gen_vouch 資料處理失敗!!");
          return;
        }
      } else {
//        errmsg("gen_vouch無資料!!");
//        return;
      }

      // update gen_memo3
      lsSql = "select count(*) ct  from gen_memo3 "
              + "where tx_date =:ls_new_date and refno like :ls_ref||'%' ";
      setString("ls_new_date", mVouchDate);
      setString("ls_ref", aaRef[ll]);
          sqlSelect(lsSql);
      if (sqlInt("ct") > 0) {
          usSql = "update gen_memo3 "
              + "set refno  = substr(refno,1,3)||lpad(to_number(substr(refno,4))+:ls_ref_cnt,3,0) "
              + "where tx_date =:ls_new_date and refno like :ls_ref||'%' ";
          setString("ls_ref_cnt", aaRefCnt[ll]);
          setString("ls_new_date", mVouchDate);
          setString("ls_ref", aaRef[ll]);
          sqlExec(usSql);
          if (sqlRowNum <= 0) {
            errmsg("update gen_memo3 資料處理失敗!!");
            return;
          }
      }

      // insert gen_vouch_chg
      isSql = "insert into gen_vouch_chg values ( " + ":ls_incident, " + ":ls_old_date, "
          + ":ls_new_date, " + ":ls_ref     , " + ":ls_ref_cnt , " + ":ls_mod_user, "
          + "sysdate     , " + "'genm0140'    " + ") ";
      setString("ls_incident", mIncident);
      setString("ls_old_date", mBusinessDate);
      setString("ls_new_date", mVouchDate);
      setString("ls_ref", aaRef[ll]);
      setString("ls_ref_cnt", aaRefCnt[ll]);
      setString("ls_mod_user", wp.loginUser);
      sqlExec(isSql);
      if (sqlRowNum <= 0) {
        errmsg("insert gen_vouch_chg 資料處理失敗!!");
        return;
      }
      // System.out.println("insert gen_vouch_chg OK!!");
    }
    // update gen_vouch2
    usSql = "UPDATE gen_vouch " + "SET tx_date = :ls_new_date " + "WHERE tx_date = :ls_old_date ";
    setString("ls_new_date", mVouchDate);
    setString("ls_old_date", mBusinessDate);
    sqlExec(usSql);
    if (sqlRowNum <= 0) {
      errmsg("update gen_vouch2 資料處理失敗!!");
      return;
    }

    // update gen_memo3
    lsSql = "select count(*) ct  from gen_memo3 "
            + "WHERE tx_date = :ls_old_date";
    setString("ls_old_date", mBusinessDate);
    sqlSelect(lsSql);
    if (sqlInt("ct") > 0) {
        usSql = "UPDATE gen_memo3 " 
                + "SET tx_date = :ls_new_date " 
                + "WHERE tx_date = :ls_old_date";
        setString("ls_new_date", mVouchDate);
        setString("ls_old_date", mBusinessDate);
        sqlExec(usSql);
    }

    if (sqlRowNum <= 0) {
      llErr++;
    } else {
      llOk++;
      sqlCommit(1);
    }
    alertMsg("資料修改: 成功=" + llOk + " 失敗=" + llErr);

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      // this.dddw_list("dddw_digest_cd", "gen_digest", "digest_cd", "", "where 1=1 group by
      // digest_cd order by digest_cd");
      // this.dddw_list("dddw_group_abbr_code", "ptr_group_code",
      // "group_abbr_code", "", "where 1=1 group by group_abbr_code order
      // by group_abbr_code");
      // 提供Detel頁下拉指標到...
      // wp.optionKey = wp.col_ss("bill_form");
      // this.dddw_list("dddw_bill_form", "cyc_bill_form", "bill_form",
      // "bill_form_name", "where 1=1 order by bill_form");
    } catch (Exception ex) {
    }
  }

}
