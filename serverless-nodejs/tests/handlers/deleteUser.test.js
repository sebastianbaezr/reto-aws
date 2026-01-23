const { deleteUser } = require('../../src/handlers/deleteUser');

describe('deleteUser handler', () => {
  test('should delete a user successfully', async () => {
    const event = {
      pathParameters: {
        id: '1'
      }
    };

    const response = await deleteUser(event, {});

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    expect(body.message).toBe('User deleted successfully');
  });

  test('should return 404 if user not found', async () => {
    const event = {
      pathParameters: {
        id: '999'
      }
    };

    const response = await deleteUser(event, {});

    expect(response.statusCode).toBe(404);
  });

  test('should return 400 if ID is missing', async () => {
    const event = {
      pathParameters: {}
    };

    const response = await deleteUser(event, {});

    expect(response.statusCode).toBe(400);
  });

  test('should return 400 if ID is not a number', async () => {
    const event = {
      pathParameters: {
        id: 'invalid'
      }
    };

    const response = await deleteUser(event, {});

    expect(response.statusCode).toBe(400);
  });

  test('should return 400 if pathParameters is null', async () => {
    const event = {
      pathParameters: null
    };

    const response = await deleteUser(event, {});

    expect(response.statusCode).toBe(400);
  });
});
