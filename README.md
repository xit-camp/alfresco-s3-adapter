# Alfresco S3 Adapter

This project is meant to provide tools to use s3 as a storage solution for Alfresco. It can be configured to work as a main content store if you are a community user. If you are an enterprise customer you can configure it to work with content store selector or as a main content store.

History

 * Forked from https://github.com/rmberg/alfresco-s3-adapter which originally was migrated from the `alfresco-cloud-store` project at https://code.google.com/p/alfresco-cloud-store/.
 * Apache License 2.0
 * Uses Amazon SDK 1.x
 * Uses Alfresco SDK 3.0 and tested with Alfresco 5.2
 * Pull Requests / Issues / Contributions are welcomed!
 * Use Findify s3mock for testing
 



Build Instructions

 * After cloning the project, run `mvn clean install` to download dependencies and build the project

Installation / Configuration

 * After installing the `alfresco-s3-adapter.amp` package you will need to add some properties to your `alfresco-global.properties` file:
 
```
# Your AWS credentials
# Alternatively these can be set in the standard locations the AWS SDK will search for them
# For example: if you are running on an EC2 instance and are using IAM roles, you can leave these blank and the credentials
# for the role will be used.
aws.accessKey=
aws.secretKey=

# The AWS Region (US-EAST-1) will be used by default if not specified
aws.regionName=us-east-1

# The S3 bucket name to use as the content store
aws.s3.bucketName=

# The endpoint url if other than AWS (for other S3-compatible vendors)
aws.s3.endpoint=

# The relative path (S3 KEY) within the bucket to use as the content store (useful if the bucket is not dedicated to alfresco content)
aws.s3.rootDirectory=/alfresco/contentstore

# The cache size
defaultS3QuotaManager.maxUsageMB=4096
# The max file size in MB to store in cache 0 means no limit
defaultS3QuotaManager.maxFileSizeMB=0
# Content cache dir
defaultS3ContentCache.cachedcontent=/tmp/cachedcontent
```
 
 
