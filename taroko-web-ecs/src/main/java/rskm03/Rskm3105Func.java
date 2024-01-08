package rskm03;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Rskm3105Func extends FuncEdit {
String kk1 = "", kk2 = "";

public Rskm3105Func(TarokoCommon wr) {
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
      kk1 = wp.itemStr("kk_area_code");
   }
   else {
      kk1 = wp.itemStr("area_code");
   }
   if (this.ibAdd) {
      if (empty(wp.itemStr("kk_area_code"))) {
         errmsg("冒用地區代碼：不可空白");
         return;
      }
   }
   if (this.isAdd()) {
      return;
   }
   sqlWhere = " where 1=1"
         + " and area_code='" + kk1 + "'"
         + " and nvl(mod_seqno,0) =" + wp.modSeqno();
   if (this.isOtherModify("rsk_ctfi_area", sqlWhere)) {
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

   strSql = "insert into rsk_ctfi_area ("
         + " area_code, " // 1
         + " cntry_code, "
         + " area_remark, "
         + " crt_user, "
         + " crt_date "//5
         + ", mod_time, mod_user, mod_pgm, mod_seqno"
         + " ) values ("
         + " ?,?,?,?,?"
         + ",sysdate,?,?,1"
         + " )";
   Object[] param = new Object[]{
         kk1
         , wp.itemStr("cntry_code")
         , wp.itemStr("area_remark")
         , wp.loginUser
         , ""
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
   strSql = "update rsk_ctfi_area set "
         + " cntry_code =?, "
         + " area_remark =?, "
         + " mod_user =?, mod_time=sysdate, mod_pgm =? "
         + ", mod_seqno =nvl(mod_seqno,0)+1 "
         + sqlWhere;
   Object[] param = new Object[]{
         wp.itemStr("cntry_code"),
         wp.itemStr("area_remark"),
         wp.loginUser,
         wp.itemStr("mod_pgm")
   };


   rc = sqlExec(strSql, param);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_ctfi_area error: " + this.sqlErrtext);
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
   strSql = "delete rsk_ctfi_area "
         + sqlWhere;
   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
   }
   return rc;
}

}
