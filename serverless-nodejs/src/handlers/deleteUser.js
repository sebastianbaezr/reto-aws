const UserService = require('../services/userService');
const { messageResponse, errorResponse } = require('../utils/response');
const { handleError } = require('../utils/errorHandler');

const userService = new UserService();

async function deleteUser(event, context) {
  console.log('Deleting user with event:', JSON.stringify(event));

  try {
    const pathParameters = event.pathParameters || {};
    const userId = pathParameters.id;

    if (!userId) {
      return errorResponse(400, 'User ID is required');
    }

    const result = await userService.deleteUser(userId);
    return messageResponse(200, result.message);
  } catch (error) {
    return handleError(error);
  }
}

module.exports = { deleteUser };
