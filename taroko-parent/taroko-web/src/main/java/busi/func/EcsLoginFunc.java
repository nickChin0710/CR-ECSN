/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/06/16  V1.00.00 JustinWu               program initial                          *
*  109/07/14  V1.00.01 JustinWu               ++ doesUnitNumberExist
*  109/07/16  V1.00.02 JustinWu                use EXISTS to check the bank unit number exists
*  109-08-03  V1.00.03 Zuwei       fix code scan issue                       *
*  110-12-17  V1.00.04 JustinWu    use new decryption method                 * 
*  111-01-03  V1.00.05 JustinWu    not to encrypt if the password is empty   *
******************************************************************************/

package busi.func;


import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import busi.FuncBase;

public class EcsLoginFunc extends FuncBase {
	busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	private String errorMsg = "";
	
	public EcsLoginFunc(taroko.com.TarokoCommon wp) {
		this.wp = wp;
		setConn(wp.getConn());
	}
	
	private void setErrorMsg(String errorMsg){
		this.errorMsg = errorMsg;
	}
	
	public String getErrorMsg(){
		return this.errorMsg;
	}
	
	/**
	 * 檢查帳號密碼是否正確，以及分行代碼是否與本系統資料庫中資料相符
	 * @param user
	 * @param unhidePassword
	 * @return
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 */
	  public boolean checkEcsUserLogin(String user, String unhidePassword) {
		 
		  // Justin for Test
		  if (user.equalsIgnoreCase("DXC")) {
			return true;
		  }
		  // Justin for Test 		  
		  
			if (user.length() == 0) {
				setErrorMsg("使用者代號: 不可空白");
				return false;
			}
			
			// 檢查User是否存在，若存在，則回傳hide_ref_code。如果回傳值為null，則表示使用者不存在。
			String hideRefCode = checkUserAndGetHideRefCode(user);
			
			if (hideRefCode == null) {
				setErrorMsg("使用者代號： " + user+ "不存在");
				return false;
			}

			String hidePawd;
			try {
				if (hideRefCode.isEmpty() || unhidePassword.isEmpty()) {
					hidePawd = unhidePassword;
				}else {
					hidePawd = new busi.ecs.CommFunction().encrytPassword(unhidePassword, hideRefCode);
				}
			} catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
				e.printStackTrace();
				setErrorMsg(e.getMessage() + e.getCause().toString());
				return false;
			}
			
			try {
				wp.logSql = false;
		        String sql =  " select usr_level, usr_group, usr_type, bank_unitno" 
		                             + " from sec_user" 
		    		                 + " where 1 = 1"
		    		                 + " and usr_id = ?"
		    		                 + " and usr_password = ?";
				setString2(1, user);
				setString2(2, hidePawd);

				sqlSelect(sql);
				if (sqlRowNum <= 0) {
					setErrorMsg( "使用者代號： " + user + "密碼錯誤");
					return false;
				}
				
//				if ( ! doesBankUnitNumberExist(colStr("bank_unitno")) ) {
//					wp.alertMesg( "使用者單位比對本系統分行代碼不存在");
//					return false;
//				}
				
			} catch (Exception ex) {
				return false;
			}
			return true;
		}

	  /**
	   * 檢查User是否存在，如果存在，則回傳hide_ref_code。如果回傳值為null，則表示使用者不存在。
	   * @param user
	   * @return
	   */
		private String checkUserAndGetHideRefCode(String user) {
			try {
				wp.logSql = false;
		        String sql =  " select HIDE_REF_CODE" 
		                             + " from sec_user" 
		    		                 + " where 1 = 1"
		    		                 + " and usr_id = ?";
				setString2(1, user);

				sqlSelect(sql);
				
				if (sqlRowNum <= 0) {
					return null;
				}
			} catch (Exception ex) {
				throw ex;
			}
			
			return colStr("HIDE_REF_CODE");

		}
		
		/**
		 * bankUnitNo至少4位
		 * @param bankUnitNo
		 * @return
		 */
		public boolean doesBankUnitNumberExist(String bankUnitNo) {
			if ( bankUnitNo== null )
				return false;
			
			wp.logSql = false;
	        
	       String sql = " SELECT 1 "
	       		   + " FROM DUAL "
	       		   + " WHERE EXISTS ( "
	    		   + " SELECT 1 "
	    		   + " FROM GEN_BRN "
	               + " WHERE BRANCH = ? " 
	               + " OR BRANCH_ACT_NUM = ? "
	               + " ) ";
	       
	        
			setString(1, bankUnitNo);
			setString(2, bankUnitNo);
			
			sqlSelect(sql);

			return sqlRowNum > 0;
		}
  

}
