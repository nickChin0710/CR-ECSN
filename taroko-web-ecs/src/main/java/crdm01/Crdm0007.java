/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-20  V1.00.00  yash       program initial                            *
* 106-09-20  V1.00.00  Andy       program update                             *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/

package crdm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0007 extends BaseEdit {
    String mExMsgType = "",mExMsgValue = "";

    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc = 1;

        strAction = wp.buttonCode;
        if (eqIgno(wp.buttonCode, "X")) {
            /* 轉換顯示畫面 */
            strAction = "new";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "Q")) {
            /* 查詢功能 */
            strAction = "Q";
            queryFunc();
        } else if (eqIgno(wp.buttonCode, "R")) { 
            //-資料讀取- 
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
            /* 瀏覽功能 :skip-page*/
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

    //for query use only
    private boolean getWhereStr() throws Exception {
        wp.whereStr =" where 1=1 ";
        if(empty(wp.itemStr("ex_msg_type")) == false){
            wp.whereStr += " and  msg_type = :msg_type ";
            setString("msg_type", wp.itemStr("ex_msg_type"));
        }
        if(empty(wp.itemStr("ex_msg_value")) == false){
            wp.whereStr += " and  msg_value = :msg_value ";
            setString("msg_value", wp.itemStr("ex_msg_value"));
        }
        return true;
    }

    @Override
    public void queryFunc() throws Exception {
        getWhereStr();
        //-page control-
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();
        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();

        wp.selectSQL = " msg_type"
	              + ", msg_id"
	              + ", msg_value"
	              + ", map_value"
	              + ", msg"
	              + ", crt_date"
	              + ", crt_user"
	              ;

        wp.daoTable = "crd_message";
        wp.whereOrder=" order by msg_value";
        getWhereStr();

        pageQuery();

        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }

        wp.listCount[1] = wp.dataCnt;
        wp.setPageValue();

    }

    @Override
    public void querySelect() throws Exception {
        mExMsgType = wp.itemStr("msg_type");
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {
        mExMsgType = wp.itemStr("kk_msg_type");
        mExMsgValue = wp.itemStr("kk_msg_value");
        if (empty(mExMsgType)){
            mExMsgType = itemKk("data_k1");
        }
        if (empty(mExMsgValue)){
        	mExMsgValue = itemKk("data_k2");
        }
        
        if (empty(mExMsgType)){
            mExMsgType = wp.colStr("msg_type");
        }
        if (empty(mExMsgValue)){
        	mExMsgValue = wp.colStr("msg_value");
        }

        //System.out.println("m_ex_msg_value:"+m_ex_msg_value);
        wp.selectSQL = "hex(rowid) as rowid, mod_seqno "
	              + ", msg_type "
	              + ", msg_id"
	              + ", msg_value"
	              + ", map_value"
	              + ", msg"
	              + ", crt_date"
	              + ", crt_user"
	              ;
        wp.daoTable = "crd_message";
        wp.whereStr = " where 1=1";
        wp.whereStr += " and  msg_type = :msg_type ";
        setString("msg_type", mExMsgType);
        wp.whereStr += " and  msg_value = :msg_value ";
        setString("msg_value", mExMsgValue);
        wp.whereOrder=" order by msg_type,msg_value";
        //System.out.println("SQL:"+wp.selectSQL+wp.daoTable+ wp.whereStr);
        pageSelect();
        if (sqlNotFind()) {
            alertErr("查無資料, msg_type=" + mExMsgType);
        }
    }

    @Override
    public void saveFunc() throws Exception {
    	//-check approve-
		if (!checkApprove(wp.itemStr("approval_user"),
		      wp.itemStr("approval_passwd")))
		   {
			  return;
		   }
        Crdm0007Func func = new Crdm0007Func(wp);

        rc = func.dbSave(strAction);
        log(func.getMsg());
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

    @Override
    public void dddwSelect() {
        try {
            if (wp.respHtml.indexOf("_detl") > 0)
                wp.optionKey = wp.itemStr("kk_msg_type");
            else
                wp.optionKey = wp.itemStr("ex_msg_type");
            this.dddwList("dddw_msg_type", "crd_message", "msg_type", "", "where 1=1 group by msg_type order by msg_type");
        }
        catch(Exception ex) {}
    }

}
