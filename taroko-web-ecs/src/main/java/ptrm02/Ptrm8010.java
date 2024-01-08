/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03  shiyuqi    修改無意義命名                                                                                   *
* 110-02-23  V1.00.04  Justin     若修改AD_DOMAIN_WF_KEY，則需重新讀取系統參數            *
* 110-10-20  V1.00.05  Sunny      修改查詢時不論是否有where條件，一律採用相同的排序，讓分頁跨頁查詢結果一致*    
******************************************************************************/
package ptrm02;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoParm;

public class Ptrm8010 extends BaseEdit {
Ptrm8010Func func;
  String wfParm = "", wfKey = "";

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

    dddwSelect();
    initButton();
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_wf_parm").toUpperCase(), "upper(wf_parm)", "like%")
        + sqlCol(wp.itemStr("ex_wf_key").toUpperCase(), "upper(wf_key)", "like%");
    wp.whereOrder = " order by wf_parm ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "wf_parm, " + "wf_key," + "wf_desc ";
    wp.daoTable = "ptr_sys_parm";
    wp.whereOrder = " order by wf_parm ";
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
    wfParm = wp.itemStr("data_k1");
    wfKey = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(wfParm)) {
      wfParm = itemKk("wf_parm");
    }
    if (empty(wfKey)) {
      wfKey = itemKk("wf_key");
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "wf_parm,   " + "wf_key, " + "wf_desc, "
        + "wf_value," + "wf_value2," + "wf_value3," + "wf_value4," + "wf_value5," + "wf_value6,"
        + "wf_value7," + "wf_value8," + "wf_value9," + "wf_value10," + "mod_seqno," + "mod_user, "
        + "uf_2ymd(mod_time) as mod_date " + ", mod_pgm";
    wp.daoTable = "ptr_sys_parm";
    wp.whereStr = "where 1=1" + sqlCol(wfParm, "wf_parm") + sqlCol(wfKey, "wf_key");                  
    
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + wfParm + ", " + wfKey);
      return;
    }
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Ptrm8010Func(wp);
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }else {
    	String wfParm =wp.itemNvl("kk_wf_parm", wp.itemStr("wf_parm"));
    	String wfKey =wp.itemNvl("kk_wf_key", wp.itemStr("wf_key"));
    	if ("SYSPARM".equalsIgnoreCase(wfParm) && 
    			TarokoParm.AD_DOMAIN_WF_KEY.equalsIgnoreCase(wfKey)) {
    		TarokoParm.getInstance().setLdapIPAndDomain(wp.getConn());
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
}

