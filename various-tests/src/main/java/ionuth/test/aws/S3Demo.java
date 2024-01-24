package ionuth.test.aws;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;  

public class S3Demo {

    private static final String BUCKET_NAME = "test-usage-iholtea";
	
    private final S3Client s3Client;
    
    public S3Demo() {
      s3Client = S3Client.builder()
          .region(Region.US_EAST_1)
          .credentialsProvider(DefaultCredentialsProvider.create())
          .build();
    }
    
    private void listBuckets() {
        
        System.out.println("");
        System.out.println("Bucket List");
        System.out.println("-------");
        ListBucketsResponse listResp = s3Client.listBuckets();
        listResp.buckets().forEach( bucket -> 
            System.out.println( "Bucket: " + bucket.name() + " created at: " + bucket.creationDate() )
        );
      
    }
    
    private void listBucketContent(String bucketName) {
        ListObjectsRequest listRequest = ListObjectsRequest.builder()
            .bucket(bucketName).build();
        System.out.println("");
        System.out.println("Contents of bucket: " + bucketName);
        System.out.println("-------");
        ListObjectsResponse listResponse = s3Client.listObjects(listRequest);
        listResponse.contents().forEach( s3Object -> {
          String strResult = "Object name: " + s3Object.key();
          strResult += " , owner: " + s3Object.owner().displayName();
          strResult += " , size: " +  s3Object.size() + " bytes";
          System.out.println(strResult);
        });
    }
    
    public static void main(String[] args) {
      
        S3Demo s3Demo = new S3Demo();
        s3Demo.listBuckets();
        s3Demo.listBucketContent(BUCKET_NAME);
      
    }                       

}
