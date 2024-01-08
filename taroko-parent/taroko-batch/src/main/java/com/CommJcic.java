package com;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.Locale;

import hdata.jcic.JcicEnum;
import hdata.jcic.LRPad;

public class CommJcic extends AccessDAO{
	public  static final String TARGET_CHARSET = "MS950";
	public  static final String JCIC_BANK_NO = "006";
	public  static final String TAIL_LAST_MARK = "TRLR";
	private String contactTel = null;
	private String contactMsg = null;
	
	public CommJcic(Connection conn[], String[] dbAlias) throws Exception {
		super.conn = conn;
		setDBalias(dbAlias);
		setSubParm(dbAlias);
	}
	
	public boolean selectContactData(JcicEnum jcicType) throws Exception {
		boolean isSelectOk = false;
        sqlCmd = "select wf_value as jcicTel, wf_value2 as jcicMsg ";
        sqlCmd += "from ptr_sys_parm  ";
        sqlCmd += "where wf_parm = 'JCIC_FILE' and wf_key  = ? ";
        setString(1, jcicType.getJcicType());
        int recordCnt = selectTable();
        if (notFound.equals("Y")==false && recordCnt > 0) {
        	setContactTel(getValue("jcicTel"));
        	setContactMsg(getValue("jcicMsg"));
        	isSelectOk = true;
        }else {
        	setContactTel("");
        	setContactMsg("");
        }
        return isSelectOk;
    }
	
	/**
	 * 取得填充或縮減的字串
	 * @param string
	 * @return 填充或縮減的字串
	 * @throws Exception
	 */
	public String getPadString(String string, int byteLength) throws Exception {
		return getPadString(string, " ", byteLength, LRPad.R);
	}
	
	/**
	 * 取得填充或縮減的字串
	 * @param string
	 * @return 填充或縮減的字串
	 * @throws Exception
	 */
	public String getPadString(String string, String filler, int byteLength, LRPad leftOrRight) throws Exception {
		byte[] strByte = string.getBytes(TARGET_CHARSET);
		if (strByte.length > byteLength) {
			strByte = subByteArr(strByte, byteLength);
		}else if (strByte.length < byteLength){
			strByte = fillByteArr(strByte, byteLength, filler, leftOrRight);
		}
		return new String(strByte, TARGET_CHARSET);
	}
	
	/**
	 * 取得填充或縮減的字串
	 * @param integer
	 * @return 填充或縮減的字串
	 * @throws Exception
	 */
	public String getPadString(int integer, String filler, int byteLength, LRPad leftOrRight) throws Exception {
		return getPadString(Integer.toString(integer), filler, byteLength, leftOrRight);
	}
	
	/**
	 * 取得填充或縮減的字串
	 * @param longVal
	 * @return 填充或縮減的字串
	 * @throws Exception
	 */
	public String getPadString(long longVal, String filler, int byteLength, LRPad leftOrRight) throws Exception {
		return getPadString(Long.toString(longVal), filler, byteLength, leftOrRight);
	}
	
	/**
	 * 取得填充或縮減的字串
	 * @param string
	 * @return 填充或縮減的字串
	 * @throws Exception
	 */
	public String getFiller(String filler, int byteLength) throws Exception {
		return getPadString( " " , filler, byteLength, LRPad.L);
	}
	
	/**
	 * 將strByte切至指定長度
	 * @param strByte 從DB串出的byte array
	 * @param byteLength 產出的byte長度
	 * @return
	 */
	private byte[] subByteArr(byte[] strByte, int byteLength) {
		byte[] tempByte = new byte[byteLength];
		for (int j = 0; j < byteLength; j++) {
			tempByte[j] = strByte[j];
		}
		return tempByte;
	}
	
	/**
	 * 將strByte填充至指定長度
	 * @param strByte 從DB串出的byte array
	 * @param byteLength 產出的byte長度
	 * @param filler 填充物
	 * @param leftOrRight 向左或右補filler
	 * @return 填充後的byteArr
	 * @throws UnsupportedEncodingException
	 */
	private byte[] fillByteArr(byte[] strByte, int byteLength, String filler, LRPad leftOrRight) throws UnsupportedEncodingException {
		byte[] fillerByteArr = null;
		try {
			fillerByteArr = filler.getBytes(TARGET_CHARSET);
		} catch (UnsupportedEncodingException e) {
			try {
				showLogMessage("I", "", String.format("Unable to convert %s into byteArr", filler));;
				fillerByteArr = " ".getBytes(TARGET_CHARSET);
			} catch (UnsupportedEncodingException e1) {
				throw e;
			}
		}
		
		byte[] tempByteArr = new byte[byteLength];
		switch(leftOrRight) {
		case L:
			for (int i = 0; i < byteLength - strByte.length; i++) {
				tempByteArr[i] = fillerByteArr[ i % (fillerByteArr.length)];
			}
			for (int i = (byteLength - strByte.length), j = 0; i < tempByteArr.length; i++, j++) {
				tempByteArr[i] = strByte[j];
			}
			break;
		case R:
			for (int i = 0; i < strByte.length; i++) {
				tempByteArr[i] = strByte[i];
			}
			for (int i =  strByte.length; i < tempByteArr.length; i++) {
				tempByteArr[i] = fillerByteArr[ i % (fillerByteArr.length) ];
			}
			break;
		}
		return tempByteArr;
	}

	public String getContactTel() {
		return contactTel;
	}

	public void setContactTel(String contactTel) {
		this.contactTel = contactTel;
	}

	public String getContactMsg() {
		return contactMsg;
	}

	public void setContactMsg(String contactMsg) {
		this.contactMsg = contactMsg;
	}
	

}
