const { snsEmailService } = require('../services/serviceRegistry');

async function enviarCorreos(event, context) {
  console.log('Processing', event.Records.length, 'messages from SQS');

  const failures = [];

  for (const record of event.Records) {
    try {
      await processMessage(record, context);
    } catch (error) {
      console.error('Failed to process message:', record.messageId, '- Error:', error.message);
      failures.push({
        itemIdentifier: record.messageId
      });
    }
  }

  return {
    batchItemFailures: failures
  };
}

async function processMessage(record, context) {
  console.log('Processing message:', record.messageId);

  const event = JSON.parse(record.body);

  if (!event.eventType) {
    throw new Error(`Event type cannot be null for message: ${record.messageId}`);
  }

  if (event.eventType !== 'USER_CREATED') {
    console.log('Ignoring event type:', event.eventType, 'for message:', record.messageId);
    return;
  }

  const user = event.data;
  if (!user || !user.email) {
    throw new Error(`User data is invalid for message: ${record.messageId}`);
  }

  console.log('Sending welcome email to:', user.email);
  await snsEmailService.sendWelcomeEmail(user);
  console.log('Successfully processed message for user:', user.id);
}

module.exports = { enviarCorreos };
