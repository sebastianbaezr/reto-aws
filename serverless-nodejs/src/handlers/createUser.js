const UserService = require('../services/userService');
const { sqsPublisher } = require('../services/serviceRegistry');
const { successResponse, errorResponse } = require('../utils/response');
const { handleError } = require('../utils/errorHandler');

const userService = new UserService();

async function createUser(event, context) {
  console.log('Creating user with event:', JSON.stringify(event));
  const requestId = context.awsRequestId;

  try {
    const body = event.body ? JSON.parse(event.body) : {};

    if (!body.nombre || !body.email) {
      return errorResponse(400, 'Nombre and email are required');
    }

    const user = await userService.createUser(body.nombre, body.email);

    try {
      await sqsPublisher.publishUserCreatedEvent(user, requestId);
      console.log('User created event published to SQS for user:', user.id);
    } catch (sqsError) {
      console.warn('WARNING: Failed to publish SQS message:', sqsError.message);
    }

    return successResponse(201, user);
  } catch (error) {
    return handleError(error);
  }
}

module.exports = { createUser };
