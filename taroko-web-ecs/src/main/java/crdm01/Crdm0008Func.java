/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-18  V1.00.00  David FU   program initial                            *
* 108-12-13  V1.00.01  Andy Liu   add col default_place                      *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
* 112-08-09  V1.00.03  Ryan       修正insert bug                              *
******************************************************************************/

package crdm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Crdm0008Func extends FuncEdit {
    String mKkCardItem = "";  

    public Crdm0008Func(TarokoCommon wr) {
        wp = wr;
        this.conn = wp.getConn();
    }

    @Override
    public int querySelect() {
        // TODO Auto-generated method 
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
            mKkCardItem = wp.itemStr("kk_card_item");
        }else{
            mKkCardItem = wp.itemStr("card_item");
        }
        if (this.isAdd())
        {
            //檢查新增資料是否重複
            String lsSql = "select count(*) as tot_cnt from crd_card_item where card_item = ?";
            Object[] param = new Object[] { mKkCardItem };
            sqlSelect(lsSql, param);
            if (colNum("tot_cnt") > 0)
            {
                errmsg("資料已存在，無法新增");
            }
            return;
        }
        else
        {
            //-other modify-
            sqlWhere = " where card_item = ?  and nvl(mod_seqno,0) = ?";
            Object[] param = new Object[] { mKkCardItem, wp.modSeqno() };
            isOtherModify("crd_card_item", sqlWhere, param);
        }
    }

    @Override
    public int dbInsert() {
        actionInit("A");
        dataCheck();
        if (rc != 1){
            return rc;
        }
        strSql = "insert into crd_card_item ("
	            + " card_item "
	            + ", name"
//                + ", qty_add_percent"
//                + ", safe_qty"
                + ", default_place "
	            + ", crt_date, crt_user "
	            + ", apr_date, apr_user "
	            + ", mod_time, mod_user, mod_pgm, mod_seqno"
	            + " ) values ("
	            + " ?, ?, ? "
	            + ", to_char(sysdate,'yyyymmdd'), ?"
	            + ", to_char(sysdate,'yyyymmdd'), ?"
	            + ", sysdate, ?, ?, 1"  
	            + " )";
        //-set ?value-
        Object[] param = new Object[] { 
              mKkCardItem // 1
            , wp.itemStr("name")
//            , empty(wp.itemStr("qty_add_percent")) ? 0 : wp.itemStr("qty_add_percent")
//            , empty(wp.itemStr("safe_qty")) ? 0 : wp.itemStr("safe_qty")
            , wp.itemStr("default_place")
            , wp.loginUser
            , wp.itemStr("apr_user")
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
        if(rc != 1){
            return rc;
        }

        strSql = "update crd_card_item set "
        		+ " name=?"
//                + ", qty_add_percent=?"
//                + ", safe_qty=? "
                + ", default_place=? "
                + " , apr_date=to_char(sysdate,'yyyymmdd'), apr_user=? "
	            + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
	            + " , mod_seqno =nvl(mod_seqno,0)+1 "
	            + sqlWhere;
        Object[] param = new Object[] { 
            wp.itemStr("name")
//            , empty(wp.itemStr("qty_add_percent")) ? 0 : wp.itemStr("qty_add_percent")
//            , empty(wp.itemStr("safe_qty")) ? 0 : wp.itemStr("safe_qty")
            , wp.itemStr("default_place")
            , wp.itemStr("apr_user")
            , wp.loginUser
            , wp.itemStr("mod_pgm") 
            , mKkCardItem, wp.modSeqno()
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
        if(rc != 1){
            return rc;
        }
        strSql = "delete crd_card_item " + sqlWhere;
        Object[] param = new Object[] { mKkCardItem, wp.modSeqno() };
        rc = sqlExec(strSql, param);
        if (sqlRowNum <= 0) {
	        errmsg(this.sqlErrtext);
        }
        return rc;
    }

}
