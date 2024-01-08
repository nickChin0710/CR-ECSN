/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-29  V1.00.01    Ryan        program initial                            *
* 109-05-06  V1.00.02    Aoyulan       updated for project coding standard   * 
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                          *   
* 111-01-09  V1.00.04   sunny        調整協商案件狀態代碼
******************************************************************************/
package colm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;


public class Colm1020 extends BaseEdit {
  CommString commString = new CommString();
  Colm1020Func func;

  String liabType = "";
  String liabStatus = "";

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
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
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
      strAction = "";
      clearFunc();
    }

    // dddw_select();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("d_bal_flag", "N");
    wp.colSet("DEFAULT_CHK", "checked");
  }

  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1";

    if (empty(wp.itemStr("ex_liab_type")) == false) {
      wp.whereStr += " and  liab_type = :liab_type ";
      setString("liab_type", wp.itemStr("ex_liab_type"));
    }

    wp.whereOrder = " order by liab_type";
    // -page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = " liab_type, " + " liab_status, " + "stat_unprint_flag," + "no_tel_coll_flag,"
        + " no_delinquent_flag, " + " no_collection_flag, " + " no_f_stop_flag, "
        + " revolve_rate_flag, " + " no_penalty_flag, " + " no_sms_flag, " + " min_pay_flag, "
        + " autopay_flag, " + " pay_stage_flag, " + " pay_stage_mark, " + " block_flag, "
        + " block_mark1, " + " block_mark3, " + " send_cs_flag, " + " d_bal_flag, "
        + " jcic_payrate_flag, " + " oppost_flag, " + " oppost_reason, " + " noauto_balance_flag, "
        + " no_interest_flag, " + " end_flag, " + " END_D_BAL_FLAG"
    // +" nego_effect_flag "
    ;

    wp.daoTable = "col_liab_param";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
  }


  @Override
  public void querySelect() throws Exception {
    initButton();
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    liabType = wp.itemStr("kk_liab_type");
    liabStatus = wp.itemStr("kk_liab_status");
    if (empty(liabType)) {
      liabType = wp.itemStr("liab_type");
    }
    if (empty(liabStatus)) {
      liabStatus = wp.itemStr("liab_status");
    }
    if (empty(liabType)) {
      liabType = itemKk("data_k1");
    }
    if (empty(liabStatus)) {
      liabStatus = itemKk("data_k2");
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + " liab_type, " + " liab_status, "
        + "stat_unprint_flag," + "no_tel_coll_flag," + " no_delinquent_flag, "
        + " no_collection_flag, " + " no_f_stop_flag, " + " revolve_rate_flag, "
        + " no_penalty_flag, " + " no_sms_flag, " + " min_pay_flag, " + " autopay_flag, "
        + " pay_stage_flag, " + " pay_stage_mark, " + " block_flag, " + " block_mark1, "
        + " block_mark3, " + " send_cs_flag, " + " d_bal_flag, " + " jcic_payrate_flag, "
        + " oppost_flag, " + " oppost_reason, " + " noauto_balance_flag, " + " no_interest_flag, "
        + " end_flag, " + " end_d_bal_flag "
    // +" nego_effect_flag "
    ;
    wp.daoTable = "col_liab_param";
    wp.whereStr = "where 1=1 and  liab_type = :liab_type and liab_status = :liab_status";
    setString("liab_type", liabType);
    setString("liab_status", liabStatus);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + liabType + liabStatus);
    }
    listWkdata();
  }

  @Override
  public void saveFunc() throws Exception {

    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    func = new Colm1020Func(wp);
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    func.varsSet("is_action", strAction);
    func.insertLog();
    this.sqlCommit(rc);


  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }
  
  /*
   * 
   * 1更生程序開始
2更生撤銷
3更生方案認可確定
4更生方案履行完畢
5更生裁定免責確定
6更生調查程序
7更生駁回
A清算程序開始
B清算程序終止(結)
C清算程序開始同時終止
D清算撤銷免責確定
E清算調查程序
F清算駁回
G清算撤回
H清算復權

   */

  void listWkdata() {
    String liabType = "";
    String[] cde = {};
    String[] txt = {};
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      liabType = wp.colStr(ii, "liab_type");
      cde = new String[] {"1", "2", "3", "4", "5", "6", "7"};
      txt = new String[] {"1.債務協商", "2.前置協商", "3.更生", "4.清算", "5.個別協商", "6.消金無擔保展延", "7.前置調解"};
      wp.colSet(ii, "tt_liab_type", commString.decode(liabType, cde, txt));
      if (liabType.equals("1")) {
//        cde = new String[] {"1", "2", "3", "4"};
//        txt = new String[] {"1.停催", "2.復催", "3.協商成功", "4.結案"};
         cde = new String[] {"1", "2", "3", "4", "5","6"};
  	     txt = new String[] {"1.受理申請", "2.停催", "3.簽約成功", "4.結案/復催", "5.結案/毀諾","6.結案/結清"};
      }
      if (liabType.equals("2")) {
//        cde = new String[] {"1", "2", "3", "4", "5"};
//        txt = new String[] {"1.受理申請", "2.停催", "3.簽約成功", "4.結案/復催", "5.結案/結清"};
    	  cde = new String[] {"1", "2", "3", "4", "5","6"};
    	  txt = new String[] {"1.受理申請", "2.停催", "3.簽約成功", "4.結案/復催", "5.結案/毀諾","6.結案/結清"};
      }
      if (liabType.equals("3")) {
        cde = new String[] {"1", "2", "3", "4", "5", "6", "7"};
        txt = new String[] {"1.更生開始", "2.更生撤回", "3.更生認可", "4.更生履行完畢", "5.更生裁定免責", "6.更生調查程序",
            "7.更生駁回"};
      }
      if (liabType.equals("4")) {
        cde = new String[] {"1", "2", "3", "4", "5", "6", "7","8"};
        txt = new String[] {"1.清算程序開始", "2.清算程序終止", "3.清算程序開始同時終止", "4.清算撤銷免責", "5.清算調查程序",
            "6.清算駁回", "7.清算撤回", "8.清算復權"};
      }
      if (liabType.equals("5")) {
//        cde = new String[] {"1", "2", "3", "4"};
//        txt = new String[] {"1.達成個別協商", "2.提前清償", "3.毀諾", "4.毀諾後清償"};
    	  cde = new String[] {"1", "2", "3", "4", "5", "6"};
    	  txt = new String[] {"1.受理申請", "2.停催", "3.簽約成功", "4.結案/復催", "5.結案/毀諾","6.結案/結清"};
      }
      if (liabType.equals("6")) {
        cde = new String[] {"1", "2", "3"};
        txt = new String[] {"1.受理申請", "2.展延成功", "3.取消或結案"};
      }
      if (liabType.equals("7")) {
//        cde = new String[] {"1", "3", "4", "5", "6"};
//        txt = new String[] {"1.受理申請", "3.簽約成功", "4.結案/復催", "5.結案/結清", "6.本行無債權"};
    	  cde = new String[] {"1", "2", "3", "4", "5", "6"};
    	  txt = new String[] {"1.受理申請", "2.停催", "3.簽約成功", "4.結案/復催", "5.結案/毀諾","6.結案/結清"};
      }
      liabType = wp.colStr(ii, "liab_status");
      wp.colSet(ii, "tt_liab_status", commString.decode(liabType, cde, txt));
    }
  }

  @Override
  public void dddwSelect() {
    /*
     * try { this.dddw_list("dddw_liab_type", "col_liab_param", "liab_type", "",
     * "where 1=1 order by liab_type"); } catch(Exception ex) {}
     */
  }

}
