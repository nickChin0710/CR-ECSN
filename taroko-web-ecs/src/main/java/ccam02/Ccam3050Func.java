package ccam02;
/**
 * 2019-0919   JH    modify
 * 2020-0420  yanghan   修改了變量名稱和方法名稱
 * 110-01-05  V1.00.03  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改
 * 2023-1219   JH    bank_id可'*',6,8
 * 2023-1222   JH    bank_id, mcht_no 有*, mcc_code須為*
 */

import busi.FuncEdit;

public class Ccam3050Func extends FuncEdit {
String mchtNo = "", bankId = "", mccCode = "", lsMchtName = "", oldMccCode = "";

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

   if (ibAdd) {
      mchtNo = wp.itemStr("kk_mcht_no");
      bankId = wp.itemStr("kk_acq_bank_id");
      mccCode = wp.itemStr("kk_mcc_code");
   } else {
      mchtNo = wp.itemStr("mcht_no");
      bankId = wp.itemStr("acq_bank_id");
      mccCode = wp.itemStr("mcc_code");
   }

   if (empty(mchtNo)) {
      errmsg("特店代號 : 不可空白");
      return;
   }
   if (empty(bankId)) {
      errmsg("收單行 : 不可空白");
      return;
   }
   if (!ibDelete && empty(mccCode)) {
      errmsg("Mcc Code: 不可空白");
      return;
   }
   //-JH:231219--
   if (!eq(bankId,"*") && bankId.length() !=6 && bankId.length() !=8) {
      errmsg("收單行: 只能為 * 或 6碼,8碼");
      return;
   }
   if (eq(bankId,"*") && eq(mchtNo,"*")) {
      errmsg("收單行,特店代號: 不可同為*");
      return;
   }

   if (ibUpdate || ibDelete) {
      oldMccCode = wp.itemStr("old_mcc_code");
   }

//    if (empty(oldMccCode)) {
//      errmsg("MCC CODE : 不可空白");
//      return;
//    }

   if (this.ibAdd || this.ibUpdate) {
      //-JH23-1222-
      if (eq(bankId,"*") || eq(mchtNo,"*")) {
         if (!eq(mccCode,"*")) {
            errmsg("收單行代碼 或是 特店代碼 其中一個有 *，MCC 就必須為 *");
            return;
         }
      }

      if (isEmpty(wp.itemStr("risk_factor"))) {
         wp.itemSet("risk_factor", "0");
      }

      if (wp.itemEmpty("mcht_risk_code")) {
         errmsg("本行風險代碼 : 不可空白 ");
         return;
      }

      if (wp.itemEmpty("risk_start_date") || wp.itemEmpty("risk_end_date")) {
         errmsg("管制期間:不可空白");
         return;
      }

      // if(chk_strend(commDate.sysDate(),wp.item_ss("risk_start_date"))==-1){
      // errmsg("管制期間: 輸入錯誤");
      // return ;
      // }

      if (chkStrend(wp.itemStr("risk_start_date"), wp.itemStr("risk_end_date")) == -1) {
         errmsg("管制期間: 起迄錯誤");
         return;
      }

      lsMchtName = wp.itemStr2("mcht_name");
      if (empty(lsMchtName))
         lsMchtName = selectMchtName();

      if (!eqIgno(mccCode, "*")) {
         if (checkMcc(mccCode) == false) {
            errmsg("MCC CODE 不存在");
            return;
         }
      }

//      if (this.ibAdd) {
//        if (!eqIgno(mccCode, "*")) {
//          if (checkMcc(mccCode) == false) {
//            errmsg("MCC CODE 不存在");
//            return;
//          }
//        }
//      } else {
//        if (!wp.itemEq("mcc_code", "*")) {
//          if (checkMcc(wp.itemStr("mcc_code")) == false) {
//            errmsg("MCC CODE 不存在");
//            return;
//          }
//        }
//      }

      if (wp.itemNum("auth_amt_s") != 0 || wp.itemNum("auth_amt_e") != 0) {
         if (wp.itemNum("auth_amt_s") > wp.itemNum("auth_amt_e")) {
            errmsg("單筆可使用金額區間 : 起迄錯誤");
            return;
         }
      }

      if (this.ibAdd) {
         return;
      }

      if (checkData() == false) {
         errmsg("資料已存在，不可異動");
         return;
      }

   }

   sqlWhere =
       " where mcht_no=?"+" and acq_bank_id=?"+" and mcc_code=?"+" and nvl(mod_seqno,0) =?";

   Object[] parms = new Object[]{mchtNo, bankId, oldMccCode, wp.itemNum("mod_seqno")};
   if (this.isOtherModify("cca_mcht_risk", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
   }
}

String selectMchtName() {

   String sql1 =
       " select mcht_name from cca_mcht_bill where 1=1 and mcht_no = ? and acq_bank_id = ? ";
   sqlSelect(sql1, new Object[]{mchtNo, bankId});
   if (sqlRowNum > 0)
      return colStr("mcht_name");

   return "";
}

boolean checkMcc(String lsMccCode) {

   String sql1 = " select count(*) as db_cnt from cca_mcc_risk where mcc_code = ? ";
   sqlSelect(sql1, new Object[]{lsMccCode});

   if (sqlRowNum < 0 || colNum("db_cnt") <= 0)
      return false;

   return true;
}

boolean checkData() {

   String sql1 = " select count(*) as db_cnt2 from cca_mcht_risk "
       +" where acq_bank_id = ? and mcht_no = ? and mcc_code = ? "+" and rowid <> ? ";

   sqlSelect(sql1, new Object[]{bankId, mchtNo, wp.itemStr("mcc_code"),
       commSqlStr.strToRowid(wp.itemStr("rowid"))});

   if (sqlRowNum < 0 || colNum("db_cnt2") > 0)
      return false;

   return true;
}


@Override
public int dbInsert() {
   actionInit("A");
   dataCheck();
   if (rc != 1)
      return rc;

   strSql = " insert into cca_mcht_risk ("+" risk_factor , "+" acq_bank_id , "+" mcht_no , "
       +" mcc_code , "+" mcht_risk_code , "+" risk_start_date , "+" risk_end_date , "
       +" auth_amt_s , "+" auth_amt_e , "+" day_limit_cnt , "
       +" day_tot_amt , "+" edc_pos_no1 , "+" edc_pos_no2 , "+" edc_pos_no3 ,"
       +" risk_remark , "+" mcht_name , "+" crt_date , "+" crt_user , "+" mod_user , "
       +" mod_pgm , "+" mod_time , "+" mod_seqno "+" ) values ( "+" :risk_factor , "
       +" :kk2 , "+" :kk1 , "+" :kk3 , "+" :mcht_risk_code , "+" :risk_start_date , "
       +" :risk_end_date , "+" :auth_amt_s , "+" :auth_amt_e , "
       +" :day_limit_cnt , "+" :day_tot_amt , "+" :edc_pos_no1 , "+" :edc_pos_no2 , "
       +" :edc_pos_no3 , "+" :risk_remark , "+" :mcht_name , "
       +" to_char(sysdate,'yyyymmdd') , "+" :crt_user , "+" :mod_user , "+" :mod_pgm , "
       +" sysdate , "+" 1 "+" )";

   item2ParmStr("risk_factor");
   setString("kk1", mchtNo);
   setString("kk2", bankId);
   setString("kk3", mccCode);
   item2ParmStr("mcht_risk_code");
   item2ParmStr("risk_start_date");
   item2ParmStr("risk_end_date");
   item2ParmNum("auth_amt_s");
   item2ParmNum("auth_amt_e");
   item2ParmNum("day_limit_cnt");
   item2ParmNum("day_tot_amt");
   item2ParmStr("edc_pos_no1");
   item2ParmStr("edc_pos_no2");
   item2ParmStr("edc_pos_no3");
   item2ParmStr("risk_remark");
   setString("mcht_name", lsMchtName);
   setString("crt_user", wp.loginUser);
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());

   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("資料已存在 不可新增 !");
   }

   return rc;
}

// 修改--> 管理控制
@Override
public int dbUpdate() {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   strSql = " update cca_mcht_risk set "+" risk_factor =:risk_factor , "
       +" mcc_code =:mcc_code , "+" mcht_risk_code =:mcht_risk_code , "
       +" risk_start_date =:risk_start_date , "+" risk_end_date =:risk_end_date , "
       +" auth_amt_s =:auth_amt_s , "+" auth_amt_e =:auth_amt_e , "
       +" day_limit_cnt =:day_limit_cnt , "
       +" day_tot_amt =:day_tot_amt , "+" edc_pos_no1 =:edc_pos_no1 , "
       +" edc_pos_no2 =:edc_pos_no2 , "+" edc_pos_no3 =:edc_pos_no3 , "
       +" risk_remark =:risk_remark , "+" mcht_name =:mcht_name , "+" mod_user =:mod_user , "
       +" mod_pgm =:mod_pgm , "+" mod_time = sysdate , "+" mod_seqno = nvl(mod_seqno,0)+1 "
       +" where acq_bank_id =:kk2 "+" and mcht_no =:kk1 "+" and mcc_code =:kk3 "
       +" and mod_seqno =:mod_seqno ";

   item2ParmStr("risk_factor");
   item2ParmStr("mcc_code");
   item2ParmStr("mcht_risk_code");
   item2ParmStr("risk_start_date");
   item2ParmStr("risk_end_date");
   item2ParmNum("auth_amt_s");
   item2ParmNum("auth_amt_e");
   item2ParmNum("day_limit_cnt");
   item2ParmNum("day_tot_amt");
   item2ParmStr("edc_pos_no1");
   item2ParmStr("edc_pos_no2");
   item2ParmStr("edc_pos_no3");
   item2ParmStr("risk_remark");
   setString("mcht_name", lsMchtName);
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", wp.modPgm());
   setString("kk1", mchtNo);
   setString("kk2", bankId);
   setString("kk3", oldMccCode);
   item2ParmNum("mod_seqno");

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
   }
   return rc;

}

// 刪除 --> 取消管制
@Override
public int dbDelete() {
   actionInit("D");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   strSql = "delete cca_mcht_risk where acq_bank_id =:kk2 and mcht_no =:kk1 and mcc_code =:kk3 ";
   setString("kk1", mchtNo);
   setString("kk2", bankId);
   setString("kk3", oldMccCode);

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
   }

   if (rc == 1) {
      deleteCcaMchtRiskDetl();
   }
   return rc;

}

public int dbInsertDetl() {
   msgOK();

   strSql = "insert into CCA_MCHT_RISK_DETL ("+" mcht_no, " // 1
       +" acq_bank_id, "+" mcc_code , "+" data_type, "+" data_code,"+" data_code2, "
       +" data_code3, "+" data_amt, "+" mod_user, mod_time "+" ) values ("+" :mcht_no, " // 1
       +" :acq_bank_id, "+" :mcc_code , "+" '1', "+" :db_card_no,"+" :auth_date1, "
       +" :auth_date2, "+" :card_tot_amt, "+" :mod_user, sysdate "+" )";
   setString("mcht_no", wp.itemStr("mcht_no"));
   setString("acq_bank_id", wp.itemStr("acq_bank_id"));
   setString("mcc_code", wp.itemStr("mcc_code"));
   var2ParmStr("db_card_no");
   var2ParmStr("auth_date1");
   var2ParmStr("auth_date2");
   var2ParmNum("card_tot_amt");
   setString("mod_user", wp.loginUser);

   this.sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("Insert CCA_MCHT_RISK_DETL error; "+getMsg());
   }
   return rc;
}

void deleteCcaMchtRiskDetl() {
   strSql = "delete cca_mcht_risk_detl where "+" mcht_no =:kk1 and acq_bank_id =:kk2 "
       +" and mcc_code =:kk3 ";
   setString("kk1", mchtNo);
   setString("kk2", bankId);
   setString("kk3", oldMccCode);
   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("delete detl error !");
   } else
      rc = 1;
}

public int dbDeleteCard() {
   msgOK();
   strSql =
       "Delete cca_mcht_risk_detl where data_type ='1' and acq_bank_id =:acq_bank_id and mcht_no =:mcht_no ";
   item2ParmStr("acq_bank_id");
   item2ParmStr("mcht_no");
   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("Delete CCA_MCHT_RISK_DETL err; "+getMsg());
      rc = -1;
   } else
      rc = 1;

   return rc;
}


}
