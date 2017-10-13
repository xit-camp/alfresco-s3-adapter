package org.redpill.alfresco.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import java.io.IOException;
import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.alfresco.service.cmr.repository.ContentStreamListener;

public class S3ContentReader extends AbstractContentReader implements AutoCloseable {

  private static final Log LOG = LogFactory.getLog(S3ContentReader.class);

  private final String key;
  private final AmazonS3 client;
  private final String bucketName;
  private S3Object fileObject;
  private ObjectMetadata fileObjectMetadata;

  /**
   * @param key the key to use when looking up data
   * @param client the s3 client to use for the connection
   * @param contentUrl the content URL - this should be relative to the root of
   * the store
   * @param bucketName the s3 bucket name
   */
  protected S3ContentReader(String key, String contentUrl, AmazonS3 client, String bucketName) {
    super(contentUrl);
    this.key = key;
    this.client = client;
    this.bucketName = bucketName;
    //Do not initialize the s3 object on reader init. Use lazy initalization
  }

  /**
   * Close file object
   *
   * @throws IOException Throws exception on error
   */
  protected void closeFileObject() throws IOException {
    if (fileObject != null) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Closing s3 file object for reader " + key);
      }
      fileObject.close();
      fileObject = null;
    }
  }

  /**
   * Lazy initialize the file object
   */
  protected void lazyInitFileObject() {
    if (fileObject == null) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Lazy init for file object for " + bucketName + " - " + key);
      }
      this.fileObject = getObject();
    }
  }

  /**
   * Lazy initialize the file metadata
   */
  protected void lazyInitFileMetadata() {
    if (fileObjectMetadata == null) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Lazy init for file metadata for " + bucketName + " - " + key);
      }
      boolean resetFileObject = false;
      if (fileObject == null) {
        resetFileObject = true;
      }
      lazyInitFileObject();
      try {
        try {
          this.fileObjectMetadata = getObjectMetadata(this.fileObject);
        } finally {
          if (resetFileObject) {
            closeFileObject();
          }
        }
      } catch (IOException e) {
        throw new ContentIOException("Error fetching object metadata", e);
      }
    }
  }

  @Override
  protected ContentReader createReader() throws ContentIOException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Called createReader for contentUrl -> " + getContentUrl() + ", Key: " + key);
    }
    return new S3ContentReader(key, getContentUrl(), client, bucketName);
  }

  @Override
  protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException {
    lazyInitFileObject();
    if (!exists()) {
      throw new ContentIOException("Content object does not exist on S3");
    }

    try {
      //We need to close the s3 object so to ensure that the thread pools are updated
      ContentStreamListener s3StreamListener = new ContentStreamListener() {
        @Override
        public void contentStreamClosed() throws ContentIOException {
          try {
            LOG.trace("Closing s3 object stream on content stream closed.");
            closeFileObject();
          } catch (IOException e) {
            throw new ContentIOException("Failed to close underlying s3 object", e);
          }
        }
      };
      this.addListener(s3StreamListener);
      return Channels.newChannel(fileObject.getObjectContent());
    } catch (Exception e) {
      throw new ContentIOException("Unable to retrieve content object from S3", e);
    }

  }

  @Override
  public boolean exists() {
    lazyInitFileMetadata();
    return fileObjectMetadata != null;
  }

  @Override
  public long getLastModified() {
    lazyInitFileMetadata();
    if (!exists()) {
      return 0L;
    }

    return fileObjectMetadata.getLastModified().getTime();

  }

  @Override
  public long getSize() {
    lazyInitFileMetadata();
    if (!exists()) {
      return 0L;
    }

    return fileObjectMetadata.getContentLength();
  }

  private S3Object getObject() {

    S3Object object = null;

    try {
      LOG.debug("GETTING OBJECT - BUCKET: " + bucketName + " KEY: " + key);
      object = client.getObject(bucketName, key);
    } catch (Exception e) {
      LOG.error("Unable to fetch S3 Object", e);
    }

    return object;
  }

  private ObjectMetadata getObjectMetadata(S3Object object) {

    ObjectMetadata metadata = null;

    if (object != null) {
      metadata = object.getObjectMetadata();
    }

    return metadata;

  }

  @Override
  public void close() throws Exception {
    closeFileObject();
  }
}
