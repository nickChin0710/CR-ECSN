/*  
*  2020-0727  V1.00.03  JustinWu  check if proc_result are all '9'. True: update case_trace_flag and case_trace_date
*  2022-1124  V1.00.04  sunny     配合卡部要求，將「接聽」(客服) 改為「受理」                     *  
 */
package cmsm02;

import busi.FuncProc;

public class Cmsp6010Func extends FuncProc {

  private final String programName = "cmsp6010";

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
    sqlWhere = " where hex(rowid) =:rowid " + " and nvl(mod_seqno,0) =:mod_seqno ";
    var2ParmStr("rowid");
    var2ParmNum("mod_seqno");
    isOtherModify("CMS_CASEDETAIL", sqlWhere);
  }

  @Override
  public int dataProc() {
    dataCheck();
    log("rc_1" + rc);
    if (rc != 1) {
      return rc;
    }
    log("wk_proc:" + varsStr("wk_proc"));
    if (varEq("wk_proc", "1")) {
      strSql = "update CMS_CASEDETAIL set" 
                  + " case_conf_flag ='Y' " 
    		      + ", mod_time = sysdate"
                  + ", mod_user =:mod_user " 
    		      + ", mod_pgm =:mod_pgm " 
                  + ", mod_seqno =mod_seqno+1 "
    		      + " where hex(rowid)=:rowid" 
                  + " and nvl(mod_seqno,0)=:mod_seqno";
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", programName);
      var2ParmStr("rowid");
      var2ParmNum("mod_seqno");
      
      log("rowid:" + varsStr("rowid") + " mod_seqno" + varsStr("mod_seqno") + " wk_proc:"
          + varsStr("wk_proc"));
      
      sqlExec(strSql);

      if (sqlRowNum <= 0) {
        errmsg("update CMS_CASEDETAIL error; " + this.getMsg());
      }
    } else if (varEq("wk_proc", "0")) {

      strSql = "update CMS_CASEDETAIL set" 
          + " case_conf_flag ='R' " 
    	  + ", proc_result ='9' "
          + ", proc_desc = '退回受理人員處理' " 
    	  + ", finish_date = to_char(sysdate,'yyyymmdd') "
          + ", apr_flag = 'Y'" 
    	  + ", apr_user =:apr_user"
          + ", apr_date = to_char(sysdate,'yyyymmdd')" 
    	  + ", mod_time = sysdate"
          + ", mod_user =:mod_user " 
    	  + ", mod_pgm =:mod_pgm " 
          + ", mod_seqno =mod_seqno+1 "
          + " where hex(rowid)=:rowid" 
          + " and nvl(mod_seqno,0)=:mod_seqno";
      
      setString("mod_user", wp.loginUser);
      setString("apr_user", wp.loginUser);
      setString("mod_pgm", programName);
      var2ParmStr("rowid");
      var2ParmNum("mod_seqno");

      sqlExec(strSql);

      if (sqlRowNum <= 0) {
        errmsg("update CMS_CASETYPE error; " + this.getMsg());
        return rc;
      }
      
      selectMinProcResult();
       
      if(sqlRowNum > 0 && colInt("minProcResult") == 9) {
    	  boolean isUpdateSuccess = updateCmsCasemasterAsComplete();
    	  if( ! isUpdateSuccess) {
    		  errmsg("update cms_casemaster 失敗 " );
    	      return rc;
    	  }
       }
      
    }
    log("rc_2" + rc);
    return rc;
  }

  /**
   * cms_CaseDetail均已結案，故更新cms_casemaster的case_result為9。
   * 且CASE_TRACE_FLAG設定為Y、CASE_TRACE_DATE為執行當下的系統日期+3天
   */
private boolean updateCmsCasemasterAsComplete() {
    strSql = "update cms_casemaster set "
            + " case_result = '9' , "
            + " finish_date = to_char(sysdate,'yyyymmdd') , "
            + " CASE_TRACE_FLAG= 'Y', "
            + " CASE_TRACE_DATE= to_char(sysdate + 3 days,'yyyymmdd'), "
            + " mod_user = :mod_user , "
            + " mod_pgm = :mod_pgm , "
            + " mod_time = sysdate , "
            + " mod_seqno = mod_seqno+1 "
            + " where case_date =:case_date "
            + " and case_seqno =:case_seqno  "
            ;

      setString("mod_user", wp.loginUser);
      setString("mod_pgm", programName);
      setString("case_date", varsStr("caseDate"));
      setString("case_seqno", varsStr("caseSeqno"));
	
      sqlExec(strSql);
      
      return sqlRowNum > 0 ;
      
}

private void selectMinProcResult() {
	strSql = " select MIN(proc_result) as minProcResult "
      		      + " from cms_casedetail "
      		      + " where case_date = :case_date "
      		      + " and case_seqno = :case_seqno "; 
    		
      setString("case_date", varsStr("caseDate"));
      setString("case_seqno", varsStr("caseSeqno"));
      
      sqlSelect(strSql);
}

}
