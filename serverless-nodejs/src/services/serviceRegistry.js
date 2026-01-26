const { SQSClient } = require('@aws-sdk/client-sqs');
const { SNSClient } = require('@aws-sdk/client-sns');
const SqsPublisher = require('./sqsPublisher');
const SnsEmailService = require('./snsEmailService');

const region = process.env.AWS_REGION || 'us-east-1';
const queueUrl = process.env.USER_CREATED_QUEUE_URL;
const topicArn = process.env.EMAIL_NOTIFICATION_TOPIC_ARN;

if (!queueUrl) {
  throw new Error('USER_CREATED_QUEUE_URL environment variable must be set');
}

if (!topicArn) {
  throw new Error('EMAIL_NOTIFICATION_TOPIC_ARN environment variable must be set');
}

const sqsClient = new SQSClient({ region });
const snsClient = new SNSClient({ region });

const sqsPublisher = new SqsPublisher(sqsClient, queueUrl);
const snsEmailService = new SnsEmailService(snsClient, topicArn);

module.exports = {
  sqsPublisher,
  snsEmailService
};
