/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
* 110-01-07  V1.00.02    shiyuqi   coding standard, rename                  * 
******************************************************************************/
package Dxc.Util.Pgp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

public class PGPUtils {

  private static final int BUFFER_SIZE = 1 << 16; // should always be power of 2
  private static final int KEY_FLAGS = 27;
  private static final int[] MASTER_KEY_CERTIFICATION_TYPES = new int[] {
      PGPSignature.POSITIVE_CERTIFICATION, PGPSignature.CASUAL_CERTIFICATION,
      PGPSignature.NO_CERTIFICATION, PGPSignature.DEFAULT_CERTIFICATION};

  @SuppressWarnings("unchecked")
  public static PGPPublicKey readPublicKey(InputStream in) throws IOException, PGPException {
    in = PGPUtil.getDecoderStream(in);

    PGPPublicKeyRingCollection pgpPub =
        new PGPPublicKeyRingCollection(in, new JcaKeyFingerprintCalculator());
    Iterator rIt = pgpPub.getKeyRings();

    while (rIt.hasNext()) {
      PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
      Iterator kIt = kRing.getPublicKeys();

      while (kIt.hasNext()) {
        PGPPublicKey k = (PGPPublicKey) kIt.next();

        if (k.isEncryptionKey()) {
          return k;
        }
      }
    }

    throw new IllegalArgumentException("Can't find encryption key in key ring.");
  }

  @SuppressWarnings("unchecked")
  public static PGPPublicKey readPublicKey2(InputStream in) throws IOException, PGPException {


    // PGPPublicKeyRingCollection keyRingCollection = new
    // PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(in)); modified by Howard
    PGPPublicKeyRingCollection keyRingCollection =
        new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(in), null);



    //
    // we just loop through the collection till we find a key suitable for encryption, in the real
    // world you would probably want to be a bit smarter about this.
    //
    PGPPublicKey publicKey = null;

    //
    // iterate through the key rings.
    //
    Iterator<PGPPublicKeyRing> rIt = keyRingCollection.getKeyRings();

    while (publicKey == null && rIt.hasNext()) {
      PGPPublicKeyRing kRing = rIt.next();
      Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
      while (publicKey == null && kIt.hasNext()) {
        PGPPublicKey key = kIt.next();
        if (key.isEncryptionKey()) {
          publicKey = key;
        }
      }
    }

    if (publicKey == null) {
      throw new IllegalArgumentException("Can't find public key in the key ring.");
    }
    if (!isForEncryption(publicKey)) {
      throw new IllegalArgumentException("KeyID " + publicKey.getKeyID()
          + " not flagged for encryption.");
    }

    return publicKey;
  }

  @SuppressWarnings("unchecked")
  public static PGPSecretKey readSecretKey(InputStream in) throws IOException, PGPException {

    // PGPSecretKeyRingCollection keyRingCollection = new
    // PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(in)); modified by Howard
    PGPSecretKeyRingCollection keyRingCollection =
        new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(in),
            new JcaKeyFingerprintCalculator());

    //
    // We just loop through the collection till we find a key suitable for signing.
    // In the real world you would probably want to be a bit smarter about this.
    //
    PGPSecretKey secretKey = null;

    Iterator<PGPSecretKeyRing> rIt = keyRingCollection.getKeyRings();
    while (secretKey == null && rIt.hasNext()) {
      PGPSecretKeyRing keyRing = rIt.next();
      Iterator<PGPSecretKey> kIt = keyRing.getSecretKeys();
      while (secretKey == null && kIt.hasNext()) {
        PGPSecretKey key = kIt.next();
        if (key.isSigningKey()) {
          secretKey = key;
        }
      }
    }

    // Validate secret key
    if (secretKey == null) {
      throw new IllegalArgumentException("Can't find private key in the key ring.");
    }
    if (!secretKey.isSigningKey()) {
      throw new IllegalArgumentException("Private key does not allow signing.");
    }
    if (secretKey.getPublicKey().isRevoked()) {
      throw new IllegalArgumentException("Private key has been revoked.");
    }
    if (!hasKeyFlags(secretKey.getPublicKey(), KeyFlags.SIGN_DATA)) {
      throw new IllegalArgumentException("Key cannot be used for signing.");
    }

    return secretKey;
  }

  /**
   * Load a secret key ring collection from keyIn and find the private key corresponding to keyID if
   * it exists.
   * 
   * @param keyIn input stream representing a key ring collection.
   * @param keyID keyID we want.
   * @param pass passphrase to decrypt secret key with.
   * @return
   * @throws IOException
   * @throws PGPException
   * @throws NoSuchProviderException
   */
  public static PGPPrivateKey findPrivateKey(InputStream keyIn, long keyID, char[] pass)
      throws IOException, PGPException, NoSuchProviderException {
    // PGPSecretKeyRingCollection pgpSec = new
    // PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn)); modified by Howard

    PGPSecretKeyRingCollection pgpSec =
        new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn),
            new JcaKeyFingerprintCalculator());

    return findPrivateKey(pgpSec.getSecretKey(keyID), pass);

  }

  /**
   * Load a secret key and find the private key in it
   * 
   * @param pgpSecKey The secret key
   * @param pass passphrase to decrypt secret key with
   * @return
   * @throws PGPException
   */
  public static PGPPrivateKey findPrivateKey(PGPSecretKey pgpSecKey, char[] pass)
      throws PGPException {
    if (pgpSecKey == null)
      return null;

    PBESecretKeyDecryptor decryptor =
        new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(pass);
    return pgpSecKey.extractPrivateKey(decryptor);
  }

  /**
   * decrypt the passed in message stream
   */
  @SuppressWarnings("unchecked")
  public static void decryptFile(InputStream in, OutputStream out, InputStream keyIn, char[] passwd)
      throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    in = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(in);


    // PGPObjectFactory pgpF = new PGPObjectFactory(in); modified by Howard
    PGPObjectFactory pgpF = new PGPObjectFactory(in, null);

    PGPEncryptedDataList enc;

    Object o = pgpF.nextObject();
    //
    // the first object might be a PGP marker packet.
    //
    if (o instanceof PGPEncryptedDataList) {
      enc = (PGPEncryptedDataList) o;
    } else {
      enc = (PGPEncryptedDataList) pgpF.nextObject();
    }

    //
    // find the secret key
    //
    Iterator<PGPPublicKeyEncryptedData> it = enc.getEncryptedDataObjects();
    PGPPrivateKey sKey = null;
    PGPPublicKeyEncryptedData pbe = null;

    while (sKey == null && it.hasNext()) {
      pbe = it.next();

      sKey = findPrivateKey(keyIn, pbe.getKeyID(), passwd);
    }

    if (sKey == null) {
      throw new IllegalArgumentException("Secret key for message not found.");
    }

    if (pbe == null) {
      throw new IllegalArgumentException("Encrypted Data not found.");
    }
    InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(sKey));

    // PGPObjectFactory plainFact = new PGPObjectFactory(clear); modified by Howard
    PGPObjectFactory plainFact = new PGPObjectFactory(clear, null);

    Object message = plainFact.nextObject();

    if (message instanceof PGPCompressedData) {
      PGPCompressedData cData = (PGPCompressedData) message;
      // PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream());modified by Howard
      PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream(), null);

      message = pgpFact.nextObject();
    }

    if (message instanceof PGPLiteralData) {
      PGPLiteralData ld = (PGPLiteralData) message;

      InputStream unc = ld.getInputStream();
      int ch;

      while ((ch = unc.read()) >= 0) {
        out.write(ch);
      }
    } else if (message instanceof PGPOnePassSignatureList) {
      throw new PGPException("Encrypted message contains a signed message - not literal data.");
    } else {
      throw new PGPException("Message is not a simple encrypted file - type unknown.");
    }

    if (pbe.isIntegrityProtected()) {
      if (!pbe.verify()) {
        throw new PGPException("Message failed integrity check");
      }
    }
  }

  public static void encryptFile(OutputStream out, String fileName, PGPPublicKey encKey,
      boolean armor, boolean withIntegrityCheck) throws IOException, NoSuchProviderException,
      PGPException {
    Security.addProvider(new BouncyCastleProvider());

    if (armor) {
      out = new ArmoredOutputStream(out);
    }

    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);

    PGPUtil.writeFileToLiteralData(comData.open(bOut), PGPLiteralData.BINARY, new File(fileName));

    comData.close();

    BcPGPDataEncryptorBuilder dataEncryptor =
        new BcPGPDataEncryptorBuilder(PGPEncryptedData.TRIPLE_DES);
    dataEncryptor.setWithIntegrityPacket(withIntegrityCheck);
    dataEncryptor.setSecureRandom(new SecureRandom());

    PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(dataEncryptor);
    encryptedDataGenerator.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(encKey));

    byte[] bytes = bOut.toByteArray();
    OutputStream cOut = encryptedDataGenerator.open(out, bytes.length);
    cOut.write(bytes);
    cOut.close();
    out.close();
  }

  @SuppressWarnings("unchecked")
  public static void signEncryptFile(OutputStream out, String fileName, PGPPublicKey publicKey,
      PGPSecretKey secretKey, String password, boolean armor, boolean withIntegrityCheck)
      throws Exception {

    // Initialize Bouncy Castle security provider
    Provider provider = new BouncyCastleProvider();
    Security.addProvider(provider);

    if (armor) {
      out = new ArmoredOutputStream(out);
    }

    BcPGPDataEncryptorBuilder dataEncryptor =
        new BcPGPDataEncryptorBuilder(PGPEncryptedData.TRIPLE_DES);
    dataEncryptor.setWithIntegrityPacket(withIntegrityCheck);
    dataEncryptor.setSecureRandom(new SecureRandom());

    PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(dataEncryptor);
    encryptedDataGenerator.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(publicKey));

    OutputStream encryptedOut = encryptedDataGenerator.open(out, new byte[PGPUtils.BUFFER_SIZE]);

    // Initialize compressed data generator
    PGPCompressedDataGenerator compressedDataGenerator =
        new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
    OutputStream compressedOut =
        compressedDataGenerator.open(encryptedOut, new byte[PGPUtils.BUFFER_SIZE]);

    // Initialize signature generator
    PGPPrivateKey privateKey = findPrivateKey(secretKey, password.toCharArray());

    PGPContentSignerBuilder signerBuilder =
        new BcPGPContentSignerBuilder(secretKey.getPublicKey().getAlgorithm(),
            HashAlgorithmTags.SHA1);

    PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(signerBuilder);
    signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);

    boolean firstTime = true;
    Iterator<String> it = secretKey.getPublicKey().getUserIDs();
    while (it.hasNext() && firstTime) {
      PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
      spGen.setSignerUserID(false, it.next());
      signatureGenerator.setHashedSubpackets(spGen.generate());
      // Exit the loop after the first iteration
      firstTime = false;
    }
    signatureGenerator.generateOnePassVersion(false).encode(compressedOut);

    // Initialize literal data generator
    PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
    OutputStream literalOut =
        literalDataGenerator.open(compressedOut, PGPLiteralData.BINARY, fileName, new Date(),
            new byte[PGPUtils.BUFFER_SIZE]);

    // Main loop - read the "in" stream, compress, encrypt and write to the "out" stream
    try (FileInputStream in = new FileInputStream(fileName);) {
      byte[] buf = new byte[PGPUtils.BUFFER_SIZE];
      int len;
      while ((len = in.read(buf)) > 0) {
        literalOut.write(buf, 0, len);
        signatureGenerator.update(buf, 0, len);
      }
    } finally {
    }

    literalDataGenerator.close();
    // Generate the signature, compress, encrypt and write to the "out" stream
    signatureGenerator.generate().encode(compressedOut);
    compressedDataGenerator.close();
    encryptedDataGenerator.close();
    if (armor) {
      out.close();
    }
  }

  public static boolean verifyFile(InputStream in, InputStream keyIn, String extractContentFile)
      throws Exception {
    in = PGPUtil.getDecoderStream(in);

    // PGPObjectFactory pgpFact = new PGPObjectFactory(in);modified by Howard
    PGPObjectFactory pgpFact = new PGPObjectFactory(in, null);

    PGPCompressedData c1 = (PGPCompressedData) pgpFact.nextObject();

    // pgpFact = new PGPObjectFactory(c1.getDataStream());modified by Howard
    pgpFact = new PGPObjectFactory(c1.getDataStream(), null);

    PGPOnePassSignatureList pgpFact1 = (PGPOnePassSignatureList) pgpFact.nextObject();

    PGPOnePassSignature ops = pgpFact1.get(0);

    PGPLiteralData pgpFact2 = (PGPLiteralData) pgpFact.nextObject();

    InputStream dIn = pgpFact2.getInputStream();

    try (OutputStream fout = new FileOutputStream(extractContentFile)) {
      IOUtils.copy(dIn, fout);
    } finally {
    }

    int ch;

    // PGPPublicKeyRingCollection pgpRing = new
    // PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(keyIn)); modified by Howard
    PGPPublicKeyRingCollection pgpRing =
        new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(keyIn), null);

    PGPPublicKey key = pgpRing.getPublicKey(ops.getKeyID());

    try (FileOutputStream out = new FileOutputStream(pgpFact2.getFileName());) {
      ops.init(new BcPGPContentVerifierBuilderProvider(), key);

      while ((ch = dIn.read()) >= 0) {
        ops.update((byte) ch);
        out.write(ch);
      }
    } finally {
    }

    PGPSignatureList pgpFact3 = (PGPSignatureList) pgpFact.nextObject();
    return ops.verify(pgpFact3.get(0));
  }

  /**
   * From LockBox Lobs PGP Encryption tools. http://www.lockboxlabs.org/content/downloads
   * 
   * I didn't think it was worth having to import a 4meg lib for three methods
   * 
   * @param key
   * @return
   */
  public static boolean isForEncryption(PGPPublicKey key) {
    if (key.getAlgorithm() == PublicKeyAlgorithmTags.RSA_SIGN
        || key.getAlgorithm() == PublicKeyAlgorithmTags.DSA
        || key.getAlgorithm() == PublicKeyAlgorithmTags.EC
        || key.getAlgorithm() == PublicKeyAlgorithmTags.ECDSA) {
      return false;
    }

    return hasKeyFlags(key, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);
  }

  /**
   * From LockBox Lobs PGP Encryption tools. http://www.lockboxlabs.org/content/downloads
   * 
   * I didn't think it was worth having to import a 4meg lib for three methods
   * 
   * @param key
   * @return
   */
  @SuppressWarnings("unchecked")
  private static boolean hasKeyFlags(PGPPublicKey encKey, int keyUsage) {
    if (encKey.isMasterKey()) {
      for (int i = 0; i != PGPUtils.MASTER_KEY_CERTIFICATION_TYPES.length; i++) {
        for (Iterator<PGPSignature> eIt =
            encKey.getSignaturesOfType(PGPUtils.MASTER_KEY_CERTIFICATION_TYPES[i]); eIt.hasNext();) {
          PGPSignature sig = eIt.next();
          if (!isMatchingUsage(sig, keyUsage)) {
            return false;
          }
        }
      }
    } else {
      for (Iterator<PGPSignature> eIt = encKey.getSignaturesOfType(PGPSignature.SUBKEY_BINDING); eIt
          .hasNext();) {
        PGPSignature sig = eIt.next();
        if (!isMatchingUsage(sig, keyUsage)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * From LockBox Lobs PGP Encryption tools. http://www.lockboxlabs.org/content/downloads
   * 
   * I didn't think it was worth having to import a 4meg lib for three methods
   * 
   * @param key
   * @return
   */
  private static boolean isMatchingUsage(PGPSignature sig, int keyUsage) {
    if (sig.hasSubpackets()) {
      PGPSignatureSubpacketVector sv = sig.getHashedSubPackets();
      if (sv.hasSubpacket(PGPUtils.KEY_FLAGS)) {
        // code fix suggested by kzt (see comments)
        /*
         * marked by Howard if ((sv.getKeyFlags() == 0 && keyUsage) == 0) { return false; }
         */
      }
    }
    return true;
  }

}
