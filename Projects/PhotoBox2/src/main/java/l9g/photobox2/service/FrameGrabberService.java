/*
 * Copyright 2024 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package l9g.photobox2.service;

import lombok.extern.slf4j.Slf4j;
import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_BGR24;
import org.bytedeco.javacv.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Service
@Slf4j
public class FrameGrabberService
{

  public synchronized void start() throws Exception
  {
    if (!running)
    {
      ProcessBuilder builder = new ProcessBuilder();
      builder.inheritIO();
      builder.command(
        "/usr/bin/libcamera-vid", "--hflip", "-n", "-t", "0",
        "--autofocus-mode", "continuous",
        "--mode", videoMode, "--roi", videoRoi,
        "--framerate", Double.toString(videoFramerate),
        "--width", Integer.toString(videoWidth),
        "--height", Integer.toString(videoHeight),
        "--codec", videoCodec, "-o", videoStreamUrl
      );
      libcameraProcess = builder.start();

      log.debug("libcamera-vid started waiting 3s");
      Thread.sleep(3000);
      log.debug("setup ffmpeg log callback");

      FFmpegLogCallback.set();
      FFmpegLogCallback.setLevel(0);

      log.debug("initialize ffmpeg grabber");
      grabber = new FFmpegFrameGrabber(videoStreamUrl);
      grabber.setFrameNumber(1);
      grabber.setFrameRate(videoFramerate);
      grabber.setFormat(videoCodec);
      grabber.setPixelFormat(AV_PIX_FMT_BGR24);

      log.debug("starting ffmpeg grabber");
      grabber.start();
      log.debug("grabber started");

      running = true;
    }
  }

  public synchronized void stop() throws FrameGrabber.Exception
  {
    if (running)
    {
      log.info("stopping grabber");
      grabber.close();
      grabber.stop();
      grabber.release();
      libcameraProcess.destroy();
      running = false;
    }
    log.debug("stop - done.");
  }

  public Frame grab() throws FrameGrabber.Exception
  {
    return grabber.grab();
  }

  public int getWidth()
  {
    return videoWidth;
  }

  public int getHeight()
  {
    return videoHeight;
  }

  @Value("${grabber.stream-url}")
  private String videoStreamUrl;

  @Value("${grabber.codec}")
  private String videoCodec;

  @Value("${grabber.mode}")
  private String videoMode;

  @Value("${grabber.roi}")
  private String videoRoi;

  @Value("${grabber.framerate}")
  private double videoFramerate;

  @Value("${grabber.width}")
  private int videoWidth;

  @Value("${grabber.height}")
  private int videoHeight;

  private Process libcameraProcess;

  private boolean running;

  private FrameGrabber grabber;
}
