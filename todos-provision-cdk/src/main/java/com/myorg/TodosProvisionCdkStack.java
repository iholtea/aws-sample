package com.myorg;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.constructs.Construct;

public class TodosProvisionCdkStack extends Stack {
    
	public TodosProvisionCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public TodosProvisionCdkStack(final Construct scope, final String id, final StackProps props) {
        
    	super(scope, id, props);
    	
    	createWebsiteBucket();
    	
    }
    
    private Bucket createWebsiteBucket() {
    	
    	// check https://docs.aws.amazon.com/cdk/api/v2/java/software/amazon/awscdk/services/s3/package-summary.html
    	// after an update to how access is granted to S3 buckets
    	// we need disable also the public access using a BlockPublicAccess object
    	// just calling publicReadAccess(true) is not enough
    	// it will result in conflicting policy and cdk deploy will throw an error
    	BlockPublicAccess bpa = BlockPublicAccess.Builder.create()
    			.blockPublicPolicy(false)
    			.blockPublicAcls(false)
    			.restrictPublicBuckets(false)
    			.build();
    	
    	// calling websiteIndexDocument() aslo enables static website hosting
    	BucketProps bucketProps = BucketProps.builder()
    			.bucketName("cdk-todo-site-iholtea")
    			.blockPublicAccess(bpa)
    			.publicReadAccess(true)
    			.websiteIndexDocument("index.html")
    			.removalPolicy(RemovalPolicy.DESTROY)
    			.autoDeleteObjects(true)
    			.build();
    	
    	return new Bucket(this, "TodoBucketId", bucketProps);
    }
}
