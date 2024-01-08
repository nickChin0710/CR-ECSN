/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-09  V1.00.01  Amber      program initial                            *
* * 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 110-01-05  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
* 111/10/07  V1.00.04  ryan       移除資料類別篩選條件            *
******************************************************************************/

package mktp02;


import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Mktp4205 extends BaseProc {


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
      // insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      // updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      // deleteFunc();
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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 執行 */
      strAction = "S2";
      dataProcess();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {

    String keyType = "";

    wp.whereStr = " where 1=1 ";

//    if (empty(wp.itemStr("ex_key_type")) == false) {
//      wp.whereStr += " and  key_type = :ex_key_type ";
//      setString("ex_key_type", wp.itemStr("ex_key_type"));
//      keyType = wp.itemStr("ex_key_type");
//    }

//    if (keyType.equals("2")) {
      if (empty(wp.itemStr("ex_card_type")) == false) {
        wp.whereStr += " and  key_data like :ex_card_type ";
        setString("ex_card_type", "%" + wp.itemStr("ex_card_type"));

      }
//    }

//    if (keyType.equals("5")) {
      if (empty(wp.itemStr("ex_group_code")) == false) {
        wp.whereStr += " and  key_data like  :ex_group_code ";
        setString("ex_group_code", wp.itemStr("ex_group_code") + "%");
      }
//      if (empty(wp.itemStr("ex_card_type")) == false) {
//        wp.whereStr += " and substrb(key_data,5,2) = :ex_card_type ";
//        setString("ex_card_type", wp.itemStr("ex_card_type"));
//
//      }

//    }



    if (empty(wp.itemStr("ex_crt_user")) == false) {
      wp.whereStr += " and  crt_user = :ex_crt_user ";
      setString("ex_crt_user", wp.itemStr("ex_crt_user"));

    }

    if (empty(wp.itemStr("ex_crt_date")) == false) {
      wp.whereStr += " and  crt_date = :ex_crt_date ";
      setString("ex_crt_date", wp.itemStr("ex_crt_date"));

    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (!getWhereStr()) {
      return;
    } ;
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(rowid) as rowid,mod_seqno ," + "rank_seq, "
        + "(select group_code||'_'||group_name from ptr_group_code where group_code= substrb(key_data,1,4)) as db_group_code,"
        + "(select card_type||'_'||name from ptr_card_type where card_type= substrb(key_data,5,2)) as db_card_type,"
        + "key_data, " + "key_type ," + "crt_user, " + "crt_date, " + "mod_time, " + "mod_seqno";

    wp.daoTable = "mkt_contri_insu_t";
    wp.whereOrder = " order by item_no";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();

    for (int cnt = 0; cnt < wp.dataCnt; cnt++) {


    }

  }

  void listWkdata() throws Exception {

  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_pp_card_no = wp.item_ss("pp_card_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {



    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] opt = wp.itemBuff("opt");
    String[] aaRankSeq = wp.itemBuff("rank_seq");
    String[] aaKeyData = wp.itemBuff("key_data");
    String[] aaKeyType = wp.itemBuff("key_type");
    String[] aaCrtUser = wp.itemBuff("crt_user");
    String[] aaCrtDate = wp.itemBuff("crt_date");

    wp.listCount[0] = aaRowid.length;


    // check
    int rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }


    }

    // save
    // -update-
    rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "ok_flag", "");

      // delete mkt_contri_insu
      String ldsSql2 = " delete  mkt_contri_insu " + " where  rank_seq=:rank_seq "
          + "  and   key_data=:key_data" + "  and   key_type=:key_type";
      setString("rank_seq", aaRankSeq[rr]);
      setString("key_data", aaKeyData[rr]);
      setString("key_type", aaKeyType[rr]);
      setString("crt_user", aaCrtUser[rr]);
      setString("crt_date", aaCrtDate[rr]);
      sqlExec(ldsSql2);

      String insSql = " insert into mkt_contri_insu ( " + "   rank_seq " + " , key_data"
          + " , key_type" + " , crt_user" + " , crt_date" + " , apr_user" + " , apr_date" // 7
          + " , mod_user" + " , mod_time" + " , mod_pgm" + " , mod_seqno" + " ) values ("
          + " ?,?,?,?,?,?" + " ,to_char(sysdate,'yyyymmdd')"// 7
          + " ,?" + " ,timestamp_format(?,'yyyymmddhh24miss')" + " ,?" + " ,1)";
      Object[] param = new Object[] {aaRankSeq[rr], aaKeyData[rr], aaKeyType[rr], aaCrtUser[rr],
          aaCrtDate[rr].replaceAll("\\/", ""), wp.loginUser, wp.loginUser, wp.sysDate + wp.sysTime,
          wp.itemStr("mod_pgm")};

      sqlExec(insSql, param);
      if (sqlRowNum <= 0) {
        wp.colSet(rr, "ok_flag", "!");
        sqlCommit(0);
        alertErr("ERROR: insert mkt_contri_insu ");
        return;
      }


      // delete t
      String ldsSql =
          " delete  mkt_contri_insu_t  where hex(rowid) = :rowid  and  mod_seqno = :mod_seqno ";
      setString("rowid", aaRowid[rr]);
      setString("mod_seqno", aaModSeqno[rr]);
      sqlExec(ldsSql);
      if (sqlRowNum <= 0) {
        wp.colSet(rr, "ok_flag", "!");
        alertErr("ERROR: delete from mkt_contri_insu_t ");
        sqlCommit(0);
        return;
      } else {
        wp.colSet(rr, "ok_flag", "V");
      }

    }
    sqlCommit(1);
    alertMsg("處理完成!");


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
      // dddw_group_code
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_group_code");
      dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1 group by group_code,group_name order by group_code");

      // dddw_card_type
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_card_type");
      dddwList("dddw_card_type", "ptr_card_type", "card_type", "name",
          "where 1=1 group by card_type,name order by card_type");
    } catch (Exception ex) {
    }
  }



}
