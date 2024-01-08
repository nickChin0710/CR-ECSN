/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-24  V1.00.00  Andy       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-05-30  V1.00.02  Andy		  update : UI, bug                           * 
* 107-06-25  V1.00.03  Andy		  update : add new process:                  *
*                                  table : crd_wtrans,crd_wharehouse,        *
*                                          crd_whtx_dtl                      *
* 107-08-20  V1.00.04  Andy       Update :bug                                *  
* 108-12-23  v1.00.05  Andy       Update :Fix bug                            *  
* 108-12-31	 V1.00.06  Andy       update : crd_nccc_card => crd_card_item    * 
* 109-01-02	 V1.00.07  Andy       update : UI                                * 
* 109-04-28  V1.00.08  YangFang   updated for project coding standard        *	
 * 109-12-30  V1.00.09  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/

package crdm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0010 extends BaseEdit {
  String mWarehouseNo = "";
  String gsPlace = "";
  String gsCardItem = "";
  String gsWhDate = "";
  String gsWhYear = "";
  String gsWhMonth = "";
  String gsLotNo = "";
  String gsTransType = "";
  String gsTransReason = "";
  String gsCardType = "";
  String gsUnitCode = "";
  double gsPrevTotal = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
      setAddNo();
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
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
      strAction = "new";
      clearFunc();
      setAddNo();
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
    } else if (eqIgno(wp.buttonCode, "I")) {
      /* 清畫面 */
      itemChange();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {

    wp.colSet("exDateS", getSysDate());
    if (wp.respHtml.indexOf("_detl") > 0) {
      wp.colSet("warehouse_date", getSysDate());
    }

  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    String exWarehouseNo = wp.itemStr("ex_warehouse_no");
    String exCardItem = wp.itemStr("ex_card_item");
    String exPlace = wp.itemStr("ex_place");
    String exTnsType = wp.itemStr("ex_tns_type");
    String exTransReason1 = wp.itemStr("ex_trans_reason1");
    String exTransReason2 = wp.itemStr("ex_trans_reason2");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");

    wp.whereStr = " where 1=1 ";
    // 異動編號
    if (empty(exWarehouseNo) == false) {
      wp.whereStr += sqlCol(exWarehouseNo, "a.warehouse_no");
    }
    // 卡樣代碼
    if (empty(exCardItem) == false) {
      wp.whereStr += sqlCol(exCardItem, "a.card_item");
    }
    // 庫存地方
    if (exPlace.equals("0") == false) {
      wp.whereStr += sqlCol(exPlace, "a.place");
    }
    // 庫存種類
    if (exTnsType.equals("0") == false) {
      wp.whereStr += sqlCol(exTnsType, "a.tns_type");
    }
    // 出入庫原因
    switch (exTnsType) {
      case "1":
        if (empty(exTransReason1) == false) {
          wp.whereStr += sqlCol(exTransReason1, "a.trans_reason");
        }
        break;
      case "2":
        if (empty(exTransReason2) == false) {
          wp.whereStr += sqlCol(exTransReason2, "a.trans_reason");
        }
        break;
    }
    // 起帳日期
    wp.whereStr += sqlStrend(exDateS, exDateE, "a.warehouse_date");

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    if (getWhereStr() == false)
      return;
    wp.pageControl();

    wp.selectSQL = "" + "a.warehouse_no, " + "a.card_item, "
        + "(select name from crd_card_item where card_item = a.card_item ) db_card_item_name, "
        + "a.warehouse_date, " + "a.card_type, " + "a.unit_code, " + "a.tns_type, " + "a.place, "
        + "a.prev_total, " + "a.use_total, " + "a.crt_date, " + "a.mod_user, " + "a.mod_time, "
        + "a.mod_pgm, " + "a.mod_seqno, " + "a.crt_user, " + "a.trans_reason, "
        + "nvl(b.ic_flag,'N') ic_flag, " + "a.item_amt ";
    wp.daoTable = "crd_whtrans a left join crd_item_unit b on  a.card_item = b.card_item ";
    wp.whereOrder = " order by a.warehouse_no";
//    getWhereStr();
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    String lsSql = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      wp.colSet(ii, "group_ct", "1");

      // db_place庫位
      String place = wp.colStr(ii, "place");
      String[] cde = new String[] {"1", "2", "3"};
      String[] txt = new String[] {"1.本行", "2.台銘", "3.宏通"};
      wp.colSet(ii, "db_place", commString.decode(place, cde, txt));

      // db_tns_type庫存種類
      String tnsType = wp.colStr(ii, "tns_type");
      String[] cde1 = new String[] {"1", "2"};
      String[] txt1 = new String[] {"1.入庫", "2.出庫"};
      wp.colSet(ii, "db_tns_type", commString.decode(tnsType, cde1, txt1));

      // db_trans_reason處理結果
      String wkTransReason = wp.colStr(ii, "trans_reason");
      String dbTransReason = "";
      if (!empty(wkTransReason)) {
        switch (tnsType) {
          case "1":
            lsSql = "select wf_type,wf_id,wf_desc from ptr_sys_idtab where wf_type = 'TRNS_IN_RSN'";
            break;
          case "2":
            lsSql = "select wf_type,wf_id,wf_desc from ptr_sys_idtab where wf_type = 'TRNS_RSN1'";
            break;
        }
        lsSql += sqlCol(wp.colStr(ii, "trans_reason"), "wf_id");
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          dbTransReason = wp.colStr(ii, "trans_reason") + "[" + sqlStr("wf_desc") + "]";
        }
      }
      wp.colSet(ii, "db_trans_reason", dbTransReason);
    }
    wp.colSet("row_ct", intToStr(rowCt));
  }

  @Override
  public void querySelect() throws Exception {
    mWarehouseNo = wp.itemStr("warehouse_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mWarehouseNo = itemKk("data_k1");

    wp.selectSQL = "hex(a.rowid) as rowid, a.mod_seqno, " + "a.warehouse_no, " + "a.lot_no, "
        + "a.lot_no as kk_lot_no, " + "a.card_item, " + "a.warehouse_date, " + "a.card_type, "
        + "a.unit_code, " + "a.tns_type, " + "a.place, " + "a.prev_total, " + "a.use_total, "
        + "a.crt_date, " + "a.mod_user, " + "a.mod_time, " + "a.mod_pgm, " + "a.mod_seqno, "
        + "a.crt_user, " + "a.trans_reason, "
        + "decode(a.tns_type,'1',a.trans_reason,'') as trans_reason1, "
        + "decode(a.tns_type,'2',a.trans_reason,'') as trans_reason2, "
        + "nvl(b.ic_flag,'N') ic_flag, " + "a.item_amt ";
    wp.daoTable = "crd_whtrans a left join crd_item_unit b on  a.card_item = b.card_item ";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  a.warehouse_no = :m_warehouse_no ";
    setString("m_warehouse_no", mWarehouseNo);

    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable
    // + wp.whereStr + wp.whereOrder);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無此資料, warehouse_no=" + mWarehouseNo);
    }
  }

  @Override
  public void insertFunc() throws Exception {
    // 新增完成直接賦予新編號
    saveFunc();
    if (rc == 1 && userAction == false) {
      if (addRetrieve)
        dataRead();
      else {
        clearFunc();
        setAddNo();
      }
    }
  }

  @Override
  public void saveFunc() throws Exception {
    gsPlace = wp.itemStr("place");
    gsCardItem = wp.itemStr("card_item");
    gsWhDate = wp.itemStr("warehouse_date");
    gsWhYear = strMid(wp.itemStr("warehouse_date"), 0, 4);
    gsWhMonth = strMid(wp.itemStr("warehouse_date"), 4, 2);
    gsLotNo = wp.itemStr("lot_no");
    gsTransType = wp.itemStr("tns_type");
    if (gsTransType.equals("1")) {
      gsTransReason = wp.itemStr("trans_reason1");
    } else {
      gsTransReason = wp.itemStr("trans_reason2");
    }

    String lsSql = "";

    if (ofValidation() != 1) {
      return;
    }

    Crdm0010Func func = new Crdm0010Func(wp);
    // 判斷crd_tx_bal是否已存在
    lsSql = "select count(*)ct  from crd_tx_bal where 1=1 ";
    lsSql += sqlCol(gsWhDate, "tx_date");
    lsSql += sqlCol(gsCardItem, "card_item");
    lsSql += sqlCol(gsPlace, "place");
    sqlSelect(lsSql);
    if (sqlNum("ct") == 0) {
      func.varsSet("crd_tx_bal_flag", "N");
    }
    if (sqlNum("ct") > 0) {
      func.varsSet("crd_tx_bal_flag", "Y");
    }

    func.varsSet("aa_prev_total", gsPrevTotal + "");
    func.varsSet("aa_card_type", gsCardType); // 20200102 add
    func.varsSet("aa_unit_code", gsUnitCode); // 20200102 add
    rc = func.dbSave(strAction); // 針對crd_wtrans之A,U,D
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }

    if (strAction.equals("D")) {
      if (gsTransType.equals("1")) {
        backCrdWarehouse1();
      } else {
        backCrdWarehouse2();
      }
      return;
    }
    // 出庫 update crd_warehouse & insert crd_whtx_dtl
    if (gsTransType.equals("2")) {
      lsSql = "select count(*) as ct from crd_warehouse " + "where wh_year =:wh_year "
          + "and card_item =:card_item " + "and place =:place ";
      setString("wh_year", gsWhYear);
      setString("card_item", gsCardItem);
      setString("place", gsPlace);
      sqlSelect(lsSql);
      if (sqlNum("ct") == 0) {
        rc = func.dbInsert2();
        this.sqlCommit(rc);
      }
      rc = updateCrdWarehouse();
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    }

    this.sqlCommit(rc);
  }

  public int ofValidation() {
    int wkUseTotal = 0;
    String lsSql = "";
    if (strAction.equals("A") || strAction.equals("U")) {
      lsSql = "select distinct card_type,unit_code from crd_item_unit "
          + "where card_item =:card_item ";
      setString("card_item", wp.itemStr("card_item"));
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        gsCardType = sqlStr("card_type");
        gsUnitCode = sqlStr("unit_code");
      } else {
        alertErr("查無卡樣資料檔!!");
        return -1;
      }
    }

    if (strAction.equals("D")) {
      if (wfChkDelete() != 1) {
        return -1;
      }
      return 1;
    }
    // 出入庫數量
    wkUseTotal = Integer.parseInt(wp.itemStr("use_total"));
    if (wkUseTotal < 0) {
      alertErr2("出入庫數量不可小於0");
      return -1;
    }

    // 入庫單價不能為0
    if (wp.itemStr("tns_type").equals("1") && wp.itemStr("trans_reason1").equals("2")) {
      if (wp.itemNum("item_amt") <= 0) {
        alertErr2("新購卡入庫單價不可空值或小於或等於0!!");
        return -1;
      }

    }

    // 庫存 v.s 出庫
    ofGetCurqty();
    if (wp.itemStr("tns_type").equals("1"))
      return 1; // 入庫不做檢核
    if (gsPrevTotal <= 0) {
      alertErr2("庫存數量不足!!");
      return -1;
    }
    if (wkUseTotal > gsPrevTotal) {
      alertErr2("出庫數量大於庫存數量!!");
      return -1;
    }
    return 1;
  }

  public int wfChkDelete() {
    String wkTnsType = "";
    int wkUseTotal = 0;
    gsPlace = wp.itemStr("place");
    gsCardItem = wp.itemStr("card_item");

    wkTnsType = wp.itemStr("tns_type");
    if (!wkTnsType.equals("1")) {
      return 1;
    }
    wkUseTotal = Integer.parseInt(wp.itemStr("use_total"));
    ofGetCurqty();
    if (wkUseTotal > gsPrevTotal) {
      alertErr2("刪除之數量大於現有庫存量");
      return -1;
    }
    return 1;
  }

  // 計算庫存
  public int ofGetCurqty() {
    String lsSql = "", whYear = "", sysYear = "";
    // 當年結crdp0010執行後這段SQL搜出來的年度會有衝突
    lsSql = "select max(wh_year) as wh_year " + "from crd_warehouse "
        + "where card_item = :as_citem " + "and place = :as_place ";
    setString("as_citem", gsCardItem);
    setString("as_place", gsPlace);
    sqlSelect(lsSql);

    if (sqlRowNum <= 0) {
      return 0;
    } else {
      if (!empty(sqlStr("wh_year"))) {
        whYear = sqlStr("wh_year");
        sysYear = strMid(getSysDate(), 0, 4);
        if (whYear.compareTo(sysYear) == 1) {
          whYear = sysYear;
        }
      } else {
        return 0;
      }
    }
    lsSql = "SELECT sum(pre_total + in_qty01 - out_qty01 " + "+ in_qty02 - out_qty02 "
        + "+ in_qty03 - out_qty03 " + "+ in_qty04 - out_qty04 " + "+ in_qty05 - out_qty05 "
        + "+ in_qty06 - out_qty06 " + "+ in_qty07 - out_qty07 " + "+ in_qty08 - out_qty08 "
        + "+ in_qty09 - out_qty09 " + "+ in_qty10 - out_qty10 " + "+ in_qty11 - out_qty11 "
        + "+ in_qty12 - out_qty12 ) as prev_total " + "FROM crd_warehouse "
        + "WHERE wh_year = :ls_yy  AND " + "card_item = :as_citem AND " + "place = :as_place ";
    setString("ls_yy", whYear);
    setString("as_citem", gsCardItem);
    setString("as_place", gsPlace);
    sqlSelect(lsSql);

    if (sqlRowNum <= 0) {
      return 0;
    } else {
      gsPrevTotal = sqlNum("prev_total");
    }

    return 1;
  }

  public void setAddNo() {
    String lsSql = "";
    String warehouseNo = "";
    // warehouse_no異動編號
    lsSql = "SELECT nvl (to_char (to_number (max (warehouse_no)) + 1),( select (to_number(to_char(sysdate,'YYYY')) - 1911) || '00001' FROM DUAL)) as warehouse_no from crd_whtrans";
    sqlSelect(lsSql);
    if(sqlRowNum>0) {
    	warehouseNo = sqlStr("warehouse_no");
    }
    wp.colSet("warehouse_no", warehouseNo);

    // lot_no 日期+流水號 //20180820 批號只有入庫且原因為新購時才賦予
    lsSql =
        "SELECT :ls_warehouse_date || substr(to_char(to_number(nvl(substr(max(lot_no),9,2),'0'))+1 , '00'),2,2) as lot_no "
            + "FROM crd_whtrans " + "where lot_no like :ls_warehouse_date||'%' ";
    setString("ls_warehouse_date", getSysDate());
    sqlSelect(lsSql);
    wp.colSet("lot_no", sqlStr("lot_no"));
    // crt_date起帳日期
    wp.colSet("crt_date", getSysDate());
    wp.colSet("wh_year", strMid(getSysDate(), 0, 4));
    wp.colSet("wh_month", strMid(getSysDate(), 4, 2));
  }

  public int updateCrdWarehouse() {
    // 出庫 : 異動 crd_warehouse
    double hWareOutQty01 = 0, hWareOutQty02 = 0, hWareOutQty03 = 0, hWareOutQty04 = 0,
        hWareOutQty05 = 0, hWareOutQty06 = 0, hWareOutQty07 = 0, hWareOutQty08 = 0,
        hWareOutQty09 = 0, hWareOutQty10 = 0, hWareOutQty11 = 0, hWareOutQty12 = 0,
        hPreTotalBal = 0, hWareItemAmt = 0, wkUseTotal = 0, tmpUseTotal = 0;
    String hWareRowid = "";
    String hWareLotNo = "";
    String lsSql = "";
    Crdm0010Func func = new Crdm0010Func(wp);

    lsSql = "select out_qty01  , out_qty02   , out_qty03   , out_qty04   ,"
        + "out_qty05  , out_qty06   , out_qty07   , out_qty08   ,"
        + "out_qty09  , out_qty10   , out_qty11   , out_qty12   ," + "pre_total   "
        + "  + ( in_qty01+ in_qty02+ in_qty03+ in_qty04+ in_qty05+ in_qty06 + "
        + "      in_qty07+ in_qty08+ in_qty09+ in_qty10+ in_qty11+ in_qty12)  "
        + "  - (out_qty01+out_qty02+out_qty03+out_qty04+out_qty05+out_qty06 + "
        + "     out_qty07+out_qty08+out_qty09+out_qty10+out_qty11+out_qty12) h_pre_total_bal,"
        + "item_amt   , lot_no      , hex(rowid) as rowid   " + "from crd_warehouse "
        + "where wh_year =:wh_year " + "and card_item =:card_item " + "and place =:place "
        + "order by lot_no ";
    setString("wh_year", gsWhYear);
    setString("card_item", gsCardItem);
    setString("place", gsPlace);
    sqlSelect(lsSql);
    int rowCt = sqlRowNum;
    wkUseTotal = wp.itemNum("use_total");
    tmpUseTotal = wkUseTotal;
    for (int i = 0; i < rowCt; i++) {
      hWareLotNo = sqlStr(i, "lot_no");
      hWareOutQty01 = sqlNum(i, "out_qty01");
      hWareOutQty02 = sqlNum(i, "out_qty02");
      hWareOutQty03 = sqlNum(i, "out_qty03");
      hWareOutQty04 = sqlNum(i, "out_qty04");
      hWareOutQty05 = sqlNum(i, "out_qty05");
      hWareOutQty06 = sqlNum(i, "out_qty06");
      hWareOutQty07 = sqlNum(i, "out_qty07");
      hWareOutQty08 = sqlNum(i, "out_qty08");
      hWareOutQty09 = sqlNum(i, "out_qty09");
      hWareOutQty10 = sqlNum(i, "out_qty10");
      hWareOutQty11 = sqlNum(i, "out_qty11");
      hWareOutQty12 = sqlNum(i, "out_qty12");
      hWareItemAmt = sqlNum(i, "item_amt");
      hPreTotalBal = sqlNum(i, "h_pre_total_bal"); // 該批號庫存數
      // System.out.println("h_pre_total_bal : "+i+" "+h_pre_total_bal);
      hWareRowid = sqlStr(i, "rowid");
      if (tmpUseTotal == 0) { // 出庫數已歸0
        return 1;
      }
      if (hPreTotalBal == 0) { // 當前期庫存+入庫-出庫為0時換下一筆
        continue;
      }
      if (hPreTotalBal >= tmpUseTotal) {
        switch (gsWhMonth) {
          case "01":
            hWareOutQty01 += tmpUseTotal;
            break;
          case "02":
            hWareOutQty02 += tmpUseTotal;
            break;
          case "03":
            hWareOutQty03 += tmpUseTotal;
            break;
          case "04":
            hWareOutQty04 += tmpUseTotal;
            break;
          case "05":
            hWareOutQty05 += tmpUseTotal;
            break;
          case "06":
            hWareOutQty06 += tmpUseTotal;
            break;
          case "07":
            hWareOutQty07 += tmpUseTotal;
            break;
          case "08":
            hWareOutQty08 += tmpUseTotal;
            break;
          case "09":
            hWareOutQty09 += tmpUseTotal;
            break;
          case "10":
            hWareOutQty10 += tmpUseTotal;
            break;
          case "11":
            hWareOutQty11 += tmpUseTotal;
            break;
          case "12":
            hWareOutQty12 += tmpUseTotal;
            break;
        }
      }
      if (hPreTotalBal < tmpUseTotal) {
        switch (gsWhMonth) {
          case "01":
            hWareOutQty01 += hPreTotalBal;
            break;
          case "02":
            hWareOutQty02 += hPreTotalBal;
            break;
          case "03":
            hWareOutQty03 += hPreTotalBal;
            break;
          case "04":
            hWareOutQty04 += hPreTotalBal;
            break;
          case "05":
            hWareOutQty05 += hPreTotalBal;
            break;
          case "06":
            hWareOutQty06 += hPreTotalBal;
            break;
          case "07":
            hWareOutQty07 += hPreTotalBal;
            break;
          case "08":
            hWareOutQty08 += hPreTotalBal;
            break;
          case "09":
            hWareOutQty09 += hPreTotalBal;
            break;
          case "10":
            hWareOutQty10 += hPreTotalBal;
            break;
          case "11":
            hWareOutQty11 += hPreTotalBal;
            break;
          case "12":
            hWareOutQty12 += hPreTotalBal;
            break;
        }
      }
      func.varsSet("h_ware_out_qty01", hWareOutQty01 + "");
      func.varsSet("h_ware_out_qty02", hWareOutQty02 + "");
      func.varsSet("h_ware_out_qty03", hWareOutQty03 + "");
      func.varsSet("h_ware_out_qty04", hWareOutQty04 + "");
      func.varsSet("h_ware_out_qty05", hWareOutQty05 + "");
      func.varsSet("h_ware_out_qty06", hWareOutQty06 + "");
      func.varsSet("h_ware_out_qty07", hWareOutQty07 + "");
      func.varsSet("h_ware_out_qty08", hWareOutQty08 + "");
      func.varsSet("h_ware_out_qty09", hWareOutQty09 + "");
      func.varsSet("h_ware_out_qty10", hWareOutQty10 + "");
      func.varsSet("h_ware_out_qty11", hWareOutQty11 + "");
      func.varsSet("h_ware_out_qty12", hWareOutQty12 + "");
      func.varsSet("h_ware_rowid", hWareRowid);
      func.varsSet("h_ware_lot_no", hWareLotNo);
      func.varsSet("tmp_use_total", tmpUseTotal + "");
      func.varsSet("h_pre_total_bal", hPreTotalBal + "");
      func.varsSet("h_ware_item_amt", hWareItemAmt + "");
      rc = func.dbUpdate1();
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
      tmpUseTotal = tmpUseTotal - hPreTotalBal;
    }
    return rc;
  }

  public void backCrdWarehouse1() {
    // 刪除入庫資料時crd_warehouse處理方式
    String usSql = "", lsSql = "", dsSql = "";
    usSql = "update crd_warehouse " + "set in_qty01 = 0, in_qty02 = 0, in_qty03 = 0, in_qty04 = 0,"
        + "in_qty05 = 0, in_qty06 = 0, in_qty07 = 0, in_qty08 = 0,"
        + "in_qty09 = 0, in_qty10 = 0, in_qty11 = 0, in_qty12 = 0, "
        + "in_qty01_buy = 0, in_qty02_buy  = 0, in_qty03_buy  = 0, in_qty04_buy  = 0,"
        + "in_qty05_buy  = 0, in_qty06_buy  = 0, in_qty07_buy  = 0, in_qty08_buy  = 0,"
        + "in_qty09_buy  = 0, in_qty10_buy  = 0, in_qty11_buy  = 0, in_qty12_buy  = 0, "
        + "item_amt = 0 " + "where wh_year =:wh_year " + "and card_item =:card_item "
        + "and place =:place " + "and lot_no =:lot_no ";
    setString("wh_year", gsWhYear);
    setString("card_item", gsCardItem);
    setString("place", gsPlace);
    setString("lot_no", gsLotNo);
    sqlExec(usSql);

    if (sqlRowNum <= 0) {
      alertErr2("資料更新失敗");
      rc = -1;
    }
    this.sqlCommit(rc);

    // 20191017 刪除已歸0之crd_warehouse資料
    lsSql = "SELECT hex(rowid) as rowid," + "pre_total + " + "(in_qty01 - out_qty01 "
        + "+ in_qty02 - out_qty02 " + "+ in_qty03 - out_qty03 " + "+ in_qty04 - out_qty04 "
        + "+ in_qty05 - out_qty05 " + "+ in_qty06 - out_qty06 " + "+ in_qty07 - out_qty07 "
        + "+ in_qty08 - out_qty08 " + "+ in_qty09 - out_qty09 " + "+ in_qty10 - out_qty10 "
        + "+ in_qty11 - out_qty11 " + "+ in_qty12 - out_qty12 ) as total " + "FROM crd_warehouse "
        + "WHERE 1=1 ";
    lsSql += sqlCol(gsWhYear, "wh_year");
    lsSql += sqlCol(gsCardItem, "card_item");
    lsSql += sqlCol(gsPlace, "place");
    lsSql += sqlCol(gsLotNo, "lot_no");
    sqlSelect(lsSql);
    if (sqlNum("total") == 0) {
      dsSql = "delete crd_warehouse where 1=1 ";
      dsSql += sqlCol(gsWhYear, "wh_year");
      dsSql += sqlCol(gsCardItem, "card_item");
      dsSql += sqlCol(gsPlace, "place");
      dsSql += sqlCol(gsLotNo, "lot_no");
      sqlExec(dsSql);
    }
    this.sqlCommit(1);

  }

  public void backCrdWarehouse2() {
    // 出庫刪除時還原crd_warehouse
    double hWareOutQty01 = 0, hWareOutQty02 = 0, hWareOutQty03 = 0, hWareOutQty04 = 0,
        hWareOutQty05 = 0, hWareOutQty06 = 0, hWareOutQty07 = 0, hWareOutQty08 = 0,
        hWareOutQty09 = 0, hWareOutQty10 = 0, hWareOutQty11 = 0, hWareOutQty12 = 0,
        hPreTotalBal = 0, hWareItemAmt = 0, wkUseTotal = 0, tmpUseTotal = 0;
    String hWareRowid = "";
    String hWareLotNo = "";
    String lsSql = "";
    Crdm0010Func func = new Crdm0010Func(wp);

    lsSql = "select " + "out_qty01  , out_qty02   , out_qty03   , out_qty04   ,"
        + "out_qty05  , out_qty06   , out_qty07   , out_qty08   ,"
        + "out_qty09  , out_qty10   , out_qty11   , out_qty12   ," + "pre_total   "
        + "  + ( in_qty01+ in_qty02+ in_qty03+ in_qty04+ in_qty05+ in_qty06 + "
        + "      in_qty07+ in_qty08+ in_qty09+ in_qty10+ in_qty11+ in_qty12)  "
        + "  - (out_qty01+out_qty02+out_qty03+out_qty04+out_qty05+out_qty06 + "
        + "     out_qty07+out_qty08+out_qty09+out_qty10+out_qty11+out_qty12) h_pre_total_bal,"
        + "item_amt   , lot_no      , hex(rowid) as rowid   " + "from crd_warehouse "
        + "where wh_year =:wh_year " + "and card_item =:card_item " + "and place =:place "
        + "order by lot_no ";
    setString("wh_year", gsWhYear);
    setString("card_item", gsCardItem);
    setString("place", gsPlace);
    sqlSelect(lsSql);
    int rowCt = sqlRowNum;
    wkUseTotal = wp.itemNum("use_total");
    tmpUseTotal = wkUseTotal;
    for (int i = 0; i < rowCt; i++) {
      hWareLotNo = sqlStr(i, "lot_no");
      hWareOutQty01 = sqlNum(i, "out_qty01");
      hWareOutQty02 = sqlNum(i, "out_qty02");
      hWareOutQty03 = sqlNum(i, "out_qty03");
      hWareOutQty04 = sqlNum(i, "out_qty04");
      hWareOutQty05 = sqlNum(i, "out_qty05");
      hWareOutQty06 = sqlNum(i, "out_qty06");
      hWareOutQty07 = sqlNum(i, "out_qty07");
      hWareOutQty08 = sqlNum(i, "out_qty08");
      hWareOutQty09 = sqlNum(i, "out_qty09");
      hWareOutQty10 = sqlNum(i, "out_qty10");
      hWareOutQty11 = sqlNum(i, "out_qty11");
      hWareOutQty12 = sqlNum(i, "out_qty12");
      hWareItemAmt = sqlNum(i, "item_amt");
      hPreTotalBal = sqlNum(i, "h_pre_total_bal"); // 該批號庫存數
      hWareRowid = sqlStr(i, "rowid");
      // System.out.println("tmp_use_total : "+i+" "+tmp_use_total);
      if (tmpUseTotal == 0) { // 人出庫數已歸0
        return;
      }
      switch (gsWhMonth) {
        case "01":
          if (hWareOutQty01 == 0)
            break;
          if (hWareOutQty01 >= tmpUseTotal) {
            hWareOutQty01 = hWareOutQty01 - tmpUseTotal;
            tmpUseTotal = 0;
          } else {
            tmpUseTotal = tmpUseTotal - hWareOutQty01;
            hWareOutQty01 = 0;
          }
          break;
        case "02":
          if (hWareOutQty02 == 0)
            break;
          if (hWareOutQty02 >= tmpUseTotal) {
            hWareOutQty02 = hWareOutQty02 - tmpUseTotal;
            tmpUseTotal = 0;
          } else {
            tmpUseTotal = tmpUseTotal - hWareOutQty02;
            hWareOutQty02 = 0;
          }
          break;
        case "03":
          if (hWareOutQty03 == 0)
            break;
          if (hWareOutQty03 >= tmpUseTotal) {
            hWareOutQty03 = hWareOutQty03 - tmpUseTotal;
            tmpUseTotal = 0;
          } else {
            tmpUseTotal = tmpUseTotal - hWareOutQty03;
            hWareOutQty03 = 0;
          }
          break;
        case "04":
          if (hWareOutQty04 == 0)
            break;
          if (hWareOutQty04 >= tmpUseTotal) {
            hWareOutQty04 = hWareOutQty04 - tmpUseTotal;
            tmpUseTotal = 0;
          } else {
            tmpUseTotal = tmpUseTotal - hWareOutQty04;
            hWareOutQty04 = 0;
          }
          break;
        case "05":
          if (hWareOutQty05 == 0)
            break;
          if (hWareOutQty05 >= tmpUseTotal) {
            hWareOutQty05 = hWareOutQty05 - tmpUseTotal;
            tmpUseTotal = 0;
          } else {
            tmpUseTotal = tmpUseTotal - hWareOutQty05;
            hWareOutQty05 = 0;
          }
          break;
        case "06":
          if (hWareOutQty06 == 0)
            break;
          if (hWareOutQty06 >= tmpUseTotal) {
            hWareOutQty06 = hWareOutQty06 - tmpUseTotal;
            tmpUseTotal = 0;
          } else {
            tmpUseTotal = tmpUseTotal - hWareOutQty06;
            hWareOutQty06 = 0;
          }

          break;
        case "07":
          if (hWareOutQty07 == 0)
            break;
          if (hWareOutQty07 >= tmpUseTotal) {
            hWareOutQty07 = hWareOutQty07 - tmpUseTotal;
            tmpUseTotal = 0;
          } else {
            tmpUseTotal = tmpUseTotal - hWareOutQty07;
            hWareOutQty07 = 0;
          }
          break;
        case "08":
          if (hWareOutQty08 == 0)
            break;
          if (hWareOutQty08 >= tmpUseTotal) {
            hWareOutQty08 = hWareOutQty08 - tmpUseTotal;
            tmpUseTotal = 0;
          } else {
            tmpUseTotal = tmpUseTotal - hWareOutQty08;
            hWareOutQty08 = 0;
          }
          break;
        case "09":
          if (hWareOutQty09 == 0)
            break;
          if (hWareOutQty09 >= tmpUseTotal) {
            hWareOutQty09 = hWareOutQty09 - tmpUseTotal;
            tmpUseTotal = 0;
          } else {
            tmpUseTotal = tmpUseTotal - hWareOutQty09;
            hWareOutQty09 = 0;
          }
          break;
        case "10":
          if (hWareOutQty10 == 0)
            break;
          if (hWareOutQty10 >= tmpUseTotal) {
            hWareOutQty10 = hWareOutQty10 - tmpUseTotal;
            tmpUseTotal = 0;
          } else {
            tmpUseTotal = tmpUseTotal - hWareOutQty10;
            hWareOutQty10 = 0;
          }
          break;
        case "11":
          if (hWareOutQty11 == 0)
            break;
          if (hWareOutQty11 >= tmpUseTotal) {
            hWareOutQty11 = hWareOutQty11 - tmpUseTotal;
            tmpUseTotal = 0;
          } else {
            tmpUseTotal = tmpUseTotal - hWareOutQty11;
            hWareOutQty11 = 0;
          }
          break;
        case "12":
          if (hWareOutQty12 == 0)
            break;
          if (hWareOutQty12 >= tmpUseTotal) {
            hWareOutQty12 = hWareOutQty12 - tmpUseTotal;
            tmpUseTotal = 0;
          } else {
            tmpUseTotal = tmpUseTotal - hWareOutQty12;
            hWareOutQty12 = 0;
          }
          break;
      }
      func.varsSet("h_ware_out_qty01", hWareOutQty01 + "");
      func.varsSet("h_ware_out_qty02", hWareOutQty02 + "");
      func.varsSet("h_ware_out_qty03", hWareOutQty03 + "");
      func.varsSet("h_ware_out_qty04", hWareOutQty04 + "");
      func.varsSet("h_ware_out_qty05", hWareOutQty05 + "");
      func.varsSet("h_ware_out_qty06", hWareOutQty06 + "");
      func.varsSet("h_ware_out_qty07", hWareOutQty07 + "");
      func.varsSet("h_ware_out_qty08", hWareOutQty08 + "");
      func.varsSet("h_ware_out_qty09", hWareOutQty09 + "");
      func.varsSet("h_ware_out_qty10", hWareOutQty10 + "");
      func.varsSet("h_ware_out_qty11", hWareOutQty11 + "");
      func.varsSet("h_ware_out_qty12", hWareOutQty12 + "");
      func.varsSet("h_ware_rowid", hWareRowid);
      rc = func.dbUpdate2();
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    }
    this.sqlCommit(rc);
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
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("card_item");
      } else {
        wp.optionKey = wp.itemStr("ex_card_item");
      }
      this.dddwList("dddw_card_item", "crd_card_item", "card_item", "name", "where 1=1 ");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("trans_reason1");
      } else {
        wp.optionKey = wp.itemStr("ex_trans_reason1");
      }
      this.dddwList("dddw_trans_reason1", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'TRNS_IN_RSN' ");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("trans_reason2");
      } else {
        wp.optionKey = wp.itemStr("ex_trans_reason2");
      }
      this.dddwList("dddw_trans_reason2", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'TRNS_RSN1' ");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("place");
      } else {
        wp.optionKey = wp.itemStr("ex_place");
      }
      this.dddwList("dddw_place", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'WH_LOC' ");

    } catch (Exception ex) {
    }
  }

  public void itemChange() {
    String lsSql = "";
    lsSql =
        "select distinct card_type,unit_code from crd_item_unit " + "where card_item =:card_item ";
    setString("card_item", wp.itemStr("card_item"));
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      wp.colSet("card_type", sqlStr("card_type"));
      wp.colSet("unit_code", sqlStr("unit_code"));
    } else {
      alertErr("查無卡樣資料檔!!");
    }
    return;
  }
}
