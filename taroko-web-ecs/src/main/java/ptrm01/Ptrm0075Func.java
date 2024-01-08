
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-06-30  V1.00.01  ryan       program initial                            *
* 111-05-30  V1.00.02  ryan       移除ebk_zek_chk欄位                                                                          *
* 111-08-11  V1.00.03  ryan       修復  ebk_zek_chk欄位 ，移除ecs_csck1欄位
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;


public class Ptrm0075Func extends FuncEdit {
  String kkHsmKeysOrg = "" ;

  public Ptrm0075Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
    	kkHsmKeysOrg = wp.itemStr("kk_hsm_keys_org");
    } else {
    	kkHsmKeysOrg = wp.itemStr("hsm_keys_org");
    }
    
    if(isEmpty(kkHsmKeysOrg)) {
    	errmsg("KEYS 不可空白");
      	return ;
    }
    
    if(this.ibDelete){
    	if(kkHsmKeysOrg.equals("00000000")){
    		errmsg("KEYS=00000000  不可刪除 ");
          	return ;
    	}
    }
    
    if (this.isAdd()) {
    	// 檢查新增資料是否重複
		String lsSql = "select count(*) as tot_cnt from ptr_hsm_keys where hsm_keys_org = ? ";
		Object[] param = new Object[] {kkHsmKeysOrg};
		sqlSelect(lsSql, param);
		if (colNum("tot_cnt") > 0) {
			errmsg("資料已存在，無法新增,請從新查詢");
			return;
		}
    }else{
        // -other modify-
        sqlWhere = " where hsm_keys_org= ? and nvl(mod_seqno,0) =? ";
        Object[] parms = new Object[] {kkHsmKeysOrg, wp.modSeqno()};
        if (this.isOtherModify("ptr_hsm_keys", sqlWhere, parms)) {
        	wp.log(sqlWhere, parms);
        	return;
        }
    }
  }
 

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into ptr_hsm_keys ( " 
    		+ "  hsm_keys_org "//1
			+ ", visa_pvka "
			+ ", visa_pvka_chk "
			+ ", visa_pvkb "
			+ ", visa_pvkb_chk "
			+ ", master_pvka "
			+ ", master_pvka_chk "
			+ ", master_pvkb "
			+ ", master_pvkb_chk "
			+ ", jcb_pvka "//10
			+ ", jcb_pvka_chk "
			+ ", jcb_pvkb "
			+ ", jcb_pvkb_chk "
			+ ", visa_cvka "
			+ ", visa_cvka_chk "
			+ ", visa_cvkb "
			+ ", visa_cvkb_chk "
			+ ", master_cvka "
			+ ", master_cvka_chk "
			+ ", master_cvkb "//20
			+ ", master_cvkb_chk "
			+ ", jcb_cvka "
			+ ", jcb_cvka_chk "
			+ ", jcb_cvkb "
			+ ", jcb_cvkb_chk "
			+ ", visa_mdk "
			+ ", visa_mdk_chk "
			+ ", master_mdk "
			+ ", master_mdk_chk "
			+ ", jcb_mdk "//30
			+ ", jcb_mdk_chk "
			+ ", net_zmk "
			+ ", net_zmk_chk "
			+ ", net_zpk "
			+ ", net_zpk_chk "
   			+ ", atm_zmk "
			+ ", atm_zmk_chk "
			+ ", atm_zpk "
			+ ", atm_zpk_chk "
			+ ", hsm_port1 "//40
			+ ", hsm_ip_addr1 "
			+ ", hsm_port2 "
			+ ", hsm_ip_addr2 "
			+ ", hsm_port3 "
			+ ", hsm_ip_addr3 "
//			+ ", ecs_csck1 "
			+ ", mob_kek "
			+ ", mob_kek_chk "
			+ ", mob_dek "
			+ ", mob_dek_chk "//50
			+ ", mob_version_id "
			+ ", acs_kek "
			+ ", acs_kek_chk "
			+ ", acs_dek "
			+ ", acs_dek_chk "
			+ ", acs_version_id "
			+ ", ebk_zek "
			+ ", ebk_zek_chk "
			+ ", ebk_dek "
			+ ", ebk_dek_chk "//60
			+ ", ebk_hmack "
			+ ", jcb_pvki "
			+ ", visa_pvki "
			+ ", master_pvki "
			+ ", remark "
			+ ", mod_user "
			+ ", mod_time "
			+ ", mod_pgm "
			+ ", mod_seqno "//69
    	+ " ) values ( "
        + " ?,?,?,?,?,?,?,?,?,?, " //10
        + " ?,?,?,?,?,?,?,?,?,?, " //20
        + " ?,?,?,?,?,?,?,?,?,?, " //30
        + " ?,?,?,?,?,?,?,?,?,?," //40
        + " ?,?,?,?,?,?,?,?,?,?, " //50
        + " ?,?,?,?,?,?,?,?,?,?, " //60
        + " ?,?,?,?, " 
        + " ?,sysdate,?,1 " 
    	+ " ) ";
    Object[] param = new Object[] {
    	  kkHsmKeysOrg //1
    	, wp.itemStr("visa_pvka")
    	, wp.itemStr("visa_pvka_chk")
    	, wp.itemStr("visa_pvkb")
    	, wp.itemStr("visa_pvkb_chk")
    	, wp.itemStr("master_pvka")
    	, wp.itemStr("master_pvka_chk")
    	, wp.itemStr("master_pvkb")
    	, wp.itemStr("master_pvkb_chk")
    	, wp.itemStr("jcb_pvka")//10
    	, wp.itemStr("jcb_pvka_chk")
    	, wp.itemStr("jcb_pvkb")
    	, wp.itemStr("jcb_pvkb_chk")
    	, wp.itemStr("visa_cvka")
    	, wp.itemStr("visa_cvka_chk")
    	, wp.itemStr("visa_cvkb")
    	, wp.itemStr("visa_cvkb_chk")
    	, wp.itemStr("master_cvka")
    	, wp.itemStr("master_cvka_chk")
    	, wp.itemStr("master_cvkb")//20
    	, wp.itemStr("master_cvkb_chk")
    	, wp.itemStr("jcb_cvka")
    	, wp.itemStr("jcb_cvka_chk")
    	, wp.itemStr("jcb_cvkb")
    	, wp.itemStr("jcb_cvkb_chk")
    	, wp.itemStr("visa_mdk")
    	, wp.itemStr("visa_mdk_chk")
    	, wp.itemStr("master_mdk")
    	, wp.itemStr("master_mdk_chk")
    	, wp.itemStr("jcb_mdk")//30
    	, wp.itemStr("jcb_mdk_chk")
    	, wp.itemStr("net_zmk")
    	, wp.itemStr("net_zmk_chk")
    	, wp.itemStr("net_zpk")
    	, wp.itemStr("net_zpk_chk")
    	, wp.itemStr("atm_zmk")
    	, wp.itemStr("atm_zmk_chk")
    	, wp.itemStr("atm_zpk")
    	, wp.itemStr("atm_zpk_chk")
    	, wp.itemStr("hsm_port1")
    	, wp.itemStr("hsm_ip_addr1")
    	, wp.itemStr("hsm_port2")
    	, wp.itemStr("hsm_ip_addr2")
    	, wp.itemStr("hsm_port3")
    	, wp.itemStr("hsm_ip_addr3")
//    	, wp.itemStr("ecs_csck1")
    	, wp.itemStr("mob_kek")
    	, wp.itemStr("mob_kek_chk")
    	, wp.itemStr("mob_dek")
    	, wp.itemStr("mob_dek_chk")
    	, wp.itemStr("mob_version_id")
    	, wp.itemStr("acs_kek")
    	, wp.itemStr("acs_kek_chk")
    	, wp.itemStr("acs_dek")
    	, wp.itemStr("acs_dek_chk")
    	, wp.itemStr("acs_version_id")
    	, wp.itemStr("ebk_zek")
    	, wp.itemStr("ebk_zek_chk")
    	, wp.itemStr("ebk_dek")
    	, wp.itemStr("ebk_dek_chk")
    	, wp.itemStr("ebk_hmack")
    	, wp.itemStr("jcb_pvki")
    	, wp.itemStr("visa_pvki")
    	, wp.itemStr("master_pvki")
    	, wp.itemStr("remark")
        , wp.loginUser
        , wp.itemStr("mod_pgm")
     };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
    	errmsg(sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbUpdate() { 
    actionInit("U");
    dataCheck();
    if (rc != 1) { 
      return rc;
    }
    strSql = "update ptr_hsm_keys set " 
			+ "  visa_pvka = ? "//1
			+ ", visa_pvka_chk = ? "
			+ ", visa_pvkb = ? "
			+ ", visa_pvkb_chk = ? "
			+ ", master_pvka = ? "
			+ ", master_pvka_chk = ? "
			+ ", master_pvkb = ? "
			+ ", master_pvkb_chk = ? "
			+ ", jcb_pvka = ? "
			+ ", jcb_pvka_chk = ? "//10
			+ ", jcb_pvkb = ? "
			+ ", jcb_pvkb_chk = ? "
			+ ", visa_cvka = ? "
			+ ", visa_cvka_chk = ? "
			+ ", visa_cvkb = ? "
			+ ", visa_cvkb_chk = ? "
			+ ", master_cvka = ? "
			+ ", master_cvka_chk = ? "
			+ ", master_cvkb = ? "
			+ ", master_cvkb_chk = ? "//20
			+ ", jcb_cvka = ? "
			+ ", jcb_cvka_chk = ? "
			+ ", jcb_cvkb = ? "
			+ ", jcb_cvkb_chk = ? "
			+ ", visa_mdk = ? "
			+ ", visa_mdk_chk = ? " 
			+ ", master_mdk = ? "
			+ ", master_mdk_chk = ? "
			+ ", jcb_mdk = ? "
			+ ", jcb_mdk_chk = ? "//30
			+ ", net_zmk = ? "
			+ ", net_zmk_chk = ? "
			+ ", net_zpk = ? "
			+ ", net_zpk_chk = ? "
			+ ", atm_zmk = ? "
			+ ", atm_zmk_chk = ? "
			+ ", atm_zpk = ? "
			+ ", atm_zpk_chk = ? "
			+ ", hsm_port1 = ? "
			+ ", hsm_ip_addr1 = ? "
			+ ", hsm_port2 = ? "
			+ ", hsm_ip_addr2 = ? "
			+ ", hsm_port3 = ? "
			+ ", hsm_ip_addr3 = ? "
//			+ ", ecs_csck1 = ? "
			+ ", mob_kek = ? "
			+ ", mob_kek_chk = ? "
			+ ", mob_dek = ? "
			+ ", mob_dek_chk = ? "
			+ ", mob_version_id = ? "
			+ ", acs_kek = ? "
			+ ", acs_kek_chk = ? "
			+ ", acs_dek = ? "
			+ ", acs_dek_chk = ? "
			+ ", acs_version_id = ? "
			+ ", ebk_zek = ? "
			+ ", ebk_zek_chk = ? "
			+ ", ebk_dek = ? "
			+ ", ebk_dek_chk = ? "
			+ ", ebk_hmack = ? "
			+ ", jcb_pvki = ? "
			+ ", visa_pvki = ? "
			+ ", master_pvki = ? "
			+ ", remark = ? "
    		+ ", mod_user = ?"
    		+ ", mod_time = sysdate"
    		+ ", mod_pgm =? "
    		+ ", mod_seqno =nvl(mod_seqno,0)+1 " 
    		+ sqlWhere;
    Object[] param = new Object[] {
    		  wp.itemStr("visa_pvka")//1
        	, wp.itemStr("visa_pvka_chk")
        	, wp.itemStr("visa_pvkb")
        	, wp.itemStr("visa_pvkb_chk")
        	, wp.itemStr("master_pvka")
        	, wp.itemStr("master_pvka_chk")
        	, wp.itemStr("master_pvkb")
        	, wp.itemStr("master_pvkb_chk")
        	, wp.itemStr("jcb_pvka")
        	, wp.itemStr("jcb_pvka_chk")//10
        	, wp.itemStr("jcb_pvkb")
        	, wp.itemStr("jcb_pvkb_chk")
        	, wp.itemStr("visa_cvka")
        	, wp.itemStr("visa_cvka_chk")
        	, wp.itemStr("visa_cvkb")
        	, wp.itemStr("visa_cvkb_chk")
        	, wp.itemStr("master_cvka")
        	, wp.itemStr("master_cvka_chk")
        	, wp.itemStr("master_cvkb")
        	, wp.itemStr("master_cvkb_chk")//20
        	, wp.itemStr("jcb_cvka")
        	, wp.itemStr("jcb_cvka_chk")
        	, wp.itemStr("jcb_cvkb")
        	, wp.itemStr("jcb_cvkb_chk")
        	, wp.itemStr("visa_mdk")
        	, wp.itemStr("visa_mdk_chk")
        	, wp.itemStr("master_mdk")
        	, wp.itemStr("master_mdk_chk")
        	, wp.itemStr("jcb_mdk")
        	, wp.itemStr("jcb_mdk_chk")//30
        	, wp.itemStr("net_zmk")
        	, wp.itemStr("net_zmk_chk")
        	, wp.itemStr("net_zpk")
        	, wp.itemStr("net_zpk_chk")
        	, wp.itemStr("atm_zmk")
        	, wp.itemStr("atm_zmk_chk")
        	, wp.itemStr("atm_zpk")
        	, wp.itemStr("atm_zpk_chk")
        	, wp.itemStr("hsm_port1")
        	, wp.itemStr("hsm_ip_addr1")
        	, wp.itemStr("hsm_port2")
        	, wp.itemStr("hsm_ip_addr2")
        	, wp.itemStr("hsm_port3")
        	, wp.itemStr("hsm_ip_addr3")
//        	, wp.itemStr("ecs_csck1")
        	, wp.itemStr("mob_kek")
        	, wp.itemStr("mob_kek_chk")
        	, wp.itemStr("mob_dek")
        	, wp.itemStr("mob_dek_chk")
        	, wp.itemStr("mob_version_id")
        	, wp.itemStr("acs_kek")
        	, wp.itemStr("acs_kek_chk")
        	, wp.itemStr("acs_dek")
        	, wp.itemStr("acs_dek_chk")
        	, wp.itemStr("acs_version_id")
        	, wp.itemStr("ebk_zek")
        	, wp.itemStr("ebk_zek_chk")
        	, wp.itemStr("ebk_dek")
        	, wp.itemStr("ebk_dek_chk")
        	, wp.itemStr("ebk_hmack")
        	, wp.itemStr("jcb_pvki")
        	, wp.itemStr("visa_pvki")
        	, wp.itemStr("master_pvki")
        	, wp.itemStr("remark")
            , wp.loginUser
            , wp.itemStr("mod_pgm")
            , kkHsmKeysOrg
            , wp.modSeqno()
    };
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
    	errmsg(this.sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete ptr_hsm_keys " + sqlWhere;
    Object[] parms = new Object[] {kkHsmKeysOrg, wp.modSeqno()};
    rc = sqlExec(strSql, parms);
    if (sqlRowNum <= 0) {
    	errmsg(this.sqlErrtext);
    }

    return rc;
  }

}
