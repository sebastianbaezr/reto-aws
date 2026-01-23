class LambdaError extends Error {
  constructor(message, statusCode) {
    super(message);
    this.name = this.constructor.name;
    this.statusCode = statusCode;
    Error.captureStackTrace(this, this.constructor);
  }
}

class BadRequestError extends LambdaError {
  constructor(message) {
    super(message, 400);
  }
}

class NotFoundError extends LambdaError {
  constructor(message) {
    super(message, 404);
  }
}

class InternalServerError extends LambdaError {
  constructor(message = 'Internal server error') {
    super(message, 500);
  }
}

function handleError(error) {
  console.error('Error:', error);

  if (error instanceof LambdaError) {
    return {
      statusCode: error.statusCode,
      body: JSON.stringify({
        error: error.message
      })
    };
  }

  if (error instanceof SyntaxError) {
    return {
      statusCode: 400,
      body: JSON.stringify({
        error: 'Invalid JSON format'
      })
    };
  }

  return {
    statusCode: 500,
    body: JSON.stringify({
      error: 'Internal server error'
    })
  };
}

module.exports = {
  LambdaError,
  BadRequestError,
  NotFoundError,
  InternalServerError,
  handleError
};
