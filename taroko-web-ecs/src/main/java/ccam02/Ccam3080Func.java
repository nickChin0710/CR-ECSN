/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 108-12-18  V1.00.00  JustinWu  program initial
 * 108-12-25  V1.00.01  JustinWu  remove risk_factor                            *
 * 109-04-20  V1.00.02  yanghan 修改了變量名稱和方法名稱
 * 109-09-25  V1.00.03 JustinWu  add acno_p_seqno
 * 2023-1222   bugfix
 ******************************************************************************/
package ccam02;

import busi.FuncAction;
import busi.SqlPrepare;

public class Ccam3080Func extends FuncAction {
String acctType = "", cardIndicator = "", idNo = "", cardNo = "",
    startDate = "", endDate = "", withSupCard = "", idPSeqno = "",
    corpPSeqno = "", datePattern = "", acnoPSeqno = "";

@Override
public void dataCheck() {
   String sql1 = "";
   acctType = wp.itemStr("kk_acct_type");
   cardIndicator = wp.itemStr("card_indicator");

   if (empty(acctType)) {
      errmsg("帳戶類別不可空白");
      return;
   }

   switch (cardIndicator) {
      // case1 begin
      case "1":
         idNo = wp.itemStr("kk_id_no");
         if (empty(idNo)) {
            errmsg("未填寫正卡人ID");
            return;
         }

//        sql1 = "select "
//            + " i.id_p_seqno, "
//            + " aa.acno_p_seqno "
//        	+ " from crd_idno as i LEFT JOIN act_acno aa ON i.ID_P_SEQNO = aa.ID_P_SEQNO  "
//            + " where i.id_no =:id_no "
//            + " and exists(select 1 from ptr_acct_type as p "
//                           + " where p.acct_type =:acct_type1 "
//                           + " and p.card_indicator='1') "
//            + " and exists ( select 1 from crd_card as c "
//                             + " where c.acct_type=:acct_type2 "
//                             + " and c.major_id_p_seqno = i.id_p_seqno)";
//         setString("id_no", idNo);
//         setString("acct_type1", acctType);
//         setString("acct_type2", acctType);
         sql1 = "select id_p_seqno, acno_p_seqno"
             +" from act_acno"
             +" where acct_type =?"
             +" and id_p_seqno in (select id_p_seqno from crd_idno where id_no =?)"
         ;
         setString(1,acctType);
         setString(2, idNo);
         sqlSelect(sql1);
         if (sqlRowNum <= 0) {
            errmsg("此ID不為正卡人ID");
            return;
         }

         idPSeqno = colStr("id_p_seqno");
         acnoPSeqno = colStr("acno_p_seqno");

         break;
      // case1 end

      // case 2 start
      case "2":
         cardNo = wp.itemStr("kk_card_no");
         if (empty(cardNo)) {
            errmsg("未填寫商務卡卡號");
            return;
         }

         sql1 = "select "
             +" c.corp_p_seqno, c.id_p_seqno, c.acno_p_seqno "
             +" from crd_card as c "
             +" where C.acct_type =? "
             +" and c.card_no =? "
//             +" and  exists(select 1 from ptr_acct_type as p where p.acct_type=:acct_type2 "
//             +" and p.card_indicator='2')"
         ;
         setString(1, acctType);
         setString(2, cardNo);
//         setString("acct_type2", acctType);

         sqlSelect(sql1);
         if (sqlRowNum <= 0) {
            errmsg("此卡不存在卡檔內");
            return;
         }

         idPSeqno = colStr("id_p_seqno");
         corpPSeqno = colStr("corp_p_seqno");
         acnoPSeqno = colStr("acno_p_seqno");

         break;
      // case 2 end

      default:
         errmsg("card_indicator錯誤");
         return;
   }

   //--已覆核的修改若存在需從待覆核的修改
   if (wp.itemEq("temp_flag", "Y")) {
      if (checkTempTable() == false) {
         errmsg("已存在待覆核資料 , 請從待覆核資料異動");
         return;
      }
   }

   if (this.ibAdd) {
      strSql = "select "+" rowid "
          +" from cca_vip "
          +" where acct_type =:acct_type "
          +" and corp_p_seqno =:corp_p_seqno "
          +" and idno_p_seqno =:idno_p_seqno ";

      setString("acct_type", acctType);
      setString("corp_p_seqno", corpPSeqno);
      setString("idno_p_seqno", idPSeqno);

      sqlSelect(strSql);
      if (!sqlNotfind) {
         errmsg("此資料已被新增");
         return;
      }

      strSql = "select "+" rowid "
          +" from cca_vip "
          +" where acct_type =:acct_type "
          +" and corp_p_seqno =:corp_p_seqno "
          +" and idno_p_seqno =:idno_p_seqno ";

      setString("acct_type", acctType);
      setString("corp_p_seqno", corpPSeqno);
      setString("idno_p_seqno", idPSeqno);

      sqlSelect(strSql);
      if (!sqlNotfind) {
         errmsg("此資料已被新增,待覆核中");
         return;
      }

   }
   if (!this.ibDelete) {

      startDate = wp.itemStr("start_date");
      endDate = wp.itemStr("end_date");
      if (empty(startDate)) {
         errmsg("未填寫起始日期");
         return;
      }
      if (empty(endDate)) {
         errmsg("未填寫結束日期");
         return;
      }

      if (chkStrend(startDate, endDate) != 1) {
         errmsg("異動日期: 起迄錯誤");
         return;
      }
   }

}

boolean checkTempTable() {
   String sql1 = "select count(*) as db_cnt from cca_vip_t where acct_type = ? and corp_p_seqno = ? and idno_p_seqno = ? ";
   sqlSelect(sql1, new Object[]{acctType, corpPSeqno, idPSeqno});

   if (colNum("db_cnt") > 0)
      return false;

   return true;
}

@Override
public int dbInsert() {
   actionInit("A");
   dataCheck();
   if (rc != 1) {
      return rc;
   }
   withSupCard = wp.itemNvl("with_sup_card", "N");

   busi.SqlPrepare sp = new SqlPrepare();

   // 新增cca_vip_t
   sp.sql2Insert("cca_vip_t");
   sp.ppstr("acct_type", acctType);
   sp.ppstr("start_date", startDate);
   sp.ppstr("end_date", endDate);
   sp.addsql(", crt_date", ", to_char(sysdate,'yyyymmdd') ");
   sp.ppstr("crt_user", wp.loginUser);
   sp.ppstr("mod_user", wp.loginUser);
   sp.addsql(", mod_time ", ", sysdate ");
   sp.ppstr("mod_pgm", wp.modPgm());
   sp.ppstr("mod_audcode", "A");

   switch (cardIndicator) {
      case "1":
         sp.ppstr("corp_p_seqno", "");
         sp.ppstr("idno_p_seqno", idPSeqno);
         sp.ppstr("acno_p_seqno", acnoPSeqno);
         sp.ppstr("with_sup_card", withSupCard);
         break;

      case "2":
         sp.ppstr("corp_p_seqno", corpPSeqno);
         sp.ppstr("idno_p_seqno", idPSeqno);
         sp.ppstr("acno_p_seqno", acnoPSeqno);
         sp.ppstr("with_sup_card", "");
         break;
   }

   rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

   if (sqlRowNum == 0) {
      errmsg("新增錯誤");
      return rc;
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

   withSupCard = wp.itemNvl("with_sup_card", "N");


   busi.SqlPrepare sp = new SqlPrepare();

   if (wp.itemEq("temp_flag", "N")) {
      // 更新cca_vip_t
      sp.sql2Update("cca_vip_t");
      sp.ppstr("start_date", startDate);
      sp.ppstr("end_date", endDate);
      sp.addsql(", crt_date = to_char(sysdate,'yyyymmdd')", "");
      sp.ppstr("crt_user", wp.loginUser);
      sp.ppstr("mod_user", wp.loginUser);
      sp.addsql(", mod_time = sysdate ", "");
      sp.ppstr("mod_pgm", wp.modPgm());

      switch (cardIndicator) {
         case "1":
            sp.ppstr("with_sup_card", withSupCard);
            break;

         case "2":
            sp.ppstr("with_sup_card", "");
            break;
      }
      sp.sql2Where(" where acct_type =?", acctType);
      sp.sql2Where(" and corp_p_seqno=?", corpPSeqno);
      sp.sql2Where(" and idno_p_seqno=?", idPSeqno);
      rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

      if (sqlRowNum <= 0) {
         errmsg("更新錯誤");
         return rc;
      }
   } else if (wp.itemEq("temp_flag", "Y")) {
      sp.sql2Insert("cca_vip_t");
      sp.ppstr("acct_type", acctType);
      sp.ppstr("start_date", startDate);
      sp.ppstr("end_date", endDate);
      sp.addsql(", crt_date", ", to_char(sysdate,'yyyymmdd') ");
      sp.ppstr("crt_user", wp.loginUser);
      sp.ppstr("mod_user", wp.loginUser);
      sp.addsql(", mod_time ", ", sysdate ");
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.ppstr("mod_audcode", "U");

      switch (cardIndicator) {
         case "1":
            sp.ppstr("corp_p_seqno", "");
            sp.ppstr("idno_p_seqno", idPSeqno);
            sp.ppstr("acno_p_seqno", acnoPSeqno);
            sp.ppstr("with_sup_card", withSupCard);
            break;

         case "2":
            sp.ppstr("corp_p_seqno", corpPSeqno);
            sp.ppstr("idno_p_seqno", idPSeqno);
            sp.ppstr("acno_p_seqno", acnoPSeqno);
            sp.ppstr("with_sup_card", "");
            break;
      }

      rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

      if (sqlRowNum <= 0) {
         errmsg("更新錯誤");
         return rc;
      }

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

   // 預防使用者在detl畫面先修改可被修改之資料，再按刪除，因此先查詢資料庫，再做後續動作。
   if (wp.itemEq("temp_flag", "Y")) {
      strSql = " select "+" acct_type, "+" corp_p_seqno , "+" idno_p_seqno, "+" with_sup_card, "
          +" start_date, "+" end_date "+" from cca_vip "+" where acct_type =:acct_type "
          +" and corp_p_seqno =:corp_p_seqno "+" and idno_p_seqno =:idno_p_seqno "
      ;

      setString("acct_type", acctType);
      setString("corp_p_seqno", corpPSeqno);
      setString("idno_p_seqno", idPSeqno);

      sqlSelect(strSql);
      if (sqlRowNum <= 0) {
         errmsg("查詢錯誤");
      }
   }


   startDate = colStr("start_date");
   endDate = colStr("end_date");
   withSupCard = colNvl("with_sup_card", "N");

   busi.SqlPrepare sp = new SqlPrepare();

   if (wp.itemEq("temp_flag", "N")) {
      strSql = "delete cca_vip_t where acct_type =:acct_type and corp_p_seqno =:corp_p_seqno "+" and idno_p_seqno =:idno_p_seqno ";
      setString("acct_type", acctType);
      setString("corp_p_seqno", corpPSeqno);
      setString("idno_p_seqno", idPSeqno);

      rc = sqlExec(strSql);
      if (sqlRowNum <= 0) {
         errmsg("刪除錯誤");
      }
   } else if (wp.itemEq("temp_flag", "Y")) {
      sp.sql2Insert("cca_vip_t");
      sp.ppstr("acct_type", acctType);
      sp.ppstr("start_date", startDate);
      sp.ppstr("end_date", endDate);
      sp.addsql(", crt_date", ", to_char(sysdate,'yyyymmdd') ");
      sp.ppstr("crt_user", wp.loginUser);
      sp.ppstr("mod_user", wp.loginUser);
      sp.addsql(", mod_time ", ", sysdate ");
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.ppstr("mod_audcode", "D");

      switch (cardIndicator) {
         case "1":
            sp.ppstr("corp_p_seqno", "");
            sp.ppstr("idno_p_seqno", idPSeqno);
            sp.ppstr("acno_p_seqno", acnoPSeqno);
            sp.ppstr("with_sup_card", withSupCard);
            break;

         case "2":
            sp.ppstr("corp_p_seqno", corpPSeqno);
            sp.ppstr("idno_p_seqno", idPSeqno);
            sp.ppstr("acno_p_seqno", acnoPSeqno);
            sp.ppstr("with_sup_card", "");
            break;
      }

      rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

      if (sqlRowNum <= 0) {
         errmsg("刪除錯誤");
         return rc;
      }
   }

   return rc;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

}
