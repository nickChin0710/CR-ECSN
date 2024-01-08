/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*                                                                            *  
******************************************************************************/
package Dxc.Util.Pgp;

// http://sloanseaman.com/wordpress/2012/05/13/revisited-pgp-encryptiondecryption-in-java/

import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;

public class PGPFileProcessor {

  private String passphrase;
  private String publicKeyFileName;
  private String secretKeyFileName;
  private String inputFileName;
  private String outputFileName;
  private boolean asciiArmored = false;
  private boolean integrityCheck = true;

  public boolean encrypt() throws Exception {
    try (FileInputStream keyIn = new FileInputStream(publicKeyFileName);
        FileOutputStream out = new FileOutputStream(outputFileName);) {
      PGPUtils.encryptFile(out, inputFileName, PGPUtils.readPublicKey(keyIn), asciiArmored,
          integrityCheck);
    } finally {
    }

    return true;
  }

  public boolean signEncrypt() throws Exception {
    try (FileOutputStream out = new FileOutputStream(outputFileName);
        FileInputStream publicKeyIn = new FileInputStream(publicKeyFileName);
        FileInputStream secretKeyIn = new FileInputStream(secretKeyFileName);) {
      PGPPublicKey publicKey = PGPUtils.readPublicKey(publicKeyIn);
      PGPSecretKey secretKey = PGPUtils.readSecretKey(secretKeyIn);

      PGPUtils.signEncryptFile(out, this.getInputFileName(), publicKey, secretKey,
          this.getPassphrase(), this.isAsciiArmored(), this.isIntegrityCheck());
    } finally {
    }

    return true;
  }

  public boolean decrypt() throws Exception {
    try (FileInputStream in = new FileInputStream(inputFileName);
        FileInputStream keyIn = new FileInputStream(secretKeyFileName);
        FileOutputStream out = new FileOutputStream(outputFileName);) {
      PGPUtils.decryptFile(in, out, keyIn, passphrase.toCharArray());
    } finally {
    }

    return true;
  }

  public boolean isAsciiArmored() {
    return asciiArmored;
  }

  public void setAsciiArmored(boolean asciiArmored) {
    this.asciiArmored = asciiArmored;
  }

  public boolean isIntegrityCheck() {
    return integrityCheck;
  }

  public void setIntegrityCheck(boolean integrityCheck) {
    this.integrityCheck = integrityCheck;
  }

  public String getPassphrase() {
    return passphrase;
  }

  public void setPassphrase(String passphrase) {
    this.passphrase = passphrase;
  }

  public String getPublicKeyFileName() {
    return publicKeyFileName;
  }

  public void setPublicKeyFileName(String publicKeyFileName) {
    this.publicKeyFileName = publicKeyFileName;
  }

  public String getSecretKeyFileName() {
    return secretKeyFileName;
  }

  public void setSecretKeyFileName(String secretKeyFileName) {
    this.secretKeyFileName = secretKeyFileName;
  }

  public String getInputFileName() {
    return inputFileName;
  }

  public void setInputFileName(String inputFileName) {
    this.inputFileName = inputFileName;
  }

  public String getOutputFileName() {
    return outputFileName;
  }

  public void setOutputFileName(String outputFileName) {
    this.outputFileName = outputFileName;
  }

}
