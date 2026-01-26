const { SNSClient, PublishCommand } = require('@aws-sdk/client-sns');

class SnsEmailService {
  constructor(snsClient, topicArn) {
    if (!snsClient) {
      throw new Error('snsClient must not be null');
    }
    if (!topicArn) {
      throw new Error('topicArn must not be null');
    }

    this.snsClient = snsClient;
    this.topicArn = topicArn;
  }

  async sendWelcomeEmail(user) {
    try {
      const subject = 'Welcome to Our Platform!';
      const message = this.buildWelcomeMessage(user);

      const command = new PublishCommand({
        TopicArn: this.topicArn,
        Subject: subject,
        Message: message,
        MessageAttributes: {
          userId: {
            DataType: 'String',
            StringValue: user.id
          },
          email: {
            DataType: 'String',
            StringValue: user.email
          }
        }
      });

      const response = await this.snsClient.send(command);
      console.log('Email notification sent. MessageId:', response.MessageId);
      return response;
    } catch (error) {
      console.error('Failed to send email via SNS:', error);
      throw new Error(`Failed to send email via SNS: ${error.message}`);
    }
  }

  buildWelcomeMessage(user) {
    return `Hello ${user.nombre},

Welcome to our platform! Your account has been successfully created.

Email: ${user.email}
User ID: ${user.id}

Best regards,
The Team`;
  }
}

module.exports = SnsEmailService;
