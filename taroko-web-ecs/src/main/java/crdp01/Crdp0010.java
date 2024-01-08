/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-11  V1.00.00  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-06-25  V1.00.02  Andy		  update : add  del_empty_warehouse()        *
* 109-04-28  V1.00.03  YangFang   updated for project coding standard        *
******************************************************************************/
package crdp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Crdp0010 extends BaseProc {
  Crdp0010Func func;
  int rr = -1;
  String msg = "", inQty = "", outQty = "";
  String lsKey1 = "", lsKey2 = "", lsKey3 = "", lsNextYY = "";
  int ilOk = 0;
  int ilErr = 0;
  double ldQty = 0, ldTmp = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-

      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("ex_year", wp.sysDate.substring(0, 4));
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "全部";
      wp.optionKey = wp.itemStr("ex_ncccid");
      dddwList("dddw_group_card",
          " ptr_group_card as a right join ptr_group_card_dtl as b on a.card_type = b.card_type ",
          " b.unit_code||a.card_type",
          " '&nbsp;&nbsp;'||a.card_type||'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'||a.group_code||'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'||b.unit_code",
          " where 1=1  group by a.card_type,a.group_code,b.unit_code  order by a.card_type");

      wp.optionKey = wp.itemStr("ex_place");
      this.dddwList("dddw_place", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'WH_LOC' ");

    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_year"))) {
      alertErr2("結算年度不能為空");
      return;
    }
    wp.whereStr = " where 1=1 and wh_year = :ex_year ";
    setString("ex_year", wp.itemStr("ex_year"));

    if (!empty(wp.itemStr("ex_place"))) {
      wp.whereStr += " and place = :ex_place ";
      setString("ex_place", wp.itemStr("ex_place"));
    }
    if (empty(wp.itemStr("ex_ncccid")) == false) {
      wp.whereStr += " and card_item = :ex_ncccid ";
      setString("ex_ncccid", wp.itemStr("ex_ncccid"));

    }

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL =
        " hex(rowid) as rowid, " + " wh_year, " + " place, " + " card_item, " + " mod_seqno ";

    wp.daoTable = "crd_warehouse";

    wp.whereOrder = "";
    System.out.println("select " + wp.selectSQL + " from " + wp.daoTable + wp.queryWhere);
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    rc = delEmptyWarehouse(); // 刪除全年度結餘數為0的資料 20180625 add
    sqlCommit(rc);

    queryFunc();
    func = new Crdp0010Func(wp);
    lsKey1 = wp.itemStr("ex_year");
    lsNextYY = String.format("%04d", (this.toInt(lsKey1) + 1));

    for (int i = 1; i <= 12; i++) {
      inQty += " ,in_qty" + String.format("%02d", i);// in_qty00~in_qty12
      outQty += " ,out_qty" + String.format("%02d", i);// out_qty00~out_qty12
    }
    // -update-

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      ilErr = 1;

      func.varsSet("aa_card_item", wp.colStr(ii, "card_item"));
      func.varsSet("aa_place", wp.colStr(ii, "place"));
      lsKey2 = wp.colStr(ii, "card_item");
      lsKey3 = wp.colStr(ii, "place");

      String sql =
          "select pre_total" + inQty + outQty + " from crd_warehouse where wh_year = :ls_key1 "
              + " and card_item = :ls_key2 and place = :ls_key3 ";
      setString("ls_key1", lsKey1);
      setString("ls_key2", lsKey2);
      setString("ls_key3", lsKey3);
      sqlSelect(sql);
      if (sqlRowNum == 0) {
        continue;
      }
      if (empty(sqlStr(0, "pre_total"))) {
        ldQty = 0;
      } else
        ldQty = sqlNum(0, "pre_total");
      for (int i = 1; i <= 12; i++) {
        if (!empty(sqlStr(0, "in_qty" + String.format("%02d", i)))) {
          ldTmp = sqlNum(0, "in_qty" + String.format("%02d", i));
          ldQty += ldTmp;
        }
        if (!empty(sqlStr(0, "out_qty" + String.format("%02d", i)))) {
          ldTmp = sqlNum(0, "out_qty" + String.format("%02d", i));
          ldQty -= ldTmp;
        }
      }
      String sql2 = "select wh_year,card_item,place,pre_total,mod_seqno " + " from crd_warehouse "
          + " where wh_year = :ls_nextYY " + " and  card_item = :ls_key2 and place = :ls_key3 ";
      setString("ls_nextYY", lsNextYY);
      setString("ls_key2", lsKey2);
      setString("ls_key3", lsKey3);
      sqlSelect(sql2);

      if (sqlRowNum == 0) {
        func.varsSet("ls_nextYY", lsNextYY);
        func.varsSet("ls_key2", lsKey2);
        func.varsSet("ls_key3", lsKey3);
        func.varsSet("ld_qty", ldQty + "");
        func.varsSet("mod_pgm", sqlStr(0, "mod_pgm"));
        ilErr = func.dataProc();
        sqlCommit(ilErr);

        if (ilErr != 1) {
          return;
        }
      } else {
        func.varsSet("ls_nextYY", lsNextYY);
        func.varsSet("ls_key2", lsKey2);
        func.varsSet("ls_key3", lsKey3);
        func.varsSet("ld_qty", ldQty + "");
        func.varsSet("mod_seqno", sqlStr(0, "mod_seqno"));
        ilErr = func.updateFunc();
        sqlCommit(ilErr);
        if (ilErr != 1) {
          return;
        }
      }

    }
    alertMsg("資料處理完成");
  }

  public int delEmptyWarehouse() {
    // 刪除當年度無結餘資料
    String lsSql = "", dsSql = "";
    int rowCt = 0;
    lsSql = "SELECT hex(rowid) as rowid," + "pre_total," + "(in_qty01 - out_qty01 "
        + "+ in_qty02 - out_qty02 " + "+ in_qty03 - out_qty03 " + "+ in_qty04 - out_qty04 "
        + "+ in_qty05 - out_qty05 " + "+ in_qty06 - out_qty06 " + "+ in_qty07 - out_qty07 "
        + "+ in_qty08 - out_qty08 " + "+ in_qty09 - out_qty09 " + "+ in_qty10 - out_qty10 "
        + "+ in_qty11 - out_qty11 " + "+ in_qty12 - out_qty12 ) as total " + "FROM crd_warehouse "
        + "WHERE 1=1 ";
    lsSql += sqlCol(wp.itemStr("ex_year"), "wh_year");
    if (!wp.itemStr("ex_place").equals("0")) {
      lsSql += sqlCol(wp.itemStr("ex_place"), "place");
    }
    lsSql += sqlCol(wp.itemStr("ex_ncccid"), "card_item");
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      return 1;
    }
    rowCt = sqlRowNum;
    for (int i = 0; i < rowCt; i++) {
      if (sqlNum(i, "pre_total") > 0) {
        continue; // 上年度有結存數
      } else {
        if (sqlNum(i, "total") == 0) {
          dsSql = "delete crd_warehouse where hex(rowid) =:rowid ";
          setString("rowid", sqlStr(i, "rowid"));
          sqlExec(dsSql);
          if (sqlRowNum < 0) {
            return -1;
          }
        }
      }
    }
    return 1;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
