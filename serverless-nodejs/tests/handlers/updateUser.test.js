const { updateUser } = require('../../src/handlers/updateUser');

describe('updateUser handler', () => {
  test('should update a user successfully', async () => {
    const event = {
      pathParameters: {
        id: '1'
      },
      body: JSON.stringify({
        nombre: 'Updated Name',
        email: 'updated@test.com'
      })
    };

    const response = await updateUser(event, {});

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    expect(body.id).toBe(1);
    expect(body.nombre).toBe('Updated Name');
    expect(body.email).toBe('updated@test.com');
  });

  test('should return 404 if user not found', async () => {
    const event = {
      pathParameters: {
        id: '999'
      },
      body: JSON.stringify({
        nombre: 'Updated Name',
        email: 'updated@test.com'
      })
    };

    const response = await updateUser(event, {});

    expect(response.statusCode).toBe(404);
  });

  test('should return 400 if email is already taken', async () => {
    const event = {
      pathParameters: {
        id: '2'
      },
      body: JSON.stringify({
        nombre: 'Updated Name',
        email: 'juan.perez@bancolombia.com'
      })
    };

    const response = await updateUser(event, {});

    expect(response.statusCode).toBe(400);
  });

  test('should return 400 if body is missing', async () => {
    const event = {
      pathParameters: {
        id: '1'
      },
      body: ''
    };

    const response = await updateUser(event, {});

    expect(response.statusCode).toBe(400);
  });

  test('should return 400 if ID is not a number', async () => {
    const event = {
      pathParameters: {
        id: 'invalid'
      },
      body: JSON.stringify({
        nombre: 'Updated Name',
        email: 'updated@test.com'
      })
    };

    const response = await updateUser(event, {});

    expect(response.statusCode).toBe(400);
  });
});
