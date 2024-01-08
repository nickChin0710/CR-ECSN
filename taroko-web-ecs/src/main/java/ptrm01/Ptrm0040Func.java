/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-24  V1.00.00  yash       program initial                            *
* 108-08-08  V1.00.01  Andy       remove col ica_no                          *
* 108-12-30  V1.00.02 JustinWu add new validation - the same card_type should has the same bin_type
* 109-04-20  V1.00.03  Tanwei       updated for project coding standard     *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名 *
* 110-01-28  V1.00.04   JustinWu    fix a bug which does not check update different bin_type
* 110-01-28  V1.00.05   JustinWu    rename and remove some bin_type
* 111-12-08  V1.00.06   Ryan      增加EPAY_TPAN_CODE欄位、檢核邏輯
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0040Func extends FuncEdit {
  String binNo = "", dataKK2 = "",tmp_bin_no_2_fm = "",tmp_bin_no_2_to = "";

  public Ptrm0040Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  //111-12-08  V1.00.06   Ryan      增加EPAY_TPAN_CODE欄位、檢核邏輯
  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      binNo = wp.itemStr("kk_bin_no");
      tmp_bin_no_2_fm = String.format("%-6s", wp.itemStr("kk_bin_no_2_fm")).replace(" ", "0");
      tmp_bin_no_2_to = String.format("%-6s", wp.itemStr("kk_bin_no_2_to")).replace(" ", "9");
      if (binNo.length() < 6) {
        errmsg("BIN NO 有誤");
        return;
      }

    } else {
      binNo = wp.itemStr("bin_no");
      tmp_bin_no_2_fm = wp.itemStr("bin_no_2_fm");
      tmp_bin_no_2_to = wp.itemStr("bin_no_2_to");
    }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
//     String lsSql = "select count(*) as tot_cnt from crd_seqno_log where BIN_NO||SEQNO  like ? ";
//      Object[] param = new Object[] {kk1 + "%"};
//      sqlSelect(lsSql, param);
//      if (colNum("tot_cnt") > 0) {
//        errmsg("此BIN_NO已被使用，無法新增");
//        return;
//      }
      
      String lsSql = "select count(*) as tot_cnt from ptr_bintable where bin_no = ? and bin_no_2_fm <= ? and bin_no_2_to >= ? ";
      Object[] param1 = new Object[] {binNo,tmp_bin_no_2_fm,tmp_bin_no_2_fm};
      sqlSelect(lsSql, param1);
      if (colNum("tot_cnt") > 0) {
        errmsg("此BIN_NO 7~12碼起已被使用，無法新增");
        return;
      }
      
      lsSql = "select count(*) as tot_cnt from ptr_bintable where bin_no = ? and bin_no_2_fm <= ? and bin_no_2_to >= ? ";
      Object[] param2 = new Object[] {binNo,tmp_bin_no_2_to,tmp_bin_no_2_to};
      sqlSelect(lsSql, param2);
      if (colNum("tot_cnt") > 0) {
        errmsg("此BIN_NO 7~12碼迄已被使用，無法新增");
        return;
      }
      
    }

    if (this.ibAdd || this.ibUpdate) {
      boolean validationBinType2cardType = false;
      double rowCnt = -1;
      String dbBinType = "";
      String lsSql = "";
      Object[] param = null;

      //不同的BIN_NO不可設相同的EPAY_TPAN_CODE
	  if (!wp.itemEmpty("epay_tpan_code")) {
		lsSql = "select count(*) as row_cnt from ptr_bintable where bin_no <> ? and epay_tpan_code = ? ";
		param = new Object[] { binNo, wp.itemStr("epay_tpan_code") };
		sqlSelect(lsSql, param);
		rowCnt = colNum("row_cnt");
		if (rowCnt > 0) {
			errmsg("不同「BIN_NO」不可設定相同的「EPAY虛擬卡號對應代碼」，因此無法新增或修改");
			return;
		}
	  }

      
      //相同的BIN_NO不可設不同的EPAY_TPAN_CODE
	  if (!wp.itemEmpty("epay_tpan_code")) {
	    lsSql = "select count(*) as row_cnt from ptr_bintable where bin_no = ? and epay_tpan_code <> ? and bin_no_2_fm <> ? and bin_no_2_to <> ? ";
        param = new Object[] {binNo,wp.itemStr("epay_tpan_code"),tmp_bin_no_2_fm,tmp_bin_no_2_to};
        sqlSelect(lsSql, param);
        rowCnt = colNum("row_cnt");   
        if(rowCnt>0) {
    	  errmsg("相同「BIN_NO」不可設定不同的「EPAY虛擬卡號對應代碼」，因此無法新增或修改");
          return;
        }
	  }


      // 查詢此卡種(card_type)的數量及所對到的bin_type
      lsSql =
          "select count(*) as row_cnt, bin_type as db_bin_type from ptr_bintable where card_type = ? group by bin_type";
      param = new Object[] {wp.itemStr("card_type")};
      sqlSelect(lsSql, param);

      dbBinType = colStr("db_bin_type");
      rowCnt = colNum("row_cnt");

      if (this.ibAdd) {
        // 新增時需進行此檢核
        validationBinType2cardType = true;
      } else if (this.ibUpdate) {
			if (rowCnt > 1) {
				// 還有其他資料有相同的card_type
				validationBinType2cardType = true;
			}
    	  
//        // 執行更新：需先確認是否有更動card_type
//        // 若有，則必須執行與新增同樣的檢核
//        // 若無，則檢查是否資料庫中只有此一筆card_type的資料
//        lsSql = "select card_type as db_card_type from ptr_bintable where bin_no = ? ";
//        param = new Object[] {wp.itemStr("bin_no")};
//        sqlSelect(lsSql, param);
//        dbCardType = colStr("db_card_type");
//        if (dbCardType.equals(wp.itemStr("card_type"))) {
//          // 未修改card_type
//          if (rowCnt > 1) {
//            // 還有其他資料有相同的card_type
//            validationBinType2cardType = true;
//          }
//        }
      }

      // 檢查卡種(card_type)是否對到唯一的國際組織卡別(bin_type)
      if (validationBinType2cardType) {
        if (notEmpty(dbBinType)) {
          if (!dbBinType.equals(wp.itemStr("bin_type"))) {
            // bin_type不同，因此無法新增或修改
            String binTypeDes =
                commString.decode(dbBinType, ",V,M,J,A,N", ",V:visa,M:mastercard,J:jcb,A:ae,N:nccc");
            errmsg("此「卡種」的「國際組織卡別」只能儲存" + binTypeDes + "，因此無法新增或修改");
            return;
          }
        }
      }

    }

    // -other modify-
    if (this.ibUpdate || this.ibDelete) {
      sqlWhere = " where bin_no= ? and bin_no_2_fm = ? and bin_no_2_to = ? ";
      Object[] param = new Object[] {binNo,tmp_bin_no_2_fm,tmp_bin_no_2_to};
      if (this.isOtherModify("ptr_bintable", sqlWhere, param)) {
        errmsg("請重新查詢 !");
        return;
      }
    }

  }

  //111-12-08  V1.00.06   Ryan      增加EPAY_TPAN_CODE欄位、檢核邏輯
  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into ptr_bintable (" + " bin_no, " + " bin_type, "
    // + " ica_no, "
        + " debit_flag, " + " card_desc, " + " dc_curr_code, " + " bin_no_2_fm," + " bin_no_2_to,"
        + " card_type," + " crt_date, " + " crt_user, " + " mod_pgm, " + " mod_seqno" + " ,epay_tpan_code "
        + " ) values (" + " ?,?,?,?,?,?,?,? " + ",to_char(sysdate,'yyyymmdd'),?,?,1,?" + " )";
    // -set ?value-
    Object[] param = new Object[] {binNo // 1
        , wp.itemStr("bin_type")
        // , wp.item_ss("ica_no")
        , !wp.itemStr("debit_flag").equals("Y") ? "N" : wp.itemStr("debit_flag"),
        wp.itemStr("card_desc"), wp.itemStr("dc_curr_code"),
        String.format("%-6s", tmp_bin_no_2_fm).replace(" ", "0"),
        String.format("%-6s", tmp_bin_no_2_to).replace(" ", "9"), wp.itemStr("card_type"),
        wp.loginUser, wp.itemStr("mod_pgm") , wp.itemStr("epay_tpan_code")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  //111-12-08  V1.00.06   Ryan      增加EPAY_TPAN_CODE欄位、檢核邏輯
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "update ptr_bintable set " + " bin_type =?, "
    // + " ica_no =?, "
        + " debit_flag =?, " + " card_desc =?, " + " dc_curr_code =?, " + " bin_no_2_fm =?, "
        + " bin_no_2_to =?, " + " card_type =?, " + " mod_user =?, " + "mod_time=sysdate, "
        + "mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 " + " ,epay_tpan_code =? " + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("bin_type"),
        // wp.item_ss("ica_no"),
        !wp.itemStr("debit_flag").equals("Y") ? "N" : wp.itemStr("debit_flag"),
        wp.itemStr("card_desc"), wp.itemStr("dc_curr_code"),
        String.format("%-6s", tmp_bin_no_2_fm).replace(" ", "0"),
        String.format("%-6s", tmp_bin_no_2_to).replace(" ", "9"), wp.itemStr("card_type"),
        wp.loginUser, wp.itemStr("mod_pgm"), wp.itemStr("epay_tpan_code") ,binNo,tmp_bin_no_2_fm,tmp_bin_no_2_to};
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
    strSql = "delete ptr_bintable " + sqlWhere;
    // ddd("del-sql="+is_sql);
    Object[] param = new Object[] {binNo,tmp_bin_no_2_fm,tmp_bin_no_2_to};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
