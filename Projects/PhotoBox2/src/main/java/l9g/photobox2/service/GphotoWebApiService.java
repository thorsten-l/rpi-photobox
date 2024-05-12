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

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import l9g.photobox2.gphoto.AutoDetectResponse;
import l9g.photobox2.gphoto.CapturedImageResponse;
import l9g.photobox2.gphoto.ShutdownResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Service
@Slf4j
@Getter
public class GphotoWebApiService
{
  @Value("${gphoto-webapi.url}")
  private String gphotoWebApiUrl;
  
  @Value("${gphoto-webapi.camera}")
  private String gphotoWebApiCamera;
  
  @Value("${gphoto-webapi.command}")
  private String[] gphotoWebApiCommand;
  
  @Value("${gphoto-webapi.directory}")
  private String gphotoWebApiDirectory;
  
  @Value("${gphoto-webapi.capture.timeout}")
  private long gphotoWebApiCaptureTimeout;
  
  private RestClient restClient;
  
  @PostConstruct
  public void initialize()
  {
    log.debug("initialize");
    restClient = RestClient.builder()
      .baseUrl(gphotoWebApiUrl)
      .build();
  }
  
  public synchronized void check() throws Exception
  {
    if ((gphotoWebApiProcess == null || !gphotoWebApiProcess.isAlive()))
    {
      log.debug("starting gphoto-webapi");
      ProcessBuilder builder = new ProcessBuilder();
      builder.command(Arrays.asList(gphotoWebApiCommand));
      File directory = new File(gphotoWebApiDirectory);
      directory.mkdirs();
      builder.directory(directory);
      builder.redirectErrorStream(true);
      builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
      builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
      gphotoWebApiProcess = builder.start();
      Thread.sleep(2000);
      if (!gphotoWebApiProcess.isAlive())
      {
        throw new Exception("gphoto-webapi not started");
      }
      log.debug("gphoto-webapi started");
    }
  }
  
  public synchronized void stop() throws InterruptedException
  {
    log.debug("stopping gphoto-webapi");
    if (gphotoWebApiProcess != null && gphotoWebApiProcess.isAlive())
    {
      log.debug("send server shutdown");
      ResponseEntity<ShutdownResponse> entity = restClient
        .get()
        .uri("/server/shutdown")
        .retrieve().toEntity(ShutdownResponse.class);
      
      if (entity.getStatusCode().is2xxSuccessful())
      {
        log.info("return_code={}", entity.getBody().getReturnCode());
      }
      else
      {
        log.error("Shutdown request NOT successful.");
      }
      
      log.debug("sleep 1000");
      Thread.sleep(1000);
      if (gphotoWebApiProcess.isAlive())
      {
        log.debug("destroy process");
        gphotoWebApiProcess.destroy();
      }
      log.debug("stopped");
    }
    else
    {
      log.debug("not running");
    }
    log.debug("stop - done.");
    gphotoWebApiProcess = null;
  }
  
  public AutoDetectResponse autoDetect() throws Exception
  {
    check();
    AutoDetectResponse response = null;
    
    ResponseEntity<AutoDetectResponse> entity = restClient
      .get()
      .uri("/auto-detect")
      .retrieve().toEntity(AutoDetectResponse.class);
    
    if (entity.getStatusCode().is2xxSuccessful())
    {
      response = entity.getBody();
      log.debug("auto-detect={}", response);
    }
    else
    {
      log.error("Auto detect request NOT successful.");
    }
    
    return response;
  }
  
  public boolean checkCamera() throws Exception
  {
    log.debug("check for camera: {}", gphotoWebApiCamera);
    AutoDetectResponse autoDetected = autoDetect();
    return (autoDetected.getConnections().length == 1
      && autoDetected.getConnections()[0].getModel()
        .equalsIgnoreCase(gphotoWebApiCamera));
  }
  
  public CapturedImageResponse captureImageDownload()
  {
    CapturedImageResponse response = null;
    
    try
    {
      check();
      
      ResponseEntity<CapturedImageResponse> entity = restClient
        .get()
        .uri("/capture-image-download")
        .retrieve().toEntity(CapturedImageResponse.class);
      
      if (entity.getStatusCode().is2xxSuccessful())
      {
        response = entity.getBody();
        log.debug("capture-image-download={}", response);
      }
      else
      {
        log.error("capture image and download request NOT successful.");
      }
    }
    catch (Throwable t)
    {
      log.error("ERROR: during capture image and download.", t);
    }
    log.debug("response={}", response);
    return response;
  }
  
  private Process gphotoWebApiProcess;
}
