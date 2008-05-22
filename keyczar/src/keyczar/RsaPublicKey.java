// Keyczar (http://code.google.com/p/keyczar/) 2008

package keyczar;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

import keyczar.enums.KeyType;
import keyczar.interfaces.EncryptingStream;
import keyczar.interfaces.SigningStream;
import keyczar.interfaces.VerifyingStream;

/**
 * Wrapping class for RSA Public Keys. These must be exported from existing RSA
 * private key sets.
 * 
 * @author steveweis@gmail.com (Steve Weis)
 * 
 */
class RsaPublicKey extends KeyczarPublicKey {
  private static final String CRYPT_ALGORITHM = "RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING";
  private static final String KEY_GEN_ALGORITHM = "RSA";
  private static final String SIG_ALGORITHM = "SHA1withRSA";

  @Override
  String getKeyGenAlgorithm() {
    return KEY_GEN_ALGORITHM;
  }

  @Override
  Stream getStream() throws KeyczarException {
    return new RsaStream();
  }

  @Override
  KeyType getType() {
    return KeyType.RSA_PUB;
  }

  private class RsaStream extends Stream implements VerifyingStream,
      EncryptingStream {
    private Cipher cipher;
    private Signature signature;

    public RsaStream() throws KeyczarException {
      try {
        signature = Signature.getInstance(SIG_ALGORITHM);
        cipher = Cipher.getInstance(CRYPT_ALGORITHM);
      } catch (GeneralSecurityException e) {
        throw new KeyczarException(e);
      }
    }

    public int digestSize() {
      return getType().getOutputSize();
    }

    @Override
    public int doFinalEncrypt(ByteBuffer input, ByteBuffer output)
        throws KeyczarException {
      try {
        return cipher.doFinal(input, output);
      } catch (GeneralSecurityException e) {
        throw new KeyczarException(e);
      }
    }

    @Override
    public SigningStream getSigningStream() {
      return new SigningStream() {
        @Override
        public int digestSize() {
          return 0;
        }

        @Override
        public void initSign() {
          // Do nothing
        }

        @Override
        public void sign(ByteBuffer output) {
          // Do nothing
        }

        @Override
        public void updateSign(ByteBuffer input) {
          // Do nothing
        }
      };
    }

    @Override
    public byte[] initEncrypt() throws KeyczarException {
      try {
        cipher.init(Cipher.ENCRYPT_MODE, getJcePublicKey());
      } catch (InvalidKeyException e) {
        throw new KeyczarException(e);
      }
      return new byte[0];
    }

    @Override
    public void initVerify() throws KeyczarException {
      try {
        signature.initVerify(getJcePublicKey());
      } catch (GeneralSecurityException e) {
        throw new KeyczarException(e);
      }
    }

    @Override
    public int ivSize() {
      return 0;
    }

    @Override
    public int maxOutputSize(int inputLen) {
      return getType().getOutputSize();
    }

    @Override
    public int updateEncrypt(ByteBuffer input, ByteBuffer output)
        throws KeyczarException {
      try {
        return cipher.update(input, output);
      } catch (ShortBufferException e) {
        throw new KeyczarException(e);
      }
    }

    @Override
    public void updateVerify(ByteBuffer input) throws KeyczarException {
      try {
        signature.update(input);
      } catch (SignatureException e) {
        throw new KeyczarException(e);
      }
    }

    @Override
    public boolean verify(ByteBuffer sig) throws KeyczarException {
      try {
        return signature.verify(sig.array(), sig.position(), sig.limit()
            - sig.position());
      } catch (GeneralSecurityException e) {
        throw new KeyczarException(e);
      }
    }
  }
}