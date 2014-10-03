/**
 * Copyright (C) 2013-2014 Regents of the University of California.
 * @author: Jeff Thompson <jefft0@remap.ucla.edu>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * A copy of the GNU Lesser General Public License is in the file COPYING.
 */

package net.named_data.jndn.security.identity;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.DigestAlgorithm;
import net.named_data.jndn.security.KeyClass;
import net.named_data.jndn.security.KeyType;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.certificate.PublicKey;
import net.named_data.jndn.util.Blob;

public class MemoryPrivateKeyStorage extends PrivateKeyStorage {
  /**
   * Set the public key for the keyName.
   * @param keyName The key name.
   * @param keyType The KeyType, such as KeyType.RSA.
   * @param publicKeyDer The public key DER byte buffer.
   * @throws SecurityException if can't decode the key DER.
   */
  public final void
  setPublicKeyForKeyName
    (Name keyName, KeyType keyType, ByteBuffer publicKeyDer) throws SecurityException
  {
    publicKeyStore_.put
      (keyName.toUri(), PublicKey.fromDer(keyType, new Blob(publicKeyDer, true)));
  }

  /**
   * Set the private key for the keyName.
   * @param keyName The key name.
   * @param keyType The KeyType, such as KeyType.RSA.
   * @param privateKeyDer The private key DER byte buffer.
   * @throws SecurityException if can't decode the key DER.
   */
  public final void
  setPrivateKeyForKeyName
    (Name keyName, KeyType keyType, ByteBuffer privateKeyDer) throws SecurityException
  {
    privateKeyStore_.put
      (keyName.toUri(), new PrivateKey(keyType, privateKeyDer));
  }

  /**
   * Set the public and private key for the keyName.
   * @param keyName The key name.
   * @param keyType The KeyType, such as KeyType.RSA.
   * @param publicKeyDer The public key DER byte buffer.
   * @param privateKeyDer The private key DER byte buffer.
   * @throws SecurityException if can't decode the key DER.
   */
  public final void
  setKeyPairForKeyName
    (Name keyName, KeyType keyType, ByteBuffer publicKeyDer,
     ByteBuffer privateKeyDer) throws SecurityException
  {
    setPublicKeyForKeyName(keyName, keyType, publicKeyDer);
    setPrivateKeyForKeyName(keyName, keyType, privateKeyDer);
  }

  /**
   * @deprecated Use setKeyPairForKeyName(keyName, KeyType.RSA, publicKeyDer, privateKeyDer).
   * @param keyName The key name.
   * @param publicKeyDer The public key DER byte buffer.
   * @param privateKeyDer The private key DER byte buffer.
   * @throws SecurityException if can't decode the key DER.
   */
  public final void
  setKeyPairForKeyName
    (Name keyName, ByteBuffer publicKeyDer, ByteBuffer privateKeyDer) throws SecurityException
  {
    setKeyPairForKeyName(keyName, KeyType.RSA, publicKeyDer, privateKeyDer);
  }

  /**
   * Generate a pair of asymmetric keys.
   * @param keyName The name of the key pair.
   * @param keyType The type of the key pair, e.g. KEY_TYPE_RSA.
   * @param keySize The size of the key pair.
   * @throws SecurityException
   */
  public void
  generateKeyPair
    (Name keyName, KeyType keyType, int keySize) throws SecurityException
  {
    throw new UnsupportedOperationException
      ("MemoryPrivateKeyStorage.generateKeyPair is not implemented");
  }

  /**
   * Get the public key
   * @param keyName The name of public key.
   * @return The public key.
   * @throws SecurityException
   */
  public PublicKey
  getPublicKey(Name keyName) throws SecurityException
  {
    PublicKey publicKey = (PublicKey)publicKeyStore_.get(keyName.toUri());
    if (publicKey == null)
      throw new SecurityException
        ("MemoryPrivateKeyStorage: Cannot find public key " + keyName.toUri());
    return publicKey;
  }

  /**
   * Fetch the private key for keyName and sign the data, returning a signature
   * Blob.
   * @param data Pointer the input byte buffer to sign.
   * @param keyName The name of the signing key.
   * @param digestAlgorithm the digest algorithm.
   * @return The signature, or Blob with a null pointer if signing fails.
   * @throws SecurityException
   */
  public Blob
  sign(ByteBuffer data, Name keyName, DigestAlgorithm digestAlgorithm)
       throws SecurityException
  {
    if (digestAlgorithm != DigestAlgorithm.SHA256)
      return new Blob();

    // Find the private key and sign.
    PrivateKey privateKey = (PrivateKey)privateKeyStore_.get(keyName.toUri());
    if (privateKey == null)
      throw new SecurityException
        ("MemoryPrivateKeyStorage: Cannot find private key " + keyName.toUri());

    Signature signature = null;
    if (privateKey.getKeyType() == KeyType.RSA) {
      try {
        signature = Signature.getInstance("SHA256withRSA");
      }
      catch (NoSuchAlgorithmException e) {
        // Don't expect this to happen.
        throw new SecurityException("SHA256withRSA algorithm is not supported");
      }
    }
    else if (privateKey.getKeyType() == KeyType.EC) {
      try {
        signature = Signature.getInstance("SHA256withECDSA");
      }
      catch (NoSuchAlgorithmException e) {
        // Don't expect this to happen.
        throw new SecurityException("SHA256withECDSA algorithm is not supported");
      }
    }
    else
      // We don't expect this to happen.
      throw new SecurityException("Unrecognized private key type");

    try {
      signature.initSign(privateKey.getPrivateKey());
    }
    catch (InvalidKeyException exception) {
      throw new SecurityException
        ("InvalidKeyException: " + exception.getMessage());
    }
    try {
      signature.update(data);
      return new Blob(signature.sign());
    }
    catch (SignatureException exception) {
      throw new SecurityException
        ("SignatureException: " + exception.getMessage());
    }
  }

  /**
   * Decrypt data.
   * @param keyName The name of the decrypting key.
   * @param data The byte buffer to be decrypted, from its position to its
   * limit.
   * @param isSymmetric If true symmetric encryption is used, otherwise
   * asymmetric encryption is used.
   * @return The decrypted data.
   * @throws SecurityException
   */
  public Blob
  decrypt(Name keyName, ByteBuffer data, boolean isSymmetric)
          throws SecurityException
  {
    throw new UnsupportedOperationException
      ("MemoryPrivateKeyStorage.decrypt is not implemented");
  }

  /**
   * Encrypt data.
   * @param keyName The name of the encrypting key.
   * @param data The byte buffer to be encrypted, from its position to its
   * limit.
   * @param isSymmetric If true symmetric encryption is used, otherwise
   * asymmetric encryption is used.
   * @return The encrypted data.
   * @throws SecurityException
   */
  public Blob
  encrypt(Name keyName, ByteBuffer data, boolean isSymmetric)
         throws SecurityException
  {
    throw new UnsupportedOperationException
      ("MemoryPrivateKeyStorage.encrypt is not implemented");
  }

  /**
   * Generate a symmetric key.
   * @param keyName The name of the key.
   * @param keyType The type of the key, e.g. KeyType.AES.
   * @param keySize The size of the key.
   * @throws SecurityException
   */
  public void
  generateKey(Name keyName, KeyType keyType, int keySize)
             throws SecurityException
  {
    throw new UnsupportedOperationException
      ("MemoryPrivateKeyStorage.generateKey is not implemented");
  }

  /**
   * Check if a particular key exists.
   * @param keyName The name of the key.
   * @param keyClass The class of the key, e.g. KEY_CLASS_PUBLIC,
   * KEY_CLASS_PRIVATE, or KEY_CLASS_SYMMETRIC.
   * @return True if the key exists, otherwise false.
   */
  public boolean
  doesKeyExist(Name keyName, KeyClass keyClass)
  {
    if (keyClass == KeyClass.PUBLIC)
      return publicKeyStore_.containsKey(keyName.toUri());
    else if (keyClass == KeyClass.PRIVATE)
      return privateKeyStore_.containsKey(keyName.toUri());
    else
      // KeyClass.SYMMETRIC not implemented yet.
      return false;
  }

  /**
   * PrivateKey is a simple class to hold a java.security.PrivateKey along with
   * a KeyType.
   */
  class PrivateKey {
    public PrivateKey(KeyType keyType, ByteBuffer keyDer) throws SecurityException {
      keyType_ = keyType;

      if (keyType == KeyType.RSA) {
        KeyFactory keyFactory = null;
        try {
          keyFactory = KeyFactory.getInstance("RSA");
        }
        catch (NoSuchAlgorithmException exception) {
          // Don't expect this to happen.
          throw new SecurityException
            ("KeyFactory: RSA is not supported: " + exception.getMessage());
        }

        try {
          privateKey_ =
            keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyDer.array()));
        }
        catch (InvalidKeySpecException exception) {
          // Don't expect this to happen.
          throw new SecurityException
            ("KeyFactory: PKCS8EncodedKeySpec is not supported for RSA: " +
             exception.getMessage());
        }
      }
      else if (keyType == KeyType.EC) {
        KeyFactory keyFactory = null;
        try {
          keyFactory = KeyFactory.getInstance("EC");
        }
        catch (NoSuchAlgorithmException exception) {
          // Don't expect this to happen.
          throw new SecurityException
            ("KeyFactory: EC is not supported: " + exception.getMessage());
        }

        try {
          privateKey_ =
            keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyDer.array()));
        }
        catch (InvalidKeySpecException exception) {
          // Don't expect this to happen.
          throw new SecurityException
            ("KeyFactory: PKCS8EncodedKeySpec is not supported for EC: " +
             exception.getMessage());
        }
      }
      else
        throw new SecurityException
          ("PrivateKey constructor: Unrecognized keyType");
    }

    public KeyType getKeyType() { return keyType_; }

    public java.security.PrivateKey getPrivateKey() { return privateKey_; }

    private KeyType keyType_;
    private java.security.PrivateKey privateKey_;
  }

  private final HashMap publicKeyStore_ =
    new HashMap(); /**< The map key is the keyName.toUri().
                      * The value is security.certificate.PublicKey. */
  private final HashMap privateKeyStore_ =
    new HashMap(); /**< The map key is the keyName.toUri().
                      * The value is MemoryPrivateKeyStorage.PrivateKey. */
}
