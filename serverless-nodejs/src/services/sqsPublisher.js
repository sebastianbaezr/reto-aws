const { SQSClient, SendMessageCommand } = require('@aws-sdk/client-sqs');

class SqsPublisher {
  constructor(sqsClient, queueUrl) {
    if (!sqsClient) {
      throw new Error('sqsClient must not be null');
    }
    if (!queueUrl) {
      throw new Error('queueUrl must not be null');
    }

    this.sqsClient = sqsClient;
    this.queueUrl = queueUrl;
  }

  async publishUserCreatedEvent(user, requestId) {
    try {
      const event = {
        eventType: 'USER_CREATED',
        timestamp: new Date().toISOString(),
        data: {
          id: user.id,
          nombre: user.nombre,
          email: user.email
        },
        metadata: {
          source: 'createUserNode',
          version: '1.0',
          requestId: requestId
        }
      };

      const command = new SendMessageCommand({
        QueueUrl: this.queueUrl,
        MessageBody: JSON.stringify(event)
      });

      const response = await this.sqsClient.send(command);
      console.log('Message sent to SQS:', response.MessageId);
      return response;
    } catch (error) {
      console.error('Failed to publish message to SQS:', error);
      throw new Error(`Failed to publish message to SQS: ${error.message}`);
    }
  }
}

module.exports = SqsPublisher;
