package ccam01;
import java.io.FileInputStream;
import java.net.ConnectException;
import java.util.Properties;

import com.tcb.ap4.tool.Decryptor;

import bank.Auth.HpeUtil;
import bank.Auth.HSM.HsmUtil;
import busi.FuncAction;
import taroko.com.TarokoParm;

public class Ccam2082Func extends FuncAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  String cardNo = "", kk2 = "";
  String hspAddr1 = "" , hspAddr2 = "" ,hspAddr3 = "" ;
  int hsmPort1 = 0 , hsmPort2 = 0 , hsmPort3 = 0 , respStatus = 0 ;
  String jaMsg = "", jaOutPut = "" ,ngMsg ="", ngOutPut = "" ,dgMsg = "", dgOutPut = "" , pvvHide = "";
  String visaPvka = "" , visaPvkb = "" , masterPvka = "" , masterPvkb = "" , jcbPvka = "" , jcbPvkb = "" ;
  String pvkA = "" , pvkB = "" , smsIP = "" , smsUser = "" , msgDesc = "" , msgDescLog = "";
  String procCode = "" , msgSeqNo = "" , msgId = "" , hsmCardNo = "";  
  
  
  @Override
  public void dataCheck() {
    cardNo = wp.itemStr("card_no");    

    selectCrdCard();
    if (rc != 1)
      return;
    hsmCardNo = cardNo.substring(3, 15);
    if (colNeq("A.current_code", "0")) {
      errmsg("該卡已掛失, 不可重新產生密碼!");
      return;
    }

  }

  void selectCrdCard() {
    strSql = "select current_code, sup_flag, uf_nvl(combo_indicator,'N') as combo_indicator, activate_flag "
            + ", activate_date, new_beg_date, new_end_date, old_beg_date, old_end_date" + ", bin_type"
            + ", mod_seqno , pvki , bin_type " 
            + " from crd_card" + " where card_no =?";
    setString2(1, cardNo);
    daoTid = "A.";
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      sqlErr("crd_card.Select");
      return;
    }
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    
    try {
    	callHSM();
    }	catch (Exception ex) {
    	return rc;
    }
    
    if (rc != 1) {
    	return rc;
    }
    
    updateCcaCardBase();
    if(rc!=1)
    	return rc;
    
    updateCrdCard();    
    if(rc!=1)
    	return rc;
        
    
    //--送簡訊
    sendSms();
    if(rc!=1)
    	return rc;
    
    insertCcaMsgLog();
    
    return rc;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

   public int updateCrdCard() {
       msgOK();
	   strSql = " update crd_card set "
//			  + " old_pvv = pvv , "
//			  + " pvv = :hide_pvv , "
//			  + " pin_block =:pin_block , "
			  + " passwd_err_count = 0 , "
			  + " passwd_err_count_resetdate = to_char(sysdate,'yyyymmdd') , "
			  + " apply_atm_flag = 'Y' , "
			  + " mod_user =:mod_user , "
			  + " mod_pgm =:mod_pgm ,"
			  + " mod_time = sysdate , "
			  + " mod_seqno = nvl(mod_seqno,0)+1 "
			  + " where card_no =:card_no "
			  ;
	   
//	   setString("hide_pvv",pvvHide);
//	   setString("pin_block",jaOutPut);
	   setString("mod_user",wp.loginUser);
	   setString("mod_pgm",wp.modPgm());
	   setString("card_no",cardNo);	   
	   
	   sqlExec(strSql);
	   if(sqlRowNum <=0) {
		   errmsg("update crd_card error");		   
	   }
	   
       return rc;
   }
   
   public int updateCcaCardBase() {
	   msgOK();
	   
	   strSql = " update cca_card_base set "
	   		  + " old_pin = pin , "
	   		  + " old_pvv = pvv , "
	   		  + " pin =:pin , "
	   		  + " pvv =:pvv , "
	   		  + " mod_time = sysdate , "
	   		  + " mod_user =:mod_user , "
	   		  + " mod_pgm =:mod_pgm , "
	   		  + " mod_seqno = nvl(mod_seqno,0)+1 "
	   		  + " where card_no =:card_no "			   
			  ;
	   
	   setString("pin",jaOutPut);
	   setString("pvv",pvvHide);
	   setString("mod_user",wp.loginUser);
	   setString("mod_pgm",wp.modPgm());
	   setString("card_no",cardNo);
	   
	   sqlExec(strSql);
	   if(sqlRowNum <=0)
		   errmsg("update cca_card_base error");
	   
	   return rc;
   }
   
   void callHSM() throws Exception {
	   getHSMIp();
	   if(rc!=1)
		   return ;
	   
	   
	   
	   //--call JA 產生 PIN OFFSET
	   String dbSwitch2Dr = TarokoParm.getInstance().getDbSwitch2Dr();
	   if("Y".equals(dbSwitch2Dr)) {
		   HsmUtil hsmCy = new HsmUtil(hspAddr3, hsmPort3);
		   try {
			   jaOutPut = hsmCy.hsmCommandJA(hsmCardNo, "04");
		   } catch (ConnectException ex1) {
				errmsg("hsmCommand connect err");
				return;
		   }		   
	   }	else	{
		   HsmUtil hsmCy = new HsmUtil(hspAddr1, hsmPort1);
		   try {
			   jaOutPut = hsmCy.hsmCommandJA(hsmCardNo, "04");
		   } catch (ConnectException ex1) {
			   hsmCy = new HsmUtil(hspAddr2, hsmPort2);
			   try {
				   jaOutPut = hsmCy.hsmCommandJA(hsmCardNo, "04");
			   } catch (ConnectException ex2) {
				   errmsg("hsmCommand connect err");
				   return;   
			   }				
		   }	
	   }
	   
	   if(jaOutPut.length() <2) {
		   errmsg("call HSM JA error !");		   
		   return ;
	   }	else	{
		   jaMsg = jaOutPut.substring(0, 2);
		   if("00".equals(jaMsg) == false) {
			   errmsg("call HSM JA error !");			   
			   return ;
		   }
		   jaOutPut = jaOutPut.substring(2);
	   }
	   
	   //--call NG 產生 PIN
	   if("Y".equals(dbSwitch2Dr)) {
		   HsmUtil hsmCy = new HsmUtil(hspAddr3, hsmPort3);
		   try {
			   ngOutPut = hsmCy.hsmCommandNG(hsmCardNo, jaOutPut);
		   } catch (ConnectException ex1) {
				errmsg("hsmCommand connect err");
				return;
		   }		   
	   }	else	{
		   HsmUtil hsmCy = new HsmUtil(hspAddr1, hsmPort1);
		   try {
			   ngOutPut = hsmCy.hsmCommandNG(hsmCardNo, jaOutPut);
		   } catch (ConnectException ex1) {
			   hsmCy = new HsmUtil(hspAddr2, hsmPort2);
			   try {
				   ngOutPut = hsmCy.hsmCommandNG(hsmCardNo, jaOutPut);
			   } catch (ConnectException ex2) {
				   errmsg("hsmCommand connect err");
				   return;   
			   }				
		   }	
	   }
	   
	   if(ngOutPut.length() <2) {
		   errmsg("call HSM NG error !");		   
		   return ;
	   }	else	{
		   ngMsg = ngOutPut.substring(0, 2);
		   if("00".equals(ngMsg) == false) {
			   errmsg("call HSM NG error !");			   
			   return ;
		   }
		   ngOutPut = ngOutPut.substring(2,6);
	   }
	   
	   //--call DG
	   if("Y".equals(dbSwitch2Dr)) {
		   HsmUtil hsmCy = new HsmUtil(hspAddr3, hsmPort3);
		   try {
			   dgOutPut = hsmCy.hsmCommandDG(pvkA+pvkB,jaOutPut,hsmCardNo, colStr("A.pvki"));
		   } catch (ConnectException ex1) {
				errmsg("hsmCommand connect err");
				return;
		   }		   
	   }	else	{
		   HsmUtil hsmCy = new HsmUtil(hspAddr1, hsmPort1);
		   try {
			   dgOutPut = hsmCy.hsmCommandDG(pvkA+pvkB,jaOutPut,hsmCardNo, colStr("A.pvki"));
		   } catch (ConnectException ex1) {
			   hsmCy = new HsmUtil(hspAddr2, hsmPort2);
			   try {
				   dgOutPut = hsmCy.hsmCommandDG(pvkA+pvkB,jaOutPut,hsmCardNo, colStr("A.pvki"));
			   } catch (ConnectException ex2) {
				   errmsg("hsmCommand connect err");
				   return;   
			   }				
		   }	
	   }
	   
	   if(dgOutPut.length() <2) {
		   errmsg("call HSM DG error !");		   
		   return ;
	   }	else	{
		   dgMsg = dgOutPut.substring(0, 2);
		   if("00".equals(dgMsg) == false) {
			   errmsg("call HSM DG error !");			   
			   return ;
		   }
		   dgOutPut = dgOutPut.substring(2);
	   }	   	  
	   
	   //--加密
	   pvvHide = HpeUtil.transPasswd(0, dgOutPut);
	   
   }
   
   void getHSMIp(){
	   
	   String sql1 = " select hsm_ip_addr1 , hsm_port1 , hsm_ip_addr2 , hsm_port2 , hsm_ip_addr3 , hsm_port3 , "
			   + " visa_pvka , visa_pvkb , master_pvka , master_pvkb , jcb_pvka , jcb_pvkb "
			   + " from ptr_hsm_keys where hsm_keys_org = '00000000' ";
	   
	   sqlSelect(sql1);
	   if(sqlRowNum <=0) {
		   errmsg("查無 HSM 位置 ");
		   return ;
	   }
	   hspAddr1 = colStr("hsm_ip_addr1"); 
	   hspAddr2 = colStr("hsm_ip_addr2"); 
	   hspAddr3 = colStr("hsm_ip_addr3"); 
	   hsmPort1 = colInt("hsm_port1"); 
	   hsmPort2 = colInt("hsm_port2"); 
	   hsmPort3 = colInt("hsm_port3"); 
	   visaPvka = colStr("visa_pvka");
	   visaPvkb = colStr("visa_pvkb");
	   masterPvka = colStr("master_pvka");
	   masterPvkb = colStr("master_pvkb");
	   jcbPvka = colStr("jcb_pvka");
	   jcbPvkb = colStr("jcb_pvkb");
	   
	   switch (colStr("A.bin_type")) {	   
	   case "V":		   
		   pvkA = visaPvka;
		   pvkB = visaPvkb;
		   break;
	   case "M":
		   pvkA = masterPvka;
		   pvkB = masterPvkb;
		   break;
	   case "J":
		   pvkA = jcbPvka;
		   pvkB = jcbPvkb;
		   break;
	   }
	   
   }
   
   void sendSms() {
	   String confPath = wp.getEcsAcdpPath();
	   Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(confPath);) {
			props.load(fis);
			fis.close();
		} catch (Exception e) {
			errmsg("sms load properties error ");
			return ;
		}
		String pawd = props.getProperty("cr.sms").trim();
		//--解密
		Decryptor decrptor = new Decryptor();
		try {
			pawd = decrptor.doDecrypt(pawd);
		} catch(Exception e) {
			errmsg("pawd error");
			return ;
		}	   
		//--取得 User、IP
		selectPtrSysParm();
		
		//--取得簡訊內容
		getSmsContent();
		
		//--發送簡訊
		smsm01.SmsSend24 sms = new smsm01.SmsSend24();
		try {
			sms.setName(wp.itemStr("chi_name"));
			sms.setPhoneNumber(wp.itemStr("cellar_phone"));
			sms.setSmsBody(msgDesc);
			sms.setUserName(smsUser);
			sms.setUserPd(pawd);
			sms.setUrl(smsIP);
			if (sms.sendSms() ==-1) {
				errmsg("發送簡訊失敗");
				return ;
			}	else	{
				msgSeqNo = sms.getMsgSeqNo();
				respStatus = sms.getReptStatus();
			}
		} catch(Exception e) {
			errmsg("發送簡訊失敗");
			return ;
		}
		
   }
   
   void selectPtrSysParm() {
		
		String sql1 = " select wf_value , wf_value3 ";
		sql1 += " from ptr_sys_parm ";
		sql1 += " where 1=1 and wf_parm = 'SMS_CONNECT' and wf_key = 'SMS_URL' ";
		sqlSelect(sql1);
		if(sqlRowNum <=0) {
			errmsg("get sms parm error");
			return ;
		}
		
		smsIP = colStr("wf_value");		
		smsUser = colStr("wf_value3");
   }
   
   void getSmsContent() {
	   
	   String sql1 = "select msg_desc , msg_id from sms_msg_id where msg_pgm = 'CCAM2082' ";
	   sqlSelect(sql1);
	   if(sqlRowNum <=0) {
		   errmsg("查無簡訊參數 : CCAM2082");
		   return ;
	   }
	   msgId = colStr("msg_id");
	   msgDesc = colStr("msg_desc");
	   msgDescLog = msgDesc ;
	   
	   //--替換變數
	   msgDesc = msgDesc.replace("<#0>", ngOutPut);
	   msgDesc = msgDesc.replace("<#1>", cardNo.substring(cardNo.length()-4, cardNo.length()));
	   msgDescLog = msgDescLog.replace("<#1>", cardNo.substring(cardNo.length()-4, cardNo.length()));
   }
   
   void insertCcaMsgLog() {
	   msgOK();
	   
	   strSql = "insert into cca_msg_log (tx_date , tx_time , card_no , card_acct_idx , acno_p_seqno , id_p_seqno , "
			  + "msg_type , birthday , chi_name , cellar_phone , send_date , proc_code , appr_pwd , sms_content , crt_date , mod_pgm , mod_time , msg_id , crt_user , tel_no_h , tel_no_o ) "
			  + " values (to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss'),:card_no ,:card_acct_idx , :acno_p_seqno , :id_p_seqno , "
			  + "'CASH' , :birthday , :chi_name , :cellar_phone , to_char(sysdate,'yyyymmdd') , '0' , :appr_pwd , :sms_content , "
			  + "to_char(sysdate,'yyyymmdd') ,:mod_pgm , sysdate , :msg_id , :crt_user , :tel_no_h , :tel_no_o )"
			  ;
	   
	   setString("card_no",cardNo);
	   setNumber("card_acct_idx",wp.itemNum("card_acct_idx"));
	   setString("acno_p_seqno",wp.itemStr("acno_p_seqno"));
	   setString("id_p_seqno",wp.itemStr("id_p_seqno"));
	   setString("birthday",wp.itemStr("birthday"));
	   setString("chi_name",wp.itemStr("chi_name"));
	   setString("cellar_phone",wp.itemStr("cellar_phone"));
	   setString("appr_pwd",msgSeqNo);
	   setString("sms_content",msgDescLog);
	   setString("mod_pgm",wp.modPgm());
	   setString("msg_id",msgId);
	   setString("crt_user",wp.loginUser);
	   setString("tel_no_h",wp.itemStr("tel_home"));
	   setString("tel_no_o",wp.itemStr("tel_offi"));
	   
	   sqlExec(strSql);
	   if(sqlRowNum <= 0 ) {
		   errmsg("insert cca_msg_log error ");
		   return ;
	   }
	   
   }
   
}
