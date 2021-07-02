/*
 * Copyright (C) 2011-2012 Yuichi Hirano
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package com.uraroji.garage.android.lame;

import android.util.Log;

/**
 * LAME interface class.
 *
 * This class is object-oriented interface.
 */
public class SimpleLame {

  static {
    System.loadLibrary("mp3lame");
    Log.d("Encoder", "Loaded native library.");
  }

  /**
   * Initialize LAME.
   *
   * @param inSamplerate input sample rate in Hz.
   * @param outChannel number of channels in input stream.
   * @param outSamplerate output sample rate in Hz.
   * @param outBitrate brate compression ratio in KHz.
   * @param quality quality=0..9. 0=best (very slow). 9=worst.<br /> recommended:<br /> 2 near-best
   * quality, not too slow<br /> 5 good quality, fast<br /> 7 ok quality, really fast
   */
  public native static int init(int inSamplerate, int outChannel,
      int outSamplerate, int outBitrate, int quality);

  /**
   * Encode buffer to mp3.
   *
   * @param buffer_l PCM data for left channel.
   * @param buffer_r PCM data for right channel.
   * @param samples number of samples per channel.
   * @param mp3buf result encoded MP3 stream. You must specified "7200 + (1.25 * samples)" length
   * array.
   * @return number of bytes output in mp3buf. Can be 0.<br /> -1: mp3buf was too small<br /> -2:
   * malloc() problem<br /> -3: lame_init_params() not called<br /> -4: psycho acoustic problems
   */
  public native static int encode(short[] buffer_l, short[] buffer_r,
      int samples, byte[] mp3buf);

  public native static void convert(String in, String out, Callback callback);

  public interface Callback {

    void onProgress(int bytes);
  }

  /**
   * Encode buffer L & R channel data interleaved to mp3.
   *
   * @param pcm PCM data for left and right channel, interleaved.
   * @param samples number of samples per channel. <strong>not</strong> number of samples in pcm[].
   * @param mp3buf result encoded MP3 stream. You must specified "7200 + (1.25 * samples)" length
   * array.
   * @return number of bytes output in mp3buf. Can be 0.<br /> -1: mp3buf was too small<br /> -2:
   * malloc() problem<br /> -3: lame_init_params() not called<br /> -4: psycho acoustic problems
   */
  public native static int encodeBufferInterleaved(short[] pcm, int samples,
      byte[] mp3buf);

  /**
   * Flush LAME buffer.
   *
   * @param mp3buf result encoded MP3 stream. You must specified at least 7200 bytes.
   * @return number of bytes output to mp3buf. Can be 0.
   */
  public native static int flush(byte[] mp3buf);

  /**
   * Close LAME.
   */
  public native static void close();
}
