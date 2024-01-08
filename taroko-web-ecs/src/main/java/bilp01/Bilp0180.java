/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-10  V1.00.00  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 109-04-23  V1.00.02  shiyuqi       updated for project coding standard     *  
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
* 111-05-31  V1.00.03  Ryan       增加異動人員與登入人員相同時不能覆核                                                    * 
******************************************************************************/
package bilp01;


import ofcapp.BaseProc;
import taroko.com.TarokoCommon;


public class Bilp0180 extends BaseProc {

  int rr = -1;
  String msg = "", msg2 = "";
  String kkIdNo = "", lsSdata = "";
  int ilOk = 0;
  int ilErr = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
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
  public void initPage() {}

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_upduser");
      this.dddwList("dddw_apuser", "sec_user", "usr_id", "usr_cname", "where 1=1  order by usr_id");
    } catch (Exception ex) {
    }
  }

  // for query use only
  int getWhereStr() throws Exception {
    String idPSeqno = "";

    if (!empty(wp.itemStr("ex_major_id")) && wp.itemStr("ex_major_id").length() < 10) {
      alertErr("請輸入 正卡身分證字號10碼");
      return -1;
    }

    if (wp.itemStr("ex_major_id").length() == 10 && empty(itemKk("data_k1"))) {
      String sqlSelect = " select id_p_seqno from crd_idno where id_no = :ex_major_id ";
      setString("ex_major_id", wp.itemStr("ex_major_id"));
      sqlSelect(sqlSelect);
      idPSeqno = sqlStr("id_p_seqno");

      sqlSelect = "select count(*) as cnt from crd_idno "
          + " where 1=1 and id_p_seqno  in (select major_id_p_seqno from crd_card where id_p_seqno = :id_p_seqno and current_code = '0')";
      setString("id_p_seqno", idPSeqno);
      sqlSelect(sqlSelect);
      if (sqlNum("cnt") <= 0) {
        alertErr("查無 有效正卡卡人資料 ?!");
        return -1;
      } else if (sqlNum("cnt") > 1) {
        queryReadIdno();
        return -1;
      }
    }
    wp.whereStr = " where 1=1 and a.card_no = b.card_no ";

    if (!empty(idPSeqno)) {
      wp.whereStr += " and b.major_id_p_seqno = :ex_id_p_seqno ";
      setString("ex_id_p_seqno", idPSeqno);
    }
    if (!empty(itemKk("data_k1"))) {
      wp.whereStr += " and b.major_id_p_seqno = :ex_id_p_seqno ";
      setString("ex_id_p_seqno", itemKk("data_k1"));
    }

    if (!empty(wp.itemStr("ex_upduser"))) {
      wp.whereStr += " and a.crt_user like :ex_upduser ";
      setString("ex_upduser", wp.itemStr("ex_upduser") + "%");
    }

    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    if (getWhereStr() == -1)
      return;
    wp.selectSQL = " hex(a.rowid) as rowid " + ",a.card_no " + ",a.start_date " + ",a.end_date "
        + ",a.amt_from " + ",a.installment_term " + ",a.reserve_type " + ",a.break_flag "
        + ",a.break_date " + ",a.crt_user " + ",a.crt_date " + ",a.apr_user " + ",a.apr_date "
        + ",a.master_rowid " + ",a.mod_user " + ",a.mod_time " + ",a.mod_seqno "
        + ",UF_IDNO_ID(b.major_id_p_seqno) as major_id ";

    wp.daoTable = " bil_assign_installment_t as a,crd_card as b ";

    wp.whereOrder = "  ";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    apprDisabled("mod_user");
    listWkdata();
  }

  void queryReadIdno() throws Exception {
    String sqlSelect = " select id_p_seqno from crd_idno where id_no = :ex_major_id ";
    setString("ex_major_id", wp.itemStr("ex_major_id"));
    sqlSelect(sqlSelect);
    String idPSeqno = sqlStr("id_p_seqno");

    wp.sqlCmd = "select " + "id_p_seqno " + ",id_no " + ",id_no_code " + ",sex " + ",birthday "
        + ",chi_name " + "from crd_idno "
        + " where 1=1 and id_p_seqno  in (select major_id_p_seqno from crd_card where id_p_seqno = :id_p_seqno and current_code = '0') ";
    setString("id_p_seqno", idPSeqno);
    pageQuery();
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.setListCount(2);
    wp.setPageValue();
    wp.colSet("queryReadCnt", intToStr(sqlRowNum));
  }

  void listWkdata() throws Exception {
    String reserveType = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      reserveType = wp.colStr(ii, "reserve_type");
      wp.colSet(ii, "tt_reserve_type", commString.decode(reserveType, ",0,1,2,3", ",一般,保費,稅款,學雜費"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {


  }

  public void dataProcess() throws Exception {
    /*
     * // -check approve- if (!check_approve(wp.item_ss("approval_user"),
     * wp.item_ss("approval_passwd"))) { return; }
     */
    String[] aaRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    String[] cardNo = wp.itemBuff("card_no");
    String[] startDate = wp.itemBuff("start_date");
    String[] endDate = wp.itemBuff("end_date");
    String[] amtFrom = wp.itemBuff("amt_from");
    String[] installmentTerm = wp.itemBuff("installment_term");
    String[] breakFlag = wp.itemBuff("break_flag");
    String[] breakDate = wp.itemBuff("break_date");
    String[] crtUser = wp.itemBuff("crt_user");
    String[] crtDate = wp.itemBuff("crt_date");
    String[] reserveType = wp.itemBuff("reserve_type");

    wp.listCount[0] = aaRowid.length;
    // -update-
    rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      int err = 0;
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      String sqlInsert = " insert into bil_assign_installment(" + "card_no " + ",start_date "
          + ",end_date " + ",amt_from " + ",installment_term " + ",break_flag " + ",break_date "
          + ",reserve_type" + ",crt_user " + ",crt_date " + ",apr_user " + ",apr_date "
          + ",MOD_USER " + ",MOD_TIME " + ",MOD_PGM " + ",MOD_SEQNO "
          + ") values (?,?,?,?,?,?,?,?,?,?,?,to_char(sysdate,'YYYYMMDD'),?,sysdate,?,1)";
      Object[] param = new Object[] {cardNo[rr], startDate[rr], endDate[rr], amtFrom[rr],
          installmentTerm[rr], breakFlag[rr], breakDate[rr], reserveType[rr], crtUser[rr],
          crtDate[rr], wp.loginUser, wp.loginUser, wp.modPgm()};
      sqlExec(sqlInsert, param);
      if (sqlRowNum <= 0) {
        sqlCommit(0);
        alertErr("insert bil_assign_installment err");
        wp.colSet(rr, "ok_flag", "!");
        return;
      }

      String sqlDelete = "delete from bil_assign_installment_t where hex(rowid) = ?";
      Object[] param2 = new Object[] {aaRowid[rr]};
      sqlExec(sqlDelete, param2);
      if (sqlRowNum <= 0) {
        sqlCommit(0);
        alertErr("delete bil_assign_installment_t err");
        wp.colSet(rr, "ok_flag", "!");
        return;
      }
      
      wp.colSet(rr, "ok_flag", "V");
      
    }
    sqlCommit(1);
    alertMsg("覆核處理成功");

  }

//	void apprDisabled(String col) throws Exception {
//		for (int ll = 0; ll < wp.listCount[0]; ll++) {
//			if (!wp.colStr(ll, col).equals(wp.loginUser)) {
//				wp.colSet(ll, "opt_disabled", "");
//			} else
//				wp.colSet(ll, "opt_disabled", "disabled");
//		}
//	}


  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
