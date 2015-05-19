/**
 * Copyright (C) 2015 Regents of the University of California.
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

package net.named_data.jndn.encrypt;

import java.nio.ByteBuffer;
import net.named_data.jndn.KeyLocator;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.encoding.Tlv0_1_1WireFormat;
import net.named_data.jndn.encoding.WireFormat;
import net.named_data.jndn.util.Blob;

/**
 * An EncryptedContent holds an encryption type, a payload and other fields
 * representing encrypted content.
 * @note This class is an experimental feature. The API may change.
 */
public class EncryptedContent {
  /**
   * Create an EncryptedContent where all the values are unspecified.
   */
  public EncryptedContent()
  {}

  /**
   * Create an EncryptedContent as a deep copy of the given object.
   * @param encryptedContent The other encryptedContent to copy.
   */
  public EncryptedContent(EncryptedContent encryptedContent)
  {
    algorithmType_ = encryptedContent.algorithmType_;
    keyLocator_ = new KeyLocator(encryptedContent.keyLocator_);
    payload_ = encryptedContent.payload_;
  }

  /**
   * Get the algorithm type.
   * @return The algorithm type. If not specified, return -1.
   */
  public final int
  getAlgorithmType() { return algorithmType_; }

  /**
   * Get the key locator.
   * @return The key locator. If not specified, getType() is KeyLocatorType.NONE.
   */
  public final KeyLocator
  getKeyLocator() { return keyLocator_; }

  /**
   * Get the payload.
   * @return The payload. If not specified, isNull() is true.
   */
  public final Blob
  getPayload() { return payload_; }

  /**
   * Set the algorithm type.
   * @param algorithmType The algorithm type. If not specified, set to -1.
   */
  public final void
  setAlgorithmType(int algorithmType)
  {
    algorithmType_ = algorithmType < 0 ? -1 : algorithmType;
  }

  /**
   * Set the key locator.
   * @param keyLocator The key locator. If not specified, set to the default
   * KeyLocator().
   */
  public final void
  setKeyLocator(KeyLocator keyLocator)
  {
    keyLocator_ = keyLocator == null ?
      new KeyLocator() : new KeyLocator(keyLocator);
  }

  /**
   * Set the encrypted payload.
   * @param payload The payload. If not specified, set to the default Blob()
   * where isNull() is true.
   */
  public final void
  setPayload(Blob payload)
  {
    payload_ = (payload_ == null ? new Blob() : payload);
  }

  /**
   * Encode this EncryptedContent to a wire encoding.
   * @param wireFormat A WireFormat object used to encode this EncryptedContent.
   * @return The encoded byte array as a Blob.
   */
  public final Blob
  wireEncode(WireFormat wireFormat)
  {
    return wireFormat.encodeEncryptedContent(this);
  }

  /**
   * Encode this EncryptedContent for the default wire format.
   * @return The encoded byte array as a Blob.
   */
  public final Blob
  wireEncode()
  {
    return wireEncode(WireFormat.getDefaultWireFormat());
  }

  /**
   * Decode the input using a particular wire format and update this
   * EncryptedContent.
   * @param input The input buffer to decode.  This reads from position() to
   * limit(), but does not change the position.
   * @param wireFormat A WireFormat object used to decode the input.
   * @throws EncodingException For invalid encoding.
   */
  public final void
  wireDecode(ByteBuffer input, WireFormat wireFormat) throws EncodingException
  {
    wireFormat.decodeEncryptedContent(this, input);
  }

  /**
   * Decode the input wire encoding using the default wire format and update
   * this EncryptedContent.
   * @param input The input buffer to decode.  This reads from position() to
   * limit(), but does not change the position.
   * @throws EncodingException For invalid encoding.
   */
  public final void
  wireDecode(ByteBuffer input) throws EncodingException
  {
    wireDecode(input, WireFormat.getDefaultWireFormat());
  }

  /**
   * Decode the input using a particular wire format and update this
   * EncryptedContent.
   * @param input The input blob to decode.
   * @param wireFormat A WireFormat object used to decode the input.
   * @throws EncodingException For invalid encoding.
   */
  public final void
  wireDecode(Blob input, WireFormat wireFormat) throws EncodingException
  {
    wireDecode(input.buf(), wireFormat);
  }

  /**
   * Decode the input the default wire format and update this EncryptedContent.
   * @param input The input blob to decode.
   * @throws EncodingException For invalid encoding.
   */
  public final void
  wireDecode(Blob input) throws EncodingException
  {
    wireDecode(input, WireFormat.getDefaultWireFormat());
  }

  private int algorithmType_ = -1;
  private KeyLocator keyLocator_ = new KeyLocator();
  private Blob payload_ = new Blob();
}