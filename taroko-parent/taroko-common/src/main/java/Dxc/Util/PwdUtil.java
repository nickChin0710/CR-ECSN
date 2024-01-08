/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  110-12-30  V1.00.01  Justin       initial                                 *
*  111-01-05  V1.00.02  Justin       rename a variable name of prop file     *
******************************************************************************/
package Dxc.Util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Properties;


import javax.crypto.Cipher;

//import com.tcb.ap4.tool.Decryptor;

public class PwdUtil {
	
private static final String ALGO_PROP_PATH = "/pwdUtilAlgo.properties";

static private final HashSet<String> ALGO_WHITELIST = new HashSet<String>(Arrays.asList(
			"RSA/ECB/OAEPWithMD5AndMGF1Padding"));
	
final String PK_FILE_NAME = "pk.pem";
final String ALGORITHM = getAlgo();
	
//	public String decrypt(String cipherText) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, FileNotFoundException {
//		Decryptor decrptor = new Decryptor();
//		return decrptor.doDecrypt(cipherText);
//	}
	
	public String encrypt(String plainText) throws Exception {
		RSAPublicKey rsaPublicKey = loadPublicKey();
		Base64.Encoder encoder = Base64.getEncoder();
		byte[] decBytes = doEncrypt(rsaPublicKey, plainText.getBytes());
		return new String(encoder.encode(decBytes));
	}
	
	private String getAlgo(){
		try(InputStream is = this.getClass().getResourceAsStream(ALGO_PROP_PATH);){
			Properties prop = new Properties();
			prop.load(is);
			String algo = prop.getProperty("algo");
			if (algo != null && isInWhiteList(algo)) {
				return algo;
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	private byte[] doEncrypt(RSAPublicKey rsaPublicKey, byte[] srcBytes) throws Exception {
		if (rsaPublicKey != null) {
			if (ALGORITHM != null) {
				Cipher cipher = Cipher.getInstance(ALGORITHM); // RSA -> RSA/ECB/OAEPWithMD5AndMGF1Padding
				cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
				byte[] resultBytes = cipher.doFinal(srcBytes);
				return resultBytes;
			}else {
				throw new Exception("演算法為空");
			}
		}
		return null;
	}
	
	private static boolean isInWhiteList(String algo) {
		for (Object legalAlgoObj : ALGO_WHITELIST.toArray()) {
			String legalAlgo = legalAlgoObj.toString();
			if (legalAlgo.startsWith(algo)) {
				return true;
			}
		}
		return false;
	}

	private RSAPublicKey loadPublicKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {	
		StringBuilder sb = new StringBuilder();
		try(InputStream is = this.getClass().getResourceAsStream("/" + PK_FILE_NAME);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));){
			 for (String line; (line = reader.readLine()) != null; ) {
			     if (sb.length() > 0) {
			         sb.append(System.lineSeparator());
			     }
			     sb.append(line);
			 }
		}

		String key = sb.toString();
		
	    String publicKeyPEM = key
	      .replace("-----BEGIN PUBLIC KEY-----", "")
	      .replaceAll(System.lineSeparator(), "")
	      .replace("-----END PUBLIC KEY-----", "");

	    Base64.Decoder decoder = Base64.getDecoder();
		byte[] encoded = decoder.decode(publicKeyPEM);

	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
	    return (RSAPublicKey) keyFactory.generatePublic(keySpec);
	}
}
