/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/06/17   V1.00.01  Zuwei  fix    coding scan issue                      *
*  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
*  109/08/05  V1.00.00    Zuwei     fix code scan issue                   *
*  109/09/03  V1.00.04  Zuwei  fix code scan issue "Weak Encryption: Inadequate RSA Padding" & "Weak Encryption: Insecure Mode of Operation"    *
*                                                                            *
*****************************************************************************/
package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class IchEncrypt extends AccessDAO {
  CommCrd comc = new CommCrd();
  byte[] ivParameter = "1A22C76912B24EE6".getBytes();
  IvParameterSpec iv = new IvParameterSpec(ivParameter);
  private byte[] sessionKey = null;
  private String publickeyPath = comc.getECSHOME() + "/conf/AES_PUBLIC_KEY.pem";
  private String privatekeyPath = comc.getECSHOME() + "/conf/AES_PRIVATE_KEY.pem";

  /*******************************************************************************/
  public byte[] aesEncrypt(byte[] sSrc) throws Exception {
//    byte[] ivParameter = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    Security.addProvider(new BouncyCastleProvider());
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
    // Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    keyGen.init(128, new SecureRandom());
    SecretKey secretKey = keyGen.generateKey();
    sessionKey = secretKey.getEncoded();
//    IvParameterSpec iv = new IvParameterSpec(ivParameter);// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
    byte[] encrypted = cipher.doFinal(sSrc);
    return encrypted;
  }

  /*******************************************************************************/
  public byte[] aesDecrypt(byte[] encryptedData, byte[] keyBytes) {
    byte[] encryptedText = null;
//    byte[] ivParameter = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//    IvParameterSpec iv = new IvParameterSpec(ivParameter);

    Key key = new SecretKeySpec(keyBytes, "AES");
    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, key, iv);
      encryptedText = cipher.doFinal(encryptedData);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return encryptedText;
  }

  /*******************************************************************************/
  private PublicKey getPublicKey() throws Exception {
    String base64PublicKey = "";
    // 2020_0615 resolve Unreleased Resource: Streams by yanghan
    try (FileInputStream fis = new FileInputStream(new File(publickeyPath));
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, "MS950"))) {
      String buffer = "";
      while ((buffer = br.readLine()) != null) {
        if (buffer.substring(0, 4).equals("----"))
          continue;
        base64PublicKey += buffer;
      }
    }

    PublicKey publicKey = null;
    try {
      X509EncodedKeySpec keySpec =
          new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.getBytes()));
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      publicKey = keyFactory.generatePublic(keySpec);
      return publicKey;
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
    }
    return publicKey;
  }

  /*******************************************************************************/
  private PrivateKey getPrivateKey() throws Exception {

    String base64PrivateKey = "";
    // 2020_0615 resolve Unreleased Resource: Streams by yanghan
    try (FileInputStream fis = new FileInputStream(new File(privatekeyPath));
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, "MS950"))) {
      String buffer = "";
      while ((buffer = br.readLine()) != null) {
        if (buffer.substring(0, 4).equals("----"))
          continue;
        base64PrivateKey += buffer;
      }
    }
    PrivateKey privateKey = null;
    try {
      PKCS8EncodedKeySpec keySpec =
          new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey.getBytes()));
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      privateKey = keyFactory.generatePrivate(keySpec);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
    }
    return privateKey;
  }

  /*******************************************************************************/
  public String rsaEncrypt(byte[] data) throws Exception {
//    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	Cipher cipher = Cipher.getInstance("RSA/CBC/OAEPWithMD5AndMGF1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
    byte[] encrypted = cipher.doFinal(data);
    return toHex(encrypted).toUpperCase();
    // Base64.Encoder encoder = Base64.getEncoder();
    // return encoder.encodeToString(encrypted);
    // return new String(encrypted, "ASCII");
  }

  /*************************************************************************/
  public String toHex(byte[] in) {
    // return DatatypeConverter.printHexBinary(in);
    CommFunction comm = new CommFunction();
    StringBuffer sb = new StringBuffer(6 * in.length);
    if (in != null) {
      for (int i = 0; i < in.length; ++i) {
        sb.append(comm.fillZero(Integer.toHexString(in[i] & 0xFF), 2));
      }
      return sb.toString();
    }

    return "";
  }

  /*******************************************************************************/
  public byte[] hexStringtoByte(String in) {
    // return DatatypeConverter.parseHexBinary(in);
    return null;
  }

  /*******************************************************************************/
  /**
   * 使用私钥对明文密文进行解密
   * 
   * @param privateKey
   * @param enStr
   * @return
   */
  public byte[] rsaDecrypt(String enStr) throws Exception {
    try {
//      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      Cipher cipher = Cipher.getInstance("RSA/CBC/OAEPWithMD5AndMGF1Padding");
      cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
      byte[] deBytes = cipher.doFinal(hexStringtoByte(enStr));
      return deBytes;
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      e.printStackTrace();
    } catch (BadPaddingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /*******************************************************************************/
  public byte[] getSessionKey() {
    return this.sessionKey;
  }

}
