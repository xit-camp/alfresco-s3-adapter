package org.redpill.alfresco.s3;

import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * Stream listener which is used to copy the temp file contents into S3
 */
public class S3StreamListener implements ContentStreamListener {

  private static final Log logger = LogFactory.getLog(S3StreamListener.class);

  private S3ContentWriter writer;

  public S3StreamListener(S3ContentWriter writer) {

    this.writer = writer;

  }

  @Override
  public void contentStreamClosed() throws ContentIOException {

    File file = writer.getTempFile();
    long size = file.length();
    writer.setSize(size);

    logger.debug("Writing to s3://" + writer.getBucketName() + "/" + writer.getKey());
    TransferManager transferManager = writer.getTransferManager();
    Upload upload = transferManager.upload(writer.getBucketName(), writer.getKey(), writer.getTempFile());
    //To have transactional consistency it is necessary to wait for the upload to go through before allowing the transaction to commit!
    try {
      upload.waitForUploadResult();
      //TODO Here we have a chance to validate MD5 sum of the file by calculating md5 for the temp file and compare it to the Etag that the upload returns
      // Ref: http://docs.aws.amazon.com/AmazonS3/latest/API/RESTObjectPUT.html
    } catch (Exception e) {
      throw new ContentIOException("S3StreamListener Failed to Upload File", e);
    } finally {
      //Remove the temp file
      writer.getTempFile().delete();
    }

  }
}
