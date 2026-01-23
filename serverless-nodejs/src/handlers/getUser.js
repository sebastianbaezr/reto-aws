const UserService = require('../services/userService');
const { successResponse, errorResponse } = require('../utils/response');
const { handleError } = require('../utils/errorHandler');

const userService = new UserService();

async function getUser(event, context) {
  console.log('Getting user with event:', JSON.stringify(event));

  try {
    const pathParameters = event.pathParameters || {};
    const userId = pathParameters.id;

    if (!userId) {
      return errorResponse(400, 'User ID is required');
    }

    const user = await userService.getUser(userId);
    return successResponse(200, user);
  } catch (error) {
    return handleError(error);
  }
}

module.exports = { getUser };
