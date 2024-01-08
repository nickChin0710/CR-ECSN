/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/09/23  V1.00.01   Ryan          Initial                              *
*                                                                          *
***************************************************************************/
package ipsm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ipsm0010 extends BaseEdit {

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
      strAction = "R";
      querySelect();
    } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
      strAction = "A";
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U";
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {

  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {

  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = "hex(rowid) as rowid," 
    		+ " nvl(mod_seqno,0) as mod_seqno, "
    		+ "wf_parm,"
    		+ "wf_key,"
    		+ "wf_desc," 
    		+ "wf_value6,"
    		+ "apr_user," 
    		+ "apr_date," 
    		+ "to_char(mod_time,'yyyymmdd') as mod_time,"
    		+ "mod_user," 
    		+ "wf_value7 ";

    wp.daoTable = " ptr_sys_parm ";
    wp.whereStr = " where 1=1 and wf_parm = 'SYSPARM' and wf_key  = 'IPS_0110' ";

    pageSelect();
    if (sqlNotFind()) {
    	alertErr(appMsg.errCondNodata);
    	return;
    }
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
      return;

    ipsm01.Ipsm0010Func func = new ipsm01.Ipsm0010Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1){
    	alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0)
    // {
    // this.btnMode_aud();
    // }
    btnModeAud("XX");
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {}


  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************

}  // End of class
