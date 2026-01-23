const { getUser } = require('../../src/handlers/getUser');

describe('getUser handler', () => {
  test('should get a user successfully', async () => {
    const event = {
      pathParameters: {
        id: '1'
      }
    };

    const response = await getUser(event, {});

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    expect(body.id).toBe(1);
    expect(body.nombre).toBe('Juan Pérez');
    expect(body.email).toBe('juan.perez@bancolombia.com');
  });

  test('should return 404 if user not found', async () => {
    const event = {
      pathParameters: {
        id: '999'
      }
    };

    const response = await getUser(event, {});

    expect(response.statusCode).toBe(404);
    const body = JSON.parse(response.body);
    expect(body.error).toContain('User not found');
  });

  test('should return 400 if ID is missing', async () => {
    const event = {
      pathParameters: {}
    };

    const response = await getUser(event, {});

    expect(response.statusCode).toBe(400);
  });

  test('should return 400 if ID is not a number', async () => {
    const event = {
      pathParameters: {
        id: 'invalid'
      }
    };

    const response = await getUser(event, {});

    expect(response.statusCode).toBe(400);
  });

  test('should return 400 if pathParameters is null', async () => {
    const event = {
      pathParameters: null
    };

    const response = await getUser(event, {});

    expect(response.statusCode).toBe(400);
  });
});
