package edu.upenn.cis.cis455.crawler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class Testsqs {

	public static void main(String[] args) {
		AWSCredentials credentials = new BasicAWSCredentials(
	  			  "AKIA4QTDU5NHBULGHQNC", 
	  			  "HcX4yk7FQ8pTOR6RQx5Y7NFxUq4j+drR2JPXzUIn"
	  			);
	    	
    	AmazonSQS sqs = AmazonSQSClientBuilder.standard()
    			  .withCredentials(new AWSStaticCredentialsProvider(credentials))
    			  .withRegion(Regions.US_EAST_1)
    			  .build();
	    	
		Map<String, String> queueAttributes = new HashMap<>();
		queueAttributes.put("FifoQueue", "true");
		queueAttributes.put("ContentBasedDeduplication", "true");
		CreateQueueRequest createFifoQueueRequest = new CreateQueueRequest(
		  "baeldung-queue4.fifo").withAttributes(queueAttributes);
		String fifoQueueUrl = sqs.createQueue(createFifoQueueRequest)
		  .getQueueUrl();
//		
//		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
//		messageAttributes.put("AttributeOne", new MessageAttributeValue()
//		  .withStringValue("This is an attribute")
//		  .withDataType("String"));

		SendMessageRequest sendMessageFifoQueue = new SendMessageRequest()
				  .withQueueUrl(fifoQueueUrl)
				  .withMessageBody("Hello world2.")
				  .withMessageGroupId("baeldung-group-1");
		sqs.sendMessage(sendMessageFifoQueue);
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(fifoQueueUrl)
				  .withWaitTimeSeconds(10)
				  .withMaxNumberOfMessages(10);

		List<Message> sqsMessages = sqs.receiveMessage(receiveMessageRequest).getMessages();
		System.out.println(sqsMessages.get(0).getBody());
		System.out.println(sqsMessages.get(1).getBody());
	}

}
