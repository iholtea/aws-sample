package com.myorg;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.s3.CfnBucket;
import software.amazon.awscdk.services.s3.CfnBucket.PublicAccessBlockConfigurationProperty;
import software.amazon.awscdk.services.s3.CfnBucketProps;
import software.constructs.Construct;

/**
 * Stack containing Level 1 constructs
 * Added as an example, not to be actually deployed
 */
public class Lvl1ConstructsStack extends Stack {
	
	public Lvl1ConstructsStack(final Construct scope, final String id, final StackProps props) {
		
		super(scope,id,props);
		
		PublicAccessBlockConfigurationProperty accessConfig =
			PublicAccessBlockConfigurationProperty.builder()
				.restrictPublicBuckets(true)
				.ignorePublicAcls(true)
				.blockPublicAcls(true)
				.blockPublicPolicy(true)
				.build();
		
		CfnBucketProps bucketProps = CfnBucketProps.builder()
			.bucketName("lvl1-construct-bucket-iholtea")
			.publicAccessBlockConfiguration(accessConfig)
			.build();
		
		CfnBucket cfnBucket = new CfnBucket(this, "L1S3Bucket", bucketProps);
		cfnBucket.applyRemovalPolicy(RemovalPolicy.DESTROY);
	}

}
