/*
 * Copyright 2017 The WebRTC project authors. All Rights Reserved.
 *
 * Use of this source code is governed by a BSD-style license
 * that can be found in the LICENSE file in the root of the source
 * tree. An additional intellectual property rights grant can be found
 * in the file PATENTS.  All contributing project authors may
 * be found in the AUTHORS file in the root of the source tree.
 */

package webrtc;

import java.nio.ByteBuffer;

public class NV12Buffer implements VideoFrame.Buffer {
  private final int width;
  private final int height;
  private final int stride;
  private final int sliceHeight;
  private final ByteBuffer buffer;
  private final Runnable releaseCallback;
  private final Object refCountLock = new Object();

  private int refCount;

  public NV12Buffer(int width, int height, int stride, int sliceHeight, ByteBuffer buffer,
      Runnable releaseCallback) {
    this.width = width;
    this.height = height;
    this.stride = stride;
    this.sliceHeight = sliceHeight;
    this.buffer = buffer;
    this.releaseCallback = releaseCallback;

    refCount = 1;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public VideoFrame.I420Buffer toI420() {
    return (VideoFrame.I420Buffer) cropAndScale(0, 0, width, height, width, height);
  }

  @Override
  public void retain() {
    synchronized (refCountLock) {
      ++refCount;
    }
  }

  @Override
  public void release() {
    synchronized (refCountLock) {
      if (--refCount == 0 && releaseCallback != null) {
        releaseCallback.run();
      }
    }
  }

  @Override
  public VideoFrame.Buffer cropAndScale(
      int cropX, int cropY, int cropWidth, int cropHeight, int scaleWidth, int scaleHeight) {
    JavaI420Buffer newBuffer = JavaI420Buffer.allocate(scaleWidth, scaleHeight);
    nativeCropAndScale(cropX, cropY, cropWidth, cropHeight, scaleWidth, scaleHeight, buffer, width,
        height, stride, sliceHeight, newBuffer.getDataY(), newBuffer.getStrideY(),
        newBuffer.getDataU(), newBuffer.getStrideU(), newBuffer.getDataV(), newBuffer.getStrideV());
    return newBuffer;
  }

  private static native void nativeCropAndScale(int cropX, int cropY, int cropWidth, int cropHeight,
      int scaleWidth, int scaleHeight, ByteBuffer src, int srcWidth, int srcHeight, int srcStride,
      int srcSliceHeight, ByteBuffer dstY, int dstStrideY, ByteBuffer dstU, int dstStrideU,
      ByteBuffer dstV, int dstStrideV);
}