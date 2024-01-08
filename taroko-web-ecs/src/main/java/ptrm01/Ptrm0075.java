package ptrm01;
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-06-30  V1.00.01  ryan       program initial                            *
*                                                                            *
******************************************************************************/
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;


public class Ptrm0075 extends BaseEdit {
  Ptrm0075Func func;
  String kkHsmKeysOrg ;

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
      wp.colSet("risk_factor", "0");
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

    wp.whereStr = "where 1=1" 
        + sqlCol(wp.itemStr("ex_hsm_keys_org"), "hsm_keys_org")
        ;

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " hex(rowid) as rowid "
    			+ ", hsm_keys_org "
    			+ ", visa_pvka "
    			+ ", visa_pvka_chk "
    			+ ", visa_pvkb "
    			+ ", visa_pvkb_chk "
    			+ ", master_pvka "
    			+ ", master_pvka_chk "
    			+ ", master_pvkb "
    			+ ", master_pvkb_chk "
    			+ ", jcb_pvka "
    			+ ", jcb_pvka_chk "
    			+ ", jcb_pvkb "
    			+ ", jcb_pvkb_chk "
    			+ ", visa_cvka "
    			+ ", visa_cvka_chk "
    			+ ", visa_cvkb "
    			+ ", visa_cvkb_chk "
    			+ ", master_cvka "
    			+ ", master_cvka_chk "
    			+ ", master_cvkb " 
    			+ ", master_cvkb_chk "
    			+ ", jcb_cvka "
    			+ ", jcb_cvka_chk "
    			+ ", jcb_cvkb "
    			+ ", jcb_cvkb_chk "
    			+ ", visa_mdk "
    			+ ", visa_mdk_chk "
    			+ ", master_mdk "
    			+ ", master_mdk_chk "
    			+ ", jcb_mdk "
    			+ ", jcb_mdk_chk "
    			+ ", net_zmk "
    			+ ", net_zmk_chk "
    			+ ", net_zpk "
    			+ ", net_zpk_chk "
       			+ ", atm_zmk "
    			+ ", atm_zmk_chk "
    			+ ", atm_zpk "
    			+ ", atm_zpk_chk "
    			+ ", hsm_port1 "
    			+ ", hsm_ip_addr1 "
    			+ ", hsm_port2 "
    			+ ", hsm_ip_addr2 "
    			+ ", hsm_port3 "
    			+ ", hsm_ip_addr3 "
    			+ ", ecs_csck1 "
    			+ ", mob_kek "
    			+ ", mob_kek_chk "
    			+ ", mob_dek "
    			+ ", mob_dek_chk "
    			+ ", mob_version_id "
    			+ ", acs_kek "
    			+ ", acs_kek_chk "
    			+ ", acs_dek "
    			+ ", acs_dek_chk "
    			+ ", acs_version_id "
    			+ ", ebk_zek "
    			+ ", ebk_zek_chk "
    			+ ", ebk_dek "
    			+ ", ebk_dek_chk "
    			+ ", ebk_hmack "
    			+ ", jcb_pvki "
    			+ ", visa_pvki "
    			+ ", master_pvki "
    			+ ", remark "
    			+ ", mod_user "
    			+ ", to_Char(mod_time,'yyyymmdd') as mod_date "
    			+ ", mod_pgm "
    			+ ", mod_seqno "
    			;
    wp.daoTable = "ptr_hsm_keys";
    if (empty(wp.whereStr)) {
        wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by hsm_keys_org ";

    // sql_ddd();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
	  kkHsmKeysOrg = wp.itemStr("data_k1");
	  dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(kkHsmKeysOrg)) {
    	kkHsmKeysOrg = itemKk("hsm_keys_org");
    }
      
    if (isEmpty(kkHsmKeysOrg)) {
      alertErr("KEYS 不可空白");
      return;
    }
    
    wp.selectSQL = " hex(rowid) as rowid "
			+ ", hsm_keys_org "
			+ ", visa_pvka "
			+ ", visa_pvka_chk "
			+ ", visa_pvkb "
			+ ", visa_pvkb_chk "
			+ ", master_pvka "
			+ ", master_pvka_chk "
			+ ", master_pvkb "
			+ ", master_pvkb_chk "
			+ ", jcb_pvka "
			+ ", jcb_pvka_chk "
			+ ", jcb_pvkb "
			+ ", jcb_pvkb_chk "
			+ ", visa_cvka "
			+ ", visa_cvka_chk "
			+ ", visa_cvkb "
			+ ", visa_cvkb_chk "
			+ ", master_cvka "
			+ ", master_cvka_chk "
			+ ", master_cvkb "
			+ ", master_cvkb_chk "
			+ ", jcb_cvka "
			+ ", jcb_cvka_chk "
			+ ", jcb_cvkb "
			+ ", jcb_cvkb_chk "
			+ ", visa_mdk "
			+ ", visa_mdk_chk "
			+ ", master_mdk "
			+ ", master_mdk_chk "
			+ ", jcb_mdk "
			+ ", jcb_mdk_chk "
			+ ", net_zmk "
			+ ", net_zmk_chk "
			+ ", net_zpk "
			+ ", net_zpk_chk "
   			+ ", atm_zmk "
			+ ", atm_zmk_chk "
			+ ", atm_zpk "
			+ ", atm_zpk_chk "
			+ ", hsm_port1 "
			+ ", hsm_ip_addr1 "
			+ ", hsm_port2 "
			+ ", hsm_ip_addr2 "
			+ ", hsm_port3 "
			+ ", hsm_ip_addr3 "
			+ ", ecs_csck1 "
			+ ", mob_kek "
			+ ", mob_kek_chk "
			+ ", mob_dek "
			+ ", mob_dek_chk "
			+ ", mob_version_id "
			+ ", acs_kek "
			+ ", acs_kek_chk "
			+ ", acs_dek "
			+ ", acs_dek_chk "
			+ ", acs_version_id "
			+ ", ebk_zek "
			+ ", ebk_zek_chk "
			+ ", ebk_dek "
			+ ", ebk_dek_chk "
			+ ", ebk_hmack "
			+ ", jcb_pvki "
			+ ", visa_pvki "
			+ ", master_pvki "
			+ ", remark "
			+ ", mod_user "
			+ ", to_Char(mod_time,'yyyymmdd') as mod_date "
			+ ", mod_pgm "
			+ ", mod_seqno "
			;

    wp.daoTable = "ptr_hsm_keys";
    wp.whereStr = "where 1=1" 
            + sqlCol(kkHsmKeysOrg, "hsm_keys_org");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + kkHsmKeysOrg );
    }

  }

  @Override
  public void saveFunc() throws Exception {

    func = new ptrm01.Ptrm0075Func(wp);
    
    if (checkApproveZz() == false) {
        return;
    }
    
    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
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
