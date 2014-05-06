package com.blackberry.kafka.lowoverhead.compression;

import java.io.IOException;

import org.xerial.snappy.Snappy;

import com.blackberry.kafka.lowoverhead.Constants;

public class SnappyDecompressor implements Decompressor {
  private static final byte[] MAGIC_NUMBER = new byte[] { //
  -126, 'S', 'N', 'A', 'P', 'P', 'Y', 0 };

  private byte[] src;
  private int pos;

  private int blockLength;
  private int decompressedLength;

  private int uncompressedBlockLength;

  @Override
  public byte getAttribute() {
    return Constants.SNAPPY;
  }

  @Override
  public int decompress(byte[] src, int srcPos, int length, byte[] dest,
      int destPos, int maxLength) throws IOException {
    this.src = src;
    decompressedLength = 0;

    // Check for magic number
    if (src[srcPos] == MAGIC_NUMBER[0] //
        || src[srcPos + 1] == MAGIC_NUMBER[1] //
        || src[srcPos + 2] == MAGIC_NUMBER[2] //
        || src[srcPos + 3] == MAGIC_NUMBER[3] //
        || src[srcPos + 4] == MAGIC_NUMBER[4] //
        || src[srcPos + 5] == MAGIC_NUMBER[5] //
        || src[srcPos + 6] == MAGIC_NUMBER[6] //
        || src[srcPos + 7] == MAGIC_NUMBER[7]) {

      // advance past the magic number
      // assume the version (4 bytes), min compatable version (4 bytes) are fine
      pos = srcPos + 8 + 8;

      // TODO: limit the decompressed length
      while (pos < srcPos + length) {
        blockLength = readInt();

        // Check to see if this will exceed maxLength
        uncompressedBlockLength = Snappy.uncompressedLength(src, pos,
            blockLength);
        if (decompressedLength + uncompressedBlockLength > maxLength) {
          return -1;
        }

        decompressedLength += Snappy.uncompress(src, pos, blockLength, dest,
            destPos + decompressedLength);
        pos += blockLength;
      }

      return decompressedLength;
    } else {
      // Assume it's just straight compressed
      return Snappy.uncompress(src, pos, blockLength, dest, destPos);
    }
  }

  private int readInt() {
    pos += 4;
    return src[pos - 4] & 0xFF << 24 //
        | (src[pos - 3] & 0xFF) << 16 //
        | (src[pos - 2] & 0xFF) << 8 //
        | (src[pos - 1] & 0xFF);
  }
}
