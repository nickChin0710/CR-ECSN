package ccam02;
/* ccam5200	自動授權檢核參數設定　autoauth_parm
 * Table: cca_sys_parm2.MPARM
 * ----------------------------------------------------------------------------
 * V00.1    Alex     2019-1210:  add initButton
 * V00.0		Alex		2017-0823:
 *  V1.00.01    yanghan  2020-04-20   修改了變量名稱和方法名稱*
 * */
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5200 extends BaseEdit {

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
  public void initPage() {
    try {
      dataRead();
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {

    // --m1_mcode
    wp.selectSQL = "sys_data1 as m1_mcode" + ", sys_data2 as m1_unpay_amt"
        + ", sys_data3 as m1_resp_code" + ", mod_seqno as mod_seqno_1" + ", crt_user" + ", crt_date"
        + ", to_char(mod_time,'yyyymmdd') as mod_date" + ", mod_user";
    wp.daoTable = "CCA_sys_parm2";
    wp.whereStr = "where 1=1" + sqlCol("MPARM", "sys_id") + sqlCol("M1", "sys_key");
    pageSelect();

    wp.selectSQL =
        "sys_data2 as m2_unpay_amt" + ", sys_data3 as m2_resp_code" + ", mod_seqno as mod_seqno_2";
    wp.daoTable = "CCA_sys_parm2";
    wp.whereStr = "where 1=1" + sqlCol("MPARM", "sys_id") + sqlCol("M2", "sys_key");
    pageSelect();

  }

  @Override
  public void saveFunc() throws Exception {
    Ccam5200Func func = new Ccam5200Func();
    func.setConn(wp);

    if (checkApproveZz() == false) {
      return;
    }
    rc = func.dbUpdate();
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
      return;
    }	else {
    	int liSeqno1 = (int) wp.itemNum("mod_seqno_1") + 1;
    	int liSeqno2 = (int) wp.itemNum("mod_seqno_2") + 1;
    	wp.colSet("mod_seqno_1", "" + liSeqno1);
    	wp.colSet("mod_seqno_2", "" + liSeqno2);
    }
    

  }

  @Override
  public void initButton() {
    this.btnModeAud("XX");

  }

}
