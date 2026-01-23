const UserService = require('../services/userService');
const { successResponse, errorResponse } = require('../utils/response');
const { handleError } = require('../utils/errorHandler');

const userService = new UserService();

async function updateUser(event, context) {
  console.log('Updating user with event:', JSON.stringify(event));

  try {
    const pathParameters = event.pathParameters || {};
    const userId = pathParameters.id;

    if (!userId) {
      return errorResponse(400, 'User ID is required');
    }

    const id = parseInt(userId, 10);
    if (isNaN(id)) {
      return errorResponse(400, 'User ID must be a number');
    }

    const body = event.body ? JSON.parse(event.body) : {};

    if (!body.nombre || !body.email) {
      return errorResponse(400, 'Nombre and email are required');
    }

    const user = userService.updateUser(id, body.nombre, body.email);
    return successResponse(200, user);
  } catch (error) {
    return handleError(error);
  }
}

module.exports = { updateUser };
