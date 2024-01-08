package secm01;
/**
 * 2019-1128   JH              ++bank_unitno
 * 109-04-20  shiyuqi        updated for project coding standard  
 * 2020-0918  JustinWu     check user_id cannot be null, delete empty user_id
 * 2020-0923  JustinWu      fix where error      
 * */
import busi.FuncEdit;

public class Secm0050Func extends FuncEdit {
  String userId = "";

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
      userId = wp.itemStr("kk_usr_id");
    } else {
      userId = wp.itemStr("usr_id");
    }
    
    if (userId.isEmpty()) {
    	errmsg("使用者代號不能為空");
    	return;
	}

    if (this.isAdd()) {
      return;
    }
    sqlWhere =
        " where 1=1" 
     + " and usr_id = :usr_id "
     + " and nvl(mod_seqno,0) = :mod_seqno ";
//    + " and usr_id = '" + userId + "'"
//    + " and nvl(mod_seqno,0) =  " + Integer.parseInt(wp.modSeqno());
    setString("usr_id", userId);
    setInt("mod_seqno", Integer.parseInt(wp.modSeqno()));

    if (this.isOtherModify("sec_user", sqlWhere)) {
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into sec_user (" + " usr_id, " // 1
        + " usr_cname, " + " usr_type, " + " usr_empno, " + " usr_deptno, " + " usr_level, "
        + " usr_amtlevel, " + " cellar_phone, " + " usr_group, bank_unitno "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " :usr_id,:usr_cname,:usr_type " + " ,:usr_empno,:usr_deptno "
        + " ,:usr_level,:usr_amtlevel " + ", :cellar_phone, :usr_group, :bank_unitno "
        + ",sysdate,:mod_user,:mod_pgm,1" + " )";
    setString("usr_id", userId);
    item2ParmStr("usr_cname");
    item2ParmStr("usr_type");
    item2ParmStr("usr_empno");
    item2ParmStr("usr_deptno");
    item2ParmStr("usr_level");
    item2ParmStr("usr_amtlevel");
    item2ParmStr("cellar_phone");
    item2ParmStr("usr_group");
    item2ParmStr("bank_unitno");
    setString("mod_user", modUser);
    setString("mod_pgm", modPgm);

    sqlExec(strSql);
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
    strSql = "update sec_user set " 
        + " usr_cname =:usr_cname, " + " usr_type =:usr_type, "
        + " usr_empno =:usr_empno, " + " usr_deptno =:usr_deptno, " + " usr_level =:usr_level, "
        + " usr_group =:usr_group, " + " bank_unitno =:bank_unitno, "
        + " usr_amtlevel =:usr_amtlevel, " + " cellar_phone =:cellar_phone, "
        + commSqlStr.setModxxx(modUser, modPgm) + sqlWhere;

    item2ParmStr("usr_cname");
    item2ParmStr("usr_type");
    item2ParmStr("usr_empno");
    item2ParmStr("usr_deptno");
    item2ParmStr("usr_level");
    item2ParmStr("usr_amtlevel");
    item2ParmStr("cellar_phone");
    item2ParmStr("usr_group");
    item2ParmStr("bank_unitno");
    setString("usr_id", userId);
    setInt("mod_seqno", Integer.parseInt(wp.modSeqno()));

    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update sec_user error: " + this.sqlErrtext);
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
    String sql =    
    		" where 1=1" 
         + " and ( usr_id = :usr_id  AND nvl(mod_seqno,0) = :mod_seqno) "
         + " or ( usr_id = '' ) ";
    
    strSql = "delete sec_user " 
             + sql;
    setString("usr_id", userId);
    setInt("mod_seqno", Integer.parseInt(wp.modSeqno()));
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }
}

