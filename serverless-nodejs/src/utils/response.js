function createResponse(statusCode, body) {
  return {
    statusCode,
    body: typeof body === 'string' ? body : JSON.stringify(body),
    headers: {
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Headers': 'Content-Type,Authorization,X-Amz-Date,X-Api-Key,X-Amz-Security-Token',
      'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE,OPTIONS'
    }
  };
}

function successResponse(statusCode, data) {
  return createResponse(statusCode, data);
}

function errorResponse(statusCode, message) {
  return createResponse(statusCode, {
    error: message
  });
}

function messageResponse(statusCode, message) {
  return createResponse(statusCode, {
    message
  });
}

module.exports = {
  createResponse,
  successResponse,
  errorResponse,
  messageResponse
};
